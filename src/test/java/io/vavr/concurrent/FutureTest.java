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

import io.vavr.*;
import io.vavr.collection.Seq;
import io.vavr.collection.Iterator;
import io.vavr.collection.List;
import io.vavr.collection.Stream;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.assertj.core.api.IterableAssert;
import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import static io.vavr.concurrent.Concurrent.waitUntil;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static io.vavr.concurrent.Concurrent.zZz;
import static org.assertj.core.api.Assertions.fail;

@SuppressWarnings("deprecation")
public class FutureTest extends AbstractValueTest {

    static final Executor TRIVIAL_EXECUTOR = Runnable::run;

    private static final Executor REJECTING_EXECUTOR = ignored -> { throw new RejectedExecutionException(); };

    @AfterClass
    public static void gracefullyFinishThreads() throws TimeoutException {
        Concurrent.gracefullyFinishThreads();
    }

    @Rule
    public TestRule watcher = new TestWatcher() {
        @Override
        protected void starting(Description desc) {
            printInfo("[STARTING]", desc);
            printForkJoinPoolInfo();
        }
        @Override
        protected void finished(Description desc) {
            printInfo("[FINISHED]", desc);
        }
        private void printInfo(String prefix, Description desc) {
            System.out.println(String.format("%s %s %s", prefix, LocalDateTime.now(), desc.getDisplayName()));
        }
        private void printForkJoinPoolInfo() {
            final ForkJoinPool pool = ForkJoinPool.commonPool();
            final String info = String.format("- [ForkJoinPool.commonPool()] parallelism: %s, poolSize: %s, isAsyncMode: %s, runningThreadCount: %s, activeThreadCount: %s, isQuiescent: %s, stealCount: %s, queuedTaskCount: %s, queuedSubmissionCount: %s, hasQueuedSubmissions: %s",
                    pool.getParallelism(),
                    pool.getPoolSize(),
                    pool.getAsyncMode(),
                    pool.getRunningThreadCount(),
                    pool.getActiveThreadCount(),
                    pool.isQuiescent(),
                    pool.getStealCount(),
                    pool.getQueuedTaskCount(),
                    pool.getQueuedSubmissionCount(),
                    pool.hasQueuedSubmissions()
            );
            System.out.println(info);
        }
    };

    @Override
    protected <T> IterableAssert<T> assertThat(Iterable<T> actual) {
        return new IterableAssert<T>(actual) {
            @SuppressWarnings("unchecked")
            @Override
            public IterableAssert<T> isEqualTo(Object expected) {
                if (actual instanceof Future && expected instanceof Future) {
                    assertThat(((Future<T>) actual).getValue()).isEqualTo(((Future<T>) expected).getValue());
                    return this;
                } else {
                    return super.isEqualTo(expected);
                }
            }
        };
    }

    @Override
    protected <T> Future<T> empty() {
        return Future.failed(TRIVIAL_EXECUTOR, new NoSuchElementException());
    }

    @Override
    protected <T> Future<T> of(T element) {
        return Future.of(TRIVIAL_EXECUTOR, () -> element);
    }

    @SafeVarargs
    @Override
    protected final <T> Future<T> of(T... elements) {
        return of(elements[0]);
    }

    @Override
    protected boolean useIsEqualToInsteadOfIsSameAs() {
        return true;
    }

    // -- static failed()

    @Test
    public void shouldCreateFailureThatFailsWithRuntimeException() {
        final Future<Object> failed = Future.failed(new RuntimeException("ooops")).await();
        assertThat(failed.isFailure()).isTrue();
        final Throwable t = failed.getValue().get().getCause();
        assertThat(t.getClass()).isEqualTo(RuntimeException.class);
        assertThat(t.getMessage()).isEqualTo("ooops");
    }

    @Test
    public void shouldCreateFailureThatFailsWithError() {
        final Future<Object> failed = Future.failed(new Error("ooops")).await();
        assertThat(failed.isFailure()).isTrue();
        final Throwable t = failed.getValue().get().getCause();
        assertThat(t.getClass()).isEqualTo(Error.class);
        assertThat(t.getMessage()).isEqualTo("ooops");
    }

    @Test
    public void shouldCreateAndFailAFutureUsingForkJoinPool() {
        final Future<Integer> future = Future.of(() -> {
            throw new Error();
        });
        future.await();
        assertFailed(future, Error.class);
    }

    @Test
    public void shouldCreateAndFailAFutureUsingTrivialExecutor() {
        final Future<Integer> future = Future.of(TRIVIAL_EXECUTOR, () -> {
            throw new Error();
        });
        assertFailed(future, Error.class);
    }

    // -- static fromJavaFuture()

    @Test
    public void shouldCreateFutureFromJavaFuture() {
        final java.util.concurrent.Future<Integer> jFuture = CompletableFuture.supplyAsync(() -> 1);
        final Future<Integer> future = Future.fromJavaFuture(jFuture).await();
        assertCompleted(future, 1);
    }

    @Test
    public void shouldCreateFutureFromJavaFutureUsingTrivialExecutor() {
        final java.util.concurrent.Future<String> jFuture = CompletableFuture.supplyAsync(() -> "Result");
        final Future<String> future = Future.fromJavaFuture(TRIVIAL_EXECUTOR, jFuture).await();
        assertCompleted(future, "Result");
    }

