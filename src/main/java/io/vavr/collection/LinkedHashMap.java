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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * An immutable {@code LinkedHashMap} implementation that has predictable (insertion-order) iteration.
 */
public final class LinkedHashMap<K, V> implements Map<K, V>, Serializable {

    private static final long serialVersionUID = 1L;

    private static final LinkedHashMap<?, ?> EMPTY = new LinkedHashMap<>(Queue.empty(), HashMap.empty());

    private final Queue<Tuple2<K, V>> list;
    private final HashMap<K, V> map;

    private LinkedHashMap(Queue<Tuple2<K, V>> list, HashMap<K, V> map) {
        this.list = list;
        this.map = map;
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link LinkedHashMap}.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @return A {@link LinkedHashMap} Collector.
     */
    public static <K, V> Collector<Tuple2<K, V>, ArrayList<Tuple2<K, V>>, LinkedHashMap<K, V>> collector() {
        return Collections.toListAndThen(LinkedHashMap::ofEntries);
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link LinkedHashMap}.
     *
     * @param keyMapper The key mapper
     * @param <K> The key type
     * @param <V> The value type
     * @param <T> Initial {@link java.util.stream.Stream} elements type
     * @return A {@link LinkedHashMap} Collector.
     */
    public static <K, V, T extends V> Collector<T, ArrayList<T>, LinkedHashMap<K, V>> collector(Function<? super T, ? extends K> keyMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        return LinkedHashMap.collector(keyMapper, v -> v);
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link LinkedHashMap}.
     *
     * @param keyMapper The key mapper
     * @param valueMapper The value mapper
     * @param <K> The key type
     * @param <V> The value type
     * @param <T> Initial {@link java.util.stream.Stream} elements type
     * @return A {@link LinkedHashMap} Collector.
     */
    public static <K, V, T> Collector<T, ArrayList<T>, LinkedHashMap<K, V>> collector(
            Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return Collections.toListAndThen(arr -> LinkedHashMap.ofEntries(Iterator.ofAll(arr)
          .map(t -> Tuple.of(keyMapper.apply(t), valueMapper.apply(t)))));
    }

    @SuppressWarnings("unchecked")
    public static <K, V> LinkedHashMap<K, V> empty() {
        return (LinkedHashMap<K, V>) EMPTY;
    }

    /**
     * Narrows a widened {@code LinkedHashMap<? extends K, ? extends V>} to {@code LinkedHashMap<K, V>}
     * by performing a type-safe cast. This is eligible because immutable/read-only
     * collections are covariant.
     *
     * @param linkedHashMap A {@code LinkedHashMap}.
     * @param <K>           Key type
     * @param <V>           Value type
     * @return the given {@code linkedHashMap} instance as narrowed type {@code LinkedHashMap<K, V>}.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> LinkedHashMap<K, V> narrow(LinkedHashMap<? extends K, ? extends V> linkedHashMap) {
        return (LinkedHashMap<K, V>) linkedHashMap;
    }

    /**
     * Returns a singleton {@code LinkedHashMap}, i.e. a {@code LinkedHashMap} of one element.
     *
     * @param entry A map entry.
     * @param <K>   The key type
     * @param <V>   The value type
     * @return A new Map containing the given entry
     */
    @SuppressWarnings("unchecked")
    public static <K, V> LinkedHashMap<K, V> of(Tuple2<? extends K, ? extends V> entry) {
        final HashMap<K, V> map = HashMap.of(entry);
        final Queue<Tuple2<K, V>> list = Queue.of((Tuple2<K, V>) entry);
        return wrap(list, map);
    }

