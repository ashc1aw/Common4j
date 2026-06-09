package cc.ashclaw.common4j.core.validate;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Precondition validation utilities — fills gaps the JDK's
 * {@link java.util.Objects} leaves open.
 *
 * Boolean assertions:
 * <pre>{@code
 * ValidationUtils.isTrue(x > 0, "x must be positive");
 * ValidationUtils.validState(!closed, "already closed");
 * }</pre>
 *
 * Value constraints (return the value for fluent assignment):
 * <pre>{@code
 * var name  = ValidationUtils.notBlank(input, "name");
 * var items = ValidationUtils.notEmpty(list, "items");
 * var count = ValidationUtils.positive(n, "count");
 * }</pre>
 */
public final class ValidationUtils {

    private ValidationUtils() {
        throw new UnsupportedOperationException("utility class");
    }

    // -- Boolean assertions (void) ---------------------------------------

    /**
     * Asserts that the given expression is {@code true}, throwing
     * {@link IllegalArgumentException} with the given message otherwise.
     *
     * @param expression the boolean expression to check
     * @param message    the error message if the assertion fails
     * @throws IllegalArgumentException if {@code expression} is false
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) throw new IllegalArgumentException(message);
    }

    /**
     * Asserts that the given expression is {@code true}, throwing
     * {@link IllegalArgumentException} with a lazily-computed message otherwise.
     *
     * @param expression      the boolean expression to check
     * @param messageSupplier supplier for the error message if the assertion fails
     * @throws IllegalArgumentException if {@code expression} is false
     */
    public static void isTrue(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) throw new IllegalArgumentException(messageSupplier.get());
    }

    /**
     * Asserts that the given expression is {@code true}, throwing
     * {@link IllegalStateException} with the given message otherwise.
     *
     * @param expression the boolean expression to check
     * @param message    the error message if the assertion fails
     * @throws IllegalStateException if {@code expression} is false
     */
    public static void validState(boolean expression, String message) {
        if (!expression) throw new IllegalStateException(message);
    }

    /**
     * Asserts that the given expression is {@code true}, throwing
     * {@link IllegalStateException} with a lazily-computed message otherwise.
     *
     * @param expression      the boolean expression to check
     * @param messageSupplier supplier for the error message if the assertion fails
     * @throws IllegalStateException if {@code expression} is false
     */
    public static void validState(boolean expression, Supplier<String> messageSupplier) {
        if (!expression) throw new IllegalStateException(messageSupplier.get());
    }

    // -- String ----------------------------------------------------------

    /**
     * Requires the string to be non-null and not blank (as defined by
     * {@link String#isBlank()}), returning it for fluent assignment.
     *
     * @param value the string to validate
     * @param name  the parameter name for error messages
     * @return the validated (non-blank) string
     * @throws NullPointerException     if {@code value} is null
     * @throws IllegalArgumentException if {@code value} is blank
     */
    public static String notBlank(String value, String name) {
        Objects.requireNonNull(value, () -> "%s must not be null".formatted(name));
        if (value.isBlank())
            throw new IllegalArgumentException("%s must not be blank".formatted(name));
        return value;
    }

    // -- Collection ------------------------------------------------------

    /**
     * Requires the collection to be non-null and non-empty, returning it
     * for fluent assignment.
     *
     * @param <E>        the element type
     * @param <C>        the collection type
     * @param collection the collection to validate
     * @param name       the parameter name for error messages
     * @return the validated (non-empty) collection
     * @throws NullPointerException     if {@code collection} is null
     * @throws IllegalArgumentException if {@code collection} is empty
     */
    public static <E, C extends Collection<E>> C notEmpty(C collection, String name) {
        Objects.requireNonNull(collection, () -> "%s must not be null".formatted(name));
        if (collection.isEmpty())
            throw new IllegalArgumentException("%s must not be empty".formatted(name));
        return collection;
    }

    // -- Map -------------------------------------------------------------

    /**
     * Requires the map to be non-null and non-empty, returning it for fluent
     * assignment.
     *
     * @param <K>  the key type
     * @param <V>  the value type
     * @param <M>  the map type
     * @param map  the map to validate
     * @param name the parameter name for error messages
     * @return the validated (non-empty) map
     * @throws NullPointerException     if {@code map} is null
     * @throws IllegalArgumentException if {@code map} is empty
     */
    public static <K, V, M extends Map<K, V>> M notEmpty(M map, String name) {
        Objects.requireNonNull(map, () -> "%s must not be null".formatted(name));
        if (map.isEmpty())
            throw new IllegalArgumentException("%s must not be empty".formatted(name));
        return map;
    }

    // -- Array -----------------------------------------------------------

    /**
     * Requires the array to be non-null and non-empty, returning it for fluent
     * assignment.
     *
     * @param <T>   the array component type
     * @param array the array to validate
     * @param name  the parameter name for error messages
     * @return the validated (non-empty) array
     * @throws NullPointerException     if {@code array} is null
     * @throws IllegalArgumentException if {@code array} is empty
     */
    public static <T> T[] notEmpty(T[] array, String name) {
        Objects.requireNonNull(array, () -> "%s must not be null".formatted(name));
        if (array.length == 0)
            throw new IllegalArgumentException("%s must not be empty".formatted(name));
        return array;
    }

    // -- Numeric ---------------------------------------------------------

    /**
     * Requires the value to be strictly positive ({@code > 0}), returning it
     * for fluent assignment.
     *
     * @param value the integer value to validate
     * @param name  the parameter name for error messages
     * @return the validated (positive) value
     * @throws IllegalArgumentException if {@code value <= 0}
     */
    public static int positive(int value, String name) {
        if (value <= 0)
            throw new IllegalArgumentException(
                    "%s must be positive: %d".formatted(name, value));
        return value;
    }

    /**
     * Requires the value to be strictly positive ({@code > 0}), returning it
     * for fluent assignment.
     *
     * @param value the long value to validate
     * @param name  the parameter name for error messages
     * @return the validated (positive) value
     * @throws IllegalArgumentException if {@code value <= 0}
     */
    public static long positive(long value, String name) {
        if (value <= 0)
            throw new IllegalArgumentException(
                    "%s must be positive: %d".formatted(name, value));
        return value;
    }

    /**
     * Requires the value to be strictly positive ({@code > 0}), returning it
     * for fluent assignment. NaN and negative zero are rejected.
     *
     * @param value the double value to validate
     * @param name  the parameter name for error messages
     * @return the validated (positive) value
     * @throws IllegalArgumentException if {@code value <= 0} or NaN
     */
    public static double positive(double value, String name) {
        if (!(value > 0))
            throw new IllegalArgumentException(
                    "%s must be positive: %f".formatted(name, value));
        return value;
    }

    /**
     * Requires the value to be non-negative ({@code >= 0}), returning it
     * for fluent assignment.
     *
     * @param value the integer value to validate
     * @param name  the parameter name for error messages
     * @return the validated (non-negative) value
     * @throws IllegalArgumentException if {@code value < 0}
     */
    public static int notNegative(int value, String name) {
        if (value < 0)
            throw new IllegalArgumentException(
                    "%s must be negative: %d".formatted(name, value));
        return value;
    }

    /**
     * Requires the value to be non-negative ({@code >= 0}), returning it
     * for fluent assignment.
     *
     * @param value the long value to validate
     * @param name  the parameter name for error messages
     * @return the validated (non-negative) value
     * @throws IllegalArgumentException if {@code value < 0}
     */
    public static long notNegative(long value, String name) {
        if (value < 0)
            throw new IllegalArgumentException(
                    "%s must be negative: %d".formatted(name, value));
        return value;
    }

    /**
     * Requires the value to be non-negative ({@code >= 0}), returning it
     * for fluent assignment. NaN and negative zero are accepted.
     *
     * @param value the double value to validate
     * @param name  the parameter name for error messages
     * @return the validated (non-negative) value
     * @throws IllegalArgumentException if {@code value < 0}
     */
    public static double notNegative(double value, String name) {
        if (!(value >= 0))
            throw new IllegalArgumentException(
                    "%s must be negative: %f".formatted(name, value));
        return value;
    }
}
