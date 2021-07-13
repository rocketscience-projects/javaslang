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

import io.vavr.*;
import io.vavr.collection.Seq;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class OptionTest extends AbstractValueTest {

    // -- AbstractValueTest

    @Override
    protected <T> Option<T> empty() {
        return Option.none();
    }

    @Override
    protected <T> Option<T> of(T element) {
        return Option.some(element);
    }

    @SafeVarargs
    @Override
    protected final <T> Option<T> of(T... elements) {
        return of(elements[0]);
    }

    @Override
    protected boolean useIsEqualToInsteadOfIsSameAs() {
        return true;
    }

    // -- construction

    // Option.narrow

    @Test
    public void shouldNarrowOption() {
        final Option<Integer> option = Option.of(42);
        final Option<Number> narrow = Option.narrow(option);
        assertThat(narrow.get()).isEqualTo(42);
    }


    // Option.of

    @Test
    public void shouldMapNullToNone() {
        assertThat(Option.of(null)).isEqualTo(Option.none());
    }

    @Test
    public void shouldMapNonNullToSome() {
        final Option<?> option = Option.of(new Object());
        assertThat(option.isDefined()).isTrue();
    }

    // Option.some

    @Test
    public void shouldWrapNullInSome() {
        final Option<?> some = Option.some(null);
        assertThat(some.get()).isEqualTo(null);
    }

    // Option.when

    @Test
    public void shouldWrapWhenIfTrue() {
        assertThat(Option.when(true, () -> null)).isEqualTo(Option.some(null));
        assertThat(Option.when(true, (Object) null)).isEqualTo(Option.some(null));
    }

    @Test
    public void shouldNotWrapWhenIfFalse() {
        assertThat(Option.when(false, () -> null)).isEqualTo(Option.none());
        assertThat(Option.when(false, (Object) null)).isEqualTo(Option.none());
    }

    @Test
    public void shouldNotExecuteWhenIfFalse() {
        assertThat(Option.when(false, () -> {
            throw new RuntimeException();
        })).isEqualTo(Option.none());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnWhenIfSupplierIsNull() {
        assertThat(Option.when(false, (Supplier<?>) null)).isEqualTo(Option.none());
    }

    // Option.unless

    @Test
    public void shouldWrapUnlessIfFalse() {
        assertThat(Option.unless(false, () -> null)).isEqualTo(Option.some(null));
        assertThat(Option.unless(false, (Object) null)).isEqualTo(Option.some(null));
    }

    @Test
    public void shouldNotWrapUnlessIfTrue() {
        assertThat(Option.unless(true, () -> null)).isEqualTo(Option.none());
        assertThat(Option.unless(true, (Object) null)).isEqualTo(Option.none());
    }

    @Test
    public void shouldNotExecuteUnlessIfTrue() {
        assertThat(Option.unless(true, () -> {
            throw new RuntimeException();
        })).isEqualTo(Option.none());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnUnlessIfSupplierIsNull() {
        assertThat(Option.unless(true, (Supplier<?>) null)).isEqualTo(Option.none());
    }

    // Option.ofOptional

    @Test
    public void shouldWrapEmptyOptional() {
        assertThat(Option.ofOptional(Optional.empty())).isEqualTo(Option.none());
    }

    @Test
    public void shouldWrapSomeOptional() {
        assertThat(Option.ofOptional(Optional.of(1))).isEqualTo(Option.of(1));
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnNullOptional() {
        assertThat(Option.ofOptional(null)).isEqualTo(Option.none());
    }

    // Option.sequence

    @Test
    public void shouldConvertListOfNonEmptyOptionsToOptionOfList() {
        final List<Option<String>> options = Arrays.asList(Option.of("a"), Option.of("b"), Option.of("c"));
        final Option<Seq<String>> reducedOption = Option.sequence(options);
        assertThat(reducedOption instanceof Option.Some).isTrue();
        assertThat(reducedOption.get().size()).isEqualTo(3);
        assertThat(reducedOption.get().mkString()).isEqualTo("abc");
    }

    @Test
    public void shouldConvertListOfEmptyOptionsToOptionOfList() {
        final List<Option<String>> options = Arrays.asList(Option.none(), Option.none(), Option.none());
        final Option<Seq<String>> option = Option.sequence(options);
        assertThat(option instanceof Option.None).isTrue();
    }

    @Test
    public void shouldConvertListOfMixedOptionsToOptionOfList() {
        final List<Option<String>> options = Arrays.asList(Option.of("a"), Option.none(), Option.of("c"));
        final Option<Seq<String>> option = Option.sequence(options);
        assertThat(option instanceof Option.None).isTrue();
    }

    // -- traverse

    @Test
    public void shouldTraverseListOfNonEmptyOptionsToOptionOfList() {
        final List<String> options = Arrays.asList("a", "b", "c");
        final Option<Seq<String>> reducedOption = Option.traverse(options, Option::of);
        assertThat(reducedOption instanceof Option.Some).isTrue();
        assertThat(reducedOption.get().size()).isEqualTo(3);
        assertThat(reducedOption.get().mkString()).isEqualTo("abc");
    }

    @Test
    public void shouldTraverseListOfEmptyOptionsToOptionOfList() {
        final List<Option<String>> options = Arrays.asList(Option.none(), Option.none(), Option.none());
        final Option<Seq<String>> option = Option.traverse(options, Function.identity());
        assertThat(option instanceof Option.None).isTrue();
    }

    @Test
    public void shouldTraverseListOfMixedOptionsToOptionOfList() {
        final List<String> options = Arrays.asList("a", "b", "c");
        final Option<Seq<String>> option =
            Option.traverse(options, x -> x.equals("b") ? Option.none() : Option.of(x));
        assertThat(option instanceof Option.None).isTrue();
    }

    // -- get

    @Test
    public void shouldSucceedOnGetWhenValueIsPresent() {
        assertThat(Option.of(1).get()).isEqualTo(1);
    }

    @Test(expected = NoSuchElementException.class)
    public void shouldThrowOnGetWhenValueIsNotDefined() {
        Option.none().get();
    }

    // -- orElse

    @Test
    public void shouldReturnSelfOnOrElseIfValueIsPresent() {
        final Option<Integer> opt = Option.of(42);
        assertThat(opt.orElse(Option.of(0))).isSameAs(opt);
    }

    @Test
    public void shouldReturnSelfOnOrElseSupplierIfValueIsPresent() {
        final Option<Integer> opt = Option.of(42);
        assertThat(opt.orElse(() -> Option.of(0))).isSameAs(opt);
    }

    @Test
    public void shouldReturnAlternativeOnOrElseIfValueIsNotDefined() {
        final Option<Integer> opt = Option.of(42);
        assertThat(Option.none().orElse(opt)).isSameAs(opt);
    }

    @Test
    public void shouldReturnAlternativeOnOrElseSupplierIfValueIsNotDefined() {
        final Option<Integer> opt = Option.of(42);
        assertThat(Option.none().orElse(() -> opt)).isSameAs(opt);
    }

    // -- getOrElse

    @Test
    public void shouldGetValueOnGetOrElseWhenValueIsPresent() {
        assertThat(Option.of(1).getOrElse(2)).isEqualTo(1);
    }

    @Test
    public void shouldGetAlternativeOnGetOrElseWhenValueIsNotDefined() {
        assertThat(Option.none().getOrElse(2)).isEqualTo(2);
    }

    // -- getOrElse

    @Test
    public void shouldGetValueOnGetOrElseGetWhenValueIsPresent() {
        assertThat(Option.of(1).getOrElse(() -> 2)).isEqualTo(1);
    }

    @Test
    public void shouldGetAlternativeOnGetOrElseGetWhenValueIsNotDefined() {
        assertThat(Option.none().getOrElse(() -> 2)).isEqualTo(2);
    }

    // -- getOrElseThrow

    @Test
    public void shouldGetValueOnGetOrElseThrowWhenValueIsPresent() {
        assertThat(Option.of(1).getOrElseThrow(() -> new RuntimeException("none"))).isEqualTo(1);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowOnGetOrElseThrowWhenValueIsNotDefined() {
        Option.none().getOrElseThrow(() -> new RuntimeException("none"));
    }

    // -- toJavaOptional

    @Test
    public void shouldConvertNoneToJavaOptional() {
        final Option<Object> none = Option.none();
        assertThat(none.toJavaOptional()).isEqualTo(Optional.empty());
    }

    @Test
    public void shouldConvertSomeToJavaOptional() {
        final Option<Integer> some = Option.some(1);
        assertThat(some.toJavaOptional()).isEqualTo(Optional.of(1));
    }

    // -- isDefined

    @Test
    public void shouldBePresentOnIsDefinedWhenValueIsDefined() {
        assertThat(Option.of(1).isDefined()).isTrue();
    }

    @Test
    public void shouldNotBePresentOnIsDefinedWhenValueIsNotDefined() {
        assertThat(Option.none().isDefined()).isFalse();
    }

    // -- isEmpty

    @Test
    public void isEmpty_is_true_when_called_on_None() {
        assertThat(Option.none().isEmpty()).isTrue();
    }

    @Test
    public void isEmpty_is_false_when_called_on_Option_of_one() {
        assertThat(Option.of(1).isEmpty()).isFalse();
    }

    @Test
    public void isEmpty_is_true_when_called_on_Option_of_null() {
        assertThat(Option.of(null).isEmpty()).isTrue();
    }

    @Test
    public void isEmpty_is_false_when_called_on_Some_of_null() {
        assertThat(Option.some(null).isEmpty()).isFalse();
    }

    // -- onEmpty

    @Test
    public void shouldThrowNullPointerExceptionWhenNullOnEmptyActionPassed() {
        try {
            final Option<String> none = Option.none();
            none.onEmpty(null);
            Assert.fail("No exception was thrown");
        } catch (NullPointerException exc) {
            assertThat(exc.getMessage()).isEqualTo("action is null");
        }
    }

    @Test
    public void shouldExecuteRunnableWhenOptionIsEmpty() {
        final AtomicBoolean state = new AtomicBoolean();
        final Option<?> option = Option.none().onEmpty(() -> state.set(false));
        assertThat(state.get()).isFalse();
        assertThat(option).isSameAs(Option.none());
    }

    @Test
    public void shouldNotThrowExceptionIfOnEmptySetAndOptionIsSome() {
        try {
            final Option<String> none = Option.some("value");
            none.onEmpty(() -> {
                throw new RuntimeException("Exception from empty option!");
            });
        } catch (RuntimeException exc) {
            Assert.fail("No exception should be thrown!");
        }
    }

    // -- filter

    @Test
    public void shouldReturnSomeOnFilterWhenValueIsDefinedAndPredicateMatches() {
        assertThat(Option.of(1).filter(i -> i == 1)).isEqualTo(Option.of(1));
    }

    @Test
    public void shouldReturnNoneOnFilterWhenValueIsDefinedAndPredicateNotMatches() {
        assertThat(Option.of(1).filter(i -> i == 2)).isEqualTo(Option.none());
    }

    @Test
    public void shouldReturnNoneOnFilterWhenValueIsNotDefinedAndPredicateNotMatches() {
        assertThat(Option.<Integer> none().filter(i -> i == 1)).isEqualTo(Option.none());
    }

    // -- filterNot

    @Test
    public void shouldReturnSomeOnFilterNotWhenValueIsDefinedAndPredicateNotMatches() {
        assertThat(Option.of(1).filterNot(i -> i == 2)).isEqualTo(Option.of(1));
    }

    @Test
    public void shouldReturnNoneOnFilterNotWhenValuesIsDefinedAndPredicateMatches() {
        assertThat(Option.of(1).filterNot(i -> i == 1)).isEqualTo(Option.none());
    }

    @Test
    public void shouldReturnNoneOnFilterNotWhenValueIsNotDefinedAndPredicateNotMatches() {
        assertThat(Option.<Integer>none().filterNot(i -> i == 1)).isEqualTo(Option.none());
    }

    @Test
    public void shouldThrowWhenFilterNotPredicateIsNull() {
        assertThatThrownBy(() -> Option.of(1).filterNot(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("predicate is null");
    }

    // -- map

    @Test
    public void shouldMapSome() {
        assertThat(Option.of(1).map(String::valueOf)).isEqualTo(Option.of("1"));
    }

    @Test
    public void shouldMapNone() {
        assertThat(Option.<Integer> none().map(String::valueOf)).isEqualTo(Option.none());
    }

    // -- flatMap

    @Test
    public void shouldFlatMapSome() {
        assertThat(Option.of(1).flatMap(i -> Option.of(String.valueOf(i)))).isEqualTo(Option.of("1"));
    }

    @Test
    public void shouldFlatMapNone() {
        assertThat(Option.<Integer> none().flatMap(i -> Option.of(String.valueOf(i)))).isEqualTo(Option.none());
    }

    @Test
    public void shouldFlatMapNonEmptyIterable() {
        final Option<Integer> option = Option.some(2);
        assertThat(Option.of(1).flatMap(i -> option)).isEqualTo(Option.of(2));
    }

    @Test
    public void shouldFlatMapEmptyIterable() {
        final Option<Integer> option = Option.none();
        assertThat(Option.of(1).flatMap(i -> option)).isEqualTo(Option.none());
    }

    // -- forEach

    @Test
    public void shouldConsumePresentValueOnForEachWhenValueIsDefined() {
        final int[] actual = new int[] { -1 };
        Option.of(1).forEach(i -> actual[0] = i);
        assertThat(actual[0]).isEqualTo(1);
    }

    @Test
    public void shouldNotConsumeAnythingOnForEachWhenValueIsNotDefined() {
        final int[] actual = new int[] { -1 };
        Option.<Integer> none().forEach(i -> actual[0] = i);
        assertThat(actual[0]).isEqualTo(-1);
    }

    // -- toEither

    @Test
    public void shouldMakeRightOnSomeToEither() {
        assertThat(API.Some(5).toEither("")).isEqualTo(API.Right(5));
    }

    @Test
    public void shouldMakeLeftOnNoneToEither() {
        assertThat(API.None().toEither("")).isEqualTo(API.Left(""));
    }

    @Test
    public void shouldMakeLeftOnNoneToEitherSupplier() {
        assertThat(API.None().toEither(() -> "")).isEqualTo(API.Left(""));
    }

    // -- transform

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnNullTransformFunction() {
        Option.some(1).transform(null);
    }

    @Test
    public void shouldApplyTransformFunctionToSome() {
        final Option<Integer> option = Option.some(1);
        final Function<Option<Integer>, String> f = o -> o.get().toString().concat("-transformed");
        assertThat(option.transform(f)).isEqualTo("1-transformed");
    }

    @Test
    public void shouldHandleTransformOnNone() {
        assertThat(Option.none().<String> transform(self -> self.isEmpty() ? "ok" : "failed")).isEqualTo("ok");
    }

    // -- collect

    @Test
    public void shouldCollectDefinedValueUsingPartialFunction() {
        final PartialFunction<Integer, String> pf = Function1.<Integer, String> of(String::valueOf).partial(i -> i % 2 == 1);
        assertThat(Option.of(3).collect(pf)).isEqualTo(Option.of("3"));
    }

    @Test
    public void shouldFilterNotDefinedValueUsingPartialFunction() {
        final PartialFunction<Integer, String> pf = Function1.<Integer, String> of(String::valueOf).partial(i -> i % 2 == 1);
        assertThat(Option.of(2).collect(pf)).isEqualTo(Option.none());
    }

    @Test
    public void shouldCollectEmptyOptionalUsingPartialFunction() {
        final PartialFunction<Integer, String> pf = Function1.<Integer, String> of(String::valueOf).partial(i -> i % 2 == 1);
        assertThat(Option.<Integer>none().collect(pf)).isEqualTo(Option.none());
    }

    @Test(expected = NullPointerException.class)
    public void shouldThrowExceptionOnNullCollectPartialFunction() {
        final PartialFunction<Integer, String> pf = null;
        Option.some(1).collect(pf);
    }

    @Test
    public void shouldNotCallPartialFunctionOnUndefinedArg() {
        final PartialFunction<Integer, Integer> pf = Function1.<Integer, Integer> of(x -> 1/x).partial(i -> i != 0);
        assertThat(Option.of(0).collect(pf)).isEqualTo(Option.none());
    }

    // -- iterator

    @Test
    public void shouldReturnIteratorOfSome() {
        assertThat((Iterator<Integer>) Option.some(1).iterator()).isNotNull();
    }

    @Test
    public void shouldReturnIteratorOfNone() {
        assertThat((Iterator<Object>) Option.none().iterator()).isNotNull();
    }

    // -- equals

    @Test
    public void shouldEqualNoneIfObjectIsSame() {
        final Option<?> none = Option.none();
        assertThat(none).isEqualTo(none);
    }

    @Test
    public void shouldEqualSomeIfObjectIsSame() {
        final Option<?> some = Option.some(1);
        assertThat(some).isEqualTo(some);
    }

    @Test
    public void shouldNotEqualNoneIfObjectIsNull() {
        assertThat(Option.none()).isNotNull();
    }

    @Test
    public void shouldNotEqualSomeIfObjectIsNull() {
        assertThat(Option.some(1)).isNotNull();
    }

    @Test
    public void shouldNotEqualNoneIfObjectIsOfDifferentType() {
        final Object none = Option.none();
        assertThat(none.equals(new Object())).isFalse();
    }

    @Test
    public void shouldNotEqualSomeIfObjectIsOfDifferentType() {
        final Object some = Option.some(1);
        assertThat(some.equals(new Object())).isFalse();
    }

    @Test
    public void shouldEqualSomeIfObjectsAreEquivalent() {
        assertThat(Option.some(1)).isEqualTo(Option.some(1));
    }

    @Test
    public void shouldNotEqualSomeIfObjectIsOfDifferentValue() {
        assertThat(Option.some(1)).isNotEqualTo(Option.some(2));
    }

    // -- hashCode

    @Test
    public void shouldHashNone() {
        assertThat(Option.none().hashCode()).isEqualTo(Objects.hash());
    }

    @Test
    public void shouldHashSome() {
        assertThat(Option.some(1).hashCode()).isEqualTo(Objects.hashCode(1));
    }

    // -- toString

    @Test
    public void shouldConvertSomeToString() {
        assertThat(Option.some(1).toString()).isEqualTo("Some(1)");
    }

    @Test
    public void shouldConvertNoneToString() {
        assertThat(Option.none().toString()).isEqualTo("None");
    }

    // -- serialization

    @Test
    public void shouldPreserveSingletonWhenDeserializingNone() {
        final Object none = Serializables.deserialize(Serializables.serialize(Option.none()));
        assertThat(none == Option.none()).isTrue();
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

    @Test
    public void foldStringToInt() {
        assertThat(Option.some("1").fold(() -> -1, Integer::valueOf)).isEqualTo(1);
        assertThat(Option.<String>none().fold(() -> -1, Integer::valueOf)).isEqualTo(-1);
    }

    @Test
    public void foldEither() {
        Either<String, Integer> right = Option.some(1).fold(() -> {
            throw new AssertionError("Must not happen");
        }, Either::right);
        Either<String, Integer> left = Option.<Integer>none().fold(() -> Either.left("Empty"), ignore -> {
            throw new AssertionError("Must not happen");
        });
        assertThat(right.get()).isEqualTo(1);
        assertThat(left.getLeft()).isEqualTo("Empty");
    }
}
