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

import io.vavr.Tuple2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A {@link LinkedHashMap}-based implementation of {@link Multimap}
 *
 * @param <K> Key type
 * @param <V> Value type
 * @deprecated marked for removal from vavr core lib, might be moved to an extended collections module
 */
@Deprecated
public final class LinkedHashMultimap<K, V> extends AbstractMultimap<K, V, LinkedHashMultimap<K, V>> implements Serializable {

    private static final long serialVersionUID = 1L;

    public static <V> Builder<V> withSeq() {
        return new Builder<>(ContainerType.SEQ, List::empty);
    }

    public static <V> Builder<V> withSet() {
        return new Builder<>(ContainerType.SET, HashSet::empty);
    }

    public static <V extends Comparable<?>> Builder<V> withSortedSet() {
        return new Builder<>(ContainerType.SORTED_SET, TreeSet::empty);
    }

    public static <V> Builder<V> withSortedSet(Comparator<? super V> comparator) {
        return new Builder<>(ContainerType.SORTED_SET, () -> TreeSet.empty(comparator));
    }

    public static class Builder<V> {

        private final ContainerType containerType;
        private final SerializableSupplier<Traversable<?>> emptyContainer;

        private Builder(ContainerType containerType, SerializableSupplier<Traversable<?>> emptyContainer) {
            this.containerType = containerType;
            this.emptyContainer = emptyContainer;
        }

        /**
         * Returns the empty {@code LinkedHashMultimap}.
         *
         * @param <K> The key type
         * @param <V2> The value type
         * @return A new empty LinkedHashMultimap.
         */
        public <K, V2 extends V> LinkedHashMultimap<K, V2> empty() {
            return new LinkedHashMultimap<>(LinkedHashMap.empty(), containerType, emptyContainer);
        }

        /**
         * Creates a {@code LinkedHashMultimap} of the given entries.
         *
         * @param <K>           The key type
         * @param <V2>          The value type
         * @param entries       Multimap entries
         * @return A new LinkedHashMultimap containing the given entries.
         */
        public <K, V2 extends V> LinkedHashMultimap<K, V2> ofEntries(Iterable<? extends Tuple2<? extends K, ? extends V2>> entries) {
            Objects.requireNonNull(entries, "entries is null");
            LinkedHashMultimap<K, V2> result = empty();
            for (Tuple2<? extends K, ? extends V2> entry : entries) {
                result = result.put(entry._1, entry._2);
            }
            return result;
        }

        /**
         * Creates a {@code LinkedHashMultimap} of the given entries.
         *
         * @param <K>           The key type
         * @param <V2>          The value type
         * @param entries       Multimap entries
         * @return A new LinkedHashMultimap containing the given entries.
         */
        @SafeVarargs
        public final <K, V2 extends V> LinkedHashMultimap<K, V2> ofEntries(Tuple2<? extends K, ? extends V2>... entries) {
            Objects.requireNonNull(entries, "entries is null");
            LinkedHashMultimap<K, V2> result = empty();
            for (Tuple2<? extends K, ? extends V2> entry : entries) {
                result = result.put(entry._1, entry._2);
            }
            return result;
        }

        /**
         * Creates a {@code LinkedHashMultimap} of the given entries.
         *
         * @param <K>           The key type
         * @param <V2>          The value type
         * @param entries       Multimap entries
         * @return A new LinkedHashMultimap containing the given entries.
         */
        @SafeVarargs
        public final <K, V2 extends V> LinkedHashMultimap<K, V2> ofEntries(java.util.Map.Entry<? extends K, ? extends V2>... entries) {
            Objects.requireNonNull(entries, "entries is null");
            LinkedHashMultimap<K, V2> result = empty();
            for (java.util.Map.Entry<? extends K, ? extends V2> entry : entries) {
                result = result.put(entry.getKey(), entry.getValue());
            }
            return result;
        }

        /**
         * Returns a {@code LinkedHashMultimap}, from a source java.util.Map.
         *
         * @param map A map
         * @param <K> The key type
         * @param <V2> The value type
         * @return A new Multimap containing the given map entries
         */
        public <K, V2 extends V> LinkedHashMultimap<K, V2> ofAll(java.util.Map<? extends K, ? extends V2> map) {
            return Multimaps.ofJavaMap(empty(), map);
        }

