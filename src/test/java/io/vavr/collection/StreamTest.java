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
package io.vavr.collection;

import io.vavr.*;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InvalidObjectException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static io.vavr.collection.Stream.concat;

public class StreamTest extends AbstractLinearSeqTest {

    @Override
    protected String stringPrefix() {
        return "Stream";
    }

    // -- construction

    @Override
    protected <T> Collector<T, ArrayList<T>, Stream<T>> collector() {
        return Stream.collector();
    }

    @Override
    protected <T> Stream<T> empty() {
        return Stream.empty();
    }

    @Override
    protected <T> Stream<T> of(T element) {
        return Stream.of(element);
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    @Override
    protected final <T> Stream<T> of(T... elements) {
        return Stream.of(elements);
    }

    @Override
    protected <T> Stream<T> ofAll(Iterable<? extends T> elements) {
        return Stream.ofAll(elements);
    }

    @Override
    protected <T extends Comparable<? super T>> Stream<T> ofJavaStream(java.util.stream.Stream<? extends T> javaStream) {
        return Stream.ofAll(javaStream);
    }

    @Override
    protected Stream<Boolean> ofAll(boolean... elements) {
        return Stream.ofAll(elements);
    }

    @Override
    protected Stream<Byte> ofAll(byte... elements) {
        return Stream.ofAll(elements);
    }

    @Override
    protected Stream<Character> ofAll(char... elements) {
        return Stream.ofAll(elements);
    }

    @Override
    protected Stream<Double> ofAll(double... elements) {
        return Stream.ofAll(elements);
    }

    @Override
    protected Stream<Float> ofAll(float... elements) {
        return Stream.ofAll(elements);
    }

    @Override
    protected Stream<Integer> ofAll(int... elements) {
        return Stream.ofAll(elements);
    }

    @Override
    protected Stream<Long> ofAll(long... elements) {
        return Stream.ofAll(elements);
    }

    @Override
    protected Stream<Short> ofAll(short... elements) {
        return Stream.ofAll(elements);
    }

    @Override
    protected <T> Stream<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        return Stream.tabulate(n, f);
    }

    @Override
    protected <T> Stream<T> fill(int n, Supplier<? extends T> s) {
        return Stream.fill(n, s);
    }

    @Override
    protected <T> Traversable<T> fill(int n, T element) {
        return Stream.fill(n, element);
    }

    @Override
    protected Stream<Character> range(char from, char toExclusive) {
        return Stream.range(from, toExclusive);
    }

    @Override
    protected Stream<Character> rangeBy(char from, char toExclusive, int step) {
        return Stream.rangeBy(from, toExclusive, step);
    }

    @Override
    protected Stream<Double> rangeBy(double from, double toExclusive, double step) {
        return Stream.rangeBy(from, toExclusive, step);
    }

    @Override
    protected Stream<Integer> range(int from, int toExclusive) {
        return Stream.range(from, toExclusive);
    }

    @Override
    protected Stream<Integer> rangeBy(int from, int toExclusive, int step) {
        return Stream.rangeBy(from, toExclusive, step);
    }

    @Override
    protected Stream<Long> range(long from, long toExclusive) {
        return Stream.range(from, toExclusive);
    }

    @Override
    protected Stream<Long> rangeBy(long from, long toExclusive, long step) {
        return Stream.rangeBy(from, toExclusive, step);
    }

    @Override
    protected Stream<Character> rangeClosed(char from, char toInclusive) {
        return Stream.rangeClosed(from, toInclusive);
    }

    @Override
    protected Stream<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return Stream.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected Stream<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return Stream.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected Stream<Integer> rangeClosed(int from, int toInclusive) {
        return Stream.rangeClosed(from, toInclusive);
    }

    @Override
    protected Stream<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return Stream.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected Stream<Long> rangeClosed(long from, long toInclusive) {
        return Stream.rangeClosed(from, toInclusive);
    }

    @Override
    protected Stream<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return Stream.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> Stream<Stream<T>> transpose(Seq<? extends Seq<T>> rows) {
        return Stream.transpose((Stream<Stream<T>>) rows);
    }

