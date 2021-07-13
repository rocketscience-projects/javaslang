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

import io.vavr.collection.Iterator;

/**
 * {@code Value} is the root type of the Vavr hierarchy, except for functions and tuples.
 * It overrides {@link Iterable#iterator()} such that {@link io.vavr.collection.Iterator}
 * is returned.
 * <p>
 * {@code Value} intentionally has no {@code stream()} method, we align to the
 * <a href="http://mail.openjdk.java.net/pipermail/lambda-libs-spec-experts/2013-June/001910.html">design decision</a>
 * of the Java Expert Group.
 *
 * @param <T> Element type
 */
public interface Value<T> extends Iterable<T> {

    /**
     * Narrows a widened {@code Value<? extends T>} to {@code Value<T>}
     * by performing a type-safe cast. This is eligible because immutable/read-only
     * types are covariant.
     *
     * @param value A {@code Value}.
     * @param <T>   Component type of the {@code Value}.
     * @return the given {@code value} instance as narrowed type {@code Value<T>}.
     */
    @SuppressWarnings("unchecked")
    static <T> Value<T> narrow(Value<? extends T> value) {
        return (Value<T>) value;
    }

    /**
     * Returns a rich {@code io.vavr.collection.Iterator}.
     *
     * @return A new Iterator
     */
    @Override
    Iterator<T> iterator();

}
