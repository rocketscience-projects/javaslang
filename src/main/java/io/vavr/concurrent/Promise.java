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

import io.vavr.control.Try;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;

import static io.vavr.concurrent.Future.DEFAULT_EXECUTOR;

/**
 * A Promise is a write-once wrapper around a read-only Future which can complete the underlying Future with a value
 * or an exception.
 * <p>
 * The underlying {@code Executor} is used to execute asynchronous handlers, e.g. via
 * {@code promise.future().onComplete(...)}.
 *
 * <h2>Creation</h2>
 * <p>
 * Promise offers static factory methods to create new promises which hasn't been fulfilled yet:
 * <ul>
 * <li>create new promises: {@link Promise#make()}</li>
 * </ul>
 * And we may create new promises that are already finished:
 * <ul>
 * <li>{@link #failed(Throwable)}</li>
 * <li>{@link #fromTry(Try)}</li>
 * <li>{@link #successful(Object)}</li>
 * </ul>
 * All the static factory methods mentioned above have additional versions which take an {@link Executor} as
 * argument. This gives us more control over thread creation and thread pool sizes.
 *
 * <h2>One-shot API</h2>
 * <p>
 * The main purpose of a {@code Promise} is to complete its underlying {@code Future}. When only a single {@code Thread}
 * will eventually complete the {@code Promise}, we use one of these methods. Calls will throw if the {@code Promise} is already
 * completed.
 * <ul>
 * <li>{@link #complete(Try)}</li>
 * <li>{@link #completeWith(Future)}</li>
 * <li>{@link #failure(Throwable)}</li>
 * <li>{@link #success(Object)}</li>
 * </ul>
 *
 * <h2>API for competing threads</h2>
 * <p>
 * When multiple {@code Thread}s may complete our {@code Promise}, we typically use one of these methods. Calls will
 * gracefully return {@code false} if the {@code Promise} is already completed.
 * <ul>
 * <li>{@link #tryComplete(Try)}</li>
 * <li>{@link #tryCompleteWith(Future)}</li>
 * <li>{@link #tryFailure(Throwable)}</li>
 * <li>{@link #trySuccess(Object)}</li>
 * </ul>
 *
 * @param <T> The result type of the underlying {@code Future}
 */
public interface Promise<T> {

    /**
     * Creates a failed {@code Promise}, backed by the {@link Future#DEFAULT_EXECUTOR}.
     *
     * @param exception The reason why it failed.
     * @param <T>       The value type of a successful result.
     * @return A failed {@code Promise}.
     * @throws NullPointerException if exception is null
     */
    static <T> Promise<T> failed(Throwable exception) {
        Objects.requireNonNull(exception, "exception is null");
        return failed(DEFAULT_EXECUTOR, exception);
    }

