package cc.ashclaw.common4j.core.collections;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CollectionUtils")
class CollectionUtilsTest {

    @Nested
    @DisplayName("partition()")
    class Partition {

        @Test
        @DisplayName("should split list evenly when size divides total")
        void shouldSplitEvenly() {
            var result = CollectionUtils.partition(List.of(1, 2, 3, 4), 2);
            assertEquals(2, result.size());
            assertEquals(List.of(1, 2), result.get(0));
            assertEquals(List.of(3, 4), result.get(1));
        }

        @Test
        @DisplayName("last batch should be smaller when size does not divide total")
        void lastBatchShouldBeSmaller() {
            var result = CollectionUtils.partition(List.of("a", "b", "c", "d", "e"), 2);
            assertEquals(3, result.size());
            assertEquals(List.of("a", "b"), result.get(0));
            assertEquals(List.of("c", "d"), result.get(1));
            assertEquals(List.of("e"), result.get(2));
        }

        @Test
        @DisplayName("size larger than list should return single batch")
        void sizeLargerThanList() {
            var result = CollectionUtils.partition(List.of(1, 2, 3), 10);
            assertEquals(1, result.size());
            assertEquals(List.of(1, 2, 3), result.get(0));
        }

        @Test
        @DisplayName("size 1 should create one batch per element")
        void size1ShouldCreateOneBatchPerElement() {
            var result = CollectionUtils.partition(List.of("x", "y", "z"), 1);
            assertEquals(3, result.size());
            assertEquals(List.of("x"), result.get(0));
            assertEquals(List.of("y"), result.get(1));
            assertEquals(List.of("z"), result.get(2));
        }

        @Test
        @DisplayName("empty list should return empty partition list")
        void emptyListShouldReturnEmpty() {
            var result = CollectionUtils.partition(List.of(), 5);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("outer result should be unmodifiable")
        void outerResultShouldBeUnmodifiable() {
            var result = CollectionUtils.partition(List.of(1, 2), 1);
            assertThrows(UnsupportedOperationException.class, () -> result.add(List.of(3)));
        }

        @Test
        @DisplayName("inner batches should be unmodifiable")
        void innerBatchesShouldBeUnmodifiable() {
            var result = CollectionUtils.partition(List.of(1, 2), 1);
            assertThrows(UnsupportedOperationException.class, () -> result.get(0).add(3));
        }

        @Test
        @DisplayName("should throw NPE when list is null")
        void shouldThrowNpeWhenListIsNull() {
            assertThrows(NullPointerException.class, () -> CollectionUtils.partition(null, 5));
        }

        @Test
        @DisplayName("should throw IAE when size is zero")
        void shouldThrowIaeWhenSizeIsZero() {
            assertThrows(IllegalArgumentException.class,
                    () -> CollectionUtils.partition(List.of(1, 2), 0));
        }

        @Test
        @DisplayName("should throw IAE when size is negative")
        void shouldThrowIaeWhenSizeIsNegative() {
            assertThrows(IllegalArgumentException.class,
                    () -> CollectionUtils.partition(List.of(1, 2), -1));
        }

        @Test
        @DisplayName("batches should be independent copies of the original")
        void batchesShouldBeIndependentCopies() {
            var original = new java.util.ArrayList<>(List.of(1, 2, 3));
            var result = CollectionUtils.partition(original, 2);
            original.set(0, 99);
            assertEquals(1, result.get(0).get(0), "batch should retain original value");
        }
    }

    @Nested
    @DisplayName("intersection()")
    class Intersection {

        @Test
        @DisplayName("should return common elements")
        void shouldReturnCommonElements() {
            var result = CollectionUtils.intersection(
                    List.of(1, 2, 3), List.of(2, 3, 4));
            assertEquals(Set.of(2, 3), result);
        }

