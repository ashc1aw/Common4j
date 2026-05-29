package cc.ashclaw.common4j.core.time;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Date/time constants — commonly used formatters and time zones for project-wide
 * consistency.
 *
 * <p>In addition to the built-in {@link DateTimeFormatter} constants (e.g.
 * {@code ISO_LOCAL_DATE}), this class provides space-separated date-times,
 * slash-separated, compact, and Chinese-locale formats.
 */
public final class DateConstants {

    private DateConstants() {
    }

    // ==================== Date formatters ====================

    /** yyyy-MM-dd — the most common date format, same as {@link DateTimeFormatter#ISO_LOCAL_DATE}. */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    /** yyyy-MM-dd HH:mm:ss — space-separated date-time, typical in databases and APIs. */
    public static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT);

    /** yyyy-MM-dd HH:mm:ss.SSS — date-time with milliseconds. */
    public static final DateTimeFormatter DATETIME_MS_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS", Locale.ROOT);

    /** yyyy/MM/dd — slash-separated date. */
    public static final DateTimeFormatter DATE_SLASH_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ROOT);

    /** yyyy/MM/dd HH:mm:ss — slash-separated date-time. */
    public static final DateTimeFormatter DATETIME_SLASH_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.ROOT);

    /** yyyyMMdd — compact date without separators. */
    public static final DateTimeFormatter DATE_COMPACT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMdd", Locale.ROOT);

    /** yyyyMMddHHmmss — compact date-time without separators. */
    public static final DateTimeFormatter DATETIME_COMPACT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmss", Locale.ROOT);

    /** yyyy年MM月dd日 — Chinese-locale date. */
    public static final DateTimeFormatter DATE_CN_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.ROOT);

    /** yyyy年MM月dd日 HH:mm:ss — Chinese-locale date-time. */
    public static final DateTimeFormatter DATETIME_CN_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss", Locale.ROOT);

    // ==================== Time formatters ====================

    /** HH:mm:ss — time with seconds. */
    public static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss", Locale.ROOT);

    /** HH:mm — time without seconds. */
    public static final DateTimeFormatter TIME_NO_SEC_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm", Locale.ROOT);

    // ==================== Partial date formatters ====================

    /** yyyy-MM — year-month. */
    public static final DateTimeFormatter YEAR_MONTH_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM", Locale.ROOT);

    /** MM-dd — month-day. */
    public static final DateTimeFormatter MONTH_DAY_FORMATTER =
            DateTimeFormatter.ofPattern("MM-dd", Locale.ROOT);

    // ==================== Common time zones ====================

    /** China Standard Time (Asia/Shanghai, UTC+8). */
    public static final ZoneId ZONE_SHANGHAI = ZoneId.of("Asia/Shanghai");

    /** Japan Standard Time (Asia/Tokyo, UTC+9). */
    public static final ZoneId ZONE_TOKYO = ZoneId.of("Asia/Tokyo");

    /** US Eastern Time (America/New_York). */
    public static final ZoneId ZONE_NEW_YORK = ZoneId.of("America/New_York");

    /** US Pacific Time (America/Los_Angeles). */
    public static final ZoneId ZONE_LOS_ANGELES = ZoneId.of("America/Los_Angeles");

    /** UK Time (Europe/London). */
    public static final ZoneId ZONE_LONDON = ZoneId.of("Europe/London");

    /** Central European Time (Europe/Paris). */
    public static final ZoneId ZONE_PARIS = ZoneId.of("Europe/Paris");
}
