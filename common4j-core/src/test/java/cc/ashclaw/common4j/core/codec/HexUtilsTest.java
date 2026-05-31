package cc.ashclaw.common4j.core.codec;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HexUtils")
class HexUtilsTest {

    @Nested
    @DisplayName("dump(byte[])")
    class DumpFull {

        @Test
        @DisplayName("should produce standard multi-line hex dump")
        void standardDump() {
            var bytes = new byte[] {
                    0x48, 0x65, 0x6C, 0x6C, 0x6F, 0x20, 0x57, 0x6F,
                    0x72, 0x6C, 0x64, 0x21, 0x00, 0x01, 0x02, 0x03,
                    0x10, 0x20
            };

            var result = HexUtils.dump(bytes);

            var lines = result.split("\n");
            assertEquals(2, lines.length);
            assertEquals("00000000  48 65 6C 6C 6F 20 57 6F 72 6C 64 21 00 01 02 03  |Hello World!....|",
                    lines[0]);
            assertEquals("00000010  10 20                                            |. |",
                    lines[1]);
        }

        @Test
        @DisplayName("should produce single line for <= 16 bytes")
        void singleLine() {
            var bytes = new byte[] {0x41, 0x42, 0x43, 0x44};

            var result = HexUtils.dump(bytes);

            assertEquals("00000000  41 42 43 44                                      |ABCD|",
                    result);
        }

        @Test
        @DisplayName("empty array should return empty string")
        void emptyArray() {
            assertEquals("", HexUtils.dump(new byte[0]));
        }

        @Test
        @DisplayName("should replace non-printable chars with '.'")
        void nonPrintableChars() {
            var bytes = new byte[] {0x00, 0x01, 0x1F, (byte) 0x80, (byte) 0xFF};

            var result = HexUtils.dump(bytes);

            assertTrue(result.contains("|.....|"), "expected all dots: " + result);
        }

        @Test
        @DisplayName("should render full printable range")
        void printableRange() {
            // 0x20 (space) to 0x7E (~) is the printable ASCII range
            var bytes = new byte[] {0x20, 0x41, 0x7A, 0x7E};

            var result = HexUtils.dump(bytes);

            assertTrue(result.contains("| Az~|"), "got: " + result);
        }
    }

    @Nested
    @DisplayName("dump(byte[], int, int)")
    class DumpSlice {

        @Test
        @DisplayName("should dump a slice of the array")
        void slice() {
            var bytes = new byte[] {0x00, 0x01, 0x02, 0x03, 0x04, 0x05};

            var result = HexUtils.dump(bytes, 2, 2);

            assertEquals("00000000  02 03                                            |..|",
                    result);
        }

        @Test
        @DisplayName("zero length should return empty string")
        void zeroLength() {
            assertEquals("", HexUtils.dump(new byte[] {1, 2, 3}, 0, 0));
        }

        @Test
        @DisplayName("dump from middle to end")
        void middleToEnd() {
            var bytes = new byte[] {0x00, 0x01, 0x41, 0x42};

            var result = HexUtils.dump(bytes, 2, 2);

            assertEquals("00000000  41 42                                            |AB|",
                    result);
        }
    }

    @Nested
    @DisplayName("dump(byte[], int, int, int)")
    class DumpCustomLineWidth {

        @Test
        @DisplayName("should use custom bytes per line")
        void customBytesPerLine() {
            var bytes = new byte[] {0x01, 0x02, 0x03, 0x04};

            var result = HexUtils.dump(bytes, 0, bytes.length, 2);

            var lines = result.split("\n");
            assertEquals(2, lines.length);
            assertEquals("00000000  01 02  |..|", lines[0]);
            assertEquals("00000002  03 04  |..|", lines[1]);
        }

        @Test
        @DisplayName("bytes per line of 1 should produce one byte per line")
        void oneBytePerLine() {
            var bytes = new byte[] {0x41, 0x42};

            var result = HexUtils.dump(bytes, 0, bytes.length, 1);

            var lines = result.split("\n");
            assertEquals(2, lines.length);
            assertEquals("00000000  41  |A|", lines[0]);
            assertEquals("00000001  42  |B|", lines[1]);
        }
    }

    @Nested
    @DisplayName("offset alignment")
    class OffsetAlignment {

        @Test
        @DisplayName("offset should reflect position within the slice, not the original array")
        void offsetReflectsSlicePosition() {
            var bytes = new byte[100];
            for (int i = 0; i < 100; i++) bytes[i] = (byte) i;

            var result = HexUtils.dump(bytes, 48, 20);

            var lines = result.split("\n");
            assertEquals(2, lines.length);
            assertTrue(lines[0].startsWith("00000000"), "offset should reset to 0: " + lines[0]);
            assertTrue(lines[1].startsWith("00000010"), "second line offset: " + lines[1]);
        }
    }

    @Nested
    @DisplayName("null / invalid input")
    class InvalidInput {

        @Test
        @DisplayName("should throw NPE when bytes is null")
        void npeWhenBytesIsNull() {
            assertThrows(NullPointerException.class, () -> HexUtils.dump(null));
            assertThrows(NullPointerException.class, () -> HexUtils.dump(null, 0, 1));
            assertThrows(NullPointerException.class, () -> HexUtils.dump(null, 0, 1, 16));
        }

        @Test
        @DisplayName("should throw IOB when offset is negative")
        void negativeOffset() {
            assertThrows(IndexOutOfBoundsException.class,
                    () -> HexUtils.dump(new byte[] {1, 2}, -1, 1));
        }

        @Test
        @DisplayName("should throw IOB when offset exceeds length")
        void offsetExceedsLength() {
            assertThrows(IndexOutOfBoundsException.class,
                    () -> HexUtils.dump(new byte[] {1, 2}, 3, 0));
        }

        @Test
        @DisplayName("should throw IOB when length is negative")
        void negativeLength() {
            assertThrows(IndexOutOfBoundsException.class,
                    () -> HexUtils.dump(new byte[] {1, 2}, 0, -1));
        }

        @Test
        @DisplayName("should throw IOB when offset+length exceeds bounds")
        void offsetPlusLengthExceedsBounds() {
            assertThrows(IndexOutOfBoundsException.class,
                    () -> HexUtils.dump(new byte[] {1, 2}, 1, 2));
        }

        @Test
        @DisplayName("should throw IAE when bytesPerLine is zero")
        void zeroBytesPerLine() {
            assertThrows(IllegalArgumentException.class,
                    () -> HexUtils.dump(new byte[] {1, 2}, 0, 2, 0));
        }

        @Test
        @DisplayName("should throw IAE when bytesPerLine is negative")
        void negativeBytesPerLine() {
            assertThrows(IllegalArgumentException.class,
                    () -> HexUtils.dump(new byte[] {1, 2}, 0, 2, -1));
        }
    }

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("should throw UnsupportedOperationException")
        void shouldThrow() throws Exception {
            var ctor = HexUtils.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            var e = assertThrows(UnsupportedOperationException.class, () -> {
                try {
                    ctor.newInstance();
                } catch (java.lang.reflect.InvocationTargetException ite) {
                    throw ite.getCause();
                }
            });
            assertNotNull(e);
        }
    }
}
