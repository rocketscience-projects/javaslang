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

import static io.vavr.collection.JavaConverters.ChangePolicy.IMMUTABLE;
import static io.vavr.collection.JavaConverters.ChangePolicy.MUTABLE;
import static io.vavr.collection.JavaConverters.ListView;

/**
 * An immutable {@code Stream} is lazy sequence of elements which may be infinitely long.
 * Its immutability makes it suitable for concurrent programming.
 * <p>
 * A {@code Stream} is composed of a {@code head} element and a lazy evaluated {@code tail} {@code Stream}.
 * <p>
 * There are two implementations of the {@code Stream} interface:
 *
 * <ul>
 * <li>{@link Empty}, which represents the empty {@code Stream}.</li>
 * <li>{@link Cons}, which represents a {@code Stream} containing one or more elements.</li>
 * </ul>
 *
 * Methods to obtain a {@code Stream}:
 *
 * <pre>
 * <code>
 * // factory methods
 * Stream.empty()                  // = Stream.of() = Nil.instance()
 * Stream.of(x)                    // = new Cons&lt;&gt;(x, Nil.instance())
 * Stream.of(Object...)            // e.g. Stream.of(1, 2, 3)
 * Stream.ofAll(Iterable)          // e.g. Stream.ofAll(List.of(1, 2, 3)) = 1, 2, 3
 * Stream.ofAll(&lt;primitive array&gt;) // e.g. List.ofAll(1, 2, 3) = 1, 2, 3
 *
 * // int sequences
 * Stream.from(0)                  // = 0, 1, 2, 3, ...
 * Stream.range(0, 3)              // = 0, 1, 2
 * Stream.rangeClosed(0, 3)        // = 0, 1, 2, 3
 *
 * // generators
 * Stream.cons(Object, Supplier)   // e.g. Stream.cons(current, () -&gt; next(current));
 * Stream.continually(Supplier)    // e.g. Stream.continually(Math::random);
 * Stream.iterate(Object, Function)// e.g. Stream.iterate(1, i -&gt; i * 2);
 * </code>
 * </pre>
 *
 * Factory method applications:
 *
 * <pre>
 * <code>
 * Stream&lt;Integer&gt;       s1 = Stream.of(1);
 * Stream&lt;Integer&gt;       s2 = Stream.of(1, 2, 3);
 *                       // = Stream.of(new Integer[] {1, 2, 3});
 *
 * Stream&lt;int[]&gt;         s3 = Stream.ofAll(1, 2, 3);
 * Stream&lt;List&lt;Integer&gt;&gt; s4 = Stream.ofAll(List.of(1, 2, 3));
 *
 * Stream&lt;Integer&gt;       s5 = Stream.ofAll(1, 2, 3);
 * Stream&lt;Integer&gt;       s6 = Stream.ofAll(List.of(1, 2, 3));
 *
 * // cuckoo's egg
 * Stream&lt;Integer[]&gt;     s7 = Stream.&lt;Integer[]&gt; of(new Integer[] {1, 2, 3});
 * </code>
 * </pre>
 *
 * Example: Generating prime numbers
 *
 * <pre>
 * <code>
 * // = Stream(2L, 3L, 5L, 7L, ...)
 * Stream.iterate(2L, PrimeNumbers::nextPrimeFrom)
 *
 * // helpers
 *
 * static long nextPrimeFrom(long num) {
 *     return Stream.from(num + 1).find(PrimeNumbers::isPrime).get();
 * }
 *
 * static boolean isPrime(long num) {
 *     return !Stream.rangeClosed(2L, (long) Math.sqrt(num)).exists(d -&gt; num % d == 0);
 * }
 * </code>
 * </pre>
 *
 * See Okasaki, Chris: <em>Purely Functional Data Structures</em> (p. 34 ff.). Cambridge, 2003.
 *
 * @param <T> component type of this Stream
 */
public abstract class Stream<T> implements LinearSeq<T> {

    private static final long serialVersionUID = 1L;

    // sealed
    private Stream() {
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link Stream}.
     *
     * @param <T> Component type of the Stream.
     * @return a {@code Collector} which collects all the input elements into a
     * {@link io.vavr.collection.Stream}, in encounter order
     */
    public static <T> Collector<T, ArrayList<T>, Stream<T>> collector() {
        return Collections.toListAndThen(Stream::ofAll);
    }

    /**
     * Lazily creates a Stream in O(1) which traverses along the concatenation of the given iterables.
     *
     * @param iterables The iterables
     * @param <T>       Component type.
     * @return A new {@code Stream}
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    public static <T> Stream<T> concat(Iterable<? extends T>... iterables) {
        return Iterator.concat(iterables).toStream();
    }

    /**
     * Lazily creates a Stream in O(1) which traverses along the concatenation of the given iterables.
     *
     * @param iterables The iterable of iterables
     * @param <T>       Component type.
     * @return A new {@code Stream}
     */
    public static <T> Stream<T> concat(Iterable<? extends Iterable<? extends T>> iterables) {
        return Iterator.<T> concat(iterables).toStream();
    }

    /**
     * Returns an infinitely long Stream of {@code int} values starting from {@code from}.
     * <p>
     * The {@code Stream} extends to {@code Integer.MIN_VALUE} when passing {@code Integer.MAX_VALUE}.
     *
     * @param value a start int value
     * @return a new Stream of int values starting from {@code from}
     */
    public static Stream<Integer> from(int value) {
        return Stream.ofAll(Iterator.from(value));
    }

    /**
     * Returns an infinite long Stream of {@code int} values starting from {@code value} and spaced by {@code step}.
     * <p>
     * The {@code Stream} extends to {@code Integer.MIN_VALUE} when passing {@code Integer.MAX_VALUE}.
     *
     * @param value a start int value
     * @param step  the step by which to advance on each next value
     * @return a new {@code Stream} of int values starting from {@code from}
     */
    public static Stream<Integer> from(int value, int step) {
        return Stream.ofAll(Iterator.from(value, step));
    }

    /**
     * Returns an infinitely long Stream of {@code long} values starting from {@code from}.
     * <p>
     * The {@code Stream} extends to {@code Integer.MIN_VALUE} when passing {@code Long.MAX_VALUE}.
     *
     * @param value a start long value
     * @return a new Stream of long values starting from {@code from}
     */
    public static Stream<Long> from(long value) {
        return Stream.ofAll(Iterator.from(value));
    }

    /**
     * Returns an infinite long Stream of {@code long} values starting from {@code value} and spaced by {@code step}.
     * <p>
     * The {@code Stream} extends to {@code Long.MIN_VALUE} when passing {@code Long.MAX_VALUE}.
     *
     * @param value a start long value
     * @param step  the step by which to advance on each next value
     * @return a new {@code Stream} of long values starting from {@code from}
     */
    public static Stream<Long> from(long value, long step) {
        return Stream.ofAll(Iterator.from(value, step));
    }

    /**
     * Generates an (theoretically) infinitely long Stream using a value Supplier.
     *
     * @param supplier A Supplier of Stream values
     * @param <T>      value type
     * @return A new Stream
     */
    public static <T> Stream<T> continually(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return Stream.ofAll(Iterator.continually(supplier));
    }

    /**
     * Generates a (theoretically) infinitely long Stream using a function to calculate the next value
     * based on the previous.
     *
     * @param seed The first value in the Stream
     * @param f    A function to calculate the next value based on the previous
     * @param <T>  value type
     * @return A new Stream
     */
    public static <T> Stream<T> iterate(T seed, Function<? super T, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return Stream.ofAll(Iterator.iterate(seed, f));
    }

