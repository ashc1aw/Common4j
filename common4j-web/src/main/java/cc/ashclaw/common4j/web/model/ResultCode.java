package cc.ashclaw.common4j.web.model;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Standard API result codes — named constants for the {@code code} field in {@link R}.
 *
 * Usage example:
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

    /** Successful operation (HTTP 200). */
    SUCCESS(200, "ok"),
    /** Invalid request syntax (HTTP 400). */
    BAD_REQUEST(400, "Bad request"),
    /** Authentication required (HTTP 401). */
    UNAUTHORIZED(401, "Unauthorized"),
    /** Insufficient permissions (HTTP 403). */
    FORBIDDEN(403, "Forbidden"),
    /** Resource not found (HTTP 404). */
    NOT_FOUND(404, "Not found"),
    /** HTTP method not allowed for this resource (HTTP 405). */
    METHOD_NOT_ALLOWED(405, "Method not allowed"),
    /** Resource state conflict (HTTP 409). */
    CONFLICT(409, "Conflict"),
    /** Rate limit exceeded (HTTP 429). */
    TOO_MANY_REQUESTS(429, "Too many requests"),
    /** Unexpected server error (HTTP 500). */
    INTERNAL_ERROR(500, "Internal server error"),
    /** Service temporarily unavailable (HTTP 503). */
    SERVICE_UNAVAILABLE(503, "Service unavailable");

    private static final Map<Integer, ResultCode> BY_CODE =
            Stream.of(values()).collect(Collectors.toUnmodifiableMap(ResultCode::code, Function.identity()));

    private final int code;
    private final String defaultMessage;

    ResultCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    /** Returns the numeric HTTP-style result code.
     * @return the numeric code */
    public int code() {
        return code;
    }

    /** Returns the default human-readable message for this result code.
     * @return the default message */
    public String defaultMessage() {
        return defaultMessage;
    }

    /**
     * Returns the constant for the given code, or {@code null} if no match.
     *
     * @param code the numeric result code
     * @return the matching {@link ResultCode}, or null
     */
    public static ResultCode fromCode(int code) {
        return BY_CODE.get(code);
    }

    // ==================== Response builders ====================

    /**
     * Returns a response with the default message and no data.
     *
     * @param <T> the payload type
     * @return an {@link R} response with this code's default message
     */
    public <T> R<T> toResponse() {
        return new R<>(code, defaultMessage, null);
    }

    /**
     * Returns a response with a custom message and no data.
     *
     * @param <T>     the payload type
     * @param message the custom message
     * @return an {@link R} response with this code and the given message
     */
    public <T> R<T> toResponse(String message) {
        return new R<>(code, message, null);
    }

    /**
     * Returns a response with the default message and the given data.
     *
     * @param <T>  the payload type
     * @param data the response payload
     * @return an {@link R} response with this code's default message and the given data
     */
    public <T> R<T> toResponse(T data) {
        return new R<>(code, defaultMessage, data);
    }

    /**
     * Returns a response with a custom message and the given data.
     *
     * @param <T>     the payload type
     * @param message the custom message
     * @param data    the response payload
     * @return an {@link R} response with this code, message, and data
     */
    public <T> R<T> toResponse(String message, T data) {
        return new R<>(code, message, data);
    }
}
