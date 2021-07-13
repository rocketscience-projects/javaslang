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
 * SortedMap implementation, backed by a Red/Black Tree.
 *
 * @param <K> Key type
 * @param <V> Value type
 */
// DEV-NOTE: use entries.min().get() in favor of iterator().next(), it is faster!
public final class TreeMap<K, V> implements SortedMap<K, V>, Serializable {

    private static final long serialVersionUID = 1L;

    private final RedBlackTree<Tuple2<K, V>> entries;

    private TreeMap(RedBlackTree<Tuple2<K, V>> entries) {
        this.entries = entries;
    }

    /**
     * Returns a {@link Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(Collector)} to obtain a
     * {@link TreeMap}.
     * <p>
     * The natural comparator is used to compare TreeMap keys.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @return A {@link TreeMap} Collector.
     */
    public static <K extends Comparable<? super K>, V> Collector<Tuple2<K, V>, ArrayList<Tuple2<K, V>>, TreeMap<K, V>> collector() {
        return createCollector(EntryComparator.natural());
    }

    /**
     * Returns a {@link Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(Collector)} to obtain a
     * {@link TreeMap}.
     *
     * @param <K>           The key type
     * @param <V>           The value type
     * @param keyComparator The comparator used to sort the entries by their key.
     * @return A {@link TreeMap} Collector.
     */
    public static <K, V> Collector<Tuple2<K, V>, ArrayList<Tuple2<K, V>>, TreeMap<K, V>> collector(Comparator<? super K> keyComparator) {
        return createCollector(EntryComparator.of(keyComparator));
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link TreeMap}.
     * <p>
     * The natural comparator is used to compare TreeMap keys.
     *
     * @param keyMapper The key mapper
     * @param <K> The key type
     * @param <V> The value type
     * @param <T> Initial {@link java.util.stream.Stream} elements type
     * @return A {@link TreeMap} Collector.
     */
    public static <K extends Comparable<? super K>, V, T extends V> Collector<T, ArrayList<T>, TreeMap<K, V>> collector(
            Function<? super T, ? extends K> keyMapper) {
        Objects.requireNonNull(keyMapper, "key comparator is null");
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        return createCollector(EntryComparator.natural(), keyMapper, v -> v);
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link TreeMap}.
     * <p>
     * The natural comparator is used to compare TreeMap keys.
     *
     * @param keyMapper The key mapper
     * @param valueMapper The value mapper
     * @param <K> The key type
     * @param <V> The value type
     * @param <T> Initial {@link java.util.stream.Stream} elements type
     * @return A {@link TreeMap} Collector.
     */
    public static <K extends Comparable<? super K>, V, T> Collector<T, ArrayList<T>, TreeMap<K, V>> collector(
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper, "key comparator is null");
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return createCollector(EntryComparator.natural(), keyMapper, valueMapper);
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link TreeMap}.
     *
     * @param keyMapper The key mapper
     * @param <K> The key type
     * @param <V> The value type
     * @param <T> Initial {@link java.util.stream.Stream} elements type
     * @param keyComparator The comparator used to sort the entries by their key.
     * @return A {@link TreeMap} Collector.
     */
    public static <K, V, T extends V> Collector<T, ArrayList<T>, TreeMap<K, V>> collector(
            Comparator<? super K> keyComparator,
            Function<? super T, ? extends K> keyMapper) {
        Objects.requireNonNull(keyMapper, "key comparator is null");
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        return createCollector(EntryComparator.of(keyComparator), keyMapper, v -> v);
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link TreeMap}.
     *
     * @param keyMapper The key mapper
     * @param valueMapper The value mapper
     * @param <K> The key type
     * @param <V> The value type
     * @param <T> Initial {@link java.util.stream.Stream} elements type
     * @param keyComparator The comparator used to sort the entries by their key.
     * @return A {@link TreeMap} Collector.
     */
    public static <K, V, T> Collector<T, ArrayList<T>, TreeMap<K, V>> collector(
            Comparator<? super K> keyComparator,
            Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        Objects.requireNonNull(keyMapper, "key comparator is null");
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return createCollector(EntryComparator.of(keyComparator), keyMapper, valueMapper);
    }

    /**
     * Returns the empty TreeMap. The underlying key comparator is the natural comparator of K.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @return A new empty TreeMap.
     */
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> empty() {
        return new TreeMap<>(RedBlackTree.empty(EntryComparator.natural()));
    }

    /**
     * Returns the empty TreeMap using the given key comparator.
     *
     * @param <K>           The key type
     * @param <V>           The value type
     * @param keyComparator The comparator used to sort the entries by their key.
     * @return A new empty TreeMap.
     */
    public static <K, V> TreeMap<K, V> empty(Comparator<? super K> keyComparator) {
        return new TreeMap<>(RedBlackTree.empty(EntryComparator.of(keyComparator)));
    }

    /**
     * Narrows a widened {@code TreeMap<? extends K, ? extends V>} to {@code TreeMap<K, V>}
     * by performing a type-safe cast. This is eligible because immutable/read-only
     * collections are covariant.
     * <p>
     * CAUTION: If {@code K} is narrowed, the underlying {@code Comparator} might fail!
     *
     * @param treeMap A {@code TreeMap}.
     * @param <K>     Key type
     * @param <V>     Value type
     * @return the given {@code treeMap} instance as narrowed type {@code TreeMap<K, V>}.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> TreeMap<K, V> narrow(TreeMap<? extends K, ? extends V> treeMap) {
        return (TreeMap<K, V>) treeMap;
    }

    /**
     * Returns a singleton {@code TreeMap}, i.e. a {@code TreeMap} of one entry.
     * The underlying key comparator is the natural comparator of K.
     *
     * @param <K>   The key type
     * @param <V>   The value type
     * @param entry A map entry.
     * @return A new TreeMap containing the given entry.
     */
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(Tuple2<? extends K, ? extends V> entry) {
        Objects.requireNonNull(entry, "entry is null");
        return createFromTuple(EntryComparator.natural(), entry);
    }

    /**
     * Returns a singleton {@code TreeMap}, i.e. a {@code TreeMap} of one entry using a specific key comparator.
     *
     * @param <K>           The key type
     * @param <V>           The value type
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param entry         A map entry.
     * @return A new TreeMap containing the given entry.
     */
    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, Tuple2<? extends K, ? extends V> entry) {
        Objects.requireNonNull(entry, "entry is null");
        return createFromTuple(EntryComparator.of(keyComparator), entry);
    }

    /**
     * Returns a {@code TreeMap}, from a source java.util.Map.
     *
     * @param map A map
     * @param <K> The key type
     * @param <V> The value type
     * @return A new Map containing the given map
     */
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> ofAll(java.util.Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map is null");
        return createFromMap(EntryComparator.natural(), map);
    }

    /**
     * Returns a {@code TreeMap}, from entries mapped from stream.
     *
     * @param stream      the source stream
     * @param keyMapper   the key mapper
     * @param valueMapper the value mapper
     * @param <T>         The stream element type
     * @param <K>         The key type
     * @param <V>         The value type
     * @return A new Map
     */
    public static <T, K extends Comparable<? super K>, V> TreeMap<K, V> ofAll(java.util.stream.Stream<? extends T> stream,
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper) {
        return Maps.ofStream(TreeMap.<K, V> empty(), stream, keyMapper, valueMapper);
    }

    /**
     * Returns a {@code TreeMap}, from entries mapped from stream.
     *
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param stream        the source stream
     * @param keyMapper     the key mapper
     * @param valueMapper   the value mapper
     * @param <T>           The stream element type
     * @param <K>           The key type
     * @param <V>           The value type
     * @return A new Map
     */
    public static <T, K, V> TreeMap<K, V> ofAll(Comparator<? super K> keyComparator,
            java.util.stream.Stream<? extends T> stream,
            Function<? super T, ? extends K> keyMapper,
            Function<? super T, ? extends V> valueMapper) {
        return Maps.ofStream(empty(keyComparator), stream, keyMapper, valueMapper);
    }

    /**
     * Returns a {@code TreeMap}, from entries mapped from stream.
     *
     * @param stream      the source stream
     * @param entryMapper the entry mapper
     * @param <T>         The stream element type
     * @param <K>         The key type
     * @param <V>         The value type
     * @return A new Map
     */
    public static <T, K extends Comparable<? super K>, V> TreeMap<K, V> ofAll(java.util.stream.Stream<? extends T> stream,
            Function<? super T, Tuple2<? extends K, ? extends V>> entryMapper) {
        return Maps.ofStream(TreeMap.<K, V> empty(), stream, entryMapper);
    }

    /**
     * Returns a {@code TreeMap}, from entries mapped from stream.
     *
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param stream      the source stream
     * @param entryMapper the entry mapper
     * @param <T>         The stream element type
     * @param <K>         The key type
     * @param <V>         The value type
     * @return A new Map
     */
    public static <T, K, V> TreeMap<K, V> ofAll(Comparator<? super K> keyComparator,
            java.util.stream.Stream<? extends T> stream,
            Function<? super T, Tuple2<? extends K, ? extends V>> entryMapper) {
        return Maps.ofStream(empty(keyComparator), stream, entryMapper);
    }

    /**
     * Returns a {@code TreeMap}, from a source java.util.Map.
     *
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param map           A map
     * @param <K>           The key type
     * @param <V>           The value type
     * @return A new Map containing the given map
     */
    public static <K, V> TreeMap<K, V> ofAll(Comparator<? super K> keyComparator, java.util.Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map is null");
        return createFromMap(EntryComparator.of(keyComparator), map);
    }

    /**
     * Returns a singleton {@code TreeMap}, i.e. a {@code TreeMap} of one element.
     *
     * @param key   A singleton map key.
     * @param value A singleton map value.
     * @param <K>   The key type
     * @param <V>   The value type
     * @return A new Map containing the given entry
     */
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K key, V value) {
        return createFromPairs(EntryComparator.natural(), key, value);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
     *
     * @param k1  a key for the map
     * @param v1  the value for k1
     * @param k2  a key for the map
     * @param v2  the value for k2
     * @param <K> The key type
     * @param <V> The value type
     * @return A new Map containing the given entries
     */
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2) {
        return createFromPairs(EntryComparator.natural(), k1, v1, k2, v2);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
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
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3) {
        return createFromPairs(EntryComparator.natural(), k1, v1, k2, v2, k3, v3);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
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
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return createFromPairs(EntryComparator.natural(), k1, v1, k2, v2, k3, v3, k4, v4);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
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
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return createFromPairs(EntryComparator.natural(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
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
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return createFromPairs(EntryComparator.natural(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
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
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        return createFromPairs(EntryComparator.natural(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
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
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        return createFromPairs(EntryComparator.natural(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
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
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        return createFromPairs(EntryComparator.natural(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
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
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> of(K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        return createFromPairs(EntryComparator.natural(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
    }

    /**
     * Returns a singleton {@code TreeMap}, i.e. a {@code TreeMap} of one element.
     *
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param key           A singleton map key.
     * @param value         A singleton map value.
     * @param <K>           The key type
     * @param <V>           The value type
     * @return A new Map containing the given entry
     */
    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K key, V value) {
        return createFromPairs(EntryComparator.of(keyComparator), key, value);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
     *
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param k1            a key for the map
     * @param v1            the value for k1
     * @param k2            a key for the map
     * @param v2            the value for k2
     * @param <K>           The key type
     * @param <V>           The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2) {
        return createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
     *
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param k1            a key for the map
     * @param v1            the value for k1
     * @param k2            a key for the map
     * @param v2            the value for k2
     * @param k3            a key for the map
     * @param v3            the value for k3
     * @param <K>           The key type
     * @param <V>           The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2, K k3, V v3) {
        return createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2, k3, v3);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
     *
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param k1            a key for the map
     * @param v1            the value for k1
     * @param k2            a key for the map
     * @param v2            the value for k2
     * @param k3            a key for the map
     * @param v3            the value for k3
     * @param k4            a key for the map
     * @param v4            the value for k4
     * @param <K>           The key type
     * @param <V>           The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4) {
        return createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2, k3, v3, k4, v4);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
     *
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param k1            a key for the map
     * @param v1            the value for k1
     * @param k2            a key for the map
     * @param v2            the value for k2
     * @param k3            a key for the map
     * @param v3            the value for k3
     * @param k4            a key for the map
     * @param v4            the value for k4
     * @param k5            a key for the map
     * @param v5            the value for k5
     * @param <K>           The key type
     * @param <V>           The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5) {
        return createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
     *
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param k1            a key for the map
     * @param v1            the value for k1
     * @param k2            a key for the map
     * @param v2            the value for k2
     * @param k3            a key for the map
     * @param v3            the value for k3
     * @param k4            a key for the map
     * @param v4            the value for k4
     * @param k5            a key for the map
     * @param v5            the value for k5
     * @param k6            a key for the map
     * @param v6            the value for k6
     * @param <K>           The key type
     * @param <V>           The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6) {
        return createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
     *
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param k1            a key for the map
     * @param v1            the value for k1
     * @param k2            a key for the map
     * @param v2            the value for k2
     * @param k3            a key for the map
     * @param v3            the value for k3
     * @param k4            a key for the map
     * @param v4            the value for k4
     * @param k5            a key for the map
     * @param v5            the value for k5
     * @param k6            a key for the map
     * @param v6            the value for k6
     * @param k7            a key for the map
     * @param v7            the value for k7
     * @param <K>           The key type
     * @param <V>           The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7) {
        return createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
     *
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param k1            a key for the map
     * @param v1            the value for k1
     * @param k2            a key for the map
     * @param v2            the value for k2
     * @param k3            a key for the map
     * @param v3            the value for k3
     * @param k4            a key for the map
     * @param v4            the value for k4
     * @param k5            a key for the map
     * @param v5            the value for k5
     * @param k6            a key for the map
     * @param v6            the value for k6
     * @param k7            a key for the map
     * @param v7            the value for k7
     * @param k8            a key for the map
     * @param v8            the value for k8
     * @param <K>           The key type
     * @param <V>           The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8) {
        return createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
     *
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param k1            a key for the map
     * @param v1            the value for k1
     * @param k2            a key for the map
     * @param v2            the value for k2
     * @param k3            a key for the map
     * @param v3            the value for k3
     * @param k4            a key for the map
     * @param v4            the value for k4
     * @param k5            a key for the map
     * @param v5            the value for k5
     * @param k6            a key for the map
     * @param v6            the value for k6
     * @param k7            a key for the map
     * @param v7            the value for k7
     * @param k8            a key for the map
     * @param v8            the value for k8
     * @param k9            a key for the map
     * @param v9            the value for k9
     * @param <K>           The key type
     * @param <V>           The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9) {
        return createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
    }

    /**
     * Creates a {@code TreeMap} of the given list of key-value pairs.
     *
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param k1            a key for the map
     * @param v1            the value for k1
     * @param k2            a key for the map
     * @param v2            the value for k2
     * @param k3            a key for the map
     * @param v3            the value for k3
     * @param k4            a key for the map
     * @param v4            the value for k4
     * @param k5            a key for the map
     * @param v5            the value for k5
     * @param k6            a key for the map
     * @param v6            the value for k6
     * @param k7            a key for the map
     * @param v7            the value for k7
     * @param k8            a key for the map
     * @param v8            the value for k8
     * @param k9            a key for the map
     * @param v9            the value for k9
     * @param k10           a key for the map
     * @param v10           the value for k10
     * @param <K>           The key type
     * @param <V>           The value type
     * @return A new Map containing the given entries
     */
    public static <K, V> TreeMap<K, V> of(Comparator<? super K> keyComparator, K k1, V v1, K k2, V v2, K k3, V v3, K k4, V v4, K k5, V v5, K k6, V v6, K k7, V v7, K k8, V v8, K k9, V v9, K k10, V v10) {
        return createFromPairs(EntryComparator.of(keyComparator), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
    }

    /**
     * Returns a TreeMap containing {@code n} values of a given Function {@code f}
     * over a range of integer values from 0 to {@code n - 1}.
     *
     * @param <K>           The key type
     * @param <V>           The value type
     * @param keyComparator The comparator used to sort the entries by their key
     * @param n             The number of elements in the TreeMap
     * @param f             The Function computing element values
     * @return A TreeMap consisting of elements {@code f(0),f(1), ..., f(n - 1)}
     * @throws NullPointerException if {@code keyComparator} or {@code f} are null
     */
    public static <K, V> TreeMap<K, V> tabulate(Comparator<? super K> keyComparator, int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V>> f) {
        Objects.requireNonNull(f, "f is null");
        return createTreeMap(EntryComparator.of(keyComparator), Collections.tabulate(n, f));
    }

    /**
     * Returns a TreeMap containing {@code n} values of a given Function {@code f}
     * over a range of integer values from 0 to {@code n - 1}.
     * The underlying key comparator is the natural comparator of K.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @param n   The number of elements in the TreeMap
     * @param f   The Function computing element values
     * @return A TreeMap consisting of elements {@code f(0),f(1), ..., f(n - 1)}
     * @throws NullPointerException if {@code f} is null
     */
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> tabulate(int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V>> f) {
        Objects.requireNonNull(f, "f is null");
        return createTreeMap(EntryComparator.natural(), Collections.tabulate(n, f));
    }

    /**
     * Returns a TreeMap containing tuples returned by {@code n} calls to a given Supplier {@code s}.
     *
     * @param <K>           The key type
     * @param <V>           The value type
     * @param keyComparator The comparator used to sort the entries by their key
     * @param n             The number of elements in the TreeMap
     * @param s             The Supplier computing element values
     * @return A TreeMap of size {@code n}, where each element contains the result supplied by {@code s}.
     * @throws NullPointerException if {@code keyComparator} or {@code s} are null
     */
    @SuppressWarnings("unchecked")
    public static <K, V> TreeMap<K, V> fill(Comparator<? super K> keyComparator, int n, Supplier<? extends Tuple2<? extends K, ? extends V>> s) {
        Objects.requireNonNull(s, "s is null");
        return createTreeMap(EntryComparator.of(keyComparator), Collections.fill(n, s));
    }

    /**
     * Returns a TreeMap containing tuples returned by {@code n} calls to a given Supplier {@code s}.
     * The underlying key comparator is the natural comparator of K.
     *
     * @param <K> The key type
     * @param <V> The value type
     * @param n   The number of elements in the TreeMap
     * @param s   The Supplier computing element values
     * @return A TreeMap of size {@code n}, where each element contains the result supplied by {@code s}.
     * @throws NullPointerException if {@code s} is null
     */
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> fill(int n, Supplier<? extends Tuple2<? extends K, ? extends V>> s) {
        Objects.requireNonNull(s, "s is null");
        return createTreeMap(EntryComparator.natural(), Collections.fill(n, s));
    }

    /**
     * Creates a {@code TreeMap} of the given entries using the natural key comparator.
     *
     * @param <K>     The key type
     * @param <V>     The value type
     * @param entries Map entries
     * @return A new TreeMap containing the given entries.
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> ofEntries(Tuple2<? extends K, ? extends V>... entries) {
        return createFromTuples(EntryComparator.natural(), entries);
    }

    /**
     * Creates a {@code TreeMap} of the given entries using the given key comparator.
     *
     * @param <K>           The key type
     * @param <V>           The value type
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param entries       Map entries
     * @return A new TreeMap containing the given entries.
     */
    @SuppressWarnings({ "unchecked", "varargs" })
    @SafeVarargs
    public static <K, V> TreeMap<K, V> ofEntries(Comparator<? super K> keyComparator, Tuple2<? extends K, ? extends V>... entries) {
        return createFromTuples(EntryComparator.of(keyComparator), entries);
    }

    /**
     * Creates a {@code TreeMap} of the given entries using the natural key comparator.
     *
     * @param <K>     The key type
     * @param <V>     The value type
     * @param entries Map entries
     * @return A new TreeMap containing the given entries.
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> ofEntries(java.util.Map.Entry<? extends K, ? extends V>... entries) {
        return createFromMapEntries(EntryComparator.natural(), entries);
    }

    /**
     * Creates a {@code TreeMap} of the given entries using the given key comparator.
     *
     * @param <K>           The key type
     * @param <V>           The value type
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param entries       Map entries
     * @return A new TreeMap containing the given entries.
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    public static <K, V> TreeMap<K, V> ofEntries(Comparator<? super K> keyComparator, java.util.Map.Entry<? extends K, ? extends V>... entries) {
        return createFromMapEntries(EntryComparator.of(keyComparator), entries);
    }

    /**
     * Creates a {@code TreeMap} of the given entries.
     *
     * @param <K>     The key type
     * @param <V>     The value type
     * @param entries Map entries
     * @return A new TreeMap containing the given entries.
     */
    public static <K extends Comparable<? super K>, V> TreeMap<K, V> ofEntries(Iterable<? extends Tuple2<? extends K, ? extends V>> entries) {
        return createTreeMap(EntryComparator.natural(), entries);
    }

    /**
     * Creates a {@code TreeMap} of the given entries.
     *
     * @param <K>           The key type
     * @param <V>           The value type
     * @param keyComparator The comparator used to sort the entries by their key.
     * @param entries       Map entries
     * @return A new TreeMap containing the given entries.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> TreeMap<K, V> ofEntries(Comparator<? super K> keyComparator, Iterable<? extends Tuple2<? extends K, ? extends V>> entries) {
        return createTreeMap(EntryComparator.of(keyComparator), entries);
    }

    // -- TreeMap API

    @Override
    public <K2, V2> TreeMap<K2, V2> bimap(Function<? super K, ? extends K2> keyMapper, Function<? super V, ? extends V2> valueMapper) {
        return bimap(this, EntryComparator.natural(), keyMapper, valueMapper);
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> bimap(Comparator<? super K2> keyComparator,
            Function<? super K, ? extends K2> keyMapper, Function<? super V, ? extends V2> valueMapper) {
        return bimap(this, EntryComparator.of(keyComparator), keyMapper, valueMapper);
    }

    @Override
    public Tuple2<V, TreeMap<K, V>> computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        return Maps.computeIfAbsent(this, key, mappingFunction);
    }

    @Override
    public Tuple2<Option<V>, TreeMap<K, V>> computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return Maps.computeIfPresent(this, key, remappingFunction);
    }

    @Override
    public boolean containsKey(K key) {
        return entries.contains(new Tuple2<>(key, /*ignored*/null));
    }

    @Override
    public TreeMap<K, V> distinct() {
        return Maps.distinct(this);
    }

    @Override
    public TreeMap<K, V> distinctBy(Comparator<? super Tuple2<K, V>> comparator) {
        return Maps.distinctBy(this, this::createFromEntries, comparator);
    }

    @Override
    public <U> TreeMap<K, V> distinctBy(Function<? super Tuple2<K, V>, ? extends U> keyExtractor) {
        return Maps.distinctBy(this, this::createFromEntries, keyExtractor);
    }

    @Override
    public TreeMap<K, V> drop(int n) {
        return Maps.drop(this, this::createFromEntries, this::emptyInstance, n);
    }

    @Override
    public TreeMap<K, V> dropRight(int n) {
        return Maps.dropRight(this, this::createFromEntries, this::emptyInstance, n);
    }

    @Override
    public TreeMap<K, V> dropUntil(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.dropUntil(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> dropWhile(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.dropWhile(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> filter(BiPredicate<? super K, ? super V> predicate) {
        return Maps.filter(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> filterNot(BiPredicate<? super K, ? super V> predicate) {
        return Maps.filterNot(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> filter(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.filter(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> filterNot(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.filterNot(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> filterKeys(Predicate<? super K> predicate) {
        return Maps.filterKeys(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> filterNotKeys(Predicate<? super K> predicate) {
        return Maps.filterNotKeys(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> filterValues(Predicate<? super V> predicate) {
        return Maps.filterValues(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> filterNotValues(Predicate<? super V> predicate) {
        return Maps.filterNotValues(this, this::createFromEntries, predicate);
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> flatMap(BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        return flatMap(this, EntryComparator.natural(), mapper);
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> flatMap(Comparator<? super K2> keyComparator,
            BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        return flatMap(this, EntryComparator.of(keyComparator), mapper);
    }

    @Override
    public Option<V> get(K key) {
        final V ignored = null;
        return entries.find(new Tuple2<>(key, ignored)).map(Tuple2::_2);
    }

    @Override
    public V getOrElse(K key, V defaultValue) {
        return get(key).getOrElse(defaultValue);
    }

    @Override
    public <C> Map<C, TreeMap<K, V>> groupBy(Function<? super Tuple2<K, V>, ? extends C> classifier) {
        return Maps.groupBy(this, this::createFromEntries, classifier);
    }

    @Override
    public Iterator<TreeMap<K, V>> grouped(int size) {
        return Maps.grouped(this, this::createFromEntries, size);
    }

    @Override
    public Tuple2<K, V> head() {
        if (isEmpty()) {
            throw new NoSuchElementException("head of empty TreeMap");
        } else {
            return entries.min().get();
        }
    }

    @Override
    public Tuple2<K, V> last() {
        if (isEmpty()) {
            throw new NoSuchElementException("last of empty TreeMap");
        } else {
            return entries.max().get();
        }
    }

    @Override
    public TreeMap<K, V> init() {
        if (isEmpty()) {
            throw new UnsupportedOperationException("init of empty TreeMap");
        } else {
            final Tuple2<K, V> max = entries.max().get();
            return new TreeMap<>(entries.delete(max));
        }
    }

    @Override
    public Option<TreeMap<K, V>> initOption() {
        return Maps.initOption(this);
    }

    @Override
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * An {@code TreeMap}'s value is computed eagerly.
     *
     * @return false
     */
    @Override
    public boolean isLazy() {
        return false;
    }

    @Override
    public Iterator<Tuple2<K, V>> iterator() {
        return entries.iterator();
    }

    @Override
    public SortedSet<K> keySet() {
        return TreeSet.ofAll(comparator(), iterator().map(Tuple2::_1));
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> map(BiFunction<? super K, ? super V, Tuple2<K2, V2>> mapper) {
        return map(this, EntryComparator.natural(), mapper);
    }

    @Override
    public <K2, V2> TreeMap<K2, V2> map(Comparator<? super K2> keyComparator,
            BiFunction<? super K, ? super V, Tuple2<K2, V2>> mapper) {
        Objects.requireNonNull(keyComparator, "keyComparator is null");
        return map(this, EntryComparator.of(keyComparator), mapper);
    }

    @Override
    public <K2> TreeMap<K2, V> mapKeys(Function<? super K, ? extends K2> keyMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        return map((k, v) -> Tuple.of(keyMapper.apply(k), v));
    }

    @Override
    public <K2> TreeMap<K2, V> mapKeys(Function<? super K, ? extends K2> keyMapper, BiFunction<? super V, ? super V, ? extends V> valueMerge) {
        final Comparator<K2> comparator = Comparators.naturalComparator();
        return Collections.mapKeys(this, TreeMap.<K2, V> empty(comparator), keyMapper, valueMerge);
    }

    @Override
    public <W> TreeMap<K, W> mapValues(Function<? super V, ? extends W> valueMapper) {
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return map(comparator(), (k, v) -> Tuple.of(k, valueMapper.apply(v)));
    }

    @Override
    public TreeMap<K, V> merge(Map<? extends K, ? extends V> that) {
        return Maps.merge(this, this::createFromEntries, that);
    }

    @Override
    public <U extends V> TreeMap<K, V> merge(Map<? extends K, U> that,
            BiFunction<? super V, ? super U, ? extends V> collisionResolution) {
        return Maps.merge(this, this::createFromEntries, that, collisionResolution);
    }

    /**
     * Returns this {@code TreeMap} if it is nonempty,
     * otherwise {@code TreeMap} created from iterable, using existing comparator.
     *
     * @param other An alternative {@code Traversable}
     * @return this {@code TreeMap} if it is nonempty,
     * otherwise {@code TreeMap} created from iterable, using existing comparator.
     */
    @Override
    public TreeMap<K, V> orElse(Iterable<? extends Tuple2<K, V>> other) {
        return isEmpty() ? ofEntries(comparator(), other) : this;
    }

    /**
     * Returns this {@code TreeMap} if it is nonempty,
     * otherwise {@code TreeMap} created from result of evaluating supplier, using existing comparator.
     *
     * @param supplier An alternative {@code Traversable}
     * @return this {@code TreeMap} if it is nonempty,
     * otherwise {@code TreeMap} created from result of evaluating supplier, using existing comparator.
     */
    @Override
    public TreeMap<K, V> orElse(Supplier<? extends Iterable<? extends Tuple2<K, V>>> supplier) {
        return isEmpty() ? ofEntries(comparator(), supplier.get()) : this;
    }

    @Override
    public Tuple2<TreeMap<K, V>, TreeMap<K, V>> partition(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.partition(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> peek(Consumer<? super Tuple2<K, V>> action) {
        return Maps.peek(this, action);
    }

    @Override
    public <U extends V> TreeMap<K, V> put(K key, U value, BiFunction<? super V, ? super U, ? extends V> merge) {
        return Maps.put(this, key, value, merge);
    }

    @Override
    public TreeMap<K, V> put(K key, V value) {
        return new TreeMap<>(entries.insert(new Tuple2<>(key, value)));
    }

    @Override
    public TreeMap<K, V> put(Tuple2<? extends K, ? extends V> entry) {
        return Maps.put(this, entry);
    }

    @Override
    public <U extends V> TreeMap<K, V> put(Tuple2<? extends K, U> entry,
            BiFunction<? super V, ? super U, ? extends V> merge) {
        return Maps.put(this, entry, merge);
    }

    @Override
    public TreeMap<K, V> remove(K key) {
        final V ignored = null;
        final Tuple2<K, V> entry = new Tuple2<>(key, ignored);
        if (entries.contains(entry)) {
            return new TreeMap<>(entries.delete(entry));
        } else {
            return this;
        }
    }

    @Override
    public TreeMap<K, V> removeAll(Iterable<? extends K> keys) {
        final V ignored = null;
        RedBlackTree<Tuple2<K, V>> removed = entries;
        for (K key : keys) {
            final Tuple2<K, V> entry = new Tuple2<>(key, ignored);
            if (removed.contains(entry)) {
                removed = removed.delete(entry);
            }
        }
        if (removed.size() == entries.size()) {
            return this;
        } else {
            return new TreeMap<>(removed);
        }
    }

    @Override
    public TreeMap<K, V> replace(Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        return Maps.replace(this, currentElement, newElement);
    }

    @Override
    public TreeMap<K, V> replaceAll(Tuple2<K, V> currentElement, Tuple2<K, V> newElement) {
        return Maps.replaceAll(this, currentElement, newElement);
    }

    @Override
    public TreeMap<K, V> replaceValue(K key, V value) {
        return Maps.replaceValue(this, key, value);
    }

    @Override
    public TreeMap<K, V> replace(K key, V oldValue, V newValue) {
        return Maps.replace(this, key, oldValue, newValue);
    }

    @Override
    public TreeMap<K, V> replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        return Maps.replaceAll(this, function);
    }

    @Override
    public TreeMap<K, V> retainAll(Iterable<? extends Tuple2<K, V>> elements) {
        Objects.requireNonNull(elements, "elements is null");
        RedBlackTree<Tuple2<K, V>> tree = RedBlackTree.empty(entries.comparator());
        for (Tuple2<K, V> entry : elements) {
            if (contains(entry)) {
                tree = tree.insert(entry);
            }
        }
        return new TreeMap<>(tree);
    }

    @Override
    public TreeMap<K, V> scan(
            Tuple2<K, V> zero,
            BiFunction<? super Tuple2<K, V>, ? super Tuple2<K, V>, ? extends Tuple2<K, V>> operation) {
        return Maps.scan(this, zero, operation, this::createFromEntries);
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public Iterator<TreeMap<K, V>> slideBy(Function<? super Tuple2<K, V>, ?> classifier) {
        return Maps.slideBy(this, this::createFromEntries, classifier);
    }

    @Override
    public Iterator<TreeMap<K, V>> sliding(int size) {
        return Maps.sliding(this, this::createFromEntries, size);
    }

    @Override
    public Iterator<TreeMap<K, V>> sliding(int size, int step) {
        return Maps.sliding(this, this::createFromEntries, size, step);
    }

    @Override
    public Tuple2<TreeMap<K, V>, TreeMap<K, V>> span(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.span(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> tail() {
        if (isEmpty()) {
            throw new UnsupportedOperationException("tail of empty TreeMap");
        } else {
            final Tuple2<K, V> min = entries.min().get();
            return new TreeMap<>(entries.delete(min));
        }
    }

    @Override
    public Option<TreeMap<K, V>> tailOption() {
        return Maps.tailOption(this);
    }

    @Override
    public TreeMap<K, V> take(int n) {
        return Maps.take(this, this::createFromEntries, n);
    }

    @Override
    public TreeMap<K, V> takeRight(int n) {
        return Maps.takeRight(this, this::createFromEntries, n);
    }

    @Override
    public TreeMap<K, V> takeUntil(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.takeUntil(this, this::createFromEntries, predicate);
    }

    @Override
    public TreeMap<K, V> takeWhile(Predicate<? super Tuple2<K, V>> predicate) {
        return Maps.takeWhile(this, this::createFromEntries, predicate);
    }

    @Override
    public java.util.TreeMap<K, V> toJavaMap() {
        return toJavaMap(() -> new java.util.TreeMap<>(comparator()), t -> t);
    }

    @Override
    public Seq<V> values() {
        return map(Tuple2::_2);
    }

    // -- Object

    @Override
    public boolean equals(Object o) {
        return Collections.equals(this, o);
    }

    @Override
    public int hashCode() {
        return Collections.hashUnordered(this);
    }

    @Override
    public String stringPrefix() {
        return "TreeMap";
    }

    @Override
    public String toString() {
        return mkString(stringPrefix() + "(", ", ", ")");
    }

    // -- private helpers

    private static <K, K2, V, V2> TreeMap<K2, V2> bimap(TreeMap<K, V> map, EntryComparator<K2, V2> entryComparator,
            Function<? super K, ? extends K2> keyMapper, Function<? super V, ? extends V2> valueMapper) {
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMapper, "valueMapper is null");
        return createTreeMap(entryComparator, map.entries, entry -> entry.map(keyMapper, valueMapper));
    }

    private static <K, V, K2, V2> TreeMap<K2, V2> flatMap(TreeMap<K, V> map, EntryComparator<K2, V2> entryComparator,
            BiFunction<? super K, ? super V, ? extends Iterable<Tuple2<K2, V2>>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return createTreeMap(entryComparator, map.entries.iterator().flatMap(entry -> mapper.apply(entry._1, entry._2)));
    }

    private static <K, K2, V, V2> TreeMap<K2, V2> map(TreeMap<K, V> map, EntryComparator<K2, V2> entryComparator,
            BiFunction<? super K, ? super V, Tuple2<K2, V2>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return createTreeMap(entryComparator, map.entries, entry -> entry.map(mapper));
    }

    // -- internal factory methods

    private static <K, V> Collector<Tuple2<K, V>, ArrayList<Tuple2<K, V>>, TreeMap<K, V>> createCollector(EntryComparator<K, V> entryComparator) {
        return Collections.toListAndThen(list -> createTreeMap(entryComparator, list));
    }

    private static <K, V, T> Collector<T, ArrayList<T>, TreeMap<K, V>> createCollector(
            EntryComparator<K, V> entryComparator,
            Function<? super T, ? extends K> keyMapper, Function<? super T, ? extends V> valueMapper) {
        return Collections.toListAndThen(arr -> createTreeMap(entryComparator, Iterator.ofAll(arr)
                .map(t -> Tuple.of(keyMapper.apply(t), valueMapper.apply(t)))));
    }

    @SuppressWarnings("unchecked")
    private static <K, V> TreeMap<K, V> createTreeMap(EntryComparator<K, V> entryComparator,
            Iterable<? extends Tuple2<? extends K, ? extends V>> entries) {
        Objects.requireNonNull(entries, "entries is null");
        RedBlackTree<Tuple2<K, V>> tree = RedBlackTree.empty(entryComparator);
        for (Tuple2<K, V> entry : (Iterable<Tuple2<K, V>>) entries) {
            tree = tree.insert(entry);
        }
        return new TreeMap<>(tree);
    }

    private static <K, K2, V, V2> TreeMap<K2, V2> createTreeMap(EntryComparator<K2, V2> entryComparator,
            Iterable<Tuple2<K, V>> entries, Function<Tuple2<K, V>, Tuple2<K2, V2>> entryMapper) {
        RedBlackTree<Tuple2<K2, V2>> tree = RedBlackTree.empty(entryComparator);
        for (Tuple2<K, V> entry : entries) {
            tree = tree.insert(entryMapper.apply(entry));
        }
        return new TreeMap<>(tree);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> TreeMap<K, V> createFromMap(EntryComparator<K, V> entryComparator, java.util.Map<? extends K, ? extends V> map) {
        Objects.requireNonNull(map, "map is null");
        RedBlackTree<Tuple2<K, V>> tree = RedBlackTree.empty(entryComparator);
        for (java.util.Map.Entry<K, V> entry : ((java.util.Map<K, V>) map).entrySet()) {
            tree = tree.insert(Tuple.of(entry.getKey(), entry.getValue()));
        }
        return new TreeMap<>(tree);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> TreeMap<K, V> createFromTuple(EntryComparator<K, V> entryComparator, Tuple2<? extends K, ? extends V> entry) {
        Objects.requireNonNull(entry, "entry is null");
        return new TreeMap<>(RedBlackTree.of(entryComparator, (Tuple2<K, V>) entry));
    }

    @SuppressWarnings("unchecked")
    private static <K, V> TreeMap<K, V> createFromTuples(EntryComparator<K, V> entryComparator, Tuple2<? extends K, ? extends V>... entries) {
        Objects.requireNonNull(entries, "entries is null");
        RedBlackTree<Tuple2<K, V>> tree = RedBlackTree.empty(entryComparator);
        for (Tuple2<? extends K, ? extends V> entry : entries) {
            tree = tree.insert((Tuple2<K, V>) entry);
        }
        return new TreeMap<>(tree);
    }

    @SafeVarargs
    private static <K, V> TreeMap<K, V> createFromMapEntries(EntryComparator<K, V> entryComparator, java.util.Map.Entry<? extends K, ? extends V>... entries) {
        Objects.requireNonNull(entries, "entries is null");
        RedBlackTree<Tuple2<K, V>> tree = RedBlackTree.empty(entryComparator);
        for (java.util.Map.Entry<? extends K, ? extends V> entry : entries) {
            final K key = entry.getKey();
            final V value = entry.getValue();
            tree = tree.insert(Tuple.of(key, value));
        }
        return new TreeMap<>(tree);
    }

    @SuppressWarnings("unchecked")
    private static <K, V> TreeMap<K, V> createFromPairs(EntryComparator<K, V> entryComparator, Object... pairs) {
        RedBlackTree<Tuple2<K, V>> tree = RedBlackTree.empty(entryComparator);
        for (int i = 0; i < pairs.length; i += 2) {
            final K key = (K) pairs[i];
            final V value = (V) pairs[i + 1];
            tree = tree.insert(Tuple.of(key, value));
        }
        return new TreeMap<>(tree);
    }

    private TreeMap<K, V> createFromEntries(Iterable<Tuple2<K, V>> tuples) {
        return createTreeMap((EntryComparator<K, V>) entries.comparator(), tuples);
    }

    private TreeMap<K, V> emptyInstance() {
        return isEmpty() ? this : new TreeMap<>(entries.emptyInstance());
    }

    @Override
    public Comparator<K> comparator() {
        return ((EntryComparator<K, V>) entries.comparator()).keyComparator();
    }

    // -- internal types

    private interface EntryComparator<K, V> extends Comparator<Tuple2<K, V>>, Serializable {

        long serialVersionUID = 1L;

        static <K, V> EntryComparator<K, V> of(Comparator<? super K> keyComparator) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            return new Specific<>(keyComparator);
        }

        static <K, V> EntryComparator<K, V> natural() {
            return Natural.instance();
        }

        Comparator<K> keyComparator();

        // -- internal impls

        final class Specific<K, V> implements EntryComparator<K, V> {

            private static final long serialVersionUID = 1L;

            private final Comparator<K> keyComparator;

            @SuppressWarnings("unchecked")
            Specific(Comparator<? super K> keyComparator) {
                this.keyComparator = (Comparator<K>) keyComparator;
            }

            @Override
            public int compare(Tuple2<K, V> e1, Tuple2<K, V> e2) {
                return keyComparator.compare(e1._1, e2._1);
            }

            @Override
            public Comparator<K> keyComparator() {
                return keyComparator;
            }
        }

        final class Natural<K, V> implements EntryComparator<K, V> {

            private static final long serialVersionUID = 1L;

            private static final Natural<?, ?> INSTANCE = new Natural<>();

            // hidden
            private Natural() {
            }

            @SuppressWarnings("unchecked")
            public static <K, V> Natural<K, V> instance() {
                return (Natural<K, V>) INSTANCE;
            }

            @SuppressWarnings("unchecked")
            @Override
            public int compare(Tuple2<K, V> e1, Tuple2<K, V> e2) {
                final K key1 = e1._1;
                final K key2 = e2._1;
                return ((Comparable<K>) key1).compareTo(key2);
            }

            @Override
            public Comparator<K> keyComparator() {
                return Comparators.naturalComparator();
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Natural;
            }

            @Override
            public int hashCode() {
                return 1;
            }

            /**
             * Instance control for object serialization.
             *
             * @return The singleton instance of NaturalEntryComparator.
             * @see java.io.Serializable
             */
            private Object readResolve() {
                return INSTANCE;
            }
        }
    }
}
