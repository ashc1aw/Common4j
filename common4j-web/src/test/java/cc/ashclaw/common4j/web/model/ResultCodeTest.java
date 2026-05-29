package cc.ashclaw.common4j.web.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ResultCode")
class ResultCodeTest {

    @Nested
    @DisplayName("Constants")
    class Constants {

        @Test
        @DisplayName("every constant should have a distinct code")
        void everyConstantShouldHaveDistinctCode() {
            var codes = new HashSet<Integer>();
            for (var rc : ResultCode.values()) {
                assertTrue(codes.add(rc.code()),
                        "duplicate code " + rc.code() + " for " + rc.name());
            }
        }

        @Test
        @DisplayName("every constant should have a non-blank default message")
        void everyConstantShouldHaveNonBlankDefaultMessage() {
            for (var rc : ResultCode.values()) {
                assertNotNull(rc.defaultMessage());
                assertFalse(rc.defaultMessage().isBlank());
            }
        }

        @Test
        @DisplayName("SUCCESS should be 200")
        void successShouldBe200() {
            assertEquals(200, ResultCode.SUCCESS.code());
        }

        @Test
        @DisplayName("error codes should match HTTP status codes")
        void errorCodesShouldMatchHttp() {
            assertEquals(400, ResultCode.BAD_REQUEST.code());
            assertEquals(401, ResultCode.UNAUTHORIZED.code());
            assertEquals(403, ResultCode.FORBIDDEN.code());
            assertEquals(404, ResultCode.NOT_FOUND.code());
            assertEquals(405, ResultCode.METHOD_NOT_ALLOWED.code());
            assertEquals(409, ResultCode.CONFLICT.code());
            assertEquals(429, ResultCode.TOO_MANY_REQUESTS.code());
            assertEquals(500, ResultCode.INTERNAL_ERROR.code());
            assertEquals(503, ResultCode.SERVICE_UNAVAILABLE.code());
        }
    }

    @Nested
    @DisplayName("fromCode()")
    class FromCode {

        @Test
        @DisplayName("should resolve all known codes")
        void shouldResolveAllKnownCodes() {
            for (var rc : ResultCode.values()) {
                assertEquals(rc, ResultCode.fromCode(rc.code()),
                        "fromCode(" + rc.code() + ") should return " + rc.name());
            }
        }

        @Test
        @DisplayName("should return null for unknown code")
        void shouldReturnNullForUnknownCode() {
            assertNull(ResultCode.fromCode(999));
            assertNull(ResultCode.fromCode(-1));
        }
    }

    @Nested
    @DisplayName("toResponse()")
    class ToResponse {

        @Test
        @DisplayName("toResponse() should use default message and null data")
        void toResponseNoArgs() {
            R<Void> r = ResultCode.NOT_FOUND.toResponse();
            assertEquals(404, r.code());
            assertEquals("Not found", r.message());
            assertNull(r.data());
        }

        @Test
        @DisplayName("toResponse(String) should use custom message")
        void toResponseWithMessage() {
            R<Void> r = ResultCode.BAD_REQUEST.toResponse("email is required");
            assertEquals(400, r.code());
            assertEquals("email is required", r.message());
            assertNull(r.data());
        }

        @Test
        @DisplayName("toResponse(T) should use default message with data")
        void toResponseWithData() {
            R<Integer> r = ResultCode.SUCCESS.toResponse(42);
            assertEquals(200, r.code());
            assertEquals("ok", r.message());
            assertEquals(42, r.data());
        }

        @Test
        @DisplayName("toResponse(String, T) should use custom message with data")
        void toResponseWithMessageAndData() {
            R<Integer> r = ResultCode.SUCCESS.toResponse("created", 42);
            assertEquals(200, r.code());
            assertEquals("created", r.message());
            assertEquals(42, r.data());
        }
    }
}
