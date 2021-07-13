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
import org.assertj.core.api.*;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static org.junit.Assert.assertTrue;

public class HashSetTest extends AbstractSetTest {

    @Override
    protected String stringPrefix() {
        return "HashSet";
    }

    @Override
    protected <T> IterableAssert<T> assertThat(Iterable<T> actual) {
        return new IterableAssert<T>(actual) {
            @Override
            public IterableAssert<T> isEqualTo(Object obj) {
                @SuppressWarnings("unchecked")
                final Iterable<T> expected = (Iterable<T>) obj;
                final java.util.Map<T, Integer> actualMap = countMap(actual);
                final java.util.Map<T, Integer> expectedMap = countMap(expected);
                assertThat(actualMap.size()).isEqualTo(expectedMap.size());
                actualMap.keySet().forEach(k -> assertThat(actualMap.get(k)).isEqualTo(expectedMap.get(k)));
                return this;
            }

            private java.util.Map<T, Integer> countMap(Iterable<? extends T> it) {
                final java.util.HashMap<T, Integer> cnt = new java.util.HashMap<>();
                it.forEach(i -> cnt.merge(i, 1, (v1, v2) -> v1 + v2));
                return cnt;
            }
        };
    }

    @Override
    protected <T> ObjectAssert<T> assertThat(T actual) {
        return new ObjectAssert<T>(actual) {
        };
    }

    @Override
    protected BooleanAssert assertThat(Boolean actual) {
        return new BooleanAssert(actual) {
        };
    }

    @Override
    protected DoubleAssert assertThat(Double actual) {
        return new DoubleAssert(actual) {
        };
    }

    @Override
    protected IntegerAssert assertThat(Integer actual) {
        return new IntegerAssert(actual) {
        };
    }

    @Override
    protected LongAssert assertThat(Long actual) {
        return new LongAssert(actual) {
        };
    }

    @Override
    protected StringAssert assertThat(String actual) {
        return new StringAssert(actual) {
        };
    }

    // -- construction

    @Override
    protected <T> Collector<T, ArrayList<T>, HashSet<T>> collector() {
        return HashSet.collector();
    }

    @Override
    protected <T> HashSet<T> empty() {
        return HashSet.empty();
    }

    @Override
    protected <T> HashSet<T> emptyWithNull() {
        return empty();
    }

    @Override
    protected <T> HashSet<T> of(T element) {
        return HashSet.of(element);
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    @Override
    protected final <T> HashSet<T> of(T... elements) {
        return HashSet.of(elements);
    }

    @Override
    protected <T> HashSet<T> ofAll(Iterable<? extends T> elements) {
        return HashSet.ofAll(elements);
    }

    @Override
    protected <T extends Comparable<? super T>> HashSet<T> ofJavaStream(java.util.stream.Stream<? extends T> javaStream) {
        return HashSet.ofAll(javaStream);
    }

    @Override
    protected HashSet<Boolean> ofAll(boolean... elements) {
        return HashSet.ofAll(elements);
    }

    @Override
    protected HashSet<Byte> ofAll(byte... elements) {
        return HashSet.ofAll(elements);
    }

    @Override
    protected HashSet<Character> ofAll(char... elements) {
        return HashSet.ofAll(elements);
    }

    @Override
    protected HashSet<Double> ofAll(double... elements) {
        return HashSet.ofAll(elements);
    }

    @Override
    protected HashSet<Float> ofAll(float... elements) {
        return HashSet.ofAll(elements);
    }

    @Override
    protected HashSet<Integer> ofAll(int... elements) {
        return HashSet.ofAll(elements);
    }

    @Override
    protected HashSet<Long> ofAll(long... elements) {
        return HashSet.ofAll(elements);
    }

    @Override
    protected HashSet<Short> ofAll(short... elements) {
        return HashSet.ofAll(elements);
    }

    @Override
    protected <T> HashSet<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        return HashSet.tabulate(n, f);
    }

    @Override
    protected <T> HashSet<T> fill(int n, Supplier<? extends T> s) {
        return HashSet.fill(n, s);
    }

    @Override
    protected int getPeekNonNilPerformingAnAction() {
        return 1;
    }

    // -- static narrow

    @Test
    public void shouldNarrowHashSet() {
        final HashSet<Double> doubles = of(1.0d);
        final HashSet<Number> numbers = HashSet.narrow(doubles);
        final int actual = numbers.add(new BigDecimal("2.0")).sum().intValue();
        assertThat(actual).isEqualTo(3);
    }

    // -- slideBy is not expected to work for larger subsequences, due to unspecified iteration order
    @Test
    public void shouldSlideNonNilBySomeClassifier() {
        // ignore
    }

