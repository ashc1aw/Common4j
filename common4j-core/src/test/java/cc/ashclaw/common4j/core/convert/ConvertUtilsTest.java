package cc.ashclaw.common4j.core.convert;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ConvertUtils")
class ConvertUtilsTest {

    @Nested
    @DisplayName("string coercion")
    class StringCoercion {

        @Test
        @DisplayName("string returns as-is")
        void stringPassthrough() {
            assertEquals("hello", ConvertUtils.coerce("hello", String.class, ""));
        }

        @Test
        @DisplayName("null string returns null")
        void nullString() {
            assertNull(ConvertUtils.coerce(null, String.class, ""));
        }
    }

    @Nested
    @DisplayName("integer coercion")
    class IntegerCoercion {

        @Test
        @DisplayName("parse positive integer")
        void positive() {
            assertEquals(42, ConvertUtils.coerce("42", int.class, ""));
            assertEquals(Integer.valueOf(42), ConvertUtils.coerce("42", Integer.class, ""));
        }

        @Test
        @DisplayName("parse negative integer")
        void negative() {
            assertEquals(-10, ConvertUtils.coerce("-10", int.class, ""));
        }

        @Test
        @DisplayName("parse with format")
        void withFormat() {
            assertEquals(1234, ConvertUtils.coerce("1,234", int.class, "#,##0"));
        }

        @Test
        @DisplayName("blank returns 0 for int")
        void blankInt() {
            assertEquals(0, ConvertUtils.coerce("  ", int.class, ""));
        }

        @Test
        @DisplayName("blank returns null for Integer")
        void blankInteger() {
            assertNull(ConvertUtils.coerce("  ", Integer.class, ""));
        }
    }

    @Nested
    @DisplayName("long coercion")
    class LongCoercion {

        @Test
        @DisplayName("parse long value")
        void parseLong() {
            assertEquals(10000000000L, ConvertUtils.coerce("10000000000", long.class, ""));
        }
    }

    @Nested
    @DisplayName("double coercion")
    class DoubleCoercion {

        @Test
        @DisplayName("parse double")
        void parseDouble() {
            assertEquals(3.14, ConvertUtils.coerce("3.14", double.class, ""));
        }
    }

    @Nested
    @DisplayName("BigDecimal coercion")
    class BigDecimalCoercion {

        @Test
        @DisplayName("parse without format")
        void withoutFormat() {
            assertEquals(new BigDecimal("1500.50"),
                    ConvertUtils.coerce("1500.50", BigDecimal.class, ""));
        }

        @Test
        @DisplayName("parse with format")
        void withFormat() {
            assertEquals(new BigDecimal("1500.50"),
                    ConvertUtils.coerce("1,500.50", BigDecimal.class, "#,##0.00"));
        }
    }

    @Nested
    @DisplayName("boolean coercion")
    class BooleanCoercion {

        @Test
        @DisplayName("true values")
        void trueValues() {
            assertTrue((Boolean) ConvertUtils.coerce("true", boolean.class, ""));
            assertTrue((Boolean) ConvertUtils.coerce("1", boolean.class, ""));
            assertTrue((Boolean) ConvertUtils.coerce("yes", boolean.class, ""));
        }

        @Test
        @DisplayName("false values")
        void falseValues() {
            assertFalse((Boolean) ConvertUtils.coerce("false", boolean.class, ""));
            assertFalse((Boolean) ConvertUtils.coerce("0", boolean.class, ""));
        }

        @Test
        @DisplayName("blank returns false for primitive")
        void blankPrimitive() {
            assertFalse((Boolean) ConvertUtils.coerce("  ", boolean.class, ""));
        }
    }

    @Nested
    @DisplayName("date coercion")
    class DateCoercion {

        @Test
        @DisplayName("parse LocalDate ISO format")
        void localDateIso() {
            assertEquals(LocalDate.of(2024, 6, 15),
                    ConvertUtils.coerce("2024-06-15", LocalDate.class, ""));
        }

        @Test
        @DisplayName("parse LocalDate with custom format")
        void localDateWithFormat() {
            assertEquals(LocalDate.of(2024, 6, 15),
                    ConvertUtils.coerce("2024/06/15", LocalDate.class, "yyyy/MM/dd"));
        }

        @Test
        @DisplayName("parse LocalDateTime ISO format")
        void localDateTimeIso() {
            assertEquals(LocalDateTime.of(2024, 6, 15, 10, 30),
                    ConvertUtils.coerce("2024-06-15T10:30:00", LocalDateTime.class, ""));
        }

        @Test
        @DisplayName("parse LocalDateTime space-separated")
        void localDateTimeSpace() {
            assertEquals(LocalDateTime.of(2024, 6, 15, 10, 30),
                    ConvertUtils.coerce("2024-06-15 10:30:00", LocalDateTime.class, ""));
        }

        @Test
        @DisplayName("parse LocalDateTime with format")
        void localDateTimeWithFormat() {
            assertEquals(LocalDateTime.of(2024, 6, 15, 10, 30),
                    ConvertUtils.coerce("2024/06/15 10:30",
                            LocalDateTime.class, "yyyy/MM/dd HH:mm"));
        }

        @Test
        @DisplayName("parse Date")
        void parseDate() {
            var result = ConvertUtils.coerce("2024-06-15 10:30:00", Date.class, "");
            assertNotNull(result);
            assertInstanceOf(Date.class, result);
        }
    }

    @Nested
    @DisplayName("typed convenience API")
    class TypedApi {

        @Test
        @DisplayName("to() returns expected type")
        void toTyped() {
            int result = ConvertUtils.to("42", int.class);
            assertEquals(42, result);
        }

        @Test
        @DisplayName("to() with format")
        void toWithFormat() {
            var result = ConvertUtils.to("2024/06/15", LocalDate.class, "yyyy/MM/dd");
            assertEquals(LocalDate.of(2024, 6, 15), result);
        }
    }

    @Nested
    @DisplayName("default values")
    class DefaultValues {

        @Test
        @DisplayName("primitive int defaults to 0")
        void intDefault() { assertEquals(0, ConvertUtils.defaultValue(int.class)); }

        @Test
        @DisplayName("primitive boolean defaults to false")
        void booleanDefault() { assertEquals(false, ConvertUtils.defaultValue(boolean.class)); }

        @Test
        @DisplayName("reference type defaults to null")
        void referenceDefault() { assertNull(ConvertUtils.defaultValue(String.class)); }
    }

    @Nested
    @DisplayName("error handling")
    class ErrorHandling {

        @Test
        @DisplayName("invalid number throws")
        void invalidNumber() {
            assertThrows(IllegalArgumentException.class, () ->
                    ConvertUtils.coerce("abc", int.class, ""));
        }

        @Test
        @DisplayName("invalid date throws")
        void invalidDate() {
            assertThrows(IllegalArgumentException.class, () ->
                    ConvertUtils.coerce("not-a-date", LocalDate.class, ""));
        }
    }

    @Nested
    @DisplayName("utility class")
    class UtilityClass {

        @Test
        @DisplayName("constructor throws")
        void constructorThrows() throws Exception {
            var ctor = ConvertUtils.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            var ex = assertThrows(java.lang.reflect.InvocationTargetException.class, ctor::newInstance);
            assertInstanceOf(UnsupportedOperationException.class, ex.getCause());
        }
    }
}
