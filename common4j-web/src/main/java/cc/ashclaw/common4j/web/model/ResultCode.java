package cc.ashclaw.common4j.web.model;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Standard API result codes — named constants for the {@code code} field in {@link R}.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // Success
 * return ResultCode.SUCCESS.toResponse(user);
 *
 * // Failure with custom message
 * return ResultCode.NOT_FOUND.toResponse("User 42 not found");
 *
 * // Full control
 * return ResultCode.INTERNAL_ERROR.toResponse("db timeout", null);
 * }</pre>
 *
 * <p>Every constant maps to a {@code code} and a {@code defaultMessage}.
 * Use {@link #toResponse()} family to build {@link R} instances without raw ints.</p>
 */
public enum ResultCode {

    SUCCESS(200, "ok"),
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    NOT_FOUND(404, "Not found"),
    METHOD_NOT_ALLOWED(405, "Method not allowed"),
    CONFLICT(409, "Conflict"),
    TOO_MANY_REQUESTS(429, "Too many requests"),
    INTERNAL_ERROR(500, "Internal server error"),
    SERVICE_UNAVAILABLE(503, "Service unavailable");

    private static final Map<Integer, ResultCode> BY_CODE =
            Stream.of(values()).collect(Collectors.toUnmodifiableMap(ResultCode::code, Function.identity()));

    private final int code;
    private final String defaultMessage;

    ResultCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public int code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }

    /**
     * Returns the constant for the given code, or {@code null} if no match.
     */
    public static ResultCode fromCode(int code) {
        return BY_CODE.get(code);
    }

    // ==================== Response builders ====================

    /** Returns a response with the default message and no data. */
    public <T> R<T> toResponse() {
        return new R<>(code, defaultMessage, null);
    }

    /** Returns a response with a custom message and no data. */
    public <T> R<T> toResponse(String message) {
        return new R<>(code, message, null);
    }

    /** Returns a response with the default message and the given data. */
    public <T> R<T> toResponse(T data) {
        return new R<>(code, defaultMessage, data);
    }

    /** Returns a response with a custom message and the given data. */
    public <T> R<T> toResponse(String message, T data) {
        return new R<>(code, message, data);
    }
}
