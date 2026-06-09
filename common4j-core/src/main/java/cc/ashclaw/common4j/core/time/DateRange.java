package cc.ashclaw.common4j.core.time;

import java.time.temporal.Temporal;
import java.util.Objects;

/**
 * A date/time range that supports containment checks.
 *
 * <p>The type parameter {@code T} must implement both {@link Temporal} and
 * {@link Comparable}, which covers {@link java.time.LocalDate},
 * {@link java.time.LocalDateTime}, {@link java.time.LocalTime},
 * {@link java.time.Instant}, and similar types.
 *
 * @param <T>            the temporal type
 * @param start          the start bound
 * @param end            the end bound
 * @param startInclusive whether the start bound is inclusive
 * @param endInclusive   whether the end bound is inclusive
 */
public record DateRange<T extends Temporal & Comparable<? super T>>(
        T start,
        T end,
        boolean startInclusive,
        boolean endInclusive) {

    /**
     * Validates that start is not null, end is not null, and start is not after end.
     *
     * @throws NullPointerException     if start or end is null
     * @throws IllegalArgumentException if start is after end
     */
    public DateRange {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");
        if (start.compareTo(end) > 0) {
            throw new IllegalArgumentException("start must not be after end");
        }
    }

    /**
     * Creates a closed interval (both bounds inclusive).
     *
     * @param start the start bound
     * @param end   the end bound
     */
    public DateRange(T start, T end) {
        this(start, end, true, true);
    }

    /**
     * Checks whether the given point falls within this range.
     *
     * @param point the point to test, not null
     * @return {@code true} if the point is contained in the range
     */
    public boolean contains(T point) {
        Objects.requireNonNull(point, "point must not be null");
        int cmpStart = start.compareTo(point);
        if (startInclusive ? cmpStart > 0 : cmpStart >= 0) {
            return false;
        }
        int cmpEnd = end.compareTo(point);
        return endInclusive ? cmpEnd >= 0 : cmpEnd > 0;
    }

    @Override
    public String toString() {
        String left = startInclusive ? "[" : "(";
        String right = endInclusive ? "]" : ")";
        return left + start + ", " + end + right;
    }
}
