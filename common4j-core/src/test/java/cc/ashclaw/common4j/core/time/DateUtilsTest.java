package cc.ashclaw.common4j.core.time;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DateUtils")
class DateUtilsTest {

    @Nested
    @DisplayName("Day boundaries — system default zone")
    class SystemDefaultDayBoundaries {

        @Test
        @DisplayName("startOfDay should return 00:00:00 today")
        void startOfDayToday() {
            LocalDateTime start = DateUtils.startOfDay();
            assertEquals(LocalDate.now(), start.toLocalDate());
            assertEquals(LocalTime.MIN, start.toLocalTime());
        }

        @Test
        @DisplayName("endOfDay should return 23:59:59.999999999 today")
        void endOfDayToday() {
            LocalDateTime end = DateUtils.endOfDay();
            assertEquals(LocalDate.now(), end.toLocalDate());
            assertEquals(LocalTime.MAX, end.toLocalTime());
        }
    }

    @Nested
    @DisplayName("Day boundaries — explicit zone")
    class ZonedDayBoundaries {

        @Test
        @DisplayName("startOfDay(ZoneId) should reject null")
        void rejectNullZone() {
            assertThrows(NullPointerException.class, () -> DateUtils.startOfDay((ZoneId) null));
            assertThrows(NullPointerException.class, () -> DateUtils.endOfDay((ZoneId) null));
        }

        @Test
        @DisplayName("startOfDay(ZoneId) should return midnight in UTC")
        void utcStartOfDay() {
            ZoneId utc = ZoneOffset.UTC;
            LocalDateTime start = DateUtils.startOfDay(utc);
            assertEquals(LocalDate.now(utc), start.toLocalDate());
            assertEquals(LocalTime.MIN, start.toLocalTime());
        }

        @Test
        @DisplayName("endOfDay(ZoneId) should return max time in UTC")
        void utcEndOfDay() {
            LocalDateTime end = DateUtils.endOfDay(ZoneOffset.UTC);
            assertEquals(LocalTime.MAX, end.toLocalTime());
        }
    }

    @Nested
    @DisplayName("Day boundaries — specific date")
    class SpecificDateBoundaries {

        private final LocalDate date = LocalDate.of(2025, 3, 15);

        @Test
        @DisplayName("startOfDay(LocalDate) should return midnight of that date")
        void startOfSpecificDate() {
            LocalDateTime start = DateUtils.startOfDay(date);
            assertEquals(date, start.toLocalDate());
            assertEquals(LocalTime.MIN, start.toLocalTime());
        }

        @Test
        @DisplayName("endOfDay(LocalDate) should return max time of that date")
        void endOfSpecificDate() {
            LocalDateTime end = DateUtils.endOfDay(date);
            assertEquals(date, end.toLocalDate());
            assertEquals(LocalTime.MAX, end.toLocalTime());
        }

        @Test
        @DisplayName("should reject null date")
        void rejectNullDate() {
            assertThrows(NullPointerException.class, () -> DateUtils.startOfDay((LocalDate) null));
            assertThrows(NullPointerException.class, () -> DateUtils.endOfDay((LocalDate) null));
        }
    }

    @Nested
    @DisplayName("Smart date parsing")
    class SmartDateParsing {

        @Test
        @DisplayName("should parse ISO date")
        void isoDate() {
            assertEquals(LocalDate.of(2025, 3, 15), DateUtils.parseDate("2025-03-15"));
        }

        @Test
        @DisplayName("should parse slash-separated date")
        void slashDate() {
            assertEquals(LocalDate.of(2025, 3, 15), DateUtils.parseDate("2025/03/15"));
        }

        @Test
        @DisplayName("should parse compact date")
        void compactDate() {
            assertEquals(LocalDate.of(2025, 3, 15), DateUtils.parseDate("20250315"));
        }

        @Test
        @DisplayName("should parse Chinese-locale date")
        void chineseDate() {
            assertEquals(LocalDate.of(2025, 3, 15), DateUtils.parseDate("2025年03月15日"));
        }

        @Test
        @DisplayName("should parse year-month")
        void yearMonth() {
            assertEquals(LocalDate.of(2025, 3, 1), DateUtils.parseDate("2025-03"));
        }

        @Test
        @DisplayName("should parse month-day with default year")
        void monthDay() {
            LocalDate result = DateUtils.parseDate("03-15");
            assertEquals(3, result.getMonthValue());
            assertEquals(15, result.getDayOfMonth());
        }

        @Test
        @DisplayName("should reject empty string")
        void rejectEmpty() {
            assertThrows(DateTimeParseException.class, () -> DateUtils.parseDate(""));
        }

        @Test
        @DisplayName("should reject unparseable string")
        void rejectUnparseable() {
            DateTimeParseException ex = assertThrows(DateTimeParseException.class,
                    () -> DateUtils.parseDate("not-a-date"));
            assertTrue(ex.getMessage().contains("tried:"));
        }

        @Test
        @DisplayName("should trim input")
        void trimInput() {
            assertEquals(LocalDate.of(2025, 3, 15), DateUtils.parseDate(" 2025-03-15 "));
        }
    }

