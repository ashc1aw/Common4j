package cc.ashclaw.common4j.core.exception;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Error codes for core-layer exceptions — categorizes failures with a numeric code and human-readable default message.
 *
 * <b>Usage</b>
 * <pre>{@code
 * throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "email must not be blank");
 * throw new BusinessException(ErrorCode.NOT_FOUND);
 *
 * // Resolve from external input
 * ErrorCode ec = ErrorCode.fromCode(1001);
 * }</pre>
 *
 * <p>Code ranges are deliberately spaced to allow future insertion without renumbering:
 * general 1xxx, data 2xxx, I/O 3xxx, concurrency/timeout 4xxx, configuration 5xxx.</p>
 */
public enum ErrorCode {

    // ==================== General (1xxx) ====================

    /** Unknown or unclassified error. */
    UNKNOWN(-1, "Unknown error"),
    /** Invalid input argument. */
    INVALID_ARGUMENT(1001, "Invalid argument"),
    /** Object is in an illegal state for the requested operation. */
    ILLEGAL_STATE(1002, "Illegal state"),
    /** Numeric value is outside the acceptable range. */
    OUT_OF_RANGE(1003, "Value out of range"),
    /** Requested operation is not supported. */
    UNSUPPORTED(1004, "Unsupported operation"),

    // ==================== Data (2xxx) ====================

    /** Requested resource was not found. */
    NOT_FOUND(2001, "Resource not found"),
    /** Resource conflicts with an existing entry. */
    DUPLICATE(2002, "Duplicate resource"),
    /** Concurrent modification conflict. */
    DATA_CONFLICT(2003, "Data conflict"),
    /** Referential integrity violation. */
    DATA_INTEGRITY(2004, "Data integrity violation"),

    // ==================== I/O (3xxx) ====================

    /** General I/O failure. */
    IO_ERROR(3001, "I/O error"),
    /** Requested file does not exist. */
    FILE_NOT_FOUND(3002, "File not found"),
    /** Stream read/write failure. */
    STREAM_ERROR(3003, "Stream error"),

    // ==================== Concurrency & time (4xxx) ====================

    /** Concurrent access conflict. */
    CONCURRENCY_ERROR(4001, "Concurrency error"),
    /** Operation exceeded the time limit. */
    TIMEOUT(4002, "Operation timed out"),
    /** Operation was interrupted. */
    INTERRUPTED(4003, "Operation interrupted"),

    // ==================== Configuration (5xxx) ====================

    /** Invalid or malformed configuration. */
    CONFIG_ERROR(5001, "Configuration error"),
    /** Required configuration value is missing. */
    MISSING_CONFIG(5002, "Required configuration is missing");

    private static final Map<Integer, ErrorCode> BY_CODE =
            Stream.of(values()).collect(Collectors.toUnmodifiableMap(ErrorCode::code, Function.identity()));

    private final int code;
    private final String defaultMessage;

    ErrorCode(int code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    /** Returns the numeric error code.
     * @return the numeric code for this error */
    public int code() {
        return code;
    }

    /** Returns the default human-readable message for this error code.
     * @return the default message */
    public String defaultMessage() {
        return defaultMessage;
    }

    /**
     * Returns the constant for the given code, or {@code null} if no match.
     *
     * @param code the numeric error code
     * @return the matching {@link ErrorCode}, or null
     */
    public static ErrorCode fromCode(int code) {
        return BY_CODE.get(code);
    }

    @Override
    public String toString() {
        return name() + "(" + code + ")";
    }
}
