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
import io.vavr.collection.JavaConverters.ChangePolicy;
import io.vavr.collection.JavaConverters.ListView;
import io.vavr.control.Option;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * Internal class, containing helpers.
 */
final class Collections {

    // checks, if the *elements* of the given iterables are equal
    static boolean areEqual(Iterable<?> iterable1, Iterable<?> iterable2) {
        final java.util.Iterator<?> iter1 = iterable1.iterator();
        final java.util.Iterator<?> iter2 = iterable2.iterator();
        while (iter1.hasNext() && iter2.hasNext()) {
            if (!Objects.equals(iter1.next(), iter2.next())) {
                return false;
            }
        }
        return iter1.hasNext() == iter2.hasNext();
    }

    static <T, C extends Seq<T>> C asJava(C source, Consumer<? super java.util.List<T>> action, ChangePolicy changePolicy) {
        Objects.requireNonNull(action, "action is null");
        final ListView<T, C> view = JavaConverters.asJava(source, changePolicy);
        action.accept(view);
        return view.getDelegate();
    }

    @SuppressWarnings("unchecked")
    static <T, S extends Seq<T>> Iterator<S> crossProduct(S empty, S seq, int power) {
        if (power < 0) {
            return Iterator.empty();
        } else {
            return Iterator.range(0, power)
                    .foldLeft(Iterator.of(empty), (product, ignored) -> product.flatMap(el -> seq.map(t -> (S) el.append(t))));
        }
    }

    // DEV-NOTE: Use this method for non-infinite and direct-access collection only
    // because of O(N) complexity of get() and infinite loop in size()
    // see https://github.com/vavr-io/vavr/issues/2007
    @SuppressWarnings("unchecked")
    static <T, S extends IndexedSeq<T>> S dropRightUntil(S seq, Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        for (int i = seq.length() - 1; i >= 0; i--) {
            if (predicate.test(seq.get(i))) {
                return (S) seq.take(i + 1);
            }
        }
        return (S) seq.take(0);
    }

    // DEV-NOTE: Use this method for non-infinite and direct-access collection only
    // because of O(N) complexity of get() and infinite loop in size()
    // see https://github.com/vavr-io/vavr/issues/2007
    @SuppressWarnings("unchecked")
    static <T, S extends IndexedSeq<T>> S dropUntil(S seq, Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        for (int i = 0; i < seq.length(); i++) {
            if (predicate.test(seq.get(i))) {
                return (S) seq.drop(i);
            }
        }
        return (S) seq.take(0);
    }

