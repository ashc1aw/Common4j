package cc.ashclaw.common4j.core.time;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.*;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DateRange")
class DateRangeTest {

    @Nested
    @DisplayName("Closed interval (default constructor)")
    class ClosedInterval {

        private final DateRange<LocalDate> range = new DateRange<>(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31));

        @Test
        @DisplayName("should contain start point")
        void containsStart() {
            assertTrue(range.contains(LocalDate.of(2025, 1, 1)));
        }

        @Test
        @DisplayName("should contain end point")
        void containsEnd() {
            assertTrue(range.contains(LocalDate.of(2025, 12, 31)));
        }

        @Test
        @DisplayName("should contain midpoint")
        void containsMidpoint() {
            assertTrue(range.contains(LocalDate.of(2025, 6, 15)));
        }

        @Test
        @DisplayName("should not contain point before start")
        void notBeforeStart() {
            assertFalse(range.contains(LocalDate.of(2024, 12, 31)));
        }

        @Test
        @DisplayName("should not contain point after end")
        void notAfterEnd() {
            assertFalse(range.contains(LocalDate.of(2026, 1, 1)));
        }

        @Test
        @DisplayName("should be start-inclusive")
        void startInclusive() {
            assertTrue(range.startInclusive());
        }

        @Test
        @DisplayName("should be end-inclusive")
        void endInclusive() {
            assertTrue(range.endInclusive());
        }

        @Test
        @DisplayName("toString should use brackets")
        void toStringUsesBrackets() {
            assertEquals("[2025-01-01, 2025-12-31]", range.toString());
        }
    }

    @Nested
    @DisplayName("Open / half-open intervals")
    class OpenIntervals {

        @Test
        @DisplayName("start-exclusive should reject start point")
        void startExclusive() {
            var range = new DateRange<>(
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 12, 31),
                    false, true);
            assertFalse(range.contains(LocalDate.of(2025, 1, 1)));
            assertTrue(range.contains(LocalDate.of(2025, 1, 2)));
        }

        @Test
        @DisplayName("end-exclusive should reject end point")
        void endExclusive() {
            var range = new DateRange<>(
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 12, 31),
                    true, false);
            assertTrue(range.contains(LocalDate.of(2025, 12, 30)));
            assertFalse(range.contains(LocalDate.of(2025, 12, 31)));
        }

        @Test
        @DisplayName("both-exclusive should use parentheses in toString")
        void bothExclusiveToString() {
            var range = new DateRange<>(
                    LocalDate.of(2025, 1, 1),
                    LocalDate.of(2025, 12, 31),
                    false, false);
            assertEquals("(2025-01-01, 2025-12-31)", range.toString());
        }
    }

    @Nested
    @DisplayName("Different temporal types")
    class DifferentTypes {

        @Test
        @DisplayName("should work with LocalDateTime")
        void localDateTime() {
            var range = new DateRange<>(
                    LocalDateTime.of(2025, 1, 1, 8, 0),
                    LocalDateTime.of(2025, 1, 1, 18, 0));
            assertTrue(range.contains(LocalDateTime.of(2025, 1, 1, 12, 0)));
            assertFalse(range.contains(LocalDateTime.of(2025, 1, 1, 19, 0)));
        }

        @Test
        @DisplayName("should work with LocalTime")
        void localTime() {
            var range = new DateRange<>(
                    LocalTime.of(9, 0),
                    LocalTime.of(17, 0));
            assertTrue(range.contains(LocalTime.of(12, 0)));
            assertFalse(range.contains(LocalTime.of(8, 0)));
        }

        @Test
        @DisplayName("should work with Instant")
        void instant() {
            var range = new DateRange<>(
                    Instant.EPOCH,
                    Instant.EPOCH.plusSeconds(3600));
            assertTrue(range.contains(Instant.EPOCH.plusSeconds(1800)));
            assertFalse(range.contains(Instant.EPOCH.minusSeconds(1)));
        }
    }

    @Nested
    @DisplayName("Construction validation")
    class Validation {

        @Test
        @DisplayName("should reject null start")
        void rejectNullStart() {
            assertThrows(NullPointerException.class,
                    () -> new DateRange<>(null, LocalDate.now()));
        }

        @Test
        @DisplayName("should reject null end")
        void rejectNullEnd() {
            assertThrows(NullPointerException.class,
                    () -> new DateRange<>(LocalDate.now(), null));
        }

        @Test
        @DisplayName("should reject null point in contains")
        void rejectNullPoint() {
            var range = new DateRange<>(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
            assertThrows(NullPointerException.class, () -> range.contains(null));
        }

        @Test
        @DisplayName("should reject start after end")
        void rejectStartAfterEnd() {
            assertThrows(IllegalArgumentException.class,
                    () -> new DateRange<>(
                            LocalDate.of(2025, 12, 31),
                            LocalDate.of(2025, 1, 1)));
        }

        @Test
        @DisplayName("should accept start equal to end")
        void acceptEqualBounds() {
            var range = new DateRange<>(LocalDate.of(2025, 6, 15), LocalDate.of(2025, 6, 15));
            assertTrue(range.contains(LocalDate.of(2025, 6, 15)));
        }
    }
}
