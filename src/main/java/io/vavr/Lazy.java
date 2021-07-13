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
package io.vavr;

import io.vavr.collection.Iterator;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import io.vavr.control.Option;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a lazy evaluated value. Compared to a Supplier, Lazy is memoizing, i.e. it evaluates only once and
 * therefore is referential transparent.
 *
 * <pre>
 * <code>
 * final Lazy&lt;Double&gt; l = Lazy.of(Math::random);
 * l.isEvaluated(); // = false
 * l.get();         // = 0.123 (random generated)
 * l.isEvaluated(); // = true
 * l.get();         // = 0.123 (memoized)
 * </code>
 * </pre>
 *
 * Example of creating a <em>real</em> lazy value (works only with interfaces):
 *
 * <pre><code>final CharSequence chars = Lazy.val(() -&gt; "Yay!", CharSequence.class);</code></pre>
 */
// DEV-NOTE: No flatMap and orElse because this more like a Functor than a Monad.
//           It represents a value rather than capturing a specific state.
public final class Lazy<T> implements Value<T>, Supplier<T>, Serializable {

    private static final long serialVersionUID = 1L;

    // read http://javarevisited.blogspot.de/2014/05/double-checked-locking-on-singleton-in-java.html
    private transient volatile Supplier<? extends T> supplier;
    private T value; // will behave as a volatile in reality, because a supplier volatile read will update all fields (see https://www.cs.umd.edu/~pugh/java/memoryModel/jsr-133-faq.html#volatile)

    // should not be called directly
    private Lazy(Supplier<? extends T> supplier) {
        this.supplier = supplier;
    }

    /**
     * Narrows a widened {@code Lazy<? extends T>} to {@code Lazy<T>}
     * by performing a type-safe cast. This is eligible because immutable/read-only
     * collections are covariant.
     *
     * @param lazy A {@code Lazy}.
     * @param <T>  Component type of the {@code Lazy}.
     * @return the given {@code lazy} instance as narrowed type {@code Lazy<T>}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Lazy<T> narrow(Lazy<? extends T> lazy) {
        return (Lazy<T>) lazy;
    }

    /**
     * Creates a {@code Lazy} that requests its value from a given {@code Supplier}. The supplier is asked only once,
     * the value is memoized.
     *
     * @param <T>      type of the lazy value
     * @param supplier A supplier
     * @return A new instance of Lazy
     */
    @SuppressWarnings("unchecked")
    public static <T> Lazy<T> of(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        if (supplier instanceof Lazy) {
            return (Lazy<T>) supplier;
        } else {
            return new Lazy<>(supplier);
        }
    }

    /**
     * Reduces many {@code Lazy} values into a single {@code Lazy} by transforming an
     * {@code Iterable<Lazy<? extends T>>} into a {@code Lazy<Seq<T>>}.
     *
     * @param <T>    Type of the lazy values.
     * @param values An iterable of lazy values.
     * @return A lazy sequence of values.
     * @throws NullPointerException if values is null
     */
    @SuppressWarnings("Convert2MethodRef") // TODO should be fixed in JDK 9 and Idea
    public static <T> Lazy<Seq<T>> sequence(Iterable<? extends Lazy<? extends T>> values) {
        Objects.requireNonNull(values, "values is null");
        return Lazy.of(() -> Vector.ofAll(values).map(lazy -> lazy.get()));
    }

