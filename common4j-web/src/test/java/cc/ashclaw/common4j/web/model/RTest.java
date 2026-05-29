package cc.ashclaw.common4j.web.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("R<T> — API response envelope")
class RTest {

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("should throw NPE when message is null")
        void shouldThrowNpeWhenMessageIsNull() {
            assertThrows(NullPointerException.class,
                    () -> new R<>(0, null, "data"));
        }

        @Test
        @DisplayName("should allow null data")
        void shouldAllowNullData() {
            R<Object> r = new R<>(0, "ok", null);
            assertNotNull(r);
            assertNull(r.data());
        }

        @Test
        @DisplayName("should store code, message, and data")
        void shouldStoreCodeMessageAndData() {
            var r = new R<>(200, "ok", "payload");
            assertEquals(200, r.code());
            assertEquals("ok", r.message());
            assertEquals("payload", r.data());
        }
    }

    @Nested
    @DisplayName("Static factories — ok()")
    class OkFactories {

        @Test
        @DisplayName("ok() should return code 0, message 'ok', null data")
        void shouldReturnSuccessNoData() {
            R<Void> r = R.ok();
            assertEquals(0, r.code());
            assertEquals("ok", r.message());
            assertNull(r.data());
        }

        @Test
        @DisplayName("ok(T) should return code 0 with data")
        void shouldReturnSuccessWithData() {
            R<String> r = R.ok("hello");
            assertEquals(0, r.code());
            assertEquals("ok", r.message());
            assertEquals("hello", r.data());
        }

        @Test
        @DisplayName("ok(String, T) should return code 0 with custom message and data")
        void shouldReturnSuccessWithCustomMessageAndData() {
            R<Integer> r = R.ok("created", 42);
            assertEquals(0, r.code());
            assertEquals("created", r.message());
            assertEquals(42, r.data());
        }

        @Test
        @DisplayName("ok(String, null) should return code 0 with custom message and null data")
        void shouldReturnSuccessWithCustomMessageAndNullData() {
            R<Void> r = R.ok("done", null);
            assertEquals(0, r.code());
            assertEquals("done", r.message());
            assertNull(r.data());
        }
    }

    @Nested
    @DisplayName("Static factories — fail()")
    class FailFactories {

        @Test
        @DisplayName("fail(String) should return code 1 with message and null data")
        void shouldReturnFailWithMessage() {
            R<Void> r = R.fail("something went wrong");
            assertEquals(1, r.code());
            assertEquals("something went wrong", r.message());
            assertNull(r.data());
        }

        @Test
        @DisplayName("fail(int, String) should return custom code with message")
        void shouldReturnFailWithCustomCode() {
            R<Void> r = R.fail(40401, "User not found");
            assertEquals(40401, r.code());
            assertEquals("User not found", r.message());
            assertNull(r.data());
        }
    }

    @Nested
    @DisplayName("isSuccess()")
    class IsSuccess {

        @Test
        @DisplayName("code 0 should be success (legacy)")
        void code0ShouldBeSuccess() {
            assertTrue(R.ok().isSuccess());
        }

        @Test
        @DisplayName("code 200 should be success (HTTP OK)")
        void code200ShouldBeSuccess() {
            assertTrue(new R<>(200, "ok", null).isSuccess());
        }

        @Test
        @DisplayName("code 201 should be success (HTTP Created)")
        void code201ShouldBeSuccess() {
            assertTrue(new R<>(201, "created", null).isSuccess());
        }

        @Test
        @DisplayName("code 299 should be success (upper bound of 2xx)")
        void code299ShouldBeSuccess() {
            assertTrue(new R<>(299, "ok", null).isSuccess());
        }

        @Test
        @DisplayName("code 1 should not be success (legacy fail)")
        void code1ShouldNotBeSuccess() {
            assertFalse(R.fail("err").isSuccess());
        }

        @Test
        @DisplayName("code 300 should not be success (redirect)")
        void code300ShouldNotBeSuccess() {
            assertFalse(new R<>(300, "redirect", null).isSuccess());
        }

        @Test
        @DisplayName("code 400 should not be success")
        void code400ShouldNotBeSuccess() {
            assertFalse(new R<>(400, "bad request", null).isSuccess());
        }

        @Test
        @DisplayName("code 500 should not be success")
        void code500ShouldNotBeSuccess() {
            assertFalse(new R<>(500, "error", null).isSuccess());
        }
    }

    @Nested
    @DisplayName("map()")
    class Map {

        @Test
        @DisplayName("should transform data on success")
        void shouldTransformDataOnSuccess() {
            R<String> r = R.ok("hello");
            R<Integer> mapped = r.map(String::length);
            assertTrue(mapped.isSuccess());
            assertEquals(5, mapped.data());
        }

        @Test
        @DisplayName("should pass through unchanged on failure")
        void shouldPassThroughOnFailure() {
            R<String> r = R.fail("error");
            R<Integer> mapped = r.map(String::length);
            assertFalse(mapped.isSuccess());
            assertEquals("error", mapped.message());
        }

        @Test
        @DisplayName("should pass through unchanged when data is null on success")
        void shouldPassThroughWhenDataIsNull() {
            R<String> r = R.ok();
            R<Integer> mapped = r.map(String::length);
            assertTrue(mapped.isSuccess());
            assertNull(mapped.data());
        }

        @Test
        @DisplayName("should handle type transformation")
        void shouldHandleTypeTransformation() {
            record Person(String name) {}
            R<Person> r = R.ok(new Person("Alice"));
            R<String> mapped = r.map(Person::name);
            assertEquals("Alice", mapped.data());
        }
    }
}