    /**
     * Returns a {@code LinkedHashMap}, from a source java.util.Map.
     *
     * @param map A map
     * @param <K> The key type
     * @param <V> The value type
     * @return A new Map containing the given map
     */
    public static <K, V> LinkedHashMap<K, V> ofAll(java.util.Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map is null");
        LinkedHashMap<K, V> result = LinkedHashMap.empty();
        for (java.util.Map.Entry<? extends K, ? extends V> entry : map.entrySet()) {
            result = result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

    /**
     * Returns a {@code LinkedHashMap}, from entries mapped from stream.
     *
     * @param stream      the source stream
     * @param entryMapper the entry mapper
     * @param <T>         The stream element type
     * @param <K>         The key type
     * @param <V>         The value type
     * @return A new Map
     */
    public static <T, K, V> LinkedHashMap<K, V> ofAll(java.util.stream.Stream<? extends T> stream,
            Function<? super T, Tuple2<? extends K, ? extends V>> entryMapper) {
        return Maps.ofStream(empty(), stream, entryMapper);
    }

    /**
     * Returns a {@code LinkedHashMap}, from entries mapped from stream.
     *
     * @param stream      the source stream
     * @param keyMapper   the key mapper
     * @param valueMapper the value mapper
     * @param <T>         The stream element type
     * @param <K>         The key type
     * @param <V>         The value type
     * @return A new Map
     */
    public static <T, K, V> LinkedHashMap<K, V> ofAll(java.util.stream.Stream<? extends T> stream,
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper) {
        return Maps.ofStream(empty(), stream, keyMapper, valueMapper);
    }

    /**
     * Returns a singleton {@code LinkedHashMap}, i.e. a {@code LinkedHashMap} of one element.
     *
     * @param key   A singleton map key.
     * @param value A singleton map value.
     * @param <K>   The key type
     * @param <V>   The value type
     * @return A new Map containing the given entry
     */
    public static <K, V> LinkedHashMap<K, V> of(K key, V value) {
        final HashMap<K, V> map = HashMap.of(key, value);
        final Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(key, value));
        return wrap(list, map);
    }

    /**
     * Creates a LinkedHashMap of the given list of key-value pairs.
     *
     * @param k1  a key for the map
     * @param v1  the value for k1
     * @param k2  a key for the map
     * @param v2  the value for k2
     * @param <K> The key type
     * @param <V> The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2) {
        final HashMap<K, V> map = HashMap.of(k1, v1, k2, v2);
        final Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2));
        return wrapNonUnique(list, map);
    }

    /**
     * Creates a LinkedHashMap of the given list of key-value pairs.
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
    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        final HashMap<K, V> map = HashMap.of(k1, v1, k2, v2, k3, v3);
        final Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2), Tuple.of(k3, v3));
        return wrapNonUnique(list, map);
    }

    /**
     * Creates a LinkedHashMap of the given list of key-value pairs.
     *
     * @param k1  a key for the map
     * @param v1  the value for k1
     * @param k2  a key for the map
     * @param v2  the value for k2
     * @param k3  a key for the map
     * @param v3  the value for k3
     * @param k4  a key for the map
     * @param v4  the value for k4
     * @param <K> The key type
     * @param <V> The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        final HashMap<K, V> map = HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4);
        final Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2), Tuple.of(k3, v3), Tuple.of(k4, v4));
        return wrapNonUnique(list, map);
    }

    /**
     * Creates a LinkedHashMap of the given list of key-value pairs.
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
    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        final HashMap<K, V> map = HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
        final Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2), Tuple.of(k3, v3), Tuple.of(k4, v4), Tuple.of(k5, v5));
        return wrapNonUnique(list, map);
    }

    /**
     * Creates a LinkedHashMap of the given list of key-value pairs.
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
    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        final HashMap<K, V> map = HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
        final Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2), Tuple.of(k3, v3), Tuple.of(k4, v4), Tuple.of(k5, v5), Tuple.of(k6, v6));
        return wrapNonUnique(list, map);
    }

    /**
     * Creates a LinkedHashMap of the given list of key-value pairs.
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
    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        final HashMap<K, V> map = HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
        final Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2), Tuple.of(k3, v3), Tuple.of(k4, v4), Tuple.of(k5, v5), Tuple.of(k6, v6), Tuple.of(k7, v7));
        return wrapNonUnique(list, map);
    }

    /**
     * Creates a LinkedHashMap of the given list of key-value pairs.
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
    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        final HashMap<K, V> map = HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
        final Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2), Tuple.of(k3, v3), Tuple.of(k4, v4), Tuple.of(k5, v5), Tuple.of(k6, v6), Tuple.of(k7, v7), Tuple.of(k8, v8));
        return wrapNonUnique(list, map);
    }

    /**
     * Creates a LinkedHashMap of the given list of key-value pairs.
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
    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        final HashMap<K, V> map = HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
        final Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2), Tuple.of(k3, v3), Tuple.of(k4, v4), Tuple.of(k5, v5), Tuple.of(k6, v6), Tuple.of(k7, v7), Tuple.of(k8, v8), Tuple.of(k9, v9));
        return wrapNonUnique(list, map);
    }

    /**
     * Creates a LinkedHashMap of the given list of key-value pairs.
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
    public static <K, V> LinkedHashMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        final HashMap<K, V> map = HashMap.of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
        final Queue<Tuple2<K, V>> list = Queue.of(Tuple.of(k1, v1), Tuple.of(k2, v2), Tuple.of(k3, v3), Tuple.of(k4, v4), Tuple.of(k5, v5), Tuple.of(k6, v6), Tuple.of(k7, v7), Tuple.of(k8, v8), Tuple.of(k9, v9), Tuple.of(k10, v10));
        return wrapNonUnique(list, map);
    }

    /**
     * Returns a LinkedHashMap containing {@code n} values of a given Function {@code f}
     * over a range of integer values from 0 to {@code n - 1}.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @param n   The number of elements in the LinkedHashMap
     * @param f   The Function computing element values
     * @return A LinkedHashMap consisting of elements {@code f(0),f(1), ..., f(n - 1)}
     * @throws NullPointerException if {@code f} is null
     */
    @SuppressWarnings("unchecked")
    public static <K, V> LinkedHashMap<K, V> tabulate(int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V>> f) {
        Objects.requireNonNull(f, "f is null");
        return ofEntries(Collections.tabulate(n, (Function<? super Integer, ? extends Tuple2<K, V>>) f));
    }

    /**
     * Returns a LinkedHashMap containing tuples returned by {@code n} calls to a given Supplier {@code s}.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @param n   The number of elements in the LinkedHashMap
     * @param s   The Supplier computing element values
     * @return A LinkedHashMap of size {@code n}, where each element contains the result supplied by {@code s}.
     * @throws NullPointerException if {@code s} is null
     */
    @SuppressWarnings("unchecked")
    public static <K, V> LinkedHashMap<K, V> fill(int n, Supplier<? extends Tuple2<? extends K, ? extends V>> s) {
        Objects.requireNonNull(s, "s is null");
        return ofEntries(Collections.fill(n, (Supplier<? extends Tuple2<K, V>>) s));
    }

    /**
     * Creates a LinkedHashMap of the given entries.
     *
     * @param entries Map entries
     * @param <K>     The key type
     * @param <V>     The value type
     * @return A new Map containing the given entries
     */
    @SuppressWarnings("unchecked")
    public static <K, V> LinkedHashMap<K, V> ofEntries(java.util.Map.Entry<? extends K, ? extends V>... entries) {
        HashMap<K, V> map = HashMap.empty();
        Queue<Tuple2<K, V>> list = Queue.empty();
        for (java.util.Map.Entry<? extends K, ? extends V> entry : entries) {
            final Tuple2<K, V> tuple = Tuple.of(entry.getKey(), entry.getValue());
            map = map.put(tuple);
            list = list.append(tuple);
        }
        return wrapNonUnique(list, map);
    }

    /**
     * Creates a LinkedHashMap of the given entries.
     *
     * @param entries Map entries
     * @param <K>     The key type
     * @param <V>     The value type
     * @return A new Map containing the given entries
     */
    @SuppressWarnings("unchecked")
    public static <K, V> LinkedHashMap<K, V> ofEntries(Tuple2<? extends K, ? extends V>... entries) {
        final HashMap<K, V> map = HashMap.ofEntries(entries);
        final Queue<Tuple2<K, V>> list = Queue.of((Tuple2<K, V>[]) entries);
        return wrapNonUnique(list, map);
    }

    /**
     * Creates a LinkedHashMap of the given entries.
     *
     * @param entries Map entries
     * @param <K>     The key type
     * @param <V>     The value type
     * @return A new Map containing the given entries
     */
    @SuppressWarnings("unchecked")
    public static <K, V> LinkedHashMap<K, V> ofEntries(Iterable<? extends Tuple2<? extends K, ? extends V>> entries) {
        Objects.requireNonNull(entries, "entries is null");
        if (entries instanceof LinkedHashMap) {
            return (LinkedHashMap<K, V>) entries;
        } else {
            HashMap<K, V> map = HashMap.empty();
            Queue<Tuple2<K, V>> list = Queue.empty();
            for (Tuple2<? extends K, ? extends V> entry : entries) {
                map = map.put(entry);
                list = list.append((Tuple2<K, V>) entry);
            }
            return wrapNonUnique(list, map);
        }
    }

    @Override
    public <K2, V2> LinkedHashMap<K2, V2> bimap(Function<? super K, ? extends K2> keyMapper, Function<? super V, ? extends V2> valueMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        final Iterator<Tuple2<K2, V2>> entries = iterator().map(entry -> Tuple.of(keyMapper.apply(entry._1), valueMapper.apply(entry._2)));
        return LinkedHashMap.ofEntries(entries);
    }

    @Override
    public Tuple2<V, LinkedHashMap<K, V>> computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return Maps.computeIfAbsent(this, key, mappingFunction);
    }

