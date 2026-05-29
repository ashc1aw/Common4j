# time

Date/time utilities built on `java.time` — smart parsing, formatting, day boundaries, ranges, and shared constants.

## Classes

### `DateUtils`

General-purpose date/time operations. All methods reject null arguments with `NullPointerException`.

**Day boundaries**

| Method | Description |
|--------|-------------|
| `startOfDay()` | Start of today (`00:00:00`), system default zone |
| `startOfDay(ZoneId)` | Start of today in the given zone |
| `endOfDay()` | End of today (`23:59:59.999999999`), system default zone |
| `endOfDay(ZoneId)` | End of today in the given zone |
| `startOfDay(LocalDate)` | Start of a specific date |
| `endOfDay(LocalDate)` | End of a specific date |

**Smart parsing** — tries multiple common formats in order, returns on first success, throws `DateTimeParseException` with every attempted format on failure.

| Method | Formats attempted |
|--------|-------------------|
| `parseDate(text)` | `yyyy-MM-dd`, `yyyy/MM/dd`, `yyyyMMdd`, `yyyy年MM月dd日`, `yyyy-MM`, `MM-dd` |
| `parseDateTime(text)` | `yyyy-MM-ddTHH:mm:ss`, `yyyy-MM-dd HH:mm:ss`, `yyyy-MM-dd HH:mm:ss.SSS`, `yyyy/MM/dd HH:mm:ss`, `yyyyMMddHHmmss`, `yyyy年MM月dd日 HH:mm:ss` |
| `parseTime(text)` | `HH:mm:ss`, `HH:mm:ss.SSS`, `HH:mm` |

**Pattern-based parsing & formatting** — accept a custom pattern string. Formatters are cached internally via `ConcurrentHashMap` so repeated calls with the same pattern reuse the same `DateTimeFormatter` instance.

| Method | Returns |
|--------|---------|
| `parseDate(text, pattern)` | `LocalDate` |
| `parseDateTime(text, pattern)` | `LocalDateTime` |
| `parseTime(text, pattern)` | `LocalTime` |
| `format(date, pattern)` | `String` |
| `format(dateTime, pattern)` | `String` |
| `format(time, pattern)` | `String` |

### `DateConstants`

Immutable, thread-safe `DateTimeFormatter` and `ZoneId` constants for project-wide consistency.

**Date formatters:** `DATE_FORMATTER`, `DATE_SLASH_FORMATTER`, `DATE_COMPACT_FORMATTER`, `DATE_CN_FORMATTER`

**Date-time formatters:** `DATETIME_FORMATTER`, `DATETIME_MS_FORMATTER`, `DATETIME_SLASH_FORMATTER`, `DATETIME_COMPACT_FORMATTER`, `DATETIME_CN_FORMATTER`

**Time formatters:** `TIME_FORMATTER`, `TIME_NO_SEC_FORMATTER`

**Partial date formatters:** `YEAR_MONTH_FORMATTER`, `MONTH_DAY_FORMATTER`

**Time zones:** `ZONE_SHANGHAI`, `ZONE_TOKYO`, `ZONE_NEW_YORK`, `ZONE_LOS_ANGELES`, `ZONE_LONDON`, `ZONE_PARIS`

### `DateRange<T>`

A generic `[start, end]` range record for `Temporal & Comparable` types (`LocalDate`, `LocalDateTime`, `LocalTime`, `Instant`, etc.).

```java
var range = new DateRange<>(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
range.contains(LocalDate.of(2025, 6, 15)); // true
```

- Default constructor creates a **closed** interval `[start, end]`.
- Canonical constructor accepts `startInclusive` and `endInclusive` for open/half-open bounds.
- `toString()` renders as `[2025-01-01, 2025-12-31]` (brackets for closed, parentheses for open).

## Design notes

- All formatters are immutable and thread-safe.
- `DateUtils` caches dynamically-created formatters keyed by pattern string, using `Locale.ROOT` for locale-neutral output.
- `DateRange` validates that `start <= end` at construction time.
