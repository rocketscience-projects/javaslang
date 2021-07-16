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

import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.Tuple3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.*;
import java.util.stream.Collector;

/**
 * A PriorityQueue.
 * @deprecated marked for removal from vavr core lib, might be moved to an extended collections module
 */
@Deprecated
public final class PriorityQueue<T> extends io.vavr.collection.AbstractQueue<T, PriorityQueue<T>> implements Serializable, Ordered<T> {

    private static final long serialVersionUID = 1L;

    private final Comparator<? super T> comparator;
    private final Seq<Node<T>> forest;
    private final int size;

    private PriorityQueue(Comparator<? super T> comparator, Seq<Node<T>> forest, int size) {
        this.comparator = comparator;
        this.forest = forest;
        this.size = size;
    }

    private PriorityQueue<T> with(Seq<Node<T>> forest, int size) {
        return new PriorityQueue<>(comparator, forest, size);
    }

    @Override
    public <R> PriorityQueue<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        return ofAll(Comparators.naturalComparator(), iterator().<R> collect(partialFunction));
    }

    /**
     * Enqueues a new element.
     *
     * @param element The new element
     * @return a new {@code PriorityQueue} instance, containing the new element
     */
    @Override
    public PriorityQueue<T> enqueue(T element) {
        final Seq<Node<T>> result = insert(comparator, element, forest);
        return with(result, size + 1);
    }

    /**
     * Enqueues the given elements. A queue has FIFO order, i.e. the first of the given elements is
     * the first which will be retrieved.
     *
     * @param elements An {@link PriorityQueue} of elements, may be empty
     * @return a new {@link PriorityQueue} instance, containing the new elements
     * @throws NullPointerException if elements is null
     */
    @Override
    public PriorityQueue<T> enqueueAll(Iterable<? extends T> elements) {
        return merge(ofAll(comparator, elements));
    }

    /**
     * Returns the first element of a non-empty {@link PriorityQueue}.
     *
     * @return The first element of this {@link PriorityQueue}.
     * @throws NoSuchElementException if this is empty
     */
    @Override
    public T head() {
        if (isEmpty()) {
            throw new NoSuchElementException("head of empty " + stringPrefix());
        } else {
            return findMin(comparator, forest).root;
        }
    }

    /**
     * Drops the first element of a non-empty {@link PriorityQueue}.
     *
     * @return A new instance of PriorityQueue containing all elements except the first.
     * @throws UnsupportedOperationException if this is empty
     */
    @Override
    public PriorityQueue<T> tail() {
        if (isEmpty()) {
            throw new UnsupportedOperationException("tail of empty " + stringPrefix());
        } else {
            return dequeue()._2;
        }
    }

    @Override
    public Tuple2<T, PriorityQueue<T>> dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("dequeue of empty " + stringPrefix());
        } else {
            final Tuple2<T, Seq<Node<T>>> dequeue = deleteMin(comparator, this.forest);
            return Tuple.of(dequeue._1, with(dequeue._2, this.size - 1));
        }
    }

    public PriorityQueue<T> merge(PriorityQueue<T> target) {
        final Seq<Node<T>> meld = meld(comparator, this.forest, target.forest);
        return with(meld, this.size + target.size);
    }

    /**
     * A {@code PriorityQueue} is computed synchronously.
     *
     * @return false
     */
    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }
    
    /**
     * A {@code PriorityQueue} is computed eagerly.
     *
     * @return false
     */
    @Override
    public boolean isLazy() {
        return false;
    }

    @Override
    public boolean isOrdered() {
        return true;
    }

    /**
     * Returns the empty PriorityQueue.
     *
     * @param <T> Component type
     * @return The empty PriorityQueue.
     */
    public static <T extends Comparable<? super T>> PriorityQueue<T> empty() {
        return empty(Comparators.naturalComparator());
    }

    public static <T> PriorityQueue<T> empty(Comparator<? super T> comparator) {
        return new PriorityQueue<>(comparator, io.vavr.collection.List.empty(), 0);
    }

    /**
     * Returns a {@link Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(Collector)} to obtain a {@code PriorityQueue<T>}.
     *
     * @param <T> Component type of the {@code PriorityQueue}.
     * @return A {@code PriorityQueue<T>} Collector.
     */
    public static <T> Collector<T, ArrayList<T>, PriorityQueue<T>> collector() {
        return Collections.toListAndThen(values -> ofAll(Comparators.naturalComparator(), values));
    }

    /**
     * Narrows a widened {@code PriorityQueue<? extends T>} to {@code PriorityQueue<T>}
     * by performing a type-safe cast. This is eligible because immutable/read-only
     * collections are covariant.
     *
     * @param queue An {@code PriorityQueue}.
     * @param <T>   Component type of the {@code PriorityQueue}.
     * @return the given {@code PriorityQueue} instance as narrowed type {@code PriorityQueue<T>}.
     */
    @SuppressWarnings("unchecked")
    public static <T> PriorityQueue<T> narrow(PriorityQueue<? extends T> queue) {
        return (PriorityQueue<T>) queue;
    }

    public static <T extends Comparable<? super T>> PriorityQueue<T> of(T element) {
        return of(Comparators.naturalComparator(), element);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<? super T>> PriorityQueue<T> of(T... elements) {
        return ofAll(Comparators.naturalComparator(), io.vavr.collection.List.of(elements));
    }

    public static <T> PriorityQueue<T> of(Comparator<? super T> comparator, T element) {
        return ofAll(comparator, io.vavr.collection.List.of(element));
    }

    @SuppressWarnings("unchecked")
    public static <T> PriorityQueue<T> of(Comparator<? super T> comparator, T... elements) {
        return ofAll(comparator, io.vavr.collection.List.of(elements));
    }

    public static <T extends Comparable<? super T>> PriorityQueue<T> ofAll(Iterable<? extends T> elements) {
        return ofAll(Comparators.naturalComparator(), elements);
    }

    @SuppressWarnings("unchecked")
    public static <T> PriorityQueue<T> ofAll(Comparator<? super T> comparator, Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (elements instanceof PriorityQueue && ((PriorityQueue) elements).comparator == comparator) {
            return (PriorityQueue<T>) elements;
        } else {
            int size = 0;
            Seq<Node<T>> forest = io.vavr.collection.List.empty();
            for (T value : elements) {
                forest = insert(comparator, value, forest);
                size++;
            }
            return new PriorityQueue<>(comparator, forest, size);
        }
    }

    public static <T extends Comparable<? super T>> PriorityQueue<T> ofAll(java.util.stream.Stream<? extends T> javaStream) {
        return ofAll(Comparators.naturalComparator(), io.vavr.collection.Iterator.ofAll(javaStream.iterator()));
    }

    public static <T> PriorityQueue<T> ofAll(Comparator<? super T> comparator, java.util.stream.Stream<? extends T> javaStream) {
        return ofAll(comparator, io.vavr.collection.Iterator.ofAll(javaStream.iterator()));
    }

    /**
     * Returns a {@link PriorityQueue} containing {@code size} values of a given Function {@code function}
     * over a range of integer values from {@code 0} to {@code size - 1}.
     *
     * @param <T>      Component type of the {@link PriorityQueue}
     * @param size     The number of elements in the {@link PriorityQueue}
     * @param function The Function computing element values
     * @return A {@link PriorityQueue} consisting of elements {@code function(0),function(1), ..., function(size - 1)}
     * @throws NullPointerException if {@code function} is null
     */
    public static <T> PriorityQueue<T> tabulate(int size, Function<? super Integer, ? extends T> function) {
        Objects.requireNonNull(function, "function is null");
        final Comparator<? super T> comparator = Comparators.naturalComparator();
        return io.vavr.collection.Collections.tabulate(size, function, empty(comparator), values -> ofAll(comparator, io.vavr.collection.List.of(values)));
    }

    /**
     * Returns a {@link PriorityQueue} containing {@code size} values supplied by a given Supplier {@code supplier}.
     *
     * @param <T>      Component type of the {@link PriorityQueue}
     * @param size     The number of elements in the {@link PriorityQueue}
     * @param supplier The Supplier computing element values
     * @return A {@link PriorityQueue} of size {@code size}, where each element contains the result supplied by {@code supplier}.
     * @throws NullPointerException if {@code supplier} is null
     */
    @SuppressWarnings("unchecked")
    public static <T> PriorityQueue<T> fill(int size, Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        final Comparator<? super T> comparator = Comparators.naturalComparator();
        return io.vavr.collection.Collections.fill(size, supplier, empty(comparator), values -> ofAll(comparator, io.vavr.collection.List.of(values)));
    }

    /**
     * Returns a {@link PriorityQueue} containing {@code n} times the given {@code element}
     *
     * @param <T>     Component type of the {@link PriorityQueue}
     * @param size    The number of elements in the {@link PriorityQueue}
     * @param element The element
     * @return A {@link PriorityQueue} of size {@code size}, where each element is the given {@code element}.
     */
    @SuppressWarnings("unchecked")
    public static <T> PriorityQueue<T> fill(int size, T element) {
        final Comparator<? super T> comparator = Comparators.naturalComparator();
        return io.vavr.collection.Collections.fillObject(size, element, empty(comparator), values -> ofAll(comparator, io.vavr.collection.List.of(values)));
    }

    @Override
    public io.vavr.collection.List<T> toList() {
        io.vavr.collection.List<T> results = io.vavr.collection.List.empty();
        for (PriorityQueue<T> queue = this; !queue.isEmpty(); ) {
            final Tuple2<T, PriorityQueue<T>> dequeue = queue.dequeue();
            results = results.prepend(dequeue._1);
            queue = dequeue._2;
        }
        return results.reverse();
    }

    /**
     * Transforms this {@code PriorityQueue}.
     *
     * @param f   A transformation
     * @param <U> Type of transformation result
     * @return An instance of type {@code U}
     * @throws NullPointerException if {@code f} is null
     */
    public final <U> U transform(Function<? super PriorityQueue<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    public PriorityQueue<T> distinct() {
        return distinctBy(comparator);
    }

    @Override
    public PriorityQueue<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return isEmpty() ? this : ofAll(comparator, iterator().distinctBy(comparator));
    }

    @Override
    public <U> PriorityQueue<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        return isEmpty() ? this : ofAll(comparator, iterator().distinctBy(keyExtractor));
    }

    @Override
    public PriorityQueue<T> drop(int n) {
        if (n <= 0 || isEmpty()) {
            return this;
        } else if (n >= length()) {
            return empty(comparator);
        } else {
            return ofAll(comparator, iterator().drop(n));
        }
    }

    @Override
    public PriorityQueue<T> dropRight(int n) {
        if (n <= 0 || isEmpty()) {
            return this;
        } else if (n >= length()) {
            return empty(comparator);
        } else {
            return ofAll(comparator, iterator().dropRight(n));
        }
    }

    @Override
    public PriorityQueue<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        PriorityQueue<T> result = this;
        while (!result.isEmpty() && predicate.test(result.head())) {
            result = result.tail();
        }
        return result;
    }

    @Override
    public PriorityQueue<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (isEmpty()) {
            return this;
        } else {
            return ofAll(comparator, iterator().filter(predicate));
        }
    }

    @Override
    public <U> PriorityQueue<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        return flatMap(Comparators.naturalComparator(), mapper);
    }

    public <U> PriorityQueue<U> flatMap(Comparator<U> comparator, Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return ofAll(comparator, iterator().flatMap(mapper));
    }

    /**
     * Accumulates the elements of this {@link PriorityQueue} by successively calling the given function {@code f} from the right,
     * starting with a value {@code zero} of type B.
     * <p>
     * Example: {@code PriorityQueue.of("a", "b", "c").foldRight("", (x, xs) -> x + xs) = "abc"}
     *
     * @param zero        Value to start the accumulation with.
     * @param accumulator The accumulator function.
     * @return an accumulated version of this.
     * @throws NullPointerException if {@code f} is null
     */
    @Override
    public <U> U foldRight(U zero, BiFunction<? super T, ? super U, ? extends U> accumulator) {
        Objects.requireNonNull(zero, "zero is null");
        Objects.requireNonNull(accumulator, "accumulator is null");
        return toList().foldRight(zero, accumulator);
    }

    @Override
    public <C> Map<C, ? extends PriorityQueue<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return io.vavr.collection.Collections.groupBy(this, classifier, (elements) -> ofAll(comparator, elements));
    }

    @Override
    public io.vavr.collection.Iterator<? extends PriorityQueue<T>> grouped(int size) {
        return sliding(size, size);
    }

    /**
     * Checks if this {@link PriorityQueue} is known to have a finite size.
     * <p>
     * This method should be implemented by classes only, i.e. not by interfaces.
     *
     * @return true, if this {@link PriorityQueue} is known to have a finite size, false otherwise.
     */
    @Override
    public boolean hasDefiniteSize() {
        return true;
    }

    /**
     * Dual of {@linkplain #tail()}, returning all elements except the last.
     *
     * @return a new instance containing all elements except the last.
     * @throws UnsupportedOperationException if this is empty
     */
    @Override
    public PriorityQueue<T> init() {
        return ofAll(comparator, iterator().init());
    }

    /**
     * Checks if this {@link PriorityQueue} can be repeatedly traversed.
     * <p>
     * This method should be implemented by classes only, i.e. not by interfaces.
     *
     * @return true, if this {@link PriorityQueue} is known to be traversable repeatedly, false otherwise.
     */
    @Override
    public boolean isTraversableAgain() {
        return true;
    }

    @Override
    public T last() {
        return Collections.last(this);
    }

    /**
     * Computes the number of elements of this {@link PriorityQueue}.
     * <p>
     * Same as {@link #size()}.
     *
     * @return the number of elements
     */
    @Override
    public int length() {
        return size;
    }

    @Override
    public <U> PriorityQueue<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return map(Comparators.naturalComparator(), mapper);
    }

    public <U> PriorityQueue<U> map(Comparator<U> comparator, Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(comparator, "comparator is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return ofAll(comparator, iterator().map(mapper));
    }

    /**
     * Returns this {@code PriorityQueue} if it is nonempty,
     * otherwise {@code PriorityQueue} created from iterable, using existing comparator.
     *
     * @param other An alternative {@code Traversable}
     * @return this {@code PriorityQueue} if it is nonempty,
     * otherwise {@code PriorityQueue} created from iterable, using existing comparator.
     */
    @Override
    public PriorityQueue<T> orElse(Iterable<? extends T> other) {
        return isEmpty() ? ofAll(comparator, other) : this;
    }

    /**
     * Returns this {@code PriorityQueue} if it is nonempty,
     * otherwise {@code PriorityQueue} created from result of evaluating supplier, using existing comparator.
     *
     * @param supplier An alternative {@code Traversable}
     * @return this {@code PriorityQueue} if it is nonempty,
     * otherwise {@code PriorityQueue} created from result of evaluating supplier, using existing comparator.
     */
    @Override
    public PriorityQueue<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return isEmpty() ? ofAll(comparator, supplier.get()) : this;
    }

    @Override
    public Tuple2<? extends PriorityQueue<T>, ? extends PriorityQueue<T>> partition(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        PriorityQueue<T> left = empty(comparator), right = left;
        for (T t : this) {
            if (predicate.test(t)) {
                left = left.enqueue(t);
            } else {
                right = right.enqueue(t);
            }
        }
        return Tuple.of(left, right);

    }

    @Override
    public PriorityQueue<T> replace(T currentElement, T newElement) {
        Objects.requireNonNull(currentElement, "currentElement is null");
        Objects.requireNonNull(newElement, "newElement is null");
        return ofAll(comparator, iterator().replace(currentElement, newElement));
    }

    @Override
    public PriorityQueue<T> replaceAll(T currentElement, T newElement) {
        Objects.requireNonNull(currentElement, "currentElement is null");
        Objects.requireNonNull(newElement, "newElement is null");
        return ofAll(comparator, iterator().replaceAll(currentElement, newElement));
    }

    @Override
    public PriorityQueue<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return io.vavr.collection.Collections.scanLeft(this, zero, operation, it -> ofAll(comparator, it));
    }

    @Override
    public <U> PriorityQueue<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        return io.vavr.collection.Collections.scanLeft(this, zero, operation, it -> ofAll(Comparators.naturalComparator(), it));
    }

    @Override
    public <U> PriorityQueue<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        return io.vavr.collection.Collections.scanRight(this, zero, operation, it -> ofAll(Comparators.naturalComparator(), it));
    }

    @Override
    public io.vavr.collection.Iterator<? extends PriorityQueue<T>> slideBy(Function<? super T, ?> classifier) {
        return iterator().slideBy(classifier).map(v -> ofAll(comparator, v));
    }

    @Override
    public io.vavr.collection.Iterator<? extends PriorityQueue<T>> sliding(int size) {
        return iterator().sliding(size).map(v -> ofAll(comparator, v));
    }

    @Override
    public io.vavr.collection.Iterator<? extends PriorityQueue<T>> sliding(int size, int step) {
        return iterator().sliding(size, step).map(v -> ofAll(comparator, v));
    }

    @Override
    public Tuple2<? extends PriorityQueue<T>, ? extends PriorityQueue<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Tuple.of(takeWhile(predicate), dropWhile(predicate));
    }

    @Override
    public PriorityQueue<T> take(int n) {
        if (n >= size() || isEmpty()) {
            return this;
        } else if (n <= 0) {
            return empty(comparator);
        } else {
            return ofAll(comparator, iterator().take(n));
        }
    }

    @Override
    public PriorityQueue<T> takeRight(int n) {
        if (n >= size() || isEmpty()) {
            return this;
        } else if (n <= 0) {
            return empty(comparator);
        } else {
            return ofAll(comparator, toArray().takeRight(n));
        }
    }

    @Override
    public PriorityQueue<T> takeUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return isEmpty() ? this : ofAll(comparator, iterator().takeUntil(predicate));
    }

    @Override
    public <U> PriorityQueue<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        Objects.requireNonNull(that, "that is null");
        return ofAll(Tuple2.comparator(comparator(), Comparators.naturalComparator()), iterator().zipWith(that, Tuple::of));
    }

    @Override
    public <U, R> PriorityQueue<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return ofAll(Comparators.naturalComparator(), iterator().zipWith(that, mapper));
    }

    @Override
    public <U> PriorityQueue<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        return ofAll(Tuple2.comparator(comparator(), Comparators.naturalComparator()), iterator().zipAll(that, thisElem, thatElem));
    }

    @Override
    public PriorityQueue<Tuple2<T, Integer>> zipWithIndex() {
        return ofAll(Tuple2.comparator(comparator(), Comparators.naturalComparator()), iterator().zipWithIndex(Tuple::of));
    }

    @Override
    public <U> PriorityQueue<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return ofAll(Comparators.naturalComparator(), iterator().zipWithIndex(mapper));
    }

    @Override
    public String stringPrefix() {
        return "PriorityQueue";
    }

    @Override
    public boolean equals(Object o) {
        return o == this || o instanceof PriorityQueue && io.vavr.collection.Collections.areEqual(this, (Iterable) o);
    }

    @Override
    public int hashCode() {
        return io.vavr.collection.Collections.hashOrdered(this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Comparator<T> comparator() {
        return (Comparator<T>) comparator;
    }

    @Override
    public PriorityQueue<T> tapEach(Consumer<? super T> action) {
        return Collections.tapEach(this, action);
    }

    /**
     * fun deleteMin [] = raise EMPTY
     * * | deleteMin ts =
     * *     val (Node (x,r,c), ts) = getMin ts
     * *     val (ts',xs') = split ([],[],c)
     * *     in fold insert xs' (meld (ts, ts')) end
     **/
    static <T> Tuple2<T, Seq<Node<T>>> deleteMin(Comparator<? super T> comparator, Seq<Node<T>> forest) {
        /* get the minimum tree and the rest of the forest */
        final Node<T> minTree = findMin(comparator, forest);
        final Seq<Node<T>> forestTail = (minTree == forest.head()) ? forest.tail() : forest.remove(minTree);

        final Seq<Node<T>> newForest = rebuild(comparator, minTree.children);
        return Tuple.of(minTree.root, meld(comparator, newForest, forestTail));
    }

    /**
     * fun insert (x, ts as t1 :: t2 :: rest) =
     * *     if rank t1 = rank t2 then skewLink(Node(x,0,[]),t1,t2) :: rest
     * *     else                      Node (x,0,[]) :: ts
     * * | insert (x, ts) =            Node (x,0,[]) :: ts
     **/
    static <T> Seq<Node<T>> insert(Comparator<? super T> comparator, T element, Seq<Node<T>> forest) {
        final Node<T> tree = Node.of(element, 0, io.vavr.collection.List.empty());
        if (forest.size() >= 2) {
            final Seq<Node<T>> tail = forest.tail();
            final Node<T> t1 = forest.head(), t2 = tail.head();
            if (t1.rank == t2.rank) {
                return tree.skewLink(comparator, t1, t2).appendTo(tail.tail());
            }
        }
        return tree.appendTo(forest);
    }

    /** fun meld (ts, ts') = meldUniq (uniqify ts, uniqify ts') */
    static <T> Seq<Node<T>> meld(Comparator<? super T> comparator, Seq<Node<T>> source, Seq<Node<T>> target) {
        return meldUnique(comparator, uniqify(comparator, source), uniqify(comparator, target));
    }

    /**
     * Find the minimum root in the forest
     * <p>
     * fun findMin [] = raise EMPTY
     * * | findMin [t] = root t
     * * | findMin (t :: ts) =
     * *     let val x = findMin ts
     * *     in if Elem.leq (root t, x) then root t else x end
     */
    static <T> Node<T> findMin(Comparator<? super T> comparator, Seq<Node<T>> forest) {
        final io.vavr.collection.Iterator<Node<T>> iterator = forest.iterator();
        Node<T> min = iterator.next();
        for (Node<T> node : iterator) {
            if (comparator.compare(node.root, min.root) < 0) {
                min = node;
            }
        }
        return min;
    }

    /**
     * Separate the rank 0 trees from the rest, rebuild the 0 rank ones and merge them back
     * <p>
     * fun split (ts,xs,[]) = (ts, xs)
     * * | split (ts,xs,t :: c) =
     * *     if rank t = 0 then split (ts,root t :: xs,c)
     * *     else               split (t :: ts,xs,c)
     */
    private static <T> Seq<Node<T>> rebuild(Comparator<? super T> comparator, Seq<Node<T>> forest) {
        Seq<Node<T>> nonZeroRank = io.vavr.collection.List.empty(), zeroRank = io.vavr.collection.List.empty();
        for (; !forest.isEmpty(); forest = forest.tail()) {
            final Node<T> initialForestHead = forest.head();
            if (initialForestHead.rank == 0) {
                zeroRank = insert(comparator, initialForestHead.root, zeroRank);
            } else {
                nonZeroRank = initialForestHead.appendTo(nonZeroRank);
            }
        }
        return meld(comparator, nonZeroRank, zeroRank);
    }

    /**
     * fun uniqify [] = []
     * *  | uniqify (t :: ts) = ins (t, ts) (∗ eliminate initial duplicate ∗)
     **/
    private static <T> Seq<Node<T>> uniqify(Comparator<? super T> comparator, Seq<Node<T>> forest) {
        return forest.isEmpty()
          ? forest
          : ins(comparator, forest.head(), forest.tail());
    }

    /**
     * fun ins (t, []) = [t]
     * * | ins (t, t' :: ts) = (∗ rank t ≤ rank t' ∗)
     * *     if rank t < rank t' then t :: t' :: ts
     * *     else                     ins (link (t, t'), ts)
     */
    private static <T> Seq<Node<T>> ins(Comparator<? super T> comparator, Node<T> tree, Seq<Node<T>> forest) {
        while (!forest.isEmpty() && tree.rank == forest.head().rank) {
            tree = tree.link(comparator, forest.head());
            forest = forest.tail();
        }
        return tree.appendTo(forest);
    }

    /**
     * fun meldUniq ([], ts) = ts
     * *  | meldUniq (ts, []) = ts
     * *  | meldUniq (t1 :: ts1, t2 :: ts2) =
     * *      if rank t1 < rank t2 then      t1 :: meldUniq (ts1, t2 :: ts2)
     * *      else if rank t2 < rank t1 then t2 :: meldUniq (t1 :: ts1, ts2)
     * *      else                           ins (link (t1, t2), meldUniq (ts1, ts2))
     **/
    private static <T> Seq<Node<T>> meldUnique(Comparator<? super T> comparator, Seq<Node<T>> forest1, Seq<Node<T>> forest2) {
        if (forest1.isEmpty()) {
            return forest2;
        } else if (forest2.isEmpty()) {
            return forest1;
        } else {
            final Node<T> tree1 = forest1.head(), tree2 = forest2.head();

            if (tree1.rank == tree2.rank) {
                final Node<T> tree = tree1.link(comparator, tree2);
                final Seq<Node<T>> forest = meldUnique(comparator, forest1.tail(), forest2.tail());
                return ins(comparator, tree, forest);
            } else {
                if (tree1.rank < tree2.rank) {
                    final Seq<Node<T>> forest = meldUnique(comparator, forest1.tail(), forest2);
                    return tree1.appendTo(forest);
                } else {
                    final Seq<Node<T>> forest = meldUnique(comparator, forest1, forest2.tail());
                    return tree2.appendTo(forest);
                }
            }
        }
    }

    /* Based on http://www.brics.dk/RS/96/37/BRICS-RS-96-37.pdf */
    static final class Node<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        final T root;
        final int rank;
        final Seq<Node<T>> children;

        private Node(T root, int rank, Seq<Node<T>> children) {
            this.root = root;
            this.rank = rank;
            this.children = children;
        }

        static <T> Node<T> of(T value, int rank, Seq<Node<T>> children) {
            return new Node<>(value, rank, children);
        }

        /*
         * fun link (t1 as Node (x1,r1,c1), t2 as Node (x2,r2,c2)) = (∗ r1 = r2 ∗)
         * *  if Elem.leq (x1,x2) then Node (x1,r1+1,t2 :: c1)
         * *  else                     Node (x2,r2+1,t1 :: c2
         */
        Node<T> link(Comparator<? super T> comparator, Node<T> tree) {
            return comparator.compare(this.root, tree.root) <= 0
              ? of(this.root, this.rank + 1, tree.appendTo(this.children))
              : of(tree.root, tree.rank + 1, this.appendTo(tree.children));
        }

        /*
         * fun skewLink (t0 as Node (x0,r0, _), t1 as Node (x1,r1,c1), t2 as Node (x2,r2,c2)) =
         * *  if Elem.leq (x1,x0) andalso Elem.leq (x1,x2) then      Node (x1,r1+1,t0 :: t2 :: c1)
         * *  else if Elem.leq (x2,x0) andalso Elem.leq (x2,x1) then Node (x2,r2+1,t0 :: t1 :: c2)
         * *  else                                                   Node (x0,r1+1,[t1, t2])
         */
        Node<T> skewLink(Comparator<? super T> comparator, Node<T> left, Node<T> right) {
            if (comparator.compare(left.root, root) <= 0 && comparator.compare(left.root, right.root) <= 0) {
                return of(left.root, left.rank + 1, appendTo(right.appendTo(left.children)));
            } else if (comparator.compare(right.root, root) <= 0) {
                return of(right.root, right.rank + 1, appendTo(left.appendTo(right.children)));
            } else {
                return of(root, left.rank + 1, io.vavr.collection.List.of(left, right));
            }
        }

        Seq<Node<T>> appendTo(Seq<Node<T>> forest) {
            return forest.prepend(this);
        }
    }
}
