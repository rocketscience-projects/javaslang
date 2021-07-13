package io.vavr.collection;

import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

public class LinkedHashMapTest extends AbstractMapTest {

    @Override
    protected String stringPrefix() {
        return "LinkedHashMap";
    }

    @Override
    <T1, T2> java.util.Map<T1, T2> javaEmptyMap() {
        return new java.util.LinkedHashMap<>();
    }

    @Override
    protected <T1 extends Comparable<? super T1>, T2> LinkedHashMap<T1, T2> emptyMap() {
        return LinkedHashMap.empty();
    }

    @Override
    protected <K extends Comparable<? super K>, V, T extends V> Collector<T, ArrayList<T>, ? extends Map<K, V>> collectorWithMapper(Function<? super T, ? extends K> keyMapper) {
        return LinkedHashMap.collector(keyMapper);
    }

    @Override
    protected <K extends Comparable<? super K>, V, T> Collector<T, ArrayList<T>, ? extends Map<K, V>> collectorWithMappers(Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return LinkedHashMap.collector(keyMapper, valueMapper);
    }

    @Override
    protected <T> Collector<Tuple2<Integer, T>, ArrayList<Tuple2<Integer, T>>, ? extends Map<Integer, T>> mapCollector() {
        return LinkedHashMap.collector();
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    @Override
    protected final <K extends Comparable<? super K>, V> LinkedHashMap<K, V> mapOfTuples(Tuple2<? extends K, ? extends V>... entries) {
        return LinkedHashMap.ofEntries(entries);
    }

    @Override
    protected <K extends Comparable<? super K>, V> Map<K, V> mapOfTuples(Iterable<? extends Tuple2<? extends K, ? extends V>> entries) {
        return LinkedHashMap.ofEntries(entries);
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    @Override
    protected final <K extends Comparable<? super K>, V> LinkedHashMap<K, V> mapOfEntries(java.util.Map.Entry<? extends K, ? extends V>... entries) {
        return LinkedHashMap.ofEntries(entries);
    }

    @Override
    protected <K extends Comparable<? super K>, V> LinkedHashMap<K, V> mapOf(K k1, V v1) {
        return LinkedHashMap.of(k1, v1);
    }

    @Override
    protected <K extends Comparable<? super K>, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
        return LinkedHashMap.of(k1, v1, k2, v2);
    }

    @Override
    protected <K extends Comparable<? super K>, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        return LinkedHashMap.of(k1, v1, k2, v2, k3, v3);
    }

    @Override
    protected <T, K extends Comparable<? super K>, V> Map<K, V> mapOf(Stream<? extends T> stream, Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return LinkedHashMap.ofAll(stream, keyMapper, valueMapper);
    }

    @Override
    protected <T, K extends Comparable<? super K>, V> Map<K, V> mapOf(Stream<? extends T> stream, Function<? super T, Tuple2<? extends K, ? extends V>> f) {
        return LinkedHashMap.ofAll(stream, f);
    }

    protected <K extends Comparable<? super K>, V> Map<K, V> mapOfNullKey(K k1, V v1, K k2, V v2) {
        return mapOf(k1, v1, k2, v2);
    }

    @Override
    protected <K extends Comparable<? super K>, V> Map<K, V> mapOfNullKey(K k1, V v1, K k2, V v2, K k3, V v3) {
        return mapOf(k1, v1, k2, v2, k3, v3);
    }

    @Override
    protected <K extends Comparable<? super K>, V> LinkedHashMap<K, V> mapTabulate(int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V>> f) {
        return LinkedHashMap.tabulate(n, f);
    }

    @Override
    protected <K extends Comparable<? super K>, V> LinkedHashMap<K, V> mapFill(int n, Supplier<? extends Tuple2<? extends K, ? extends V>> s) {
        return LinkedHashMap.fill(n, s);
    }

    @Test
    public void shouldKeepOrder() {
        final List<Character> actual = LinkedHashMap.<Integer, Character> empty().put(3, 'a').put(2, 'b').put(1, 'c').foldLeft(List.empty(), (s, t) -> s.append(t._2));
        Assertions.assertThat(actual).isEqualTo(List.of('a', 'b', 'c'));
    }

