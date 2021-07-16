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
import java.util.Comparator;
import java.util.Objects;
import java.util.Random;
import java.util.function.*;

/**
 * Interface for immutable sequential data structures.
 * <p>
 * Basic operations:
 *
 * <ul>
 * <li>{@link #append(Object)}</li>
 * <li>{@link #appendAll(Iterable)}</li>
 * <li>{@link #insert(int, Object)}</li>
 * <li>{@link #insertAll(int, Iterable)}</li>
 * <li>{@link #prepend(Object)}</li>
 * <li>{@link #prependAll(Iterable)}</li>
 * <li>{@link #update(int, Object)}</li>
 * </ul>
 *
 * Conversion:
 *
 * <ul>
 * <li>{@link #asPartialFunction}</li>
 * </ul>
 *
 * Filtering:
 *
 * <ul>
 * <li>{@link #remove(Object)}</li>
 * <li>{@link #removeAll(Object)}</li>
 * <li>{@link #removeAll(Iterable)}</li>
 * <li>{@link #removeAt(int)}</li>
 * <li>{@link #removeFirst(Predicate)}</li>
 * <li>{@link #removeLast(Predicate)}</li>
 * </ul>
 *
 * Selection:
 *
 * <ul>
 * <li>{@link #get(int)}</li>
 * <li>{@link #indexOf(Object)}</li>
 * <li>{@link #indexOf(Object, int)}</li>
 * <li>{@link #lastIndexOf(Object)}</li>
 * <li>{@link #lastIndexOf(Object, int)}</li>
 * <li>{@link #slice(int, int)}</li>
 * <li>{@link #subSequence(int)}</li>
 * <li>{@link #subSequence(int, int)}</li>
 * </ul>
 *
 * Transformation:
 *
 * <ul>
 * <li>{@link #crossProduct()}</li>
 * <li>{@link #crossProduct(int)}</li>
 * <li>{@link #crossProduct(Iterable)}</li>
 * <li>{@link #combinations()}</li>
 * <li>{@link #combinations(int)}</li>
 * <li>{@link #intersperse(Object)}</li>
 * <li>{@link #padTo(int, Object)}</li>
 * <li>{@link #permutations()}</li>
 * <li>{@link #reverse()}</li>
 * <li>{@link #shuffle()}</li>
 * <li>{@link #sorted()}</li>
 * <li>{@link #sorted(Comparator)}</li>
 * <li>{@link #splitAt(int)}</li>
 * <li>{@link #zip(Iterable)}</li>
 * <li>{@link #zipAll(Iterable, Object, Object)}</li>
 * <li>{@link #zipWithIndex()}</li>
 * </ul>
 *
 * Traversal:
 *
 * <ul>
 * <li>{@link #iterator(int)}</li>
 * </ul>
 *
 * Views:
 *
 * <ul>
 * <li>{@link #asJava()}</li>
 * <li>{@link #asJava(Consumer)}</li>
 * <li>{@link #asJavaMutable()}</li>
 * <li>{@link #asJavaMutable(Consumer)}</li>
 * </ul>
 *
 * @param <T> Component type
 */
public interface Seq<T> extends Traversable<T>, PartialFunction<Integer, T>, Serializable {

    long serialVersionUID = 1L;

    /**
     * Narrows a widened {@code Seq<? extends T>} to {@code Seq<T>}
     * by performing a type-safe cast. This is eligible because immutable/read-only
     * collections are covariant.
     *
     * @param seq A {@code Seq}.
     * @param <T> Component type of the {@code Seq}.
     * @return the given {@code seq} instance as narrowed type {@code Seq<T>}.
     */
    @SuppressWarnings("unchecked")
    static <T> Seq<T> narrow(Seq<? extends T> seq) {
        return (Seq<T>) seq;
    }

    /**
     * Appends an element to this.
     *
     * @param element An element
     * @return A new Seq containing the given element appended to this elements
     */
    Seq<T> append(T element);

    /**
     * Appends all given elements to this.
     *
     * @param elements An Iterable of elements
     * @return A new Seq containing the given elements appended to this elements
     * @throws NullPointerException if {@code elements} is null
     */
    Seq<T> appendAll(Iterable<? extends T> elements);

    /**
     * A {@code Seq} is a partial function which returns the element at the specified index if the
     * index is valid. It's up to the caller to make sure the index is valid (for instance through
     * {@code isDefinedAt}).
     * The behaviour is undefined if the index is out of bounds.
     * It may throw any {@code RuntimeException} or return an arbitrary value.
     *
     * @param index an index
     * @return the element at the given index
     */
    T apply(Integer index);
    
    /**
     * Creates an <strong>immutable</strong> {@link java.util.List} view on top of this {@code Seq},
     * i.e. calling mutators will result in {@link UnsupportedOperationException} at runtime.
     * <p>
     * The difference to conversion methods {@code toJava*()} is that
     *
     * <ul>
     * <li>A view is created in O(1) (constant time) whereas conversion takes O(n) (linear time), with n = collection size.</li>
     * <li>The operations on a view have the same performance characteristics than the underlying persistent Vavr collection whereas the performance characteristics of a converted collection are those of the Java standard collections.</li>
     * </ul>
     *
     * Please note that our immutable {@code java.util.List} view throws {@code UnsupportedOperationException} before
     * checking method arguments. Java does handle this case inconsistently.
     *
     * @return A new immutable {@link java.util.Collection} view on this {@code Traversable}.
     */
    java.util.List<T> asJava();