    @Nested
    @DisplayName("Smart date-time parsing")
    class SmartDateTimeParsing {

        @Test
        @DisplayName("should parse ISO date-time")
        void isoDateTime() {
            LocalDateTime dt = DateUtils.parseDateTime("2025-03-15T14:30:00");
            assertEquals(LocalDateTime.of(2025, 3, 15, 14, 30, 0), dt);
        }

        @Test
        @DisplayName("should parse space-separated date-time")
        void spaceDateTime() {
            LocalDateTime dt = DateUtils.parseDateTime("2025-03-15 14:30:00");
            assertEquals(LocalDateTime.of(2025, 3, 15, 14, 30, 0), dt);
        }

        @Test
        @DisplayName("should parse date-time with milliseconds")
        void msDateTime() {
            LocalDateTime dt = DateUtils.parseDateTime("2025-03-15 14:30:00.500");
            assertEquals(LocalDateTime.of(2025, 3, 15, 14, 30, 0, 500_000_000), dt);
        }

        @Test
        @DisplayName("should parse slash date-time")
        void slashDateTime() {
            LocalDateTime dt = DateUtils.parseDateTime("2025/03/15 14:30:00");
            assertEquals(LocalDateTime.of(2025, 3, 15, 14, 30, 0), dt);
        }

        @Test
        @DisplayName("should parse compact date-time")
        void compactDateTime() {
            LocalDateTime dt = DateUtils.parseDateTime("20250315143000");
            assertEquals(LocalDateTime.of(2025, 3, 15, 14, 30, 0), dt);
        }

        @Test
        @DisplayName("should reject unparseable date-time")
        void rejectUnparseable() {
            assertThrows(DateTimeParseException.class,
                    () -> DateUtils.parseDateTime("bad date-time"));
        }
    }

    @Nested
    @DisplayName("Smart time parsing")
    class SmartTimeParsing {

        @Test
        @DisplayName("should parse ISO time")
        void isoTime() {
            assertEquals(LocalTime.of(14, 30, 0), DateUtils.parseTime("14:30:00"));
        }

        @Test
        @DisplayName("should parse HH:mm")
        void shortTime() {
            assertEquals(LocalTime.of(14, 30), DateUtils.parseTime("14:30"));
        }

        @Test
        @DisplayName("should reject unparseable time")
        void rejectUnparseable() {
            assertThrows(DateTimeParseException.class,
                    () -> DateUtils.parseTime("25:00:00"));
        }
    }

    @Nested
    @DisplayName("Pattern-based parsing")
    class PatternParsing {

        @Test
        @DisplayName("should parse date with custom pattern")
        void customDatePattern() {
            LocalDate d = DateUtils.parseDate("15|03|2025", "dd|MM|yyyy");
            assertEquals(LocalDate.of(2025, 3, 15), d);
        }

        @Test
        @DisplayName("should parse date-time with custom pattern")
        void customDateTimePattern() {
            LocalDateTime dt = DateUtils.parseDateTime("2025.03.15-14:30", "yyyy.MM.dd-HH:mm");
            assertEquals(LocalDateTime.of(2025, 3, 15, 14, 30), dt);
        }

        @Test
        @DisplayName("should parse time with custom pattern")
        void customTimePattern() {
            LocalTime t = DateUtils.parseTime("14-30-00", "HH-mm-ss");
            assertEquals(LocalTime.of(14, 30, 0), t);
        }

        @Test
        @DisplayName("should reject null text for pattern parse")
        void rejectNullText() {
            assertThrows(NullPointerException.class,
                    () -> DateUtils.parseDate(null, "yyyy-MM-dd"));
        }

        @Test
        @DisplayName("should reject null pattern")
        void rejectNullPattern() {
            assertThrows(NullPointerException.class,
                    () -> DateUtils.parseDate("2025-03-15", (String) null));
        }
    }

    @Nested
    @DisplayName("Formatting")
    class Formatting {

        @Test
        @DisplayName("should format LocalDate")
        void formatDate() {
            String s = DateUtils.format(LocalDate.of(2025, 3, 15), "yyyy/MM/dd");
            assertEquals("2025/03/15", s);
        }

        @Test
        @DisplayName("should format LocalDateTime")
        void formatDateTime() {
            String s = DateUtils.format(LocalDateTime.of(2025, 3, 15, 14, 30, 0),
                    "yyyy-MM-dd HH:mm:ss");
            assertEquals("2025-03-15 14:30:00", s);
        }

        @Test
        @DisplayName("should format LocalTime")
        void formatTime() {
            String s = DateUtils.format(LocalTime.of(14, 30, 0), "HH:mm");
            assertEquals("14:30", s);
        }

        @Test
        @DisplayName("should reject null date for format")
        void rejectNullDate() {
            assertThrows(NullPointerException.class,
                    () -> DateUtils.format((LocalDate) null, "yyyy-MM-dd"));
        }

        @Test
        @DisplayName("should reject null pattern for format")
        void rejectNullPattern() {
            assertThrows(NullPointerException.class,
                    () -> DateUtils.format(LocalDate.now(), (String) null));
        }
    }
}