    /**
     * Generates a (theoretically) infinitely long Stream using a repeatedly invoked supplier
     * that provides a {@code Some} for each next value and a {@code None} for the end.
     * The {@code Supplier} will be invoked only that many times until it returns {@code None},
     * and repeated iteration over the stream will produce the same values in the same order,
     * without any further invocations to the {@code Supplier}.
     *
     * @param supplier A Supplier of iterator values
     * @param <T> value type
     * @return A new Stream
     */
    public static <T> Stream<T> iterate(Supplier<? extends Option<? extends T>> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return Stream.ofAll(Iterator.iterate(supplier));
    }

    /**
     * Constructs a Stream of a head element and a tail supplier.
     *
     * @param head         The head element of the Stream
     * @param tailSupplier A supplier of the tail values. To end the stream, return {@link Stream#empty}.
     * @param <T>          value type
     * @return A new Stream
     */
    @SuppressWarnings("unchecked")
    public static <T> Stream<T> cons(T head, Supplier<? extends Stream<? extends T>> tailSupplier) {
        Objects.requireNonNull(tailSupplier, "tailSupplier is null");
        return new ConsImpl<>(head, (Supplier<Stream<T>>) tailSupplier);
    }

    /**
     * Returns the single instance of Nil. Convenience method for {@code Nil.instance()}.
     * <p>
     * Note: this method intentionally returns type {@code Stream} and not {@code Nil}. This comes handy when folding.
     * If you explicitly need type {@code Nil} use {@linkplain Empty#instance()}.
     *
     * @param <T> Component type of Nil, determined by type inference in the particular context.
     * @return The empty list.
     */
    public static <T> Stream<T> empty() {
        return Empty.instance();
    }

    /**
     * Narrows a widened {@code Stream<? extends T>} to {@code Stream<T>}
     * by performing a type-safe cast. This is eligible because immutable/read-only
     * collections are covariant.
     *
     * @param stream A {@code Stream}.
     * @param <T>    Component type of the {@code Stream}.
     * @return the given {@code stream} instance as narrowed type {@code Stream<T>}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Stream<T> narrow(Stream<? extends T> stream) {
        return (Stream<T>) stream;
    }

    /**
     * Returns a singleton {@code Stream}, i.e. a {@code Stream} of one element.
     *
     * @param element An element.
     * @param <T>     The component type
     * @return A new Stream instance containing the given element
     */
    public static <T> Stream<T> of(T element) {
        return cons(element, Empty::instance);
    }