    /**
     * Creates an <strong>immutable</strong> {@link java.util.List} view on top of this {@code Seq}
     * that is passed to the given {@code action}.
     *
     * @param action A side-effecting unit of work that operates on an immutable {@code java.util.List} view.
     * @return this instance
     * @see Seq#asJava()
     */
    Seq<T> asJava(Consumer<? super java.util.List<T>> action);

    /**
     * Creates a <strong>mutable</strong> {@link java.util.List} view on top of this {@code Seq},
     * i.e. all mutator methods of the {@link java.util.List} are implemented.
     *
     * @return A new mutable {@link java.util.Collection} view on this {@code Traversable}.
     * @see Seq#asJava()
     */
    java.util.List<T> asJavaMutable();

    /**
     * Creates a <strong>mutable</strong> {@link java.util.List} view on top of this {@code Seq}
     * that is passed to the given {@code action}.
     *
     * @param action A side-effecting unit of work that operates on a mutable {@code java.util.List} view.
     * @return this instance, if only read operations are performed on the {@code java.util.List} view or a new instance of this type, if write operations are performed on the {@code java.util.List} view.
     * @see Seq#asJavaMutable()
     */
    Seq<T> asJavaMutable(Consumer<? super java.util.List<T>> action);

    /**
     * Turns this {@code Seq} into a {@link PartialFunction} which is defined at a specific index, if this {@code Seq}
     * contains at least index + 1 elements. When applied to a defined index, the partial function will return
     * the value of this {@code Seq} at the specified index.
     *
     * @return a new {@link PartialFunction}
     * @throws IndexOutOfBoundsException if this is empty, index &lt; 0 or index &gt;= length()
     */
    PartialFunction<Integer, T> asPartialFunction() throws IndexOutOfBoundsException;

    @Override
    <R> Seq<R> collect(PartialFunction<? super T, ? extends R> partialFunction);

    /**
     * Returns the union of all combinations from k = 0 to length().
     * <p>
     * Examples:
     * <pre>
     * <code>
     * [].combinations() = [[]]
     *
     * [1,2,3].combinations() = [
     *   [],                  // k = 0
     *   [1], [2], [3],       // k = 1
     *   [1,2], [1,3], [2,3], // k = 2
     *   [1,2,3]              // k = 3
     * ]
     * </code>
     * </pre>
     *
     * @return the combinations of this
     */
    Seq<? extends Seq<T>> combinations();

    /**
     * Returns the k-combination of this traversable, i.e. all subset of this of k distinct elements.
     *
     * @param k Size of subsets
     * @return the k-combination of this elements
     * @see <a href="http://en.wikipedia.org/wiki/Combination">Combination</a>
     */
    Seq<? extends Seq<T>> combinations(int k);

    /**
     * Tests whether this sequence contains a given sequence as a slice.
     * <p>
     * Note: may not terminate for infinite sequences.
     *
     * @param that the sequence to test
     * @return true if this sequence contains a slice with the same elements as that, otherwise false.
     * @throws NullPointerException if {@code that} is null.
     */
    default boolean containsSlice(Iterable<? extends T> that) {
        Objects.requireNonNull(that, "that is null");
        return indexOfSlice(that) >= 0;
    }

    /**
     * Calculates the cross product (, i.e. square) of {@code this x this}.
     * <p>
     * Example:
     * <pre>
     * <code>
     * // = List of Tuples (1, 1), (1, 2), (1, 3), (2, 1), (2, 2), (2, 3), (3, 1), (3, 2), (3, 3)
     * List.of(1, 2, 3).crossProduct();
     * </code>
     * </pre>
     *
     * @return a new Iterator containing the square of {@code this}
     */
    default Iterator<Tuple2<T, T>> crossProduct() {
        return crossProduct(this);
    }

    /**
     * Calculates the n-ary cartesian power (or <em>cross product</em> or simply <em>product</em>) of this.
     * <p>
     * Example:
     * <pre>
     * <code>
     * // = ((A,A), (A,B), (A,C), ..., (B,A), (B,B), ..., (Z,Y), (Z,Z))
     * CharSeq.rangeClosed('A', 'Z').crossProduct(2);
     * </code>
     * </pre>
     * <p>
     * Cartesian power of negative value will return empty iterator.
     * <p>
     * Example:
     * <pre>
     * <code>
     * // = ()
     * CharSeq.rangeClosed('A', 'Z').crossProduct(-1);
     * </code>
     * </pre>
     *
     * @param power the number of cartesian multiplications
     * @return A new Iterator representing the n-ary cartesian power of this
     */
    Iterator<? extends Seq<T>> crossProduct(int power);

    /**
     * Calculates the cross product {@code this x that}.
     * <p>
     * Example:
     * <pre>
     * <code>
     * // = List of Tuples (1, 'a'), (1, 'b'), (2, 'a'), (2, 'b'), (3, 'a'), (3, 'b')
     * List.of(1, 2, 3).crossProduct(List.of('a', 'b');
     * </code>
     * </pre>
     *
     * @param that Another iterable
     * @param <U>  Component type
     * @return a new Iterator containing the cross product {@code this x that}
     * @throws NullPointerException if that is null
     */
    default <U> Iterator<Tuple2<T, U>> crossProduct(Iterable<? extends U> that) {
        Objects.requireNonNull(that, "that is null");
        final Stream<U> other = Stream.ofAll(that);
        return Iterator.ofAll(this).flatMap(a -> other.map(b -> Tuple.of(a, b)));
    }

