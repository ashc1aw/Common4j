package cc.ashclaw.common4j.core.text;

/**
 * String utilities bridging gaps the JDK still leaves open.
 *
 * <h3>Naming conventions</h3>
 * <pre>{@code
 * StringUtils.camelToSnake("userName");   // "user_name"
 * StringUtils.snakeToCamel("user_name");  // "userName"
 * }</pre>
 *
 * <h3>Case helpers</h3>
 * <pre>{@code
 * StringUtils.capitalize("hello");    // "Hello"
 * StringUtils.uncapitalize("Hello");  // "hello"
 * }</pre>
 *
 * <h3>Null-safe defaults</h3>
 * <pre>{@code
 * StringUtils.defaultIfBlank(input, "N/A");
 * StringUtils.defaultString(input, "");
 * }</pre>
 */
public final class StringUtils {

    private StringUtils() {
        throw new UnsupportedOperationException("utility class");
    }

    // ── Naming conventions ────────────────────────────────────────────

    /**
     * Converts camelCase to snake_case.
     *
     * <pre>{@code
     * camelToSnake("userName")    → "user_name"
     * camelToSnake("HTTPHeader")  → "http_header"
     * camelToSnake("ABC")         → "abc"
     * }</pre>
     *
     * @param camel the camelCase string, may be null
     * @return the snake_case string, or null if input is null
     */
    public static String camelToSnake(String camel) {
        if (camel == null) return null;
        if (camel.isEmpty()) return camel;

        var sb = new StringBuilder();
        int len = camel.length();

        for (int i = 0; i < len; i++) {
            char c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                // Insert underscore before uppercase that follows a lowercase or digit,
                // or before an uppercase that's followed by a lowercase (acronym → break before last uppercase)
                if (i > 0
                        && (Character.isLowerCase(camel.charAt(i - 1))
                                || Character.isDigit(camel.charAt(i - 1))
                                || (i + 1 < len && Character.isLowerCase(camel.charAt(i + 1))))) {
                    sb.append('_');
                }
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * Converts snake_case to camelCase.
     *
     * <pre>{@code
     * snakeToCamel("user_name")   → "userName"
     * snakeToCamel("_private")    → "_private"
     * snakeToCamel("HTTP_HEADER") → "httpHeader"
     * }</pre>
     *
     * @param snake the snake_case string, may be null
     * @return the camelCase string, or null if input is null
     */
    public static String snakeToCamel(String snake) {
        if (snake == null) return null;
        if (snake.isEmpty()) return snake;

        var sb = new StringBuilder();
        int len = snake.length();
        boolean upperNext = false;
        int i = 0;

        // Preserve leading underscores (e.g. "_private" → "_private")
        while (i < len && snake.charAt(i) == '_') {
            sb.append('_');
            i++;
        }

        for (; i < len; i++) {
            char c = snake.charAt(i);
            if (c == '_') {
                upperNext = true;
            } else {
                sb.append(upperNext ? Character.toUpperCase(c) : Character.toLowerCase(c));
                upperNext = false;
            }
        }
        return sb.toString();
    }

    // ── Case helpers ───────────────────────────────────────────────────

    /**
     * Capitalizes the first character of a string.
     *
     * <pre>{@code
     * capitalize("hello")  → "Hello"
     * capitalize("Hello")  → "Hello"
     * capitalize("")       → ""
     * capitalize(null)     → null
     * }</pre>
     */
    public static String capitalize(String value) {
        if (value == null) return null;
        if (value.isEmpty()) return value;
        char first = value.charAt(0);
        if (Character.isUpperCase(first)) return value;
        return Character.toUpperCase(first) + value.substring(1);
    }

    /**
     * Uncapitalizes the first character of a string (reverse of {@link #capitalize}).
     *
     * <pre>{@code
     * uncapitalize("Hello")  → "hello"
     * uncapitalize("hello")  → "hello"
     * uncapitalize("")       → ""
     * uncapitalize(null)     → null
     * }</pre>
     */
    public static String uncapitalize(String value) {
        if (value == null) return null;
        if (value.isEmpty()) return value;
        char first = value.charAt(0);
        if (Character.isLowerCase(first)) return value;
        return Character.toLowerCase(first) + value.substring(1);
    }

    // ── Truncation ─────────────────────────────────────────────────────

    /**
     * Truncates a string to the given length, appending {@code "..."} if truncated.
     *
     * <pre>{@code
     * truncate("Hello World", 8)  → "Hello..."
     * truncate("Hi", 8)           → "Hi"
     * truncate(null, 5)           → null
     * }</pre>
     *
     * @param value    the string to truncate, may be null
     * @param maxLen   maximum length including the ellipsis (must be at least 3)
     * @return the truncated string, or null if input is null
     * @throws IllegalArgumentException if maxLen is less than 3
     */
    public static String truncate(String value, int maxLen) {
        if (value == null) return null;
        if (maxLen < 3) throw new IllegalArgumentException("maxLen must be >= 3: " + maxLen);
        if (value.length() <= maxLen) return value;
        return value.substring(0, maxLen - 3) + "...";
    }

    // ── Null-safe defaults ─────────────────────────────────────────────

    /**
     * Returns the input string if non-blank, or the default value otherwise.
     *
     * <pre>{@code
     * defaultIfBlank("hello", "N/A")  → "hello"
     * defaultIfBlank("  ", "N/A")     → "N/A"
     * defaultIfBlank(null, "N/A")     → "N/A"
     * defaultIfBlank("", "N/A")       → "N/A"
     * }</pre>
     */
    public static String defaultIfBlank(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }

    /**
     * Returns the input string if non-null, or an empty string otherwise.
     *
     * <pre>{@code
     * defaultString("hello")  → "hello"
     * defaultString(null)     → ""
     * }</pre>
     */
    public static String defaultString(String value) {
        return value == null ? "" : value;
    }

    /**
     * Returns the input string if non-null, or the given default otherwise.
     *
     * <pre>{@code
     * defaultString(null, "N/A")  → "N/A"
     * defaultString("hi", "N/A")  → "hi"
     * }</pre>
     */
    public static String defaultString(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }
}
