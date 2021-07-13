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
import io.vavr.collection.JavaConverters.ChangePolicy;
import io.vavr.collection.JavaConverters.ListView;
import io.vavr.control.Option;
import org.assertj.core.api.*;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CharSeqTest {

    protected <T> IterableAssert<T> assertThat(Iterable<T> actual) {
        return new IterableAssert<T>(actual) {};
    }

    protected <T> ObjectAssert<T> assertThat(T actual) {
        return new ObjectAssert<T>(actual) {};
    }

    protected BooleanAssert assertThat(Boolean actual) {
        return new BooleanAssert(actual) {};
    }

    protected DoubleAssert assertThat(Double actual) {
        return new DoubleAssert(actual) {};
    }

    protected IntegerAssert assertThat(Integer actual) {
        return new IntegerAssert(actual) {};
    }

    protected LongAssert assertThat(Long actual) {
        return new LongAssert(actual) {};
    }

    protected StringAssert assertThat(String actual) {
        return new StringAssert(actual) {};
    }

    // -- static ofAll

    @Test
    public void shouldThrowWhenCreatingCharSeqFromIterableThatContainsNull() {
        assertThatThrownBy(() -> CharSeq.ofAll(Arrays.asList('1', null))).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void shouldReturnSelfWhenIterableIsInstanceOfCharSeq() {
        final CharSeq source = CharSeq.ofAll(Arrays.asList('a', 'b', 'c'));
        final CharSeq target = CharSeq.ofAll(source);
        assertThat(target).isSameAs(source);
    }

    @Test
    public void shouldReturnSelfWhenIterableIsInstanceOfListView() {
        final ListView<Character, CharSeq> source = JavaConverters
                .asJava(CharSeq.ofAll(asList('a', 'b', 'c')), ChangePolicy.IMMUTABLE);
        final CharSeq target = CharSeq.ofAll(source);
        assertThat(target).isSameAs(source.getDelegate());
    }

    // -- asJavaMutable*

    @Test
    public void shouldConvertAsJava() {
        final java.util.List<Character> list = CharSeq.of('1', '2', '3').asJavaMutable();
        list.add('4');
        assertThat(list).isEqualTo(Arrays.asList('1', '2', '3', '4'));
    }

    @Test
    public void shouldConvertAsJavaWithConsumer() {
        final CharSeq seq = CharSeq.of('1', '2', '3').asJavaMutable(list -> {
            assertThat(list).isEqualTo(Arrays.asList('1', '2', '3'));
            list.add('4');
        });
        assertThat(seq).isEqualTo(CharSeq.of('1', '2', '3', '4'));
    }

    @Test
    public void shouldConvertAsJavaAndRethrowException() {
        assertThatThrownBy(() -> CharSeq.of('1', '2', '3').asJavaMutable(list -> { throw new RuntimeException("test");}))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    @Test
    public void shouldConvertAsJavaImmutable() {
        final java.util.List<Character> list = CharSeq.of('1', '2', '3').asJava();
        assertThat(list).isEqualTo(Arrays.asList('1', '2', '3'));
        assertThatThrownBy(() -> list.add('4')).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    public void shouldConvertAsJavaImmutableWithConsumer() {
        final CharSeq seq = CharSeq.of('1', '2', '3').asJava(list -> {
            assertThat(list).isEqualTo(Arrays.asList('1', '2', '3'));
            assertThatThrownBy(() -> list.add('4')).isInstanceOf(UnsupportedOperationException.class);
        });
        assertThat(seq).isEqualTo(CharSeq.of('1', '2', '3'));
    }

    @Test
    public void shouldConvertAsJavaImmutableAndRethrowException() {
        assertThatThrownBy(() -> CharSeq.of('1', '2', '3').asJava(list -> { throw new RuntimeException("test");}))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("test");
    }

    // -- compareTo(Object)

    @Test
    public void shouldCastTheCharSeqIntoJavaLangComparable() {
        assertThat(CharSeq.empty()).isInstanceOf(Comparable.class);
    }

    @Test
    public void shouldCompareToWhenLessThan() {
        assertThat(CharSeq.of("a").compareTo(CharSeq.of("b"))).isEqualTo(-1);
    }

    @Test
    public void shouldCompareToWhenGreaterThan() {
        assertThat(CharSeq.of("b").compareTo(CharSeq.of("a"))).isEqualTo(1);
    }

    @Test
    public void shouldCompareToWhenEqualsThan() {
        assertThat(CharSeq.of("a").compareTo(CharSeq.of("a"))).isEqualTo(0);
    }

    // -- exists

    @Test
    public void shouldBeAwareOfExistingElement() {
        assertThat(CharSeq.of('1', '2').exists(i -> i == '2')).isTrue();
    }

    @Test
    public void shouldBeAwareOfNonExistingElement() {
        assertThat(CharSeq.empty().exists(i -> i == 1)).isFalse();
    }

    // -- forAll

    @Test
    public void shouldBeAwareOfPropertyThatHoldsForAll() {
        assertThat(CharSeq.of('2', '4').forAll(i -> i % 2 == 0)).isTrue();
    }

    @Test
    public void shouldBeAwareOfPropertyThatNotHoldsForAll() {
        assertThat(CharSeq.of('2', '3').forAll(i -> i % 2 == 0)).isFalse();
    }

    // -- padTo

    @Test
    public void shouldPadEmptyToEmpty() {
        assertThat(CharSeq.empty().padTo(0, 'a')).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldPadEmptyToNonEmpty() {
        assertThat(CharSeq.empty().padTo(2, 'a')).isEqualTo(CharSeq.of('a', 'a'));
    }

    @Test
    public void shouldPadNonEmptyZeroLen() {
        final CharSeq seq = CharSeq.of('a');
        assertThat(seq.padTo(0, 'b')).isSameAs(seq);
    }

    @Test
    public void shouldPadNonEmpty() {
        assertThat(CharSeq.of('a').padTo(2, 'a')).isEqualTo(CharSeq.of('a', 'a'));
        assertThat(CharSeq.of('a').padTo(2, 'b')).isEqualTo(CharSeq.of('a', 'b'));
        assertThat(CharSeq.of('a').padTo(3, 'b')).isEqualTo(CharSeq.of('a', 'b', 'b'));
    }

    @Test
    public void shouldThrowWhenPadToAddingNulls() {
        assertThatThrownBy(() -> CharSeq.of('a').padTo(2, null)).isInstanceOf(NullPointerException.class);
    }

    // -- leftPadTo

    @Test
    public void shouldLeftPadEmptyToEmpty() {
        assertThat(CharSeq.empty().leftPadTo(0, 'a')).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldLeftPadEmptyToNonEmpty() {
        assertThat(CharSeq.empty().leftPadTo(2, 'a')).isEqualTo(CharSeq.of('a', 'a'));
    }

    @Test
    public void shouldLeftPadNonEmptyZeroLen() {
        final CharSeq seq = CharSeq.of('a');
        assertThat(seq.leftPadTo(0, 'b')).isSameAs(seq);
    }

    @Test
    public void shouldLeftPadNonEmpty() {
        assertThat(CharSeq.of('a').leftPadTo(2, 'a')).isEqualTo(CharSeq.of('a', 'a'));
        assertThat(CharSeq.of('a').leftPadTo(2, 'b')).isEqualTo(CharSeq.of('b', 'a'));
        assertThat(CharSeq.of('a').leftPadTo(3, 'b')).isEqualTo(CharSeq.of('b', 'b', 'a'));
    }

    @Test
    public void shouldThrowWhenLeftPadToAddingNulls() {
        assertThatThrownBy(() -> CharSeq.of('a').leftPadTo(2, null)).isInstanceOf(NullPointerException.class);
    }

    // -- orElse

    @Test
    public void shouldCaclEmptyOrElseSameOther() {
        CharSeq other = CharSeq.of("42");
        assertThat(CharSeq.empty().orElse(other)).isSameAs(other);
    }

    @Test
    public void shouldCaclEmptyOrElseEqualOther() {
        assertThat(CharSeq.empty().orElse(Arrays.asList('1', '2'))).isEqualTo(CharSeq.of("12"));
    }

    @Test
    public void shouldCaclNonemptyOrElseOther() {
        CharSeq src = CharSeq.of("42");
        assertThat(src.orElse(CharSeq.of("12"))).isSameAs(src);
    }

    @Test
    public void shouldCaclEmptyOrElseSupplier() {
        Supplier<CharSeq> other = () -> CharSeq.of("42");
        assertThat(CharSeq.empty().orElse(other)).isEqualTo(CharSeq.of("42"));
    }

    @Test
    public void shouldCaclNonemptyOrElseSupplier() {
        CharSeq src = CharSeq.of("42");
        assertThat(src.orElse(() -> CharSeq.of("12"))).isSameAs(src);
    }

    // -- partition

    @Test
    public void shouldPartitionInOneIteration() {
        final AtomicInteger count = new AtomicInteger(0);
        final Tuple2<CharSeq, CharSeq> results = CharSeq.of('1', '2', '3').partition(c -> {
            count.incrementAndGet();
            return true;
        });
        assertThat(results._1).isEqualTo(CharSeq.of('1', '2', '3'));
        assertThat(results._2).isEqualTo(CharSeq.of());
        assertThat(count.get()).isEqualTo(3);
    }

    // -- patch

    @Test
    public void shouldPatchEmptyByEmpty() {
        assertThat(CharSeq.empty().patch(0, CharSeq.empty(), 0)).isSameAs(CharSeq.empty());
        assertThat(CharSeq.empty().patch(-1, CharSeq.empty(), -1)).isSameAs(CharSeq.empty());
        assertThat(CharSeq.empty().patch(-1, CharSeq.empty(), 1)).isSameAs(CharSeq.empty());
        assertThat(CharSeq.empty().patch(1, CharSeq.empty(), -1)).isSameAs(CharSeq.empty());
        assertThat(CharSeq.empty().patch(1, CharSeq.empty(), 1)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldPatchEmptyByNonEmpty() {
        final Seq<Character> s = CharSeq.of('1', '2', '3');
        assertThat(CharSeq.empty().patch(0, s, 0)).isEqualTo(s);
        assertThat(CharSeq.empty().patch(-1, s, -1)).isEqualTo(s);
        assertThat(CharSeq.empty().patch(-1, s, 1)).isEqualTo(s);
        assertThat(CharSeq.empty().patch(1, s, -1)).isEqualTo(s);
        assertThat(CharSeq.empty().patch(1, s, 1)).isEqualTo(s);
    }

    @Test
    public void shouldPatchNonEmptyByEmpty() {
        final Seq<Character> s = CharSeq.of('1', '2', '3');
        assertThat(s.patch(-1, CharSeq.empty(), -1)).isEqualTo(CharSeq.of('1', '2', '3'));
        assertThat(s.patch(-1, CharSeq.empty(), 0)).isEqualTo(CharSeq.of('1', '2', '3'));
        assertThat(s.patch(-1, CharSeq.empty(), 1)).isEqualTo(CharSeq.of('2', '3'));
        assertThat(s.patch(-1, CharSeq.empty(), 3)).isSameAs(CharSeq.empty());
        assertThat(s.patch(0, CharSeq.empty(), -1)).isEqualTo(CharSeq.of('1', '2', '3'));
        assertThat(s.patch(0, CharSeq.empty(), 0)).isEqualTo(CharSeq.of('1', '2', '3'));
        assertThat(s.patch(0, CharSeq.empty(), 1)).isEqualTo(CharSeq.of('2', '3'));
        assertThat(s.patch(0, CharSeq.empty(), 3)).isSameAs(CharSeq.empty());
        assertThat(s.patch(1, CharSeq.empty(), -1)).isEqualTo(CharSeq.of('1', '2', '3'));
        assertThat(s.patch(1, CharSeq.empty(), 0)).isEqualTo(CharSeq.of('1', '2', '3'));
        assertThat(s.patch(1, CharSeq.empty(), 1)).isEqualTo(CharSeq.of('1', '3'));
        assertThat(s.patch(1, CharSeq.empty(), 3)).isEqualTo(CharSeq.of('1'));
        assertThat(s.patch(4, CharSeq.empty(), -1)).isEqualTo(CharSeq.of('1', '2', '3'));
        assertThat(s.patch(4, CharSeq.empty(), 0)).isEqualTo(CharSeq.of('1', '2', '3'));
        assertThat(s.patch(4, CharSeq.empty(), 1)).isEqualTo(CharSeq.of('1', '2', '3'));
        assertThat(s.patch(4, CharSeq.empty(), 3)).isEqualTo(CharSeq.of('1', '2', '3'));
    }

    @Test
    public void shouldPatchNonEmptyByNonEmpty() {
        final Seq<Character> s = CharSeq.of('1', '2', '3');
        final Seq<Character> d = CharSeq.of('4', '5', '6');
        assertThat(s.patch(-1, d, -1)).isEqualTo(CharSeq.of('4', '5', '6', '1', '2', '3'));
        assertThat(s.patch(-1, d, 0)).isEqualTo(CharSeq.of('4', '5', '6', '1', '2', '3'));
        assertThat(s.patch(-1, d, 1)).isEqualTo(CharSeq.of('4', '5', '6', '2', '3'));
        assertThat(s.patch(-1, d, 3)).isEqualTo(CharSeq.of('4', '5', '6'));
        assertThat(s.patch(0, d, -1)).isEqualTo(CharSeq.of('4', '5', '6', '1', '2', '3'));
        assertThat(s.patch(0, d, 0)).isEqualTo(CharSeq.of('4', '5', '6', '1', '2', '3'));
        assertThat(s.patch(0, d, 1)).isEqualTo(CharSeq.of('4', '5', '6', '2', '3'));
        assertThat(s.patch(0, d, 3)).isEqualTo(CharSeq.of('4', '5', '6'));
        assertThat(s.patch(1, d, -1)).isEqualTo(CharSeq.of('1', '4', '5', '6', '2', '3'));
        assertThat(s.patch(1, d, 0)).isEqualTo(CharSeq.of('1', '4', '5', '6', '2', '3'));
        assertThat(s.patch(1, d, 1)).isEqualTo(CharSeq.of('1', '4', '5', '6', '3'));
        assertThat(s.patch(1, d, 3)).isEqualTo(CharSeq.of('1', '4', '5', '6'));
        assertThat(s.patch(4, d, -1)).isEqualTo(CharSeq.of('1', '2', '3', '4', '5', '6'));
        assertThat(s.patch(4, d, 0)).isEqualTo(CharSeq.of('1', '2', '3', '4', '5', '6'));
        assertThat(s.patch(4, d, 1)).isEqualTo(CharSeq.of('1', '2', '3', '4', '5', '6'));
        assertThat(s.patch(4, d, 3)).isEqualTo(CharSeq.of('1', '2', '3', '4', '5', '6'));
    }

    @Test
    public void shouldThrowWhenPatchingWithNulls() {
        assertThatThrownBy(() -> CharSeq.of('1').patch(0, List.of((Character) null), 1)).isInstanceOf(NullPointerException.class);
    }

    // -- peek

    @Test
    public void shouldPeekNil() {
        assertThat(CharSeq.empty().peek(t -> {})).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldPeekNonNilPerformingNoAction() {
        assertThat(CharSeq.of('1').peek(t -> {})).isEqualTo(CharSeq.of('1'));
    }

    @Test
    public void shouldPeekSingleValuePerformingAnAction() {
        final char[] effect = { 0 };
        final CharSeq actual = CharSeq.of('1').peek(i -> effect[0] = i);
        assertThat(actual).isEqualTo(CharSeq.of('1'));
        assertThat(effect[0]).isEqualTo('1');
    }

    // -- static rangeClosed()

    @Test
    public void shouldCreateRangeClosedWhereFromIsGreaterThanTo() {
        assertThat(CharSeq.rangeClosed('b', 'a')).isEmpty();
    }

    @Test
    public void shouldCreateRangeClosedWhereFromEqualsTo() {
        assertThat(CharSeq.rangeClosed('a', 'a')).isEqualTo(CharSeq.of('a'));
    }

    @Test
    public void shouldCreateRangeClosedWhereFromIsLessThanTo() {
        assertThat(CharSeq.rangeClosed('a', 'c')).isEqualTo(CharSeq.of('a', 'b', 'c'));
    }

    @Test
    public void shouldCreateRangeClosedWhereFromAndToEqualMIN_VALUE() {
        assertThat(CharSeq.rangeClosed(Character.MIN_VALUE, Character.MIN_VALUE)).isEqualTo(CharSeq.of(Character.MIN_VALUE));
    }

    @Test
    public void shouldCreateRangeClosedWhereFromAndToEqualMAX_VALUE() {
        assertThat(CharSeq.rangeClosed(Character.MAX_VALUE, Character.MAX_VALUE)).isEqualTo(CharSeq.of(Character.MAX_VALUE));
    }

    // -- static rangeClosedBy()

    @Test
    public void shouldCreateRangeClosedByWhereFromIsGreaterThanToAndStepWrongDirection() {
        assertThat(CharSeq.rangeClosedBy('b', 'a', 1)).isEmpty();
        assertThat(CharSeq.rangeClosedBy('b', 'a', 3)).isEmpty();
        assertThat(CharSeq.rangeClosedBy('a', 'b', -1)).isEmpty();
        assertThat(CharSeq.rangeClosedBy('a', 'b', -3)).isEmpty();
    }

    @Test
    public void shouldCreateRangeClosedByWhereFromEqualsTo() {
        assertThat(CharSeq.rangeClosedBy('a', 'a', 1)).isEqualTo(CharSeq.of('a'));
        assertThat(CharSeq.rangeClosedBy('a', 'a', 3)).isEqualTo(CharSeq.of('a'));
        assertThat(CharSeq.rangeClosedBy('a', 'a', -1)).isEqualTo(CharSeq.of('a'));
        assertThat(CharSeq.rangeClosedBy('a', 'a', -3)).isEqualTo(CharSeq.of('a'));
    }

    @Test
    public void shouldCreateRangeClosedByWhereFromIsLessThanToAndStepCorrectDirection() {
        assertThat(CharSeq.rangeClosedBy('a', 'c', 1)).isEqualTo(CharSeq.of('a', 'b', 'c'));
        assertThat(CharSeq.rangeClosedBy('a', 'e', 2)).isEqualTo(CharSeq.of('a', 'c', 'e'));
        assertThat(CharSeq.rangeClosedBy('a', 'f', 2)).isEqualTo(CharSeq.of('a', 'c', 'e'));
        assertThat(CharSeq.rangeClosedBy((char) (Character.MAX_VALUE - 2), Character.MAX_VALUE, 3)).isEqualTo(CharSeq.of((char) (Character.MAX_VALUE - 2)));
        assertThat(CharSeq.rangeClosedBy((char) (Character.MAX_VALUE - 3), Character.MAX_VALUE, 3)).isEqualTo(CharSeq.of((char) (Character.MAX_VALUE - 3), Character.MAX_VALUE));
        assertThat(CharSeq.rangeClosedBy('c', 'a', -1)).isEqualTo(CharSeq.of('c', 'b', 'a'));
        assertThat(CharSeq.rangeClosedBy('e', 'a', -2)).isEqualTo(CharSeq.of('e', 'c', 'a'));
        assertThat(CharSeq.rangeClosedBy('e', (char) ('a' - 1), -2)).isEqualTo(CharSeq.of('e', 'c', 'a'));
        assertThat(CharSeq.rangeClosedBy((char) (Character.MIN_VALUE + 2), Character.MIN_VALUE, -3)).isEqualTo(CharSeq.of((char) (Character.MIN_VALUE + 2)));
        assertThat(CharSeq.rangeClosedBy((char) (Character.MIN_VALUE + 3), Character.MIN_VALUE, -3)).isEqualTo(CharSeq.of((char) (Character.MIN_VALUE + 3), Character.MIN_VALUE));
    }

    @Test
    public void shouldCreateRangeClosedByWhereFromAndToEqualMIN_VALUE() {
        assertThat(CharSeq.rangeClosedBy(Character.MIN_VALUE, Character.MIN_VALUE, 1)).isEqualTo(CharSeq.of(Character.MIN_VALUE));
        assertThat(CharSeq.rangeClosedBy(Character.MIN_VALUE, Character.MIN_VALUE, 3)).isEqualTo(CharSeq.of(Character.MIN_VALUE));
        assertThat(CharSeq.rangeClosedBy(Character.MIN_VALUE, Character.MIN_VALUE, -1)).isEqualTo(CharSeq.of(Character.MIN_VALUE));
        assertThat(CharSeq.rangeClosedBy(Character.MIN_VALUE, Character.MIN_VALUE, -3)).isEqualTo(CharSeq.of(Character.MIN_VALUE));
    }

    @Test
    public void shouldCreateRangeClosedByWhereFromAndToEqualMAX_VALUE() {
        assertThat(CharSeq.rangeClosedBy(Character.MAX_VALUE, Character.MAX_VALUE, 1)).isEqualTo(CharSeq.of(Character.MAX_VALUE));
        assertThat(CharSeq.rangeClosedBy(Character.MAX_VALUE, Character.MAX_VALUE, 3)).isEqualTo(CharSeq.of(Character.MAX_VALUE));
        assertThat(CharSeq.rangeClosedBy(Character.MAX_VALUE, Character.MAX_VALUE, -1)).isEqualTo(CharSeq.of(Character.MAX_VALUE));
        assertThat(CharSeq.rangeClosedBy(Character.MAX_VALUE, Character.MAX_VALUE, -3)).isEqualTo(CharSeq.of(Character.MAX_VALUE));
    }

    // -- static range()

    @Test
    public void shouldCreateRangeWhereFromIsGreaterThanTo() {
        assertThat(CharSeq.range('b', 'a').isEmpty());
    }

    @Test
    public void shouldCreateRangeWhereFromEqualsTo() {
        assertThat(CharSeq.range('a', 'a')).isEmpty();
    }

    @Test
    public void shouldCreateRangeWhereFromIsLessThanTo() {
        assertThat(CharSeq.range('a', 'c')).isEqualTo(CharSeq.of('a', 'b'));
    }

    @Test
    public void shouldCreateRangeWhereFromAndToEqualMIN_VALUE() {
        assertThat(CharSeq.range(Character.MIN_VALUE, Character.MIN_VALUE)).isEmpty();
    }

    @Test
    public void shouldCreateRangeWhereFromAndToEqualMAX_VALUE() {
        assertThat(CharSeq.range(Character.MAX_VALUE, Character.MAX_VALUE)).isEmpty();
    }

    // -- static rangeBy()

    @Test
    public void shouldCreateRangeByWhereFromIsGreaterThanToAndStepWrongDirection() {
        assertThat(CharSeq.rangeBy('b', 'a', 1)).isEmpty();
        assertThat(CharSeq.rangeBy('b', 'a', 3)).isEmpty();
        assertThat(CharSeq.rangeBy('a', 'b', -1)).isEmpty();
        assertThat(CharSeq.rangeBy('a', 'b', -3)).isEmpty();
    }

    @Test
    public void shouldCreateRangeByWhereFromEqualsTo() {
        assertThat(CharSeq.rangeBy('a', 'a', 1)).isEmpty();
        assertThat(CharSeq.rangeBy('a', 'a', 3)).isEmpty();
        assertThat(CharSeq.rangeBy('a', 'a', -1)).isEmpty();
        assertThat(CharSeq.rangeBy('a', 'a', -3)).isEmpty();
    }

    @Test
    public void shouldCreateRangeByWhereFromIsLessThanToAndStepCorrectDirection() {
        assertThat(CharSeq.rangeBy('a', 'c', 1)).isEqualTo(CharSeq.of('a', 'b'));
        assertThat(CharSeq.rangeBy('a', 'd', 2)).isEqualTo(CharSeq.of('a', 'c'));
        assertThat(CharSeq.rangeBy('c', 'a', -1)).isEqualTo(CharSeq.of('c', 'b'));
        assertThat(CharSeq.rangeBy('d', 'a', -2)).isEqualTo(CharSeq.of('d', 'b'));
        assertThat(CharSeq.rangeBy((char) (Character.MAX_VALUE - 3), Character.MAX_VALUE, 3)).isEqualTo(CharSeq.of((char) (Character.MAX_VALUE - 3)));
        assertThat(CharSeq.rangeBy((char) (Character.MAX_VALUE - 4), Character.MAX_VALUE, 3)).isEqualTo(CharSeq.of((char) (Character.MAX_VALUE - 4), (char) (Character.MAX_VALUE - 1)));
        assertThat(CharSeq.rangeBy((char) (Character.MIN_VALUE + 3), Character.MIN_VALUE, -3)).isEqualTo(CharSeq.of((char) (Character.MIN_VALUE + 3)));
        assertThat(CharSeq.rangeBy((char) (Character.MIN_VALUE + 4), Character.MIN_VALUE, -3)).isEqualTo(CharSeq.of((char) (Character.MIN_VALUE + 4), (char) (Character.MIN_VALUE + 1)));
    }

    @Test
    public void shouldCreateRangeByWhereFromAndToEqualMIN_VALUE() {
        assertThat(CharSeq.rangeBy(Character.MIN_VALUE, Character.MIN_VALUE, 1)).isEmpty();
        assertThat(CharSeq.rangeBy(Character.MIN_VALUE, Character.MIN_VALUE, 3)).isEmpty();
        assertThat(CharSeq.rangeBy(Character.MIN_VALUE, Character.MIN_VALUE, -1)).isEmpty();
        assertThat(CharSeq.rangeBy(Character.MIN_VALUE, Character.MIN_VALUE, -3)).isEmpty();
    }

    @Test
    public void shouldCreateRangeByWhereFromAndToEqualMAX_VALUE() {
        assertThat(CharSeq.rangeBy(Character.MAX_VALUE, Character.MAX_VALUE, 1)).isEmpty();
        assertThat(CharSeq.rangeBy(Character.MAX_VALUE, Character.MAX_VALUE, 3)).isEmpty();
        assertThat(CharSeq.rangeBy(Character.MAX_VALUE, Character.MAX_VALUE, -1)).isEmpty();
        assertThat(CharSeq.rangeBy(Character.MAX_VALUE, Character.MAX_VALUE, -3)).isEmpty();
    }

    // step == 0

    @Test(expected = IllegalArgumentException.class)
    public void shouldProhibitCharRangeByStepZero() {
        CharSeq.rangeBy('a', 'b', 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldProhibitCharRangeClosedByStepZero() {
        CharSeq.rangeClosedBy('a', 'b', 0);
    }

    // -- average

    @Test
    public void shouldReturnNoneWhenComputingAverageOfNil() {
        assertThat(CharSeq.empty().average()).isEqualTo(Option.none());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowWhenComputingAverageOfStrings() {
        CharSeq.of('a', 'b', 'c').average();
    }

    // -- contains

    @Test
    public void shouldRecognizeNilContainsNoElement() {
        final boolean actual = CharSeq.empty().contains((Character) null);
        assertThat(actual).isFalse();
    }

    @Test
    public void shouldRecognizeNonNilDoesNotContainElement() {
        final boolean actual = CharSeq.of('1', '2', '3').contains('0');
        assertThat(actual).isFalse();
    }

    @Test
    public void shouldRecognizeNonNilDoesContainElement() {
        final boolean actual = CharSeq.of('1', '2', '3').contains('2');
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldRecognizeNonNilDoesNotContainCharSequence() {
        final boolean actual = CharSeq.of('1', '2', '3').contains("13");
        assertThat(actual).isFalse();
    }

    @Test
    public void shouldRecognizeNonNilDoesContainCharSequence() {
        final boolean actual = CharSeq.of('1', '2', '3').contains("23");
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldNotContainNull() {
        assertThat(CharSeq.of('a').contains((Character) null)).isFalse();
    }

    // -- containsAll

    @Test
    public void shouldRecognizeNilNotContainsAllElements() {
        final boolean actual = CharSeq.empty().containsAll(CharSeq.of('1', '2', '3'));
        assertThat(actual).isFalse();
    }

    @Test
    public void shouldRecognizeNonNilNotContainsAllOverlappingElements() {
        final boolean actual = CharSeq.of('1', '2', '3').containsAll(CharSeq.of('2', '3', '4'));
        assertThat(actual).isFalse();
    }

    @Test
    public void shouldRecognizeNonNilContainsAllOnSelf() {
        final boolean actual = CharSeq.of('1', '2', '3').containsAll(CharSeq.of('1', '2', '3'));
        assertThat(actual).isTrue();
    }

    // -- distinct

    @Test
    public void shouldComputeDistinctOfEmptyTraversable() {
        assertThat(CharSeq.empty().distinct()).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldComputeDistinctOfNonEmptyTraversable() {
        assertThat(CharSeq.of('1', '1', '2', '2', '3', '3').distinct()).isEqualTo(CharSeq.of('1', '2', '3'));
    }

    // -- distinct(Comparator)

    @Test
    public void shouldComputeDistinctByOfEmptyTraversableUsingComparator() {
        final Comparator<Character> comparator = Comparator.comparingInt(i -> i);
        assertThat(CharSeq.empty().distinctBy(comparator)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldComputeDistinctByOfNonEmptyTraversableUsingComparator() {
        final Comparator<Character> comparator = Comparator.comparingInt(s -> s);
        assertThat(CharSeq.of('1', '2', '3', '3', '4', '5').distinctBy(comparator))
                .isEqualTo(CharSeq.of('1', '2', '3', '4', '5'));
    }

    // -- distinct(Function)

    @Test
    public void shouldComputeDistinctByOfEmptyTraversableUsingKeyExtractor() {
        assertThat(CharSeq.empty().distinctBy(Function.identity())).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldComputeDistinctByOfNonEmptyTraversableUsingKeyExtractor() {
        assertThat(CharSeq.of('1', '2', '3', '3', '4', '5').distinctBy(c -> c))
                .isEqualTo(CharSeq.of('1', '2', '3', '4', '5'));
    }

    // -- drop

    @Test
    public void shouldDropNoneOnNil() {
        assertThat(CharSeq.empty().drop(1)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldDropNoneIfCountIsNegative() {
        final CharSeq t = CharSeq.of('1', '2', '3');
        assertThat(t.drop(-1)).isSameAs(t);
    }

    @Test
    public void shouldDropAsExpectedIfCountIsLessThanSize() {
        assertThat(CharSeq.of('1', '2', '3').drop(2)).isEqualTo(CharSeq.of('3'));
    }

    @Test
    public void shouldDropAllIfCountExceedsSize() {
        assertThat(CharSeq.of('1', '2', '3').drop('4')).isSameAs(CharSeq.empty());
    }

    // -- dropUntil

    @Test
    public void shouldDropUntilNoneOnNil() {
        assertThat(CharSeq.empty().dropUntil(ignored -> true)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldDropUntilNoneIfPredicateIsTrue() {
        assertThat(CharSeq.of('1', '2', '3').dropUntil(ignored -> true)).isEqualTo(CharSeq.of('1', '2', '3'));
    }

    @Test
    public void shouldDropUntilAllIfPredicateIsFalse() {
        assertThat(CharSeq.of('1', '2', '3').dropUntil(ignored -> false)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldDropUntilCorrect() {
        assertThat(CharSeq.of('1', '2', '3').dropUntil(i -> i >= '2')).isEqualTo(CharSeq.of('2', '3'));
    }

    // -- dropWhile

    @Test
    public void shouldDropWhileNoneOnNil() {
        assertThat(CharSeq.empty().dropWhile(ignored -> true)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldDropWhileNoneIfPredicateIsFalse() {
        final CharSeq t = CharSeq.of('1', '2', '3');
        assertThat(t.dropWhile(ignored -> false)).isSameAs(t);
    }

    @Test
    public void shouldDropWhileAllIfPredicateIsTrue() {
        assertThat(CharSeq.of('1', '2', '3').dropWhile(ignored -> true)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldDropWhileCorrect() {
        assertThat(CharSeq.of('1', '2', '3').dropWhile(i -> i == '1')).isEqualTo(CharSeq.of('2', '3'));
    }

    // -- dropRight

    @Test
    public void shouldDropRightNoneOnNil() {
        assertThat(CharSeq.empty().dropRight(1)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldDropRightNoneIfCountIsNegative() {
        final CharSeq t = CharSeq.of('1', '2', '3');
        assertThat(t.dropRight(-1)).isSameAs(t);
    }

    @Test
    public void shouldDropRightAsExpectedIfCountIsLessThanSize() {
        assertThat(CharSeq.of('1', '2', '3').dropRight(2)).isEqualTo(CharSeq.of('1'));
    }

    @Test
    public void shouldDropRightAllIfCountExceedsSize() {
        assertThat(CharSeq.of('1', '2', '3').dropRight(4)).isSameAs(CharSeq.empty());
    }

    // -- dropRightUntil

    @Test
    public void shouldDropRightUntilNoneOnNil() {
        assertThat(CharSeq.empty().dropRightUntil(ignored -> true)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldDropRightUntilNoneIfPredicateIsTrue() {
        final CharSeq t = CharSeq.of('1', '2', '3');
        assertThat(t.dropRightUntil(ignored -> true)).isSameAs(t);
    }

    @Test
    public void shouldDropRightUntilAllIfPredicateIsFalse() {
        assertThat(CharSeq.of('1', '2', '3').dropRightUntil(ignored -> false)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldDropRightUntilCorrect() {
        assertThat(CharSeq.of('1', '2', '3').dropRightUntil(i -> i == '2')).isEqualTo(CharSeq.of('1', '2'));
    }

    // -- dropRightWhile

    @Test
    public void shouldDropRightWhileNoneOnNil() {
        assertThat(CharSeq.empty().dropRightWhile(ignored -> true)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldDropRightWhileNoneIfPredicateIsFalse() {
        final CharSeq t = CharSeq.of('1', '2', '3');
        assertThat(t.dropRightWhile(ignored -> false)).isSameAs(t);
    }

    @Test
    public void shouldDropRightWhileAllIfPredicateIsTrue() {
        assertThat(CharSeq.of('1', '2', '3').dropRightWhile(ignored -> true)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldDropRightWhileAccordingToPredicate() {
        assertThat(CharSeq.of('1', '2', '3').dropRightWhile(i -> i != '2')).isEqualTo(CharSeq.of('1', '2'));
    }

    // -- existsUnique

    @Test
    public void shouldBeAwareOfExistingUniqueElement() {
        assertThat(CharSeq.of('1', '2').existsUnique(i -> i == '1')).isTrue();
    }

    @Test
    public void shouldBeAwareOfNonExistingUniqueElement() {
        assertThat(CharSeq.empty().existsUnique(i -> i == '1')).isFalse();
    }

    @Test
    public void shouldBeAwareOfExistingNonUniqueElement() {
        assertThat(CharSeq.of('1', '1', '2').existsUnique(i -> i == '1')).isFalse();
    }

    // -- fill

    @Test
    public void shouldFillTheCharSeqCallingTheSupplierInTheRightOrder() {
        final java.util.LinkedList<Character> chars = new java.util.LinkedList<>(asList('0', '1'));
        final CharSeq actual = CharSeq.fill(2, chars::remove);
        assertThat(actual).isEqualTo(CharSeq.of('0', '1'));
    }

    @Test
    public void shouldFillTheCharSeqWith0Elements() {
        assertThat(CharSeq.fill(0, () -> 'a')).isEqualTo(CharSeq.empty());
    }

    @Test
    public void shouldFillTheCharSeqWith0ElementsWhenNIsNegative() {
        assertThat(CharSeq.fill(-1, () -> 'a')).isEqualTo(CharSeq.empty());
    }

    @Test
    public void shouldThrowWhenFillingWithNullValue() {
        assertThatThrownBy(() -> CharSeq.fill(1, () -> null)).isInstanceOf(NullPointerException.class);
    }

    // -- filter

    @Test
    public void shouldFilterEmptyTraversable() {
        assertThat(CharSeq.empty().filter(ignored -> true)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldFilterNonEmptyTraversable() {
        assertThat(CharSeq.of('1', '2', '3', '4').filter(i -> i == '2' || i == '4')).isEqualTo(CharSeq.of('2', '4'));
    }

    @Test
    public void shouldFilterNonEmptyTraversableAllMatch() {
        final CharSeq t = CharSeq.of('1', '2', '3', '4');
        assertThat(t.filter(i -> true)).isSameAs(t);
    }

    // -- filterNot

    @Test
    public void shouldFilterNotOnEmptyTraversable() {
        assertThat(CharSeq.empty().filterNot(ignored -> true)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldFilterNotOnNonEmptyTraversable() {
        assertThat(CharSeq.of('1', '2', '3', '4').filterNot(i -> i == '2' || i == '4')).isEqualTo(CharSeq.of('1', '3'));
    }

    @Test
    public void shouldFilterNotOnNonEmptyTraversableNoneMatch() {
        final CharSeq t = CharSeq.of('1', '2', '3', '4');
        assertThat(t.filterNot(i -> false)).isSameAs(t);
    }

    // -- find

    @Test
    public void shouldFindFirstOfNil() {
        assertThat(CharSeq.empty().find(ignored -> true)).isEqualTo(Option.none());
    }

    @Test
    public void shouldFindFirstOfNonNil() {
        assertThat(CharSeq.of('1', '2', '3', '4').find(i -> i % 2 == 0)).isEqualTo(Option.of('2'));
    }

    // -- findLast

    @Test
    public void shouldFindLastOfNil() {
        assertThat(CharSeq.empty().findLast(ignored -> true)).isEqualTo(Option.none());
    }

    @Test
    public void shouldFindLastOfNonNil() {
        assertThat(CharSeq.of('1', '2', '3', '4').findLast(i -> i % 2 == 0)).isEqualTo(Option.of('4'));
    }

    // -- flatMap

    @Test
    public void shouldFlatMapEmptyTraversable() {
        assertThat(CharSeq.empty().flatMap(CharSeq::of)).isEqualTo(Vector.empty());
    }

    @Test
    public void shouldFlatMapNonEmptyTraversable() {
        assertThat(CharSeq.of('1', '2', '3').flatMap(CharSeq::of)).isEqualTo(Vector.of('1', '2', '3'));
    }

    @Test
    public void shouldFlatMapTraversableByExpandingElements() {
        assertThat(CharSeq.of('1', '2', '3').flatMap(i -> {
            switch (i) {
                case '1':
                    return CharSeq.of('1', '2', '3');
                case '2':
                    return CharSeq.of('4', '5');
                default:
                    return CharSeq.of('6');
            }
        })).isEqualTo(Vector.of('1', '2', '3', '4', '5', '6'));
    }

    @Test
    public void shouldFlatMapElementsToSequentialValuesInTheRightOrder() {
        final AtomicInteger seq = new AtomicInteger('0');
        final IndexedSeq<Character> actualInts = CharSeq.of('0', '1', '2').flatMap(
                ignored -> Vector.of((char) seq.getAndIncrement(), (char) seq.getAndIncrement()));
        final IndexedSeq<Character> expectedInts = Vector.of('0', '1', '2', '3', '4', '5');
        assertThat(actualInts).isEqualTo(expectedInts);
    }

    // -- flatMapChars()

    @Test
    public void shouldFlatMapChars() {
        assertThat(CharSeq.empty().flatMapChars(c -> "X")).isEqualTo(CharSeq.empty());
        assertThat(CharSeq.of('1', '2', '3').flatMapChars(c -> c == '1' ? "*" : "-")).isEqualTo(CharSeq.of("*--"));
    }

    // -- fold

    @Test
    public void shouldFoldNil() {
        assertThat(CharSeq.empty().fold('0', (a, b) -> b)).isEqualTo('0');
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenFoldNullOperator() {
        CharSeq.empty().fold(null, null);
    }

    @Test
    public void shouldFoldNonNil() {
        assertThat(CharSeq.of('1', '2', '3').fold('0', (a, b) -> b)).isEqualTo('3');
    }

    // -- foldLeft

    @Test
    public void shouldFoldLeftNil() {
        assertThat(CharSeq.empty().foldLeft("", (xs, x) -> xs + x)).isEqualTo("");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenFoldLeftNullOperator() {
        CharSeq.empty().foldLeft(null, null);
    }

    @Test
    public void shouldFoldLeftNonNil() {
        assertThat(CharSeq.of('a', 'b', 'c').foldLeft("", (xs, x) -> xs + x)).isEqualTo("abc");
    }

    // -- foldRight

    @Test
    public void shouldFoldRightNil() {
        assertThat(CharSeq.empty().foldRight("", (x, xs) -> x + xs)).isEqualTo("");
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenFoldRightNullOperator() {
        CharSeq.empty().foldRight(null, null);
    }

    @Test
    public void shouldFoldRightNonNil() {
        assertThat(CharSeq.of('a', 'b', 'c').foldRight("", (x, xs) -> x + xs)).isEqualTo("abc");
    }

    // -- head

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowWhenHeadOnNil() {
        CharSeq.empty().head();
    }

    @Test
    public void shouldReturnHeadOfNonNil() {
        assertThat(CharSeq.of('1', '2', '3').head()).isEqualTo('1');
    }

    // -- headOption

    @Test
    public void shouldReturnNoneWhenCallingHeadOptionOnNil() {
        assertThat(CharSeq.empty().headOption().isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnSomeHeadWhenCallingHeadOptionOnNonNil() {
        assertThat(CharSeq.of('1', '2', '3').headOption()).isEqualTo(Option.some('1'));
    }

    // -- hasDefiniteSize

    @Test
    public void shouldReturnSomethingOnHasDefiniteSize() {
        assertThat(CharSeq.empty().hasDefiniteSize()).isTrue();
    }

    // -- groupBy

    @Test
    public void shouldNilGroupBy() {
        assertThat(CharSeq.empty().groupBy(Function.identity())).isEqualTo(LinkedHashMap.empty());
    }

    @Test
    public void shouldNonNilGroupByIdentity() {
        final Map<?, ?> actual = CharSeq.of('a', 'b', 'c').groupBy(Function.identity());
        final Map<?, ?> expected = LinkedHashMap.empty().put('a', CharSeq.of('a')).put('b', CharSeq.of('b')).put('c', CharSeq.of('c'));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldNonNilGroupByEqual() {
        final Map<?, ?> actual = CharSeq.of('a', 'b', 'c').groupBy(c -> 1);
        final Map<?, ?> expected = LinkedHashMap.empty().put(1, CharSeq.of('a', 'b', 'c'));
        assertThat(actual).isEqualTo(expected);
    }

    // -- init

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowWhenInitOfNil() {
        CharSeq.empty().init();
    }

    @Test
    public void shouldGetInitOfNonNil() {
        assertThat(CharSeq.of('1', '2', '3').init()).isEqualTo(CharSeq.of('1', '2'));
    }

    // -- initOption

    @Test
    public void shouldReturnNoneWhenCallingInitOptionOnNil() {
        assertThat(CharSeq.empty().initOption().isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnSomeInitWhenCallingInitOptionOnNonNil() {
        assertThat(CharSeq.of('1', '2', '3').initOption()).isEqualTo(Option.some(CharSeq.of('1', '2')));
    }

    // -- isLazy

    @Test
    public void shouldVerifyLazyProperty() {
        assertThat(CharSeq.empty().isLazy()).isFalse();
        assertThat(CharSeq.of('1').isLazy()).isFalse();
    }

    // -- isEmpty

    @Test
    public void shouldRecognizeNil() {
        assertThat(CharSeq.empty().isEmpty()).isTrue();
    }

    @Test
    public void shouldRecognizeNonNil() {
        assertThat(CharSeq.of('1').isEmpty()).isFalse();
    }

    // -- iterator

    @Test
    public void shouldNotHasNextWhenNilIterator() {
        assertThat(CharSeq.empty().iterator().hasNext()).isFalse();
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowOnNextWhenNilIterator() {
        CharSeq.empty().iterator().next();
    }

    @Test
    public void shouldIterateFirstElementOfNonNil() {
        assertThat(CharSeq.of('1', '2', '3').iterator().next()).isEqualTo('1');
    }

    @Test
    public void shouldFullyIterateNonNil() {
        final Iterator<Character> iterator = CharSeq.of('1', '2', '3').iterator();
        int actual;
        for (int i = 1; i <= 3; i++) {
            actual = iterator.next();
            assertThat(actual).isEqualTo('0' + i);
        }
        assertThat(iterator.hasNext()).isFalse();
    }

    // -- mkString()

    @Test
    public void shouldMkStringNil() {
        assertThat(CharSeq.empty().mkString()).isEqualTo("");
    }

    @Test
    public void shouldMkStringNonNil() {
        assertThat(CharSeq.of('a', 'b', 'c').mkString()).isEqualTo("abc");
    }

    // -- mkString(delimiter)

    @Test
    public void shouldMkStringWithDelimiterNil() {
        assertThat(CharSeq.empty().mkString(",")).isEqualTo("");
    }

    @Test
    public void shouldMkStringWithDelimiterNonNil() {
        assertThat(CharSeq.of('a', 'b', 'c').mkString(",")).isEqualTo("a,b,c");
    }

    // -- mkString(delimiter, prefix, suffix)

    @Test
    public void shouldMkStringWithDelimiterAndPrefixAndSuffixNil() {
        assertThat(CharSeq.empty().mkString("[", ",", "]")).isEqualTo("[]");
    }

    @Test
    public void shouldMkStringWithDelimiterAndPrefixAndSuffixNonNil() {
        assertThat(CharSeq.of('a', 'b', 'c').mkString("[", ",", "]")).isEqualTo("[a,b,c]");
    }

    // -- last

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowWhenLastOnNil() {
        CharSeq.empty().last();
    }

    @Test
    public void shouldReturnLastOfNonNil() {
        assertThat(CharSeq.of('1', '2', '3').last()).isEqualTo('3');
    }

    // -- lastOption

    @Test
    public void shouldReturnNoneWhenCallingLastOptionOnNil() {
        assertThat(CharSeq.empty().lastOption().isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnSomeLastWhenCallingLastOptionOnNonNil() {
        assertThat(CharSeq.of('1', '2', '3').lastOption()).isEqualTo(Option.some('3'));
    }

    // -- length

    @Test
    public void shouldComputeLengthOfNil() {
        assertThat(CharSeq.empty().length()).isEqualTo(0);
    }

    @Test
    public void shouldComputeLengthOfNonNil() {
        assertThat(CharSeq.of('1', '2', '3').length()).isEqualTo(3);
    }

    // -- map

    @Test
    public void shouldMapNil() {
        assertThat(CharSeq.empty().map(i -> i + 1)).isEqualTo(Vector.empty());
    }

    @Test
    public void shouldMapNonNil() {
        assertThat(CharSeq.of('1', '2', '3').map(i -> (char) (i + 1))).isEqualTo(Vector.of('2', '3', '4'));
    }

    @Test
    public void shouldMapElementsToSequentialValuesInTheRightOrder() {
        final AtomicInteger seq = new AtomicInteger('0');
        final Traversable<Character> expectedInts = Vector.of('0', '1', '2', '3', '4');
        final Traversable<Character> actualInts = expectedInts.map(ignored -> (char) seq.getAndIncrement());
        assertThat(actualInts).isEqualTo(expectedInts);
    }

    @Test
    public void shouldMapToVectorWhenMapIsUsed() {
        assertThat(CharSeq.of('a', 'b', 'c').map(Integer::valueOf)).isInstanceOf(Vector.class);
    }

    @Test
    public void shouldMapToCharSeqWhenMapCharsIsUsed() {
        assertThat(CharSeq.empty().mapChars(c -> (char) (c + 1))).isEqualTo(CharSeq.empty());
        assertThat(CharSeq.of('a', 'b', 'c').mapChars(c -> (char) (c + 1))).isEqualTo(CharSeq.of("bcd"));
    }

    // -- partition

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenPartitionNilAndPredicateIsNull() {
        CharSeq.empty().partition(null);
    }

    @Test
    public void shouldPartitionNil() {
        assertThat(CharSeq.empty().partition(e -> true)).isEqualTo(Tuple.of(CharSeq.empty(), CharSeq.empty()));
    }

    @Test
    public void shouldPartitionIntsInOddAndEvenHavingOddAndEventNumbers() {
        assertThat(CharSeq.of('1', '2', '3', '4').partition(i -> i % 2 != 0))
                .isEqualTo(Tuple.of(CharSeq.of('1', '3'), CharSeq.of('2', '4')));
    }

    @Test
    public void shouldPartitionIntsInOddAndEvenHavingOnlyOddNumbers() {
        assertThat(CharSeq.of('1', '3').partition(i -> i % 2 != 0)).isEqualTo(Tuple.of(CharSeq.of('1', '3'), CharSeq.empty()));
    }

    @Test
    public void shouldPartitionIntsInOddAndEvenHavingOnlyEvenNumbers() {
        assertThat(CharSeq.of('2', '4').partition(i -> i % 2 != 0)).isEqualTo(Tuple.of(CharSeq.empty(), CharSeq.of('2', '4')));
    }

    // -- permutations

    @Test
    public void shouldComputePermutationsOfEmptyList() {
        assertThat(CharSeq.empty().permutations()).isEmpty();
    }

    @Test
    public void shouldComputePermutationsOfNonEmpty() {
        assertThat(CharSeq.of("123").permutations())
                .isEqualTo(Vector.of(CharSeq.of("123"), CharSeq.of("132"), CharSeq.of("213"), CharSeq.of("231"), CharSeq.of("312"), CharSeq.of("321")));
    }

    // -- max

    @Test
    public void shouldReturnNoneWhenComputingMaxOfNil() {
        assertThat(CharSeq.empty().max()).isEqualTo(Option.none());
    }

    @Test
    public void shouldComputeMaxOfChar() {
        assertThat(CharSeq.of('a', 'b', 'c').max()).isEqualTo(Option.some('c'));
    }

    // -- maxBy(Comparator)

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMaxByWithNullComparator() {
        CharSeq.of('1').maxBy((Comparator<Character>) null);
    }

    @Test
    public void shouldThrowWhenMaxByOfNil() {
        assertThat(CharSeq.empty().maxBy((o1, o2) -> 0)).isEqualTo(Option.none());
    }

    @Test
    public void shouldCalculateMaxByOfInts() {
        assertThat(CharSeq.of('1', '2', '3').maxBy(Comparator.comparingInt(i -> i))).isEqualTo(Option.some('3'));
    }

    @Test
    public void shouldCalculateInverseMaxByOfInts() {
        assertThat(CharSeq.of('1', '2', '3').maxBy((i1, i2) -> i2 - i1)).isEqualTo(Option.some('1'));
    }

    // -- maxBy(Function)

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMaxByWithNullFunction() {
        CharSeq.of('1').maxBy((Function<Character, Character>) null);
    }

    @Test
    public void shouldThrowWhenMaxByFunctionOfNil() {
        assertThat(CharSeq.empty().maxBy(i -> i)).isEqualTo(Option.none());
    }

    @Test
    public void shouldCalculateMaxByFunctionOfInts() {
        assertThat(CharSeq.of('1', '2', '3').maxBy(i -> i)).isEqualTo(Option.some('3'));
    }

    @Test
    public void shouldCalculateInverseMaxByFunctionOfInts() {
        assertThat(CharSeq.of('1', '2', '3').maxBy(i -> -i)).isEqualTo(Option.some('1'));
    }

    // -- min

    @Test
    public void shouldReturnNoneWhenComputingMinOfNil() {
        assertThat(CharSeq.empty().min()).isEqualTo(Option.none());
    }

    @Test
    public void shouldComputeMinOfChar() {
        assertThat(CharSeq.of('a', 'b', 'c').min()).isEqualTo(Option.some('a'));
    }

    // -- minBy(Comparator)

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMinByWithNullComparator() {
        CharSeq.of('1').minBy((Comparator<Character>) null);
    }

    @Test
    public void shouldThrowWhenMinByOfNil() {
        assertThat(CharSeq.empty().minBy((o1, o2) -> 0)).isEqualTo(Option.none());
    }

    @Test
    public void shouldCalculateMinByOfInts() {
        assertThat(CharSeq.of('1', '2', '3').minBy(Comparator.comparingInt(i -> i))).isEqualTo(Option.some('1'));
    }

    @Test
    public void shouldCalculateInverseMinByOfInts() {
        assertThat(CharSeq.of('1', '2', '3').minBy((i1, i2) -> i2 - i1)).isEqualTo(Option.some('3'));
    }

    // -- minBy(Function)

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenMinByWithNullFunction() {
        CharSeq.of('1').minBy((Function<Character, Character>) null);
    }

    @Test
    public void shouldThrowWhenMinByFunctionOfNil() {
        assertThat(CharSeq.empty().minBy(i -> i)).isEqualTo(Option.none());
    }

    @Test
    public void shouldCalculateMinByFunctionOfInts() {
        assertThat(CharSeq.of('1', '2', '3').minBy(i -> i)).isEqualTo(Option.some('1'));
    }

    @Test
    public void shouldCalculateInverseMinByFunctionOfInts() {
        assertThat(CharSeq.of('1', '2', '3').minBy(i -> -i)).isEqualTo(Option.some('3'));
    }

    // -- peek

    @Test
    public void shouldPeekNonNilPerformingAnAction() {
        final char[] effect = { 0 };
        final CharSeq actual = CharSeq.of('1', '2', '3').peek(i -> effect[0] = i);
        assertThat(actual).isEqualTo(CharSeq.of('1', '2', '3')); // traverses all elements in the lazy case
        assertThat(effect[0]).isEqualTo('1');
    }

    // -- product

    @Test
    public void shouldComputeProductOfNil() {
        assertThat(CharSeq.empty().product()).isEqualTo(1);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowWhenComputingProductOfStrings() {
        CharSeq.of('1', '2', '3').product();
    }

    // -- reduce

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowWhenReduceNil() {
        CharSeq.empty().reduce((a, b) -> a);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenReduceNullOperator() {
        CharSeq.empty().reduce(null);
    }

    @Test
    public void shouldReduceNonNil() {
        assertThat(CharSeq.of('1', '2', '3').reduce((a, b) -> b)).isEqualTo('3');
    }

    // -- reduceLeft

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowWhenReduceLeftNil() {
        CharSeq.empty().reduceLeft((a, b) -> a);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenReduceLeftNullOperator() {
        CharSeq.empty().reduceLeft(null);
    }

    @Test
    public void shouldReduceLeftNonNil() {
        assertThat(CharSeq.of('a', 'b', 'c').reduceLeft((xs, x) -> x)).isEqualTo('c');
    }

    // -- reduceRight

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowWhenReduceRightNil() {
        CharSeq.empty().reduceRight((a, b) -> a);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenReduceRightNullOperator() {
        CharSeq.empty().reduceRight(null);
    }

    @Test
    public void shouldReduceRightNonNil() {
        assertThat(CharSeq.of('a', 'b', 'c').reduceRight((x, xs) -> x)).isEqualTo('a');
    }

    // -- replace(curr, new)

    @Test
    public void shouldReplaceElementOfNilUsingCurrNew() {
        assertThat(CharSeq.empty().replace('1', '2')).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldReplaceElementOfNonNilUsingCurrNew() {
        assertThat(CharSeq.of('0', '1', '2', '1').replace('1', '3')).isEqualTo(CharSeq.of('0', '3', '2', '1'));
    }

    @Test
    public void shouldReturnSameInstanceWhenReplacingNullCharWithNullChar() {
        final CharSeq charSeq = CharSeq.of('a');
        assertThat(charSeq.replace((Character) null, (Character) null)).isSameAs(charSeq);
    }

    @Test
    public void shouldReturnSameInstanceWhenReplacingNullCharWithNonNullChar() {
        final CharSeq charSeq = CharSeq.of('a');
        assertThat(charSeq.replace((Character) null, 'b')).isSameAs(charSeq);
    }

    @Test
    public void shouldThrowWhenReplacingNonNullCharWithNullChar() {
        assertThatThrownBy(() -> CharSeq.of('a').replace('a', (Character) null)).isInstanceOf(NullPointerException.class);
    }

    // -- replaceAll(curr, new)

    @Test
    public void shouldReplaceAllElementsOfNilUsingCurrNew() {
        assertThat(CharSeq.empty().replaceAll('1', '2')).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldReplaceAllElementsOfNonNilUsingCurrNew() {
        assertThat(CharSeq.of('0', '1', '2', '1').replaceAll('1', '3')).isEqualTo(CharSeq.of('0', '3', '2', '3'));
    }

    @Test
    public void shouldReturnSameInstanceWhenReplacingAllNullCharWithNullChar() {
        final CharSeq charSeq = CharSeq.of('a');
        assertThat(charSeq.replaceAll((Character) null, (Character) null)).isSameAs(charSeq);
    }

    @Test
    public void shouldReturnSameInstanceWhenReplacingAllNullCharWithNonNullChar() {
        final CharSeq charSeq = CharSeq.of('a');
        assertThat(charSeq.replaceAll((Character) null, 'b')).isSameAs(charSeq);
    }

    @Test
    public void shouldThrowWhenReplacingAllNonNullCharWithNullChar() {
        assertThatThrownBy(() -> CharSeq.of('a').replaceAll('a', (Character) null)).isInstanceOf(NullPointerException.class);
    }

    // -- retainAll

    @Test
    public void shouldRetainAllElementsFromNil() {
        assertThat(CharSeq.empty().retainAll(CharSeq.of('1', '2', '3'))).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldRetainAllExistingElementsFromNonNil() {
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3').retainAll(CharSeq.of('1', '2')))
                .isEqualTo(CharSeq.of('1', '2', '1', '2'));
    }

    @Test
    public void shouldNotRetainAllNonExistingElementsFromNonNil() {
        assertThat(CharSeq.of('1', '2', '3').retainAll(CharSeq.of('4', '5'))).isSameAs(CharSeq.empty());
    }

    // -- shuffle

    @Test
    public void shouldShuffleEmpty() {
        assertThat(CharSeq.empty().shuffle().isEmpty());
    }

    @Test
    public void shouldShuffleHaveSameLength() {
        final CharSeq actual = CharSeq.of('1', '2', '3');
        assertThat(actual.shuffle().size()).isEqualTo(actual.size());
    }

    @Test
    public void shouldShuffleHaveSameElements() {
        final CharSeq actual = CharSeq.of('1', '2', '3');
        final CharSeq shuffled = actual.shuffle();
        assertThat(shuffled).containsOnlyOnce('1', '2', '3');
        assertThat(shuffled.indexOf(4)).isEqualTo(-1);
    }

    @Test
    public void shouldShuffleEmptyDeterministic() {
        assertThat(CharSeq.empty().shuffle(new Random(514662720L)).isEmpty());
    }

    @Test
    public void shouldShuffleHaveSameLengthDeterministic() {
        final CharSeq actual = CharSeq.of('1', '2', '3');
        assertThat(actual.shuffle(new Random(514662720L)).size()).isEqualTo(actual.size());
    }

    @Test
    public void shouldShuffleHaveSameElementsDeterministic() {
        final CharSeq actual = CharSeq.of('1', '2', '3');
        final CharSeq shuffled = actual.shuffle(new Random(514662720L));
        assertThat(shuffled).containsExactly('2', '3', '1');
        assertThat(shuffled.indexOf(4)).isEqualTo(-1);
    }

    // -- slideBy(classifier)

    @Test
    public void shouldSlideNilByClassifier() {
        assertThat(CharSeq.empty().slideBy(Function.identity())).isEmpty();
    }

    @Test
    public void shouldSlideSingularByClassifier() {
        final io.vavr.collection.List<Traversable<Character>> actual = CharSeq.of('1').slideBy(Function.identity()).toList().map(Vector::ofAll);
        final io.vavr.collection.List<Traversable<Character>> expected = io.vavr.collection.List.of(Vector.of('1'));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSlideNonNilByIdentityClassifier() {
        final io.vavr.collection.List<Traversable<Character>> actual = CharSeq.of('1', '2', '3').slideBy(Function.identity()).toList().map(Vector::ofAll);
        final io.vavr.collection.List<Traversable<Character>> expected = io.vavr.collection.List.of(Vector.of('1'), Vector.of('2'), Vector.of('3'));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSlideNonNilByConstantClassifier() {
        final io.vavr.collection.List<Traversable<Character>> actual = CharSeq.of('1', '2', '3').slideBy(e -> "same").toList().map(Vector::ofAll);
        final io.vavr.collection.List<Traversable<Character>> expected = io.vavr.collection.List.of(Vector.of('1', '2', '3'));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSlideNonNilBySomeClassifier() {
        final io.vavr.collection.List<Traversable<Character>> actual = CharSeq.of('1', '1', '1', '2', '2', '3', '4').slideBy(Function.identity()).toList().map(Vector::ofAll);
        final io.vavr.collection.List<Traversable<Character>> expected = io.vavr.collection.List.of(Vector.of('1', '1', '1'), Vector.of('2', '2'), Vector.of('3'), Vector.of('4'));
        assertThat(actual).isEqualTo(expected);
    }

    // -- sliding(size)

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenSlidingNilByZeroSize() {
        CharSeq.empty().sliding(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenSlidingNilByNegativeSize() {
        CharSeq.empty().sliding(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenSlidingNonNilByZeroSize() {
        CharSeq.of('1').sliding(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenSlidingNonNilByNegativeSize() {
        CharSeq.of('1').sliding(-1);
    }

    @Test
    public void shouldSlideNilBySize() {
        assertThat(CharSeq.empty().sliding(1).isEmpty()).isTrue();
    }

    @Test
    public void shouldSlideNonNilBySize1() {
        assertThat(CharSeq.of('1', '2', '3').sliding(1).toList())
                .isEqualTo(io.vavr.collection.List.of(CharSeq.of('1'), CharSeq.of('2'), CharSeq.of('3')));
    }

    @Test
    public void shouldSlideNonNilBySize2() {
        assertThat(CharSeq.of('1', '2', '3', '4', '5').sliding(2).toList())
                .isEqualTo(io.vavr.collection.List.of(CharSeq.of('1', '2'), CharSeq.of('2', '3'), CharSeq.of('3', '4'), CharSeq.of('4', '5')));
    }

    // -- sliding(size, step)

    @Test
    public void shouldSlideNilBySizeAndStep() {
        assertThat(CharSeq.empty().sliding(1, 1).isEmpty()).isTrue();
    }

    @Test
    public void shouldSlide5ElementsBySize2AndStep3() {
        assertThat(CharSeq.of('1', '2', '3', '4', '5').sliding(2, 3).toList())
                .isEqualTo(io.vavr.collection.List.of(CharSeq.of('1', '2'), CharSeq.of('4', '5')));
    }

    @Test
    public void shouldSlide5ElementsBySize2AndStep4() {
        assertThat(CharSeq.of('1', '2', '3', '4', '5').sliding(2, 4).toList())
                .isEqualTo(io.vavr.collection.List.of(CharSeq.of('1', '2'), CharSeq.of('5')));
    }

    @Test
    public void shouldSlide5ElementsBySize2AndStep5() {
        assertThat(CharSeq.of('1', '2', '3', '4', '5').sliding(2, 5).toList()).isEqualTo(io.vavr.collection.List.of(CharSeq.of('1', '2')));
    }

    @Test
    public void shouldSlide4ElementsBySize5AndStep3() {
        assertThat(CharSeq.of('1', '2', '3', '4').sliding(5, 3).toList())
                .isEqualTo(io.vavr.collection.List.of(CharSeq.of('1', '2', '3', '4')));
    }

    // -- span

    @Test
    public void shouldSpanNil() {
        assertThat(CharSeq.empty().span(i -> i < 2)).isEqualTo(Tuple.of(CharSeq.empty(), CharSeq.empty()));
    }

    @Test
    public void shouldSpanNonNil() {
        final CharSeq cs = CharSeq.of('0', '1', '2', '3');
        assertThat(cs.span(i -> i == '0' || i == '1'))
                .isEqualTo(Tuple.of(CharSeq.of('0', '1'), CharSeq.of('2', '3')));
        assertThat(cs.span(i -> false))
                .isEqualTo(Tuple.of(CharSeq.empty(), cs));
        assertThat(cs.span(i -> true))
                .isEqualTo(Tuple.of(cs, CharSeq.empty()));
    }

    // -- spliterator

    @Test
    public void shouldSplitNil() {
        final java.util.List<Character> actual = new java.util.ArrayList<>();
        CharSeq.empty().spliterator().forEachRemaining(actual::add);
        assertThat(actual).isEmpty();
    }

    @Test
    public void shouldSplitNonNil() {
        final java.util.List<Character> actual = new java.util.ArrayList<>();
        CharSeq.of('1', '2', '3').spliterator().forEachRemaining(actual::add);
        assertThat(actual).isEqualTo(asList('1', '2', '3'));
    }

    @Test
    public void shouldHaveImmutableSpliterator() {
        assertThat(CharSeq.of('1', '2', '3').spliterator().hasCharacteristics(Spliterator.IMMUTABLE)).isTrue();
    }

    @Test
    public void shouldHaveSizedSpliterator() {
        assertThat(CharSeq.of('1', '2', '3').spliterator()
                .hasCharacteristics(Spliterator.SIZED | Spliterator.SUBSIZED)).isTrue();
    }

    @Test
    public void shouldReturnSizeWhenSpliterator() {
        assertThat(CharSeq.of('1', '2', '3').spliterator().getExactSizeIfKnown()).isEqualTo(3);
    }

    // -- startsWith

    @Test
    public void shouldStartsNilOfNilCalculate() {
        assertThat(CharSeq.empty().startsWith(CharSeq.empty())).isTrue();
    }

    @Test
    public void shouldStartsNilOfNonNilCalculate() {
        assertThat(CharSeq.empty().startsWith(CharSeq.of('a'))).isFalse();
    }

    @Test
    public void shouldStartsNilOfNilWithOffsetCalculate() {
        assertThat(CharSeq.empty().startsWith(CharSeq.empty(), 1)).isFalse();
    }

    @Test
    public void shouldStartsNilOfNonNilWithOffsetCalculate() {
        assertThat(CharSeq.empty().startsWith(CharSeq.of('a'), 1)).isFalse();
    }

    @Test
    public void shouldStartsNonNilOfNilCalculate() {
        assertThat(CharSeq.of('a', 'b', 'c').startsWith(CharSeq.empty())).isTrue();
    }

    @Test
    public void shouldStartsNonNilOfNonNilCalculate() {
        assertThat(CharSeq.of('a', 'b', 'c').startsWith(CharSeq.of('a', 'b'))).isTrue();
        assertThat(CharSeq.of('a', 'b', 'c').startsWith(CharSeq.of('a', 'b', 'c'))).isTrue();
        assertThat(CharSeq.of('a', 'b', 'c').startsWith(CharSeq.of('a', 'b', 'c', 'd'))).isFalse();
        assertThat(CharSeq.of('a', 'b', 'c').startsWith(CharSeq.of('a', 'c'))).isFalse();
    }

    @Test
    public void shouldStartsNonNilOfNilWithOffsetCalculate() {
        assertThat(CharSeq.of('a', 'b', 'c').startsWith(CharSeq.empty(), 1)).isTrue();
    }

    @Test
    public void shouldStartsNonNilOfNonNilWithOffsetCalculate() {
        assertThat(CharSeq.of('a', 'b', 'c').startsWith(CharSeq.of('b', 'c'), 1)).isTrue();
        assertThat(CharSeq.of('a', 'b', 'c').startsWith(CharSeq.of('b', 'c', 'd'), 1)).isFalse();
        assertThat(CharSeq.of('a', 'b', 'c').startsWith(CharSeq.of('b', 'd'), 1)).isFalse();
    }

    // -- sum

    @Test
    public void shouldComputeSumOfNil() {
        assertThat(CharSeq.empty().sum()).isEqualTo(0);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowWhenComputingSumOfStrings() {
        CharSeq.of('1', '2', '3').sum();
    }

    // -- tabulate

    @Test
    public void shouldTabulateTheCharSeq() {
        final Function<Number, Character> f = i -> i.toString().charAt(0);
        final CharSeq actual = CharSeq.tabulate(3, f);
        assertThat(actual).isEqualTo(CharSeq.of('0', '1', '2'));
    }

    @Test
    public void shouldTabulateTheCharSeqCallingTheFunctionInTheRightOrder() {
        final java.util.LinkedList<Character> chars = new java.util.LinkedList<>(asList('0', '1', '2'));
        final CharSeq actual = CharSeq.tabulate(3, i -> chars.remove());
        assertThat(actual).isEqualTo(CharSeq.of('0', '1', '2'));
    }

    @Test
    public void shouldTabulateTheCharSeqWith0Elements() {
        assertThat(CharSeq.tabulate(0, i -> 'a')).isEqualTo(CharSeq.empty());
    }

    @Test
    public void shouldTabulateTheCharSeqWith0ElementsWhenNIsNegative() {
        assertThat(CharSeq.tabulate(-1, i -> 'a')).isEqualTo(CharSeq.empty());
    }

    @Test
    public void shouldThrowWhenTabulatingNullValues() {
        assertThatThrownBy(() -> CharSeq.tabulate(1, i -> null)).isInstanceOf(NullPointerException.class);
    }

    // -- take

    @Test
    public void shouldTakeNoneOnNil() {
        assertThat(CharSeq.empty().take(1)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldTakeNoneIfCountIsNegative() {
        assertThat(CharSeq.of('1', '2', '3').take(-1)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldTakeAsExpectedIfCountIsLessThanSize() {
        assertThat(CharSeq.of('1', '2', '3').take(2)).isEqualTo(CharSeq.of('1', '2'));
    }

    @Test
    public void shouldTakeAllIfCountExceedsSize() {
        final CharSeq t = CharSeq.of('1', '2', '3');
        assertThat(t.take(4)).isSameAs(t);
    }

    // -- takeUntil

    @Test
    public void shouldTakeUntilNoneOnNil() {
        assertThat(CharSeq.empty().takeUntil(x -> true)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldTakeUntilAllOnFalseCondition() {
        final CharSeq t = CharSeq.of('1', '2', '3');
        assertThat(t.takeUntil(x -> false)).isSameAs(t);
    }

    @Test
    public void shouldTakeUntilAllOnTrueCondition() {
        assertThat(CharSeq.of('1', '2', '3').takeUntil(x -> true)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldTakeUntilAsExpected() {
        assertThat(CharSeq.of('2', '4', '5', '6').takeUntil(x -> x % 2 != 0)).isEqualTo(CharSeq.of('2', '4'));
    }

    // -- takeWhile

    @Test
    public void shouldTakeWhileNoneOnNil() {
        assertThat(CharSeq.empty().takeWhile(x -> true)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldTakeWhileAllOnFalseCondition() {
        assertThat(CharSeq.of('1', '2', '3').takeWhile(x -> false)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldTakeWhileAllOnTrueCondition() {
        final CharSeq t = CharSeq.of('1', '2', '3');
        assertThat(t.takeWhile(x -> true)).isSameAs(t);
    }

    @Test
    public void shouldTakeWhileAsExpected() {
        assertThat(CharSeq.of('2', '4', '5', '6').takeWhile(x -> x % 2 == 0)).isEqualTo(CharSeq.of('2', '4'));
    }

    // -- takeRight

    @Test
    public void shouldTakeRightNoneOnNil() {
        assertThat(CharSeq.empty().takeRight(1)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldTakeRightNoneIfCountIsNegative() {
        assertThat(CharSeq.of('1', '2', '3').takeRight(-1)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldTakeRightAsExpectedIfCountIsLessThanSize() {
        assertThat(CharSeq.of('1', '2', '3').takeRight(2)).isEqualTo(CharSeq.of('2', '3'));
    }

    @Test
    public void shouldTakeRightAllIfCountExceedsSize() {
        final CharSeq t = CharSeq.of('1', '2', '3');
        assertThat(t.takeRight(4)).isSameAs(t);
    }

    // -- takeRightUntil

    @Test
    public void shouldTakeRightUntilNoneOnNil() {
        assertThat(CharSeq.empty().takeRightUntil(x -> true)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldTakeRightUntilAllOnFalseCondition() {
        final CharSeq t = CharSeq.of('1', '2', '3');
        assertThat(t.takeRightUntil(x -> false)).isSameAs(t);
    }

    @Test
    public void shouldTakeRightUntilAllOnTrueCondition() {
        assertThat(CharSeq.of('1', '2', '3').takeRightUntil(x -> true)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldTakeRightUntilAsExpected() {
        assertThat(CharSeq.of('2', '3', '4', '6').takeRightUntil(x -> x % 2 != 0)).isEqualTo(CharSeq.of('4', '6'));
    }

    // -- takeRightWhile

    @Test
    public void shouldTakeRightWhileNoneOnNil() {
        assertThat(CharSeq.empty().takeRightWhile(x -> true)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldTakeRightWhileAllOnFalseCondition() {
        assertThat(CharSeq.of('1', '2', '3').takeRightWhile(x -> false)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldTakeRightWhileAllOnTrueCondition() {
        final CharSeq t = CharSeq.of('1', '2', '3');
        assertThat(t.takeRightWhile(x -> true)).isSameAs(t);
    }

    @Test
    public void shouldTakeRightWhileAsExpected() {
        assertThat(CharSeq.of('2', '3', '4', '6').takeRightWhile(x -> x % 2 == 0)).isEqualTo(CharSeq.of('4', '6'));
    }

    // -- tail

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowWhenTailOnNil() {
        CharSeq.empty().tail();
    }

    @Test
    public void shouldReturnTailOfNonNil() {
        assertThat(CharSeq.of('1', '2', '3').tail()).isEqualTo(CharSeq.of('2', '3'));
    }

    // -- tailOption

    @Test
    public void shouldReturnNoneWhenCallingTailOptionOnNil() {
        assertThat(CharSeq.empty().tailOption().isEmpty()).isTrue();
    }

    @Test
    public void shouldReturnSomeTailWhenCallingTailOptionOnNonNil() {
        assertThat(CharSeq.of('1', '2', '3').tailOption()).isEqualTo(Option.some(CharSeq.of('2', '3')));
    }

    // -- toLowerCase

    @Test
    public void shouldConvertToLowerCase() {
        assertThat(CharSeq.of("Vavr").toLowerCase()).isEqualTo(CharSeq.of("vavr"));
        assertThat(CharSeq.of("Vavr").toLowerCase(Locale.ENGLISH)).isEqualTo(CharSeq.of("vavr"));
    }

    // -- toUpperCase

    @Test
    public void shouldConvertTotoUpperCase() {
        assertThat(CharSeq.of("Vavr").toUpperCase()).isEqualTo(CharSeq.of("VAVR"));
        assertThat(CharSeq.of("Vavr").toUpperCase(Locale.ENGLISH)).isEqualTo(CharSeq.of("VAVR"));
    }

    // -- capitalize

    @Test
    public void shouldCapitalize() {
        assertThat(CharSeq.of("vavr").capitalize()).isEqualTo(CharSeq.of("Vavr"));
        assertThat(CharSeq.of("").capitalize()).isEqualTo(CharSeq.of(""));
        assertThat(CharSeq.of("vavr").capitalize(Locale.ENGLISH)).isEqualTo(CharSeq.of("Vavr"));
        assertThat(CharSeq.of("").capitalize(Locale.ENGLISH)).isEqualTo(CharSeq.of(""));
    }

    // -- toJavaArray(Class)

    @Test
    public void shouldHaveOverloadedToJavaArray() {
        final Character[] actual = CharSeq.of('a', 'b', 'c').toJavaArray();
        assertThat(actual).isEqualTo(new Character[] {'a', 'b', 'c'});
    }

    @Test
    public void shouldConvertNilToJavaArray() {
        final Character[] actual = CharSeq.empty().toJavaArray(Character[]::new);
        final Character[] expected = new Character[] {};
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldConvertNonNilToJavaArray() {
        final Character[] array = CharSeq.of('1', '2').toJavaArray(Character[]::new);
        final Character[] expected = new Character[] { '1', '2' };
        assertThat(array).isEqualTo(expected);
    }

    // -- toJavaList

    @Test
    public void shouldConvertNilToArrayList() {
        assertThat(CharSeq.empty().toJavaList()).isEqualTo(new ArrayList<Integer>());
    }

    @Test
    public void shouldConvertNonNilToArrayList() {
        assertThat(CharSeq.of('1', '2', '3').toJavaList()).isEqualTo(asList('1', '2', '3'));
    }

    // -- toJavaMap(Function)

    @Test
    public void shouldConvertNilToHashMap() {
        assertThat(CharSeq.empty().toJavaMap(x -> Tuple.of(x, x))).isEqualTo(new java.util.HashMap<>());
    }

    @Test
    public void shouldConvertNonNilToHashMap() {
        final java.util.Map<Character, Character> expected = new java.util.HashMap<>();
        expected.put('1', '1');
        expected.put('2', '2');
        assertThat(CharSeq.of('1', '2').toJavaMap(x -> Tuple.of(x, x))).isEqualTo(expected);
    }

    // -- toJavaSet

    @Test
    public void shouldConvertNilToHashSet() {
        assertThat(CharSeq.empty().toJavaSet()).isEqualTo(new java.util.HashSet<>());
    }

    @Test
    public void shouldConvertNonNilToHashSet() {
        final java.util.Set<Character> expected = new java.util.HashSet<>();
        expected.add('2');
        expected.add('1');
        expected.add('3');
        assertThat(CharSeq.of('1', '2', '2', '3').toJavaSet()).isEqualTo(expected);
    }

    // ++++++ OBJECT ++++++

    // -- equals

    @Test
    public void shouldEqualSameTraversableInstance() {
        final Traversable<?> traversable = CharSeq.empty();
        assertThat(traversable).isEqualTo(traversable);
    }

    @Test
    public void shouldNilNotEqualsNull() {
        assertThat(CharSeq.empty()).isNotNull();
    }

    @Test
    public void shouldNonNilNotEqualsNull() {
        assertThat(CharSeq.of('1')).isNotNull();
    }

    @Test
    public void shouldEmptyNotEqualsDifferentType() {
        assertThat(CharSeq.empty()).isNotEqualTo("");
    }

    @Test
    public void shouldNonEmptyNotEqualsDifferentType() {
        assertThat(CharSeq.of('1')).isNotEqualTo("");
    }

    @Test
    public void shouldRecognizeEqualityOfNils() {
        assertThat(CharSeq.empty()).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldRecognizeEqualityOfNonNils() {
        assertThat(CharSeq.of('1', '2', '3').equals(CharSeq.of('1', '2', '3'))).isTrue();
    }

    @Test
    public void shouldRecognizeContentEqualityOfNonNil() {
        assertThat(CharSeq.of('1', '2', '3').contentEquals(new StringBuffer().append("123"))).isTrue();
        assertThat(CharSeq.of('1', '2', '3').contentEquals("123")).isTrue();
    }

    @Test
    public void shouldRecognizeNonEqualityOfTraversablesOfSameSize() {
        assertThat(CharSeq.of('1', '2', '3').equals(CharSeq.of('1', '2', '4'))).isFalse();
    }

    @Test
    public void shouldRecognizeNonEqualityOfTraversablesOfDifferentSize() {
        assertThat(CharSeq.of('1', '2', '3').equals(CharSeq.of('1', '2'))).isFalse();
    }

    // -- hashCode

    @Test
    public void shouldCalculateHashCodeOfNil() {
        assertThat(CharSeq.empty().hashCode() == CharSeq.empty().hashCode()).isTrue();
    }

    @Test
    public void shouldCalculateHashCodeOfNonNil() {
        assertThat(CharSeq.of('1', '2').hashCode() == CharSeq.of('1', '2').hashCode()).isTrue();
    }

    @Test
    public void shouldCalculateDifferentHashCodesForDifferentTraversables() {
        assertThat(CharSeq.of('1', '2').hashCode() != CharSeq.of('2', '3').hashCode()).isTrue();
    }

    // -- Serializable interface

    @Test
    public void shouldSerializeDeserializeNil() {
        final Object actual = Serializables.deserialize(Serializables.serialize(CharSeq.empty()));
        final Object expected = CharSeq.empty();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldPreserveSingletonInstanceOnDeserialization() {
        final boolean actual = Serializables.deserialize(Serializables.serialize(CharSeq.empty())) == CharSeq.empty();
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldSerializeDeserializeNonNil() {
        final Object actual = Serializables.deserialize(Serializables.serialize(CharSeq.of('1', '2', '3')));
        final Object expected = CharSeq.of('1', '2', '3');
        assertThat(actual).isEqualTo(expected);
    }

    // -- append

    @Test
    public void shouldAppendElementToNil() {
        final CharSeq actual = CharSeq.empty().append('1');
        final CharSeq expected = CharSeq.of('1');
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldAppendElementToNonNil() {
        final CharSeq actual = CharSeq.of('1', '2').append('3');
        final CharSeq expected = CharSeq.of('1', '2', '3');
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldThrowWhenAppendingNull() {
        assertThatThrownBy(() -> CharSeq.of('1').append(null)).isInstanceOf(NullPointerException.class);
    }

    // -- appendAll

    @Test(expected = NullPointerException.class)
    public void shouldThrowOnAppendAllOfNull() {
        CharSeq.empty().appendAll(null);
    }

    @Test
    public void shouldAppendAllNilToNil() {
        final CharSeq actual = CharSeq.empty().appendAll(CharSeq.empty());
        final CharSeq expected = CharSeq.empty();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldAppendAllNonNilToNil() {
        final CharSeq actual = CharSeq.empty().appendAll(CharSeq.of('1', '2', '3'));
        final CharSeq expected = CharSeq.of('1', '2', '3');
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldAppendAllNilToNonNil() {
        final CharSeq actual = CharSeq.of('1', '2', '3').appendAll(CharSeq.empty());
        final CharSeq expected = CharSeq.of('1', '2', '3');
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldAppendAllNonNilToNonNil() {
        final CharSeq actual = CharSeq.of('1', '2', '3').appendAll(CharSeq.of('4', '5', '6'));
        final CharSeq expected = CharSeq.of('1', '2', '3', '4', '5', '6');
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldThrowWhenAppendingAllIterableThatContainsNull() {
        assertThatThrownBy(() -> CharSeq.empty().appendAll(Arrays.asList('1', null))).isInstanceOf(NullPointerException.class);
    }

    // -- asPartialFunction

    @Test
    public void shouldUseSeqAsPartialFunction() {
        assertThat(CharSeq.of('1', '2', '3').asPartialFunction().apply(1)).isEqualTo('2');
    }

    // -- combinations

    @Test
    public void shouldComputeCombinationsOfEmptyList() {
        assertThat(CharSeq.empty().combinations()).isEqualTo(Vector.of(CharSeq.empty()));
    }

    @Test
    public void shouldComputeCombinationsOfNonEmptyList() {
        assertThat(CharSeq.of("123").combinations()).isEqualTo(Vector.of(CharSeq.empty(), CharSeq.of("1"), CharSeq.of("2"), CharSeq.of("3"), CharSeq.of("12"), CharSeq.of("13"), CharSeq.of("23"), CharSeq.of("123")));
    }

    // -- combinations(k)

    @Test
    public void shouldComputeKCombinationsOfEmptyList() {
        assertThat(CharSeq.empty().combinations(1)).isEmpty();
    }

    @Test
    public void shouldComputeKCombinationsOfNonEmptyList() {
        assertThat(CharSeq.of("123").combinations(2)).isEqualTo(Vector.of(CharSeq.of("12"), CharSeq.of("13"), CharSeq.of("23")));
    }

    @Test
    public void shouldComputeKCombinationsOfNegativeK() {
        assertThat(CharSeq.of("1").combinations(-1)).isEqualTo(Vector.of(CharSeq.empty()));
    }

    // -- containsSlice

    @Test
    public void shouldRecognizeNilNotContainsSlice() {
        final boolean actual = CharSeq.empty().containsSlice(CharSeq.of('1', '2', '3'));
        assertThat(actual).isFalse();
    }

    @Test
    public void shouldRecognizeNonNilDoesContainSlice() {
        final boolean actual = CharSeq.of('1', '2', '3', '4', '5').containsSlice(CharSeq.of('2', '3'));
        assertThat(actual).isTrue();
    }

    @Test
    public void shouldRecognizeNonNilDoesNotContainSlice() {
        final boolean actual = CharSeq.of('1', '2', '3', '4', '5').containsSlice(CharSeq.of('2', '1', '4'));
        assertThat(actual).isFalse();
    }

    // -- crossProduct()

    @Test
    public void shouldCalculateCrossProductOfNil() {
        final Iterator<Tuple2<Character, Character>> actual = CharSeq.empty().crossProduct();
        assertThat(actual).isEmpty();
    }

    @Test
    public void shouldCalculateCrossProductOfNonNil() {
        final io.vavr.collection.List<Tuple2<Character, Character>> actual = CharSeq.of('1', '2', '3').crossProduct().toList();
        final io.vavr.collection.List<Tuple2<Character, Character>> expected = Iterator.of(Tuple.of('1', '1'), Tuple.of('1', '2'),
                Tuple.of('1', '3'), Tuple.of('2', '1'), Tuple.of('2', '2'), Tuple.of('2', '3'), Tuple.of('3', '1'),
                Tuple.of('3', '2'), Tuple.of('3', '3')).toList();
        assertThat(actual).isEqualTo(expected);
    }

    // -- crossProduct(int)

    @Test
    public void shouldCalculateCrossProductPower() {
        final io.vavr.collection.List<CharSeq> actual = CharSeq.of("12").crossProduct(2).toList();
        final io.vavr.collection.List<CharSeq> expected = Iterator.of(CharSeq.of('1', '1'), CharSeq.of('1', '2'), CharSeq.of('2', '1'), CharSeq.of('2', '2')).toList();
        assertThat(actual).isEqualTo(expected);
    }

    // -- crossProduct(Iterable)

    @Test
    public void shouldCalculateCrossProductOfNilAndNil() {
        final Traversable<Tuple2<Character, Object>> actual = CharSeq.empty().crossProduct(CharSeq.empty());
        assertThat(actual).isEmpty();
    }

    @Test
    public void shouldCalculateCrossProductOfNilAndNonNil() {
        final Traversable<Tuple2<Character, Object>> actual = CharSeq.empty().crossProduct(CharSeq.of('1', '2', '3'));
        assertThat(actual).isEmpty();
    }

    @Test
    public void shouldCalculateCrossProductOfNonNilAndNil() {
        final Traversable<Tuple2<Character, Character>> actual =
                CharSeq.of('1', '2', '3')
                        .crossProduct(CharSeq.empty());
        assertThat(actual).isEmpty();
    }

    @Test
    public void shouldCalculateCrossProductOfNonNilAndNonNil() {
        final io.vavr.collection.List<Tuple2<Character, Character>> actual =
                CharSeq.of('1', '2', '3')
                        .crossProduct(CharSeq.of('a', 'b'))
                        .toList();
        final io.vavr.collection.List<Tuple2<Character, Character>> expected = Vector.of(Tuple.of('1', 'a'), Tuple.of('1', 'b'),
                Tuple.of('2', 'a'), Tuple.of('2', 'b'), Tuple.of('3', 'a'), Tuple.of('3', 'b')).toList();
        assertThat(actual).isEqualTo(expected);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowWhenCalculatingCrossProductAndThatIsNull() {
        CharSeq.empty().crossProduct(null);
    }

    // -- get

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenGetWithNegativeIndexOnNil() {
        CharSeq.empty().get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenGetWithNegativeIndexOnNonNil() {
        CharSeq.of('1').get(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenGetOnNil() {
        CharSeq.empty().get(0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenGetWithTooBigIndexOnNonNil() {
        CharSeq.of('1').get(1);
    }

    @Test
    public void shouldGetFirstElement() {
        assertThat(CharSeq.of('1', '2', '3').get(0)).isEqualTo('1');
    }

    @Test
    public void shouldGetLastElement() {
        assertThat(CharSeq.of('1', '2', '3').get(2)).isEqualTo('3');
    }

    // -- getOption

    @Test
    public void shouldReturnNoneWhenGetOptionWithNegativeIndexOnNil() {
        assertThat(CharSeq.empty().getOption(-1)).isEqualTo(Option.none());
    }

    @Test
    public void shouldReturnNoneWhenGetOptionWithNegativeIndexOnNonNil() {
        assertThat(CharSeq.of('1').getOption(-1)).isEqualTo(Option.none());
    }

    @Test
    public void shouldReturnNoneWhenGetOptionOnNil() {
        assertThat(CharSeq.empty().getOption(0)).isEqualTo(Option.none());
    }

    @Test
    public void shouldReturnNoneWhenGetOptionWithTooBigIndexOnNonNil() {
        assertThat(CharSeq.of('1').getOption(1)).isEqualTo(Option.none());
    }

    @Test
    public void shouldGetOptionFirstElement() {
        assertThat(CharSeq.of('1', '2', '3').getOption(0)).isEqualTo(Option.of('1'));
    }

    @Test
    public void shouldGetOptionLastElement() {
        assertThat(CharSeq.of('1', '2', '3').getOption(2)).isEqualTo(Option.of('3'));
    }

    // -- getOrElse

    @Test
    public void shouldReturnDefaultWhenGetOrElseWithNegativeIndexOnNil() {
        assertThat(CharSeq.empty().getOrElse(-1, 'x')).isEqualTo('x');
    }

    @Test
    public void shouldReturnDefaultWhenGetOrElseWithNegativeIndexOnNonNil() {
        assertThat(CharSeq.of('1').getOrElse(-1, 'y')).isEqualTo('y');
    }

    @Test
    public void shouldReturnDefaultWhenGetOrElseOnNil() {
        assertThat(CharSeq.empty().getOrElse(0, 'z')).isEqualTo('z');
    }

    @Test
    public void shouldReturnDefaultWhenGetOrElseWithTooBigIndexOnNonNil() {
        assertThat(CharSeq.of('1').getOrElse(1, 'z')).isEqualTo('z');
    }

    @Test
    public void shouldGetOrElseFirstElement() {
        assertThat(CharSeq.of('1', '2', '3').getOrElse(0, 'x')).isEqualTo('1');
    }

    @Test
    public void shouldGetOrElseLastElement() {
        assertThat(CharSeq.of('1', '2', '3').getOrElse(2, 'x')).isEqualTo('3');
    }

    // -- grouped

    @Test
    public void shouldGroupedNil() {
        assertThat(CharSeq.empty().grouped(1).isEmpty()).isTrue();
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenGroupedWithSizeZero() {
        CharSeq.empty().grouped(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowWhenGroupedWithNegativeSize() {
        CharSeq.empty().grouped(-1);
    }

    @Test
    public void shouldGroupedTraversableWithEqualSizedBlocks() {
        final io.vavr.collection.List<CharSeq> actual = CharSeq.of('1', '2', '3', '4').grouped(2).toList();
        final io.vavr.collection.List<CharSeq> expected = io.vavr.collection.List.of(CharSeq.of('1', '2'), CharSeq.of('3', '4'));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldGroupedTraversableWithRemainder() {
        final io.vavr.collection.List<CharSeq> actual = CharSeq.of('1', '2', '3', '4', '5').grouped(2).toList();
        final io.vavr.collection.List<CharSeq> expected = io.vavr.collection.List.of(CharSeq.of('1', '2'), CharSeq.of('3', '4'), CharSeq.of('5'));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldGroupedWhenTraversableLengthIsSmallerThanBlockSize() {
        final io.vavr.collection.List<CharSeq> actual = CharSeq.of('1', '2', '3', '4').grouped(5).toList();
        final io.vavr.collection.List<CharSeq> expected = io.vavr.collection.List.of(CharSeq.of('1', '2', '3', '4'));
        assertThat(actual).isEqualTo(expected);
    }

    // -- indexOf

    @Test
    public void shouldNotFindIndexOfElementWhenSeqIsEmpty() {
        assertThat(CharSeq.empty().indexOf(1)).isEqualTo(-1);

        assertThat(CharSeq.empty().indexOfOption(1)).isEqualTo(Option.none());
    }

    @Test
    public void shouldNotFindIndexOfElementWhenStartIsGreater() {
        assertThat(CharSeq.of('1', '2', '3', '4').indexOf(2, 2)).isEqualTo(-1);

        assertThat(CharSeq.of('1', '2', '3', '4').indexOfOption(2, 2)).isEqualTo(Option.none());
    }

    @Test
    public void shouldFindIndexOfFirstElement() {
        assertThat(CharSeq.of('1', '2', '3').indexOf('1')).isEqualTo(0);
        assertThat(CharSeq.of('1', '2', '3').indexOf(Character.valueOf('1'))).isEqualTo(0);

        assertThat(CharSeq.of('1', '2', '3').indexOfOption('1')).isEqualTo(Option.some(0));
        assertThat(CharSeq.of('1', '2', '3').indexOfOption(Character.valueOf('1'))).isEqualTo(Option.some(0));
    }

    @Test
    public void shouldFindIndexOfInnerElement() {
        assertThat(CharSeq.of('1', '2', '3').indexOf('2')).isEqualTo(1);
        assertThat(CharSeq.of('1', '2', '3').indexOf(Character.valueOf('2'))).isEqualTo(1);

        assertThat(CharSeq.of('1', '2', '3').indexOfOption('2')).isEqualTo(Option.some(1));
        assertThat(CharSeq.of('1', '2', '3').indexOfOption(Character.valueOf('2'))).isEqualTo(Option.some(1));
    }

    @Test
    public void shouldFindIndexOfLastElement() {
        assertThat(CharSeq.of('1', '2', '3').indexOf('3')).isEqualTo(2);
        assertThat(CharSeq.of('1', '2', '3').indexOf(Character.valueOf('3'))).isEqualTo(2);

        assertThat(CharSeq.of('1', '2', '3').indexOfOption('3')).isEqualTo(Option.some(2));
        assertThat(CharSeq.of('1', '2', '3').indexOfOption(Character.valueOf('3'))).isEqualTo(Option.some(2));
    }

    // -- indexOfSlice

    @Test
    public void shouldNotFindIndexOfSliceWhenSeqIsEmpty() {
        assertThat(CharSeq.empty().indexOfSlice(CharSeq.of('2', '3'))).isEqualTo(-1);

        assertThat(CharSeq.empty().indexOfSliceOption(CharSeq.of('2', '3'))).isEqualTo(Option.none());
    }

    @Test
    public void shouldNotFindIndexOfSliceWhenStartIsGreater() {
        assertThat(CharSeq.of('1', '2', '3', '4').indexOfSlice(CharSeq.of('2', '3'), 2)).isEqualTo(-1);

        assertThat(CharSeq.of('1', '2', '3', '4').indexOfSliceOption(CharSeq.of('2', '3'), 2)).isEqualTo(Option.none());
    }

    @Test
    public void shouldFindIndexOfFirstSlice() {
        assertThat(CharSeq.of('1', '2', '3', '4').indexOfSlice(CharSeq.of('1', '2'))).isEqualTo(0);

        assertThat(CharSeq.of('1', '2', '3', '4').indexOfSliceOption(CharSeq.of('1', '2'))).isEqualTo(Option.some(0));
    }

    @Test
    public void shouldFindIndexOfInnerSlice() {
        assertThat(CharSeq.of('1', '2', '3', '4').indexOfSlice(CharSeq.of('2', '3'))).isEqualTo(1);

        assertThat(CharSeq.of('1', '2', '3', '4').indexOfSliceOption(CharSeq.of('2', '3'))).isEqualTo(Option.some(1));
    }

    @Test
    public void shouldFindIndexOfLastSlice() {
        assertThat(CharSeq.of('1', '2', '3').indexOfSlice(CharSeq.of('2', '3'))).isEqualTo(1);

        assertThat(CharSeq.of('1', '2', '3').indexOfSliceOption(CharSeq.of('2', '3'))).isEqualTo(Option.some(1));
    }

    // -- lastIndexOf

    @Test
    public void shouldNotFindLastIndexOfElementWhenSeqIsEmpty() {
        assertThat(CharSeq.empty().lastIndexOf(1)).isEqualTo(-1);

        assertThat(CharSeq.empty().lastIndexOfOption(1)).isEqualTo(Option.none());
    }

    @Test
    public void shouldNotFindLastIndexOfElementWhenEndIdLess() {
        assertThat(CharSeq.of('1', '2', '3', '4').lastIndexOf(3, 1)).isEqualTo(-1);

        assertThat(CharSeq.of('1', '2', '3', '4').lastIndexOfOption(3, 1)).isEqualTo(Option.none());
    }

    @Test
    public void shouldFindLastIndexOfElement() {
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3').lastIndexOf('1')).isEqualTo(3);
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3').lastIndexOf(Character.valueOf('1'))).isEqualTo(3);

        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3').lastIndexOfOption('1')).isEqualTo(Option.some(3));
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3').lastIndexOfOption(Character.valueOf('1'))).isEqualTo(Option.some(3));
    }

    @Test
    public void shouldFindLastIndexOfElementWithEnd() {
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3').lastIndexOf('1', 1)).isEqualTo(0);
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3').lastIndexOf(Character.valueOf('1'), 1)).isEqualTo(0);

        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3').lastIndexOfOption('1', 1)).isEqualTo(Option.some(0));
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3').lastIndexOfOption(Character.valueOf('1'), 1)).isEqualTo(Option.some(0));
    }

    // -- lastIndexOfSlice

    @Test
    public void shouldNotFindLastIndexOfSliceWhenSeqIsEmpty() {
        assertThat(CharSeq.empty().lastIndexOfSlice(CharSeq.of('2', '3'))).isEqualTo(-1);

        assertThat(CharSeq.empty().lastIndexOfSliceOption(CharSeq.of('2', '3'))).isEqualTo(Option.none());
    }

    @Test
    public void shouldNotFindLastIndexOfSliceWhenEndIdLess() {
        assertThat(CharSeq.of('1', '2', '3', '4', '5').lastIndexOfSlice(CharSeq.of('3', '4'), 1)).isEqualTo(-1);

        assertThat(CharSeq.of('1', '2', '3', '4', '5').lastIndexOfSliceOption(CharSeq.of('3', '4'), 1)).isEqualTo(Option.none());
    }

    @Test
    public void shouldFindLastIndexOfSlice() {
        assertThat(CharSeq.of('1', '2', '3', '1', '2').lastIndexOfSlice(CharSeq.empty())).isEqualTo(5);
        assertThat(CharSeq.of('1', '2', '3', '1', '2').lastIndexOfSlice(CharSeq.of('2'))).isEqualTo(4);
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3', '4').lastIndexOfSlice(CharSeq.of('2', '3'))).isEqualTo(4);

        assertThat(CharSeq.of('1', '2', '3', '1', '2').lastIndexOfSliceOption(CharSeq.empty())).isEqualTo(Option.some(5));
        assertThat(CharSeq.of('1', '2', '3', '1', '2').lastIndexOfSliceOption(CharSeq.of('2'))).isEqualTo(Option.some(4));
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3', '4').lastIndexOfSliceOption(CharSeq.of('2', '3'))).isEqualTo(Option.some(4));
    }

    @Test
    public void shouldFindLastIndexOfSliceWithEnd() {
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3').lastIndexOfSlice(CharSeq.empty(), 2)).isEqualTo(2);
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3').lastIndexOfSlice(CharSeq.of('2'), 2)).isEqualTo(1);
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3').lastIndexOfSlice(CharSeq.of('2', '3'), 2)).isEqualTo(1);
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3', '4').lastIndexOfSlice(CharSeq.of('2', '3'), 2)).isEqualTo(1);

        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3').lastIndexOfSliceOption(CharSeq.empty(), 2)).isEqualTo(Option.some(2));
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3').lastIndexOfSliceOption(CharSeq.of('2'), 2)).isEqualTo(Option.some(1));
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3').lastIndexOfSliceOption(CharSeq.of('2', '3'), 2)).isEqualTo(Option.some(1));
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3', '4').lastIndexOfSliceOption(CharSeq.of('2', '3'), 2)).isEqualTo(Option.some(1));
    }

    // -- insert

    @Test
    public void shouldInsertIntoNil() {
        final CharSeq actual = CharSeq.empty().insert(0, '1');
        final CharSeq expected = CharSeq.of('1');
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldInsertInFrontOfElement() {
        final CharSeq actual = CharSeq.of('4').insert(0, '1');
        final CharSeq expected = CharSeq.of('1', '4');
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldInsertBehindOfElement() {
        final CharSeq actual = CharSeq.of('4').insert(1, '1');
        final CharSeq expected = CharSeq.of('4', '1');
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldInsertIntoSeq() {
        final CharSeq actual = CharSeq.of('1', '2', '3').insert(2, '4');
        final CharSeq expected = CharSeq.of('1', '2', '4', '3');
        assertThat(actual).isEqualTo(expected);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenInsertOnNonNilWithNegativeIndex() {
        CharSeq.of('1').insert(-1, null);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenInsertOnNilWithNegativeIndex() {
        CharSeq.empty().insert(-1, null);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowOnInsertWhenExceedingUpperBound() {
        CharSeq.empty().insert(1, null);
    }

    @Test
    public void shouldThrowWhenInsertingNull() {
        assertThatThrownBy(() -> CharSeq.empty().insert(0, null)).isInstanceOf(NullPointerException.class);
    }

    // -- insertAll

    @Test
    public void shouldInsertAllIntoNil() {
        final CharSeq actual = CharSeq.empty().insertAll(0, CharSeq.of('1', '2', '3'));
        final CharSeq expected = CharSeq.of('1', '2', '3');
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldInsertAllInFrontOfElement() {
        final CharSeq actual = CharSeq.of('4').insertAll(0, CharSeq.of('1', '2', '3'));
        final CharSeq expected = CharSeq.of('1', '2', '3', '4');
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldInsertAllBehindOfElement() {
        final CharSeq actual = CharSeq.of('4').insertAll(1, CharSeq.of('1', '2', '3'));
        final CharSeq expected = CharSeq.of('4', '1', '2', '3');
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldInsertAllIntoSeq() {
        final CharSeq actual = CharSeq.of('1', '2', '3').insertAll(2, CharSeq.of('4', '5'));
        final CharSeq expected = CharSeq.of('1', '2', '4', '5', '3');
        assertThat(actual).isEqualTo(expected);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowOnInsertAllWithNil() {
        CharSeq.empty().insertAll(0, null);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenInsertOnNonNilAllWithNegativeIndex() {
        CharSeq.of('1').insertAll(-1, CharSeq.empty());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenInsertOnNilAllWithNegativeIndex() {
        CharSeq.empty().insertAll(-1, CharSeq.empty());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowOnInsertAllWhenExceedingUpperBound() {
        CharSeq.empty().insertAll(1, CharSeq.empty());
    }

    @Test
    public void shouldThrowWhenInsertingAllIterableContainingNull() {
        assertThatThrownBy(() -> CharSeq.empty().insertAll(0, Arrays.asList(null, null))).isInstanceOf(NullPointerException.class);
    }

    // -- intersperse

    @Test
    public void shouldIntersperseNil() {
        assertThat(CharSeq.empty().intersperse(',')).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldIntersperseSingleton() {
        assertThat(CharSeq.of('a').intersperse(',')).isEqualTo(CharSeq.of('a'));
    }

    @Test
    public void shouldIntersperseMultipleElements() {
        assertThat(CharSeq.of('a', 'b').intersperse(',')).isEqualTo(CharSeq.of('a', ',', 'b'));
    }

    @Test
    public void shouldThrowWhenInterspersingWillNullSeparator() {
        assertThatThrownBy(() -> CharSeq.of('a', 'b').intersperse(null)).isInstanceOf(NullPointerException.class);
    }

    // -- iterator(int)

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenNilIteratorStartingAtIndex() {
        CharSeq.empty().iterator(1);
    }

    @Test
    public void shouldIterateFirstElementOfNonNilStartingAtIndex() {
        assertThat(CharSeq.of('1', '2', '3').iterator(1).next()).isEqualTo('2');
    }

    @Test
    public void shouldFullyIterateNonNilStartingAtIndex() {
        int actual = -1;
        for (final java.util.Iterator<Character> iter = CharSeq.of('1', '2', '3').iterator(1); iter.hasNext(); ) {
            actual = iter.next();
        }
        assertThat(actual).isEqualTo('3');
    }

    // -- prepend

    @Test
    public void shouldPrependElementToNil() {
        final CharSeq actual = CharSeq.empty().prepend('1');
        final CharSeq expected = CharSeq.of('1');
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldPrependElementToNonNil() {
        final CharSeq actual = CharSeq.of('2', '3').prepend('1');
        final CharSeq expected = CharSeq.of('1', '2', '3');
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldThrowWhenPrependingNull() {
        assertThatThrownBy(() -> CharSeq.empty().prepend(null)).isInstanceOf(NullPointerException.class);
    }

    // -- prependAll

    @Test(expected = NullPointerException.class)
    public void shouldThrowOnPrependAllOfNull() {
        CharSeq.empty().prependAll(null);
    }

    @Test
    public void shouldPrependAllNilToNil() {
        final CharSeq actual = CharSeq.empty().prependAll(CharSeq.empty());
        final CharSeq expected = CharSeq.empty();
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldPrependAllNilToNonNil() {
        final CharSeq actual = CharSeq.of('1', '2', '3').prependAll(CharSeq.empty());
        final CharSeq expected = CharSeq.of('1', '2', '3');
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldPrependAllNonNilToNil() {
        final CharSeq actual = CharSeq.empty().prependAll(CharSeq.of('1', '2', '3'));
        final CharSeq expected = CharSeq.of('1', '2', '3');
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldPrependAllNonNilToNonNil() {
        final CharSeq actual = CharSeq.of('4', '5', '6').prependAll(CharSeq.of('1', '2', '3'));
        final CharSeq expected = CharSeq.of('1', '2', '3', '4', '5', '6');
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldThrowWhenPrependingIterableContainingNull() {
        assertThatThrownBy(() -> CharSeq.empty().prependAll(List.of((Character) null))).isInstanceOf(NullPointerException.class);
    }

    // -- remove

    @Test
    public void shouldRemoveElementFromNil() {
        assertThat(CharSeq.empty().remove(null)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldRemoveElementFromSingleton() {
        assertThat(CharSeq.of('1').remove('1')).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldRemoveFirstElement() {
        assertThat(CharSeq.of('1', '2', '3').remove('1')).isEqualTo(CharSeq.of('2', '3'));
    }

    @Test
    public void shouldRemoveLastElement() {
        assertThat(CharSeq.of('1', '2', '3').remove('3')).isEqualTo(CharSeq.of('1', '2'));
    }

    @Test
    public void shouldRemoveInnerElement() {
        assertThat(CharSeq.of('1', '2', '3').remove('2')).isEqualTo(CharSeq.of('1', '3'));
    }

    @Test
    public void shouldRemoveNonExistingElement() {
        final CharSeq t = CharSeq.of('1', '2', '3');
        assertThat(t.remove('4')).isSameAs(t);
    }

    @Test
    public void shouldRemoveNull() {
        final CharSeq charSeq = CharSeq.of('a');
        assertThat(charSeq.remove(null)).isSameAs(charSeq);
    }

    // -- removeFirst(Predicate)

    @Test
    public void shouldRemoveFirstElementByPredicateFromNil() {
        assertThat(CharSeq.empty().removeFirst(v -> true)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldRemoveFirstElementByPredicateFromSingleton() {
        assertThat(CharSeq.of('1').removeFirst(v -> v == '1')).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldRemoveFirstElementByPredicateBegin() {
        assertThat(CharSeq.of('1', '2', '3').removeFirst(v -> v == '1')).isEqualTo(CharSeq.of('2', '3'));
    }

    @Test
    public void shouldRemoveFirstElementByPredicateBeginM() {
        assertThat(CharSeq.of('1', '2', '1', '3').removeFirst(v -> v == '1')).isEqualTo(CharSeq.of('2', '1', '3'));
    }

    @Test
    public void shouldRemoveFirstElementByPredicateEnd() {
        assertThat(CharSeq.of('1', '2', '3').removeFirst(v -> v == '3')).isEqualTo(CharSeq.of('1', '2'));
    }

    @Test
    public void shouldRemoveFirstElementByPredicateInner() {
        assertThat(CharSeq.of('1', '2', '3', '4', '5').removeFirst(v -> v == '3'))
                .isEqualTo(CharSeq.of('1', '2', '4', '5'));
    }

    @Test
    public void shouldRemoveFirstElementByPredicateInnerM() {
        assertThat(CharSeq.of('1', '2', '3', '2', '5').removeFirst(v -> v == '2'))
                .isEqualTo(CharSeq.of('1', '3', '2', '5'));
    }

    @Test
    public void shouldRemoveFirstElementByPredicateNonExisting() {
        final CharSeq t = CharSeq.of('1', '2', '3');
        assertThat(t.removeFirst(v -> v == 4)).isSameAs(t);
    }

    // -- removeLast(Predicate)

    @Test
    public void shouldRemoveLastElementByPredicateFromNil() {
        assertThat(CharSeq.empty().removeLast(v -> true)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldRemoveLastElementByPredicateFromSingleton() {
        assertThat(CharSeq.of('1').removeLast(v -> v == '1')).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldRemoveLastElementByPredicateBegin() {
        assertThat(CharSeq.of('1', '2', '3').removeLast(v -> v == '1')).isEqualTo(CharSeq.of('2', '3'));
    }

    @Test
    public void shouldRemoveLastElementByPredicateEnd() {
        assertThat(CharSeq.of('1', '2', '3').removeLast(v -> v == '3')).isEqualTo(CharSeq.of('1', '2'));
    }

    @Test
    public void shouldRemoveLastElementByPredicateEndM() {
        assertThat(CharSeq.of('1', '3', '2', '3').removeLast(v -> v == '3')).isEqualTo(CharSeq.of('1', '3', '2'));
    }

    @Test
    public void shouldRemoveLastElementByPredicateInner() {
        assertThat(CharSeq.of('1', '2', '3', '4', '5').removeLast(v -> v == '3'))
                .isEqualTo(CharSeq.of('1', '2', '4', '5'));
    }

    @Test
    public void shouldRemoveLastElementByPredicateInnerM() {
        assertThat(CharSeq.of('1', '2', '3', '2', '5').removeLast(v -> v == '2'))
                .isEqualTo(CharSeq.of('1', '2', '3', '5'));
    }

    @Test
    public void shouldRemoveLastElementByPredicateNonExisting() {
        final CharSeq t = CharSeq.of('1', '2', '3');
        assertThat(t.removeLast(v -> v == 4)).isSameAs(t);
    }

    // -- removeAll(Iterable)

    @Test
    public void shouldRemoveAllElementsFromNil() {
        assertThat(CharSeq.empty().removeAll(CharSeq.of('1', '2', '3'))).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldRemoveAllExistingElementsFromNonNil() {
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3').removeAll(CharSeq.of('1', '2')))
                .isEqualTo(CharSeq.of('3', '3'));
    }

    @Test
    public void shouldNotRemoveAllNonExistingElementsFromNonNil() {
        final CharSeq t = CharSeq.of('1', '2', '3');
        assertThat(t.removeAll(CharSeq.of('4', '5'))).isSameAs(t);
    }

    @Test
    public void shouldRemoveAllIterableContainingNull() {
        final CharSeq charSeq = CharSeq.of('a');
        assertThat(charSeq.removeAll(List.of((Character) null))).isSameAs(charSeq);
    }

    // -- removeAll(Object)

    @Test
    public void shouldRemoveAllObjectsFromNil() {
        assertThat(CharSeq.empty().removeAll('1')).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldRemoveAllExistingObjectsFromNonNil() {
        assertThat(CharSeq.of('1', '2', '3', '1', '2', '3').removeAll('1')).isEqualTo(CharSeq.of('2', '3', '2', '3'));
    }

    @Test
    public void shouldNotRemoveAllNonObjectsElementsFromNonNil() {
        final CharSeq t = CharSeq.of('1', '2', '3');
        assertThat(t.removeAll('4')).isSameAs(t);
    }

    @Test
    public void shouldRemoveAllNull() {
        final CharSeq charSeq = CharSeq.of('a');
        assertThat(charSeq.removeAll((Character) null)).isSameAs(charSeq);
    }

    // -- reverse

    @Test
    public void shouldReverseNil() {
        assertThat(CharSeq.empty().reverse()).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldReverseNonNil() {
        assertThat(CharSeq.of('1', '2', '3').reverse()).isEqualTo(CharSeq.of('3', '2', '1'));
    }

    // -- rotateLeft

    @Test
    public void shouldRotateLeftOnEmpty() {
        assertThat(CharSeq.empty().rotateLeft(1)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldRotateLeftOnSingle() {
        CharSeq seq = CharSeq.of('1');
        assertThat(seq.rotateLeft(1)).isSameAs(seq);
    }

    @Test
    public void shouldRotateLeftForZero() {
        CharSeq seq = CharSeq.of('1', '2', '3', '4', '5');
        assertThat(seq.rotateLeft(0)).isSameAs(seq);
    }

    @Test
    public void shouldRotateLeftForNegativeLessThatLen() {
        assertThat(CharSeq.of('1', '2', '3', '4', '5').rotateLeft(-2)).isEqualTo(CharSeq.of('4', '5', '1', '2', '3'));
    }

    @Test
    public void shouldRotateLeftForPositiveLessThatLen() {
        assertThat(CharSeq.of('1', '2', '3', '4', '5').rotateLeft(2)).isEqualTo(CharSeq.of('3', '4', '5', '1', '2'));
    }

    @Test
    public void shouldRotateLeftForPositiveGreaterThatLen() {
        assertThat(CharSeq.of('1', '2', '3', '4', '5').rotateLeft(5 + 2)).isEqualTo(CharSeq.of('3', '4', '5', '1', '2'));
    }

    @Test
    public void shouldRotateLeftForPositiveModuloLen() {
        CharSeq seq = CharSeq.of('1', '2', '3', '4', '5');
        assertThat(seq.rotateLeft(seq.length() * 3)).isSameAs(seq);
    }

    // -- rotateRight

    @Test
    public void shouldRotateRightOnEmpty() {
        assertThat(CharSeq.empty().rotateRight(1)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldRotateRightOnSingle() {
        CharSeq seq = CharSeq.of('1');
        assertThat(seq.rotateRight(1)).isSameAs(seq);
    }

    @Test
    public void shouldRotateRightForZero() {
        CharSeq seq = CharSeq.of('1', '2', '3', '4', '5');
        assertThat(seq.rotateRight(0)).isSameAs(seq);
    }

    @Test
    public void shouldRotateRightForNegativeLessThatLen() {
        assertThat(CharSeq.of('1', '2', '3', '4', '5').rotateRight(-2)).isEqualTo(CharSeq.of('3', '4', '5', '1', '2'));
    }

    @Test
    public void shouldRotateRightForPositiveLessThatLen() {
        assertThat(CharSeq.of('1', '2', '3', '4', '5').rotateRight(2)).isEqualTo(CharSeq.of('4', '5', '1', '2', '3'));
    }

    @Test
    public void shouldRotateRightForPositiveGreaterThatLen() {
        assertThat(CharSeq.of('1', '2', '3', '4', '5').rotateRight(5 + 2)).isEqualTo(CharSeq.of('4', '5', '1', '2', '3'));
    }

    @Test
    public void shouldRotateRightForPositiveModuloLen() {
        CharSeq seq = CharSeq.of('1', '2', '3', '4', '5');
        assertThat(seq.rotateRight(seq.length() * 3)).isSameAs(seq);
    }

    // -- update

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenUpdateWithNegativeIndexOnNil() {
        CharSeq.empty().update(-1, (Character) null);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenUpdateWithNegativeIndexOnNonNil() {
        CharSeq.of('1').update(-1, '2');
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenUpdateOnNil() {
        CharSeq.empty().update(0, (Character) null);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenUpdateWithIndexExceedingByOneOnNonNil() {
        CharSeq.of('1').update(1, '2');
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenUpdateWithIndexExceedingByTwoOnNonNil() {
        CharSeq.of('1').update(2, '2');
    }

    @Test
    public void shouldUpdateFirstElement() {
        assertThat(CharSeq.of('1', '2', '3').update(0, '4')).isEqualTo(CharSeq.of('4', '2', '3'));
    }

    @Test
    public void shouldUpdateLastElement() {
        assertThat(CharSeq.of('1', '2', '3').update(2, '4')).isEqualTo(CharSeq.of('1', '2', '4'));
    }

    @Test
    public void shouldThrowWhenUpdatingCharAtIndexWithNullChar() {
        assertThatThrownBy(() -> CharSeq.of('a').update(0, (Character) null)).isInstanceOf(NullPointerException.class);
    }

    // -- higher order update

    @Test
    public void shouldUpdateViaFunction() {
        final Seq<Character> actual = CharSeq.of("hello").update(0, Character::toUpperCase);
        final Seq<Character> expected = CharSeq.of("Hello");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldThrowWhenUpdatingCharAtIndexWithNullCharUsingFunction() {
        assertThatThrownBy(() -> CharSeq.of('a').update(0, c -> (Character) null)).isInstanceOf(NullPointerException.class);
    }

    // -- slice()

    @Test
    public void shouldSlice() {
        assertThat(CharSeq.empty().slice(0, 0)).isSameAs(CharSeq.empty());
        assertThat(CharSeq.of("123").slice(0, 0)).isSameAs(CharSeq.empty());
        assertThat(CharSeq.of("123").slice(1, 0)).isSameAs(CharSeq.empty());
        assertThat(CharSeq.of("123").slice(4, 5)).isSameAs(CharSeq.empty());
        assertThat(CharSeq.of("123").slice(0, 3)).isEqualTo(CharSeq.of("123"));
        assertThat(CharSeq.of("123").slice(-1, 2)).isEqualTo(CharSeq.of("12"));
        assertThat(CharSeq.of("123").slice(0, 2)).isEqualTo(CharSeq.of("12"));
        assertThat(CharSeq.of("123").slice(1, 2)).isEqualTo(CharSeq.of("2"));
        assertThat(CharSeq.of("123").slice(1, 3)).isEqualTo(CharSeq.of("23"));
        assertThat(CharSeq.of("123").slice(1, 4)).isEqualTo(CharSeq.of("23"));
    }

    // -- sorted()

    @Test
    public void shouldSortNil() {
        assertThat(CharSeq.empty().sorted()).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldSortNonNil() {
        assertThat(CharSeq.of('3', '4', '1', '2').sorted()).isEqualTo(CharSeq.of('1', '2', '3', '4'));
    }

    // -- sorted(Comparator)

    @Test
    public void shouldSortNilUsingComparator() {
        assertThat(CharSeq.empty().sorted((i, j) -> j - i)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldSortNonNilUsingComparator() {
        assertThat(CharSeq.of('3', '4', '1', '2').sorted((i, j) -> j - i)).isEqualTo(CharSeq.of('4', '3', '2', '1'));
    }

    // -- sortBy()

    @Test
    public void shouldSortByFunction() {
        assertThat(CharSeq.of("123").sortBy(c -> -c)).isEqualTo(CharSeq.of("321"));
    }

    @Test
    public void shouldSortByComparator() {
        assertThat(CharSeq.of("123").sortBy((i, j) -> j - i, c -> c)).isEqualTo(CharSeq.of("321"));
    }

    // -- splitAt(index)

    @Test
    public void shouldSplitAtNil() {
        assertThat(CharSeq.empty().splitAt(1)).isEqualTo(Tuple.of(CharSeq.empty(), CharSeq.empty()));
    }

    @Test
    public void shouldSplitAtNonNil() {
        assertThat(CharSeq.of('1', '2', '3').splitAt(1)).isEqualTo(Tuple.of(CharSeq.of('1'), CharSeq.of('2', '3')));
    }

    @Test
    public void shouldSplitAtBegin() {
        assertThat(CharSeq.of('1', '2', '3').splitAt(0)).isEqualTo(Tuple.of(CharSeq.empty(), CharSeq.of('1', '2', '3')));
    }

    @Test
    public void shouldSplitAtEnd() {
        assertThat(CharSeq.of('1', '2', '3').splitAt(3)).isEqualTo(Tuple.of(CharSeq.of('1', '2', '3'), CharSeq.empty()));
    }

    @Test
    public void shouldSplitAtOutOfBounds() {
        assertThat(CharSeq.of('1', '2', '3').splitAt(5)).isEqualTo(Tuple.of(CharSeq.of('1', '2', '3'), CharSeq.empty()));
        assertThat(CharSeq.of('1', '2', '3').splitAt(-1)).isEqualTo(Tuple.of(CharSeq.empty(), CharSeq.of('1', '2', '3')));
    }

    // -- splitAt(predicate)

    @Test
    public void shouldSplitPredicateAtNil() {
        assertThat(CharSeq.empty().splitAt(e -> true)).isEqualTo(Tuple.of(CharSeq.empty(), CharSeq.empty()));
    }

    @Test
    public void shouldSplitPredicateAtNonNil() {
        assertThat(CharSeq.of('1', '2', '3').splitAt(e -> e == '2'))
                .isEqualTo(Tuple.of(CharSeq.of('1'), CharSeq.of('2', '3')));
    }

    @Test
    public void shouldSplitAtPredicateBegin() {
        assertThat(CharSeq.of('1', '2', '3').splitAt(e -> e == '1'))
                .isEqualTo(Tuple.of(CharSeq.empty(), CharSeq.of('1', '2', '3')));
    }

    @Test
    public void shouldSplitAtPredicateEnd() {
        assertThat(CharSeq.of('1', '2', '3').splitAt(e -> e == '3'))
                .isEqualTo(Tuple.of(CharSeq.of('1', '2'), CharSeq.of('3')));
    }

    @Test
    public void shouldSplitAtPredicateNotFound() {
        assertThat(CharSeq.of('1', '2', '3').splitAt(e -> e == '5'))
                .isEqualTo(Tuple.of(CharSeq.of('1', '2', '3'), CharSeq.empty()));
    }

    // -- splitAtInclusive(predicate)

    @Test
    public void shouldSplitInclusivePredicateAtNil() {
        assertThat(CharSeq.empty().splitAtInclusive(e -> true)).isEqualTo(Tuple.of(CharSeq.empty(), CharSeq.empty()));
    }

    @Test
    public void shouldSplitInclusivePredicateAtNonNil() {
        assertThat(CharSeq.of('1', '2', '3').splitAtInclusive(e -> e == '2'))
                .isEqualTo(Tuple.of(CharSeq.of('1', '2'), CharSeq.of('3')));
    }

    @Test
    public void shouldSplitAtInclusivePredicateBegin() {
        assertThat(CharSeq.of('1', '2', '3').splitAtInclusive(e -> e == '1'))
                .isEqualTo(Tuple.of(CharSeq.of('1'), CharSeq.of('2', '3')));
    }

    @Test
    public void shouldSplitAtInclusivePredicateEnd() {
        assertThat(CharSeq.of('1', '2', '3').splitAtInclusive(e -> e == '3'))
                .isEqualTo(Tuple.of(CharSeq.of('1', '2', '3'), CharSeq.empty()));
    }

    @Test
    public void shouldSplitAtInclusivePredicateNotFound() {
        assertThat(CharSeq.of('1', '2', '3').splitAtInclusive(e -> e == '5'))
                .isEqualTo(Tuple.of(CharSeq.of('1', '2', '3'), CharSeq.empty()));
    }

    // -- removeAt(index)

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldRemoveIndexAtNil() {
        CharSeq.empty().removeAt(1);
    }

    @Test
    public void shouldRemoveIndexAtSingleton() {
        assertThat(CharSeq.of('1').removeAt(0)).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldRemoveIndexAtNonNil() {
        assertThat(CharSeq.of('1', '2', '3').removeAt(1)).isEqualTo(CharSeq.of('1', '3'));
    }

    @Test
    public void shouldRemoveIndexAtBegin() {
        assertThat(CharSeq.of('1', '2', '3').removeAt(0)).isEqualTo(CharSeq.of('2', '3'));
    }

    @Test
    public void shouldRemoveIndexAtEnd() {
        assertThat(CharSeq.of('1', '2', '3').removeAt(2)).isEqualTo(CharSeq.of('1', '2'));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldRemoveIndxOutOfBoundsLeft() {
        assertThat(CharSeq.of('1', '2', '3').removeAt(-1)).isEqualTo(CharSeq.of('1', '2', '3'));
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldRemoveIndxOutOfBoundsRight() {
        assertThat(CharSeq.of('1', '2', '3').removeAt(5)).isEqualTo(CharSeq.of('1', '2', '3'));
    }

    // -- repeat

    @Test
    public void shouldRepeat() {
        assertThat(CharSeq.empty().repeat(0)).isEqualTo(CharSeq.empty());
        assertThat(CharSeq.empty().repeat(5)).isEqualTo(CharSeq.empty());
        assertThat(CharSeq.of("123").repeat(0)).isEqualTo(CharSeq.empty());
        assertThat(CharSeq.of("123").repeat(4)).isEqualTo(CharSeq.of("123123123123"));
        assertThat(CharSeq.of("123").repeat(5)).isEqualTo(CharSeq.of("123123123123123"));
        assertThat(CharSeq.repeat('1', 0)).isEqualTo(CharSeq.empty());
        assertThat(CharSeq.repeat('!', 5)).isEqualTo(CharSeq.of("!!!!!"));
    }

    @Test
    public void shouldCompareRepeatAgainstTestImpl() {
        final String value = ".";
        for (int i = 0; i < 10; i++) {
            final String source = testRepeat(value, i);
            for (int j = 0; j < 100; j++) {
                final String actual = CharSeq.of(source).repeat(j).mkString();
                final String expected = testRepeat(source, j);
                assertThat(actual).isEqualTo(expected);
            }
        }
    }

    private String testRepeat(String source, int times) {
        return Stream.continually(source).take(times).mkString();
    }

    // -- transform()

    @Test
    public void shouldTransform() {
        final String transformed = CharSeq.of('0').transform(v -> String.valueOf(v.head()));
        assertThat(transformed).isEqualTo("0");
    }

    // -- scan, scanLeft, scanRight

    @Test
    public void shouldScan() {
        final CharSeq seq = CharSeq.of('1');
        final CharSeq result = seq.scan('0', (c1, c2) -> (char) (c1 + c2));
        assertThat(result.mkString()).isEqualTo(Vector.of('0', 'a').mkString());
    }

    @Test
    public void shouldScanLeft() {
        final CharSeq seq = CharSeq.of('1');
        final IndexedSeq<Character> result = seq.scanLeft('0', (c1, c2) -> (char) (c1 + c2));
        assertThat(result).isEqualTo(Vector.of('0', 'a'));
    }

    @Test
    public void shouldScanRight() {
        final CharSeq seq = CharSeq.of('1');
        final IndexedSeq<Character> result = seq.scanRight('0', (c1, c2) -> (char) (c1 + c2));
        assertThat(result).isEqualTo(Vector.of('a', '0'));
    }

    // -- subSequence(beginIndex)

    @Test
    public void shouldReturnNilWhenSubSequenceFrom0OnNil() {
        final CharSeq actual = CharSeq.empty().subSequence(0);
        assertThat(actual).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldReturnIdentityWhenSubSequenceFrom0OnNonNil() {
        final CharSeq actual = CharSeq.of('1').subSequence(0);
        assertThat(actual).isEqualTo(CharSeq.of('1'));
    }

    @Test
    public void shouldReturnNilWhenSubSequenceFrom1OnSeqOf1() {
        final CharSeq actual = CharSeq.of('1').subSequence(1);
        assertThat(actual).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldReturnSubSequenceWhenIndexIsWithinRange() {
        final CharSeq actual = CharSeq.of('1', '2', '3').subSequence(1);
        assertThat(actual).isEqualTo(CharSeq.of('2', '3'));
    }

    @Test
    public void shouldReturnNilWhenSubSequenceBeginningWithSize() {
        final CharSeq actual = CharSeq.of('1', '2', '3').subSequence(3);
        assertThat(actual).isSameAs(CharSeq.empty());
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenSubSequenceOnNil() {
        CharSeq.empty().subSequence(1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenSubSequenceWithOutOfLowerBound() {
        CharSeq.of('1', '2', '3').subSequence(-1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenSubSequenceWithOutOfUpperBound() {
        CharSeq.of('1', '2', '3').subSequence(4);
    }

    // -- subSequence(beginIndex, endIndex)

    @Test
    public void shouldReturnNilWhenSubSequenceFrom0To0OnNil() {
        final CharSeq actual = CharSeq.empty().subSequence(0, 0);
        assertThat(actual).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldReturnNilWhenSubSequenceFrom0To0OnNonNil() {
        final CharSeq actual = CharSeq.of('1').subSequence(0, 0);
        assertThat(actual).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldReturnSeqWithFirstElementWhenSubSequenceFrom0To1OnNonNil() {
        final CharSeq actual = CharSeq.of('1').subSequence(0, 1);
        assertThat(actual).isEqualTo(CharSeq.of('1'));
    }

    @Test
    public void shouldReturnNilWhenSubSequenceFrom1To1OnNonNil() {
        final CharSeq actual = CharSeq.of('1').subSequence(1, 1);
        assertThat(actual).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldReturnSubSequenceWhenIndicesAreWithinRange() {
        final CharSeq actual = CharSeq.of('1', '2', '3').subSequence(1, 3);
        assertThat(actual).isEqualTo(CharSeq.of('2', '3'));
    }

    @Test
    public void shouldReturnNilWhenIndicesBothAreUpperBound() {
        final CharSeq actual = CharSeq.of('1', '2', '3').subSequence(3, 3);
        assertThat(actual).isSameAs(CharSeq.empty());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowOnSubSequenceOnNonNilWhenBeginIndexIsGreaterThanEndIndex() {
        CharSeq.of('1', '2', '3').subSequence(1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowOnSubSequenceOnNilWhenBeginIndexIsGreaterThanEndIndex() {
        CharSeq.empty().subSequence(1, 0);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowOnSubSequenceOnNonNilWhenBeginIndexExceedsLowerBound() {
        CharSeq.of('1', '2', '3').subSequence(-1, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowOnSubSequenceOnNilWhenBeginIndexExceedsLowerBound() {
        CharSeq.empty().subSequence(-1, 2);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowWhenSubSequence2OnNil() {
        CharSeq.empty().subSequence(0, 1);
    }

    @Test(expected = IndexOutOfBoundsException.class)
    public void shouldThrowOnSubSequenceWhenEndIndexExceedsUpperBound() {
        CharSeq.of('1', '2', '3').subSequence(1, 4).mkString(); // force computation of last element, e.g. because Stream is lazy
    }

    // -- zip

    @Test
    public void shouldZipNils() {
        final Seq<?> actual = CharSeq.empty().zip(CharSeq.empty());
        assertThat(actual).isEqualTo(Vector.empty());
    }

    @Test
    public void shouldZipEmptyAndNonNil() {
        final Seq<?> actual = CharSeq.empty().zip(CharSeq.of('1'));
        assertThat(actual).isEqualTo(Vector.empty());
    }

    @Test
    public void shouldZipNonEmptyAndNil() {
        final Seq<?> actual = CharSeq.of('1').zip(CharSeq.empty());
        assertThat(actual).isEqualTo(Vector.empty());
    }

    @Test
    public void shouldZipNonNilsIfThisIsSmaller() {
        final IndexedSeq<Tuple2<Character, Character>> actual = CharSeq.of('1', '2').zip(CharSeq.of('a', 'b', 'c'));
        final IndexedSeq<Tuple2<Character, Character>> expected = Vector.of(Tuple.of('1', 'a'), Tuple.of('2', 'b'));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipNonNilsIfThatIsSmaller() {
        final IndexedSeq<Tuple2<Character, Character>> actual = CharSeq.of('1', '2', '3').zip(CharSeq.of('a', 'b'));
        final IndexedSeq<Tuple2<Character, Character>> expected = Vector.of(Tuple.of('1', 'a'), Tuple.of('2', 'b'));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipNonNilsOfSameSize() {
        final IndexedSeq<Tuple2<Character, Character>> actual = CharSeq.of('1', '2', '3').zip(CharSeq.of('a', 'b', 'c'));
        final IndexedSeq<Tuple2<Character, Character>> expected = Vector.of(Tuple.of('1', 'a'), Tuple.of('2', 'b'), Tuple.of('3', 'c'));
        assertThat(actual).isEqualTo(expected);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowIfZipWithThatIsNull() {
        CharSeq.empty().zip(null);
    }

    // -- zipAll

    @Test
    public void shouldZipAllNils() {
        final Seq<?> actual = CharSeq.empty().zipAll(CharSeq.empty(), null, null);
        assertThat(actual).isEqualTo(Vector.empty());
    }

    @Test
    public void shouldZipAllEmptyAndNonNil() {
        final IndexedSeq<?> actual = CharSeq.empty().zipAll(CharSeq.of('1'), null, null);
        final IndexedSeq<Tuple2<Object, Character>> expected = Vector.of(Tuple.of(null, '1'));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonEmptyAndNil() {
        final IndexedSeq<?> actual = CharSeq.of('1').zipAll(CharSeq.empty(), null, null);
        final IndexedSeq<Tuple2<Character, Object>> expected = Vector.of(Tuple.of('1', null));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonNilsIfThisIsSmaller() {
        final IndexedSeq<Tuple2<Character, Character>> actual = CharSeq.of('1', '2').zipAll(CharSeq.of('a', 'b', 'c'), '9', 'z');
        final IndexedSeq<Tuple2<Character, Character>> expected = Vector.of(Tuple.of('1', 'a'), Tuple.of('2', 'b'), Tuple.of('9', 'c'));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonNilsIfThatIsSmaller() {
        final IndexedSeq<Tuple2<Character, Character>> actual = CharSeq.of('1', '2', '3').zipAll(CharSeq.of('a', 'b'), '9', 'z');
        final IndexedSeq<Tuple2<Character, Character>> expected = Vector.of(Tuple.of('1', 'a'), Tuple.of('2', 'b'), Tuple.of('3', 'z'));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldZipAllNonNilsOfSameSize() {
        final IndexedSeq<Tuple2<Character, Character>> actual = CharSeq.of('1', '2', '3').zipAll(CharSeq.of('a', 'b', 'c'), '9', 'z');
        final IndexedSeq<Tuple2<Character, Character>> expected = Vector.of(Tuple.of('1', 'a'), Tuple.of('2', 'b'), Tuple.of('3', 'c'));
        assertThat(actual).isEqualTo(expected);
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowIfZipAllWithThatIsNull() {
        CharSeq.empty().zipAll(null, null, null);
    }

    // -- zipWithIndex

    @Test
    public void shouldZipNilWithIndex() {
        assertThat(CharSeq.empty().zipWithIndex()).isEqualTo(Vector.empty());
    }

    @Test
    public void shouldZipNonNilWithIndex() {
        final IndexedSeq<Tuple2<Character, Integer>> actual = CharSeq.of("abc").zipWithIndex();
        final IndexedSeq<Tuple2<Character, Integer>> expected = Vector.of(Tuple.of('a', 0), Tuple.of('b', 1), Tuple.of('c', 2));
        assertThat(actual).isEqualTo(expected);
    }

    // -- static collector()

    @Test
    public void shouldStreamAndCollectNil() {
        final Seq<?> actual = java.util.stream.Stream.<Character> empty().collect(CharSeq.collector());
        assertThat(actual).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldStreamAndCollectNonNil() {
        final Seq<?> actual = java.util.stream.Stream.of('1', '2', '3').collect(CharSeq.collector());
        assertThat(actual).isEqualTo(CharSeq.of('1', '2', '3'));
    }

    @Test
    public void shouldParallelStreamAndCollectNil() {
        final Seq<?> actual = java.util.stream.Stream.<Character> empty().parallel().collect(CharSeq.collector());
        assertThat(actual).isSameAs(CharSeq.empty());
    }

    @Test
    public void shouldParallelStreamAndCollectNonNil() {
        final Seq<?> actual = java.util.stream.Stream.of('1', '2', '3').parallel().collect(CharSeq.collector());
        assertThat(actual).isEqualTo(CharSeq.of('1', '2', '3'));
    }

    // -- static empty()

    @Test
    public void shouldCreateNil() {
        final Seq<?> actual = CharSeq.empty();
        assertThat(actual.length()).isEqualTo(0);
    }

    // -- static of(String)

    @Test
    public void shouldWrapOtherCharSeq() {
        final CharSeq cs1 = CharSeq.of("123");
        final CharSeq cs2 = CharSeq.of(cs1);
        assertThat(cs1 == cs2).isTrue();
    }

    @Test
    public void shouldCreateSeqOfElements() {
        final CharSeq actual = CharSeq.of('1', '2');
        assertThat(actual.length()).isEqualTo(2);
        assertThat(actual.get(0)).isEqualTo('1');
        assertThat(actual.get(1)).isEqualTo('2');
    }

    // -- static ofAll(Iterable)

    @Test
    public void shouldCreateListOfIterable() {
        final java.util.List<Character> arrayList = asList('1', '2');
        final CharSeq actual = CharSeq.ofAll(arrayList);
        assertThat(actual.length()).isEqualTo(2);
        assertThat(actual.get(0)).isEqualTo('1');
        assertThat(actual.get(1)).isEqualTo('2');
    }

    @Test
    public void ofShouldReturnTheSingletonEmpty() {
        assertThat(CharSeq.of()).isSameAs(CharSeq.empty());
    }

    @Test
    public void ofAllShouldReturnTheSingletonEmpty() {
        assertThat(CharSeq.ofAll(Iterator.empty())).isSameAs(CharSeq.empty());
    }

    // -- unfold

    @Test
    public void shouldUnfoldRightToEmpty() {
        assertThat(CharSeq.unfoldRight(0, x -> Option.none())).isEqualTo(CharSeq.empty());
    }

    @Test
    public void shouldUnfoldRightSimpleCharSeq() {
        this.<CharSeq> assertThat(
                CharSeq.unfoldRight('j', x -> x == 'a'
                                              ? Option.none()
                                              : Option.of(new Tuple2<>(x, (char) (x - 1)))))
                .isEqualTo(CharSeq.of("jihgfedcb"));
    }

    @Test
    public void shouldUnfoldLeftToEmpty() {
        assertThat(CharSeq.unfoldLeft(0, x -> Option.none())).isEqualTo(CharSeq.empty());
    }

    @Test
    public void shouldUnfoldLeftSimpleCharSeq() {
        this.<CharSeq> assertThat(
                CharSeq.unfoldLeft('j', x -> x == 'a'
                                             ? Option.none()
                                             : Option.of(new Tuple2<>((char) (x - 1), x))))
                .isEqualTo(CharSeq.of("bcdefghij"));
    }

    @Test
    public void shouldUnfoldToEmpty() {
        assertThat(CharSeq.unfold('j', x -> Option.none())).isEqualTo(CharSeq.empty());
    }

    @Test
    public void shouldUnfoldSimpleCharSeq() {
        assertThat(
                CharSeq.unfold('j', x -> x == 'a'
                                         ? Option.none()
                                         : Option.of(new Tuple2<>((char) (x - 1), x))))
                .isEqualTo(CharSeq.of("bcdefghij"));
    }

    // -- number conversion

    // decode*

    @Test
    public void shouldDecodeByte() {
        assertThat(CharSeq.of("1").decodeByte()).isEqualTo(Byte.decode("1"));
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotDecodeByteGivenEmptyCharSeq() {
        CharSeq.empty().decodeByte();
    }

    @Test
    public void shouldDecodeInteger() {
        assertThat(CharSeq.of("1").decodeInteger()).isEqualTo(Integer.decode("1"));
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotDecodeIntegerGivenEmptyCharSeq() {
        CharSeq.empty().decodeInteger();
    }

    @Test
    public void shouldDecodeLong() {
        assertThat(CharSeq.of("1").decodeLong()).isEqualTo(Long.decode("1"));
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotDecodeLongGivenEmptyCharSeq() {
        CharSeq.empty().decodeLong();
    }

    @Test
    public void shouldDecodeShort() {
        assertThat(CharSeq.of("1").decodeShort()).isEqualTo(Short.decode("1"));
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotDecodeShortGivenEmptyCharSeq() {
        CharSeq.empty().decodeShort();
    }

    // parse*

    @Test
    public void shouldParseBooleanWhenTrue() {
        assertThat(CharSeq.of("true").parseBoolean()).isEqualTo(Boolean.parseBoolean("true"));
    }

    @Test
    public void shouldParseBooleanWhenFalse() {
        assertThat(CharSeq.of("false").parseBoolean()).isEqualTo(Boolean.parseBoolean("false"));
    }

    @Test
    public void shouldParseByte() {
        assertThat(CharSeq.of("1").parseByte()).isEqualTo((byte) 1);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseByteGivenEmptyCharSeq() {
        CharSeq.empty().parseByte();
    }

    @Test
    public void shouldParseByteUsingRadix() {
        assertThat(CharSeq.of("11").parseByte(2)).isEqualTo((byte) 3);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseByteUsingRadixGivenEmptyCharSeq() {
        CharSeq.empty().parseByte(2);
    }

    @Test
    public void shouldParseDouble() {
        assertThat(CharSeq.of("1.0").parseDouble()).isEqualTo(1.0d);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseDoubleGivenEmptyCharSeq() {
        CharSeq.empty().parseDouble();
    }

    @Test
    public void shouldParseFloat() {
        assertThat(CharSeq.of("1.0").parseFloat()).isEqualTo(1.0f);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseFloatGivenEmptyCharSeq() {
        CharSeq.empty().parseFloat();
    }

    @Test
    public void shouldParseInt() {
        assertThat(CharSeq.of("1").parseInt()).isEqualTo(1);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseIntGivenEmptyCharSeq() {
        CharSeq.empty().parseInt();
    }

    @Test
    public void shouldParseIntUsingRadix() {
        assertThat(CharSeq.of("11").parseInt(2)).isEqualTo(3);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseIntUsingRadixGivenEmptyCharSeq() {
        CharSeq.empty().parseInt(2);
    }

    @Test
    public void shouldParseUnsignedInt() {
        assertThat(CharSeq.of("+1").parseUnsignedInt()).isEqualTo(1);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseUnsignedIntGivenNegativeNumber() {
        CharSeq.of("-1").parseUnsignedInt();
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseUnsignedIntGivenEmptyCharSeq() {
        CharSeq.empty().parseUnsignedInt();
    }

    @Test
    public void shouldParseUnsignedIntUsingRadix() {
        assertThat(CharSeq.of("+11").parseUnsignedInt(2)).isEqualTo(3);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseUnsignedIntUsingRadixGivenNegativeNumber() {
        CharSeq.of("-1").parseUnsignedInt(2);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseUnsignedIntUsingRadixGivenEmptyCharSeq() {
        CharSeq.empty().parseUnsignedInt(2);
    }

    @Test
    public void shouldParseLong() {
        assertThat(CharSeq.of("1").parseLong()).isEqualTo(1L);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseLongGivenEmptyCharSeq() {
        CharSeq.empty().parseLong();
    }

    @Test
    public void shouldParseLongUsingRadix() {
        assertThat(CharSeq.of("11").parseLong(2)).isEqualTo(3L);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseLongUsingRadixGivenEmptyCharSeq() {
        CharSeq.empty().parseLong(2);
    }

    @Test
    public void shouldParseUnsignedLong() {
        assertThat(CharSeq.of("+1").parseUnsignedLong()).isEqualTo(1);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseUnsignedLongGivenNegativeNumber() {
        CharSeq.of("-1").parseUnsignedLong();
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseUnsignedLongGivenEmptyCharSeq() {
        CharSeq.empty().parseUnsignedLong();
    }

    @Test
    public void shouldParseUnsignedLongUsingRadix() {
        assertThat(CharSeq.of("+11").parseUnsignedLong(2)).isEqualTo(3);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseUnsignedLongUsingRadixGivenNegativeNumber() {
        CharSeq.of("-1").parseUnsignedLong(2);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseUnsignedLongUsingRadixGivenEmptyCharSeq() {
        CharSeq.empty().parseUnsignedLong(2);
    }

    @Test
    public void shouldParseShort() {
        assertThat(CharSeq.of("1").parseShort()).isEqualTo((short) 1);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseShortGivenEmptyCharSeq() {
        CharSeq.empty().parseShort();
    }

    @Test
    public void shouldParseShortUsingRadix() {
        assertThat(CharSeq.of("11").parseShort(2)).isEqualTo((short) 3);
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotParseShortUsingRadixGivenEmptyCharSeq() {
        CharSeq.empty().parseShort(2);
    }

    // to*

    @Test
    public void shouldConvertToBooleanWhenTrue() {
        assertThat(CharSeq.of("true").toBoolean()).isEqualTo(Boolean.valueOf("true"));
    }

    @Test
    public void shouldConvertToBooleanWhenFalse() {
        assertThat(CharSeq.of("false").toBoolean()).isEqualTo(Boolean.valueOf("false"));
    }

    @Test
    public void shouldConvertToByte() {
        assertThat(CharSeq.of("1").toByte()).isEqualTo(Byte.valueOf("1"));
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotConvertToByteGivenEmptyCharSeq() {
        CharSeq.empty().toByte();
    }

    @Test
    public void shouldConvertToByteUsingRadix() {
        assertThat(CharSeq.of("11").toByte(2)).isEqualTo(Byte.valueOf("11", 2));
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotConvertToByteUsingRadixGivenEmptyCharSeq() {
        CharSeq.empty().toByte(2);
    }

    @Test
    public void shouldConvertToDouble() {
        assertThat(CharSeq.of("1.0").toDouble()).isEqualTo(Double.valueOf("1.0"));
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotConvertToDoubleGivenEmptyCharSeq() {
        CharSeq.empty().toDouble();
    }

    @Test
    public void shouldConvertToFloat() {
        assertThat(CharSeq.of("1.0").toFloat()).isEqualTo(Float.valueOf("1.0"));
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotConvertToFloatGivenEmptyCharSeq() {
        CharSeq.empty().toFloat();
    }

    @Test
    public void shouldConvertToInteger() {
        assertThat(CharSeq.of("1").toInteger()).isEqualTo(Integer.valueOf("1"));
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotConvertToIntegerGivenEmptyCharSeq() {
        CharSeq.empty().toInteger();
    }

    @Test
    public void shouldConvertToIntegerUsingRadix() {
        assertThat(CharSeq.of("11").toInteger(2)).isEqualTo(Integer.valueOf("11", 2));
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotConvertToIntegerUsingRadixGivenEmptyCharSeq() {
        CharSeq.empty().toInteger(2);
    }

    @Test
    public void shouldConvertToLong() {
        assertThat(CharSeq.of("1").toLong()).isEqualTo(Long.valueOf("1"));
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotConvertToLongGivenEmptyCharSeq() {
        CharSeq.empty().toLong();
    }

    @Test
    public void shouldConvertToLongUsingRadix() {
        assertThat(CharSeq.of("11").toLong(2)).isEqualTo(Long.valueOf("11", 2));
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotConvertToLongUsingRadixGivenEmptyCharSeq() {
        CharSeq.empty().toLong(2);
    }

    @Test
    public void shouldConvertToShort() {
        assertThat(CharSeq.of("1").toShort()).isEqualTo(Short.valueOf("1"));
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotConvertToShortGivenEmptyCharSeq() {
        CharSeq.empty().toShort();
    }

    @Test
    public void shouldConvertToShortUsingRadix() {
        assertThat(CharSeq.of("11").toShort(2)).isEqualTo(Short.valueOf("11", 2));
    }

    @Test(expected = NumberFormatException.class)
    public void shouldNotConvertToShortUsingRadixGivenEmptyCharSeq() {
        CharSeq.empty().toShort(2);
    }

}