    /**
     * Tests whether this sequence ends with the given sequence.
     * <p>
     * Note: If the both the receiver object this and the argument that are infinite sequences this method may not terminate.
     *
     * @param that the sequence to test
     * @return true if this sequence has that as a suffix, false otherwise.
     */
    default boolean endsWith(Seq<? extends T> that) {
        Objects.requireNonNull(that, "that is null");
        final Iterator<T> i = this.iterator().drop(length() - that.length());
        final Iterator<? extends T> j = that.iterator();
        while (i.hasNext() && j.hasNext()) {
            if (!Objects.equals(i.next(), j.next())) {
                return false;
            }
        }
        return !j.hasNext();
    }

    /**
     * Returns the element at the specified index.
     *
     * @param index an index
     * @return the element at the given index
     * @throws IndexOutOfBoundsException if this is empty, index &lt; 0 or index &gt;= length()
     */
    default T get(int index) {
        if (isDefinedAt(index)) {
            return apply(index);
        }
        throw new IndexOutOfBoundsException("get(" + index + ")");
    }

    /**
     * Returns an optional value, which may element at the specified index, or be empty
     * if the index is invalid for this collection.
     *
     * @param index an index
     * @return an optional value containing the element at the given index, or being empty if this is empty, index &lt; 0 or index &gt;= length()
     */
    default Option<T> getOption(int index) {
        return isDefinedAt(index) ? Option.some(apply(index)) : Option.none();
    }

    /**
     * Returns the element at the specified index, or the default value you give if
     * the index is invalid for this collection.
     *
     * @param index an index
     * @param defaultValue a default value to use if the index is invalid for this collection.
     * @return the element at the given index or the default value if this is empty, index &lt; 0 or index &gt;= length()
     */
    default T getOrElse(int index, T defaultValue) {
        return isDefinedAt(index) ? apply(index) : defaultValue;
    }

    /**
     * Returns the index of the first occurrence of the given element or -1 if this does not contain the given element.
     *
     * @param element an element
     * @return the index of the first occurrence of the given element
     */
    default int indexOf(T element) {
        return indexOf(element, 0);
    }

    /**
     * Returns the index of the first occurrence of the given element as an {@code Option}
     *
     * @param element an element
     * @return {@code Some(index)} or {@code None} if not found.
     */
    default Option<Integer> indexOfOption(T element) {
        return Collections.indexOption(indexOf(element));
    }

    /**
     * Returns the index of the first occurrence of the given element after or at some start index
     * or -1 if this does not contain the given element.
     *
     * @param element an element
     * @param from    start index
     * @return the index of the first occurrence of the given element
     */
    int indexOf(T element, int from);

    /**
     * Returns the index of the first occurrence of the given element,
     * after or at some start index as an {@code Option}
     *
     * @param element an element
     * @param from    start index
     * @return {@code Some(index)} or {@code None} if not found.
     */
    default Option<Integer> indexOfOption(T element, int from) {
        return Collections.indexOption(indexOf(element, from));
    }

    /**
     * Finds first index where this sequence contains a given sequence as a slice.
     * <p>
     * Note: may not terminate for infinite sequences.
     *
     * @param that the sequence to test
     * @return the first index such that the elements of this sequence starting at this index match
     * the elements of sequence that, or -1 of no such slice exists.
     * @throws NullPointerException if {@code that} is null.
     */
    default int indexOfSlice(Iterable<? extends T> that) {
        return indexOfSlice(that, 0);
    }

    /**
     * Finds first index where this sequence contains a given sequence as an {@code Option} of a slice.
     * <p>
     * Note: may not terminate for infinite sequences.
     *
     * @param that the sequence to test
     * @return {@code Some(index)} or {@code None} if not found.
     */
    default Option<Integer> indexOfSliceOption(Iterable<? extends T> that) {
        return Collections.indexOption(indexOfSlice(that));
    }

    /**
     * Finds first index after or at a start index where this sequence contains a given sequence as a slice.
     * Note: may not terminate for infinite sequences.
     *
     * @param that the sequence to test
     * @param from the start index
     * @return the first index &gt;= from such that the elements of this sequence starting at this index match
     * the elements of sequence that, or -1 of no such slice exists.
     * @throws NullPointerException if {@code that} is null.
     */
    int indexOfSlice(Iterable<? extends T> that, int from);

    /**
     * Finds first index after or at a start index where this sequence contains a given sequence as an {@code Option} of a slice.
     * Note: may not terminate for infinite sequences.
     *
     * @param that the sequence to test
     * @param from the start index
     * @return {@code Some(index)} or {@code None} if not found.
     */
    default Option<Integer> indexOfSliceOption(Iterable<? extends T> that, int from) {
        return Collections.indexOption(indexOfSlice(that, from));
    }

    /**
     * Finds index of first element satisfying some predicate.
     *
     * @param predicate the predicate used to test elements.
     * @return the index of the first element of this Seq that satisfies the given
     * {@code predicate}, or {@code -1}, if none exists.
     */
    default int indexWhere(Predicate<? super T> predicate) {
        return indexWhere(predicate, 0);
    }

    /**
     * Finds index of first element satisfying some predicate as an {@code Option}.
     *
     * @param predicate the predicate used to test elements.
     * @return {@code Some(index)} or {@code None} if not found.
     */
    default Option<Integer> indexWhereOption(Predicate<? super T> predicate) {
        return Collections.indexOption(indexWhere(predicate));
    }

