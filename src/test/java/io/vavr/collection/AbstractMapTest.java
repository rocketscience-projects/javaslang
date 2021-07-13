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
import org.assertj.core.api.IterableAssert;
import org.junit.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.stream.Collector;

import static io.vavr.API.Some;
import static io.vavr.Serializables.deserialize;
import static io.vavr.Serializables.serialize;
import static java.util.Arrays.asList;

public abstract class AbstractMapTest extends AbstractTraversableTest {

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
                actualMap.forEach((k, v) -> assertThat(v).isEqualTo(expectedMap.get(k)));
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
    protected <T> Collector<T, ArrayList<T>, IntMap<T>> collector() {
        final Collector<Tuple2<Integer, T>, ArrayList<Tuple2<Integer, T>>, ? extends Map<Integer, T>> mapCollector = mapCollector();
        return new Collector<T, ArrayList<T>, IntMap<T>>() {
            @Override
            public Supplier<ArrayList<T>> supplier() {
                return ArrayList::new;
            }

            @Override
            public BiConsumer<ArrayList<T>, T> accumulator() {
                return ArrayList::add;
            }

            @Override
            public BinaryOperator<ArrayList<T>> combiner() {
                return (left, right) -> fromTuples(mapCollector.combiner().apply(toTuples(left), toTuples(right)));
            }

            @Override
            public Function<ArrayList<T>, IntMap<T>> finisher() {
                return AbstractMapTest.this::ofAll;
            }

            @Override
            public Set<Characteristics> characteristics() {
                return mapCollector.characteristics();
            }

            private ArrayList<Tuple2<Integer, T>> toTuples(java.util.List<T> list) {
                final ArrayList<Tuple2<Integer, T>> result = new ArrayList<>();
                Stream.ofAll(list)
                        .zipWithIndex()
                        .map(tu -> Tuple.of(tu._2, tu._1))
                        .forEach(result::add);
                return result;
            }

            private ArrayList<T> fromTuples(java.util.List<Tuple2<Integer, T>> list) {
                final ArrayList<T> result = new ArrayList<>();
                Stream.ofAll(list)
                        .map(tu -> tu._2)
                        .forEach(result::add);
                return result;
            }
        };
    }

    @Override
    protected <T> IntMap<T> empty() {
        return IntMap.of(emptyMap());
    }

    @Override
    protected boolean emptyShouldBeSingleton() {
        return false;
    }

    private <T> Map<Integer, T> emptyInt() {
        return emptyMap();
    }

    protected Map<Integer, Integer> emptyIntInt() {
        return emptyMap();
    }

    private Map<Integer, String> emptyIntString() {
        return emptyMap();
    }

    abstract <T1, T2> java.util.Map<T1, T2> javaEmptyMap();

    protected abstract <T1 extends Comparable<? super T1>, T2> Map<T1, T2> emptyMap();

    protected abstract <K extends Comparable<? super K>, V, T extends V> Collector<T, ArrayList<T>, ? extends Map<K, V>> collectorWithMapper(
            Function<? super T,? extends K> keyMapper);

    protected abstract <K extends Comparable<? super K>, V, T> Collector<T, ArrayList<T>, ? extends Map<K, V>> collectorWithMappers(
            Function<? super T,? extends K> keyMapper, Function<? super T, ? extends V> valueMapper);

    protected boolean emptyMapShouldBeSingleton() {
        return true;
    }

    protected abstract <T> Collector<Tuple2<Integer, T>, ArrayList<Tuple2<Integer, T>>, ? extends Map<Integer, T>> mapCollector();

    @SuppressWarnings("unchecked")
    protected abstract <K extends Comparable<? super K>, V> Map<K, V> mapOfTuples(Tuple2<? extends K, ? extends V>... entries);

    protected abstract <K extends Comparable<? super K>, V> Map<K, V> mapOfTuples(Iterable<? extends Tuple2<? extends K, ? extends V>> entries);

    @SuppressWarnings("unchecked")
    protected abstract <K extends Comparable<? super K>, V> Map<K, V> mapOfEntries(java.util.Map.Entry<? extends K, ? extends V>... entries);

    protected abstract <K extends Comparable<? super K>, V> Map<K, V> mapOf(K k1, V v1);

