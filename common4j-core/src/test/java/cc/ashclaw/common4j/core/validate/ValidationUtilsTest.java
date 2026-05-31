package cc.ashclaw.common4j.core.validate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ValidationUtils")
class ValidationUtilsTest {

    @Nested
    @DisplayName("isTrue(boolean, String)")
    class IsTrueString {

        @Test
        @DisplayName("passes when true")
        void passesWhenTrue() {
            assertDoesNotThrow(() -> ValidationUtils.isTrue(true, "ok"));
        }

        @Test
        @DisplayName("throws IllegalArgumentException when false")
        void throwsWhenFalse() {
            var e = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.isTrue(false, "bad"));
            assertEquals("bad", e.getMessage());
        }

        @Test
        @DisplayName("message is not computed when expression is true")
        void messageNotComputedWhenTrue() {
            ValidationUtils.isTrue(true, () -> {
                throw new AssertionError("should not compute message");
            });
        }
    }

    @Nested
    @DisplayName("isTrue(boolean, Supplier<String>)")
    class IsTrueSupplier {

        @Test
        @DisplayName("passes when true")
        void passesWhenTrue() {
            assertDoesNotThrow(() -> ValidationUtils.isTrue(true, () -> "ok"));
        }

        @Test
        @DisplayName("throws IllegalArgumentException when false with lazy message")
        void throwsWhenFalse() {
            var e = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.isTrue(false, () -> "lazy"));
            assertEquals("lazy", e.getMessage());
        }

        @Test
        @DisplayName("supplier not called when expression is true")
        void supplierNotCalledWhenTrue() {
            ValidationUtils.isTrue(true, () -> {
                throw new AssertionError("should not be called");
            });
        }
    }

    @Nested
    @DisplayName("validState(boolean, String)")
    class ValidStateString {

        @Test
        @DisplayName("passes when true")
        void passesWhenTrue() {
            assertDoesNotThrow(() -> ValidationUtils.validState(true, "ok"));
        }

        @Test
        @DisplayName("throws IllegalStateException when false")
        void throwsWhenFalse() {
            var e = assertThrows(IllegalStateException.class,
                    () -> ValidationUtils.validState(false, "bad state"));
            assertEquals("bad state", e.getMessage());
        }
    }

    @Nested
    @DisplayName("validState(boolean, Supplier<String>)")
    class ValidStateSupplier {

        @Test
        @DisplayName("passes when true")
        void passesWhenTrue() {
            assertDoesNotThrow(() -> ValidationUtils.validState(true, () -> "ok"));
        }

        @Test
        @DisplayName("throws IllegalStateException when false with lazy message")
        void throwsWhenFalse() {
            var e = assertThrows(IllegalStateException.class,
                    () -> ValidationUtils.validState(false, () -> "lazy state"));
            assertEquals("lazy state", e.getMessage());
        }

        @Test
        @DisplayName("supplier not called when expression is true")
        void supplierNotCalledWhenTrue() {
            ValidationUtils.validState(true, () -> {
                throw new AssertionError("should not be called");
            });
        }
    }

    @Nested
    @DisplayName("notBlank(String, String)")
    class NotBlank {

        @Test
        @DisplayName("returns value when non-blank")
        void returnsWhenNonBlank() {
            assertEquals("hello", ValidationUtils.notBlank("hello", "name"));
        }

        @Test
        @DisplayName("throws NPE when null")
        void throwsNpeWhenNull() {
            var e = assertThrows(NullPointerException.class,
                    () -> ValidationUtils.notBlank(null, "name"));
            assertEquals("name must not be null", e.getMessage());
        }

        @Test
        @DisplayName("throws IAE when empty")
        void throwsWhenEmpty() {
            var e = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.notBlank("", "name"));
            assertEquals("name must not be blank", e.getMessage());
        }

        @Test
        @DisplayName("throws IAE when whitespace only")
        void throwsWhenWhitespace() {
            var e = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.notBlank("   ", "name"));
            assertEquals("name must not be blank", e.getMessage());
        }

        @Test
        @DisplayName("name appears in exception message")
        void nameInMessage() {
            var e = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.notBlank("", "email"));
            assertTrue(e.getMessage().contains("email"));
        }
    }

    @Nested
    @DisplayName("notEmpty(Collection, String)")
    class NotEmptyCollection {

        @Test
        @DisplayName("returns collection when non-empty")
        void returnsWhenNonEmpty() {
            var list = List.of("a", "b");
            assertSame(list, ValidationUtils.notEmpty(list, "items"));
        }

        @Test
        @DisplayName("throws NPE when null")
        void throwsNpeWhenNull() {
            var e = assertThrows(NullPointerException.class,
                    () -> ValidationUtils.notEmpty((List<?>) null, "items"));
            assertEquals("items must not be null", e.getMessage());
        }

        @Test
        @DisplayName("throws IAE when empty")
        void throwsWhenEmpty() {
            var e = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.notEmpty(List.of(), "items"));
            assertEquals("items must not be empty", e.getMessage());
        }

        @Test
        @DisplayName("works with ArrayList")
        void worksWithArrayList() {
            var list = new ArrayList<>(List.of(1));
            assertSame(list, ValidationUtils.notEmpty(list, "nums"));
        }
    }

    @Nested
    @DisplayName("notEmpty(Map, String)")
    class NotEmptyMap {

        @Test
        @DisplayName("returns map when non-empty")
        void returnsWhenNonEmpty() {
            var map = Map.of("k", "v");
            assertSame(map, ValidationUtils.notEmpty(map, "config"));
        }

        @Test
        @DisplayName("throws NPE when null")
        void throwsNpeWhenNull() {
            var e = assertThrows(NullPointerException.class,
                    () -> ValidationUtils.notEmpty((Map<?, ?>) null, "config"));
            assertEquals("config must not be null", e.getMessage());
        }

        @Test
        @DisplayName("throws IAE when empty")
        void throwsWhenEmpty() {
            var e = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.notEmpty(Map.of(), "config"));
            assertEquals("config must not be empty", e.getMessage());
        }

        @Test
        @DisplayName("works with HashMap")
        void worksWithHashMap() {
            var map = new HashMap<>(Map.of(1, "a"));
            assertSame(map, ValidationUtils.notEmpty(map, "m"));
        }
    }

    @Nested
    @DisplayName("notEmpty(T[], String)")
    class NotEmptyArray {

        @Test
        @DisplayName("returns array when non-empty")
        void returnsWhenNonEmpty() {
            var arr = new String[]{"a", "b"};
            assertSame(arr, ValidationUtils.notEmpty(arr, "tags"));
        }

        @Test
        @DisplayName("throws NPE when null")
        void throwsNpeWhenNull() {
            var e = assertThrows(NullPointerException.class,
                    () -> ValidationUtils.notEmpty((String[]) null, "tags"));
            assertEquals("tags must not be null", e.getMessage());
        }

        @Test
        @DisplayName("throws IAE when empty")
        void throwsWhenEmpty() {
            var e = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.notEmpty(new String[0], "tags"));
            assertEquals("tags must not be empty", e.getMessage());
        }
    }

    @Nested
    @DisplayName("positive(int, String)")
    class PositiveInt {

        @Test
        @DisplayName("returns value when positive")
        void returnsWhenPositive() {
            assertEquals(42, ValidationUtils.positive(42, "count"));
        }

        @Test
        @DisplayName("throws when zero")
        void throwsWhenZero() {
            var e = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.positive(0, "count"));
            assertTrue(e.getMessage().contains("positive"));
            assertTrue(e.getMessage().contains("0"));
        }

        @Test
        @DisplayName("throws when negative")
        void throwsWhenNegative() {
            var e = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.positive(-5, "count"));
            assertTrue(e.getMessage().contains("-5"));
        }

        @Test
        @DisplayName("name appears in exception message")
        void nameInMessage() {
            var e = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.positive(0, "size"));
            assertTrue(e.getMessage().contains("size"));
        }

        @Test
        @DisplayName("1 is valid (edge case)")
        void oneIsValid() {
            assertEquals(1, ValidationUtils.positive(1, "n"));
        }
    }

    @Nested
    @DisplayName("positive(long, String)")
    class PositiveLong {

        @Test
        @DisplayName("returns value when positive")
        void returnsWhenPositive() {
            assertEquals(42L, ValidationUtils.positive(42L, "count"));
        }

        @Test
        @DisplayName("throws when zero")
        void throwsWhenZero() {
            assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.positive(0L, "count"));
        }

        @Test
        @DisplayName("throws when negative")
        void throwsWhenNegative() {
            assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.positive(-1L, "count"));
        }
    }

    @Nested
    @DisplayName("positive(double, String)")
    class PositiveDouble {

        @Test
        @DisplayName("returns value when positive")
        void returnsWhenPositive() {
            assertEquals(3.14, ValidationUtils.positive(3.14, "pi"));
        }

        @Test
        @DisplayName("throws when zero")
        void throwsWhenZero() {
            assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.positive(0.0, "val"));
        }

        @Test
        @DisplayName("throws when negative")
        void throwsWhenNegative() {
            assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.positive(-0.5, "val"));
        }

        @Test
        @DisplayName("throws when NaN")
        void throwsWhenNaN() {
            assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.positive(Double.NaN, "val"));
        }
    }

    @Nested
    @DisplayName("notNegative(int, String)")
    class NotNegativeInt {

        @Test
        @DisplayName("returns value when positive")
        void returnsWhenPositive() {
            assertEquals(10, ValidationUtils.notNegative(10, "offset"));
        }

        @Test
        @DisplayName("returns zero (valid)")
        void zeroIsValid() {
            assertEquals(0, ValidationUtils.notNegative(0, "offset"));
        }

        @Test
        @DisplayName("throws when negative")
        void throwsWhenNegative() {
            var e = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.notNegative(-3, "offset"));
            assertTrue(e.getMessage().contains("-3"));
        }

        @Test
        @DisplayName("name appears in exception message")
        void nameInMessage() {
            var e = assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.notNegative(-1, "index"));
            assertTrue(e.getMessage().contains("index"));
        }
    }

    @Nested
    @DisplayName("notNegative(long, String)")
    class NotNegativeLong {

        @Test
        @DisplayName("returns value when positive")
        void returnsWhenPositive() {
            assertEquals(100L, ValidationUtils.notNegative(100L, "size"));
        }

        @Test
        @DisplayName("returns zero (valid)")
        void zeroIsValid() {
            assertEquals(0L, ValidationUtils.notNegative(0L, "size"));
        }

        @Test
        @DisplayName("throws when negative")
        void throwsWhenNegative() {
            assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.notNegative(-1L, "size"));
        }
    }

    @Nested
    @DisplayName("notNegative(double, String)")
    class NotNegativeDouble {

        @Test
        @DisplayName("returns value when positive")
        void returnsWhenPositive() {
            assertEquals(2.5, ValidationUtils.notNegative(2.5, "val"));
        }

        @Test
        @DisplayName("returns zero (valid)")
        void zeroIsValid() {
            assertEquals(0.0, ValidationUtils.notNegative(0.0, "val"));
        }

        @Test
        @DisplayName("returns negative zero (valid)")
        void negativeZeroIsValid() {
            assertEquals(-0.0, ValidationUtils.notNegative(-0.0, "val"));
        }

        @Test
        @DisplayName("throws when negative")
        void throwsWhenNegative() {
            assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.notNegative(-0.1, "val"));
        }

        @Test
        @DisplayName("throws when NaN")
        void throwsWhenNaN() {
            assertThrows(IllegalArgumentException.class,
                    () -> ValidationUtils.notNegative(Double.NaN, "val"));
        }
    }

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("should throw UnsupportedOperationException")
        void shouldThrow() throws Exception {
            var ctor = ValidationUtils.class.getDeclaredConstructor();
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
