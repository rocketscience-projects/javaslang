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

import io.vavr.PartialFunction;
import io.vavr.Tuple;
import io.vavr.Tuple3;
import io.vavr.Tuple2;
import io.vavr.control.Option;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.function.*;

public final class IntMultimap<T> implements Traversable<T>, Serializable {

    private static final long serialVersionUID = 1L;

    private final Multimap<Integer, T> original;

    public static <T> IntMultimap<T> of(Multimap<Integer, T> original) {
        return new IntMultimap<>(original);
    }

    // DEV-NOTE: needs to be used internally to ensure the isSameAs property of the original is reflected by this impl
    private IntMultimap<T> unit(Multimap<Integer, T> original) {
        return (this.original == original) ? this : of(original);
    }

    private IntMultimap(Multimap<Integer, T> original) {
        this.original = original;
    }

    @Override
    public String stringPrefix() {
        return "IntMultimap";
    }

    @Override
    public boolean isDistinct() {
        return original.isDistinct();
    }

    @Override
    public boolean isLazy() {
        return original.isLazy();
    }

    @Override
    public boolean isSequential() {
        return original.isSequential();
    }

    @Override
    public boolean equals(Object o) {
        final Object that = (o instanceof IntMultimap) ? ((IntMultimap) o).original : o;
        return Collections.equals(original, that);
    }

    @Override
    public int hashCode() {
        return original.hashCode();
    }

    @Override
    public String toString() {
        return mkString(stringPrefix() + "(", ", ", ")");
    }

    @Override
    public <R> Seq<R> collect(PartialFunction<? super T, ? extends R> partialFunction) {
        Objects.requireNonNull(partialFunction, "partialFunction is null");
        final PartialFunction<Tuple2<Integer, T>, R> pf = new PartialFunction<Tuple2<Integer, T>, R>() {
            private static final long serialVersionUID = 1L;
            @Override
            public R apply(Tuple2<Integer, T> entry) {
                return partialFunction.apply(entry._2);
            }
            @Override
            public boolean isDefinedAt(Tuple2<Integer, T> entry) {
                return partialFunction.isDefinedAt(entry._2);
            }
        };
        return original.collect(pf);
    }

    @Override
    public IntMultimap<T> distinct() {
        return unit(original.distinct());
    }

    @Override
    public IntMultimap<T> distinctBy(Comparator<? super T> comparator) {
        return unit(original.distinctBy((o1, o2) -> comparator.compare(o1._2, o2._2)));
    }

    @Override
    public <U> IntMultimap<T> distinctBy(Function<? super T, ? extends U> keyExtractor) {
        return unit(original.distinctBy(f -> keyExtractor.apply(f._2)));
    }

    @Override
    public IntMultimap<T> drop(int n) {
        final Multimap<Integer, T> dropped = original.drop(n);
        return dropped == original ? this : unit(dropped);
    }

    @Override
    public IntMultimap<T> dropRight(int n) {
        final Multimap<Integer, T> dropped = original.dropRight(n);
        return dropped == original ? this : unit(dropped);
    }

    @Override
    public IntMultimap<T> dropUntil(Predicate<? super T> predicate) {
        return unit(original.dropUntil(p -> predicate.test(p._2)));
    }

    @Override
    public IntMultimap<T> dropWhile(Predicate<? super T> predicate) {
        return unit(original.dropWhile(p -> predicate.test(p._2)));
    }

    @Override
    public IntMultimap<T> filter(Predicate<? super T> predicate) {
        return unit(original.filter(p -> predicate.test(p._2)));
    }

    @Override
    public IntMultimap<T> filterNot(Predicate<? super T> predicate) {
        return unit(original.filterNot(p -> predicate.test(p._2)));
    }

    @Override
    public <U> Seq<U> flatMap(Function<? super T, ? extends Iterable<? extends U>> mapper) {
        return original.flatMap(e -> mapper.apply(e._2));
    }