        @Test
        @DisplayName("should return empty set when there is no overlap")
        void shouldReturnEmptyWhenNoOverlap() {
            var result = CollectionUtils.intersection(
                    Set.of(1, 2), Set.of(3, 4));
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should return all elements when sets are equal")
        void shouldReturnAllWhenEqual() {
            var result = CollectionUtils.intersection(
                    List.of(1, 2), List.of(1, 1, 2, 2));
            assertEquals(Set.of(1, 2), result);
        }

        @Test
        @DisplayName("result should be unmodifiable")
        void resultShouldBeUnmodifiable() {
            var result = CollectionUtils.intersection(Set.of(1), Set.of(1));
            assertThrows(UnsupportedOperationException.class, () -> result.add(2));
        }

        @Test
        @DisplayName("should throw NPE when first argument is null")
        void shouldThrowNpeWhenFirstArgIsNull() {
            assertThrows(NullPointerException.class,
                    () -> CollectionUtils.intersection(null, Set.of(1)));
        }

        @Test
        @DisplayName("should throw NPE when second argument is null")
        void shouldThrowNpeWhenSecondArgIsNull() {
            assertThrows(NullPointerException.class,
                    () -> CollectionUtils.intersection(Set.of(1), null));
        }

        @Test
        @DisplayName("should accept 3 or more collections")
        void shouldAcceptThreeOrMoreCollections() {
            var result = CollectionUtils.intersection(
                    List.of(1, 2, 3), List.of(2, 3, 4), Set.of(2, 5));
            assertEquals(Set.of(2), result);
        }

        @Test
        @DisplayName("three disjoint collections should return empty")
        void threeDisjointShouldReturnEmpty() {
            var result = CollectionUtils.intersection(
                    Set.of(1), Set.of(2), Set.of(3));
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should throw NPE when any vararg is null")
        void shouldThrowNpeWhenVarargIsNull() {
            assertThrows(NullPointerException.class,
                    () -> CollectionUtils.intersection(Set.of(1), Set.of(2), (Set<Integer>) null));
        }
    }

    @Nested
    @DisplayName("union()")
    class Union {

        @Test
        @DisplayName("should return all distinct elements from both")
        void shouldReturnAllDistinctElements() {
            var result = CollectionUtils.union(
                    List.of(1, 2), List.of(2, 3));
            assertEquals(Set.of(1, 2, 3), result);
        }

        @Test
        @DisplayName("should deduplicate overlapping elements")
        void shouldDeduplicateOverlappingElements() {
            var result = CollectionUtils.union(
                    List.of(1, 2), List.of(1, 2));
            assertEquals(Set.of(1, 2), result);
        }

        @Test
        @DisplayName("should include all when disjoint")
        void shouldIncludeAllWhenDisjoint() {
            var result = CollectionUtils.union(Set.of(1), Set.of(2));
            assertEquals(Set.of(1, 2), result);
        }

        @Test
        @DisplayName("result should be unmodifiable")
        void resultShouldBeUnmodifiable() {
            var result = CollectionUtils.union(Set.of(1), Set.of(2));
            assertThrows(UnsupportedOperationException.class, () -> result.add(3));
        }

        @Test
        @DisplayName("should throw NPE when first argument is null")
        void shouldThrowNpeWhenFirstArgIsNull() {
            assertThrows(NullPointerException.class,
                    () -> CollectionUtils.union(null, Set.of(1)));
        }

        @Test
        @DisplayName("should accept 3 or more collections")
        void shouldAcceptThreeOrMoreCollections() {
            var result = CollectionUtils.union(
                    Set.of(1, 2), Set.of(3), Set.of(2, 4));
            assertEquals(Set.of(1, 2, 3, 4), result);
        }

        @Test
        @DisplayName("three disjoint collections should include all")
        void threeDisjointShouldIncludeAll() {
            var result = CollectionUtils.union(Set.of(1), Set.of(2), Set.of(3));
            assertEquals(Set.of(1, 2, 3), result);
        }

        @Test
        @DisplayName("should throw NPE when any vararg is null")
        void shouldThrowNpeWhenVarargIsNull() {
            assertThrows(NullPointerException.class,
                    () -> CollectionUtils.union(Set.of(1), Set.of(2), (Set<Integer>) null));
        }
    }

    @Nested
    @DisplayName("difference()")
    class Difference {

        @Test
        @DisplayName("should return elements only in source")
        void shouldReturnElementsOnlyInSource() {
            var result = CollectionUtils.difference(
                    List.of(1, 2, 3), List.of(2, 3, 4));
            assertEquals(Set.of(1), result);
        }

        @Test
        @DisplayName("should return all source when there is no overlap")
        void shouldReturnAllSourceWhenNoOverlap() {
            var result = CollectionUtils.difference(
                    Set.of(1, 2), Set.of(3, 4));
            assertEquals(Set.of(1, 2), result);
        }

        @Test
        @DisplayName("should return empty when source is subset of remove")
        void shouldReturnEmptyWhenSourceIsSubset() {
            var result = CollectionUtils.difference(
                    Set.of(1, 2), Set.of(1, 2, 3));
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("result should be unmodifiable")
        void resultShouldBeUnmodifiable() {
            var result = CollectionUtils.difference(Set.of(1), Set.of(2));
            assertThrows(UnsupportedOperationException.class, () -> result.add(3));
        }

        @Test
        @DisplayName("should throw NPE when source is null")
        void shouldThrowNpeWhenSourceIsNull() {
            assertThrows(NullPointerException.class,
                    () -> CollectionUtils.difference(null, Set.of(1)));
        }
    }
}