    /**
     * Finds index of the first element satisfying some predicate after or at
     * some start index.
     *
     * @param predicate the predicate used to test elements.
     * @param from      the start index
     * @return the index {@code >= from} of the first element of this Seq that
     * satisfies the given {@code predicate}, or {@code -1}, if none exists.
     */
    int indexWhere(Predicate<? super T> predicate, int from);

    /**
     * Finds index of the first element satisfying some predicate after or at
     * some start index as an {@code Option}.
     *
     * @param predicate the predicate used to test elements.
     * @param from      the start index
     * @return {@code Some(index)} or {@code None} if not found.
     */
    default Option<Integer> indexWhereOption(Predicate<? super T> predicate, int from) {
        return Collections.indexOption(indexWhere(predicate, from));
    }

    /**
     * Inserts the given element at the specified index.
     *
     * @param index   an index
     * @param element an element
     * @return a new Seq, where the given element is inserted into this at the given index
     * @throws IndexOutOfBoundsException if this is empty, index &lt; 0 or index &gt;= length()
     */
    Seq<T> insert(int index, T element);

    /**
     * Inserts the given elements at the specified index.
     *
     * @param index    an index
     * @param elements An Iterable of elements
     * @return a new Seq, where the given elements are inserted into this at the given index
     * @throws IndexOutOfBoundsException if this is empty, index &lt; 0 or index &gt;= length()
     */
    Seq<T> insertAll(int index, Iterable<? extends T> elements);

    /**
     * Inserts an element between all elements of this Traversable.
     *
     * @param element An element.
     * @return an interspersed version of this
     */
    Seq<T> intersperse(T element);

    /**
     * Returns an iterator of this elements starting at the given index.
     * The result is equivalent to {@code this.subSequence(index).iterator()}.
     *
     * @param index an index
     * @return a new Iterator, starting with the element at the given index or the empty Iterator, if index = length()
     * @throws IndexOutOfBoundsException if index &lt; 0 or index &gt; length()
     */
    default Iterator<T> iterator(int index) {
        return subSequence(index).iterator();
    }

    /**
     * Returns the index of the last occurrence of the given element or -1 if this does not contain the given element.
     *
     * @param element an element
     * @return the index of the last occurrence of the given element
     */
    default int lastIndexOf(T element) {
        return lastIndexOf(element, Integer.MAX_VALUE);
    }

    /**
     * Returns the index of the last occurrence of the given element as an {@code Option}.
     *
     * @param element an element
     * @return {@code Some(index)} or {@code None} if not found.
     */
    default Option<Integer> lastIndexOfOption(T element) {
        return Collections.indexOption(lastIndexOf(element));
    }

    /**
     * Finds index of last element satisfying some predicate.
     *
     * @param predicate the predicate used to test elements.
     * @return the index of the last element of this Seq that satisfies the given {@code predicate}, or {@code -1},
     * if none exists.
     */
    default int lastIndexWhere(Predicate<? super T> predicate) {
        return lastIndexWhere(predicate, length() - 1);
    }

    /**
     * Finds index of last element satisfying some predicate as an {@code Option}.
     *
     * @param predicate the predicate used to test elements.
     * @return {@code Some(index)} or {@code None} if not found.
     */
    default Option<Integer> lastIndexWhereOption(Predicate<? super T> predicate) {
        return Collections.indexOption(lastIndexWhere(predicate));
    }

    /**
     * Finds index of last element satisfying some predicate before or at given
     * end index.
     *
     * @param predicate the predicate used to test elements.
     * @param end       the maximum index of the search
     * @return the index {@code <= end} of the last element of this Seq that
     * satisfies the given {@code predicate}, or {@code -1}, if none exists.
     */
    int lastIndexWhere(Predicate<? super T> predicate, int end);

    /**
     * Finds index of last element satisfying some predicate before or at given end index as an {@code Option}.
     *
     * @param predicate the predicate used to test elements.
     * @param end       the maximum index of the search
     * @return {@code Some(index)} or {@code None} if not found.
     */
    default Option<Integer> lastIndexWhereOption(Predicate<? super T> predicate, int end) {
        return Collections.indexOption(lastIndexWhere(predicate, end));
    }

    /**
     * Turns this sequence into a plain function returning an Option result.
     *
     * @return a function that takes an index i and returns the value of
     * this sequence in a Some if the index is within bounds, otherwise a None.
     * @deprecated Will be removed
     */
    @Deprecated
    default Function1<Integer, Option<T>> lift() {
        return i -> (i >= 0 && i < length()) ? Option.some(apply(i)) : Option.none();
    }

    /**
     * Returns the index of the last occurrence of the given element before or at a given end index
     * or -1 if this does not contain the given element.
     *
     * @param element an element
     * @param end     the end index
     * @return the index of the last occurrence of the given element
     */
    int lastIndexOf(T element, int end);

    /**
     * Returns the index of the last occurrence of the given element before or at a given end index as an {@code Option}.
     *
     * @param element an element
     * @param end     the end index
     * @return {@code Some(index)} or {@code None} if not found.
     */
    default Option<Integer> lastIndexOfOption(T element, int end) {
        return Collections.indexOption(lastIndexOf(element, end));
    }

