package io.vavr.collection;

import io.vavr.Function1;
import io.vavr.Tuple2;
import io.vavr.Tuple3;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static java.util.stream.Collectors.toList;
import static io.vavr.Serializables.deserialize;
import static io.vavr.Serializables.serialize;
import static org.assertj.core.api.Assertions.assertThatCode;

public class BitSetTest extends AbstractSortedSetTest {

    private final static int MAX_BIT = 1_000_000;

    private enum E {
        V1, V2, V3
    }

    @Override
    protected String stringPrefix() {
        return "BitSet";
    }

    @Override
    protected <T> IterableAssert<T> assertThat(Iterable<T> actual) {
        return new IterableAssert<T>(actual) {
            @Override
            @SuppressWarnings("unchecked")
            public IterableAssert<T> isEqualTo(Object obj) {
                if (obj instanceof BitSet || actual instanceof BitSet) {
                    Assertions.assertThat(HashSet.ofAll(actual)).isEqualTo(HashSet.ofAll((Iterable<T>) obj));
                } else {
                    super.isEqualTo(obj);
                }
                return this;
            }
        };
    }

    @Override
    protected <T> ObjectAssert<T> assertThat(T actual) {
        return new ObjectAssert<T>(actual) {
            @Override
            public ObjectAssert<T> isEqualTo(Object expected) {
                if (actual instanceof Tuple2) {
                    final Tuple2<?, ?> t1 = (Tuple2<?, ?>) actual;
                    final Tuple2<?, ?> t2 = (Tuple2<?, ?>) expected;
                    assertThat((Iterable<?>) t1._1).isEqualTo(t2._1);
                    assertThat((Iterable<?>) t1._2).isEqualTo(t2._2);
                    return this;
                } else if (actual instanceof Tuple3) {
                    final Tuple3<?, ?, ?> t1 = (Tuple3<?, ?, ?>) actual;
                    final Tuple3<?, ?, ?> t2 = (Tuple3<?, ?, ?>) expected;
                    assertThat((Iterable<?>) t1._1).isEqualTo(t2._1);
                    assertThat((Iterable<?>) t1._2).isEqualTo(t2._2);
                    assertThat((Iterable<?>) t1._3).isEqualTo(t2._3);
                    return this;
                } else {
                    return super.isEqualTo(expected);
                }
            }
        };
    }

    private <T> BitSet.Builder<T> bsBuilder() {
        final Mapper<T> mapper = new Mapper<>();
        return BitSet.withRelations(
                (Function1<Integer, T> & Serializable) mapper::fromInt,
                (Function1<T, Integer> & Serializable) mapper::toInt);
    }

    @Override
    protected <T> Collector<T, ArrayList<T>, ? extends Traversable<T>> collector() {
        return this.<T> bsBuilder().collector();
    }

    @Override
    protected <T> BitSet<T> empty() {
        return this.<T> bsBuilder().empty();
    }

    @Override
    protected <T> BitSet<T> emptyWithNull() {
        return empty();
    }

    @Override
    protected boolean emptyShouldBeSingleton() {
        return false;
    }

    @Override
    protected <T> BitSet<T> of(T element) {
        return this.<T> bsBuilder().of(element);
    }