    @Test
    public void shouldKeepValuesOrder() {
        final List<Character> actual = LinkedHashMap.<Integer, Character> empty().put(3, 'a').put(2, 'b').put(1, 'c').values().foldLeft(List.empty(), List::append);
        Assertions.assertThat(actual).isEqualTo(List.of('a', 'b', 'c'));
    }

    // -- static narrow

    @Test
    public void shouldNarrowLinkedHashMap() {
        final LinkedHashMap<Integer, Double> int2doubleMap = mapOf(1, 1.0d);
        final LinkedHashMap<Number, Number> number2numberMap = LinkedHashMap.narrow(int2doubleMap);
        final int actual = number2numberMap.put(new BigDecimal("2"), new BigDecimal("2.0")).values().sum().intValue();
        assertThat(actual).isEqualTo(3);
    }

    // -- static ofAll(Iterable)

    @Test
    public void shouldWrapMap() {
        final java.util.Map<Integer, Integer> source = new java.util.HashMap<>();
        source.put(1, 2);
        source.put(3, 4);
        assertThat(LinkedHashMap.ofAll(source)).isEqualTo(emptyIntInt().put(1, 2).put(3, 4));
    }

    // -- keySet

    @Test
    public void shouldKeepKeySetOrder() {
        final Set<Integer> keySet = LinkedHashMap.of(4, "d", 1, "a", 2, "b").keySet();
        assertThat(keySet.mkString()).isEqualTo("412");
    }

    // -- map

    @Test
    public void shouldReturnModifiedKeysMapWithNonUniqueMapperAndPredictableOrder() {
        final Map<Integer, String> actual = LinkedHashMap.of(3, "3").put(1, "1").put(2, "2")
                .mapKeys(Integer::toHexString).mapKeys(String::length);
        final Map<Integer, String> expected = LinkedHashMap.of(1, "2");
        assertThat(actual).isEqualTo(expected);
    }
    
    // -- put

    @Test
    public void shouldKeepOrderWhenPuttingAnExistingKeyAndNonExistingValue() {
        final Map<Integer, String> map = mapOf(1, "a", 2, "b", 3, "c");
        final Map<Integer, String> actual = map.put(1, "d");
        final Map<Integer, String> expected = mapOf(1, "d", 2, "b", 3, "c");
        assertThat(actual.toList()).isEqualTo(expected.toList());
    }

    @Test
    public void shouldKeepOrderWhenPuttingAnExistingKeyAndExistingValue() {
        final Map<Integer, String> map = mapOf(1, "a", 2, "b", 3, "c");
        final Map<Integer, String> actual = map.put(1, "a");
        final Map<Integer, String> expected = mapOf(1, "a", 2, "b", 3, "c");
        assertThat(actual.toList()).isEqualTo(expected.toList());
    }

    // -- replace

    @Test
    public void shouldReturnSameInstanceIfReplacingNonExistingPairUsingNonExistingKey() {
        final Map<Integer, String> map = LinkedHashMap.of(1, "a", 2, "b");
        final Map<Integer, String> actual = map.replace(Tuple.of(0, "?"), Tuple.of(0, "!"));
        assertThat(actual).isSameAs(map);
    }

    @Test
    public void shouldReturnSameInstanceIfReplacingNonExistingPairUsingExistingKey() {
        final Map<Integer, String> map = LinkedHashMap.of(1, "a", 2, "b");
        final Map<Integer, String> actual = map.replace(Tuple.of(2, "?"), Tuple.of(2, "!"));
        assertThat(actual).isSameAs(map);
    }

    @Test
    public void shouldPreserveOrderWhenReplacingExistingPairWithSameKeyAndDifferentValue() {
        final Map<Integer, String> map = LinkedHashMap.of(1, "a", 2, "b", 3, "c");
        final Map<Integer, String> actual = map.replace(Tuple.of(2, "b"), Tuple.of(2, "B"));
        final Map<Integer, String> expected = LinkedHashMap.of(1, "a", 2, "B", 3, "c");
        assertThat(actual).isEqualTo(expected);
        Assertions.assertThat(List.ofAll(actual)).isEqualTo(List.ofAll(expected));
    }

