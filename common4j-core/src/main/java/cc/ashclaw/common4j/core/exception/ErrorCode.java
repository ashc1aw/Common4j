package cc.ashclaw.common4j.core.exception;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Error codes for core-layer exceptions — categorizes failures with a numeric code and human-readable default message.
 *
 * <h3>Usage</h3>
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

    UNKNOWN(-1, "Unknown error"),
    INVALID_ARGUMENT(1001, "Invalid argument"),
    ILLEGAL_STATE(1002, "Illegal state"),
    OUT_OF_RANGE(1003, "Value out of range"),
    UNSUPPORTED(1004, "Unsupported operation"),

    // ==================== Data (2xxx) ====================

    NOT_FOUND(2001, "Resource not found"),
    DUPLICATE(2002, "Duplicate resource"),
    DATA_CONFLICT(2003, "Data conflict"),
    DATA_INTEGRITY(2004, "Data integrity violation"),

    // ==================== I/O (3xxx) ====================

    IO_ERROR(3001, "I/O error"),
    FILE_NOT_FOUND(3002, "File not found"),
    STREAM_ERROR(3003, "Stream error"),

    // ==================== Concurrency & time (4xxx) ====================

    CONCURRENCY_ERROR(4001, "Concurrency error"),
    TIMEOUT(4002, "Operation timed out"),
    INTERRUPTED(4003, "Operation interrupted"),

    // ==================== Configuration (5xxx) ====================

    CONFIG_ERROR(5001, "Configuration error"),
    MISSING_CONFIG(5002, "Required configuration is missing");

    private static final Map<Integer, ErrorCode> BY_CODE =
            Stream.of(values()).collect(Collectors.toUnmodifiableMap(ErrorCode::code, Function.identity()));

    private final int code;
    private final String defaultMessage;

    ErrorCode(int code, String defaultMessage) {
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
    public static ErrorCode fromCode(int code) {
        return BY_CODE.get(code);
    }

    @Override
    public String toString() {
        return name() + "(" + code + ")";
    }
}
