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
package io.vavr.control;

import io.vavr.PartialFunction;
import io.vavr.collection.Iterator;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;

import java.io.Serializable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Replacement for {@link java.util.Optional}.
 * <p>
 * Option is a <a href="http://stackoverflow.com/questions/13454347/monads-with-java-8">monadic</a> container type which
 * represents an optional value. Instances of Option are either an instance of {@link Some} or the
 * singleton {@link None}.
 * <p>
 * Most of the API is taken from {@link java.util.Optional}. A similar type can be found in <a
 * href="http://hackage.haskell.org/package/base-4.6.0.1/docs/Data-Maybe.html">Haskell</a> and <a
 * href="http://www.scala-lang.org/api/current/#scala.Option">Scala</a>.
 *
 * @param <T> The type of the optional value.
 */
public abstract class Option<T> implements Iterable<T>, io.vavr.Value<T>, Serializable {

    private static final long serialVersionUID = 1L;

    // sealed
    private Option() {}

    /**
     * Creates a new {@code Option} of a given value.
     *
     * <pre>{@code
     * // = Some(3), an Option which contains the value 3
     * Option<Integer> option = Option.of(3);
     *
     * // = None, the empty Option
     * Option<Integer> none = Option.of(null);
     * }</pre>
     *
     * @param value A value
     * @param <T>   type of the value
     * @return {@code Some(value)} if value is not {@code null}, {@code None} otherwise
     */
    public static <T> Option<T> of(T value) {
        return (value == null) ? none() : some(value);
    }

    /**
     * Reduces many {@code Option}s into a single {@code Option} by transforming an
     * {@code Iterable<Option<? extends T>>} into a {@code Option<Seq<T>>}. If any of
     * the Options are {@link Option.None}, then this returns {@link Option.None}.
     *
     * <pre>{@code
     * Seq<Option<Integer>> seq = Vector.of(Option.of(1), Option.of(2), Option.of(3));
     *
     * // = Some(Seq(1, 2, 3))
     * Option<Seq<Integer>> option = Option.sequence(seq);
     *
     * Seq<Option<Integer>> seq = Vector.of(Option.of(1), Option.none());
     *
     * // = None since some elements in the Iterable are None
     * Option<Seq<Integer>> option = Option.sequence(seq);
     * }</pre>
     * @param values An {@code Iterable} of {@code Option}s
     * @param <T>    type of the Options
     * @return An {@code Option} of a {@link Seq} of results
     * @throws NullPointerException if {@code values} is null
     */
    public static <T> Option<Seq<T>> sequence(Iterable<? extends Option<? extends T>> values) {
        Objects.requireNonNull(values, "values is null");
        Vector<T> vector = Vector.empty();
        for (Option<? extends T> value : values) {
            if (value.isEmpty()) {
                return Option.none();
            }
            vector = vector.append(value.get());
        }
        return Option.some(vector);
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliterator(iterator(), isEmpty() ? 0 : 1,
                Spliterator.IMMUTABLE | Spliterator.ORDERED | Spliterator.SIZED | Spliterator.SUBSIZED);
    }

