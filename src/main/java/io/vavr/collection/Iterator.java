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

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;

import static io.vavr.collection.BigDecimalHelper.areEqual;
import static io.vavr.collection.BigDecimalHelper.asDecimal;
import static java.lang.Double.NEGATIVE_INFINITY;
import static java.lang.Double.POSITIVE_INFINITY;
import static java.math.RoundingMode.HALF_UP;

/**
 * {@code io.vavr.collection.Iterator} is a compositional replacement for {@code java.util.Iterator}
 * whose purpose is to iterate <em>once</em> over a sequence of elements.
 * <p>
 * <strong>Note:</strong> Iterators encapsulate mutable state.
 * They are not meant to be used concurrently by different threads. Do not reuse Iterators, e.g. after passing to
 * {@linkplain io.vavr.collection.List#ofAll(Iterable)}.
 * <p>
 * There are two abstract methods: {@code hasNext} for checking if there is a next element available,
 * and {@code next} which removes the next element from the iterator and returns it. They can be called
 * an arbitrary amount of times. If {@code hasNext} returns false, a call of {@code next} will throw
 * a {@code NoSuchElementException}.
 * <p>
 * <strong>Caution: Other methods than {@code hasNext} and {@code next} can be called only once (exclusively).
 * More specifically, after calling a method it cannot be guaranteed that the next call will succeed.</strong>
 * <p>
 * An Iterator that can be only used once because it is a traversal pointer into a collection, and not a collection
 * itself.
 *
 * @param <T> Component type
 */
// DEV-NOTE: we prefer returning empty() over this if !hasNext() == true in order to free memory.
public interface Iterator<T> extends java.util.Iterator<T>, Traversable<T> {

    /**
     * Creates an Iterator which traverses along the concatenation of the given iterables.
     *
     * @param iterables The iterables
     * @param <T>       Component type.
     * @return A new {@code io.vavr.collection.Iterator}
     */
    @SuppressWarnings("varargs")
    @SafeVarargs
    static <T> Iterator<T> concat(Iterable<? extends T>... iterables) {
        Objects.requireNonNull(iterables, "iterables is null");
        if (iterables.length == 0) {
            return empty();
        } else {
            return new ConcatIterator<>(of(iterables).map(Iterable::iterator));
        }
    }

    /**
     * Creates an Iterator which traverses along the concatenation of the given iterables.
     *
     * @param iterables The iterable of iterables
     * @param <T>       Component type.
     * @return A new {@code io.vavr.collection.Iterator}
     */
    static <T> Iterator<T> concat(Iterable<? extends Iterable<? extends T>> iterables) {
        Objects.requireNonNull(iterables, "iterables is null");
        final Iterator<Iterator<T>> iterators = ofAll(iterables).map(Iterator::ofAll);
        if (!iterators.hasNext()) {
            return empty();
        } else {
            return new ConcatIterator<>(iterators);
        }
    }

    /**
     * Returns the singleton instance of the empty {@code Iterator}.
     * <p>
     * A call to {@link #hasNext()} will always return {@code false}.
     * A call to {@link #next()} will always throw a {@link NoSuchElementException}.
     *
     * @param <T> Element type
     * @return The empty {@code Iterator}
     */
    @SuppressWarnings("unchecked")
    static <T> Iterator<T> empty() {
        return (Iterator<T>) EmptyIterator.INSTANCE;
    }

    /**
     * Narrows a widened {@code Iterator<? extends T>} to {@code Iterator<T>}
     * by performing a type-safe cast. This is eligible because immutable/read-only
     * collections are covariant.
     *
     * @param iterator An {@code Iterator}.
     * @param <T>      Component type of the {@code Iterator}.
     * @return the given {@code iterator} instance as narrowed type {@code Iterator<T>}.
     */
    @SuppressWarnings("unchecked")
    static <T> Iterator<T> narrow(Iterator<? extends T> iterator) {
        return (Iterator<T>) iterator;
    }

    /**
     * Creates an {@code Iterator} which iterates over the given element.
     *
     * @param element An element
     * @param <T>     Element type
     * @return A new {@code Iterator}
     */
    static <T> Iterator<T> of(T element) {
        return new SingletonIterator<>(element);
    }

    /**
     * Creates an {@code Iterator} which iterates over the given elements.
     *
     * <pre>{@code
     * Iterator<Integer> iterator = Iterator.of(1, 2, 3);
     * }</pre>
     *
     * @param elements Zero or more elements
     * @param <T>      Element type
     * @return The singleton instance of the empty {@code Iterator}, if {@code elements.length == 0},
     *         otherwise a new {@code Iterator}.
     * @throws NullPointerException if {@code elements} is null
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    static <T> Iterator<T> of(T... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return (elements.length == 0) ? empty() : new ArrayIterator<>(elements);
    }

    /**
     * Creates a {@link io.vavr.collection.Iterator} based on the given {@link Iterable}. This is a convenience method for
     * {@code Iterator.ofAll(iterable.iterator()}.
     *
     * @param iterable A {@link Iterable}
     * @param <T>      Component type.
     * @return A new {@code io.vavr.collection.Iterator}
     */
    @SuppressWarnings("unchecked")
    static <T> Iterator<T> ofAll(Iterable<? extends T> iterable) {
        Objects.requireNonNull(iterable, "iterable is null");
        if (iterable instanceof Iterator) {
            return (Iterator<T>) iterable;
        } else {
            return ofAll(iterable.iterator());
        }
    }

