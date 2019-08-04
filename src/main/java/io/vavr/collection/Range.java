package io.vavr.collection;

import java.util.NoSuchElementException;

/**
 * A {@code Range} represents a finite sequence of elements.
 * <p>
 * Example: Use a Range in a for-loop
 *
 * <pre>{@code
 * // prints '0' to '10' on the console
 * for (int i : Range.inclusive(0, 10)) {
 *     System.out.println(i);
 * }
 * }</pre>
 *
 * Example: Performing an action for each element, with index
 *
 * <pre>{@code
 * // prints '0: 10' to '9: 1' on the console
 * Range.exclusive(10, 0).forEachWithIndex((element, index) -> {
 *     System.out.println(index + ": " + element);
 * });
 * }</pre>
 *
 * Example: Generic conversion
 *
 * <pre>{@code
 * // = List(102, 100, 98)
 * List<Integer> intList = Range.exclusiveBy('f', 'a', -2).to(List::ofAll);
 *
 * // = List(f, d, b)
 * List<Character> charList = list.map(i -> (char) i.shortValue());
 * }</pre>
 *
 * @param <T> element type
 */
public interface Range<T> extends io.vavr.Iterable<T> {

    static Range<Integer> inclusive(int from, int toInclusive) {
        return Range.inclusiveBy(from, toInclusive, from <= toInclusive ? 1 : -1);
    }

    static Range<Integer> inclusiveBy(int from, int toInclusive, int step) {
        if (step == 0) {
            throw new IllegalArgumentException("step cannot be 0");
        } else if (from == toInclusive) {
            return () -> Iterator.of(from);
        } if (step > 0) {
            if (from > toInclusive) {
                return Iterator::empty;
            } else {
                return () -> new AbstractRangeIterator(from, step) {
                    @Override
                    public boolean hasNext() {
                        return !overflow && next <= toInclusive;
                    }
                };
            }
        } else {
            if (from < toInclusive) {
                return Iterator::empty;
            } else {
                return () -> new AbstractRangeIterator(from, step) {
                    @Override
                    public boolean hasNext() {
                        return !overflow && next >= toInclusive;
                    }
                };
            }
        }
    }

    static Range<Integer> exclusive(int from, int toExclusive) {
        return Range.exclusiveBy(from, toExclusive, from <= toExclusive ? 1 : -1);
    }

    static Range<Integer> exclusiveBy(int from, int toExclusive, int step) {
        var signum = Integer.signum(step);
        var toInclusive = toExclusive - signum;
        if (Integer.signum(toInclusive) != Integer.signum(toExclusive)) {
            // because of abs(signum) <= abs(step) and overflow detection, toExclusive will not be included
            return Range.inclusiveBy(from, toExclusive, step);
        } else {
            return Range.inclusiveBy(from, toInclusive, step);
        }
    }

}

abstract class AbstractRangeIterator implements Iterator<Integer> {

    final int step;

    int next;
    boolean overflow = false;

    AbstractRangeIterator(int from, int step) {
        this.next = from;
        this.step = step;
    }

    @Override
    public Integer next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        final int curr = next;
        final int r = curr + step;
        overflow = ((curr ^ r) & (step ^ r)) < 0;
        next = r;
        return curr;
    }
}
