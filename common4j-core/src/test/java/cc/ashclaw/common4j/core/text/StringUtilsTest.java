package cc.ashclaw.common4j.core.text;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StringUtils")
class StringUtilsTest {

    @Nested
    @DisplayName("camelToSnake")
    class CamelToSnake {

        @Test
        @DisplayName("simple camelCase")
        void simple() {
            assertEquals("user_name", StringUtils.camelToSnake("userName"));
        }

        @Test
        @DisplayName("multiple words")
        void multipleWords() {
            assertEquals("user_profile_image_url", StringUtils.camelToSnake("userProfileImageUrl"));
        }

        @Test
        @DisplayName("already snake_case")
        void alreadySnake() {
            assertEquals("user_name", StringUtils.camelToSnake("user_name"));
        }

        @Test
        @DisplayName("acronym handling")
        void acronym() {
            assertEquals("http_header", StringUtils.camelToSnake("HTTPHeader"));
        }

        @Test
        @DisplayName("all uppercase")
        void allUppercase() {
            assertEquals("abc", StringUtils.camelToSnake("ABC"));
        }

        @Test
        @DisplayName("single word")
        void singleWord() {
            assertEquals("name", StringUtils.camelToSnake("name"));
        }

        @Test
        @DisplayName("null returns null")
        void nullInput() {
            assertNull(StringUtils.camelToSnake(null));
        }

        @Test
        @DisplayName("empty returns empty")
        void emptyInput() {
            assertEquals("", StringUtils.camelToSnake(""));
        }

        @Test
        @DisplayName("with digits")
        void withDigits() {
            assertEquals("json2_xml", StringUtils.camelToSnake("json2Xml"));
        }
    }

    @Nested
    @DisplayName("snakeToCamel")
    class SnakeToCamel {

        @Test
        @DisplayName("simple snake_case")
        void simple() {
            assertEquals("userName", StringUtils.snakeToCamel("user_name"));
        }

        @Test
        @DisplayName("multiple underscores")
        void multipleUnderscores() {
            assertEquals("userProfileImageUrl", StringUtils.snakeToCamel("user_profile_image_url"));
        }

        @Test
        @DisplayName("already camelCase")
        void alreadyCamel() {
            assertEquals("username", StringUtils.snakeToCamel("username"));
        }

        @Test
        @DisplayName("leading underscore preserved")
        void leadingUnderscore() {
            assertEquals("_private", StringUtils.snakeToCamel("_private"));
        }

        @Test
        @DisplayName("uppercase letters lowered")
        void uppercaseLetters() {
            assertEquals("httpHeader", StringUtils.snakeToCamel("HTTP_HEADER"));
        }

        @Test
        @DisplayName("null returns null")
        void nullInput() {
            assertNull(StringUtils.snakeToCamel(null));
        }

        @Test
        @DisplayName("empty returns empty")
        void emptyInput() {
            assertEquals("", StringUtils.snakeToCamel(""));
        }
    }

    @Nested
    @DisplayName("capitalize")
    class Capitalize {

        @Test
        @DisplayName("lowercase first letter")
        void lowercase() {
            assertEquals("Hello", StringUtils.capitalize("hello"));
        }

        @Test
        @DisplayName("already capitalized")
        void alreadyCapitalized() {
            assertEquals("Hello", StringUtils.capitalize("Hello"));
        }

        @Test
        @DisplayName("single character")
        void singleChar() {
            assertEquals("A", StringUtils.capitalize("a"));
        }

        @Test
        @DisplayName("null returns null")
        void nullInput() {
            assertNull(StringUtils.capitalize(null));
        }

        @Test
        @DisplayName("empty returns empty")
        void emptyInput() {
            assertEquals("", StringUtils.capitalize(""));
        }
    }

    @Nested
    @DisplayName("uncapitalize")
    class Uncapitalize {

        @Test
        @DisplayName("uppercase first letter")
        void uppercase() {
            assertEquals("hello", StringUtils.uncapitalize("Hello"));
        }

        @Test
        @DisplayName("already uncapitalized")
        void alreadyUncapitalized() {
            assertEquals("hello", StringUtils.uncapitalize("hello"));
        }

        @Test
        @DisplayName("null returns null")
        void nullInput() {
            assertNull(StringUtils.uncapitalize(null));
        }

        @Test
        @DisplayName("empty returns empty")
        void emptyInput() {
            assertEquals("", StringUtils.uncapitalize(""));
        }
    }

    @Nested
    @DisplayName("truncate")
    class Truncate {

        @Test
        @DisplayName("string longer than max")
        void longerThanMax() {
            assertEquals("Hello...", StringUtils.truncate("Hello World", 8));
        }

        @Test
        @DisplayName("string shorter than max")
        void shorterThanMax() {
            assertEquals("Hi", StringUtils.truncate("Hi", 8));
        }

        @Test
        @DisplayName("string equals max")
        void equalsMax() {
            assertEquals("Hello", StringUtils.truncate("Hello", 5));
        }

        @Test
        @DisplayName("null returns null")
        void nullInput() {
            assertNull(StringUtils.truncate(null, 5));
        }

        @Test
        @DisplayName("maxLen less than 3 throws")
        void maxLenTooSmall() {
            assertThrows(IllegalArgumentException.class, () ->
                    StringUtils.truncate("hello", 2));
        }
    }

    @Nested
    @DisplayName("defaultIfBlank")
    class DefaultIfBlank {

        @Test
        @DisplayName("non-blank returns value")
        void nonBlank() {
            assertEquals("hello", StringUtils.defaultIfBlank("hello", "N/A"));
        }

        @Test
        @DisplayName("blank returns default")
        void blank() {
            assertEquals("N/A", StringUtils.defaultIfBlank("  ", "N/A"));
        }

        @Test
        @DisplayName("null returns default")
        void nullInput() {
            assertEquals("N/A", StringUtils.defaultIfBlank(null, "N/A"));
        }

        @Test
        @DisplayName("empty returns default")
        void empty() {
            assertEquals("N/A", StringUtils.defaultIfBlank("", "N/A"));
        }
    }

    @Nested
    @DisplayName("defaultString")
    class DefaultString {

        @Test
        @DisplayName("non-null returns value")
        void nonNull() {
            assertEquals("hello", StringUtils.defaultString("hello"));
        }

        @Test
        @DisplayName("null returns empty string")
        void nullInput() {
            assertEquals("", StringUtils.defaultString(null));
        }

        @Test
        @DisplayName("null returns custom default")
        void nullWithCustom() {
            assertEquals("N/A", StringUtils.defaultString(null, "N/A"));
        }

        @Test
        @DisplayName("non-null with custom default")
        void nonNullWithCustom() {
            assertEquals("hi", StringUtils.defaultString("hi", "N/A"));
        }
    }

    @Nested
    @DisplayName("utility class")
    class UtilityClass {

        @Test
        @DisplayName("constructor throws")
        void constructorThrows() throws Exception {
            var ctor = StringUtils.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            var ex = assertThrows(java.lang.reflect.InvocationTargetException.class, ctor::newInstance);
            assertInstanceOf(UnsupportedOperationException.class, ex.getCause());
        }
    }
}