    /**
     * Maps the values of an iterable to a sequence of mapped values into a single {@code Option} by
     * transforming an {@code Iterable<? extends T>} into a {@code Option<Seq<U>>}.
     *
     * <pre>{@code
     * Function<Integer, Option<String>> mapper = i -> {
     *      if (i <= 0) {
     *          return Option.none();
     *      }
     *      return Option.of("a" = i.toString());
     * }
     *
     * // = Some(Seq("a1", "a2", "a3"))
     * Option<Seq<String>> option = traverse(Vector.of(1, 2, 3), mapper);
     *
     * // = None
     * Option<Seq<Integer>> none = traverse(Vector.of(-1, 0, 1), mapper);
     * }</pre>
     *
     * @param values   An {@code Iterable} of values.
     * @param mapper   A mapper of values to Options
     * @param <T>      The type of the given values.
     * @param <U>      The mapped value type.
     * @return A {@code Option} of a {@link Seq} of results.
     * @throws NullPointerException if values or f is null.
     */
    public static <T, U> Option<Seq<U>> traverse(Iterable<? extends T> values, Function<? super T, ? extends Option<? extends U>> mapper) {
        Objects.requireNonNull(values, "values is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return sequence(Iterator.ofAll(values).map(mapper));
    }

    /**
     * Creates a new {@code Some} of a given value.
     * <p>
     * The only difference to {@link Option#of(Object)} is, when called with argument {@code null}.
     *
     * <pre>{@code
     * // = Some(3)
     * Option.some(3);
     *
     * // = Some(null)
     * Option.some(null);
     * }</pre>
     *
     * @param value A value
     * @param <T>   type of the value
     * @return {@code Some(value)}
     */
    public static <T> Option<T> some(T value) {
        return new Some<>(value);
    }

    /**
     * Returns the single instance of {@code None}
     *
     * <pre>{@code
     * // = None
     * Option<String> none = Option.none();
     * }</pre>
     *
     * @param <T> component type
     * @return the single instance of {@code None}
     */
    public static <T> Option<T> none() {
        @SuppressWarnings("unchecked")
        final None<T> none = (None<T>) None.INSTANCE;
        return none;
    }

    /**
     * Narrows a widened {@code Option<? extends T>} to {@code Option<T>}
     * by performing a type-safe cast. This is eligible because immutable/read-only
     * collections are covariant.
     *
     * <pre>{@code
     * Option<Integer> option = Option.of(3);
     * // Narrow to an Option of Number
     * Option<Number> narrowed = Option.narrow(option);
     * }</pre>
     *
     * @param option A {@code Option}.
     * @param <T>    Component type of the {@code Option}.
     * @return the given {@code option} instance as narrowed type {@code Option<T>}.
     */
    @SuppressWarnings("unchecked")
    public static <T> Option<T> narrow(Option<? extends T> option) {
        return (Option<T>) option;
    }

    /**
     * Unless the given {@code condition} is true, returns {@code Option.some(supplier.get())},
     * otherwise returns {@code Option.none()}.
     *
     * <pre>{@code
     * Supplier<String> supplier = () -> "supplied";
     *
     * // = Some("supplied")
     * Option<String> supplied = Option.unless(false, supplier);
     *
     * // = None
     * Option<String> none = Option.unless(true, supplier);
     * }</pre>
     *
     * @param <T>       value type
     * @param condition a boolean value
     * @param supplier  a value supplier
     * @return {@code Some(supplier.get())} if {@code condition} is false, otherwise {@code None}
     * @throws NullPointerException if the given {@code supplier} is null
     */
    public static <T> Option<T> unless(boolean condition, Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return when(!condition, supplier);
    }

    /**
     * Unless the given {@code condition} is true, returns {@code Option.some(value)},
     * otherwise returns {@code Option.none()}.
     *
     * <pre>{@code
     * // = Some(5)
     * Option<Integer> option = Option.when(true, 5);
     *
     * // = None
     * Option<Integer> none = Option.when(false, 5);
     * }</pre>
     *
     * @param <T>       type of the value
     * @param condition a boolean value
     * @param value     a possibly {@code null} value
     * @return {@code Some(value)} if {@code condition} is false, otherwise {@code None}
     */
    public static <T> Option<T> unless(boolean condition, T value) {
        return when(!condition, value);
    }

    /**
     * When the given {@code condition} is true, returns {@code Option.some(supplier.get())},
     * otherwise returns {@code Option.none()}.
     *
     * <pre>{@code
     * Supplier<String> supplier = () -> "supplied";
     *
     * // = Some("supplied")
     * Option<String> supplied = Option.when(true, supplier);
     *
     * // = None
     * Option<String> none = Option.when(false, supplier);
     * }</pre>
     *
     * @param <T>       value type
     * @param condition a boolean value
     * @param supplier  a value supplier
     * @return {@code Some(supplier.get())} if {@code condition} is true, otherwise {@code None}
     * @throws NullPointerException if the given {@code supplier} is null
     */
    public static <T> Option<T> when(boolean condition, Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return condition ? some(supplier.get()) : none();
    }

    /**
     * When the given {@code condition} is true, returns {@code Option.some(value)},
     * otherwise returns {@code Option.none()}.
     *
     * <pre>{@code
     * // = Some(5)
     * Option<Integer> option = Option.when(true, 5);
     *
     * // = None
     * Option<Integer> none = Option.when(false, 5);
     * }</pre>
     *
     * @param <T>       type of the value
     * @param condition a boolean value
     * @param value     a possibly {@code null} value
     * @return {@code Some(value)} if {@code condition} is true, otherwise {@code None}
     */
    public static <T> Option<T> when(boolean condition, T value) {
        return condition ? some(value) : none();
    }

    /**
     * Wraps a Java Optional to a new Option
     *
     * <pre>{@code
     * Optional<String> optional = Optional.ofNullable("value");
     *
     * // Make a Some("value") from an Optional
     * Option<String> option = Option.ofOptional(optional);
     *
     * Optional<String> empty = Optional.empty();
     *
     * // Make a None from an empty Optional
     * Option<String> none = Option.ofOptional(empty);
     * }</pre>
     *
     * @param optional a given optional to wrap in {@code Option}
     * @param <T>      type of the value
     * @return {@code Some(optional.get())} if value is Java {@code Optional} is present, {@code None} otherwise
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Option<T> ofOptional(Optional<? extends T> optional) {
        Objects.requireNonNull(optional, "optional is null");
        return optional.<Option<T>>map(Option::of).orElseGet(Option::none);
    }

    /**
     * Collects value that is in the domain of the given {@code partialFunction} by mapping the value to type {@code R}.
     *
     * <pre>{@code
     * partialFunction.isDefinedAt(value)
     * }</pre>
     *
     * If the element makes it through that filter, the mapped instance is wrapped in {@code Option}
     *
     * <pre>{@code
     * R newValue = partialFunction.apply(value)
     * }</pre>
     *
     *
     * @param partialFunction A function that is not necessarily defined on value of this option.
     * @param <R> The new value type
     * @return A new {@code Option} instance containing value of type {@code R}
     * @throws NullPointerException if {@code partialFunction} is null
     */
    public final <R> Option<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        Objects.requireNonNull(partialFunction, "partialFunction is null");
        return flatMap(partialFunction.lift());
    }