    // -- static fromCompletableFuture()

    @Test
    public void shouldCreateFutureFromCompletedJavaCompletableFuture() {
        final CompletableFuture<Integer> jFuture = CompletableFuture.completedFuture(1);
        final Future<Integer> future = Future.fromCompletableFuture(jFuture);
        assertCompleted(future, 1);
    }

    @Test
    public void shouldCreateFutureFromFailedJavaCompletableFuture() {
        final CompletableFuture<Integer> jFuture = new CompletableFuture<>();
        jFuture.completeExceptionally(new RuntimeException("some"));
        final Future<Integer> future = Future.fromCompletableFuture(jFuture);
        assertFailed(future, RuntimeException.class);
    }

    @Test
    public void shouldCreateFutureFromJavaCompletableFuture() {
        final CompletableFuture<Integer> jFuture = CompletableFuture.supplyAsync(() -> 1);
        final Future<Integer> future = Future.fromCompletableFuture(jFuture).await();
        assertCompleted(future, 1);
    }

    @Test
    public void shouldCreateFutureFromLateFailingJavaCompletableFuture() {
        final CompletableFuture<Integer> jFuture = Future.<Integer> of(zZz(new RuntimeException())).toCompletableFuture();
        final Future<Integer> future = Future.fromCompletableFuture(jFuture).await();
        assertFailed(future, RuntimeException.class);
    }

    @Test
    public void shouldCreateFutureFromJavaCompletableFutureUsingTrivialExecutor() {
        final java.util.concurrent.Future<String> jFuture = CompletableFuture.supplyAsync(() -> "Result");
        final Future<String> future = Future.fromJavaFuture(TRIVIAL_EXECUTOR, jFuture).await();
        assertCompleted(future, "Result");
    }

    // TODO: test the cases isDone(), isCompletedExceptionally(), isCancelled()

    // -- static find()

    @Test
    public void shouldFindNoneWhenEmptySeq() {
        final Future<Option<Object>> testee = Future.find(List.empty(), t -> true);
        assertCompleted(testee, Option.none());
    }

    @Test
    public void shouldFindFirstValueThatSatisfiesAPredicateUsingForkJoinPool() {
        final Seq<Future<Integer>> futures = Stream.from(1).map(i -> Future.of(() -> i)).take(20);
        final Future<Option<Integer>> testee = Future.find(futures, i -> i == 13);
        testee.await();
        assertCompleted(testee, Option.some(13));
    }

    @Test
    public void shouldFailFindingFirstValueBecauseNoResultSatisfiesTheGivenPredicateUsingForkJoinPool() {
        final Seq<Future<Integer>> futures = Stream.from(1).map(i -> Future.of(() -> i)).take(20);
        final Future<Option<Integer>> testee = Future.find(futures, i -> false);
        testee.await();
        assertCompleted(testee, Option.none());
    }

    @Test
    public void shouldFindOneSucceedingFutureWhenAllOthersFailUsingDefaultExecutor() {
        final Seq<Future<Integer>> futures = Stream.from(1)
                .map(i -> Future.<Integer> of(() -> {
                    throw new Error();
                }))
                .take(12)
                .append(Future.of(() -> 13));
        final Future<Option<Integer>> testee = Future.find(futures, i -> i == 13);
        testee.await();
        assertCompleted(testee, Option.some(13));
    }

    @Test
    public void shouldFindNoneWhenAllFuturesFailUsingForkJoinPool() {
        final Seq<Future<Integer>> futures = Stream.from(1)
                .map(i -> Future.<Integer> of(() -> {
                    throw new Error(String.valueOf(i));
                }))
                .take(20);
        final Future<Option<Integer>> testee = Future.find(futures, i -> i == 13);
        testee.await();
        assertCompleted(testee, Option.none());
    }

    // -- static firstCompletedOf()

    @Test
    public void shouldGetFirstCompletedOfFailuresUsingForkJoinPool() {
        final Seq<Future<Object>> futures = Stream.from(1).map(i -> Future.of(zZz(new Error()))).take(3);
        final Future<?> testee = Future.firstCompletedOf(futures);
        testee.await();
        assertThat(testee.getValue().get().isFailure()).isTrue();
    }

    @Test
    public void shouldGetFirstCompletedOfSucceedingFuturesUsingForkJoinPool() {
        final Seq<Future<Integer>> futures = Stream.from(1).map(i -> Future.of(zZz(i))).take(3);
        final Future<?> testee = Future.firstCompletedOf(futures).await();
        assertThat(testee.getValue().get().isSuccess()).isTrue();
    }

    // -- static fromTry()

    @Test
    public void shouldCreateFailFutureFromTry() {
        final Future<Integer> future = Future.fromTry(Try.of(() -> { throw new Error(); }));
        future.await();
        assertThat(future.isFailure()).isTrue();
    }

    @Test
    public void shouldCreateSuccessFutureFromTry() {
        final Future<Integer> future = Future.fromTry(Try.of(() -> 42));
        future.await();
        assertThat(future.get()).isEqualTo(42);
    }

    // -- static of()

    @Test
    public void shouldCreateAndCompleteAFutureUsingTrivialExecutor() {
        final Future<Integer> future = Future.of(TRIVIAL_EXECUTOR, () -> 1);
        assertCompleted(future, 1);
    }

