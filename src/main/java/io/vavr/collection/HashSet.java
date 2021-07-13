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

import java.io.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * An immutable {@code HashSet} implementation.
 *
 * @param <T> Component type
 */
@SuppressWarnings("deprecation")
public final class HashSet<T> implements Set<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private static final HashSet<?> EMPTY = new HashSet<>(HashArrayMappedTrie.empty());

    private final HashArrayMappedTrie<T, T> tree;

    private HashSet(HashArrayMappedTrie<T, T> tree) {
        this.tree = tree;
    }

    @SuppressWarnings("unchecked")
    public static <T> HashSet<T> empty() {
        return (HashSet<T>) EMPTY;
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link HashSet}.
     *
     * @param <T> Component type of the HashSet.
     * @return A io.vavr.collection.HashSet Collector.
     */
    public static <T> Collector<T, ArrayList<T>, HashSet<T>> collector() {
        return Collections.toListAndThen(HashSet::ofAll);
    }

    /**
     * Narrows a widened {@code HashSet<? extends T>} to {@code HashSet<T>}
     * by performing a type-safe cast. This is eligible because immutable/read-only
     * collections are covariant.
     *
     * @param hashSet A {@code HashSet}.
     * @param <T>     Component type of the {@code HashSet}.
     * @return the given {@code hashSet} instance as narrowed type {@code HashSet<T>}.
     */
    @SuppressWarnings("unchecked")
    public static <T> HashSet<T> narrow(HashSet<? extends T> hashSet) {
        return (HashSet<T>) hashSet;
    }

    /**
     * Returns a singleton {@code HashSet}, i.e. a {@code HashSet} of one element.
     *
     * @param element An element.
     * @param <T>     The component type
     * @return A new HashSet instance containing the given element
     */
    public static <T> HashSet<T> of(T element) {
        return HashSet.<T> empty().add(element);
    }

    /**
     * Creates a HashSet of the given elements.
     *
     * <pre><code>HashSet.of(1, 2, 3, 4)</code></pre>
     *
     * @param <T>      Component type of the HashSet.
     * @param elements Zero or more elements.
     * @return A set containing the given elements.
     * @throws NullPointerException if {@code elements} is null
     */
    @SafeVarargs
    public static <T> HashSet<T> of(T... elements) {
        Objects.requireNonNull(elements, "elements is null");
        HashArrayMappedTrie<T, T> tree = HashArrayMappedTrie.empty();
        for (T element : elements) {
            tree = tree.put(element, element);
        }
        return tree.isEmpty() ? empty() : new HashSet<>(tree);
    }

    /**
     * Returns an HashSet containing {@code n} values of a given Function {@code f}
     * over a range of integer values from 0 to {@code n - 1}.
     *
     * @param <T> Component type of the HashSet
     * @param n   The number of elements in the HashSet
     * @param f   The Function computing element values
     * @return An HashSet consisting of elements {@code f(0),f(1), ..., f(n - 1)}
     * @throws NullPointerException if {@code f} is null
     */
    public static <T> HashSet<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return Collections.tabulate(n, f, HashSet.empty(), HashSet::of);
    }

    /**
     * Returns a HashSet containing tuples returned by {@code n} calls to a given Supplier {@code s}.
     *
     * @param <T> Component type of the HashSet
     * @param n   The number of elements in the HashSet
     * @param s   The Supplier computing element values
     * @return An HashSet of size {@code n}, where each element contains the result supplied by {@code s}.
     * @throws NullPointerException if {@code s} is null
     */
    public static <T> HashSet<T> fill(int n, Supplier<? extends T> s) {
        Objects.requireNonNull(s, "s is null");
        return Collections.fill(n, s, HashSet.empty(), HashSet::of);
    }

    /**
     * Creates a HashSet of the given elements.
     *
     * @param elements Set elements
     * @param <T>      The value type
     * @return A new HashSet containing the given entries
     */
    @SuppressWarnings("unchecked")
    public static <T> HashSet<T> ofAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (elements instanceof HashSet) {
            return (HashSet<T>) elements;
        } else {
            final HashArrayMappedTrie<T, T> tree = addAll(HashArrayMappedTrie.empty(), elements);
            return tree.isEmpty() ? empty() : new HashSet<>(tree);
        }
    }

    /**
     * Creates a HashSet that contains the elements of the given {@link java.util.stream.Stream}.
     *
     * @param javaStream A {@link java.util.stream.Stream}
     * @param <T>        Component type of the Stream.
     * @return A HashSet containing the given elements in the same order.
     */
    public static <T> HashSet<T> ofAll(java.util.stream.Stream<? extends T> javaStream) {
        Objects.requireNonNull(javaStream, "javaStream is null");
        return HashSet.ofAll(Iterator.ofAll(javaStream.iterator()));
    }

    /**
     * Creates a HashSet from boolean values.
     *
     * @param elements boolean values
     * @return A new HashSet of Boolean values
     * @throws NullPointerException if elements is null
     */
    public static HashSet<Boolean> ofAll(boolean... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return HashSet.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a HashSet from byte values.
     *
     * @param elements byte values
     * @return A new HashSet of Byte values
     * @throws NullPointerException if elements is null
     */
    public static HashSet<Byte> ofAll(byte... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return HashSet.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a HashSet from char values.
     *
     * @param elements char values
     * @return A new HashSet of Character values
     * @throws NullPointerException if elements is null
     */
    public static HashSet<Character> ofAll(char... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return HashSet.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a HashSet from double values.
     *
     * @param elements double values
     * @return A new HashSet of Double values
     * @throws NullPointerException if elements is null
     */
    public static HashSet<Double> ofAll(double... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return HashSet.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a HashSet from float values.
     *
     * @param elements float values
     * @return A new HashSet of Float values
     * @throws NullPointerException if elements is null
     */
    public static HashSet<Float> ofAll(float... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return HashSet.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a HashSet from int values.
     *
     * @param elements int values
     * @return A new HashSet of Integer values
     * @throws NullPointerException if elements is null
     */
    public static HashSet<Integer> ofAll(int... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return HashSet.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a HashSet from long values.
     *
     * @param elements long values
     * @return A new HashSet of Long values
     * @throws NullPointerException if elements is null
     */
    public static HashSet<Long> ofAll(long... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return HashSet.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a HashSet from short values.
     *
     * @param elements short values
     * @return A new HashSet of Short values
     * @throws NullPointerException if elements is null
     */
    public static HashSet<Short> ofAll(short... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return HashSet.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a HashSet of int numbers starting from {@code from}, extending to {@code toExclusive - 1}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * HashSet.range(0, 0)  // = HashSet()
     * HashSet.range(2, 0)  // = HashSet()
     * HashSet.range(-2, 2) // = HashSet(-2, -1, 0, 1)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1
     * @return a range of int values as specified or the empty range if {@code from >= toExclusive}
     */
    public static HashSet<Integer> range(int from, int toExclusive) {
        return HashSet.ofAll(Iterator.range(from, toExclusive));
    }

    public static HashSet<Character> range(char from, char toExclusive) {
        return HashSet.ofAll(Iterator.range(from, toExclusive));
    }

    /**
     * Creates a HashSet of int numbers starting from {@code from}, extending to {@code toExclusive - 1},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * HashSet.rangeBy(1, 3, 1)  // = HashSet(1, 2)
     * HashSet.rangeBy(1, 4, 2)  // = HashSet(1, 3)
     * HashSet.rangeBy(4, 1, -2) // = HashSet(4, 2)
     * HashSet.rangeBy(4, 1, 2)  // = HashSet()
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1
     * @param step        the step
     * @return a range of long values as specified or the empty range if<br>
     * {@code from >= toInclusive} and {@code step > 0} or<br>
     * {@code from <= toInclusive} and {@code step < 0}
     * @throws IllegalArgumentException if {@code step} is zero
     */
    public static HashSet<Integer> rangeBy(int from, int toExclusive, int step) {
        return HashSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static HashSet<Character> rangeBy(char from, char toExclusive, int step) {
        return HashSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static HashSet<Double> rangeBy(double from, double toExclusive, double step) {
        return HashSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    /**
     * Creates a HashSet of long numbers starting from {@code from}, extending to {@code toExclusive - 1}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * HashSet.range(0L, 0L)  // = HashSet()
     * HashSet.range(2L, 0L)  // = HashSet()
     * HashSet.range(-2L, 2L) // = HashSet(-2L, -1L, 0L, 1L)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1
     * @return a range of long values as specified or the empty range if {@code from >= toExclusive}
     */
    public static HashSet<Long> range(long from, long toExclusive) {
        return HashSet.ofAll(Iterator.range(from, toExclusive));
    }

    /**
     * Creates a HashSet of long numbers starting from {@code from}, extending to {@code toExclusive - 1},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * HashSet.rangeBy(1L, 3L, 1L)  // = HashSet(1L, 2L)
     * HashSet.rangeBy(1L, 4L, 2L)  // = HashSet(1L, 3L)
     * HashSet.rangeBy(4L, 1L, -2L) // = HashSet(4L, 2L)
     * HashSet.rangeBy(4L, 1L, 2L)  // = HashSet()
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1
     * @param step        the step
     * @return a range of long values as specified or the empty range if<br>
     * {@code from >= toInclusive} and {@code step > 0} or<br>
     * {@code from <= toInclusive} and {@code step < 0}
     * @throws IllegalArgumentException if {@code step} is zero
     */
    public static HashSet<Long> rangeBy(long from, long toExclusive, long step) {
        return HashSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    /**
     * Creates a HashSet of int numbers starting from {@code from}, extending to {@code toInclusive}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * HashSet.rangeClosed(0, 0)  // = HashSet(0)
     * HashSet.rangeClosed(2, 0)  // = HashSet()
     * HashSet.rangeClosed(-2, 2) // = HashSet(-2, -1, 0, 1, 2)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toInclusive the last number
     * @return a range of int values as specified or the empty range if {@code from > toInclusive}
     */
    public static HashSet<Integer> rangeClosed(int from, int toInclusive) {
        return HashSet.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static HashSet<Character> rangeClosed(char from, char toInclusive) {
        return HashSet.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    /**
     * Creates a HashSet of int numbers starting from {@code from}, extending to {@code toInclusive},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * HashSet.rangeClosedBy(1, 3, 1)  // = HashSet(1, 2, 3)
     * HashSet.rangeClosedBy(1, 4, 2)  // = HashSet(1, 3)
     * HashSet.rangeClosedBy(4, 1, -2) // = HashSet(4, 2)
     * HashSet.rangeClosedBy(4, 1, 2)  // = HashSet()
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toInclusive the last number
     * @param step        the step
     * @return a range of int values as specified or the empty range if<br>
     * {@code from > toInclusive} and {@code step > 0} or<br>
     * {@code from < toInclusive} and {@code step < 0}
     * @throws IllegalArgumentException if {@code step} is zero
     */
    public static HashSet<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return HashSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static HashSet<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return HashSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static HashSet<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return HashSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    /**
     * Creates a HashSet of long numbers starting from {@code from}, extending to {@code toInclusive}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * HashSet.rangeClosed(0L, 0L)  // = HashSet(0L)
     * HashSet.rangeClosed(2L, 0L)  // = HashSet()
     * HashSet.rangeClosed(-2L, 2L) // = HashSet(-2L, -1L, 0L, 1L, 2L)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toInclusive the last number
     * @return a range of long values as specified or the empty range if {@code from > toInclusive}
     */
    public static HashSet<Long> rangeClosed(long from, long toInclusive) {
        return HashSet.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    /**
     * Creates a HashSet of long numbers starting from {@code from}, extending to {@code toInclusive},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * HashSet.rangeClosedBy(1L, 3L, 1L)  // = HashSet(1L, 2L, 3L)
     * HashSet.rangeClosedBy(1L, 4L, 2L)  // = HashSet(1L, 3L)
     * HashSet.rangeClosedBy(4L, 1L, -2L) // = HashSet(4L, 2L)
     * HashSet.rangeClosedBy(4L, 1L, 2L)  // = HashSet()
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toInclusive the last number
     * @param step        the step
     * @return a range of int values as specified or the empty range if<br>
     * {@code from > toInclusive} and {@code step > 0} or<br>
     * {@code from < toInclusive} and {@code step < 0}
     * @throws IllegalArgumentException if {@code step} is zero
     */
    public static HashSet<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return HashSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    @Override
    public HashSet<T> add(T element) {
        return contains(element) ? this : new HashSet<>(tree.put(element, element));
    }

    @Override
    public HashSet<T> addAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (isEmpty() && elements instanceof HashSet) {
            @SuppressWarnings("unchecked")
            final HashSet<T> set = (HashSet<T>) elements;
            return set;
        }
        final HashArrayMappedTrie<T, T> that = addAll(tree, elements);
        if (that.size() == tree.size()) {
            return this;
        } else {
            return new HashSet<>(that);
        }
    }

    @Override
    public <R> HashSet<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        return ofAll(iterator().<R> collect(partialFunction));
    }

    @Override
    public boolean contains(T element) {
        return tree.get(element).isDefined();
    }

    @Override
    public HashSet<T> diff(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (isEmpty() || elements.isEmpty()) {
            return this;
        } else {
            return removeAll(elements);
        }
    }

    @Override
    public HashSet<T> distinct() {
        return this;
    }

    @Override
    public HashSet<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return HashSet.ofAll(iterator().distinctBy(comparator));
    }

    @Override
    public <U> HashSet<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        return HashSet.ofAll(iterator().distinctBy(keyExtractor));
    }

    @Override
    public HashSet<T> drop(int n) {
        if (n <= 0) {
            return this;
        } else {
            return HashSet.ofAll(iterator().drop(n));
        }
    }

    @Override
    public HashSet<T> dropRight(int n) {
        return drop(n);
    }

    @Override
    public HashSet<T> dropUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return dropWhile(predicate.negate());
    }

    @Override
    public HashSet<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final HashSet<T> dropped = HashSet.ofAll(iterator().dropWhile(predicate));
        return dropped.length() == length() ? this : dropped;
    }

    @Override
    public HashSet<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final HashSet<T> filtered = HashSet.ofAll(iterator().filter(predicate));

        if (filtered.isEmpty()) {
            return empty();
        } else if (filtered.length() == length()) {
            return this;
        } else {
            return filtered;
        }
    }

    @Override
    public HashSet<T> filterNot(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return filter(predicate.negate());
    }

    @Override
    public <U> HashSet<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (isEmpty()) {
            return empty();
        } else {
            final HashArrayMappedTrie<U, U> that = foldLeft(HashArrayMappedTrie.empty(),
                    (tree, t) -> addAll(tree, mapper.apply(t)));
            return new HashSet<>(that);
        }
    }

    @Override
    public <U> U foldRight(U zero, BiFunction<? super T, ? super U, ? extends U> f) {
        return foldLeft(zero, (u, t) -> f.apply(t, u));
    }

    @Override
    public <C> Map<C, HashSet<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return Collections.groupBy(this, classifier, HashSet::ofAll);
    }

    @Override
    public Iterator<HashSet<T>> grouped(int size) {
        return sliding(size, size);
    }

    @Override
    public boolean hasDefiniteSize() {
        return true;
    }

    @Override
    public T head() {
        if (tree.isEmpty()) {
            throw new NoSuchElementException("head of empty set");
        }
        return iterator().next();
    }

    @Override
    public Option<T> headOption() {
        return iterator().headOption();
    }

    @Override
    public HashSet<T> init() {
        return tail();
    }

    @Override
    public Option<HashSet<T>> initOption() {
        return tailOption();
    }

    @Override
    public HashSet<T> intersect(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (isEmpty() || elements.isEmpty()) {
            return empty();
        } else {
            final int size = size();
            if (size <= elements.size()) {
                return retainAll(elements);
            } else {
                final HashSet<T> results = HashSet.<T> ofAll(elements).retainAll(this);
                return (size == results.size()) ? this : results;
            }
        }
    }

    @Override
    public boolean isEmpty() {
        return tree.isEmpty();
    }

    /**
     * A {@code HashSet} is computed eagerly.
     *
     * @return false
     */
    @Override
    public boolean isLazy() {
        return false;
    }

    @Override
    public boolean isTraversableAgain() {
        return true;
    }

    @Override
    public Iterator<T> iterator() {
        return tree.keysIterator();
    }

    @Override
    public T last() {
        return Collections.last(this);
    }

    @Override
    public int length() {
        return tree.size();
    }

    @Override
    public <U> HashSet<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (isEmpty()) {
            return empty();
        } else {
            final HashArrayMappedTrie<U, U> that = foldLeft(HashArrayMappedTrie.empty(), (tree, t) -> {
                final U u = mapper.apply(t);
                return tree.put(u, u);
            });
            return new HashSet<>(that);
        }
    }

    @Override
    public String mkString(CharSequence prefix, CharSequence delimiter, CharSequence suffix) {
        return iterator().mkString(prefix, delimiter, suffix);
    }

    @Override
    public HashSet<T> orElse(Iterable<? extends T> other) {
        return isEmpty() ? ofAll(other) : this;
    }

    @Override
    public HashSet<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return isEmpty() ? ofAll(supplier.get()) : this;
    }

    @Override
    public Tuple2<HashSet<T>, HashSet<T>> partition(Predicate<? super T> predicate) {
        return Collections.partition(this, HashSet::ofAll, predicate);
    }

    @Override
    public HashSet<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (!isEmpty()) {
            action.accept(iterator().head());
        }
        return this;
    }

    @Override
    public HashSet<T> remove(T element) {
        final HashArrayMappedTrie<T, T> newTree = tree.remove(element);
        return (newTree == tree) ? this : new HashSet<>(newTree);
    }

    @Override
    public HashSet<T> removeAll(Iterable<? extends T> elements) {
        return Collections.removeAll(this, elements);
    }

    @Override
    public HashSet<T> replace(T currentElement, T newElement) {
        if (tree.containsKey(currentElement)) {
            return remove(currentElement).add(newElement);
        } else {
            return this;
        }
    }

    @Override
    public HashSet<T> replaceAll(T currentElement, T newElement) {
        return replace(currentElement, newElement);
    }

    @Override
    public HashSet<T> retainAll(Iterable<? extends T> elements) {
        return Collections.retainAll(this, elements);
    }

    @Override
    public HashSet<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return scanLeft(zero, operation);
    }

    @Override
    public <U> HashSet<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        return Collections.scanLeft(this, zero, operation, HashSet::ofAll);
    }

    @Override
    public <U> HashSet<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        return Collections.scanRight(this, zero, operation, HashSet::ofAll);
    }

    @Override
    public Iterator<HashSet<T>> slideBy(Function<? super T, ?> classifier) {
        return iterator().slideBy(classifier).map(HashSet::ofAll);
    }

    @Override
    public Iterator<HashSet<T>> sliding(int size) {
        return sliding(size, 1);
    }

    @Override
    public Iterator<HashSet<T>> sliding(int size, int step) {
        return iterator().sliding(size, step).map(HashSet::ofAll);
    }

    @Override
    public Tuple2<HashSet<T>, HashSet<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final Tuple2<Iterator<T>, Iterator<T>> t = iterator().span(predicate);
        return Tuple.of(HashSet.ofAll(t._1), HashSet.ofAll(t._2));
    }

    @Override
    public HashSet<T> tail() {
        if (tree.isEmpty()) {
            throw new UnsupportedOperationException("tail of empty set");
        }
        return remove(head());
    }

    @Override
    public Option<HashSet<T>> tailOption() {
        if (tree.isEmpty()) {
            return Option.none();
        } else {
            return Option.some(tail());
        }
    }

    @Override
    public HashSet<T> take(int n) {
        if (n >= size() || isEmpty()) {
            return this;
        } else if (n <= 0) {
            return empty();
        } else {
            return ofAll(() -> iterator().take(n));
        }
    }

    @Override
    public HashSet<T> takeRight(int n) {
        return take(n);
    }

    @Override
    public HashSet<T> takeUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return takeWhile(predicate.negate());
    }

    @Override
    public HashSet<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final HashSet<T> taken = HashSet.ofAll(iterator().takeWhile(predicate));
        return taken.length() == length() ? this : taken;
    }

    /**
     * Transforms this {@code HashSet}.
     *
     * @param f   A transformation
     * @param <U> Type of transformation result
     * @return An instance of type {@code U}
     * @throws NullPointerException if {@code f} is null
     */
    public <U> U transform(Function<? super HashSet<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    public java.util.HashSet<T> toJavaSet() {
        return toJavaSet(java.util.HashSet::new);
    }

    @SuppressWarnings("unchecked")
    @Override
    public HashSet<T> union(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (isEmpty()) {
            if (elements instanceof HashSet) {
                return (HashSet<T>) elements;
            } else {
                return HashSet.ofAll(elements);
            }
        } else if (elements.isEmpty()) {
            return this;
        } else {
            final HashArrayMappedTrie<T, T> that = addAll(tree, elements);
            if (that.size() == tree.size()) {
                return this;
            } else {
                return new HashSet<>(that);
            }
        }
    }

    @Override
    public <U> HashSet<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        return zipWith(that, Tuple::of);
    }

    @Override
    public <U, R> HashSet<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return HashSet.ofAll(iterator().zipWith(that, mapper));
    }

    @Override
    public <U> HashSet<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        return HashSet.ofAll(iterator().zipAll(that, thisElem, thatElem));
    }

    @Override
    public HashSet<Tuple2<T, Integer>> zipWithIndex() {
        return zipWithIndex(Tuple::of);
    }

    @Override
    public <U> HashSet<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return HashSet.ofAll(iterator().zipWithIndex(mapper));
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
        return "HashSet";
    }

    @Override
    public String toString() {
        return mkString(stringPrefix() + "(", ", ", ")");
    }

    private static <T> HashArrayMappedTrie<T, T> addAll(HashArrayMappedTrie<T, T> initial,
            Iterable<? extends T> additional) {
        HashArrayMappedTrie<T, T> that = initial;
        for (T t : additional) {
            that = that.put(t, t);
        }
        return that;
    }

    // -- Serialization

    /**
     * {@code writeReplace} method for the serialization proxy pattern.
     * <p>
     * The presence of this method causes the serialization system to emit a SerializationProxy instance instead of
     * an instance of the enclosing class.
     *
     * @return A SerializationProxy for this enclosing class.
     */
    private Object writeReplace() {
        return new SerializationProxy<>(this.tree);
    }

    /**
     * {@code readObject} method for the serialization proxy pattern.
     * <p>
     * Guarantees that the serialization system will never generate a serialized instance of the enclosing class.
     *
     * @param stream An object serialization stream.
     * @throws java.io.InvalidObjectException This method will throw with the message "Proxy required".
     */
    private void readObject(ObjectInputStream stream) throws InvalidObjectException {
        throw new InvalidObjectException("Proxy required");
    }

    /**
     * A serialization proxy which, in this context, is used to deserialize immutable, linked Lists with final
     * instance fields.
     *
     * @param <T> The component type of the underlying list.
     */
    // DEV NOTE: The serialization proxy pattern is not compatible with non-final, i.e. extendable,
    // classes. Also, it may not be compatible with circular object graphs.
    private static final class SerializationProxy<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        // the instance to be serialized/deserialized
        private transient HashArrayMappedTrie<T, T> tree;

        /**
         * Constructor for the case of serialization, called by {@link HashSet#writeReplace()}.
         * <p/>
         * The constructor of a SerializationProxy takes an argument that concisely represents the logical state of
         * an instance of the enclosing class.
         *
         * @param tree a Cons
         */
        SerializationProxy(HashArrayMappedTrie<T, T> tree) {
            this.tree = tree;
        }

        /**
         * Write an object to a serialization stream.
         *
         * @param s An object serialization stream.
         * @throws java.io.IOException If an error occurs writing to the stream.
         */
        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            s.writeInt(tree.size());
            for (Tuple2<T, T> e : tree) {
                s.writeObject(e._1);
            }
        }

        /**
         * Read an object from a deserialization stream.
         *
         * @param s An object deserialization stream.
         * @throws ClassNotFoundException If the object's class read from the stream cannot be found.
         * @throws InvalidObjectException If the stream contains no list elements.
         * @throws IOException            If an error occurs reading from the stream.
         */
        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            s.defaultReadObject();
            final int size = s.readInt();
            if (size < 0) {
                throw new InvalidObjectException("No elements");
            }
            HashArrayMappedTrie<T, T> temp = HashArrayMappedTrie.empty();
            for (int i = 0; i < size; i++) {
                @SuppressWarnings("unchecked")
                final T element = (T) s.readObject();
                temp = temp.put(element, element);
            }
            tree = temp;
        }

        /**
         * {@code readResolve} method for the serialization proxy pattern.
         * <p>
         * Returns a logically equivalent instance of the enclosing class. The presence of this method causes the
         * serialization system to translate the serialization proxy back into an instance of the enclosing class
         * upon deserialization.
         *
         * @return A deserialized instance of the enclosing class.
         */
        private Object readResolve() {
            return tree.isEmpty() ? HashSet.empty() : new HashSet<>(tree);
        }
    }
}