    /**
     * Creates an Iterator based on the given Iterator by
     * delegating calls of {@code hasNext()} and {@code next()} to it.
     *
     * @param iterator A {@link java.util.Iterator}
     * @param <T>      Component type.
     * @return A new {@code io.vavr.collection.Iterator}
     */
    @SuppressWarnings("unchecked")
    static <T> Iterator<T> ofAll(java.util.Iterator<? extends T> iterator) {
        Objects.requireNonNull(iterator, "iterator is null");
        if (iterator instanceof Iterator) {
            return (Iterator<T>) iterator;
        } else {
            return new Iterator<T>() {

                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return iterator.next();
                }
            };
        }
    }

    /**
     * Creates an Iterator from boolean values.
     *
     * @param elements boolean values
     * @return A new Iterator of Boolean values
     * @throws NullPointerException if elements is null
     */
    static Iterator<Boolean> ofAll(boolean... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new Iterator<Boolean>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < elements.length;
            }

            @Override
            public Boolean next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elements[i++];
            }
        };
    }

    /**
     * Creates an Iterator from byte values.
     *
     * @param elements byte values
     * @return A new Iterator of Byte values
     * @throws NullPointerException if elements is null
     */
    static Iterator<Byte> ofAll(byte... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new Iterator<Byte>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < elements.length;
            }

            @Override
            public Byte next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elements[i++];
            }
        };
    }

    /**
     * Creates an Iterator from char values.
     *
     * @param elements char values
     * @return A new Iterator of Character values
     * @throws NullPointerException if elements is null
     */
    static Iterator<Character> ofAll(char... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new Iterator<Character>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < elements.length;
            }

            @Override
            public Character next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elements[i++];
            }
        };
    }

    /**
     * Creates ann Iterator from double values.
     *
     * @param elements double values
     * @return A new Iterator of Double values
     * @throws NullPointerException if elements is null
     */
    static Iterator<Double> ofAll(double... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new Iterator<Double>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < elements.length;
            }

            @Override
            public Double next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elements[i++];
            }
        };
    }

    /**
     * Creates an Iterator from float values.
     *
     * @param elements float values
     * @return A new Iterator of Float values
     * @throws NullPointerException if elements is null
     */
    static Iterator<Float> ofAll(float... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new Iterator<Float>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < elements.length;
            }

            @Override
            public Float next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elements[i++];
            }
        };
    }

    /**
     * Creates an Iterator from int values.
     *
     * @param elements int values
     * @return A new Iterator of Integer values
     * @throws NullPointerException if elements is null
     */
    static Iterator<Integer> ofAll(int... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new Iterator<Integer>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < elements.length;
            }

            @Override
            public Integer next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elements[i++];
            }
        };
    }

    /**
     * Creates an Iterator from long values.
     *
     * @param elements long values
     * @return A new Iterator of Long values
     * @throws NullPointerException if elements is null
     */
    static Iterator<Long> ofAll(long... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new Iterator<Long>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < elements.length;
            }

            @Override
            public Long next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elements[i++];
            }
        };
    }

    /**
     * Creates an Iterator from short values.
     *
     * @param elements short values
     * @return A new Iterator of Short values
     * @throws NullPointerException if elements is null
     */
    static Iterator<Short> ofAll(short... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return new Iterator<Short>() {
            int i = 0;

            @Override
            public boolean hasNext() {
                return i < elements.length;
            }

            @Override
            public Short next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return elements[i++];
            }
        };
    }

    /**
     * Returns an Iterator on a sequence of {@code n} values of a given Function {@code f}
     * over a range of integer values from 0 to {@code n - 1}.
     *
     * @param <T> Component type of the Iterator
     * @param n   The number of elements
     * @param f   The Function computing element values
     * @return An Iterator on a sequence of elements {@code f(0),f(1), ..., f(n - 1)}
     * @throws NullPointerException if {@code f} is null
     */
    static <T> Iterator<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return io.vavr.collection.Collections.tabulate(n, f);
    }

    /**
     * Returns an Iterator on a sequence of {@code n} values supplied by a given Supplier {@code s}.
     *
     * @param <T> Component type of the Iterator
     * @param n   The number of elements
     * @param s   The Supplier computing element values
     * @return An iterator on a sequence of {@code n} elements, where each element contains the result supplied by {@code s}.
     * @throws NullPointerException if {@code s} is null
     */
    static <T> Iterator<T> fill(int n, Supplier<? extends T> s) {
        Objects.requireNonNull(s, "s is null");
        return io.vavr.collection.Collections.fill(n, s);
    }

    /**
     * Returns a Iterator containing {@code n} times the given {@code element}
     *
     * @param <T>     Component type of the Iterator
     * @param n       The number of elements
     * @param element The element
     * @return An iterator of {@code n} sequence elements, where each element is the given {@code element}.
     */
    static <T> Iterator<T> fill(int n, T element) {
        return io.vavr.collection.Collections.fillObject(n, element);
    }

    /**
     * Creates an Iterator of characters starting from {@code from}, extending to {@code toExclusive - 1}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Iterator.range('a', 'c')  // = ('a', 'b')
     * Iterator.range('c', 'a')  // = ()
     * </code>
     * </pre>
     *
     * @param from        the first character
     * @param toExclusive the successor of the last character
     * @return a range of characters as specified or the empty range if {@code from >= toExclusive}
     */
    static Iterator<Character> range(char from, char toExclusive) {
        return rangeBy(from, toExclusive, 1);
    }

    /**
     * Creates an Iterator of characters starting from {@code from}, extending to {@code toExclusive - 1},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Iterator.rangeBy('a', 'c', 1)  // = ('a', 'b')
     * Iterator.rangeBy('a', 'd', 2)  // = ('a', 'c')
     * Iterator.rangeBy('d', 'a', -2) // = ('d', 'b')
     * Iterator.rangeBy('d', 'a', 2)  // = ()
     * </code>
     * </pre>
     *
     * @param from        the first character
     * @param toExclusive the successor of the last character if step &gt; 0, the predecessor of the last character if step &lt; 0
     * @param step        the step
     * @return a range of characters as specified or the empty range if {@code signum(step) == signum(from - toExclusive)}.
     * @throws IllegalArgumentException if {@code step} is zero
     */
    static Iterator<Character> rangeBy(char from, char toExclusive, int step) {
        return rangeBy((int) from, (int) toExclusive, step).map(i -> (char) i.shortValue());
    }

    static Iterator<Double> rangeBy(double from, double toExclusive, double step) {
        final BigDecimal fromDecimal = asDecimal(from), toDecimal = asDecimal(toExclusive), stepDecimal = asDecimal(step);
        return rangeBy(fromDecimal, toDecimal, stepDecimal).map(BigDecimal::doubleValue);
    }

    static Iterator<BigDecimal> rangeBy(BigDecimal from, BigDecimal toExclusive, BigDecimal step) {
        if (step.signum() == 0) {
            throw new IllegalArgumentException("step cannot be 0");
        } else if (areEqual(from, toExclusive) || step.signum() == from.subtract(toExclusive).signum()) {
            return empty();
        } else {
            if (step.signum() > 0) {
                return new Iterator<BigDecimal>() {
                    BigDecimal i = from;

                    @Override
                    public boolean hasNext() {
                        return i.compareTo(toExclusive) < 0;
                    }

                    @Override
                    public BigDecimal next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        final BigDecimal next = this.i;
                        this.i = next.add(step);
                        return next;
                    }
                };
            } else {
                return new Iterator<BigDecimal>() {
                    BigDecimal i = from;

                    @Override
                    public boolean hasNext() {
                        return i.compareTo(toExclusive) > 0;
                    }

                    @Override
                    public BigDecimal next() {
                        if (!hasNext()) {
                            throw new NoSuchElementException();
                        }
                        final BigDecimal next = this.i;
                        this.i = next.add(step);
                        return next;
                    }
                };
            }
        }
    }

    /**
     * Creates an Iterator of int numbers starting from {@code from}, extending to {@code toExclusive - 1}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Iterator.range(0, 0)  // = ()
     * Iterator.range(2, 0)  // = ()
     * Iterator.range(-2, 2) // = (-2, -1, 0, 1)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1
     * @return a range of int values as specified or the empty range if {@code from >= toExclusive}
     */
    static Iterator<Integer> range(int from, int toExclusive) {
        return rangeBy(from, toExclusive, 1);
    }

    /**
     * Creates an Iterator of int numbers starting from {@code from}, extending to {@code toExclusive - 1},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Iterator.rangeBy(1, 3, 1)  // = (1, 2)
     * Iterator.rangeBy(1, 4, 2)  // = (1, 3)
     * Iterator.rangeBy(4, 1, -2) // = (4, 2)
     * Iterator.rangeBy(4, 1, 2)  // = ()
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1 if step &gt; 0, the last number - 1 if step &lt; 0
     * @param step        the step
     * @return a range of long values as specified or the empty range if {@code (from == toExclusive) || (step * (from - toExclusive) > 0)}.
     * @throws IllegalArgumentException if {@code step} is zero
     */
    static Iterator<Integer> rangeBy(int from, int toExclusive, int step) {
        if (step == 0) {
            throw new IllegalArgumentException("step cannot be 0");
        }
        if (step > 0) {
            return new RangeIntForwardIterator(from, toExclusive, step, false);
        } else {
            return new RangeIntBackwardIterator(from, toExclusive, step, false);
        }
    }

    /**
     * Creates an Iterator of long numbers starting from {@code from}, extending to {@code toExclusive - 1}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Iterator.range(0L, 0L)  // = ()
     * Iterator.range(2L, 0L)  // = ()
     * Iterator.range(-2L, 2L) // = (-2L, -1L, 0L, 1L)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1
     * @return a range of long values as specified or the empty range if {@code from >= toExclusive}
     */
    static Iterator<Long> range(long from, long toExclusive) {
        return rangeBy(from, toExclusive, 1);
    }

    /**
     * Creates an Iterator of long numbers starting from {@code from}, extending to {@code toExclusive - 1},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Iterator.rangeBy(1L, 3L, 1L)  // = (1L, 2L)
     * Iterator.rangeBy(1L, 4L, 2L)  // = (1L, 3L)
     * Iterator.rangeBy(4L, 1L, -2L) // = (4L, 2L)
     * Iterator.rangeBy(4L, 1L, 2L)  // = ()
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toExclusive the last number + 1 if step &gt; 0, the last number - 1 if step &lt; 0
     * @param step        the step
     * @return a range of long values as specified or the empty range if {@code (from == toExclusive) || (step * (from - toExclusive) > 0)}.
     * @throws IllegalArgumentException if {@code step} is zero
     */
    static Iterator<Long> rangeBy(long from, long toExclusive, long step) {
        if (step == 0) {
            throw new IllegalArgumentException("step cannot be 0");
        }
        if (step > 0) {
            return new RangeLongForwardIterator(from, toExclusive, step, false);
        } else {
            return new RangeLongBackwardIterator(from, toExclusive, step, false);
        }
    }

    /**
     * Creates an Iterator of characters starting from {@code from}, extending to {@code toInclusive}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Iterator.rangeClosed('a', 'c')  // = ('a', 'b', 'c')
     * Iterator.rangeClosed('c', 'a')  // = ()
     * </code>
     * </pre>
     *
     * @param from        the first character
     * @param toInclusive the last character
     * @return a range of characters as specified or the empty range if {@code from > toInclusive}
     */
    static Iterator<Character> rangeClosed(char from, char toInclusive) {
        return rangeClosedBy(from, toInclusive, 1);
    }

    /**
     * Creates an Iterator of characters starting from {@code from}, extending to {@code toInclusive},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Iterator.rangeClosedBy('a', 'c', 1)  // = ('a', 'b', 'c')
     * Iterator.rangeClosedBy('a', 'd', 2)  // = ('a', 'c')
     * Iterator.rangeClosedBy('d', 'a', -2) // = ('d', 'b')
     * Iterator.rangeClosedBy('d', 'a', 2)  // = ()
     * </code>
     * </pre>
     *
     * @param from        the first character
     * @param toInclusive the last character
     * @param step        the step
     * @return a range of characters as specified or the empty range if {@code signum(step) == signum(from - toInclusive)}.
     * @throws IllegalArgumentException if {@code step} is zero
     */
    static Iterator<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return rangeClosedBy((int) from, (int) toInclusive, step).map(i -> (char) i.shortValue());
    }

    static Iterator<Double> rangeClosedBy(double from, double toInclusive, double step) {
        if (from == toInclusive) {
            return of(from);
        }

        final double toExclusive = (step > 0) ? Math.nextUp(toInclusive) : Math.nextDown(toInclusive);
        return rangeBy(from, toExclusive, step);
    }

    /**
     * Creates an Iterator of int numbers starting from {@code from}, extending to {@code toInclusive}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Iterator.rangeClosed(0, 0)  // = (0)
     * Iterator.rangeClosed(2, 0)  // = ()
     * Iterator.rangeClosed(-2, 2) // = (-2, -1, 0, 1, 2)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toInclusive the last number
     * @return a range of int values as specified or the empty range if {@code from > toInclusive}
     */
    static Iterator<Integer> rangeClosed(int from, int toInclusive) {
        return rangeClosedBy(from, toInclusive, 1);
    }

    /**
     * Creates an Iterator of int numbers starting from {@code from}, extending to {@code toInclusive},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Iterator.rangeClosedBy(1, 3, 1)  // = (1, 2, 3)
     * Iterator.rangeClosedBy(1, 4, 2)  // = (1, 3)
     * Iterator.rangeClosedBy(4, 1, -2) // = (4, 2)
     * Iterator.rangeClosedBy(4, 1, 2)  // = ()
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toInclusive the last number
     * @param step        the step
     * @return a range of int values as specified or the empty range if {@code signum(step) == signum(from - toInclusive)}.
     * @throws IllegalArgumentException if {@code step} is zero
     */
    static Iterator<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        if (step == 0) {
            throw new IllegalArgumentException("step cannot be 0");
        }
        if (step > 0) {
            return new RangeIntForwardIterator(from, toInclusive, step, true);
        } else {
            return new RangeIntBackwardIterator(from, toInclusive, step, true);
        }
    }

    /**
     * Creates an Iterator of long numbers starting from {@code from}, extending to {@code toInclusive}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Iterator.rangeClosed(0L, 0L)  // = (0L)
     * Iterator.rangeClosed(2L, 0L)  // = ()
     * Iterator.rangeClosed(-2L, 2L) // = (-2L, -1L, 0L, 1L, 2L)
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toInclusive the last number
     * @return a range of long values as specified or the empty range if {@code from > toInclusive}
     */
    static Iterator<Long> rangeClosed(long from, long toInclusive) {
        return rangeClosedBy(from, toInclusive, 1L);
    }

    /**
     * Creates an Iterator of long numbers starting from {@code from}, extending to {@code toInclusive},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * Iterator.rangeClosedBy(1L, 3L, 1L)  // = (1L, 2L, 3L)
     * Iterator.rangeClosedBy(1L, 4L, 2L)  // = (1L, 3L)
     * Iterator.rangeClosedBy(4L, 1L, -2L) // = (4L, 2L)
     * Iterator.rangeClosedBy(4L, 1L, 2L)  // = ()
     * </code>
     * </pre>
     *
     * @param from        the first number
     * @param toInclusive the last number
     * @param step        the step
     * @return a range of int values as specified or the empty range if {@code signum(step) == signum(from - toInclusive)}.
     * @throws IllegalArgumentException if {@code step} is zero
     */
    static Iterator<Long> rangeClosedBy(long from, long toInclusive, long step) {
        if (step == 0) {
            throw new IllegalArgumentException("step cannot be 0");
        }
        if (step > 0) {
            return new RangeLongForwardIterator(from, toInclusive, step, true);
        } else {
            return new RangeLongBackwardIterator(from, toInclusive, step, true);
        }
    }

    /**
     * Returns an infinite iterator of int values starting from {@code value}.
     * <p>
     * The {@code Iterator} extends to {@code Integer.MIN_VALUE} when passing {@code Integer.MAX_VALUE}.
     *
     * @param value a start int value
     * @return a new {@code Iterator} of int values starting from {@code from}
     */
    static Iterator<Integer> from(int value) {
        return new Iterator<Integer>() {
            private int next = value;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return next++;
            }
        };
    }

    /**
     * Returns an infinite iterator of int values starting from {@code value} and spaced by {@code step}.
     * <p>
     * The {@code Iterator} extends to {@code Integer.MIN_VALUE} when passing {@code Integer.MAX_VALUE}.
     *
     * @param value a start int value
     * @param step  the step by which to advance on each iteration
     * @return a new {@code Iterator} of int values starting from {@code from}
     */
    static Iterator<Integer> from(int value, int step) {
        return new Iterator<Integer>() {
            private int next = value;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Integer next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final int result = next;
                next += step;
                return result;
            }
        };
    }

    /**
     * Returns an infinite iterator of long values starting from {@code value}.
     * <p>
     * The {@code Iterator} extends to {@code Long.MIN_VALUE} when passing {@code Long.MAX_VALUE}.
     *
     * @param value a start long value
     * @return a new {@code Iterator} of long values starting from {@code from}
     */
    static Iterator<Long> from(long value) {
        return new Iterator<Long>() {
            private long next = value;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Long next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return next++;
            }
        };
    }

    /**
     * Returns an infinite iterator of long values starting from {@code value} and spaced by {@code step}.
     * <p>
     * The {@code Iterator} extends to {@code Long.MIN_VALUE} when passing {@code Long.MAX_VALUE}.
     *
     * @param value a start long value
     * @param step  the step by which to advance on each iteration
     * @return a new {@code Iterator} of long values starting from {@code from}
     */
    static Iterator<Long> from(long value, long step) {
        return new Iterator<Long>() {
            private long next = value;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public Long next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final long result = next;
                next += step;
                return result;
            }
        };
    }

    /**
     * Generates an infinite iterator using a value Supplier.
     *
     * @param supplier A Supplier of iterator values
     * @param <T>      value type
     * @return A new {@code Iterator}
     */
    static <T> Iterator<T> continually(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return supplier.get();
            }
        };
    }

    /**
     * Creates an iterator that repeatedly invokes the supplier
     * while it's a {@code Some} and end on the first {@code None}
     *
     * @param supplier A Supplier of iterator values
     * @param <T> value type
     * @return A new {@code Iterator}
     * @throws NullPointerException if supplier produces null value
     */
    static <T> Iterator<T> iterate(Supplier<? extends Option<? extends T>> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return new Iterator<T>() {
            Option<? extends T> nextOption;

            @Override
            public boolean hasNext() {
                if (nextOption == null) {
                    nextOption = supplier.get();
                }
                return nextOption.isDefined();
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final T next =  nextOption.get();
                nextOption = null;
                return next;
            }
        };
    }

    /**
     * Generates an infinite iterator using a function to calculate the next value
     * based on the previous.
     *
     * @param seed The first value in the iterator
     * @param f    A function to calculate the next value based on the previous
     * @param <T>  value type
     * @return A new {@code Iterator}
     */
    static <T> Iterator<T> iterate(T seed, Function<? super T, ? extends T> f) {
        Objects.requireNonNull(f, "f is null");
        return new Iterator<T>() {
            Function<? super T, ? extends T> nextFunc = s -> {
                nextFunc = f;
                return seed;
            };
            T current = null;

            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                current = nextFunc.apply(current);
                return current;
            }
        };
    }

    /**
     * Creates an infinite iterator returning the given element.
     *
     * @param t   An element
     * @param <T> Element type
     * @return A new Iterator containing infinite {@code t}'s.
     */
    static <T> Iterator<T> continually(T t) {
        return new Iterator<T>() {
            @Override
            public boolean hasNext() {
                return true;
            }

            @Override
            public T next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                return t;
            }
        };
    }

    // -- Additional methods of Iterator

    @Override
    default <R> Iterator<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        Objects.requireNonNull(partialFunction, "partialFunction is null");
        return filter(partialFunction::isDefinedAt).map(partialFunction);
    }

    // DEV-NOTE: cannot use arg Iterable, it would be ambiguous
    default Iterator<T> concat(java.util.Iterator<? extends T> that) {
        Objects.requireNonNull(that, "that is null");
        if (!that.hasNext()) {
            return this;
        } else if (!hasNext()) {
            return ofAll(that);
        } else {
            return concat(this, ofAll(that));
        }
    }

    /**
     * Inserts an element between all elements of this Iterator.
     *
     * @param element An element.
     * @return an interspersed version of this
     */
    default Iterator<T> intersperse(T element) {
        if (!hasNext()) {
            return empty();
        } else {
            final Iterator<T> that = this;
            return new Iterator<T>() {

                boolean insertElement = false;

                @Override
                public boolean hasNext() {
                    return that.hasNext();
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    if (insertElement) {
                        insertElement = false;
                        return element;
                    } else {
                        insertElement = true;
                        return that.next();
                    }
                }
            };
        }
    }

    /**
     * Transforms this {@code Iterator}.
     *
     * @param f   A transformation
     * @param <U> Type of transformation result
     * @return An instance of type {@code U}
     * @throws NullPointerException if {@code f} is null
     */
    default <U> U transform(Function<? super Iterator<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    default <U> Iterator<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        return zipWith(that, Tuple::of);
    }

    @Override
    default <U, R> Iterator<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        if (isEmpty()) {
            return empty();
        } else {
            final Iterator<T> it1 = this;
            final java.util.Iterator<? extends U> it2 = that.iterator();
            return new Iterator<R>() {
                @Override
                public boolean hasNext() {
                    return it1.hasNext() && it2.hasNext();
                }

                @Override
                public R next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return mapper.apply(it1.next(), it2.next());
                }
            };
        }
    }

    @Override
    default <U> Iterator<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        final java.util.Iterator<? extends U> thatIt = that.iterator();
        if (isEmpty() && !thatIt.hasNext()) {
            return empty();
        } else {
            final Iterator<T> thisIt = this;
            return new Iterator<Tuple2<T, U>>() {
                @Override
                public boolean hasNext() {
                    return thisIt.hasNext() || thatIt.hasNext();
                }

                @Override
                public Tuple2<T, U> next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    final T v1 = thisIt.hasNext() ? thisIt.next() : thisElem;
                    final U v2 = thatIt.hasNext() ? thatIt.next() : thatElem;
                    return Tuple.of(v1, v2);
                }
            };
        }
    }

    @Override
    default Iterator<Tuple2<T, Integer>> zipWithIndex() {
        return zipWithIndex(Tuple::of);
    }

    @Override
    default <U> Iterator<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (isEmpty()) {
            return empty();
        } else {
            final Iterator<T> it1 = this;
            return new Iterator<U>() {
                private int index = 0;

                @Override
                public boolean hasNext() {
                    return it1.hasNext();
                }

                @Override
                public U next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return mapper.apply(it1.next(), index++);
                }
            };
        }
    }

    @Override
    default <T1, T2> Tuple2<Iterator<T1>, Iterator<T2>> unzip(
            Function<? super T, ? extends T1> unzipper1, Function<? super T, ? extends T2> unzipper2) {
        Objects.requireNonNull(unzipper1, "unzipper1 is null");
        Objects.requireNonNull(unzipper2, "unzipper2 is null");
        if (!hasNext()) {
            return Tuple.of(empty(), empty());
        } else {
            final Stream<T> stream = Stream.ofAll(() -> this);
            final Iterator<T1> iter1 = stream.iterator().map(unzipper1);
            final Iterator<T2> iter2 = stream.iterator().map(unzipper2);
            return Tuple.of(iter1, iter2);
        }
    }

    @Override
    default <T1, T2, T3> Tuple3<Iterator<T1>, Iterator<T2>, Iterator<T3>> unzip3(
            Function<? super T, ? extends T1> unzipper1,
            Function<? super T, ? extends T2> unzipper2,
            Function<? super T, ? extends T3> unzipper3) {
        Objects.requireNonNull(unzipper1, "unzipper1 is null");
        Objects.requireNonNull(unzipper2, "unzipper2 is null");
        Objects.requireNonNull(unzipper3, "unzipper3 is null");
        if (!hasNext()) {
            return Tuple.of(empty(), empty(), empty());
        } else {
            final Stream<T> stream = Stream.ofAll(() -> this);
            final Iterator<T1> iter1 = stream.iterator().map(unzipper1);
            final Iterator<T2> iter2 = stream.iterator().map(unzipper2);
            final Iterator<T3> iter3 = stream.iterator().map(unzipper3);
            return Tuple.of(iter1, iter2, iter3);
        }
    }

    /**
     * Creates an iterator from a seed value and a function.
     * The function takes the seed at first.
     * The function should return {@code None} when it's
     * done generating elements, otherwise {@code Some} {@code Tuple}
     * of the value to add to the resulting iterator and
     * the element for the next call.
     * <p>
     * Example:
     * <pre>
     * <code>
     * Iterator.unfold(10, x -&gt; x == 0
     *                 ? Option.none()
     *                 : Option.of(new Tuple2&lt;&gt;(x-1, x)));
     * // List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
     * </code>
     * </pre>
     *
     * @param <T>  type of seeds and unfolded values
     * @param seed the start value for the iteration
     * @param f    the function to get the next step of the iteration
     * @return a list with the values built up by the iteration
     * @throws NullPointerException if {@code f} is null
     */
    static <T> Iterator<T> unfold(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends T>>> f) {
        return unfoldLeft(seed, f);
    }

    /**
     * Creates an iterator from a seed value and a function.
     * The function takes the seed at first.
     * The function should return {@code None} when it's
     * done generating elements, otherwise {@code Some} {@code Tuple}
     * of the value to add to the resulting iterator and
     * the element for the next call.
     * <p>
     * Example:
     * <pre>
     * <code>
     * Iterator.unfoldLeft(10, x -&gt; x == 0
     *                    ? Option.none()
     *                    : Option.of(new Tuple2&lt;&gt;(x-1, x)));
     * // List(1, 2, 3, 4, 5, 6, 7, 8, 9, 10))
     * </code>
     * </pre>
     *
     * @param <T>  type of seeds
     * @param <U>  type of unfolded values
     * @param seed the start value for the iteration
     * @param f    the function to get the next step of the iteration
     * @return a list with the values built up by the iteration
     * @throws NullPointerException if {@code f} is null
     */
    static <T, U> Iterator<U> unfoldLeft(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends U>>> f) {
        Objects.requireNonNull(f, "f is null");
        return Stream.<U> ofAll(
                unfoldRight(seed, f.andThen(tupleOpt -> tupleOpt.map(t -> Tuple.of(t._2, t._1)))))
                .reverse().iterator();
    }

    /**
     * Creates an iterator from a seed value and a function.
     * The function takes the seed at first.
     * The function should return {@code None} when it's
     * done generating elements, otherwise {@code Some} {@code Tuple}
     * of the element for the next call and the value to add to the
     * resulting iterator.
     * <p>
     * Example:
     * <pre>
     * <code>
     * Iterator.unfoldRight(10, x -&gt; x == 0
     *             ? Option.none()
     *             : Option.of(new Tuple2&lt;&gt;(x, x-1)));
     * // List(10, 9, 8, 7, 6, 5, 4, 3, 2, 1))
     * </code>
     * </pre>
     *
     * @param <T>  type of seeds
     * @param <U>  type of unfolded values
     * @param seed the start value for the iteration
     * @param f    the function to get the next step of the iteration
     * @return a list with the values built up by the iteration
     * @throws NullPointerException if {@code f} is null
     */
    static <T, U> Iterator<U> unfoldRight(T seed, Function<? super T, Option<Tuple2<? extends U, ? extends T>>> f) {
        Objects.requireNonNull(f, "the unfold iterating function is null");
        return new Iterator<U>() {
            private Option<Tuple2<? extends U, ? extends T>> nextVal = f.apply(seed);

            @Override
            public boolean hasNext() {
                return nextVal.isDefined();
            }

            @Override
            public U next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                final U result = nextVal.get()._1;
                nextVal = f.apply(nextVal.get()._2);
                return result;
            }
        };
    }

    // -- Overridden methods of Traversable

    @Override
    default Iterator<T> distinct() {
        if (!hasNext()) {
            return empty();
        } else {
            return new DistinctIterator<>(this, io.vavr.collection.HashSet.empty(), Function.identity());
        }
    }

    @Override
    default Iterator<T> distinctBy(Comparator<? super T> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        if (!hasNext()) {
            return empty();
        } else {
            return new DistinctIterator<>(this, TreeSet.empty(comparator), Function.identity());
        }
    }

    @Override
    default <U> Iterator<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        if (!hasNext()) {
            return empty();
        } else {
            return new DistinctIterator<>(this, io.vavr.collection.HashSet.empty(), keyExtractor);
        }
    }

    /**
     * Removes up to n elements from this iterator.
     *
     * @param n A number
     * @return The empty iterator, if {@code n <= 0} or this is empty, otherwise a new iterator without the first n elements.
     */
    @Override
    default Iterator<T> drop(int n) {
        if (n <= 0) {
            return this;
        } else if (!hasNext()) {
            return empty();
        } else {
            final Iterator<T> that = this;
            return new Iterator<T>() {

                long count = n;

                @Override
                public boolean hasNext() {
                    while (count > 0 && that.hasNext()) {
                        that.next(); // discarded
                        count--;
                    }
                    return that.hasNext();
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return that.next();
                }
            };
        }
    }

    @Override
    default Iterator<T> dropRight(int n) {
        if (n <= 0) {
            return this;
        } else if (!hasNext()) {
            return empty();
        } else {
            final Iterator<T> that = this;
            return new Iterator<T>() {
                private io.vavr.collection.Queue<T> queue = io.vavr.collection.Queue.empty();

                @Override
                public boolean hasNext() {
                    while (queue.length() < n && that.hasNext()) {
                        queue = queue.append(that.next());
                    }
                    return queue.length() == n && that.hasNext();
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    final Tuple2<T, io.vavr.collection.Queue<T>> t = queue.append(that.next()).dequeue();
                    queue = t._2;
                    return t._1;
                }
            };
        }
    }

    @Override
    default Iterator<T> dropUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return dropWhile(predicate.negate());
    }

    @Override
    default Iterator<T> dropWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (!hasNext()) {
            return empty();
        } else {
            final CachedIterator<T> that = new CachedIterator<>(this);
            while (that.hasNext() && predicate.test(that.touch())) {
                that.next();
            }
            return that;
        }
    }

    /**
     * Returns an Iterator that contains elements that satisfy the given {@code predicate}.
     *
     * @param predicate A predicate
     * @return A new Iterator
     */
    @Override
    default Iterator<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (!hasNext()) {
            return empty();
        } else {
            final Iterator<T> that = this;
            return new Iterator<T>() {

                Option<T> next = Option.none();

                @Override
                public boolean hasNext() {
                    while (next.isEmpty() && that.hasNext()) {
                        final T candidate = that.next();
                        if (predicate.test(candidate)) {
                            next = Option.some(candidate);
                        }
                    }
                    return next.isDefined();
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    final T result = next.get();
                    next = Option.none();
                    return result;
                }
            };
        }
    }

    /**
     * Returns an Iterator that contains elements that not satisfy the given {@code predicate}.
     *
     * @param predicate A predicate
     * @return A new Iterator
     */
    @Override
    default Iterator<T> filterNot(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return filter(predicate.negate());
    }

    @Override
    default Option<T> findLast(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        T last = null;
        while (hasNext()) {
            final T elem = next();
            if (predicate.test(elem)) {
                last = elem;
            }
        }
        return Option.of(last);
    }

    /**
     * Maps the elements of this Iterator to Iterables and concats their iterators.
     *
     * @param mapper A mapper
     * @param <U>    Component type of the resulting Iterator
     * @return A new Iterator
     */
    // DEV-NOTE: the shorter implementation `concat(map(mapper))` does not perform well
    @Override
    default <U> Iterator<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (!hasNext()) {
            return empty();
        } else {
            final Iterator<T> that = this;
            return new Iterator<U>() {

                final Iterator<? extends T> inputs = that;
                java.util.Iterator<? extends U> current = java.util.Collections.emptyIterator();

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
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return current.next();
                }
            };
        }
    }

    @Override
    default <U> U foldRight(U zero, BiFunction<? super T, ? super U, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return Stream.ofAll(this).foldRight(zero, f);
    }

    @Override
    default T get() {
        return head();
    }

    @Override
    default <C> Map<C, Iterator<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return io.vavr.collection.Collections.groupBy(this, classifier, Iterator::ofAll);
    }

    @Override
    default Iterator<Seq<T>> grouped(int size) {
        return new GroupedIterator<>(this, size, size);
    }

    @Override
    default boolean hasDefiniteSize() {
        return false;
    }

    @Override
    default T head() {
        if (!hasNext()) {
            throw new NoSuchElementException("head() on empty iterator");
        }
        return next();
    }

    @Override
    default Iterator<T> init() {
        if (!hasNext()) {
            throw new UnsupportedOperationException();
        } else {
            return dropRight(1);
        }
    }

    @Override
    default Option<Iterator<T>> initOption() {
        return hasNext() ? Option.some(init()) : Option.none();
    }

    /**
     * An {@code Iterator} is computed synchronously.
     *
     * @return false
     */
    @Override
    default boolean isAsync() {
        return false;
    }

    @Override
    default boolean isEmpty() {
        return !hasNext();
    }

    /**
     * An {@code Iterator} is computed lazily.
     *
     * @return true
     */
    @Override
    default boolean isLazy() {
        return true;
    }

    @Override
    default boolean isTraversableAgain() {
        return false;
    }

    @Override
    default boolean isSequential() {
        return true;
    }

    @Override
    default Iterator<T> iterator() {
        return this;
    }

    @Override
    default T last() {
        return Collections.last(this);
    }

    @Override
    default int length() {
        return foldLeft(0, (n, ignored) -> n + 1);
    }

    /**
     * Maps the elements of this Iterator lazily using the given {@code mapper}.
     *
     * @param mapper A mapper.
     * @param <U>    Component type
     * @return A new Iterator
     */
    @Override
    default <U> Iterator<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (!hasNext()) {
            return empty();
        } else {
            final Iterator<T> that = this;
            return new Iterator<U>() {

                @Override
                public boolean hasNext() {
                    return that.hasNext();
                }

                @Override
                public U next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return mapper.apply(that.next());
                }
            };
        }
    }

    /**
     * A safe alternative to {@link #next()} that is equivalent to
     *
     * <pre>{@code
     * hasNext() ? Option.some(next()) : Option.none()
     * }</pre>
     *
     * @return a new instance of {@link Option}
     */
    default Option<T> nextOption() {
        return hasNext() ? Option.some(next()) : Option.none();
    }

    @Override
    default Iterator<T> orElse(Iterable<? extends T> other) {
        return isEmpty() ? ofAll(other) : this;
    }

    @Override
    default Iterator<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return isEmpty() ? ofAll(supplier.get()) : this;
    }

    @Override
    default Tuple2<Iterator<T>, Iterator<T>> partition(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (!hasNext()) {
            return Tuple.of(empty(), empty());
        } else {
            final Tuple2<Iterator<T>, Iterator<T>> dup = IteratorModule.duplicate(this);
            return Tuple.of(dup._1.filter(predicate), dup._2.filterNot(predicate));
        }
    }

    @Override
    default Iterator<T> peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (!hasNext()) {
            return empty();
        } else {
            final Iterator<T> that = this;
            return new Iterator<T>() {
                @Override
                public boolean hasNext() {
                    return that.hasNext();
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    final T next = that.next();
                    action.accept(next);
                    return next;
                }
            };
        }
    }

    @Override
    default Iterator<T> tapEach(Consumer<? super T> action) {
        return peek(action);
    }

    @Override
    default T reduceLeft(BiFunction<? super T, ? super T, ? extends T> op) {
        Objects.requireNonNull(op, "op is null");
        if (isEmpty()) {
            throw new NoSuchElementException("reduceLeft on Nil");
        } else {
            T xs = next();
            while (hasNext()) {
                xs = op.apply(xs, next());
            }
            return xs;
        }
    }

    @Override
    default T reduceRight(BiFunction<? super T, ? super T, ? extends T> op) {
        Objects.requireNonNull(op, "op is null");
        if (isEmpty()) {
            throw new NoSuchElementException("reduceRight on Nil");
        } else {
            final Stream<T> reversed = Stream.ofAll(this).reverse();
            return reversed.reduceLeft((xs, x) -> op.apply(x, xs));
        }
    }

    @Override
    default Iterator<T> replace(T currentElement, T newElement) {
        if (!hasNext()) {
            return empty();
        } else {
            final Iterator<T> that = this;
            return new Iterator<T>() {
                boolean isFirst = true;

                @Override
                public boolean hasNext() {
                    return that.hasNext();
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    final T elem = that.next();
                    if (isFirst && Objects.equals(currentElement, elem)) {
                        isFirst = false;
                        return newElement;
                    } else {
                        return elem;
                    }
                }
            };
        }
    }

    @Override
    default Iterator<T> replaceAll(T currentElement, T newElement) {
        if (!hasNext()) {
            return empty();
        } else {
            final Iterator<T> that = this;
            return new Iterator<T>() {

                @Override
                public boolean hasNext() {
                    return that.hasNext();
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    final T elem = that.next();
                    if (Objects.equals(currentElement, elem)) {
                        return newElement;
                    } else {
                        return elem;
                    }
                }
            };
        }
    }

    @Override
    default Iterator<T> retainAll(Iterable<? extends T> elements) {
        return io.vavr.collection.Collections.retainAll(this, elements);
    }

    @Override
    default Traversable<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        return scanLeft(zero, operation);
    }

    @Override
    default <U> Iterator<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        Objects.requireNonNull(operation, "operation is null");
        if (isEmpty()) {
            return of(zero);
        } else {
            final Iterator<T> that = this;
            return new Iterator<U>() {

                boolean isFirst = true;
                U acc = zero;

                @Override
                public boolean hasNext() {
                    return isFirst || that.hasNext();
                }

                @Override
                public U next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    if (isFirst) {
                        isFirst = false;
                        return acc;
                    } else {
                        acc = operation.apply(acc, that.next());
                        return acc;
                    }
                }
            };
        }
    }

    // not lazy!
    @Override
    default <U> Iterator<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        Objects.requireNonNull(operation, "operation is null");
        if (isEmpty()) {
            return of(zero);
        } else {
            return io.vavr.collection.Collections.scanRight(this, zero, operation, Function.identity());
        }
    }

    @Override
    default Iterator<Seq<T>> slideBy(Function<? super T, ?> classifier) {
        Objects.requireNonNull(classifier, "classifier is null");
        if (!hasNext()) {
            return empty();
        } else {
            final CachedIterator<T> source = new CachedIterator<>(this);
            return new Iterator<Seq<T>>() {
                private Stream<T> next = null;

                @Override
                public boolean hasNext() {
                    if (next == null && source.hasNext()) {
                        final Object key = classifier.apply(source.touch());
                        final java.util.List<T> acc = new ArrayList<>();
                        acc.add(source.next());
                        while (source.hasNext() && key.equals(classifier.apply(source.touch()))) {
                            acc.add(source.next());
                        }
                        next = Stream.ofAll(acc);
                    }
                    return next != null;
                }

                @Override
                public Stream<T> next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    final Stream<T> result = next;
                    next = null;
                    return result;
                }
            };
        }
    }

    @Override
    default Iterator<Seq<T>> sliding(int size) {
        return sliding(size, 1);
    }

    @Override
    default Iterator<Seq<T>> sliding(int size, int step) {
        return new GroupedIterator<>(this, size, step);
    }

    @Override
    default Tuple2<Iterator<T>, Iterator<T>> span(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (!hasNext()) {
            return Tuple.of(empty(), empty());
        } else {
            final Stream<T> that = Stream.ofAll(this);
            return Tuple.of(that.iterator().takeWhile(predicate), that.iterator().dropWhile(predicate));
        }
    }


    @Override
    default String stringPrefix() {
        return "Iterator";
    }

    @Override
    default Iterator<T> tail() {
        if (!hasNext()) {
            throw new UnsupportedOperationException();
        } else {
            next(); // remove first element
            return this;
        }
    }

    @Override
    default Option<Iterator<T>> tailOption() {
        if (hasNext()) {
            next();
            return Option.some(this);
        } else {
            return Option.none();
        }
    }

    /**
     * Take the first n elements from this iterator.
     *
     * @param n A number
     * @return The empty iterator, if {@code n <= 0} or this is empty, otherwise a new iterator without the first n elements.
     */
    @Override
    default Iterator<T> take(int n) {
        if (n <= 0 || !hasNext()) {
            return empty();
        } else {
            final Iterator<T> that = this;
            return new Iterator<T>() {

                long count = n;

                @Override
                public boolean hasNext() {
                    return count > 0 && that.hasNext();
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    count--;
                    return that.next();
                }
            };
        }
    }

    @Override
    default Iterator<T> takeRight(int n) {
        if (n <= 0) {
            return empty();
        } else {
            final Iterator<T> that = this;
            return new Iterator<T>() {
                private io.vavr.collection.Queue<T> queue = io.vavr.collection.Queue.empty();

                @Override
                public boolean hasNext() {
                    while (that.hasNext()) {
                        queue = queue.enqueue(that.next());
                        if (queue.length() > n) {
                            queue = queue.dequeue()._2;
                        }
                    }
                    return queue.length() > 0;
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    final Tuple2<T, io.vavr.collection.Queue<T>> t = queue.dequeue();
                    queue = t._2;
                    return t._1;
                }
            };
        }
    }

    @Override
    default Iterator<T> takeUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return takeWhile(predicate.negate());
    }

    @Override
    default Iterator<T> takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (!hasNext()) {
            return empty();
        } else {
            final Iterator<T> that = this;
            return new Iterator<T>() {

                private T next;
                private boolean cached = false;
                private boolean finished = false;

                @Override
                public boolean hasNext() {
                    if (cached) {
                        return true;
                    } else if (finished) {
                        return false;
                    } else if (that.hasNext()) {
                        next = that.next();
                        if (predicate.test(next)) {
                            cached = true;
                            return true;
                        }
                    }
                    finished = true;
                    return false;
                }

                @Override
                public T next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    cached = false;
                    return next;
                }
            };
        }
    }

    /**
     * Converts this to a {@link Seq}.
     *
     * @return A new {@link Seq}.
     */
    default Seq<T> toSeq() {
        return toList();
    }
}

