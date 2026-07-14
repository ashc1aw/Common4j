package cc.ashclaw.common4j.web.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PageRequest<T>")
class PageRequestTest {

    /** 测试用业务条件对象 */
    record UserQuery(String name) {}

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("should store given page, size, and condition")
        void shouldStoreGivenValues() {
            var q = new UserQuery("alice");
            var req = new PageRequest<>(3L, 50L, q);

            assertEquals(3L, req.page());
            assertEquals(50L, req.size());
            assertSame(q, req.condition());
        }

        @Test
        @DisplayName("null condition should be allowed")
        void nullConditionShouldBeAllowed() {
            var req = new PageRequest<>(1L, 20L, null);
            assertNull(req.condition());
        }

        @Test
        @DisplayName("should have proper equals and hashCode")
        void shouldHaveProperEqualsAndHashCode() {
            var a = new PageRequest<>(3L, 20L, "x");
            var b = new PageRequest<>(3L, 20L, "x");
            assertEquals(a, b);
            assertEquals(a.hashCode(), b.hashCode());
        }

        @Test
        @DisplayName("different conditions should not be equal")
        void differentConditionsShouldNotBeEqual() {
            var a = new PageRequest<>(1L, 20L, "x");
            var b = new PageRequest<>(1L, 20L, "y");
            assertNotEquals(a, b);
        }

        @Test
        @DisplayName("toString should include page, size, and condition")
        void toStringShouldIncludeAllFields() {
            var req = new PageRequest<>(2L, 30L, "x");
            var str = req.toString();
            assertTrue(str.contains("2"), str);
            assertTrue(str.contains("30"), str);
            assertTrue(str.contains("x"), str);
        }
    }

    @Nested
    @DisplayName("Clamping (compact constructor)")
    class Clamping {

        @Test
        @DisplayName("page 0 should clamp to 1")
        void page0ShouldClampTo1() {
            assertEquals(1L, new PageRequest<>(0L, 20L, null).page());
        }

        @Test
        @DisplayName("negative page should clamp to 1")
        void negativePageShouldClampTo1() {
            assertEquals(1L, new PageRequest<>(-5L, 20L, null).page());
        }

        @Test
        @DisplayName("size 0 should clamp to default 10")
        void size0ShouldClampToDefault() {
            assertEquals(10L, new PageRequest<>(1L, 0L, null).size());
        }

        @Test
        @DisplayName("negative size should clamp to default 10")
        void negativeSizeShouldClampToDefault() {
            assertEquals(10L, new PageRequest<>(1L, -1L, null).size());
        }

        @Test
        @DisplayName("clamping should not affect condition")
        void clampingShouldNotAffectCondition() {
            var q = new UserQuery("alice");
            var req = new PageRequest<>(-1L, -1L, q);
            assertEquals(1L, req.page());
            assertEquals(10L, req.size());
            assertSame(q, req.condition());
        }

        @Test
        @DisplayName("valid page and size should be accepted unchanged")
        void validValuesShouldBeAccepted() {
            var req = new PageRequest<>(1L, 1L, null);
            assertEquals(1L, req.page());
            assertEquals(1L, req.size());

            var big = new PageRequest<>(1L, 10_000L, null);
            assertEquals(10_000L, big.size());
        }
    }

    @Nested
    @DisplayName("of(page, size)")
    class OfPageSize {

        @Test
        @DisplayName("should create with condition set to null")
        void shouldCreateWithNullCondition() {
            var req = PageRequest.of(2L, 20L);
            assertEquals(2L, req.page());
            assertEquals(20L, req.size());
            assertNull(req.condition());
        }

        @Test
        @DisplayName("invalid input should still be clamped")
        void invalidInputShouldBeClamped() {
            var req = PageRequest.of(0L, 0L);
            assertEquals(1L, req.page());
            assertEquals(10L, req.size());
        }
    }

    @Nested
    @DisplayName("of(page, size, condition)")
    class OfPageSizeCondition {

        @Test
        @DisplayName("should store all three values")
        void shouldStoreAllValues() {
            var q = new UserQuery("bob");
            var req = PageRequest.of(3L, 15L, q);
            assertEquals(3L, req.page());
            assertEquals(15L, req.size());
            assertSame(q, req.condition());
        }

        @Test
        @DisplayName("null condition should be allowed")
        void nullConditionShouldBeAllowed() {
            var req = PageRequest.of(1L, 10L, null);
            assertNull(req.condition());
        }

        @Test
        @DisplayName("generic type should be inferred from condition")
        void genericTypeShouldBeInferred() {
            // Compile-time check: 返回类型应为 PageRequest<String>
            PageRequest<String> req = PageRequest.of(1L, 10L, "hello");
            assertEquals("hello", req.condition());
        }
    }
}
