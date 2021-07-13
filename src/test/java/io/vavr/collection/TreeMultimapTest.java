package io.vavr.collection;

import io.vavr.Tuple2;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.*;
import java.util.stream.Stream;

public class TreeMultimapTest extends AbstractMultimapTest {

    @Override
    protected String stringPrefix() {
        return "TreeMultimap[" + containerName() + "]";
    }

    @Override
    <T1, T2> java.util.Map<T1, T2> javaEmptyMap() {
        return new java.util.TreeMap<>();
    }

    @Override
    protected <T1 extends Comparable<T1>, T2> TreeMultimap<T1, T2> emptyMap(Comparator<? super T2> comparator) {
        switch (containerType) {
            case SEQ:
                return TreeMultimap.withSeq().empty();
            case SET:
                return TreeMultimap.withSet().empty();
            case SORTED_SET:
                return TreeMultimap.withSortedSet(comparator).empty();
            default:
                throw new RuntimeException();
        }
    }

    @Override
    protected <T> Collector<Tuple2<Integer, T>, ArrayList<Tuple2<Integer, T>>, ? extends Multimap<Integer, T>> mapCollector() {
        switch (containerType) {
            case SEQ:
                return TreeMultimap.withSeq().collector();
            case SET:
                return TreeMultimap.withSet().collector();
            case SORTED_SET:
                return TreeMultimap.withSortedSet(Comparators.naturalComparator()).collector();
            default:
                throw new RuntimeException();
        }
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    @Override
    protected final <K extends Comparable<K>, V> TreeMultimap<K, V> mapOfTuples(Tuple2<? extends K, ? extends V>... entries) {
        switch (containerType) {
            case SEQ:
                return TreeMultimap.withSeq().ofEntries(entries);
            case SET:
                return TreeMultimap.withSet().ofEntries(entries);
            case SORTED_SET:
                return TreeMultimap.withSortedSet(Comparators.naturalComparator()).ofEntries(entries);
            default:
                throw new RuntimeException();
        }
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    @Override
    protected final <K extends Comparable<K>, V> TreeMultimap<K, V> mapOfEntries(java.util.Map.Entry<? extends K, ? extends V>... entries) {
        switch (containerType) {
            case SEQ:
                return TreeMultimap.withSeq().ofEntries(entries);
            case SET:
                return TreeMultimap.withSet().ofEntries(entries);
            case SORTED_SET:
                return TreeMultimap.withSortedSet(Comparators.naturalComparator()).ofEntries(entries);
            default:
                throw new RuntimeException();
        }
    }

    @Override
    protected <K extends Comparable<K>, V> TreeMultimap<K, V> mapOfPairs(K k1, V v1, K k2, V v2, K k3, V v3) {
        switch (containerType) {
            case SEQ:
                return TreeMultimap.withSeq().of(k1, v1, k2, v2, k3, v3);
            case SET:
                return TreeMultimap.withSet().of(k1, v1, k2, v2, k3, v3);
            case SORTED_SET:
                return TreeMultimap.withSortedSet(Comparators.naturalComparator()).of(k1, v1, k2, v2, k3, v3);
            default:
                throw new RuntimeException();
        }
    }

    @Override
    protected <K extends Comparable<K>, V> TreeMultimap<K, V> mapOf(K key, V value) {
        switch (containerType) {
            case SEQ:
                return TreeMultimap.withSeq().of(key, value);
            case SET:
                return TreeMultimap.withSet().of(key, value);
            case SORTED_SET:
                return TreeMultimap.withSortedSet(Comparators.naturalComparator()).of(key, value);
            default:
                throw new RuntimeException();
        }
    }

    @Override
    protected <K extends Comparable<? super K>, V> TreeMultimap<K, V> mapOf(Map<? extends K, ? extends V> map) {
        switch (containerType) {
            case SEQ:
                return TreeMultimap.withSeq().ofAll(map);
            case SET:
                return TreeMultimap.withSet().ofAll(map);
            case SORTED_SET:
                return TreeMultimap.withSortedSet(Comparators.naturalComparator()).ofAll(map);
            default:
                throw new RuntimeException();
        }
    }

    @Override
    protected <T, K extends Comparable<? super K>, V> TreeMultimap<K, V> mapOf(Stream<? extends T> stream, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        switch (containerType) {
            case SEQ:
                return TreeMultimap.withSeq().ofAll(stream, keyMapper, valueMapper);
            case SET:
                return TreeMultimap.withSet().ofAll(stream, keyMapper, valueMapper);
            case SORTED_SET:
                return TreeMultimap.withSortedSet(Comparators.naturalComparator()).ofAll(stream, keyMapper, valueMapper);
            default:
                throw new RuntimeException();
        }
    }

    @Override
    protected <T, K extends Comparable<? super K>, V> TreeMultimap<K, V> mapOf(Stream<? extends T> stream, Function<? super T, Tuple2<? extends K, ? extends V>> f) {
        switch (containerType) {
            case SEQ:
                return TreeMultimap.withSeq().ofAll(stream, f);
            case SET:
                return TreeMultimap.withSet().ofAll(stream, f);
            case SORTED_SET:
                return TreeMultimap.withSortedSet(Comparators.naturalComparator()).ofAll(stream, f);
            default:
                throw new RuntimeException();
        }
    }

    @Override
    protected <K extends Comparable<K>, V> TreeMultimap<K, V> mapTabulate(int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V>> f) {
        switch (containerType) {
            case SEQ:
                return TreeMultimap.withSeq().tabulate(n, f);
            case SET:
                return TreeMultimap.withSet().tabulate(n, f);
            case SORTED_SET:
                return TreeMultimap.withSortedSet(Comparators.naturalComparator()).tabulate(n, f);
            default:
                throw new RuntimeException();
        }
    }

    @Override
    protected <K extends Comparable<K>, V> TreeMultimap<K, V> mapFill(int n, Supplier<? extends Tuple2<? extends K, ? extends V>> s) {
        switch (containerType) {
            case SEQ:
                return TreeMultimap.withSeq().fill(n, s);
            case SET:
                return TreeMultimap.withSet().fill(n, s);
            case SORTED_SET:
                return TreeMultimap.withSortedSet(Comparators.naturalComparator()).fill(n, s);
            default:
                throw new RuntimeException();
        }
    }

    @Override
    protected <K extends Comparable<K>, V> Multimap<K, V> mapFill(int n, Tuple2<? extends K, ? extends V> element) {
        switch (containerType) {
            case SEQ:
                return TreeMultimap.withSeq().fill(n, element);
            case SET:
                return TreeMultimap.withSet().fill(n, element);
            case SORTED_SET:
                return TreeMultimap.withSortedSet(Comparators.naturalComparator()).fill(n, element);
            default:
                throw new RuntimeException();
        }
    }

    // -- static narrow

    @Test
    public void shouldNarrowMap() {
        final TreeMultimap<Integer, Number> int2doubleMap = (TreeMultimap<Integer, Number>) this.<Integer, Number> emptyMap().put(1, 1.0d);
        final TreeMultimap<Number, Number> number2numberMap = TreeMultimap.narrow(int2doubleMap);
        final int actual = number2numberMap.put(2, new BigDecimal("2.0")).values().sum().intValue();
        assertThat(actual).isEqualTo(3);
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
    public void shouldReturnTrueWhenIsSequentialCalled() {
        assertThat(of(1, 2, 3).isSequential()).isFalse();
    }

}