    //fixme: delete, when useIsEqualToInsteadOfIsSameAs() will be eliminated from AbstractValueTest class
    @Override
    protected boolean useIsEqualToInsteadOfIsSameAs() {
        return true;
    }

    @Test
    @Override
    public void shouldRemoveNonExistingElement() {
        final Seq<Integer> t = of(1, 2, 3);
        assertThat(t.remove(4)).isEqualTo(t).isNotSameAs(t);
    }

    @Test
    @Override
    public void shouldRemoveFirstElementByPredicateNonExisting() {
        final Seq<Integer> t = of(1, 2, 3);
        assertThat(t.removeFirst(v -> v == 4)).isEqualTo(t).isNotSameAs(t);
    }

    @Test
    @Override
    public void shouldRemoveLastElementByPredicateNonExisting() {
        final Seq<Integer> t = of(1, 2, 3);
        assertThat(t.removeLast(v -> v == 4)).isEqualTo(t).isNotSameAs(t);
    }

    @Test
    @Override
    public void shouldNotRemoveAllNonExistingElementsFromNonNil() {
        final Seq<Integer> t = of(1, 2, 3);
        assertThat(t.removeAll(of(4, 5))).isEqualTo(t).isNotSameAs(t);
    }

    @Test
    @Override
    public void shouldNotRemoveAllNonObjectsElementsFromNonNil() {
        final Seq<Integer> seq = of(1, 2, 3);
        assertThat(seq.removeAll(4)).isEqualTo(seq).isNotSameAs(seq);
    }

    // -- static concat()

    @Test
    public void shouldConcatEmptyIterableIterable() {
        final Iterable<Iterable<Integer>> empty = List.empty();
        assertThat(concat(empty)).isSameAs(empty());
    }

    @Test
    public void shouldConcatNonEmptyIterableIterable() {
        final Iterable<Iterable<Integer>> itIt = List.of(List.of(1, 2), List.of(3));
        assertThat(concat(itIt)).isEqualTo(of(1, 2, 3));
    }

    @Test
    public void shouldConcatEmptyArrayIterable() {
        assertThat(concat()).isSameAs(empty());
    }

    @Test
    public void shouldConcatNonEmptyArrayIterable() {
        assertThat(concat(List.of(1, 2), List.of(3))).isEqualTo(of(1, 2, 3));
    }

    // -- static from(int)

    @Test
    public void shouldGenerateIntStream() {
        assertThat(Stream.from(-1).take(3)).isEqualTo(Stream.of(-1, 0, 1));
    }

    @Test
    public void shouldGenerateOverflowingIntStream() {
        //noinspection NumericOverflow
        assertThat(Stream.from(Integer.MAX_VALUE).take(2))
                .isEqualTo(Stream.of(Integer.MAX_VALUE, Integer.MAX_VALUE + 1));
    }

    // -- static from(int, int)

    @Test
    public void shouldGenerateIntStreamWithStep() {
        assertThat(Stream.from(-1, 6).take(3)).isEqualTo(Stream.of(-1, 5, 11));
    }

    @Test
    public void shouldGenerateOverflowingIntStreamWithStep() {
        //noinspection NumericOverflow
        assertThat(Stream.from(Integer.MAX_VALUE, 2).take(2))
                .isEqualTo(Stream.of(Integer.MAX_VALUE, Integer.MAX_VALUE + 2));
    }

    // -- static from(long)

    @Test
    public void shouldGenerateLongStream() {
        assertThat(Stream.from(-1L).take(3)).isEqualTo(Stream.of(-1L, 0L, 1L));
    }

    @Test
    public void shouldGenerateOverflowingLongStream() {
        //noinspection NumericOverflow
        assertThat(Stream.from(Long.MAX_VALUE).take(2))
                .isEqualTo(Stream.of(Long.MAX_VALUE, Long.MAX_VALUE + 1));
    }

    // -- static from(long, long)

    @Test
    public void shouldGenerateLongStreamWithStep() {
        assertThat(Stream.from(-1L, 5L).take(3)).isEqualTo(Stream.of(-1L, 4L, 9L));
    }

