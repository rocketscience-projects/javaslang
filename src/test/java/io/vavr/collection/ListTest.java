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
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import org.junit.Test;

import java.io.InvalidObjectException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class ListTest extends AbstractLinearSeqTest {

    @Override
    protected String stringPrefix() {
        return "List";
    }

    // -- construction

    @Override
    protected <T> Collector<T, ArrayList<T>, List<T>> collector() {
        return List.collector();
    }

    @Override
    protected <T> List<T> empty() {
        return List.empty();
    }

    @Override
    protected <T> List<T> of(T element) {
        return List.of(element);
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    @Override
    protected final <T> List<T> of(T... elements) {
        return List.of(elements);
    }

    @Override
    protected <T> List<T> ofAll(Iterable<? extends T> elements) {
        return List.ofAll(elements);
    }

    @Override
    protected <T extends Comparable<? super T>> List<T> ofJavaStream(java.util.stream.Stream<? extends T> javaStream) {
        return List.ofAll(javaStream);
    }

    @Override
    protected List<Boolean> ofAll(boolean... elements) {
        return List.ofAll(elements);
    }

    @Override
    protected List<Byte> ofAll(byte... elements) {
        return List.ofAll(elements);
    }

    @Override
    protected List<Character> ofAll(char... elements) {
        return List.ofAll(elements);
    }

    @Override
    protected List<Double> ofAll(double... elements) {
        return List.ofAll(elements);
    }

    @Override
    protected List<Float> ofAll(float... elements) {
        return List.ofAll(elements);
    }

    @Override
    protected List<Integer> ofAll(int... elements) {
        return List.ofAll(elements);
    }

    @Override
    protected List<Long> ofAll(long... elements) {
        return List.ofAll(elements);
    }

    @Override
    protected List<Short> ofAll(short... elements) {
        return List.ofAll(elements);
    }

    @Override
    protected <T> List<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        return List.tabulate(n, f);
    }

    @Override
    protected <T> List<T> fill(int n, Supplier<? extends T> s) {
        return List.fill(n, s);
    }

    @Override
    protected <T> Traversable<T> fill(int n, T element) {
        return List.fill(n, element);
    }

    @Override
    protected List<Character> range(char from, char toExclusive) {
        return List.range(from, toExclusive);
    }

    @Override
    protected List<Character> rangeBy(char from, char toExclusive, int step) {
        return List.rangeBy(from, toExclusive, step);
    }

    @Override
    protected List<Double> rangeBy(double from, double toExclusive, double step) {
        return List.rangeBy(from, toExclusive, step);
    }

    @Override
    protected List<Integer> range(int from, int toExclusive) {
        return List.range(from, toExclusive);
    }

    @Override
    protected List<Integer> rangeBy(int from, int toExclusive, int step) {
        return List.rangeBy(from, toExclusive, step);
    }

    @Override
    protected List<Long> range(long from, long toExclusive) {
        return List.range(from, toExclusive);
    }

    @Override
    protected List<Long> rangeBy(long from, long toExclusive, long step) {
        return List.rangeBy(from, toExclusive, step);
    }

    @Override
    protected List<Character> rangeClosed(char from, char toInclusive) {
        return List.rangeClosed(from, toInclusive);
    }

    @Override
    protected List<Character> rangeClosedBy(char from, char toInclusive, int step) {
        return List.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected List<Double> rangeClosedBy(double from, double toInclusive, double step) {
        return List.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected List<Integer> rangeClosed(int from, int toInclusive) {
        return List.rangeClosed(from, toInclusive);
    }

    @Override
    protected List<Integer> rangeClosedBy(int from, int toInclusive, int step) {
        return List.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    protected List<Long> rangeClosed(long from, long toInclusive) {
        return List.rangeClosed(from, toInclusive);
    }

    @Override
    protected List<Long> rangeClosedBy(long from, long toInclusive, long step) {
        return List.rangeClosedBy(from, toInclusive, step);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected <T> List<List<T>> transpose(Seq<? extends Seq<T>> rows) {
        return List.transpose((List<List<T>>) rows);
    }

    @Override
    protected int getPeekNonNilPerformingAnAction() {
        return 1;
    }

    // -- static narrow

    @Test
    public void shouldNarrowList() {
        final List<Double> doubles = of(1.0d);
        final List<Number> numbers = List.narrow(doubles);
        final int actual = numbers.append(new BigDecimal("2.0")).sum().intValue();
        assertThat(actual).isEqualTo(3);
    }

    // -- static ofAll

    @Test
    public void shouldAcceptNavigableSet() {
        final java.util.TreeSet<Integer> javaSet = new java.util.TreeSet<>();
        javaSet.add(2);
        javaSet.add(1);
        assertThat(List.ofAll(javaSet)).isEqualTo(List.of(1, 2));
    }

    @Test
    public void shouldReturnSelfWhenIterableIsInstanceOfList() {
        final List<Integer> source = ofAll(1, 2, 3);
        final List<Integer> target = List.ofAll(source);
        assertThat(target).isSameAs(source);
    }

    @Test
    public void shouldReturnSelfWhenIterableIsInstanceOfListView() {
        final JavaConverters.ListView<Integer, List<Integer>> source = JavaConverters
                .asJava(ofAll(1, 2, 3), JavaConverters.ChangePolicy.IMMUTABLE);
        final List<Integer> target = List.ofAll(source);
        assertThat(target).isSameAs(source.getDelegate());
    }

    // -- partition

    @Test
    public void shouldPartitionInOneIteration() {
        final AtomicInteger count = new AtomicInteger(0);
        final Tuple2<List<Integer>, List<Integer>> results = of(1, 2, 3).partition(i -> {
            count.incrementAndGet();
            return true;
        });
        assertThat(results._1).isEqualTo(of(1, 2, 3));
        assertThat(results._2).isEqualTo(of());
        assertThat(count.get()).isEqualTo(3);
    }

    // -- peek

    @Test(expected = NoSuchElementException.class)
    public void shouldFailPeekOfNil() {
        empty().peek();
    }

    @Test
    public void shouldPeekOfNonNil() {
        assertThat(of(1).peek()).isEqualTo(1);
        assertThat(of(1, 2).peek()).isEqualTo(1);
    }

    // -- peekOption

    @Test
    public void shouldPeekOption() {
        assertThat(empty().peekOption()).isSameAs(Option.none());
        assertThat(of(1).peekOption()).isEqualTo(Option.of(1));
        assertThat(of(1, 2).peekOption()).isEqualTo(Option.of(1));
    }

    // -- pop

    @Test(expected = NoSuchElementException.class)
    public void shouldFailPopOfNil() {
        empty().pop();
    }

    @Test
    public void shouldPopOfNonNil() {
        assertThat(of(1).pop()).isSameAs(empty());
        assertThat(of(1, 2).pop()).isEqualTo(of(2));
    }

    // -- popOption

    @Test
    public void shouldPopOption() {
        assertThat(empty().popOption()).isSameAs(Option.none());
        assertThat(of(1).popOption()).isEqualTo(Option.of(empty()));
        assertThat(of(1, 2).popOption()).isEqualTo(Option.of(of(2)));
    }

    // -- pop2

    @Test(expected = NoSuchElementException.class)
    public void shouldFailPop2OfNil() {
        empty().pop2();
    }

    @Test
    public void shouldPop2OfNonNil() {
        assertThat(of(1).pop2()).isEqualTo(Tuple.of(1, empty()));
        assertThat(of(1, 2).pop2()).isEqualTo(Tuple.of(1, of(2)));
    }

    // -- pop2Option

    @Test
    public void shouldPop2Option() {
        assertThat(empty().pop2Option()).isSameAs(Option.none());
        assertThat(of(1).pop2Option()).isEqualTo(Option.of(Tuple.of(1, empty())));
        assertThat(of(1, 2).pop2Option()).isEqualTo(Option.of(Tuple.of(1, of(2))));
    }

    // -- push

    @Test
    public void shouldPushElements() {
        assertThat(empty().push(1)).isEqualTo(of(1));
        assertThat(empty().push(1, 2, 3)).isEqualTo(of(3, 2, 1));
        assertThat(empty().pushAll(of(1, 2, 3))).isEqualTo(of(3, 2, 1));
        assertThat(of(0).push(1)).isEqualTo(of(1, 0));
        assertThat(of(0).push(1, 2, 3)).isEqualTo(of(3, 2, 1, 0));
        assertThat(of(0).pushAll(of(1, 2, 3))).isEqualTo(of(3, 2, 1, 0));
    }

    // -- transform()

    @Test
    public void shouldTransform() {
        final String transformed = of(42).transform(v -> String.valueOf(v.head()));
        assertThat(transformed).isEqualTo("42");
    }

    // -- toString

    @Test
    public void shouldStringifyNil() {
        assertThat(empty().toString()).isEqualTo("List()");
    }

    @Test
    public void shouldStringifyNonNil() {
        assertThat(of(1, 2, 3).toString()).isEqualTo("List(1, 2, 3)");
    }

    // -- unfold

    @Test
    public void shouldUnfoldRightToEmpty() {
        assertThat(List.unfoldRight(0, x -> Option.none())).isEqualTo(empty());
    }

    @Test
    public void shouldUnfoldRightSimpleList() {
        assertThat(
                List.unfoldRight(10, x -> x == 0
                                          ? Option.none()
                                          : Option.of(new Tuple2<>(x, x - 1))))
                .isEqualTo(of(10, 9, 8, 7, 6, 5, 4, 3, 2, 1));
    }

    @Test
    public void shouldUnfoldLeftToEmpty() {
        assertThat(List.unfoldLeft(0, x -> Option.none())).isEqualTo(empty());
    }

    @Test
    public void shouldUnfoldLeftSimpleList() {
        assertThat(
                List.unfoldLeft(10, x -> x == 0
                                         ? Option.none()
                                         : Option.of(new Tuple2<>(x - 1, x))))
                .isEqualTo(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    @Test
    public void shouldUnfoldToEmpty() {
        assertThat(List.unfold(0, x -> Option.none())).isEqualTo(empty());
    }

    @Test
    public void shouldUnfoldSimpleList() {
        assertThat(
                List.unfold(10, x -> x == 0
                                     ? Option.none()
                                     : Option.of(new Tuple2<>(x - 1, x))))
                .isEqualTo(of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
    }

    // -- Cons test

    @Test(expected = InvalidObjectException.class)
    public void shouldNotSerializeEnclosingClass() throws Throwable {
        Serializables.callReadObject(List.of(1));
    }

    @Test(expected = InvalidObjectException.class)
    public void shouldNotDeserializeListWithSizeLessThanOne() throws Throwable {
        try {
            /*
             * This implementation is stable regarding jvm impl changes of object serialization. The index of the number
             * of List elements is gathered dynamically.
             */
            final byte[] listWithOneElement = Serializables.serialize(List.of(0));
            final byte[] listWithTwoElements = Serializables.serialize(List.of(0, 0));
            int index = -1;
            for (int i = 0; i < listWithOneElement.length && index == -1; i++) {
                final byte b1 = listWithOneElement[i];
                final byte b2 = listWithTwoElements[i];
                if (b1 != b2) {
                    if (b1 != 1 || b2 != 2) {
                        throw new IllegalStateException("Difference does not indicate number of elements.");
                    } else {
                        index = i;
                    }
                }
            }
            if (index == -1) {
                throw new IllegalStateException("Hack incomplete - index not found");
            }
            /*
             * Hack the serialized data and fake zero elements.
             */
            listWithOneElement[index] = 0;
            Serializables.deserialize(listWithOneElement);
        } catch (IllegalStateException x) {
            throw (x.getCause() != null) ? x.getCause() : x;
        }
    }

    //fixme: delete, when useIsEqualToInsteadOfIsSameAs() will be eliminated from AbstractValueTest class
    @Override
    protected boolean useIsEqualToInsteadOfIsSameAs() {
        return false;
    }

    // -- toList

    @Test
    public void shouldReturnSelfOnConvertToList() {
        final List<Integer> value = of(1, 2, 3);
        assertThat(value.toList()).isSameAs(value);
    }

    // -- spliterator


    @Test
    public void shouldHaveSizedSpliterator() {
        assertThat(of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.SIZED | Spliterator.SUBSIZED)).isTrue();
    }

    @Test
    public void shouldReturnSizeWhenSpliterator() {
        assertThat(of(1, 2, 3).spliterator().getExactSizeIfKnown()).isEqualTo(3);
    }
}