        /**
         * Returns a {@code LinkedHashMultimap}, from entries mapped from stream.
         *
         * @param stream      the source stream
         * @param keyMapper   the key mapper
         * @param valueMapper the value mapper
         * @param <T>         The stream element type
         * @param <K>         The key type
         * @param <V2>        The value type
         * @return A new Multimap
         */
        public <T, K, V2 extends V> LinkedHashMultimap<K, V2> ofAll(java.util.stream.Stream<? extends T> stream,
                                                              Function<? super T, ? extends K> keyMapper,
                                                              Function<? super T, ? extends V2> valueMapper) {
            return Multimaps.ofStream(empty(), stream, keyMapper, valueMapper);
        }

        /**
         * Returns a {@code LinkedHashMultimap}, from entries mapped from stream.
         *
         * @param stream      the source stream
         * @param entryMapper the entry mapper
         * @param <T>         The stream element type
         * @param <K>         The key type
         * @param <V2>        The value type
         * @return A new Multimap
         */
        public <T, K, V2 extends V> LinkedHashMultimap<K, V2> ofAll(java.util.stream.Stream<? extends T> stream,
                                                             Function<? super T, Tuple2<? extends K, ? extends V2>> entryMapper) {
            return Multimaps.ofStream(empty(), stream, entryMapper);
        }

        /**
         * Returns a LinkedHashMultimap containing {@code n} values of a given Function {@code f}
         * over a range of integer values from 0 to {@code n - 1}.
         *
         * @param <K> The key type
         * @param <V2> The value type
         * @param n   The number of elements in the LinkedHashMultimap
         * @param f   The Function computing element values
         * @return A LinkedHashMultimap consisting of elements {@code f(0),f(1), ..., f(n - 1)}
         * @throws NullPointerException if {@code f} is null
         */
        @SuppressWarnings("unchecked")
        public <K, V2 extends V> LinkedHashMultimap<K, V2> tabulate(int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V2>> f) {
            Objects.requireNonNull(f, "f is null");
            return ofEntries(Collections.tabulate(n, (Function<? super Integer, ? extends Tuple2<K, V2>>) f));
        }

        /**
         * Returns a LinkedHashMultimap containing {@code n} values supplied by a given Supplier {@code s}.
         *
         * @param <K> The key type
         * @param <V2> The value type
         * @param n   The number of elements in the LinkedHashMultimap
         * @param s   The Supplier computing element values
         * @return A LinkedHashMultimap of size {@code n}, where each element contains the result supplied by {@code s}.
         * @throws NullPointerException if {@code s} is null
         */
        @SuppressWarnings("unchecked")
        public <K, V2 extends V> LinkedHashMultimap<K, V2> fill(int n, Supplier<? extends Tuple2<? extends K, ? extends V2>> s) {
            Objects.requireNonNull(s, "s is null");
            return ofEntries(Collections.fill(n, (Supplier<? extends Tuple2<K, V2>>) s));
        }

        /**
         * Returns a LinkedHashMultimap containing {@code n} times the given {@code element}
         *
         * @param <K>     The key type
         * @param <V2>    The value type
         * @param n       The number of elements in the LinkedHashMultimap
         * @param element The element
         * @return A LinkedHashMultimap of size {@code 1}, where each element contains {@code n} values of {@code element._2}.
         */
        @SuppressWarnings("unchecked")
        public <K, V2 extends V> LinkedHashMultimap<K, V2> fill(int n, Tuple2<? extends K, ? extends V2> element) {
            return ofEntries(Collections.fillObject(n, element));
        }

