package cc.ashclaw.common4j.core.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("MaskUtils")
class MaskUtilsTest {

    @Nested
    @DisplayName("mask(String, int, int)")
    class DefaultMaskChar {

        @Test
        @DisplayName("should mask middle with *")
        void masksMiddle() {
            assertEquals("138****1234", MaskUtils.mask("13812341234", 3, 4));
        }

        @Test
        @DisplayName("should mask the entire middle portion")
        void masksEntireMiddle() {
            assertEquals("a****z", MaskUtils.mask("abcdez", 1, 1));
        }

        @Test
        @DisplayName("should return all masks when keep is 0")
        void allMasked() {
            assertEquals("****", MaskUtils.mask("1234", 0, 0));
        }

        @Test
        @DisplayName("should return as-is when keep covers the entire string")
        void tooShortReturnsAsIs() {
            assertEquals("ab", MaskUtils.mask("ab", 1, 1));
            assertEquals("abc", MaskUtils.mask("abc", 2, 2));
        }

        @Test
        @DisplayName("should return as-is when keep exceeds string length")
        void keepExceedsLength() {
            assertEquals("hi", MaskUtils.mask("hi", 5, 5));
        }

        @Test
        @DisplayName("null should return null")
        void nullReturnsNull() {
            assertNull(MaskUtils.mask(null, 1, 1));
        }

        @Test
        @DisplayName("empty should return empty")
        void emptyReturnsEmpty() {
            assertEquals("", MaskUtils.mask("", 1, 1));
        }

        @Test
        @DisplayName("should throw IAE when keepPrefix is negative")
        void negativePrefix() {
            assertThrows(IllegalArgumentException.class,
                    () -> MaskUtils.mask("abc", -1, 1));
        }

        @Test
        @DisplayName("should throw IAE when keepSuffix is negative")
        void negativeSuffix() {
            assertThrows(IllegalArgumentException.class,
                    () -> MaskUtils.mask("abc", 1, -1));
        }
    }

    @Nested
    @DisplayName("mask(String, int, int, char)")
    class CustomMaskChar {

        @Test
        @DisplayName("should use custom mask character")
        void customMaskChar() {
            assertEquals("622202XXXXXX7890",
                    MaskUtils.mask("6222021234567890", 6, 4, 'X'));
        }

        @Test
        @DisplayName("should mask with digit character")
        void digitMask() {
            assertEquals("32XX01", MaskUtils.mask("320101", 2, 2, 'X'));
        }

        @Test
        @DisplayName("should work with space as mask")
        void spaceMask() {
            assertEquals("ab  cd", MaskUtils.mask("abXXcd", 2, 2, ' '));
        }
    }

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("should throw UnsupportedOperationException")
        void shouldThrow() throws Exception {
            var ctor = MaskUtils.class.getDeclaredConstructor();
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