    @SuppressWarnings("unchecked")
    static <K, V> boolean equals(Map<K, V> source, Object object) {
        if (source == object) {
            return true;
        } else if (source != null && object instanceof Map) {
            final Map<K, V> map = (Map<K, V>) object;
            if (source.size() != map.size()) {
                return false;
            } else {
                try {
                    return source.forAll(map::contains);
                } catch (ClassCastException e) {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    static <K, V> boolean equals(Multimap<K, V> source, Object object) {
        if (source == object) {
            return true;
        } else if (source != null && object instanceof Multimap) {
            final Multimap<K, V> multimap = (Multimap<K, V>) object;
            if (source.size() != multimap.size()) {
                return false;
            } else {
                try {
                    return source.forAll(multimap::contains);
                } catch (ClassCastException e) {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    static <V> boolean equals(Seq<V> source, Object object) {
        if (object == source) {
            return true;
        } else if (source != null && object instanceof Seq) {
            final Seq<V> seq = (Seq<V>) object;
            return seq.size() == source.size() && areEqual(source, seq);
        } else {
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    static <V> boolean equals(Set<V> source, Object object) {
        if (source == object) {
            return true;
        } else if (source != null && object instanceof Set) {
            final Set<V> set = (Set<V>) object;
            if (source.size() != set.size()) {
                return false;
            } else {
                try {
                    return source.forAll(set::contains);
                } catch (ClassCastException e) {
                    return false;
                }
            }
        } else {
            return false;
        }
    }

    static <T> Iterator<T> fill(int n, Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return tabulate(n, ignored -> supplier.get());
    }

    static <T, R extends java.util.Collection<T>> R toJavaCollection(
            Traversable<T> source, Function<Integer, R> containerSupplier) {
        return toJavaCollection(source, containerSupplier, 16);
    }

    static <T, R extends java.util.Collection<T>> R toJavaCollection(
            Traversable<T> source, Function<Integer, R> containerSupplier, int defaultInitialCapacity) {
        final int size = source.isTraversableAgain() && !source.isLazy() ? source.size() : defaultInitialCapacity;
        final R container = containerSupplier.apply(size);
        source.forEach(container::add);
        return container;
    }

    static <T, R> Collector<T, ArrayList<T>, R> toListAndThen(Function<ArrayList<T>, R> finisher) {
        final Supplier<ArrayList<T>> supplier = ArrayList::new;
        final BiConsumer<ArrayList<T>, T> accumulator = ArrayList::add;
        final BinaryOperator<ArrayList<T>> combiner = (left, right) -> {
            left.addAll(right);
            return left;
        };
        return Collector.of(supplier, accumulator, combiner, finisher);
    }

    static <T, K, V, E extends Tuple2<? extends K, ? extends V>, R extends Map<K, V>> R toMap(
            Traversable<T> source, R empty, Function<Iterable<E>, R> ofAll, Function<? super T, ? extends E> f) {
        if (source.isEmpty()) {
            return empty;
        } else {
            return ofAll.apply(Iterator.ofAll(source).map(f));
        }
    }

    static <T> Iterator<T> fillObject(int n, T element) {
        return n <= 0 ? Iterator.empty() : Iterator.continually(element).take(n);
    }

    static <C extends Traversable<T>, T> C fill(int n, Supplier<? extends T> s, C empty, Function<T[], C> of) {
        Objects.requireNonNull(s, "s is null");
        Objects.requireNonNull(empty, "empty is null");
        Objects.requireNonNull(of, "of is null");
        return tabulate(n, anything -> s.get(), empty, of);
    }

    static <C extends Traversable<T>, T> C fillObject(int n, T element, C empty, Function<T[], C> of) {
        Objects.requireNonNull(empty, "empty is null");
        Objects.requireNonNull(of, "of is null");
        if (n <= 0) {
            return empty;
        } else {
            @SuppressWarnings("unchecked")
            final T[] elements = (T[]) new Object[n];
            Arrays.fill(elements, element);
            return of.apply(elements);
        }
    }

    @SuppressWarnings("unchecked")
    static <C extends Traversable<T>, T> C filterNot(C source, Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (source.isEmpty()) {
            return source;
        } else {
            return (C) source.filter(predicate.negate());
        }
    }

    static <T, C, R extends Iterable<T>> Map<C, R> groupBy(Traversable<T> source, Function<? super T, ? extends C> classifier, Function<? super Iterable<T>, R> mapper) {
        Objects.requireNonNull(classifier, "classifier is null");
        Objects.requireNonNull(mapper, "mapper is null");
        Map<C, R> results = LinkedHashMap.empty();
        for (java.util.Map.Entry<? extends C, Collection<T>> entry : groupBy(source, classifier)) {
            results = results.put(entry.getKey(), mapper.apply(entry.getValue()));
        }
        return results;

    }

    private static <T, C> java.util.Set<java.util.Map.Entry<C, Collection<T>>> groupBy(Traversable<T> source, Function<? super T, ? extends C> classifier) {
        final java.util.Map<C, Collection<T>> results = new java.util.LinkedHashMap<>(source.isTraversableAgain() ? source.size() : 16);
        for (T value : source) {
            final C key = classifier.apply(value);
            results.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
        return results.entrySet();
    }

    // hashes the elements respecting their order
    static int hashOrdered(Iterable<?> iterable) {
        return hash(iterable, (acc, hash) -> acc * 31 + hash);
    }

    // hashes the elements regardless of their order
    static int hashUnordered(Iterable<?> iterable) {
        return hash(iterable, Integer::sum);
    }

    private static int hash(Iterable<?> iterable, IntBinaryOperator accumulator) {
        if (iterable == null) {
            return 0;
        } else {
            int hashCode = 1;
            for (Object o : iterable) {
                hashCode = accumulator.applyAsInt(hashCode, Objects.hashCode(o));
            }
            return hashCode;
        }
    }

    static Option<Integer> indexOption(int index) {
        return Option.when(index >= 0, index);
    }

    // @param iterable may not be null
    static boolean isEmpty(Iterable<?> iterable) {
        return iterable instanceof Traversable && ((Traversable<?>) iterable).isEmpty()
                || iterable instanceof Collection && ((Collection<?>) iterable).isEmpty()
                || !iterable.iterator().hasNext();
    }

    static <T> boolean isTraversableAgain(Iterable<? extends T> iterable) {
        return (iterable instanceof Collection) ||
                (iterable instanceof Traversable && ((Traversable<?>) iterable).isTraversableAgain());
    }

    static <T> T last(Traversable<T> source){
        if (source.isEmpty()) {
            throw new NoSuchElementException("last of empty " + source.stringPrefix());
        } else {
            final Iterator<T> it = source.iterator();
            T result = null;
            while (it.hasNext()) {
                result = it.next();
            }
            return result;
        }
    }

    @SuppressWarnings("unchecked")
    static <K, V, K2, U extends Map<K2, V>> U mapKeys(Map<K, V> source, U zero, Function<? super K, ? extends K2> keyMapper, BiFunction<? super V, ? super V, ? extends V> valueMerge) {
        Objects.requireNonNull(zero, "zero is null");
        Objects.requireNonNull(keyMapper, "keyMapper is null");
        Objects.requireNonNull(valueMerge, "valueMerge is null");
        return source.foldLeft(zero, (acc, entry) -> {
            final K2 k2 = keyMapper.apply(entry._1);
            final V v2 = entry._2;
            final Option<V> v1 = acc.get(k2);
            final V v = v1.isDefined() ? valueMerge.apply(v1.get(), v2) : v2;
            return (U) acc.put(k2, v);
        });
    }

    static <C extends Traversable<T>, T> Tuple2<C, C> partition(C collection, Function<Iterable<T>, C> creator,
                                                                Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final java.util.List<T> left = new java.util.ArrayList<>();
        final java.util.List<T> right = new java.util.ArrayList<>();
        for (T element : collection) {
            (predicate.test(element) ? left : right).add(element);
        }
        return Tuple.of(creator.apply(left), creator.apply(right));
    }

    @SuppressWarnings("unchecked")
    static <C extends Traversable<T>, T> C removeAll(C source, Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (source.isEmpty()) {
            return source;
        } else {
            final Set<T> removed = HashSet.ofAll(elements);
            return removed.isEmpty() ? source : (C) source.filter(e -> !removed.contains(e));
        }
    }

    @SuppressWarnings("unchecked")
    static <C extends Traversable<T>, T> C removeAll(C source, T element) {
        if (source.isEmpty()) {
            return source;
        } else {
            return (C) source.filter(e -> !Objects.equals(e, element));
        }
    }

    @SuppressWarnings("unchecked")
    static <C extends Traversable<T>, T> C retainAll(C source, Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (source.isEmpty()) {
            return source;
        } else {
            final Set<T> retained = HashSet.ofAll(elements);
            return (C) source.filter(retained::contains);
        }
    }

    static <T> Iterator<T> reverseIterator(Iterable<T> iterable) {
        if (iterable instanceof java.util.List) {
            return reverseListIterator((java.util.List<T>) iterable);
        } else if (iterable instanceof Seq) {
            return ((Seq<T>) iterable).reverseIterator();
        } else {
            return List.<T>empty().pushAll(iterable).iterator();
        }
    }

    private static <T> Iterator<T> reverseListIterator(java.util.List<T> list) {
        return new Iterator<T>() {
            private final java.util.ListIterator<T> delegate = list.listIterator(list.size());

            @Override
            public boolean hasNext() {
                return delegate.hasPrevious();
            }

            @Override
            public T next() {
                return delegate.previous();
            }
        };
    }

    @SuppressWarnings("unchecked")
    static <T, C extends Seq<T>> C rotateLeft(C source, int n) {
        if (source.isEmpty() || n == 0) {
            return source;
        } else if (n < 0) {
            return rotateRight(source, -n);
        } else {
            int len = source.length();
            int m = n % len;
            if (m == 0) {
                return source;
            } else {
                return (C) source.drop(m).appendAll(source.take(m));
            }
        }
    }

    @SuppressWarnings("unchecked")
    static <T, C extends Seq<T>> C rotateRight(C source, int n) {
        if (source.isEmpty() || n == 0) {
            return source;
        } else if (n < 0) {
            return rotateLeft(source, -n);
        } else {
            int len = source.length();
            int m = n % len;
            if (m == 0) {
                return source;
            } else {
                return (C) source.takeRight(m).appendAll(source.dropRight(m));
            }
        }
    }

    static <T, U, R extends Traversable<U>> R scanLeft(Traversable<? extends T> source,
                                                       U zero, BiFunction<? super U, ? super T, ? extends U> operation, Function<Iterator<U>, R> finisher) {
        Objects.requireNonNull(operation, "operation is null");
        final Iterator<U> iterator = source.iterator().scanLeft(zero, operation);
        return finisher.apply(iterator);
    }

    static <T, U, R extends Traversable<U>> R scanRight(Traversable<? extends T> source,
                                                        U zero, BiFunction<? super T, ? super U, ? extends U> operation, Function<Iterator<U>, R> finisher) {
        Objects.requireNonNull(operation, "operation is null");
        final Iterator<? extends T> reversedElements = reverseIterator(source);
        return scanLeft(reversedElements, zero, (u, t) -> operation.apply(t, u), us -> finisher.apply(reverseIterator(us)));
    }

    static <T, U, R extends Seq<T>> R sortBy(Seq<? extends T> source, Comparator<? super U> comparator, Function<? super T, ? extends U> mapper, Collector<T, ?, R> collector) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return source.toJavaStream()
                .sorted((e1, e2) -> comparator.compare(mapper.apply(e1), mapper.apply(e2)))
                .collect(collector);
    }

    static <T, S extends Seq<T>> S shuffle(S source, Function<? super Iterable<T>, S> ofAll) {
        if (source.length() <= 1) {
            return source;
        }

        final java.util.List<T> list = source.toJavaList();
        java.util.Collections.shuffle(list);
        return ofAll.apply(list);
    }

    static <T, S extends Seq<T>> S shuffle(S source, Random random, Function<? super Iterable<T>, S> ofAll) {
        if (source.length() <= 1) {
            return source;
        }

        final java.util.List<T> list = source.toJavaList();
        java.util.Collections.shuffle(list, random);
        return ofAll.apply(list);
    }

    static void subSequenceRangeCheck(int beginIndex, int endIndex, int length) {
        if (beginIndex < 0 || endIndex > length) {
            throw new IndexOutOfBoundsException("subSequence(" + beginIndex + ", " + endIndex + "), length = " + length);
        } else if (beginIndex > endIndex) {
            throw new IllegalArgumentException("subSequence(" + beginIndex + ", " + endIndex + ")");
        }
    }

    static <T> Iterator<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        if (n <= 0) {
            return Iterator.empty();
        } else {
            return new Iterator<T>() {

                int i = 0;

                @Override
                public boolean hasNext() {
                    return i < n;
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return f.apply(i++);
                }
            };
        }
    }

    static <C extends Traversable<T>, T> C tabulate(int n, Function<? super Integer, ? extends T> f, C empty, Function<T[], C> of) {
        Objects.requireNonNull(f, "f is null");
        Objects.requireNonNull(empty, "empty is null");
        Objects.requireNonNull(of, "of is null");
        if (n <= 0) {
            return empty;
        } else {
            @SuppressWarnings("unchecked")
            final T[] elements = (T[]) new Object[n];
            for (int i = 0; i < n; i++) {
                elements[i] = f.apply(i);
            }
            return of.apply(elements);
        }
    }

    // DEV-NOTE: Use this method for non-infinite and direct-access collection only
    // because of O(N) complexity of get() and infinite loop in size()
    // see https://github.com/vavr-io/vavr/issues/2007
    @SuppressWarnings("unchecked")
    static <T, S extends IndexedSeq<T>> S takeRightUntil(S seq, Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        for (int i = seq.length() - 1; i >= 0; i--) {
            if (predicate.test(seq.get(i))) {
                return (S) seq.drop(i + 1);
            }
        }
        return seq;
    }

    // DEV-NOTE: Use this method for non-infinite and direct-access collection only
    // because of O(N) complexity of get() and infinite loop in size()
    // see https://github.com/vavr-io/vavr/issues/2007
    @SuppressWarnings("unchecked")
    static <T, S extends IndexedSeq<T>> S takeUntil(S seq, Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        for (int i = 0; i < seq.length(); i++) {
            if (predicate.test(seq.get(i))) {
                return (S) seq.take(i);
            }
        }
        return seq;
    }

    static <T, U extends Seq<T>, V extends Seq<U>> V transpose(V matrix, Function<Iterable<U>, V> rowFactory, Function<T[], U> columnFactory) {
        Objects.requireNonNull(matrix, "matrix is null");
        if (matrix.isEmpty() || (matrix.length() == 1 && matrix.head().length() <= 1)) {
            return matrix;
        } else {
            return transposeNonEmptyMatrix(matrix, rowFactory, columnFactory);
        }
    }

    private static <T, U extends Seq<T>, V extends Seq<U>> V transposeNonEmptyMatrix(V matrix, Function<Iterable<U>, V> rowFactory, Function<T[], U> columnFactory) {
        final int newHeight = matrix.head().size(), newWidth = matrix.size();
        @SuppressWarnings("unchecked") final T[][] results = (T[][]) new Object[newHeight][newWidth];

        if (matrix.exists(r -> r.size() != newHeight)) {
            throw new IllegalArgumentException("the parameter `matrix` is invalid!");
        }

        int rowIndex = 0;
        for (U row : matrix) {
            int columnIndex = 0;
            for (T element : row) {
                results[columnIndex][rowIndex] = element;
                columnIndex++;
            }
            rowIndex++;
        }

        return rowFactory.apply(Iterator.of(results).map(columnFactory));
    }

    static <T> IterableWithSize<T> withSize(Iterable<? extends T> iterable) {
        return isTraversableAgain(iterable) ? withSizeTraversable(iterable) : withSizeTraversable(List.ofAll(iterable));
    }

    private static <T> IterableWithSize<T> withSizeTraversable(Iterable<? extends T> iterable) {
        if (iterable instanceof Collection) {
            return new IterableWithSize<>(iterable, ((Collection<?>) iterable).size());
        } else {
            return new IterableWithSize<>(iterable, ((Traversable<?>) iterable).size());
        }
    }

    static class IterableWithSize<T> {
        private final Iterable<? extends T> iterable;
        private final int size;

        IterableWithSize(Iterable<? extends T> iterable, int size) {
            this.iterable = iterable;
            this.size = size;
        }

        java.util.Iterator<? extends T> iterator() {
            return iterable.iterator();
        }

        java.util.Iterator<? extends T> reverseIterator() {
            return Collections.reverseIterator(iterable);
        }

        int size() {
            return size;
        }

        Object[] toArray() {
            if (iterable instanceof Collection<?>) {
                return ((Collection<? extends T>) iterable).toArray();
            } else {
                return ArrayType.asArray(iterator(), size());
            }
        }
    }

}