    /**
     * Finds last index where this sequence contains a given sequence as a slice.
     * <p>
     * Note: will not terminate for infinite sequences.
     *
     * @param that the sequence to test
     * @return the last index such that the elements of this sequence starting a this index match the elements
     * of sequence that, or -1 of no such slice exists.
     * @throws NullPointerException if {@code that} is null.
     */
    default int lastIndexOfSlice(Iterable<? extends T> that) {
        return lastIndexOfSlice(that, Integer.MAX_VALUE);
    }

    /**
     * Finds last index where this sequence contains a given sequence as a slice as an {@code Option}.
     *
     * @param that the sequence to test
     * @return {@code Some(index)} or {@code None} if not found.
     */
    default Option<Integer> lastIndexOfSliceOption(Iterable<? extends T> that) {
        return Collections.indexOption(lastIndexOfSlice(that));
    }

    /**
     * Finds last index before or at a given end index where this sequence contains a given sequence as a slice.
     *
     * @param that the sequence to test
     * @param end  the end index
     * @return the last index &lt;= end such that the elements of this sequence starting at this index match
     * the elements of sequence that, or -1 of no such slice exists.
     * @throws NullPointerException if {@code that} is null.
     */
    int lastIndexOfSlice(Iterable<? extends T> that, int end);

    /**
     * Finds last index before or at a given end index where this sequence contains a given sequence as a slice as an {@code Option}.
     *
     * @param that the sequence to test
     * @param end  the end index
     * @return {@code Some(index)} or {@code None} if not found.
     */
    default Option<Integer> lastIndexOfSliceOption(Iterable<? extends T> that, int end) {
        return Collections.indexOption(lastIndexOfSlice(that, end));
    }

    /**
     * A copy of this sequence with an element appended until a given target length is reached.
     * <p>
     * Note: lazily-evaluated Seq implementations need to process all elements in order to gather the overall length.
     *
     * @param length  the target length
     * @param element the padding element
     * @return a new sequence consisting of all elements of this sequence followed by the minimal number
     * of occurrences of <code>element</code> so that the resulting sequence has a length of at least <code>length</code>.
     */
    Seq<T> padTo(int length, T element);

    /**
     * A copy of this sequence with an element prepended until a given target length is reached.
     * <p>
     * Note: lazily-evaluated Seq implementations need to process all elements in order to gather the overall length.
     *
     * @param length  the target length
     * @param element the padding element
     * @return a new sequence consisting of all elements of this sequence prepended by the minimal number
     * of occurrences of <code>element</code> so that the resulting sequence has a length of at least <code>length</code>.
     */
    Seq<T> leftPadTo(int length, T element);

    /**
     * Produces a new list where a slice of elements in this list is replaced by another sequence.
     *
     * @param from     the index of the first replaced element
     * @param that     sequence for replacement
     * @param replaced the number of elements to drop in the original list
     * @return a new sequence.
     */
    Seq<T> patch(int from, Iterable<? extends T> that, int replaced);

    /**
     * Computes all unique permutations.
     * <p>
     * Example:
     * <pre>
     * <code>
     * [].permutations() = []
     *
     * [1,2,3].permutations() = [
     *   [1,2,3],
     *   [1,3,2],
     *   [2,1,3],
     *   [2,3,1],
     *   [3,1,2],
     *   [3,2,1]
     * ]
     * </code>
     * </pre>
     *
     * @return this unique permutations
     */
    Seq<? extends Seq<T>> permutations();

    /**
     * Returns the length of the longest prefix whose elements all satisfy some predicate.
     *
     * Note: may not terminate for infinite-sized collections.
     *
     * @param predicate the predicate used to test elements.
     * @return the length of the longest prefix of this general sequence such that every
     * element of the segment satisfies the predicate p.
     */
    default int prefixLength(Predicate<? super T> predicate) {
        return segmentLength(predicate, 0);
    }

    /**
     * Prepends an element to this.
     *
     * @param element An element
     * @return A new Seq containing the given element prepended to this elements
     */
    Seq<T> prepend(T element);

    /**
     * Prepends all given elements to this.
     *
     * @param elements An Iterable of elements
     * @return A new Seq containing the given elements prepended to this elements
     */
    Seq<T> prependAll(Iterable<? extends T> elements);

    /**
     * Removes the first occurrence of the given element.
     *
     * @param element An element to be removed from this Seq.
     * @return a Seq containing all elements of this without the first occurrence of the given element.
     */
    Seq<T> remove(T element);

    /**
     * Removes all occurrences of the given element.
     *
     * @param element An element to be removed from this Seq.
     * @return a Seq containing all elements of this but not the given element.
     */
    Seq<T> removeAll(T element);

    /**
     * Removes all occurrences of the given elements.
     *
     * @param elements Elements to be removed from this Seq.
     * @return a Seq containing all elements of this but none of the given elements.
     * @throws NullPointerException if {@code elements} is null
     */
    Seq<T> removeAll(Iterable<? extends T> elements);

    /**
     * Removes the element at the specified position in this sequence. Shifts any subsequent elements to the left
     * (subtracts one from their indices).
     *
     * @param index position of element to remove
     * @return a sequence containing all elements of this without the element at the specified position.
     * @throws IndexOutOfBoundsException if this is empty, index &lt; 0 or index &gt;= length()
     */
    Seq<T> removeAt(int index);

    /**
     * Removes the first occurrence that satisfy predicate
     *
     * @param predicate an predicate
     * @return a new Seq
     */
    Seq<T> removeFirst(Predicate<T> predicate);

