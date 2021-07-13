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
import java.util.Collection;
import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * A {@link TreeMap}-based implementation of {@link Multimap}
 *
 * @param <K> Key type
 * @param <V> Value type
 * @deprecated marked for removal from vavr core lib, might be moved to an extended collections module
 */
@Deprecated
public final class TreeMultimap<K, V> extends AbstractMultimap<K, V, TreeMultimap<K, V>> implements Serializable, SortedMultimap<K, V> {

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
         * Returns the empty TreeMultimap. The underlying key comparator is the natural comparator of K.
         *
         * @param <K> The key type
         * @param <V2> The value type
         * @return A new empty TreeMultimap.
         */
        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> empty() {
            return empty(Comparators.naturalComparator());
        }

        /**
         * Returns the empty TreeMultimap using the given key comparator.
         *
         * @param <K>           The key type
         * @param <V2>          The value type
         * @param keyComparator The comparator used to sort the entries by their key.
         * @return A new empty TreeMultimap.
         */
        public <K, V2 extends V> TreeMultimap<K, V2> empty(Comparator<? super K> keyComparator) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            return new TreeMultimap<>(TreeMap.empty(keyComparator), containerType, emptyContainer);
        }

        /**
         * Creates a {@code TreeMultimap} of the given entries.
         * The underlying key comparator is the natural comparator of K.
         *
         * @param <K>     The key type
         * @param <V2>    The value type
         * @param entries Multimap entries
         * @return A new TreeMultimap containing the given entries.
         */
        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> ofEntries(Iterable<? extends Tuple2<? extends K, ? extends V2>> entries) {
            return ofEntries(Comparators.naturalComparator(), entries);
        }

        /**
         * Creates a {@code TreeMultimap} of the given entries.
         *
         * @param <K>           The key type
         * @param <V2>          The value type
         * @param keyComparator The comparator used to sort the entries by their key.
         * @param entries       Multimap entries
         * @return A new TreeMultimap containing the given entries.
         */
        public <K, V2 extends V> TreeMultimap<K, V2> ofEntries(Comparator<? super K> keyComparator, Iterable<? extends Tuple2<? extends K, ? extends V2>> entries) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            Objects.requireNonNull(entries, "entries is null");
            TreeMultimap<K, V2> result = empty(keyComparator);
            for (Tuple2<? extends K, ? extends V2> entry : entries) {
                result = result.put(entry._1, entry._2);
            }
            return result;
        }

        /**
         * Creates a {@code TreeMultimap} of the given entries.
         * The underlying key comparator is the natural comparator of K.
         *
         * @param <K>     The key type
         * @param <V2>    The value type
         * @param entries Multimap entries
         * @return A new TreeMultimap containing the given entries.
         */
        @SuppressWarnings({ "unchecked", "varargs" })
        @SafeVarargs
        public final <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> ofEntries(Tuple2<? extends K, ? extends V2>... entries) {
            return ofEntries(Comparators.naturalComparator(), entries);
        }

        /**
         * Creates a {@code TreeMultimap} of the given entries.
         *
         * @param <K>           The key type
         * @param <V2>          The value type
         * @param keyComparator The comparator used to sort the entries by their key.
         * @param entries       Multimap entries
         * @return A new TreeMultimap containing the given entries.
         */
        @SuppressWarnings({ "unchecked", "varargs" })
        @SafeVarargs
        public final <K, V2 extends V> TreeMultimap<K, V2> ofEntries(Comparator<? super K> keyComparator, Tuple2<? extends K, ? extends V2>... entries) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            Objects.requireNonNull(entries, "entries is null");
            TreeMultimap<K, V2> result = empty(keyComparator);
            for (Tuple2<? extends K, ? extends V2> entry : entries) {
                result = result.put(entry._1, entry._2);
            }
            return result;
        }

        /**
         * Creates a {@code TreeMultimap} of the given entries.
         * The underlying key comparator is the natural comparator of K.
         *
         * @param <K>     The key type
         * @param <V2>    The value type
         * @param entries Multimap entries
         * @return A new TreeMultimap containing the given entries.
         */
        @SuppressWarnings({ "unchecked", "varargs" })
        @SafeVarargs
        public final <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> ofEntries(java.util.Map.Entry<? extends K, ? extends V2>... entries) {
            return ofEntries(Comparators.naturalComparator(), entries);
        }

        /**
         * Creates a {@code TreeMultimap} of the given entries.
         *
         * @param <K>           The key type
         * @param <V2>          The value type
         * @param keyComparator The comparator used to sort the entries by their key.
         * @param entries       Multimap entries
         * @return A new TreeMultimap containing the given entries.
         */
        @SuppressWarnings({ "unchecked", "varargs" })
        @SafeVarargs
        public final <K, V2 extends V> TreeMultimap<K, V2> ofEntries(Comparator<? super K> keyComparator, java.util.Map.Entry<? extends K, ? extends V2>... entries) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            Objects.requireNonNull(entries, "entries is null");
            TreeMultimap<K, V2> result = empty(keyComparator);
            for (java.util.Map.Entry<? extends K, ? extends V2> entry : entries) {
                result = result.put(entry.getKey(), entry.getValue());
            }
            return result;
        }

        /**
         * Returns a {@code TreeMultimap}, from a source java.util.Map.
         *
         * @param map A map
         * @param keyComparator The comparator used to sort the entries by their key.
         * @param <K> The key type
         * @param <V2> The value type
         * @return A new Multimap containing the given map entries
         */
        public <K, V2 extends V> TreeMultimap<K, V2> ofAll(Comparator<? super K> keyComparator, java.util.Map<? extends K, ? extends V2> map) {
            return Multimaps.ofJavaMap(empty(keyComparator), map);
        }

        /**
         * Returns a {@code TreeMultimap}, from a source java.util.Map.
         *
         * @param map A map
         * @param <K> The key type
         * @param <V2> The value type
         * @return A new Multimap containing the given map entries
         */
        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> ofAll(java.util.Map<? extends K, ? extends V2> map) {
            return Multimaps.ofJavaMap(this.<K, V2>empty(), map);
        }

        /**
         * Returns a {@code TreeMultimap}, from entries mapped from stream.
         *
         * @param keyComparator The comparator used to sort the entries by their key.
         * @param stream      the source stream
         * @param keyMapper   the key mapper
         * @param valueMapper the value mapper
         * @param <T>         The stream element type
         * @param <K>         The key type
         * @param <V2>        The value type
         * @return A new Multimap
         */
        public <T, K, V2 extends V> TreeMultimap<K, V2> ofAll(Comparator<? super K> keyComparator,
                                                              java.util.stream.Stream<? extends T> stream,
                                                              Function<? super T, ? extends K> keyMapper,
                                                              Function<? super T, ? extends V2> valueMapper) {
            return Multimaps.ofStream(empty(keyComparator), stream, keyMapper, valueMapper);
        }

        /**
         * Returns a {@code TreeMultimap}, from entries mapped from stream.
         *
         * @param stream      the source stream
         * @param keyMapper   the key mapper
         * @param valueMapper the value mapper
         * @param <T>         The stream element type
         * @param <K>         The key type
         * @param <V2>        The value type
         * @return A new Multimap
         */
        public <T, K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> ofAll(java.util.stream.Stream<? extends T> stream,
                                                              Function<? super T, ? extends K> keyMapper,
                                                              Function<? super T, ? extends V2> valueMapper) {
            return Multimaps.ofStream(this.<K, V2>empty(), stream, keyMapper, valueMapper);
        }

        /**
         * Returns a {@code TreeMultimap}, from entries mapped from stream.
         *
         * @param keyComparator The comparator used to sort the entries by their key.
         * @param stream      the source stream
         * @param entryMapper the entry mapper
         * @param <T>         The stream element type
         * @param <K>         The key type
         * @param <V2>        The value type
         * @return A new Multimap
         */
        public <T, K, V2 extends V> TreeMultimap<K, V2> ofAll(Comparator<? super K> keyComparator,
                                                              java.util.stream.Stream<? extends T> stream,
                                                              Function<? super T, Tuple2<? extends K, ? extends V2>> entryMapper) {
            return Multimaps.ofStream(empty(keyComparator), stream, entryMapper);
        }

        /**
         * Returns a {@code TreeMultimap}, from entries mapped from stream.
         *
         * @param stream      the source stream
         * @param entryMapper the entry mapper
         * @param <T>         The stream element type
         * @param <K>         The key type
         * @param <V2>        The value type
         * @return A new Multimap
         */
        public <T, K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> ofAll(java.util.stream.Stream<? extends T> stream,
                                                              Function<? super T, Tuple2<? extends K, ? extends V2>> entryMapper) {
            return Multimaps.ofStream(empty(), stream, entryMapper);
        }

        /**
         * Returns a TreeMultimap containing {@code n} values of a given Function {@code f}
         * over a range of integer values from 0 to {@code n - 1}.
         * The underlying key comparator is the natural comparator of K.
         *
         * @param <K> The key type
         * @param <V2> The value type
         * @param n   The number of elements in the TreeMultimap
         * @param f   The Function computing element values
         * @return A TreeMultimap consisting of elements {@code f(0),f(1), ..., f(n - 1)}
         * @throws NullPointerException if {@code f} is null
         */
        @SuppressWarnings("unchecked")
        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> tabulate(int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V2>> f) {
            return tabulate(Comparators.naturalComparator(), n, f);
        }

        /**
         * Returns a TreeMultimap containing {@code n} values of a given Function {@code f}
         * over a range of integer values from 0 to {@code n - 1}.
         *
         * @param <K>           The key type
         * @param <V2>           The value type
         * @param keyComparator The comparator used to sort the entries by their key
         * @param n             The number of elements in the TreeMultimap
         * @param f             The Function computing element values
         * @return A TreeMultimap consisting of elements {@code f(0),f(1), ..., f(n - 1)}
         * @throws NullPointerException if {@code keyComparator} or {@code f} are null
         */
        @SuppressWarnings("unchecked")
        public <K, V2 extends V> TreeMultimap<K, V2> tabulate(Comparator<? super K> keyComparator, int n, Function<? super Integer, ? extends Tuple2<? extends K, ? extends V2>> f) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            Objects.requireNonNull(f, "f is null");
            return ofEntries(keyComparator, Collections.tabulate(n, (Function<? super Integer, ? extends Tuple2<K, V2>>) f));
        }

        /**
         * Returns a TreeMultimap containing {@code n} values supplied by a given Supplier {@code s}.
         * The underlying key comparator is the natural comparator of K.
         *
         * @param <K> The key type
         * @param <V2> The value type
         * @param n   The number of elements in the TreeMultimap
         * @param s   The Supplier computing element values
         * @return A TreeMultimap of size {@code n}, where each element contains the result supplied by {@code s}.
         * @throws NullPointerException if {@code s} is null
         */
        @SuppressWarnings("unchecked")
        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> fill(int n, Supplier<? extends Tuple2<? extends K, ? extends V2>> s) {
            return fill(Comparators.naturalComparator(), n, s);
        }

        /**
         * Returns a TreeMultimap containing {@code n} values supplied by a given Supplier {@code s}.
         *
         * @param <K>           The key type
         * @param <V2>           The value type
         * @param keyComparator The comparator used to sort the entries by their key
         * @param n             The number of elements in the TreeMultimap
         * @param s             The Supplier computing element values
         * @return A TreeMultimap of size {@code n}, where each element contains the result supplied by {@code s}.
         * @throws NullPointerException if {@code keyComparator} or {@code s} are null
         */
        @SuppressWarnings("unchecked")
        public <K, V2 extends V> TreeMultimap<K, V2> fill(Comparator<? super K> keyComparator, int n, Supplier<? extends Tuple2<? extends K, ? extends V2>> s) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            Objects.requireNonNull(s, "s is null");
            return ofEntries(keyComparator, Collections.fill(n, (Supplier<? extends Tuple2<K, V2>>) s));
        }

        /**
         * Returns a TreeMultimap containing {@code n} times the given {@code element}
         * The underlying key comparator is the natural comparator of K.
         *
         * @param <K>     The key type
         * @param <V2>    The value type
         * @param n       The number of elements in the TreeMultimap
         * @param element The element
         * @return A TreeMultimap of size {@code 1}, where each element contains {@code n} values of {@code element._2}.
         */
        @SuppressWarnings("unchecked")
        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> fill(int n, Tuple2<? extends K, ? extends V2> element) {
            return fill(Comparators.naturalComparator(), n, element);
        }

        /**
         * Returns a TreeMultimap containing {@code n} times the given {@code element}
         *
         * @param <K>           The key type
         * @param <V2>          The value type
         * @param keyComparator The comparator used to sort the entries by their key
         * @param n             The number of elements in the TreeMultimap
         * @param element       The element
         * @return A TreeMultimap of size {@code 1}, where each element contains {@code n} values of {@code element._2}.
         */
        @SuppressWarnings("unchecked")
        public <K, V2 extends V> TreeMultimap<K, V2> fill(Comparator<? super K> keyComparator, int n, Tuple2<? extends K, ? extends V2> element) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            return ofEntries(keyComparator, Collections.fillObject(n, element));
        }

        /**
         * Creates a TreeMultimap of the given key-value pair.
         *
         * @param key   A singleton map key.
         * @param value A singleton map value.
         * @param <K>   The key type
         * @param <V2>  The value type
         * @return A new Multimap containing the given entries
         */
        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K key, V2 value) {
            return of(Comparators.naturalComparator(), key, value);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
         *
         * @param k1   a key for the map
         * @param v1   the value for k1
         * @param k2   a key for the map
         * @param v2   the value for k2
         * @param <K>  The key type
         * @param <V2> The value type
         * @return A new Multimap containing the given entries
         */
        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2) {
            return of(Comparators.naturalComparator(), k1, v1, k2, v2);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
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
        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3) {
            return of(Comparators.naturalComparator(), k1, v1, k2, v2, k3, v3);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
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
        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4) {
            return of(Comparators.naturalComparator(), k1, v1, k2, v2, k3, v3, k4, v4);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
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
        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5) {
            return of(Comparators.naturalComparator(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
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
        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6) {
            return of(Comparators.naturalComparator(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
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
        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7) {
            return of(Comparators.naturalComparator(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
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
        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8) {
            return of(Comparators.naturalComparator(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
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
        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8, K k9, V2 v9) {
            return of(Comparators.naturalComparator(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
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
        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8, K k9, V2 v9, K k10, V2 v10) {
            return of(Comparators.naturalComparator(), k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9, k10, v10);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
         *
         * @param <K>   The key type
         * @param <V2>  The value type
         * @param entry The key-value pair used to form a new TreeMultimap.
         * @return A new Multimap containing the given entry
         */
        public <K extends Comparable<? super K>, V2 extends V> TreeMultimap<K, V2> of(Tuple2<? extends K, ? extends V2> entry) {
            return of(Comparators.naturalComparator(), entry);
        }

        /**
         * Creates a TreeMultimap of the given key-value pair.
         *
         * @param keyComparator The comparator used to sort the entries by their key.
         * @param key           A singleton map key.
         * @param value         A singleton map value.
         * @param <K>           The key type
         * @param <V2>          The value type
         * @return A new Multimap containing the given entry
         */
        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K key, V2 value) {
            final TreeMultimap<K, V2> e = empty(keyComparator);
            return e.put(key, value);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
         *
         * @param k1            a key for the map
         * @param v1            the value for k1
         * @param k2            a key for the map
         * @param v2            the value for k2
         * @param <K>           The key type
         * @param <V2>          The value type
         * @param keyComparator The comparator used to sort the entries by their key.
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2) {
            return of(keyComparator, k1, v1).put(k2, v2);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
         *
         * @param k1            a key for the map
         * @param v1            the value for k1
         * @param k2            a key for the map
         * @param v2            the value for k2
         * @param k3            a key for the map
         * @param v3            the value for k3
         * @param <K>           The key type
         * @param <V2>          The value type
         * @param keyComparator The comparator used to sort the entries by their key.
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2, K k3, V2 v3) {
            return of(keyComparator, k1, v1, k2, v2).put(k3, v3);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
         *
         * @param k1            a key for the map
         * @param v1            the value for k1
         * @param k2            a key for the map
         * @param v2            the value for k2
         * @param k3            a key for the map
         * @param v3            the value for k3
         * @param k4            a key for the map
         * @param v4            the value for k4
         * @param <K>           The key type
         * @param <V2>          The value type
         * @param keyComparator The comparator used to sort the entries by their key.
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4) {
            return of(keyComparator, k1, v1, k2, v2, k3, v3).put(k4, v4);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
         *
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
         * @param <V2>          The value type
         * @param keyComparator The comparator used to sort the entries by their key.
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5) {
            return of(keyComparator, k1, v1, k2, v2, k3, v3, k4, v4).put(k5, v5);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
         *
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
         * @param <V2>          The value type
         * @param keyComparator The comparator used to sort the entries by their key.
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6) {
            return of(keyComparator, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5).put(k6, v6);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
         *
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
         * @param <V2>          The value type
         * @param keyComparator The comparator used to sort the entries by their key.
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7) {
            return of(keyComparator, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6).put(k7, v7);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
         *
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
         * @param <V2>          The value type
         * @param keyComparator The comparator used to sort the entries by their key.
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8) {
            return of(keyComparator, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7).put(k8, v8);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
         *
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
         * @param <V2>          The value type
         * @param keyComparator The comparator used to sort the entries by their key.
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8, K k9, V2 v9) {
            return of(keyComparator, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8).put(k9, v9);
        }

        /**
         * Creates a TreeMultimap of the given list of key-value pairs.
         *
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
         * @param <V2>          The value type
         * @param keyComparator The comparator used to sort the entries by their key.
         * @return A new Multimap containing the given entries
         */
        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, K k1, V2 v1, K k2, V2 v2, K k3, V2 v3, K k4, V2 v4, K k5, V2 v5, K k6, V2 v6, K k7, V2 v7, K k8, V2 v8, K k9, V2 v9, K k10, V2 v10) {
            return of(keyComparator, k1, v1, k2, v2, k3, v3, k4, v4, k5, v5, k6, v6, k7, v7, k8, v8, k9, v9).put(k10, v10);
        }

        /**
         * Returns a singleton {@code TreeMultimap}, i.e. a {@code TreeMultimap} of one entry using a specific key comparator.
         *
         * @param <K>           The key type
         * @param <V2>          The value type
         * @param keyComparator The comparator used to sort the entries by their key.
         * @param entry         A map entry.
         * @return A new TreeMultimap containing the given entry.
         */
        public <K, V2 extends V> TreeMultimap<K, V2> of(Comparator<? super K> keyComparator, Tuple2<? extends K, ? extends V2> entry) {
            final TreeMultimap<K, V2> e = empty(keyComparator);
            return e.put(entry._1, entry._2);
        }

        /**
         * Returns a {@link Collector} which may be used in conjunction with
         * {@link java.util.stream.Stream#collect(Collector)} to obtain a
         * {@link TreeMultimap}.
         * <p>
         * The natural comparator is used to compare TreeMultimap keys.
         *
         * @param <K> The key type
         * @param <V2> The value type
         * @return A {@link TreeMultimap} Collector.
         */
        public <K extends Comparable<? super K>, V2 extends V> Collector<Tuple2<K, V2>, ArrayList<Tuple2<K, V2>>, TreeMultimap<K, V2>> collector() {
            return collector(Comparators.naturalComparator());
        }

        /**
         * Returns a {@link Collector} which may be used in conjunction with
         * {@link java.util.stream.Stream#collect(Collector)} to obtain a
         * {@link TreeMultimap}.
         *
         * @param <K>           The key type
         * @param <V2>           The value type
         * @param keyComparator The comparator used to sort the entries by their key.
         * @return A {@link TreeMultimap} Collector.
         */
        public <K, V2 extends V> Collector<Tuple2<K, V2>, ArrayList<Tuple2<K, V2>>, TreeMultimap<K, V2>> collector(Comparator<? super K> keyComparator) {
            Objects.requireNonNull(keyComparator, "keyComparator is null");
            return Collections.toListAndThen(list -> ofEntries(keyComparator, list));
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
    public static <K, V> TreeMultimap<K, V> narrow(TreeMultimap<? extends K, ? extends V> map) {
        return (TreeMultimap<K, V>) map;
    }

    private TreeMultimap(Map<K, Traversable<V>> back, ContainerType containerType, SerializableSupplier<Traversable<?>> emptyContainer) {
        super(back, containerType, emptyContainer);
    }

    @Override
    protected <K2, V2> Map<K2, V2> emptyMapSupplier() {
        return TreeMap.empty(Comparators.naturalComparator());
    }

    @SuppressWarnings("unchecked")
    @Override
    protected <K2, V2> TreeMultimap<K2, V2> emptyInstance() {
        return new TreeMultimap<>(emptyMapSupplier(), getContainerType(), emptyContainer);
    }

    @Override
    protected <K2, V2> TreeMultimap<K2, V2> createFromMap(Map<K2, Traversable<V2>> back) {
        return new TreeMultimap<>(back, getContainerType(), emptyContainer);
    }

    @Override
    public Comparator<K> comparator() {
        return ((SortedMap<K, Traversable<V>>) back).comparator();
    }

    @Override
    public SortedSet<K> keySet() {
        return ((SortedMap<K, Traversable<V>>) back).keySet();
    }

    @Override
    public java.util.SortedMap<K, Collection<V>> toJavaMap() {
        return toJavaMap(new java.util.TreeMap<>());
    }

    @Override
    public String stringPrefix() {
        return "TreeMultimap";
    }
}
