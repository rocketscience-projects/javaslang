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

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

abstract class AbstractQueue<T, Q extends AbstractQueue<T, Q>> implements Traversable<T> {

    /**
     * Removes an element from this Queue.
     *
     * @return a tuple containing the first element and the remaining elements of this Queue
     * @throws NoSuchElementException if this Queue is empty
     */
    public Tuple2<T, Q> dequeue() {
        if (isEmpty()) {
            throw new NoSuchElementException("dequeue of empty " + stringPrefix());
        } else {
            return Tuple.of(head(), tail());
        }
    }

    /**
     * Removes an element from this Queue.
     *
     * @return {@code None} if this Queue is empty, otherwise {@code Some} {@code Tuple} containing the first element and the remaining elements of this Queue
     */
    public Option<Tuple2<T, Q>> dequeueOption() {
        return isEmpty() ? Option.none() : Option.some(dequeue());
    }

    /**
     * Enqueues a new element.
     *
     * @param element The new element
     * @return a new {@code Queue} instance, containing the new element
     */
    public abstract Q enqueue(T element);

    /**
     * Enqueues the given elements. A queue has FIFO order, i.e. the first of the given elements is
     * the first which will be retrieved.
     *
     * @param elements Elements, may be empty
     * @return a new {@code Queue} instance, containing the new elements
     * @throws NullPointerException if elements is null
     */
    @SuppressWarnings("unchecked")
    public Q enqueue(T... elements) {
        Objects.requireNonNull(elements, "elements is null");
        return enqueueAll(List.of(elements));
    }

    /**
     * Enqueues the given elements. A queue has FIFO order, i.e. the first of the given elements is
     * the first which will be retrieved.
     *
     * @param elements An Iterable of elements, may be empty
     * @return a new {@code Queue} instance, containing the new elements
     * @throws NullPointerException if elements is null
     */
    public abstract Q enqueueAll(Iterable<? extends T> elements);

    /**
     * Returns the first element without modifying it.
     *
     * @return the first element
     * @throws NoSuchElementException if this Queue is empty
     */
    public T peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("peek of empty " + stringPrefix());
        } else {
            return head();
        }
    }

    /**
     * Returns the first element without modifying the Queue.
     *
     * @return {@code None} if this Queue is empty, otherwise a {@code Some} containing the first element
     */
    public Option<T> peekOption() {
        return isEmpty() ? Option.none() : Option.some(peek());
    }

    @Override
    public Q dropUntil(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return dropWhile(predicate.negate());
    }

    @Override
    public abstract Q dropWhile(Predicate<? super T> predicate);

    @SuppressWarnings("unchecked")
    @Override
    public Q filterNot(Predicate<? super T> predicate) {
        return Collections.filterNot((Q) this, predicate);
    }

    /**
     * Dual of {@linkplain #tail()}, returning all elements except the last.
     *
     * @return a new instance containing all elements except the last.
     * @throws UnsupportedOperationException if this is empty
     */
    @Override
    public abstract Q init();

    /**
     * Dual of {@linkplain #tailOption()}, returning all elements except the last as {@code Option}.
     *
     * @return {@code Some(Q)} or {@code None} if this is empty.
     */
    @Override
    public Option<Q> initOption() {
        return isEmpty() ? Option.none() : Option.some(init());
    }

    /**
     * Drops the first element of a non-empty Traversable.
     *
     * @return A new instance of Traversable containing all elements except the first.
     * @throws UnsupportedOperationException if this is empty
     */
    @Override
    public abstract Q tail();

    @Override
    public Option<Q> tailOption() {
        return isEmpty() ? Option.none() : Option.some(tail());
    }

    @Override
    @SuppressWarnings("unchecked")
    public Q retainAll(Iterable<? extends T> elements) {
        return Collections.retainAll((Q) this, elements);
    }

    @SuppressWarnings("unchecked")
    public Q removeAll(Iterable<? extends T> elements) {
        return Collections.removeAll((Q) this, elements);
    }

    @Override
    public Q takeWhile(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return takeUntil(predicate.negate());
    }

    @Override
    public abstract Q takeUntil(Predicate<? super T> predicate);

    @SuppressWarnings("unchecked")
    @Override
    public Q peek(Consumer<? super T> action) {
        Objects.requireNonNull(action, "action is null");
        if (!isEmpty()) {
            action.accept(head());
        }
        return (Q) this;
    }

    @Override
    public String toString() {
        return mkString(stringPrefix() + "(", ", ", ")");
    }

}
