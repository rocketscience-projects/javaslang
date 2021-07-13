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
import org.assertj.core.api.Assertions;
import org.assertj.core.api.IterableAssert;
import org.assertj.core.api.ObjectAssert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InvalidObjectException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

/**
 * Tests all methods defined in {@link Tree}.
 */
public class TreeTest extends AbstractTraversableTest {

    /**
     * <pre><code>
     *         1
     *        / \
     *       /   \
     *      /     \
     *     2       3
     *    / \     /
     *   4   5   6
     *  /       / \
     * 7       8   9
     * </code></pre>
     */
    private final Tree<Integer> tree = $(1, $(2, $(4, $(7)), $(5)), $(3, $(6, $(8), $(9))));

    @Override
    protected String stringPrefix() {
        return "Tree";
    }

    @Override
    protected <T> IterableAssert<T> assertThat(Iterable<T> actual) {
        return new IterableAssert<T>(actual) {
            @SuppressWarnings("unchecked")
            @Override
            public IterableAssert<T> isEqualTo(Object expected) {
                if (actual instanceof Option) {
                    final Option<?> opt1 = ((Option<?>) actual);
                    final Option<?> opt2 = (Option<?>) expected;
                    Assertions.assertThat(convOption(opt1)).isEqualTo(convOption(opt2));
                } else if (expected instanceof Map) {
                    final Map<?, ?> map1 = (Map<?, ?>) actual;
                    final Map<?, ?> map2 = (Map<?, ?>) expected;
                    Assertions.assertThat(convMap(map1)).isEqualTo(convMap(map2));
                } else if (expected instanceof Tree) {
                    assertThat(Stream.ofAll(actual)).isEqualTo(Stream.ofAll((Tree<?>) expected));
                } else {
                    Assertions.assertThat(actual).isEqualTo((Iterable<T>) expected);
                }
                return this;
            }

            private Option<?> convOption(Option<?> option) {
                return option.map(o -> (o instanceof Iterable) ? Stream.ofAll((Iterable<?>) o) : o);
            }

            private Map<?, ?> convMap(Map<?, ?> map) {
                return map.map((k, v) -> Tuple.of(k, v instanceof Iterable ? Stream.ofAll((Iterable<?>) v) : v));
            }
        };
    }

    @Override
    protected <T> ObjectAssert<T> assertThat(T actual) {
        return new ObjectAssert<T>(actual) {
            @Override
            public ObjectAssert<T> isEqualTo(Object expected) {
                if (actual instanceof Tuple2) {
                    final Tuple2<?, ?> t1 = (Tuple2<?, ?>) actual;
                    final Tuple2<?, ?> t2 = (Tuple2<?, ?>) expected;
                    assertThat((Iterable<?>) t1._1).isEqualTo(t2._1);
                    assertThat((Iterable<?>) t1._2).isEqualTo(t2._2);
                    return this;
                } else {
                    return super.isEqualTo(expected);
                }
            }
        };
    }

    @Override
    protected <T> Collector<T, ArrayList<T>, Tree<T>> collector() {
        return Tree.collector();
    }

    @Override
    protected <T> Tree<T> empty() {
        return Tree.empty();
    }

