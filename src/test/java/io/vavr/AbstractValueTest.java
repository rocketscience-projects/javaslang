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

import io.vavr.collection.*;
import io.vavr.control.Either;
import io.vavr.control.Try;
import io.vavr.control.Validation;
import io.vavr.concurrent.Future;
import io.vavr.control.Option;
import org.assertj.core.api.*;
import org.junit.Test;

import java.io.Serializable;
import java.util.*;

import static io.vavr.API.*;
import static io.vavr.Predicates.anyOf;
import static io.vavr.Predicates.instanceOf;

public abstract class AbstractValueTest {

    protected Random getRandom(int seed) {
        if (seed >= 0) {
            return new Random(seed);
        } else {
            final Random random = new Random();
            seed = random.nextInt();
            System.out.println("using seed: " + seed);
            random.setSeed(seed);
            return random;
        }
    }

    protected <T> IterableAssert<T> assertThat(Iterable<T> actual) {
        return new IterableAssert<T>(actual) {};
    }

    protected <T> ObjectAssert<T> assertThat(T actual) {
        return new ObjectAssert<T>(actual) {};
    }

    protected <T> ObjectArrayAssert<T> assertThat(T[] actual) {
        return new ObjectArrayAssert<T>(actual) {};
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

    abstract protected <T> Value<T> empty();

    abstract protected <T> Value<T> of(T element);

    @SuppressWarnings("unchecked")
    abstract protected <T> Value<T> of(T... elements);

    // TODO: Eliminate this method. Switching the behavior of unit tests is evil. Tests should not contain additional logic. Also it seems currently to be used in different sematic contexts.
    abstract protected boolean useIsEqualToInsteadOfIsSameAs();

    // -- Serialization

    /**
     * States whether the specific Value implementation is Serializable.
     * <p>
     * Test classes override this method to return false if needed.
     *
     * @return true (by default), if the Value is Serializable, false otherwise
     */
    private boolean isSerializable() {
        final Object nonEmpty = of(1);
        if (empty() instanceof Serializable != nonEmpty instanceof Serializable) {
            throw new Error("empty and non-empty do not consistently implement Serializable");
        }
        final boolean actual = nonEmpty instanceof Serializable;
        final boolean expected = Match(nonEmpty).of(
                Case($(anyOf(
                        instanceOf(Future.class),
                        instanceOf(io.vavr.collection.Iterator.class)
                )), false),
                Case($(anyOf(
                        instanceOf(Either.class),
                        instanceOf(Lazy.class),
                        instanceOf(Option.class),
                        instanceOf(Try.class),
                        instanceOf(Traversable.class),
                        instanceOf(Validation.class)
                )), true)
        );
        assertThat(actual).isEqualTo(expected);
        return actual;
    }

    @Test
    public void shouldSerializeDeserializeEmpty() {
        if (isSerializable()) {
            final Value<?> testee = empty();
            final Value<?> actual = Serializables.deserialize(Serializables.serialize(testee));
            assertThat(actual).isEqualTo(testee);
        }
    }

    @Test
    public void shouldSerializeDeserializeSingleValued() {
        if (isSerializable()) {
            final Value<?> testee = of(1);
            final Value<?> actual = Serializables.deserialize(Serializables.serialize(testee));
            assertThat(actual).isEqualTo(testee);
        }
    }

    @Test
    public void shouldSerializeDeserializeMultiValued() {
        if (isSerializable()) {
            final Value<?> testee = of(1, 2, 3);
            final Value<?> actual = Serializables.deserialize(Serializables.serialize(testee));
            assertThat(actual).isEqualTo(testee);
        }
    }

    @Test
    public void shouldPreserveSingletonInstanceOnDeserialization() {
        if (isSerializable() && !useIsEqualToInsteadOfIsSameAs()) {
            final Value<?> empty = empty();
            final Value<?> actual = Serializables.deserialize(Serializables.serialize(empty));
            assertThat(actual).isSameAs(empty);
        }
    }

    // -- equals

    @Test
    public void shouldRecognizeSameObject() {
        final Value<Integer> v = of(1);
        //noinspection EqualsWithItself
        assertThat(v.equals(v)).isTrue();
    }

    @Test
    public void shouldRecognizeEqualObjects() {
        final Value<Integer> v1 = of(1);
        final Value<Integer> v2 = of(1);
        assertThat(v1.equals(v2)).isTrue();
    }

    @Test
    public void shouldRecognizeUnequalObjects() {
        final Value<Integer> v1 = of(1);
        final Value<Integer> v2 = of(2);
        assertThat(v1.equals(v2)).isFalse();
    }

}