    @Test
    public void shouldNotCancelCompletedFutureUsingTrivialExecutor() {
        final Future<Integer> future = Future.of(TRIVIAL_EXECUTOR, () -> 1);
        assertThat(future.cancel()).isFalse();
        assertCompleted(future, 1);
    }

    @Test
    public void shouldCompleteWithFailureWhenExecutorThrowsRejectedExecutionException() {
        final Future<Integer> future = Future.of(REJECTING_EXECUTOR, () -> 1);
        assertFailed(future, RejectedExecutionException.class);
    }

    @Test
    public void shouldCompleteOneFutureUsingAThreadPoolExecutorLimitedToOneThread() {
        final ExecutorService service = new ThreadPoolExecutor(1, 1, 0L, MILLISECONDS, new SynchronousQueue<>());
        final Future<Integer> future = Future.of(service, () -> expensiveOperation(1)).await();
        assertCompleted(future, 1);
        service.shutdown();
    }

    @Test
    public void shouldCompleteThreeFuturesUsingAThreadPoolExecutorLimitedToTwoThreads() {
        final ExecutorService service = new ThreadPoolExecutor(1, 2, 0L, MILLISECONDS, new SynchronousQueue<>());
        final Stream<Future<Integer>> futures = Stream
                .rangeClosed(1, 3)
                .map(value -> Future.of(service, () -> expensiveOperation(value)));
        futures.forEach(future -> Try.run(future::await));
        assertThat(futures.flatMap(Function.identity()).toList().sorted()).isEqualTo(List.of(1, 2, 3));
        service.shutdown();
    }

    private static <T> T expensiveOperation(T value) throws InterruptedException {
        Thread.sleep(500);
        return value;
    }

    // -- static reduce()

    @Test(expected = NoSuchElementException.class)
    public void shouldFailReduceEmptySequence() {
        Future.<Integer> reduce(List.empty(), (i1, i2) -> i1 + i2);
    }

    @Test
    public void shouldReduceSequenceOfFutures() {
        final Future<String> future = Future.reduce(
                List.of(Future.of(zZz("Va")), Future.of(zZz("vr"))),
                (i1, i2) -> i1 + i2
        ).await();
        assertThat(future.get()).isEqualTo("Vavr");
    }

    @Test
    public void shouldReduceWithErrorIfSequenceOfFuturesContainsOneError() {
        final Future<Integer> future = Future.reduce(
                List.of(Future.of(zZz(13)), Future.of(zZz(new Error()))),
                (i1, i2) -> i1 + i2
        ).await();
        assertFailed(future, Error.class);
    }

    // -- static run()

    @Test
    public void shouldCompleteRunnable() {
        final int[] sideEffect = new int[] { 0 };
        Future.run(() -> sideEffect[0] = 42).await();
        assertThat(sideEffect[0]).isEqualTo(42);
    }

    // -- static sequence()

    @Test
    public void shouldCompleteWithSeqOfValueIfSequenceOfFuturesContainsNoError() {
        final Future<Seq<Integer>> sequence = Future.sequence(
                List.of(Future.of(zZz(1)), Future.of(zZz(2)))
        ).await();
        assertThat(sequence.getValue().get()).isEqualTo(Try.success(Stream.of(1, 2)));
    }

    @Test
    public void shouldCompleteWithErrorIfSequenceOfFuturesContainsOneError() {
        final Future<Seq<Integer>> sequence = Future.sequence(
                List.of(Future.of(zZz(13)), Future.of(zZz(new Error())))
        ).await();
        assertFailed(sequence, Error.class);
    }

    // -- static successful()

    @Test
    public void shouldCreateSuccessful() {
        final Future<Integer> succ = Future.successful(42);
        assertThat(succ.isCompleted()).isTrue();
        assertThat(succ.isSuccess()).isTrue();
        assertThat(succ.get()).isEqualTo(42);
    }

    // -- static traverse()

    @Test
    public void shouldCompleteTraverse() {
        final Future<Seq<Integer>> future = Future.traverse(List.of(1, 2, 3), i -> Future.of(zZz(i))).await();
        assertThat(future.get()).isEqualTo(Stream.of(1, 2, 3));
    }