    @Test
    public void shouldGenerateOverflowingLongStreamWithStep() {
        //noinspection NumericOverflow
        assertThat(Stream.from(Long.MAX_VALUE, 2).take(2))
                .isEqualTo(Stream.of(Long.MAX_VALUE, Long.MAX_VALUE + 2));
    }
    // -- static continually(Supplier)

    @Test
    public void shouldGenerateInfiniteStreamBasedOnSupplier() {
        assertThat(Stream.continually(() -> 1).take(13).reduce(Integer::sum)).isEqualTo(13);
    }

    // -- static iterate(T, Function)

    @Test
    public void shouldGenerateInfiniteStreamBasedOnSupplierWithAccessToPreviousValue() {
        assertThat(Stream.iterate(2, (i) -> i + 2).take(3).reduce(Integer::sum)).isEqualTo(12);
    }

    // -- static iterate(Supplier<Option>)

    @Test
    public void shouldGenerateInfiniteStreamBasedOnOptionSupplier() {
        assertThat(Stream.iterate(() -> Option.of(1)).take(5).reduce(Integer::sum)).isEqualTo(5);
    }

    // -- static continually (T)

    @Test
    public void shouldGenerateInfiniteStreamBasedOnRepeatedElement() {
        assertThat(Stream.continually(2).take(3).reduce(Integer::sum)).isEqualTo(6);
    }

    // -- static cons(T, Supplier)

    @Test
    public void shouldBuildStreamBasedOnHeadAndTailSupplierWithAccessToHead() {
        assertThat(Stream.cons(1, () -> Stream.cons(2, Stream::empty))).isEqualTo(Stream.of(1, 2));
    }

    // -- static narrow

    @Test
    public void shouldNarrowStream() {
        final Stream<Double> doubles = of(1.0d);
        final Stream<Number> numbers = Stream.narrow(doubles);
        final int actual = numbers.append(new BigDecimal("2.0")).sum().intValue();
        assertThat(actual).isEqualTo(3);
    }

    // -- static ofAll

    @Test
    public void shouldReturnSelfWhenIterableIsInstanceOfStream() {
        final Stream<Integer> source = ofAll(1, 2, 3);
        final Stream<Integer> target = Stream.ofAll(source);
        assertThat(target).isSameAs(source);
    }

    @Test
    public void shouldReturnSelfWhenIterableIsInstanceOfListView() {
        final JavaConverters.ListView<Integer, Stream<Integer>> source = JavaConverters
                .asJava(ofAll(1, 2, 3), JavaConverters.ChangePolicy.IMMUTABLE);
        final Stream<Integer> target = Stream.ofAll(source);
        assertThat(target).isSameAs(source.getDelegate());
    }

    // -- append

    @Test
    public void shouldAppendMillionTimes() {
        final int bigNum = 1_000_000;
        assertThat(Stream.range(0, bigNum).foldLeft(Stream.empty(), Stream::append).length()).isEqualTo(bigNum);
    }

    // -- appendAll

    @Test
    public void shouldAppendAll() {
        assertThat(of(1, 2, 3).appendAll(of(4, 5, 6))).isEqualTo(of(1, 2, 3, 4, 5, 6));
    }

    @Test
    public void shouldAppendAllIfThisIsEmpty() {
        assertThat(empty().appendAll(of(4, 5, 6))).isEqualTo(of(4, 5, 6));
    }

    @Test
    public void shouldAppendAllIfThatIsInfinite() {
        assertThat(of(1, 2, 3).appendAll(Stream.from(4)).take(6)).isEqualTo(of(1, 2, 3, 4, 5, 6));
    }

    @Test
    public void shouldAppendAllToInfiniteStream() {
        assertThat(Stream.from(1).appendAll(Stream.continually(() -> -1)).take(6)).isEqualTo(of(1, 2, 3, 4, 5, 6));
    }

    // -- combinations

    @Test
    public void shouldComputeCombinationsOfEmptyStream() {
        assertThat(Stream.empty().combinations()).isEqualTo(Stream.of(Stream.empty()));
    }

    @Test
    public void shouldComputeCombinationsOfNonEmptyStream() {
        assertThat(Stream.of(1, 2, 3).combinations()).isEqualTo(Stream.of(Stream.empty(), Stream.of(1), Stream.of(2),
                Stream.of(3), Stream.of(1, 2), Stream.of(1, 3), Stream.of(2, 3), Stream.of(1, 2, 3)));
    }