    @Override
    public <U> U foldRight(U zero, BiFunction<? super T, ? super U, ? extends U> f) {
        Objects.requireNonNull(f, "f is null");
        return original.foldRight(zero, (e, u) -> f.apply(e._2, u));
    }

    @Override
    public <C> Map<C, ? extends IntMultimap<T>> groupBy(Function<? super T, ? extends C> classifier) {
        return original.groupBy(e -> classifier.apply(e._2)).map((k, v) -> Tuple.of(k, IntMultimap.of(v)));
    }

    @Override
    public Iterator<IntMultimap<T>> grouped(int size) {
        return original.grouped(size).map(IntMultimap::of);
    }

    @Override
    public boolean hasDefiniteSize() {
        return original.hasDefiniteSize();
    }

    @Override
    public T head() {
        return original.head()._2;
    }

    @Override
    public Option<T> headOption() {
        return original.headOption().map(o -> o._2);
    }

    @Override
    public IntMultimap<T> init() {
        return IntMultimap.of(original.init());
    }

    @Override
    public Option<? extends IntMultimap<T>> initOption() {
        return original.initOption().map(IntMultimap::of);
    }

    @Override
    public boolean isEmpty() {
        return original.isEmpty();
    }

    @Override
    public boolean isTraversableAgain() {
        return original.isTraversableAgain();
    }

    @Override
    public T last() {
        return original.last()._2;
    }

    @Override
    public int length() {
        return original.length();
    }

    @Override
    public <U> Seq<U> map(Function<? super T, ? extends U> mapper) {
        return original.map(e -> mapper.apply(e._2));
    }

    @Override
    public IntMultimap<T> orElse(Iterable<? extends T> other) {
        return unit(original.orElse(List.ofAll(other).zipWithIndex().map(t -> Tuple.of(t._2, t._1))));
    }

    @SuppressWarnings("unchecked")
    @Override
    public IntMultimap<T> orElse(Supplier<? extends Iterable<? extends T>> supplier) {
        return unit(original.orElse(() -> (Iterable<? extends Tuple2<Integer, T>>) List.ofAll(supplier.get()).zipWithIndex().map(t -> Tuple.of(t._2, t._1))));
    }

