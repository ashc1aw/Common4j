package cc.ashclaw.common4j.core.id;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IdUtils")
class IdUtilsTest {

    @Nested
    @DisplayName("Snowflake")
    class Snowflake {

        @Test
        @DisplayName("should generate positive ID")
        void shouldGeneratePositiveId() {
            assertTrue(IdUtils.nextId() > 0);
        }

        @Test
        @DisplayName("should generate string ID")
        void shouldGenerateStringId() {
            String s = IdUtils.nextString();
            assertNotNull(s);
            assertFalse(s.isEmpty());
            Long.parseLong(s); // should not throw
        }

        @Test
        @DisplayName("should return same instance from getSnowflake")
        void shouldReturnSameInstance() {
            cc.ashclaw.common4j.core.id.Snowflake s1 = IdUtils.getSnowflake();
            cc.ashclaw.common4j.core.id.Snowflake s2 = IdUtils.getSnowflake();
            assertSame(s1, s2);
        }
    }

    @Nested
    @DisplayName("UUID")
    class UUID {

        @Test
        @DisplayName("should generate 36-char UUID with dashes")
        void shouldGenerateUUIDWithDashes() {
            String uuid = IdUtils.randomUUID();
            assertEquals(36, uuid.length());
            assertEquals('-', uuid.charAt(8));
            assertEquals('-', uuid.charAt(13));
            assertEquals('-', uuid.charAt(18));
            assertEquals('-', uuid.charAt(23));
        }

        @Test
        @DisplayName("should generate 32-char UUID without dashes")
        void shouldGenerateUUIDSimple() {
            String uuid = IdUtils.randomUUIDSimple();
            assertEquals(32, uuid.length());
            assertFalse(uuid.contains("-"));
        }

        @Test
        @DisplayName("should generate unique UUIDs")
        void shouldGenerateUniqueUUIDs() {
            String u1 = IdUtils.randomUUIDSimple();
            String u2 = IdUtils.randomUUIDSimple();
            assertNotEquals(u1, u2);
        }

        @Test
        @DisplayName("should parse standard 36-char UUID")
        void shouldParseStandardUUID() {
            String s = "550e8400-e29b-41d4-a716-446655440000";
            java.util.UUID uuid = IdUtils.uuidFromString(s);
            assertEquals(s, uuid.toString());
        }

        @Test
        @DisplayName("should parse simple 32-char UUID")
        void shouldParseSimpleUUID() {
            String s = "550e8400e29b41d4a716446655440000";
            java.util.UUID uuid = IdUtils.uuidFromString(s);
            assertEquals(32, uuid.toString().replace("-", "").length());
        }

        @Test
        @DisplayName("should roundtrip random UUID simple")
        void shouldRoundtripRandomUUIDSimple() {
            String original = IdUtils.randomUUIDSimple();
            java.util.UUID uuid = IdUtils.uuidFromString(original);
            String roundtripped = String.format("%016x%016x",
                    uuid.getMostSignificantBits(), uuid.getLeastSignificantBits());
            assertEquals(original, roundtripped);
        }

        @Test
        @DisplayName("should reject invalid UUID string")
        void shouldRejectInvalidUUID() {
            assertThrows(IllegalArgumentException.class,
                    () -> IdUtils.uuidFromString("not-a-uuid"));
        }

        @Test
        @DisplayName("should reject wrong-length UUID string")
        void shouldRejectWrongLengthUUID() {
            assertThrows(IllegalArgumentException.class,
                    () -> IdUtils.uuidFromString("1234567890"));
        }
    }
}
