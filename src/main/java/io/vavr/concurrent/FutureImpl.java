/* ____  ______________  ________________________  __________
 * \   \/   /      \   \/   /   __/   /      \   \/   /      \
 *  \______/___/\___\______/___/_____/___/\___\______/___/\___\
 *
 * Copyright 2021 Vavr, https://vavr.io
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vavr.concurrent;

import io.vavr.collection.Queue;
import io.vavr.control.Option;
import io.vavr.control.Try;

import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

/**
 * <strong>INTERNAL API - This class is subject to change.</strong>
 *
 * @param <T> Result of the computation.
 */
@SuppressWarnings("deprecation")
final class FutureImpl<T> implements Future<T> {

    /**
     * Used to start new threads.
     */
    private final Executor executor;

    /**
     * Used to synchronize state changes.
     */
    private final Object lock = new Object();

    /**
     * Indicates if this Future is cancelled
     *
     * GuardedBy("lock")
     */
    private volatile boolean cancelled;

    /**
     * Once the Future is completed, the value is defined.
     *
     * GuardedBy("lock")
     */
    private volatile Option<Try<T>> value;

    /**
     * The queue of actions is filled when calling onComplete() before the Future is completed or cancelled.
     * Otherwise actions = null.
     *
     * GuardedBy("lock")
     */
    private Queue<Consumer<Try<T>>> actions;

    /**
     * The queue of waiters is filled when calling await() before the Future is completed or cancelled.
     * Otherwise waiters = null.
     *
     * GuardedBy("lock")
     */
    private Queue<Thread> waiters;

    /**
     * The Thread which runs the computation.
     *
     * GuardedBy("lock")
     */
    private Thread thread;


    // single constructor
    private FutureImpl(Executor executor, Option<Try<T>> value, Queue<Consumer<Try<T>>> actions, Queue<Thread> waiters, Computation<T> computation) {
        this.executor = executor;
        synchronized (lock) {
            this.cancelled = false;
            this.value = value;
            this.actions = actions;
            this.waiters = waiters;
            try {
                computation.execute(this::tryComplete, this::updateThread);
            } catch (Throwable x) {
                tryComplete(Try.failure(x));
            }
        }
    }

    /**
     * Creates a {@code FutureImpl} that needs to be automatically completed by calling {@link #tryComplete(Try)}.
     *
     * @param executor An {@link Executor} to run and control the computation and to perform the actions.
     * @param <T> value type of the Future
     * @return a new {@code FutureImpl} instance
     */
    static <T> FutureImpl<T> of(Executor executor) {
        return new FutureImpl<>(executor, Option.none(), Queue.empty(), Queue.empty(), (complete, updateThread) -> {});
    }

    /**
     * Creates a {@code FutureImpl} that is immediately completed with the given value. No task will be started.
     *
     * @param executor An {@link Executor} to run and control the computation and to perform the actions.
     * @param value the result of this Future
     * @param <T> value type of the Future
     * @return a new {@code FutureImpl} instance
     */
    static <T> FutureImpl<T> of(Executor executor, Try<? extends T> value) {
        return new FutureImpl<>(executor, Option.some(Try.narrow(value)), null, null, (complete, updateThread) -> {});
    }

    /**
     * Creates a {@code FutureImpl} that is eventually completed.
     * The given {@code computation} is <em>synchronously</em> executed, no thread is started.
     *
     * @param executor An {@link Executor} to run and control the computation and to perform the actions.
     * @param task     A non-blocking computation
     * @param <T>      value type of the Future
     * @return a new {@code FutureImpl} instance
     */
    static <T> FutureImpl<T> sync(Executor executor, Task<? extends T> task) {
        return new FutureImpl<>(executor, Option.none(), Queue.empty(), Queue.empty(), (complete, updateThread) ->
            task.run(complete::with)
        );
    }