    /**
     * Removes the last occurrence that satisfy predicate
     *
     * @param predicate an predicate
     * @return a new Seq
     */
    Seq<T> removeLast(Predicate<T> predicate);

    /**
     * Reverses the order of elements.
     *
     * @return the reversed elements.
     */
    Seq<T> reverse();

    /**
     * An iterator yielding elements in reversed order.
     * <p>
     * Note: {@code xs.reverseIterator()} is the same as {@code xs.reverse().iterator()} but might
     * be more efficient.
     *
     * @return an iterator yielding the elements of this Seq in reversed order
     */
    Iterator<T> reverseIterator();

    /**
     * Circular rotates the elements by the specified distance to the left direction.
     * 
     * <pre>{@code
     * // = List(3, 4, 5, 1, 2)
     * List.of(1, 2, 3, 4, 5).rotateLeft(2);
     * }</pre>
     *
     * @param n distance of left rotation
     * @return the rotated elements.
     */
    Seq<T> rotateLeft(int n);

    /**
     * Circular rotates the elements by the specified distance to the right direction.
     *
     * <pre>{@code
     * // = List(4, 5, 1, 2, 3)
     * List.of(1, 2, 3, 4, 5).rotateRight(2);
     * }</pre>
     *
     * @param n distance of right rotation
     * @return the rotated elements.
     */
    Seq<T> rotateRight(int n);

    /**
     * Computes length of longest segment whose elements all satisfy some predicate.
     * <p>
     * Note: may not terminate for infinite sequences.
     *
     * @param predicate the predicate used to test elements.
     * @param from      the index where the search starts.
     * @return the length of the longest segment of this sequence starting from index
     * from such that every element of the segment satisfies the predicate p.
     */
    int segmentLength(Predicate<? super T> predicate, int from);

    /**
     * Randomize the order of the elements in the current sequence.
     *
     * @return a sequence with the same elements as the current sequence in a random order.
     */
    Seq<T> shuffle();

    /**
     * Randomize the order of the elements in the current sequence using the given source
     * of randomness.
     *
     * @param random A random number generator
     *
     * @return a sequence with the same elements as the current sequence in a random order.
     */
    Seq<T> shuffle(Random random);

    /**
     * Returns a Seq that is a <em>slice</em> of this. The slice begins with the element at the specified
     * {@code beginIndex} and extends to the element at index {@code endIndex - 1}.
     * <p>
     * Examples:
     *
     * <pre>
     * <code>
     * List.of(1, 2, 3, 4).slice(1, 3); // = (2, 3)
     * List.of(1, 2, 3, 4).slice(0, 4); // = (1, 2, 3, 4)
     * List.of(1, 2, 3, 4).slice(2, 2); // = ()
     * List.of(1, 2).slice(1, 0);       // = ()
     * List.of(1, 2).slice(-10, 10);    // = (1, 2)
     * </code>
     * </pre>
     *
     * See also {@link #subSequence(int, int)} which throws in some cases instead of returning a sequence.
     *
     * @param beginIndex the beginning index, inclusive
     * @param endIndex   the end index, exclusive
     * @return the specified slice
     */
    Seq<T> slice(int beginIndex, int endIndex);

    /**
     * Sorts this elements according to their natural order. If this elements are not
     * {@code Comparable}, a {@code java.lang.ClassCastException} may be thrown.
     *
     * @return A sorted version of this
     * @throws ClassCastException if this elements are not {@code Comparable}
     */
    Seq<T> sorted();

    /**
     * Sorts this elements according to the provided {@code Comparator}. If this elements are not
     * {@code Comparable}, a {@code java.lang.ClassCastException} may be thrown.
     *
     * @param comparator A comparator
     * @return a sorted version of this
     */
    Seq<T> sorted(Comparator<? super T> comparator);

    /**
     * Sorts this elements by comparing the elements in a different domain, using the given {@code mapper}.
     *
     * @param mapper A mapper
     * @param <U>    The domain where elements are compared
     * @return a sorted version of this
     * @throws NullPointerException if {@code mapper} is null
     */
    <U extends Comparable<? super U>> Seq<T> sortBy(Function<? super T, ? extends U> mapper);

    /**
     * Sorts this elements by comparing the elements in a different domain, using the given {@code mapper}.
     *
     * @param comparator A comparator
     * @param mapper     A mapper
     * @param <U>        The domain where elements are compared
     * @return a sorted version of this
     * @throws NullPointerException if {@code comparator} or {@code mapper} is null
     */
    <U> Seq<T> sortBy(Comparator<? super U> comparator, Function<? super T, ? extends U> mapper);

    /**
     * Splits a Seq at the specified index. The result of {@code splitAt(n)} is equivalent to
     * {@code Tuple.of(take(n), drop(n))}.
     *
     * @param n An index.
     * @return A {@link Tuple} containing the first n and the remaining elements.
     */
    Tuple2<? extends Seq<T>, ? extends Seq<T>> splitAt(int n);

    /**
     * Splits a sequence at the first element which satisfies the {@link Predicate}, e.g. Tuple(init, element+tail).
     *
     * @param predicate An predicate
     * @return A {@link Tuple} containing divided sequences
     */
    Tuple2<? extends Seq<T>, ? extends Seq<T>> splitAt(Predicate<? super T> predicate);

    /**
     * Splits a sequence at the first element which satisfies the {@link Predicate}, e.g. Tuple(init+element, tail).
     *
     * @param predicate An predicate
     * @return A {@link Tuple} containing divided sequences
     */
    Tuple2<? extends Seq<T>, ? extends Seq<T>> splitAtInclusive(Predicate<? super T> predicate);

