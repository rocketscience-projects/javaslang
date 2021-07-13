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
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Collections;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import static io.vavr.API.*;
import static java.util.Arrays.asList;
import static java.util.Comparator.comparingInt;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.Assertions.within;

public abstract class AbstractTraversableTest extends AbstractValueTest {

    protected final boolean isTraversableAgain() {
        return empty().isTraversableAgain();
    }

    protected final boolean isOrdered() {
        return empty().isOrdered();
    }

    protected abstract <T> Collector<T, ArrayList<T>, ? extends Traversable<T>> collector();

    @Override
    protected abstract <T> Traversable<T> empty();

    protected boolean emptyShouldBeSingleton() {
        return true;
    }

    @Override
    protected abstract <T> Traversable<T> of(T element);

    @SuppressWarnings("unchecked")
    @Override
    protected abstract <T> Traversable<T> of(T... elements);

    protected abstract <T> Traversable<T> ofAll(Iterable<? extends T> elements);

    protected abstract <T extends Comparable<? super T>> Traversable<T> ofJavaStream(java.util.stream.Stream<? extends T> javaStream);

    protected abstract Traversable<Boolean> ofAll(boolean... elements);

    protected abstract Traversable<Byte> ofAll(byte... elements);

    protected abstract Traversable<Character> ofAll(char... elements);

    protected abstract Traversable<Double> ofAll(double... elements);

    protected abstract Traversable<Float> ofAll(float... elements);

    protected abstract Traversable<Integer> ofAll(int... elements);

    protected abstract Traversable<Long> ofAll(long... elements);

    protected abstract Traversable<Short> ofAll(short... elements);

    protected abstract <T> Traversable<T> tabulate(int n, Function<? super Integer, ? extends T> f);

    protected abstract <T> Traversable<T> fill(int n, Supplier<? extends T> s);

    // -- static empty()

    @Test
    public void shouldCreateNil() {
        final Traversable<?> actual = empty();
        assertThat(actual.length()).isEqualTo(0);
    }

    // -- static narrow()

    @Test
    public void shouldNarrowTraversable() {
        final Traversable<Double> doubles = of(1.0d);
        final Traversable<Number> numbers = Traversable.narrow(doubles);
        final boolean actual = numbers.contains(new BigDecimal("2.0"));
        assertThat(actual).isFalse();
    }

    // -- static of()

    @Test
    public void shouldCreateSeqOfSeqUsingCons() {
        final List<List<Object>> actual = of(List.empty()).toList();
        assertThat(actual).isEqualTo(List.of(List.empty()));
    }

    // -- static of(T...)

    @Test
    public void shouldCreateInstanceOfElements() {
        final List<Integer> actual = of(1, 2).toList();
        assertThat(actual).isEqualTo(List.of(1, 2));
    }

    // -- static of(Iterable)

    @Test
    public void shouldCreateListOfIterable() {
        final java.util.List<Integer> arrayList = asList(1, 2);
        final List<Integer> actual = ofAll(arrayList).toList();
        assertThat(actual).isEqualTo(List.of(1, 2));
    }

    // -- static ofAll(java.util.stream.Stream)

    @Test
    public void shouldCreateStreamFromEmptyJavaUtilStream() {
        final java.util.stream.Stream<Integer> javaStream = java.util.stream.Stream.empty();
        assertThat(ofJavaStream(javaStream)).isEqualTo(empty());
    }

    @Test
    public void shouldCreateStreamFromNonEmptyJavaUtilStream() {
        final java.util.stream.Stream<Integer> javaStream = java.util.stream.Stream.of(1, 2, 3);
        assertThat(ofJavaStream(javaStream)).isEqualTo(of(1, 2, 3));
    }

    // -- static of(<primitive array>)

    @Test
    public void shouldCreateListOfPrimitiveBooleanArray() {
        final Traversable<Boolean> actual = ofAll(true, false);
        final Traversable<Boolean> expected = of(true, false);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldCreateListOfPrimitiveByteArray() {
        final Traversable<Byte> actual = ofAll((byte) 1, (byte) 2, (byte) 3);
        final Traversable<Byte> expected = of((byte) 1, (byte) 2, (byte) 3);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldCreateListOfPrimitiveCharArray() {
        final Traversable<Character> actual = ofAll('a', 'b', 'c');
        final Traversable<Character> expected = of('a', 'b', 'c');
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldCreateListOfPrimitiveDoubleArray() {
        final Traversable<Double> actual = ofAll(1d, 2d, 3d);
        final Traversable<Double> expected = of(1d, 2d, 3d);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldCreateListOfPrimitiveFloatArray() {
        final Traversable<Float> actual = ofAll(1f, 2f, 3f);
        final Traversable<Float> expected = of(1f, 2f, 3f);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldCreateListOfPrimitiveIntArray() {
        final Traversable<Integer> actual = ofAll(1, 2, 3);
        final Traversable<Integer> expected = of(1, 2, 3);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldCreateListOfPrimitiveLongArray() {
        final Traversable<Long> actual = ofAll(1L, 2L, 3L);
        final Traversable<Long> expected = of(1L, 2L, 3L);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldCreateListOfPrimitiveShortArray() {
        final Traversable<Short> actual = ofAll((short) 1, (short) 2, (short) 3);
        final Traversable<Short> expected = of((short) 1, (short) 2, (short) 3);
        assertThat(actual).isEqualTo(expected);
    }

    // -- average

    @Test
    public void shouldReturnNoneWhenComputingAverageOfNil() {
        assertThat(empty().average()).isEqualTo(Option.none());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowWhenComputingAverageOfStrings() {
        of("1", "2", "3").average();
    }

    @Test
    public void shouldComputeAverageOfByte() {
        assertThat(of((byte) 1, (byte) 2).average().get()).isEqualTo(1.5);
    }

    @Test
    public void shouldComputeAverageOfDouble() {
        assertThat(of(.1, .2, .3).average().get()).isEqualTo(.2, within(10e-17));
    }

    @Test
    public void shouldComputeAverageOfFloat() {
        assertThat(of(.1f, .2f, .3f).average().get()).isEqualTo(.2, within(10e-9));
    }

    @Test
    public void shouldComputeAverageOfInt() {
        assertThat(of(1, 2, 3).average().get()).isEqualTo(2);
    }

    @Test
    public void shouldComputeAverageOfLong() {
        assertThat(of(1L, 2L, 3L).average().get()).isEqualTo(2);
    }

    @Test
    public void shouldComputeAverageOfShort() {
        assertThat(of((short) 1, (short) 2, (short) 3).average().get()).isEqualTo(2);
    }

    @Test
    public void shouldComputeAverageOfBigInteger() {
        assertThat(of(BigInteger.ZERO, BigInteger.ONE).average().get()).isEqualTo(.5);
    }

    @Test
    public void shouldComputeAverageOfBigDecimal() {
        assertThat(of(BigDecimal.ZERO, BigDecimal.ONE).average().get()).isEqualTo(.5);
    }

    @Test
    public void shouldComputeAverageAndCompensateErrors() {
        // Kahan's summation algorithm (used by DoubleStream.average()) returns 0.0 (false)
        // Neumaier's modification of Kahan's algorithm returns 0.75 (correct)
        assertThat(of(1.0, +10e100, 2.0, -10e100).average().get()).isEqualTo(0.75);
    }

    @Test
    public void shouldCalculateAverageOfDoublesContainingNaN() {
        assertThat(of(1.0, Double.NaN, 2.0).average().get()).isNaN();
    }

    @Test
    public void shouldCalculateAverageOfFloatsContainingNaN() {
        assertThat(of(1.0f, Float.NaN, 2.0f).average().get()).isNaN();
    }

    @Test
    public void shouldCalculateAverageOfDoubleAndFloat() {
        assertThat(this.<Number> of(1.0, 1.0f).average().get()).isEqualTo(1.0);
    }

    @Test
    public void shouldCalculateAverageOfDoublePositiveAndNegativeInfinity() {
        assertThat(of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).average().get()).isNaN();
    }

    @Test
    public void shouldCalculateAverageOfFloatPositiveAndNegativeInfinity() {
        assertThat(of(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY).average().get()).isNaN();
    }

    // -- collect

    @Test
    public void shouldThrowOnCollectWhenPartialFunctionIsNull() {
        assertThatThrownBy(() -> empty().collect((PartialFunction<Object, ?>) null))
                .isExactlyInstanceOf(NullPointerException.class)
                .hasMessage("partialFunction is null");
    }

    @Test
    public void shouldCollectUsingPartialFunction() {
        final PartialFunction<Integer, String> pf = new PartialFunction<Integer, String>() {
            private static final long serialVersionUID = 1L;
            @Override
            public String apply(Integer i) {
                return String.valueOf(i);
            }
            @Override
            public boolean isDefinedAt(Integer i) {
                return i % 2 == 1;
            }
        };
        final Traversable<String> actual = of(1, 2, 3).collect(pf);
        assertThat(actual).isEqualTo(of("1", "3"));
    }

    @Test
    public void shouldCollectUsingCase() {
        final Traversable<String> actual = of(1, 2, 3).collect(
                Case($(i -> i % 2 == 1), String::valueOf)
        );
        assertThat(actual).isEqualTo(of("1", "3"));
    }

    @Test
    public void shouldCollectUsingMap() {
        final Map<Integer, String> map = Map(1, "one", 3, "three");
        final Traversable<String> actual = of(1, 2, 3, 4).collect(map.asPartialFunction());
        assertThat(actual).isEqualTo(of("one", "three"));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void shouldCollectUsingMultimap() {
        if (!isOrdered()) {
            final Multimap<Integer, String> map = HashMultimap.withSeq().of(1, "one", 1, "un", 3, "three", 3, "trois");
            final Traversable<Traversable<String>> actual = of(1, 2, 3, 4).collect(map.asPartialFunction());
            assertThat(actual).isEqualTo(of(List("one", "un"), List("three", "trois")));
        }
    }

    @Test
    public void shouldCollectUsingSeq() {
        final Seq<String> map = List("one", "two", "three", "four");
        final Traversable<String> actual = of(0, 2).collect(map.asPartialFunction());
        assertThat(actual).isEqualTo(of("one", "three"));
    }

    // -- contains

    @Test
    public void shouldRecognizeNilContainsNoElement() {
        final boolean actual = empty().contains(null);
        assertThat(actual).isFalse();
    }

    @Test
    public void shouldRecognizeNonNilDoesNotContainElement() {
        final boolean actual = of(1, 2, 3).contains(0);
        assertThat(actual).isFalse();
    }

    @Test
    public void shouldRecognizeNonNilDoesContainElement() {
        final boolean actual = of(1, 2, 3).contains(2);
        assertThat(actual).isTrue();
    }

    // -- containsAll

    @Test
    public void shouldHandleDuplicates() {
        final boolean actual = of(1, 2, 3, 2, 3, 1).containsAll(of(1, 2, 2));
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldRecognizeNilNotContainsAllElements() {
        final boolean actual = empty().containsAll(of(1, 2, 3));
        assertThat(actual).isFalse();
    }

    @Test
    public void shouldRecognizeNonNilNotContainsAllOverlappingElements() {
        final boolean actual = of(1, 2, 3).containsAll(of(2, 3, 4));
        assertThat(actual).isFalse();
    }

    @Test
    public void shouldRecognizeNonNilContainsAllOnSelf() {
        final boolean actual = of(1, 2, 3).containsAll(of(1, 2, 3));
        assertThat(actual).isTrue();
    }

    // -- count

    @Test
    public void shouldCountWhenIsEmpty() {
        assertThat(empty().count(ignored -> true)).isEqualTo(0);
    }

    @Test
    public void shouldCountWhenNoneSatisfiesThePredicate() {
        assertThat(of(1, 2, 3).count(ignored -> false)).isEqualTo(0);
    }

    @Test
    public void shouldCountWhenAllSatisfyThePredicate() {
        assertThat(of(1, 2, 3).count(ignored -> true)).isEqualTo(3);
    }

    @Test
    public void shouldCountWhenSomeSatisfyThePredicate() {
        assertThat(of(1, 2, 3).count(i -> i % 2 == 0)).isEqualTo(1);
    }

    // -- distinct

    @Test
    public void shouldComputeDistinctOfEmptyTraversable() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(empty().distinct()).isEqualTo(empty());
        } else {
            assertThat(empty().distinct()).isSameAs(empty());
        }
    }

    @Test
    public void shouldComputeDistinctOfNonEmptyTraversable() {
        final Traversable<Integer> testee = of(1, 1, 2, 2, 3, 3);
        final Traversable<Integer> actual = testee.distinct();
        final Traversable<Integer> expected = of(1, 2, 3);
        assertThat(actual).isEqualTo(expected);
        if (testee.isDistinct()) {
            assertThat(actual).isSameAs(testee);
        }
    }

    // -- distinctBy(Comparator)

    @Test
    public void shouldComputeDistinctByOfEmptyTraversableUsingComparator() {
        final Comparator<Integer> comparator = comparingInt(i -> i);
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(this.<Integer> empty().distinctBy(comparator)).isEqualTo(empty());
        } else {
            assertThat(this.<Integer> empty().distinctBy(comparator)).isSameAs(empty());
        }
    }

    @Test
    public void shouldComputeDistinctByOfNonEmptyTraversableUsingComparator() {
        final Comparator<String> comparator = comparingInt(s -> (s.charAt(1)));
        final Traversable<String> distinct = of("1a", "2a", "3a", "3b", "4b", "5c").distinctBy(comparator).map(s -> s.substring(1));
        assertThat(distinct).isEqualTo(of("a", "b", "c"));
    }


    @Test
    public void shouldReturnSameInstanceWhenDistinctByComparatorEmptyTraversable() {
        final Traversable<?> empty = empty();
        assertThat(empty.distinctBy(Comparators.naturalComparator())).isSameAs(empty);
    }

    // -- distinctBy(Function)

    @Test
    public void shouldComputeDistinctByOfEmptyTraversableUsingKeyExtractor() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(empty().distinctBy(Function.identity())).isEqualTo(empty());
        } else {
            assertThat(empty().distinctBy(Function.identity())).isSameAs(empty());
        }
    }

    @Test
    public void shouldComputeDistinctByOfNonEmptyTraversableUsingKeyExtractor() {
        final Function<String, Character> function = c -> c.charAt(1);
        final Traversable<String> distinct = of("1a", "2a", "3a", "3b", "4b", "5c").distinctBy(function).map(s -> s.substring(1));
        assertThat(distinct).isEqualTo(of("a", "b", "c"));
    }

    @Test
    public void shouldReturnSameInstanceWhenDistinctByFunctionEmptyTraversable() {
        final Traversable<?> empty = empty();
        assertThat(empty.distinctBy(Function.identity())).isSameAs(empty);
    }

    // -- drop

    @Test
    public void shouldDropNoneOnNil() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(empty().drop(1)).isEqualTo(empty());
        } else {
            assertThat(empty().drop(1)).isSameAs(empty());
        }
    }

    @Test
    public void shouldDropNoneIfCountIsNegative() {
        assertThat(of(1, 2, 3).drop(-1)).isEqualTo(of(1, 2, 3));
    }

    @Test
    public void shouldDropAsExpectedIfCountIsLessThanSize() {
        assertThat(of(1, 2, 3).drop(2)).isEqualTo(of(3));
    }

    @Test
    public void shouldDropAllIfCountExceedsSize() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(of(1, 2, 3).drop(4)).isEqualTo(empty());
        } else {
            assertThat(of(1, 2, 3).drop(4)).isSameAs(empty());
        }
    }