    /**
     * Creates a real _lazy value_ of type {@code T}, backed by a {@linkplain java.lang.reflect.Proxy} which delegates
     * to a {@code Lazy} instance.
     *
     * @param supplier A supplier
     * @param type     An interface
     * @param <T>      type of the lazy value
     * @return A new instance of T
     */
    @SuppressWarnings("unchecked")
    public static <T> T val(Supplier<? extends T> supplier, Class<T> type) {
        Objects.requireNonNull(supplier, "supplier is null");
        Objects.requireNonNull(type, "type is null");
        if (!type.isInterface()) {
            throw new IllegalArgumentException("type has to be an interface");
        }
        final Lazy<T> lazy = Lazy.of(supplier);
        final InvocationHandler handler = (proxy, method, args) -> method.invoke(lazy.get(), args);
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, handler);
    }


    /**
     * Lazily filters the underlying value of this {@code Lazy} instance by the given {@code predicate}.
     *
     * <pre>{@code
     * // = Lazy(Some(1))
     * Lazy.of(() -> 1).filter(i -> i == 1)
     *
     * // = Lazy(None)
     * Lazy.of(() -> 1).filter(i -> i == 2)
     * }</pre>
     *
     * @param predicate a {@link Predicate}
     * @return a new {@code Lazy} instance
     */
    public Lazy<Option<T>> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Lazy.of(() -> {
            final T v = get();
            return predicate.test(v) ? Option.some(v) : Option.none();
        });
    }

    /**
     * Lazily filters the underlying value of this {@code Lazy} instance by the negation of the given {@code predicate}.
     *
     * <pre>{@code
     * // = Lazy(None)
     * Lazy.of(() -> 1).filterNot(i -> i == 1)
     *
     * // = Lazy(Some(1))
     * Lazy.of(() -> 1).filterNot(i -> i == 2)
     * }</pre>
     *
     * @param predicate a {@link Predicate}
     * @return the result of {@link #filter(Predicate)} by calling {@code filter(predicate.negate())}
     */
    public Lazy<Option<T>> filterNot(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return filter(predicate.negate());
    }

    /**
     * Evaluates this lazy value and caches it, when called the first time.
     * On subsequent calls, returns the cached value.
     *
     * @return the lazy evaluated value
     */
    @Override
    public T get() {
        return (supplier == null) ? value : computeValue();
    }
    
    private synchronized T computeValue() {
        final Supplier<? extends T> s = supplier;
        if (s != null) {
            value = s.get();
            supplier = null;
        }
        return value;
    }

    /**
     * Checks, if this lazy value is evaluated.
     * <p>
     * Note: A value is internally evaluated (once) by calling {@link #get()}.
     *
     * @return true, if the value is evaluated, false otherwise.
     * @throws UnsupportedOperationException if this value is undefined
     */
    public boolean isEvaluated() {
        return supplier == null;
    }

    @Override
    public Iterator<T> iterator() {
        return Iterator.of(get());
    }

    public <U> Lazy<U> map(Function<? super T, ? extends U> mapper) {
        return Lazy.of(() -> mapper.apply(get()));
    }

    /**
     * Lazily performs the given {@code action} if this {@code Lazy} is defined.
     *
     * <pre>{@code
     * import static io.vavr.API.println;
     *
     * // does not print anything
     * Lazy<Integer> testee = Lazy.of(() -> 1).peek(i -> println(i));
     *
     * // does print 1
     * int i = testee.get();
     * }</pre>
     *
     * @param action The action that will be performed on the element
     * @return a new {@code Lazy} instance
     */
    public Lazy<T> peek(Consumer<? super T> action) {
        return Lazy.of(()-> {
            final T val = get();
            action.accept(val);
            return val;
        });
    }

    @Override
    public final Spliterator<T> spliterator() {
        return Spliterators.spliterator(iterator(), 1,
                Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED);
    }

    /**
     * Transforms this {@code Lazy}.
     *
     * @param f   A transformation
     * @param <U> Type of transformation result
     * @return An instance of type {@code U}
     * @throws NullPointerException if {@code f} is null
     */
    public <U> U transform(Function<? super Lazy<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    public boolean equals(Object o) {
        return (o == this) || (o instanceof Lazy && Objects.equals(((Lazy<?>) o).get(), get()));
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(get());
    }

    @Override
    public String toString() {
        return "Lazy(" + (!isEvaluated() ? "?" : value) + ")";
    }

    /**
     * Ensures that the value is evaluated before serialization.
     *
     * @param s An object serialization stream.
     * @throws java.io.IOException If an error occurs writing to the stream.
     */
    private void writeObject(ObjectOutputStream s) throws IOException {
        get(); // evaluates the lazy value if it isn't evaluated yet!
        s.defaultWriteObject();
    }
}