    // -- combinations(k)

    @Test
    public void shouldComputeKCombinationsOfEmptyStream() {
        assertThat(Stream.empty().combinations(1)).isEqualTo(Stream.empty());
    }

    @Test
    public void shouldComputeKCombinationsOfNonEmptyStream() {
        assertThat(Stream.of(1, 2, 3).combinations(2))
                .isEqualTo(Stream.of(Stream.of(1, 2), Stream.of(1, 3), Stream.of(2, 3)));
    }

    // -- flatMap

    @Test
    public void shouldFlatMapInfiniteTraversable() {
        assertThat(Stream.iterate(1, i -> i + 1).flatMap(i -> List.of(i, 2 * i)).take(7))
                .isEqualTo(Stream.of(1, 2, 2, 4, 3, 6, 4));
    }

    // -- partition

    @Test
    public void shouldPartitionInTwoIterations() {
        final AtomicInteger count = new AtomicInteger(0);
        final Tuple2<Stream<Integer>, Stream<Integer>> results = Stream.of(1, 2, 3).partition(i -> {
            count.incrementAndGet();
            return true;
        });
        assertThat(results._1).isEqualTo(of(1, 2, 3));
        assertThat(results._2).isEqualTo(of());
        assertThat(count.get()).isEqualTo(6);
    }

    @Test
    public void shouldPartitionLazily() {
        final java.util.Set<Integer> itemsCalled = new java.util.HashSet<>();

        final Stream<Integer> infiniteStream = Stream.iterate(0, i -> i + 1);
        assertThat(itemsCalled).isEmpty();

        final Tuple2<Stream<Integer>, Stream<Integer>> results = infiniteStream.partition(i -> {
            itemsCalled.add(i);
            return i % 2 == 0;
        });
        assertThat(itemsCalled).containsExactly(0, 1);
        assertThat(results._1.head()).isEqualTo(0);
        assertThat(results._2.head()).isEqualTo(1);
        assertThat(results._1.take(3)).isEqualTo(of(0, 2, 4));
        assertThat(results._2.take(3)).isEqualTo(of(1, 3, 5));
        assertThat(itemsCalled).containsExactly(0, 1, 2, 3, 4, 5);
    }

    // -- peek

    @Override
    protected int getPeekNonNilPerformingAnAction() {
        return 3;
    }

    // -- permutations

    @Test
    public void shouldComputePermutationsOfEmptyStream() {
        assertThat(Stream.empty().permutations()).isEqualTo(Stream.empty());
    }

    @Test
    public void shouldComputePermutationsOfNonEmptyStream() {
        assertThat(Stream.of(1, 2, 3).permutations()).isEqualTo(Stream.ofAll(Stream.of(Stream.of(1, 2, 3),
                Stream.of(1, 3, 2), Stream.of(2, 1, 3), Stream.of(2, 3, 1), Stream.of(3, 1, 2), Stream.of(3, 2, 1))));
    }

    // -- appendSelf

    @Test
    public void shouldRecurrentlyCalculateFibonacci() {
        assertThat(Stream.of(1, 1).appendSelf(self -> self.zip(self.tail()).map(t -> t._1 + t._2)).take(10))
                .isEqualTo(Stream.of(1, 1, 2, 3, 5, 8, 13, 21, 34, 55));
    }

    @Test
    public void shouldRecurrentlyCalculatePrimes() {
        assertThat(Stream
                .of(2)
                .appendSelf(self -> Stream
                        .iterate(3, i -> i + 2)
                        .filter(i -> self.takeWhile(j -> j * j <= i).forAll(k -> i % k > 0)))
                .take(10)).isEqualTo(Stream.of(2, 3, 5, 7, 11, 13, 17, 19, 23, 29));
    }

    @Test
    public void shouldDoNothingOnNil() {
        assertThat(Stream.empty().appendSelf(self -> self)).isEqualTo(Stream.empty());
    }