    @Override
    protected <T> Tree.Node<T> of(T element) {
        return Tree.of(element);
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    @Override
    protected final <T> Tree<T> of(T... elements) {
        return Tree.ofAll(List.of(elements));
    }

    @Override
    protected <T> Tree<T> ofAll(Iterable<? extends T> elements) {
        return Tree.ofAll(elements);
    }

    @Override
    protected <T extends Comparable<? super T>> Tree<T> ofJavaStream(java.util.stream.Stream<? extends T> javaStream) {
        return Tree.ofAll(javaStream);
    }

    @Override
    protected Tree<Boolean> ofAll(boolean... elements) {
        return Tree.ofAll(List.ofAll(elements));
    }

    @Override
    protected Tree<Byte> ofAll(byte... elements) {
        return Tree.ofAll(List.ofAll(elements));
    }

    @Override
    protected Tree<Character> ofAll(char... elements) {
        return Tree.ofAll(List.ofAll(elements));
    }

    @Override
    protected Tree<Double> ofAll(double... elements) {
        return Tree.ofAll(List.ofAll(elements));
    }

    @Override
    protected Tree<Float> ofAll(float... elements) {
        return Tree.ofAll(List.ofAll(elements));
    }

    @Override
    protected Tree<Integer> ofAll(int... elements) {
        return Tree.ofAll(List.ofAll(elements));
    }

    @Override
    protected Tree<Long> ofAll(long... elements) {
        return Tree.ofAll(List.ofAll(elements));
    }

    @Override
    protected Tree<Short> ofAll(short... elements) {
        return Tree.ofAll(List.ofAll(elements));
    }

    @Override
    protected <T> Tree<T> tabulate(int n, Function<? super Integer, ? extends T> f) {
        return Tree.tabulate(n, f);
    }

    @Override
    protected <T> Tree<T> fill(int n, Supplier<? extends T> s) {
        return Tree.fill(n, s);
    }

    protected <T> Tree<T> fill(int n, T element) {
        return Tree.fill(n, element);
    }

    @Override
    protected boolean useIsEqualToInsteadOfIsSameAs() {
        return true;
    }

    @Override
    protected int getPeekNonNilPerformingAnAction() {
        return 1;
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    protected final <T> Tree.Node<T> $(T value, Tree.Node<T>... children) {
        return Tree.of(value, children);
    }

    // -- static narrow

    @Test
    public void shouldNarrowTree() {
        final Tree<Double> doubles = of(1.0d);
        final Tree<Number> numbers = Tree.narrow(doubles);
        final boolean actual = numbers.contains(new BigDecimal("2.0"));
        assertThat(actual).isFalse();
    }

    // -- fill(int, Supplier)

    @Test
    public void shouldReturnManyAfterFillWithConstantSupplier() {
        assertThat(fill(17, () -> 7))
                .hasSize(17)
                .containsOnly(7);
    }

    // -- fill(int, T)

    @Test
    public void shouldReturnEmptyAfterFillWithZeroCount() {
        assertThat(fill(0, 7)).isEqualTo(empty());
    }

    @Test
    public void shouldReturnEmptyAfterFillWithNegativeCount() {
        assertThat(fill(-1, 7)).isEqualTo(empty());
    }

    @Test
    public void shouldReturnManyAfterFillWithConstant() {
        assertThat(fill(17, 7))
                .hasSize(17)
                .containsOnly(7);
    }

    // -- static recurse(T, Function)

    @Test
    public void shouldRecurseBuildTree() {
        Tree<Integer> generatedTree = Tree.recurse(1, p -> p == 0 ? List.empty()
                : p == 1 ? List.of(2, 3)
                : p == 2 ? List.of(4, 5)
                : p == 3 ? List.of(6)
                : p == 4 ? List.of(7)
                : p == 6 ? List.of(8, 9)
                : List.empty()
        );
        assertThat(generatedTree).isEqualTo(tree).hasToString(tree.toString());
        final List<? extends Tree<Integer>> children = generatedTree.getChildren();
        assertThat(children.length()).isEqualTo(2);
        assertThat(children.get(0).toLispString()).isEqualTo("(2 (4 7) 5)");
        assertThat(children.get(1).toLispString()).isEqualTo("(3 (6 8 9))");
    }

    // -- static build(Iterable, Function, Function)

    /**
     * Example tree:
     * <pre>
     * <code>
     *  1
     *        / \
     *       /   \
     *      /     \
     *     2       3
     *    / \     /
     *   4   5   6
     *  /       / \
     * 7       8   9
     * </code>
     * </pre>
     */
    @Test
    public void shouldBuildTreeFromFlatSource() {
        //List[(id, parent)]
        Iterable<Tuple2<Integer, Integer>> flatSourceOfTreeStructure = List.of(
                Tuple.of(1, null)
                , Tuple.of(2, 1), Tuple.of(3, 1)
                , Tuple.of(4, 2), Tuple.of(5, 2), Tuple.of(6, 3)
                , Tuple.of(7, 4), Tuple.of(8, 6), Tuple.of(9, 6)
        );
        List<Tree.Node<Tuple2<Integer, Integer>>> roots = Tree.build(flatSourceOfTreeStructure, Tuple2::_1, Tuple2::_2);
        assertThat(roots).isNotEmpty().hasSize(1);
        //Tree[id]
        Tree<Integer> root = roots.head().map(Tuple2::_1);
        assertThat(root).isEqualTo(tree).hasToString(tree.toString());
        final List<? extends Tree<Integer>> children = root.getChildren();
        assertThat(children.length()).isEqualTo(2);
        assertThat(children.get(0).toLispString()).isEqualTo("(2 (4 7) 5)");
        assertThat(children.get(1).toLispString()).isEqualTo("(3 (6 8 9))");
    }

    @Test
    public void shouldBuildEmptyRootListFromFlatSourceWithoutRoot() {
        //List[(id, parent)]
        Iterable<Tuple2<Integer, Integer>> flatSourceOfTreeStructure = List.of(
                Tuple.of(1, 1)
                , Tuple.of(2, 1), Tuple.of(3, 1)
        );
        List<Tree.Node<Tuple2<Integer, Integer>>> roots = Tree.build(flatSourceOfTreeStructure, Tuple2::_1, Tuple2::_2);
        assertThat(roots).isEmpty();
    }

    @Test
    public void shouldIgnoreCyclesWhileBuildFromFlatSource() {
        //List[(id, parent)]
        Iterable<Tuple2<Integer, Integer>> flatSourceOfTreeStructure = List.of(
                Tuple.of(1, null)
                //Cycle: (2 -> 3), (3 -> 4), (4 -> 2)
                , Tuple.of(2, 3), Tuple.of(3, 4), Tuple.of(4, 2)
        );
        List<Tree.Node<Tuple2<Integer, Integer>>> roots = Tree.build(flatSourceOfTreeStructure, Tuple2::_1, Tuple2::_2);
        assertThat(roots).isNotEmpty().hasSize(1);
        //Tree[id]
        Tree<Integer> root = roots.head().map(Tuple2::_1);
        assertThat(root).isEqualTo($(1));
    }

    @Test
    public void shouldBuildListWithManyRootsIfAny() {
        //List[(id, parent)]
        Iterable<Tuple2<Integer, Integer>> flatSourceOfTreeStructure = List.of(
                //Subtree 1
                Tuple.of(1, null)
                , Tuple.of(2, 1), Tuple.of(3, 1)
                , Tuple.of(4, 2)
                //Subtree 2
                , Tuple.of(10, null)
                , Tuple.of(20, 10), Tuple.of(30, 10)
                , Tuple.of(40, 20)
        );
        List<Tree.Node<Tuple2<Integer, Integer>>> roots = Tree.build(flatSourceOfTreeStructure, Tuple2::_1, Tuple2::_2);
        assertThat(roots).isNotEmpty().hasSize(2);
        //Tree[id]
        Tree<Integer> root1 = roots.head().map(Tuple2::_1);
        assertThat(root1).isNotEmpty().isEqualTo($(1, $(2, $(4)), $(3)));
        //Tree[id]
        Tree<Integer> root2 = roots.tail().head().map(Tuple2::_1);
        assertThat(root2).isNotEmpty().isEqualTo($(10, $(20, $(40)), $(30)));
    }

    // -- Tree test

    @Test
    public void shouldInstantiateTreeBranchWithOf() {
        final Tree<Integer> actual = Tree.of(1, Tree.of(2), Tree.of(3));
        final Tree<Integer> expected = new Tree.Node<>(1, List.of(new Tree.Node<>(2, List.empty()), new Tree.Node<>(3, List.empty())));
        assertThat(actual).isEqualTo(expected);
    }

    // -- Leaf test

    @Test
    public void shouldInstantiateTreeLeafWithOf() {
        final Tree<Integer> actual = Tree.of(1);
        final Tree<Integer> expected = new Tree.Node<>(1, List.empty());
        assertThat(actual).isEqualTo(expected);
    }

    // -- Node test

    @Test
    public void shouldCreateANodeWithoutChildren() {
        new Tree.Node<>(1, List.empty());
    }

    @Test(expected = InvalidObjectException.class)
    public void shouldNotCallReadObjectOnNodeInstance() throws Throwable {
        Serializables.callReadObject(tree);
    }

    // -- getValue

    @Test(expected = UnsupportedOperationException.class)
    public void shouldNotGetValueOfNil() {
        Tree.empty().getValue();
    }

    @Test
    public void shouldNotGetValueOfNonNil() {
        assertThat(tree.getValue()).isEqualTo(1);
    }

    // -- size

    @Test
    public void shouldCalculateSizeOfALeaf() {
        assertThat($(0).size()).isEqualTo(1);
    }

    @Test
    public void shouldCalculateSizeOfNestedNodes() {
        assertThat(tree.size()).isEqualTo(9);
    }

    // -- isEmpty

    @Test
    public void shouldIdentifyNilAsEmpty() {
        assertThat(Tree.empty().isEmpty()).isTrue();
    }

    @Test
    public void shouldIdentifyNonNilAsNotEmpty() {
        assertThat(tree.isEmpty()).isFalse();
    }

    // -- isLeaf

    @Test
    public void shouldIdentifyLeafAsLeaf() {
        assertThat($(0).isLeaf()).isTrue();
    }

    @Test
    public void shouldIdentifyNonLeafAsNonLeaf() {
        assertThat(tree.isLeaf()).isFalse();
    }

    @Test
    public void shouldIdentifyNilAsNonLeaf() {
        assertThat(Tree.empty().isLeaf()).isFalse();
    }

    // -- isBranch

    @Test
    public void shouldIdentifyLeafAsNonBranch() {
        assertThat($(0).isBranch()).isFalse();
    }

    @Test
    public void shouldIdentifyNonLeafAsBranch() {
        assertThat(tree.isBranch()).isTrue();
    }

    @Test
    public void shouldIdentifyNilAsNonBranch() {
        assertThat(Tree.empty().isBranch()).isFalse();
    }

    // -- getChildren

    @Test
    public void shouldGetChildrenOfLeaf() {
        assertThat($(0).getChildren()).isEqualTo(List.empty());
    }

    @Test
    public void shouldGetChildrenOfBranch() {
        final List<? extends Tree<Integer>> children = tree.getChildren();
        assertThat(children.length()).isEqualTo(2);
        assertThat(children.get(0).toLispString()).isEqualTo("(2 (4 7) 5)");
        assertThat(children.get(1).toLispString()).isEqualTo("(3 (6 8 9))");
    }

    @Test
    public void shouldIGetChildrenOfNil() {
        assertThat(Tree.empty().getChildren()).isEqualTo(List.empty());
    }

    // -- branchCount

    @Test
    public void shouldCountBranchesOfNil() {
        assertThat(Tree.empty().branchCount()).isEqualTo(0);
    }

    @Test
    public void shouldCountBranchesOfNonNil() {
        assertThat(tree.branchCount()).isEqualTo(5);
    }

    // -- leafCount

    @Test
    public void shouldCountLeavesOfNil() {
        assertThat(Tree.empty().leafCount()).isEqualTo(0);
    }

    @Test
    public void shouldCountLeavesOfNonNil() {
        assertThat(tree.leafCount()).isEqualTo(4);
    }

    // -- nodeCount

    @Test
    public void shouldCountNodesOfNil() {
        assertThat(Tree.empty().nodeCount()).isEqualTo(0);
    }

    @Test
    public void shouldCountNodesOfNonNil() {
        assertThat(tree.nodeCount()).isEqualTo(9);
    }

    // -- contains

    @Test
    public void shouldNotFindNodeInNil() {
        assertThat(Tree.empty().contains(1)).isFalse();
    }

    @Test
    public void shouldFindExistingNodeInNonNil() {
        assertThat(tree.contains(5)).isTrue();
    }

    @Test
    public void shouldNotFindNonExistingNodeInNonNil() {
        assertThat(tree.contains(0)).isFalse();
    }

    // -- flatMap

    @Test
    public void shouldFlatMapEmptyTree() {
        assertThat(Tree.empty().flatMap(t -> Tree.of(1))).isEqualTo(Tree.empty());
    }

    @Test
    public void shouldFlatMapNonEmptyTree() {
        final Tree.Node<Integer> testee = $(1, $(2), $(3));
        final Tree<Integer> actual = testee.flatMap(i -> $(i, $(i), $(i)));
        final Tree<Integer> expected = $(1, $(1), $(1), $(2, $(2), $(2)), $(3, $(3), $(3)));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldFlatMapNonEmptyByExpandingElements() {
        assertThat(of(1, 2, 3).flatMap(i -> {
            switch (i) {
                case 1:
                    return of(1, 2, 3);
                case 2:
                    return of(4, 5);
                default:
                    return of(6);
            }
        })).isEqualTo($(1, $(2), $(3), $(4, $(5)), $(6)));
    }

    @Test
    public void shouldFlatMapNonEmptyInTheRightOrder() {
        final AtomicInteger seq = new AtomicInteger(0);
        final Tree<Integer> actualInts = $(0, $(1), $(2))
                .flatMap(ignored -> of(seq.getAndIncrement(), seq.getAndIncrement()));
        final Tree<Integer> expectedInts = $(0, $(1), $(2, $(3)), $(4, $(5)));
        assertThat(actualInts).isEqualTo(expectedInts);
    }

    // -- iterator

    @Override
    @Test
    public void shouldNotHasNextWhenNilIterator() {
        assertThat(Tree.empty().iterator().hasNext()).isFalse();
    }

    @Override
    @Test(expected = NoSuchElementException.class)
    public void shouldThrowOnNextWhenNilIterator() {
        Tree.empty().iterator().next();
    }

    @Override
    @Test
    public void shouldIterateFirstElementOfNonNil() {
        assertThat(tree.iterator().next()).isEqualTo(1);
    }

    @Override
    @Test
    public void shouldFullyIterateNonNil() {
        final int length = List
                .of(1, 2, 4, 7, 5, 3, 6, 8, 9)
                .zip(tree)
                .filter(t -> Objects.equals(t._1, t._2))
                .length();
        assertThat(length).isEqualTo(9);
    }

    // -- map

    @Test
    public void shouldMapEmpty() {
        assertThat(Tree.empty().map(i -> i)).isEqualTo(Tree.empty());
    }

    @Test
    public void shouldMapTree() {
        assertThat(tree.map(i -> (char) (i + 64)).toLispString()).isEqualTo("(A (B (D G) E) (C (F H I)))");
    }

    // -- replace

    @Test
    public void shouldReplaceNullInEmpty() {
        assertThat(Tree.empty().replace(null, null)).isEmpty();
    }

    @Test
    public void shouldReplaceFirstOccurrenceUsingDepthFirstSearchInNonEmptyTree() {
        //   1        1
        //  / \  ->  / \
        // 2   3    99  3
        final Tree<Integer> testee = Tree.of(1, Tree.of(2), Tree.of(3));
        final Tree<Integer> actual = testee.replace(2, 99);
        final Tree<Integer> expected = Tree.of(1, Tree.of(99), Tree.of(3));
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void shouldNotReplaceAnyElementIfThereIsNoOccurrenceInNonEmptyTree() {
        final Tree<Integer> testee = Tree.of(1, Tree.of(2), Tree.of(3));
        final Tree<Integer> actual = testee.replace(4, 99);
        assertThat(actual).isEqualTo(testee);
    }

    // -- values()

    @Test
    public void shouldTraverseValuesOfEmptyTree() {
        assertThat(Tree.empty().values()).isEqualTo(empty());
    }

    // -- values(Order)

    @Test
    public void shouldTraverseValuesUsingPreOrder() {
        assertThat(tree.values(Tree.Order.PRE_ORDER)).isEqualTo(Stream.of(1, 2, 4, 7, 5, 3, 6, 8, 9));
    }

    @Test
    public void shouldTraverseValuesUsingInOrder() {
        assertThat(tree.values(Tree.Order.IN_ORDER)).isEqualTo(Stream.of(7, 4, 2, 5, 1, 8, 6, 9, 3));
    }

    @Test
    public void shouldTraverseValuesUsingPostOrder() {
        assertThat(tree.values(Tree.Order.POST_ORDER)).isEqualTo(Stream.of(7, 4, 5, 2, 8, 9, 6, 3, 1));
    }

    @Test
    public void shouldTraverseValuesUsingLevelOrder() {
        assertThat(tree.values(Tree.Order.LEVEL_ORDER)).isEqualTo(Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9));
    }

    // equals

    @SuppressWarnings("EqualsWithItself")
    @Test
    public void shouldBeAwareThatTwoTreesOfSameInstanceAreEqual() {
        // DEV_NOTE: intentionally not called `assertThat(Tree.empty()).isEqualTo(Tree.empty())`
        assertThat(Tree.empty().equals(Tree.empty())).isTrue();
    }

    @Test
    public void shouldBeAwareOfTwoDifferentEqualTrees() {
        assertThat($(0).equals($(0))).isTrue();
    }

    @Test
    public void shouldBeAwareThatTreeNotEqualsObject() {
        assertThat($(0)).isNotEqualTo(new Object());
    }

    // hashCode

    @Test
    public void shouldBeAwareThatHashCodeOfEmptyIsOne() {
        assertThat(Tree.empty().hashCode()).isEqualTo(1);
    }

    @Test
    public void shouldBeAwareThatHashCodeOfLeafIsGreaterThanOne() {
        assertThat($(0).hashCode()).isGreaterThan(1);
    }

    // -- transform()

    @Test
    public void shouldTransform() {
        final String transformed = $(42, $(2), $(3)).transform(v -> String.valueOf(v.head()));
        assertThat(transformed).isEqualTo("42");
    }

    // toString

    @Test
    public void shouldReturnStringRepresentationOfEmpty() {
        assertThat(Tree.empty().toString()).isEqualTo("Tree()");
    }

    @Test
    public void shouldReturnLispStringRepresentationOfNode() {
        assertThat(tree.toString()).isEqualTo("Tree(1, 2, 4, 7, 5, 3, 6, 8, 9)");
    }

    // -- toLispString

    @Test
    public void shouldConvertEmptyToLispString() {
        assertThat(Tree.empty().toLispString()).isEqualTo("()");
    }

    @Test
    public void shouldConvertNonEmptyToLispString() {
        assertThat(tree.toLispString()).isEqualTo("(1 (2 (4 7) 5) (3 (6 8 9)))");
    }

    // draw

    @Test
    public void shouldReturnDrawStringOfEmpty() {
        assertThat(Tree.empty().draw()).isEqualTo("▣");
    }

    @Test
    public void shouldReturnDrawStringOfNode() {
        assertThat(tree.draw()).isEqualTo("1\n" +
                "├──2\n" +
                "│  ├──4\n" +
                "│  │  └──7\n" +
                "│  └──5\n" +
                "└──3\n" +
                "   └──6\n" +
                "      ├──8\n" +
                "      └──9");
    }

    // -- serialization

    @Test
    public void shouldSerializeDeserializeComplexTree() {
        final Object actual = Serializables.deserialize(Serializables.serialize(tree));
        assertThat(actual).isEqualTo(tree);
    }

    // ~~~~~~~ DISABLED TESTS ~~~~~~~

    // -- distinctBy(Comparator)

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceWhenDistinctByComparatorEmptyTraversable() {
        // TODO: remove this overridden method with #1826
    }

    // -- distinctBy(Function)

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceWhenDistinctByFunctionEmptyTraversable() {
        // TODO: remove this overridden method with #1826
    }

    // -- drop

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceWhenDropZeroCount() {
        // Tree.drop() returns a Seq
    }

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceWhenDropNegativeCount() {
        // Tree.drop() returns a Seq
    }

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceWhenEmptyDropOne() {
        // Tree.drop() returns a Seq
    }

    // -- dropRight

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceWhenDropRightZeroCount() {
        // Tree.dropRight() returns a Seq
    }

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceWhenDropRightNegativeCount() {
        // Tree.dropRight() returns a Seq
    }

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceWhenEmptyDropRightOne() {
        // Tree.dropRight() returns a Seq
    }

    // -- dropUntil

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceWhenEmptyDropUntil() {
        // Tree.dropUntil returns a Seq
    }

    // -- dropWhile

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceWhenEmptyDropWhile() {
        // Tree.dropWhile returns a Seq
    }

    // -- filter

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceWhenFilteringEmptyTraversable() {
        // TODO: remove this overridden method with #1826
    }

    // -- filterNot

    @Ignore
    @Override
    @Test
    public void shouldReturnTheSameInstanceWhenFilterNotOnEmptyTraversable() {
        // TODO: remove this overridden method with #1826
    }

    // -- take

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceIfTakeAll() {
        // Tree.take returns Seq
    }

    // -- takeRight

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceIfTakeRightAll() {
        // Tree.takeRight returns Seq
    }

    // -- takeUntil

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceWhenEmptyTakeUntil() {
        // Tree.takeUntil() returns a Seq
    }

    // -- takeWhile

    @Ignore
    @Override
    @Test
    public void shouldReturnSameInstanceWhenEmptyTakeWhile() {
        // Tree.takeWhile() returns a Seq
    }

    // -- spliterator

    @Test
    public void shouldHaveOrderedSpliterator() {
        assertThat(of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.ORDERED)).isTrue();
    }

    @Test
    public void shouldNotHaveSortedSpliterator() {
        assertThat(of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.SORTED)).isFalse();
    }

    @Test
    public void shouldHaveSizedSpliterator() {
        assertThat(of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.SIZED | Spliterator.SUBSIZED)).isTrue();
    }

    @Test
    public void shouldNotHaveDistinctSpliterator() {
        assertThat(of(1, 2, 3).spliterator().hasCharacteristics(Spliterator.DISTINCT)).isFalse();
    }

    @Test
    public void shouldReturnSizeWhenSpliterator() {
        assertThat(of(1, 2, 3).spliterator().getExactSizeIfKnown()).isEqualTo(3);
    }

    // -- isSequential()

    @Test
    public void shouldReturnTrueWhenIsSequentialCalled() {
        assertThat(of(1, 2, 3).isSequential()).isTrue();
    }

}