    @Override
    protected <T> BitSet<T> of(Comparator<? super T> comparator, T element) {
        // comparator is not used
        return this.<T> bsBuilder().of(element);
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    @Override
    protected final <T> BitSet<T> of(Comparator<? super T> comparator, T... elements) {
        // comparator is not used
        return this.<T> bsBuilder().of(elements);
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    @Override
    protected final <T> BitSet<T> of(T... elements) {
        return this.<T> bsBuilder().of(elements);
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
    protected <T> BitSet<T> ofAll(Iterable<? extends T> elements) {
        return this.<T> bsBuilder().ofAll(elements);
    }

    @Override
    protected <T extends Comparable<? super T>> BitSet<T> ofJavaStream(java.util.stream.Stream<? extends T> javaStream) {
        return this.<T> bsBuilder().ofAll(javaStream);
    }

    @Override
    protected BitSet<Boolean> ofAll(boolean... elements) {
        return BitSet.ofAll(elements);
    }

    @Override
    protected BitSet<Byte> ofAll(byte... elements) {
        return BitSet.ofAll(elements);
    }

    @Override
    protected BitSet<Character> ofAll(char... elements) {
        return BitSet.ofAll(elements);
    }

    @Override
    protected BitSet<Double> ofAll(double... elements) {
        return this.<Double> bsBuilder().ofAll(Iterator.ofAll(elements));
    }

    @Override
    protected BitSet<Float> ofAll(float... elements) {
        return this.<Float> bsBuilder().ofAll(Iterator.ofAll(elements));
    }

    @Override
    protected BitSet<Integer> ofAll(int... elements) {
        return BitSet.ofAll(elements);
    }

    @Override
    protected BitSet<Long> ofAll(long... elements) {
        return BitSet.ofAll(elements);
    }

    @Override
    protected BitSet<Short> ofAll(short... elements) {
        return BitSet.ofAll(elements);
    }

    @Override
    protected <T> BitSet<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        return this.<T> bsBuilder().tabulate(n, f);
    }

    @Override
    protected <T> BitSet<T> fill(int n, Supplier<? extends T> s) {
        return this.<T> bsBuilder().fill(n, s);
    }

    @Override
    protected BitSet<Character> range(char from, char toExclusive) {
        return BitSet.range(from, toExclusive);
    }

    @Override
    protected BitSet<Character> rangeBy(char from, char toExclusive, int step) {
        return BitSet.rangeBy(from, toExclusive, step);
    }

    @Override
    protected BitSet<Double> rangeBy(double from, double toExclusive, double step) {
        return this.<Double> bsBuilder().ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    private static boolean isBadRange(int a, int b) {
        return a < 0 || b < 0 || a > MAX_BIT || b > MAX_BIT;
    }

    private static boolean isBadRange(long a, long b) {
        return a < 0 || b < 0 || a > MAX_BIT || b > MAX_BIT;
    }

    @Override
    protected BitSet<Integer> range(int from, int toExclusive) {
        if (isBadRange(from, toExclusive)) {
            return this.<Integer> bsBuilder().ofAll(Iterator.range(from, toExclusive));
        } else {
            return BitSet.range(from, toExclusive);
        }
    }

    @Override
    protected BitSet<Integer> rangeBy(int from, int toExclusive, int step) {
        if (isBadRange(from, toExclusive)) {
            return this.<Integer> bsBuilder().ofAll(Iterator.rangeBy(from, toExclusive, step));
        } else {
            return BitSet.rangeBy(from, toExclusive, step);
        }
    }

    @Override
    protected BitSet<Long> range(long from, long toExclusive) {
        if (isBadRange(from, toExclusive)) {
            return this.<Long> bsBuilder().ofAll(Iterator.range(from, toExclusive));
        } else {
            return BitSet.range(from, toExclusive);
        }
    }

    @Override
    protected BitSet<Long> rangeBy(long from, long toExclusive, long step) {
        if (isBadRange(from, toExclusive)) {
            return this.<Long> bsBuilder().ofAll(Iterator.rangeBy(from, toExclusive, step));
        } else {
            return BitSet.rangeBy(from, toExclusive, step);
        }
    }

    @Override
    protected BitSet<Character> rangeClosed(char from, char toInclusive) {
        return BitSet.rangeClosed(from, toInclusive);
    }

    @Override
    protected BitSet<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return BitSet.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected BitSet<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return this.<Double> bsBuilder().ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    @Override
    protected BitSet<Integer> rangeClosed(int from, int toInclusive) {
        if (isBadRange(from, toInclusive)) {
            return this.<Integer> bsBuilder().ofAll(Iterator.rangeClosed(from, toInclusive));
        } else {
            return BitSet.rangeClosed(from, toInclusive);
        }
    }

    @Override
    protected BitSet<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        if (isBadRange(from, toInclusive)) {
            return this.<Integer> bsBuilder().ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
        } else {
            return BitSet.rangeClosedBy(from, toInclusive, step);
        }
    }

    @Override
    protected BitSet<Long> rangeClosed(long from, long toInclusive) {
        if (isBadRange(from, toInclusive)) {
            return this.<Long> bsBuilder().ofAll(Iterator.rangeClosed(from, toInclusive));
        } else {
            return BitSet.rangeClosed(from, toInclusive);
        }
    }

    @Override
    protected BitSet<Long> rangeClosedBy(long from, long toInclusive, long step) {
        if (isBadRange(from, toInclusive)) {
            return this.<Long> bsBuilder().ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
        } else {
            return BitSet.rangeClosedBy(from, toInclusive, step);
        }
    }

    // BitSet specific

    @Test
    public void testBitSet1() {
        BitSet<Integer> bs = BitSet.empty();

        bs = bs.add(2);
        assertThat(bs.head()).isEqualTo(2);
        assertThat(bs.length()).isEqualTo(1);

        bs = bs.add(4);
        assertThat(bs.head()).isEqualTo(2);
        assertThat(bs.length()).isEqualTo(2);

        bs = bs.remove(2);
        assertThat(bs.head()).isEqualTo(4);
        assertThat(bs.contains(2)).isFalse();
        assertThat(bs.length()).isEqualTo(1);

        bs = bs.remove(4);
        assertThat(bs.isEmpty()).isTrue();
        assertThat(bs.length()).isEqualTo(0);
    }

    @Test
    public void testBitSet2() {
        BitSet<Integer> bs = BitSet.empty();

        bs = bs.add(2);
        assertThat(bs.head()).isEqualTo(2);
        assertThat(bs.add(2)).isSameAs(bs);
        assertThat(bs.length()).isEqualTo(1);

        bs = bs.add(70);
        assertThat(bs.head()).isEqualTo(2);
        assertThat(bs.add(2)).isSameAs(bs);
        assertThat(bs.add(70)).isSameAs(bs);
        assertThat(bs.length()).isEqualTo(2);

        bs = bs.remove(2);
        assertThat(bs.head()).isEqualTo(70);
        assertThat(bs.contains(2)).isFalse();
        assertThat(bs.length()).isEqualTo(1);

        bs = bs.remove(70);
        assertThat(bs.isEmpty()).isTrue();
        assertThat(bs.length()).isEqualTo(0);

        bs = bs.add(2);
        bs = bs.add(70);
        bs = bs.add(3);
        assertThat(bs.length()).isEqualTo(3);
        bs = bs.add(71);
        assertThat(bs.length()).isEqualTo(4);
        bs = bs.add(701);
        assertThat(bs.length()).isEqualTo(5);

    }

    @Test
    public void testBitSetN() {
        BitSet<Integer> bs = BitSet.empty();

        bs = bs.add(2);
        assertThat(bs.head()).isEqualTo(2);

        bs = bs.add(700);
        assertThat(bs.head()).isEqualTo(2);
        assertThat(bs.add(2)).isSameAs(bs);
        assertThat(bs.add(700)).isSameAs(bs);

        bs = bs.remove(2);
        assertThat(bs.head()).isEqualTo(700);
        assertThat(bs.contains(2)).isFalse();

        bs = bs.remove(700);
        assertThat(bs.isEmpty()).isTrue();

    }

    @Test
    public void testFactories() {
        assertThat(BitSet.of(7).contains(7)).isTrue();     // BitSet1, < 64
        assertThat(BitSet.of(77).contains(77)).isTrue();   // BitSet2, < 2*64
        assertThat(BitSet.of(777).contains(777)).isTrue(); // BitSetN, >= 2*64
        assertThat(BitSet.ofAll(List.of(1).toJavaStream())).isEqualTo(BitSet.of(1));
        assertThat(BitSet.fill(1, () -> 1)).isEqualTo(BitSet.of(1));
        assertThat(BitSet.tabulate(1, i -> 1)).isEqualTo(BitSet.of(1));
    }

    @Test
    public void shouldAllAll() {
        assertThat(BitSet.empty().add(7).addAll(List.of(1, 2))).isEqualTo(BitSet.of(1, 2, 7));
        assertThat(BitSet.empty().add(77).addAll(List.of(1, 2))).isEqualTo(BitSet.of(1, 2, 77));
        assertThat(BitSet.empty().add(777).addAll(List.of(1, 2))).isEqualTo(BitSet.of(1, 2, 777));
    }

    @Test
    public void shouldCollectInts() {
        final Traversable<Integer> actual = java.util.stream.Stream.of(1, 2, 3).collect(BitSet.collector());
        assertThat(actual).isEqualTo(of(1, 2, 3));
    }

    @Test
    public void testEnums() {
        BitSet<E> bs = BitSet.withEnum(E.class).empty();
        bs = bs.add(E.V2);
        assert bs.head() == E.V2;
        bs = bs.add(E.V3);
        assert bs.head() == E.V2;
        bs = bs.remove(E.V2);
        assert bs.head() == E.V3;
        assert !bs.contains(E.V2);
        assert bs.contains(E.V3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAddNegativeElementToEmpty() {
        BitSet.empty().add(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAddNegativeElementToBitSet2() {
        BitSet.empty().add(77).add(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAddNegativeElementToBitSetN() {
        BitSet.empty().add(777).add(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowAddNegativeElements() {
        BitSet.empty().addAll(List.of(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowContainsNegativeElements() {
        BitSet.empty().contains(-1);
    }

    @Test
    public void shouldSerializeDeserializeNativeBitSet() {
        final Object actual = deserialize(serialize(BitSet.of(1, 2, 3)));
        final Object expected = BitSet.of(1, 2, 3);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSerializeDeserializeEnumBitSet() {
        final Object actual = deserialize(serialize(BitSet.withEnum(E.class).of(E.V1, E.V2)));
        final Object expected = BitSet.withEnum(E.class).of(E.V1, E.V2);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldBehaveExactlyLikeAnotherBitSet() {
        for (int i = 0; i < 10; i++) {
            final Random random = getRandom(123456789);

            final java.util.BitSet mutableBitSet = new java.util.BitSet();
            BitSet<Integer> functionalBitSet = BitSet.empty();

            final int size = 5_000;
            for (int j = 0; j < size; j++) {
                /* Insert */
                if (random.nextInt() % 3 == 0) {
                    assertMinimumsAreEqual(mutableBitSet, functionalBitSet);

                    final int value = random.nextInt(size);
                    mutableBitSet.set(value);
                    functionalBitSet = functionalBitSet.add(value);
                }

                assertMinimumsAreEqual(mutableBitSet, functionalBitSet);

                /* Delete */
                if (random.nextInt() % 5 == 0) {
                    if (!mutableBitSet.isEmpty()) { mutableBitSet.clear(mutableBitSet.nextSetBit(0)); }
                    if (!functionalBitSet.isEmpty()) { functionalBitSet = functionalBitSet.tail(); }

                    assertMinimumsAreEqual(mutableBitSet, functionalBitSet);
                }
            }

            final Collection<Integer> oldValues = mutableBitSet.stream().sorted().boxed().collect(toList());
            final Collection<Integer> newValues = functionalBitSet.toJavaList();
            assertThat(oldValues).isEqualTo(newValues);
        }
    }

    private void assertMinimumsAreEqual(java.util.BitSet oldSet, BitSet<Integer> newSet) {
        assertThat(oldSet.isEmpty()).isEqualTo(newSet.isEmpty());
        if (!newSet.isEmpty()) {
            assertThat(oldSet.nextSetBit(0)).isEqualTo(newSet.head());
        }
    }

    // -- toSortedSet

    @Override
    @Test
    public void shouldConvertToSortedSetWithoutComparatorOnComparable() {
        final BitSet<Integer> value = BitSet.of(3, 7, 1, 15, 0);
        final Set<Integer> set = value.toSortedSet();
        assertThat(set).isEqualTo(TreeSet.of(0, 1, 3, 7, 15));
    }

    // -- toPriorityQueue

    @Test
    @Override
    public void shouldConvertToPriorityQueueUsingImplicitComparator() {
        final BitSet<Integer> value = BitSet.of(1, 3, 2);
        final PriorityQueue<Integer> queue = value.toPriorityQueue();
        assertThat(queue).isEqualTo(PriorityQueue.of(1, 2, 3));
    }

    @Test
    @Override
    public void shouldConvertToPriorityQueueUsingExplicitComparator() {
        final Comparator<Integer> comparator = Comparator.naturalOrder();
        final BitSet<Integer> value = BitSet.of(1, 3, 2);
        final PriorityQueue<Integer> queue = value.toPriorityQueue(comparator);
        assertThat(queue).isEqualTo(PriorityQueue.of(comparator, 1, 2, 3));
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

    // -- head, init, last, tail

    @Test
    public void shouldReturnHeadOfNonEmptyHavingReversedOrder() {
        // BitSet can't have reverse order
    }

    @Test
    public void shouldReturnInitOfNonEmptyHavingReversedOrder() {
        // BitSet can't have reverse order
    }

    @Test
    public void shouldReturnLastOfNonEmptyHavingReversedOrder() {
        // BitSet can't have reverse order
    }

    @Test
    public void shouldReturnTailOfNonEmptyHavingReversedOrder() {
        // BitSet can't have reverse order
    }

    // -- classes

    private static class Mapper<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        private final java.util.Map<Integer, T> fromIntMap = new java.util.HashMap<>();
        private final java.util.Map<T, Integer> toIntMap = new java.util.HashMap<>();
        private int nextValue = 0;

        synchronized T fromInt(Integer i) {
            if (i < nextValue) {
                return fromIntMap.get(i);
            } else {
                throw new RuntimeException();
            }
        }

        synchronized Integer toInt(T value) {
            Integer i = toIntMap.get(value);
            if (i == null) {
                i = nextValue++;
                toIntMap.put(value, i);
                fromIntMap.put(i, value);
            }
            return i;
        }
    }
}