    /**
     * Creates a failed {@code Promise}, backed by the given {@link Executor}.
     *
     * @param executor An {@code Executor} passed to the underlying {@link Future}.
     * @param exception       The reason why it failed.
     * @param <T>             The value type of a successful result.
     * @return A failed {@code Promise}.
     * @throws NullPointerException if executor or exception is null
     */
    static <T> Promise<T> failed(Executor executor, Throwable exception) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(exception, "exception is null");
        return Promise.<T> make(executor).failure(exception);
    }

    /**
     * Creates a {@code Promise} from a {@link Try}, backed by the {@link Future#DEFAULT_EXECUTOR}.
     *
     * @param result The result.
     * @param <T>    The value type of a successful result.
     * @return A completed {@code Promise} which contains either a {@code Success} or a {@code Failure}.
     * @throws NullPointerException if result is null
     */
    static <T> Promise<T> fromTry(Try<? extends T> result) {
        return fromTry(DEFAULT_EXECUTOR, result);
    }

    /**
     * Creates a {@code Promise} from a {@link Try}, backed by the given {@link Executor}.
     *
     * @param executor An {@code Executor} passed to the underlying {@link Future}.
     * @param result          The result.
     * @param <T>             The value type of a successful result.
     * @return A completed {@code Promise} which contains either a {@code Success} or a {@code Failure}.
     * @throws NullPointerException if executor or result is null
     */
    static <T> Promise<T> fromTry(Executor executor, Try<? extends T> result) {
        Objects.requireNonNull(executor, "executor is null");
        Objects.requireNonNull(result, "result is null");
        return Promise.<T> make(executor).complete(result);
    }

    /**
     * Makes a {@code Promise} that isn't fulfilled yet, backed by the {@link Future#DEFAULT_EXECUTOR}.
     * {@link ForkJoinPool#commonPool()}.
     *
     * @param <T> Result type of the {@code Promise}.
     * @return A new {@code Promise}.
     */
    static <T> Promise<T> make() {
        return make(DEFAULT_EXECUTOR);
    }

    /**
     * Makes a {@code Promise} that isn't fulfilled yet, backed by the given {@link Executor}.
     *
     * @param executor An {@code Executor} passed to the underlying {@link Future}.
     * @param <T>             Result type of the {@code Promise}.
     * @return A new {@code Promise}.
     * @throws NullPointerException if executor is null
     */
    static <T> Promise<T> make(Executor executor) {
        Objects.requireNonNull(executor, "executor is null");
        return new PromiseImpl<>(FutureImpl.of(executor));
    }

    /**
     * Narrows a widened {@code Promise<? extends T>} to {@code Promise<T>}
     * by performing a type-safe cast. This is eligible because immutable/read-only
     * collections are covariant.
     *
     * @param promise A {@code Promise}.
     * @param <T>     Component type of the {@code Promise}.
     * @return the given {@code promise} instance as narrowed type {@code Promise<T>}.
     */
    @SuppressWarnings("unchecked")
    static <T> Promise<T> narrow(Promise<? extends T> promise) {
        return (Promise<T>) promise;
    }

    /**
     * Creates a succeeded {@code Promise}, backed by the {@link Future#DEFAULT_EXECUTOR}.
     *
     * @param result The result.
     * @param <T>    The value type of a successful result.
     * @return A succeeded {@code Promise}.
     */
    static <T> Promise<T> successful(T result) {
        return successful(DEFAULT_EXECUTOR, result);
    }

    /**
     * Creates a succeeded {@code Promise}, backed by the given {@link Executor}.
     *
     * @param executor An {@code Executor} passed to the underlying {@link Future}.
     * @param result          The result.
     * @param <T>             The value type of a successful result.
     * @return A succeeded {@code Promise}.
     * @throws NullPointerException if executor is null
     */
    static <T> Promise<T> successful(Executor executor, T result) {
        Objects.requireNonNull(executor, "executor is null");
        return Promise.<T> make(executor).success(result);
    }

    /**
     * Returns the {@link Executor} used by the underlying {@link Future} of this {@code Promise}.
     *
     * @return The underlying {@code Executor}.
     */
    Executor executor();

    /**
     * Returns the underlying {@link Future} of this {@code Promise}.
     *
     * @return The {@code Future}.
     */
    Future<T> future();

    /**
     * Checks if this {@code Promise} is completed, i.e. has a value.
     *
     * @return true, if the computation successfully finished or failed, false otherwise.
     */
    default boolean isCompleted() {
        return future().isCompleted();
    }

    /**
     * Completes this {@code Promise} with the given {@code value}.
     *
     * @param value Either a {@link Try.Success} containing the result or a {@link Try.Failure} containing an exception.
     * @return This {@code Promise}.
     * @throws IllegalStateException if this {@code Promise} has already been completed.
     */
    default Promise<T> complete(Try<? extends T> value) {
        if (tryComplete(value)) {
            return this;
        } else {
            throw new IllegalStateException("Promise already completed.");
        }
    }

    /**
     * Attempts to completes this {@code Promise} with the given {@code value}.
     *
     * @param value Either a {@link Try.Success} containing the result or a {@link Try.Failure} containing an exception.
     * @return {@code false} if this {@code Promise} has already been completed, {@code true} otherwise.
     * @throws IllegalStateException if this {@code Promise} has already been completed.
     */
    boolean tryComplete(Try<? extends T> value);

    /**
     * Completes this {@code Promise} with the given {@code Future}, once that {@code Future} is completed.
     *
     * @param other Another {@code Future} to react on.
     * @return This {@code Promise}.
     */
    default Promise<T> completeWith(Future<? extends T> other) {
        return tryCompleteWith(other);
    }

    /**
     * Attempts to complete this {@code Promise} with the specified {@code Future}, once that {@code Future} is completed.
     *
     * @param other Another {@code Future} to react on.
     * @return This {@code Promise}.
     */
    default Promise<T> tryCompleteWith(Future<? extends T> other) {
        other.onComplete(this::tryComplete);
        return this;
    }

    /**
     * Completes this {@code Promise} with the given {@code value}.
     *
     * @param value A value.
     * @return This {@code Promise}.
     * @throws IllegalStateException if this {@code Promise} has already been completed.
     */
    default Promise<T> success(T value) {
        return complete(Try.success(value));
    }

    /**
     * Completes this {@code Promise} with the given {@code value}.
     *
     * @param value A value.
     * @return {@code false} if this {@code Promise} has already been completed, {@code true} otherwise.
     */
    default boolean trySuccess(T value) {
        return tryComplete(Try.success(value));
    }

    /**
     * Completes this {@code Promise} with the given {@code exception}.
     *
     * @param exception An exception.
     * @return This {@code Promise}.
     * @throws IllegalStateException if this {@code Promise} has already been completed.
     */
    default Promise<T> failure(Throwable exception) {
        return complete(Try.failure(exception));
    }

    /**
     * Completes this {@code Promise} with the given {@code exception}.
     *
     * @param exception An exception.
     * @return {@code false} if this {@code Promise} has already been completed, {@code true} otherwise.
     */
    default boolean tryFailure(Throwable exception) {
        return tryComplete(Try.failure(exception));
    }
}

/**
 * Internal {@code Promise} implementation.
 *
 * @param <T> result type
 */
final class PromiseImpl<T> implements Promise<T> {

    private final FutureImpl<T> future;

    PromiseImpl(FutureImpl<T> future) {
        this.future = future;
    }

    @Override
    public Executor executor() {
        return future.executor();
    }

    @Override
    public Future<T> future() {
        return future;
    }

    @Override
    public boolean tryComplete(Try<? extends T> value) {
        return future.tryComplete(value);
    }

    // The underlying FutureImpl is MUTABLE and therefore we CANNOT CHANGE DEFAULT equals() and hashCode() behavior.
    // See http://stackoverflow.com/questions/4718009/mutable-objects-and-hashcode

    @Override
    public String toString() {
        return "Promise(" + future.getValue().map(String::valueOf).getOrElse("?") + ")";
    }
}