    /**
     * Creates a {@code FutureImpl} that is eventually completed.
     * The given {@code computation} is <em>asynchronously</em> executed, a new thread is started.
     *
     * @param executor An {@link Executor} to run and control the computation and to perform the actions.
     * @param task     A (possibly blocking) computation
     * @param <T>      value type of the Future
     * @return a new {@code FutureImpl} instance
     */
    static <T> FutureImpl<T> async(Executor executor, Task<? extends T> task) {
        // In a single-threaded context this Future may already have been completed during initialization.
        return new FutureImpl<>(executor, Option.none(), Queue.empty(), Queue.empty(), (complete, updateThread) ->
                executor.execute(() -> {
                    updateThread.run();
                    try {
                        task.run(complete::with);
                    } catch (Throwable x) {
                        complete.with(Try.failure(x));
                    }
                })
        );
    }

    @Override
    public Future<T> await() {
        if (!isCompleted()) {
            _await(-1L, -1L, null);
        }
        return this;
    }

    @Override
    public Future<T> await(long timeout, TimeUnit unit) {
        final long now = System.nanoTime();
        Objects.requireNonNull(unit, "unit is null");
        if (timeout < 0) {
            throw new IllegalArgumentException("negative timeout");
        }
        if (!isCompleted()) {
            _await(now, timeout, unit);
        }
        return this;
    }