        /**
         * Creates a LinkedHashMultimap of the given key-value pair.
         *
         * @param key   A singleton map key.
         * @param value A singleton map value.
         * @param <K>   The key type
         * @param <V2>  The value type
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K key, V2 value) {
            final LinkedHashMultimap<K, V2> e = empty();
            return e.put(key, value);
        }

        /**
         * Creates a LinkedHashMultimap of the given list of key-value pairs.
         *
         * @param k1   a key for the map
         * @param v1   the value for k1
         * @param k2   a key for the map
         * @param v2   the value for k2
         * @param <K>  The key type
         * @param <V2> The value type
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2) {
            return of(k1, v1).put(k2, v2);
        }

        /**
         * Creates a LinkedHashMultimap of the given list of key-value pairs.
         *
         * @param k1   a key for the map
         * @param v1   the value for k1
         * @param k2   a key for the map
         * @param v2   the value for k2
         * @param k3   a key for the map
         * @param v3   the value for k3
         * @param <K>  The key type
         * @param <V2> The value type
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3) {
            return of(k1, v1, k2, v2).put(k3, v3);
        }

        /**
         * Creates a LinkedHashMultimap of the given list of key-value pairs.
         *
         * @param k1   a key for the map
         * @param v1   the value for k1
         * @param k2   a key for the map
         * @param v2   the value for k2
         * @param k3   a key for the map
         * @param v3   the value for k3
         * @param k4   a key for the map
         * @param v4   the value for k4
         * @param <K>  The key type
         * @param <V2> The value type
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4) {
            return of(k1, v1, k2, v2, k3, v3).put(k4, v4);
        }

        /**
         * Creates a LinkedHashMultimap of the given list of key-value pairs.
         *
         * @param k1   a key for the map
         * @param v1   the value for k1
         * @param k2   a key for the map
         * @param v2   the value for k2
         * @param k3   a key for the map
         * @param v3   the value for k3
         * @param k4   a key for the map
         * @param v4   the value for k4
         * @param k5   a key for the map
         * @param v5   the value for k5
         * @param <K>  The key type
         * @param <V2> The value type
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4).put(k5, v5);
        }

        /**
         * Creates a LinkedHashMultimap of the given list of key-value pairs.
         *
         * @param k1   a key for the map
         * @param v1   the value for k1
         * @param k2   a key for the map
         * @param v2   the value for k2
         * @param k3   a key for the map
         * @param v3   the value for k3
         * @param k4   a key for the map
         * @param v4   the value for k4
         * @param k5   a key for the map
         * @param v5   the value for k5
         * @param k6   a key for the map
         * @param v6   the value for k6
         * @param <K>  The key type
         * @param <V2> The value type
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5).put(k6, v6);
        }

        /**
         * Creates a LinkedHashMultimap of the given list of key-value pairs.
         *
         * @param k1   a key for the map
         * @param v1   the value for k1
         * @param k2   a key for the map
         * @param v2   the value for k2
         * @param k3   a key for the map
         * @param v3   the value for k3
         * @param k4   a key for the map
         * @param v4   the value for k4
         * @param k5   a key for the map
         * @param v5   the value for k5
         * @param k6   a key for the map
         * @param v6   the value for k6
         * @param k7   a key for the map
         * @param v7   the value for k7
         * @param <K>  The key type
         * @param <V2> The value type
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6).put(k7, v7);
        }

        /**
         * Creates a LinkedHashMultimap of the given list of key-value pairs.
         *
         * @param k1   a key for the map
         * @param v1   the value for k1
         * @param k2   a key for the map
         * @param v2   the value for k2
         * @param k3   a key for the map
         * @param v3   the value for k3
         * @param k4   a key for the map
         * @param v4   the value for k4
         * @param k5   a key for the map
         * @param v5   the value for k5
         * @param k6   a key for the map
         * @param v6   the value for k6
         * @param k7   a key for the map
         * @param v7   the value for k7
         * @param k8   a key for the map
         * @param v8   the value for k8
         * @param <K>  The key type
         * @param <V2> The value type
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7).put(k8, v8);
        }

        /**
         * Creates a LinkedHashMultimap of the given list of key-value pairs.
         *
         * @param k1   a key for the map
         * @param v1   the value for k1
         * @param k2   a key for the map
         * @param v2   the value for k2
         * @param k3   a key for the map
         * @param v3   the value for k3
         * @param k4   a key for the map
         * @param v4   the value for k4
         * @param k5   a key for the map
         * @param v5   the value for k5
         * @param k6   a key for the map
         * @param v6   the value for k6
         * @param k7   a key for the map
         * @param v7   the value for k7
         * @param k8   a key for the map
         * @param v8   the value for k8
         * @param k9   a key for the map
         * @param v9   the value for k9
         * @param <K>  The key type
         * @param <V2> The value type
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8, K k9, V2 v9) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8).put(k9, v9);
        }

        /**
         * Creates a LinkedHashMultimap of the given list of key-value pairs.
         *
         * @param k1   a key for the map
         * @param v1   the value for k1
         * @param k2   a key for the map
         * @param v2   the value for k2
         * @param k3   a key for the map
         * @param v3   the value for k3
         * @param k4   a key for the map
         * @param v4   the value for k4
         * @param k5   a key for the map
         * @param v5   the value for k5
         * @param k6   a key for the map
         * @param v6   the value for k6
         * @param k7   a key for the map
         * @param v7   the value for k7
         * @param k8   a key for the map
         * @param v8   the value for k8
         * @param k9   a key for the map
         * @param v9   the value for k9
         * @param k10  a key for the map
         * @param v10  the value for k10
         * @param <K>  The key type
         * @param <V2> The value type
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8, K k9, V2 v9, K k10, V2 v10) {
            return of(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9).put(k10, v10);
        }

        /**
         * Creates a LinkedHashMultimap of the given key-value pair.
         * Returns a singleton {@code LinkedHashMultimap}, i.e. a {@code LinkedHashMultimap} of one entry.
         *
         * @param <K>   The key type
         * @param <V2>   The value type
         * @param entry A tuple containing the key-value pair.
         * @return A new LinkedHashMultimap containing the given entry.
         */
        public <K, V2 extends V> LinkedHashMultimap<K, V2> of(Tuple2<? extends K, ? extends V2> entry) {
            final LinkedHashMultimap<K, V2> e = empty();
            return e.put(entry._1, entry._2);
        }

