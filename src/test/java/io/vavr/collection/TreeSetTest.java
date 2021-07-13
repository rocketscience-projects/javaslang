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
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static io.vavr.TestComparators.toStringComparator;
import static io.vavr.collection.Comparators.naturalComparator;
import static java.util.Comparator.nullsFirst;
import static org.assertj.core.api.Assertions.assertThatCode;

public class TreeSetTest extends AbstractSortedSetTest {

    @Override
    protected String stringPrefix() {
        return "TreeSet";
    }

    @Override
    protected <T> Collector<T, ArrayList<T>, ? extends TreeSet<T>> collector() {
        return TreeSet.collector(naturalComparator());
    }

    @Override
    protected <T> TreeSet<T> empty() {
        return TreeSet.empty(naturalComparator());
    }

    @Override
    protected <T> TreeSet<T> emptyWithNull() {
        return TreeSet.empty(nullsFirst(naturalComparator()));
    }

    @Override
    protected boolean emptyShouldBeSingleton() {
        return false;
    }

    @Override
    protected <T> TreeSet<T> of(T element) {
        return TreeSet.of(naturalComparator(), element);
    }

    @Override
    protected <T> TreeSet<T> of(Comparator<? super T> comparator, T element) {
        return TreeSet.of(comparator, element);
    }

    @Override
    @SafeVarargs
    @SuppressWarnings("varargs")
    protected final <T> TreeSet<T> of(Comparator<? super T> comparator, T... elements) {
        return TreeSet.of(comparator, elements);
    }

    @Override
    @SafeVarargs
    @SuppressWarnings("varargs")
    protected final <T> TreeSet<T> of(T... elements) {
        return TreeSet.<T> of(naturalComparator(), elements);
    }

    @Override
    protected <T> TreeSet<T> ofAll(Iterable<? extends T> elements) {
        return TreeSet.ofAll(naturalComparator(), elements);
    }

    @Override
    protected <T extends Comparable<? super T>> TreeSet<T> ofJavaStream(java.util.stream.Stream<? extends T> javaStream) {
        return TreeSet.ofAll(javaStream);
    }

    @Override
    protected TreeSet<Boolean> ofAll(boolean... elements) {
        return TreeSet.ofAll(elements);
    }

    @Override
    protected TreeSet<Byte> ofAll(byte... elements) {
        return TreeSet.ofAll(elements);
    }

    @Override
    protected TreeSet<Character> ofAll(char... elements) {
        return TreeSet.ofAll(elements);
    }

    @Override
    protected TreeSet<Double> ofAll(double... elements) {
        return TreeSet.ofAll(elements);
    }

    @Override
    protected TreeSet<Float> ofAll(float... elements) {
        return TreeSet.ofAll(elements);
    }

    @Override
    protected TreeSet<Integer> ofAll(int... elements) {
        return TreeSet.ofAll(elements);
    }

    @Override
    protected TreeSet<Long> ofAll(long... elements) {
        return TreeSet.ofAll(elements);
    }

    @Override
    protected TreeSet<Short> ofAll(short... elements) {
        return TreeSet.ofAll(elements);
    }