    /**
     * Blocks the current thread.
     * <p>
     * If timeout = 0 then {@code LockSupport.park()} is called (start, timeout and unit are not used),
     * otherwise {@code LockSupport.park(timeout, unit}} is called.
     * <p>
     * If a timeout > -1 is specified and the deadline is not met, this Future fails with a {@link TimeoutException}.
     * <p>
     * If this Thread was interrupted, this Future fails with a {@link InterruptedException}.
     *
     * @param start   the start time in nanos, based on {@linkplain System#nanoTime()}
     * @param timeout a timeout in the given {@code unit} of time
     * @param unit    a time unit
     */
    private void _await(long start, long timeout, TimeUnit unit) {
        try {
            ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker() {

                final long duration = (unit == null) ? -1 : unit.toNanos(timeout);
                final Thread waitingThread = Thread.currentThread();

                boolean threadEnqueued = false;

                /**
                 * Parks the Future's thread.
                 * <p>
                 * LockSupport.park() / parkNanos() may return when the Thread is permitted to be scheduled again.
                 * If so, the Future's tryComplete() method wasn't called yet. In that case the block() method is
                 * called again. The remaining timeout is recalculated accordingly.
                 *
                 * @return true, if this Future is completed, false otherwise
                 */
                @Override
                public boolean block() {
                    try {
                        if (!threadEnqueued) {
                            synchronized (lock) {
                                waiters = waiters.enqueue(waitingThread);
                            }
                            threadEnqueued = true;
                        }
                        if (timeout > -1) {
                            final long delta = System.nanoTime() - start;
                            final long remainder = duration - delta;
                            LockSupport.parkNanos(remainder); // returns immediately if remainder <= 0
                            if (System.nanoTime() - start > duration) {
                                tryComplete(Try.failure(new TimeoutException("timeout after " + timeout + " " + unit.name().toLowerCase())));
                            }
                        } else {
                            LockSupport.park();
                        }
                        if (waitingThread.isInterrupted()) {
                            tryComplete(Try.failure(new ExecutionException(new InterruptedException())));
                        }
                    } catch (Throwable x) {
                        tryComplete(Try.failure(x));
                    }
                    return isCompleted();
                }

                @Override
                public boolean isReleasable() {
                    return isCompleted();
                }
            });
        } catch (Throwable x) {
            tryComplete(Try.failure(x));
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!isCompleted()) {
            synchronized (lock) {
                if (!isCompleted()) {
                    if (mayInterruptIfRunning && this.thread != null) {
                        this.thread.interrupt();
                    }
                    this.cancelled = tryComplete(Try.failure(new CancellationException()));
                    return this.cancelled;
                }
            }
        }
        return false;
    }

    private void updateThread() {
        // cancellation may have been initiated by a different thread before this.thread is set by the worker thread
        if (!isCompleted()) {
            synchronized (lock) {
                if (!isCompleted()) {
                    this.thread = Thread.currentThread();
                    try {
                        this.thread.setUncaughtExceptionHandler((thread, x) -> handleUncaughtException(x));
                    } catch (SecurityException x) {
                        // we are not allowed to set the uncaught exception handler of the worker thread ¯\_(ツ)_/¯
                    }
                }
            }
        }
    }

    @Override
    public Executor executor() {
        return executor;
    }

    @Deprecated
    @Override
    public ExecutorService executorService() {
        if (executor instanceof ExecutorService) {
            return (ExecutorService) executor;
        } else {
            throw new UnsupportedOperationException("Removed starting with Vavr 0.10.0, use executor() instead.");
        }
    }

    @Override
    public Option<Try<T>> getValue() {
        return value;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public boolean isCompleted() {
        return value.isDefined();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Future<T> onComplete(Consumer<? super Try<T>> action) {
        Objects.requireNonNull(action, "action is null");
        if (isCompleted()) {
            perform(action);
        } else {
            synchronized (lock) {
                if (isCompleted()) {
                    perform(action);
                } else {
                    actions = actions.enqueue((Consumer<Try<T>>) action);
                }
            }
        }
        return this;
    }

    // This class is MUTABLE and therefore CANNOT CHANGE DEFAULT equals() and hashCode() behavior.
    // See http://stackoverflow.com/questions/4718009/mutable-objects-and-hashcode

    @Override
    public String toString() {
        final Option<Try<T>> value = this.value;
        final String s = (value == null || value.isEmpty()) ? "?" : value.get().toString();
        return stringPrefix() + "(" + s + ")";
    }

    /**
     * INTERNAL METHOD, SHOULD BE USED BY THE CONSTRUCTOR, ONLY.
     * <p>
     * Completes this Future with a value and performs all actions.
     * <p>
     * This method is idempotent. I.e. it does nothing, if this Future is already completed.
     *
     * @param value A Success containing a result or a Failure containing an Exception.
     * @throws IllegalStateException if the Future is already completed or cancelled.
     * @throws NullPointerException  if the given {@code value} is null.
     */
    boolean tryComplete(Try<? extends T> value) {
        Objects.requireNonNull(value, "value is null");
        if (isCompleted()) {
            return false;
        } else {
            final Queue<Consumer<Try<T>>> actions;
            final Queue<Thread> waiters;
            // it is essential to make the completed state public *before* performing the actions
            synchronized (lock) {
                if (isCompleted()) {
                    actions = null;
                    waiters = null;
                } else {
                    actions = this.actions;
                    waiters = this.waiters;
                    this.value = Option.some(Try.narrow(value));
                    this.actions = null;
                    this.waiters = null;
                    this.thread = null;
                }
            }
            if (waiters != null) {
                waiters.forEach(this::unlock);
            }
            if(isCancelled()) {
                return true;
            }
            if (actions != null) {
                actions.forEach(this::perform);
                return true;
            } else {
                return false;
            }
        }
    }

    private void perform(Consumer<? super Try<T>> action) {
        try {
            executor.execute(() -> action.accept(value.get()));
        } catch (Throwable x) {
            handleUncaughtException(x);
        }
    }

    private void unlock(Thread waiter) {
        try {
            LockSupport.unpark(waiter);
        } catch (Throwable x) {
            handleUncaughtException(x);
        }
    }

    private void handleUncaughtException(Throwable x) {
        tryComplete(Try.failure(x));
    }

    private interface Computation<T> {
        void execute(Task.Complete<T> complete, Runnable updateThread) throws Throwable;
    }
}
