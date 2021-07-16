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
import io.vavr.control.Option;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * An immutable {@code HashMap} implementation based on a
 * <a href="https://en.wikipedia.org/wiki/Hash_array_mapped_trie">Hash array mapped trie (HAMT)</a>.
 */
public final class HashMap<K, V> implements Map<K, V>, Serializable {

    private static final long serialVersionUID = 1L;

    private static final HashMap<?, ?> EMPTY = new HashMap<>(HashArrayMappedTrie.empty());

    private final HashArrayMappedTrie<K, V> trie;

    private HashMap(HashArrayMappedTrie<K, V> trie) {
        this.trie = trie;
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link HashMap}.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @return A {@link HashMap} Collector.
     */
    public static <K, V> Collector<Tuple2<K, V>, ArrayList<Tuple2<K, V>>, HashMap<K, V>> collector() {
        return Collections.toListAndThen(HashMap::ofEntries);
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link HashMap}.
     *
     * @param keyMapper The key mapper
     * @param <K> The key type
     * @param <V> The value type
     * @param <T> Initial {@link java.util.stream.Stream} elements type
     * @return A {@link HashMap} Collector.
     */
    public static <K, V, T extends V> Collector<T, ArrayList<T>, HashMap<K, V>> collector(Function<? super T, ? extends K> keyMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        return HashMap.collector(keyMapper, v -> v);
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link HashMap}.
     *
     * @param keyMapper The key mapper
     * @param valueMapper The value mapper
     * @param <K> The key type
     * @param <V> The value type
     * @param <T> Initial {@link java.util.stream.Stream} elements type
     * @return A {@link HashMap} Collector.
     */
    public static <K, V, T> Collector<T, ArrayList<T>, HashMap<K, V>> collector(
            Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return Collections.toListAndThen(arr -> HashMap.ofEntries(Iterator.ofAll(arr)
                .map(t -> Tuple.of(keyMapper.apply(t), valueMapper.apply(t)))));
    }

    @SuppressWarnings("unchecked")
    public static <K, V> HashMap<K, V> empty() {
        return (HashMap<K, V>) EMPTY;
    }

    /**
     * Narrows a widened {@code HashMap<? extends K, ? extends V>} to {@code HashMap<K, V>}
     * by performing a type-safe cast. This is eligible because immutable/read-only
     * collections are covariant.
     *
     * @param hashMap A {@code HashMap}.
     * @param <K>     Key type
     * @param <V>     Value type
     * @return the given {@code hashMap} instance as narrowed type {@code HashMap<K, V>}.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> HashMap<K, V> narrow(HashMap<? extends K, ? extends V> hashMap) {
        return (HashMap<K, V>) hashMap;
    }

    /**
     * Returns a singleton {@code HashMap}, i.e. a {@code HashMap} of one element.
     *
     * @param entry A map entry.
     * @param <K>   The key type
     * @param <V>   The value type
     * @return A new Map containing the given entry
     */
    public static <K, V> HashMap<K, V> of(Tuple2<? extends K, ? extends V> entry) {
        return new HashMap<>(HashArrayMappedTrie.<K, V> empty().put(entry._1, entry._2));
    }

    /**
     * Returns a {@code HashMap}, from a source java.util.Map.
     *
     * @param map A map
     * @param <K> The key type
     * @param <V> The value type
     * @return A new Map containing the given map
     */
    public static <K, V> HashMap<K, V> ofAll(java.util.Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map is null");
        HashArrayMappedTrie<K, V> tree = HashArrayMappedTrie.empty();
        for (java.util.Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            tree = tree.put(entry.getKey(), entry.getValue());
        }
        return wrap(tree);
    }

    /**
     * Returns a {@code HashMap}, from entries mapped from stream.
     *
     * @param stream      the source stream
     * @param keyMapper   the key mapper
     * @param valueMapper the value mapper
     * @param <T>         The stream element type
     * @param <K>         The key type
     * @param <V>         The value type
     * @return A new Map
     */
    public static <T, K, V> HashMap<K, V> ofAll(java.util.stream.Stream<? extends T> stream,
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper) {
        return Maps.ofStream(empty(), stream, keyMapper, valueMapper);
    }