    @Test
    public void shouldRecurrentlyCalculateArithmeticProgression() {
        assertThat(Stream.of(1).appendSelf(self -> self.map(t -> t + 1)).take(4)).isEqualTo(Stream.of(1, 2, 3, 4));
    }

    @Test
    public void shouldRecurrentlyCalculateGeometricProgression() {
        assertThat(Stream.of(1).appendSelf(self -> self.map(t -> t * 2)).take(4)).isEqualTo(Stream.of(1, 2, 4, 8));
    }

    // -- containsSlice

    @Test
    public void shouldRecognizeInfiniteDoesContainSlice() {
        final boolean actual = Stream.iterate(1, i -> i + 1).containsSlice(of(12, 13, 14));
        assertThat(actual).isTrue();
    }

    // -- cycle

    @Test
    public void shouldCycleEmptyStream() {
        assertThat(empty().cycle()).isEqualTo(empty());
    }

    @Test
    public void shouldCycleNonEmptyStream() {
        assertThat(of(1, 2, 3).cycle().take(9)).isEqualTo(of(1, 2, 3, 1, 2, 3, 1, 2, 3));
    }

    // -- cycle(int)

    @Test
    public void shouldCycleTimesEmptyStream() {
        assertThat(empty().cycle(3)).isEqualTo(empty());
    }

    @Test
    public void shouldCycleTimesNonEmptyStream() {
        assertThat(of(1, 2, 3).cycle(-1)).isEqualTo(empty());
        assertThat(of(1, 2, 3).cycle(0)).isEqualTo(empty());
        assertThat(of(1, 2, 3).cycle(1)).isEqualTo(of(1, 2, 3));
        assertThat(of(1, 2, 3).cycle(3)).isEqualTo(of(1, 2, 3, 1, 2, 3, 1, 2, 3));
    }

    // -- dropUntil

    @Test
    public void shouldDropInfiniteStreamUntilPredicate() {
        final Stream<Integer> naturalNumbers = Stream.iterate(0, i -> i + 1);
        final Stream<Integer> naturalNumbersBiggerThanTen = naturalNumbers.dropUntil(i -> i > 10);
        final Integer firstNaturalNumberBiggerThanTen = naturalNumbersBiggerThanTen.head();
        assertThat(firstNaturalNumberBiggerThanTen).isEqualTo(11);
    }

    // -- dropRight

    @Test
    public void shouldLazyDropRight() {
        assertThat(Stream.from(1).takeUntil(i -> i == 18).dropRight(7)).isEqualTo(Stream.range(1, 11));
    }

    // -- extend

    @Test
    public void shouldExtendStreamWithConstantValue() {
        assertThat(Stream.of(1, 2, 3).extend(42).take(6)).isEqualTo(of(1, 2, 3, 42, 42, 42));
    }

    @Test
    public void shouldExtendStreamWithSupplier() {
        assertThat(Stream.of(1, 2, 3).extend(() -> 42).take(6)).isEqualTo(of(1, 2, 3, 42, 42, 42));
    }

    @Test
    public void shouldExtendStreamWithFunction() {
        assertThat(Stream.of(1, 2, 3).extend(i -> i + 1).take(6)).isEqualTo(of(1, 2, 3, 4, 5, 6));
    }

    @Test
    public void shouldExtendEmptyStreamWithConstantValue() {
        assertThat(Stream.of().extend(42).take(6)).isEqualTo(of(42, 42, 42, 42, 42, 42));
    }

    @Test
    public void shouldExtendEmptyStreamWithSupplier() {
        assertThat(Stream.of().extend(() -> 42).take(6)).isEqualTo(of(42, 42, 42, 42, 42, 42));
    }

    @Test
    public void shouldReturnAnEmptyStreamWhenExtendingAnEmptyStreamWithFunction() {
        assertThat(Stream.<Integer> of().extend(i -> i + 1)).isEqualTo(of());
    }

    @Test
    public void shouldReturnTheOriginalStreamWhenTryingToExtendInfiniteStreamWithConstantValue() {
        assertThat(Stream.continually(1).extend(42).take(6)).isEqualTo(of(1, 1, 1, 1, 1, 1));
    }