    protected abstract <K extends Comparable<? super K>, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2);

    protected abstract <K extends Comparable<? super K>, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3);

    protected abstract <T, K extends Comparable<? super K>, V> Map<K, V> mapOf(java.util.stream.Stream<? extends T> stream,
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper);

    protected abstract <T, K extends Comparable<? super K>, V> Map<K, V> mapOf(java.util.stream.Stream<? extends T> stream,
            Function<? super T, Tuple2<? extends K, ? extends V>> f);

    protected abstract <K extends Comparable<? super K>, V> Map<K, V> mapOfNullKey(K k1, V v1, K k2, V v2);

    protected abstract <K extends Comparable<? super K>, V> Map<K, V> mapOfNullKey(K k1, V v1, K k2, V v2, K k3, V v3);

    protected abstract <K extends Comparable<? super K>, V> Map<K, V> mapTabulate(int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V>> f);

    protected abstract <K extends Comparable<? super K>, V> Map<K, V> mapFill(int n, Supplier<? extends Tuple2<? extends K, ? extends V>> s);

    @Override
    protected boolean useIsEqualToInsteadOfIsSameAs() {
        return true;
    }

    @Override
    protected int getPeekNonNilPerformingAnAction() {
        return 1;
    }

    @Override
    protected <T> IntMap<T> of(T element) {
        Map<Integer, T> map = emptyMap();
        map = map.put(0, element);
        return IntMap.of(map);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <T> IntMap<T> of(T... elements) {
        Map<Integer, T> map = emptyMap();
        for (T element : elements) {
            map = map.put(map.size(), element);
        }
        return IntMap.of(map);
    }

    @Override
    protected <T> IntMap<T> ofAll(Iterable<? extends T> elements) {
        Map<Integer, T> map = emptyMap();
        for (T element : elements) {
            map = map.put(map.size(), element);
        }
        return IntMap.of(map);
    }

    @Override
    protected <T extends Comparable<? super T>> IntMap<T> ofJavaStream(java.util.stream.Stream<? extends T> javaStream) {
        return ofAll(io.vavr.collection.Iterator.ofAll(javaStream.iterator()));
    }

    @Override
    protected IntMap<Boolean> ofAll(boolean... elements) {
        return ofAll(io.vavr.collection.Iterator.ofAll(elements));
    }

    @Override
    protected IntMap<Byte> ofAll(byte... elements) {
        return ofAll(io.vavr.collection.Iterator.ofAll(elements));
    }

    @Override
    protected IntMap<Character> ofAll(char... elements) {
        return ofAll(io.vavr.collection.Iterator.ofAll(elements));
    }

    @Override
    protected IntMap<Double> ofAll(double... elements) {
        return ofAll(io.vavr.collection.Iterator.ofAll(elements));
    }

    @Override
    protected IntMap<Float> ofAll(float... elements) {
        return ofAll(io.vavr.collection.Iterator.ofAll(elements));
    }

    @Override
    protected IntMap<Integer> ofAll(int... elements) {
        return ofAll(io.vavr.collection.Iterator.ofAll(elements));
    }

    @Override
    protected IntMap<Long> ofAll(long... elements) {
        return ofAll(io.vavr.collection.Iterator.ofAll(elements));
    }

    @Override
    protected IntMap<Short> ofAll(short... elements) {
        return ofAll(io.vavr.collection.Iterator.ofAll(elements));
    }

    @Override
    protected <T> IntMap<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Map<Integer, T> map = emptyMap();
        for (int i = 0; i < n; i++) {
            map = map.put(map.size(), f.apply(i));
        }
        return IntMap.of(map);
    }

    @Override
    protected <T> IntMap<T> fill(int n, Supplier<? extends T> s) {
        return tabulate(n, anything -> s.get());
    }

    // -- narrow

    @Test
    public void shouldNarrowMap() {
        final Map<Integer, Double> int2doubleMap = mapOf(1, 1.0d);
        final Map<Number, Number> number2numberMap = Map.narrow(int2doubleMap);
        final int actual = number2numberMap.put(new BigDecimal("2"), new BigDecimal("2.0")).values().sum().intValue();
        assertThat(actual).isEqualTo(3);
    }

    // -- mappers collector

    @Test
    public void shouldCollectWithKeyMapper() {
        Map<Integer, Integer> map = java.util.stream.Stream.of(1, 2, 3).collect(collectorWithMapper(i -> i * 2));
        assertThat(map).isEqualTo(mapOf(2, 1, 4, 2, 6, 3));
    }

    @Test
    public void shouldCollectWithKeyValueMappers() {
        Map<Integer, String> map = java.util.stream.Stream.of(1, 2, 3).collect(collectorWithMappers(i -> i * 2, String::valueOf));
        assertThat(map).isEqualTo(mapOf(2, "1", 4, "2", 6, "3"));
    }

    // -- construction

    @Test
    public void shouldBeTheSame() {
        assertThat(mapOf(1, 2)).isEqualTo(emptyInt().put(1, 2));
    }

    protected static java.util.Map.Entry<Integer, String> asJavaEntry(int key, String value) {
        return new java.util.AbstractMap.SimpleEntry<>(key, value);
    }

    @SafeVarargs
    protected final <K, V> java.util.Map<K, V> asJavaMap(java.util.Map.Entry<K, V>... entries) {
        final java.util.Map<K, V> results = javaEmptyMap();
        for (java.util.Map.Entry<K, V> entry : entries) {
            results.put(entry.getKey(), entry.getValue());
        }
        return results;
    }

    @Test
    public void shouldConstructFromJavaStream() {
        final java.util.stream.Stream<Integer> javaStream = java.util.stream.Stream.of(1, 2, 3);
        final Map<String, Integer> map = mapOf(javaStream, String::valueOf, Function.identity());
        assertThat(map).isEqualTo(this.<String, Integer> emptyMap().put("1", 1).put("2", 2).put("3", 3));
    }

    @Test
    public void shouldConstructFromJavaStreamWithDuplicatedKeys() {
        assertThat(mapOf(Stream.range(0, 4).toJavaStream()
                , i -> Math.max(1, Math.min(i, 2))
                , i -> String.valueOf(i + 1)
        ))
                .hasSize(2)
                .isEqualTo(mapOf(1, "2", 2, "4"));
    }

    @Test
    public void shouldConstructFromJavaStreamEntries() {
        final java.util.stream.Stream<Integer> javaStream = java.util.stream.Stream.of(1, 2, 3);
        final Map<String, Integer> map = mapOf(javaStream, i -> Tuple.of(String.valueOf(i), i));
        assertThat(map).isEqualTo(this.<String, Integer> emptyMap().put("1", 1).put("2", 2).put("3", 3));
    }

    @Test
    public void shouldConstructFromJavaStreamEntriesWithDuplicatedKeys() {
        assertThat(mapOf(Stream.range(0, 4).toJavaStream(), i ->
                Map.entry(Math.max(1, Math.min(i, 2)), String.valueOf(i + 1))
        ))
                .hasSize(2)
                .isEqualTo(mapOf(1, "2", 2, "4"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldConstructFromUtilEntries() {
        final Map<Integer, String> actual = mapOfEntries(asJavaEntry(1, "1"), asJavaEntry(2, "2"), asJavaEntry(3, "3"));
        final Map<Integer, String> expected = this.<Integer, String>emptyMap().put(1, "1").put(2, "2").put(3, "3");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldConstructFromUtilEntriesWithDuplicatedKeys() {
        assertThat(mapOfEntries(
                asJavaEntry(1, "1"), asJavaEntry(1, "2"),
                asJavaEntry(2, "3"), asJavaEntry(2, "4")
        ))
                .hasSize(2)
                .isEqualTo(mapOf(1, "2", 2, "4"));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldConstructFromEntriesVararg() {
        final Map<String, Integer> actual = mapOfTuples(Map.entry("1", 1), Map.entry("2", 2), Map.entry("3", 3));
        final Map<String, Integer> expected = this.<String, Integer>emptyMap().put("1", 1).put("2", 2).put("3", 3);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldConstructFromEntriesVarargWithDuplicatedKeys() {
        assertThat(mapOfTuples(
                Map.entry(1, "1"), Map.entry(1, "2"),
                Map.entry(2, "3"), Map.entry(2, "4")
        ))
                .hasSize(2)
                .isEqualTo(mapOf(1, "2", 2, "4"));
    }

    @Test
    public void shouldConstructFromEntriesIterable() {
        final Map<String, Integer> actual = mapOfTuples(asList(Map.entry("1", 1), Map.entry("2", 2), Map.entry("3", 3)));
        final Map<String, Integer> expected = this.<String, Integer>emptyMap().put("1", 1).put("2", 2).put("3", 3);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldConstructFromEntriesIterableWithDuplicatedKeys() {
        assertThat(mapOfTuples(asList(
                Map.entry(1, "1"), Map.entry(1, "2"),
                Map.entry(2, "3"), Map.entry(2, "4")
        )))
                .hasSize(2)
                .isEqualTo(mapOf(1, "2", 2, "4"));
    }

    @Test
    public void shouldConstructFromPairs() {
        final Map<String, Integer> actual = mapOf("1", 1, "2", 2, "3", 3);
        final Map<String, Integer> expected = this.<String, Integer>emptyMap().put("1", 1).put("2", 2).put("3", 3);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldConstructFromPairsWithDuplicatedKeys() {
        final Map<Integer, String> actual = mapOf(1, "1", 1, "2", 2, "3");
        final Map<Integer, String> expected = this.<Integer, String>emptyMap().put(1, "2").put(2, "3");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldConstructWithTabulate() {
        final Map<String, Integer> actual = mapTabulate(4, i -> Tuple.of(i.toString(), i));
        final Map<String, Integer> expected = this.<String, Integer>emptyMap().put("0", 0).put("1", 1).put("2", 2).put("3", 3);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldConstructWithTabulateWithDuplicatedKeys() {
        assertThat(mapTabulate(4, i ->
                Tuple.of(Math.max(1, Math.min(i, 2)), String.valueOf(i + 1))
        ))
                .hasSize(2)
                .isEqualTo(mapOf(1, "2", 2, "4"));
    }

    @Test
    public void shouldConstructWithFill() {
        AtomicInteger i = new AtomicInteger();
        final Map<String, Integer> actual = mapFill(4, () -> Tuple.of(String.valueOf(i.get()), i.getAndIncrement()));
        final Map<String, Integer> expected = this.<String, Integer>emptyMap().put("0", 0).put("1", 1).put("2", 2).put("3", 3);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldConstructWithFillWithDuplicatedKeys() {
        AtomicInteger i = new AtomicInteger();
        assertThat(mapFill(4, () ->
                Tuple.of(Math.max(1, Math.min(i.get(), 2)), String.valueOf(i.getAndIncrement() + 1))
        ))
                .hasSize(2)
                .isEqualTo(mapOf(1, "2", 2, "4"));
    }

    // -- asPartialFunction

    @Test
    public void shouldImplementPartialFunction() {
        PartialFunction<Integer, String> f = mapOf(1, "1").asPartialFunction();
        assertThat(f.isDefinedAt(1)).isTrue();
        assertThat(f.apply(1)).isEqualTo("1");
        assertThat(f.isDefinedAt(2)).isFalse();
    }

    @Test
    public void shouldApplyExistingKey() {
        assertThat(emptyInt().put(1, 2).asPartialFunction().apply(1)).isEqualTo(2);
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldApplyNonExistingKey() {
        emptyInt().put(1, 2).asPartialFunction().apply(3);
    }

    // -- equality

    @Test
    public void shouldObeyEqualityConstraints() {

        // sequential collections
        assertThat(emptyMap().equals(io.vavr.collection.HashMap.empty())).isTrue();
        assertThat(mapOf(1, "a").equals(io.vavr.collection.HashMap.of(1, "a"))).isTrue();
        assertThat(mapOf(1, "a", 2, "b", 3, "c").equals(io.vavr.collection.HashMap.of(1, "a", 2, "b",3, "c"))).isTrue();
        assertThat(mapOf(1, "a", 2, "b", 3, "c").equals(io.vavr.collection.HashMap.of(3, "c", 2, "b",1, "a"))).isTrue();

        // other classes
        assertThat(empty().equals(io.vavr.collection.List.empty())).isFalse();
        assertThat(empty().equals(HashMultimap.withSeq().empty())).isFalse();
        assertThat(empty().equals(io.vavr.collection.HashSet.empty())).isFalse();

        assertThat(empty().equals(LinkedHashMultimap.withSeq().empty())).isFalse();
        assertThat(empty().equals(io.vavr.collection.LinkedHashSet.empty())).isFalse();

        assertThat(empty().equals(TreeMultimap.withSeq().empty())).isFalse();
        assertThat(empty().equals(io.vavr.collection.TreeSet.empty())).isFalse();
    }

    // -- head

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowWhenHeadEmpty() {
        emptyMap().head();
    }

    // -- init

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowWhenInitEmpty() {
        emptyMap().init();
    }

    // -- tail

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowWhenTailEmpty() {
        emptyMap().tail();
    }

    // -- toString

    @Test
    public void shouldMakeString() {
        assertThat(emptyMap().toString()).endsWith("Map()");
        assertThat(emptyInt().put(1, 2).toString()).endsWith("Map(" + Tuple.of(1, 2) + ")");
    }

    // -- toJavaMap

    @Test
    public void shouldConvertToJavaMap() {
        final Map<Integer, String> actual = mapOf(1, "1", 2, "2", 3, "3");
        final java.util.Map<Integer, String> expected = asJavaMap(asJavaEntry(1, "1"), asJavaEntry(2, "2"), asJavaEntry(3, "3"));
        assertThat(actual.toJavaMap()).isEqualTo(expected);
    }

    // -- contains

    @Test
    public void shouldFindKey() {
        assertThat(emptyInt().put(1, 2).containsKey(1)).isTrue();
        assertThat(emptyInt().put(1, 2).containsKey(2)).isFalse();
    }

    @Test
    public void shouldFindValue() {
        assertThat(emptyInt().put(1, 2).containsValue(2)).isTrue();
        assertThat(emptyInt().put(1, 2).containsValue(1)).isFalse();
    }

    @Test
    public void shouldRecognizeNotContainedKeyValuePair() {
        final io.vavr.collection.TreeMap<String, Integer> testee = io.vavr.collection.TreeMap.of(Tuple.of("one", 1));
        assertThat(testee.contains(Tuple.of("one", 0))).isFalse();
    }

    @Test
    public void shouldRecognizeContainedKeyValuePair() {
        final io.vavr.collection.TreeMap<String, Integer> testee = io.vavr.collection.TreeMap.of(Tuple.of("one", 1));
        assertThat(testee.contains(Tuple.of("one", 1))).isTrue();
    }

    // -- flatMap

    @SuppressWarnings("unchecked")
    @Test
    public void shouldFlatMapUsingBiFunction() {
        final Map<Integer, Integer> testee = mapOfTuples(Tuple.of(1, 11), Tuple.of(2, 22), Tuple.of(3, 33));
        final Map<String, String> actual = testee
                .flatMap((k, v) -> io.vavr.collection.List.of(Tuple.of(String.valueOf(k), String.valueOf(v)),
                        Tuple.of(String.valueOf(k * 10), String.valueOf(v * 10))));
        final Map<String, String> expected = mapOfTuples(Tuple.of("1", "11"), Tuple.of("10", "110"), Tuple.of("2", "22"),
                Tuple.of("20", "220"), Tuple.of("3", "33"), Tuple.of("30", "330"));
        assertThat(actual).isEqualTo(expected);
    }

    // -- keySet

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnKeySet() {
        final io.vavr.collection.Set<Integer> actual = mapOfTuples(Tuple.of(1, 11), Tuple.of(2, 22), Tuple.of(3, 33)).keySet();
        assertThat(actual).isEqualTo(io.vavr.collection.HashSet.of(1, 2, 3));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnKeysIterator() {
        final Iterator<Integer> actual = mapOfTuples(Tuple.of(1, 11), Tuple.of(2, 22), Tuple.of(3, 33)).keysIterator();
        assertThat(actual).isEqualTo(io.vavr.collection.Iterator.of(1, 2, 3));
    }

    // -- values

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnValuesSeq() {
        final Seq<Integer> actual = mapOfTuples(Tuple.of(1, 11), Tuple.of(2, 22), Tuple.of(3, 33)).values();
        assertThat(actual).isEqualTo(io.vavr.collection.Iterator.of(11, 22, 33));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldReturnValuesIterator() {
        final Iterator<Integer> actual = mapOfTuples(Tuple.of(1, 11), Tuple.of(2, 22), Tuple.of(3, 33)).valuesIterator();
        assertThat(actual).isEqualTo(io.vavr.collection.Iterator.of(11, 22, 33));
    }

    // -- biMap

    @Test
    public void shouldBiMapEmpty() {
        assertThat(emptyInt().bimap(i -> i + 1, o -> o)).isEqualTo(io.vavr.collection.Vector.empty());
    }

    @Test
    public void shouldBiMapNonEmpty() {
        final Seq<Tuple2<Integer, String>> expected = Stream.of(Tuple.of(2, "1!"), Tuple.of(3, "2!"));
        final Seq<Tuple2<Integer, String>> actual = emptyInt().put(1, "1").put(2, "2").bimap(i -> i + 1, s -> s + "!").toStream();
        assertThat(actual).isEqualTo(expected);
    }

    // -- orElse
    // DEV-Note: IntMap converts `other` to map

    @Override
    @Test
    public void shouldCaclEmptyOrElseSameOther() {
        Iterable<Integer> other = of(42);
        assertThat(empty().orElse(other)).isEqualTo(other);
    }

    @Test
    public void shouldCaclEmptyOrElseSameSupplier() {
        Iterable<Integer> other = of(42);
        Supplier<Iterable<Integer>> supplier = () -> other;
        assertThat(empty().orElse(supplier)).isEqualTo(other);
    }

    // -- map

    @Test
    public void shouldMapEmpty() {
        assertThat(emptyInt().map(Tuple2::_1)).isEqualTo(io.vavr.collection.Vector.empty());
    }

    @Test
    public void shouldMapNonEmpty() {
        final Seq<Integer> expected = io.vavr.collection.Vector.of(1, 2);
        final Seq<Integer> actual = emptyInt().put(1, "1").put(2, "2").map(Tuple2::_1);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldReturnEmptySetWhenAskedForTuple2SetOfAnEmptyMap() {
        assertThat(emptyMap().toSet()).isEqualTo(io.vavr.collection.HashSet.empty());
    }

    @Test
    public void shouldReturnTuple2SetOfANonEmptyMap() {
        assertThat(emptyInt().put(1, "1").put(2, "2").toSet()).isEqualTo(io.vavr.collection.HashSet.of(Tuple.of(1, "1"), Tuple.of(2, "2")));
    }

    @Test
    public void shouldReturnModifiedKeysMap() {
        final Map<String, String> actual = emptyIntString().put(1, "1").put(2, "2").mapKeys(k -> k * 12).mapKeys(Integer::toHexString).mapKeys(String::toUpperCase);
        final Map<String, String> expected = this.<String, String> emptyMap().put("C", "1").put("18", "2");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldReturnModifiedKeysMapWithNonUniqueMapper() {
        final Map<Integer, String> actual = emptyIntString()
                .put(1, "1").put(2, "2").put(3, "3")
                .mapKeys(k -> k * 118).mapKeys(Integer::toHexString).mapKeys(AbstractMapTest::md5).mapKeys(String::length);
        assertThat(actual).hasSize(1);
        assertThat(actual.values()).hasSize(1);
        //In different cases (based on items order) transformed map may contain different values
        assertThat(actual.values().head()).isIn("1", "2", "3");
    }

    public static String md5(String src) {
        try {
            final MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(src.getBytes(StandardCharsets.UTF_8));
            return toHexString(md.digest());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Returns a string in the hexadecimal format.
     *
     * @param bytes the converted bytes
     * @return the hexadecimal string representing the bytes data
     * @throws IllegalArgumentException if the byte array is null
     */
    public static String toHexString(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("byte array must not be null");
        }

        final StringBuilder hex = new StringBuilder(bytes.length * 2);
        for (byte aByte : bytes) {
            hex.append(Character.forDigit((aByte & 0XF0) >> 4, 16));
            hex.append(Character.forDigit((aByte & 0X0F), 16));
        }
        return hex.toString();
    }

    @Test
    public void shouldReturnModifiedKeysMapWithNonUniqueMapperAndMergedValues() {
        final Map<Integer, String> actual = emptyIntString()
                .put(1, "1").put(2, "2").put(3, "3")
                .mapKeys(k -> k * 118).mapKeys(Integer::toHexString).mapKeys(AbstractMapTest::md5)//Unique key mappers
                .mapKeys(String::length, (v1, v2) -> io.vavr.collection.List.of(v1.split("#")).append(v2).sorted().mkString("#"));
        final Map<Integer, String> expected = emptyIntString().put(32, "1#2#3");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldReturnModifiedValuesMap() {
        assertThat(emptyIntString().put(1, "1").put(2, "2").mapValues(Integer::parseInt)).isEqualTo(emptyInt().put(1, 1).put(2, 2));
    }

    @Test
    public void shouldReturnListWithMappedValues() {
        assertThat(emptyIntInt().put(1, 1).put(2, 2).iterator((a, b) -> a + b).toList()).isEqualTo(io.vavr.collection.List.of(2, 4));
    }

    // -- merge(Map)

    @Test
    public void shouldMerge() {
        final Map<Integer, Integer> m1 = emptyIntInt().put(1, 1).put(2, 2);
        final Map<Integer, Integer> m2 = emptyIntInt().put(1, 1).put(4, 4);
        final Map<Integer, Integer> m3 = emptyIntInt().put(3, 3).put(4, 4);
        assertThat(m1.merge(m2)).isEqualTo(emptyIntInt().put(1, 1).put(2, 2).put(4, 4));
        assertThat(m1.merge(m3)).isEqualTo(emptyIntInt().put(1, 1).put(2, 2).put(3, 3).put(4, 4));
    }

    @Test
    public void shouldReturnSameMapWhenMergeNonEmptyWithEmpty() {
        final Map<Integer, String> map = mapOf(1, "a", 2, "b", 3, "c");
        assertThat(map.merge(emptyMap())).isSameAs(map);
    }

    @Test
    public void shouldReturnSameMapWhenMergeEmptyWithNonEmpty() {
        final Map<Integer, String> map = mapOf(1, "a", 2, "b", 3, "c");
        if (map.isOrdered()) {
            assertThat(this.<Integer, String> emptyMap().merge(map)).isEqualTo(map);
        } else {
            assertThat(this.<Integer, String> emptyMap().merge(map)).isSameAs(map);
        }
    }

    // -- merge(Map, BiFunction)

    @Test
    public void shouldMergeCollisions() {
        final Map<Integer, Integer> m1 = emptyIntInt().put(1, 1).put(2, 2);
        final Map<Integer, Integer> m2 = emptyIntInt().put(1, 2).put(4, 4);
        final Map<Integer, Integer> m3 = emptyIntInt().put(3, 3).put(4, 4);
        assertThat(emptyIntInt().merge(m2, Math::max)).isEqualTo(m2);
        assertThat(m2.merge(emptyIntInt(), Math::max)).isEqualTo(m2);
        assertThat(m1.merge(m2, Math::max)).isEqualTo(emptyIntInt().put(1, 2).put(2, 2).put(4, 4));
        assertThat(m1.merge(m3, Math::max)).isEqualTo(emptyIntInt().put(1, 1).put(2, 2).put(3, 3).put(4, 4));
    }

    @Test
    public void shouldReturnSameMapWhenMergeNonEmptyWithEmptyUsingCollisionResolution() {
        final Map<Integer, Integer> map = mapOf(1, 1, 2, 2, 3, 3);
        assertThat(map.merge(emptyMap(), Math::max)).isSameAs(map);
    }

    @Test
    public void shouldReturnSameMapWhenMergeEmptyWithNonEmptyUsingCollisionResolution() {
        final Map<Integer, Integer> map = mapOf(1, 1, 2, 2, 3, 3);
        if (map.isOrdered()) {
            assertThat(this.<Integer, Integer> emptyMap().merge(map, Math::max)).isEqualTo(map);
        } else {
            assertThat(this.<Integer, Integer> emptyMap().merge(map, Math::max)).isSameAs(map);
        }
    }

    // -- equality

    @Test
    public void shouldIgnoreOrderOfEntriesWhenComparingForEquality() {
        final Map<?, ?> map1 = emptyInt().put(1, 'a').put(2, 'b').put(3, 'c');
        final Map<?, ?> map2 = emptyInt().put(3, 'c').put(2, 'b').put(1, 'a').remove(2).put(2, 'b');
        assertThat(map1).isEqualTo(map2);
    }

    // -- put

    @Test
    public void shouldPutTuple() {
        assertThat(emptyIntInt().put(Tuple.of(1, 2))).isEqualTo(emptyIntInt().put(1, 2));
    }

    @Test
    public void shouldPutNullKeyIntoMapThatContainsNullKey() {
        final Map<Integer, String> map = mapOfNullKey(1, "a", null, "b", 2, "c");
        assertThat(map.put(null, "!")).isEqualTo(mapOfNullKey(1, "a", null, "!", 2, "c"));
    }

    @Test
    public void shouldPutExistingKeyAndNonEqualValue() {
        final Map<IntMod2, String> map = mapOf(new IntMod2(1), "a");

        // we need to compare Strings because equals (intentionally) does not work for IntMod2
        final String actual = map.put(new IntMod2(3), "b").toString();
        final String expected = map.stringPrefix() + "((3, b))";

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldPutExistingKeyAndEqualValue() {
        final Map<IntMod2, String> map = mapOf(new IntMod2(1), "a");

        // we need to compare Strings because equals (intentionally) does not work for IntMod2
        final String actual = map.put(new IntMod2(3), "a").toString();
        final String expected = map.stringPrefix() + "((3, a))";

        assertThat(actual).isEqualTo(expected);
    }

    // -- remove

    @Test
    public void shouldRemoveKey() {
        final Map<Integer, Object> src = emptyInt().put(1, 'a').put(2, 'b').put(3, 'c');
        assertThat(src.remove(2)).isEqualTo(emptyInt().put(1, 'a').put(3, 'c'));
        assertThat(src.remove(33)).isSameAs(src);
    }

    @Test
    public void shouldRemoveFromMapThatContainsFirstEntryHavingNullKey() {
        final Map<Integer, String> map = mapOfNullKey(null, "a", 1, "b", 2, "c");
        assertThat(map.remove(1)).isEqualTo(mapOfNullKey(null, "a", 2, "c"));
    }

    // -- removeAll

    @Test
    public void shouldRemoveAllKeys() {
        final Map<Integer, Object> src = emptyInt().put(1, 'a').put(2, 'b').put(3, 'c');
        assertThat(src.removeAll(io.vavr.collection.List.of(1, 3))).isEqualTo(emptyInt().put(2, 'b'));
        assertThat(src.removeAll(io.vavr.collection.List.of(33))).isSameAs(src);
        assertThat(src.removeAll(io.vavr.collection.List.empty())).isSameAs(src);
    }

    @Test
    public void shouldReturnSameMapWhenNonEmptyRemoveAllEmpty() {
        final Map<Integer, String> map = mapOf(1, "a", 2, "b", 3, "c");
        assertThat(map.removeAll(io.vavr.collection.List.empty())).isSameAs(map);
    }

    @Test
    public void shouldReturnSameMapWhenEmptyRemoveAllNonEmpty() {
        final Map<Integer, String> empty = emptyMap();
        assertThat(empty.removeAll(io.vavr.collection.List.of(1, 2, 3))).isSameAs(empty);
    }

    // -- transform

    @Test
    public void shouldTransform() {
        final Map<?, ?> actual = emptyIntInt().put(1, 11).transform(map -> map.put(2, 22));
        assertThat(actual).isEqualTo(emptyIntInt().put(1, 11).put(2, 22));
    }

    // -- zip

    @Test
    public void shouldZipNils() {
        final Seq<Tuple2<Tuple2<Integer, Object>, Object>> actual = emptyInt().zip(io.vavr.collection.List.empty());
        assertThat(actual).isEqualTo(Stream.empty());
    }

    @Test
    public void shouldZipEmptyAndNonNil() {
        final Seq<Tuple2<Tuple2<Integer, Object>, Integer>> actual = emptyInt().zip(io.vavr.collection.List.of(1));
        assertThat(actual).isEqualTo(Stream.empty());
    }

    @Test
    public void shouldZipNonEmptyAndNil() {
        final Seq<Tuple2<Tuple2<Integer, Integer>, Object>> actual = emptyIntInt().put(0, 1).zip(io.vavr.collection.List.empty());
        assertThat(actual).isEqualTo(Stream.empty());
    }

    @Test
    public void shouldZipNonNilsIfThisIsSmaller() {
        final Seq<Tuple2<Tuple2<Integer, Integer>, Integer>> actual = emptyIntInt()
                .put(0, 0)
                .put(1, 1)
                .zip(io.vavr.collection.List.of(5, 6, 7));
        assertThat(actual).isEqualTo(Stream.of(Tuple.of(Tuple.of(0, 0), 5), Tuple.of(Tuple.of(1, 1), 6)));
    }

    @Test
    public void shouldZipNonNilsIfThatIsSmaller() {
        final Seq<Tuple2<Tuple2<Integer, Integer>, Integer>> actual = emptyIntInt()
                .put(0, 0)
                .put(1, 1)
                .put(2, 2)
                .zip(io.vavr.collection.List.of(5, 6));
        assertThat(actual).isEqualTo(Stream.of(Tuple.of(Tuple.of(0, 0), 5), Tuple.of(Tuple.of(1, 1), 6)));
    }

    @Test
    public void shouldZipNonNilsOfSameSize() {
        final Seq<Tuple2<Tuple2<Integer, Integer>, Integer>> actual = emptyIntInt()
                .put(0, 0)
                .put(1, 1)
                .put(2, 2)
                .zip(io.vavr.collection.List.of(5, 6, 7));
        assertThat(actual).isEqualTo(
                Stream.of(Tuple.of(Tuple.of(0, 0), 5), Tuple.of(Tuple.of(1, 1), 6), Tuple.of(Tuple.of(2, 2), 7)));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowIfZipWithThatIsNull() {
        emptyMap().zip(null);
    }

    // -- zipWithIndex

    @Test
    public void shouldZipNilWithIndex() {
        assertThat(emptyMap().zipWithIndex()).isEqualTo(Stream.empty());
    }

    @Test
    public void shouldZipNonNilWithIndex() {
        final Seq<Tuple2<Tuple2<Integer, Integer>, Integer>> actual = emptyIntInt()
                .put(0, 0)
                .put(1, 1)
                .put(2, 2)
                .zipWithIndex();
        assertThat(actual).isEqualTo(
                Stream.of(Tuple.of(Tuple.of(0, 0), 0), Tuple.of(Tuple.of(1, 1), 1), Tuple.of(Tuple.of(2, 2), 2)));
    }

    // -- zipAll

    @Test
    public void shouldZipAllNils() {
        final Seq<Tuple2<Tuple2<Integer, Object>, Object>> actual = emptyInt().zipAll(empty(), null, null);
        assertThat(actual).isEqualTo(Stream.empty());
    }

    @Test
    public void shouldZipAllEmptyAndNonNil() {
        final Seq<Tuple2<Tuple2<Integer, Object>, Object>> actual = emptyInt().zipAll(io.vavr.collection.List.of(1), null, null);
        assertThat(actual).isEqualTo(Stream.of(Tuple.of(null, 1)));
    }

    @Test
    public void shouldZipAllNonEmptyAndNil() {
        final Seq<Tuple2<Tuple2<Integer, Object>, Object>> actual = emptyInt().put(0, 1).zipAll(empty(), null, null);
        assertThat(actual).isEqualTo(Stream.of(Tuple.of(Tuple.of(0, 1), null)));
    }

    @Test
    public void shouldZipAllNonNilsIfThisIsSmaller() {
        final Seq<Tuple2<Tuple2<Integer, Integer>, String>> actual = emptyIntInt()
                .put(1, 1)
                .put(2, 2)
                .zipAll(of("a", "b", "c"), Tuple.of(9, 10), "z");
        final Seq<Tuple2<Tuple2<Object, Object>, String>> expected = Stream.of(Tuple.of(Tuple.of(1, 1), "a"),
                Tuple.of(Tuple.of(2, 2), "b"), Tuple.of(Tuple.of(9, 10), "c"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonNilsIfThisIsMoreSmaller() {
        final Seq<Tuple2<Tuple2<Integer, Integer>, String>> actual = emptyIntInt()
                .put(1, 1)
                .put(2, 2)
                .zipAll(of("a", "b", "c", "d"), Tuple.of(9, 10), "z");
        final Seq<Tuple2<Tuple2<Object, Object>, String>> expected = Stream.of(Tuple.of(Tuple.of(1, 1), "a"),
                Tuple.of(Tuple.of(2, 2), "b"), Tuple.of(Tuple.of(9, 10), "c"), Tuple.of(Tuple.of(9, 10), "d"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonNilsIfThatIsSmaller() {
        final Seq<Tuple2<Tuple2<Integer, Integer>, String>> actual = emptyIntInt()
                .put(1, 1)
                .put(2, 2)
                .put(3, 3)
                .zipAll(this.of("a", "b"), Tuple.of(9, 10), "z");
        final Seq<Tuple2<Tuple2<Object, Object>, String>> expected = Stream.of(Tuple.of(Tuple.of(1, 1), "a"),
                Tuple.of(Tuple.of(2, 2), "b"), Tuple.of(Tuple.of(3, 3), "z"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonNilsIfThatIsMoreSmaller() {
        final Seq<Tuple2<Tuple2<Integer, Integer>, String>> actual = emptyIntInt()
                .put(1, 1)
                .put(2, 2)
                .put(3, 3)
                .put(4, 4)
                .zipAll(of("a", "b"), Tuple.of(9, 10), "z");
        final Seq<Tuple2<Tuple2<Object, Object>, String>> expected = Stream.of(Tuple.of(Tuple.of(1, 1), "a"),
                Tuple.of(Tuple.of(2, 2), "b"), Tuple.of(Tuple.of(3, 3), "z"), Tuple.of(Tuple.of(4, 4), "z"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonNilsOfSameSize() {
        final Seq<Tuple2<Tuple2<Integer, Integer>, String>> actual = emptyIntInt()
                .put(1, 1)
                .put(2, 2)
                .put(3, 3)
                .zipAll(of("a", "b", "c"), Tuple.of(9, 10), "z");
        final Seq<Tuple2<Tuple2<Object, Object>, String>> expected = Stream.of(Tuple.of(Tuple.of(1, 1), "a"),
                Tuple.of(Tuple.of(2, 2), "b"), Tuple.of(Tuple.of(3, 3), "c"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowIfZipAllWithThatIsNull() {
        emptyMap().zipAll(null, null, null);
    }

    // -- special cases

    @Override
    public void shouldComputeDistinctOfNonEmptyTraversable() {
        final Map<Integer, Object> testee = emptyInt().put(1, 1).put(2, 2).put(3, 3);
        assertThat(testee.distinct()).isEqualTo(testee);
    }

    @Override
    public void shouldReturnSomeTailWhenCallingTailOptionOnNonNil() {
        assertThat(of(1, 2, 3).tailOption().get()).isEqualTo(Option.some(of(2, 3)).get());
    }

    @Override
    public void shouldPreserveSingletonInstanceOnDeserialization() {
        final Map<?, ?> obj = deserialize(serialize(emptyMap()));
        final boolean actual = obj == emptyMap();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldSerializeDeserializeNonEmptyMap() {
        final Object expected = of('a', 'b', 'c');
        final Object actual = deserialize(serialize(expected));
        assertThat(actual).isEqualTo(expected);
    }

    @Override
    public void shouldFoldRightNonNil() {
        final String actual = of('a', 'b', 'c').foldRight("", (x, xs) -> x + xs);
        final io.vavr.collection.List<String> expected = io.vavr.collection.List.of('a', 'b', 'c').permutations().map(io.vavr.collection.List::mkString);
        assertThat(actual).isIn(expected);
    }

    // -- forEach

    @Test
    public void forEachByKeyValue() {
        final Map<Integer, Integer> map = mapOf(1, 2).put(3, 4);
        final int[] result = { 0 };
        map.forEach((k, v) -> {
            result[0] += k + v;
        });
        assertThat(result[0]).isEqualTo(10);
    }

    @Test
    public void forEachByTuple() {
        final Map<Integer, Integer> map = mapOf(1, 2).put(3, 4);
        final int[] result = { 0 };
        map.forEach(t -> {
            result[0] += t._1 + t._2;
        });
        assertThat(result[0]).isEqualTo(10);
    }

    // -- put with merge function

    @Test
    public void putWithWasntPresent() {
        final Map<Integer, Integer> map = mapOf(1, 2)
                .put(2, 3, (x, y) -> x + y);
        assertThat(map).isEqualTo(emptyIntInt().put(1, 2).put(2, 3));
    }

    @Test
    public void putWithWasPresent() {
        final Map<Integer, Integer> map = mapOf(1, 2)
                .put(1, 3, (x, y) -> x + y);
        assertThat(map).isEqualTo(emptyIntInt().put(1, 5));
    }

    @Test
    public void putWithTupleWasntPresent() {
        final Map<Integer, Integer> map = mapOf(1, 2)
                .put(Tuple.of(2, 3), (x, y) -> x + y);
        assertThat(map).isEqualTo(emptyIntInt().put(1, 2).put(2, 3));
    }

    @Test
    public void putWithTupleWasPresent() {
        final Map<Integer, Integer> map = mapOf(1, 2)
                .put(Tuple.of(1, 3), (x, y) -> x + y);
        assertThat(map).isEqualTo(emptyIntInt().put(1, 5));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldTabulateTheSeq() {
        final Function<Number, Tuple2<Long, Float>> f = i -> new Tuple2<>(i.longValue(), i.floatValue());
        final Map<Long, Float> map = mapTabulate(3, f);
        assertThat(map).isEqualTo(mapOfTuples(new Tuple2<>(0l, 0f), new Tuple2<>(1l, 1f), new Tuple2<>(2l, 2f)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldTabulateTheSeqCallingTheFunctionInTheRightOrder() {
        final LinkedList<Integer> ints = new LinkedList<>(asList(0, 0, 1, 1, 2, 2));
        final Function<Integer, Tuple2<Long, Float>> f = i -> new Tuple2<>(ints.remove().longValue(), ints.remove().floatValue());
        final Map<Long, Float> map = mapTabulate(3, f);
        assertThat(map).isEqualTo(mapOfTuples(new Tuple2<>(0l, 0f), new Tuple2<>(1l, 1f), new Tuple2<>(2l, 2f)));
    }

    @Test
    public void shouldTabulateTheSeqWith0Elements() {
        assertThat(mapTabulate(0, i -> new Tuple2<>(i, i))).isEqualTo(empty());
    }

    @Test
    public void shouldTabulateTheSeqWith0ElementsWhenNIsNegative() {
        assertThat(mapTabulate(-1, i -> new Tuple2<>(i, i))).isEqualTo(empty());
    }

    // -- fill(int, Supplier)

    @SuppressWarnings("unchecked")
    @Test
    public void shouldFillTheSeqCallingTheSupplierInTheRightOrder() {
        final LinkedList<Integer> ints = new LinkedList<>(asList(0, 0, 1, 1, 2, 2));
        final Supplier<Tuple2<Long, Float>> s = () -> new Tuple2<>(ints.remove().longValue(), ints.remove().floatValue());
        final Map<Long, Float> actual = mapFill(3, s);
        assertThat(actual).isEqualTo(mapOfTuples(new Tuple2<>(0l, 0f), new Tuple2<>(1l, 1f), new Tuple2<>(2l, 2f)));
    }

    @Test
    public void shouldFillTheSeqWith0Elements() {
        assertThat(mapFill(0, () -> new Tuple2<>(1, 1))).isEqualTo(empty());
    }

    @Test
    public void shouldReturnSingleMapAfterFillWithConstantKeys() {
        AtomicInteger value = new AtomicInteger(83);
        assertThat(mapFill(17, () -> Tuple.of(7, value.getAndIncrement())))
                .hasSize(1)
                .isEqualTo(mapOf(7, value.decrementAndGet()));
    }

    @Test
    public void shouldFillTheSeqWith0ElementsWhenNIsNegative() {
        assertThat(mapFill(-1, () -> new Tuple2<>(1, 1))).isEqualTo(empty());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void mapOfTuplesShouldReturnTheSingletonEmpty() {
        if (!emptyMapShouldBeSingleton()) { return; }
        assertThat(mapOfTuples()).isSameAs(emptyMap());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void mapOfEntriesShouldReturnTheSingletonEmpty() {
        if (!emptyMapShouldBeSingleton()) { return; }
        assertThat(mapOfEntries()).isSameAs(emptyMap());
    }

    // -- filter

    @Test
    public void shouldBiFilterWork() throws Exception {
        final Map<Integer, String> src = mapTabulate(20, n -> Tuple.of(n, Integer.toHexString(n)));
        final Pattern isDigits = Pattern.compile("^\\d+$");
        final Map<Integer, String> dst = src.filter((k, v) -> k % 2 == 0 && isDigits.matcher(v).matches());
        assertThat(dst).isEqualTo(emptyIntString().put(0, "0").put(2, "2").put(4, "4").put(6, "6").put(8, "8").put(16, "10").put(18, "12"));
    }

    @Test
    public void shouldKeyFilterWork() throws Exception {
        final Map<Integer, String> src = mapTabulate(20, n -> Tuple.of(n, Integer.toHexString(n)));
        final Map<Integer, String> dst = src.filterKeys(k -> k % 2 == 0);
        assertThat(dst).isEqualTo(emptyIntString().put(0, "0").put(2, "2").put(4, "4").put(6, "6").put(8, "8").put(10, "a").put(12, "c").put(14, "e").put(16, "10").put(18, "12"));
    }

    @Test
    public void shouldValueFilterWork() throws Exception {
        final Map<Integer, String> src = mapTabulate(10, n -> Tuple.of(n, Integer.toHexString(n)));
        final Pattern isDigits = Pattern.compile("^\\d+$");
        final Map<Integer, String> dst = src.filterValues(v -> isDigits.matcher(v).matches());
        assertThat(dst).isEqualTo(emptyIntString().put(0, "0").put(1, "1").put(2, "2").put(3, "3").put(4, "4").put(5, "5").put(6, "6").put(7, "7").put(8, "8").put(9, "9"));
    }

    // -- filterNot

    @Test
    public void biFilterNotShouldWork() {
        final Map<Integer, String> src = mapTabulate(20, n -> Tuple.of(n, Integer.toHexString(n)));
        final Pattern isDigits = Pattern.compile("^\\d+$");
        final Map<Integer, String> dst = src.filterNot((k, v) -> k % 2 == 0 && isDigits.matcher(v).matches());
        assertThat(dst).isEqualTo(emptyIntString().put(1, "1").put(3, "3").put(5, "5").put(7, "7").put(9, "9").put(10, "a").put(11, "b").put(12, "c").put(13, "d").put(14, "e").put(15, "f").put(17, "11").put(19, "13"));
    }

    @Test
    public void keyFilterNotShouldWork() {
        final Map<Integer, String> src = mapTabulate(20, n -> Tuple.of(n, Integer.toHexString(n)));
        final Map<Integer, String> dst = src.filterNotKeys(k -> k % 2 == 0);
        assertThat(dst).isEqualTo(emptyIntString().put(1, "1").put(3, "3").put(5, "5").put(7, "7").put(9, "9").put(11, "b").put(13, "d").put(15, "f").put(17, "11").put(19, "13"));
    }

    @Test
    public void valueFilterNotShouldWork() {
        final Map<Integer, String> src = mapTabulate(15, n -> Tuple.of(n, Integer.toHexString(n)));
        final Pattern isDigits = Pattern.compile("^\\d+$");
        final Map<Integer, String> dst = src.filterNotValues(v -> isDigits.matcher(v).matches());
        assertThat(dst).isEqualTo(emptyIntString().put(10, "a").put(11, "b").put(12, "c").put(13, "d").put(14, "e"));
    }

    // -- computeIfAbsent

    @Test
    public void shouldComputeIfAbsent() {
        final Map<Integer, String> map = emptyIntString().put(1, "v");
        assertThat(map.computeIfAbsent(1, k -> "b")).isEqualTo(Tuple.of("v", map));
        assertThat(map.computeIfAbsent(2, k -> "n")).isEqualTo(Tuple.of("n", emptyIntString().put(1, "v").put(2, "n")));
    }

    // -- computeIfAbsent

    @Test
    public void shouldComputeIfPresent() {
        final Map<Integer, String> map = emptyIntString().put(1, "v");
        assertThat(map.computeIfPresent(1, (k, v) -> "b")).isEqualTo(Tuple.of(Option.of("b"), emptyIntString().put(1, "b")));
        assertThat(map.computeIfPresent(2, (k, v) -> "n")).isEqualTo(Tuple.of(Option.none(), map));
    }

    // -- get with nulls

    @Test
    public void shouldReturnOptionOfNullWhenAccessingKeysSetToNull() {
        final Map<String, String> map = mapOf("1", null);
        assertThat(map.get("1")).isEqualTo(Option.some(null));
    }

    @Test
    public void shouldReturnOptionOfKeyWhenAccessingPresentKeysInAMapWithNulls() {
        final Map<String, String> map = mapOf("1", "a").put("2", null);
        assertThat(map.get("1")).isEqualTo(Option.of("a"));
    }

    @Test
    public void shouldReturnNoneWhenAccessingAbsentKeysInAMapWithNulls() {
        final Map<String, String> map = mapOf("1", "a").put("2", null);
        assertThat(map.get("3")).isEqualTo(Option.none());
    }

    @Test
    public void shouldReturnSameInstanceIfReplacingCurrentValueWithNonExistingKey() {
        final Map<Integer, String> map = mapOf(1, "a", 2, "b");
        final Map<Integer, String> actual = map.replaceValue(3, "?");
        assertThat(actual).isSameAs(map);
    }

    @Test
    public void shouldReplaceCurrentValueForExistingKey() {
        final Map<Integer, String> map = mapOf(1, "a", 2, "b");
        final Map<Integer, String> actual = map.replaceValue(2, "c");
        final Map<Integer, String> expected = mapOf(1, "a", 2, "c");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldReplaceCurrentValueForExistingKeyAndEqualOldValue() {
        final Map<Integer, String> map = mapOf(1, "a", 2, "b");
        final Map<Integer, String> actual = map.replace(2, "b", "c");
        final Map<Integer, String> expected = mapOf(1, "a", 2, "c");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldReturnSameInstanceForExistingKeyAndNonEqualOldValue() {
        final Map<Integer, String> map = mapOf(1, "a", 2, "b");
        final Map<Integer, String> actual = map.replace(2, "d", "c");
        assertThat(actual).isSameAs(map);
    }

    @Test
    public void shouldReturnSameInstanceIfReplacingCurrentValueWithOldValueWithNonExistingKey() {
        final Map<Integer, String> map = mapOf(1, "a", 2, "b");
        final Map<Integer, String> actual = map.replace(3, "?", "!");
        assertThat(actual).isSameAs(map);
    }

    @Test
    public void shouldReplaceAllValuesWithFunctionResult() {
        final Map<Integer, String> map = mapOf(1, "a", 2, "b");
        final Map<Integer, String> actual = map.replaceAll((integer, s) -> s + integer);
        final Map<Integer, String> expected = mapOf(1, "a1", 2, "b2");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldGetValueOfNullKeyWhenPutFirstHavingTwoEntries() {
        final Map<Integer, String> map = mapOfNullKey(null, "a", 2, "b");
        assertThat(map.get(null)).isEqualTo(Some("a"));
    }

    @Test
    public void shouldGetValueOfNullKeyWhenPutLastHavingTwoEntries() {
        final Map<Integer, String> map = mapOfNullKey(1, "a", null, "b");
        assertThat(map.get(null)).isEqualTo(Some("b"));
    }

    @Test
    public void shouldGetAPresentNullValueWhenPutFirstHavingTwoEntries() {
        final Map<Integer, String> map = mapOf(1, null, 2, "b");
        assertThat(map.get(1)).isEqualTo(Some(null));
    }

    @Test
    public void shouldGetAPresentNullValueWhenPutLastHavingTwoEntries() {
        final Map<Integer, String> map = mapOf(1, "a", 2, null);
        assertThat(map.get(2)).isEqualTo(Some(null));
    }

    // -- getOrElse

    @Test
    public void shouldReturnDefaultValue() {
        final Map<String, String> map = mapOf("1", "a").put("2", "b");
        assertThat(map.getOrElse("3", "3")).isEqualTo("3");
    }

    // -- partition

    @Test
    public void shouldPartitionInOneIteration() {
        final AtomicInteger count = new AtomicInteger(0);
        final Map<String, Integer> map = mapOf("1", 1, "2", 2, "3", 3);
        final Tuple2<? extends Map<String, Integer>, ? extends Map<String, Integer>> results = map.partition(entry -> {
            count.incrementAndGet();
            return true;
        });
        assertThat(results._1).isEqualTo(mapOf("1", 1, "2", 2, "3", 3));
        assertThat(results._2).isEmpty();
        assertThat(count.get()).isEqualTo(3);
    }

    // -- spliterator

    @Test
    public void shouldHaveSizedSpliterator() {
        assertThat(of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.SIZED | Spliterator.SUBSIZED)).isTrue();
    }

    @Test
    public void shouldHaveDistinctSpliterator() {
        assertThat(of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.DISTINCT)).isTrue();
    }

    @Test
    public void shouldReturnSizeWhenSpliterator() {
        assertThat(of(1, 2, 3).spliterator().getExactSizeIfKnown()).isEqualTo(3);
    }

    @Override
    @Test
    @SuppressWarnings("unchecked")
    public void shouldPartitionIntsInOddAndEvenHavingOddAndEvenNumbers() {
        assertThat(of(1, 2, 3, 4).partition(i -> i % 2 != 0))
                .isEqualTo(Tuple.of(mapOfTuples(Tuple.of(0, 1), Tuple.of(2, 3)),
                        mapOfTuples(Tuple.of(1, 2), Tuple.of(3, 4))));
    }

    @Override
    @Test
    @SuppressWarnings("unchecked")
    public void shouldSpanNonNil() {
        assertThat(of(0, 1, 2, 3).span(i -> i < 2))
                .isEqualTo(Tuple.of(mapOfTuples(Tuple.of(0, 0), Tuple.of(1, 1)),
                mapOfTuples(Tuple.of(2, 2), Tuple.of(3, 3))));
    }

    @Override
    @Test
    @SuppressWarnings("unchecked")
    public void shouldSpanAndNotTruncate() {
        assertThat(of(1, 1, 2, 2, 3, 3).span(x -> x % 2 == 1))
                .isEqualTo(Tuple.of(mapOfTuples(Tuple.of(0,1), Tuple.of(1, 1)),
                        mapOfTuples(Tuple.of(2, 2), Tuple.of(3, 2),
                                Tuple.of(4, 3), Tuple.of(5, 3))));
        assertThat(of(1, 1, 2, 2, 4, 4).span(x -> x == 1))
                .isEqualTo(Tuple.of(mapOfTuples(Tuple.of(0,1), Tuple.of(1, 1)),
                        mapOfTuples(Tuple.of(2, 2), Tuple.of(3, 2),
                        Tuple.of(4, 4), Tuple.of(5, 4))));
    }

    @Override
    @Test
    public void shouldNonNilGroupByIdentity() {
        final Map<?, ?> actual = of('a', 'b', 'c').groupBy(Function.identity());
        final Map<?, ?> expected = io.vavr.collection.LinkedHashMap.empty().put('a', mapOf(0, 'a')).put('b', mapOf(1,'b'))
                .put('c', mapOf(2,'c'));
        assertThat(actual).isEqualTo(expected);
    }

}