final class ArrayIterator<T> implements Iterator<T> {

    private final T[] elements;
    private int index = 0;

    ArrayIterator(T[] elements) {
        this.elements = elements;
    }

    @Override
    public boolean hasNext() {
        return index < elements.length;
    }

    @Override
    public T next() {
        try {
            return elements[index++];
        } catch(IndexOutOfBoundsException x) {
            index--;
            throw new NoSuchElementException();
        }
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        while (index < elements.length) {
            action.accept(elements[index++]);
        }
    }

    @Override
    public String toString() {
        return "ArrayIterator";
    }
}

final class CachedIterator<T> implements Iterator<T> {

    private final Iterator<T> that;

    private T next;
    private boolean cached = false;

    CachedIterator(Iterator<T> that) {
        this.that = that;
    }

    @Override
    public boolean hasNext() {
        return cached || that.hasNext();
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        if (cached) {
            T result = next;
            next = null;
            cached = false;
            return result;
        } else {
            return that.next();
        }
    }

    T touch() {
        next = next();
        cached = true;
        return next;
    }

    @Override
    public String toString() {
        return "CachedIterator";
    }
}

interface IteratorModule {
    /**
     * Creates two new iterators that both iterates over the same elements as
     * this iterator and in the same order. The duplicate iterators are
     * considered equal if they are positioned at the same element.
     * <p>
     * Given that most methods on iterators will make the original iterator
     * unfit for further use, this methods provides a reliable way of calling
     * multiple such methods on an iterator.
     *
     * @return a pair of iterators
     */
    static <T> Tuple2<Iterator<T>, Iterator<T>> duplicate(Iterator<T> iterator) {
        final java.util.Queue<T> gap = new java.util.LinkedList<>();
        final AtomicReference<Iterator<T>> ahead = new AtomicReference<>();
        class Partner implements Iterator<T> {

            @Override
            public boolean hasNext() {
                return (this != ahead.get() && !gap.isEmpty()) || iterator.hasNext();
            }

            @Override
            public T next() {
                if (gap.isEmpty()) {
                    ahead.set(this);
                }
                if (this == ahead.get()) {
                    final T element = iterator.next();
                    gap.add(element);
                    return element;
                } else {
                    return gap.poll();
                }
            }
        }
        return Tuple.of(new Partner(), new Partner());
    }
}

