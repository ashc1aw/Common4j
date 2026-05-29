package cc.ashclaw.common4j.web.model;

import java.util.Objects;
import java.util.function.Function;

/**
 * A generic API response envelope carrying a business code, message, and payload.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * // Success with data
 * return R.ok(user);
 *
 * // Success without data
 * return R.ok();
 *
 * // Business failure
 * return R.fail("User not found");
 *
 * // Failure with custom code
 * return R.fail(40401, "User not found");
 *
 * // Transform the data
 * return userService.findById(id).map(User::toDto);
 * }</pre>
 *
 * @param <T> the payload type
 * @param code    business status code — {@code 0} means success
 * @param message human-readable description
 * @param data    the payload, may be {@code null}
 */
public record R<T>(int code, String message, T data) {

    public R {
        Objects.requireNonNull(message, "message must not be null");
    }

    // ==================== Static factories ====================

    /** Returns a success response with no payload. */
    public static <T> R<T> ok() {
        return new R<>(0, "ok", null);
    }

    /** Returns a success response with the given payload. */
    public static <T> R<T> ok(T data) {
        return new R<>(0, "ok", data);
    }

    /** Returns a success response with a custom message and payload. */
    public static <T> R<T> ok(String message, T data) {
        return new R<>(0, message, data);
    }

    /** Returns a failure response with code 1. */
    public static <T> R<T> fail(String message) {
        return new R<>(1, message, null);
    }

    /** Returns a failure response with the given business code. */
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }

    // ==================== Query helpers ====================

    /** {@code true} when code is 0 (legacy) or in the HTTP 2xx range. */
    public boolean isSuccess() {
        return code == 0 || (code >= 200 && code < 300);
    }

    // ==================== Transformation ====================

    /**
     * Transforms the payload if this is a success; returns itself otherwise.
     *
     * <pre>{@code
     * R<User> r = userService.findById(1L);
     * R<UserDto> dto = r.map(User::toDto);
     * }</pre>
     */
    @SuppressWarnings("unchecked")
    public <U> R<U> map(Function<? super T, ? extends U> mapper) {
        if (!isSuccess() || data == null) {
            return (R<U>) this;
        }
        return R.ok(message, mapper.apply(data));
    }
}