    // TODO move to traversable
    // -- zip

    @Test
    public void shouldZipNils() {
        final HashSet<Tuple2<Object, Object>> actual = empty().zip(empty());
        assertThat(actual).isEqualTo(empty());
    }

    @Test
    public void shouldZipEmptyAndNonNil() {
        final HashSet<Tuple2<Object, Integer>> actual = empty().zip(of(1));
        assertThat(actual).isEqualTo(empty());
    }

    @Test
    public void shouldZipNonEmptyAndNil() {
        final HashSet<Tuple2<Integer, Integer>> actual = of(1).zip(empty());
        assertThat(actual).isEqualTo(empty());
    }

    @Test
    public void shouldZipNonNilsIfThisIsSmaller() {
        final HashSet<Tuple2<Integer, String>> actual = of(1, 2).zip(of("a", "b", "c"));
        final HashSet<Tuple2<Integer, String>> expected = of(Tuple.of(1, "a"), Tuple.of(2, "b"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipNonNilsIfThatIsSmaller() {
        final HashSet<Tuple2<Integer, String>> actual = of(1, 2, 3).zip(of("a", "b"));
        final HashSet<Tuple2<Integer, String>> expected = of(Tuple.of(1, "a"), Tuple.of(2, "b"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipNonNilsOfSameSize() {
        final HashSet<Tuple2<Integer, String>> actual = of(1, 2, 3).zip(of("a", "b", "c"));
        final HashSet<Tuple2<Integer, String>> expected = of(Tuple.of(1, "a"), Tuple.of(2, "b"), Tuple.of(3, "c"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowIfZipWithThatIsNull() {
        empty().zip(null);
    }

    // TODO move to traversable
    // -- zipAll

    @Test
    public void shouldZipAllNils() {
        // ignore
    }

    @Test
    public void shouldZipAllEmptyAndNonNil() {
        // ignore
    }

    @Test
    public void shouldZipAllNonEmptyAndNil() {
        final HashSet<?> actual = of(1).zipAll(empty(), null, null);
        final HashSet<Tuple2<Integer, Object>> expected = of(Tuple.of(1, null));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonNilsIfThisIsSmaller() {
        final HashSet<Tuple2<Integer, String>> actual = of(1, 2).zipAll(of("a", "b", "c"), 9, "z");
        final HashSet<Tuple2<Integer, String>> expected = of(Tuple.of(1, "a"), Tuple.of(2, "b"), Tuple.of(9, "c"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonNilsIfThatIsSmaller() {
        final HashSet<Tuple2<Integer, String>> actual = of(1, 2, 3).zipAll(of("a", "b"), 9, "z");
        final HashSet<Tuple2<Integer, String>> expected = of(Tuple.of(1, "a"), Tuple.of(2, "b"), Tuple.of(3, "z"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonNilsOfSameSize() {
        final HashSet<Tuple2<Integer, String>> actual = of(1, 2, 3).zipAll(of("a", "b", "c"), 9, "z");
        final HashSet<Tuple2<Integer, String>> expected = of(Tuple.of(1, "a"), Tuple.of(2, "b"), Tuple.of(3, "c"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowIfZipAllWithThatIsNull() {
        empty().zipAll(null, null, null);
    }

    // TODO move to traversable
    // -- zipWithIndex

    @Test
    public void shouldZipNilWithIndex() {
        assertThat(this.<String> empty().zipWithIndex()).isEqualTo(this.<Tuple2<String, Integer>> empty());
    }

    @Test
    public void shouldZipNonNilWithIndex() {
        final HashSet<Tuple2<String, Integer>> actual = of("a", "b", "c").zipWithIndex();
        final HashSet<Tuple2<String, Integer>> expected = of(Tuple.of("a", 0), Tuple.of("b", 1), Tuple.of("c", 2));
        assertThat(actual).isEqualTo(expected);
    }

    // -- transform()

    @Test
    public void shouldTransform() {
        final String transformed = of(42).transform(v -> String.valueOf(v.head()));
        assertThat(transformed).isEqualTo("42");
    }

    // HashSet special cases

    @Override
    public void shouldDropRightAsExpectedIfCountIsLessThanSize() {
        assertThat(of(1, 2, 3).dropRight(2)).isEqualTo(of(3));
    }

    @Override
    public void shouldTakeRightAsExpectedIfCountIsLessThanSize() {
        assertThat(of(1, 2, 3).takeRight(2)).isEqualTo(of(1, 2));
    }

    @Override
    public void shouldGetInitOfNonNil() {
        assertThat(of(1, 2, 3).init()).isEqualTo(of(2, 3));
    }

    @Override
    public void shouldFoldRightNonNil() {
        final String actual = of('a', 'b', 'c').foldRight("", (x, xs) -> x + xs);
        final List<String> expected = List.of('a', 'b', 'c').permutations().map(List::mkString);
        assertThat(actual).isIn(expected);
    }

    @Override
    public void shouldReduceRightNonNil() {
        final String actual = of("a", "b", "c").reduceRight((x, xs) -> x + xs);
        final List<String> expected = List.of("a", "b", "c").permutations().map(List::mkString);
        assertThat(actual).isIn(expected);
    }

    @Override
    public void shouldMkStringWithDelimiterNonNil() {
        final String actual = of('a', 'b', 'c').mkString(",");
        final List<String> expected = List.of('a', 'b', 'c').permutations().map(l -> l.mkString(","));
        assertThat(actual).isIn(expected);
    }

    @Override
    public void shouldMkStringWithDelimiterAndPrefixAndSuffixNonNil() {
        final String actual = of('a', 'b', 'c').mkString("[", ",", "]");
        final List<String> expected = List.of('a', 'b', 'c').permutations().map(l -> l.mkString("[", ",", "]"));
        assertThat(actual).isIn(expected);
    }

    @Override
    public void shouldComputeDistinctByOfNonEmptyTraversableUsingComparator() {
        // TODO
    }

    @Override
    public void shouldComputeDistinctByOfNonEmptyTraversableUsingKeyExtractor() {
        // TODO
    }

    @Override
    public void shouldFindLastOfNonNil() {
        final int actual = of(1, 2, 3, 4).findLast(i -> i % 2 == 0).get();
        assertThat(actual).isIn(List.of(1, 2, 3, 4));
    }

    @Override
    public void shouldThrowWhenFoldRightNullOperator() {
        throw new NullPointerException(); // TODO
    }

    @Override
    public void shouldReturnSomeInitWhenCallingInitOptionOnNonNil() {
        // TODO
    }

    @Test
    public void shouldBeEqual() {
        assertTrue(HashSet.of(1).equals(HashSet.of(1)));
    }

    //fixme: delete, when useIsEqualToInsteadOfIsSameAs() will be eliminated from AbstractValueTest class
    @Override
    protected boolean useIsEqualToInsteadOfIsSameAs() {
        return false;
    }

    @Override
    protected HashSet<Character> range(char from, char toExclusive) {
        return HashSet.range(from, toExclusive);
    }

    @Override
    protected HashSet<Character> rangeBy(char from, char toExclusive, int step) {
        return HashSet.rangeBy(from, toExclusive, step);
    }

    @Override
    protected HashSet<Double> rangeBy(double from, double toExclusive, double step) {
        return HashSet.rangeBy(from, toExclusive, step);
    }

    @Override
    protected HashSet<Integer> range(int from, int toExclusive) {
        return HashSet.range(from, toExclusive);
    }

    @Override
    protected HashSet<Integer> rangeBy(int from, int toExclusive, int step) {
        return HashSet.rangeBy(from, toExclusive, step);
    }

    @Override
    protected HashSet<Long> range(long from, long toExclusive) {
        return HashSet.range(from, toExclusive);
    }

    @Override
    protected HashSet<Long> rangeBy(long from, long toExclusive, long step) {
        return HashSet.rangeBy(from, toExclusive, step);
    }

    @Override
    protected HashSet<Character> rangeClosed(char from, char toInclusive) {
        return HashSet.rangeClosed(from, toInclusive);
    }

    @Override
    protected HashSet<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return HashSet.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected HashSet<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return HashSet.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected HashSet<Integer> rangeClosed(int from, int toInclusive) {
        return HashSet.rangeClosed(from, toInclusive);
    }

    @Override
    protected HashSet<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return HashSet.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected HashSet<Long> rangeClosed(long from, long toInclusive) {
        return HashSet.rangeClosed(from, toInclusive);
    }

    @Override
    protected HashSet<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return HashSet.rangeClosedBy(from, toInclusive, step);
    }

    // -- toSet

    @Test
    public void shouldReturnSelfOnConvertToSet() {
        final HashSet<Integer> value = of(1, 2, 3);
        assertThat(value.toSet()).isSameAs(value);
    }

    // -- spliterator

    @Test
    public void shouldNotHaveSortedSpliterator() {
        assertThat(of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.SORTED)).isFalse();
    }

    @Test
    public void shouldNotHaveOrderedSpliterator() {
        assertThat(of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.ORDERED)).isFalse();
    }

    // -- isSequential()

    @Test
    public void shouldReturnFalseWhenIsSequentialCalled() {
        assertThat(of(1, 2, 3).isSequential()).isFalse();
    }

}
