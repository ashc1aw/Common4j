package cc.ashclaw.common4j.core.convert;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Objects;

/**
 * String-to-type coercion for the types most common in configuration parsing,
 * file import, and annotation-driven mapping.
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * int port = ConvertUtils.to("8080", int.class);
 * LocalDate d = ConvertUtils.to("2024/06/15", LocalDate.class, "yyyy/MM/dd");
 * BigDecimal bd = ConvertUtils.to("1,234.56", BigDecimal.class, "#,##0.00");
 * }</pre>
 *
 * <p>For blank or empty input, {@link #defaultValue(Class)} determines the result:
 * {@code null} for reference types, 0 for numeric primitives, false for boolean.
 *
 * <p>This is a low-level utility. The typed {@code to()} methods are preferred
 * for application code; {@code coerce()} exists for framework-style dispatch
 * where the target type is known only at runtime.
 */
public final class ConvertUtils {

    private ConvertUtils() {
        throw new UnsupportedOperationException("utility class");
    }

    // ── Typed convenience API ────────────────────────────────────────

    /** Coerces a string to the given type with sensible defaults. */
    @SuppressWarnings("unchecked")
    public static <T> T to(String value, Class<T> targetType) {
        return (T) coerce(value, targetType, "");
    }

    /** Coerces a string to the given type using a format pattern for numbers/dates. */
    @SuppressWarnings("unchecked")
    public static <T> T to(String value, Class<T> targetType, String format) {
        return (T) coerce(value, targetType, format);
    }

    // ── Core coercion ─────────────────────────────────────────────────

    /**
     * Coerces a raw string to the target type, optionally using a format pattern
     * (e.g. {@code "#,##0.00"} for numbers, {@code "yyyy/MM/dd"} for dates).
     *
     * @param value      the string to convert, may be null
     * @param targetType the target type
     * @param format     optional format pattern, empty string means no format
     * @return the converted value, or the type's default if the string is blank
     * @throws IllegalArgumentException if the value cannot be parsed
     */
    public static Object coerce(String value, Class<?> targetType, String format) {
        if (targetType == String.class) return value;
        var t = value == null ? "" : value.trim();
        if (t.isEmpty()) return defaultValue(targetType);
        try {
            if (targetType == int.class || targetType == Integer.class)
                return format == null || format.isEmpty()
                        ? Integer.parseInt(t)
                        : parseNumber(t, format).intValue();
            if (targetType == long.class || targetType == Long.class)
                return format == null || format.isEmpty()
                        ? Long.parseLong(t)
                        : parseNumber(t, format).longValue();
            if (targetType == double.class || targetType == Double.class)
                return format == null || format.isEmpty()
                        ? Double.parseDouble(t)
                        : parseNumber(t, format).doubleValue();
            if (targetType == BigDecimal.class)
                return format == null || format.isEmpty()
                        ? new BigDecimal(t)
                        : parseBigDecimal(t, format);
            if (targetType == boolean.class || targetType == Boolean.class)
                return "true".equalsIgnoreCase(t) || "1".equals(t) || "yes".equalsIgnoreCase(t);
            if (targetType == LocalDate.class)
                return format == null || format.isEmpty()
                        ? LocalDate.parse(t)
                        : LocalDate.parse(t, DateTimeFormatter.ofPattern(format));
            if (targetType == LocalDateTime.class) {
                if (format == null || format.isEmpty()) {
                    var normalized = t.replace(" ", "T");
                    return LocalDateTime.parse(normalized);
                }
                return LocalDateTime.parse(t, DateTimeFormatter.ofPattern(format));
            }
            if (targetType == Date.class) {
                LocalDateTime ldt;
                if (format == null || format.isEmpty()) {
                    ldt = LocalDateTime.parse(t.replace(" ", "T"));
                } else {
                    ldt = LocalDateTime.parse(t, DateTimeFormatter.ofPattern(format));
                }
                return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "cannot convert \"%s\" to %s".formatted(t, targetType.getSimpleName()), e);
        }
        return value;
    }

    /**
     * Returns the natural "zero" value for a type: {@code null} for reference types,
     * 0 for numeric primitives, false for boolean.
     */
    public static Object defaultValue(Class<?> type) {
        Objects.requireNonNull(type, "type must not be null");
        return type.isPrimitive() ? (type == boolean.class ? false : 0) : null;
    }

    // ── Number parsing helpers ────────────────────────────────────────

    private static Number parseNumber(String text, String format) throws ParseException {
        var pos = new ParsePosition(0);
        var df = new DecimalFormat(format);
        Number result = df.parse(text, pos);
        if (pos.getIndex() != text.length()) {
            throw new ParseException(
                    "unparsed text: \"" + text.substring(pos.getIndex()) + "\"", pos.getIndex());
        }
        return result;
    }

    private static BigDecimal parseBigDecimal(String text, String format) throws ParseException {
        var pos = new ParsePosition(0);
        var df = new DecimalFormat(format);
        df.setParseBigDecimal(true);
        Number result = df.parse(text, pos);
        if (pos.getIndex() != text.length()) {
            throw new ParseException(
                    "unparsed text: \"" + text.substring(pos.getIndex()) + "\"", pos.getIndex());
        }
        return (BigDecimal) result;
    }
}