        /**
         * Returns a {@link Collector} which may be used in conjunction with
         * {@link java.util.stream.Stream#collect(Collector)} to obtain a
         * {@link LinkedHashMultimap}.
         *
         * @param <K> The key type
         * @param <V2> The value type
         * @return A {@link LinkedHashMultimap} Collector.
         */
        public <K, V2 extends V> Collector<Tuple2<K, V2>, ArrayList<Tuple2<K, V2>>, Multimap<K, V2>> collector() {
            return Collections.toListAndThen(this::ofEntries);
        }
    }

    /**
     * Narrows a widened {@code HashMultimap<? extends K, ? extends V>} to {@code HashMultimap<K, V>}
     * by performing a type safe-cast. This is eligible because immutable/read-only
     * collections are covariant.
     *
     * @param map A {@code Map}.
     * @param <K> Key type
     * @param <V> Value type
     * @return the given {@code multimap} instance as narrowed type {@code Multimap<K, V>}.
     */
    @SuppressWarnings("unchecked")
    public static <K, V> LinkedHashMultimap<K, V> narrow(LinkedHashMultimap<? extends K, ? extends V> map) {
        return (LinkedHashMultimap<K, V>) map;
    }

    private LinkedHashMultimap(Map<K, Traversable<V>> back, ContainerType containerType, SerializableSupplier<Traversable<?>> emptyContainer) {
        super(back, containerType, emptyContainer);
    }

    @Override
    protected <K2, V2> Map<K2, V2> emptyMapSupplier() {
        return LinkedHashMap.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <K2, V2> LinkedHashMultimap<K2, V2> emptyInstance() {
        return new LinkedHashMultimap<>(LinkedHashMap.empty(), getContainerType(), emptyContainer);
    }

    @Override
    protected <K2, V2> LinkedHashMultimap<K2, V2> createFromMap(Map<K2, Traversable<V2>> back) {
        return new LinkedHashMultimap<>(back, getContainerType(), emptyContainer);
    }

    @Override
    public boolean isSequential() {
        return true;
    }

    @Override
    public String stringPrefix() {
        return "LinkedHashMultimap";
    }
}