    /**
     * Returns true, if this is {@code None}, otherwise false, if this is {@code Some}.
     *
     * <pre>{@code
     * // Prints "false"
     * System.out.println(Option.of(10).isEmpty());
     *
     * // Prints "true"
     * System.out.println(Option.none().isEmpty());
     * }</pre>
     *
     * @return true, if this {@code Option} is empty, false otherwise
     */
    public abstract boolean isEmpty();

    @Override
    public final Iterator<T> iterator() {
        return isEmpty() ? Iterator.empty() : Iterator.of(get());
    }

    /**
     * Runs a Java Runnable passed as parameter if this {@code Option} is empty.
     *
     * <pre>{@code
     * Runnable print = () -> System.out.println("Option is empty");
     *
     * // Prints nothing
     * Option.of("value").onEmpty(print);
     *
     * // Prints "Option is empty"
     * Option.none().onEmpty(print);
     * }</pre>
     *
     * @param action a given Runnable to be run
     * @return this {@code Option}
     */
    public final Option<T> onEmpty(Runnable action) {
        Objects.requireNonNull(action, "action is null");
        if (isEmpty()) {
            action.run();
        }
        return this;
    }

    /**
     * Returns true, if this is {@code Some}, otherwise false, if this is {@code None}.
     * <p>
     * Please note that it is possible to create {@code new Some(null)}, which is defined.
     *
     * <pre>{@code
     * // Prints "true"
     * System.out.println(Option.of(10).isDefined());
     *
     * // Prints "false"
     * System.out.println(Option.none().isDefined());
     *
     * // Prints "true
     * System.out.println(Option.of(null).isDefined());
     * }</pre>
     *
     * @return true, if this {@code Option} has a defined value, false otherwise
     */
    public final boolean isDefined() {
        return !isEmpty();
    }

    /**
     * Gets the value if this is a {@code Some} or throws if this is a {@code None}.
     *
     * <pre>{@code
     * // Prints "57"
     * System.out.println(Option.of(57).get());
     *
     * // Throws a NoSuchElementException
     * Option.none().get();
     * }</pre>
     *
     * @return the value
     * @throws NoSuchElementException if this is a {@code None}.
     */
    public abstract T get();

