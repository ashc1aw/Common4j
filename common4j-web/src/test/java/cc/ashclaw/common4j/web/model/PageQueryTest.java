package cc.ashclaw.common4j.web.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PageQuery")
class PageQueryTest {

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("default constructor should use page 1, size 20")
        void defaultConstructor() {
            var pq = new PageQuery();
            assertEquals(1, pq.page());
            assertEquals(20, pq.size());
        }

        @Test
        @DisplayName("of(int) should use given page and default size")
        void of() {
            var pq = PageQuery.of(3);
            assertEquals(3, pq.page());
            assertEquals(20, pq.size());
        }

        @Test
        @DisplayName("should store given page and size")
        void shouldStoreGivenPageAndSize() {
            var pq = new PageQuery(5, 50);
            assertEquals(5, pq.page());
            assertEquals(50, pq.size());
        }

        @Test
        @DisplayName("should have proper equals and hashCode")
        void shouldHaveProperEqualsAndHashCode() {
            var a = new PageQuery(3, 20);
            var b = new PageQuery(3, 20);
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("toString should include page and size")
        void toStringShouldIncludePageAndSize() {
            var pq = new PageQuery(2, 30);
            var str = pq.toString();
            assertTrue(str.contains("2"));
            assertTrue(str.contains("30"));
        }
    }

    @Nested
    @DisplayName("Clamping")
    class Clamping {

        @Test
        @DisplayName("page < 1 should clamp to 1")
        void pageBelow1ShouldClampTo1() {
            assertEquals(1, new PageQuery(0, 20).page());
            assertEquals(1, new PageQuery(-5, 20).page());
        }

        @Test
        @DisplayName("size < 1 should clamp to default 20")
        void sizeBelow1ShouldClampToDefault() {
            assertEquals(20, new PageQuery(1, 0).size());
            assertEquals(20, new PageQuery(1, -1).size());
        }

        @Test
        @DisplayName("size > 100 should clamp to 100")
        void sizeAbove100ShouldClampTo100() {
            assertEquals(100, new PageQuery(1, 101).size());
            assertEquals(100, new PageQuery(1, 999).size());
        }

        @Test
        @DisplayName("size 1 should be accepted")
        void size1ShouldBeAccepted() {
            assertEquals(1, new PageQuery(1, 1).size());
        }

        @Test
        @DisplayName("size 100 should be accepted")
        void size100ShouldBeAccepted() {
            assertEquals(100, new PageQuery(1, 100).size());
        }
    }

    @Nested
    @DisplayName("offset()")
    class Offset {

        @Test
        @DisplayName("page 1 size 20 should offset 0")
        void page1Size20() {
            assertEquals(0, new PageQuery(1, 20).offset());
        }

        @Test
        @DisplayName("page 3 size 20 should offset 40")
        void page3Size20() {
            assertEquals(40, new PageQuery(3, 20).offset());
        }

        @Test
        @DisplayName("page 2 size 50 should offset 50")
        void page2Size50() {
            assertEquals(50, new PageQuery(2, 50).offset());
        }

        @Test
        @DisplayName("default query should offset 0")
        void defaultQueryShouldOffset0() {
            assertEquals(0, new PageQuery().offset());
        }
    }
}
