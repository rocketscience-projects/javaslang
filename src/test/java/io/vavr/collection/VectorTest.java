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

import io.vavr.Serializables;
import io.vavr.Tuple2;
import io.vavr.collection.JavaConverters.ChangePolicy;
import io.vavr.collection.JavaConverters.ListView;
import io.vavr.control.Option;
import org.junit.Test;

import java.io.InvalidObjectException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class VectorTest extends AbstractIndexedSeqTest {

    @Override
    protected String stringPrefix() {
        return "Vector";
    }

    @Override
    protected <T> Collector<T, ArrayList<T>, Vector<T>> collector() {
        return Vector.collector();
    }

    @Override
    protected <T> Vector<T> empty() {
        return Vector.empty();
    }

    @Override
    protected <T> Vector<T> of(T element) {
        return Vector.of(element);
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    @Override
    protected final <T> Vector<T> of(T... elements) {
        return Vector.of(elements);
    }

    @Override
    protected <T> Vector<T> ofAll(Iterable<? extends T> elements) {
        return Vector.ofAll(elements);
    }

    @Override
    protected <T extends Comparable<? super T>> Vector<T> ofJavaStream(java.util.stream.Stream<? extends T> javaStream) {
        return Vector.ofAll(javaStream);
    }

    @Override
    protected Vector<Boolean> ofAll(boolean... elements) {
        return Vector.ofAll(elements);
    }

    @Override
    protected Vector<Byte> ofAll(byte... elements) {
        return Vector.ofAll(elements);
    }

    @Override
    protected Vector<Character> ofAll(char... elements) {
        return Vector.ofAll(elements);
    }

    @Override
    protected Vector<Double> ofAll(double... elements) {
        return Vector.ofAll(elements);
    }

    @Override
    protected Vector<Float> ofAll(float... elements) {
        return Vector.ofAll(elements);
    }

    @Override
    protected Vector<Integer> ofAll(int... elements) {
        return Vector.ofAll(elements);
    }

    @Override
    protected Vector<Long> ofAll(long... elements) {
        return Vector.ofAll(elements);
    }

    @Override
    protected Vector<Short> ofAll(short... elements) {
        return Vector.ofAll(elements);
    }

    @Override
    protected <T> Vector<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        return Vector.tabulate(n, f);
    }

    @Override
    protected <T> Vector<T> fill(int n, Supplier<? extends T> s) {
        return Vector.fill(n, s);
    }

    @Override
    protected <T> Traversable<T> fill(int n, T element) {
        return Vector.fill(n, element);
    }

    @Override
    protected Vector<Character> range(char from, char toExclusive) {
        return Vector.range(from, toExclusive);
    }

    @Override
    protected Vector<Character> rangeBy(char from, char toExclusive, int step) {
        return Vector.rangeBy(from, toExclusive, step);
    }

    @Override
    protected Vector<Double> rangeBy(double from, double toExclusive, double step) {
        return Vector.rangeBy(from, toExclusive, step);
    }

    @Override
    protected int getPeekNonNilPerformingAnAction() {
        return 1;
    }

    //fixme: delete, when useIsEqualToInsteadOfIsSameAs() will be eliminated from AbstractValueTest class
    @Override
    protected boolean useIsEqualToInsteadOfIsSameAs() {
        return false;
    }

    @Override
    protected Vector<Integer> range(int from, int toExclusive) {
        return Vector.range(from, toExclusive);
    }

    @Override
    protected Vector<Integer> rangeBy(int from, int toExclusive, int step) {
        return Vector.rangeBy(from, toExclusive, step);
    }

    @Override
    protected Vector<Long> range(long from, long toExclusive) {
        return Vector.range(from, toExclusive);
    }

    @Override
    protected Vector<Long> rangeBy(long from, long toExclusive, long step) {
        return Vector.rangeBy(from, toExclusive, step);
    }

    @Override
    protected Vector<Character> rangeClosed(char from, char toInclusive) {
        return Vector.rangeClosed(from, toInclusive);
    }

    @Override
    protected Vector<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return Vector.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected Vector<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return Vector.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected Vector<Integer> rangeClosed(int from, int toInclusive) {
        return Vector.rangeClosed(from, toInclusive);
    }

    @Override
    protected Vector<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return Vector.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected Vector<Long> rangeClosed(long from, long toInclusive) {
        return Vector.rangeClosed(from, toInclusive);
    }

    @Override
    protected Vector<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return Vector.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> Vector<Vector<T>> transpose(Seq<? extends Seq<T>> rows) {
        return Vector.transpose((Vector<Vector<T>>) rows);
    }

    // -- static narrow

    @Test
    public void shouldNarrowVector() {
        final Vector<Double> doubles = of(1.0d);
        final Vector<Number> numbers = Vector.narrow(doubles);
        final int actual = numbers.append(new BigDecimal("2.0")).sum().intValue();
        assertThat(actual).isEqualTo(3);
    }

    // -- static ofAll

    @Test
    public void shouldReturnSelfWhenIterableIsInstanceOfVector() {
        final Vector<Integer> source = ofAll(1, 2, 3);
        final Vector<Integer> target = Vector.ofAll(source);
        assertThat(target).isSameAs(source);
    }

    @Test
    public void shouldReturnSelfWhenIterableIsInstanceOfListView() {
        final ListView<Integer, Vector<Integer>> source = JavaConverters
                .asJava(ofAll(1, 2, 3), ChangePolicy.IMMUTABLE);
        final Vector<Integer> target = Vector.ofAll(source);
        assertThat(target).isSameAs(source.getDelegate());
    }

    // -- partition

    @Test
    public void shouldPartitionInOneIteration() {
        final AtomicInteger count = new AtomicInteger(0);
        final Vector<Integer> values = ofAll(1, 2, 3);
        final Tuple2<Vector<Integer>, Vector<Integer>> results = values.partition(v -> {
            count.incrementAndGet();
            return true;
        });
        assertThat(results._1).isEqualTo(ofAll(1, 2, 3));
        assertThat(results._2).isEmpty();
        assertThat(count.get()).isEqualTo(3);
    }

    // -- primitives

    @Test
    public void shouldAddNullToPrimitiveVector() {
        final Vector<Integer> primitives = rangeClosed(0, 2);

        assertThat(primitives.append(null)).isEqualTo(of(0, 1, 2, null));
        assertThat(primitives.prepend(null)).isEqualTo(of(null, 0, 1, 2));
        assertThat(primitives.update(1, (Integer) null)).isEqualTo(of(0, null, 2));
    }

    @Test
    public void shouldAddObjectToPrimitiveVector() {
        final String object = "String";
        final Vector<Object> primitives = Vector.narrow(rangeClosed(0, 2));

        assertThat(primitives.append(object)).isEqualTo(of(0, 1, 2, object));
        assertThat(primitives.prepend(object)).isEqualTo(of(object, 0, 1, 2));
        assertThat(primitives.update(1, object)).isEqualTo(of(0, object, 2));
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowForVoidType() {
        ArrayType.of(void.class);
    }

    // -- transform()

    @Test
    public void shouldTransform() {
        final String transformed = of(42).transform(v -> String.valueOf(v.head()));
        assertThat(transformed).isEqualTo("42");
    }

    // -- unfold

    @Test
    public void shouldUnfoldRightToEmpty() {
        assertThat(Vector.unfoldRight(0, x -> Option.none())).isEqualTo(empty());
    }

    @Test
    public void shouldUnfoldRightSimpleVector() {
        assertThat(
                Vector.unfoldRight(10, x -> x == 0
                                            ? Option.none()
                                            : Option.of(new Tuple2<>(x, x - 1))))
                .isEqualTo(of(10, 9, 8, 7, 6, 5, 4, 3, 2, 1));
    }

    @Test
    public void shouldUnfoldLeftToEmpty() {
        assertThat(Vector.unfoldLeft(0, x -> Option.none())).isEqualTo(empty());
    }

    @Test
    public void shouldUnfoldLeftSimpleVector() {
        assertThat(
                Vector.unfoldLeft(10, x -> x == 0
                                           ? Option.none()
                                           : Option.of(new Tuple2<>(x - 1, x))))
                .isEqualTo(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void shouldUnfoldToEmpty() {
        assertThat(Vector.unfold(0, x -> Option.none())).isEqualTo(empty());
    }

    @Test
    public void shouldUnfoldSimpleVector() {
        assertThat(
                Vector.unfold(10, x -> x == 0
                                       ? Option.none()
                                       : Option.of(new Tuple2<>(x - 1, x))))
                .isEqualTo(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    // -- dropRightWhile

    @Test
    public void shouldDropRightWhileNoneOnNil() {
        assertThat(empty().dropRightWhile(ignored -> true)).isEqualTo(empty());
    }

    @Test
    public void shouldDropRightWhileNoneIfPredicateIsFalse() {
        assertThat(of(1, 2, 3).dropRightWhile(ignored -> false)).isEqualTo(of(1, 2, 3));
    }

    @Test
    public void shouldDropRightWhileAllIfPredicateIsTrue() {
        assertThat(of(1, 2, 3).dropRightWhile(ignored -> true)).isEqualTo(empty());
    }

    @Test
    public void shouldDropRightWhileCorrect() {
        assertThat(ofAll("abc  ".toCharArray()).dropRightWhile(Character::isWhitespace)).isEqualTo(ofAll("abc".toCharArray()));
    }

    // -- toString

    @Test
    public void shouldStringifyNil() {
        assertThat(empty().toString()).isEqualTo("Vector()");
    }

    @Test
    public void shouldStringifyNonNil() {
        assertThat(of(null, 1, 2, 3).toString()).isEqualTo("Vector(null, 1, 2, 3)");
    }

    // -- Cons test

    @Test(expected = InvalidObjectException.class)
    public void shouldNotSerializeEnclosingClass() throws Throwable {
        Serializables.callReadObject(List.of(1));
    }

    // -- toVector

    @Test
    public void shouldReturnSelfOnConvertToVector() {
        final Vector<Integer> value = of(1, 2, 3);
        assertThat(value.toVector()).isSameAs(value);
    }
}
