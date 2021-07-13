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
import io.vavr.collection.ArrayModule.Combinations;
import io.vavr.collection.JavaConverters.ListView;
import io.vavr.control.Option;

import java.io.Serializable;
import java.util.*;
import java.util.function.*;
import java.util.stream.Collector;

import static io.vavr.collection.JavaConverters.ChangePolicy.IMMUTABLE;
import static io.vavr.collection.JavaConverters.ChangePolicy.MUTABLE;
import static java.util.Arrays.copyOf;
import static java.util.Arrays.sort;

/**
 * Array is a Traversable wrapper for {@code Object[]} containing elements of type {@code T}.
 *
 * @param <T> Component type
 */
public final class Array<T> implements IndexedSeq<T>, Serializable {
    private static final long serialVersionUID = 1L;

    private static final Array<?> EMPTY = new Array<>(new Object[0]);

    private final Object[] delegate;

    private Array(Object[] delegate) {
        this.delegate = delegate;
    }

    static <T> Array<T> wrap(Object[] array) {
        return (array.length == 0) ? empty() : new Array<>(array);
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link Array}.
     *
     * @param <T> Component type of the Array.
     * @return a {@code Collector} which collects all the input elements into a
     * {@link io.vavr.collection.Array}, in encounter order
     */
    public static <T> Collector<T, ArrayList<T>, Array<T>> collector() {
        return Collections.toListAndThen(Array::ofAll);
    }

    @SuppressWarnings("unchecked")
    public static <T> Array<T> empty() {
        return (Array<T>) EMPTY;
    }

    /**
     * Narrows a widened {@code Array<? extends T>} to {@code Array<T>}
     * by performing a type-safe cast. This is eligible because immutable/read-only
     * collections are covariant.
     *
     * <pre>{@code
     * Array<Double> doubles = Array.of(1.0d, 2.0d);
     * Array<Number> numbers = Array.narrow(doubles); // = Array(1.0d, 2.0d)
     * }</pre>
     *
     * @param array An {@code Array}.
     * @param <T>   Component type of the {@code Array}.
     * @return the given {@code array} instance as narrowed type {@code Array<T>}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Array<T> narrow(Array<? extends T> array) {
        return (Array<T>) array;
    }

    /**
     * Returns a singleton {@code Array}, i.e. a {@code Array} of one element.
     *
     * <pre>{@code
     * Array<Double> doubles = Array.of(1.0d); // = Array(1.0d)
     * Array<Integer> ints = Array.of(10);     // = Array(10)
     * }</pre>
     *
     * @param element An element.
     * @param <T>     The component type
     * @return A new Array instance containing the given element
     */
    public static <T> Array<T> of(T element) {
        return wrap(new Object[] { element });
    }