    // -- andThen

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldCompleteWithErrorIfFailAndThenFail() {
        final Future<Integer> future = Future.<Integer> of(zZz(new Error("fail!")))
                .andThen(t -> zZz(new Error("and then fail!"))).await();
        assertFailed(future, Error.class);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Test
    public void shouldCompleteWithSuccessIfSuccessAndThenFail() {
        final Future<Integer> future = Future.of(zZz(42))
                .andThen(t -> zZz(new Error("and then fail!"))).await();
        assertThat(future.getValue().get()).isEqualTo(Try.success(42));
    }

    @Test
    public void shouldCompleteWithSpecificOrderIfSuccessAndThenSuccess() {
        final int[] sideEffect = new int[] { 0 };
        final Future<Void> future = Future.run(() -> Thread.sleep(250)).andThen(t -> sideEffect[0] = 42);
        assertThat(future.isCompleted()).isFalse();
        assertThat(sideEffect[0]).isEqualTo(0);
        future.await();
        assertThat(sideEffect[0]).isEqualTo(42);
    }

    // -- orElse

    @Test
    public void shouldReturnSelfResultOnOrElseIfSuccess() {
        final Future<String> f1 = Future.of(() -> "f1");
        final Future<String> f2 = f1.orElse(Future.of(() -> "f2")).await();
        assertThat(f2.get()).isEqualTo("f1");
    }

    @Test
    public void shouldReturnSelfResultOnOrElseSupplierIfSuccess() {
        final Future<String> f1 = Future.of(() -> "f1");
        final Future<String> f2 = f1.orElse(() -> Future.of(() -> "f2")).await();
        assertThat(f2.get()).isEqualTo("f1");
    }

    @Test
    public void shouldReturnOtherResultOnOrElseIfFailure() {
        final Future<String> f1 = Future.failed(new RuntimeException());
        final Future<String> f2 = f1.orElse(Future.of(() -> "f2")).await();
        assertThat(f2.get()).isEqualTo("f2");
    }

    @Test
    public void shouldReturnOtherResultOnOrElseSupplierIfFailure() {
        final Future<String> f1 = Future.failed(new RuntimeException());
        final Future<String> f2 = f1.orElse(() -> Future.of(() -> "f2")).await();
        assertThat(f2.get()).isEqualTo("f2");
    }

    // -- await()

    @Test
    public void shouldAwaitOnGet() {
        final Future<Integer> future = Future.of(() -> {
            Try.run(() -> Thread.sleep(250L));
            return 1;
        });
        assertThat(future.get()).isEqualTo(1);
    }

    // -- await(timeout, timeunit)

    @Test
    public void shouldAwaitAndTimeout() {
        final long timeout = 100;
        final Future<Void> future = Future.run(() -> {
            long millis = 1;
            while ((millis = millis << 1) < 1024) {
                Thread.sleep(millis);
            }
        });
        final Future<Void> returnedFuture = future.await(timeout, TimeUnit.MILLISECONDS);
        assertThat(returnedFuture).isSameAs(future);
        assertThat(future.isFailure()).isTrue();
        assertThat(future.getCause().get()).isInstanceOf(TimeoutException.class);
        assertThat(future.getCause().get().getMessage()).isEqualTo("timeout after 100 milliseconds");
    }
    
    @Test
    public void shouldHandleInterruptedExceptionCorrectlyInAwait() {
        // the Future should never be completed as long as the InterruptedException is rethrown by the Try...
        final Future<Void> future = Future.run(() -> { throw new InterruptedException(); });
        // ...therefore the timeout will occur
        future.await(100, TimeUnit.MILLISECONDS);
        assertThat(future.isFailure()).isTrue();
        assertThat(future.getCause().get()).isInstanceOf(TimeoutException.class);
        assertThat(future.getCause().get().getMessage()).isEqualTo("timeout after 100 milliseconds");
    }

    // -- failed

    @Test
    public void shouldConvertToFailedFromFail() {
        final Future<Throwable> future = Future.of(zZz(new Error())).failed().await();
        assertThat(future.isSuccess()).isTrue();
        assertThat(future.get().getClass()).isEqualTo(Error.class);
    }

    @Test
    public void shouldConvertToFailedFromSuccess() {
        final Future<Throwable> future = Future.of(zZz(42)).failed().await();
        assertThat(future.isFailure()).isTrue();
        assertThat(future.getValue().get().getCause().getClass()).isEqualTo(NoSuchElementException.class);
    }

    // -- fallbackTo

    @Test
    public void shouldFallbackToThisResult() {
        final Future<Integer> future = Future.of(() -> 1);
        final Future<Integer> that = Future.of(() -> {
            throw new Error();
        });
        final Future<Integer> testee = future.fallbackTo(that).await();
        assertThat(testee.getValue().get()).isEqualTo(Try.success(1));
    }

    @Test
    public void shouldFallbackToThatResult() {
        final Future<Integer> future = Future.of(() -> {
            throw new Error();
        });
        final Future<Integer> that = Future.of(() -> 1);
        final Future<Integer> testee = future.fallbackTo(that).await();
        assertThat(testee.getValue().get()).isEqualTo(Try.success(1));
    }

    @Test
    public void shouldFallbackToThisFailure() {
        final Future<Integer> future = Future.of(() -> {
            throw new Error("ok");
        });
        final Future<Integer> that = Future.of(() -> {
            throw new Error();
        });
        final Future<Integer> testee = future.fallbackTo(that).await();
        final Try<Integer> result = testee.getValue().get();
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getCause().getMessage()).isEqualTo("ok");
    }

    // -- fold()

    @Test
    public void shouldFoldEmptyIterable() {
        final Seq<Future<Integer>> futures = Stream.empty();
        final Future<Integer> testee = Future.fold(futures, 0, (a, b) -> a + b).await();
        assertThat(testee.getValue().get()).isEqualTo(Try.success(0));
    }

    @Test
    public void shouldFoldNonEmptyIterableOfSucceedingFutures() {
        final Seq<Future<Integer>> futures = Stream.from(1).map(i -> Future.of(zZz(i))).take(5);
        final Future<Integer> testee = Future.fold(futures, 0, (a, b) -> a + b).await();
        assertThat(testee.getValue().get()).isEqualTo(Try.success(15));
    }