    @Override
    public Tuple2<IntMultimap<T>, IntMultimap<T>> partition(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate, "predicate is null");
        return original.partition(p -> predicate.test(p._2)).map(IntMultimap::of, IntMultimap::of);
    }

    @Override
    public IntMultimap<T> peek(Consumer<? super T> action) {
        original.peek(e -> action.accept(e._2));
        return this;
    }

    @Override
    public IntMultimap<T> replace(T currentElement, T newElement) {
        final Option<Tuple2<Integer, T>> currentEntryOpt = original.find(e -> e._2.equals(currentElement));
        if (currentEntryOpt.isDefined()) {
            final Tuple2<Integer, T> currentEntry = currentEntryOpt.get();
            return unit(original.replace(currentEntry, Tuple.of(original.size() + 1, newElement)));
        } else {
            return this;
        }
    }

    @Override
    public IntMultimap<T> replaceAll(T currentElement, T newElement) {
        Multimap<Integer, T> result = original;
        for (Tuple2<Integer, T> entry : original.filter(e -> e._2.equals(currentElement))) {
            result = result.replaceAll(entry, Tuple.of(entry._1, newElement));
        }
        return unit(result);
    }

    @Override
    public IntMultimap<T> retainAll(Iterable<? extends T> elements) {
        final Set<T> elementsSet = HashSet.ofAll(elements);
        return unit(original.retainAll(original.filter(e -> elementsSet.contains(e._2))));
    }

    @Override
    public Traversable<T> scan(T zero, BiFunction<? super T, ? super T, ? extends T> operation) {
        final int[] index = new int[] { 0 };
        return original.scan(Tuple.of(-1, zero), (i, t) -> Tuple.of(index[0]++, operation.apply(i._2, t._2))).values();
    }

    @Override
    public <U> Traversable<U> scanLeft(U zero, BiFunction<? super U, ? super T, ? extends U> operation) {
        return original.scanLeft(zero, (i, t) -> operation.apply(i, t._2));
    }

    @Override
    public <U> Traversable<U> scanRight(U zero, BiFunction<? super T, ? super U, ? extends U> operation) {
        return original.scanRight(zero, (t, i) -> operation.apply(t._2, i));
    }

    @Override
    public Iterator<IntMultimap<T>> slideBy(Function<? super T, ?> classifier) {
        return original.slideBy(e -> classifier.apply(e._2)).map(IntMultimap::of);
    }

    @Override
    public Iterator<IntMultimap<T>> sliding(int size) {
        return original.sliding(size).map(IntMultimap::of);
    }

    @Override
    public Iterator<IntMultimap<T>> sliding(int size, int step) {
        return original.sliding(size, step).map(IntMultimap::of);
    }

    @Override
    public Tuple2<? extends IntMultimap<T>, ? extends IntMultimap<T>> span(Predicate<? super T> predicate) {
        return original.span(p -> predicate.test(p._2)).map(IntMultimap::of, IntMultimap::of);
    }

    public Spliterator<T> spliterator() {
        class SpliteratorProxy implements Spliterator<T> {
            private final Spliterator<Tuple2<Integer, T>> spliterator;

            private SpliteratorProxy(Spliterator<Tuple2<Integer, T>> spliterator) {
                this.spliterator = spliterator;
            }

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                return spliterator.tryAdvance(a -> action.accept(a._2));
            }

            @Override
            public Spliterator<T> trySplit() {
                return new SpliteratorProxy(spliterator.trySplit());
            }

            @Override
            public long estimateSize() {
                return spliterator.estimateSize();
            }

            @Override
            public int characteristics() {
                return spliterator.characteristics();
            }

            @Override
            public Comparator<? super T> getComparator() {
                if (hasCharacteristics(Spliterator.SORTED)) {
                    return null;
                }
                throw new IllegalStateException();
            }
        }
        return new SpliteratorProxy(original.spliterator());
    }

    @Override
    public IntMultimap<T> tail() {
        return IntMultimap.of(original.tail());
    }

    @Override
    public Option<IntMultimap<T>> tailOption() {
        return original.tailOption().map(IntMultimap::of);
    }

    @Override
    public IntMultimap<T> take(int n) {
        return unit(original.take(n));
    }

    @Override
    public IntMultimap<T> takeRight(int n) {
        return unit(original.takeRight(n));
    }

    @Override
    public Traversable<T> takeUntil(Predicate<? super T> predicate) {
        return unit(original.takeUntil(p -> predicate.test(p._2)));
    }

    @Override
    public IntMultimap<T> takeWhile(Predicate<? super T> predicate) {
        return unit(original.takeWhile(p -> predicate.test(p._2)));
    }

    @Override
    public <U> Seq<Tuple2<T, U>> zip(Iterable<? extends U> that) {
        return zipWith(that, Tuple::of);
    }

    @Override
    public <U, R> Seq<R> zipWith(Iterable<? extends U> that, BiFunction<? super T, ? super U, ? extends R> mapper) {
        Objects.requireNonNull(that, "that is null");
        Objects.requireNonNull(mapper, "mapper is null");
        return Stream.ofAll(iterator().zipWith(that, mapper));
    }

    @Override
    public <U> Seq<Tuple2<T, U>> zipAll(Iterable<? extends U> that, T thisElem, U thatElem) {
        Objects.requireNonNull(that, "that is null");
        return Stream.ofAll(iterator().zipAll(that, thisElem, thatElem));
    }

    @Override
    public Seq<Tuple2<T, Integer>> zipWithIndex() {
        return zipWithIndex(Tuple::of);
    }

    @Override
    public <U> Seq<U> zipWithIndex(BiFunction<? super T, ? super Integer, ? extends U> mapper) {
        Objects.requireNonNull(mapper, "mapper is null");
        return Stream.ofAll(iterator().zipWithIndex(mapper));
    }
}
