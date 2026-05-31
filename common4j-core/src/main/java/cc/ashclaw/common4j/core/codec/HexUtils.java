package cc.ashclaw.common4j.core.codec;

import java.util.HexFormat;
import java.util.Objects;

/**
 * Hex utilities beyond what {@link java.util.HexFormat} provides.
 *
 * <p>For basic encode/decode, use {@link HexFormat#of() HexFormat.of()} directly.
 * HexUtils fills the gaps it doesn't cover.</p>
 *
 * <h3>Hex dump</h3>
 * <pre>{@code
 * byte[] data = {0x48, 0x65, 0x6C, 0x6C, 0x6F, ...};
 *
 * System.out.println(HexUtils.dump(data));
 * // 00000000  48 65 6C 6C 6F 20 57 6F 72 6C 64 21 00 00 00 00  |Hello World!....|
 * // 00000010  00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00  |................|
 * }</pre>
 *
 * <p>Use the offset+length overloads to dump a slice of a larger buffer
 * (e.g. a ByteBuffer's backing array, or a network packet payload).</p>
 */
public final class HexUtils {

    private static final int DEFAULT_BYTES_PER_LINE = 16;

    private HexUtils() {
        throw new UnsupportedOperationException("utility class");
    }

    // -- hex dump ------------------------------------------------------------

    /**
     * Returns a multi-line hex dump of the byte array in classic format:
     * 8-digit hex offset, 16 hex bytes per line, and an ASCII sidebar.
     *
     * @param bytes the byte array to dump, must not be null
     * @return the formatted hex dump, or empty string when bytes is empty
     */
    public static String dump(byte[] bytes) {
        Objects.requireNonNull(bytes, "bytes must not be null");
        return dump(bytes, 0, bytes.length, DEFAULT_BYTES_PER_LINE);
    }

    /**
     * Returns a hex dump of a slice of the byte array.
     *
     * @param bytes  the byte array to dump, must not be null
     * @param offset the starting index (inclusive)
     * @param length the number of bytes to include
     * @return the formatted hex dump, or empty string when length is 0
     * @throws IndexOutOfBoundsException if offset or length is out of range
     */
    public static String dump(byte[] bytes, int offset, int length) {
        Objects.requireNonNull(bytes, "bytes must not be null");
        return dump(bytes, offset, length, DEFAULT_BYTES_PER_LINE);
    }

    /**
     * Returns a hex dump of a slice of the byte array with custom line width.
     *
     * @param bytes        the byte array to dump, must not be null
     * @param offset       the starting index (inclusive)
     * @param length       the number of bytes to include
     * @param bytesPerLine number of hex bytes per line, must be positive
     * @return the formatted hex dump, or empty string when length is 0
     * @throws IndexOutOfBoundsException if offset or length is out of range
     * @throws IllegalArgumentException  if bytesPerLine is not positive
     */
    public static String dump(byte[] bytes, int offset, int length, int bytesPerLine) {
        Objects.requireNonNull(bytes, "bytes must not be null");
        if (offset < 0 || offset > bytes.length)
            throw new IndexOutOfBoundsException(
                    "offset " + offset + " out of [0, " + bytes.length + "]");
        if (length < 0 || offset + length > bytes.length)
            throw new IndexOutOfBoundsException(
                    "length " + length + " out of range from offset " + offset);
        if (bytesPerLine <= 0)
            throw new IllegalArgumentException("bytesPerLine must be positive: " + bytesPerLine);

        if (length == 0) return "";

        var sb = new StringBuilder();
        int end = offset + length;

        for (int lineStart = offset; lineStart < end; lineStart += bytesPerLine) {
            int lineEnd = Math.min(lineStart + bytesPerLine, end);

            // offset column
            sb.append("%08X  ".formatted(lineStart - offset));

            // hex column — pad short last line to keep alignment
            for (int j = 0; j < bytesPerLine; j++) {
                int idx = lineStart + j;
                if (idx < lineEnd) {
                    sb.append("%02X ".formatted(bytes[idx] & 0xFF));
                } else {
                    sb.append("   ");
                }
            }

            // ascii sidebar
            sb.append(" |");
            for (int j = lineStart; j < lineEnd; j++) {
                int b = bytes[j] & 0xFF;
                sb.append(b >= 0x20 && b < 0x7F ? (char) b : '.');
            }
            sb.append('|');

            if (lineEnd < end) sb.append('\n');
        }

        return sb.toString();
    }
}