final class ConcatIterator<T> implements Iterator<T> {

    private static final class Iterators<T> {

        private final java.util.Iterator<T> head;
        private Iterators<T> tail;

        @SuppressWarnings("unchecked")
        Iterators(java.util.Iterator<? extends T> head) {
            this.head = (java.util.Iterator<T>) head;
        }
    }

    private Iterators<T> curr;
    private Iterators<T> last;
    private boolean nextCalculated = false;

    ConcatIterator(java.util.Iterator<? extends java.util.Iterator<? extends T>> iterators) {
        this.curr = this.last = iterators.hasNext() ? new Iterators<>(iterators.next()) : null;
        while (iterators.hasNext()) {
            this.last = this.last.tail = new Iterators<>(iterators.next());
        }
    }

    @Override
    public boolean hasNext() {
        if (nextCalculated) {
            return curr != null;
        } else {
            nextCalculated = true;
            while (true) {
                if (curr.head.hasNext()) {
                    return true;
                } else {
                    curr = curr.tail;
                    if (curr == null) {
                        last = null; // release reference
                        return false;
                    }
                }
            }
        }
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        nextCalculated = false;
        return curr.head.next();
    }

    @Override
    public Iterator<T> concat(java.util.Iterator<? extends T> that) {
        if (curr == null) {
            nextCalculated = false;
            curr = last = new Iterators<>(that);
        } else {
            last = last.tail = new Iterators<>(that);
        }
        return this;
    }