    /**
     * Tests whether this list starts with the given sequence.
     *
     * @param that the sequence to test
     * @return true if that is empty or that is prefix of this collection, false otherwise.
     */
    default boolean startsWith(Iterable<? extends T> that) {
        return startsWith(that, 0);
    }

    /**
     * Tests whether this list contains the given sequence at a given index.
     * <p>
     * Note: If the both the receiver object this and the argument that are infinite sequences this method may not terminate.
     *
     * @param that   the sequence to test
     * @param offset the index where the sequence is searched.
     * @return true if that is empty or that is prefix of this collection starting from the given offset, false otherwise.
     */
    default boolean startsWith(Iterable<? extends T> that, int offset) {
        Objects.requireNonNull(that, "that is null");
        if (offset < 0) { return false; }
        final Iterator<T> i = this.iterator().drop(offset);
        final java.util.Iterator<? extends T> j = that.iterator();
        while (i.hasNext() && j.hasNext()) {
            if (!Objects.equals(i.next(), j.next())) {
                return false;
            }
        }
        return !j.hasNext();
    }

    /**
     * Returns a Seq that is a subsequence of this. The subsequence begins with the element at the specified
     * {@code beginIndex} and extends to the end of this Seq.
     * <p>
     * Examples:
     *
     * <pre>
     * <code>
     * List.of(1, 2).subSequence(0);     // = (1, 2)
     * List.of(1, 2).subSequence(1);     // = (2)
     * List.of(1, 2).subSequence(2);     // = ()
     * List.of(1, 2).subSequence(10);    // throws IndexOutOfBoundsException
     * List.of(1, 2).subSequence(-10);   // throws IndexOutOfBoundsException
     * </code>
     * </pre>
     *
     * See also {@link #drop(int)} which is similar but does not throw.
     *
     * @param beginIndex the beginning index, inclusive
     * @return the specified subsequence
     * @throws IndexOutOfBoundsException if {@code beginIndex} is negative or larger than the length of this
     *                                   {@code String} object.
     */
    Seq<T> subSequence(int beginIndex);

    /**
     * Returns a Seq that is a subsequence of this. The subsequence begins with the element at the specified
     * {@code beginIndex} and extends to the element at index {@code endIndex - 1}.
     * <p>
     * Examples:
     *
     * <pre>
     * <code>
     * List.of(1, 2, 3, 4).subSequence(1, 3); // = (2, 3)
     * List.of(1, 2, 3, 4).subSequence(0, 4); // = (1, 2, 3, 4)
     * List.of(1, 2, 3, 4).subSequence(2, 2); // = ()
     * List.of(1, 2).subSequence(1, 0);       // throws IndexOutOfBoundsException
     * List.of(1, 2).subSequence(-10, 1);     // throws IndexOutOfBoundsException
     * List.of(1, 2).subSequence(0, 10);      // throws IndexOutOfBoundsException
     * </code>
     * </pre>
     *
     * See also {@link #slice(int, int)} which returns an empty sequence instead of throwing.
     *
     * @param beginIndex the beginning index, inclusive
     * @param endIndex   the end index, exclusive
     * @return the specified subsequence
     * @throws IndexOutOfBoundsException if {@code beginIndex} or {@code endIndex} is negative or
     *                                   if {@code endIndex} is greater than {@code length()}
     * @throws IllegalArgumentException  if {@code beginIndex} is greater than {@code endIndex}
     */
    Seq<T> subSequence(int beginIndex, int endIndex);

    /**
     * Updates the given element at the specified index.
     *
     * @param index   an index
     * @param element an element
     * @return a new Seq consisting of all previous elements, with a single one (at the given index), changed to the new value.
     * @throws IndexOutOfBoundsException if this is empty, index &lt; 0 or index &gt;= length()
     */
    Seq<T> update(int index, T element);

    /**
     * Updates the given element at the specified index using the specified function.
     *
     * @param index   an index
     * @param updater a function transforming the previous value
     * @return a new Seq consisting of all previous elements, with a single one (at the given index), changed to the new value.
     * @throws IndexOutOfBoundsException if this is empty, index &lt; 0 or index &gt;= length()
     */
    Seq<T> update(int index, Function<? super T, ? extends T> updater);

    /**
     * Searches this sequence for a specific element. The sequence must already be sorted into ascending natural
     * order. If it is not sorted, the results are undefined.
     * <p>
     * If this sequence is an `IndexedSeq`, a binary search is used. Otherwise, a linear search is used.
     *
     * @param element the element to find
     * @return the index of the search element, if it is contained in the sequence;
     * otherwise, <code>(-(<i>insertion point</i>) - 1)</code>. The
     * <i>insertion point</i> is defined as the point at which the
     * element would be inserted into the sequence. Note that this guarantees that
     * the return value will be &gt;= 0 if and only if the element is found.
     * @throws ClassCastException if T cannot be cast to {@code Comparable<? super T>}
     */
    int search(T element);