    /**
     * Creates a Stream of the given elements.
     *
     * <pre><code>  Stream.of(1, 2, 3, 4)
     * = Nil.instance().prepend(4).prepend(3).prepend(2).prepend(1)
     * = new Cons(1, new Cons(2, new Cons(3, new Cons(4, Nil.instance()))))</code></pre>
     *
     * @param <T>      Component type of the Stream.
     * @param elements Zero or more elements.
     * @return A list containing the given elements in the same order.
     */
    @SafeVarargs
    public static <T> Stream<T> of(T... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(new Iterator<T>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < elements.length;
            }

            @Override
            public T next() {
                return elements[i++];
            }
        });
    }

    /**
     * Returns a Stream containing {@code n} values of a given Function {@code f}
     * over a range of integer values from 0 to {@code n - 1}.
     *
     * @param <T> Component type of the Stream
     * @param n   The number of elements in the Stream
     * @param f   The Function computing element values
     * @return A Stream consisting of elements {@code f(0),f(1), ..., f(n - 1)}
     * @throws NullPointerException if {@code f} is null
     */
    public static <T> Stream<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return Stream.ofAll(io.vavr.collection.Collections.tabulate(n, f));
    }

    /**
     * Returns a Stream containing {@code n} values supplied by a given Supplier {@code s}.
     *
     * @param <T> Component type of the Stream
     * @param n   The number of elements in the Stream
     * @param s   The Supplier computing element values
     * @return A Stream of size {@code n}, where each element contains the result supplied by {@code s}.
     * @throws NullPointerException if {@code s} is null
     */
    public static <T> Stream<T> fill(int n, Supplier<? extends T> s) {
        Objects.requireNonNull(s, "s is null");
        return Stream.ofAll(io.vavr.collection.Collections.fill(n, s));
    }

    /**
     * Returns a Stream containing {@code n} times the given {@code element}
     *
     * @param <T>     Component type of the Stream
     * @param n       The number of elements in the Stream
     * @param element The element
     * @return A Stream of size {@code n}, where each element is the given {@code element}.
     */
    public static <T> Stream<T> fill(int n, T element) {
        return Stream.ofAll(io.vavr.collection.Collections.fillObject(n, element));
    }

    /**
     * Creates a Stream of the given elements.
     *
     * @param <T>      Component type of the Stream.
     * @param elements An Iterable of elements.
     * @return A Stream containing the given elements in the same order.
     */
    @SuppressWarnings("unchecked")
    public static <T> Stream<T> ofAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (elements instanceof Stream) {
            return (Stream<T>) elements;
        } else if (elements instanceof ListView
                && ((ListView<T, ?>) elements).getDelegate() instanceof Stream) {
            return (Stream<T>) ((ListView<T, ?>) elements).getDelegate();
        } else {
            return StreamFactory.create(elements.iterator());
        }
    }

    /**
     * Creates a Stream that contains the elements of the given {@link java.util.stream.Stream}.
     *
     * @param javaStream A {@link java.util.stream.Stream}
     * @param <T>        Component type of the Stream.
     * @return A Stream containing the given elements in the same order.
     */
    public static <T> Stream<T> ofAll(java.util.stream.Stream<? extends T> javaStream) {
        Objects.requireNonNull(javaStream, "javaStream is null");
        return StreamFactory.create(javaStream.iterator());
    }

    /**
     * Creates a Stream from boolean values.
     *
     * @param elements boolean values
     * @return A new Stream of Boolean values
     * @throws NullPointerException if elements is null
     */
    public static Stream<Boolean> ofAll(boolean... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a Stream from byte values.
     *
     * @param elements byte values
     * @return A new Stream of Byte values
     * @throws NullPointerException if elements is null
     */
    public static Stream<Byte> ofAll(byte... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a Stream from char values.
     *
     * @param elements char values
     * @return A new Stream of Character values
     * @throws NullPointerException if elements is null
     */
    public static Stream<Character> ofAll(char... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a Stream values double values.
     *
     * @param elements double values
     * @return A new Stream of Double values
     * @throws NullPointerException if elements is null
     */
    public static Stream<Double> ofAll(double... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a Stream from float values.
     *
     * @param elements float values
     * @return A new Stream of Float values
     * @throws NullPointerException if elements is null
     */
    public static Stream<Float> ofAll(float... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a Stream from int values.
     *
     * @param elements int values
     * @return A new Stream of Integer values
     * @throws NullPointerException if elements is null
     */
    public static Stream<Integer> ofAll(int... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a Stream from long values.
     *
     * @param elements long values
     * @return A new Stream of Long values
     * @throws NullPointerException if elements is null
     */
    public static Stream<Long> ofAll(long... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(Iterator.ofAll(elements));
    }

    /**
     * Creates a Stream from short values.
     *
     * @param elements short values
     * @return A new Stream of Short values
     * @throws NullPointerException if elements is null
     */
    public static Stream<Short> ofAll(short... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return Stream.ofAll(Iterator.ofAll(elements));
    }

    public static Stream<Character> range(char from, char toExclusive) {
        return Stream.ofAll(Iterator.range(from, toExclusive));
    }

    public static Stream<Character> rangeBy(char from, char toExclusive, int step) {
        return Stream.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static Stream<Double> rangeBy(double from, double toExclusive, double step) {
        return Stream.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    /**
     * Creates a Stream of int numbers starting from {@code from}, extending to {@code toExclusive - 1}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Stream.range(0, 0)  // = Stream()
     * Stream.range(2, 0)  // = Stream()
     * Stream.range(-2, 2) // = Stream(-2, -1, 0, 1)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1
     * @return a range of int values as specified or {@code Nil} if {@code from >= toExclusive}
     */
    public static Stream<Integer> range(int from, int toExclusive) {
        return Stream.ofAll(Iterator.range(from, toExclusive));
    }

    /**
     * Creates a Stream of int numbers starting from {@code from}, extending to {@code toExclusive - 1},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Stream.rangeBy(1, 3, 1)  // = Stream(1, 2)
     * Stream.rangeBy(1, 4, 2)  // = Stream(1, 3)
     * Stream.rangeBy(4, 1, -2) // = Stream(4, 2)
     * Stream.rangeBy(4, 1, 2)  // = Stream()
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1
     * @param step        the step
     * @return a range of long values as specified or {@code Nil} if<br>
     * {@code from >= toInclusive} and {@code step > 0} or<br>
     * {@code from <= toInclusive} and {@code step < 0}
     * @throws IllegalArgumentException if {@code step} is zero
     */
    public static Stream<Integer> rangeBy(int from, int toExclusive, int step) {
        return Stream.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    /**
     * Creates a Stream of long numbers starting from {@code from}, extending to {@code toExclusive - 1}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Stream.range(0L, 0L)  // = Stream()
     * Stream.range(2L, 0L)  // = Stream()
     * Stream.range(-2L, 2L) // = Stream(-2L, -1L, 0L, 1L)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1
     * @return a range of long values as specified or {@code Nil} if {@code from >= toExclusive}
     */
    public static Stream<Long> range(long from, long toExclusive) {
        return Stream.ofAll(Iterator.range(from, toExclusive));
    }

    /**
     * Creates a Stream of long numbers starting from {@code from}, extending to {@code toExclusive - 1},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Stream.rangeBy(1L, 3L, 1L)  // = Stream(1L, 2L)
     * Stream.rangeBy(1L, 4L, 2L)  // = Stream(1L, 3L)
     * Stream.rangeBy(4L, 1L, -2L) // = Stream(4L, 2L)
     * Stream.rangeBy(4L, 1L, 2L)  // = Stream()
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1
     * @param step        the step
     * @return a range of long values as specified or {@code Nil} if<br>
     * {@code from >= toInclusive} and {@code step > 0} or<br>
     * {@code from <= toInclusive} and {@code step < 0}
     * @throws IllegalArgumentException if {@code step} is zero
     */
    public static Stream<Long> rangeBy(long from, long toExclusive, long step) {
        return Stream.ofAll(Iterator.rangeBy(from, toExclusive, step));
    }

    public static Stream<Character> rangeClosed(char from, char toInclusive) {
        return Stream.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    public static Stream<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return Stream.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    public static Stream<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return Stream.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    /**
     * Creates a Stream of int numbers starting from {@code from}, extending to {@code toInclusive}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Stream.rangeClosed(0, 0)  // = Stream(0)
     * Stream.rangeClosed(2, 0)  // = Stream()
     * Stream.rangeClosed(-2, 2) // = Stream(-2, -1, 0, 1, 2)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toInclusive the last number
     * @return a range of int values as specified or {@code Nil} if {@code from > toInclusive}
     */
    public static Stream<Integer> rangeClosed(int from, int toInclusive) {
        return Stream.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    /**
     * Creates a Stream of int numbers starting from {@code from}, extending to {@code toInclusive},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Stream.rangeClosedBy(1, 3, 1)  // = Stream(1, 2, 3)
     * Stream.rangeClosedBy(1, 4, 2)  // = Stream(1, 3)
     * Stream.rangeClosedBy(4, 1, -2) // = Stream(4, 2)
     * Stream.rangeClosedBy(4, 1, 2)  // = Stream()
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toInclusive the last number
     * @param step        the step
     * @return a range of int values as specified or {@code Nil} if<br>
     * {@code from > toInclusive} and {@code step > 0} or<br>
     * {@code from < toInclusive} and {@code step < 0}
     * @throws IllegalArgumentException if {@code step} is zero
     */
    public static Stream<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return Stream.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    /**
     * Creates a Stream of long numbers starting from {@code from}, extending to {@code toInclusive}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Stream.rangeClosed(0L, 0L)  // = Stream(0L)
     * Stream.rangeClosed(2L, 0L)  // = Stream()
     * Stream.rangeClosed(-2L, 2L) // = Stream(-2L, -1L, 0L, 1L, 2L)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toInclusive the last number
     * @return a range of long values as specified or {@code Nil} if {@code from > toInclusive}
     */
    public static Stream<Long> rangeClosed(long from, long toInclusive) {
        return Stream.ofAll(Iterator.rangeClosed(from, toInclusive));
    }

    /**
     * Creates a Stream of long numbers starting from {@code from}, extending to {@code toInclusive},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Stream.rangeClosedBy(1L, 3L, 1L)  // = Stream(1L, 2L, 3L)
     * Stream.rangeClosedBy(1L, 4L, 2L)  // = Stream(1L, 3L)
     * Stream.rangeClosedBy(4L, 1L, -2L) // = Stream(4L, 2L)
     * Stream.rangeClosedBy(4L, 1L, 2L)  // = Stream()
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toInclusive the last number
     * @param step        the step
     * @return a range of int values as specified or {@code Nil} if<br>
     * {@code from > toInclusive} and {@code step > 0} or<br>
     * {@code from < toInclusive} and {@code step < 0}
     * @throws IllegalArgumentException if {@code step} is zero
     */
    public static Stream<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return Stream.ofAll(Iterator.rangeClosedBy(from, toInclusive, step));
    }

    /**
     * Transposes the rows and columns of a {@link Stream} matrix.
     *
     * @param <T> matrix element type
     * @param matrix to be transposed.
     * @return a transposed {@link Stream} matrix.
     * @throws IllegalArgumentException if the row lengths of {@code matrix} differ.
     * <p>
     * ex: {@code
     * Stream.transpose(Stream(Stream(1,2,3), Stream(4,5,6))) → Stream(Stream(1,4), Stream(2,5), Stream(3,6))
     * }
     */
    public static <T> Stream<Stream<T>> transpose(Stream<Stream<T>> matrix) {
        return io.vavr.collection.Collections.transpose(matrix, Stream::ofAll, Stream::of);
    }

    /**
     * Creates a Stream from a seed value and a function.
     * The function takes the seed at first.
     * The function should return {@code None} when it's
     * done generating the Stream, otherwise {@code Some} {@code Tuple}
     * of the element for the next call and the value to add to the
     * resulting Stream.
     * <p>
     * Example:
     * <pre>
     * <code>
     * Stream.unfoldRight(10, x -&gt; x == 0
     *             ? Option.none()
     *             : Option.of(new Tuple2&lt;&gt;(x, x-1)));
     * // Stream(10, 9, 8, 7, 6, 5, 4, 3, 2, 1))
     * </code>
     * </pre>
     *
     * @param <T>  type of seeds
     * @param <U>  type of unfolded values
     * @param seed the start value for the iteration
     * @param f    the function to get the next step of the iteration
     * @return a Stream with the values built up by the iteration
     * @throws NullPointerException if {@code f} is null
     */
    public static <T, U> Stream<U> unfoldRight(T seed, Function<? super T, Option<Tuple2<? extends U, ? extends T>>> f) {
        return Iterator.unfoldRight(seed, f).toStream();
    }

    /**
     * Creates a Stream from a seed value and a function.
     * The function takes the seed at first.
     * The function should return {@code None} when it's
     * done generating the Stream, otherwise {@code Some} {@code Tuple}
     * of the value to add to the resulting Stream and
     * the element for the next call.
     * <p>
     * Example:
     * <pre>
     * <code>
     * Stream.unfoldLeft(10, x -&gt; x == 0
     *             ? Option.none()
     *             : Option.of(new Tuple2&lt;&gt;(x-1, x)));
     * // Stream(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
     * </code>
     * </pre>
     *
     * @param <T>  type of seeds
     * @param <U>  type of unfolded values
     * @param seed the start value for the iteration
     * @param f    the function to get the next step of the iteration
     * @return a Stream with the values built up by the iteration
     * @throws NullPointerException if {@code f} is null
     */
    public static <T, U> Stream<U> unfoldLeft(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends U>>> f) {
        return Iterator.unfoldLeft(seed, f).toStream();
    }

    /**
     * Creates a Stream from a seed value and a function.
     * The function takes the seed at first.
     * The function should return {@code None} when it's
     * done generating the Stream, otherwise {@code Some} {@code Tuple}
     * of the value to add to the resulting Stream and
     * the element for the next call.
     * <p>
     * Example:
     * <pre>
     * <code>
     * Stream.unfold(10, x -&gt; x == 0
     *             ? Option.none()
     *             : Option.of(new Tuple2&lt;&gt;(x-1, x)));
     * // Stream(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
     * </code>
     * </pre>
     *
     * @param <T>  type of seeds and unfolded values
     * @param seed the start value for the iteration
     * @param f    the function to get the next step of the iteration
     * @return a Stream with the values built up by the iteration
     * @throws NullPointerException if {@code f} is null
     */
    public static <T> Stream<T> unfold(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends T>>> f) {
        return Iterator.unfold(seed, f).toStream();
    }

    /**
     * Repeats an element infinitely often.
     *
     * @param t   An element
     * @param <T> Element type
     * @return A new Stream containing infinite {@code t}'s.
     */
    public static <T> Stream<T> continually(T t) {
        return Stream.ofAll(Iterator.continually(t));
    }

    @Override
    public abstract Stream<T> append(T element);

    @Override
    public abstract Stream<T> appendAll(Iterable<? extends T> elements);

    /**
     * Appends itself to the end of stream with {@code mapper} function.
     * <p>
     * <strong>Example:</strong>
     * <p>
     * Well known Scala code for Fibonacci infinite sequence
     * <pre>
     * <code>
     * val fibs:Stream[Int] = 0 #:: 1 #:: (fibs zip fibs.tail).map{ t =&gt; t._1 + t._2 }
     * </code>
     * </pre>
     * can be transformed to
     * <pre>
     * <code>
     * Stream.of(0, 1).appendSelf(self -&gt; self.zip(self.tail()).map(t -&gt; t._1 + t._2));
     * </code>
     * </pre>
     *
     * @param mapper an mapper
     * @return a new Stream
     */
    public final Stream<T> appendSelf(Function<? super Stream<T>, ? extends Stream<T>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return isEmpty() ? this : new AppendSelf<>((Cons<T>) this, mapper).stream();
    }

    @Override
    public final java.util.List<T> asJava() {
        return JavaConverters.asJava(this, IMMUTABLE);
    }

    @Override
    public final Stream<T> asJava(Consumer<? super java.util.List<T>> action) {
        return Collections.asJava(this, action, IMMUTABLE);
    }

    @Override
    public final java.util.List<T> asJavaMutable() {
        return JavaConverters.asJava(this, MUTABLE);
    }

    @Override
    public final Stream<T> asJavaMutable(Consumer<? super java.util.List<T>> action) {
        return Collections.asJava(this, action, MUTABLE);
    }

    @Override
    public final <R> Stream<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        return ofAll(iterator().<R> collect(partialFunction));
    }

    @Override
    public final Stream<Stream<T>> combinations() {
        return Stream.rangeClosed(0, length()).map(this::combinations).flatMap(Function.identity());
    }

    @Override
    public final Stream<Stream<T>> combinations(int k) {
        return Combinations.apply(this, Math.max(k, 0));
    }

    @Override
    public final Iterator<Stream<T>> crossProduct(int power) {
        return io.vavr.collection.Collections.crossProduct(Stream.empty(), this, power);
    }

    /**
     * Repeat the elements of this Stream infinitely.
     * <p>
     * Example:
     * <pre>
     * <code>
     * // = 1, 2, 3, 1, 2, 3, 1, 2, 3, ...
     * Stream.of(1, 2, 3).cycle();
     * </code>
     * </pre>
     *
     * @return A new Stream containing this elements cycled.
     */
    public final Stream<T> cycle() {
        return isEmpty() ? this : appendSelf(Function.identity());
    }

    /**
     * Repeat the elements of this Stream {@code count} times.
     * <p>
     * Example:
     * <pre>
     * <code>
     * // = empty
     * Stream.of(1, 2, 3).cycle(0);
     *
     * // = 1, 2, 3
     * Stream.of(1, 2, 3).cycle(1);
     *
     * // = 1, 2, 3, 1, 2, 3, 1, 2, 3
     * Stream.of(1, 2, 3).cycle(3);
     * </code>
     * </pre>
     *
     * @param count the number of cycles to be performed
     * @return A new Stream containing this elements cycled {@code count} times.
     */
    public final Stream<T> cycle(int count) {
        if (count <= 0 || isEmpty()) {
            return empty();
        } else {
            final Stream<T> self = this;
            return Stream.ofAll(new Iterator<T>() {
                Stream<T> stream = self;
                int i = count - 1;

                @Override
                public boolean hasNext() {
                    return !stream.isEmpty() || i > 0;
                }

                @Override
                public T next() {
                    if (stream.isEmpty()) {
                        i--;
                        stream = self;
                    }
                    final T result = stream.head();
                    stream = stream.tail();
                    return result;
                }
            });
        }
    }

    @Override
    public final Stream<T> distinct() {
        return distinctBy(Function.identity());
    }

    @Override
    public final Stream<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        final java.util.Set<T> seen = new java.util.TreeSet<>(comparator);
        return filter(seen::add);
    }

    @Override
    public final <U> Stream<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        final java.util.Set<U> seen = new java.util.HashSet<>();
        return filter(t -> seen.add(keyExtractor.apply(t)));
    }

    @Override
    public final Stream<T> drop(int n) {
        Stream<T> stream = this;
        while (n-- > 0 && !stream.isEmpty()) {
            stream = stream.tail();
        }
        return stream;
    }

    @Override
    public final Stream<T> dropUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return dropWhile(predicate.negate());
    }

    @Override
    public final Stream<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        Stream<T> stream = this;
        while (!stream.isEmpty() && predicate.test(stream.head())) {
            stream = stream.tail();
        }
        return stream;
    }

    @Override
    public final Stream<T> dropRight(int n) {
        if (n <= 0) {
            return this;
        } else {
            return DropRight.apply(take(n).toList(), List.empty(), drop(n));
        }
    }

    @Override
    public final Stream<T> dropRightUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return reverse().dropUntil(predicate).reverse();
    }

    @Override
    public final Stream<T> dropRightWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return dropRightUntil(predicate.negate());
    }

    @Override
    public final Stream<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (isEmpty()) {
            return this;
        } else {
            Stream<T> stream = this;
            while (!stream.isEmpty() && !predicate.test(stream.head())) {
                stream = stream.tail();
            }
            final Stream<T> finalStream = stream;
            return stream.isEmpty() ? Stream.empty()
                                    : cons(stream.head(), () -> finalStream.tail().filter(predicate));
        }
    }

    @Override
    public final Stream<T> filterNot(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Collections.filterNot(this, predicate);
    }

    @Override
    public final <U> Stream<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return isEmpty() ? Empty.instance() : Stream.ofAll(new FlatMapIterator<>(this.iterator(), mapper));
    }

    @Override
    public T apply(Integer index) {
        return get(index);
    }

    @Override
    public final T get(int index) {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("get(" + index + ") on Nil");
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("get(" + index + ")");
        }
        Stream<T> stream = this;
        for (int i = index - 1; i >= 0; i--) {
            stream = stream.tail();
            if (stream.isEmpty()) {
                throw new IndexOutOfBoundsException("get(" + index + ") on Stream of size " + (index - i));
            }
        }
        return stream.head();
    }

    @Override
    public final Option<T> getOption(int index) {
        if (isEmpty() || index < 0) {
            return Option.none();
        }
        Stream<T> stream = this;
        for (int i = index - 1; i >= 0; i--) {
            stream = stream.tail();
            if (stream.isEmpty()) {
                return Option.none();
            }
        }
        return Option.some(stream.head());
    }

    @Override
    public final T getOrElse(int index, T defaultValue) {
        return getOption(index).getOrElse(defaultValue);
    }

    @Override
    public final <C> Map<C, Stream<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return io.vavr.collection.Collections.groupBy(this, classifier, Stream::ofAll);
    }

    @Override
    public final Iterator<Stream<T>> grouped(int size) {
        return sliding(size, size);
    }

    @Override
    public final boolean hasDefiniteSize() {
        return false;
    }

    @Override
    public final int indexOf(T element, int from) {
        int index = 0;
        for (Stream<T> stream = this; !stream.isEmpty(); stream = stream.tail(), index++) {
            if (index >= from && Objects.equals(stream.head(), element)) {
                return index;
            }
        }
        return -1;
    }

    @Override
    public final Stream<T> init() {
        if (isEmpty()) {
            throw new UnsupportedOperationException("init of empty stream");
        } else {
            final Stream<T> tail = tail();
            if (tail.isEmpty()) {
                return Empty.instance();
            } else {
                return cons(head(), tail::init);
            }
        }
    }

    @Override
    public final Option<Stream<T>> initOption() {
        return isEmpty() ? Option.none() : Option.some(init());
    }

    @Override
    public final Stream<T> insert(int index, T element) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("insert(" + index + ", e)");
        } else if (index == 0) {
            return cons(element, () -> this);
        } else if (isEmpty()) {
            throw new IndexOutOfBoundsException("insert(" + index + ", e) on Nil");
        } else {
            return cons(head(), () -> tail().insert(index - 1, element));
        }
    }

    @Override
    public final Stream<T> insertAll(int index, Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (index < 0) {
            throw new IndexOutOfBoundsException("insertAll(" + index + ", elements)");
        } else if (index == 0) {
            return isEmpty() ? Stream.ofAll(elements) : Stream.<T> ofAll(elements).appendAll(this);
        } else if (isEmpty()) {
            throw new IndexOutOfBoundsException("insertAll(" + index + ", elements) on Nil");
        } else {
            return cons(head(), () -> tail().insertAll(index - 1, elements));
        }
    }

    @Override
    public final Stream<T> intersperse(T element) {
        if (isEmpty()) {
            return this;
        } else {
            return cons(head(), () -> {
                final Stream<T> tail = tail();
                return tail.isEmpty() ? tail : cons(element, () -> tail.intersperse(element));
            });
        }
    }

    /**
     * A {@code Stream} is computed lazily.
     *
     * @return true
     */
    @Override
    public final boolean isLazy() {
        return true;
    }

    @Override
    public final boolean isTraversableAgain() {
        return true;
    }

    @Override
    public final T last() {
        return Collections.last(this);
    }

    @Override
    public final int lastIndexOf(T element, int end) {
        int result = -1, index = 0;
        for (Stream<T> stream = this; index <= end && !stream.isEmpty(); stream = stream.tail(), index++) {
            if (Objects.equals(stream.head(), element)) {
                result = index;
            }
        }
        return result;
    }

    @Override
    public final int length() {
        return foldLeft(0, (n, ignored) -> n + 1);
    }

    @Override
    @Deprecated
    public boolean isDefinedAt(Integer index) {
        // we can't use length() because the stream might be infinite
        return 0 <= index && drop(index).nonEmpty();
    }

    @Override
    public final <U> Stream<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (isEmpty()) {
            return Empty.instance();
        } else {
            return cons(mapper.apply(head()), () -> tail().map(mapper));
        }
    }

    @Override
    public final Stream<T> padTo(int length, T element) {
        if (length <= 0) {
            return this;
        } else if (isEmpty()) {
            return Stream.continually(element).take(length);
        } else {
            return cons(head(), () -> tail().padTo(length - 1, element));
        }
    }

    @Override
    public final Stream<T> leftPadTo(int length, T element) {
        final int actualLength = length();
        if (length <= actualLength) {
            return this;
        } else {
            return Stream.continually(element).take(length - actualLength).appendAll(this);
        }
    }

    @Override
    public final Stream<T> orElse(Iterable<? extends T> other) {
        return isEmpty() ? ofAll(other) : this;
    }

    @Override
    public final Stream<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return isEmpty() ? ofAll(supplier.get()) : this;
    }

    @Override
    public final Stream<T> patch(int from, Iterable<? extends T> that, int replaced) {
        from = Math.max(from, 0);
        replaced = Math.max(replaced, 0);
        Stream<T> result = take(from).appendAll(that);
        from += replaced;
        result = result.appendAll(drop(from));
        return result;
    }

    @Override
    public final Tuple2<Stream<T>, Stream<T>> partition(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Tuple.of(filter(predicate), filter(predicate.negate()));
    }

    @Override
    public final Stream<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (isEmpty()) {
            return this;
        } else {
            final T head = head();
            action.accept(head);
            return cons(head, () -> tail().peek(action));
        }
    }

    @Override
    public final Stream<Stream<T>> permutations() {
        if (isEmpty()) {
            return Empty.instance();
        } else {
            final Stream<T> tail = tail();
            if (tail.isEmpty()) {
                return Stream.of(this);
            } else {
                final Stream<Stream<T>> zero = Empty.instance();
                return distinct().foldLeft(zero, (xs, x) -> {
                    final Function<Stream<T>, Stream<T>> prepend = l -> l.prepend(x);
                    return xs.appendAll(remove(x).permutations().map(prepend));
                });
            }
        }
    }

    @Override
    public final Stream<T> prepend(T element) {
        return cons(element, () -> this);
    }

    @Override
    public final Stream<T> prependAll(Iterable<? extends T> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (isEmpty()) {
            if (elements instanceof Stream) {
                @SuppressWarnings("unchecked")
                final Stream<T> stream = (Stream<T>) elements;
                return stream;
            } else {
                return Stream.ofAll(elements);
            }
        } else {
            return Stream.<T> ofAll(elements).appendAll(this);
        }
    }

    @Override
    public final Stream<T> remove(T element) {
        if (isEmpty()) {
            return this;
        } else {
            final T head = head();
            return Objects.equals(head, element) ? tail() : cons(head, () -> tail().remove(element));
        }
    }

    @Override
    public final Stream<T> removeFirst(Predicate<T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (isEmpty()) {
            return this;
        } else {
            final T head = head();
            return predicate.test(head) ? tail() : cons(head, () -> tail().removeFirst(predicate));
        }
    }

    @Override
    public final Stream<T> removeLast(Predicate<T> predicate) {
        return isEmpty() ? this : reverse().removeFirst(predicate).reverse();
    }

    @Override
    public final Stream<T> removeAt(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("removeAt(" + index + ")");
        } else if (index == 0) {
            return tail();
        } else if (isEmpty()) {
            throw new IndexOutOfBoundsException("removeAt() on Nil");
        } else {
            return cons(head(), () -> tail().removeAt(index - 1));
        }
    }

    @Override
    public final Stream<T> removeAll(T element) {
        return io.vavr.collection.Collections.removeAll(this, element);
    }

    @Override
    public final Stream<T> removeAll(Iterable<? extends T> elements) {
        return io.vavr.collection.Collections.removeAll(this, elements);
    }

    @Override
    public final Stream<T> replace(T currentElement, T newElement) {
        if (isEmpty()) {
            return this;
        } else {
            final T head = head();
            if (Objects.equals(head, currentElement)) {
                return cons(newElement, this::tail);
            } else {
                return cons(head, () -> tail().replace(currentElement, newElement));
            }
        }
    }

    @Override
    public final Stream<T> replaceAll(T currentElement, T newElement) {
        if (isEmpty()) {
            return this;
        } else {
            final T head = head();
            final T newHead = Objects.equals(head, currentElement) ? newElement : head;
            return cons(newHead, () -> tail().replaceAll(currentElement, newElement));
        }
    }

    @Override
    public final Stream<T> retainAll(Iterable<? extends T> elements) {
        return io.vavr.collection.Collections.retainAll(this, elements);
    }

    @Override
    public final Stream<T> reverse() {
        return isEmpty() ? this : foldLeft(Stream.empty(), Stream::prepend);
    }

    @Override
    public final Stream<T> rotateLeft(int n) {
        return Collections.rotateLeft(this, n);
    }

    @Override
    public final Stream<T> rotateRight(int n) {
        return Collections.rotateRight(this, n);
    }

    @Override
    public final Stream<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return scanLeft(zero, operation);
    }

    @Override
    public final <U> Stream<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        // lazily streams the elements of an iterator
        return io.vavr.collection.Collections.scanLeft(this, zero, operation, Iterator::toStream);
    }

    // not lazy!
    @Override
    public final <U> Stream<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        return io.vavr.collection.Collections.scanRight(this, zero, operation, Iterator::toStream);
    }

    @Override
    public final Stream<T> shuffle() {
        return io.vavr.collection.Collections.shuffle(this, Stream::ofAll);
    }

    @Override
    public final Stream<T> shuffle(Random random) {
        return io.vavr.collection.Collections.shuffle(this, random, Stream::ofAll);
    }

    @Override
    public final Stream<T> slice(int beginIndex, int endIndex) {
        if (beginIndex >= endIndex || isEmpty()) {
            return empty();
        } else {
            final int lowerBound = Math.max(beginIndex, 0);
            if (lowerBound == 0) {
                return cons(head(), () -> tail().slice(0, endIndex - 1));
            } else {
                return tail().slice(lowerBound - 1, endIndex - 1);
            }
        }
    }

    @Override
    public final Iterator<Stream<T>> slideBy(Function<? super T, ?> classifier) {
        return iterator().slideBy(classifier).map(Stream::ofAll);
    }

    @Override
    public final Iterator<Stream<T>> sliding(int size) {
        return sliding(size, 1);
    }

    @Override
    public final Iterator<Stream<T>> sliding(int size, int step) {
        return iterator().sliding(size, step).map(Stream::ofAll);
    }

    @Override
    public final Stream<T> sorted() {
        return isEmpty() ? this : toJavaStream().sorted().collect(Stream.collector());
    }

    @Override
    public final Stream<T> sorted(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return isEmpty() ? this : toJavaStream().sorted(comparator).collect(Stream.collector());
    }

    @Override
    public final <U extends Comparable<? super U>> Stream<T> sortBy(Function<? super T, ? extends U> mapper) {
        return sortBy(U::compareTo, mapper);
    }

    @Override
    public final <U> Stream<T> sortBy(Comparator<? super U> comparator, Function<? super T, ? extends U> mapper) {
        return Collections.sortBy(this, comparator, mapper, collector());
    }

    @Override
    public final Tuple2<Stream<T>, Stream<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Tuple.of(takeWhile(predicate), dropWhile(predicate));
    }

    @Override
    public final Tuple2<Stream<T>, Stream<T>> splitAt(int n) {
        return Tuple.of(take(n), drop(n));
    }

    @Override
    public final Tuple2<Stream<T>, Stream<T>> splitAt(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Tuple.of(takeWhile(predicate.negate()), dropWhile(predicate.negate()));
    }

    @Override
    public final Tuple2<Stream<T>, Stream<T>> splitAtInclusive(Predicate<? super T> predicate) {
        final Tuple2<Stream<T>, Stream<T>> split = splitAt(predicate);
        if (split._2.isEmpty()) {
            return split;
        } else {
            return Tuple.of(split._1.append(split._2.head()), split._2.tail());
        }
    }

    @Override
    public final String stringPrefix() {
        return "Stream";
    }

    @Override
    public final Stream<T> subSequence(int beginIndex) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException("subSequence(" + beginIndex + ")");
        }
        Stream<T> result = this;
        for (int i = 0; i < beginIndex; i++, result = result.tail()) {
            if (result.isEmpty()) {
                throw new IndexOutOfBoundsException("subSequence(" + beginIndex + ") on Stream of size " + i);
            }
        }
        return result;
    }

    @Override
    public final Stream<T> subSequence(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException("subSequence(" + beginIndex + ", " + endIndex + ")");
        }
        if (beginIndex > endIndex) {
            throw new IllegalArgumentException("subSequence(" + beginIndex + ", " + endIndex + ")");
        }
        if (beginIndex == endIndex) {
            return Empty.instance();
        } else if (isEmpty()) {
            throw new IndexOutOfBoundsException("subSequence of Nil");
        } else if (beginIndex == 0) {
            return cons(head(), () -> tail().subSequence(0, endIndex - 1));
        } else {
            return tail().subSequence(beginIndex - 1, endIndex - 1);
        }
    }

    @Override
    public abstract Stream<T> tail();

    @Override
    public final Option<Stream<T>> tailOption() {
        return isEmpty() ? Option.none() : Option.some(tail());
    }

    @Override
    public final Stream<T> take(int n) {
        if (n < 1 || isEmpty()) {
            return empty();
        } else if (n == 1) {
            return cons(head(), Stream::empty);
        } else {
            return cons(head(), () -> tail().take(n - 1));
        }
    }

    @Override
    public final Stream<T> takeUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return takeWhile(predicate.negate());
    }

    @Override
    public final Stream<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (isEmpty()) {
            return Empty.instance();
        } else {
            final T head = head();
            if (predicate.test(head)) {
                return cons(head, () -> tail().takeWhile(predicate));
            } else {
                return Empty.instance();
            }
        }
    }

    @Override
    public final Stream<T> takeRight(int n) {
        Stream<T> right = this;
        Stream<T> remaining = drop(n);
        while (!remaining.isEmpty()) {
            right = right.tail();
            remaining = remaining.tail();
        }
        return right;
    }

    @Override
    public final Stream<T> takeRightUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return reverse().takeUntil(predicate).reverse();
    }

    @Override
    public final Stream<T> takeRightWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return takeRightUntil(predicate.negate());
    }

    /**
     * Transforms this {@code Stream}.
     *
     * @param f   A transformation
     * @param <U> Type of transformation result
     * @return An instance of type {@code U}
     * @throws NullPointerException if {@code f} is null
     */
    public final <U> U transform(Function<? super Stream<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    public final Stream<T> update(int index, T element) {
        if (isEmpty()) {
            throw new IndexOutOfBoundsException("update(" + index + ", e) on Nil");
        }
        if (index < 0) {
            throw new IndexOutOfBoundsException("update(" + index + ", e)");
        }
        Stream<T> preceding = Empty.instance();
        Stream<T> tail = this;
        for (int i = index; i > 0; i--, tail = tail.tail()) {
            if (tail.isEmpty()) {
                throw new IndexOutOfBoundsException("update at " + index);
            }
            preceding = preceding.prepend(tail.head());
        }
        if (tail.isEmpty()) {
            throw new IndexOutOfBoundsException("update at " + index);
        }
        // skip the current head element because it is replaced
        return preceding.reverse().appendAll(tail.tail().prepend(element));
    }

    @Override
    public final Stream<T> update(int index, Function<? super T, ? extends T> updater) {
        Objects.requireNonNull(updater, "updater is null");
        return update(index, updater.apply(get(index)));
    }

    @Override
    public final <U> Stream<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        return zipWith(that, Tuple::of);
    }

    @Override
    public final <U, R> Stream<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return Stream.ofAll(iterator().zipWith(that, mapper));
    }

    @Override
    public final <U> Stream<Tuple2<T, U>> zipAll(Iterable<? extends U> iterable, T thisElem, U thatElem) {
        Objects.requireNonNull(iterable, "iterable is null");
        return Stream.ofAll(iterator().zipAll(iterable, thisElem, thatElem));
    }

    @Override
    public final Stream<Tuple2<T, Integer>> zipWithIndex() {
        return zipWithIndex(Tuple::of);
    }

    @Override
    public final <U> Stream<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return Stream.ofAll(iterator().zipWithIndex(mapper));
    }

    /**
     * Extends (continues) this {@code Stream} with a constantly repeated value.
     *
     * @param next value with which the stream should be extended
     * @return new {@code Stream} composed from this stream extended with a Stream of provided value
     */
    public final Stream<T> extend(T next) {
        return Stream.ofAll(this.appendAll(Stream.continually(next)));
    }

    /**
     * Extends (continues) this {@code Stream} with values provided by a {@code Supplier}
     *
     * @param nextSupplier a supplier which will provide values for extending a stream
     * @return new {@code Stream} composed from this stream extended with values provided by the supplier
     */
    public final Stream<T> extend(Supplier<? extends T> nextSupplier) {
        Objects.requireNonNull(nextSupplier, "nextSupplier is null");
        return Stream.ofAll(appendAll(Stream.continually(nextSupplier)));
    }

    /**
     * Extends (continues) this {@code Stream} with a Stream of values created by applying
     * consecutively provided {@code Function} to the last element of the original Stream.
     *
     * @param nextFunction a function which calculates the next value basing on the previous value
     * @return new {@code Stream} composed from this stream extended with values calculated by the provided function
     */
    public final Stream<T> extend(Function<? super T, ? extends T> nextFunction) {
        Objects.requireNonNull(nextFunction, "nextFunction is null");
        if (isEmpty()) {
            return this;
        } else {
            final Stream<T> that = this;
            return Stream.ofAll(new Iterator<T>() {

                Stream<T> stream = that;
                T last = null;

                @Override
                public boolean hasNext() {
                    return true;
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    if (stream.isEmpty()) {
                        stream = Stream.iterate(nextFunction.apply(last), nextFunction);
                    }
                    last = stream.head();
                    stream = stream.tail();
                    return last;
                }
            });
        }
    }

    /**
     * The empty Stream.
     * <p>
     * This is a singleton, i.e. not Cloneable.
     *
     * @param <T> Component type of the Stream.
     */
    public static final class Empty<T> extends Stream<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        private static final Empty<?> INSTANCE = new Empty<>();

        // hidden
        private Empty() {
        }

        @Override
        public Stream<T> append(T element) {
            return Stream.of(element);
        }

        @Override
        public Stream<T> appendAll(Iterable<? extends T> elements) {
            Objects.requireNonNull(elements, "elements is null");
            if (Collections.isEmpty(elements)) {
                return this;
            } else {
                return Stream.ofAll(elements);
            }
        }

        /**
         * Returns the singleton empty Stream instance.
         *
         * @param <T> Component type of the Stream
         * @return The empty Stream
         */
        @SuppressWarnings("unchecked")
        public static <T> Empty<T> instance() {
            return (Empty<T>) INSTANCE;
        }

        @Override
        public T head() {
            throw new NoSuchElementException("head of empty stream");
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Iterator<T> iterator() {
            return Iterator.empty();
        }

        @Override
        public Stream<T> tail() {
            throw new UnsupportedOperationException("tail of empty stream");
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
        public String toString() {
            return stringPrefix() + "()";
        }

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
     * Non-empty {@code Stream}, consisting of a {@code head}, and {@code tail}.
     *
     * @param <T> Component type of the Stream.
     */
    public static abstract class Cons<T> extends Stream<T> {

        private static final long serialVersionUID = 1L;

        final T head;
        final Lazy<Stream<T>> tail;

        Cons(T head, Supplier<Stream<T>> tail) {
            Objects.requireNonNull(tail, "tail is null");
            this.head = head;
            this.tail = Lazy.of(tail);
        }

        @Override
        public T head() {
            return head;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Iterator<T> iterator() {
            return new StreamIterator<>(this);
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
        public String toString() {
            final StringBuilder builder = new StringBuilder(stringPrefix()).append("(");
            Stream<T> stream = this;
            while (stream != null && !stream.isEmpty()) {
                final Cons<T> cons = (Cons<T>) stream;
                builder.append(cons.head);
                if (cons.tail.isEvaluated()) {
                    stream = stream.tail();
                    if (!stream.isEmpty()) {
                        builder.append(", ");
                    }
                } else {
                    builder.append(", ?");
                    stream = null;
                }
            }
            return builder.append(")").toString();
        }
    }

    private static final class ConsImpl<T> extends Cons<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        ConsImpl(T head, Supplier<Stream<T>> tail) {
            super(head, tail);
        }

        @Override
        public Stream<T> append(T element) {
            return new AppendElements<>(head(), io.vavr.collection.Queue.of(element), this::tail);
        }

        @Override
        public Stream<T> appendAll(Iterable<? extends T> elements) {
            Objects.requireNonNull(elements, "elements is null");
            if (Collections.isEmpty(elements)) {
                return this;
            } else {
                return Stream.ofAll(Iterator.concat(this, elements));
            }
        }

        @Override
        public Stream<T> tail() {
            return tail.get();
        }

        private Object writeReplace() {
            return new SerializationProxy<>(this);
        }

        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }
    }

    private static final class AppendElements<T> extends Cons<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        private final io.vavr.collection.Queue<T> queue;

        AppendElements(T head, io.vavr.collection.Queue<T> queue, Supplier<Stream<T>> tail) {
            super(head, tail);
            this.queue = queue;
        }

        @Override
        public Stream<T> append(T element) {
            return new AppendElements<>(head, queue.append(element), tail);
        }

        @Override
        public Stream<T> appendAll(Iterable<? extends T> elements) {
            Objects.requireNonNull(elements, "elements is null");
            return new AppendElements<>(head, queue.appendAll(elements), tail);
        }

        @Override
        public Stream<T> tail() {
            final Stream<T> t = tail.get();
            if (t.isEmpty()) {
                return Stream.ofAll(queue);
            } else {
                if (t instanceof ConsImpl) {
                    final ConsImpl<T> c = (ConsImpl<T>) t;
                    return new AppendElements<>(c.head(), queue, c.tail);
                } else {
                    final AppendElements<T> a = (AppendElements<T>) t;
                    return new AppendElements<>(a.head(), a.queue.appendAll(queue), a.tail);
                }
            }
        }

        private Object writeReplace() {
            return new SerializationProxy<>(this);
        }

        private void readObject(ObjectInputStream stream) throws InvalidObjectException {
            throw new InvalidObjectException("Proxy required");
        }
    }

    /**
     * A serialization proxy which, in this context, is used to deserialize immutable, linked Streams with final
     * instance fields.
     *
     * @param <T> The component type of the underlying stream.
     */
    // DEV NOTE: The serialization proxy pattern is not compatible with non-final, i.e. extendable,
    // classes. Also, it may not be compatible with circular object graphs.
    private static final class SerializationProxy<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        // the instance to be serialized/deserialized
        private transient Cons<T> stream;

        /**
         * Constructor for the case of serialization.
         * <p>
         * The constructor of a SerializationProxy takes an argument that concisely represents the logical state of
         * an instance of the enclosing class.
         *
         * @param stream a Cons
         */
        SerializationProxy(Cons<T> stream) {
            this.stream = stream;
        }

        /**
         * Write an object to a serialization stream.
         *
         * @param s An object serialization stream.
         * @throws java.io.IOException If an error occurs writing to the stream.
         */
        private void writeObject(ObjectOutputStream s) throws IOException {
            s.defaultWriteObject();
            s.writeInt(stream.length());
            for (Stream<T> l = stream; !l.isEmpty(); l = l.tail()) {
                s.writeObject(l.head());
            }
        }

        /**
         * Read an object from a deserialization stream.
         *
         * @param s An object deserialization stream.
         * @throws ClassNotFoundException If the object's class read from the stream cannot be found.
         * @throws InvalidObjectException If the stream contains no stream elements.
         * @throws IOException            If an error occurs reading from the stream.
         */
        private void readObject(ObjectInputStream s) throws ClassNotFoundException, IOException {
            s.defaultReadObject();
            final int size = s.readInt();
            if (size <= 0) {
                throw new InvalidObjectException("No elements");
            }
            Stream<T> temp = Empty.instance();
            for (int i = 0; i < size; i++) {
                @SuppressWarnings("unchecked")
                final T element = (T) s.readObject();
                temp = temp.append(element);
            }
            // DEV-NOTE: Cons is deserialized
            stream = (Cons<T>) temp;
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
            return stream;
        }
    }

    private static final class AppendSelf<T> {

        private final Cons<T> self;

        AppendSelf(Cons<T> self, Function<? super Stream<T>, ? extends Stream<T>> mapper) {
            this.self = appendAll(self, mapper);
        }

        private Cons<T> appendAll(Cons<T> stream, Function<? super Stream<T>, ? extends Stream<T>> mapper) {
            return (Cons<T>) Stream.cons(stream.head(), () -> {
                final Stream<T> tail = stream.tail();
                return tail.isEmpty() ? mapper.apply(self) : appendAll((Cons<T>) tail, mapper);
            });
        }

        Cons<T> stream() {
            return self;
        }
    }

    private interface Combinations {

        static <T> Stream<Stream<T>> apply(Stream<T> elements, int k) {
            if (k == 0) {
                return Stream.of(Stream.empty());
            } else {
                return elements.zipWithIndex().flatMap(
                        t -> apply(elements.drop(t._2 + 1), (k - 1)).map((Stream<T> c) -> c.prepend(t._1))
                );
            }
        }
    }

    private interface DropRight {

        // works with infinite streams by buffering elements
        static <T> Stream<T> apply(io.vavr.collection.List<T> front, io.vavr.collection.List<T> rear, Stream<T> remaining) {
            if (remaining.isEmpty()) {
                return remaining;
            } else if (front.isEmpty()) {
                return apply(rear.reverse(), io.vavr.collection.List.empty(), remaining);
            } else {
                return Stream.cons(front.head(),
                        () -> apply(front.tail(), rear.prepend(remaining.head()), remaining.tail()));
            }
        }
    }

    private interface StreamFactory {

        static <T> Stream<T> create(java.util.Iterator<? extends T> iterator) {
            return iterator.hasNext() ? Stream.cons(iterator.next(), () -> create(iterator)) : Empty.instance();
        }
    }

    private static final class StreamIterator<T> implements Iterator<T> {

        private Supplier<Stream<T>> current;

        StreamIterator(Cons<T> stream) {
            this.current = () -> stream;
        }

        @Override
        public String stringPrefix() {
            return "StreamIterator";
        }

        @Override
        public boolean hasNext() {
            return !current.get().isEmpty();
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            final Stream<T> stream = current.get();
            // DEV-NOTE: we make the stream even more lazy because the next head must not be evaluated on hasNext()
            current = stream::tail;
            return stream.head();
        }
    }

    private static final class FlatMapIterator<T, U> implements Iterator<U> {

        final Function<? super T, ? extends Iterable<? extends U>> mapper;
        final Iterator<? extends T> inputs;
        java.util.Iterator<? extends U> current = java.util.Collections.emptyIterator();

        FlatMapIterator(Iterator<? extends T> inputs, Function<? super T, ? extends Iterable<? extends U>> mapper) {
            this.inputs = inputs;
            this.mapper = mapper;
        }

        @Override
        public String stringPrefix() {
            return "FlatMapIterator";
        }

        @Override
        public boolean hasNext() {
            boolean currentHasNext;
            while (!(currentHasNext = current.hasNext()) && inputs.hasNext()) {
                current = mapper.apply(inputs.next()).iterator();
            }
            return currentHasNext;
        }

        @Override
        public U next() {
            return current.next();
        }
    }
}
