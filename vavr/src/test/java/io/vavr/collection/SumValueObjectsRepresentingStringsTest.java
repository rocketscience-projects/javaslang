/*  __    __  __  __    __  ___
 * \  \  /  /    \  \  /  /  __/
 *  \  \/  /  /\  \  \/  /  /
 *   \____/__/  \__\____/__/
 *
 * Copyright 2014-2017 Vavr, http://vavr.io
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

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.util.Collection;
import java.util.function.BiFunction;

@RunWith(Parameterized.class)
public class SumValueObjectsRepresentingStringsTest extends SumArbitraryValueObjectsTest<SumValueObjectsRepresentingStringsTest.StringValue> {
    private List<StringValue> items;

    private StringValue expectedSum;

    public SumValueObjectsRepresentingStringsTest(List<StringValue> items, StringValue expectedSum) {
        this.items = items;
        this.expectedSum = expectedSum;
    }
    @Parameters(name = "case {index}: sum({0}) = {1}")
    public static Collection<Object[]> data() {
        return List.of(
                specialCase(List.empty(), ""),
                specialCase(List.of("hello"), "hello"),
                specialCase(List.of("a", "1", "b", "2", "c", "3"), "a1b2c3"),
                specialCase(List.of("a", "", "b"), "ab")
        ).toJavaList();
    }

    private static Object[] specialCase(List<String> itemsAsStrings, String expectedSumAsString) {
        return new Object[] {
                itemsAsStrings.map(StringValue::with),
                StringValue.with(expectedSumAsString) };
    }


    @Override
    protected StringValue expectedSum() {
        return expectedSum;
    }

    @Override
    protected BiFunction<StringValue, StringValue, StringValue> addFunction() {
        return monoid().addFunction();
    }

    @Override
    protected Monoid<StringValue> monoid() {
        return StringValue.monoid();
    }

    @Override
    protected StringValue identityElement() {
        return monoid().identityElement();
    }

    @Override
    protected List<StringValue> items() {
        return items;
    }

    public static class StringValue {
        private static StringValueMonoid monoid = new StringValueMonoid();

        private final String text;

        public StringValue(String text) {
            this.text = text;
        }

        public static StringValue with(String text) {
            return new StringValue(text);
        }

        public StringValue append(StringValue that) {
            return StringValue.with(this.text + that.text);
        }

        public static Monoid<StringValue> monoid() {
            return monoid;
        }

        @Override
        public boolean equals(Object other) {
            if (other instanceof StringValue) {
                StringValue that = (StringValue) other;
                if (this.text == that.text) {
                    return true;
                } else if (this.text == null || that.text == null) {
                    return false;
                } else {
                    return this.text.equals(that.text);
                }
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return text.hashCode();
        }

        @Override
        public String toString() {
            return String.format("StringValue[text=%s]", text);
        }

        public static class StringValueMonoid implements Monoid<StringValue> {
            @Override
            public StringValue identityElement() {
                return StringValue.with("");
            }

            @Override
            public BiFunction<StringValue, StringValue, StringValue> addFunction()  {
                return StringValue::append;
            }
        }
    }
}