    @Test
    public void shouldPreserveOrderWhenReplacingExistingPairWithDifferentKeyValue() {
        final Map<Integer, String> map = LinkedHashMap.of(1, "a", 2, "b", 3, "c");
        final Map<Integer, String> actual = map.replace(Tuple.of(2, "b"), Tuple.of(4, "B"));
        final Map<Integer, String> expected = LinkedHashMap.of(1, "a", 4, "B", 3, "c");
        assertThat(actual).isEqualTo(expected);
        Assertions.assertThat(List.ofAll(actual)).isEqualTo(List.ofAll(expected));
    }

    @Test
    public void shouldPreserveOrderWhenReplacingExistingPairAndRemoveOtherIfKeyAlreadyExists() {
        final Map<Integer, String> map = LinkedHashMap.of(1, "a", 2, "b", 3, "c", 4, "d", 5, "e");
        final Map<Integer, String> actual = map.replace(Tuple.of(2, "b"), Tuple.of(4, "B"));
        final Map<Integer, String> expected = LinkedHashMap.of(1, "a", 4, "B", 3, "c", 5, "e");
        assertThat(actual).isEqualTo(expected);
        Assertions.assertThat(List.ofAll(actual)).isEqualTo(List.ofAll(expected));
    }

    @Test
    public void shouldReturnSameInstanceWhenReplacingExistingPairWithIdentity() {
        final Map<Integer, String> map = LinkedHashMap.of(1, "a", 2, "b", 3, "c");
        final Map<Integer, String> actual = map.replace(Tuple.of(2, "b"), Tuple.of(2, "b"));
        assertThat(actual).isSameAs(map);
    }

    // -- scan, scanLeft, scanRight

    @Test
    public void shouldScan() {
        final Map<Integer, String> map = this.<Integer, String> emptyMap()
                .put(Tuple.of(1, "a"))
                .put(Tuple.of(2, "b"))
                .put(Tuple.of(3, "c"))
                .put(Tuple.of(4, "d"));
        final Map<Integer, String> result = map.scan(Tuple.of(0, "x"), (t1, t2) -> Tuple.of(t1._1 + t2._1, t1._2 + t2._2));
        assertThat(result).isEqualTo(LinkedHashMap.empty()
                .put(0, "x")
                .put(1, "xa")
                .put(3, "xab")
                .put(6, "xabc")
                .put(10, "xabcd"));
    }

    @Test
    public void shouldScanLeft() {
        final Map<Integer, String> map = this.<Integer, String> emptyMap()
                .put(Tuple.of(1, "a"))
                .put(Tuple.of(2, "b"))
                .put(Tuple.of(3, "c"))
                .put(Tuple.of(4, "d"));
        final Seq<Tuple2<Integer, String>> result = map.scanLeft(Tuple.of(0, "x"), (t1, t2) -> Tuple.of(t1._1 + t2._1, t1._2 + t2._2));
        assertThat(result).isEqualTo(List.of(
                Tuple.of(0, "x"),
                Tuple.of(1, "xa"),
                Tuple.of(3, "xab"),
                Tuple.of(6, "xabc"),
                Tuple.of(10, "xabcd")));
    }

    @Test
    public void shouldScanRight() {
        final Map<Integer, String> map = this.<Integer, String> emptyMap()
                .put(Tuple.of(1, "a"))
                .put(Tuple.of(2, "b"))
                .put(Tuple.of(3, "c"))
                .put(Tuple.of(4, "d"));
        final Seq<Tuple2<Integer, String>> result = map.scanRight(Tuple.of(0, "x"), (t1, t2) -> Tuple.of(t1._1 + t2._1, t1._2 + t2._2));
        assertThat(result).isEqualTo(List.of(
                Tuple.of(10, "abcdx"),
                Tuple.of(9, "bcdx"),
                Tuple.of(7, "cdx"),
                Tuple.of(4, "dx"),
                Tuple.of(0, "x")));
    }

    // -- spliterator

    @Test
    public void shouldNotHaveSortedSpliterator() {
        assertThat(of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.SORTED)).isFalse();
    }

    @Test
    public void shouldHaveOrderedSpliterator() {
        assertThat(of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.ORDERED)).isTrue();
    }

    // -- isSequential()

    @Test
    public void shouldReturnTrueWhenIsSequentialCalled() {
        assertThat(LinkedHashMap.of(1, 2, 3, 4).isSequential()).isTrue();
    }

}
