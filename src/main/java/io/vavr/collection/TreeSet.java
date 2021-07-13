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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * SortedSet implementation, backed by a Red/Black Tree.
 *
 * @param <T> Component type
 */
// DEV-NOTE: it is not possible to create an EMPTY TreeSet without a Comparator type in scope
@SuppressWarnings("deprecation")
public final class TreeSet<T> implements SortedSet<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private final RedBlackTree<T> tree;

    TreeSet(RedBlackTree<T> tree) {
        this.tree = tree;
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link TreeSet}.
     * <p>
     * The natural comparator is used to compare TreeSet elements.
     *
     * @param <T> Component type of the List.
     * @return A io.vavr.collection.List Collector.
     */
    public static <T extends Comparable<? super T>> Collector<T, ArrayList<T>, TreeSet<T>> collector() {
        return collector(Comparators.naturalComparator());
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link TreeSet}.
     *
     * @param <T>        Component type of the List.
     * @param comparator An element comparator
     * @return A io.vavr.collection.List Collector.
     */
    public static <T> Collector<T, ArrayList<T>, TreeSet<T>> collector(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return Collections.toListAndThen(list -> TreeSet.ofAll(comparator, list));
    }

    public static <T extends Comparable<? super T>> TreeSet<T> empty() {
        return empty(Comparators.naturalComparator());
    }

    public static <T> TreeSet<T> empty(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return new TreeSet<>(RedBlackTree.empty(comparator));
    }

    /**
     * Narrows a widened {@code TreeSet<? extends T>} to {@code TreeSet<T>}
     * by performing a type-safe cast. This is eligible because immutable/read-only
     * collections are covariant.
     * <p>
     * CAUTION: The underlying {@code Comparator} might fail!
     *
     * @param treeSet A {@code TreeSet}.
     * @param <T>     Component type of the {@code TreeSet}.
     * @return the given {@code treeSet} instance as narrowed type {@code TreeSet<T>}.
     */
    @SuppressWarnings("unchecked")
    public static <T> TreeSet<T> narrow(TreeSet<? extends T> treeSet) {
        return (TreeSet<T>) treeSet;
    }

    public static <T extends Comparable<? super T>> TreeSet<T> of(T value) {
        return of(Comparators.naturalComparator(), value);
    }

    public static <T> TreeSet<T> of(Comparator<? super T> comparator, T value) {
        Objects.requireNonNull(comparator, "comparator is null");
        return new TreeSet<>(RedBlackTree.of(comparator, value));
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    public static <T extends Comparable<? super T>> TreeSet<T> of(T... values) {
        return TreeSet.<T> of(Comparators.naturalComparator(), values);
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    public static <T> TreeSet<T> of(Comparator<? super T> comparator, T... values) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(values, "values is null");
        return new TreeSet<>(RedBlackTree.of(comparator, values));
    }

    /**
     * Returns a TreeSet containing {@code n} values of a given Function {@code f}
     * over a range of integer values from 0 to {@code n - 1}.
     *
     * @param <T>        Component type of the TreeSet
     * @param comparator The comparator used to sort the elements
     * @param n          The number of elements in the TreeSet
     * @param f          The Function computing element values
     * @return A TreeSet consisting of elements {@code f(0),f(1), ..., f(n - 1)}
     * @throws NullPointerException if {@code comparator} or {@code f} are null
     */
    public static <T> TreeSet<T> tabulate(Comparator<? super T> comparator, int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(f, "f is null");
        return Collections.tabulate(n, f, TreeSet.empty(comparator), values -> of(comparator, values));
    }

    /**
     * Returns a TreeSet containing {@code n} values of a given Function {@code f}
     * over a range of integer values from 0 to {@code n - 1}.
     * The underlying comparator is the natural comparator of T.
     *
     * @param <T> Component type of the TreeSet
     * @param n   The number of elements in the TreeSet
     * @param f   The Function computing element values
     * @return A TreeSet consisting of elements {@code f(0),f(1), ..., f(n - 1)}
     * @throws NullPointerException if {@code f} is null
     */
    public static <T extends Comparable<? super T>> TreeSet<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return tabulate(Comparators.naturalComparator(), n, f);
    }

    /**
     * Returns a TreeSet containing tuples returned by {@code n} calls to a given Supplier {@code s}.
     *
     * @param <T>        Component type of the TreeSet
     * @param comparator The comparator used to sort the elements
     * @param n          The number of elements in the TreeSet
     * @param s          The Supplier computing element values
     * @return A TreeSet of size {@code n}, where each element contains the result supplied by {@code s}.
     * @throws NullPointerException if {@code comparator} or {@code s} are null
     */
    public static <T> TreeSet<T> fill(Comparator<? super T> comparator, int n, Supplier<? extends T> s) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(s, "s is null");
        return Collections.fill(n, s, TreeSet.empty(comparator), values -> of(comparator, values));
    }

    /**
     * Returns a TreeSet containing tuples returned by {@code n} calls to a given Supplier {@code s}.
     * The underlying comparator is the natural comparator of T.
     *
     * @param <T> Component type of the TreeSet
     * @param n   The number of elements in the TreeSet
     * @param s   The Supplier computing element values
     * @return A TreeSet of size {@code n}, where each element contains the result supplied by {@code s}.
     * @throws NullPointerException if {@code s} is null
     */
    public static <T extends Comparable<? super T>> TreeSet<T> fill(int n, Supplier<? extends T> s) {
        Objects.requireNonNull(s, "s is null");
        return fill(Comparators.naturalComparator(), n, s);
    }

    public static <T extends Comparable<? super T>> TreeSet<T> ofAll(Iterable<? extends T> values) {
        return ofAll(Comparators.naturalComparator(), values);
    }

    @SuppressWarnings("unchecked")
    public static <T> TreeSet<T> ofAll(Comparator<? super T> comparator, Iterable<? extends T> values) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(values, "values is null");
        if (values instanceof TreeSet && ((TreeSet<?>) values).comparator() == comparator) {
            return (TreeSet<T>) values;
        } else {
            return values.iterator().hasNext() ? new TreeSet<>(RedBlackTree.ofAll(comparator, values)) : empty(comparator);
        }
    }

    public static <T extends Comparable<? super T>> TreeSet<T> ofAll(java.util.stream.Stream<? extends T> javaStream) {
        Objects.requireNonNull(javaStream, "javaStream is null");
        return ofAll(Iterator.ofAll(javaStream.iterator()));
    }

    public static <T> TreeSet<T> ofAll(Comparator<? super T> comparator, java.util.stream.Stream<? extends T> javaStream) {
        Objects.requireNonNull(javaStream, "javaStream is null");
        return ofAll(comparator, Iterator.ofAll(javaStream.iterator()));
    }

    /**
     * Creates a TreeSet from boolean values.
     *
     * @param elements boolean values
     * @return A new TreeSet of Boolean values
     * @throws NullPointerException if elements is null
     */
    public static TreeSet<Boolean> ofAll(boolean... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return TreeSet.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a TreeSet from byte values.
     *
     * @param elements byte values
     * @return A new TreeSet of Byte values
     * @throws NullPointerException if elements is null
     */
    public static TreeSet<Byte> ofAll(byte... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return TreeSet.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a TreeSet from char values.
     *
     * @param elements char values
     * @return A new TreeSet of Character values
     * @throws NullPointerException if elements is null
     */
    public static TreeSet<Character> ofAll(char... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return TreeSet.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a TreeSet from double values.
     *
     * @param elements double values
     * @return A new TreeSet of Double values
     * @throws NullPointerException if elements is null
     */
    public static TreeSet<Double> ofAll(double... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return TreeSet.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a TreeSet from float values.
     *
     * @param elements float values
     * @return A new TreeSet of Float values
     * @throws NullPointerException if elements is null
     */
    public static TreeSet<Float> ofAll(float... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return TreeSet.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a TreeSet from int values.
     *
     * @param elements int values
     * @return A new TreeSet of Integer values
     * @throws NullPointerException if elements is null
     */
    public static TreeSet<Integer> ofAll(int... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return TreeSet.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a TreeSet from long values.
     *
     * @param elements long values
     * @return A new TreeSet of Long values
     * @throws NullPointerException if elements is null
     */
    public static TreeSet<Long> ofAll(long... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return TreeSet.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a TreeSet from short values.
     *
     * @param elements short values
     * @return A new TreeSet of Short values
     * @throws NullPointerException if elements is null
     */
    public static TreeSet<Short> ofAll(short... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return TreeSet.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a TreeSet of int numbers starting from {@code from}, extending to {@code toExclusive - 1}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * TreeSet.range(0, 0)  // = TreeSet()
     * TreeSet.range(2, 0)  // = TreeSet()
     * TreeSet.range(-2, 2) // = TreeSet(-2, -1, 0, 1)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1
     * @return a range of int values as specified or the empty range if {@code from >= toExclusive}
     */
    public static TreeSet<Integer> range(int from, int toExclusive) {
        return TreeSet.ofAll(Iterator.range(from, toExclusive));
    }

    public static TreeSet<Character> range(char from, char toExclusive) {
        return TreeSet.ofAll(Iterator.range(from, toExclusive));
    }

    /**
     * Creates a TreeSet of int numbers starting from {@code from}, extending to {@code toExclusive - 1},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * TreeSet.rangeBy(1, 3, 1)  // = TreeSet(1, 2)
     * TreeSet.rangeBy(1, 4, 2)  // = TreeSet(1, 3)
     * TreeSet.rangeBy(4, 1, -2) // = TreeSet(4, 2)
     * TreeSet.rangeBy(4, 1, 2)  // = TreeSet()
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
    public static TreeSet<Integer> rangeBy(int from, int toExclusive, int step) {
        return TreeSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static TreeSet<Character> rangeBy(char from, char toExclusive, int step) {
        return TreeSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static TreeSet<Double> rangeBy(double from, double toExclusive, double step) {
        return TreeSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    /**
     * Creates a TreeSet of long numbers starting from {@code from}, extending to {@code toExclusive - 1}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * TreeSet.range(0L, 0L)  // = TreeSet()
     * TreeSet.range(2L, 0L)  // = TreeSet()
     * TreeSet.range(-2L, 2L) // = TreeSet(-2L, -1L, 0L, 1L)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1
     * @return a range of long values as specified or the empty range if {@code from >= toExclusive}
     */
    public static TreeSet<Long> range(long from, long toExclusive) {
        return TreeSet.ofAll(Iterator.range(from, toExclusive));
    }

    /**
     * Creates a TreeSet of long numbers starting from {@code from}, extending to {@code toExclusive - 1},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * TreeSet.rangeBy(1L, 3L, 1L)  // = TreeSet(1L, 2L)
     * TreeSet.rangeBy(1L, 4L, 2L)  // = TreeSet(1L, 3L)
     * TreeSet.rangeBy(4L, 1L, -2L) // = TreeSet(4L, 2L)
     * TreeSet.rangeBy(4L, 1L, 2L)  // = TreeSet()
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
    public static TreeSet<Long> rangeBy(long from, long toExclusive, long step) {
        return TreeSet.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    /**
     * Creates a TreeSet of int numbers starting from {@code from}, extending to {@code toInclusive}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * TreeSet.rangeClosed(0, 0)  // = TreeSet(0)
     * TreeSet.rangeClosed(2, 0)  // = TreeSet()
     * TreeSet.rangeClosed(-2, 2) // = TreeSet(-2, -1, 0, 1, 2)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toInclusive the last number
     * @return a range of int values as specified or the empty range if {@code from > toInclusive}
     */
    public static TreeSet<Integer> rangeClosed(int from, int toInclusive) {
        return TreeSet.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static TreeSet<Character> rangeClosed(char from, char toInclusive) {
        return TreeSet.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    /**
     * Creates a TreeSet of int numbers starting from {@code from}, extending to {@code toInclusive},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * TreeSet.rangeClosedBy(1, 3, 1)  // = TreeSet(1, 2, 3)
     * TreeSet.rangeClosedBy(1, 4, 2)  // = TreeSet(1, 3)
     * TreeSet.rangeClosedBy(4, 1, -2) // = TreeSet(4, 2)
     * TreeSet.rangeClosedBy(4, 1, 2)  // = TreeSet()
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
    public static TreeSet<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return TreeSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static TreeSet<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return TreeSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static TreeSet<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return TreeSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    /**
     * Creates a TreeSet of long numbers starting from {@code from}, extending to {@code toInclusive}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * TreeSet.rangeClosed(0L, 0L)  // = TreeSet(0L)
     * TreeSet.rangeClosed(2L, 0L)  // = TreeSet()
     * TreeSet.rangeClosed(-2L, 2L) // = TreeSet(-2L, -1L, 0L, 1L, 2L)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toInclusive the last number
     * @return a range of long values as specified or the empty range if {@code from > toInclusive}
     */
    public static TreeSet<Long> rangeClosed(long from, long toInclusive) {
        return TreeSet.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    /**
     * Creates a TreeSet of long numbers starting from {@code from}, extending to {@code toInclusive},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * TreeSet.rangeClosedBy(1L, 3L, 1L)  // = TreeSet(1L, 2L, 3L)
     * TreeSet.rangeClosedBy(1L, 4L, 2L)  // = TreeSet(1L, 3L)
     * TreeSet.rangeClosedBy(4L, 1L, -2L) // = TreeSet(4L, 2L)
     * TreeSet.rangeClosedBy(4L, 1L, 2L)  // = TreeSet()
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
    public static TreeSet<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return TreeSet.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    @Override
    public TreeSet<T> add(T element) {
        return contains(element) ? this : new TreeSet<>(tree.insert(element));
    }

    @Override
    public TreeSet<T> addAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        RedBlackTree<T> that = tree;
        for (T element : elements) {
            if (!that.contains(element)) {
                that = that.insert(element);
            }
        }
        if (tree == that) {
            return this;
        } else {
            return new TreeSet<>(that);
        }
    }

    @Override
    public <R> TreeSet<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        return ofAll(Comparators.naturalComparator(), iterator().<R> collect(partialFunction));
    }

    @Override
    public Comparator<T> comparator() {
        return tree.comparator();
    }

    @SuppressWarnings("unchecked")
    @Override
    public TreeSet<T> diff(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (isEmpty()) {
            return this;
        } else if (elements instanceof TreeSet) {
            final TreeSet<T> that = (TreeSet<T>) elements;
            return that.isEmpty() ? this : new TreeSet<>(tree.difference(that.tree));
        } else {
            return removeAll(elements);
        }
    }

    @Override
    public boolean contains(T element) {
        return tree.contains(element);
    }

    @Override
    public TreeSet<T> distinct() {
        return this;
    }

    @Override
    public TreeSet<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return isEmpty() ? this : TreeSet.ofAll(tree.comparator(), iterator().distinctBy(comparator));
    }

    @Override
    public <U> TreeSet<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        return isEmpty() ? this : TreeSet.ofAll(tree.comparator(), iterator().distinctBy(keyExtractor));
    }

    @Override
    public TreeSet<T> drop(int n) {
        if (n <= 0 || isEmpty()) {
            return this;
        } else if (n >= length()) {
            return empty(tree.comparator());
        } else {
            return TreeSet.ofAll(tree.comparator(), iterator().drop(n));
        }
    }

    @Override
    public TreeSet<T> dropRight(int n) {
        if (n <= 0 || isEmpty()) {
            return this;
        } else if (n >= length()) {
            return empty(tree.comparator());
        } else {
            return TreeSet.ofAll(tree.comparator(), iterator().dropRight(n));
        }
    }

    @Override
    public TreeSet<T> dropUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return dropWhile(predicate.negate());
    }

    @Override
    public TreeSet<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final TreeSet<T> treeSet = TreeSet.ofAll(tree.comparator(), iterator().dropWhile(predicate));
        return (treeSet.length() == length()) ? this : treeSet;
    }

    @Override
    public TreeSet<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final TreeSet<T> treeSet = TreeSet.ofAll(tree.comparator(), iterator().filter(predicate));
        return (treeSet.length() == length()) ? this : treeSet;
    }

    @Override
    public TreeSet<T> filterNot(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return filter(predicate.negate());
    }

    @Override
    public <U> TreeSet<U> flatMap(Comparator<? super U> comparator,
            Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return TreeSet.ofAll(comparator, iterator().flatMap(mapper));
    }

    @Override
    public <U> Set<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return isEmpty() ? HashSet.empty() : HashSet.ofAll(this).flatMap(mapper);
    }

    @Override
    public <U> U foldRight(U zero, BiFunction<? super T, ? super U, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return iterator().foldRight(zero, f);
    }

    @Override
    public <C> Map<C, TreeSet<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return Collections.groupBy(this, classifier, elements -> ofAll(comparator(), elements));
    }

    @Override
    public Iterator<TreeSet<T>> grouped(int size) {
        return sliding(size, size);
    }

    @Override
    public boolean hasDefiniteSize() {
        return true;
    }

    @Override
    public T head() {
        if (isEmpty()) {
            throw new NoSuchElementException("head of empty TreeSet");
        } else {
            return tree.min().get();
        }
    }

    @Override
    public Option<T> headOption() {
        return tree.min();
    }

    @Override
    public TreeSet<T> init() {
        if (isEmpty()) {
            throw new UnsupportedOperationException("init of empty TreeSet");
        } else {
            return new TreeSet<>(tree.delete(tree.max().get()));
        }
    }

    @Override
    public Option<TreeSet<T>> initOption() {
        return isEmpty() ? Option.none() : Option.some(init());
    }

    @SuppressWarnings("unchecked")
    @Override
    public TreeSet<T> intersect(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (isEmpty()) {
            return this;
        } else if (elements instanceof TreeSet) {
            final TreeSet<T> that = (TreeSet<T>) elements;
            return new TreeSet<>(tree.intersection(that.tree));
        } else {
            return retainAll(elements);
        }
    }

    @Override
    public boolean isEmpty() {
        return tree.isEmpty();
    }

    /**
     * A {@code TreeSet} is computed eagerly.
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
        return tree.iterator();
    }

    @Override
    public T last() {
        if (isEmpty()) {
            throw new NoSuchElementException("last of empty TreeSet");
        } else {
            return tree.max().get();
        }
    }

    @Override
    public int length() {
        return tree.size();
    }

    @Override
    public <U> TreeSet<U> map(Comparator<? super U> comparator, Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return TreeSet.ofAll(comparator, iterator().map(mapper));
    }

    @Override
    public <U> Set<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return isEmpty() ? HashSet.empty() : HashSet.ofAll(this).map(mapper);
    }

    /**
     * Returns this {@code TreeSet} if it is nonempty,
     * otherwise {@code TreeSet} created from iterable, using existing comparator.
     *
     * @param other An alternative {@code Traversable}
     * @return this {@code TreeSet} if it is nonempty,
     * otherwise {@code TreeSet} created from iterable, using existing comparator.
     */
    @Override
    public TreeSet<T> orElse(Iterable<? extends T> other) {
        return isEmpty() ? ofAll(tree.comparator(), other) : this;
    }

    /**
     * Returns this {@code TreeSet} if it is nonempty,
     * otherwise {@code TreeSet} created from result of evaluating supplier, using existing comparator.
     *
     * @param supplier An alternative {@code Traversable}
     * @return this {@code TreeSet} if it is nonempty,
     * otherwise {@code TreeSet} created from result of evaluating supplier, using existing comparator.
     */
    @Override
    public TreeSet<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return isEmpty() ? ofAll(tree.comparator(), supplier.get()) : this;
    }

    @Override
    public Tuple2<TreeSet<T>, TreeSet<T>> partition(Predicate<? super T> predicate) {
        return Collections.partition(this, values -> TreeSet.ofAll(tree.comparator(), values), predicate);
    }

    @Override
    public TreeSet<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (!isEmpty()) {
            action.accept(head());
        }
        return this;
    }

    @Override
    public TreeSet<T> remove(T element) {
        return new TreeSet<>(tree.delete(element));
    }

    @Override
    public TreeSet<T> removeAll(Iterable<? extends T> elements) {
        return Collections.removeAll(this, elements);
    }

    @Override
    public TreeSet<T> replace(T currentElement, T newElement) {
        if (tree.contains(currentElement)) {
            return new TreeSet<>(tree.delete(currentElement).insert(newElement));
        } else {
            return this;
        }
    }

    @Override
    public TreeSet<T> replaceAll(T currentElement, T newElement) {
        // a set has only one occurrence
        return replace(currentElement, newElement);
    }

    @Override
    public TreeSet<T> retainAll(Iterable<? extends T> elements) {
        return Collections.retainAll(this, elements);
    }

    @Override
    public TreeSet<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return Collections.scanLeft(this, zero, operation, iter -> TreeSet.ofAll(comparator(), iter));
    }

    @Override
    public <U> Set<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        if (zero instanceof Comparable) {
            final Comparator<U> comparator = Comparators.naturalComparator();
            return Collections.scanLeft(this, zero, operation, iter -> TreeSet.ofAll(comparator, iter));
        } else {
            return Collections.scanLeft(this, zero, operation, HashSet::ofAll);
        }
    }

    @Override
    public <U> Set<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        if (zero instanceof Comparable) {
            final Comparator<U> comparator = Comparators.naturalComparator();
            return Collections.scanRight(this, zero, operation, iter -> TreeSet.ofAll(comparator, iter));
        } else {
            return Collections.scanRight(this, zero, operation, HashSet::ofAll);
        }
    }

    @Override
    public Iterator<TreeSet<T>> slideBy(Function<? super T, ?> classifier) {
        return iterator().slideBy(classifier).map(seq -> TreeSet.ofAll(tree.comparator(), seq));
    }

    @Override
    public Iterator<TreeSet<T>> sliding(int size) {
        return sliding(size, 1);
    }

    @Override
    public Iterator<TreeSet<T>> sliding(int size, int step) {
        return iterator().sliding(size, step).map(seq -> TreeSet.ofAll(tree.comparator(), seq));
    }

    @Override
    public Tuple2<TreeSet<T>, TreeSet<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return iterator().span(predicate).map(i1 -> TreeSet.ofAll(tree.comparator(), i1),
                i2 -> TreeSet.ofAll(tree.comparator(), i2));
    }

    @Override
    public TreeSet<T> tail() {
        if (isEmpty()) {
            throw new UnsupportedOperationException("tail of empty TreeSet");
        } else {
            return new TreeSet<>(tree.delete(tree.min().get()));
        }
    }

    @Override
    public Option<TreeSet<T>> tailOption() {
        return isEmpty() ? Option.none() : Option.some(tail());
    }

    @Override
    public TreeSet<T> take(int n) {
        if (n <= 0) {
            return empty(tree.comparator());
        } else if (n >= length()) {
            return this;
        } else {
            return TreeSet.ofAll(tree.comparator(), iterator().take(n));
        }
    }

    @Override
    public TreeSet<T> takeRight(int n) {
        if (n <= 0) {
            return empty(tree.comparator());
        } else if (n >= length()) {
            return this;
        } else {
            return TreeSet.ofAll(tree.comparator(), iterator().takeRight(n));
        }
    }

    @Override
    public TreeSet<T> takeUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final TreeSet<T> treeSet = takeWhile(predicate.negate());
        return (treeSet.length() == length()) ? this : treeSet;
    }

    @Override
    public TreeSet<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final TreeSet<T> treeSet = TreeSet.ofAll(tree.comparator(), iterator().takeWhile(predicate));
        return (treeSet.length() == length()) ? this : treeSet;
    }

    /**
     * Transforms this {@code TreeSet}.
     *
     * @param f   A transformation
     * @param <U> Type of transformation result
     * @return An instance of type {@code U}
     * @throws NullPointerException if {@code f} is null
     */
    public <U> U transform(Function<? super TreeSet<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    public java.util.TreeSet<T> toJavaSet() {
        return toJavaSet(ignore -> new java.util.TreeSet<>(comparator()));
    }

    @SuppressWarnings("unchecked")
    @Override
    public TreeSet<T> union(Set<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (elements instanceof TreeSet) {
            final TreeSet<T> that = (TreeSet<T>) elements;
            return that.isEmpty() ? this : new TreeSet<>(tree.union(that.tree));
        } else {
            return addAll(elements);
        }
    }

    @Override
    public <U> TreeSet<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        Objects.requireNonNull(that, "that is null");
        final Comparator<Tuple2<T, U>> tuple2Comparator = Tuple2.comparator(tree.comparator(), Comparators.naturalComparator());
        return TreeSet.ofAll(tuple2Comparator, iterator().zipWith(that, Tuple::of));
    }

    @Override
    public <U, R> TreeSet<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return TreeSet.ofAll(Comparators.naturalComparator(), iterator().zipWith(that, mapper));
    }

    @Override
    public <U> TreeSet<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        final Comparator<Tuple2<T, U>> tuple2Comparator = Tuple2.comparator(tree.comparator(), Comparators.naturalComparator());
        return TreeSet.ofAll(tuple2Comparator, iterator().zipAll(that, thisElem, thatElem));
    }

    @Override
    public TreeSet<Tuple2<T, Integer>> zipWithIndex() {
        final Comparator<? super T> component1Comparator = tree.comparator();
        final Comparator<Tuple2<T, Integer>> tuple2Comparator = (t1, t2) -> component1Comparator.compare(t1._1, t2._1);
        return TreeSet.ofAll(tuple2Comparator, iterator().zipWithIndex());
    }

    @Override
    public <U> SortedSet<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        return TreeSet.ofAll(Comparators.naturalComparator(), iterator().zipWithIndex(mapper));
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
        return "TreeSet";
    }

    @Override
    public String toString() {
        return mkString(stringPrefix() + "(", ", ", ")");
    }
}
