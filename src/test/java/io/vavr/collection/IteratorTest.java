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

import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import io.vavr.control.Option;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static io.vavr.collection.Iterator.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class IteratorTest extends AbstractTraversableTest {

    @Override
    protected String stringPrefix() {
        return "Iterator";
    }

    @Override
    protected <T> IterableAssert<T> assertThat(Iterable<T> actual) {
        return new IterableAssert<T>(actual) {
            @SuppressWarnings("unchecked")
            @Override
            public IterableAssert<T> isEqualTo(Object expected) {
                if (actual instanceof Option) {
                    final Option<?> opt1 = ((Option<?>) actual);
                    final Option<?> opt2 = (Option<?>) expected;
                    Assertions.assertThat(wrapIterator(opt1)).isEqualTo(wrapIterator(opt2));
                    return this;
                } else {
                    final Iterable<T> iterable = (Iterable<T>) expected;
                    Assertions.assertThat(List.ofAll(actual)).isEqualTo(List.ofAll(iterable));
                    return this;
                }
            }

            private Option<?> wrapIterator(Option<?> option) {
                return option.map(o -> (o instanceof Iterator) ? List.ofAll((Iterator<?>) o) : o);
            }
        };
    }

    @Override
    protected <T> ObjectAssert<T> assertThat(T actual) {
        return new ObjectAssert<T>(actual) {
            @Override
            public ObjectAssert<T> isEqualTo(Object expected) {
                if (actual instanceof Tuple2) {
                    final Tuple2<?, ?> t1 = ((Tuple2<?, ?>) actual).map(this::toList);
                    final Tuple2<?, ?> t2 = ((Tuple2<?, ?>) expected).map(this::toList);
                    Assertions.assertThat((Object) t1).isEqualTo(t2);
                    return this;
                } else if (actual instanceof Tuple3) {
                    final Tuple3<?, ?, ?> t1 = ((Tuple3<?, ?, ?>) actual).map(this::toList);
                    final Tuple3<?, ?, ?> t2 = ((Tuple3<?, ?, ?>) expected).map(this::toList);
                    Assertions.assertThat((Object) t1).isEqualTo(t2);
                    return this;
                } else {
                    return super.isEqualTo(expected);
                }
            }

            private Tuple2<Object, Object> toList(Object o1, Object o2) {
                return Tuple.of(wrapIterator(o1), wrapIterator(o2));
            }

            private Tuple3<Object, Object, Object> toList(Object o1, Object o2, Object o3) {
                return Tuple.of(wrapIterator(o1), wrapIterator(o2), wrapIterator(o3));
            }

            private Object wrapIterator(Object o) {
                return (o instanceof Iterator) ? List.ofAll((Iterator<?>) o) : o;
            }
        };
    }

    @Override
    protected <T> Collector<T, ArrayList<T>, ? extends Iterator<T>> collector() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected <T> Iterator<T> empty() {
        return Iterator.empty();
    }

    @Override
    protected <T> Iterator<T> of(T element) {
        return Iterator.of(element);
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    @Override
    protected final <T> Iterator<T> of(T... elements) {
        return Iterator.of(elements);
    }

    @Override
    protected <T> Iterator<T> ofAll(Iterable<? extends T> elements) {
        return Iterator.ofAll(elements);
    }

    @Override
    protected <T extends Comparable<? super T>> Iterator<T> ofJavaStream(java.util.stream.Stream<? extends T> javaStream) {
        return Iterator.ofAll(javaStream.iterator());
    }

    @Override
    protected Iterator<Boolean> ofAll(boolean... elements) {
        return Iterator.ofAll(elements);
    }

    @Override
    protected Iterator<Byte> ofAll(byte... elements) {
        return Iterator.ofAll(elements);
    }

    @Override
    protected Iterator<Character> ofAll(char... elements) {
        return Iterator.ofAll(elements);
    }

    @Override
    protected Iterator<Double> ofAll(double... elements) {
        return Iterator.ofAll(elements);
    }

    @Override
    protected Iterator<Float> ofAll(float... elements) {
        return Iterator.ofAll(elements);
    }

    @Override
    protected Iterator<Integer> ofAll(int... elements) {
        return Iterator.ofAll(elements);
    }

    @Override
    protected Iterator<Long> ofAll(long... elements) {
        return Iterator.ofAll(elements);
    }

    @Override
    protected Iterator<Short> ofAll(short... elements) {
        return Iterator.ofAll(elements);
    }

    @Override
    protected <T> Iterator<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        return Iterator.tabulate(n, f);
    }

    @Override
    protected <T> Iterator<T> fill(int n, Supplier<? extends T> s) {
        return Iterator.fill(n, s);
    }

    protected <T> Iterator<T> fill(int n, T element) {
        return Iterator.fill(n, element);
    }

    @Override
    protected boolean useIsEqualToInsteadOfIsSameAs() {
        return true;
    }

    @Override
    protected int getPeekNonNilPerformingAnAction() {
        return 3;
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldFailOfEmptyArgList() {
        of().next();
    }

    // -- static .empty()

    @Test
    public void shouldCreateEmptyIterator() {
        assertThat(Iterator.empty() instanceof Iterator).isTrue();
    }

    // -- static .of()

    @Test
    public void shouldCreateIteratorOfOneElement() {
        final Iterator<Object> iterator = Iterator.of(1);
        assertThat(iterator).isNotNull();
    }

    // -- static narrow()

    @Test
    public void shouldNarrowIterator() {
        final Iterator<Double> doubles = of(1.0d);
        Iterator<Number> numbers = narrow(doubles);
        numbers = numbers.concat(Iterator.of(new BigDecimal("2.0")));
        final int actual = numbers.sum().intValue();
        assertThat(actual).isEqualTo(3);
    }

    // -- static ofAll()

    @Test(expected = NoSuchElementException.class)
    public void shouldFailOfEmptyIterable() {
        ofAll(List.empty()).next();
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldFailOfEmptyBoolean() {
        ofAll(new boolean[0]).next();
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldFailOfEmptyByte() {
        ofAll(new byte[0]).next();
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldFailOfEmptyChar() {
        ofAll(new char[0]).next();
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldFailOfEmptyDouble() {
        ofAll(new double[0]).next();
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldFailOfEmptyFloat() {
        ofAll(new float[0]).next();
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldFailOfEmptyInt() {
        ofAll(new int[0]).next();
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldFailOfEmptyLong() {
        ofAll(new long[0]).next();
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldFailOfEmptyShort() {
        ofAll(new short[0]).next();
    }

    // -- static concat()

    @Test
    public void shouldConcatEmptyIterableIterable() {
        final Iterable<Iterable<Integer>> empty = List.empty();
        assertThat(concat(empty)).isSameAs(Iterator.empty());
    }

    @Test
    public void shouldConcatNonEmptyIterableIterable() {
        final Iterable<Iterable<Integer>> itIt = List.of(List.of(1, 2), List.of(3));
        assertThat(concat(itIt)).isEqualTo(Iterator.of(1, 2, 3));
    }

    @Test
    public void shouldConcatEmptyArrayIterable() {
        assertThat(concat()).isSameAs(Iterator.empty());
    }

    @Test
    public void shouldConcatNonEmptyArrayIterable() {
        assertThat(concat(List.of(1, 2), List.of(3))).isEqualTo(Iterator.of(1, 2, 3));
    }

    @Test
    public void shouldConcatNestedConcatIterators() {
        assertThat(concat(List.of(1, 2), List.of(3), concat(List.of(4, 5)))).isEqualTo(Iterator.of(1, 2, 3, 4, 5));
        assertThat(concat(concat(List.of(4, 5)), List.of(1, 2), List.of(3))).isEqualTo(Iterator.of(4, 5, 1, 2, 3));
    }

    @Test
    public void shouldConcatToConcatIterator() {
        assertThat(concat(List.of(1, 2)).concat(List.of(3).iterator())).isEqualTo(Iterator.of(1, 2, 3));
    }

    @Test
    public void shouldTraverseElementsOfConcatIteratorHavingEmptyIterator() {
        Iterator<Integer> iterator = concat(List.of(1), List.empty(), List.of(2));
        assertThat(iterator.next()).isEqualTo(1);
        assertThat(iterator.next()).isEqualTo(2);
        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    public void shouldConcatToConcatIteratorAfterAllElementsWereRead() {
        Iterator<Integer> iterator = concat(List.of(1), List.of(2));
        assertThat(iterator.toList()).isEqualTo(List.of(1, 2));
        assertThat(iterator.hasNext()).isFalse();
        iterator = iterator.concat(List.of(3).iterator());
        assertThat(iterator.hasNext()).isTrue();
        assertThat(iterator.toList()).isEqualTo(List.of(3));
        assertThat(iterator.hasNext()).isFalse();
    }

    // -- fill(int, Supplier)

    @Test
    public void shouldReturnManyAfterFillWithConstantSupplier() {
        assertThat(fill(17, () -> 7))
                .hasSize(17);
    }

    // -- fill(int, T)

    @Test
    public void shouldReturnEmptyAfterFillWithZeroCount() {
        assertThat(fill(0, 7)).isEqualTo(empty());
    }

    @Test
    public void shouldReturnEmptyAfterFillWithNegativeCount() {
        assertThat(fill(-1, 7)).isEqualTo(empty());
    }

    @Test
    public void shouldReturnManyAfterFillWithConstant() {
        assertThat(fill(17, 7))
                .hasSize(17);
    }

    // -- concat

    @Test
    public void shouldConcatThisNonEmptyWithEmpty() {
        final Iterator<Integer> it = Iterator.of(1);
        assertThat(it.concat(Iterator.<Integer> empty())).isSameAs(it);
    }

    @Test
    public void shouldConcatThisEmptyWithNonEmpty() {
        final Iterator<Integer> it = Iterator.of(1);
        assertThat(Iterator.<Integer> empty().concat(it)).isSameAs(it);
    }

    @Test
    public void shouldConcatThisNonEmptyWithNonEmpty() {
        assertThat(Iterator.of(1).concat(Iterator.of(2))).isEqualTo(Iterator.of(1, 2));
    }

    // -- transform

    @Test
    public void shouldTransform() {
        final Iterator<?> it = Iterator.of(1, 2).transform(ii -> ii.drop(1));
        assertThat(it).isEqualTo(Iterator.of(2));
    }

    // -- static from(int)

    @Test
    public void shouldGenerateIntStream() {
        assertThat(from(-1).take(3)).isEqualTo(Iterator.of(-1, 0, 1));
    }

    @Test
    public void shouldGenerateOverflowingIntStream() {
        //noinspection NumericOverflow
        assertThat(from(Integer.MAX_VALUE).take(2))
                .isEqualTo(Iterator.of(Integer.MAX_VALUE, Integer.MAX_VALUE + 1));
    }

    // -- static from(int, int)

    @Test
    public void shouldGenerateIntStreamWithStep() {
        assertThat(from(-1, 6).take(3)).isEqualTo(Iterator.of(-1, 5, 11));
    }

    @Test
    public void shouldGenerateOverflowingIntStreamWithStep() {
        //noinspection NumericOverflow
        assertThat(from(Integer.MAX_VALUE, 2).take(2))
                .isEqualTo(Iterator.of(Integer.MAX_VALUE, Integer.MAX_VALUE + 2));
    }

    // -- static from(long)

    @Test
    public void shouldGenerateLongStream() {
        assertThat(from(-1L).take(3)).isEqualTo(Iterator.of(-1L, 0L, 1L));
    }

    @Test
    public void shouldGenerateOverflowingLongStream() {
        //noinspection NumericOverflow
        assertThat(from(Long.MAX_VALUE).take(2)).isEqualTo(Iterator.of(Long.MAX_VALUE, Long.MAX_VALUE + 1));
    }

    // -- static from(long, long)

    @Test
    public void shouldGenerateLongStreamWithStep() {
        assertThat(from(-1L, 5L).take(3)).isEqualTo(Iterator.of(-1L, 4L, 9L));
    }

    @Test
    public void shouldGenerateOverflowingLongStreamWithStep() {
        //noinspection NumericOverflow
        assertThat(from(Long.MAX_VALUE, 2).take(2)).isEqualTo(Iterator.of(Long.MAX_VALUE, Long.MAX_VALUE + 2));
    }

    // -- static continually(Supplier)

    @Test
    public void shouldGenerateInfiniteStreamBasedOnSupplier() {
        assertThat(continually(() -> 1).take(13).reduce((i, j) -> i + j)).isEqualTo(13);
    }

    @Test
    public void shouldGenerateInfiniteStreamBasedOnConstant() {
        assertThat(continually(1).take(13).reduce((i, j) -> i + j)).isEqualTo(13);
    }

    // -- static iterate(T, Function)

    @Test
    public void shouldGenerateInfiniteStreamBasedOnSupplierWithAccessToPreviousValue() {
        assertThat(iterate(2, (i) -> i + 2).take(3).reduce((i, j) -> i + j)).isEqualTo(12);
    }

    @Test
    public void shouldNotCallSupplierUntilNecessary() {
        assertThat(iterate(2, (i) -> {
            throw new RuntimeException();
        }).head()).isEqualTo(2);
    }

    // -- static iterate(Supplier<Option>)

    static class OptionSupplier implements Supplier<Option<Integer>> {

        int cnt;
        final int end;

        OptionSupplier(int start) {
            this(start, Integer.MAX_VALUE);
        }

        OptionSupplier(int start, int end) {
            this.cnt = start;
            this.end = end;
        }

        @Override
        public Option<Integer> get() {
            Option<Integer> res;
            if (cnt < end) {
                res = Option.some(cnt);
            } else {
                res = Option.none();
            }
            cnt++;
            return res;
        }
    }

    @Test
    public void shouldGenerateInfiniteStreamBasedOnOptionSupplier() {
        assertThat(Iterator.iterate(new OptionSupplier(1)).take(5).reduce((i, j) -> i + j)).isEqualTo(15);
    }

    @Test
    public void shouldGenerateFiniteStreamBasedOnOptionSupplier() {
        assertThat(Iterator.iterate(new OptionSupplier(1, 4)).take(50000).reduce((i, j) -> i + j)).isEqualTo(6);
    }

    // -- distinct

    @Test
    public void shouldStayEmptyOnDistinct() {
        assertThat(empty().distinct().toList()).isSameAs(List.empty());
    }

    @Test(/* #2425 */)
    public void shouldNotEatNullOnDistinct() {
        assertThat(of((String) null).distinct().toList()).isEqualTo(List.of((String) null));
    }

    @Test
    public void shouldKeepDistinctElementsOnDistinct() {
        assertThat(of(1, 2, 3).distinct().toList()).isEqualTo(List.of(1, 2, 3));
    }

    @Test
    public void shouldRemoveDuplicatesOnDistinct() {
        assertThat(of(1, 2, 1, 3, 3).distinct().toList()).isEqualTo(List.of(1, 2, 3));
    }

    // -- groupBy

    @Override
    public void shouldNonNilGroupByIdentity() {
        // we can't compare iterators, should map it to sequences
        final Seq<?> actual = of('a', 'b', 'c').groupBy(Function.identity()).map(e -> Tuple.of(e._1, List.ofAll(e._2)));
        final Seq<?> expected = HashMap.of(
                'a', List.ofAll(of('a')),
                'b', List.ofAll(of('b')),
                'c', List.ofAll(of('c'))).toList();
        assertThat(actual).isEqualTo(expected);
    }

    @Override
    public void shouldNonNilGroupByEqual() {
        // we can't compare iterators, should map it to sequences
        final Seq<?> actual = of('a', 'b', 'c').groupBy(c -> 1).map(e -> Tuple.of(e._1, List.ofAll(e._2)));
        final Seq<?> expected = HashMap.of(1, List.ofAll(of('a', 'b', 'c'))).toList();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldCreateDoubleRangeByFromInfinity() {
        assertThat(rangeBy(Double.NEGATIVE_INFINITY, 0.0, 1.0)).startsWith(Double.NEGATIVE_INFINITY, -Double.MAX_VALUE);
        assertThat(rangeBy(Double.POSITIVE_INFINITY, 0.0, -1.0)).startsWith(Double.POSITIVE_INFINITY, Double.MAX_VALUE);
    }

    @Test
    public void shouldCreateDoubleRangeClosedByFromInfinity() {
        assertThat(rangeClosedBy(Double.NEGATIVE_INFINITY, 0.0, 1.0)).startsWith(Double.NEGATIVE_INFINITY, -Double.MAX_VALUE);
        assertThat(rangeClosedBy(Double.POSITIVE_INFINITY, 0.0, -1.0)).startsWith(Double.POSITIVE_INFINITY, Double.MAX_VALUE);
    }

    @Test
    public void shouldCreateDoubleRangeByFromMaxToInfinity() {
        assertThat(rangeBy(Double.MAX_VALUE, Double.POSITIVE_INFINITY, 3E307)).isEqualTo(of(Double.MAX_VALUE));
        assertThat(rangeBy(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY, -3E307)).isEqualTo(of(-Double.MAX_VALUE));
    }

    @Test
    public void shouldCreateDoubleRangeClosedByFromMaxToInfinity() {
        assertThat(rangeClosedBy(Double.MAX_VALUE, Double.POSITIVE_INFINITY, 3E307)).isEqualTo(of(Double.MAX_VALUE));
        assertThat(rangeClosedBy(-Double.MAX_VALUE, Double.NEGATIVE_INFINITY, -3E307)).isEqualTo(of(-Double.MAX_VALUE));
    }

    @Test
    @Override
    public void shouldTakeUntilAllOnFalseCondition() {
        final Iterator<Integer> actual = of(1, 2, 3).takeUntil(x -> false);
        assertThat(actual).isEqualTo(of(1, 2, 3));
        assertThat(actual.hasNext()).isFalse();
    }

    @Test
    public void shouldTakeUntilAllOnTrueCondition() {
        final Iterator<Integer> actual = of(1, 2, 3).takeUntil(x -> true);
        assertThat(actual).isEqualTo(empty());
        assertThat(actual.hasNext()).isFalse();
    }

    // -- hasNext

    @Test
    public void shouldNotHaveNextWhenEmpty() {
        assertThat(Iterator.empty().hasNext()).isFalse();
    }

    @Test
    public void shouldHaveNextWhenIteratorOfOneElementAndNextWasNotCalled() {
        assertThat(Iterator.of(1).hasNext()).isTrue();
    }

    @Test
    public void shouldNotHaveNextWhenIteratorOfOneElementAndNextWasCalled() {
        final Iterator<Object> iterator = Iterator.of(1);
        iterator.next();
        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    public void multipleHasNext() {
        multipleHasNext(() -> Iterator.of(1));
        multipleHasNext(() -> Iterator.of(1, 2, 3));
        multipleHasNext(() -> Iterator.ofAll(true, true, false, true));
        multipleHasNext(() -> Iterator.ofAll(new byte[] {1, 2, 3, 4}));
        multipleHasNext(() -> Iterator.ofAll(new char[] {1, 2, 3, 4}));
        multipleHasNext(() -> Iterator.ofAll(new double[] {1, 2, 3, 4}));
        multipleHasNext(() -> Iterator.ofAll(new float[] {1, 2, 3, 4}));
        multipleHasNext(() -> Iterator.ofAll(1, 2, 3, 4));
        multipleHasNext(() -> Iterator.ofAll(new long[] {1, 2, 3, 4}));
        multipleHasNext(() -> Iterator.ofAll(new short[] {1, 2, 3, 4}));
        multipleHasNext(() -> Iterator.ofAll(Iterator.of(1, 2, 3).toJavaList().iterator()));
        multipleHasNext(() -> Iterator.ofAll(Iterator.of(1, 2, 3).toJavaList()));

        multipleHasNext(() -> Iterator.concat(List.of(Iterator.empty(), Iterator.of(1, 2, 3))));
        multipleHasNext(() -> Iterator.concat(List.of(Iterator.of(1, 2, 3), Iterator.of(1, 2, 3))));
        multipleHasNext(() -> Iterator.concat(Iterator.of(1, 2, 3), Iterator.of(1, 2, 3)));
        multipleHasNext(() -> Iterator.continually(() -> 1), 5);
        multipleHasNext(() -> Iterator.continually(1), 5);
        multipleHasNext(() -> Iterator.fill(3, () -> 1));
        multipleHasNext(() -> Iterator.from(1), 5);
        multipleHasNext(() -> Iterator.from(1, 2), 5);
        multipleHasNext(() -> Iterator.from(1L), 5);
        multipleHasNext(() -> Iterator.from(1L, 2L), 5);
        multipleHasNext(() -> Iterator.iterate(1, i -> i + 1), 5);
        multipleHasNext(() -> Iterator.iterate(new OptionSupplier(1)), 5);
        multipleHasNext(() -> Iterator.tabulate(10, i -> i + 1));
        multipleHasNext(() -> Iterator.unfold(10, x -> x == 0 ? Option.none() : Option.of(new Tuple2<>(x - 1, x))));
        multipleHasNext(() -> Iterator.unfoldLeft(10, x -> x == 0 ? Option.none() : Option.of(new Tuple2<>(x - 1, x))));
        multipleHasNext(() -> Iterator.unfoldRight(10, x -> x == 0 ? Option.none() : Option.of(new Tuple2<>(x, x - 1))));

        multipleHasNext(() -> Iterator.range('a', 'd'));
        multipleHasNext(() -> Iterator.range(1, 4));
        multipleHasNext(() -> Iterator.range(1L, 4L));
        multipleHasNext(() -> Iterator.rangeClosed('a', 'd'));
        multipleHasNext(() -> Iterator.rangeClosed(1, 4));
        multipleHasNext(() -> Iterator.rangeClosed(1L, 4L));
        multipleHasNext(() -> Iterator.rangeBy('a', 'd', 1));
        multipleHasNext(() -> Iterator.rangeBy(1, 4, 1));
        multipleHasNext(() -> Iterator.rangeBy(1d, 4d, 1));
        multipleHasNext(() -> Iterator.rangeBy(1L, 4L, 1));
        multipleHasNext(() -> Iterator.rangeClosedBy('a', 'd', 1));
        multipleHasNext(() -> Iterator.rangeClosedBy(1, 4, 1));
        multipleHasNext(() -> Iterator.rangeClosedBy(1d, 4d, 1));
        multipleHasNext(() -> Iterator.rangeClosedBy(1L, 4L, 1));

        multipleHasNext(() -> Iterator.of(1, 2, 3).concat(Iterator.of(1, 2, 3)));
        multipleHasNext(() -> Iterator.of(1, 2, 1, 2, 1, 2).distinct());
        multipleHasNext(() -> Iterator.of(1, 2, 1, 2, 1, 2).distinctBy(e -> e % 2));
        multipleHasNext(() -> Iterator.of(1, 2, 1, 2, 1, 2).distinctBy(Comparator.comparingInt(e -> e % 2)));
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).drop(1));
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).dropRight(1));
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).dropUntil(e -> e == 3));
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).dropWhile(e -> e == 1));
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).filter(e -> e > 1));
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).filterNot(e -> e <= 1));
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).flatMap(e -> Iterator.of(e, e + 1)));
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).grouped(2));
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).intersperse(-1));
        multipleHasNext(() -> Iterator.of(1, 2, 3).map(i -> i * 2));
        multipleHasNext(() -> Iterator.of(1, 2, 3).partition(i -> i < 2)._1);
        multipleHasNext(() -> Iterator.of(1, 2, 3).partition(i -> i < 2)._2);
        multipleHasNext(() -> Iterator.of(1, 2, 3, 2).replace(2, 42));
        multipleHasNext(() -> Iterator.of(1, 2, 3, 2).replaceAll(2, 42));
        multipleHasNext(() -> Iterator.of(1, 2, 3).retainAll(List.of(2)));
        multipleHasNext(() -> Iterator.of(1, 2, 3).scanLeft(1, (a, b) -> a + b));
        multipleHasNext(() -> Iterator.of(1, 2, 3).scanRight(1, (a, b) -> a + b));
        multipleHasNext(() -> Iterator.of(1, 2, 3).slideBy(Function.identity()));
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).sliding(2));
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).sliding(2, 1));
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).unzip(i -> i, i -> i + 1)._1);
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).unzip(i -> i, i -> i + 1)._2);
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).unzip3(i -> i, i -> i + 1, i -> i + 2)._1);
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).unzip3(i -> i, i -> i + 1, i -> i + 2)._2);
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).unzip3(i -> i, i -> i + 1, i -> i + 2)._3);
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).zip(Iterator.from(1)));
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).zipAll(Iterator.of(1, 2), -1, -2));
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).zipWith(Iterator.of(1, 2), (a, b) -> a + b));
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).zipWithIndex());
        multipleHasNext(() -> Iterator.of(1, 2, 3, 4).zipWithIndex((a, i) -> a + i));
    }

    private <T> void multipleHasNext(Supplier<Iterator<T>> it) {
        multipleHasNext(it, -1);
    }

    private <T> void multipleHasNext(Supplier<Iterator<T>> it, int maxLen) {
        final Iterator<T> testee1 = it.get();
        final Iterator<T> testee2 = it.get();
        // ask 2 times
        assertThat(testee2.hasNext()).isTrue();
        assertThat(testee2.hasNext()).isTrue();
        // results should be still the same
        if (maxLen >= 0) {
            assertThat(testee1.take(maxLen).toList()).isEqualTo(testee2.take(maxLen).toList());
        } else {
            assertThat(testee1.toList()).isEqualTo(testee2.toList());
        }
    }

    // -- next()

    @Test
    public void shouldThrowOnNextWhenEmpty() {
        assertThatThrownBy(Iterator.empty()::next).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void shouldReturnValueOnNextWhenIteratorOfOneElementAndNextWasNotCalled() {
        final Iterator<Object> iterator = Iterator.of(1);
        assertThat(iterator.next()).isSameAs(1);
    }

    @Test
    public void shouldThrowOnNextWhenIteratorOfOneElementAndNextWasCalled() {
        final Iterator<Object> iterator = Iterator.of(1);
        iterator.next();
        assertThatThrownBy(iterator::next).isInstanceOf(NoSuchElementException.class);
    }

    // -- nextOption()

    @Test
    public void shouldReturnNoneOnNextOptionWhenEmpty() {
        assertThat(Iterator.empty().nextOption()).isSameAs(Option.none());
    }

    @Test
    public void shouldReturnSomeOnNextOptionWhenIteratorOfOneElement() {
        assertThat(Iterator.of(1).nextOption()).isEqualTo(Option.some(1));
    }

    // -- partition()

    @Test
    public void shouldPartition() {
        final Tuple2<Iterator<String>, Iterator<String>> partitions = of("1", "2", "3").partition("2"::equals);
        assertThat(String.join(", ", partitions._1)).isEqualTo("2");
        assertThat(String.join(", ", partitions._2)).isEqualTo("1, 3");
    }

    @Test(timeout = 5_000L)  // avoid endless test caused by infinite iterator
    public void shouldPartitionLazily() {
        final java.util.List<Integer> itemsCalled = new java.util.ArrayList<>();

        // Given an infinite iterator
        final Iterator<Integer> iterator = Iterator.iterate(1, i -> {
            itemsCalled.add(i);
            return i + 1;
        });

        // When partitioning it
        // Then the partitioning is done lazily (otherwise the test will timeout)
        final Tuple2<Iterator<Integer>, Iterator<Integer>> partitions = iterator.partition(i -> i % 2 == 0);
        assertThat(itemsCalled).isEmpty();

        // When moving forwards iterators
        // Then the moves are done as expected
        assertThat(partitions._1.hasNext()).isTrue();
        assertThat(partitions._1.next()).isEqualTo(2);
        for (int i : of(1, 3, 5)) {
            assertThat(partitions._2.hasNext()).isTrue();
            assertThat(partitions._2.next()).isEqualTo(i);
        }
        assertThat(itemsCalled).containsExactly(1, 2, 3, 4);
    }

    // -- .toString()

    @Test
    public void shouldBeReliableToStringWhenEmpty() {
        assertThat(Iterator.empty().toString()).isEqualTo("EmptyIterator");
    }

    @Test
    public void shouldBeReliableToStringWhenIteratorOfOneElement() {
        assertThat(Iterator.of(1).toString()).isEqualTo("SingletonIterator");
    }

    // -- toSeq

    @Test
    public void shouldConvertToEmptySeq() {
        assertThat(Iterator.<Integer>of().toSeq()).isEmpty();
    }


    @Test
    public void shouldConvertToSeq() {
        assertThat(Iterator.of(1,2,3).toSeq()).containsExactly(1, 2, 3);
    }

    // -- unfoldRight()

    @Test
    public void shouldUnfoldRightToEmpty() {
        assertThat(Iterator.unfoldRight(0, x -> Option.none())).isEqualTo(empty());
    }

    @Test
    public void shouldUnfoldRightSimpleList() {
        assertThat(
                Iterator.unfoldRight(10, x -> x == 0
                                              ? Option.none()
                                              : Option.of(new Tuple2<>(x, x - 1))))
                .isEqualTo(of(10, 9, 8, 7, 6, 5, 4, 3, 2, 1));
    }

    @Test
    public void shouldUnfoldLeftToEmpty() {
        assertThat(Iterator.unfoldLeft(0, x -> Option.none())).isEqualTo(empty());
    }

    @Test
    public void shouldUnfoldLeftSimpleList() {
        assertThat(
                Iterator.unfoldLeft(10, x -> x == 0
                                             ? Option.none()
                                             : Option.of(new Tuple2<>(x - 1, x))))
                .isEqualTo(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void shouldUnfoldToEmpty() {
        assertThat(Iterator.unfold(0, x -> Option.none())).isEqualTo(empty());
    }

    @Test
    public void shouldUnfoldSimpleList() {
        assertThat(
                Iterator.unfold(10, x -> x == 0
                                         ? Option.none()
                                         : Option.of(new Tuple2<>(x - 1, x))))
                .isEqualTo(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    // -- class initialization (see #1773)

    @Test(timeout = 5_000)
    public void shouldNotDeadlockOnConcurrentClassInitialization() {
        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        executorService.execute(new ClassInitializer("io.vavr.collection.Seq"));
        executorService.execute(new ClassInitializer("io.vavr.collection.List"));
        executorService.shutdown();
        // try to access Vavr List and it will hang
        List.empty().forEach(System.out::println);
    }

    static class ClassInitializer implements Runnable {

        private String type;

        ClassInitializer(String type) {
            this.type = type;
        }

        @Override
        public void run() {
            try {
                Class.forName(type);
            } catch (ClassNotFoundException e) {
                throw new Error(e);
            }
        }
    }

    // ~~~~~~~ DISABLED TESTS ~~~~~~~

    // -- equals

    @Ignore
    @Override
    @Test
    public void shouldRecognizeEqualityOfNonNils() {
        // a equals impl would enforce evaluation which is not wanted
    }

    @Ignore
    @Override
    @Test
    public void shouldRecognizeEqualObjects() {
        // Iterator equality undefined
    }

    @Ignore
    @Override
    @Test
    public void shouldRecognizeUnequalObjects() {
        // Iterator equality undefined
    }

    // -- hashCode()

    @Ignore
    @Override
    @Test
    public void shouldCalculateHashCodeOfNonNil() {
        // a hashCode impl would enforce evaluation which is not wanted
    }

    @Ignore
    @Override
    @Test
    public void shouldCalculateDifferentHashCodesForDifferentTraversables() {
        // a hashCode impl would enforce evaluation which is not wanted
    }

    @Ignore
    @Override
    @Test
    public void shouldComputeHashCodeOfEmpty() {
        // a hashCode impl would enforce evaluation which is not wanted
    }

    // -- isLazy()

    @Override
    @Test
    public void shouldVerifyLazyProperty() {
        assertThat(empty().isLazy()).isTrue();
        assertThat(of(1).isLazy()).isTrue();
    }

    // -- take()

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceIfTakeAll() {
        // take consumes the Iterator
    }

    // -- takeRight()

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceIfTakeRightAll() {
        // takeRight consumes the Iterator
    }

    // -- toString()

    @Ignore
    @Override
    @Test
    public void shouldConformEmptyStringRepresentation() {
        // used for Traversables, not for Iterators
    }

    @Ignore
    @Override
    @Test
    public void shouldConformNonEmptyStringRepresentation() {
        // used for Traversables, not for Iterators
    }

    // -- spliterator()

    @Test
    public void shouldHaveOrderedSpliterator() {
        assertThat(of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.ORDERED)).isTrue();
    }

    @Test
    public void shouldNotHaveSortedSpliterator() {
        assertThat(of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.SORTED)).isFalse();
    }

    @Test
    public void shouldNotHaveSizedSpliterator() {
        assertThat(of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.SIZED | Spliterator.SUBSIZED)).isFalse();
    }

    @Test
    public void shouldNotHaveDistinctSpliterator() {
        assertThat(of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.DISTINCT)).isFalse();
    }

    @Test
    public void shouldNotReturnSizeWhenSpliterator() {
        assertThat(of(1, 2, 3).spliterator().getExactSizeIfKnown()).isEqualTo(-1);
    }

    // -- isSequential()

    @Test
    public void shouldReturnTrueWhenIsSequentialCalled() {
        assertThat(of(1, 2, 3).isSequential()).isTrue();
    }

}
