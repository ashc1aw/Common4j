package cc.ashclaw.common4j.core.stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;

import java.util.Vector;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StreamUtils")
class StreamUtilsTest {

    @Nested
    @DisplayName("stream(Iterator)")
    class StreamFromIterator {

        @Test
        @DisplayName("should stream all elements from an iterator")
        void streamsAllElements() {
            var iter = List.of("a", "b", "c").iterator();
            var result = StreamUtils.stream(iter).toList();
            assertEquals(List.of("a", "b", "c"), result);
        }

        @Test
        @DisplayName("empty iterator should produce empty stream")
        void emptyIterator() {
            var iter = List.of().iterator();
            var result = StreamUtils.stream(iter).toList();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should throw NPE when iterator is null")
        void npeWhenNull() {
            assertThrows(NullPointerException.class, () -> StreamUtils.stream((Iterator<?>) null));
        }

        @Test
        @DisplayName("stream should be lazy — iterator not consumed until terminal op")
        void lazyEvaluation() {
            var iter = List.of(1, 2, 3).iterator();
            var stream = StreamUtils.stream(iter);
            // stream created but not consumed — the underlying iterator still has all elements
            var result = stream.limit(2).toList();
            assertEquals(List.of(1, 2), result);
        }
    }

    @Nested
    @DisplayName("stream(Enumeration)")
    class StreamFromEnumeration {

        @Test
        @DisplayName("should stream all elements from an enumeration")
        void streamsAllElements() {
            var vec = new Vector<>(List.of("x", "y", "z"));
            var result = StreamUtils.stream(vec.elements()).toList();
            assertEquals(List.of("x", "y", "z"), result);
        }

        @Test
        @DisplayName("empty enumeration should produce empty stream")
        void emptyEnumeration() {
            var vec = new Vector<String>();
            var result = StreamUtils.stream(vec.elements()).toList();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should throw NPE when enumeration is null")
        void npeWhenNull() {
            assertThrows(NullPointerException.class, () -> StreamUtils.stream((java.util.Enumeration<?>) null));
        }
    }

    @Nested
    @DisplayName("zip()")
    class Zip {

        @Test
        @DisplayName("should zip equal-length streams")
        void equalLength() {
            var a = Stream.of(1, 2, 3);
            var b = Stream.of("x", "y", "z");
            var result = StreamUtils.zip(a, b, (n, s) -> n + s).toList();
            assertEquals(List.of("1x", "2y", "3z"), result);
        }

        @Test
        @DisplayName("should stop at the shorter stream (first shorter)")
        void firstShorter() {
            var a = Stream.of(1, 2);
            var b = Stream.of("a", "b", "c");
            var result = StreamUtils.zip(a, b, (n, s) -> n + s).toList();
            assertEquals(List.of("1a", "2b"), result);
        }

        @Test
        @DisplayName("should stop at the shorter stream (second shorter)")
        void secondShorter() {
            var a = Stream.of(1, 2, 3);
            var b = Stream.of("x");
            var result = StreamUtils.zip(a, b, (n, s) -> n + s).toList();
            assertEquals(List.of("1x"), result);
        }

        @Test
        @DisplayName("empty first stream should produce empty result")
        void emptyFirst() {
            var result = StreamUtils.zip(Stream.empty(), Stream.of(1, 2), (a, b) -> "x").toList();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("empty second stream should produce empty result")
        void emptySecond() {
            var result = StreamUtils.zip(Stream.of(1, 2), Stream.empty(), (a, b) -> "x").toList();
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("should work with infinite streams")
        void infiniteStream() {
            var a = Stream.iterate(0, i -> i + 1);
            var b = Stream.of("a", "b", "c");
            var result = StreamUtils.zip(a, b, (n, s) -> n + s)
                    .limit(3).toList();
            assertEquals(List.of("0a", "1b", "2c"), result);
        }

        @Test
        @DisplayName("should throw NPE when first stream is null")
        void npeFirstNull() {
            assertThrows(NullPointerException.class,
                    () -> StreamUtils.zip(null, Stream.of(1), (a, b) -> "x"));
        }

        @Test
        @DisplayName("should throw NPE when second stream is null")
        void npeSecondNull() {
            assertThrows(NullPointerException.class,
                    () -> StreamUtils.zip(Stream.of(1), null, (a, b) -> "x"));
        }

        @Test
        @DisplayName("should throw NPE when zipper is null")
        void npeZipperNull() {
            assertThrows(NullPointerException.class,
                    () -> StreamUtils.zip(Stream.of(1), Stream.of(2), null));
        }
    }

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("should throw UnsupportedOperationException")
        void shouldThrow() throws Exception {
            var ctor = StreamUtils.class.getDeclaredConstructor();
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