    @Test
    public void shouldFoldNonEmptyIterableOfFailingFutures() {
        Iterable<Future<Integer>> futures = Stream.from(1).map(i -> Future.<Integer>of(zZz(new Error()))).take(5);
        final Future<Integer> testee = Future.fold(futures, 0, Integer::sum).await();
        assertFailed(testee, Error.class);
    }

    // -- cancel()

    @Test
    public void shouldInterruptLockedFuture() {
        final Object monitor = new Object();
        final AtomicBoolean running = new AtomicBoolean(false);
        final Future<?> future = blocking(() -> {
            synchronized (monitor) {
                running.set(true);
                monitor.wait(); // wait forever
            }
        });
        waitUntil(running::get);
        synchronized (monitor) {
            future.cancel();
        }
        assertThat(future.isCancelled()).isTrue();
    }

    @Test(expected = CancellationException.class)
    public void shouldThrowOnGetAfterCancellation() {
        final Object monitor = new Object();
        final AtomicBoolean running = new AtomicBoolean(false);
        final Future<?> future = blocking(() -> {
            synchronized (monitor) {
                running.set(true);
                monitor.wait(); // wait forever
            }
        });
        waitUntil(running::get);
        synchronized (monitor) {
            future.cancel();
        }
        assertThat(future.isCancelled()).isTrue();
        future.get();
        fail("Future was expected to throw on get() after cancellation!");
    }

    @Test
    public void shouldCancelFutureThatNeverCompletes() {
        @SuppressWarnings("deprecation")
        final Future<?> future = Future.run(complete -> {
            // we break our promise, the Future is never completed
        });

        assertThat(future.isCompleted()).isFalse();
        assertThat(future.isCancelled()).isFalse();

        assertThat(future.cancel()).isTrue();
        assertThat(future.isCompleted()).isTrue();
    }

    // -- collect()

    @Test
    public void shouldCollectDefinedValueUsingPartialFunction() {
        final PartialFunction<Integer, String> pf = Function1.<Integer, String> of(String::valueOf).partial(i -> i % 2 == 1);
        final Future<String> future = Future.of(zZz(3)).collect(pf).await();
        assertThat(future.getValue().get()).isEqualTo(Try.success("3"));
    }

    @Test
    public void shouldFilterNotDefinedValueUsingPartialFunction() {
        final PartialFunction<Integer, String> pf = Function1.<Integer, String> of(String::valueOf).partial(i -> i % 2 == 1);
        final Future<String> future = Future.of(zZz(2)).collect(pf).await();
        assertThat(future.getValue().get().isFailure()).isTrue();
    }

