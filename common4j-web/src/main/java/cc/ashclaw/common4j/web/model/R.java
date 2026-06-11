package cc.ashclaw.common4j.web.model;

import java.util.Objects;
import java.util.function.Function;

/**
 * A generic API response envelope carrying a business code, message, and payload.
 *
 * Usage example:
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

    /**
     * Validates that the message is non-null.
     *
     * @throws NullPointerException if message is null
     */
    public R {
        Objects.requireNonNull(message, "message must not be null");
    }

    // ==================== Static factories ====================

    /**
     * Returns a success response with no payload.
     *
     * @param <T> the payload type
     * @return a success {@link R} with code 0 and no data
     */
    public static <T> R<T> ok() {
        return new R<>(0, "ok", null);
    }

    /**
     * Returns a success response with the given payload.
     *
     * @param <T>  the payload type
     * @param data the response payload
     * @return a success {@link R} with code 0 and the given data
     */
    public static <T> R<T> ok(T data) {
        return new R<>(0, "ok", data);
    }

    /**
     * Returns a success response with a custom message and payload.
     *
     * @param <T>     the payload type
     * @param message the success message
     * @param data    the response payload
     * @return a success {@link R} with code 0, the given message and data
     */
    public static <T> R<T> ok(String message, T data) {
        return new R<>(0, message, data);
    }

    /**
     * Returns a failure response with code 1.
     *
     * @param <T>     the payload type
     * @param message the failure message
     * @return a failure {@link R} with code 1 and the given message
     */
    public static <T> R<T> fail(String message) {
        return new R<>(1, message, null);
    }

    /**
     * Returns a failure response with the given business code.
     *
     * @param <T>     the payload type
     * @param code    the failure code
     * @param message the failure message
     * @return a failure {@link R} with the given code and message
     */
    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }

    // ==================== Query helpers ====================

    /**
     * Returns {@code true} when code is 0 (legacy) or in the HTTP 2xx range.
     *
     * @return whether this response indicates success
     */
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
     *
     * @param <U>    the transformed payload type
     * @param mapper the transformation function
     * @return a new {@link R} with the mapped payload if successful; otherwise this instance cast
     */
    @SuppressWarnings("unchecked")
    public <U> R<U> map(Function<? super T, ? extends U> mapper) {
        if (!isSuccess() || data == null) {
            return (R<U>) this;
        }
        return R.ok(message, mapper.apply(data));
    }
}
