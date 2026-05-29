package cc.ashclaw.common4j.web.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PageResult<T>")
class PageResultTest {

    @Nested
    @DisplayName("of()")
    class Of {

        @Test
        @DisplayName("should create from records, total, and query")
        void shouldCreateFromRecordsTotalAndQuery() {
            var records = List.of("a", "b", "c");
            var query = new PageQuery(2, 3);
            var pr = PageResult.of(records, 10, query);

            assertEquals(records, pr.records());
            assertEquals(10, pr.total());
            assertEquals(2, pr.page());
            assertEquals(3, pr.size());
        }

        @Test
        @DisplayName("records list should be unmodifiable")
        void recordsShouldBeUnmodifiable() {
            var pr = PageResult.of(List.of("a"), 1, new PageQuery());
            assertThrows(UnsupportedOperationException.class,
                    () -> pr.records().add("b"));
        }

        @Test
        @DisplayName("should accept empty records list")
        void shouldAcceptEmptyRecords() {
            var pr = PageResult.of(List.of(), 0, new PageQuery());
            assertTrue(pr.isEmpty());
        }

        @Test
        @DisplayName("should handle zero total with empty records")
        void shouldHandleZeroTotal() {
            var pr = PageResult.of(List.of(), 0, new PageQuery(1, 20));
            assertEquals(0, pr.total());
            assertEquals(0, pr.totalPages());
        }
    }

    @Nested
    @DisplayName("totalPages()")
    class TotalPages {

        @Test
        @DisplayName("exact division")
        void exactDivision() {
            var pr = PageResult.of(List.of("a", "b"), 10, new PageQuery(1, 5));
            assertEquals(2, pr.totalPages());
        }

        @Test
        @DisplayName("partial last page")
        void partialLastPage() {
            var pr = PageResult.of(List.of("a", "b", "c"), 10, new PageQuery(1, 3));
            assertEquals(4, pr.totalPages());
        }

        @Test
        @DisplayName("zero total should return zero pages")
        void zeroTotalShouldReturnZeroPages() {
            var pr = PageResult.of(List.of(), 0, new PageQuery(1, 20));
            assertEquals(0, pr.totalPages());
        }

        @Test
        @DisplayName("total less than size should return 1 page")
        void totalLessThanSize() {
            var pr = PageResult.of(List.of("a"), 1, new PageQuery(1, 20));
            assertEquals(1, pr.totalPages());
        }
    }

    @Nested
    @DisplayName("hasNext() / hasPrev()")
    class Navigation {

        @Test
        @DisplayName("first page of many should have next but no prev")
        void firstPageOfMany() {
            var pr = PageResult.of(List.of("a", "b"), 10, new PageQuery(1, 2));
            assertTrue(pr.hasNext());
            assertFalse(pr.hasPrev());
        }

        @Test
        @DisplayName("last page should have prev but no next")
        void lastPage() {
            var pr = PageResult.of(List.of("a", "b"), 10, new PageQuery(5, 2));
            assertFalse(pr.hasNext());
            assertTrue(pr.hasPrev());
        }

        @Test
        @DisplayName("middle page should have both")
        void middlePage() {
            var pr = PageResult.of(List.of("a", "b"), 9, new PageQuery(2, 3));
            assertTrue(pr.hasNext());
            assertTrue(pr.hasPrev());
        }

        @Test
        @DisplayName("single page should have neither")
        void singlePage() {
            var pr = PageResult.of(List.of("a"), 1, new PageQuery(1, 20));
            assertFalse(pr.hasNext());
            assertFalse(pr.hasPrev());
        }

        @Test
        @DisplayName("empty result should have neither")
        void emptyResult() {
            var pr = PageResult.of(List.of(), 0, new PageQuery(1, 20));
            assertFalse(pr.hasNext());
            assertFalse(pr.hasPrev());
        }
    }

    @Nested
    @DisplayName("isEmpty()")
    class IsEmpty {

        @Test
        @DisplayName("should return true when records list is empty")
        void shouldReturnTrueWhenEmpty() {
            var pr = PageResult.of(List.of(), 100, new PageQuery(1, 20));
            assertTrue(pr.isEmpty());
        }

        @Test
        @DisplayName("should return false when records list has items")
        void shouldReturnFalseWhenNotEmpty() {
            var pr = PageResult.of(List.of("a"), 100, new PageQuery(1, 20));
            assertFalse(pr.isEmpty());
        }
    }

    @Nested
    @DisplayName("map()")
    class Map {

        @Test
        @DisplayName("should transform records keeping metadata")
        void shouldTransformRecordsKeepingMetadata() {
            var pr = PageResult.of(List.of("a", "bb", "ccc"), 10, new PageQuery(1, 3));

            PageResult<Integer> mapped = pr.map(String::length);

            assertEquals(List.of(1, 2, 3), mapped.records());
            assertEquals(10, mapped.total());
            assertEquals(1, mapped.page());
            assertEquals(3, mapped.size());
        }

        @Test
        @DisplayName("should work on empty page")
        void shouldWorkOnEmptyPage() {
            var pr = PageResult.of(List.<String>of(), 0, new PageQuery());
            PageResult<Integer> mapped = pr.map(String::length);

            assertTrue(mapped.isEmpty());
            assertEquals(0, mapped.total());
        }
    }
}
