package cc.ashclaw.common4j.core.time;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Date/time utilities for common operations including smart parsing, formatting,
 * and day-boundary calculations.
 *
 * <p>Smart parsing methods try multiple common formats in descending order of
 * prevalence, returning on the first successful parse. If all fail, a
 * {@link DateTimeParseException} is thrown with details of every format attempted.
 * <p>All formatter instances are immutable and thread-safe.
 */
public final class DateUtils {

    private DateUtils() {
    }

    private static final ConcurrentHashMap<String, DateTimeFormatter> FORMATTER_CACHE = new ConcurrentHashMap<>();

    private static DateTimeFormatter cachedFormatter(String pattern) {
        return FORMATTER_CACHE.computeIfAbsent(pattern, p -> DateTimeFormatter.ofPattern(p, Locale.ROOT));
    }

    // ==================== Smart-parser formatter lists (ordered by prevalence) ====================

    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateConstants.DATE_SLASH_FORMATTER,
            DateConstants.DATE_COMPACT_FORMATTER,
            DateConstants.DATE_CN_FORMATTER,
            DateConstants.YEAR_MONTH_FORMATTER,
            DateConstants.MONTH_DAY_FORMATTER);

    private static final List<DateTimeFormatter> DATETIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateConstants.DATETIME_FORMATTER,
            DateConstants.DATETIME_MS_FORMATTER,
            DateConstants.DATETIME_SLASH_FORMATTER,
            DateConstants.DATETIME_COMPACT_FORMATTER,
            DateConstants.DATETIME_CN_FORMATTER);

    private static final List<DateTimeFormatter> TIME_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_TIME,
            DateConstants.TIME_FORMATTER,
            DateConstants.TIME_NO_SEC_FORMATTER);

    // ==================== Start / end of today ====================

    /**
     * Returns the start of today ({@code 00:00:00}) in the system default time zone.
     *
     * <p>Prefer {@link #startOfDay(ZoneId)} for explicit zone control.
     */
    public static LocalDateTime startOfDay() {
        return LocalDate.now().atStartOfDay();
    }

    /**
     * Returns the start of today ({@code 00:00:00}) in the given zone.
     *
     * @param zone the time zone, not null
     */
    public static LocalDateTime startOfDay(ZoneId zone) {
        Objects.requireNonNull(zone, "zone must not be null");
        return LocalDate.now(zone).atStartOfDay();
    }

    /**
     * Returns the end of today ({@code 23:59:59.999999999}) in the system default
     * time zone.
     *
     * <p>Prefer {@link #endOfDay(ZoneId)} for explicit zone control.
     * <p>The return value has nanosecond precision. If downstream systems only
     * support milliseconds, use the next day's {@code startOfDay()} as an
     * exclusive upper bound instead.
     */
    public static LocalDateTime endOfDay() {
        return LocalDate.now().atTime(LocalTime.MAX);
    }

    /**
     * Returns the end of today ({@code 23:59:59.999999999}) in the given zone.
     *
     * @param zone the time zone, not null
     */
    public static LocalDateTime endOfDay(ZoneId zone) {
        Objects.requireNonNull(zone, "zone must not be null");
        return LocalDate.now(zone).atTime(LocalTime.MAX);
    }

    // ==================== Start / end of a specific date ====================

    /**
     * Returns the start ({@code 00:00:00}) of the given date.
     *
     * @param date the date, not null
     * @return the date-time at the start of the day
     */
    public static LocalDateTime startOfDay(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.atStartOfDay();
    }

    /**
     * Returns the end ({@code 23:59:59.999999999}) of the given date.
     *
     * @param date the date, not null
     * @return the date-time at the end of the day
     */
    public static LocalDateTime endOfDay(LocalDate date) {
        Objects.requireNonNull(date, "date must not be null");
        return date.atTime(LocalTime.MAX);
    }

    // ==================== Smart parsing ====================

    /**
     * Smart-parses a date string, trying common formats in order.
     *
     * <p>Attempts: yyyy-MM-dd, yyyy/MM/dd, yyyyMMdd, yyyy年MM月dd日, yyyy-MM, MM-dd.
     *
     * @param text the date string to parse
     * @return the parsed date
     * @throws DateTimeParseException if none of the formats match
     */
    public static LocalDate parseDate(CharSequence text) {
        return smartParse(text, DATE_FORMATTERS, LocalDate::parse, "date");
    }

    /**
     * Smart-parses a date-time string, trying common formats in order.
     *
     * <p>Attempts: yyyy-MM-ddTHH:mm:ss, yyyy-MM-dd HH:mm:ss,
     * yyyy-MM-dd HH:mm:ss.SSS, yyyy/MM/dd HH:mm:ss, yyyyMMddHHmmss,
     * yyyy年MM月dd日 HH:mm:ss.
     *
     * @param text the date-time string to parse
     * @return the parsed date-time
     * @throws DateTimeParseException if none of the formats match
     */
    public static LocalDateTime parseDateTime(CharSequence text) {
        return smartParse(text, DATETIME_FORMATTERS, LocalDateTime::parse, "date-time");
    }

    /**
     * Smart-parses a time string, trying common formats in order.
     *
     * <p>Attempts: HH:mm:ss, HH:mm:ss.SSS (ISO format), HH:mm.
     *
     * @param text the time string to parse
     * @return the parsed time
     * @throws DateTimeParseException if none of the formats match
     */
    public static LocalTime parseTime(CharSequence text) {
        return smartParse(text, TIME_FORMATTERS, LocalTime::parse, "time");
    }

    // ==================== Pattern-based parsing ====================

    /**
     * Parses a date string using the given pattern.
     *
     * @param text    the date string, not null
     * @param pattern the format pattern, e.g. {@code "yyyy-MM-dd"}, not null
     * @return the parsed date
     */
    public static LocalDate parseDate(CharSequence text, String pattern) {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");
        return LocalDate.parse(text, cachedFormatter(pattern));
    }

    /**
     * Parses a date-time string using the given pattern.
     *
     * @param text    the date-time string, not null
     * @param pattern the format pattern, e.g. {@code "yyyy-MM-dd HH:mm:ss"}, not null
     * @return the parsed date-time
     */
    public static LocalDateTime parseDateTime(CharSequence text, String pattern) {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");
        return LocalDateTime.parse(text, cachedFormatter(pattern));
    }

    /**
     * Parses a time string using the given pattern.
     *
     * @param text    the time string, not null
     * @param pattern the format pattern, e.g. {@code "HH:mm:ss"}, not null
     * @return the parsed time
     */
    public static LocalTime parseTime(CharSequence text, String pattern) {
        Objects.requireNonNull(text, "text must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");
        return LocalTime.parse(text, cachedFormatter(pattern));
    }

    // ==================== Formatting ====================

    /**
     * Formats a date with the given pattern.
     *
     * @param date    the date, not null
     * @param pattern the format pattern, e.g. {@code "yyyy-MM-dd"}, not null
     * @return the formatted string
     */
    public static String format(LocalDate date, String pattern) {
        Objects.requireNonNull(date, "date must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");
        return date.format(cachedFormatter(pattern));
    }

    /**
     * Formats a date-time with the given pattern.
     *
     * @param dateTime the date-time, not null
     * @param pattern  the format pattern, e.g. {@code "yyyy-MM-dd HH:mm:ss"}, not null
     * @return the formatted string
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        Objects.requireNonNull(dateTime, "dateTime must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");
        return dateTime.format(cachedFormatter(pattern));
    }

    /**
     * Formats a time with the given pattern.
     *
     * @param time    the time, not null
     * @param pattern the format pattern, e.g. {@code "HH:mm:ss"}, not null
     * @return the formatted string
     */
    public static String format(LocalTime time, String pattern) {
        Objects.requireNonNull(time, "time must not be null");
        Objects.requireNonNull(pattern, "pattern must not be null");
        return time.format(cachedFormatter(pattern));
    }

    // ==================== Internal helpers ====================

    @FunctionalInterface
    private interface TemporalParser<T> {
        T parse(CharSequence text, DateTimeFormatter formatter) throws DateTimeParseException;
    }

    /**
     * Tries each formatter in order, returning the first successful parse.
     *
     * @param text       the string to parse
     * @param formatters immutable list of formatters to try
     * @param parser     the parse function
     * @param typeLabel  label for error messages (e.g. "date")
     */
    private static <T> T smartParse(
            CharSequence text,
            List<DateTimeFormatter> formatters,
            TemporalParser<T> parser,
            String typeLabel) {
        Objects.requireNonNull(text);
        String trimmed = text.toString().trim();
        if (trimmed.isEmpty()) {
            throw new DateTimeParseException("Empty text cannot be parsed as " + typeLabel, text, 0);
        }
        for (DateTimeFormatter f : formatters) {
            try {
                return parser.parse(trimmed, f);
            } catch (DateTimeParseException _) {
            }
        }
        String attempted = formatters.stream()
                .map(DateTimeFormatter::toString)
                .collect(java.util.stream.Collectors.joining(", "));
        throw new DateTimeParseException(
                "Text '" + trimmed + "' could not be parsed as " + typeLabel
                        + ", tried: " + attempted,
                trimmed, 0);
    }
}