    @Override
    public Tuple2<Option<V>, LinkedHashMap<K, V>> computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return Maps.computeIfPresent(this, key, remappingFunction);
    }

    @Override
    public boolean containsKey(K key) {
        return map.containsKey(key);
    }

    @Override
    public LinkedHashMap<K, V> distinct() {
        return Maps.distinct(this);
    }

    @Override
    public LinkedHashMap<K, V> distinctBy(Comparator<? super Tuple2<K, V>> comparator) {
        return Maps.distinctBy(this, this::createFromEntries, comparator);
    }

    @Override
    public <U> LinkedHashMap<K, V> distinctBy(Function<? super Tuple2<K, V>, ? extends U> keyExtractor) {
        return Maps.distinctBy(this, this::createFromEntries, keyExtractor);
    }

    @Override
    public LinkedHashMap<K, V> drop(int n) {
        return Maps.drop(this, this::createFromEntries, LinkedHashMap::empty, n);
    }

    @Override
    public LinkedHashMap<K, V> dropRight(int n) {
        return Maps.dropRight(this, this::createFromEntries, LinkedHashMap::empty, n);
    }

    @Override
    public LinkedHashMap<K, V> dropUntil(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.dropUntil(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> dropWhile(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.dropWhile(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> filter(BiPredicate<? super K, ? super V> predicate) {
        return Maps.filter(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> filterNot(BiPredicate<? super K, ? super V> predicate) {
        return Maps.filterNot(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> filter(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.filter(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> filterNot(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.filterNot(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> filterKeys(Predicate<? super K> predicate) {
        return Maps.filterKeys(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> filterNotKeys(Predicate<? super K> predicate) {
        return Maps.filterNotKeys(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> filterValues(Predicate<? super V> predicate) {
        return Maps.filterValues(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> filterNotValues(Predicate<? super V> predicate) {
        return Maps.filterNotValues(this, this::createFromEntries, predicate);
    }

    @Override
    public <K2, V2> LinkedHashMap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return foldLeft(LinkedHashMap.<K2, V2> empty(), (acc, entry) -> {
            for (Tuple2<? extends K2, ? extends V2> mappedEntry : mapper.apply(entry._1, entry._2)) {
                acc = acc.put(mappedEntry);
            }
            return acc;
        });
    }

    @Override
    public Option<V> get(K key) {
        return map.get(key);
    }

    @Override
    public V getOrElse(K key, V defaultValue) {
        return map.getOrElse(key, defaultValue);
    }

    @Override
    public <C> Map<C, LinkedHashMap<K, V>> groupBy(Function<? super Tuple2<K, V>, ? extends C> classifier) {
        return Maps.groupBy(this, this::createFromEntries, classifier);
    }

    @Override
    public Iterator<LinkedHashMap<K, V>> grouped(int size) {
        return Maps.grouped(this, this::createFromEntries, size);
    }

    @Override
    public Tuple2<K, V> head() {
        return list.head();
    }

    @Override
    public LinkedHashMap<K, V> init() {
        if (isEmpty()) {
            throw new UnsupportedOperationException("init of empty LinkedHashMap");
        } else {
            return LinkedHashMap.ofEntries(list.init());
        }
    }

    @Override
    public Option<LinkedHashMap<K, V>> initOption() {
        return Maps.initOption(this);
    }

    /**
     * An {@code LinkedHashMap}'s value is computed synchronously.
     *
     * @return false
     */
    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    /**
     * An {@code LinkedHashMap}'s value is computed eagerly.
     *
     * @return false
     */
    @Override
    public boolean isLazy() {
        return false;
    }

    @Override
    public boolean isSequential() {
        return true;
    }

    @Override
    public Iterator<Tuple2<K, V>> iterator() {
        return list.iterator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Set<K> keySet() {
        return LinkedHashSet.wrap((LinkedHashMap<K, Object>) this);
    }

    @Override
    public Tuple2<K, V> last() {
        return list.last();
    }

    @Override
    public <K2, V2> LinkedHashMap<K2, V2> map(BiFunction<? super K, ? super V, Tuple2<K2, V2>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return foldLeft(LinkedHashMap.empty(), (acc, entry) -> acc.put(entry.map(mapper)));
    }

    @Override
    public <K2> LinkedHashMap<K2, V> mapKeys(Function<? super K, ? extends K2> keyMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        return map((k, v) -> Tuple.of(keyMapper.apply(k), v));
    }

    @Override
    public <K2> LinkedHashMap<K2, V> mapKeys(Function<? super K, ? extends K2> keyMapper, BiFunction<? super V, ? super V, ? extends V> valueMerge) {
        return Collections.mapKeys(this, LinkedHashMap.empty(), keyMapper, valueMerge);
    }

    @Override
    public <W> LinkedHashMap<K, W> mapValues(Function<? super V, ? extends W> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return map((k, v) -> Tuple.of(k, mapper.apply(v)));
    }

    @Override
    public LinkedHashMap<K, V> merge(Map<? extends K, ? extends V> that) {
        return Maps.merge(this, this::createFromEntries, that);
    }

    @Override
    public <U extends V> LinkedHashMap<K, V> merge(Map<? extends K, U> that,
            BiFunction<? super V, ? super U, ? extends V> collisionResolution) {
        return Maps.merge(this, this::createFromEntries, that, collisionResolution);
    }

    @Override
    public LinkedHashMap<K, V> orElse(Iterable<? extends Tuple2<K, V>> other) {
        return isEmpty() ? ofEntries(other) : this;
    }

    @Override
    public LinkedHashMap<K, V> orElse(Supplier<? extends Iterable<? extends Tuple2<K, V>>> supplier) {
        return isEmpty() ? ofEntries(supplier.get()) : this;
    }

    @Override
    public Tuple2<LinkedHashMap<K, V>, LinkedHashMap<K, V>> partition(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.partition(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> peek(Consumer<? super Tuple2<K, V>> action) {
        return Maps.peek(this, action);
    }

    @Override
    public LinkedHashMap<K, V> tapEach(Consumer<? super Tuple2<K, V>> action) {
        return Collections.tapEach(this, action);
    }

    @Override
    public <U extends V> LinkedHashMap<K, V> put(K key, U value, BiFunction<? super V, ? super U, ? extends V> merge) {
        return Maps.put(this, key, value, merge);
    }

    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key, the old value is
     * replaced by the specified value.
     * <p>
     * Note that this method has a worst-case linear complexity.
     *
     * @param key   key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return A new Map containing these elements and that entry.
     */
    @Override
    public LinkedHashMap<K, V> put(K key, V value) {
        final Queue<Tuple2<K, V>> newList;
        final Option<V> currentEntry = get(key);
        if (currentEntry.isDefined()) {
            newList = list.replace(Tuple.of(key, currentEntry.get()), Tuple.of(key, value));
        } else {
            newList = list.append(Tuple.of(key, value));
        }
        final HashMap<K, V> newMap = map.put(key, value);
        return wrap(newList, newMap);
    }

    @Override
    public LinkedHashMap<K, V> put(Tuple2<? extends K, ? extends V> entry) {
        return Maps.put(this, entry);
    }

    @Override
    public <U extends V> LinkedHashMap<K, V> put(Tuple2<? extends K, U> entry,
            BiFunction<? super V, ? super U, ? extends V> merge) {
        return Maps.put(this, entry, merge);
    }

    @Override
    public LinkedHashMap<K, V> remove(K key) {
        if (containsKey(key)) {
            final Queue<Tuple2<K, V>> newList = list.removeFirst(t -> Objects.equals(t._1, key));
            final HashMap<K, V> newMap = map.remove(key);
            return wrap(newList, newMap);
        } else {
            return this;
        }
    }

    @Override
    public LinkedHashMap<K, V> removeAll(Iterable<? extends K> keys) {
        Objects.requireNonNull(keys, "keys is null");
        final HashSet<K> toRemove = HashSet.ofAll(keys);
        final Queue<Tuple2<K, V>> newList = list.filter(t -> !toRemove.contains(t._1));
        final HashMap<K, V> newMap = map.filter(t -> !toRemove.contains(t._1));
        return newList.size() == size() ? this : wrap(newList, newMap);
    }

    @Override
    public LinkedHashMap<K, V> replace(Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        Objects.requireNonNull(currentElement, "currentElement is null");
        Objects.requireNonNull(newElement, "newElement is null");

        // We replace the whole element, i.e. key and value have to be present.
        if (!Objects.equals(currentElement, newElement) && contains(currentElement)) {

            Queue<Tuple2<K, V>> newList = list;
            HashMap<K, V> newMap = map;

            final K currentKey = currentElement._1;
            final K newKey = newElement._1;

            // If current key and new key are equal, the element will be automatically replaced,
            // otherwise we need to remove the pair (newKey, ?) from the list manually.
            if (!Objects.equals(currentKey, newKey)) {
                final Option<V> value = newMap.get(newKey);
                if (value.isDefined()) {
                    newList = newList.remove(Tuple.of(newKey, value.get()));
                }
            }

            newList = newList.replace(currentElement, newElement);
            newMap = newMap.remove(currentKey).put(newElement);

            return wrap(newList, newMap);

        } else {
            return this;
        }
    }

    @Override
    public LinkedHashMap<K, V> replaceAll(Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        return Maps.replaceAll(this, currentElement, newElement);
    }

    @Override
    public LinkedHashMap<K, V> replaceValue(K key, V value) {
        return Maps.replaceValue(this, key, value);
    }

    @Override
    public LinkedHashMap<K, V> replace(K key, V oldValue, V newValue) {
        return Maps.replace(this, key, oldValue, newValue);
    }

    @Override
    public LinkedHashMap<K, V> replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        return Maps.replaceAll(this, function);
    }

    @Override
    public LinkedHashMap<K, V> retainAll(Iterable<? extends Tuple2<K, V>> elements) {
        Objects.requireNonNull(elements, "elements is null");
        LinkedHashMap<K, V> result = empty();
        for (Tuple2<K, V> entry : elements) {
            if (contains(entry)) {
                result = result.put(entry._1, entry._2);
            }
        }
        return result;
    }

    @Override
    public LinkedHashMap<K, V> scan(
            Tuple2<K, V> zero,
            BiFunction<? super Tuple2<K, V>, ? super Tuple2<K, V>, ? extends Tuple2<K, V>> operation) {
        return Maps.scan(this, zero, operation, this::createFromEntries);
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public Iterator<LinkedHashMap<K, V>> slideBy(Function<? super Tuple2<K, V>, ?> classifier) {
        return Maps.slideBy(this, this::createFromEntries, classifier);
    }

    @Override
    public Iterator<LinkedHashMap<K, V>> sliding(int size) {
        return Maps.sliding(this, this::createFromEntries, size);
    }

    @Override
    public Iterator<LinkedHashMap<K, V>> sliding(int size, int step) {
        return Maps.sliding(this, this::createFromEntries, size, step);
    }

    @Override
    public Tuple2<LinkedHashMap<K, V>, LinkedHashMap<K, V>> span(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.span(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> tail() {
        if (isEmpty()) {
            throw new UnsupportedOperationException("tail of empty LinkedHashMap");
        } else {
            return LinkedHashMap.ofEntries(list.tail());
        }
    }

    @Override
    public Option<LinkedHashMap<K, V>> tailOption() {
        return Maps.tailOption(this);
    }

    @Override
    public LinkedHashMap<K, V> take(int n) {
        return Maps.take(this, this::createFromEntries, n);
    }

    @Override
    public LinkedHashMap<K, V> takeRight(int n) {
        return Maps.takeRight(this, this::createFromEntries, n);
    }

    @Override
    public LinkedHashMap<K, V> takeUntil(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.takeUntil(this, this::createFromEntries, predicate);
    }

    @Override
    public LinkedHashMap<K, V> takeWhile(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.takeWhile(this, this::createFromEntries, predicate);
    }

    @Override
    public java.util.LinkedHashMap<K, V> toJavaMap() {
        return toJavaMap(java.util.LinkedHashMap::new, t -> t);
    }

    @Override
    public Seq<V> values() {
        return map(t -> t._2);
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
        return "LinkedHashMap";
    }

    @Override
    public String toString() {
        return mkString(stringPrefix() + "(", ", ", ")");
    }

    /**
     * Construct Map with given values and key order.
     *
     * @param list The list of key-value tuples with unique keys.
     * @param map  The map of key-value tuples.
     * @param <K>  The key type
     * @param <V>  The value type
     * @return A new Map containing the given map with given key order
     */
    private static <K, V> LinkedHashMap<K, V> wrap(Queue<Tuple2<K, V>> list, HashMap<K, V> map) {
        return list.isEmpty() ? empty() : new LinkedHashMap<>(list, map);
    }

    /**
     * Construct Map with given values and key order.
     *
     * @param list The list of key-value tuples with non-unique keys.
     * @param map  The map of key-value tuples.
     * @param <K>  The key type
     * @param <V>  The value type
     * @return A new Map containing the given map with given key order
     */
    private static <K, V> LinkedHashMap<K, V> wrapNonUnique(Queue<Tuple2<K, V>> list, HashMap<K, V> map) {
        return list.isEmpty() ? empty() : new LinkedHashMap<>(list.reverse().distinctBy(Tuple2::_1).reverse().toQueue(), map);
    }

    // We need this method to narrow the argument of `ofEntries`.
    // If this method is static with type args <K, V>, the jdk fails to infer types at the call site.
    private LinkedHashMap<K, V> createFromEntries(Iterable<Tuple2<K, V>> tuples) {
        return LinkedHashMap.ofEntries(tuples);
    }

}
