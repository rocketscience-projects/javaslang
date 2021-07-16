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
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import static io.vavr.collection.Tree.Order.PRE_ORDER;

/**
 * A general Tree interface.
 *
 * @param <T> component type of this Tree
 */
public abstract class Tree<T> implements Traversable<T>, Serializable {

    private static final long serialVersionUID = 1L;

    // sealed
    private Tree() {
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link Tree}.
     *
     * @param <T> Component type of the Tree.
     * @return A io.vavr.collection.Tree Collector.
     */
    public static <T> Collector<T, ArrayList<T>, Tree<T>> collector() {
        return Collections.toListAndThen(Tree::ofAll);
    }

    /**
     * Returns the singleton empty tree.
     *
     * @param <T> Type of tree values.
     * @return The empty tree.
     */
    public static <T> Empty<T> empty() {
        return Empty.instance();
    }

    /**
     * Narrows a widened {@code Tree<? extends T>} to {@code Tree<T>}
     * by performing a type-safe cast. This is eligible because immutable/read-only
     * collections are covariant.
     *
     * @param tree An {@code Tree}.
     * @param <T>  Component type of the {@code Tree}.
     * @return the given {@code tree} instance as narrowed type {@code Tree<T>}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Tree<T> narrow(Tree<? extends T> tree) {
        return (Tree<T>) tree;
    }

    /**
     * Returns a new Node containing the given value and having no children.
     *
     * @param value A value
     * @param <T>   Value type
     * @return A new Node instance.
     */
    public static <T> Node<T> of(T value) {
        return new Node<>(value, io.vavr.collection.List.empty());
    }

    /**
     * Returns a new Node containing the given value and having the given children.
     *
     * @param value    A value
     * @param children The child nodes, possibly empty
     * @param <T>      Value type
     * @return A new Node instance.
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    public static <T> Node<T> of(T value, Node<T>... children) {
        Objects.requireNonNull(children, "children is null");
        return new Node<>(value, io.vavr.collection.List.of(children));
    }

    /**
     * Returns a new Node containing the given value and having the given children.
     *
     * @param value    A value
     * @param children The child nodes, possibly empty
     * @param <T>      Value type
     * @return A new Node instance.
     */
    public static <T> Node<T> of(T value, Iterable<Node<T>> children) {
        Objects.requireNonNull(children, "children is null");
        return new Node<>(value, io.vavr.collection.List.ofAll(children));
    }

    /**
     * Creates a Tree of the given elements.
     *
     * @param <T>    Component type of the List.
     * @param values Zero or more values.
     * @return A Tree containing the given values.
     * @throws NullPointerException if {@code values} is null
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    public static <T> Tree<T> of(T... values) {
        Objects.requireNonNull(values, "values is null");
        final io.vavr.collection.List<T> list = io.vavr.collection.List.of(values);
        return list.isEmpty() ? Empty.instance() : new Node<>(list.head(), list.tail().map(Tree::of));
    }

    /**
     * Creates a Tree of the given elements.
     * <p>
     * If the given iterable is a tree, it is returned as result.
     * if the iteration order of the elements is stable.
     *
     * @param <T>      Component type of the List.
     * @param iterable An Iterable of elements.
     * @return A list containing the given elements in the same order.
     * @throws NullPointerException if {@code elements} is null
     */
    @SuppressWarnings("unchecked")
    public static <T> Tree<T> ofAll(Iterable<? extends T> iterable) {
        Objects.requireNonNull(iterable, "iterable is null");
        if (iterable instanceof Tree) {
            return (Tree<T>) iterable;
        } else {
            final io.vavr.collection.List<T> list = io.vavr.collection.List.ofAll(iterable);
            return list.isEmpty() ? Empty.instance() : new Node<>(list.head(), list.tail().map(Tree::of));
        }
    }

    /**
     * Creates a Tree that contains the elements of the given {@link java.util.stream.Stream}.
     *
     * @param javaStream A {@link java.util.stream.Stream}
     * @param <T>        Component type of the Stream.
     * @return A Tree containing the given elements in the same order.
     */
    public static <T> Tree<T> ofAll(java.util.stream.Stream<? extends T> javaStream) {
        Objects.requireNonNull(javaStream, "javaStream is null");
        return ofAll(io.vavr.collection.Iterator.ofAll(javaStream.iterator()));
    }