    /**
     * Searches this sequence for a specific element. The sequence must already be sorted into ascending order
     * according to the specified comparator. If it is not sorted, the results are undefined.
     * <p>
     * If this sequence is an `IndexedSeq`, a binary search is used. Otherwise, a linear search is used.
     *
     * @param element    the element to find
     * @param comparator the comparator by which this sequence is ordered
     * @return the index of the search element, if it is contained in the sequence;
     * otherwise, <code>(-(<i>insertion point</i>) - 1)</code>. The
     * <i>insertion point</i> is defined as the point at which the
     * element would be inserted into the sequence. Note that this guarantees that
     * the return value will be &gt;= 0 if and only if the element is found.
     */
    int search(T element, Comparator<? super T> comparator);

    // -- Adjusted return types of Traversable methods

    @Override
    Seq<T> distinct();

    @Override
    Seq<T> distinctBy(Comparator<? super T> comparator);

    @Override
    <U> Seq<T> distinctBy(Function<? super T, ? extends U> keyExtractor);

    @Override
    Seq<T> drop(int n);

    @Override
    Seq<T> dropUntil(Predicate<? super T> predicate);

    @Override
    Seq<T> dropWhile(Predicate<? super T> predicate);

    @Override
    Seq<T> dropRight(int n);

    /**
     * Drops elements until the predicate holds for the current element, starting from the end.
     *
     * @param predicate A condition tested subsequently for this elements, starting from the end.
     * @return a new instance consisting of all elements until and including the last one which does satisfy the given
     * predicate.
     * @throws NullPointerException if {@code predicate} is null
     */
    Seq<T> dropRightUntil(Predicate<? super T> predicate);

    /**
     * Drops elements while the predicate holds for the current element, starting from the end.
     * <p>
     * Note: This is essentially the same as {@code dropRightUntil(predicate.negate())}.
     * It is intended to be used with method references, which cannot be negated directly.
     *
     * @param predicate A condition tested subsequently for this elements, starting from the end.
     * @return a new instance consisting of all elements until and including the last one which does not satisfy the
     * given predicate.
     * @throws NullPointerException if {@code predicate} is null
     */
    Seq<T> dropRightWhile(Predicate<? super T> predicate);

    @Override
    Seq<T> filter(Predicate<? super T> predicate);

    @Override
    Seq<T> filterNot(Predicate<? super T> predicate);

    @Override
    <U> Seq<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper);

    @Override
    default <U> U foldRight(U zero, BiFunction<? super T, ? super U, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return reverse().foldLeft(zero, (xs, x) -> f.apply(x, xs));
    }

    @Override
    <C> Map<C, ? extends Seq<T>> groupBy(Function<? super T, ? extends C> classifier);

    @Override
    Iterator<? extends Seq<T>> grouped(int size);

    @Override
    Seq<T> init();

    @Override
    Option<? extends Seq<T>> initOption();

    @Override
    <U> Seq<U> map(Function<? super T, ? extends U> mapper);

    @Override
    Seq<T> orElse(Iterable<? extends T> other);

    @Override
    Seq<T> orElse(Supplier<? extends Iterable<? extends T>> supplier);

    @Override
    Tuple2<? extends Seq<T>, ? extends Seq<T>> partition(Predicate<? super T> predicate);

    @Override
    Seq<T> peek(Consumer<? super T> action);

    @Override
    Seq<T> tapEach(Consumer<? super T> action);
    
    @Override
    Seq<T> replace(T currentElement, T newElement);

    @Override
    Seq<T> replaceAll(T currentElement, T newElement);

    @Override
    Seq<T> retainAll(Iterable<? extends T> elements);

    @Override
    Seq<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation);

    @Override
    <U> Seq<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation);

    @Override
    <U> Seq<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation);

    @Override
    Iterator<? extends Seq<T>> slideBy(Function<? super T, ?> classifier);

    @Override
    Iterator<? extends Seq<T>> sliding(int size);

    @Override
    Iterator<? extends Seq<T>> sliding(int size, int step);

    @Override
    Tuple2<? extends Seq<T>, ? extends Seq<T>> span(Predicate<? super T> predicate);

    @Override
    Seq<T> tail();

    @Override
    Option<? extends Seq<T>> tailOption();

    @Override
    Seq<T> take(int n);

    @Override
    Seq<T> takeUntil(Predicate<? super T> predicate);

    @Override
    Seq<T> takeWhile(Predicate<? super T> predicate);

    @Override
    Seq<T> takeRight(int n);

    /**
     * Takes elements until the predicate holds for the current element, starting from the end.
     *
     * @param predicate A condition tested subsequently for this elements, starting from the end.
     * @return a new instance consisting of all elements after the last one which does satisfy the given predicate.
     * @throws NullPointerException if {@code predicate} is null
     */
    Seq<T> takeRightUntil(Predicate<? super T> predicate);

    /**
     * Takes elements while the predicate holds for the current element, starting from the end.
     * <p>
     * Note: This is essentially the same as {@code takeRightUntil(predicate.negate())}.
     * It is intended to be used with method references, which cannot be negated directly.
     *
     * @param predicate A condition tested subsequently for this elements, starting from the end.
     * @return a new instance consisting of all elements after the last one which does not satisfy the given predicate.
     * @throws NullPointerException if {@code predicate} is null
     */
    Seq<T> takeRightWhile(Predicate<? super T> predicate);

    @Override
    <U> Seq<Tuple2<T, U>> zip(Iterable<? extends U> that);

    @Override
    <U, R> Seq<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper);

    @Override
    <U> Seq<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem);

    @Override
    Seq<Tuple2<T, Integer>> zipWithIndex();

    @Override
    <U> Seq<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper);

    @Override
    default boolean isSequential() {
        return true;
    }
}