    /**
     * Returns the value if this is a {@code Some} or the {@code other} value if this is a {@code None}.
     * <p>
     * Please note, that the other value is eagerly evaluated.
     *
     * <pre>{@code
     * // Prints "Hello"
     * System.out.println(Option.of("Hello").getOrElse("World"));
     *
     * // Prints "World"
     * Option.none().getOrElse("World");
     * }</pre>
     *
     * @param other An alternative value
     * @return This value, if this Option is defined or the {@code other} value, if this Option is empty.
     */
    public final T getOrElse(T other) {
        return isEmpty() ? other : get();
    }

    /**
     * Returns the value if this is a {@code Some}, otherwise {@code supplier.get()} is returned.
     * <p>
     * Please note, that the alternate value is lazily evaluated.
     *
     * <pre>{@code
     * Supplier<Double> supplier = () -> 5.342;
     *
     * // = 1.2
     * Option.of(1.2).getOrElse(supplier);
     *
     * // = 5.342
     * Option.none().getOrElse(supplier);
     * }</pre>
     *
     * @param supplier An alternative value supplier
     * @return This value, if this Option is defined or the {@code other} value, if this Option is empty.
     */
    public final T getOrElse(Supplier<? extends T> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return isEmpty() ? supplier.get() : get();
    }

    /**
     * Returns this {@code Option} if it is nonempty, otherwise return the alternative.
     *
     * <pre>{@code
     * Option<String> other = Option.of("Other");
     *
     * // = Some("Hello World")
     * Option.of("Hello World").orElse(other);
     *
     * // = Some("Other")
     * Option.none().orElse(other);
     * }</pre>
     *
     * @param other An alternative {@code Option}
     * @return this {@code Option} if it is nonempty, otherwise return the alternative.
     */
    @SuppressWarnings("unchecked")
    public final Option<T> orElse(Option<? extends T> other) {
        Objects.requireNonNull(other, "other is null");
        return isEmpty() ? (Option<T>) other : this;
    }

    /**
     * Returns this {@code Option} if it is nonempty, otherwise return the result of evaluating supplier.
     *
     * <pre>{@code
     * Supplier<Option<Integer>> supplier = () -> Option.of(5);
     *
     * // = Some(2)
     * Option.of(2).orElse(supplier);
     *
     * // = Some(5)
     * Option.none().orElse(supplier);
     * }</pre>
     *
     * @param supplier An alternative {@code Option} supplier
     * @return this {@code Option} if it is nonempty, otherwise return the result of evaluating supplier.
     */
    @SuppressWarnings("unchecked")
    public final Option<T> orElse(Supplier<? extends Option<? extends T>> supplier) {
        Objects.requireNonNull(supplier, "supplier is null");
        return isEmpty() ? (Option<T>) supplier.get() : this;
    }

    /**
     * Returns the value if this is a {@code Some}, otherwise throws an exception.
     *
     * <pre>{@code
     * Supplier<RuntimeException> supplier = () -> new RuntimeException();
     *
     * // = 12
     * Option.of(12).getOrElseThrow(supplier);
     *
     * // throws RuntimeException
     * Option.none().getOrElseThrow(supplier);
     * }</pre>
     *
     * @param exceptionSupplier An exception supplier
     * @param <X>               A throwable
     * @return This value, if this Option is defined, otherwise throws X
     * @throws X a throwable
     */
    public final <X extends Throwable> T getOrElseThrow(Supplier<X> exceptionSupplier) throws X {
        Objects.requireNonNull(exceptionSupplier, "exceptionSupplier is null");
        if (isEmpty()) {
            throw exceptionSupplier.get();
        } else {
            return get();
        }
    }

