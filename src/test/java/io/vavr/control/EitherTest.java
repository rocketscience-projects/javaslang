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

import io.vavr.AbstractValueTest;
import io.vavr.collection.List;
import io.vavr.collection.Seq;
import io.vavr.collection.Vector;
import org.junit.Test;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Function;

import static io.vavr.API.Left;
import static io.vavr.API.Right;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class EitherTest extends AbstractValueTest {

    // -- AbstractValueTest

    @Override
    protected <T> Either<?, T> empty() {
        return Either.<T, T> left(null);
    }

    @Override
    protected <T> Either<?, T> of(T element) {
        return Either.<T, T> right(element);
    }

    @SafeVarargs
    @Override
    protected final <T> Either<?, T> of(T... elements) {
        return of(elements[0]);
    }

    @Override
    protected boolean useIsEqualToInsteadOfIsSameAs() {
        return true;
    }

    // -- Either

    @Test
    public void shouldBimapLeft() {
        final Either<Integer, String> actual = Either.<Integer, String> left(1).bimap(i -> i + 1, s -> s + "1");
        final Either<Integer, String> expected = Either.left(2);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldBimapRight() {
        final Either<Integer, String> actual = Either.<Integer, String> right("1").bimap(i -> i + 1, s -> s + "1");
        final Either<Integer, String> expected = Either.right("11");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldFoldLeft() {
        final String value = Either.left("L").fold(l -> l + "+", r -> r + "-");
        assertThat(value).isEqualTo("L+");
    }

    @Test
    public void shouldFoldRight() {
        final String value = Either.right("R").fold(l -> l + "-", r -> r + "+");
        assertThat(value).isEqualTo("R+");
    }

    // -- sequence

    @Test
    public void shouldThrowWhenSequencingNull() {
        assertThatThrownBy(() -> Either.sequence(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("eithers is null");
    }

    @Test
    public void shouldSequenceEmptyIterableOfEither() {
        final Iterable<Either<Integer, String>> eithers = List.empty();
        final Either<Seq<Integer>, Seq<String>> actual = Either.sequence(eithers);
        final Either<Seq<Integer>, Seq<String>> expected = Either.right(Vector.empty());
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSequenceNonEmptyIterableOfRight() {
        final Iterable<Either<Integer, String>> eithers = List.of(Either.right("a"), Either.right("b"), Either.right("c"));
        final Either<Seq<Integer>, Seq<String>> actual = Either.sequence(eithers);
        final Either<Seq<Integer>, Seq<String>> expected = Either.right(Vector.of("a", "b", "c"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSequenceNonEmptyIterableOfLeft() {
        final Iterable<Either<Integer, String>> eithers = List.of(Either.left(1), Either.left(2), Either.left(3));
        final Either<Seq<Integer>, Seq<String>> actual = Either.sequence(eithers);
        final Either<Seq<Integer>, Seq<String>> expected = Either.left(Vector.of(1, 2, 3));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSequenceNonEmptyIterableOfMixedEither() {
        final Iterable<Either<Integer,String>> eithers = List.of(Either.right("a"), Either.left(1), Either.right("c"), Either.left(3));
        final Either<Seq<Integer>, Seq<String>> actual = Either.sequence(eithers);
        final Either<Seq<Integer>, Seq<String>> expected = Either.left(Vector.of(1, 3));
        assertThat(actual).isEqualTo(expected);
    }

    // -- sequenceRight

    @Test
    public void shouldThrowWhenSequencingRightNull() {
        assertThatThrownBy(() -> Either.sequenceRight(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("eithers is null");
    }

    @Test
    public void shouldSequenceRightEmptyIterableOfEither() {
        final Iterable<Either<Integer, String>> eithers = List.empty();
        final Either<Integer, Seq<String>> actual = Either.sequenceRight(eithers);
        final Either<Integer, Seq<String>> expected = Either.right(Vector.empty());
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSequenceRightNonEmptyIterableOfRight() {
        final Iterable<Either<Integer, String>> eithers = List.of(Either.right("a"), Either.right("b"), Either.right("c"));
        final Either<Integer, Seq<String>> actual = Either.sequenceRight(eithers);
        final Either<Integer, Seq<String>> expected = Either.right(Vector.of("a", "b", "c"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSequenceRightNonEmptyIterableOfLeft() {
        final Iterable<Either<Integer, String>> eithers = List.of(Either.left(1), Either.left(2), Either.left(3));
        final Either<Integer, Seq<String>> actual = Either.sequenceRight(eithers);
        final Either<Integer, Seq<String>> expected = Either.left(1);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldSequenceRightNonEmptyIterableOfMixedEither() {
        final Iterable<Either<Integer,String>> eithers = List.of(Either.right("a"), Either.left(1), Either.right("c"), Either.left(3));
        final Either<Integer, Seq<String>> actual = Either.sequenceRight(eithers);
        final Either<Integer, Seq<String>> expected = Either.left(1);
        assertThat(actual).isEqualTo(expected);
    }

    // -- transform

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnNullTransformFunction() {
        Either.right(1).transform(null);
    }

    @Test
    public void shouldApplyTransformFunctionToRight() {
        final Either<?, Integer> either = Either.right(1);
        final Function<Either<?, Integer>, String> f = e -> e.get().toString().concat("-transformed");
        assertThat(either.transform(f)).isEqualTo("1-transformed");
    }

    @Test
    public void shouldHandleTransformOnLeft() {
        assertThat(Either.left(0).<String> transform(self -> self.isEmpty() ? "ok" : "failed")).isEqualTo("ok");
    }

    // -- traverse

    @Test
    public void shouldThrowWhenTraversingNull() {
        assertThatThrownBy(() -> Either.traverse(null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("values is null");
    }

    @Test
    public void shouldTraverseEmptyIterableOfEither() {
        final Iterable<String> values = List.empty();
        final Either<Seq<Integer>, Seq<String>> actual = Either.traverse(values, Either::right);
        final Either<Seq<Integer>, Seq<String>> expected = Either.right(Vector.empty());
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldTraverseNonEmptyIterableOfRight() {
        final Iterable<String> values = List.of("a", "b", "c");
        final Either<Seq<Integer>, Seq<String>> actual = Either.traverse(values, Either::right);
        final Either<Seq<Integer>, Seq<String>> expected = Either.right(Vector.of("a", "b", "c"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldTraverseNonEmptyIterableOfLeft() {
        final Iterable<Integer> values = List.of(1, 2, 3);
        final Either<Seq<Integer>, Seq<String>> actual = Either.traverse(values, Either::left);
        final Either<Seq<Integer>, Seq<String>> expected = Either.left(Vector.of(1, 2, 3));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldTraverseNonEmptyIterableOfMixedEither() {
        final Iterable<String> values = List.of("a", "1", "c", "3");
        final Either<Seq<Integer>, Seq<String>> actual =
            Either.traverse(values, x -> x.matches("^\\d+$") ? Either.left(Integer.parseInt(x)) : Either.right(x));
        final Either<Seq<Integer>, Seq<String>> expected = Either.left(Vector.of(1, 3));
        assertThat(actual).isEqualTo(expected);
    }

    // -- traverseRight

    @Test
    public void shouldThrowWhenTraversingRightNull() {
        assertThatThrownBy(() -> Either.traverseRight(null, null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("values is null");
    }

    @Test
    public void shouldTraverseRightEmptyIterableOfEither() {
        final Iterable<String> values = List.empty();
        final Either<Integer, Seq<String>> actual = Either.traverseRight(values, Either::right);
        final Either<Integer, Seq<String>> expected = Either.right(Vector.empty());
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldTraverseRightNonEmptyIterableOfRight() {
        final Iterable<String> values = List.of("a", "b", "c");
        final Either<Integer, Seq<String>> actual = Either.traverseRight(values, Either::right);
        final Either<Integer, Seq<String>> expected = Either.right(Vector.of("a", "b", "c"));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldTraverseRightNonEmptyIterableOfLeft() {
        final Iterable<Integer> values = List.of(1, 2, 3);
        final Either<Integer, Seq<String>> actual = Either.traverseRight(values, Either::left);
        final Either<Integer, Seq<String>> expected = Either.left(1);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldTraverseRightNonEmptyIterableOfMixedEither() {
        final Iterable<String> values = List.of("a", "1", "c", "3");
        final Either<Integer, Seq<String>> actual =
            Either.traverseRight(values, x -> x.matches("^\\d+$") ? Either.left(Integer.parseInt(x)) : Either.right(x));
        final Either<Integer, Seq<String>> expected = Either.left(1);
        assertThat(actual).isEqualTo(expected);
    }

    // --

    @Test
    public void shouldReturnSameWhenCallingMapOnLeft() {
        final Either<Integer, Object> actual = Left(1);
        assertThat(actual.map(v -> { throw new IllegalStateException(); })).isSameAs(actual);
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowIfRightGetLeft() {
        Right(1).getLeft();
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowIfLeftGet() {
        Left(1).get();
    }

    @Test
    public void shouldSwapLeft() {
        assertThat(Either.left(1).swap()).isEqualTo(Either.right(1));
    }

    @Test
    public void shouldSwapRight() {
        assertThat(Either.right(1).swap()).isEqualTo(Either.left(1));
    }

    // -- recover

    @Test
    public void shouldRecoverWithLeftToRightEither() {
        assertThat(Either.left(1).recoverWith(lvalue -> Either.right(lvalue + 1))).isEqualTo(Either.right(2));
    }

    @Test
    public void shouldRecoverWithLeftToLeftEither() {
        assertThat(Either.left(1).recoverWith(lvalue -> Either.left(lvalue + 1))).isEqualTo(Either.left(2));
    }

    @Test
    public void shouldRecoverWithRight() {
        final Either<String, String> value = Either.<String, String>right("R").recoverWith(lvalue -> Either.left("L"));
        assertThat(value).isEqualTo(Either.right("R"));
    }

    @Test
    public void shouldRecoverLeft() {
        assertThat(Either.left(1).recover(lvalue -> "R")).isEqualTo(Either.right("R"));
    }

    @Test
    public void shouldRecoverRightWithoutInvokingRecovery() {
        // Recover function should not be invoked, so hardcode it to fail
        Function<Object, String> recoveryFunction = $ -> {
            throw new RuntimeException("Lazy recovery function should not be invoked for a Right!");
        };

        assertThat(Either.right("R").recover(recoveryFunction)).isEqualTo(Either.right("R"));
    }

    // -- Either.narrow

    @Test
    public void shouldNarrowRightEither() {
        Either<String, Integer> either = Either.right(42);
        Either<CharSequence, Number> narrow = Either.narrow(either);
        assertThat(narrow.get()).isEqualTo(42);
    }

    @Test
    public void shouldNarrowLeftEither() {
        Either<String, Integer> either = Either.left("vavr");
        Either<CharSequence, Number> narrow = Either.narrow(either);
        assertThat(narrow.getLeft()).isEqualTo("vavr");
    }

    // orElse

    @Test
    public void shouldEitherOrElseEither() {
        assertThat(Either.right(1).orElse(Either.right(2)).get()).isEqualTo(1);
        assertThat(Either.left(1).orElse(Either.right(2)).get()).isEqualTo(2);
    }

    @Test
    public void shouldEitherOrElseSupplier() {
        assertThat(Either.right(1).orElse(() -> Either.right(2)).get()).isEqualTo(1);
        assertThat(Either.left(1).orElse(() -> Either.right(2)).get()).isEqualTo(2);
    }

    // -- Left

    @Test
    public void shouldReturnTrueWhenCallingIsLeftOnLeft() {
        assertThat(Either.left(1).isLeft()).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenCallingIsRightOnLeft() {
        assertThat(Either.left(1).isRight()).isFalse();
    }

    // -- filter

    @Test
    public void shouldFilterRight() {
        Either<String, Integer> either = Either.right(42);
        assertThat(either.filter(i -> true).get()).isSameAs(either);
        assertThat(either.filter(i -> false)).isSameAs(Option.none());
    }

    @Test
    public void shouldFilterLeft() {
        Either<String, Integer> either = Either.left("vavr");
        assertThat(either.filter(i -> true).get()).isSameAs(either);
        assertThat(either.filter(i -> false).get()).isSameAs(either);
    }

    // -- filterNot

    @Test
    public void shouldFilterNotRight() {
        Either<String, Integer> either = Either.right(42);
        assertThat(either.filterNot(i -> false).get()).isSameAs(either);
        assertThat(either.filterNot(i -> true)).isSameAs(Option.none());
    }

    @Test
    public void shouldFilterNotLeft() {
        Either<String, Integer> either = Either.left("vavr");
        assertThat(either.filterNot(i -> false).get()).isSameAs(either);
        assertThat(either.filterNot(i -> true).get()).isSameAs(either);
    }

    @Test
    public void shouldThrowWhenNullPredicate() {
        assertThatThrownBy(() -> Either.left(42).filterNot(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("predicate is null");
    }

    // -- filterOrElse

    @Test
    public void shouldFilterOrElseRight() {
        Either<String, Integer> either = Either.right(42);
        assertThat(either.filterOrElse(i -> true, Object::toString)).isSameAs(either);
        assertThat(either.filterOrElse(i -> false, Object::toString)).isEqualTo(Either.left("42"));
    }

    @Test
    public void shouldFilterOrElseLeft() {
        Either<String, Integer> either = Either.left("vavr");
        assertThat(either.filterOrElse(i -> true, Object::toString)).isSameAs(either);
        assertThat(either.filterOrElse(i -> false, Object::toString)).isSameAs(either);
    }

    // -- flatMap

    @Test
    public void shouldFlatMapRight() {
        Either<String, Integer> either = Either.right(42);
        assertThat(either.flatMap(v -> Either.right("ok")).get()).isEqualTo("ok");
    }

    @Test
    public void shouldFlatMapLeft() {
        Either<String, Integer> either = Either.left("vavr");
        assertThat(either.flatMap(v -> Either.right("ok"))).isSameAs(either);
    }

    // -- peekLeft

    @Test
    public void shouldPeekLeftNil() {
        assertThat(empty().peekLeft(t -> {})).isEqualTo(empty());
    }

    @Test
    public void shouldPeekLeftForLeft() {
        final int[] effect = { 0 };
        final Either<Integer, ?> actual = Either.left(1).peekLeft(i -> effect[0] = i);
        assertThat(actual).isEqualTo(Either.left(1));
        assertThat(effect[0]).isEqualTo(1);
    }

    @Test
    public void shouldNotPeekLeftForRight() {
        Either.right(1).peekLeft(i -> { throw new IllegalStateException(); });
    }

    // equals

    @Test
    public void shouldEqualLeftIfObjectIsSame() {
        final Either<Integer, ?> left = Either.left(1);
        assertThat(left.equals(left)).isTrue();
    }

    @Test
    public void shouldNotEqualLeftIfObjectIsNull() {
        assertThat(Either.left(1).equals(null)).isFalse();
    }

    @Test
    public void shouldNotEqualLeftIfObjectIsOfDifferentType() {
        assertThat(Either.left(1).equals(new Object())).isFalse();
    }

    @Test
    public void shouldEqualLeft() {
        assertThat(Either.left(1)).isEqualTo(Either.left(1));
    }

    // hashCode

    @Test
    public void shouldHashLeft() {
        assertThat(Either.left(1).hashCode()).isEqualTo(Objects.hashCode(1));
    }

    // toString

    @Test
    public void shouldConvertLeftToString() {
        assertThat(Either.left(1).toString()).isEqualTo("Left(1)");
    }

    // -- Right

    @Test
    public void shouldReturnTrueWhenCallingIsRightOnRight() {
        assertThat(Either.right(1).isRight()).isTrue();
    }

    @Test
    public void shouldReturnFalseWhenCallingIsLeftOnRight() {
        assertThat(Either.right(1).isLeft()).isFalse();
    }

    // equals

    @Test
    public void shouldEqualRightIfObjectIsSame() {
        final Either<?, ?> right = Either.right(1);
        assertThat(right.equals(right)).isTrue();
    }

    @Test
    public void shouldNotEqualRightIfObjectIsNull() {
        assertThat(Either.right(1).equals(null)).isFalse();
    }

    @Test
    public void shouldNotEqualRightIfObjectIsOfDifferentType() {
        assertThat(Either.right(1).equals(new Object())).isFalse();
    }

    @Test
    public void shouldEqualRight() {
        assertThat(Either.right(1)).isEqualTo(Either.right(1));
    }

    // hashCode

    @Test
    public void shouldHashRight() {
        assertThat(Either.right(1).hashCode()).isEqualTo(Objects.hashCode(1));
    }

    // toString

    @Test
    public void shouldConvertRightToString() {
        assertThat(Either.right(1).toString()).isEqualTo("Right(1)");
    }


    // -- spliterator

    @Test
    public void shouldHaveSizedSpliterator() {
        assertThat(of(1).spliterator().hasCharacteristics(Spliterator.SIZED | Spliterator.SUBSIZED)).isTrue();
    }

    @Test
    public void shouldHaveOrderedSpliterator() {
        assertThat(of(1).spliterator().hasCharacteristics(Spliterator.ORDERED)).isTrue();
    }

    @Test
    public void shouldReturnSizeWhenSpliterator() {
        assertThat(of(1).spliterator().getExactSizeIfKnown()).isEqualTo(1);
    }
}