    @Override
    public String toString() {
        return "ConcatIterator";
    }
}

final class DistinctIterator<T, U> implements Iterator<T> {

    private final Iterator<? extends T> that;
    private io.vavr.collection.Set<U> known;
    private final Function<? super T, ? extends U> keyExtractor;
    private boolean nextDefined = false;
    private T next;

    DistinctIterator(Iterator<? extends T> that, Set<U> set, Function<? super T, ? extends U> keyExtractor) {
        this.that = that;
        this.known = set;
        this.keyExtractor = keyExtractor;
    }

    @Override
    public boolean hasNext() {
        return nextDefined || searchNext();
    }

    private boolean searchNext() {
        while (that.hasNext()) {
            final T elem = that.next();
            final U key = keyExtractor.apply(elem);
            if (!known.contains(key)) {
                known = known.add(key);
                nextDefined = true;
                next = elem;
                return true;
            }
        }
        return false;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final T result = next;
        nextDefined = false;
        next = null;
        return result;
    }

    @Override
    public String toString() {
        return "DistinctIterator";
    }
}

final class EmptyIterator implements Iterator<Object> {

    static final EmptyIterator INSTANCE = new EmptyIterator();

    private EmptyIterator() {}

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public Object next() {
        throw new NoSuchElementException();
    }

