package cc.ashclaw.common4j.core.text;


/**
 * String masking for logging, display, and PII redaction.
 *
 * <pre>{@code
 * MaskUtils.mask("13812341234", 3, 4);   // "138****1234"  (phone)
 * MaskUtils.mask("secret", 1, 1);        // "s***t"        (general)
 * MaskUtils.mask("6222021234567890", 0, 4, 'X');  // "XXXXXXXXXXXX7890"  (bank card)
 * }</pre>
 *
 * <p>{@code null} input returns {@code null} — safe to chain with {@code Optional}.</p>
 */
public final class MaskUtils {

    private static final char DEFAULT_MASK_CHAR = '*';

    private MaskUtils() {
        throw new UnsupportedOperationException("utility class");
    }

    /**
     * Replaces the middle portion of a string with {@code *}, keeping
     * the first {@code keepPrefix} and last {@code keepSuffix} characters visible.
     *
     * <pre>{@code
     * mask("1234567890", 3, 4)  → "123***7890"
     * mask("abcd", 1, 1)        → "a**d"
     * mask("ab", 1, 1)          → "ab"     (too short, returned as-is)
     * mask(null, 1, 1)          → null
     * mask("", 1, 1)            → ""
     * }</pre>
     *
     * @param value       the string to mask, may be null
     * @param keepPrefix  number of leading characters to keep visible (non-negative)
     * @param keepSuffix  number of trailing characters to keep visible (non-negative)
     * @return the masked string, or null if input is null
     * @throws IllegalArgumentException if keepPrefix or keepSuffix is negative
     */
    public static String mask(String value, int keepPrefix, int keepSuffix) {
        return mask(value, keepPrefix, keepSuffix, DEFAULT_MASK_CHAR);
    }

    /**
     * Replaces the middle portion of a string with a custom mask character.
     *
     * @param value       the string to mask, may be null
     * @param keepPrefix  number of leading characters to keep visible (non-negative)
     * @param keepSuffix  number of trailing characters to keep visible (non-negative)
     * @param maskChar    the character to use for masking
     * @return the masked string, or null if input is null
     * @throws IllegalArgumentException if keepPrefix or keepSuffix is negative
     */
    public static String mask(String value, int keepPrefix, int keepSuffix, char maskChar) {
        if (value == null) return null;
        if (value.isEmpty()) return value;
        if (keepPrefix < 0) throw new IllegalArgumentException("keepPrefix must be >= 0: " + keepPrefix);
        if (keepSuffix < 0) throw new IllegalArgumentException("keepSuffix must be >= 0: " + keepSuffix);

        int len = value.length();
        int keep = keepPrefix + keepSuffix;
        if (keep >= len) return value;

        int maskLen = len - keep;
        return value.substring(0, keepPrefix)
                + String.valueOf(maskChar).repeat(maskLen)
                + value.substring(len - keepSuffix);
    }
}