    @Override
    protected <T> TreeSet<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        return TreeSet.tabulate(naturalComparator(), n, f);
    }

    @Override
    protected <T> TreeSet<T> fill(int n, Supplier<? extends T> s) {
        return TreeSet.fill(naturalComparator(), n, s);
    }

    @Override
    protected boolean useIsEqualToInsteadOfIsSameAs() {
        return true;
    }

    @Override
    protected int getPeekNonNilPerformingAnAction() {
        return 1;
    }

    @Override
    protected TreeSet<Character> range(char from, char toExclusive) {
        return TreeSet.range(from, toExclusive);
    }

    @Override
    protected TreeSet<Character> rangeBy(char from, char toExclusive, int step) {
        return TreeSet.rangeBy(from, toExclusive, step);
    }

    @Override
    protected TreeSet<Double> rangeBy(double from, double toExclusive, double step) {
        return TreeSet.rangeBy(from, toExclusive, step);
    }

    @Override
    protected TreeSet<Integer> range(int from, int toExclusive) {
        return TreeSet.range(from, toExclusive);
    }

    @Override
    protected TreeSet<Integer> rangeBy(int from, int toExclusive, int step) {
        return TreeSet.rangeBy(from, toExclusive, step);
    }

    @Override
    protected TreeSet<Long> range(long from, long toExclusive) {
        return TreeSet.range(from, toExclusive);
    }

    @Override
    protected TreeSet<Long> rangeBy(long from, long toExclusive, long step) {
        return TreeSet.rangeBy(from, toExclusive, step);
    }

    @Override
    protected TreeSet<Character> rangeClosed(char from, char toInclusive) {
        return TreeSet.rangeClosed(from, toInclusive);
    }

    @Override
    protected TreeSet<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return TreeSet.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected TreeSet<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return TreeSet.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected TreeSet<Integer> rangeClosed(int from, int toInclusive) {
        return TreeSet.rangeClosed(from, toInclusive);
    }

    @Override
    protected TreeSet<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return TreeSet.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected TreeSet<Long> rangeClosed(long from, long toInclusive) {
        return TreeSet.rangeClosed(from, toInclusive);
    }

    @Override
    protected TreeSet<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return TreeSet.rangeClosedBy(from, toInclusive, step);
    }

    // -- collector

    @Test
    public void shouldCollectEmpty() {
        final TreeSet<Integer> actual = java.util.stream.Stream.<Integer>empty().collect(TreeSet.collector());
        assertThat(actual).isEmpty();
    }

    @Test
    public void shouldCollectNonEmpty() {
        final TreeSet<Integer> actual = java.util.stream.Stream.of(1, 2, 3).collect(TreeSet.collector());
        final TreeSet<Integer> expected = of(1, 2, 3);
        assertThat(actual).isEqualTo(expected);
    }

    // -- construct

    @Test
    public void shouldConstructEmptySetWithExplicitComparator() {
        final TreeSet<Integer> ts = TreeSet.<Integer> of(naturalComparator()
            .reversed())
            .addAll(Array.ofAll(1, 2, 3));
        assertThat(ts.toArray()).isEqualTo(Array.of(3, 2, 1));
    }

    @Test
    public void shouldConstructStreamFromEmptyJavaStream() {
        final TreeSet<Integer> actual = ofJavaStream(java.util.stream.Stream.<Integer>empty());
        final TreeSet<Integer> expected = empty();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldConstructStreamFromNonEmptyJavaStream() {
        final TreeSet<Integer> actual = TreeSet.ofAll(naturalComparator(), java.util.stream.Stream.of(1, 2, 3));
        final TreeSet<Integer> expected = of(1, 2, 3);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldConstructStreamFromNonEmptyJavaStreamWithoutComparator() {
        final TreeSet<Integer> actual = ofJavaStream(java.util.stream.Stream.of(1, 2, 3));
        final TreeSet<Integer> expected = of(1, 2, 3);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldConstructFromTreeSetWithoutComparator() {
        final TreeSet<Integer> actual = TreeSet.ofAll(TreeSet.of(1));
        final TreeSet<Integer> expected = of(1);
        assertThat(actual).isEqualTo(expected);
    }

    // -- static narrow

    @Test
    public void shouldNarrowTreeSet() {
        final TreeSet<Double> doubles = TreeSet.of(toStringComparator(), 1.0d);
        final TreeSet<Number> numbers = TreeSet.narrow(doubles);
        final int actual = numbers.add(new BigDecimal("2.0")).sum().intValue();
        assertThat(actual).isEqualTo(3);
    }

    // -- addAll

    @Test
    public void shouldKeepComparator() {
        final List<Integer> actual = TreeSet.empty(inverseIntComparator()).addAll(TreeSet.of(1, 2, 3)).toList();
        final List<Integer> expected = List.of(3, 2, 1);
        assertThat(actual).isEqualTo(expected);
    }

    // -- zip

    @Override
    @Test
    public void shouldZipNonNilWithIndex() {
        final Traversable<Tuple2<String, Integer>> actual = of("a", "b", "c").zipWithIndex();
        final Traversable<Tuple2<String, Integer>> expected = of(Tuple2.comparator(naturalComparator(), naturalComparator()), Tuple.of("a", 0), Tuple.of("b", 1), Tuple.of("c", 2));
        assertThat(actual).isEqualTo(expected);
    }

    @Override
    @Test
    public void shouldZipNonNilsOfSameSize() {
        final Traversable<Tuple2<Integer, String>> actual = of(1, 2, 3).zip(of("a", "b", "c"));
        @SuppressWarnings("unchecked")
        final Traversable<Tuple2<Integer, String>> expected = of(Tuple2.comparator(naturalComparator(), naturalComparator()), Tuple.of(1, "a"), Tuple.of(2, "b"), Tuple.of(3, "c"));
        assertThat(actual).isEqualTo(expected);
    }

    @Override
    @Test
    public void shouldZipNonNilsIfThatIsSmaller() {
        final Traversable<Tuple2<Integer, String>> actual = of(1, 2, 3).zip(of("a", "b"));
        final Traversable<Tuple2<Integer, String>> expected = of(Tuple2.comparator(naturalComparator(), naturalComparator()), Tuple.of(1, "a"), Tuple.of(2, "b"));
        assertThat(actual).isEqualTo(expected);
    }

    @Override
    @Test
    public void shouldZipNonNilsIfThisIsSmaller() {
        final Traversable<Tuple2<Integer, String>> actual = of(1, 2).zip(of("a", "b", "c"));
        final Traversable<Tuple2<Integer, String>> expected = of(Tuple2.comparator(naturalComparator(), naturalComparator()), Tuple.of(1, "a"), Tuple.of(2, "b"));
        assertThat(actual).isEqualTo(expected);
    }

    // -- zipAll

    @Override
    @Test
    public void shouldZipAllNonNilsOfSameSize() {
        final Traversable<Tuple2<Integer, String>> actual = of(1, 2, 3).zipAll(of("a", "b", "c"), 9, "z");
        final Traversable<Tuple2<Integer, String>> expected = of(Tuple2.comparator(naturalComparator(), naturalComparator()), Tuple.of(1, "a"), Tuple.of(2, "b"), Tuple.of(3, "c"));
        assertThat(actual).isEqualTo(expected);
    }

    @Override
    @Test
    public void shouldZipAllNonNilsIfThisIsSmaller() {
        final Traversable<Tuple2<Integer, String>> actual = of(1, 2).zipAll(of("a", "b", "c"), 9, "z");
        final Traversable<Tuple2<Integer, String>> expected = of(Tuple2.comparator(naturalComparator(), naturalComparator()), Tuple.of(1, "a"), Tuple.of(2, "b"), Tuple.of(9, "c"));
        assertThat(actual).isEqualTo(expected);
    }

    @Override
    @Test
    public void shouldZipAllNonNilsIfThatIsSmaller() {
        final Traversable<Tuple2<Integer, String>> actual = of(1, 2, 3).zipAll(of("a", "b"), 9, "z");
        final Traversable<Tuple2<Integer, String>> expected = of(Tuple2.comparator(naturalComparator(), naturalComparator()), Tuple.of(1, "a"), Tuple.of(2, "b"), Tuple.of(3, "z"));
        assertThat(actual).isEqualTo(expected);
    }
    
    // -- removeAll
    
    @Test
    public void shouldKeepComparatorOnRemoveAll() {
        final TreeSet<Integer> ts = TreeSet.of(naturalComparator()
            .reversed(), 1, 2, 3)
            .removeAll(Array.ofAll(1, 2, 3))
            .addAll(Array.ofAll(4, 5, 6));
        assertThat(ts.toArray()).isEqualTo(Array.of(6, 5, 4));
    }

    // -- diff

    @Test
    public void shouldCalculateDiffIfNotTreeSet() {
        final TreeSet<Integer> actual = of(1, 2, 3).diff(HashSet.of(1, 2));
        final TreeSet<Integer> expected = of(3);
        assertThat(actual).isEqualTo(expected);
    }

    // -- union

    @Test
    public void shouldCalculateUnionIfNotTreeSet() {
        final TreeSet<Integer> actual = of(1, 2, 3).union(HashSet.of(4));
        final TreeSet<Integer> expected = of(1, 2, 3, 4);
        assertThat(actual).isEqualTo(expected);
    }

    // -- intersect

    @Test
    public void shouldCalculateEmptyIntersectIfNotTreeSet() {
        final TreeSet<Integer> actual = of(1, 2, 3).intersect(HashSet.of(4));
        assertThat(actual).isEmpty();
    }

    @Test
    public void shouldCalculateIntersectIfNotTreeSet() {
        final TreeSet<Integer> actual = of(1, 2, 3).intersect(HashSet.of(3));
        final TreeSet<Integer> expected = of(3);
        assertThat(actual).isEqualTo(expected);
    }

    // -- fill

    @Test
    public void shouldFillWithoutComparator() {
        final TreeSet<Integer> actual = TreeSet.fill(3, () -> 1);
        final TreeSet<Integer> expected = of(1, 1, 1);
        assertThat(actual).isEqualTo(expected);
    }

    // -- tabulate

    @Test
    public void shouldTabulateWithoutComparator() {
        final TreeSet<Integer> actual = TreeSet.tabulate(3, Function.identity());
        final TreeSet<Integer> expected = of(0, 1, 2);
        assertThat(actual).isEqualTo(expected);
    }

    // -- toSortedSet

    @Test
    public void shouldReturnSelfOnConvertToSortedSet() {
        final TreeSet<Integer> value = of(1, 2, 3);
        assertThat(value.toSortedSet()).isSameAs(value);
    }

    @Test
    public void shouldReturnSelfOnConvertToSortedSetWithSameComparator() {
        final TreeSet<Integer> value = of(1, 2, 3);
        assertThat(value.toSortedSet(value.comparator())).isSameAs(value);
    }

    @Test
    public void shouldNotReturnSelfOnConvertToSortedSetWithDifferentComparator() {
        final TreeSet<Integer> value = of(1, 2, 3);
        assertThat(value.toSortedSet(Integer::compareTo)).isNotSameAs(value);
    }

    @Test
    public void shouldPreserveComparatorOnConvertToSortedSetWithoutDistinctComparator() {
        final TreeSet<Integer> value = TreeSet.of(naturalComparator().reversed(), 1, 2, 3);
        assertThat(value.toSortedSet().mkString(",")).isEqualTo("3,2,1");
    }

    // -- map()

    @Test
    public void shouldMapElementsToUncomparableType() {
        assertThatCode(() -> of(1, 2, 3).map(i -> new Object())).doesNotThrowAnyException();
    }

    // -- flatMap()

    @Test
    public void shouldFlatMapToUncomparableType() {
        assertThatCode(() -> of(1, 2, 3).flatMap(i -> HashSet.of(new Object()))).doesNotThrowAnyException();
    }

    // -- transform()

    @Test
    public void shouldTransform() {
        final String transformed = of(42).transform(v -> String.valueOf(v.head()));
        assertThat(transformed).isEqualTo("42");
    }

    // -- helpers

    private static Comparator<Integer> inverseIntComparator() {
        return (i1, i2) -> Integer.compare(i2, i1);
    }

    // -- ignored tests

    @Override
    @Test
    @Ignore
    public void shouldCalculateAverageOfDoubleAndFloat() {
        // it is not possible to create a TreeSet containing unrelated types
    }

    @Override
    @Test
    @Ignore
    public void shouldZipAllEmptyAndNonNil() {
    }

    @Override
    @Test
    @Ignore
    public void shouldZipAllNonEmptyAndNil() {
    }
}