    /**
     * Returns a {@code HashMap}, from entries mapped from stream.
     *
     * @param stream      the source stream
     * @param entryMapper the entry mapper
     * @param <T>         The stream element type
     * @param <K>         The key type
     * @param <V>         The value type
     * @return A new Map
     */
    public static <T, K, V> HashMap<K, V> ofAll(java.util.stream.Stream<? extends T> stream,
            Function<? super T, Tuple2<? extends K, ? extends V>> entryMapper) {
        return Maps.ofStream(empty(), stream, entryMapper);
    }

    /**
     * Returns a singleton {@code HashMap}, i.e. a {@code HashMap} of one element.
     *
     * @param key   A singleton map key.
     * @param value A singleton map value.
     * @param <K>   The key type
     * @param <V>   The value type
     * @return A new Map containing the given entry
     */
    public static <K, V> HashMap<K, V> of(K key, V value) {
        return new HashMap<>(HashArrayMappedTrie.<K, V> empty().put(key, value));
    }

    /**
     * Creates a HashMap of the given list of key-value pairs.
     *
     * @param k1  a key for the map
     * @param v1  the value for k1
     * @param k2  a key for the map
     * @param v2  the value for k2
     * @param <K> The key type
     * @param <V> The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2) {
        return of(k1, v1).put(k2, v2);
    }

    /**
     * Creates a HashMap of the given list of key-value pairs.
     *
     * @param k1  a key for the map
     * @param v1  the value for k1
     * @param k2  a key for the map
     * @param v2  the value for k2
     * @param k3  a key for the map
     * @param v3  the value for k3
     * @param <K> The key type
     * @param <V> The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return of(k1, v1, k2, v2).put(k3, v3);
    }

    /**
     * Creates a HashMap of the given list of key-value pairs.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @param k1  a key for the map
     * @param v1  the value for k1
     * @param k2  a key for the map
     * @param v2  the value for k2
     * @param k3  a key for the map
     * @param v3  the value for k3
     * @param k4  a key for the map
     * @param v4  the value for k4
     * @return A new Map containing the given entries
     */
    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return of(k1, v1, k2, v2, k3, v3).put(k4, v4);
    }

    /**
     * Creates a HashMap of the given list of key-value pairs.
     *
     * @param k1  a key for the map
     * @param v1  the value for k1
     * @param k2  a key for the map
     * @param v2  the value for k2
     * @param k3  a key for the map
     * @param v3  the value for k3
     * @param k4  a key for the map
     * @param v4  the value for k4
     * @param k5  a key for the map
     * @param v5  the value for k5
     * @param <K> The key type
     * @param <V> The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return of(k1, v1, k2, v2, k3, v3, k4, v4).put(k5, v5);
    }

    /**
     * Creates a HashMap of the given list of key-value pairs.
     *
     * @param k1  a key for the map
     * @param v1  the value for k1
     * @param k2  a key for the map
     * @param v2  the value for k2
     * @param k3  a key for the map
     * @param v3  the value for k3
     * @param k4  a key for the map
     * @param v4  the value for k4
     * @param k5  a key for the map
     * @param v5  the value for k5
     * @param k6  a key for the map
     * @param v6  the value for k6
     * @param <K> The key type
     * @param <V> The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5).put(k6, v6);
    }

    /**
     * Creates a HashMap of the given list of key-value pairs.
     *
     * @param k1  a key for the map
     * @param v1  the value for k1
     * @param k2  a key for the map
     * @param v2  the value for k2
     * @param k3  a key for the map
     * @param v3  the value for k3
     * @param k4  a key for the map
     * @param v4  the value for k4
     * @param k5  a key for the map
     * @param v5  the value for k5
     * @param k6  a key for the map
     * @param v6  the value for k6
     * @param k7  a key for the map
     * @param v7  the value for k7
     * @param <K> The key type
     * @param <V> The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6).put(k7, v7);
    }

    /**
     * Creates a HashMap of the given list of key-value pairs.
     *
     * @param k1  a key for the map
     * @param v1  the value for k1
     * @param k2  a key for the map
     * @param v2  the value for k2
     * @param k3  a key for the map
     * @param v3  the value for k3
     * @param k4  a key for the map
     * @param v4  the value for k4
     * @param k5  a key for the map
     * @param v5  the value for k5
     * @param k6  a key for the map
     * @param v6  the value for k6
     * @param k7  a key for the map
     * @param v7  the value for k7
     * @param k8  a key for the map
     * @param v8  the value for k8
     * @param <K> The key type
     * @param <V> The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7).put(k8, v8);
    }

    /**
     * Creates a HashMap of the given list of key-value pairs.
     *
     * @param k1  a key for the map
     * @param v1  the value for k1
     * @param k2  a key for the map
     * @param v2  the value for k2
     * @param k3  a key for the map
     * @param v3  the value for k3
     * @param k4  a key for the map
     * @param v4  the value for k4
     * @param k5  a key for the map
     * @param v5  the value for k5
     * @param k6  a key for the map
     * @param v6  the value for k6
     * @param k7  a key for the map
     * @param v7  the value for k7
     * @param k8  a key for the map
     * @param v8  the value for k8
     * @param k9  a key for the map
     * @param v9  the value for k9
     * @param <K> The key type
     * @param <V> The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8).put(k9, v9);
    }

    /**
     * Creates a HashMap of the given list of key-value pairs.
     *
     * @param k1  a key for the map
     * @param v1  the value for k1
     * @param k2  a key for the map
     * @param v2  the value for k2
     * @param k3  a key for the map
     * @param v3  the value for k3
     * @param k4  a key for the map
     * @param v4  the value for k4
     * @param k5  a key for the map
     * @param v5  the value for k5
     * @param k6  a key for the map
     * @param v6  the value for k6
     * @param k7  a key for the map
     * @param v7  the value for k7
     * @param k8  a key for the map
     * @param v8  the value for k8
     * @param k9  a key for the map
     * @param v9  the value for k9
     * @param k10 a key for the map
     * @param v10 the value for k10
     * @param <K> The key type
     * @param <V> The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> HashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9).put(k10, v10);
    }

    /**
     * Returns an HashMap containing {@code n} values of a given Function {@code f}
     * over a range of integer values from 0 to {@code n - 1}.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @param n   The number of elements in the HashMap
     * @param f   The Function computing element values
     * @return An HashMap consisting of elements {@code f(0),f(1), ..., f(n - 1)}
     * @throws NullPointerException if {@code f} is null
     */
    @SuppressWarnings("unchecked")
    public static <K, V> HashMap<K, V> tabulate(int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V>> f) {
        Objects.requireNonNull(f, "f is null");
        return ofEntries(Collections.tabulate(n, (Function<? super Integer, ? extends Tuple2<K, V>>) f));
    }

    /**
     * Returns a HashMap containing tuples returned by {@code n} calls to a given Supplier {@code s}.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @param n   The number of elements in the HashMap
     * @param s   The Supplier computing element values
     * @return An HashMap of size {@code n}, where each element contains the result supplied by {@code s}.
     * @throws NullPointerException if {@code s} is null
     */
    @SuppressWarnings("unchecked")
    public static <K, V> HashMap<K, V> fill(int n, Supplier<? extends Tuple2<? extends K, ? extends V>> s) {
        Objects.requireNonNull(s, "s is null");
        return ofEntries(Collections.fill(n, (Supplier<? extends Tuple2<K, V>>) s));
    }

    /**
     * Creates a HashMap of the given entries.
     *
     * @param entries Map entries
     * @param <K>     The key type
     * @param <V>     The value type
     * @return A new Map containing the given entries
     */
    @SafeVarargs
    public static <K, V> HashMap<K, V> ofEntries(java.util.Map.Entry<? extends K, ? extends V>... entries) {
        Objects.requireNonNull(entries, "entries is null");
        HashArrayMappedTrie<K, V> trie = HashArrayMappedTrie.empty();
        for (java.util.Map.Entry<? extends K, ? extends V> entry : entries) {
            trie = trie.put(entry.getKey(), entry.getValue());
        }
        return wrap(trie);
    }

    /**
     * Creates a HashMap of the given entries.
     *
     * @param entries Map entries
     * @param <K>     The key type
     * @param <V>     The value type
     * @return A new Map containing the given entries
     */
    @SafeVarargs
    public static <K, V> HashMap<K, V> ofEntries(Tuple2<? extends K, ? extends V>... entries) {
        Objects.requireNonNull(entries, "entries is null");
        HashArrayMappedTrie<K, V> trie = HashArrayMappedTrie.empty();
        for (Tuple2<? extends K, ? extends V> entry : entries) {
            trie = trie.put(entry._1, entry._2);
        }
        return wrap(trie);
    }

    /**
     * Creates a HashMap of the given entries.
     *
     * @param entries Map entries
     * @param <K>     The key type
     * @param <V>     The value type
     * @return A new Map containing the given entries
     */
    @SuppressWarnings("unchecked")
    public static <K, V> HashMap<K, V> ofEntries(Iterable<? extends Tuple2<? extends K, ? extends V>> entries) {
        Objects.requireNonNull(entries, "entries is null");
        if (entries instanceof HashMap) {
            return (HashMap<K, V>) entries;
        } else {
            HashArrayMappedTrie<K, V> trie = HashArrayMappedTrie.empty();
            for (Tuple2<? extends K, ? extends V> entry : entries) {
                trie = trie.put(entry._1, entry._2);
            }
            return trie.isEmpty() ? empty() : wrap(trie);
        }
    }

    @Override
    public <K2, V2> HashMap<K2, V2> bimap(Function<? super K, ? extends K2> keyMapper, Function<? super V, ? extends V2> valueMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        final Iterator<Tuple2<K2, V2>> entries = iterator().map(entry -> Tuple.of(keyMapper.apply(entry._1), valueMapper.apply(entry._2)));
        return HashMap.ofEntries(entries);
    }

    @Override
    public Tuple2<V, HashMap<K, V>> computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return Maps.computeIfAbsent(this, key, mappingFunction);
    }

    @Override
    public Tuple2<Option<V>, HashMap<K, V>> computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return Maps.computeIfPresent(this, key, remappingFunction);
    }

    @Override
    public boolean containsKey(K key) {
        return trie.containsKey(key);
    }

    @Override
    public HashMap<K, V> distinct() {
        return Maps.distinct(this);
    }

    @Override
    public HashMap<K, V> distinctBy(Comparator<? super Tuple2<K, V>> comparator) {
        return Maps.distinctBy(this, this::createFromEntries, comparator);
    }

    @Override
    public <U> HashMap<K, V> distinctBy(Function<? super Tuple2<K, V>, ? extends U> keyExtractor) {
        return Maps.distinctBy(this, this::createFromEntries, keyExtractor);
    }

    @Override
    public HashMap<K, V> drop(int n) {
        return Maps.drop(this, this::createFromEntries, HashMap::empty, n);
    }

    @Override
    public HashMap<K, V> dropRight(int n) {
        return Maps.dropRight(this, this::createFromEntries, HashMap::empty, n);
    }

    @Override
    public HashMap<K, V> dropUntil(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.dropUntil(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> dropWhile(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.dropWhile(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> filter(BiPredicate<? super K, ? super V> predicate) {
        return Maps.filter(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> filterNot(BiPredicate<? super K, ? super V> predicate) {
        return Maps.filterNot(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> filter(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.filter(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> filterNot(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.filterNot(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> filterKeys(Predicate<? super K> predicate) {
        return Maps.filterKeys(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> filterNotKeys(Predicate<? super K> predicate) {
        return Maps.filterNotKeys(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> filterValues(Predicate<? super V> predicate) {
        return Maps.filterValues(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> filterNotValues(Predicate<? super V> predicate) {
        return Maps.filterNotValues(this, this::createFromEntries, predicate);
    }

    @Override
    public <K2, V2> HashMap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return foldLeft(HashMap.<K2, V2> empty(), (acc, entry) -> {
            for (Tuple2<? extends K2, ? extends V2> mappedEntry : mapper.apply(entry._1, entry._2)) {
                acc = acc.put(mappedEntry);
            }
            return acc;
        });
    }

    @Override
    public Option<V> get(K key) {
        return trie.get(key);
    }

    @Override
    public V getOrElse(K key, V defaultValue) {
        return trie.getOrElse(key, defaultValue);
    }

    @Override
    public <C> Map<C, HashMap<K, V>> groupBy(Function<? super Tuple2<K, V>, ? extends C> classifier) {
        return Maps.groupBy(this, this::createFromEntries, classifier);
    }

    @Override
    public Iterator<HashMap<K, V>> grouped(int size) {
        return Maps.grouped(this, this::createFromEntries, size);
    }

    @Override
    public Tuple2<K, V> head() {
        if (isEmpty()) {
            throw new NoSuchElementException("head of empty HashMap");
        } else {
            return iterator().next();
        }
    }

    @Override
    public HashMap<K, V> init() {
        if (trie.isEmpty()) {
            throw new UnsupportedOperationException("init of empty HashMap");
        } else {
            return remove(last()._1);
        }
    }

    @Override
    public Option<HashMap<K, V>> initOption() {
        return Maps.initOption(this);
    }

    /**
     * A {@code HashMap} is computed synchronously.
     *
     * @return false
     */
    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return trie.isEmpty();
    }

    /**
     * A {@code HashMap} is computed eagerly.
     *
     * @return false
     */
    @Override
    public boolean isLazy() {
        return false;
    }

    @Override
    public Iterator<Tuple2<K, V>> iterator() {
        return trie.iterator();
    }

    @Override
    public Set<K> keySet() {
        return HashSet.ofAll(iterator().map(Tuple2::_1));
    }

    @Override
    public Iterator<K> keysIterator() {
        return trie.keysIterator();
    }

    @Override
    public Tuple2<K, V> last() {
        return Collections.last(this);
    }

    @Override
    public <K2, V2> HashMap<K2, V2> map(BiFunction<? super K, ? super V, Tuple2<K2, V2>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return foldLeft(HashMap.empty(), (acc, entry) -> acc.put(entry.map(mapper)));
    }

    @Override
    public <K2> HashMap<K2, V> mapKeys(Function<? super K, ? extends K2> keyMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        return map((k, v) -> Tuple.of(keyMapper.apply(k), v));
    }

    @Override
    public <K2> HashMap<K2, V> mapKeys(Function<? super K, ? extends K2> keyMapper, BiFunction<? super V, ? super V, ? extends V> valueMerge) {
        return Collections.mapKeys(this, HashMap.empty(), keyMapper, valueMerge);
    }

    @Override
    public <V2> HashMap<K, V2> mapValues(Function<? super V, ? extends V2> valueMapper) {
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return map((k, v) -> Tuple.of(k, valueMapper.apply(v)));
    }

    @Override
    public HashMap<K, V> merge(Map<? extends K, ? extends V> that) {
        return Maps.merge(this, this::createFromEntries, that);
    }

    @Override
    public <U extends V> HashMap<K, V> merge(Map<? extends K, U> that,
            BiFunction<? super V, ? super U, ? extends V> collisionResolution) {
        return Maps.merge(this, this::createFromEntries, that, collisionResolution);
    }

    @Override
    public HashMap<K, V> orElse(Iterable<? extends Tuple2<K, V>> other) {
        return isEmpty() ? ofEntries(other) : this;
    }

    @Override
    public HashMap<K, V> orElse(Supplier<? extends Iterable<? extends Tuple2<K, V>>> supplier) {
        return isEmpty() ? ofEntries(supplier.get()) : this;
    }

    @Override
    public Tuple2<HashMap<K, V>, HashMap<K, V>> partition(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.partition(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> peek(Consumer<? super Tuple2<K, V>> action) {
        return Maps.peek(this, action);
    }

    @Override
    public HashMap<K, V> tapEach(Consumer<? super Tuple2<K, V>> action) {
        return Collections.tapEach(this, action);
    }

    @Override
    public <U extends V> HashMap<K, V> put(K key, U value, BiFunction<? super V, ? super U, ? extends V> merge) {
        return Maps.put(this, key, value, merge);
    }

    @Override
    public HashMap<K, V> put(K key, V value) {
        return new HashMap<>(trie.put(key, value));
    }

    @Override
    public HashMap<K, V> put(Tuple2<? extends K, ? extends V> entry) {
        return Maps.put(this, entry);
    }

    @Override
    public <U extends V> HashMap<K, V> put(Tuple2<? extends K, U> entry,
            BiFunction<? super V, ? super U, ? extends V> merge) {
        return Maps.put(this, entry, merge);
    }

    @Override
    public HashMap<K, V> remove(K key) {
        final HashArrayMappedTrie<K, V> result = trie.remove(key);
        return result.size() == trie.size() ? this : wrap(result);
    }

    @Override
    public HashMap<K, V> removeAll(Iterable<? extends K> keys) {
        Objects.requireNonNull(keys, "keys is null");
        HashArrayMappedTrie<K, V> result = trie;
        for (K key : keys) {
            result = result.remove(key);
        }

        if (result.isEmpty()) {
            return empty();
        } else if (result.size() == trie.size()) {
            return this;
        } else {
            return wrap(result);
        }
    }

    @Override
    public HashMap<K, V> replace(Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        return Maps.replace(this, currentElement, newElement);
    }

    @Override
    public HashMap<K, V> replaceAll(Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        return Maps.replaceAll(this, currentElement, newElement);
    }

    @Override
    public HashMap<K, V> replaceValue(K key, V value) {
        return Maps.replaceValue(this, key, value);
    }

    @Override
    public HashMap<K, V> replace(K key, V oldValue, V newValue) {
        return Maps.replace(this, key, oldValue, newValue);
    }

    @Override
    public HashMap<K, V> replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        return Maps.replaceAll(this, function);
    }

    @Override
    public HashMap<K, V> retainAll(Iterable<? extends Tuple2<K, V>> elements) {
        Objects.requireNonNull(elements, "elements is null");
        HashArrayMappedTrie<K, V> tree = HashArrayMappedTrie.empty();
        for (Tuple2<K, V> entry : elements) {
            if (contains(entry)) {
                tree = tree.put(entry._1, entry._2);
            }
        }
        return wrap(tree);
    }

    @Override
    public HashMap<K, V> scan(
            Tuple2<K, V> zero,
            BiFunction<? super Tuple2<K, V>, ? super Tuple2<K, V>, ? extends Tuple2<K, V>> operation) {
        return Maps.scan(this, zero, operation, this::createFromEntries);
    }

    @Override
    public int size() {
        return trie.size();
    }

    @Override
    public Iterator<HashMap<K, V>> slideBy(Function<? super Tuple2<K, V>, ?> classifier) {
        return Maps.slideBy(this, this::createFromEntries, classifier);
    }

    @Override
    public Iterator<HashMap<K, V>> sliding(int size) {
        return Maps.sliding(this, this::createFromEntries, size);
    }

    @Override
    public Iterator<HashMap<K, V>> sliding(int size, int step) {
        return Maps.sliding(this, this::createFromEntries, size, step);
    }

    @Override
    public Tuple2<HashMap<K, V>, HashMap<K, V>> span(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.span(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> tail() {
        if (trie.isEmpty()) {
            throw new UnsupportedOperationException("tail of empty HashMap");
        } else {
            return remove(head()._1);
        }
    }

    @Override
    public Option<HashMap<K, V>> tailOption() {
        return Maps.tailOption(this);
    }

    @Override
    public HashMap<K, V> take(int n) {
        return Maps.take(this, this::createFromEntries, n);
    }

    @Override
    public HashMap<K, V> takeRight(int n) {
        return Maps.takeRight(this, this::createFromEntries, n);
    }

    @Override
    public HashMap<K, V> takeUntil(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.takeUntil(this, this::createFromEntries, predicate);
    }

    @Override
    public HashMap<K, V> takeWhile(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.takeWhile(this, this::createFromEntries, predicate);
    }

    @Override
    public java.util.HashMap<K, V> toJavaMap() {
        return toJavaMap(java.util.HashMap::new, t -> t);
    }

    @Override
    public Stream<V> values() {
        return trie.valuesIterator().toStream();
    }

    @Override
    public Iterator<V> valuesIterator() {
        return trie.valuesIterator();
    }

    @Override
    public boolean equals(Object o) {
        return Collections.equals(this, o);
    }

    @Override
    public int hashCode() {
        return Collections.hashUnordered(this);
    }

    private Object readResolve() {
        return isEmpty() ? EMPTY : this;
    }

    @Override
    public String stringPrefix() {
        return "HashMap";
    }

    @Override
    public String toString() {
        return mkString(stringPrefix() + "(", ", ", ")");
    }

    private static <K, V> HashMap<K, V> wrap(HashArrayMappedTrie<K, V> trie) {
        return trie.isEmpty() ? empty() : new HashMap<>(trie);
    }

    // We need this method to narrow the argument of `ofEntries`.
    // If this method is static with type args <K, V>, the jdk fails to infer types at the call site.
    private HashMap<K, V> createFromEntries(Iterable<Tuple2<K, V>> tuples) {
        return HashMap.ofEntries(tuples);
    }
}
