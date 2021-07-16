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
import io.vavr.collection.CharSeqModule.Combinations;
import io.vavr.collection.JavaConverters.ListView;
import io.vavr.control.Option;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.*;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collector;

import static io.vavr.collection.JavaConverters.ChangePolicy.IMMUTABLE;
import static io.vavr.collection.JavaConverters.ChangePolicy.MUTABLE;

/**
 * The CharSeq (read: character sequence) collection essentially is a rich String wrapper having all operations
 * we know from the functional Vavr collections.
 * <p>
 * <strong>Note:</strong>Because CharSeq represents a sequence of primitive characters (i.e. a String),
 * it breaks the Liskov Substitution Principle in the way, that the CharSeq cannot contain {@code null} elements.
 * In future version of Java, CharSeq should extend IndexedSeq&lt;char&gt; instead.
 */
public final class CharSeq implements CharSequence, IndexedSeq<Character>, Serializable, Comparable<CharSeq> {

    private static final long serialVersionUID = 1L;

    private static final CharSeq EMPTY = new CharSeq("");

    private final String back;

    private CharSeq(String javaString) {
        this.back = javaString;
    }

    public static CharSeq empty() {
        return EMPTY;
    }

    /**
     * Returns a {@link java.util.stream.Collector} which may be used in conjunction with
     * {@link java.util.stream.Stream#collect(java.util.stream.Collector)} to obtain a {@link CharSeq}.
     *
     * @return a {@code Collector} which collects all the input elements into a
     * {@link io.vavr.collection.CharSeq}, in encounter order
     */
    public static Collector<Character, ArrayList<Character>, CharSeq> collector() {
        return Collections.toListAndThen(CharSeq::ofAll);
    }

    /**
     * Creates a String of {@code CharSequence}.
     *
     * @param sequence {@code CharSequence} instance.
     * @return A new {@link io.vavr.collection.CharSeq}
     */
    // DEV-NOTE: Needs to be 'of' instead of 'ofAll' because 'ofAll(CharSeq)' is ambiguous.
    public static CharSeq of(CharSequence sequence) {
        Objects.requireNonNull(sequence, "sequence is null");
        if (sequence instanceof CharSeq) {
            return (CharSeq) sequence;
        } else {
            return sequence.length() == 0 ? empty() : new CharSeq(sequence.toString());
        }
    }

    /**
     * Returns a singleton {@code CharSeq}, i.e. a {@code CharSeq} of one character.
     *
     * @param character A character.
     * @return A new {@code CharSeq} instance containing the given element
     */
    public static CharSeq of(char character) {
        return new CharSeq(new String(new char[] { character }));
    }

    /**
     * Creates a String of the given characters.
     *
     * @param characters Zero or more characters.
     * @return A string containing the given characters in the same order.
     * @throws NullPointerException if {@code elements} is null
     */
    public static CharSeq of(char... characters) {
        Objects.requireNonNull(characters, "characters is null");
        if (characters.length == 0) {
            return empty();
        } else {
            final char[] chrs = new char[characters.length];
            System.arraycopy(characters, 0, chrs, 0, characters.length);
            return new CharSeq(new String(chrs));
        }
    }

    /**
     * Creates a String of the given elements.
     * <p>
     * The resulting string has the same iteration order as the given iterable of elements
     * if the iteration order of the elements is stable.
     *
     * @param elements An Iterable of elements.
     * @return A string containing the given elements in the same order.
     * @throws NullPointerException if {@code elements} is null or {@code elements} contains null
     */
    @SuppressWarnings("unchecked")
    public static CharSeq ofAll(Iterable<? extends Character> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (Collections.isEmpty(elements)){
            return EMPTY;
        }
        if (elements instanceof CharSeq) {
            return (CharSeq) elements;
        }
        if (elements instanceof ListView
                && ((ListView<Character, ?>) elements).getDelegate() instanceof CharSeq) {
            return (CharSeq) ((ListView<Character, ?>) elements).getDelegate();
        }
        final StringBuilder sb = new StringBuilder();
        for (char character : elements) {
            sb.append(character);
        }
        return of(sb);
    }

