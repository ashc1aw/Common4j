package cc.ashclaw.common4j.core.id;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Snowflake")
class SnowflakeTest {

    private final Snowflake sf = new Snowflake(0);

    @Nested
    @DisplayName("Generation")
    class Generation {

        @Test
        @DisplayName("should generate positive IDs")
        void shouldGeneratePositiveIds() {
            assertTrue(sf.nextId() > 0);
            assertTrue(sf.nextId() > 0);
        }

        @Test
        @DisplayName("should generate monotonically increasing IDs")
        void shouldGenerateMonotonicIds() {
            long id1 = sf.nextId();
            long id2 = sf.nextId();
            long id3 = sf.nextId();
            assertTrue(id2 > id1);
            assertTrue(id3 > id2);
        }

        @Test
        @DisplayName("should generate unique IDs under concurrent load")
        void shouldGenerateUniqueIdsConcurrently() throws Exception {
            Snowflake sf = new Snowflake(1);
            Set<Long> ids = ConcurrentHashMap.newKeySet();
            int threads = 8;
            int perThread = 10_000;
            CountDownLatch latch = new CountDownLatch(threads);

            for (int t = 0; t < threads; t++) {
                Thread.ofVirtual().start(() -> {
                    try {
                        for (int i = 0; i < perThread; i++) {
                            ids.add(sf.nextId());
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await();
            assertEquals(threads * perThread, ids.size());
        }
    }

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("should accept worker ID 0")
        void shouldAcceptWorkerIdZero() {
            assertTrue(new Snowflake(0).nextId() > 0);
        }

        @Test
        @DisplayName("should accept max worker ID")
        void shouldAcceptMaxWorkerId() {
            Snowflake sf = new Snowflake(1023);
            long id = sf.nextId();
            assertEquals(1023, Snowflake.extractWorkerId(id));
        }

        @Test
        @DisplayName("should reject negative worker ID")
        void shouldRejectNegativeWorkerId() {
            assertThrows(IllegalArgumentException.class, () -> new Snowflake(-1));
        }

        @Test
        @DisplayName("should reject worker ID above max")
        void shouldRejectWorkerIdAboveMax() {
            assertThrows(IllegalArgumentException.class, () -> new Snowflake(1024));
        }

        @Test
        @DisplayName("should reject future epoch")
        void shouldRejectFutureEpoch() {
            long futureEpoch = System.currentTimeMillis() + 86_400_000;
            assertThrows(IllegalArgumentException.class, () -> new Snowflake(1, futureEpoch));
        }
    }

    @Nested
    @DisplayName("Parsing")
    class Parsing {

        @Test
        @DisplayName("should roundtrip via instance parse")
        void shouldRoundtripViaInstanceParse() {
            Snowflake sf = new Snowflake(42);
            long id = sf.nextId();
            Snowflake.IdInfo info = sf.parse(id);
            assertEquals(42, info.workerId());
            assertTrue(info.sequence() >= 0 && info.sequence() <= 4095);
            assertTrue(info.timestamp().isAfter(Instant.ofEpochMilli(Snowflake.DEFAULT_EPOCH)));
        }

        @Test
        @DisplayName("should roundtrip via static parse with epoch")
        void shouldRoundtripViaStaticParse() {
            Snowflake sf = new Snowflake(7, Snowflake.DEFAULT_EPOCH);
            long id = sf.nextId();
            Snowflake.IdInfo info = Snowflake.parse(id, Snowflake.DEFAULT_EPOCH);
            assertEquals(7, info.workerId());
        }

        @Test
        @DisplayName("should extract worker ID statically")
        void shouldExtractWorkerId() {
            Snowflake sf = new Snowflake(512);
            long id = sf.nextId();
            assertEquals(512, Snowflake.extractWorkerId(id));
        }

        @Test
        @DisplayName("should extract sequence statically")
        void shouldExtractSequence() {
            long id = sf.nextId();
            long seq = Snowflake.extractSequence(id);
            assertTrue(seq >= 0 && seq <= 4095);
        }

        @Test
        @DisplayName("should extract timestamp statically")
        void shouldExtractTimestamp() {
            Instant before = Instant.now();
            long id = sf.nextId();
            Instant after = Instant.now();
            Instant ts = Snowflake.extractTimestamp(id);
            assertFalse(ts.isBefore(before.minusSeconds(1)));
            assertFalse(ts.isAfter(after.plusSeconds(1)));
        }
    }

    @Nested
    @DisplayName("String generation")
    class StringGeneration {

        @Test
        @DisplayName("should return valid decimal string")
        void shouldReturnValidDecimalString() {
            String s = sf.nextString();
            long parsed = Long.parseLong(s);
            assertTrue(parsed > 0);
        }
    }
}