    @Test
    public void shouldReturnSameInstanceWhenDropZeroCount() {
        final Traversable<Integer> t = of(1, 2, 3);
        assertThat(t.drop(0)).isSameAs(t);
    }

    @Test
    public void shouldReturnSameInstanceWhenDropNegativeCount() {
        final Traversable<Integer> t = of(1, 2, 3);
        assertThat(t.drop(-1)).isSameAs(t);
    }

    @Test
    public void shouldReturnSameInstanceWhenEmptyDropOne() {
        final Traversable<?> empty = empty();
        assertThat(empty.drop(1)).isSameAs(empty);
    }

    // -- dropRight

    @Test
    public void shouldDropRightNoneOnNil() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(empty().dropRight(1)).isEqualTo(empty());
        } else {
            assertThat(empty().dropRight(1)).isSameAs(empty());
        }
    }

    @Test
    public void shouldDropRightNoneIfCountIsNegative() {
        assertThat(of(1, 2, 3).dropRight(-1)).isEqualTo(of(1, 2, 3));
    }

    @Test
    public void shouldDropRightAsExpectedIfCountIsLessThanSize() {
        assertThat(of(1, 2, 3).dropRight(2)).isEqualTo(of(1));
    }

    @Test
    public void shouldDropRightAllIfCountExceedsSize() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(of(1, 2, 3).dropRight(4)).isEqualTo(empty());
        } else {
            assertThat(of(1, 2, 3).dropRight(4)).isSameAs(empty());
        }
    }

    @Test
    public void shouldReturnSameInstanceWhenDropRightZeroCount() {
        final Traversable<Integer> t = of(1, 2, 3);
        assertThat(t.dropRight(0)).isSameAs(t);
    }

    @Test
    public void shouldReturnSameInstanceWhenDropRightNegativeCount() {
        final Traversable<Integer> t = of(1, 2, 3);
        assertThat(t.dropRight(-1)).isSameAs(t);
    }

    @Test
    public void shouldReturnSameInstanceWhenEmptyDropRightOne() {
        final Traversable<?> empty = empty();
        assertThat(empty.dropRight(1)).isSameAs(empty);
    }

    // -- dropUntil

    @Test
    public void shouldDropUntilNoneOnNil() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(empty().dropUntil(ignored -> true)).isEqualTo(empty());
        } else {
            assertThat(empty().dropUntil(ignored -> true)).isSameAs(empty());
        }
    }

    @Test
    public void shouldDropUntilNoneIfPredicateIsTrue() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(of(1, 2, 3).dropUntil(ignored -> true)).isEqualTo(of(1, 2, 3));
        } else {
            final Traversable<Integer> t = of(1, 2, 3);
            assertThat(t.dropUntil(ignored -> true)).isSameAs(t);
        }
    }

    @Test
    public void shouldDropUntilAllIfPredicateIsFalse() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(of(1, 2, 3).dropUntil(ignored -> false)).isEqualTo(empty());
        } else {
            assertThat(of(1, 2, 3).dropUntil(ignored -> false)).isSameAs(empty());
        }
    }

    @Test
    public void shouldDropUntilCorrect() {
        assertThat(of(1, 2, 3).dropUntil(i -> i >= 2)).isEqualTo(of(2, 3));
    }

    @Test
    public void shouldReturnSameInstanceWhenEmptyDropUntil() {
        final Traversable<?> empty = empty();
        assertThat(empty.dropUntil(ignored -> true)).isSameAs(empty);
    }

    // -- dropWhile

    @Test
    public void shouldDropWhileNoneOnNil() {
        final Traversable<?> empty = empty();
        final Traversable<?> actual = empty.dropWhile(ignored -> true);
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(actual).isEqualTo(empty);
        } else {
            assertThat(actual).isSameAs(empty);
        }
    }

    @Test
    public void shouldDropWhileNoneIfPredicateIsFalse() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(of(1, 2, 3).dropWhile(ignored -> false)).isEqualTo(of(1, 2, 3));
        } else {
            final Traversable<Integer> t = of(1, 2, 3);
            assertThat(t.dropWhile(ignored -> false)).isSameAs(t);
        }
    }

    @Test
    public void shouldDropWhileAllIfPredicateIsTrue() {
        final Traversable<Integer> actual = of(1, 2, 3).dropWhile(ignored -> true);
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(actual).isEqualTo(empty());
        } else {
            assertThat(actual).isSameAs(empty());
        }
    }

    @Test
    public void shouldDropWhileAccordingToPredicate() {
        assertThat(of(1, 2, 3).dropWhile(i -> i < 2)).isEqualTo(of(2, 3));
    }

    @Test
    public void shouldDropWhileAndNotTruncate() {
        assertThat(of(1, 2, 3).dropWhile(i -> i % 2 == 1)).isEqualTo(of(2, 3));
    }

    @Test
    public void shouldReturnSameInstanceWhenEmptyDropWhile() {
        final Traversable<?> empty = empty();
        assertThat(empty.dropWhile(ignored -> true)).isSameAs(empty);
    }

    // -- exists

    @Test
    public void shouldBeAwareOfExistingElement() {
        final Traversable<Integer> testee = of(1, 2);
        assertThat(testee.exists(i -> i == 2)).isTrue();
    }

    @Test
    public void shouldBeAwareOfNonExistingElement() {
        assertThat(this.<Integer> empty().exists(i -> i == 1)).isFalse();
    }

    // -- existsUnique

    @Test
    public void shouldBeAwareOfExistingUniqueElement() {
        assertThat(of(1, 2).existsUnique(i -> i == 1)).isTrue();
    }

    @Test
    public void shouldBeAwareOfNonExistingUniqueElement() {
        assertThat(this.<Integer> empty().existsUnique(i -> i == 1)).isFalse();
    }

    @Test
    public void shouldBeAwareOfExistingNonUniqueElement() {
        assertThat(of(1, 1, 2).existsUnique(i -> i == 1)).isFalse();
    }

    // -- filter

    @Test
    public void shouldFilterExistingElements() {
        assertThat(of(1, 2, 3).filter(i -> i == 1)).isEqualTo(of(1));
        assertThat(of(1, 2, 3).filter(i -> i == 2)).isEqualTo(of(2));
        assertThat(of(1, 2, 3).filter(i -> i == 3)).isEqualTo(of(3));
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(of(1, 2, 3).filter(ignore -> true)).isEqualTo(of(1, 2, 3));
        } else {
            final Traversable<Integer> t = of(1, 2, 3);
            assertThat(t.filter(ignore -> true)).isSameAs(t);
        }
    }

    @Test
    public void shouldFilterNonExistingElements() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(this.<Integer> empty().filter(i -> i == 0)).isEqualTo(empty());
            assertThat(of(1, 2, 3).filter(i -> i == 0)).isEqualTo(empty());
        } else {
            assertThat(this.<Integer> empty().filter(i -> i == 0)).isSameAs(empty());
            assertThat(of(1, 2, 3).filter(i -> i == 0)).isSameAs(empty());
        }
    }

    @Test
    public void shouldReturnSameInstanceWhenFilteringEmptyTraversable() {
        final Traversable<?> empty = empty();
        assertThat(empty.filter(v -> true)).isSameAs(empty);
    }

    // -- filterNot

    @Test
    public void shouldFilterNotTheExistingElements() {
        assertThat(of(1, 2, 3).filterNot(i -> i == 1)).isEqualTo(of(2, 3));
        assertThat(of(1, 2, 3).filterNot(i -> i == 2)).isEqualTo(of(1, 3));
        assertThat(of(1, 2, 3).filterNot(i -> i == 3)).isEqualTo(of(1, 2));
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(of(1, 2, 3).filterNot(ignore -> false)).isEqualTo(of(1, 2, 3));
        } else {
            final Traversable<Integer> t = of(1, 2, 3);
            assertThat(t.filterNot(ignore -> false)).isSameAs(t);
        }
    }

    @Test
    public void shouldFilterNotTheNonExistingElements() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(this.<Integer> empty().filterNot(i -> i == 0)).isEqualTo(empty());
            assertThat(of(1, 2, 3).filterNot(i -> i > 0)).isEqualTo(empty());
        } else {
            assertThat(this.<Integer> empty().filterNot(i -> i == 0)).isSameAs(empty());
            assertThat(of(1, 2, 3).filterNot(i -> i > 0)).isSameAs(empty());
        }
    }

    @Test
    public void shouldReturnTheSameInstanceWhenFilterNotOnEmptyTraversable() {
        final Traversable<?> empty = empty();
        assertThat(empty.filterNot(v -> true)).isSameAs(empty);
    }

    // -- find

    @Test
    public void shouldFindFirstOfNil() {
        assertThat(empty().find(ignored -> true)).isEqualTo(Option.none());
    }

    @Test
    public void shouldFindFirstOfNonNil() {
        assertThat(of(1, 2, 3, 4).find(i -> i % 2 == 0)).isEqualTo(Option.of(2));
    }

    // -- findLast

    @Test
    public void shouldFindLastOfNil() {
        assertThat(empty().findLast(ignored -> true)).isEqualTo(Option.none());
    }

    @Test
    public void shouldFindLastOfNonNil() {
        assertThat(of(1, 2, 3, 4).findLast(i -> i % 2 == 0)).isEqualTo(Option.of(4));
    }

    // -- flatMap

    @Test
    public void shouldFlatMapEmpty() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(empty().flatMap(v -> of(v, 0))).isEqualTo(empty());
        } else {
            assertThat(empty().flatMap(v -> of(v, 0))).isSameAs(empty());
        }
    }

    @Test
    public void shouldFlatMapNonEmpty() {
        assertThat(of(1, 2, 3).flatMap(v -> of(v, 0))).isEqualTo(of(1, 0, 2, 0, 3, 0));
    }

    // -- fold

    @Test
    public void shouldFoldNil() {
        assertThat(this.<String> empty().fold("", (a, b) -> a + b)).isEqualTo("");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenFoldNullOperator() {
        this.<String> empty().fold(null, null);
    }

    @Test
    public void shouldFoldSingleElement() {
        assertThat(of(1).fold(0, (a, b) -> a + b)).isEqualTo(1);
    }

    @Test
    public void shouldFoldMultipleElements() {
        assertThat(of(1, 2, 3).fold(0, (a, b) -> a + b)).isEqualTo(6);
    }

    // -- foldLeft

    @Test
    public void shouldFoldLeftNil() {
        assertThat(this.<String> empty().foldLeft("", (xs, x) -> xs + x)).isEqualTo("");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenFoldLeftNullOperator() {
        this.<String> empty().foldLeft(null, null);
    }

    @Test
    public void shouldFoldLeftNonNil() {
        assertThat(of("a", "b", "c").foldLeft("!", (xs, x) -> xs + x)).isEqualTo("!abc");
    }

    // -- foldRight

    @Test
    public void shouldFoldRightNil() {
        assertThat(this.<String> empty().foldRight("", (x, xs) -> x + xs)).isEqualTo("");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenFoldRightNullOperator() {
        this.<String> empty().foldRight(null, null);
    }

    @Test
    public void shouldFoldRightNonNil() {
        assertThat(of("a", "b", "c").foldRight("!", (x, xs) -> x + xs)).isEqualTo("abc!");
    }

    // -- forAll

    @Test
    public void shouldBeAwareOfPropertyThatHoldsForAll() {
        assertThat(of(2, 4).forAll(i -> i % 2 == 0)).isTrue();
    }

    @Test
    public void shouldBeAwareOfPropertyThatNotHoldsForAll() {
        assertThat(of(1, 2).forAll(i -> i % 2 == 0)).isFalse();
    }

    // -- forEach

    @Test
    public void shouldPerformsActionOnEachElement() {
        final int[] consumer = new int[1];
        final Traversable<Integer> value = of(1, 2, 3);
        value.forEach(i -> consumer[0] += i);
        assertThat(consumer[0]).isEqualTo(6);
    }

    // -- forEachWithIndex

    @Test
    public void shouldConsumeNoElementWithIndexWhenEmpty() {
        final boolean[] actual = { false };
        final boolean[] expected = { false };
        empty().forEachWithIndex((chr, index) -> actual[0] = true);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldConsumeEachElementWithIndexWhenNonEmpty() {
        final java.util.List<Tuple2<Character, Integer>> actual = new java.util.ArrayList<>();
        final java.util.List<Tuple2<Character, Integer>> expected = Arrays.asList(Tuple.of('a', 0), Tuple.of('b', 1), Tuple.of('c', 2));
        ofAll('a', 'b', 'c').forEachWithIndex((chr, index) -> actual.add(Tuple.of(chr, index)));
        assertThat(actual).isEqualTo(expected);
    }

    // -- groupBy

    @Test
    public void shouldNilGroupBy() {
        assertThat(empty().groupBy(Function.identity())).isEqualTo(LinkedHashMap.empty());
    }

    @Test
    public void shouldNonNilGroupByIdentity() {
        final Map<?, ?> actual = of('a', 'b', 'c').groupBy(Function.identity());
        final Map<?, ?> expected = LinkedHashMap.empty().put('a', of('a')).put('b', of('b')).put('c', of('c'));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldNonNilGroupByEqual() {
        final Map<?, ?> actual = of('a', 'b', 'c').groupBy(c -> 1);
        final Map<?, ?> expected = LinkedHashMap.empty().put(1, of('a', 'b', 'c'));
        assertThat(actual).isEqualTo(expected);
    }

    // -- arrangeBy

    @Test
    public void shouldNilArrangeBy() {
        assertThat(empty().arrangeBy(Function.identity())).isEqualTo(Option.of(LinkedHashMap.empty()));
    }

    @Test
    public void shouldNonNilArrangeByIdentity() {
        final Option<Map<Character,Character>> actual = of('a', 'b', 'c').arrangeBy(Function.identity());
        final Option<Map<?, ?>> expected = Option.of(LinkedHashMap.empty().put('a', 'a').put('b', 'b').put('c', 'c'));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldNonNilArrangeByEqual() {
        final Option<Map<Integer, Character>> actual = of('a', 'b', 'c').arrangeBy(c -> 1);
        final Option<Map<?, ?>> expected = Option.none();
        assertThat(actual).isEqualTo(expected);
    }

    // -- grouped

    @Test
    public void shouldGroupedNil() {
        assertThat(empty().grouped(1).isEmpty()).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenGroupedWithSizeZero() {
        empty().grouped(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenGroupedWithNegativeSize() {
        empty().grouped(-1);
    }

    @Test
    public void shouldGroupedTraversableWithEqualSizedBlocks() {
        final List<Traversable<Integer>> actual = of(1, 2, 3, 4).grouped(2).toList().map(io.vavr.collection.Vector::ofAll);
        final List<Traversable<Integer>> expected = List.of(io.vavr.collection.Vector.of(1, 2), io.vavr.collection.Vector.of(3, 4));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldGroupedTraversableWithRemainder() {
        final List<Traversable<Integer>> actual = of(1, 2, 3, 4, 5).grouped(2).toList().map(io.vavr.collection.Vector::ofAll);
        final List<Traversable<Integer>> expected = List.of(io.vavr.collection.Vector.of(1, 2), io.vavr.collection.Vector.of(3, 4), io.vavr.collection.Vector.of(5));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldGroupedWhenTraversableLengthIsSmallerThanBlockSize() {
        final List<Traversable<Integer>> actual = of(1, 2, 3, 4).grouped(5).toList().map(io.vavr.collection.Vector::ofAll);
        final List<Traversable<Integer>> expected = List.of(io.vavr.collection.Vector.of(1, 2, 3, 4));
        assertThat(actual).isEqualTo(expected);
    }

    // -- hasDefiniteSize

    @Test
    public void shouldReturnSomethingOnHasDefiniteSize() {
        empty().hasDefiniteSize();
    }

    // -- head

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowWhenHeadOnNil() {
        empty().head();
    }

    @Test
    public void shouldReturnHeadOfNonNil() {
        assertThat(of(1, 2, 3).head()).isEqualTo(1);
    }

    // -- headOption

    @Test
    public void shouldReturnNoneWhenCallingHeadOptionOnNil() {
        assertThat(empty().headOption().isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnSomeHeadWhenCallingHeadOptionOnNonNil() {
        assertThat(of(1, 2, 3).headOption()).isEqualTo(Option.some(1));
    }

    // -- init

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowWhenInitOfNil() {
        empty().init().head();
    }

    @Test
    public void shouldGetInitOfNonNil() {
        assertThat(of(1, 2, 3).init()).isEqualTo(of(1, 2));
    }

    // -- initOption

    @Test
    public void shouldReturnNoneWhenCallingInitOptionOnNil() {
        assertThat(empty().initOption().isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnSomeInitWhenCallingInitOptionOnNonNil() {
        assertThat(of(1, 2, 3).initOption()).isEqualTo(Option.some(of(1, 2)));
    }

    // -- isEmpty

    @Test
    public void shouldRecognizeNil() {
        assertThat(empty().isEmpty()).isTrue();
    }

    @Test
    public void shouldRecognizeNonNil() {
        assertThat(of(1).isEmpty()).isFalse();
    }

    // -- isLazy

    @Test
    public void shouldVerifyLazyProperty() {
        assertThat(empty().isLazy()).isFalse();
        assertThat(of(1).isLazy()).isFalse();
    }

    // -- iterator

    @Test
    public void shouldNotHasNextWhenNilIterator() {
        assertThat(empty().iterator().hasNext()).isFalse();
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowOnNextWhenNilIterator() {
        empty().iterator().next();
    }

    @Test
    public void shouldIterateFirstElementOfNonNil() {
        assertThat(of(1, 2, 3).iterator().next()).isEqualTo(1);
    }

    @Test
    public void shouldFullyIterateNonNil() {
        final io.vavr.collection.Iterator<Integer> iterator = of(1, 2, 3).iterator();
        int actual;
        for (int i = 1; i <= 3; i++) {
            actual = iterator.next();
            assertThat(actual).isEqualTo(i);
        }
        assertThat(iterator.hasNext()).isFalse();
    }

    @Test
    public void shouldThrowWhenCallingNextOnEmptyIterator() {
        assertThatThrownBy(() -> empty().iterator().next()).isInstanceOf(NoSuchElementException.class);
    }

    @Test
    public void shouldThrowWhenCallingNextTooOftenOnNonEmptyIterator() {
        final io.vavr.collection.Iterator<Integer> iterator = of(1).iterator();
        assertThatThrownBy(() -> {
            iterator.next();
            iterator.next();
        }).isInstanceOf(NoSuchElementException.class);
    }

    // -- last

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowWhenLastOnNil() {
        empty().last();
    }

    @Test
    public void shouldReturnLastOfNonNil() {
        assertThat(of(1, 2, 3).last()).isEqualTo(3);
    }

    // -- lastOption

    @Test
    public void shouldReturnNoneWhenCallingLastOptionOnNil() {
        assertThat(empty().lastOption().isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnSomeLastWhenCallingLastOptionOnNonNil() {
        assertThat(of(1, 2, 3).lastOption()).isEqualTo(Option.some(3));
    }

    // -- length

    @Test
    public void shouldComputeLengthOfNil() {
        assertThat(empty().length()).isEqualTo(0);
    }

    @Test
    public void shouldComputeLengthOfNonNil() {
        assertThat(of(1, 2, 3).length()).isEqualTo(3);
    }

    // -- max

    @Test
    public void shouldReturnNoneWhenComputingMaxOfNil() {
        assertThat(empty().max()).isEqualTo(Option.none());
    }

    @Test
    public void shouldComputeMaxOfOneValue() { assertThat(of(5).max()).isEqualTo(Option.some(5)); }

    @Test
    public void shouldComputeMaxOfStrings() {
        assertThat(of("1", "2", "3").max()).isEqualTo(Option.some("3"));
    }

    @Test
    public void shouldComputeMaxOfBoolean() {
        assertThat(of(true, false).max()).isEqualTo(Option.some(true));
    }

    @Test
    public void shouldComputeMaxOfByte() {
        assertThat(of((byte) 1, (byte) 2).max()).isEqualTo(Option.some((byte) 2));
    }

    @Test
    public void shouldComputeMaxOfChar() {
        assertThat(of('a', 'b', 'c').max()).isEqualTo(Option.some('c'));
    }

    @Test
    public void shouldComputeMaxOfDouble() {
        assertThat(of(.1, .2, .3).max()).isEqualTo(Option.some(.3));
    }

    @Test
    public void shouldComputeMaxOfFloat() {
        assertThat(of(.1f, .2f, .3f).max()).isEqualTo(Option.some(.3f));
    }

    @Test
    public void shouldComputeMaxOfInt() {
        assertThat(of(1, 2, 3).max()).isEqualTo(Option.some(3));
    }

    @Test
    public void shouldComputeMaxOfLong() {
        assertThat(of(1L, 2L, 3L).max()).isEqualTo(Option.some(3L));
    }

    @Test
    public void shouldComputeMaxOfShort() {
        assertThat(of((short) 1, (short) 2, (short) 3).max()).isEqualTo(Option.some((short) 3));
    }

    @Test
    public void shouldComputeMaxOfBigInteger() {
        assertThat(of(BigInteger.ZERO, BigInteger.ONE).max()).isEqualTo(Option.some(BigInteger.ONE));
    }

    @Test
    public void shouldComputeMaxOfBigDecimal() {
        assertThat(of(BigDecimal.ZERO, BigDecimal.ONE).max()).isEqualTo(Option.some(BigDecimal.ONE));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenMaxOfNullAndInt() {
        of(null, 1).max();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenMaxOfIntAndNull() {
        of(1, null).max();
    }

    @Test
    public void shouldCalculateMaxOfDoublesContainingNaN() {
        assertThat(of(1.0, Double.NaN, 2.0).max().get()).isNaN();
    }

    @Test
    public void shouldCalculateMaxOfFloatsContainingNaN() {
        assertThat(of(1.0f, Float.NaN, 2.0f).max().get()).isEqualTo(Float.NaN);
    }

    @Test
    public void shouldThrowClassCastExceptionWhenTryingToCalculateMaxOfDoubleAndFloat() {
        assertThatThrownBy(() -> this.<Number> of(1.0, 1.0f).max()).isInstanceOf(ClassCastException.class);
    }

    @Test
    public void shouldCalculateMaxOfDoublePositiveAndNegativeInfinity() {
        assertThat(of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).max().get()).isEqualTo(Double.POSITIVE_INFINITY);
    }

    @Test
    public void shouldCalculateMaxOfFloatPositiveAndNegativeInfinity() {
        assertThat(of(Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY).max().get()).isEqualTo(Float.POSITIVE_INFINITY);
    }

    // -- maxBy(Comparator)

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMaxByWithNullComparator() {
        of(1).maxBy((Comparator<Integer>) null);
    }

    @Test
    public void shouldThrowWhenMaxByOfNil() {
        assertThat(empty().maxBy((o1, o2) -> 0)).isEqualTo(Option.none());
    }

    @Test
    public void shouldCalculateMaxByOfInts() {
        assertThat(of(1, 2, 3).maxBy(comparingInt(i -> i))).isEqualTo(Option.some(3));
    }

    @Test
    public void shouldCalculateInverseMaxByOfInts() {
        assertThat(of(1, 2, 3).maxBy((i1, i2) -> i2 - i1)).isEqualTo(Option.some(1));
    }

    // -- maxBy(Function)

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMaxByWithNullFunction() {
        of(1).maxBy((Function<Integer, Integer>) null);
    }

    @Test
    public void shouldThrowWhenMaxByFunctionOfNil() {
        assertThat(this.<Integer> empty().maxBy(i -> i)).isEqualTo(Option.none());
    }

    @Test
    public void shouldCalculateMaxByFunctionOfInts() {
        assertThat(of(1, 2, 3).maxBy(i -> i)).isEqualTo(Option.some(3));
    }

    @Test
    public void shouldCalculateInverseMaxByFunctionOfInts() {
        assertThat(of(1, 2, 3).maxBy(i -> -i)).isEqualTo(Option.some(1));
    }

    @Test
    public void shouldCallMaxFunctionOncePerElement() {
        final int[] cnt = { 0 };
        assertThat(of(1, 2, 3).maxBy(i -> {
            cnt[0]++;
            return i;
        })).isEqualTo(Option.some(3));
        assertThat(cnt[0]).isEqualTo(3);
    }

    // -- min

    @Test
    public void shouldReturnNoneWhenComputingMinOfNil() {
        assertThat(empty().min()).isEqualTo(Option.none());
    }

    @Test
    public void shouldComputeMinOfOneValue() { assertThat(of(5).min()).isEqualTo(Option.some(5)); }

    @Test
    public void shouldComputeMinOfStrings() {
        assertThat(of("1", "2", "3").min()).isEqualTo(Option.some("1"));
    }

    @Test
    public void shouldComputeMinOfBoolean() {
        assertThat(of(true, false).min()).isEqualTo(Option.some(false));
    }

    @Test
    public void shouldComputeMinOfByte() {
        assertThat(of((byte) 1, (byte) 2).min()).isEqualTo(Option.some((byte) 1));
    }

    @Test
    public void shouldComputeMinOfChar() {
        assertThat(of('a', 'b', 'c').min()).isEqualTo(Option.some('a'));
    }

    @Test
    public void shouldComputeMinOfDouble() {
        assertThat(of(.1, .2, .3).min()).isEqualTo(Option.some(.1));
    }

    @Test
    public void shouldComputeMinOfFloat() {
        assertThat(of(.1f, .2f, .3f).min()).isEqualTo(Option.some(.1f));
    }

    @Test
    public void shouldComputeMinOfInt() {
        assertThat(of(1, 2, 3).min()).isEqualTo(Option.some(1));
    }

    @Test
    public void shouldComputeMinOfLong() {
        assertThat(of(1L, 2L, 3L).min()).isEqualTo(Option.some(1L));
    }

    @Test
    public void shouldComputeMinOfShort() {
        assertThat(of((short) 1, (short) 2, (short) 3).min()).isEqualTo(Option.some((short) 1));
    }

    @Test
    public void shouldComputeMinOfBigInteger() {
        assertThat(of(BigInteger.ZERO, BigInteger.ONE).min()).isEqualTo(Option.some(BigInteger.ZERO));
    }

    @Test
    public void shouldComputeMinOfBigDecimal() {
        assertThat(of(BigDecimal.ZERO, BigDecimal.ONE).min()).isEqualTo(Option.some(BigDecimal.ZERO));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenMinOfNullAndInt() {
        of(null, 1).min();
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowNPEWhenMinOfIntAndNull() {
        of(1, null).min();
    }

    @Test
    public void shouldCalculateMinOfDoublesContainingNaN() {
        assertThat(of(1.0, Double.NaN, 2.0).min().get()).isNaN();
    }

    @Test
    public void shouldCalculateMinOfFloatsContainingNaN() {
        assertThat(of(1.0f, Float.NaN, 2.0f).min().get()).isEqualTo(Float.NaN);
    }

    @Test
    public void shouldThrowClassCastExceptionWhenTryingToCalculateMinOfDoubleAndFloat() {
        assertThatThrownBy(() -> this.<Number> of(1.0, 1.0f).min()).isInstanceOf(ClassCastException.class);
    }

    @Test
    public void shouldCalculateMinOfDoublePositiveAndNegativeInfinity() {
        assertThat(of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).min().get()).isEqualTo(Double.NEGATIVE_INFINITY);
    }

    @Test
    public void shouldCalculateMinOfFloatPositiveAndNegativeInfinity() {
        assertThat(of(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY).min().get()).isEqualTo(Double.NEGATIVE_INFINITY);
    }

    // -- minBy(Comparator)

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMinByWithNullComparator() {
        of(1).minBy((Comparator<Integer>) null);
    }

    @Test
    public void shouldThrowWhenMinByOfNil() {
        assertThat(empty().minBy((o1, o2) -> 0)).isEqualTo(Option.none());
    }

    @Test
    public void shouldCalculateMinByOfInts() {
        assertThat(of(1, 2, 3).minBy(comparingInt(i -> i))).isEqualTo(Option.some(1));
    }

    @Test
    public void shouldCalculateInverseMinByOfInts() {
        assertThat(of(1, 2, 3).minBy((i1, i2) -> i2 - i1)).isEqualTo(Option.some(3));
    }

    // -- minBy(Function)

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMinByWithNullFunction() {
        of(1).minBy((Function<Integer, Integer>) null);
    }

    @Test
    public void shouldThrowWhenMinByFunctionOfNil() {
        assertThat(this.<Integer> empty().minBy(i -> i)).isEqualTo(Option.none());
    }

    @Test
    public void shouldCalculateMinByFunctionOfInts() {
        assertThat(of(1, 2, 3).minBy(i -> i)).isEqualTo(Option.some(1));
    }

    @Test
    public void shouldCalculateInverseMinByFunctionOfInts() {
        assertThat(of(1, 2, 3).minBy(i -> -i)).isEqualTo(Option.some(3));
    }

    @Test
    public void shouldCallMinFunctionOncePerElement() {
        final int[] cnt = { 0 };
        assertThat(of(1, 2, 3).minBy(i -> {
            cnt[0]++;
            return i;
        })).isEqualTo(Option.some(1));
        assertThat(cnt[0]).isEqualTo(3);
    }

    // -- mkCharSeq()

    @Test
    public void shouldMkCharSeqNil() {
        assertThat(empty().mkCharSeq()).isEqualTo(CharSeq.empty());
    }

    @Test
    public void shouldMkCharSeqNonNil() {
        assertThat(of('a', 'b', 'c').mkCharSeq()).isEqualTo(CharSeq.of("abc"));
    }

    // -- mkCharSeq(delimiter)

    @Test
    public void shouldMkCharSeqWithDelimiterNil() {
        assertThat(empty().mkCharSeq(",")).isEqualTo(CharSeq.empty());
    }

    @Test
    public void shouldMkCharSeqWithDelimiterNonNil() {
        assertThat(of('a', 'b', 'c').mkCharSeq(",")).isEqualTo(CharSeq.of("a,b,c"));
    }

    // -- mkCharSeq(delimiter, prefix, suffix)

    @Test
    public void shouldMkCharSeqWithDelimiterAndPrefixAndSuffixNil() {
        assertThat(empty().mkCharSeq("[", ",", "]")).isEqualTo(CharSeq.of("[]"));
    }

    @Test
    public void shouldMkCharSeqWithDelimiterAndPrefixAndSuffixNonNil() {
        assertThat(of('a', 'b', 'c').mkCharSeq("[", ",", "]")).isEqualTo(CharSeq.of("[a,b,c]"));
    }

    // -- mkString()

    @Test
    public void shouldMkStringNil() {
        assertThat(empty().mkString()).isEqualTo("");
    }

    @Test
    public void shouldMkStringNonNil() {
        assertThat(of('a', 'b', 'c').mkString()).isEqualTo("abc");
    }

    // -- mkString(delimiter)

    @Test
    public void shouldMkStringWithDelimiterNil() {
        assertThat(empty().mkString(",")).isEqualTo("");
    }

    @Test
    public void shouldMkStringWithDelimiterNonNil() {
        assertThat(of('a', 'b', 'c').mkString(",")).isEqualTo("a,b,c");
    }

    // -- mkString(delimiter, prefix, suffix)

    @Test
    public void shouldMkStringWithDelimiterAndPrefixAndSuffixNil() {
        assertThat(empty().mkString("[", ",", "]")).isEqualTo("[]");
    }

    @Test
    public void shouldMkStringWithDelimiterAndPrefixAndSuffixNonNil() {
        assertThat(of('a', 'b', 'c').mkString("[", ",", "]")).isEqualTo("[a,b,c]");
    }

    // -- nonEmpty

    @Test
    public void shouldCalculateNonEmpty() {
        assertThat(empty().nonEmpty()).isFalse();
        assertThat(of(1).nonEmpty()).isTrue();
    }

    // -- orElse

    @Test
    public void shouldCaclEmptyOrElseSameOther() {
        final Iterable<Integer> other = of(42);
        assertThat(empty().orElse(other)).isSameAs(other);
    }

    @Test
    public void shouldCaclEmptyOrElseEqualOther() {
        assertThat(empty().orElse(Arrays.asList(1, 2))).isEqualTo(of(1, 2));
    }

    @Test
    public void shouldCaclNonemptyOrElseOther() {
        final Traversable<Integer> src = of(42);
        assertThat(src.orElse(List.of(1))).isSameAs(src);
    }

    @Test
    public void shouldCaclEmptyOrElseSameSupplier() {
        final Iterable<Integer> other = of(42);
        final Supplier<Iterable<Integer>> supplier = () -> other;
        assertThat(empty().orElse(supplier)).isSameAs(other);
    }

    @Test
    public void shouldCaclEmptyOrElseEqualSupplier() {
        assertThat(empty().orElse(() -> Arrays.asList(1, 2))).isEqualTo(of(1, 2));
    }

    @Test
    public void shouldCaclNonemptyOrElseSupplier() {
        final Traversable<Integer> src = of(42);
        assertThat(src.orElse(() -> List.of(1))).isSameAs(src);
    }

    // -- partition

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenPartitionNilAndPredicateIsNull() {
        empty().partition(null);
    }

    @Test
    public void shouldPartitionNil() {
        assertThat(empty().partition(e -> true)).isEqualTo(Tuple.of(empty(), empty()));
    }

    @Test
    public void shouldPartitionIntsInOddAndEvenHavingOddAndEvenNumbers() {
        assertThat(of(1, 2, 3, 4).partition(i -> i % 2 != 0)).isEqualTo(Tuple.of(of(1, 3), of(2, 4)));
    }

    @Test
    public void shouldPartitionIntsInOddAndEvenHavingOnlyOddNumbers() {
        assertThat(of(1, 3).partition(i -> i % 2 != 0)).isEqualTo(Tuple.of(of(1, 3), empty()));
    }

    @Test
    public void shouldPartitionIntsInOddAndEvenHavingOnlyEvenNumbers() {
        assertThat(of(2, 4).partition(i -> i % 2 != 0)).isEqualTo(Tuple.of(empty(), of(2, 4)));
    }

    // -- peek

    @Test
    public void shouldPeekNil() {
        assertThat(empty().peek(t -> {})).isEqualTo(empty());
    }

    @Test
    public void shouldPeekNonNilPerformingNoAction() {
        assertThat(of(1).peek(t -> {})).isEqualTo(of(1));
    }

    @Test
    public void shouldPeekSingleValuePerformingAnAction() {
        final int[] effect = { 0 };
        final Traversable<Integer> actual = of(1).peek(i -> effect[0] = i);
        assertThat(actual).isEqualTo(of(1));
        assertThat(effect[0]).isEqualTo(1);
    }

    @Test
    public void shouldPeekNonNilPerformingAnAction() {
        final int[] effect = { 0 };
        final Traversable<Integer> actual = of(1, 2, 3).peek(i -> effect[0] = i);
        assertThat(actual).isEqualTo(of(1, 2, 3)); // traverses all elements in the lazy case
        assertThat(effect[0]).isEqualTo(getPeekNonNilPerformingAnAction());
    }

    // returns the peek result of the specific Traversable implementation
    abstract protected int getPeekNonNilPerformingAnAction();

    // -- product

    @Test
    public void shouldComputeProductOfNil() {
        assertThat(empty().product()).isEqualTo(1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowWhenComputingProductOfStrings() {
        of("1", "2", "3").product();
    }

    @Test
    public void shouldComputeProductOfByte() {
        assertThat(of((byte) 1, (byte) 2).product()).isEqualTo(2L);
    }

    @Test
    public void shouldComputeProductOfDouble() {
        assertThat(of(.1, .2, .3).product().doubleValue()).isEqualTo(.006, within(10e-18));
    }

    @Test
    public void shouldComputeProductOfFloat() {
        assertThat(of(.1f, .2f, .3f).product().doubleValue()).isEqualTo(.006, within(10e-10));
    }

    @Test
    public void shouldComputeProductOfInt() {
        assertThat(of(1, 2, 3).product()).isEqualTo(6L);
    }

    @Test
    public void shouldComputeProductOfLong() {
        assertThat(of(1L, 2L, 3L).product()).isEqualTo(6L);
    }

    @Test
    public void shouldComputeProductOfShort() {
        assertThat(of((short) 1, (short) 2, (short) 3).product()).isEqualTo(6L);
    }

    @Test
    public void shouldComputeProductOfBigInteger() {
        assertThat(of(BigInteger.ZERO, BigInteger.ONE).product()).isEqualTo(BigInteger.ZERO);
    }

    @Test
    public void shouldComputeProductOfBigDecimal() {
        assertThat(of(BigDecimal.ZERO, BigDecimal.ONE).product()).isEqualTo(BigDecimal.ZERO);
    }

    // -- reduceOption

    @Test
    public void shouldThrowWhenReduceOptionNil() {
        assertThat(this.<String> empty().reduceOption((a, b) -> a + b)).isSameAs(Option.none());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenReduceOptionNullOperator() {
        this.<String> empty().reduceOption(null);
    }

    @Test
    public void shouldReduceOptionNonNil() {
        assertThat(of(1, 2, 3).reduceOption((a, b) -> a + b)).isEqualTo(Option.of(6));
    }

    // -- reduce

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowWhenReduceNil() {
        this.<String> empty().reduce((a, b) -> a + b);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenReduceNullOperator() {
        this.<String> empty().reduce(null);
    }

    @Test
    public void shouldReduceNonNil() {
        assertThat(of(1, 2, 3).reduce((a, b) -> a + b)).isEqualTo(6);
    }

    // -- reduceLeftOption

    @Test
    public void shouldThrowWhenReduceLeftOptionNil() {
        assertThat(this.<String> empty().reduceLeftOption((a, b) -> a + b)).isSameAs(Option.none());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenReduceLeftOptionNullOperator() {
        this.<String> empty().reduceLeftOption(null);
    }

    @Test
    public void shouldReduceLeftOptionNonNil() {
        assertThat(of("a", "b", "c").reduceLeftOption((xs, x) -> xs + x)).isEqualTo(Option.of("abc"));
    }

    // -- reduceLeft

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowWhenReduceLeftNil() {
        this.<String> empty().reduceLeft((a, b) -> a + b);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenReduceLeftNullOperator() {
        this.<String> empty().reduceLeft(null);
    }

    @Test
    public void shouldReduceLeftNonNil() {
        assertThat(of("a", "b", "c").reduceLeft((xs, x) -> xs + x)).isEqualTo("abc");
    }

    // -- reduceRightOption

    @Test
    public void shouldThrowWhenReduceRightOptionNil() {
        assertThat(this.<String> empty().reduceRightOption((a, b) -> a + b)).isSameAs(Option.none());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenReduceRightOptionNullOperator() {
        this.<String> empty().reduceRightOption(null);
    }

    @Test
    public void shouldReduceRightOptionNonNil() {
        assertThat(of("a", "b", "c").reduceRightOption((x, xs) -> x + xs)).isEqualTo(Option.of("abc"));
    }

    // -- reduceRight

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowWhenReduceRightNil() {
        this.<String> empty().reduceRight((a, b) -> a + b);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenReduceRightNullOperator() {
        this.<String> empty().reduceRight(null);
    }

    @Test
    public void shouldReduceRightNonNil() {
        assertThat(of("a", "b", "c").reduceRight((x, xs) -> x + xs)).isEqualTo("abc");
    }

    // -- replace(curr, new)

    @Test
    public void shouldReplaceElementOfNilUsingCurrNew() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(this.<Integer> empty().replace(1, 2)).isEqualTo(empty());
        } else {
            assertThat(this.<Integer> empty().replace(1, 2)).isSameAs(empty());
        }
    }

    @Test
    public void shouldReplaceFirstOccurrenceOfNonNilUsingCurrNewWhenMultipleOccurrencesExist() {
        final Traversable<Integer> testee = of(0, 1, 2, 1);
        final Traversable<Integer> actual = testee.replace(1, 3);
        final Traversable<Integer> expected = of(0, 3, 2, 1);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldReplaceElementOfNonNilUsingCurrNewWhenOneOccurrenceExists() {
        assertThat(of(0, 1, 2).replace(1, 3)).isEqualTo(of(0, 3, 2));
    }

    @Test
    public void shouldReplaceElementOfNonNilUsingCurrNewWhenNoOccurrenceExists() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(of(0, 1, 2).replace(33, 3)).isEqualTo(of(0, 1, 2));
        } else {
            final Traversable<Integer> src = of(0, 1, 2);
            assertThat(src.replace(33, 3)).isSameAs(src);
        }
    }

    // -- replaceAll(curr, new)

    @Test
    public void shouldReplaceAllElementsOfNilUsingCurrNew() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(this.<Integer> empty().replaceAll(1, 2)).isEqualTo(empty());
        } else {
            assertThat(this.<Integer> empty().replaceAll(1, 2)).isSameAs(empty());
        }
    }

    @Test
    public void shouldReplaceAllElementsOfNonNilUsingCurrNonExistingNew() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(of(0, 1, 2, 1).replaceAll(33, 3)).isEqualTo(of(0, 1, 2, 1));
        } else {
            final Traversable<Integer> src = of(0, 1, 2, 1);
            assertThat(src.replaceAll(33, 3)).isSameAs(src);
        }
    }

    @Test
    public void shouldReplaceAllElementsOfNonNilUsingCurrNew() {
        assertThat(of(0, 1, 2, 1).replaceAll(1, 3)).isEqualTo(of(0, 3, 2, 3));
    }

    // -- retainAll

    @Test
    public void shouldRetainAllElementsFromNil() {
        final Traversable<Object> empty = empty();
        final Traversable<Object> actual = empty.retainAll(of(1, 2, 3));
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(actual).isEqualTo(empty);
        } else {
            assertThat(actual).isSameAs(empty);
        }
    }

    @Test
    public void shouldRetainAllExistingElementsFromNonNil() {
        final Traversable<Integer> src = of(1, 2, 3, 2, 1, 3);
        final Traversable<Integer> expected = of(1, 2, 2, 1);
        final Traversable<Integer> actual = src.retainAll(of(1, 2));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldRetainAllElementsFromNonNil() {
        final Traversable<Integer> src = of(1, 2, 1, 2, 2);
        final Traversable<Integer> expected = of(1, 2, 1, 2, 2);
        final Traversable<Integer> actual = src.retainAll(of(1, 2));
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(actual).isEqualTo(expected);
        } else {
            assertThat(actual).isSameAs(src);
        }
    }

    @Test
    public void shouldNotRetainAllNonExistingElementsFromNonNil() {
        final Traversable<Integer> src = of(1, 2, 3);
        final Traversable<Object> expected = empty();
        final Traversable<Integer> actual = src.retainAll(of(4, 5));
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(actual).isEqualTo(expected);
        } else {
            assertThat(actual).isSameAs(expected);
        }
    }

    // -- scan, scanLeft, scanRight

    @Test
    public void shouldScanEmpty() {
        final Traversable<Integer> testee = empty();
        final Traversable<Integer> actual = testee.scan(0, (s1, s2) -> s1 + s2);
        assertThat(actual).isEqualTo(this.of(0));
    }

    @Test
    public void shouldScanLeftEmpty() {
        final Traversable<Integer> testee = empty();
        final Traversable<Integer> actual = testee.scanLeft(0, (s1, s2) -> s1 + s2);
        assertThat(actual).isEqualTo(of(0));
    }

    @Test
    public void shouldScanRightEmpty() {
        final Traversable<Integer> testee = empty();
        final Traversable<Integer> actual = testee.scanRight(0, (s1, s2) -> s1 + s2);
        assertThat(actual).isEqualTo(of(0));
    }

    @Test
    public void shouldScanNonEmpty() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final Traversable<Integer> actual = testee.scan(0, (acc, s) -> acc + s);
        assertThat(actual).isEqualTo(of(0, 1, 3, 6));
    }

    @Test
    public void shouldScanLeftNonEmpty() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final Traversable<String> actual = testee.scanLeft("x", (acc, i) -> acc + i);
        assertThat(actual).isEqualTo(of("x", "x1", "x12", "x123"));
    }

    @Test
    public void shouldScanRightNonEmpty() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final Traversable<String> actual = testee.scanRight("x", (i, acc) -> acc + i);
        assertThat(actual).isEqualTo(of("x321", "x32", "x3", "x"));
    }

    @Test
    public void shouldScanWithNonComparable() {
        final Traversable<NonComparable> testee = of(new NonComparable("a"));
        final List<NonComparable> actual = List.ofAll(testee.scan(new NonComparable("x"), (u1, u2) -> new NonComparable(u1.value + u2.value)));
        final List<NonComparable> expected = List.of("x", "xa").map(NonComparable::new);
        assertThat(actual).containsAll(expected);
        assertThat(expected).containsAll(actual);
        assertThat(actual.length()).isEqualTo(expected.length());
    }

    @Test
    public void shouldScanLeftWithNonComparable() {
        final Traversable<NonComparable> testee = of(new NonComparable("a"));
        final List<NonComparable> actual = List.ofAll(testee.scanLeft(new NonComparable("x"), (u1, u2) -> new NonComparable(u1.value + u2.value)));
        final List<NonComparable> expected = List.of("x", "xa").map(NonComparable::new);
        assertThat(actual).containsAll(expected);
        assertThat(expected).containsAll(actual);
        assertThat(actual.length()).isEqualTo(expected.length());
    }

    @Test
    public void shouldScanRightWithNonComparable() {
        final Traversable<NonComparable> testee = of(new NonComparable("a"));
        final List<NonComparable> actual = List.ofAll(testee.scanRight(new NonComparable("x"), (u1, u2) -> new NonComparable(u1.value + u2.value)));
        final List<NonComparable> expected = List.of("ax", "x").map(NonComparable::new);
        assertThat(actual).containsAll(expected);
        assertThat(expected).containsAll(actual);
        assertThat(actual.length()).isEqualTo(expected.length());
    }

    // -- slideBy(classifier)

    @Test
    public void shouldSlideNilByClassifier() {
        assertThat(empty().slideBy(Function.identity())).isEmpty();
    }

    @Test(timeout=1000)
    public void shouldTerminateSlideByClassifier() {
        AtomicInteger ai = new AtomicInteger(0);
        List<List<String>> expected = List.of(List.of("a", "-"), List.of( "-"), List.of("d") );
        List<List<String>> actual = List.of("a", "-", "-", "d")
                .slideBy(x -> x.equals("-") ? ai.getAndIncrement() : ai.get())
                .toList();
        assertThat(actual).containsAll(expected);
        assertThat(expected).containsAll(actual);
    }

    @Test
    public void shouldSlideSingularByClassifier() {
        final List<Traversable<Integer>> actual = of(1).slideBy(Function.identity()).toList().map(io.vavr.collection.Vector::ofAll);
        final List<Traversable<Integer>> expected = List.of(io.vavr.collection.Vector.of(1));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSlideNonNilByIdentityClassifier() {
        final List<Traversable<Integer>> actual = of(1, 2, 3).slideBy(Function.identity()).toList().map(io.vavr.collection.Vector::ofAll);
        final List<Traversable<Integer>> expected = List.of(io.vavr.collection.Vector.of(1), io.vavr.collection.Vector.of(2), io.vavr.collection.Vector.of(3));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSlideNonNilByConstantClassifier() {
        final List<Traversable<Integer>> actual = of(1, 2, 3).slideBy(e -> "same").toList().map(io.vavr.collection.Vector::ofAll);
        final List<Traversable<Integer>> expected = List.of(io.vavr.collection.Vector.of(1, 2, 3));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSlideNonNilBySomeClassifier() {
        final List<Traversable<Integer>> actual = of(10, 20, 30, 42, 52, 60, 72).slideBy(e -> e % 10).toList().map(io.vavr.collection.Vector::ofAll);
        final List<Traversable<Integer>> expected = List.of(io.vavr.collection.Vector.of(10, 20, 30), io.vavr.collection.Vector.of(42, 52), io.vavr.collection.Vector.of(60), io.vavr.collection.Vector.of(72));
        assertThat(actual).isEqualTo(expected);
    }

    // -- sliding(size)

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenSlidingNilByZeroSize() {
        empty().sliding(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenSlidingNilByNegativeSize() {
        empty().sliding(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenSlidingNonNilByZeroSize() {
        of(1).sliding(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenSlidingNonNilByNegativeSize() {
        of(1).sliding(-1);
    }

    @Test
    public void shouldSlideNilBySize() {
        assertThat(empty().sliding(1)).isEmpty();
    }

    @Test
    public void shouldSlideNonNilBySize1() {
        final List<Traversable<Integer>> actual = of(1, 2, 3).sliding(1).toList().map(io.vavr.collection.Vector::ofAll);
        final List<Traversable<Integer>> expected = List.of(io.vavr.collection.Vector.of(1), io.vavr.collection.Vector.of(2), io.vavr.collection.Vector.of(3));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSlideNonNilBySize2() {
        final List<Traversable<Integer>> actual = of(1, 2, 3, 4, 5).sliding(2).toList().map(io.vavr.collection.Vector::ofAll);
        final List<Traversable<Integer>> expected = List.of(io.vavr.collection.Vector.of(1, 2), io.vavr.collection.Vector.of(2, 3), io.vavr.collection.Vector.of(3, 4), io.vavr.collection.Vector.of(4, 5));
        assertThat(actual).isEqualTo(expected);
    }

    // -- sliding(size, step)

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenSlidingNilByPositiveStepAndNegativeSize() {
        empty().sliding(-1, 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenSlidingNilByNegativeStepAndNegativeSize() {
        empty().sliding(-1, -1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenSlidingNilByNegativeStepAndPositiveSize() {
        empty().sliding(1, -1);
    }

    @Test
    public void shouldSlideNilBySizeAndStep() {
        assertThat(empty().sliding(1, 1).isEmpty()).isTrue();
    }

    @Test
    public void shouldSlide5ElementsBySize2AndStep3() {
        final List<Traversable<Integer>> actual = of(1, 2, 3, 4, 5).sliding(2, 3).toList().map(io.vavr.collection.Vector::ofAll);
        final List<Traversable<Integer>> expected = List.of(io.vavr.collection.Vector.of(1, 2), io.vavr.collection.Vector.of(4, 5));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSlide5ElementsBySize2AndStep4() {
        final List<Traversable<Integer>> actual = of(1, 2, 3, 4, 5).sliding(2, 4).toList().map(io.vavr.collection.Vector::ofAll);
        final List<Traversable<Integer>> expected = List.of(io.vavr.collection.Vector.of(1, 2), io.vavr.collection.Vector.of(5));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSlide5ElementsBySize2AndStep5() {
        final List<Traversable<Integer>> actual = of(1, 2, 3, 4, 5).sliding(2, 5).toList().map(io.vavr.collection.Vector::ofAll);
        final List<Traversable<Integer>> expected = List.of(io.vavr.collection.Vector.of(1, 2));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSlide4ElementsBySize5AndStep3() {
        final List<Traversable<Integer>> actual = of(1, 2, 3, 4).sliding(5, 3).toList().map(io.vavr.collection.Vector::ofAll);
        final List<Traversable<Integer>> expected = List.of(io.vavr.collection.Vector.of(1, 2, 3, 4));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSlide7ElementsBySize1AndStep3() {
        final List<Traversable<Integer>> actual = of(1, 2, 3, 4, 5, 6 ,7).sliding(1, 3).toList().map(io.vavr.collection.Vector::ofAll);
        final List<Traversable<Integer>> expected = List.of(io.vavr.collection.Vector.of(1), io.vavr.collection.Vector.of(4), io.vavr.collection.Vector.of(7));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSlide7ElementsBySize2AndStep3() {
        final List<Traversable<Integer>> actual = of(1, 2, 3, 4, 5, 6 ,7).sliding(2, 3).toList().map(io.vavr.collection.Vector::ofAll);
        final List<Traversable<Integer>> expected = List.of(io.vavr.collection.Vector.of(1, 2), io.vavr.collection.Vector.of(4, 5), io.vavr.collection.Vector.of(7));
        assertThat(actual).isEqualTo(expected);
    }

    // -- span

    @Test
    public void shouldSpanNil() {
        assertThat(this.<Integer> empty().span(i -> i < 2)).isEqualTo(Tuple.of(empty(), empty()));
    }

    @Test
    public void shouldSpanNonNil() {
        assertThat(of(0, 1, 2, 3).span(i -> i < 2)).isEqualTo(Tuple.of(of(0, 1), of(2, 3)));
    }

    @Test
    public void shouldSpanAndNotTruncate() {
        assertThat(of(1, 1, 2, 2, 3, 3).span(x -> x % 2 == 1)).isEqualTo(Tuple.of(of(1, 1), of(2, 2, 3, 3)));
        assertThat(of(1, 1, 2, 2, 4, 4).span(x -> x == 1)).isEqualTo(Tuple.of(of(1, 1), of(2, 2, 4, 4)));
    }

    // -- spliterator

    @Test
    public void shouldSplitNil() {
        final java.util.List<Integer> actual = new java.util.ArrayList<>();
        this.<Integer>empty().spliterator().forEachRemaining(actual::add);
        assertThat(actual).isEmpty();
    }

    @Test
    public void shouldSplitNonNil() {
        final java.util.List<Integer> actual = new java.util.ArrayList<>();
        of(1, 2, 3).spliterator().forEachRemaining(actual::add);
        assertThat(actual).isEqualTo(asList(1, 2, 3));
    }

    @Test
    public void shouldHaveImmutableSpliterator() {
        assertThat(of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.IMMUTABLE)).isTrue();
    }

    // -- sum

    @Test
    public void shouldComputeSumOfNil() {
        assertThat(empty().sum()).isEqualTo(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowWhenComputingSumOfStrings() {
        of("1", "2", "3").sum();
    }

    @Test
    public void shouldComputeSumOfByte() {
        assertThat(of((byte) 1, (byte) 2).sum()).isEqualTo(3L);
    }

    @Test
    public void shouldComputeSumOfDouble() {
        assertThat(of(.1, .2, .3).sum().doubleValue()).isEqualTo(.6, within(10e-16));
    }

    @Test
    public void shouldComputeSumOfFloat() {
        assertThat(of(.1f, .2f, .3f).sum().doubleValue()).isEqualTo(.6, within(10e-8));
    }

    @Test
    public void shouldComputeSumOfInt() {
        assertThat(of(1, 2, 3).sum()).isEqualTo(6L);
    }

    @Test
    public void shouldComputeSumOfLong() {
        assertThat(of(1L, 2L, 3L).sum()).isEqualTo(6L);
    }

    @Test
    public void shouldComputeSumOfShort() {
        assertThat(of((short) 1, (short) 2, (short) 3).sum()).isEqualTo(6L);
    }

    @Test
    public void shouldComputeSumOfBigInteger() {
        assertThat(of(BigInteger.ZERO, BigInteger.ONE).sum()).isEqualTo(BigInteger.ONE);
    }

    @Test
    public void shouldComputeSumOfBigDecimal() {
        assertThat(of(BigDecimal.ZERO, BigDecimal.ONE).sum()).isEqualTo(BigDecimal.ONE);
    }

    // -- take

    @Test
    public void shouldTakeNoneOnNil() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(empty().take(1)).isEqualTo(empty());
        } else {
            assertThat(empty().take(1)).isSameAs(empty());
        }
    }

    @Test
    public void shouldTakeNoneIfCountIsNegative() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(of(1, 2, 3).take(-1)).isEqualTo(empty());
        } else {
            assertThat(of(1, 2, 3).take(-1)).isSameAs(empty());
        }
    }

    @Test
    public void shouldTakeAsExpectedIfCountIsLessThanSize() {
        assertThat(of(1, 2, 3).take(2)).isEqualTo(of(1, 2));
    }

    @Test
    public void shouldTakeAllIfCountExceedsSize() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(of(1, 2, 3).take(4)).isEqualTo(of(1, 2, 3));
        } else {
            final Traversable<Integer> t = of(1, 2, 3);
            assertThat(t.take(4)).isSameAs(t);
        }
    }

    @Test
    public void shouldReturnSameInstanceIfTakeAll() {
        final Traversable<?> t = of(1, 2, 3);
        assertThat(t.take(3)).isSameAs(t);
        assertThat(t.take(4)).isSameAs(t);
    }

    // -- takeRight

    @Test
    public void shouldTakeRightNoneOnNil() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(empty().takeRight(1)).isEqualTo(empty());
        } else {
            assertThat(empty().takeRight(1)).isSameAs(empty());
        }
    }

    @Test
    public void shouldTakeRightNoneIfCountIsNegative() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(of(1, 2, 3).takeRight(-1)).isEqualTo(empty());
        } else {
            assertThat(of(1, 2, 3).takeRight(-1)).isSameAs(empty());
        }
    }

    @Test
    public void shouldTakeRightAsExpectedIfCountIsLessThanSize() {
        assertThat(of(1, 2, 3).takeRight(2)).isEqualTo(of(2, 3));
    }

    @Test
    public void shouldTakeRightAllIfCountExceedsSize() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(of(1, 2, 3).takeRight(4)).isEqualTo(of(1, 2, 3));
        } else {
            final Traversable<Integer> t = of(1, 2, 3);
            assertThat(t.takeRight(4)).isSameAs(t);
        }
    }

    @Test
    public void shouldReturnSameInstanceIfTakeRightAll() {
        final Traversable<?> t = of(1, 2, 3);
        assertThat(t.takeRight(3)).isSameAs(t);
        assertThat(t.takeRight(4)).isSameAs(t);
    }

    // -- takeUntil

    @Test
    public void shouldTakeUntilNoneOnNil() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(empty().takeUntil(x -> true)).isEqualTo(empty());
        } else {
            assertThat(empty().takeUntil(x -> true)).isSameAs(empty());
        }
    }

    @Test
    public void shouldTakeUntilAllOnFalseCondition() {
        final Traversable<Integer> t = of(1, 2, 3);
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(of(1, 2, 3).takeUntil(x -> false)).isEqualTo(of(1, 2, 3));
        } else {
            assertThat(t.takeUntil(x -> false)).isSameAs(t);
        }
    }

    @Test
    public void shouldTakeUntilAllOnTrueCondition() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(of(1, 2, 3).takeUntil(x -> true)).isEqualTo(empty());
        } else {
            assertThat(of(1, 2, 3).takeUntil(x -> true)).isSameAs(empty());
        }
    }

    @Test
    public void shouldTakeUntilAsExpected() {
        assertThat(of(2, 4, 5, 6).takeUntil(x -> x % 2 != 0)).isEqualTo(of(2, 4));
    }

    @Test
    public void shouldReturnSameInstanceWhenEmptyTakeUntil() {
        final Traversable<?> empty = empty();
        assertThat(empty.takeUntil(ignored -> false)).isSameAs(empty);
    }

    // -- takeWhile

    @Test
    public void shouldTakeWhileNoneOnNil() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(empty().takeWhile(x -> true)).isEqualTo(empty());
        } else {
            assertThat(empty().takeWhile(x -> true)).isSameAs(empty());
        }
    }

    @Test
    public void shouldTakeWhileAllOnFalseCondition() {
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(of(1, 2, 3).takeWhile(x -> false)).isEqualTo(empty());
        } else {
            assertThat(of(1, 2, 3).takeWhile(x -> false)).isSameAs(empty());
        }
    }

    @Test
    public void shouldTakeWhileAllOnTrueCondition() {
        final Traversable<Integer> t = of(1, 2, 3);
        if (useIsEqualToInsteadOfIsSameAs()) {
            assertThat(of(1, 2, 3).takeWhile(x -> true)).isEqualTo(of(1, 2, 3));
        } else {
            assertThat(t.takeWhile(x -> true)).isSameAs(t);
        }
    }

    @Test
    public void shouldTakeWhileAsExpected() {
        assertThat(of(2, 4, 5, 6).takeWhile(x -> x % 2 == 0)).isEqualTo(of(2, 4));
    }

    @Test
    public void shouldReturnSameInstanceWhenEmptyTakeWhile() {
        final Traversable<?> empty = empty();
        assertThat(empty.takeWhile(ignored -> false)).isSameAs(empty);
    }

    // -- tail

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowWhenTailOnNil() {
        empty().tail();
    }

    @Test
    public void shouldReturnTailOfNonNil() {
        assertThat(of(1, 2, 3).tail()).isEqualTo(of(2, 3));
    }

    // -- tailOption

    @Test
    public void shouldReturnNoneWhenCallingTailOptionOnNil() {
        assertThat(empty().tailOption().isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnSomeTailWhenCallingTailOptionOnNonNil() {
        assertThat(of(1, 2, 3).tailOption()).isEqualTo(Option.some(of(2, 3)));
    }

    // -- unzip

    @Test
    public void shouldUnzipNil() {
        assertThat(empty().unzip(x -> x, y -> y)).isEqualTo(Tuple.of(Iterator.empty(), Iterator.empty()));
    }

    @Test
    public void shouldUnzipNonNil() {
        final Traversable<Integer> actual0 = of(0, 1);
        final Tuple2<Iterator<Integer>, Iterator<Character>> actual = actual0.unzip(i -> i, i -> (char) ((short) 'a' + i));
        final Tuple2<Iterator<Integer>, Iterator<Character>> expected = Tuple.of(of(0, 1).iterator(), of('a', 'b').iterator());
        assertThat(actual.map(Traversable<Integer>::toArray, Traversable<Character>::toArray)).isEqualTo(expected.map(Traversable<Integer>::toArray, Traversable<Character>::toArray));
    }

    @Test
    public void shouldUnzip3Nil() {
        assertThat(empty().unzip3(x -> x, y -> y, z -> z)).isEqualTo(Tuple.of(Iterator.empty(), Iterator.empty(), Iterator.empty()));
    }

    @Test
    public void shouldUnzip3NonNil() {
        final Tuple3<Iterator<Integer>, Iterator<Character>, Iterator<Character>> actual
                = of(0, 1).unzip3(i -> i, i -> (char) ((short) 'a' + i), i -> (char) ((short) 'a' + i + 1));
        final Tuple3<Iterator<Integer>, Iterator<Character>, Iterator<Character>> expected
                = Tuple.of(of(0, 1).iterator(), of('a', 'b').iterator(), of('b', 'c').iterator());
        assertThat(actual.map(Traversable<Integer>::toArray, Traversable<Character>::toArray, Traversable<Character>::toArray)).isEqualTo(expected.map(Traversable<Integer>::toArray, Traversable<Character>::toArray, Traversable<Character>::toArray));
    }

    // -- zip

    @Test
    public void shouldZipNils() {
        final Traversable<?> actual = empty().zip(empty());
        assertThat(actual).isEmpty();
    }

    @Test
    public void shouldZipEmptyAndNonNil() {
        final Traversable<?> actual = empty().zip(of(1));
        assertThat(actual).isEmpty();
    }

    @Test
    public void shouldZipNonEmptyAndNil() {
        final Traversable<?> actual = of(1).zip(empty());
        assertThat(actual).isEmpty();
    }

    @Test
    public void shouldZipNonNilsIfThisIsSmaller() {
        final Traversable<Tuple2<Integer, String>> actual = of(1, 2).zip(of("a", "b", "c"));
        @SuppressWarnings("unchecked")
        final Traversable<Tuple2<Integer, String>> expected = of(Tuple.of(1, "a"), Tuple.of(2, "b"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipNonNilsIfThatIsSmaller() {
        final Traversable<Tuple2<Integer, String>> actual = of(1, 2, 3).zip(of("a", "b"));
        @SuppressWarnings("unchecked")
        final Traversable<Tuple2<Integer, String>> expected = of(Tuple.of(1, "a"), Tuple.of(2, "b"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipNonNilsOfSameSize() {
        final Traversable<Tuple2<Integer, String>> actual = of(1, 2, 3).zip(of("a", "b", "c"));
        @SuppressWarnings("unchecked")
        final Traversable<Tuple2<Integer, String>> expected = of(Tuple.of(1, "a"), Tuple.of(2, "b"), Tuple.of(3, "c"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldZipWithNonNilsOfSameSize() {
        final Traversable<String> actual = of(1, 2, 3).zipWith(of("a", "b", "c"), (integer, s) -> integer + s);
        final Traversable<String> expected = of("1a", "2b", "3c");
        assertThat(actual).isEqualTo(expected);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowIfZipWithThatIsNull() {
        empty().zip(null);
    }

    // -- zipAll

    @Test
    public void shouldZipAllNils() {
        final Traversable<?> actual = empty().zipAll(empty(), null, null);
        assertThat(actual).isEmpty();
    }

    @Test
    public void shouldZipAllEmptyAndNonNil() {
        final Traversable<?> actual = empty().zipAll(of(1), null, null);
        final Traversable<Tuple2<Object, Integer>> expected = of(Tuple.of(null, 1));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonEmptyAndNil() {
        final Traversable<?> actual = of(1).zipAll(empty(), null, null);
        final Traversable<Tuple2<Integer, Object>> expected = of(Tuple.of(1, null));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonNilsIfThisIsSmaller() {
        final Traversable<Tuple2<Integer, String>> actual = of(1, 2).zipAll(of("a", "b", "c"), 9, "z");
        @SuppressWarnings("unchecked")
        final Traversable<Tuple2<Integer, String>> expected = of(Tuple.of(1, "a"), Tuple.of(2, "b"), Tuple.of(9, "c"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonNilsIfThatIsSmaller() {
        final Traversable<Tuple2<Integer, String>> actual = of(1, 2, 3).zipAll(of("a", "b"), 9, "z");
        @SuppressWarnings("unchecked")
        final Traversable<Tuple2<Integer, String>> expected = of(Tuple.of(1, "a"), Tuple.of(2, "b"), Tuple.of(3, "z"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonNilsOfSameSize() {
        final Traversable<Tuple2<Integer, String>> actual = of(1, 2, 3).zipAll(of("a", "b", "c"), 9, "z");
        @SuppressWarnings("unchecked")
        final Traversable<Tuple2<Integer, String>> expected = of(Tuple.of(1, "a"), Tuple.of(2, "b"), Tuple.of(3, "c"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowIfZipAllWithThatIsNull() {
        empty().zipAll(null, null, null);
    }

    // -- zipWithIndex

    @Test
    public void shouldZipNilWithIndex() {
        assertThat(this.<String>empty().zipWithIndex()).isEqualTo(this.<Tuple2<String, Integer>>empty());
    }

    @Test
    public void shouldZipNonNilWithIndex() {
        final Traversable<Tuple2<String, Integer>> actual = of("a", "b", "c").zipWithIndex();
        @SuppressWarnings("unchecked")
        final Traversable<Tuple2<String, Integer>> expected = of(Tuple.of("a", 0), Tuple.of("b", 1), Tuple.of("c", 2));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void shouldZipNonNilWithIndexWithMapper() {
        final Traversable<String> actual = of("a", "b", "c").zipWithIndex((s, idx) -> s + idx);
        final Traversable<String> expected = of("a0", "b1", "c2");
        assertThat(actual).isEqualTo(expected);
    }

    // -- toJavaArray(IntFunction)

    @Test
    public void shouldConvertNilToJavaArray() {
        final Integer[] actual = List.<Integer> empty().toJavaArray(Integer[]::new);
        final Integer[] expected = new Integer[] {};
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldConvertNonNilToJavaArray() {
        final Integer[] array = of(1, 2).toJavaArray(Integer[]::new);
        final Integer[] expected = new Integer[] { 1, 2 };
        assertThat(array).isEqualTo(expected);
    }

    // -- toJavaList

    @Test
    public void shouldConvertNilToArrayList() {
        assertThat(this.<Integer> empty().toJavaList()).isEqualTo(new ArrayList<Integer>());
    }

    @Test
    public void shouldConvertNonNilToArrayList() {
        assertThat(of(1, 2, 3).toJavaList()).isEqualTo(asList(1, 2, 3));
    }

    // -- toJavaMap(Function)

    @Test
    public void shouldConvertNilToHashMap() {
        assertThat(this.<Integer> empty().toJavaMap(x -> Tuple.of(x, x))).isEqualTo(new java.util.HashMap<>());
    }

    @Test
    public void shouldConvertNonNilToHashMap() {
        final java.util.Map<Integer, Integer> expected = new java.util.HashMap<>();
        expected.put(1, 1);
        expected.put(2, 2);
        assertThat(of(1, 2).toJavaMap(x -> Tuple.of(x, x))).isEqualTo(expected);
    }

    // -- toJavaSet

    @Test
    public void shouldConvertNilToHashSet() {
        assertThat(this.<Integer> empty().toJavaSet()).isEqualTo(new java.util.HashSet<>());
    }

    @Test
    public void shouldConvertNonNilToHashSet() {
        final java.util.Set<Integer> expected = new java.util.HashSet<>();
        expected.add(2);
        expected.add(1);
        expected.add(3);
        assertThat(of(1, 2, 2, 3).toJavaSet()).isEqualTo(expected);
    }

    // -- Conversions toXxx()

    @Test
    public void shouldConvertToArray() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final Array<Integer> array = testee.toArray();
        assertThat(array).isEqualTo(Array.of(1, 2, 3));
    }

    @Test
    public void shouldConvertToCharSeq() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final CharSeq charSeq = testee.toCharSeq();
        final CharSeq expected = CharSeq.of(of(1, 2, 3).iterator().mkString());
        assertThat(charSeq).isEqualTo(expected);
    }

    @Test
    public void shouldConvertToList() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final io.vavr.collection.List<Integer> list = testee.toList();
        assertThat(list).isEqualTo(io.vavr.collection.List.of(1, 2, 3));
    }

    @Test
    public void shouldConvertToHashMap() {
        final Traversable<Integer> testee = of(9, 5, 1);
        final io.vavr.collection.Map<Integer, Integer> map = testee.toMap(i -> Tuple.of(i, i));
        assertThat(map).isEqualTo(io.vavr.collection.HashMap.of(1, 1, 5, 5, 9, 9));
    }

    @Test
    public void shouldConvertToHashMapTwoFunctions() {
        final Traversable<Integer> testee = of(9, 5, 1);
        final io.vavr.collection.Map<Integer, Integer> map = testee.toMap(Function.identity(), Function.identity());
        assertThat(map).isEqualTo(io.vavr.collection.HashMap.of(1, 1, 5, 5, 9, 9));
    }

    @Test
    public void shouldConvertToLinkedMap() {
        final Traversable<Integer> testee = of(1, 5, 9);
        final io.vavr.collection.Map<Integer, Integer> map = testee.toLinkedMap(i -> Tuple.of(i, i));
        assertThat(map).isEqualTo(io.vavr.collection.LinkedHashMap.of(1, 1, 5, 5, 9, 9));
    }

    @Test
    public void shouldConvertToLinkedMapTwoFunctions() {
        final Traversable<Integer> testee = of(1, 5, 9);
        final io.vavr.collection.Map<Integer, Integer> map = testee.toLinkedMap(Function.identity(), Function.identity());
        assertThat(map).isEqualTo(io.vavr.collection.LinkedHashMap.of(1, 1, 5, 5, 9, 9));
    }

    @Test
    public void shouldConvertToSortedMap() {
        final Traversable<Integer> testee = of(9, 5, 1);
        final io.vavr.collection.SortedMap<Integer, Integer> map = testee.toSortedMap(i -> Tuple.of(i, i));
        assertThat(map).isEqualTo(io.vavr.collection.TreeMap.of(1, 1, 5, 5, 9, 9));
    }

    @Test
    public void shouldConvertToSortedMapTwoFunctions() {
        final Traversable<Integer> testee = of(9, 5, 1);
        final io.vavr.collection.SortedMap<Integer, Integer> map = testee.toSortedMap(Function.identity(), Function.identity());
        assertThat(map).isEqualTo(io.vavr.collection.TreeMap.of(1, 1, 5, 5, 9, 9));
    }

    @Test
    public void shouldConvertToSortedMapWithComparator() {
        final Traversable<Integer> testee = of(9, 5, 1);
        final Comparator<Integer> comparator = ((Comparator<Integer>) Integer::compareTo).reversed();
        final io.vavr.collection.SortedMap<Integer, Integer> map = testee.toSortedMap(comparator, i -> Tuple.of(i, i));
        assertThat(map).isEqualTo(io.vavr.collection.TreeMap.of(comparator, 9, 9, 5, 5, 1, 1));
    }

    @Test
    public void shouldConvertToSortedMapTwoFunctionsWithComparator() {
        final Traversable<Integer> testee = of(9, 5, 1);
        final Comparator<Integer> comparator = ((Comparator<Integer>) Integer::compareTo).reversed();
        final io.vavr.collection.SortedMap<Integer, Integer> map = testee.toSortedMap(comparator, Function.identity(), Function.identity());
        assertThat(map).isEqualTo(io.vavr.collection.TreeMap.of(comparator, 9, 9, 5, 5, 1, 1));
    }

    @Test
    public void shouldConvertToQueue() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final io.vavr.collection.Queue<Integer> queue = testee.toQueue();
        assertThat(queue).isEqualTo(io.vavr.collection.Queue.of(1, 2, 3));
    }

    @Test
    public void shouldConvertToPriorityQueueUsingImplicitComparator() {
        final Traversable<Integer> testee = of(1, 3, 2);
        final io.vavr.collection.PriorityQueue<Integer> queue = testee.toPriorityQueue();
        assertThat(queue).isEqualTo(io.vavr.collection.PriorityQueue.of(1, 2, 3));
    }

    @Test
    public void shouldConvertToPriorityQueueUsingExplicitComparator() {
        final Comparator<Integer> comparator = Comparator.naturalOrder();
        final Traversable<Integer> testee = of(1, 3, 2);
        final io.vavr.collection.PriorityQueue<Integer> queue = testee.toPriorityQueue(comparator);
        assertThat(queue).isEqualTo(io.vavr.collection.PriorityQueue.of(comparator, 1, 2, 3));
    }

    @Test
    public void shouldConvertToPriorityQueueUsingSerializableComparator() {
        final Traversable<Integer> testee = of(1, 3, 2);
        final io.vavr.collection.PriorityQueue<Integer> queue = testee.toPriorityQueue();
        final io.vavr.collection.PriorityQueue<Integer> actual = Serializables.deserialize(Serializables.serialize(queue));
        assertThat(actual).isEqualTo(queue);
    }

    @Test
    public void shouldConvertToSet() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final io.vavr.collection.Set<Integer> set = testee.toSet();
        assertThat(set).isEqualTo(io.vavr.collection.HashSet.of(1, 2, 3));
    }

    @Test
    public void shouldConvertToLinkedSet() {
        final Traversable<Integer> testee = of(3, 7, 1, 15, 0);
        final io.vavr.collection.Set<Integer> set = testee.toLinkedSet();
        final io.vavr.collection.List<Integer> itemsInOrder;
        if (testee instanceof Traversable && !((Traversable) testee).isTraversableAgain()) {
            itemsInOrder = io.vavr.collection.List.of(3, 7, 1, 15, 0);
        } else {
            itemsInOrder = testee.toList();
        }
        assertThat(set).isEqualTo(itemsInOrder.foldLeft(io.vavr.collection.LinkedHashSet.empty(), io.vavr.collection.LinkedHashSet::add));
    }

    @Test
    public void shouldConvertToSortedSetWithoutComparatorOnComparable() {
        final Traversable<Integer> testee = of(3, 7, 1, 15, 0);
        final io.vavr.collection.SortedSet<Integer> set = testee.toSortedSet();
        assertThat(set).isEqualTo(io.vavr.collection.TreeSet.of(0, 1, 3, 7, 15));
    }

    @Test(expected = ClassCastException.class)
    public void shouldThrowOnConvertToSortedSetWithoutComparatorOnNonComparable() {
        final Traversable<Object> testee = of(new Object(), new Object());
        final io.vavr.collection.SortedSet<Object> set = testee.toSortedSet();
    }

    @Test
    public void shouldConvertToSortedSet() {
        final Traversable<Integer> testee = of(3, 7, 1, 15, 0);
        final Comparator<Integer> comparator = Comparator.comparingInt(Integer::bitCount);
        final io.vavr.collection.SortedSet<Integer> set = testee.toSortedSet(comparator.reversed());
        assertThat(set).isEqualTo(io.vavr.collection.TreeSet.of(comparator.reversed(), 0, 1, 3, 7, 15));
    }

    @Test
    public void shouldConvertToSortedSetUsingSerializableComparator() {
        final Traversable<Integer> testee = of(1, 3, 2);
        final io.vavr.collection.SortedSet<Integer> set = testee.toSortedSet();
        final io.vavr.collection.SortedSet<Integer> actual = Serializables.deserialize(Serializables.serialize(set));
        assertThat(actual).isEqualTo(set);
    }

    @Test
    public void shouldConvertToStream() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final Stream<Integer> stream = testee.toStream();
        assertThat(stream).isEqualTo(Stream.of(1, 2, 3));
    }

    @Test
    public void shouldConvertToVector() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final io.vavr.collection.Vector<Integer> vector = testee.toVector();
        assertThat(vector).isEqualTo(io.vavr.collection.Vector.of(1, 2, 3));
    }

    @Test
    public void shouldConvertToJavaArray() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final Object[] ints = testee.toJavaArray();
        assertThat(ints).isEqualTo(new Object[] { 1, 2, 3 });
    }

    @Test
    public void shouldConvertToJavaArrayWithFactory() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final Integer[] ints = testee.toJavaArray(Integer[]::new);
        assertThat(ints).containsOnly(1, 2, 3);
    }

    @Test
    public void shouldConvertToJavaCollectionUsingSupplier() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final java.util.List<Integer> ints = testee.toJavaCollection(ArrayList::new);
        assertThat(ints).isEqualTo(Arrays.asList(1, 2, 3));
    }

    @Test
    public void shouldConvertToJavaList() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final java.util.List<Integer> list = testee.toJavaList();
        assertThat(list).isEqualTo(Arrays.asList(1, 2, 3));
    }

    @Test
    public void shouldConvertToJavaListUsingSupplier() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final java.util.List<Integer> ints = testee.toJavaList(ArrayList::new);
        assertThat(ints).isEqualTo(Arrays.asList(1, 2, 3));
    }

    @Test
    public void shouldConvertToJavaMapUsingFunction() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final java.util.Map<Integer, Integer> map = testee.toJavaMap(v -> Tuple.of(v, v));
        assertThat(map).isEqualTo(javaMap(1, 1, 2, 2, 3, 3));
    }

    @Test
    public void shouldConvertToJavaMapUsingSupplierAndFunction() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final java.util.Map<Integer, Integer> map = testee.toJavaMap(java.util.HashMap::new, i -> Tuple.of(i, i));
        assertThat(map).isEqualTo(javaMap(1, 1, 2, 2, 3, 3));
    }

    @Test
    public void shouldConvertToJavaMapUsingSupplierAndTwoFunction() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final java.util.Map<Integer, String> map = testee.toJavaMap(java.util.HashMap::new, Function.identity(), String::valueOf);
        assertThat(map).isEqualTo(javaMap(1, "1", 2, "2", 3, "3"));
    }

    @Test
    public void shouldConvertToJavaSet() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final java.util.Set<Integer> set = testee.toJavaSet();
        assertThat(set).isEqualTo(javaSet(1, 2, 3));
    }

    @Test
    public void shouldConvertToJavaSetUsingSupplier() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final java.util.Set<Integer> set = testee.toJavaSet(java.util.HashSet::new);
        assertThat(set).isEqualTo(javaSet(1, 2, 3));
    }

    @Test
    public void shouldConvertToJavaStream() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final java.util.stream.Stream<Integer> s1 = testee.toJavaStream();
        final java.util.stream.Stream<Integer> s2 = java.util.stream.Stream.of(1, 2, 3);
        assertThat(io.vavr.collection.List.ofAll(s1::iterator)).isEqualTo(io.vavr.collection.List.ofAll(s2::iterator));
    }

    @Test
    public void shouldConvertToJavaParallelStream() {
        final Traversable<Integer> testee = of(1, 2, 3);
        final java.util.stream.Stream<Integer> s1 = testee.toJavaParallelStream();
        assertThat(s1.isParallel()).isTrue();
        final java.util.stream.Stream<Integer> s2 = java.util.stream.Stream.of(1, 2, 3);
        assertThat(io.vavr.collection.List.ofAll(s1::iterator)).isEqualTo(io.vavr.collection.List.ofAll(s2::iterator));
    }

    // -- toTree

    @Test
    public void shouldConvertToTree() {
        //Traversable["id:parent")]
        final Traversable<String> testee = of(
                "1:",
                "2:1", "3:1",
                "4:2", "5:2", "6:3",
                "7:4", "8:6", "9:6"
        );
        final Seq<Tree<String>> roots = Tree
                .build(testee, s -> s.split(":")[0], s -> s.split(":").length == 1 ? null : s.split(":")[1])
                .map(l -> l.map(s -> s.split(":")[0]));
        assertThat(roots).hasSize(1);
        final Tree<String> root = roots.head();
        if (testee.hasDefiniteSize()) {
            assertThat(root).hasSameSizeAs(testee);
        }
        assertThat(root.toLispString()).isEqualTo("(1 (2 (4 7) 5) (3 (6 8 9)))");
    }

    @SuppressWarnings("unchecked")
    static <K, V> java.util.Map<K, V> javaMap(Object... pairs) {
        Objects.requireNonNull(pairs, "pairs is null");
        if ((pairs.length & 1) != 0) {
            throw new IllegalArgumentException("Odd length of key-value pairs list");
        }
        final java.util.Map<K, V> map = new java.util.HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            map.put((K) pairs[i], (V) pairs[i + 1]);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    static <T> java.util.Set<T> javaSet(T... elements) {
        Objects.requireNonNull(elements, "elements is null");
        final java.util.Set<T> set = new java.util.HashSet<>();
        Collections.addAll(set, elements);
        return set;
    }

    // ++++++ OBJECT ++++++

    // -- equals

    @SuppressWarnings("EqualsWithItself")
    @Test
    public void shouldEqualSameTraversableInstance() {
        final Traversable<?> nonEmpty = of(1);
        assertThat(nonEmpty.equals(nonEmpty)).isTrue();
        assertThat(empty().equals(empty())).isTrue();
    }

    @Test
    public void shouldNilNotEqualsNull() {
        assertThat(empty()).isNotNull();
    }

    @Test
    public void shouldNonNilNotEqualsNull() {
        assertThat(of(1)).isNotNull();
    }

    @Test
    public void shouldEmptyNotEqualsDifferentType() {
        assertThat(empty()).isNotEqualTo("");
    }

    @Test
    public void shouldNonEmptyNotEqualsDifferentType() {
        assertThat(of(1)).isNotEqualTo("");
    }

    @Test
    public void shouldRecognizeEqualityOfNils() {
        assertThat(empty()).isEqualTo(empty());
    }

    @Test
    public void shouldRecognizeEqualityOfNonNils() {
        assertThat(of(1, 2, 3).equals(of(1, 2, 3))).isTrue();
    }

    @Test
    public void shouldRecognizeNonEqualityOfTraversablesOfSameSize() {
        assertThat(of(1, 2, 3).equals(of(1, 2, 4))).isFalse();
    }

    @Test
    public void shouldRecognizeNonEqualityOfTraversablesOfDifferentSize() {
        assertThat(of(1, 2, 3).equals(of(1, 2))).isFalse();
        assertThat(of(1, 2).equals(of(1, 2, 3))).isFalse();
    }

    // -- hashCode

    @Test
    public void shouldCalculateHashCodeOfNil() {
        assertThat(empty().hashCode() == empty().hashCode()).isTrue();
    }

    @Test
    public void shouldCalculateHashCodeOfNonNil() {
        assertThat(of(1, 2).hashCode() == of(1, 2).hashCode()).isTrue();
    }

    @Test
    public void shouldCalculateDifferentHashCodesForDifferentTraversables() {
        assertThat(of(1, 2).hashCode() != of(2, 3).hashCode()).isTrue();
    }

    @Test
    public void shouldComputeHashCodeOfEmpty() {
        assertThat(empty().hashCode()).isEqualTo(1);
    }

    @Test
    public void shouldNotThrowStackOverflowErrorWhenCalculatingHashCodeOf1000000Integers() {
        assertThat(ofAll(io.vavr.collection.Iterator.range(0, 1000000)).hashCode()).isNotNull();
    }

    // -- toString

    abstract protected String stringPrefix();

    @Test
    public void shouldConformEmptyStringRepresentation() {
        final Traversable<Object> testee = empty();
        if (!testee.hasDefiniteSize()) {
            assertThat(testee.toString()).isEqualTo(stringPrefix() + "()");
            testee.size(); // evaluates all elements of lazy collections
        }
        assertThat(testee.toString()).isEqualTo(toString(testee));
    }

    @Test
    public void shouldConformNonEmptyStringRepresentation() {
        final Traversable<Object> testee = of("a", "b", "c");
        if (isTraversableAgain()) {
            if (!testee.hasDefiniteSize()) {
                assertThat(testee.toString()).isEqualTo(stringPrefix() + "(a, ?)");
                testee.size(); // evaluates all elements of lazy collections
            }
            assertThat(testee.toString()).isEqualTo(toString(testee));
        } else {
            assertThat(testee.toString()).isEqualTo(stringPrefix() + "(?)");
        }
    }

    private static String toString(Traversable<?> traversable) {
        return traversable.mkString(traversable.stringPrefix() + "(", ", ", ")");
    }

    // -- static collector()

    @Test
    public void shouldStreamAndCollectNil() {
        testCollector(() -> {
            final Traversable<?> actual = java.util.stream.Stream.empty().collect(collector());
            assertThat(actual).isEmpty();
        });
    }

    @Test
    public void shouldStreamAndCollectNonNil() {
        testCollector(() -> {
            final Traversable<?> actual = java.util.stream.Stream.of(1, 2, 3).collect(this.<Object> collector());
            assertThat(actual).isEqualTo(of(1, 2, 3));
        });
    }

    @Test
    public void shouldParallelStreamAndCollectNil() {
        testCollector(() -> {
            final Traversable<?> actual = java.util.stream.Stream.empty().parallel().collect(collector());
            assertThat(actual).isEmpty();
        });
    }

    @Test
    public void shouldParallelStreamAndCollectNonNil() {
        testCollector(() -> {
            final Traversable<?> actual = java.util.stream.Stream.of(1, 2, 3).parallel().collect(this.<Object> collector());
            assertThat(actual).isEqualTo(of(1, 2, 3));
        });
    }

    // -- single

    @Test(expected = NoSuchElementException.class)
    public void shouldSingleFailEmpty() {
        empty().single();
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldSingleFailTwo() {
        of(1, 2).single();
    }

    @Test
    public void shouldSingleWork() {
        assertThat(of(1).single()).isEqualTo(1);
    }

    // -- singleOption

    @Test
    public void shouldSingleOptionFailEmpty() {
        assertThat(empty().singleOption()).isEqualTo(Option.none());
    }

    @Test
    public void shouldSingleOptionFailTwo() {
        assertThat(of(1, 2).singleOption()).isEqualTo(Option.none());
    }

    @Test
    public void shouldSingleOptionWork() {
        assertThat(of(1).singleOption()).isEqualTo(Option.of(1));
    }

    @Test
    public void shouldTabulateTheSeq() {
        final Function<Number, Integer> f = i -> i.intValue() * i.intValue();
        final Traversable<Number> actual = tabulate(3, f);
        assertThat(actual).isEqualTo(of(0, 1, 4));
    }

    @Test
    public void shouldTabulateTheSeqCallingTheFunctionInTheRightOrder() {
        final java.util.LinkedList<Integer> ints = new java.util.LinkedList<>(asList(0, 1, 2));
        final Function<Integer, Integer> f = i -> ints.remove();
        final Traversable<Integer> actual = tabulate(3, f);
        assertThat(actual).isEqualTo(of(0, 1, 2));
    }

    @Test
    public void shouldTabulateTheSeqWith0Elements() {
        assertThat(tabulate(0, i -> i)).isEqualTo(empty());
    }

    @Test
    public void shouldTabulateTheSeqWith0ElementsWhenNIsNegative() {
        assertThat(tabulate(-1, i -> i)).isEqualTo(empty());
    }

    // -- fill(int, Supplier)

    @Test
    public void shouldFillTheSeqCallingTheSupplierInTheRightOrder() {
        final java.util.LinkedList<Integer> ints = new java.util.LinkedList<>(asList(0, 1));
        final Traversable<Number> actual = fill(2, ints::remove);
        assertThat(actual).isEqualTo(of(0, 1));
    }

    @Test
    public void shouldFillTheSeqWith0Elements() {
        assertThat(fill(0, () -> 1)).isEqualTo(empty());
    }

    @Test
    public void shouldFillTheSeqWith0ElementsWhenNIsNegative() {
        assertThat(fill(-1, () -> 1)).isEqualTo(empty());
    }

    @Test
    public void ofShouldReturnTheSingletonEmpty() {
        if (!emptyShouldBeSingleton()) { return; }
        assertThat(of()).isSameAs(empty());
    }

    @Test
    public void ofAllShouldReturnTheSingletonEmpty() {
        if (!emptyShouldBeSingleton()) { return; }
        assertThat(ofAll(io.vavr.collection.Iterator.empty())).isSameAs(empty());
    }

    private void testCollector(Runnable test) {
        if (isTraversableAgain()) {
            test.run();
        } else {
            try {
                collector();
                fail("Collections which are not traversable again should not define a Collector.");
            } catch (UnsupportedOperationException x) {
                // ok
            } catch (Throwable x) {
                fail("Unexpected exception", x);
            }
        }
    }

    // helpers

    /**
     * Wraps a String in order to ensure that it is not Comparable.
     */
    static final class NonComparable {

        final String value;

        NonComparable(String value) {
            this.value = value;
        }

        @Override
        public int hashCode() {
            return Objects.hash(value);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (obj instanceof NonComparable) {
                final NonComparable that = (NonComparable) obj;
                return Objects.equals(this.value, that.value);
            } else {
                return false;
            }
        }

        @Override
        public String toString() {
            return value;
        }
    }
}