    /**
     * Returns a CharSeq containing {@code n} values of a given Function {@code f}
     * over a range of integer values from 0 to {@code n - 1}.
     *
     * @param n The number of elements in the CharSeq
     * @param f The Function computing element values
     * @return A CharSeq consisting of elements {@code f(0),f(1), ..., f(n - 1)}
     * @throws NullPointerException if {@code f} is null
     */
    public static CharSeq tabulate(int n, Function<? super Integer, ? extends Character> f) {
        Objects.requireNonNull(f, "f is null");
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n; i++) {
            sb.append(f.apply(i).charValue());
        }
        return of(sb);
    }

    /**
     * Returns a CharSeq containing {@code n} values supplied by a given Supplier {@code s}.
     *
     * @param n The number of elements in the CharSeq
     * @param s The Supplier computing element values
     * @return A CharSeq of size {@code n}, where each element contains the result supplied by {@code s}.
     * @throws NullPointerException if {@code s} is null
     */
    public static CharSeq fill(int n, Supplier<? extends Character> s) {
        return tabulate(n, anything -> s.get());
    }

    /**
     * Creates a CharSeq starting from character {@code from}, extending to character {@code toExclusive - 1}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * CharSeq.range('a', 'c')  // = "ab"
     * CharSeq.range('c', 'a')  // = ""
     * </code>
     * </pre>
     *
     * @param from        the first character
     * @param toExclusive the successor of the last character
     * @return a range of characters as specified or the empty range if {@code from >= toExclusive}
     */
    public static CharSeq range(char from, char toExclusive) {
        return new CharSeq(io.vavr.collection.Iterator.range(from, toExclusive).mkString());
    }

    public static CharSeq rangeBy(char from, char toExclusive, int step) {
        return new CharSeq(io.vavr.collection.Iterator.rangeBy(from, toExclusive, step).mkString());
    }

    /**
     * Creates a CharSeq starting from character {@code from}, extending to character {@code toInclusive}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * CharSeq.rangeClosed('a', 'c')  // = "abc"
     * CharSeq.rangeClosed('c', 'a')  // = ""
     * </code>
     * </pre>
     *
     * @param from        the first character
     * @param toInclusive the last character
     * @return a range of characters as specified or the empty range if {@code from > toInclusive}
     */
    public static CharSeq rangeClosed(char from, char toInclusive) {
        return new CharSeq(io.vavr.collection.Iterator.rangeClosed(from, toInclusive).mkString());
    }

    /**
     * Creates a CharSeq starting from character {@code from}, extending to character {@code toInclusive},
     * with {@code step}.
     * <p>
     * Examples:
     * <pre>
     * <code>
     * CharSeq.rangeClosedBy('a', 'c', 1)  // = ('a', 'b', 'c')
     * CharSeq.rangeClosedBy('a', 'd', 2)  // = ('a', 'c')
     * CharSeq.rangeClosedBy('d', 'a', -2) // = ('d', 'b')
     * CharSeq.rangeClosedBy('d', 'a', 2)  // = ()
     * </code>
     * </pre>
     *
     * @param from        the first character
     * @param toInclusive the last character
     * @param step        the step
     * @return a range of characters as specified or the empty range if {@code step * (from - toInclusive) > 0}.
     * @throws IllegalArgumentException if {@code step} is zero
     */
    public static CharSeq rangeClosedBy(char from, char toInclusive, int step) {
        return new CharSeq(io.vavr.collection.Iterator.rangeClosedBy(from, toInclusive, step).mkString());
    }

    /**
     * Creates a CharSeq from a seed value and a function.
     * The function takes the seed at first.
     * The function should return {@code None} when it's
     * done generating the CharSeq, otherwise {@code Some} {@code Tuple}
     * of the element for the next call and the value to add to the
     * resulting CharSeq.
     * <p>
     * Example:
     * <pre>
     * <code>
     * CharSeq.unfoldRight('j', x -&gt; x == 'a'
     *             ? Option.none()
     *             : Option.of(new Tuple2&lt;&gt;(new Character(x), (char)(x-1))));
     * // CharSeq.of("jihgfedcb"))
     * </code>
     * </pre>
     *
     * @param <T>  type of seeds
     * @param seed the start value for the iteration
     * @param f    the function to get the next step of the iteration
     * @return a CharSeq with the values built up by the iteration
     * @throws NullPointerException if {@code f} is null
     */
    public static <T> CharSeq unfoldRight(T seed, Function<? super T, Option<Tuple2<? extends Character, ? extends T>>> f) {
        return CharSeq.ofAll(io.vavr.collection.Iterator.unfoldRight(seed, f));
    }

    /**
     * Creates a CharSeq from a seed value and a function.
     * The function takes the seed at first.
     * The function should return {@code None} when it's
     * done generating the CharSeq, otherwise {@code Some} {@code Tuple}
     * of the value to add to the resulting CharSeq and
     * the element for the next call.
     * <p>
     * Example:
     * <pre>
     * <code>
     * CharSeq.unfoldLeft('j', x -&gt; x == 'a'
     *             ? Option.none()
     *             : Option.of(new Tuple2&lt;&gt;((char)(x-1), new Character(x))));
     * // CharSeq.of("bcdefghij"))
     * </code>
     * </pre>
     *
     * @param <T>  type of seeds
     * @param seed the start value for the iteration
     * @param f    the function to get the next step of the iteration
     * @return a CharSeq with the values built up by the iteration
     * @throws NullPointerException if {@code f} is null
     */
    public static <T> CharSeq unfoldLeft(T seed, Function<? super T, Option<Tuple2<? extends T, ? extends Character>>> f) {
        return CharSeq.ofAll(io.vavr.collection.Iterator.unfoldLeft(seed, f));
    }

    /**
     * Creates a CharSeq from a seed value and a function.
     * The function takes the seed at first.
     * The function should return {@code None} when it's
     * done generating the CharSeq, otherwise {@code Some} {@code Tuple}
     * of the value to add to the resulting CharSeq and
     * the element for the next call.
     * <p>
     * Example:
     * <pre>
     * <code>
     * CharSeq.unfold('j', x -&gt; x == 'a'
     *                ? Option.none()
     *                : Option.of(new Tuple2&lt;&gt;((char)(x-1), new Character(x))));
     * // CharSeq.of("bcdefghij"))
     * </code>
     * </pre>
     *
     * @param seed the start value for the iteration
     * @param f    the function to get the next step of the iteration
     * @return a CharSeq with the values built up by the iteration
     * @throws NullPointerException if {@code f} is null
     */
    public static CharSeq unfold(Character seed, Function<? super Character, Option<Tuple2<? extends Character, ? extends Character>>> f) {
        return CharSeq.ofAll(io.vavr.collection.Iterator.unfold(seed, f));
    }

    private Tuple2<CharSeq, CharSeq> splitByBuilder(StringBuilder sb) {
        if (sb.length() == 0) {
            return Tuple.of(EMPTY, this);
        } else if (sb.length() == length()) {
            return Tuple.of(this, EMPTY);
        } else {
            return Tuple.of(of(sb), of(back.substring(sb.length())));
        }
    }

    /**
     * Repeats a character {@code times} times.
     *
     * @param character A character
     * @param times     Repetition count
     * @return A CharSeq representing {@code character * times}
     */
    public static CharSeq repeat(char character, int times) {
        final int length = Math.max(times, 0);
        final char[] characters = new char[length];
        java.util.Arrays.fill(characters, character);
        return new CharSeq(String.valueOf(characters));
    }

    /**
     * Repeats this CharSeq {@code times} times.
     * <p>
     * Example: {@code CharSeq.of("ja").repeat(13) = "jajajajajajajajajajajajaja"}
     *
     * @param times Repetition count
     * @return A CharSeq representing {@code this * times}
     */
    public CharSeq repeat(int times) {
        if (times <= 0 || isEmpty()) {
            return empty();
        } else if (times == 1) {
            return this;
        } else {
            final int finalLength = length() * times;
            final char[] result = new char[finalLength];
            back.getChars(0, length(), result, 0);

            int i = length();
            for (; i <= (finalLength >>> 1); i <<= 1) {
                System.arraycopy(result, 0, result, i, i);
            }
            System.arraycopy(result, 0, result, i, finalLength - i);

            return of(new String(result));
        }
    }

    //
    //
    // IndexedSeq
    //
    //

    @Override
    public CharSeq append(Character element) {
        // DEV-NOTE: we need to unbox, otherwise "null" will be appended to back
        final char c = element;
        return of(back + c);
    }

    @Override
    public CharSeq appendAll(Iterable<? extends Character> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (Collections.isEmpty(elements)) {
            return this;
        }
        final StringBuilder sb = new StringBuilder(back);
        for (char element : elements) {
            sb.append(element);
        }
        return of(sb);
    }

    @Override
    public java.util.List<Character> asJava() {
        return JavaConverters.asJava(this, IMMUTABLE);
    }

    @Override
    public CharSeq asJava(Consumer<? super java.util.List<Character>> action) {
        return Collections.asJava(this, action, IMMUTABLE);
    }

    @Override
    public java.util.List<Character> asJavaMutable() {
        return JavaConverters.asJava(this, MUTABLE);
    }

    @Override
    public CharSeq asJavaMutable(Consumer<? super java.util.List<Character>> action) {
        return Collections.asJava(this, action, MUTABLE);
    }

    @Override
    public <R> IndexedSeq<R> collect(PartialFunction<? super Character, ? extends R> partialFunction) {
        return Vector.ofAll(iterator().<R> collect(partialFunction));
    }

    @Override
    public IndexedSeq<CharSeq> combinations() {
        return Vector.rangeClosed(0, length()).map(this::combinations).flatMap(Function.identity());
    }

    @Override
    public IndexedSeq<CharSeq> combinations(int k) {
        return Combinations.apply(this, Math.max(k, 0));
    }

    @Override
    public io.vavr.collection.Iterator<CharSeq> crossProduct(int power) {
        return io.vavr.collection.Collections.crossProduct(CharSeq.empty(), this, power);
    }

    @Override
    public CharSeq distinct() {
        return distinctBy(Function.identity());
    }

    @Override
    public CharSeq distinctBy(Comparator<? super Character> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        final java.util.Set<Character> seen = new java.util.TreeSet<>(comparator);
        return filter(seen::add);
    }

    @Override
    public <U> CharSeq distinctBy(Function<? super Character, ? extends U> keyExtractor) {
        Objects.requireNonNull(keyExtractor, "keyExtractor is null");
        final java.util.Set<U> seen = new java.util.HashSet<>();
        return filter(t -> seen.add(keyExtractor.apply(t)));
    }

    @Override
    public CharSeq drop(int n) {
        if (n <= 0) {
            return this;
        } else if (n >= length()) {
            return EMPTY;
        } else {
            return of(back.substring(n));
        }
    }

    @Override
    public CharSeq dropUntil(Predicate<? super Character> predicate) {
        return io.vavr.collection.Collections.dropUntil(this, predicate);
    }

    @Override
    public CharSeq dropWhile(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return dropUntil(predicate.negate());
    }

    @Override
    public CharSeq dropRight(int n) {
        if (n <= 0) {
            return this;
        } else if (n >= length()) {
            return EMPTY;
        } else {
            return of(back.substring(0, length() - n));
        }
    }

    @Override
    public CharSeq dropRightWhile(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return dropRightUntil(predicate.negate());
    }

    @Override
    public CharSeq dropRightUntil(Predicate<? super Character> predicate) {
        return io.vavr.collection.Collections.dropRightUntil(this, predicate);
    }

    @Override
    public CharSeq filter(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < back.length(); i++) {
            final char ch = get(i);
            if (predicate.test(ch)) {
                sb.append(ch);
            }
        }
        if (sb.length() == 0) {
            return EMPTY;
        } else if (sb.length() == length()) {
            return this;
        } else {
            return of(sb);
        }
    }

    @Override
    public CharSeq filterNot(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return Collections.filterNot(this, predicate);
    }

    @Override
    public <U> IndexedSeq<U> flatMap(Function<? super Character, ? extends Iterable<? extends U>> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (isEmpty()) {
            return Vector.empty();
        } else {
            IndexedSeq<U> result = Vector.empty();
            for (int i = 0; i < length(); i++) {
                for (U u : mapper.apply(get(i))) {
                    result = result.append(u);
                }
            }
            return result;
        }
    }

    public CharSeq flatMapChars(CharFunction<? extends CharSequence> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (isEmpty()) {
            return this;
        } else {
            final StringBuilder builder = new StringBuilder();
            for (int i = 0; i < back.length(); i++) {
                builder.append(mapper.apply(back.charAt(i)));
            }
            return of(builder);
        }
    }

    @Override
    public <C> Map<C, CharSeq> groupBy(Function<? super Character, ? extends C> classifier) {
        return io.vavr.collection.Collections.groupBy(this, classifier, CharSeq::ofAll);
    }

    @Override
    public io.vavr.collection.Iterator<CharSeq> grouped(int size) {
        return sliding(size, size);
    }

    @Override
    public boolean hasDefiniteSize() {
        return true;
    }

    @Override
    public CharSeq init() {
        if (isEmpty()) {
            throw new UnsupportedOperationException("init of empty string");
        } else {
            return of(back.substring(0, length() - 1));
        }
    }

    @Override
    public Option<CharSeq> initOption() {
        return isEmpty() ? Option.none() : Option.some(init());
    }

    @Override
    public CharSeq insert(int index, Character element) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("insert(" + index + ", e)");
        }
        if (index > length()) {
            throw new IndexOutOfBoundsException("insert(" + index + ", e) on String of length " + length());
        }
        final char c = element;
        return of(new StringBuilder(back).insert(index, c).toString());
    }

    @Override
    public CharSeq insertAll(int index, Iterable<? extends Character> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (index < 0) {
            throw new IndexOutOfBoundsException("insertAll(" + index + ", elements)");
        }
        if (index > length()) {
            throw new IndexOutOfBoundsException("insertAll(" + index + ", elements) on String of length " + length());
        }
        final StringBuilder sb = new StringBuilder(back.substring(0, index));
        for (char element : elements) {
            sb.append(element);
        }
        sb.append(back.substring(index));
        return of(sb);
    }

    @Override
    public io.vavr.collection.Iterator<Character> iterator() {
        return io.vavr.collection.Iterator.ofAll(toCharArray());
    }

    @Override
    public CharSeq intersperse(Character element) {
        final char c = element; // intentionally throw when element is null
        if (isEmpty()) {
            return EMPTY;
        } else {
            final StringBuilder sb = new StringBuilder().append(head());
            for (int i = 1; i < length(); i++) {
                sb.append(c).append(get(i));
            }
            return of(sb);
        }
    }

    @Override
    public <U> IndexedSeq<U> map(Function<? super Character, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        IndexedSeq<U> result = Vector.empty();
        for (int i = 0; i < length(); i++) {
            result = result.append(mapper.apply(get(i)));
        }
        return result;
    }

    @Override
    public String mkString() {
        return back;
    }

    @Override
    public CharSeq padTo(int length, Character element) {
        final int actualLength = back.length();
        if (length <= actualLength) {
            return this;
        } else {
            return new CharSeq(back + padding(element, length - actualLength));
        }
    }

    @Override
    public CharSeq leftPadTo(int length, Character element) {
        final int actualLength = back.length();
        if (length <= actualLength) {
            return this;
        } else {
            return of(padding(element, length - actualLength).append(back));
        }
    }

    @Override
    public CharSeq orElse(Iterable<? extends Character> other) {
        return isEmpty() ? ofAll(other) : this;
    }

    @Override
    public CharSeq orElse(Supplier<? extends Iterable<? extends Character>> supplier) {
        return isEmpty() ? ofAll(supplier.get()) : this;
    }

    private static StringBuilder padding(char element, int limit) {
        final StringBuilder padding = new StringBuilder();
        for (int i = 0; i < limit; i++) {
            padding.append(element);
        }
        return padding;
    }

    @Override
    public CharSeq patch(int from, Iterable<? extends Character> that, int replaced) {
        from = from < 0 ? 0 : from > length() ? length() : from;
        replaced = Math.max(replaced, 0);
        final StringBuilder sb = new StringBuilder(back.substring(0, from));
        for (char character : that) {
            sb.append(character);
        }
        from += replaced;
        if (from < length()) {
            sb.append(back.substring(from));
        }
        return sb.length() == 0 ? EMPTY : of(sb);
    }

    public CharSeq mapChars(CharUnaryOperator mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        if (isEmpty()) {
            return this;
        } else {
            final char[] chars = back.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                chars[i] = mapper.apply(chars[i]);
            }
            return CharSeq.of(chars);
        }
    }

    @Override
    public Tuple2<CharSeq, CharSeq> partition(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (isEmpty()) {
            return Tuple.of(EMPTY, EMPTY);
        }
        final StringBuilder left = new StringBuilder();
        final StringBuilder right = new StringBuilder();
        for (int i = 0; i < length(); i++) {
            final Character t = get(i);
            (predicate.test(t) ? left : right).append(t);
        }
        if (left.length() == 0) {
            return Tuple.of(EMPTY, of(right.toString()));
        } else if (right.length() == 0) {
            return Tuple.of(of(left.toString()), EMPTY);
        } else {
            return Tuple.of(of(left.toString()), of(right.toString()));
        }
    }

    @Override
    public CharSeq peek(Consumer<? super Character> action) {
        Objects.requireNonNull(action, "action is null");
        if (!isEmpty()) {
            action.accept(get(0));
        }
        return this;
    }

    @Override
    public CharSeq tapEach(Consumer<? super Character> action) {
        Objects.requireNonNull(action, "action is null");
        for (int i = 0; i < back.length(); i++) {
            action.accept(get(i));
        }
        return this;
    }

    @Override
    public IndexedSeq<CharSeq> permutations() {
        if (isEmpty()) {
            return Vector.empty();
        } else {
            if (length() == 1) {
                return Vector.of(this);
            } else {
                IndexedSeq<CharSeq> result = Vector.empty();
                for (Character t : distinct()) {
                    for (CharSeq ts : remove(t).permutations()) {
                        result = result.append(CharSeq.of(t).appendAll(ts));
                    }
                }
                return result;
            }
        }
    }

    @Override
    public CharSeq prepend(Character element) {
        final char c = element;
        return of(c + back);
    }

    @Override
    public CharSeq prependAll(Iterable<? extends Character> elements) {
        Objects.requireNonNull(elements, "elements is null");
        if (Collections.isEmpty(elements)) {
            return this;
        } else if (isEmpty()) {
            return ofAll(elements);
        } else {
            final StringBuilder sb = new StringBuilder();
            for (char element : elements) {
                sb.append(element);
            }
            sb.append(back);
            return CharSeq.of(sb);
        }
    }

    @Override
    public CharSeq remove(Character element) {
        if (element == null) {
            return this;
        }
        final StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (int i = 0; i < length(); i++) {
            final char c = get(i);
            if (!found && c == element) {
                found = true;
            } else {
                sb.append(c);
            }
        }
        return sb.length() == 0 ? EMPTY : sb.length() == length() ? this : of(sb);
    }

    @Override
    public CharSeq removeFirst(Predicate<Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (int i = 0; i < back.length(); i++) {
            final char ch = get(i);
            if (predicate.test(ch)) {
                if (found) {
                    sb.append(ch);
                }
                found = true;
            } else {
                sb.append(ch);
            }
        }
        return found ? (sb.length() == 0 ? EMPTY : of(sb.toString())) : this;
    }

    @Override
    public CharSeq removeLast(Predicate<Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        for (int i = length() - 1; i >= 0; i--) {
            if (predicate.test(get(i))) {
                return removeAt(i);
            }
        }
        return this;
    }

    @Override
    public CharSeq removeAt(int index) {
        final String removed = back.substring(0, index) + back.substring(index + 1);
        return removed.isEmpty() ? EMPTY : of(removed);
    }

    @Override
    public CharSeq removeAll(Character element) {
        if (element == null) {
            return this;
        }
        return io.vavr.collection.Collections.removeAll(this, element);
    }

    @Override
    public CharSeq removeAll(Iterable<? extends Character> elements) {
        return io.vavr.collection.Collections.removeAll(this, elements);
    }

    @Override
    public CharSeq replace(Character currentElement, Character newElement) {
        if (currentElement == null) {
            return this;
        }
        final char currentChar = currentElement;
        final char newChar = newElement;
        final StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (int i = 0; i < length(); i++) {
            final char c = get(i);
            if (!found && c == currentChar) {
                sb.append(newChar);
                found = true;
            } else {
                sb.append(c);
            }
        }
        return found ? of(sb) : this;
    }

    @Override
    public CharSeq replaceAll(Character currentElement, Character newElement) {
        if (currentElement == null) {
            return this;
        }
        final char currentChar = currentElement;
        final char newChar = newElement;
        final StringBuilder sb = new StringBuilder();
        boolean found = false;
        for (int i = 0; i < length(); i++) {
            final char c = get(i);
            if (c == currentChar) {
                sb.append(newChar);
                found = true;
            } else {
                sb.append(c);
            }
        }
        return found ? of(sb) : this;
    }

    @Override
    public CharSeq retainAll(Iterable<? extends Character> elements) {
        return io.vavr.collection.Collections.retainAll(this, elements);
    }

    @Override
    public CharSeq reverse() {
        return of(new StringBuilder(back).reverse().toString());
    }

    @Override
    public CharSeq rotateLeft(int n) {
        return Collections.rotateLeft(this, n);
    }

    @Override
    public CharSeq rotateRight(int n) {
        return Collections.rotateRight(this, n);
    }

    @Override
    public CharSeq scan(Character zero, BiFunction<? super Character, ? super Character, ? extends Character> operation) {
        return io.vavr.collection.Collections.scanLeft(this, zero, operation, io.vavr.collection.Iterator::toCharSeq);
    }

    @Override
    public <U> IndexedSeq<U> scanLeft(U zero, BiFunction<? super U, ? super Character, ? extends U> operation) {
        return io.vavr.collection.Collections.scanLeft(this, zero, operation, io.vavr.collection.Iterator::toVector);
    }

    @Override
    public <U> IndexedSeq<U> scanRight(U zero, BiFunction<? super Character, ? super U, ? extends U> operation) {
        return io.vavr.collection.Collections.scanRight(this, zero, operation, io.vavr.collection.Iterator::toVector);
    }

    @Override
    public CharSeq shuffle() {
        return io.vavr.collection.Collections.shuffle(this, CharSeq::ofAll);
    }

    @Override
    public CharSeq shuffle(Random random) {
        return io.vavr.collection.Collections.shuffle(this, random, CharSeq::ofAll);
    }

    @Override
    public CharSeq slice(int beginIndex, int endIndex) {
        final int from = Math.max(beginIndex, 0);
        final int to = endIndex > length() ? length() : endIndex;
        if (from >= to) {
            return EMPTY;
        }
        if (from <= 0 && to >= length()) {
            return this;
        }
        return CharSeq.of(back.substring(from, to));
    }

    @Override
    public io.vavr.collection.Iterator<CharSeq> slideBy(Function<? super Character, ?> classifier) {
        return iterator().slideBy(classifier).map(CharSeq::ofAll);
    }

    @Override
    public io.vavr.collection.Iterator<CharSeq> sliding(int size) {
        return sliding(size, 1);
    }

    @Override
    public io.vavr.collection.Iterator<CharSeq> sliding(int size, int step) {
        return iterator().sliding(size, step).map(CharSeq::ofAll);
    }

    @Override
    public CharSeq sorted() {
        return isEmpty() ? this : toJavaStream().sorted().collect(CharSeq.collector());
    }

    @Override
    public CharSeq sorted(Comparator<? super Character> comparator) {
        Objects.requireNonNull(comparator, "comparator is null");
        return isEmpty() ? this : toJavaStream().sorted(comparator).collect(CharSeq.collector());
    }

    @Override
    public <U extends Comparable<? super U>> CharSeq sortBy(Function<? super Character, ? extends U> mapper) {
        return sortBy(U::compareTo, mapper);
    }

    @Override
    public <U> CharSeq sortBy(Comparator<? super U> comparator, Function<? super Character, ? extends U> mapper) {
        return Collections.sortBy(this, comparator, mapper, collector());
    }

    @Override
    public Tuple2<CharSeq, CharSeq> span(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length(); i++) {
            final char c = get(i);
            if (predicate.test(c)) {
                sb.append(c);
            } else {
                break;
            }
        }
        return splitByBuilder(sb);
    }

    @Override
    public CharSeq subSequence(int beginIndex) {
        if (beginIndex < 0 || beginIndex > length()) {
            throw new IndexOutOfBoundsException("begin index " + beginIndex + " < 0");
        }
        if (beginIndex == 0) {
            return this;
        } else if (beginIndex == length()) {
            return EMPTY;
        } else {
            return CharSeq.of(back.substring(beginIndex));
        }
    }

    @Override
    public CharSeq tail() {
        if (isEmpty()) {
            throw new UnsupportedOperationException("tail of empty string");
        } else {
            return CharSeq.of(back.substring(1));
        }
    }

    @Override
    public Option<CharSeq> tailOption() {
        return isEmpty() ? Option.none() : Option.some(tail());
    }

    @Override
    public CharSeq take(int n) {
        if (n <= 0) {
            return EMPTY;
        } else if (n >= length()) {
            return this;
        } else {
            return CharSeq.of(back.substring(0, n));
        }
    }

    @Override
    public CharSeq takeUntil(Predicate<? super Character> predicate) {
        return io.vavr.collection.Collections.takeUntil(this, predicate);
    }

    @Override
    public CharSeq takeWhile(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return takeUntil(predicate.negate());
    }

    @Override
    public CharSeq takeRight(int n) {
        if (n <= 0) {
            return EMPTY;
        } else if (n >= length()) {
            return this;
        } else {
            return CharSeq.of(back.substring(length() - n));
        }
    }

    @Override
    public CharSeq takeRightUntil(Predicate<? super Character> predicate) {
        return io.vavr.collection.Collections.takeRightUntil(this, predicate);
    }

    @Override
    public CharSeq takeRightWhile(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return takeRightUntil(predicate.negate());
    }

    /**
     * Transforms this {@code CharSeq}.
     *
     * @param f   A transformation
     * @param <U> Type of transformation result
     * @return An instance of type {@code U}
     * @throws NullPointerException if {@code f} is null
     */
    public <U> U transform(Function<? super CharSeq, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return f.apply(this);
    }

    @Override
    public CharSeq update(int index, Character element) {
        if ((index < 0) || (index >= length())) {
            throw new IndexOutOfBoundsException("update(" + index + ")");
        } else {
            char c = element;
            return of(back.substring(0, index) + c + back.substring(index + 1));
        }
    }

    @Override
    public CharSeq update(int index, Function<? super Character, ? extends Character> updater) {
        Objects.requireNonNull(updater, "updater is null");
        final char c = updater.apply(get(index));
        return update(index, c);
    }

    @Override
    public <U> IndexedSeq<Tuple2<Character, U>> zip(Iterable<? extends U> that) {
        return zipWith(that, Tuple::of);
    }

    @Override
    public <U, R> IndexedSeq<R> zipWith(Iterable<? extends U> that, BiFunction<? super Character, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        IndexedSeq<R> result = Vector.empty();
        final io.vavr.collection.Iterator<Character> list1 = iterator();
        final java.util.Iterator<? extends U> list2 = that.iterator();
        while (list1.hasNext() && list2.hasNext()) {
            result = result.append(mapper.apply(list1.next(), list2.next()));
        }
        return result;
    }

    @Override
    public <U> IndexedSeq<Tuple2<Character, U>> zipAll(Iterable<? extends U> that, Character thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        IndexedSeq<Tuple2<Character, U>> result = Vector.empty();
        final io.vavr.collection.Iterator<Character> list1 = iterator();
        final java.util.Iterator<? extends U> list2 = that.iterator();
        while (list1.hasNext() || list2.hasNext()) {
            final Character elem1 = list1.hasNext() ? list1.next() : thisElem;
            final U elem2 = list2.hasNext() ? list2.next() : thatElem;
            result = result.append(Tuple.of(elem1, elem2));
        }
        return result;
    }

    @Override
    public IndexedSeq<Tuple2<Character, Integer>> zipWithIndex() {
        return zipWithIndex(Tuple::of);
    }

    @Override
    public <U> IndexedSeq<U> zipWithIndex(BiFunction<? super Character, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        IndexedSeq<U> result = Vector.empty();
        for (int i = 0; i < length(); i++) {
            result = result.append(mapper.apply(get(i), i));
        }
        return result;
    }

    @Override
    public Character apply(Integer index) {
        return back.charAt(index);
    }

    @Override
    public int indexOf(Character element, int from) {
        return back.indexOf(element, from);
    }

    @Override
    public int lastIndexOf(Character element, int end) {
        return back.lastIndexOf(element, end);
    }

    @Override
    public Tuple2<CharSeq, CharSeq> splitAt(int n) {
        if (n <= 0) {
            return Tuple.of(EMPTY, this);
        } else if (n >= length()) {
            return Tuple.of(this, EMPTY);
        } else {
            return Tuple.of(of(back.substring(0, n)), of(back.substring(n)));
        }
    }

    @Override
    public Tuple2<CharSeq, CharSeq> splitAt(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (isEmpty()) {
            return Tuple.of(EMPTY, EMPTY);
        }
        final StringBuilder left = new StringBuilder();
        for (int i = 0; i < length(); i++) {
            final Character t = get(i);
            if (!predicate.test(t)) {
                left.append(t);
            } else {
                break;
            }
        }
        return splitByBuilder(left);
    }

    @Override
    public Tuple2<CharSeq, CharSeq> splitAtInclusive(Predicate<? super Character> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        if (isEmpty()) {
            return Tuple.of(EMPTY, EMPTY);
        }
        final StringBuilder left = new StringBuilder();
        for (int i = 0; i < length(); i++) {
            final Character t = get(i);
            left.append(t);
            if (predicate.test(t)) {
                break;
            }
        }
        return splitByBuilder(left);
    }

    @Override
    public boolean startsWith(Iterable<? extends Character> that, int offset) {
        return startsWith(CharSeq.ofAll(that), offset);
    }

    @Override
    public Character head() {
        if (isEmpty()) {
            throw new NoSuchElementException("head of empty string");
        } else {
            return get(0);
        }
    }

    /**
     * A {@code CharSeq} is computed synchronously.
     *
     * @return false
     */
    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isEmpty() {
        return back.isEmpty();
    }

    /**
     * A {@code CharSeq} is computed eagerly.
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

    private Object readResolve() {
        return isEmpty() ? EMPTY : this;
    }

    @Override
    public boolean equals(Object o) {
        return io.vavr.collection.Collections.equals(this, o);
    }

    @Override
    public int hashCode() {
        return io.vavr.collection.Collections.hashOrdered(this);
    }

    //
    //
    // CharSequence
    //
    //

    /**
     * Returns the {@code char} value at the
     * specified index. An index ranges from {@code 0} to
     * {@code length() - 1}. The first {@code char} value of the sequence
     * is at index {@code 0}, the next at index {@code 1},
     * and so on, as for array indexing.
     * <p>
     * If the {@code char} value specified by the index is a
     * <a href="Character.html#unicode">surrogate</a>, the surrogate
     * value is returned.
     *
     * @param index the index of the {@code char} value.
     * @return the {@code char} value at the specified index of this string.
     * The first {@code char} value is at index {@code 0}.
     * @throws IndexOutOfBoundsException if the {@code index}
     *                                   argument is negative or not less than the length of this
     *                                   string.
     */
    @Override
    public char charAt(int index) {
        return get(index);
    }

    /**
     * Returns the length of this string.
     * The length is equal to the number of <a href="Character.html#unicode">Unicode
     * code units</a> in the string.
     *
     * @return the length of the sequence of characters represented by this
     * object.
     */
    @Override
    public int length() {
        return back.length();
    }

    //
    //
    // String
    //
    //

    /**
     * Returns the character (Unicode code point) at the specified
     * index. The index refers to {@code char} values
     * (Unicode code units) and ranges from {@code 0} to
     * {@link #length()}{@code  - 1}.
     * <p>
     * If the {@code char} value specified at the given index
     * is in the high-surrogate range, the following index is less
     * than the length of this {@code CharSeq}, and the
     * {@code char} value at the following index is in the
     * low-surrogate range, then the supplementary code point
     * corresponding to this surrogate pair is returned. Otherwise,
     * the {@code char} value at the given index is returned.
     *
     * @param index the index to the {@code char} values
     * @return the code point value of the character at the
     * {@code index}
     * @throws IndexOutOfBoundsException if the {@code index}
     *                                   argument is negative or not less than the length of this
     *                                   string.
     */
    public int codePointAt(int index) {
        return back.codePointAt(index);
    }

    /**
     * Returns the character (Unicode code point) before the specified
     * index. The index refers to {@code char} values
     * (Unicode code units) and ranges from {@code 1} to {@link
     * CharSequence#length() length}.
     * <p>
     * If the {@code char} value at {@code (index - 1)}
     * is in the low-surrogate range, {@code (index - 2)} is not
     * negative, and the {@code char} value at {@code (index -
     * 2)} is in the high-surrogate range, then the
     * supplementary code point value of the surrogate pair is
     * returned. If the {@code char} value at {@code index -
     * 1} is an unpaired low-surrogate or a high-surrogate, the
     * surrogate value is returned.
     *
     * @param index the index following the code point that should be returned
     * @return the Unicode code point value before the given index.
     * @throws IndexOutOfBoundsException if the {@code index}
     *                                   argument is less than 1 or greater than the length
     *                                   of this string.
     */
    public int codePointBefore(int index) {
        return back.codePointBefore(index);
    }

    /**
     * Returns the number of Unicode code points in the specified text
     * range of this {@code CharSeq}. The text range begins at the
     * specified {@code beginIndex} and extends to the
     * {@code char} at index {@code endIndex - 1}. Thus the
     * length (in {@code char}s) of the text range is
     * {@code endIndex-beginIndex}. Unpaired surrogates within
     * the text range count as one code point each.
     *
     * @param beginIndex the index to the first {@code char} of
     *                   the text range.
     * @param endIndex   the index after the last {@code char} of
     *                   the text range.
     * @return the number of Unicode code points in the specified text
     * range
     * @throws IndexOutOfBoundsException if the
     *                                   {@code beginIndex} is negative, or {@code endIndex}
     *                                   is larger than the length of this {@code CharSeq}, or
     *                                   {@code beginIndex} is larger than {@code endIndex}.
     */
    public int codePointCount(int beginIndex, int endIndex) {
        return back.codePointCount(beginIndex, endIndex);
    }

    /**
     * Returns the index within this {@code CharSeq} that is
     * offset from the given {@code index} by
     * {@code codePointOffset} code points. Unpaired surrogates
     * within the text range given by {@code index} and
     * {@code codePointOffset} count as one code point each.
     *
     * @param index           the index to be offset
     * @param codePointOffset the offset in code points
     * @return the index within this {@code CharSeq}
     * @throws IndexOutOfBoundsException if {@code index}
     *                                   is negative or larger then the length of this
     *                                   {@code CharSeq}, or if {@code codePointOffset} is positive
     *                                   and the substring starting with {@code index} has fewer
     *                                   than {@code codePointOffset} code points,
     *                                   or if {@code codePointOffset} is negative and the substring
     *                                   before {@code index} has fewer than the absolute value
     *                                   of {@code codePointOffset} code points.
     */
    public int offsetByCodePoints(int index, int codePointOffset) {
        return back.offsetByCodePoints(index, codePointOffset);
    }

    /**
     * Copies characters from this string into the destination character
     * array.
     * <p>
     * The first character to be copied is at index {@code srcBegin};
     * the last character to be copied is at index {@code srcEnd-1}
     * (thus the total number of characters to be copied is
     * {@code srcEnd-srcBegin}). The characters are copied into the
     * subarray of {@code dst} starting at index {@code dstBegin}
     * and ending at index:
     * <blockquote><pre>
     *     dstbegin + (srcEnd-srcBegin) - 1
     * </pre></blockquote>
     *
     * @param srcBegin index of the first character in the string
     *                 to copy.
     * @param srcEnd   index after the last character in the string
     *                 to copy.
     * @param dst      the destination array.
     * @param dstBegin the start offset in the destination array.
     * @throws IndexOutOfBoundsException If any of the following
     *                                   is true:
     *                                   <ul><li>{@code srcBegin} is negative.
     *                                   <li>{@code srcBegin} is greater than {@code srcEnd}
     *                                   <li>{@code srcEnd} is greater than the length of this
     *                                   string
     *                                   <li>{@code dstBegin} is negative
     *                                   <li>{@code dstBegin+(srcEnd-srcBegin)} is larger than
     *                                   {@code dst.length}</ul>
     */
    public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
        back.getChars(srcBegin, srcEnd, dst, dstBegin);
    }

    /**
     * Encodes this {@code CharSeq} into a sequence of bytes using the named
     * charset, storing the result into a new byte array.
     * <p>
     * The behavior of this method when this string cannot be encoded in
     * the given charset is unspecified.  The {@link
     * java.nio.charset.CharsetEncoder} class should be used when more control
     * over the encoding process is required.
     *
     * @param charsetName The name of a supported {@linkplain java.nio.charset.Charset
     *                    charset}
     * @return The resultant byte array
     * @throws UnsupportedEncodingException If the named charset is not supported
     */
    public byte[] getBytes(String charsetName) throws UnsupportedEncodingException {
        return back.getBytes(charsetName);
    }

    /**
     * Encodes this {@code CharSeq} into a sequence of bytes using the given
     * {@linkplain java.nio.charset.Charset charset}, storing the result into a
     * new byte array.
     * <p>
     * This method always replaces malformed-input and unmappable-character
     * sequences with this charset's default replacement byte array.  The
     * {@link java.nio.charset.CharsetEncoder} class should be used when more
     * control over the encoding process is required.
     *
     * @param charset The {@linkplain java.nio.charset.Charset} to be used to encode
     *                the {@code CharSeq}
     * @return The resultant byte array
     */
    public byte[] getBytes(Charset charset) {
        return back.getBytes(charset);
    }

    /**
     * Encodes this {@code CharSeq} into a sequence of bytes using the
     * platform's default charset, storing the result into a new byte array.
     * <p>
     * The behavior of this method when this string cannot be encoded in
     * the default charset is unspecified.  The {@link
     * java.nio.charset.CharsetEncoder} class should be used when more control
     * over the encoding process is required.
     *
     * @return The resultant byte array
     */
    public byte[] getBytes() {
        return back.getBytes();
    }

    /**
     * Compares this string to the specified {@code StringBuffer}.  The result
     * is {@code true} if and only if this {@code CharSeq} represents the same
     * sequence of characters as the specified {@code StringBuffer}. This method
     * synchronizes on the {@code StringBuffer}.
     *
     * @param sb The {@code StringBuffer} to compare this {@code CharSeq} against
     * @return {@code true} if this {@code CharSeq} represents the same
     * sequence of characters as the specified {@code StringBuffer},
     * {@code false} otherwise
     */
    public boolean contentEquals(StringBuffer sb) {
        return back.contentEquals(sb);
    }

    /**
     * Compares this string to the specified {@code CharSequence}.  The
     * result is {@code true} if and only if this {@code CharSeq} represents the
     * same sequence of char values as the specified sequence. Note that if the
     * {@code CharSequence} is a {@code StringBuffer} then the method
     * synchronizes on it.
     *
     * @param cs The sequence to compare this {@code CharSeq} against
     * @return {@code true} if this {@code CharSeq} represents the same
     * sequence of char values as the specified sequence, {@code
     * false} otherwise
     */
    public boolean contentEquals(CharSequence cs) {
        return back.contentEquals(cs);
    }

    /**
     * Compares this {@code CharSeq} to another {@code CharSeq}, ignoring case
     * considerations.  Two strings are considered equal ignoring case if they
     * are of the same length and corresponding characters in the two strings
     * are equal ignoring case.
     * <p>
     * Two characters {@code c1} and {@code c2} are considered the same
     * ignoring case if at least one of the following is true:
     * <ul>
     * <li> The two characters are the same (as compared by the
     * {@code ==} operator)
     * <li> Applying the method {@link
     * Character#toUpperCase(char)} to each character
     * produces the same result
     * <li> Applying the method {@link
     * Character#toLowerCase(char)} to each character
     * produces the same result
     * </ul>
     *
     * @param anotherString The {@code CharSeq} to compare this {@code CharSeq} against
     * @return {@code true} if the argument is not {@code null} and it
     * represents an equivalent {@code CharSeq} ignoring case; {@code
     * false} otherwise
     * @see #equals(Object)
     */
    public boolean equalsIgnoreCase(CharSeq anotherString) {
        return back.equalsIgnoreCase(anotherString.back);
    }

    /**
     * Compares two strings lexicographically.
     * The comparison is based on the Unicode value of each character in
     * the strings. The character sequence represented by this
     * {@code CharSeq} object is compared lexicographically to the
     * character sequence represented by the argument string. The result is
     * a negative integer if this {@code CharSeq} object
     * lexicographically precedes the argument string. The result is a
     * positive integer if this {@code CharSeq} object lexicographically
     * follows the argument string. The result is zero if the strings
     * are equal; {@code compareTo} returns {@code 0} exactly when
     * the {@link #equals(Object)} method would return {@code true}.
     * <p>
     * This is the definition of lexicographic ordering. If two strings are
     * different, then either they have different characters at some index
     * that is a valid index for both strings, or their lengths are different,
     * or both. If they have different characters at one or more index
     * positions, let <i>k</i> be the smallest such index; then the string
     * whose character at position <i>k</i> has the smaller value, as
     * determined by using the &lt; operator, lexicographically precedes the
     * other string. In this case, {@code compareTo} returns the
     * difference of the two character values at position {@code k} in
     * the two string -- that is, the value:
     * <blockquote><pre>
     * this.charAt(k)-anotherString.charAt(k)
     * </pre></blockquote>
     * If there is no index position at which they differ, then the shorter
     * string lexicographically precedes the longer string. In this case,
     * {@code compareTo} returns the difference of the lengths of the
     * strings -- that is, the value:
     * <blockquote><pre>
     * this.length()-anotherString.length()
     * </pre></blockquote>
     *
     * @param anotherString the {@code CharSeq} to be compared.
     * @return the value {@code 0} if the argument string is equal to
     * this string; a value less than {@code 0} if this string
     * is lexicographically less than the string argument; and a
     * value greater than {@code 0} if this string is
     * lexicographically greater than the string argument.
     */
    public int compareTo(CharSeq anotherString) {
        return back.compareTo(anotherString.back);
    }

    /**
     * Compares two strings lexicographically, ignoring case
     * differences. This method returns an integer whose sign is that of
     * calling {@code compareTo} with normalized versions of the strings
     * where case differences have been eliminated by calling
     * {@code Character.toLowerCase(Character.toUpperCase(character))} on
     * each character.
     * <p>
     * Note that this method does <em>not</em> take locale into account,
     * and will result in an unsatisfactory ordering for certain locales.
     * The java.text package provides <em>collators</em> to allow
     * locale-sensitive ordering.
     *
     * @param str the {@code CharSeq} to be compared.
     * @return a negative integer, zero, or a positive integer as the
     * specified String is greater than, equal to, or less
     * than this String, ignoring case considerations.
     */
    public int compareToIgnoreCase(CharSeq str) {
        return back.compareToIgnoreCase(str.back);
    }

    /**
     * Tests if two string regions are equal.
     * <p>
     * A substring of this {@code CharSeq} object is compared to a substring
     * of the argument other. The result is true if these substrings
     * represent identical character sequences. The substring of this
     * {@code CharSeq} object to be compared begins at index {@code toffset}
     * and has length {@code len}. The substring of other to be compared
     * begins at index {@code ooffset} and has length {@code len}. The
     * result is {@code false} if and only if at least one of the following
     * is true:
     * <ul><li>{@code toffset} is negative.
     * <li>{@code ooffset} is negative.
     * <li>{@code toffset+len} is greater than the length of this
     * {@code CharSeq} object.
     * <li>{@code ooffset+len} is greater than the length of the other
     * argument.
     * <li>There is some nonnegative integer <i>k</i> less than {@code len}
     * such that:
     * {@code this.charAt(toffset + }<i>k</i>{@code ) != other.charAt(ooffset + }
     * <i>k</i>{@code )}
     * </ul>
     *
     * @param toffset the starting offset of the subregion in this string.
     * @param other   the string argument.
     * @param ooffset the starting offset of the subregion in the string
     *                argument.
     * @param len     the number of characters to compare.
     * @return {@code true} if the specified subregion of this string
     * exactly matches the specified subregion of the string argument;
     * {@code false} otherwise.
     */
    public boolean regionMatches(int toffset, CharSeq other, int ooffset, int len) {
        return back.regionMatches(toffset, other.back, ooffset, len);
    }

    /**
     * Tests if two string regions are equal.
     * <p>
     * A substring of this {@code CharSeq} object is compared to a substring
     * of the argument {@code other}. The result is {@code true} if these
     * substrings represent character sequences that are the same, ignoring
     * case if and only if {@code ignoreCase} is true. The substring of
     * this {@code CharSeq} object to be compared begins at index
     * {@code toffset} and has length {@code len}. The substring of
     * {@code other} to be compared begins at index {@code ooffset} and
     * has length {@code len}. The result is {@code false} if and only if
     * at least one of the following is true:
     * <ul><li>{@code toffset} is negative.
     * <li>{@code ooffset} is negative.
     * <li>{@code toffset+len} is greater than the length of this
     * {@code CharSeq} object.
     * <li>{@code ooffset+len} is greater than the length of the other
     * argument.
     * <li>{@code ignoreCase} is {@code false} and there is some nonnegative
     * integer <i>k</i> less than {@code len} such that:
     * <blockquote><pre>
     * this.charAt(toffset+k) != other.charAt(ooffset+k)
     * </pre></blockquote>
     * <li>{@code ignoreCase} is {@code true} and there is some nonnegative
     * integer <i>k</i> less than {@code len} such that:
     * <blockquote><pre>
     * Character.toLowerCase(this.charAt(toffset+k)) !=
     * Character.toLowerCase(other.charAt(ooffset+k))
     * </pre></blockquote>
     * and:
     * <blockquote><pre>
     * Character.toUpperCase(this.charAt(toffset+k)) !=
     *         Character.toUpperCase(other.charAt(ooffset+k))
     * </pre></blockquote>
     * </ul>
     *
     * @param ignoreCase if {@code true}, ignore case when comparing
     *                   characters.
     * @param toffset    the starting offset of the subregion in this
     *                   string.
     * @param other      the string argument.
     * @param ooffset    the starting offset of the subregion in the string
     *                   argument.
     * @param len        the number of characters to compare.
     * @return {@code true} if the specified subregion of this string
     * matches the specified subregion of the string argument;
     * {@code false} otherwise. Whether the matching is exact
     * or case insensitive depends on the {@code ignoreCase}
     * argument.
     */
    public boolean regionMatches(boolean ignoreCase, int toffset, CharSeq other, int ooffset, int len) {
        return back.regionMatches(ignoreCase, toffset, other.back, ooffset, len);
    }

    @Override
    public CharSeq subSequence(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new IndexOutOfBoundsException("begin index " + beginIndex + " < 0");
        }
        if (endIndex > length()) {
            throw new IndexOutOfBoundsException("endIndex " + endIndex + " > length " + length());
        }
        final int subLen = endIndex - beginIndex;
        if (subLen < 0) {
            throw new IllegalArgumentException("beginIndex " + beginIndex + " > endIndex " + endIndex);
        }
        if (beginIndex == 0 && endIndex == length()) {
            return this;
        } else {
            return CharSeq.of(back.subSequence(beginIndex, endIndex));
        }
    }

    /**
     * Tests if the substring of this string beginning at the
     * specified index starts with the specified prefix.
     *
     * @param prefix  the prefix.
     * @param toffset where to begin looking in this string.
     * @return {@code true} if the character sequence represented by the
     * argument is a prefix of the substring of this object starting
     * at index {@code toffset}; {@code false} otherwise.
     * The result is {@code false} if {@code toffset} is
     * negative or greater than the length of this
     * {@code CharSeq} object; otherwise the result is the same
     * as the result of the expression
     * <pre>
     *          this.substring(toffset).startsWith(prefix)
     *          </pre>
     */
    public boolean startsWith(CharSeq prefix, int toffset) {
        return back.startsWith(prefix.back, toffset);
    }

    /**
     * Tests if this string starts with the specified prefix.
     *
     * @param prefix the prefix.
     * @return {@code true} if the character sequence represented by the
     * argument is a prefix of the character sequence represented by
     * this string; {@code false} otherwise.
     * Note also that {@code true} will be returned if the
     * argument is an empty string or is equal to this
     * {@code CharSeq} object as determined by the
     * {@link #equals(Object)} method.
     */
    public boolean startsWith(CharSeq prefix) {
        return back.startsWith(prefix.back);
    }

    /**
     * Tests if this string ends with the specified suffix.
     *
     * @param suffix the suffix.
     * @return {@code true} if the character sequence represented by the
     * argument is a suffix of the character sequence represented by
     * this object; {@code false} otherwise. Note that the
     * result will be {@code true} if the argument is the
     * empty string or is equal to this {@code CharSeq} object
     * as determined by the {@link #equals(Object)} method.
     */
    public boolean endsWith(CharSeq suffix) {
        return back.endsWith(suffix.back);
    }

    /**
     * Returns the index within this string of the first occurrence of
     * the specified character. If a character with value
     * {@code ch} occurs in the character sequence represented by
     * this {@code CharSeq} object, then the index (in Unicode
     * code units) of the first such occurrence is returned. For
     * values of {@code ch} in the range from 0 to 0xFFFF
     * (inclusive), this is the smallest value <i>k</i> such that:
     * <blockquote><pre>
     * this.charAt(<i>k</i>) == ch
     * </pre></blockquote>
     * is true. For other values of {@code ch}, it is the
     * smallest value <i>k</i> such that:
     * <blockquote><pre>
     * this.codePointAt(<i>k</i>) == ch
     * </pre></blockquote>
     * is true. In either case, if no such character occurs in this
     * string, then {@code -1} is returned.
     *
     * @param ch a character (Unicode code point).
     * @return the index of the first occurrence of the character in the
     * character sequence represented by this object, or
     * {@code -1} if the character does not occur.
     */
    public int indexOf(int ch) {
        return back.indexOf(ch);
    }

    /**
     * Returns the index of the first occurrence of the given element as an {@code Option}.
     *
     * @param ch a character (Unicode code point).
     * @return {@code Some(index)} or {@code None} if not found.
     */
    Option<Integer> indexOfOption(int ch) {
        return io.vavr.collection.Collections.indexOption(indexOf(ch));
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified character, starting the search at the specified index.
     * <p>
     * If a character with value {@code ch} occurs in the
     * character sequence represented by this {@code CharSeq}
     * object at an index no smaller than {@code fromIndex}, then
     * the index of the first such occurrence is returned. For values
     * of {@code ch} in the range from 0 to 0xFFFF (inclusive),
     * this is the smallest value <i>k</i> such that:
     * <blockquote><pre>
     * (this.charAt(<i>k</i>) == ch) {@code &&} (<i>k</i> &gt;= fromIndex)
     * </pre></blockquote>
     * is true. For other values of {@code ch}, it is the
     * smallest value <i>k</i> such that:
     * <blockquote><pre>
     * (this.codePointAt(<i>k</i>) == ch) {@code &&} (<i>k</i> &gt;= fromIndex)
     * </pre></blockquote>
     * is true. In either case, if no such character occurs in this
     * string at or after position {@code fromIndex}, then
     * {@code -1} is returned.
     * <p>
     * There is no restriction on the value of {@code fromIndex}. If it
     * is negative, it has the same effect as if it were zero: this entire
     * string may be searched. If it is greater than the length of this
     * string, it has the same effect as if it were equal to the length of
     * this string: {@code -1} is returned.
     * <p>
     * All indices are specified in {@code char} values
     * (Unicode code units).
     *
     * @param ch        a character (Unicode code point).
     * @param fromIndex the index to start the search from.
     * @return the index of the first occurrence of the character in the
     * character sequence represented by this object that is greater
     * than or equal to {@code fromIndex}, or {@code -1}
     * if the character does not occur.
     */
    public int indexOf(int ch, int fromIndex) {
        return back.indexOf(ch, fromIndex);
    }

    /**
     * Returns the index of the first occurrence of the given element as an {@code Option},
     * starting the search at the specified index.
     *
     * @param ch        a character (Unicode code point).
     * @param fromIndex the index to start the search from.
     * @return {@code Some(index)} or {@code None} if not found.
     */
    Option<Integer> indexOfOption(int ch, int fromIndex) {
        return io.vavr.collection.Collections.indexOption(indexOf(ch, fromIndex));
    }

    /**
     * Returns the index within this string of the last occurrence of
     * the specified character. For values of {@code ch} in the
     * range from 0 to 0xFFFF (inclusive), the index (in Unicode code
     * units) returned is the largest value <i>k</i> such that:
     * <blockquote><pre>
     * this.charAt(<i>k</i>) == ch
     * </pre></blockquote>
     * is true. For other values of {@code ch}, it is the
     * largest value <i>k</i> such that:
     * <blockquote><pre>
     * this.codePointAt(<i>k</i>) == ch
     * </pre></blockquote>
     * is true.  In either case, if no such character occurs in this
     * string, then {@code -1} is returned.  The
     * {@code CharSeq} is searched backwards starting at the last
     * character.
     *
     * @param ch a character (Unicode code point).
     * @return the index of the last occurrence of the character in the
     * character sequence represented by this object, or
     * {@code -1} if the character does not occur.
     */
    public int lastIndexOf(int ch) {
        return back.lastIndexOf(ch);
    }

    /**
     * Returns the index of the last occurrence of the given element as an {@code Option}.
     *
     * @param ch a character (Unicode code point).
     * @return {@code Some(index)} or {@code None} if not found.
     */
    Option<Integer> lastIndexOfOption(int ch) {
        return io.vavr.collection.Collections.indexOption(lastIndexOf(ch));
    }

    /**
     * Returns the index within this string of the last occurrence of
     * the specified character, searching backward starting at the
     * specified index. For values of {@code ch} in the range
     * from 0 to 0xFFFF (inclusive), the index returned is the largest
     * value <i>k</i> such that:
     * <blockquote><pre>
     * (this.charAt(<i>k</i>) == ch) {@code &&} (<i>k</i> &lt;= fromIndex)
     * </pre></blockquote>
     * is true. For other values of {@code ch}, it is the
     * largest value <i>k</i> such that:
     * <blockquote><pre>
     * (this.codePointAt(<i>k</i>) == ch) {@code &&} (<i>k</i> &lt;= fromIndex)
     * </pre></blockquote>
     * is true. In either case, if no such character occurs in this
     * string at or before position {@code fromIndex}, then
     * {@code -1} is returned.
     * <p>
     * All indices are specified in {@code char} values
     * (Unicode code units).
     *
     * @param ch        a character (Unicode code point).
     * @param fromIndex the index to start the search from. There is no
     *                  restriction on the value of {@code fromIndex}. If it is
     *                  greater than or equal to the length of this string, it has
     *                  the same effect as if it were equal to one less than the
     *                  length of this string: this entire string may be searched.
     *                  If it is negative, it has the same effect as if it were -1:
     *                  -1 is returned.
     * @return the index of the last occurrence of the character in the
     * character sequence represented by this object that is less
     * than or equal to {@code fromIndex}, or {@code -1}
     * if the character does not occur before that point.
     */
    public int lastIndexOf(int ch, int fromIndex) {
        return back.lastIndexOf(ch, fromIndex);
    }

    /**
     * Returns the index of the last occurrence of the given element as an {@code Option},
     * starting the search at the specified index.
     *
     * @param ch        a character (Unicode code point).
     * @param fromIndex the index to start the search from.
     * @return {@code Some(index)} or {@code None} if not found.
     */
    public Option<Integer> lastIndexOfOption(int ch, int fromIndex) {
        return io.vavr.collection.Collections.indexOption(lastIndexOf(ch, fromIndex));
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring.
     * <p>
     * The returned index is the smallest value <i>k</i> for which:
     * <blockquote><pre>
     * this.startsWith(str, <i>k</i>)
     * </pre></blockquote>
     * If no such value of <i>k</i> exists, then {@code -1} is returned.
     *
     * @param str the substring to search for.
     * @return the index of the first occurrence of the specified substring,
     * or {@code -1} if there is no such occurrence.
     */
    public int indexOf(CharSeq str) {
        return back.indexOf(str.back);
    }

    /**
     * Returns the index of the first occurrence of the given element as an {@code Option}.
     *
     * @param str the substring to search for.
     * @return {@code Some(index)} or {@code None} if not found.
     */
    public Option<Integer> indexOfOption(CharSeq str) {
        return io.vavr.collection.Collections.indexOption(indexOf(str));
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring, starting at the specified index.
     * <p>
     * The returned index is the smallest value <i>k</i> for which:
     * <blockquote><pre>
     * <i>k</i> &gt;= fromIndex {@code &&} this.startsWith(str, <i>k</i>)
     * </pre></blockquote>
     * If no such value of <i>k</i> exists, then {@code -1} is returned.
     *
     * @param str       the substring to search for.
     * @param fromIndex the index from which to start the search.
     * @return the index of the first occurrence of the specified substring,
     * starting at the specified index,
     * or {@code -1} if there is no such occurrence.
     */
    public int indexOf(CharSeq str, int fromIndex) {
        return back.indexOf(str.back, fromIndex);
    }

    /**
     * Returns the index of the first occurrence of the given element as an {@code Option},
     * starting the search at the specified index.
     *
     * @param str       the substring to search for.
     * @param fromIndex the index from which to start the search.
     * @return {@code Some(index)} or {@code None} if not found.
     */
    public Option<Integer> indexOfOption(CharSeq str, int fromIndex) {
        return io.vavr.collection.Collections.indexOption(indexOf(str, fromIndex));
    }

    /**
     * Returns the index within this string of the last occurrence of the
     * specified substring.  The last occurrence of the empty string ""
     * is considered to occur at the index value {@code this.length()}.
     * <p>
     * The returned index is the largest value <i>k</i> for which:
     * <blockquote><pre>
     * this.startsWith(str, <i>k</i>)
     * </pre></blockquote>
     * If no such value of <i>k</i> exists, then {@code -1} is returned.
     *
     * @param str the substring to search for.
     * @return the index of the last occurrence of the specified substring,
     * or {@code -1} if there is no such occurrence.
     */
    public int lastIndexOf(CharSeq str) {
        return back.lastIndexOf(str.back);
    }

    /**
     * Returns the index of the last occurrence of the given element as an {@code Option}.
     *
     * @param str the substring to search for.
     * @return {@code Some(index)} or {@code None} if not found.
     */
    public Option<Integer> lastIndexOfOption(CharSeq str) {
        return io.vavr.collection.Collections.indexOption(lastIndexOf(str));
    }

    /**
     * Returns the index within this string of the last occurrence of the
     * specified substring, searching backward starting at the specified index.
     * <p>
     * The returned index is the largest value <i>k</i> for which:
     * <blockquote><pre>
     * <i>k</i> {@code <=} fromIndex {@code &&} this.startsWith(str, <i>k</i>)
     * </pre></blockquote>
     * If no such value of <i>k</i> exists, then {@code -1} is returned.
     *
     * @param str       the substring to search for.
     * @param fromIndex the index to start the search from.
     * @return the index of the last occurrence of the specified substring,
     * searching backward from the specified index,
     * or {@code -1} if there is no such occurrence.
     */
    public int lastIndexOf(CharSeq str, int fromIndex) {
        return back.lastIndexOf(str.back, fromIndex);
    }

    /**
     * Returns the index of the last occurrence of the given element as an {@code Option},
     * starting the search at the specified index.
     *
     * @param str       the substring to search for.
     * @param fromIndex the index to start the search from.
     * @return {@code Some(index)} or {@code None} if not found.
     */
    public Option<Integer> lastIndexOfOption(CharSeq str, int fromIndex) {
        return io.vavr.collection.Collections.indexOption(lastIndexOf(str, fromIndex));
    }

    /**
     * Returns a string that is a substring of this string. The
     * substring begins with the character at the specified index and
     * extends to the end of this string. <p>
     * Examples:
     * <blockquote><pre>
     * "unhappy".substring(2) returns "happy"
     * "Harbison".substring(3) returns "bison"
     * "emptiness".substring(9) returns "" (an empty string)
     * </pre></blockquote>
     *
     * @param beginIndex the beginning index, inclusive.
     * @return the specified substring.
     * @throws IndexOutOfBoundsException if
     *                                   {@code beginIndex} is negative or larger than the
     *                                   length of this {@code CharSeq} object.
     */
    public CharSeq substring(int beginIndex) {
        return CharSeq.of(back.substring(beginIndex));
    }

    /**
     * Returns a string that is a substring of this string. The
     * substring begins at the specified {@code beginIndex} and
     * extends to the character at index {@code endIndex - 1}.
     * Thus the length of the substring is {@code endIndex-beginIndex}.
     * <p>
     * Examples:
     * <blockquote><pre>
     * "hamburger".substring(4, 8) returns "urge"
     * "smiles".substring(1, 5) returns "mile"
     * </pre></blockquote>
     *
     * @param beginIndex the beginning index, inclusive.
     * @param endIndex   the ending index, exclusive.
     * @return the specified substring.
     * @throws IndexOutOfBoundsException if the
     *                                   {@code beginIndex} is negative, or
     *                                   {@code endIndex} is larger than the length of
     *                                   this {@code CharSeq} object, or
     *                                   {@code beginIndex} is larger than
     *                                   {@code endIndex}.
     */
    public CharSeq substring(int beginIndex, int endIndex) {
        return CharSeq.of(back.substring(beginIndex, endIndex));
    }

    @Override
    public String stringPrefix() {
        return "CharSeq";
    }

    /**
     * Returns a string containing the characters in this sequence in the same
     * order as this sequence.  The length of the string will be the length of
     * this sequence.
     *
     * @return a string consisting of exactly this sequence of characters
     */
    @Override
    public String toString() {
        return back;
    }

    /**
     * Concatenates the specified string to the end of this string.
     * <p>
     * If the length of the argument string is {@code 0}, then this
     * {@code CharSeq} object is returned. Otherwise, a
     * {@code CharSeq} object is returned that represents a character
     * sequence that is the concatenation of the character sequence
     * represented by this {@code CharSeq} object and the character
     * sequence represented by the argument string.<p>
     * Examples:
     * <blockquote><pre>
     * "cares".concat("s") returns "caress"
     * "to".concat("get").concat("her") returns "together"
     * </pre></blockquote>
     *
     * @param str the {@code CharSeq} that is concatenated to the end
     *            of this {@code CharSeq}.
     * @return a string that represents the concatenation of this object's
     * characters followed by the string argument's characters.
     */
    public CharSeq concat(CharSeq str) {
        return CharSeq.of(back.concat(str.back));
    }

    /**
     * Tells whether or not this string matches the given <a
     * href="https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#sum">regular expression</a>.
     * <p>
     * An invocation of this method of the form
     * <i>str</i>{@code .matches(}<i>regex</i>{@code )} yields exactly the
     * same result as the expression
     * <blockquote>
     * {@link Pattern}.{@link Pattern#matches(String, CharSequence)
     * matches(<i>regex</i>, <i>str</i>)}
     * </blockquote>
     *
     * @param regex the regular expression to which this string is to be matched
     * @return {@code true} if, and only if, this string matches the
     * given regular expression
     * @throws PatternSyntaxException if the regular expression's syntax is invalid
     * @see Pattern
     */
    public boolean matches(String regex) {
        return back.matches(regex);
    }

    /**
     * Returns true if and only if this string contains the specified
     * sequence of char values.
     *
     * @param s the sequence to search for
     * @return true if this string contains {@code s}, false otherwise
     */
    public boolean contains(CharSequence s) {
        return back.contains(s);
    }

    /**
     * Replaces the first substring of this string that matches the given <a
     * href="https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#sum">regular expression</a> with the
     * given replacement.
     * <p>
     * An invocation of this method of the form
     * <i>str</i>{@code .replaceFirst(}<i>regex</i>{@code ,} <i>repl</i>{@code )}
     * yields exactly the same result as the expression
     * <blockquote>
     * <code>
     * {@link Pattern}.{@link
     * Pattern#compile compile}(<i>regex</i>).{@link
     * Pattern#matcher(CharSequence) matcher}(<i>str</i>).{@link
     * java.util.regex.Matcher#replaceFirst replaceFirst}(<i>repl</i>)
     * </code>
     * </blockquote>
     * Note that backslashes ({@code \}) and dollar signs ({@code $}) in the
     * replacement string may cause the results to be different than if it were
     * being treated as a literal replacement string; see
     * {@link java.util.regex.Matcher#replaceFirst}.
     * Use {@link java.util.regex.Matcher#quoteReplacement} to suppress the special
     * meaning of these characters, if desired.
     *
     * @param regex       the regular expression to which this string is to be matched
     * @param replacement the string to be substituted for the first match
     * @return The resulting {@code CharSeq}
     * @throws PatternSyntaxException if the regular expression's syntax is invalid
     * @see Pattern
     */
    public CharSeq replaceFirst(String regex, String replacement) {
        return CharSeq.of(back.replaceFirst(regex, replacement));
    }

    /**
     * Replaces each substring of this string that matches the given <a
     * href="https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#sum">regular expression</a> with the
     * given replacement.
     * <p>
     * An invocation of this method of the form
     * <i>str</i>{@code .replaceAll(}<i>regex</i>{@code ,} <i>repl</i>{@code )}
     * yields exactly the same result as the expression
     * <blockquote>
     * <code>
     * {@link Pattern}.{@link
     * Pattern#compile compile}(<i>regex</i>).{@link
     * Pattern#matcher(CharSequence) matcher}(<i>str</i>).{@link
     * java.util.regex.Matcher#replaceAll replaceAll}(<i>repl</i>)
     * </code>
     * </blockquote>
     * Note that backslashes ({@code \}) and dollar signs ({@code $}) in the
     * replacement string may cause the results to be different than if it were
     * being treated as a literal replacement string; see
     * {@link java.util.regex.Matcher#replaceAll Matcher.replaceAll}.
     * Use {@link java.util.regex.Matcher#quoteReplacement} to suppress the special
     * meaning of these characters, if desired.
     *
     * @param regex       the regular expression to which this string is to be matched
     * @param replacement the string to be substituted for each match
     * @return The resulting {@code CharSeq}
     * @throws PatternSyntaxException if the regular expression's syntax is invalid
     * @see Pattern
     */
    public CharSeq replaceAll(String regex, String replacement) {
        return CharSeq.of(back.replaceAll(regex, replacement));
    }

    /**
     * Replaces each substring of this string that matches the literal target
     * sequence with the specified literal replacement sequence. The
     * replacement proceeds from the beginning of the string to the end, for
     * example, replacing "aa" with "b" in the string "aaa" will result in
     * "ba" rather than "ab".
     *
     * @param target      The sequence of char values to be replaced
     * @param replacement The replacement sequence of char values
     * @return The resulting string
     */
    public CharSeq replace(CharSequence target, CharSequence replacement) {
        return CharSeq.of(back.replace(target, replacement));
    }

    /**
     * Splits this string around matches of the given
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#sum">regular expression</a>.
     * <p>
     * This method works as if by invoking the two-argument {@link #split(String, int)}
     * method with the given expression and a limit argument of zero.
     * Trailing empty strings are therefore not included in the resulting {@link Seq}.
     * <p>
     * The string {@code "boo:and:foo"}, for example, yields the following results with these expressions:
     * <blockquote>
     * <table style="border-collapse: collapse; border-spacing: 0">
     * <caption>Split examples showing regex and result</caption>
     * <tr>
     * <th>Regex</th>
     * <th>Result</th>
     * </tr>
     * <tr>
     * <td>:</td>
     * <td>{@code { "boo", "and", "foo" }}</td>
     * </tr>
     * <tr>
     * <td>o</td>
     * <td>{@code { "b", "", ":and:f" }}</td>
     * </tr>
     * </table>
     * </blockquote>
     *
     * @param regex the delimiting regular expression
     * @return the Seq of strings computed by splitting this string around matches of the given regular expression
     * @throws PatternSyntaxException if the regular expression's syntax is invalid
     * @see Pattern
     */
    public Seq<CharSeq> split(String regex) {
        return split(regex, 0);
    }

    /**
     * Splits this string around matches of the given
     * <a href="https://docs.oracle.com/javase/8/docs/api/java/util/regex/Pattern.html#sum">regular expression</a>.
     * <p>
     * The array returned by this method contains each substring of this
     * string that is terminated by another substring that matches the given
     * expression or is terminated by the end of the string.  The substrings in
     * the array are in the order in which they occur in this string.  If the
     * expression does not match any part of the input then the resulting array
     * has just one element, namely this string.
     * <p>
     * When there is a positive-width match at the beginning of this
     * string then an empty leading substring is included at the beginning
     * of the resulting array. A zero-width match at the beginning however
     * never produces such empty leading substring.
     * <p>
     * The {@code limit} parameter controls the number of times the
     * pattern is applied and therefore affects the length of the resulting
     * array.  If the limit <i>n</i> is greater than zero then the pattern
     * will be applied at most <i>n</i>&nbsp;-&nbsp;1 times, the array's
     * length will be no greater than <i>n</i>, and the array's last entry
     * will contain all input beyond the last matched delimiter.  If <i>n</i>
     * is non-positive then the pattern will be applied as many times as
     * possible and the array can have any length.  If <i>n</i> is zero then
     * the pattern will be applied as many times as possible, the array can
     * have any length, and trailing empty strings will be discarded.
     * <p>
     * The string {@code "boo:and:foo"}, for example, yields the
     * following results with these parameters:
     * <blockquote>
     * <table style="border-collapse: collapse; border-spacing: 0">
     * <caption>Split example showing regex, limit, and result</caption>
     * <tr>
     * <th>Regex</th>
     * <th>Limit</th>
     * <th>Result</th>
     * </tr>
     * <tr><td>:</td>
     * <td>2</td>
     * <td>{@code { "boo", "and:foo" }}</td></tr>
     * <tr><td>:</td>
     * <td>5</td>
     * <td>{@code { "boo", "and", "foo" }}</td></tr>
     * <tr><td>:</td>
     * <td>-2</td>
     * <td>{@code { "boo", "and", "foo" }}</td></tr>
     * <tr><td>o</td>
     * <td>5</td>
     * <td>{@code { "b", "", ":and:f", "", "" }}</td></tr>
     * <tr><td>o</td>
     * <td>-2</td>
     * <td>{@code { "b", "", ":and:f", "", "" }}</td></tr>
     * <tr><td>o</td>
     * <td>0</td>
     * <td>{@code { "b", "", ":and:f" }}</td></tr>
     * </table></blockquote>
     * An invocation of this method of the form
     * <i>str.</i>{@code split(}<i>regex</i>{@code ,}&nbsp;<i>n</i>{@code )}
     * yields the same result as the expression
     * <blockquote>
     * <code>
     * {@link Pattern}.{@link
     * Pattern#compile compile}(<i>regex</i>).{@link
     * Pattern#split(CharSequence, int) split}(<i>str</i>,&nbsp;<i>n</i>)
     * </code>
     * </blockquote>
     *
     * @param regex the delimiting regular expression
     * @param limit the result threshold, as described above
     * @return the Seq of strings computed by splitting this string around matches of the given regular expression
     * @throws PatternSyntaxException if the regular expression's syntax is invalid
     * @see Pattern
     */
    public Seq<CharSeq> split(String regex, int limit) {
        final Seq<String> split = Array.wrap(back.split(regex, limit));
        return split.map(CharSeq::of);
    }

    /**
     * Converts all of the characters in this {@code CharSeq} to lower
     * case using the rules of the given {@code Locale}.  Case mapping is based
     * on the Unicode Standard version specified by the {@link Character Character}
     * class. Since case mappings are not always 1:1 char mappings, the resulting
     * {@code CharSeq} may be a different length than the original {@code CharSeq}.
     * <p>
     * Examples of lowercase  mappings are in the following table:
     * <table style="border-collapse: collapse; border-spacing: 0">
     * <caption>Lowercase mapping examples showing language code of locale, upper case, lower case, and description</caption>
     * <tr>
     * <th>Language Code of Locale</th>
     * <th>Upper Case</th>
     * <th>Lower Case</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>tr (Turkish)</td>
     * <td>&#92;u0130</td>
     * <td>&#92;u0069</td>
     * <td>capital letter I with dot above -&gt; small letter i</td>
     * </tr>
     * <tr>
     * <td>tr (Turkish)</td>
     * <td>&#92;u0049</td>
     * <td>&#92;u0131</td>
     * <td>capital letter I -&gt; small letter dotless i </td>
     * </tr>
     * <tr>
     * <td>(all)</td>
     * <td>French Fries</td>
     * <td>french fries</td>
     * <td>lowercased all chars in String</td>
     * </tr>
     * <tr>
     * <td>(all)</td>
     * <td><img src="doc-files/capiota.gif" alt="capiota"><img src="doc-files/capchi.gif" alt="capchi">
     * <img src="doc-files/captheta.gif" alt="captheta"><img src="doc-files/capupsil.gif" alt="capupsil">
     * <img src="doc-files/capsigma.gif" alt="capsigma"></td>
     * <td><img src="doc-files/iota.gif" alt="iota"><img src="doc-files/chi.gif" alt="chi">
     * <img src="doc-files/theta.gif" alt="theta"><img src="doc-files/upsilon.gif" alt="upsilon">
     * <img src="doc-files/sigma1.gif" alt="sigma"></td>
     * <td>lowercased all chars in String</td>
     * </tr>
     * </table>
     *
     * @param locale use the case transformation rules for this locale
     * @return the {@code CharSeq}, converted to lowercase.
     * @see String#toLowerCase()
     * @see String#toUpperCase()
     * @see String#toUpperCase(Locale)
     */
    public CharSeq toLowerCase(Locale locale) {
        return CharSeq.of(back.toLowerCase(locale));
    }

    /**
     * Converts all of the characters in this {@code CharSeq} to lower
     * case using the rules of the default locale. This is equivalent to calling
     * {@code toLowerCase(Locale.getDefault())}.
     * <p>
     * <b>Note:</b> This method is locale sensitive, and may produce unexpected
     * results if used for strings that are intended to be interpreted locale
     * independently.
     * Examples are programming language identifiers, protocol keys, and HTML
     * tags.
     * For instance, {@code "TITLE".toLowerCase()} in a Turkish locale
     * returns {@code "t\u005Cu0131tle"}, where '\u005Cu0131' is the
     * LATIN SMALL LETTER DOTLESS I character.
     * To obtain correct results for locale insensitive strings, use
     * {@code toLowerCase(Locale.ROOT)}.
     * <p>
     *
     * @return the {@code CharSeq}, converted to lowercase.
     * @see String#toLowerCase(Locale)
     */
    public CharSeq toLowerCase() {
        return CharSeq.of(back.toLowerCase(Locale.getDefault()));
    }

    /**
     * Converts all of the characters in this {@code CharSeq} to upper
     * case using the rules of the given {@code Locale}. Case mapping is based
     * on the Unicode Standard version specified by the {@link Character Character}
     * class. Since case mappings are not always 1:1 char mappings, the resulting
     * {@code CharSeq} may be a different length than the original {@code CharSeq}.
     * <p>
     * Examples of locale-sensitive and 1:M case mappings are in the following table.
     *
     * <table style="border-collapse: collapse; border-spacing: 0">
     * <caption>Examples of locale-sensitive and 1:M case mappings. Shows Language code of locale, lower case, upper case, and description.</caption>
     * <tr>
     * <th>Language Code of Locale</th>
     * <th>Lower Case</th>
     * <th>Upper Case</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>tr (Turkish)</td>
     * <td>&#92;u0069</td>
     * <td>&#92;u0130</td>
     * <td>small letter i -&gt; capital letter I with dot above</td>
     * </tr>
     * <tr>
     * <td>tr (Turkish)</td>
     * <td>&#92;u0131</td>
     * <td>&#92;u0049</td>
     * <td>small letter dotless i -&gt; capital letter I</td>
     * </tr>
     * <tr>
     * <td>(all)</td>
     * <td>&#92;u00df</td>
     * <td>&#92;u0053 &#92;u0053</td>
     * <td>small letter sharp s -&gt; two letters: SS</td>
     * </tr>
     * <tr>
     * <td>(all)</td>
     * <td>Fahrvergn&uuml;gen</td>
     * <td>FAHRVERGN&Uuml;GEN</td>
     * <td></td>
     * </tr>
     * </table>
     *
     * @param locale use the case transformation rules for this locale
     * @return the {@code CharSeq}, converted to uppercase.
     * @see String#toUpperCase()
     * @see String#toLowerCase()
     * @see String#toLowerCase(Locale)
     */
    public CharSeq toUpperCase(Locale locale) {
        return CharSeq.of(back.toUpperCase(locale));
    }

    /**
     * Converts all of the characters in this {@code CharSeq} to upper
     * case using the rules of the default locale. This method is equivalent to
     * {@code toUpperCase(Locale.getDefault())}.
     * <p>
     * <b>Note:</b> This method is locale sensitive, and may produce unexpected
     * results if used for strings that are intended to be interpreted locale
     * independently.
     * Examples are programming language identifiers, protocol keys, and HTML
     * tags.
     * For instance, {@code "title".toUpperCase()} in a Turkish locale
     * returns {@code "T\u005Cu0130TLE"}, where '\u005Cu0130' is the
     * LATIN CAPITAL LETTER I WITH DOT ABOVE character.
     * To obtain correct results for locale insensitive strings, use
     * {@code toUpperCase(Locale.ROOT)}.
     * <p>
     *
     * @return the {@code CharSeq}, converted to uppercase.
     * @see String#toUpperCase(Locale)
     */
    public CharSeq toUpperCase() {
        return CharSeq.of(back.toUpperCase(Locale.getDefault()));
    }

    /**
     * Converts the first character in this {@code CharSeq} to upper
     * case using the rules of the given {@code Locale}. If the {@code CharSeq} is
     * empty, it won't have any effect. Case mapping is based
     * on the Unicode Standard version specified by the {@link Character Character}
     * class. Since case mappings are not always 1:1 char mappings, the resulting
     * {@code CharSeq} may be a different length than the original {@code CharSeq}.
     * <p>
     * Examples of locale-sensitive and 1:M case mappings are in the following table.
     *
     * <table style="border-collapse: collapse; border-spacing: 0">
     * <caption>Examples of locale-sensitive and 1:M case mappings. Shows Language code of locale, lower case, upper case, and description.</caption>
     * <tr>
     * <th>Language Code of Locale</th>
     * <th>Lower Case</th>
     * <th>Upper Case</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>tr (Turkish)</td>
     * <td>&#92;u0069</td>
     * <td>&#92;u0130</td>
     * <td>small letter i -&gt; capital letter I with dot above</td>
     * </tr>
     * <tr>
     * <td>tr (Turkish)</td>
     * <td>&#92;u0131</td>
     * <td>&#92;u0049</td>
     * <td>small letter dotless i -&gt; capital letter I</td>
     * </tr>
     * <tr>
     * <td>(all)</td>
     * <td>&#92;u00df</td>
     * <td>&#92;u0053 &#92;u0053</td>
     * <td>small letter sharp s -&gt; two letters: SS</td>
     * </tr>
     * <tr>
     * <td>(all)</td>
     * <td>Fahrvergn&uuml;gen</td>
     * <td>FAHRVERGN&Uuml;GEN</td>
     * <td></td>
     * </tr>
     * </table>
     *
     * @param locale use the case transformation rules for this locale
     * @return the {@code CharSeq}, capitalized.
     */
    public CharSeq capitalize(Locale locale) {
        if (back.isEmpty()) {
            return this;
        }
        return CharSeq.of(back.substring(0, 1).toUpperCase(locale) + back.substring(1));
    }

    /**
     * Converts the first character in this {@code CharSeq} to upper
     * case using the rules of the default locale. If the {@code CharSeq} is
     * empty, it won't have any effect. This method is equivalent to
     * {@code capitalize(Locale.getDefault())}.
     * <p>
     * <b>Note:</b> This method is locale sensitive, and may produce unexpected
     * results if used for strings that are intended to be interpreted locale
     * independently.
     * Examples are programming language identifiers, protocol keys, and HTML
     * tags.
     * For instance, {@code "title".toUpperCase()} in a Turkish locale
     * returns {@code "T\u005Cu0130TLE"}, where '\u005Cu0130' is the
     * LATIN CAPITAL LETTER I WITH DOT ABOVE character.
     * To obtain correct results for locale insensitive strings, use
     * {@code toUpperCase(Locale.ROOT)}.
     * <p>
     *
     * @return the {@code CharSeq}, capitalized.
     */
    public CharSeq capitalize() {
        return capitalize(Locale.getDefault());
    }

    /**
     * Returns a string whose value is this string, with any leading and trailing
     * whitespace removed.
     * <p>
     * If this {@code CharSeq} object represents an empty character
     * sequence, or the first and last characters of character sequence
     * represented by this {@code CharSeq} object both have codes
     * greater than {@code '\u005Cu0020'} (the space character), then a
     * reference to this {@code CharSeq} object is returned.
     * <p>
     * Otherwise, if there is no character with a code greater than
     * {@code '\u005Cu0020'} in the string, then a
     * {@code CharSeq} object representing an empty string is
     * returned.
     * <p>
     * Otherwise, let <i>k</i> be the index of the first character in the
     * string whose code is greater than {@code '\u005Cu0020'}, and let
     * <i>m</i> be the index of the last character in the string whose code
     * is greater than {@code '\u005Cu0020'}. A {@code CharSeq}
     * object is returned, representing the substring of this string that
     * begins with the character at index <i>k</i> and ends with the
     * character at index <i>m</i>-that is, the result of
     * {@code this.substring(k, m + 1)}.
     * <p>
     * This method may be used to trim whitespace (as defined above) from
     * the beginning and end of a string.
     *
     * @return A string whose value is this string, with any leading and trailing white
     * space removed, or this string if it has no leading or
     * trailing white space.
     */
    public CharSeq trim() {
        return of(back.trim());
    }

    /**
     * Converts this string to a new character array.
     *
     * @return a newly allocated character array whose length is the length
     * of this string and whose contents are initialized to contain
     * the character sequence represented by this string.
     */
    public char[] toCharArray() {
        return back.toCharArray();
    }

    // -- number conversion

    /**
     * Decodes this {@code CharSeq} into a {@code Byte} by calling {@link Byte#decode(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * Byte value = charSeq.decodeByte();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * Byte value = Byte.decode(charSeq.mkString());
     * </code></pre>
     *
     * @return a {@code Byte} object holding the byte value represented by this {@code CharSeq}
     * @throws NumberFormatException if this {@code CharSeq} does not contain a parsable byte.
     */
    public Byte decodeByte() {
        return Byte.decode(back);
    }

    /**
     * Decodes this {@code CharSeq} into an {@code Integer} by calling {@link Integer#decode(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * Integer value = charSeq.decodeInteger();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * Integer value = Integer.decode(charSeq.mkString());
     * </code></pre>
     *
     * @return an {@code Integer} object holding the int value represented by this {@code CharSeq}
     * @throws NumberFormatException if this {@code CharSeq} does not contain a parsable int.
     */
    public Integer decodeInteger() {
        return Integer.decode(back);
    }

    /**
     * Decodes this {@code CharSeq} into a {@code Long} by calling {@link Long#decode(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * Long value = charSeq.decodeLong();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * Long value = Long.decode(charSeq.mkString());
     * </code></pre>
     *
     * @return a {@code Long} object holding the long value represented by this {@code CharSeq}
     * @throws NumberFormatException if this {@code CharSeq} does not contain a parsable long.
     */
    public Long decodeLong() {
        return Long.decode(back);
    }

    /**
     * Decodes this {@code CharSeq} into a {@code Short} by calling {@link Short#decode(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * Short value = charSeq.decodeShort();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * Short value = Short.decode(charSeq.mkString());
     * </code></pre>
     *
     * @return a {@code Short} object holding the short value represented by this {@code CharSeq}
     * @throws NumberFormatException if this {@code CharSeq} does not contain a parsable short.
     */
    public Short decodeShort() {
        return Short.decode(back);
    }

    /**
     * Parses this {@code CharSeq} as a boolean by calling {@link Boolean#parseBoolean(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * boolean value = charSeq.parseBoolean();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * boolean value = Boolean.parseBoolean(charSeq.mkString());
     * </code></pre>
     *
     * @return the boolean represented by this {@code CharSeq}
     */
    public boolean parseBoolean() {
        return Boolean.parseBoolean(back);
    }

    /**
     * Parses this {@code CharSeq} as a signed decimal byte by calling {@link Byte#parseByte(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * byte value = charSeq.parseByte();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * byte value = Byte.parseByte(charSeq.mkString());
     * </code></pre>
     *
     * @return the byte value represented by this {@code CharSeq} in decimal
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable byte.
     */
    public byte parseByte() {
        return Byte.parseByte(back);
    }

    /**
     * Parses this {@code CharSeq} as a signed byte in the specified radix
     * by calling {@link Byte#parseByte(String, int)}.
     * <p>
     * We write
     *
     * <pre><code>
     * byte value = charSeq.parseByte(radix);
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * byte value = Byte.parseByte(charSeq.mkString(), radix);
     * </code></pre>
     *
     * @param radix the radix to be used in interpreting this {@code CharSeq}
     * @return the byte value represented by this {@code CharSeq} in the specified radix
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable byte.
     */
    public byte parseByte(int radix) {
        return Byte.parseByte(back, radix);
    }

    /**
     * Parses this {@code CharSeq} as a double by calling {@link Double#parseDouble(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * double value = charSeq.parseDouble();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * double value = Double.parseDouble(charSeq.mkString());
     * </code></pre>
     *
     * @return the double value represented by this {@code CharSeq}
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable double.
     */
    public double parseDouble() {
        return Double.parseDouble(back);
    }

    /**
     * Parses this {@code CharSeq} as a float by calling {@link Float#parseFloat(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * float value = charSeq.parseFloat();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * float value = Double.parseFloat(charSeq.mkString());
     * </code></pre>
     *
     * @return the float value represented by this {@code CharSeq}
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable float.
     */
    public float parseFloat() {
        return Float.parseFloat(back);
    }

    /**
     * Parses this {@code CharSeq} as a signed decimal int by calling {@link Integer#parseInt(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * int value = charSeq.parseInt();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * int value = Integer.parseInt(charSeq.mkString());
     * </code></pre>
     *
     * @return the int value represented by this {@code CharSeq} in decimal
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable int.
     */
    public int parseInt() {
        return Integer.parseInt(back);
    }

    /**
     * Parses this {@code CharSeq} as a signed int in the specified radix
     * by calling {@link Integer#parseInt(String, int)}.
     * <p>
     * We write
     *
     * <pre><code>
     * int value = charSeq.parseInt(radix);
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * int value = Integer.parseInt(charSeq.mkString(), radix);
     * </code></pre>
     *
     * @param radix the radix to be used in interpreting this {@code CharSeq}
     * @return the int value represented by this {@code CharSeq} in the specified radix
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable int.
     */
    public int parseInt(int radix) {
        return Integer.parseInt(back, radix);
    }

    /**
     * Parses this {@code CharSeq} as a unsigned decimal int by calling {@link Integer#parseUnsignedInt(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * int value = charSeq.parseUnsignedInt();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * int value = Integer.parseUnsignedInt(charSeq.mkString());
     * </code></pre>
     *
     * @return the unsigned int value represented by this {@code CharSeq} in decimal
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable unsigned int.
     */
    public int parseUnsignedInt() {
        return Integer.parseUnsignedInt(back);
    }

    /**
     * Parses this {@code CharSeq} as a unsigned int in the specified radix
     * by calling {@link Integer#parseUnsignedInt(String, int)}.
     * <p>
     * We write
     *
     * <pre><code>
     * int value = charSeq.parseUnsignedInt(radix);
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * int value = Integer.parseUnsignedInt(charSeq.mkString(), radix);
     * </code></pre>
     *
     * @param radix the radix to be used in interpreting this {@code CharSeq}
     * @return the unsigned int value represented by this {@code CharSeq} in the specified radix
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable unsigned int.
     */
    public int parseUnsignedInt(int radix) {
        return Integer.parseUnsignedInt(back, radix);
    }

    /**
     * Parses this {@code CharSeq} as a signed decimal long by calling {@link Long#parseLong(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * long value = charSeq.parseLong();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * long value = Long.parseLong(charSeq.mkString());
     * </code></pre>
     *
     * @return the long value represented by this {@code CharSeq} in decimal
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable long.
     */
    public long parseLong() {
        return Long.parseLong(back);
    }

    /**
     * Parses this {@code CharSeq} as a signed long in the specified radix
     * by calling {@link Long#parseLong(String, int)}.
     * <p>
     * We write
     *
     * <pre><code>
     * long value = charSeq.parseLong(radix);
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * long value = Long.parseLong(charSeq.mkString(), radix);
     * </code></pre>
     *
     * @param radix the radix to be used in interpreting this {@code CharSeq}
     * @return the long value represented by this {@code CharSeq} in the specified radix
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable long.
     */
    public long parseLong(int radix) {
        return Long.parseLong(back, radix);
    }

    /**
     * Parses this {@code CharSeq} as a unsigned decimal long by calling {@link Long#parseUnsignedLong(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * long value = charSeq.parseUnsignedLong();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * long value = Long.parseUnsignedLong(charSeq.mkString());
     * </code></pre>
     *
     * @return the unsigned long value represented by this {@code CharSeq} in decimal
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable unsigned long.
     */
    public long parseUnsignedLong() {
        return Long.parseUnsignedLong(back);
    }

    /**
     * Parses this {@code CharSeq} as a unsigned long in the specified radix
     * by calling {@link Long#parseUnsignedLong(String, int)}.
     * <p>
     * We write
     *
     * <pre><code>
     * long value = charSeq.parseUnsignedLong(radix);
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * long value = Long.parseUnsignedLong(charSeq.mkString(), radix);
     * </code></pre>
     *
     * @param radix the radix to be used in interpreting this {@code CharSeq}
     * @return the unsigned long value represented by this {@code CharSeq} in the specified radix
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable unsigned long.
     */
    public long parseUnsignedLong(int radix) {
        return Long.parseUnsignedLong(back, radix);
    }

    /**
     * Parses this {@code CharSeq} as a signed decimal short by calling {@link Short#parseShort(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * short value = charSeq.parseShort();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * short value = Short.parseShort(charSeq.mkString());
     * </code></pre>
     *
     * @return the short value represented by this {@code CharSeq} in decimal
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable short.
     */
    public short parseShort() {
        return Short.parseShort(back);
    }

    /**
     * Parses this {@code CharSeq} as a signed short in the specified radix
     * by calling {@link Short#parseShort(String, int)}.
     * <p>
     * We write
     *
     * <pre><code>
     * short value = charSeq.parseShort(radix);
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * short value = Short.parseShort(charSeq.mkString(), radix);
     * </code></pre>
     *
     * @param radix the radix to be used in interpreting this {@code CharSeq}
     * @return the short value represented by this {@code CharSeq} in the specified radix
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable short.
     */
    public short parseShort(int radix) {
        return Short.parseShort(back, radix);
    }

    /**
     * Converts this {@code CharSeq} to a {@code Boolean} by calling {@link Boolean#valueOf(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * Boolean value = charSeq.toBoolean();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * Boolean value = Boolean.valueOf(charSeq.mkString());
     * </code></pre>
     *
     * @return the {@code Boolean} value represented by this {@code CharSeq}
     */
    public Boolean toBoolean() {
        return Boolean.valueOf(back);
    }

    /**
     * Converts this {@code CharSeq} to a {@code Byte} by calling {@link Byte#valueOf(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * Byte value = charSeq.toByte();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * Byte value = Byte.valueOf(charSeq.mkString());
     * </code></pre>
     *
     * @return a {@code Byte} object holding the value represented by this {@code CharSeq}
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable byte.
     */
    public Byte toByte() {
        return Byte.valueOf(back);
    }

    /**
     * Converts this {@code CharSeq} to a {@code Byte} in the specified radix
     * by calling {@link Byte#valueOf(String, int)}.
     * <p>
     * We write
     *
     * <pre><code>
     * Byte value = charSeq.toByte(radix);
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * Byte value = Byte.valueOf(charSeq.mkString(), radix);
     * </code></pre>
     *
     * @param radix the radix to be used in interpreting this char sequence
     * @return a {@code Byte} object holding the value represented by this {@code CharSeq}
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable byte.
     */
    public Byte toByte(int radix) {
        return Byte.valueOf(back, radix);
    }

    /**
     * Converts this {@code CharSeq} to a {@code Double} by calling {@link Double#valueOf(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * Double value = charSeq.toDouble();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * Double value = Double.valueOf(charSeq.mkString());
     * </code></pre>
     *
     * @return a {@code Double} object holding the value represented by this {@code CharSeq}
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable double.
     */
    public Double toDouble() {
        return Double.valueOf(back);
    }

    /**
     * Converts this {@code CharSeq} to a {@code Float} by calling {@link Float#valueOf(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * Float value = charSeq.toFloat();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * Float value = Float.valueOf(charSeq.mkString());
     * </code></pre>
     *
     * @return a {@code Float} object holding the value represented by this {@code CharSeq}
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable float.
     */
    public Float toFloat() {
        return Float.valueOf(back);
    }

    /**
     * Converts this {@code CharSeq} to an {@code Integer} by calling {@link Integer#valueOf(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * Integer value = charSeq.toInteger();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * Integer value = Integer.valueOf(charSeq.mkString());
     * </code></pre>
     *
     * @return an {@code Integer} object holding the value represented by this {@code CharSeq}
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable int.
     */
    public Integer toInteger() {
        return Integer.valueOf(back);
    }

    /**
     * Converts this {@code CharSeq} to an {@code Integer} in the specified radix
     * by calling {@link Integer#valueOf(String, int)}.
     * <p>
     * We write
     *
     * <pre><code>
     * Integer value = charSeq.toInteger(radix);
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * Integer value = Integer.valueOf(charSeq.mkString(), radix);
     * </code></pre>
     *
     * @param radix the radix to be used in interpreting this char sequence
     * @return an {@code Integer} object holding the value represented by this {@code CharSeq}
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable int.
     */
    public Integer toInteger(int radix) {
        return Integer.valueOf(back, radix);
    }

    /**
     * Converts this {@code CharSeq} to a {@code Long} by calling {@link Long#valueOf(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * Long value = charSeq.toLong();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * Long value = Long.valueOf(charSeq.mkString());
     * </code></pre>
     *
     * @return a {@code Long} object holding the value represented by this {@code CharSeq}
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable long.
     */
    public Long toLong() {
        return Long.valueOf(back);
    }

    /**
     * Converts this {@code CharSeq} to a {@code Long} in the specified radix
     * by calling {@link Long#valueOf(String, int)}.
     * <p>
     * We write
     *
     * <pre><code>
     * Long value = charSeq.toLong(radix);
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * Long value = Long.valueOf(charSeq.mkString(), radix);
     * </code></pre>
     *
     * @param radix the radix to be used in interpreting this char sequence
     * @return a {@code Long} object holding the value represented by this {@code CharSeq}
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable long.
     */
    public Long toLong(int radix) {
        return Long.valueOf(back, radix);
    }

    /**
     * Converts this {@code CharSeq} to a {@code Short} by calling {@link Short#valueOf(String)}.
     * <p>
     * We write
     *
     * <pre><code>
     * Short value = charSeq.toShort();
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * Short value = Short.valueOf(charSeq.mkString());
     * </code></pre>
     *
     * @return a {@code Short} object holding the value represented by this {@code CharSeq}
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable short.
     */
    public Short toShort() {
        return Short.valueOf(back);
    }

    /**
     * Converts this {@code CharSeq} to a {@code Short} in the specified radix
     * by calling {@link Short#valueOf(String, int)}.
     * <p>
     * We write
     *
     * <pre><code>
     * Short value = charSeq.toShort(radix);
     * </code></pre>
     *
     * instead of
     *
     * <pre><code>
     * Short value = Short.valueOf(charSeq.mkString(), radix);
     * </code></pre>
     *
     * @param radix the radix to be used in interpreting this char sequence
     * @return a {@code Short} object holding the value represented by this {@code CharSeq}
     * @throws NumberFormatException If this {@code CharSeq} does not contain a parsable short.
     */
    public Short toShort(int radix) {
        return Short.valueOf(back, radix);
    }

    // -- conversion overrides

    @Override
    public Character[] toJavaArray() {
        return toJavaList().toArray(new Character[0]);
    }

    // -- functional interfaces

    @FunctionalInterface
    public interface CharUnaryOperator {
        char apply(char c);
    }

    @FunctionalInterface
    public interface CharFunction<R> {
        R apply(char c);
    }
}

interface CharSeqModule {
    interface Combinations {
        static IndexedSeq<CharSeq> apply(CharSeq elements, int k) {
            if (k == 0) {
                return Vector.of(CharSeq.empty());
            } else {
                return elements.zipWithIndex().flatMap(
                        t -> apply(elements.drop(t._2 + 1), (k - 1)).map((CharSeq c) -> c.prepend(t._1))
                );
            }
        }
    }
}