    /**
     * Returns {@code Some(value)} if this is a {@code Some} and the value satisfies the given predicate.
     * Otherwise {@code None} is returned.
     *
     * <pre>{@code
     * Predicate<Integer> isLessThanTen = i -> i < 10;
     *
     * // = Some(8)
     * Option.some(8).filter(isLessThanTen);
     *
     * // = None
     * Option.some(12).filter(isLessThanTen);
     *
     * // = None
     * Option.<Integer>none().filter(isLessThanTen);
     * }</pre>
     *
     * @param predicate A predicate which is used to test an optional value
     * @return {@code Some(value)} or {@code None} as specified
     */
    public final Option<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return isEmpty() || predicate.test(get()) ? this : none();
    }

    /**
     * Returns {@code Some(value)} if this is a {@code Some} and the value not satisfies the given predicate.
     * Otherwise {@code None} is returned.
     *
     * <pre>{@code
     * Predicate<Integer> isEven = i -> (i & 1) == 0;
     *
     * // = Some(5)
     * Option.some(5).filterNot(isEven);
     *
     * // = None
     * Option.some(12).filterNot(isEven);
     *
     * // = None
     * Option.<Integer>none().filterNot(isEven);
     * }</pre>
     *
     * @param predicate A predicate which is used to test an optional value
     * @return {@code Some(value)} or {@code None} as specified
     */
    public final Option<T> filterNot(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return filter(predicate.negate());
    }

    /**
     * Maps the value to a new {@code Option} if this is a {@code Some}, otherwise returns {@code None}.
     *
     * <pre>{@code
     * Function<Integer, Option<Integer>> mapper = i -> i < 10 ? Option.of(i * 2) : Option.none();
     *
     * // = Some(14)
     * Option.of(7).flatMap(mapper);
     *
     * // = None
     * Option.of(11).flatMap(mapper);
     *
     * // = None
     * Option.<Integer>none().flatMap(mapper);
     * }</pre>
     *
     * @param mapper A mapper
     * @param <U>    Component type of the resulting Option
     * @return a new {@code Option}
     */
    @SuppressWarnings("unchecked")
    public final <U> Option<U> flatMap(Function<? super T, ? extends Option<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return isEmpty() ? none() : (Option<U>) mapper.apply(get());
    }

    /**
     * Maps the value and wraps it in a new {@code Some} if this is a {@code Some}, otherwise returns a {@code None}.
     *
     * <pre>{@code
     * Function<String, String> mapper = s -> s + " World!";
     *
     * // = Some("Hello World!")
     * Option.of("Hello").map(mapper);
     *
     * // = None
     * Option.<String>none().map(mapper);
     * }</pre>
     *
     * @param mapper A value mapper
     * @param <U>    The new value type
     * @return a new {@code Some} containing the mapped value if this Option is defined, otherwise {@code None}, if this is empty.
     */
    public final <U> Option<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return isEmpty() ? none() : some(mapper.apply(get()));
    }

    /**
     * Folds either the {@code None} or the {@code Some} side of the Option value.
     *
     * <pre>{@code
     * Supplier<Double> ifNone = () -> 3.14;
     * Function<String, Double> mapper = s -> Double.valueOf(s) + 0.98;
     *
     * // = Some(4.98)
     * Option.of("4").fold(ifNone, mapper);
     *
     * // = Some(3.14)
     * Option.<String>none().fold(ifNone, mapper);
     * }</pre>
     *
     * @param ifNone  maps the left value if this is a None
     * @param f maps the value if this is a Some
     * @param <U>         type of the folded value
     * @return A value of type U
     */
    public final <U> U fold(Supplier<? extends U> ifNone, Function<? super T, ? extends U> f) {
        return this.<U>map(f).getOrElse(ifNone);
    }

    /**
     * Performs the given {@code noneAction} if this option is not defined.
     * Performs the given {@code someAction} to this value, if this option is defined.
     *
     * @param noneAction The action that will be performed on the left element
     * @param someAction The action that will be performed on the right element
     * @return this instance
     */
    public final Option<T> peek(Runnable noneAction, Consumer<? super T> someAction) {
        Objects.requireNonNull(noneAction, "noneAction is null");
        Objects.requireNonNull(someAction, "someAction is null");

        if (isEmpty()) {
            noneAction.run();
        } else {
            someAction.accept(get());
        }

        return this;
    }

    /**
     * Transforms this {@code Option}.
     *
     * <pre>{@code
     * Function<Option<Integer>, String> f = o -> o.getOrElse(3).toString().concat("-transformed"));
     *
     * // Prints "1-transformed"
     * System.out.println(Option.of(1).transform(f));
     *
     * // Prints "3-transformed"
     * System.out.println(Option.<Integer>none().transform(f));
     * }</pre>
     *
     * @param f   A transformation
     * @param <U> Type of transformation result
     * @return An instance of type {@code U}
     * @throws NullPointerException if {@code f} is null
     */
    public final <U> U transform(Function<? super Option<T>, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    /**
     * Converts this {@code Option} to a {@link java.util.Optional}.
     *
     * <pre>{@code
     * // = Optional[1]
     * Option.some(1).toJavaOptional();
     *
     * // = Optional.empty
     * Option.some(null).toJavaOptional();
     *
     * // = Optional.empty
     * Option.none().toJavaOptional();
     * }</pre>
     *
     * @return A new {@link java.util.Optional}.
     */
    public Optional<T> toJavaOptional() {
        return isEmpty() ? Optional.empty() : Optional.ofNullable(get());
    }

    /**
     * Converts this to an {@link Either}.
     *
     * @param left A left value for the {@link Either}
     * @param <L>  Either left component type
     * @return A new {@link Either}.
     */
    public <L> Either<L, T> toEither(L left) {
        return isEmpty() ? Either.left(left) : Either.right(get());
    }

    /**
     * Converts this to an {@link Either}.
     *
     * @param leftSupplier A {@link Supplier} for the left value for the {@link Either}
     * @param <L>          Validation error component type
     * @return A new {@link Either}.
     */
    public <L> Either<L, T> toEither(Supplier<? extends L> leftSupplier) {
        Objects.requireNonNull(leftSupplier, "leftSupplier is null");
        return isEmpty() ? Either.left(leftSupplier.get()) : Either.right(get());
    }

    /**
     * Converts this to a {@link Try}.
     * <p>
     * If this value is undefined, i.e. empty, then a new {@code Failure(NoSuchElementException)} is returned,
     * otherwise a new {@code Success(value)} is returned.
     *
     * @return A new {@link Try}.
     */
    public Try<T> toTry() {
        return isEmpty() ? Try.failure(new NoSuchElementException()) : Try.success((get()));
    }

    /**
     * Converts this to a {@link Try}.
     * <p>
     * If this value is undefined, i.e. empty, then a new {@code Failure(ifEmpty.get())} is returned,
     * otherwise a new {@code Success(value)} is returned.
     *
     * @param ifEmpty an exception supplier
     * @return A new {@link Try}.
     */
    public Try<T> toTry(Supplier<? extends Throwable> ifEmpty) {
        Objects.requireNonNull(ifEmpty, "ifEmpty is null");
        return isEmpty() ? Try.failure(ifEmpty.get()) : Try.success(get());
    }

    /**
     * Some represents a defined {@link Option}. It contains a value which may be null. However, to
     * create an Option containing null, {@code new Some(null)} has to be called. In all other cases
     * {@link Option#of(Object)} is sufficient.
     *
     * @param <T> The type of the optional value.
     * @deprecated will be removed from the public API
     */
    @Deprecated
    public static final class Some<T> extends Option<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        private final T value;

        /**
         * Creates a new Some containing the given value.
         *
         * @param value A value, may be null
         */
        private Some(T value) {
            this.value = value;
        }

        @Override
        public T get() {
            return value;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj == this) || (obj instanceof Some && Objects.equals(value, ((Some<?>) obj).value));
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(value);
        }

        @Override
        public String toString() {
            return "Some(" + value + ")";
        }
    }

    /**
     * None is a singleton representation of the undefined {@link Option}.
     *
     * @param <T> The type of the optional value.
     * @deprecated will be removed from the public API
     */
    @Deprecated
    public static final class None<T> extends Option<T> implements Serializable {

        private static final long serialVersionUID = 1L;

        /**
         * The singleton instance of None.
         */
        private static final None<?> INSTANCE = new None<>();

        /**
         * Hidden constructor.
         */
        private None() {
        }

        @Override
        public T get() {
            throw new NoSuchElementException("No value present");
        }

        @Override
        public boolean isEmpty() {
            return true;
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
            return "None";
        }

        // -- Serializable implementation

        /**
         * Instance control for object serialization.
         *
         * @return The singleton instance of None.
         * @see Serializable
         */
        private Object readResolve() {
            return INSTANCE;
        }
    }
}