    @Override
    public void forEachRemaining(Consumer<? super Object> action) {
        Objects.requireNonNull(action);
    }

    @Override
    public String toString() {
        return "EmptyIterator";
    }
}

final class GroupedIterator<T> implements Iterator<Seq<T>> {

    private final Iterator<T> that;
    private final int size;
    private final int step;
    private final int gap;
    private final int preserve;

    private Object[] buffer;

    GroupedIterator(Iterator<T> that, int size, int step) {
        if (size < 1 || step < 1) {
            throw new IllegalArgumentException("size (" + size + ") and step (" + step + ") must both be positive");
        }
        this.that = that;
        this.size = size;
        this.step = step;
        this.gap = Math.max(step - size, 0);
        this.preserve = Math.max(size - step, 0);
        this.buffer = take(that, new Object[size], 0, size);
    }

    @Override
    public boolean hasNext() {
        return buffer.length > 0;
    }

    @Override
    public Seq<T> next() {
        if (buffer.length == 0) {
            throw new NoSuchElementException();
        }
        final Object[] result = buffer;
        if (that.hasNext()) {
            buffer = new Object[size];
            if (preserve > 0) {
                System.arraycopy(result, step, buffer, 0, preserve);
            }
            if (gap > 0) {
                drop(that, gap);
                buffer = take(that, buffer, preserve, size);
            } else {
                buffer = take(that, buffer, preserve, step);
            }
        } else {
            buffer = new Object[0];
        }
        return Array.wrap(result);
    }

