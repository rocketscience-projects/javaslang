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

import io.vavr.Tuple2;
import io.vavr.collection.JavaConverters.ChangePolicy;
import io.vavr.collection.JavaConverters.ListView;
import io.vavr.control.Option;
import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ArrayTest extends AbstractIndexedSeqTest {

    @Override
    protected String stringPrefix() {
        return "Array";
    }

    @Override
    protected <T> Collector<T, ArrayList<T>, ? extends Seq<T>> collector() {
        return Array.collector();
    }

    @Override
    protected <T> Array<T> empty() {
        return Array.empty();
    }

    @Override
    protected <T> Array<T> of(T element) {
        return Array.of(element);
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    @Override
    protected final <T> Array<T> of(T... elements) {
        return Array.of(elements);
    }

    @Override
    protected <T> Array<T> ofAll(Iterable<? extends T> elements) {
        return Array.ofAll(elements);
    }

    @Override
    protected <T extends Comparable<? super T>> Array<T> ofJavaStream(java.util.stream.Stream<? extends T> javaStream) {
        return Array.ofAll(javaStream);
    }

    @Override
    protected Array<Boolean> ofAll(boolean... elements) {
        return Array.ofAll(elements);
    }

    @Override
    protected Array<Byte> ofAll(byte... elements) {
        return Array.ofAll(elements);
    }

    @Override
    protected Array<Character> ofAll(char... elements) {
        return Array.ofAll(elements);
    }

    @Override
    protected Array<Double> ofAll(double... elements) {
        return Array.ofAll(elements);
    }

    @Override
    protected Array<Float> ofAll(float... elements) {
        return Array.ofAll(elements);
    }

    @Override
    protected Array<Integer> ofAll(int... elements) {
        return Array.ofAll(elements);
    }

    @Override
    protected Array<Long> ofAll(long... elements) {
        return Array.ofAll(elements);
    }

    @Override
    protected Array<Short> ofAll(short... elements) {
        return Array.ofAll(elements);
    }

    @Override
    protected <T> Array<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        return Array.tabulate(n, f);
    }

    @Override
    protected <T> Array<T> fill(int n, Supplier<? extends T> s) {
        return Array.fill(n, s);
    }

    @Override
    protected <T> Traversable<T> fill(int n, T element) {
        return Array.fill(n, element);
    }

    @Override
    protected Array<Character> range(char from, char toExclusive) {
        return Array.range(from, toExclusive);
    }

    @Override
    protected Array<Character> rangeBy(char from, char toExclusive, int step) {
        return Array.rangeBy(from, toExclusive, step);
    }

    @Override
    protected Array<Double> rangeBy(double from, double toExclusive, double step) {
        return Array.rangeBy(from, toExclusive, step);
    }

    @Override
    protected Array<Integer> range(int from, int toExclusive) {
        return Array.range(from, toExclusive);
    }

    @Override
    protected Array<Integer> rangeBy(int from, int toExclusive, int step) {
        return Array.rangeBy(from, toExclusive, step);
    }

    @Override
    protected Array<Long> range(long from, long toExclusive) {
        return Array.range(from, toExclusive);
    }

    @Override
    protected Array<Long> rangeBy(long from, long toExclusive, long step) {
        return Array.rangeBy(from, toExclusive, step);
    }

    @Override
    protected Array<Character> rangeClosed(char from, char toInclusive) {
        return Array.rangeClosed(from, toInclusive);
    }

    @Override
    protected Array<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return Array.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected Array<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return Array.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected Array<Integer> rangeClosed(int from, int toInclusive) {
        return Array.rangeClosed(from, toInclusive);
    }

    @Override
    protected Array<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return Array.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected Array<Long> rangeClosed(long from, long toInclusive) {
        return Array.rangeClosed(from, toInclusive);
    }

    @Override
    protected Array<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return Array.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> Array<Array<T>> transpose(Seq<? extends Seq<T>> rows) {
        return Array.transpose((Array<Array<T>>) rows);
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

    // -- static narrow

    @Test
    public void shouldNarrowArray() {
        final Array<Double> doubles = of(1.0d);
        final Array<Number> numbers = Array.narrow(doubles);
        final int actual = numbers.append(new BigDecimal("2.0")).sum().intValue();
        assertThat(actual).isEqualTo(3);
    }

    // -- static ofAll

    @Test
    public void shouldReturnSelfWhenIterableIsInstanceOfArray() {
        final Array<Integer> source = ofAll(1, 2, 3);
        final Array<Integer> target = Array.ofAll(source);
        assertThat(target).isSameAs(source);
    }

    @Test
    public void shouldReturnSelfWhenIterableIsInstanceOfListView() {
        final ListView<Integer, Array<Integer>> source = JavaConverters
                .asJava(ofAll(1, 2, 3), ChangePolicy.IMMUTABLE);
        final Array<Integer> target = Array.ofAll(source);
        assertThat(target).isSameAs(source.getDelegate());
    }

    // -- get()

    @Test
    public void shouldThrowExceptionWhenGetIndexEqualToLength() {
        final Array<Integer> array = of(1);
        Assertions.assertThatThrownBy(() -> array.get(1))
            .isInstanceOf(IndexOutOfBoundsException.class).hasMessage("get(1)");
    }

    // -- partition

    @Test
    public void shouldPartitionInOneIteration() {
        final AtomicInteger count = new AtomicInteger(0);
        final Tuple2<Array<Integer>, Array<Integer>> results = of(1, 2, 3).partition(i -> {
            count.incrementAndGet();
            return true;
        });
        assertThat(results._1).isEqualTo(of(1, 2, 3));
        assertThat(results._2).isEqualTo(of());
        assertThat(count.get()).isEqualTo(3);
    }

    // -- transform()

    @Test
    public void shouldTransform() {
        String transformed = of(42).transform(v -> String.valueOf(v.head()));
        assertThat(transformed).isEqualTo("42");
    }

    // -- unfold

    @Test
    public void shouldUnfoldRightToEmpty() {
        assertThat(Array.unfoldRight(0, x -> Option.none())).isEqualTo(empty());
    }

    @Test
    public void shouldUnfoldRightSimpleArray() {
        assertThat(
                Array.unfoldRight(10, x -> x == 0
                                           ? Option.none()
                                           : Option.of(new Tuple2<>(x, x - 1))))
                .isEqualTo(of(10, 9, 8, 7, 6, 5, 4, 3, 2, 1));
    }

    @Test
    public void shouldUnfoldLeftToEmpty() {
        assertThat(Array.unfoldLeft(0, x -> Option.none())).isEqualTo(empty());
    }

    @Test
    public void shouldUnfoldLeftSimpleArray() {
        assertThat(
                Array.unfoldLeft(10, x -> x == 0
                                          ? Option.none()
                                          : Option.of(new Tuple2<>(x - 1, x))))
                .isEqualTo(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void shouldUnfoldToEmpty() {
        assertThat(Array.unfold(0, x -> Option.none())).isEqualTo(empty());
    }

    @Test
    public void shouldUnfoldSimpleArray() {
        assertThat(
                Array.unfold(10, x -> x == 0
                                      ? Option.none()
                                      : Option.of(new Tuple2<>(x - 1, x))))
                .isEqualTo(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    // -- toString

    @Test
    public void shouldStringifyNil() {
        assertThat(empty().toString()).isEqualTo("Array()");
    }

    @Test
    public void shouldStringifyNonNil() {
        assertThat(of(1, 2, 3).toString()).isEqualTo("Array(1, 2, 3)");
    }

    // -- toArray

    @Test
    public void shouldReturnSelfOnConvertToArray() {
        Array<Integer> value = of(1, 2, 3);
        assertThat(value.toArray()).isSameAs(value);
    }
}