    @Test
    public void shouldReturnTheOriginalStreamWhenTryingToExtendInfiniteStreamWithSupplier() {
        assertThat(Stream.continually(1).extend(() -> 42).take(6)).isEqualTo(of(1, 1, 1, 1, 1, 1));
    }

    @Test
    public void shouldReturnTheOriginalStreamWhenTryingToExtendInfiniteStreamWithFunction() {
        assertThat(Stream.continually(1).extend(i -> i + 1).take(6)).isEqualTo(of(1, 1, 1, 1, 1, 1));
    }

    // -- isDefinedAt

    @Test
    public void shouldBeDefinedAtNonNegativeIndexWhenInfinitelyLong() {
        assertThat(Stream.continually(1).asPartialFunction().isDefinedAt(1)).isTrue();
    }

    // -- isLazy

    @Override
    @Test
    public void shouldVerifyLazyProperty() {
        assertThat(empty().isLazy()).isTrue();
        assertThat(of(1).isLazy()).isTrue();
    }

    // -- subSequence(int, int)

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceIfSubSequenceStartsAtZeroAndEndsAtLastElement() {
        // Stream is lazy
    }

    // -- tail

    @Test
    public void shouldEvaluateTailAtMostOnce() {
        final int[] counter = { 0 };
        final Stream<Integer> stream = Stream.continually(() -> counter[0]++);
        // this test ensures that the `tail.append(100)` does not modify the tail elements
        final Stream<Integer> tail = stream.tail().append(100);
        final String expected = stream.drop(1).take(3).mkString(",");
        final String actual = tail.take(3).mkString(",");
        assertThat(expected).isEqualTo("1,2,3");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldNotProduceStackOverflow() {
        Stream.range(0, 1_000_000)
                .map(String::valueOf)
                .foldLeft(Stream.<String> empty(), Stream::append)
                .mkString();
    }

    @Test // See #327, #594
    public void shouldNotEvaluateHeadOfTailWhenCallingIteratorHasNext() {

        final Integer[] vals = new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };

        final CheckedFunction2<StringBuilder, Integer, Void> doStuff = (builder, i) -> {
            builder.append(i);
            if (i == 5) {
                throw new Exception("Some error !!!");
            } else {
                return null;
            }
        };

        final StringBuilder actual = new StringBuilder();
        final CheckedFunction1<Integer, Void> consumer1 = doStuff.apply(actual);
        Stream.of(vals)
                .map(v -> Try.run(() -> consumer1.apply(v)))
                .find(Try::isFailure)
                .getOrElse(() -> Try.success(null));

        final StringBuilder expected = new StringBuilder();
        final CheckedFunction1<Integer, Void> consumer2 = doStuff.apply(expected);
        java.util.stream.Stream.of(vals)
                .map(v -> Try.run(() -> consumer2.apply(v)))
                .filter(Try::isFailure)
                .findFirst()
                .orElseGet(() -> Try.success(null));

        assertThat(actual.toString()).isEqualTo(expected.toString());
    }

    // -- take

    @Test
    public void shouldNotEvaluateNPlusOneWhenTakeN() {
        final Predicate<Integer> hiddenThrow = i -> {
            if (i == 0) {
                return true;
            } else {
                throw new IllegalArgumentException();
            }
        };
        assertThat(Stream.from(0).filter(hiddenThrow).take(1).sum().intValue()).isEqualTo(0);
    }

    @Ignore
    @Test
    public void shouldTakeZeroOfEmptyFilteredInfiniteStream() {
        assertThat(Stream.continually(1).filter(i -> false).take(0).isEmpty()).isTrue();
    }

    @Ignore
    @Test
    public void shouldTakeZeroOfEmptyFlatMappedInfiniteStream() {
        assertThat(Stream.continually(1).flatMap(i -> Stream.empty()).take(0).isEmpty()).isTrue();
    }

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceIfTakeAll() {
        // the size of a possibly infinite stream is unknown
    }

    // -- toStream

    @Test
    public void shouldReturnSelfOnConvertToStream() {
        final Stream<Integer> testee = of(1, 2, 3);
        assertThat(testee.toStream()).isSameAs(testee);
    }

    // -- toString

    @Test
    public void shouldStringifyNil() {
        assertThat(empty().toString()).isEqualTo("Stream()");
    }