    @Override
    public String toString() {
        return "GroupedIterator";
    }

    private static void drop(Iterator<?> source, int count) {
        for (int i = 0; i < count && source.hasNext(); i++) {
            source.next();
        }
    }

    private static Object[] take(Iterator<?> source, Object[] target, int offset, int count) {
        int i = offset;
        while (i < count + offset && source.hasNext()) {
            target[i] = source.next();
            i++;
        }
        if (i < target.length) {
            final Object[] result = new Object[i];
            System.arraycopy(target, 0, result, 0, i);
            return result;
        } else {
            return target;
        }
    }
}

final class SingletonIterator<T> implements Iterator<T> {

    private final T element;
    private boolean hasNext = true;

    SingletonIterator(T element) {
        this.element = element;
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        hasNext = false;
        return element;
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        Objects.requireNonNull(action);
        if (hasNext) {
            action.accept(element);
            hasNext = false;
        }
    }

    @Override
    public String toString() {
        return "SingletonIterator";
    }
}

final class RangeIntForwardIterator implements Iterator<Integer> {

    private final int start;

    private final int end;

    private final int step;

    private final boolean inclusive;

    private int next;

    private boolean overflow;

    RangeIntForwardIterator(int start, int end, int step, boolean inclusive) {
        this.start = start;
        this.next = start;
        this.end = end;
        this.step = step;
        this.inclusive = inclusive;
    }