    @Test
    public void shouldCollectEmptyFutureUsingPartialFunction() {
        final PartialFunction<Integer, String> pf = Function1.<Integer, String> of(String::valueOf).partial(i -> i % 2 == 1);
        final Future<String> future = Future.<Integer> of(zZz(new Error())).collect(pf).await();
        assertThat(future.getValue().get().isFailure()).isTrue();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnNullCollectPartialFunction() {
        final PartialFunction<Integer, String> pf = null;
        Future.of(zZz(3)).collect(pf);
    }

    // -- executorService()

    @Test
    public void shouldReturnExecutor() {
        final Future<Integer> f1 = Future.of(() -> 42);
        assertThat(f1.executor()).isSameAs(Future.DEFAULT_EXECUTOR);
        final ExecutorService service = java.util.concurrent.Executors.newCachedThreadPool();
        final Future<Integer> f2 = Future.of(service, () -> 42);
        assertThat(f2.executor()).isSameAs(service);
        service.shutdown();
    }

    // -- getCause()

    @Test
    public void shouldGetCauseOfUncompletedFuture() {
        final AtomicBoolean running = new AtomicBoolean(false);
        final Future<?> future = blocking(() -> {
            synchronized (running) {
                running.set(true);
                running.wait();
            }
        });
        assertThat(future.getCause()).isEqualTo(Option.none());
        waitUntil(running::get);
        synchronized (running) {
            running.notify();
        }
    }

    @Test
    public void shouldGetCauseOfFailedFuture() {
        final Error error = new Error();
        assertThat(Future.failed(error).getCause()).isEqualTo(Option.some(error));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowWhenGettingCauseOfSucceededFuture() {
        Future.successful("ok").getCause();
    }

    // -- getValue()

    @Test
    public void shouldGetValueOfUncompletedFuture() {
        final AtomicBoolean running = new AtomicBoolean(false);
        final Future<?> future = blocking(() -> {
            synchronized (running) {
                running.set(true);
                running.wait();
            }
        });
        assertThat(future.getValue()).isEqualTo(Option.none());
        waitUntil(running::get);
        synchronized (running) {
            running.notify();
        }
    }

    @Test
    public void shouldGetValueOfSucceededFuture() {
        assertThat(Future.successful("ok").getValue()).isEqualTo(Option.some(Try.success("ok")));
    }

    @Test
    public void shouldThrowWhenGettingValueOfFailedFuture() {
        final Error error = new Error();
        assertThat(Future.failed(error).getValue()).isEqualTo(Option.some(Try.failure(error)));
    }

    // -- isCompleted()

    @Test
    public void shouldBeCompletedWhenSuccessful() {
        assertThat(Future.successful(null).isCompleted()).isTrue();
    }

    @Test
    public void shouldBeCompletedWhenFailed() {
        assertThat(Future.failed(new Exception()).isCompleted()).isTrue();
    }

    @Test
    public void shouldBeCompletedWhenResultIsPresent() {
        final Future<Object> future = Future.of(() -> null).await();
        assertThat(future.isCompleted()).isTrue();
    }

    // -- isSuccess()

    @Test
    public void shouldBeSuccessful() {
        assertThat(Future.successful(null).isSuccess()).isTrue();
    }

    // -- isFailure()

    @Test
    public void shouldBeFailed() {
        assertThat(Future.failed(new Exception()).isFailure()).isTrue();
    }
    
    // -- onComplete()

    @Test
    public void shouldRegisterCallbackBeforeFutureCompletes() {

        final AtomicBoolean ok = new AtomicBoolean(false);
        final AtomicReference<Task.Complete<Boolean>> computation = new AtomicReference<>(null);

        // this computation never ends
        @SuppressWarnings("deprecation")
        final Future<Boolean> future = Future.run(computation::set);

        // now we have time to register an onComplete handler
        future.onComplete(result -> result.forEach(ok::set));

        // now we complete the future...
        assertThat(future.isCompleted()).isFalse();
        assertThat(computation.get().with(Try.success(true))).isTrue();

        // ...and wait for the result
        waitUntil(ok::get);
    }

    @Test
    public void shouldPerformActionAfterFutureCompleted() {
        final int[] actual = new int[] { -1 };
        final Future<Integer> future = Future.of(TRIVIAL_EXECUTOR, () -> 1);
        assertCompleted(future, 1);
        assertThat(actual[0]).isEqualTo(-1);
        future.onComplete(result -> actual[0] = result.get());
        assertThat(actual[0]).isEqualTo(1);
    }

    // -- onFailure()

    @Test
    public void shouldDoActionOnFailure() {
        final Future<?> future = Future.of(zZz(new Error()));
        final Throwable[] holder = new Throwable[] { null };
        future.onFailure(t -> holder[0] = t);
        waitUntil(() -> holder[0] != null);
        assertThat(holder[0].getClass()).isEqualTo(Error.class);
    }

    // -- onSuccess()

    @Test
    public void shouldDoActionOnSuccess() {
        final Future<Integer> future = Future.of(zZz(42));
        final int[] holder = new int[] { 0 };
        future.onSuccess(i -> holder[0] = i);
        waitUntil(() -> holder[0] > 0);
        assertThat(holder[0]).isEqualTo(42);
    }

    // -- recover()

    @Test
    public void shouldRecoverFailedFuture() {
        final Future<Integer> recovered = Future.<Integer> of(zZz(new Error())).recover(t -> 42);
        waitUntil(recovered::isCompleted);
        assertThat(recovered.isSuccess()).isTrue();
        assertThat(recovered.get()).isEqualTo(42);
    }

    @Test
    public void shouldRecoverFailedFutureWithSpecificCause() {
        final Future<Integer> recovered = Future.<Integer> of(zZz(new Error())).recover(Error.class, t -> 42);
        waitUntil(recovered::isCompleted);
        assertThat(recovered.isSuccess()).isTrue();
        assertThat(recovered.get()).isEqualTo(42);
    }

    @Test
    public void shouldNotRecoverFailedFutureWithMismatchingCause() {
        final Future<Integer> recovered = Future.<Integer> of(zZz(new Error())).recover(ArithmeticException.class, t -> 42);
        waitUntil(recovered::isCompleted);
        assertThat(recovered.isFailure()).isTrue();
    }

    @Test
    public void shouldNotCrashWhenRecoverFails() {
        final Future<Integer> recovered = Future.<Integer> of(zZz(new Error())).recover(t -> {
            throw new ArithmeticException();
        });
        waitUntil(recovered::isCompleted);
        waitUntil(recovered::isFailure);
        assertThat(recovered.getCause().get().getClass()).isEqualTo(ArithmeticException.class);
    }

    @Test
    public void shouldNotCrashWhenRecoverFailsWithSpecificCause() {
        final Future<Integer> recovered = Future.<Integer> of(zZz(new Error())).recover(Error.class, t -> {
            throw new ArithmeticException();
        });
        waitUntil(recovered::isCompleted);
        waitUntil(recovered::isFailure);
        assertThat(recovered.getCause().get().getClass()).isEqualTo(ArithmeticException.class);
    }

    // -- recoverWith()

    @Test
    public void shouldRecoverFailedFutureWithFuture() {
        final Future<String> recovered = Future.<String> of(() -> { throw new Error("oh!"); })
                .recoverWith(x -> Future.of(x::getMessage));
        waitUntil(recovered::isCompleted);
        assertThat(recovered.isSuccess()).isTrue();
        assertThat(recovered.get()).isEqualTo("oh!");
    }

    @Test
    public void shouldRecoverSuccessFutureWithFuture() {
        final Future<String> recovered = Future.of(() -> "oh!")
                .recoverWith(ignored -> Future.of(() -> "ignored"));
        waitUntil(recovered::isCompleted);
        assertThat(recovered.isSuccess()).isTrue();
        assertThat(recovered.get()).isEqualTo("oh!");
    }

    // -- toCompletableFuture

    @Test
    public void shouldConvertCompletedFutureToCompletableFuture() {
        final CompletableFuture<Integer> completableFuture = Future.of(() -> 42).toCompletableFuture();
        assertThat(completableFuture.isDone());
        assertThat(Try.of(completableFuture::get).get()).isEqualTo(42);
    }

    @Test
    public void shouldConvertFailedFutureToCompletableFuture() {
        final CompletableFuture<Integer> completableFuture = Future.<Integer> failed(new RuntimeException()).toCompletableFuture();
        assertThat(completableFuture.isCompletedExceptionally());
        assertThat(Try.of(completableFuture::get).getCause()).isExactlyInstanceOf(ExecutionException.class);
    }

    @Test
    public void shouldConvertLateFutureToCompletableFuture() {
        final CompletableFuture<Integer> completableFuture = Future.of(zZz(42)).toCompletableFuture();
        assertThat(Try.of(completableFuture::get).get()).isEqualTo(42);
    }

    // -- toTry()

    @Test
    public void shouldConvertSuccessfulFutureToTry() {
        assertThat(Future.successful(1).toTry()).isEqualTo(Try.success(1));
    }

    @Test
    public void shouldConvertFailedFutureToTry() {
        assertThat(Future.failed(new Error("!")).toTry()).isEqualTo(Try.failure(new Error("!")));
    }

    // -- transform()

    @Test
    public void shouldTransform() {
        final String transformed = Future.of(() -> 42).transform(f -> String.valueOf(f.get()));
        assertThat(transformed).isEqualTo("42");
    }

    // -- transformValue()

    @Test
    public void shouldTransformResultFromSuccessToSuccess() {
        final Future<String> future = Future.of(zZz(42)).transformValue(t -> Try.of(() -> "forty two"));
        future.await();
        waitUntil(future::isSuccess);
        assertThat(future.get()).isEqualTo("forty two");
    }

    @Test
    public void shouldTransformResultFromSuccessToFailure() {
        final Future<String> future = Future.of(zZz(42)).transformValue(t -> Try.failure(new Error()));
        future.await();
        waitUntil(future::isFailure);
        assertThat(future.getCause().get().getClass()).isEqualTo(Error.class);
    }

    @Test
    public void shouldTransformResultFromSuccessToFailureThroughError() {
        final Future<String> future = Future.of(zZz(42)).transformValue(t -> Try.of(() -> {
            throw new ArithmeticException();
        }));
        future.await();
        waitUntil(future::isFailure);
        assertThat(future.getCause().get().getClass()).isEqualTo(ArithmeticException.class);
    }

    @Test
    public void shouldTransformResultFromFailureToSuccess() {
        final Future<String> future = Future.of(zZz(new Error())).transformValue(t -> Try.of(() -> "forty two"));
        future.await();
        waitUntil(future::isSuccess);
        assertThat(future.get()).isEqualTo("forty two");
    }

    @Test
    public void shouldTransformResultFromFailureToFailure() {
        final Future<String> future = Future.of(() -> {
            throw new ArithmeticException();
        }).transformValue(t -> Try.failure(new Error()));
        future.await();
        waitUntil(future::isFailure);
        assertThat(future.getCause().get().getClass()).isEqualTo(Error.class);
    }

    @Test
    public void shouldTransformResultFromFailureToFailureThroughError() {
        final Future<String> future = Future.of(zZz(new Error())).transformValue(t -> Try.of(() -> {
            throw new ArithmeticException();
        }));
        future.await();
        waitUntil(future::isFailure);
        assertThat(future.getCause().get().getClass()).isEqualTo(ArithmeticException.class);
    }

    // -- zip()

    @Test
    public void shouldZipSuccess() {
        final Future<Tuple2<Integer, Integer>> future = Future.of(zZz(1)).zip(Future.of(zZz(2)));
        future.await();
        waitUntil(future::isSuccess);
        assertThat(future.get()).isEqualTo(Tuple.of(1, 2));
    }

    @Test
    public void shouldZipFailure() {
        final Future<Tuple2<Integer, Integer>> future = Future.<Integer> of(zZz(new Error())).zip(Future.of(zZz(2)));
        future.await();
        waitUntil(future::isFailure);
        assertThat(future.getCause().get().getClass()).isEqualTo(Error.class);
    }

    // -- zipWith()

    @Test
    public void shouldZipWithSuccess() {
        final Future<List<Integer>> future = Future.of(zZz(1)).zipWith(Future.of(zZz(2)), List::of);
        future.await();
        waitUntil(future::isSuccess);
        assertThat(future.get()).isEqualTo(List.of(1, 2));
    }

    @Test
    public void shouldZipWithFailure() {
        final Future<List<Integer>> future = Future.<Integer> of(zZz(new Error())).zipWith(Future.of(zZz(2)), List::of);
        future.await();
        waitUntil(future::isFailure);
        assertThat(future.getCause().get().getClass()).isEqualTo(Error.class);
    }

    @Test
    public void shouldZipWithCombinatorFailure() {
        final Future<List<Integer>> future = Future.of(zZz(3)).zipWith(Future.of(zZz(2)), (t, u) -> {
            throw new RuntimeException();
        });
        future.await();
        waitUntil(future::isFailure);
        assertThat(future.getCause().get().getClass()).isEqualTo(RuntimeException.class);
    }

    // -- Value implementation

    @Test
    public void shouldFilterFutureWhenFilterIsNotEmpty() {
        final Future<Integer> future = Future.successful(42);
        assertThat(future.filter(i -> i == 42).get()).isEqualTo(42);
    }

    @Test
    public void shouldFilterFutureWhenFilterIsEmpty() {
        final Future<Integer> future = Future.successful(42);
        assertThat(future.filter(i -> i == 43).isEmpty()).isTrue();
    }

    @Test
    public void shouldFlatMapFuture() {
        final Future<Integer> future = Future.of(zZz(42)).flatMap(i -> Future.of(zZz(i * 2)));
        future.await();
        assertThat(future.get()).isEqualTo(84);
    }

    @Test
    public void shouldMapFuture() {
        final Future<Integer> future = Future.of(zZz(42)).map(i -> i * 2);
        future.await();
        assertThat(future.get()).isEqualTo(84);
    }

    @Test
    public void shouldReturnIterator() {
        final Iterator<Integer> it = Future.successful(42).iterator();
        assertThat(it.hasNext()).isTrue();
        assertThat(it.next()).isEqualTo(42);
        assertThat(it.hasNext()).isFalse();
    }

    // TODO: also test what happens an exception occurs within one of these
    // TODO: { filter, flatten, flatMap, get, isEmpty, iterator, map, peek }
    // TODO: method calls and compare it with Scala

    // TODO: also test what happens when FutureImpl.createThread throws a SecurityException

    // -- map()

    @Test
    public void shouldMapTheHappyPath() {
        final Future<String> testee = Future.of(zZz(1)).map(Object::toString);
        testee.await();
        assertCompleted(testee, "1");
    }

    @Test
    public void shouldMapWhenCrashingDuringFutureComputation() {
        final Future<String> testee = Future.<Integer> of(zZz(new Error())).map(Object::toString);
        testee.await();
        assertFailed(testee, Error.class);
    }

    @Test
    public void shouldMapWhenCrashingDuringMapping() {
        final Future<String> testee = Future.of(zZz(1)).map(i -> {
            throw new IllegalStateException();
        });
        testee.await();
        assertFailed(testee, IllegalStateException.class);
    }

    // -- mapTry()

    @Test
    public void shouldMapTryTheHappyPath() {
        final Future<String> testee = Future.of(zZz(1)).mapTry(Object::toString);
        testee.await();
        assertCompleted(testee, "1");
    }

    @Test
    public void shouldMapTryWhenCrashingDuringFutureComputation() {
        final Future<String> testee = Future.<Integer> of(zZz(new Error())).mapTry(Object::toString);
        testee.await();
        assertFailed(testee, Error.class);
    }

    @Test
    public void shouldMapTryWhenCrashingDuringMapping() {
        final Future<String> testee = Future.of(zZz(1)).mapTry(i -> {
            throw new IOException();
        });
        testee.await();
        assertFailed(testee, IOException.class);
    }

    // -- equals

    @Override
    @Test
    public void shouldRecognizeEqualObjects() {
        // Future equality undefined
    }

    @Override
    @Test
    public void shouldRecognizeUnequalObjects() {
        // Future equality undefined
    }

    // -- (helpers)

    private void assertFailed(Future<?> future, Class<? extends Throwable> exception) {
        assertThat(future.isCompleted()).isTrue();
        assertThat(future.getValue().get().failed().get()).isExactlyInstanceOf(exception);
    }

    // checks the invariant for cancelled state
    private <T> void assertCompleted(Future<?> future, T value) {
        assertThat(future.isCompleted()).isTrue();
        assertThat(future.getValue()).isEqualTo(Option.some(Try.success(value)));
    }

    private static Future<Void> blocking(CheckedRunnable computation) {
        return blocking(() ->  {
            computation.run();
            return null;
        });
    }

    private static <T> Future<T> blocking(CheckedFunction0<? extends T> computation) {
        return Future.of(() -> {
            final AtomicReference<T> result = new AtomicReference<>(null);
            final AtomicReference<Throwable> errorRef = new AtomicReference<>(null);
            ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker() {
                boolean releasable = false;
                @Override
                public boolean block() {
                    try {
                        result.set(computation.apply());
                    } catch(Throwable x) {
                        errorRef.set(x);
                    }
                    return releasable = true;
                }
                @Override
                public boolean isReleasable() {
                    return releasable;
                }
            });
            final Throwable error = errorRef.get();
            if (error != null) {
                throw error;
            } else {
                return result.get();
            }
        });
    }

    // -- spliterator

    @Test
    public void shouldHaveSizedSpliterator() {
        assertThat(of(1).spliterator().hasCharacteristics(Spliterator.SIZED | Spliterator.SUBSIZED)).isTrue();
    }

    @Test
    public void shouldHaveOrderedSpliterator() {
        assertThat(of(1).spliterator().hasCharacteristics(Spliterator.ORDERED)).isTrue();
    }

    @Test
    public void shouldReturnSizeWhenSpliterator() {
        assertThat(of(1).spliterator().getExactSizeIfKnown()).isEqualTo(1);
    }
}