    /**
     * Creates an Array of the given elements.
     *
     * <pre>{@code
     * Array<Integer> ints = Array.of(1, 2, 3); // = Array(1, 2, 3)
     * Array<Double> doubles = Array.of(1.0d, 2.0d, 3.0d); // = Array(1.0, 2.0, 3.0)
     * }</pre>
     *
     * @param <T>      Component type of the Array.
     * @param elements Zero or more elements.
     * @return An Array containing the given elements in the same order.
     * @throws NullPointerException if {@code elements} is null
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    public static <T> Array<T> of(T... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return wrap(copyOf(elements, elements.length));
    }

    /**
     * Creates an Array of the given elements.
     * <p>
     * The resulting Array has the same iteration order as the given iterable of elements
     * if the iteration order of the elements is stable.
     *
     * <pre>{@code
     * List<Integer> integers = List.of(1, 2, 3);
     * Array<Integer> ints = Array.ofAll(integers); // = Array(1, 2, 3)
     * }</pre>
     *
     * @param <T>      Component type of the Array.
     * @param elements An Iterable of elements.
     * @return An Array containing the given elements in the same order.
     * @throws NullPointerException if {@code elements} is null
     */
    @SuppressWarnings("unchecked")
    public static <T> Array<T> ofAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (elements instanceof Array) {
            return (Array<T>) elements;
        }
        if (elements instanceof ListView
                && ((ListView<T, ?>) elements).getDelegate() instanceof Array) {
            return (Array<T>) ((ListView<T, ?>) elements).getDelegate();
        }
        return wrap(toArray(elements));
    }

    /**
     * Creates an Array that contains the elements of the given {@link java.util.stream.Stream}.
     *
     * <pre>{@code
     * List<Integer> integers = List.of(1, 2, 3);
     * Array<Integer> ints = Array.ofAll(integers.toJavaStream()); // = Array(1, 2, 3)
     * }</pre>
     *
     * @param javaStream A {@link java.util.stream.Stream}
     * @param <T>        Component type of the Stream.
     * @return An Array containing the given elements in the same order.
     */
    public static <T> Array<T> ofAll(java.util.stream.Stream<? extends T> javaStream) {
        Objects.requireNonNull(javaStream, "javaStream is null");
        return wrap(javaStream.toArray());
    }

    /**
     * Creates an Array from boolean values.
     *
     * <pre>{@code
     * Array<Boolean> booleans = Array.ofAll(true, false); // = Array(true, false)
     * }</pre>
     *
     * @param elements boolean values
     * @return A new Array of Boolean values
     * @throws NullPointerException if elements is null
     */
    public static Array<Boolean> ofAll(boolean... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates an Array from byte values.
     *
     * <pre>{@code
     * Array<Byte> bytes = Array.ofAll((byte)1, (byte)2); // = Array(1, 2)
     * }</pre>
     *
     * @param elements byte values
     * @return A new Array of Byte values
     * @throws NullPointerException if elements is null
     */
    public static Array<Byte> ofAll(byte... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates an Array from char values.
     *
     * <pre>{@code
     * Array<Character> characters = Array.ofAll('a', 'e', 'i', 'o', 'u'); // = Array(a, e, i, o, u)
     * }</pre>
     *
     * @param elements char values
     * @return A new Array of Character values
     * @throws NullPointerException if elements is null
     */
    public static Array<Character> ofAll(char... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates an Array from double values.
     *
     * <pre>{@code
     * Array<Double> doubles = Array.ofAll(1d, 2d); // = Array(1.0, 2.0)
     * }</pre>
     * @param elements double values
     * @return A new Array of Double values
     * @throws NullPointerException if elements is null
     */
    public static Array<Double> ofAll(double... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates an Array from float values.
     *
     * <pre>{@code
     * Array<Float> floats = Array.ofAll(1.1f, 2.2f); // = Array(1.1, 2.2)
     * }</pre>
     *
     * @param elements float values
     * @return A new Array of Float values
     * @throws NullPointerException if elements is null
     */
    public static Array<Float> ofAll(float... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates an Array from int values.
     *
     * <pre>{@code
     * Array<Integer> ints = Array.ofAll(1, 2); // = Array(1, 2)
     * }</pre>
     *
     * @param elements int values
     * @return A new Array of Integer values
     * @throws NullPointerException if elements is null
     */
    public static Array<Integer> ofAll(int... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates an Array from long values.
     *
     * <pre>{@code
     * Array<Long> longValues = Array.ofAll(1L, 2L); // = Array(1, 2)
     * }</pre>
     *
     * @param elements long values
     * @return A new Array of Long values
     * @throws NullPointerException if elements is null
     */
    public static Array<Long> ofAll(long... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates an Array from short values.
     *
     * <pre>{@code
     * Array<Short> shortValues = Array.ofAll((short)1, (short)2); // = Array(1, 2)
     * }</pre>
     *
     * @param elements short values
     * @return A new Array of Short values
     * @throws NullPointerException if elements is null
     */
    public static Array<Short> ofAll(short... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return ofAll(Iterator.ofAll(elements));
    }

    /**
     * Returns an Array containing {@code n} values of a given Function {@code f}
     * over a range of integer values from 0 to {@code n - 1}.
     *
     * <pre>{@code
     * Array<Integer> values = Array.tabulate(5, i -> 2 * i); // = Array(0, 2, 4, 6, 8)
     * }</pre>
     *
     * @param <T> Component type of the Array
     * @param n   The number of elements in the Array
     * @param f   The Function computing element values
     * @return An Array consisting of elements {@code f(0),f(1), ..., f(n - 1)}
     * @throws NullPointerException if {@code f} is null
     */
    public static <T> Array<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return io.vavr.collection.Collections.tabulate(n, f, empty(), Array::of);
    }

    /**
     * Returns an Array containing {@code n} values supplied by a given Supplier {@code s}.
     *
     * @param <T> Component type of the Array
     * @param n   The number of elements in the Array
     * @param s   The Supplier computing element values
     * @return An Array of size {@code n}, where each element contains the result supplied by {@code s}.
     * @throws NullPointerException if {@code s} is null
     */
    public static <T> Array<T> fill(int n, Supplier<? extends T> s) {
        Objects.requireNonNull(s, "s is null");
        return io.vavr.collection.Collections.fill(n, s, empty(), Array::of);
    }

    /**
     * Returns an Array containing {@code n} times the given {@code element}
     *
     * <pre>{@code
     * Array<Integer> values = Array.fill(5, 20); // = Array(20, 20, 20, 20, 20)
     * }</pre>
     *
     * @param <T>     Component type of the Array
     * @param n       The number of elements in the Array
     * @param element The element
     * @return An Array of size {@code n}, where each element is the given {@code element}.
     */
    public static <T> Array<T> fill(int n, T element) {
        return io.vavr.collection.Collections.fillObject(n, element, empty(), Array::of);
    }

    public static Array<Character> range(char from, char toExclusive) {
        return ofAll(Iterator.range(from, toExclusive));
    }

    public static Array<Character> rangeBy(char from, char toExclusive, int step) {
        return ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static Array<Double> rangeBy(double from, double toExclusive, double step) {
        return ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    /**
     * Creates an Array of int numbers starting from {@code from}, extending to {@code toExclusive - 1}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Array.range(0, 0)  // = Array()
     * Array.range(2, 0)  // = Array()
     * Array.range(-2, 2) // = Array(-2, -1, 0, 1)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1
     * @return a range of int values as specified or the empty range if {@code from >= toExclusive}
     */
    public static Array<Integer> range(int from, int toExclusive) {
        return ofAll(Iterator.range(from, toExclusive));
    }

    /**
     * Creates an Array of int numbers starting from {@code from}, extending to {@code toExclusive - 1},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Array.rangeBy(1, 3, 1)  // = Array(1, 2)
     * Array.rangeBy(1, 4, 2)  // = Array(1, 3)
     * Array.rangeBy(4, 1, -2) // = Array(4, 2)
     * Array.rangeBy(4, 1, 2)  // = Array()
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
    public static Array<Integer> rangeBy(int from, int toExclusive, int step) {
        return ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    /**
     * Creates an Array of long numbers starting from {@code from}, extending to {@code toExclusive - 1}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Array.range(0L, 0L)  // = Array()
     * Array.range(2L, 0L)  // = Array()
     * Array.range(-2L, 2L) // = Array(-2L, -1L, 0L, 1L)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1
     * @return a range of long values as specified or the empty range if {@code from >= toExclusive}
     */
    public static Array<Long> range(long from, long toExclusive) {
        return ofAll(Iterator.range(from, toExclusive));
    }

    /**
     * Creates an Array of long numbers starting from {@code from}, extending to {@code toExclusive - 1},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Array.rangeBy(1L, 3L, 1L)  // = Array(1L, 2L)
     * Array.rangeBy(1L, 4L, 2L)  // = Array(1L, 3L)
     * Array.rangeBy(4L, 1L, -2L) // = Array(4L, 2L)
     * Array.rangeBy(4L, 1L, 2L)  // = Array()
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
    public static Array<Long> rangeBy(long from, long toExclusive, long step) {
        return ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static Array<Character> rangeClosed(char from, char toInclusive) {
        return ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static Array<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static Array<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    /**
     * Creates an Array of int numbers starting from {@code from}, extending to {@code toInclusive}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Array.rangeClosed(0, 0)  // = Array(0)
     * Array.rangeClosed(2, 0)  // = Array()
     * Array.rangeClosed(-2, 2) // = Array(-2, -1, 0, 1, 2)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toInclusive the last number
     * @return a range of int values as specified or the empty range if {@code from > toInclusive}
     */
    public static Array<Integer> rangeClosed(int from, int toInclusive) {
        return ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    /**
     * Creates an Array of int numbers starting from {@code from}, extending to {@code toInclusive},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Array.rangeClosedBy(1, 3, 1)  // = Array(1, 2, 3)
     * Array.rangeClosedBy(1, 4, 2)  // = Array(1, 3)
     * Array.rangeClosedBy(4, 1, -2) // = Array(4, 2)
     * Array.rangeClosedBy(4, 1, 2)  // = Array()
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
    public static Array<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    /**
     * Creates an Array of long numbers starting from {@code from}, extending to {@code toInclusive}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Array.rangeClosed(0L, 0L)  // = Array(0L)
     * Array.rangeClosed(2L, 0L)  // = Array()
     * Array.rangeClosed(-2L, 2L) // = Array(-2L, -1L, 0L, 1L, 2L)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toInclusive the last number
     * @return a range of long values as specified or the empty range if {@code from > toInclusive}
     */
    public static Array<Long> rangeClosed(long from, long toInclusive) {
        return ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    /**
     * Creates an Array of long numbers starting from {@code from}, extending to {@code toInclusive},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Array.rangeClosedBy(1L, 3L, 1L)  // = Array(1L, 2L, 3L)
     * Array.rangeClosedBy(1L, 4L, 2L)  // = Array(1L, 3L)
     * Array.rangeClosedBy(4L, 1L, -2L) // = Array(4L, 2L)
     * Array.rangeClosedBy(4L, 1L, 2L)  // = Array()
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
    public static Array<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    /**
     * Transposes the rows and columns of an {@link Array} matrix.
     *
     * @param matrix to be transposed.
     * @return a transposed {@link Array} matrix.
     * @throws IllegalArgumentException if the row lengths of {@code matrix} differ.
     * <p>
     * ex: {@code
     * Array.transpose(Array(Array(1,2,3), Array(4,5,6))) → Array(Array(1,4), Array(2,5), Array(3,6))
     * }
     */
    static <T> Array<Array<T>> transpose(Array<Array<T>> matrix) {
        return io.vavr.collection.Collections.transpose(matrix, Array::ofAll, Array::of);
    }

    /**
     * Creates an Array from a seed value and a function.
     * The function takes the seed at first.
     * The function should return {@code None} when it's
     * done generating the Array, otherwise {@code Some} {@code Tuple}
     * of the element for the next call and the value to add to the
     * resulting Array.
     * <p>
     * Example:
     * <pre>
     * <code>
     * Array.unfoldRight(10, x -&gt; x == 0
     *             ? Option.none()
     *             : Option.of(new Tuple2&lt;gt;(x, x-1)));
     * // Array(10, 9, 8, 7, 6, 5, 4, 3, 2, 1))
     * </code>
     * </pre>
     *
     * @param <T>  type of seeds
     * @param <U>  type of unfolded values
     * @param seed the start value for the iteration
     * @param f    the function to get the next step of the iteration
     * @return an Array with the values built up by the iteration
     * @throws NullPointerException if {@code f} is null
     */
    public static <T, U> Array<U> unfoldRight(T seed, Function<? super T, Option<Tuple2<? extends U, ? extends T>>> f) {
        return Iterator.unfoldRight(seed, f).toArray();
    }

    /**
     * Creates an Array from a seed value and a function.
     * The function takes the seed at first.
     * The function should return {@code None} when it's
     * done generating the list, otherwise {@code Some} {@code Tuple}
     * of the value to add to the resulting list and
     * the element for the next call.
     * <p>
     * Example:
     * <pre>
     * <code>
     * Array.unfoldLeft(10, x -&gt; x == 0
     *             ? Option.none()
     *             : Option.of(new Tuple2&lt;gt;(x-1, x)));
     * // Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
     * </code>
     * </pre>
     *
     * @param <T>  type of seeds
     * @param <U>  type of unfolded values
     * @param seed the start value for the iteration
     * @param f    the function to get the next step of the iteration
     * @return an Array with the values built up by the iteration
     * @throws NullPointerException if {@code f} is null
     */
    public static <T, U> Array<U> unfoldLeft(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends U>>> f) {
        return Iterator.unfoldLeft(seed, f).toArray();
    }

    /**
     * Creates an Array from a seed value and a function.
     * The function takes the seed at first.
     * The function should return {@code None} when it's
     * done generating the list, otherwise {@code Some} {@code Tuple}
     * of the value to add to the resulting list and
     * the element for the next call.
     * <p>
     * Example:
     * <pre>
     * <code>
     * Array.unfold(10, x -&gt; x == 0
     *             ? Option.none()
     *             : Option.of(new Tuple2&lt;gt;(x-1, x)));
     * // Array(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
     * </code>
     * </pre>
     *
     * @param <T>  type of seeds and unfolded values
     * @param seed the start value for the iteration
     * @param f    the function to get the next step of the iteration
     * @return an Array with the values built up by the iteration
     * @throws NullPointerException if {@code f} is null
     */
    public static <T> Array<T> unfold(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends T>>> f) {
        return Iterator.unfold(seed, f).toArray();
    }

    @Override
    public Array<T> append(T element) {
        final Object[] copy = copyOf(delegate, delegate.length + 1);
        copy[delegate.length] = element;
        return wrap(copy);
    }

    @Override
    public Array<T> appendAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (isEmpty() && elements instanceof Array) {
            @SuppressWarnings("unchecked")
            final Array<T> array = (Array<T>) elements;
            return array;
        }
        final Object[] source = toArray(elements);
        if (source.length == 0) {
            return this;
        } else {
            final Object[] arr = copyOf(delegate, delegate.length + source.length);
            System.arraycopy(source, 0, arr, delegate.length, source.length);
            return wrap(arr);
        }
    }

    @Override
    public java.util.List<T> asJava() {
        return JavaConverters.asJava(this, IMMUTABLE);
    }

    @Override
    public Array<T> asJava(Consumer<? super java.util.List<T>> action) {
        return Collections.asJava(this, action, IMMUTABLE);
    }

    @Override
    public java.util.List<T> asJavaMutable() {
        return JavaConverters.asJava(this, MUTABLE);
    }

    @Override
    public Array<T> asJavaMutable(Consumer<? super java.util.List<T>> action) {
        return Collections.asJava(this, action, MUTABLE);
    }

    @Override
    public <R> Array<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        return ofAll(iterator().<R> collect(partialFunction));
    }

    @Override
    public boolean hasDefiniteSize() {
        return true;
    }

    /**
     * An {@code Array} is computed eagerly.
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

    @SuppressWarnings("unchecked")
    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < delegate.length;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return (T) delegate[index++];
            }
        };
    }

    @Override
    public Array<Array<T>> combinations() {
        return rangeClosed(0, length()).map(this::combinations).flatMap(Function.identity());
    }

    @Override
    public Array<Array<T>> combinations(int k) {
        return Combinations.apply(this, Math.max(k, 0));
    }

    @Override
    public Iterator<Array<T>> crossProduct(int power) {
        return io.vavr.collection.Collections.crossProduct(empty(), this, power);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T apply(Integer index) {
        return (T) delegate[index];
    }

    @Override
    public Array<T> distinct() {
        return distinctBy(Function.identity());
    }

    @Override
    public Array<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        final java.util.Set<T> seen = new java.util.TreeSet<>(comparator);
        return filter(seen::add);
    }

    @Override
    public <U> Array<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        final java.util.Set<U> seen = new java.util.HashSet<>();
        return filter(t -> seen.add(keyExtractor.apply(t)));
    }

    @Override
    public Array<T> drop(int n) {
        if (n <= 0) {
            return this;
        } else if (n >= length()) {
            return empty();
        } else {
            final Object[] arr = new Object[delegate.length - n];
            System.arraycopy(delegate, n, arr, 0, arr.length);
            return wrap(arr);
        }
    }

    @Override
    public Array<T> dropUntil(Predicate<? super T> predicate) {
        return io.vavr.collection.Collections.dropUntil(this, predicate);
    }

    @Override
    public Array<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return dropUntil(predicate.negate());
    }

    @Override
    public Array<T> dropRight(int n) {
        if (n <= 0) {
            return this;
        } else if (n >= length()) {
            return empty();
        } else {
            return wrap(copyOf(delegate, delegate.length - n));
        }
    }

    @Override
    public Array<T> dropRightUntil(Predicate<? super T> predicate) {
        return io.vavr.collection.Collections.dropRightUntil(this, predicate);
    }

    @Override
    public Array<T> dropRightWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return dropRightUntil(predicate.negate());
    }

    @Override
    public Array<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final java.util.List<T> list = new ArrayList<>();
        for (T t : this) {
            if (predicate.test(t)) {
                list.add(t);
            }
        }
        if (list.isEmpty()) {
            return empty();
        } else if (list.size() == size()) {
            return this;
        } else {
            return wrap(list.toArray());
        }
    }

    @Override
    public Array<T> filterNot(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Collections.filterNot(this, predicate);
    }

    @Override
    public <U> Array<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (isEmpty()) {
            return empty();
        } else {
            final java.util.List<U> list = new ArrayList<>();
            for (T t : this) {
                for (U u : mapper.apply(t)) {
                    list.add(u);
                }
            }
            return wrap(list.toArray());
        }
    }

    @Override
    public <C> Map<C, Array<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return io.vavr.collection.Collections.groupBy(this, classifier, Array::ofAll);
    }

    @Override
    public Iterator<Array<T>> grouped(int size) {
        return sliding(size, size);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T head() {
        if (isEmpty()) {
            throw new NoSuchElementException("head on empty Array");
        } else {
            return (T) delegate[0];
        }
    }

    @Override
    public int indexOf(T element, int from) {
        for (int i = from; i < length(); i++) {
            if (Objects.equals(get(i), element)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public Array<T> init() {
        if (isEmpty()) {
            throw new UnsupportedOperationException("init of empty Array");
        }
        return dropRight(1);
    }

    @Override
    public Option<Array<T>> initOption() {
        return isEmpty() ? Option.none() : Option.some(init());
    }

    @Override
    public boolean isEmpty() {
        return delegate.length == 0;
    }

    private Object readResolve() {
        return isEmpty() ? EMPTY : this;
    }

    @Override
    public Array<T> insert(int index, T element) {
        if (index < 0 || index > length()) {
            throw new IndexOutOfBoundsException("insert(" + index + ", e) on Array of length " + length());
        }
        final Object[] arr = new Object[delegate.length + 1];
        System.arraycopy(delegate, 0, arr, 0, index);
        arr[index] = element;
        System.arraycopy(delegate, index, arr, index + 1, delegate.length - index);
        return wrap(arr);
    }

    @Override
    public Array<T> insertAll(int index, Iterable<? extends T> elements) {
        if (index < 0 || index > length()) {
            throw new IndexOutOfBoundsException("insert(" + index + ", e) on Array of length " + length());
        }
        if (isEmpty() && elements instanceof Array) {
            @SuppressWarnings("unchecked")
            final Array<T> array = (Array<T>) elements;
            return array;
        }
        final Object[] list = toArray(elements);
        if (list.length == 0) {
            return this;
        } else {
            final Object[] arr = new Object[delegate.length + list.length];
            System.arraycopy(delegate, 0, arr, 0, index);
            System.arraycopy(list, 0, arr, index, list.length);
            System.arraycopy(delegate, index, arr, index + list.length, delegate.length - index);
            return wrap(arr);
        }
    }

    @Override
    public Array<T> intersperse(T element) {
        if (delegate.length <= 1) {
            return this;
        } else {
            final Object[] arr = new Object[delegate.length * 2 - 1];
            for (int i = 0; i < delegate.length; i++) {
                arr[i * 2] = delegate[i];
                if (i > 0) {
                    arr[i * 2 - 1] = element;
                }
            }
            return wrap(arr);
        }
    }

    @Override
    public int lastIndexOf(T element, int end) {
        for (int i = Math.min(end, length() - 1); i >= 0; i--) {
            if (Objects.equals(get(i), element)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public int length() {
        return delegate.length;
    }

    @Override
    public <U> Array<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        final Object[] arr = new Object[length()];
        for (int i = 0; i < delegate.length; i++) {
            arr[i] = mapper.apply(get(i));
        }
        return wrap(arr);
    }

    @Override
    public Array<T> orElse(Iterable<? extends T> other) {
        return isEmpty() ? ofAll(other) : this;
    }

    @Override
    public Array<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return isEmpty() ? ofAll(supplier.get()) : this;
    }

    @Override
    public Array<T> padTo(int length, T element) {
        final int actualLength = length();
        if (length <= actualLength) {
            return this;
        } else {
            return appendAll(Iterator.continually(element).take(length - actualLength));
        }
    }

    @Override
    public Array<T> leftPadTo(int length, T element) {
        final int actualLength = length();
        if (length <= actualLength) {
            return this;
        } else {
            return prependAll(Iterator.continually(element).take(length - actualLength));
        }
    }

    @Override
    public Array<T> patch(int from, Iterable<? extends T> that, int replaced) {
        from = Math.max(from, 0);
        replaced = Math.max(replaced, 0);
        Array<T> result = take(from).appendAll(that);
        from += replaced;
        result = result.appendAll(drop(from));
        return result;
    }

    @Override
    public Tuple2<Array<T>, Array<T>> partition(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final java.util.List<T> left = new ArrayList<>(), right = new ArrayList<>();
        for (T t : this) {
            (predicate.test(t) ? left : right).add(t);
        }
        return Tuple.of(ofAll(left), ofAll(right));
    }

    @Override
    public Array<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (!isEmpty()) {
            action.accept(head());
        }
        return this;
    }

    @Override
    public Array<Array<T>> permutations() {
        if (isEmpty()) {
            return empty();
        } else if (delegate.length == 1) {
            return of(this);
        } else {
            final Array<Array<T>> zero = empty();
            return distinct().foldLeft(zero, (xs, x) -> {
                final Function<Array<T>, Array<T>> prepend = l -> l.prepend(x);
                return xs.appendAll(remove(x).permutations().map(prepend));
            });
        }
    }

    @Override
    public Array<T> prepend(T element) {
        return insert(0, element);
    }

    @Override
    public Array<T> prependAll(Iterable<? extends T> elements) {
        return insertAll(0, elements);
    }

    @Override
    public Array<T> remove(T element) {
        int index = -1;
        for (int i = 0; i < length(); i++) {
            final T value = get(i);
            if (Objects.equals(element, value)) {
                index = i;
                break;
            }
        }
        if (index < 0) {
            return this;
        } else {
            return removeAt(index);
        }
    }

    @Override
    public Array<T> removeFirst(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        int found = -1;
        for (int i = 0; i < length(); i++) {
            final T value = get(i);
            if (predicate.test(value)) {
                found = i;
                break;
            }
        }
        if (found < 0) {
            return this;
        } else {
            return removeAt(found);
        }
    }

    @Override
    public Array<T> removeLast(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        int found = -1;
        for (int i = length() - 1; i >= 0; i--) {
            final T value = get(i);
            if (predicate.test(value)) {
                found = i;
                break;
            }
        }
        if (found < 0) {
            return this;
        } else {
            return removeAt(found);
        }
    }

    @Override
    public Array<T> removeAt(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("removeAt(" + index + ")");
        } else if (index >= length()) {
            throw new IndexOutOfBoundsException("removeAt(" + index + ")");
        } else {
            final Object[] arr = new Object[length() - 1];
            System.arraycopy(delegate, 0, arr, 0, index);
            System.arraycopy(delegate, index + 1, arr, index, length() - index - 1);
            return wrap(arr);
        }
    }

    @Override
    public Array<T> removeAll(T element) {
        return io.vavr.collection.Collections.removeAll(this, element);
    }

    @Override
    public Array<T> removeAll(Iterable<? extends T> elements) {
        return io.vavr.collection.Collections.removeAll(this, elements);
    }

    @Override
    public Array<T> replace(T currentElement, T newElement) {
        final Object[] arr = new Object[length()];
        boolean found = false;
        for (int i = 0; i < length(); i++) {
            final T value = get(i);
            if (found) {
                arr[i] = delegate[i];
            } else {
                if (Objects.equals(currentElement, value)) {
                    arr[i] = newElement;
                    found = true;
                } else {
                    arr[i] = delegate[i];
                }
            }
        }
        return found ? wrap(arr) : this;
    }

    @Override
    public Array<T> replaceAll(T currentElement, T newElement) {
        final Object[] arr = new Object[length()];
        boolean changed = false;
        for (int i = 0; i < length(); i++) {
            final T value = get(i);
            if (Objects.equals(currentElement, value)) {
                arr[i] = newElement;
                changed = true;
            } else {
                arr[i] = delegate[i];
            }
        }
        return changed ? wrap(arr) : this;
    }

    @Override
    public Array<T> retainAll(Iterable<? extends T> elements) {
        return io.vavr.collection.Collections.retainAll(this, elements);
    }

    @Override
    public Array<T> reverse() {
        if (size() <= 1) {
            return this;
        } else {
            final int length = delegate.length;
            final Object[] arr = new Object[length];
            for (int i = 0, j = length - 1; i < length; i++, j--) {
                arr[j] = delegate[i];
            }
            return wrap(arr);
        }
    }

    @Override
    public Array<T> rotateLeft(int n) {
        return Collections.rotateLeft(this, n);
    }

    @Override
    public Array<T> rotateRight(int n) {
        return Collections.rotateRight(this, n);
    }

    @Override
    public Array<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return scanLeft(zero, operation);
    }

    @Override
    public <U> Array<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        return io.vavr.collection.Collections.scanLeft(this, zero, operation, Array::ofAll);
    }

    @Override
    public <U> Array<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        return io.vavr.collection.Collections.scanRight(this, zero, operation, Array::ofAll);
    }

    @Override
    public Array<T> shuffle() {
        return io.vavr.collection.Collections.shuffle(this, Array::ofAll);
    }

    @Override
    public Array<T> shuffle(Random random) {
        return io.vavr.collection.Collections.shuffle(this, random, Array::ofAll);
    }

    @Override
    public Array<T> slice(int beginIndex, int endIndex) {
        if (beginIndex >= endIndex || beginIndex >= length() || isEmpty()) {
            return empty();
        }
        if (beginIndex <= 0 && endIndex >= length()) {
            return this;
        }
        final int index = Math.max(beginIndex, 0);
        final int length = Math.min(endIndex, length()) - index;
        final Object[] arr = new Object[length];
        System.arraycopy(delegate, index, arr, 0, length);
        return wrap(arr);
    }

    @Override
    public Iterator<Array<T>> slideBy(Function<? super T, ?> classifier) {
        return iterator().slideBy(classifier).map(Array::ofAll);
    }

    @Override
    public Iterator<Array<T>> sliding(int size) {
        return sliding(size, 1);
    }

    @Override
    public Iterator<Array<T>> sliding(int size, int step) {
        return iterator().sliding(size, step).map(Array::ofAll);
    }

    @Override
    public Array<T> sorted() {
        final Object[] arr = copyOf(delegate, delegate.length);
        sort(arr);
        return wrap(arr);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Array<T> sorted(Comparator<? super T> comparator) {
        final Object[] arr = copyOf(delegate, delegate.length);
        sort(arr, (o1, o2) -> comparator.compare((T) o1, (T) o2));
        return wrap(arr);
    }

    @Override
    public <U extends Comparable<? super U>> Array<T> sortBy(Function<? super T, ? extends U> mapper) {
        return sortBy(U::compareTo, mapper);
    }

    @Override
    public <U> Array<T> sortBy(Comparator<? super U> comparator, Function<? super T, ? extends U> mapper) {
        return Collections.sortBy(this, comparator, mapper, collector());
    }

    @Override
    public Tuple2<Array<T>, Array<T>> splitAt(int n) {
        return Tuple.of(take(n), drop(n));
    }

    @Override
    public Tuple2<Array<T>, Array<T>> splitAt(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final Array<T> init = takeWhile(predicate.negate());
        return Tuple.of(init, drop(init.length()));
    }

    @Override
    public Tuple2<Array<T>, Array<T>> splitAtInclusive(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        for (int i = 0; i < delegate.length; i++) {
            final T value = get(i);
            if (predicate.test(value)) {
                if (i == delegate.length - 1) {
                    return Tuple.of(this, empty());
                } else {
                    return Tuple.of(take(i + 1), drop(i + 1));
                }
            }
        }
        return Tuple.of(this, empty());
    }

    @Override
    public Tuple2<Array<T>, Array<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Tuple.of(takeWhile(predicate), dropWhile(predicate));
    }

    @Override
    public Array<T> subSequence(int beginIndex) {
        if (beginIndex < 0 || beginIndex > length()) {
            throw new IndexOutOfBoundsException("subSequence(" + beginIndex + ")");
        } else {
            return drop(beginIndex);
        }
    }

    @Override
    public Array<T> subSequence(int beginIndex, int endIndex) {
        Collections.subSequenceRangeCheck(beginIndex, endIndex, length());
        if (beginIndex == endIndex) {
            return empty();
        } else if (beginIndex == 0 && endIndex == length()) {
            return this;
        } else {
            final Object[] arr = new Object[endIndex - beginIndex];
            System.arraycopy(delegate, beginIndex, arr, 0, arr.length);
            return wrap(arr);
        }
    }

    @Override
    public Array<T> tail() {
        if (isEmpty()) {
            throw new UnsupportedOperationException("tail() on empty Array");
        } else {
            final Object[] arr = new Object[delegate.length - 1];
            System.arraycopy(delegate, 1, arr, 0, arr.length);
            return wrap(arr);
        }
    }

    @Override
    public Option<Array<T>> tailOption() {
        return isEmpty() ? Option.none() : Option.some(tail());
    }

    @Override
    public Array<T> take(int n) {
        if (n >= length()) {
            return this;
        } else if (n <= 0) {
            return empty();
        } else {
            return wrap(copyOf(delegate, n));
        }
    }

    @Override
    public Array<T> takeUntil(Predicate<? super T> predicate) {
        return io.vavr.collection.Collections.takeUntil(this, predicate);
    }

    @Override
    public Array<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return takeUntil(predicate.negate());
    }

    @Override
    public Array<T> takeRight(int n) {
        if (n >= length()) {
            return this;
        } else if (n <= 0) {
            return empty();
        } else {
            final Object[] arr = new Object[n];
            System.arraycopy(delegate, delegate.length - n, arr, 0, n);
            return wrap(arr);
        }
    }

    @Override
    public Array<T> takeRightUntil(Predicate<? super T> predicate) {
        return io.vavr.collection.Collections.takeRightUntil(this, predicate);
    }

    @Override
    public Array<T> takeRightWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return takeRightUntil(predicate.negate());
    }

    /**
     * Transforms this {@code Array}.
     *
     * @param f   A transformation
     * @param <U> Type of transformation result
     * @return An instance of type {@code U}
     * @throws NullPointerException if {@code f} is null
     */
    public <U> U transform(Function<? super Array<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    public Array<T> update(int index, T element) {
        if ((index < 0) || (index >= length())) {
            throw new IndexOutOfBoundsException("update(" + index + ")");
        } else {
            final Object[] arr = copyOf(delegate, delegate.length);
            arr[index] = element;
            return wrap(arr);
        }
    }

    @Override
    public Array<T> update(int index, Function<? super T, ? extends T> updater) {
        Objects.requireNonNull(updater, "updater is null");
        return update(index, updater.apply(get(index)));
    }

    @Override
    public <U> Array<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        return zipWith(that, Tuple::of);
    }

    @Override
    public <U, R> Array<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return ofAll(iterator().zipWith(that, mapper));
    }

    @Override
    public <U> Array<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        return ofAll(iterator().zipAll(that, thisElem, thatElem));
    }

    @Override
    public Array<Tuple2<T, Integer>> zipWithIndex() {
        return ofAll(iterator().zipWithIndex());
    }

    @Override
    public <U> Array<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return ofAll(iterator().zipWithIndex(mapper));
    }

    @Override
    public boolean equals(Object o) {
        return io.vavr.collection.Collections.equals(this, o);
    }

    @Override
    public int hashCode() {
        return io.vavr.collection.Collections.hashOrdered(this);
    }

    @Override
    public String stringPrefix() {
        return "Array";
    }

    @Override
    public String toString() {
        return mkString(stringPrefix() + "(", ", ", ")");
    }

    private static <T> Object[] toArray(Iterable<T> elements) {
        if (elements instanceof Array) {
            final Array<T> array = (Array<T>) elements;
            return array.delegate;
        } else {
            return io.vavr.collection.Collections.withSize(elements).toArray();
        }
    }
}

interface ArrayModule {

    final class Combinations {

        static <T> Array<Array<T>> apply(Array<T> elements, int k) {
            if (k == 0) {
                return Array.of(Array.empty());
            } else {
                return elements.zipWithIndex().flatMap(
                        t -> apply(elements.drop(t._2 + 1), (k - 1)).map(c -> c.prepend(t._1))
                );
            }
        }
    }
}