    @Override
    public boolean hasNext() {
        if (start > end) {
            return false;
        }
        if (inclusive) {
            return !overflow && next <= end;
        } else {
            return !overflow && next < end;
        }
    }

    @Override
    public Integer next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        int curr = next;
        int r = curr + step;
        overflow = ((curr ^ r) & (step ^ r)) < 0;
        next = r;
        return curr;
    }
}

final class RangeIntBackwardIterator implements Iterator<Integer> {

    private final int start;

    private final int end;

    private final int step;

    private final boolean inclusive;

    private int next;

    private boolean underflow;

    RangeIntBackwardIterator(int start, int end, int step, boolean inclusive) {
        this.start = start;
        this.next = start;
        this.end = end;
        this.step = step;
        this.inclusive = inclusive;
    }

    @Override
    public boolean hasNext() {
        if (start < end) {
            return false;
        }
        if (inclusive) {
            return !underflow && next >= end;
        } else {
            return !underflow && next > end;
        }
    }

    @Override
    public Integer next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        int curr = next;
        int r = curr + step;
        underflow = ((curr ^ r) & (step ^ r)) < 0;
        next = r;
        return curr;
    }
}

final class RangeLongForwardIterator implements Iterator<Long> {

    private final long start;

    private final long end;

    private final long step;

    private final boolean inclusive;

    private long next;

    private boolean overflow;

    RangeLongForwardIterator(long start, long end, long step, boolean inclusive) {
        this.start = start;
        this.next = start;
        this.end = end;
        this.step = step;
        this.inclusive = inclusive;
    }

    @Override
    public boolean hasNext() {
        if (start > end) {
            return false;
        }
        if (inclusive) {
            return !overflow && next <= end;
        } else {
            return !overflow && next < end;
        }
    }

    @Override
    public Long next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        long curr = next;
        long r = curr + step;
        overflow = ((curr ^ r) & (step ^ r)) < 0;
        next = r;
        return curr;
    }
}

final class RangeLongBackwardIterator implements Iterator<Long> {

    private final long start;

    private final long end;

    private final long step;

    private final boolean inclusive;

    private long next;

    private boolean underflow;

    RangeLongBackwardIterator(long start, long end, long step, boolean inclusive) {
        this.start = start;
        this.next = start;
        this.end = end;
        this.step = step;
        this.inclusive = inclusive;
    }

    @Override
    public boolean hasNext() {
        if (start < end) {
            return false;
        }
        if (inclusive) {
            return !underflow && next >= end;
        } else {
            return !underflow && next > end;
        }
    }

    @Override
    public Long next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        long curr = next;
        long r = curr + step;
        underflow = ((curr ^ r) & (step ^ r)) < 0;
        next = r;
        return curr;
    }
}

final class BigDecimalHelper {

    private static final Lazy<BigDecimal> INFINITY_DISTANCE = Lazy.of(() -> {
        final BigDecimal two = BigDecimal.valueOf(2);
        final BigDecimal supremum = BigDecimal.valueOf(Math.nextDown(Double.POSITIVE_INFINITY));
        BigDecimal lowerBound = supremum;
        BigDecimal upperBound = two.pow(Double.MAX_EXPONENT + 1);
        while (true) {
            final BigDecimal magicValue = lowerBound.add(upperBound).divide(two, HALF_UP);
            if (Double.isInfinite(magicValue.doubleValue())) {
                if (areEqual(magicValue, upperBound)) {
                    return magicValue.subtract(supremum);
                }
                upperBound = magicValue;
            } else {
                lowerBound = magicValue;
            }
        }
    });

    /* scale-independent equality */
    static boolean areEqual(BigDecimal from, BigDecimal toExclusive) {
        return from.compareTo(toExclusive) == 0;
    }

    /* parse infinite values also */
    static BigDecimal asDecimal(double number) {
        if (number == NEGATIVE_INFINITY) {
            final BigDecimal result = BigDecimal.valueOf(Math.nextUp(NEGATIVE_INFINITY));
            return result.subtract(INFINITY_DISTANCE.get());
        } else if (number == POSITIVE_INFINITY) {
            final BigDecimal result = BigDecimal.valueOf(Math.nextDown(POSITIVE_INFINITY));
            return result.add(INFINITY_DISTANCE.get());
        } else {
            return BigDecimal.valueOf(number);
        }
    }
}