    /**
     * Returns a Tree containing {@code n} values of a given Function {@code f}
     * over a range of integer values from 0 to {@code n - 1}.
     *
     * @param <T> Component type of the Tree
     * @param n   The number of elements in the Tree
     * @param f   The Function computing element values
     * @return A Tree consisting of elements {@code f(0),f(1), ..., f(n - 1)}
     * @throws NullPointerException if {@code f} is null
     */
    public static <T> Tree<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return io.vavr.collection.Collections.tabulate(n, f, empty(), Tree::of);
    }

    /**
     * Returns a Tree containing {@code n} values supplied by a given Supplier {@code s}.
     *
     * @param <T> Component type of the Tree
     * @param n   The number of elements in the Tree
     * @param s   The Supplier computing element values
     * @return A Tree of size {@code n}, where each element contains the result supplied by {@code s}.
     * @throws NullPointerException if {@code s} is null
     */
    public static <T> Tree<T> fill(int n, Supplier<? extends T> s) {
        Objects.requireNonNull(s, "s is null");
        return io.vavr.collection.Collections.fill(n, s, empty(), Tree::of);
    }

    /**
     * Returns a Tree containing {@code n} times the given {@code element}
     *
     * @param <T>     Component type of the Tree
     * @param n       The number of elements in the Tree
     * @param element The element
     * @return A Tree of size {@code n}, where each element is the given {@code element}.
     */
    public static <T> Tree<T> fill(int n, T element) {
        return io.vavr.collection.Collections.fillObject(n, element, empty(), Tree::of);
    }

    /**
     * Recursively builds a non-empty {@code Tree}, starting with the given {@code seed} value and proceeding in depth-first order.
     * <p>
     * The children of a node are created by
     * <ol>
     * <li>applying the {@code descend} function to the node value</li>
     * <li>calling this method recursively by using each derived child value as new seed (in iteration order).</li>
     * </ol>
     * <p>
     * Example:
     *
     * <pre>{@code
     * // = (1 (2 4 5) 3)
     * Tree.recurse(1, i ->
     *   (i == 1) ? List.of(2, 3) :
     *   (i == 2) ? List.(4, 5) :
     *   List.empty()
     * ).toLispString();
     * }</pre>
     *
     * @param seed    The start value for the Tree
     * @param descend A function to calculate the child values
     * @param <T>     Value type
     * @return a new, non-empty {@code Tree} instance
     * @throws NullPointerException if {@code descend} is null
     */
    public static <T> Node<T> recurse(T seed, Function<? super T, ? extends Iterable<? extends T>> descend) {
        Objects.requireNonNull(descend, "descend is null");
        return Tree.of(seed, Stream.of(seed).flatMap(descend).map(children -> recurse(children, descend)));
    }

    /**
     * Build a {@code List} with roots of {@code Tree} from flat source.
     * <p>
     * {@code parentMapper} must return {@code null} for root element.
     *
     * <pre>{@code
     *  // = [(1, null, "I"), (2, 1, "II"), (3, 1, "III"), (4, 2, "IV"), (5, 2, "V")]
     *  List<MenuItem> items = ...; // MenuItem(id, parentId, label)
     *
     *  //      I
     *  //     / \
     *  //   II  III
     *  //   /\
     *  //  IV V
     *  Tree<MenuItem> menu = Tree.build(items, MenuItem::getId, MenuItem::getParentId);
     * }</pre>
     *
     * @param source       Flat source
     * @param idMapper     A mapper from source item to unique identifier of that item
     * @param parentMapper A mapper from source item to unique identifier of parent item. Need return null for root items
     * @param <T>          Value type
     * @param <ID>         Id type
     * @return a new, maybe empty {@code List} instance with non-empty {@code Tree} instances
     * @throws NullPointerException if {@code source}, {@code idMapper} or {@code parentMapper} is null
     */
    public static <T, ID> List<Node<T>> build(Iterable<? extends T> source, Function<? super T, ? extends ID> idMapper, Function<? super T, ? extends ID> parentMapper) {
        Objects.requireNonNull(source, "source is null");
        Objects.requireNonNull(source, "idMapper is null");
        Objects.requireNonNull(source, "parentMapper is null");
        final List<T> list = List.ofAll(source);
        final Map<ID, List<T>> byParent = list.groupBy(parentMapper);
        final Function<? super T, Iterable<? extends T>> descend = idMapper
                .andThen(byParent::get)
                .andThen(o -> o.getOrElse(List::empty));
        final List<T> roots = byParent.get(null).getOrElse(List::empty);
        return roots.map(v -> recurse(v, descend));
    }

    @Override
    public final <R> Tree<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        return ofAll(iterator().<R>collect(partialFunction));
    }

    /**
     * Gets the value of this tree.
     *
     * @return The value of this tree.
     * @throws java.lang.UnsupportedOperationException if this tree is empty
     */
    public abstract T getValue();

    /**
     * Returns the children of this tree.
     *
     * @return the tree's children
     */
    public abstract io.vavr.collection.List<Node<T>> getChildren();

    /**
     * Checks if this Tree is a leaf. A tree is a leaf if it is a Node with no children.
     * Because the empty tree is no Node, it is not a leaf by definition.
     *
     * @return true if this tree is a leaf, false otherwise.
     */
    public abstract boolean isLeaf();

    /**
     * Checks if this Tree is a branch. A Tree is a branch if it is a Node which has children.
     * Because the empty tree is not a Node, it is not a branch by definition.
     *
     * @return true if this tree is a branch, false otherwise.
     */
    public final boolean isBranch() {
        return !(isEmpty() || isLeaf());
    }

    /**
     * A {@code Tree} is computed synchronously.
     *
     * @return false
     */
    @Override
    public final boolean isAsync() {
        return false;
    }

    @Override
    public final boolean isDistinct() {
        return false;
    }

    /**
     * A {@code Tree} is computed eagerly.
     *
     * @return false
     */
    @Override
    public final boolean isLazy() {
        return false;
    }

    @Override
    public final boolean isSequential() {
        return true;
    }

    /**
     * Traverses this tree values in a specific {@link Order}.
     *
     * @param order A traversal order
     * @return A new Iterator
     */
    public final io.vavr.collection.Iterator<T> iterator(Order order) {
        return values(order).iterator();
    }

    /**
     * Creates a <a href="https://www.tutorialspoint.com/lisp/lisp_tree.htm">Lisp-like</a> representation of this {@code Tree}.
     *
     * @return This {@code Tree} as Lisp-string, i.e. represented as list of lists.
     */
    public abstract String toLispString();

    /**
     * Transforms this {@code Tree}.
     *
     * @param f   A transformation
     * @param <U> Type of transformation result
     * @return An instance of type {@code U}
     * @throws NullPointerException if {@code f} is null
     */
    public final <U> U transform(Function<? super Tree<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    /**
     * Traverses this tree in {@link Order#PRE_ORDER}.
     *
     * @return A sequence of nodes.
     */
    public final Seq<Node<T>> traverse() {
        return traverse(PRE_ORDER);
    }

    /**
     * Traverses this tree in a specific order.
     *
     * @param order the tree traversal order
     * @return A sequence of nodes.
     * @throws java.lang.NullPointerException if order is null
     */
    public final Seq<Node<T>> traverse(Order order) {
        Objects.requireNonNull(order, "order is null");
        if (isEmpty()) {
            return Stream.empty();
        } else {
            final Node<T> node = (Node<T>) this;
            switch (order) {
                case PRE_ORDER:
                    return Tree.traversePreOrder(node);
                case IN_ORDER:
                    return Tree.traverseInOrder(node);
                case POST_ORDER:
                    return Tree.traversePostOrder(node);
                case LEVEL_ORDER:
                    return Tree.traverseLevelOrder(node);
                default:
                    throw new IllegalStateException("Unknown order: " + order.name());
            }
        }
    }

    /**
     * Traverses this tree values in {@link Order#PRE_ORDER}.
     * Syntactic sugar for {@code traverse().map(Node::getValue)}.
     *
     * @return A sequence of the tree values.
     */
    public final Seq<T> values() {
        return traverse(PRE_ORDER).map(Node::getValue);
    }

    /**
     * Traverses this tree values in a specific order.
     * Syntactic sugar for {@code traverse(order).map(Node::getValue)}.
     *
     * @param order the tree traversal order
     * @return A sequence of the tree values.
     * @throws java.lang.NullPointerException if order is null
     */
    public final Seq<T> values(Order order) {
        return traverse(order).map(Node::getValue);
    }

    /**
     * Counts the number of branches of this tree. The empty tree and a leaf have no branches.
     *
     * @return The number of branches of this tree.
     */
    public final int branchCount() {
        if (isEmpty() || isLeaf()) {
            return 0;
        } else {
            return getChildren().foldLeft(1, (count, child) -> count + child.branchCount());
        }
    }

    /**
     * Counts the number of leaves of this tree. The empty tree has no leaves.
     *
     * @return The number of leaves of this tree.
     */
    public final int leafCount() {
        if (isEmpty()) {
            return 0;
        } else if (isLeaf()) {
            return 1;
        } else {
            return getChildren().foldLeft(0, (count, child) -> count + child.leafCount());
        }
    }

    /**
     * Counts the number of nodes (i.e. branches and leaves) of this tree. The empty tree has no nodes.
     *
     * @return The number of nodes of this tree.
     */
    public final int nodeCount() {
        return length();
    }

    // -- Methods inherited from Traversable

    @Override
    public final Seq<T> distinct() {
        return values().distinct();
    }

    @Override
    public final Seq<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        if (isEmpty()) {
            return Stream.empty();
        } else {
            return values().distinctBy(comparator);
        }
    }

    @Override
    public final <U> Seq<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        if (isEmpty()) {
            return Stream.empty();
        } else {
            return values().distinctBy(keyExtractor);
        }
    }

    @Override
    public final Seq<T> drop(int n) {
        if (n >= length()) {
            return Stream.empty();
        } else {
            return values().drop(n);
        }
    }

    @Override
    public final Seq<T> dropRight(int n) {
        if (n >= length()) {
            return Stream.empty();
        } else {
            return values().dropRight(n);
        }
    }

    @Override
    public final Seq<T> dropUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return dropWhile(predicate.negate());
    }

    @Override
    public final Seq<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (isEmpty()) {
            return Stream.empty();
        } else {
            return values().dropWhile(predicate);
        }
    }

    @Override
    public final Seq<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (isEmpty()) {
            return Stream.empty();
        } else {
            return values().filter(predicate);
        }
    }

    @Override
    public final Seq<T> filterNot(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (isEmpty()) {
            return Stream.empty();
        } else {
            return values().filterNot(predicate);
        }
    }

    @Override
    public final <U> Tree<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return isEmpty() ? Empty.instance() : Tree.flatMap((Node<T>) this, mapper);
    }

    @Override
    public final <U> U foldRight(U zero, BiFunction<? super T, ? super U, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        if (isEmpty()) {
            return zero;
        } else {
            return iterator().foldRight(zero, f);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public final <C> Map<C, Seq<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return io.vavr.collection.Collections.groupBy(values(), classifier, Stream::ofAll);
    }

    @Override
    public final io.vavr.collection.Iterator<Seq<T>> grouped(int size) {
        return sliding(size, size);
    }

    @Override
    public final boolean hasDefiniteSize() {
        return true;
    }

    @Override
    public final T head() {
        if (isEmpty()) {
            throw new NoSuchElementException("head of empty tree");
        } else {
            return iterator().next();
        }
    }

    @Override
    public final Seq<T> init() {
        if (isEmpty()) {
            throw new UnsupportedOperationException("init of empty tree");
        } else {
            return values().init();
        }
    }

    @Override
    public final Option<Seq<T>> initOption() {
        return isEmpty() ? Option.none() : Option.some(init());
    }

    @Override
    public final boolean isTraversableAgain() {
        return true;
    }

    @Override
    public final io.vavr.collection.Iterator<T> iterator() {
        return values().iterator();
    }

    @Override
    public final <U> Tree<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return isEmpty() ? Empty.instance() : Tree.map((Node<T>) this, mapper);
    }

    @Override
    public final Tree<T> orElse(Iterable<? extends T> other) {
        return isEmpty() ? ofAll(other) : this;
    }

    @Override
    public final Tree<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return isEmpty() ? ofAll(supplier.get()) : this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Tuple2<Seq<T>, Seq<T>> partition(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (isEmpty()) {
            return Tuple.of(Stream.empty(), Stream.empty());
        } else {
            return (Tuple2<Seq<T>, Seq<T>>) values().partition(predicate);
        }
    }

    @Override
    public final Tree<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (!isEmpty()) {
            action.accept(head());
        }
        return this;
    }

    @Override
    public final Tree<T> tapEach(Consumer<? super T> action) {
        return Collections.tapEach(this, action);
    }

    @Override
    public final Tree<T> replace(T currentElement, T newElement) {
        if (isEmpty()) {
            return Empty.instance();
        } else {
            return Tree.replace((Node<T>) this, currentElement, newElement);
        }
    }

    @Override
    public final Tree<T> replaceAll(T currentElement, T newElement) {
        return map(t -> Objects.equals(t, currentElement) ? newElement : t);
    }

    @Override
    public final Seq<T> retainAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        return values().retainAll(elements);
    }

    @Override
    public final Seq<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return scanLeft(zero, operation);
    }

    @Override
    public final <U> Seq<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        return io.vavr.collection.Collections.scanLeft(this, zero, operation, io.vavr.collection.Iterator::toStream);
    }

    @Override
    public final <U> Seq<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        return io.vavr.collection.Collections.scanRight(this, zero, operation, io.vavr.collection.Iterator::toStream);
    }

    @Override
    public final io.vavr.collection.Iterator<Seq<T>> slideBy(Function<? super T, ?> classifier) {
        return iterator().slideBy(classifier);
    }

    @Override
    public final io.vavr.collection.Iterator<Seq<T>> sliding(int size) {
        return sliding(size, 1);
    }

    @Override
    public final io.vavr.collection.Iterator<Seq<T>> sliding(int size, int step) {
        return iterator().sliding(size, step);
    }

    @SuppressWarnings("unchecked")
    @Override
    public final Tuple2<Seq<T>, Seq<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (isEmpty()) {
            return Tuple.of(Stream.empty(), Stream.empty());
        } else {
            return (Tuple2<Seq<T>, Seq<T>>) values().span(predicate);
        }
    }

    @Override
    public final String stringPrefix() {
        return "Tree";
    }

    @Override
    public final Seq<T> tail() {
        if (isEmpty()) {
            throw new UnsupportedOperationException("tail of empty tree");
        } else {
            return values().tail();
        }
    }

    @Override
    public final Option<Seq<T>> tailOption() {
        return isEmpty() ? Option.none() : Option.some(tail());
    }

    @Override
    public final Seq<T> take(int n) {
        if (isEmpty()) {
            return Stream.empty();
        } else {
            return values().take(n);
        }
    }

    @Override
    public final Seq<T> takeRight(int n) {
        if (isEmpty()) {
            return Stream.empty();
        } else {
            return values().takeRight(n);
        }
    }

    @Override
    public final Seq<T> takeUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return values().takeUntil(predicate);
    }

    @Override
    public final Seq<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return values().takeWhile(predicate);
    }

    @Override
    public final <U> Tree<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        return zipWith(that, Tuple::of);
    }

    @Override
    public final <U, R> Tree<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        if (isEmpty()) {
            return Empty.instance();
        } else {
            return Tree.zip((Node<T>) this, that.iterator(), mapper);
        }
    }

    @Override
    public final <U> Tree<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        if (isEmpty()) {
            return io.vavr.collection.Iterator.<U>ofAll(that).map(elem -> Tuple.of(thisElem, elem)).toTree();
        } else {
            final java.util.Iterator<? extends U> thatIter = that.iterator();
            final Tree<Tuple2<T, U>> tree = Tree.zipAll((Node<T>) this, thatIter, thatElem);
            if (thatIter.hasNext()) {
                final Iterable<Node<Tuple2<T, U>>> remainder = io.vavr.collection.Iterator
                        .ofAll(thatIter)
                        .map(elem -> of(Tuple.of(thisElem, elem)));
                return new Node<>(tree.getValue(), tree.getChildren().appendAll(remainder));
            } else {
                return tree;
            }
        }
    }

    @Override
    public final Tree<Tuple2<T, Integer>> zipWithIndex() {
        return zipWithIndex(Tuple::of);
    }

    @Override
    public final <U> Tree<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return zipWith(io.vavr.collection.Iterator.from(0), mapper);
    }

    /**
     * Creates a neat 2-dimensional drawing of a tree. Unicode characters are used to draw node junctions.
     *
     * @return A nice string representation of the tree.
     */
    public abstract String draw();

    /**
     * Represents a tree node.
     *
     * @param <T> value type
     * @deprecated will be removed from the public API
     */
    @Deprecated
    public static final class Node<T> extends Tree<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        private final T value;
        private final io.vavr.collection.List<Node<T>> children;

        private final int size;

        /**
         * Constructs a rose tree branch.
         *
         * @param value    A value.
         * @param children A non-empty list of children.
         * @throws NullPointerException     if children is null
         * @throws IllegalArgumentException if children is empty
         */
        public Node(T value, io.vavr.collection.List<Node<T>> children) {
            Objects.requireNonNull(children, "children is null");
            this.value = value;
            this.children = children;
            this.size = children.foldLeft(1, (acc, child) -> acc + child.size);
        }

        @Override
        public io.vavr.collection.List<Node<T>> getChildren() {
            return children;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public int length() {
            return size;
        }

        @Override
        public boolean isLeaf() {
            return size == 1;
        }

        @Override
        public T last() {
            return children.isEmpty() ? value : children.last().last();
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (o instanceof Node) {
                final Node<?> that = (Node<?>) o;
                return Objects.equals(this.getValue(), that.getValue())
                        && Objects.equals(this.getChildren(), that.getChildren());
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Tuple.hash(value, children);
        }

        @Override
        public String toString() {
            return mkString(stringPrefix() + "(", ", ", ")");
        }

        @Override
        public String toLispString() {
            return toLispString(this);
        }

        @Override
        public String draw() {
            final StringBuilder builder = new StringBuilder();
            drawAux("", builder);
            return builder.toString();
        }

        private void drawAux(String indent, StringBuilder builder) {
            builder.append(value);
            for (io.vavr.collection.List<Node<T>> it = children; !it.isEmpty(); it = it.tail()) {
                final boolean isLast = it.tail().isEmpty();
                builder.append('\n')
                        .append(indent)
                        .append(isLast ? "└──" : "├──");
                it.head().drawAux(indent + (isLast ? "   " : "│  "), builder);
            }
        }

        private static String toLispString(Tree<?> tree) {
            final String value = String.valueOf(tree.getValue());
            if (tree.isLeaf()) {
                return value;
            } else {
                final String children = tree.getChildren().map(child -> toLispString(child)).mkString(" ");
                return "(" + value + " " + children + ")";
            }
        }

        // -- Serializable implementation

        /**
         * {@code writeReplace} method for the serialization proxy pattern.
         * <p>
         * The presence of this method causes the serialization system to emit a SerializationProxy instance instead of
         * an instance of the enclosing class.
         *
         * @return A SerializationProxy for this enclosing class.
         */
        private Object writeReplace() {
            return new SerializationProxy<>(this);
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
         * A serialization proxy which, in this context, is used to deserialize immutable nodes with final
         * instance fields.
         *
         * @param <T> The component type of the underlying tree.
         */
        // DEV NOTE: The serialization proxy pattern is not compatible with non-final, i.e. extendable,
        // classes. Also, it may not be compatible with circular object graphs.
        private static final class SerializationProxy<T> implements Serializable {

            private static final long serialVersionUID = 1L;

            // the instance to be serialized/deserialized
            private transient Node<T> node;

            /**
             * Constructor for the case of serialization, called by {@link Node#writeReplace()}.
             * <p/>
             * The constructor of a SerializationProxy takes an argument that concisely represents the logical state of
             * an instance of the enclosing class.
             *
             * @param node a Branch
             */
            SerializationProxy(Node<T> node) {
                this.node = node;
            }

            /**
             * Write an object to a serialization stream.
             *
             * @param s An object serialization stream.
             * @throws java.io.IOException If an error occurs writing to the stream.
             */
            private void writeObject(ObjectOutputStream s) throws IOException {
                s.defaultWriteObject();
                s.writeObject(node.value);
                s.writeObject(node.children);
            }

            /**
             * Read an object from a deserialization stream.
             *
             * @param s An object deserialization stream.
             * @throws ClassNotFoundException If the object's class read from the stream cannot be found.
             * @throws IOException            If an error occurs reading from the stream.
             */
            @SuppressWarnings("unchecked")
            private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
                s.defaultReadObject();
                final T value = (T) s.readObject();
                final io.vavr.collection.List<Node<T>> children = (io.vavr.collection.List<Node<T>>) s.readObject();
                node = new Node<>(value, children);
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
                return node;
            }
        }
    }

    /**
     * The empty tree. Use Tree.empty() to create an instance.
     *
     * @param <T> type of the tree's values
     * @deprecated will be removed from the public API
     */
    @Deprecated
    public static final class Empty<T> extends Tree<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        private static final Empty<?> INSTANCE = new Empty<>();

        // hidden
        private Empty() {
        }

        @SuppressWarnings("unchecked")
        public static <T> Empty<T> instance() {
            return (Empty<T>) INSTANCE;
        }

        @Override
        public io.vavr.collection.List<Node<T>> getChildren() {
            return List.empty();
        }

        @Override
        public T getValue() {
            throw new UnsupportedOperationException("getValue of empty Tree");
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public int length() {
            return 0;
        }

        @Override
        public boolean isLeaf() {
            return false;
        }

        @Override
        public T last() {
            throw new NoSuchElementException("last of empty tree");
        }

        @Override
        public boolean equals(Object o) {
            return o == this;
        }

        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public String toString() {
            return stringPrefix() + "()";
        }

        @Override
        public String toLispString() {
            return "()";
        }

        @Override
        public String draw() {
            return "▣";
        }

        // -- Serializable implementation

        /**
         * Instance control for object serialization.
         *
         * @return The singleton instance of Nil.
         * @see java.io.Serializable
         */
        private Object readResolve() {
            return INSTANCE;
        }
    }

    /**
     * Tree traversal order.
     * <p>
     * Example tree:
     * <pre>
     * <code>
     *         1
     *        / \
     *       /   \
     *      /     \
     *     2       3
     *    / \     /
     *   4   5   6
     *  /       / \
     * 7       8   9
     * </code>
     * </pre>
     * <p>
     * See also
     * <ul>
     * <li><a href="http://en.wikipedia.org/wiki/Tree_traversal">Tree traversal</a> (wikipedia)</li>
     * <li>See <a href="http://rosettacode.org/wiki/Tree_traversal">Tree traversal</a> (rosetta code)</li>
     * </ul>
     */
    // see http://programmers.stackexchange.com/questions/138766/in-order-traversal-of-m-way-trees
    public enum Order {

        /**
         * 1 2 4 7 5 3 6 8 9 (= depth-first)
         */
        PRE_ORDER,

        /**
         * 7 4 2 5 1 8 6 9 3
         */
        IN_ORDER,

        /**
         * 7 4 5 2 8 9 6 3 1
         */
        POST_ORDER,

        /**
         * 1 2 3 4 5 6 7 8 9 (= breadth-first)
         */
        LEVEL_ORDER
    }

    @SuppressWarnings("unchecked")
    private static <T, U> Tree<U> flatMap(Node<T> node, Function<? super T, ? extends Iterable<? extends U>> mapper) {
        final Tree<U> mapped = ofAll(mapper.apply(node.getValue()));
        if (mapped.isEmpty()) {
            return empty();
        } else {
            final io.vavr.collection.List<Node<U>> children = (io.vavr.collection.List<Node<U>>) (Object) node
                    .getChildren()
                    .map(child -> flatMap(child, mapper))
                    .filter(Tree::nonEmpty);
            return of(mapped.get(), children.prependAll(mapped.getChildren()));
        }
    }

    private static <T, U> Node<U> map(Node<T> node, Function<? super T, ? extends U> mapper) {
        final U value = mapper.apply(node.getValue());
        final io.vavr.collection.List<Node<U>> children = node.getChildren().map(child -> map(child, mapper));
        return new Node<>(value, children);
    }

    // Idea:
    // Traverse (depth-first) until a match is found, then stop and rebuild relevant parts of the tree.
    // If not found, return the same tree instance.
    private static <T> Node<T> replace(Node<T> node, T currentElement, T newElement) {
        if (Objects.equals(node.getValue(), currentElement)) {
            return new Node<>(newElement, node.getChildren());
        } else {
            for (Node<T> child : node.getChildren()) {
                final Node<T> newChild = replace(child, currentElement, newElement);
                final boolean found = newChild != child;
                if (found) {
                    final io.vavr.collection.List<Node<T>> newChildren = node.getChildren().replace(child, newChild);
                    return new Node<>(node.getValue(), newChildren);
                }
            }
            return node;
        }
    }

    private static <T> Stream<Node<T>> traversePreOrder(Node<T> node) {
        return node.getChildren().foldLeft(Stream.of(node),
                (acc, child) -> acc.appendAll(traversePreOrder(child)));
    }

    private static <T> Stream<Node<T>> traverseInOrder(Node<T> node) {
        if (node.isLeaf()) {
            return Stream.of(node);
        } else {
            final io.vavr.collection.List<Node<T>> children = node.getChildren();
            return children
                    .tail()
                    .foldLeft(Stream.<Node<T>>empty(), (acc, child) -> acc.appendAll(traverseInOrder(child)))
                    .prepend(node)
                    .prependAll(traverseInOrder(children.head()));
        }
    }

    private static <T> Stream<Node<T>> traversePostOrder(Node<T> node) {
        return node
                .getChildren()
                .foldLeft(Stream.<Node<T>>empty(), (acc, child) -> acc.appendAll(traversePostOrder(child)))
                .append(node);
    }

    private static <T> Stream<Node<T>> traverseLevelOrder(Node<T> node) {
        Stream<Node<T>> result = Stream.empty();
        final java.util.Queue<Node<T>> queue = new java.util.LinkedList<>();
        queue.add(node);
        while (!queue.isEmpty()) {
            final Node<T> next = queue.remove();
            result = result.prepend(next);
            queue.addAll(next.getChildren().toJavaList());
        }
        return result.reverse();
    }

    @SuppressWarnings("unchecked")
    private static <T, U, R> Tree<R> zip(Node<T> node, java.util.Iterator<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        if (!that.hasNext()) {
            return Empty.instance();
        } else {
            final R value = mapper.apply(node.getValue(), that.next());
            final io.vavr.collection.List<Node<R>> children = (io.vavr.collection.List<Node<R>>) (Object) node
                    .getChildren()
                    .map(child -> zip(child, that, mapper))
                    .filter(Tree::nonEmpty);
            return new Node<>(value, children);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T, U> Tree<Tuple2<T, U>> zipAll(Node<T> node, java.util.Iterator<? extends U> that, U thatElem) {
        if (!that.hasNext()) {
            return node.map(value -> Tuple.of(value, thatElem));
        } else {
            final Tuple2<T, U> value = Tuple.of(node.getValue(), that.next());
            final io.vavr.collection.List<Node<Tuple2<T, U>>> children = (io.vavr.collection.List<Node<Tuple2<T, U>>>) (Object) node
                    .getChildren()
                    .map(child -> zipAll(child, that, thatElem))
                    .filter(Tree::nonEmpty);
            return new Node<>(value, children);
        }
    }
}