    @Test
    public void shouldStringifyNonNil() {
        assertThat(of(1, 2, 3).toString()).isEqualTo("Stream(1, ?)");
    }

    @Test
    public void shouldStringifyNonNilEvaluatingFirstTail() {
        final Stream<Integer> stream = this.of(1, 2, 3);
        stream.tail(); // evaluates second head element
        assertThat(stream.toString()).isEqualTo("Stream(1, 2, ?)");
    }

    @Test
    public void shouldStringifyNonNilAndNilTail() {
        final Stream<Integer> stream = this.of(1);
        stream.tail(); // evaluates empty tail
        assertThat(stream.toString()).isEqualTo("Stream(1)");
    }

    // -- transform()

    @Test
    public void shouldTransform() {
        String transformed = of(42).transform(v -> String.valueOf(v.head()));
        assertThat(transformed).isEqualTo("42");
    }

    // -- unfold

    @Test
    public void shouldUnfoldRightToEmpty() {
        assertThat(Stream.unfoldRight(0, x -> Option.none())).isEqualTo(empty());
    }

    @Test
    public void shouldUnfoldRightSimpleStream() {
        assertThat(
                Stream.unfoldRight(10, x -> x == 0
                                            ? Option.none()
                                            : Option.of(new Tuple2<>(x, x - 1))))
                .isEqualTo(of(10, 9, 8, 7, 6, 5, 4, 3, 2, 1));
    }

    @Test
    public void shouldUnfoldLeftToEmpty() {
        assertThat(Stream.unfoldLeft(0, x -> Option.none())).isEqualTo(empty());
    }

    @Test
    public void shouldUnfoldLeftSimpleStream() {
        assertThat(
                Stream.unfoldLeft(10, x -> x == 0
                                           ? Option.none()
                                           : Option.of(new Tuple2<>(x - 1, x))))
                .isEqualTo(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void shouldUnfoldToEmpty() {
        assertThat(Stream.unfold(0, x -> Option.none())).isEqualTo(empty());
    }

    @Test
    public void shouldUnfoldSimpleStream() {
        assertThat(
                Stream.unfold(10, x -> x == 0
                                       ? Option.none()
                                       : Option.of(new Tuple2<>(x - 1, x))))
                .isEqualTo(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    // -- Serializable

    @Test(expected = InvalidObjectException.class)
    public void shouldNotSerializeEnclosingClassOfCons() throws Throwable {
        Serializables.callReadObject(Stream.cons(1, Stream::empty));
    }

    @Test(expected = InvalidObjectException.class)
    public void shouldNotDeserializeStreamWithSizeLessThanOne() throws Throwable {
        try {
            /*
             * This implementation is stable regarding jvm impl changes of object serialization. The index of the number
             * of Stream elements is gathered dynamically.
             */
            final byte[] listWithOneElement = Serializables.serialize(Stream.of(0));
            final byte[] listWithTwoElements = Serializables.serialize(Stream.of(0, 0));
            int index = -1;
            for (int i = 0; i < listWithOneElement.length && index == -1; i++) {
                final byte b1 = listWithOneElement[i];
                final byte b2 = listWithTwoElements[i];
                if (b1 != b2) {
                    if (b1 != 1 || b2 != 2) {
                        throw new IllegalStateException("Difference does not indicate number of elements.");
                    } else {
                        index = i;
                    }
                }
            }
            if (index == -1) {
                throw new IllegalStateException("Hack incomplete - index not found");
            }
            /*
             * Hack the serialized data and fake zero elements.
			 */
            listWithOneElement[index] = 0;
            Serializables.deserialize(listWithOneElement);
        } catch (IllegalStateException x) {
            throw (x.getCause() != null) ? x.getCause() : x;
        }
    }

    // -- spliterator

    @Test
    public void shouldNotHaveSizedSpliterator() {
        assertThat(of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.SIZED | Spliterator.SUBSIZED)).isFalse();
    }


    @Test
    public void shouldReturnSizeWhenSpliterator() {
        assertThat(of(1, 2, 3).spliterator().getExactSizeIfKnown()).isEqualTo(-1);
    }

}
