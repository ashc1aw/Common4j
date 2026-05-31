package cc.ashclaw.common4j.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DigestUtils")
class DigestUtilsTest {

    @Nested
    @DisplayName("md5(String)")
    class Md5String {

        @Test
        @DisplayName("returns known hash")
        void knownHash() {
            assertEquals("5d41402abc4b2a76b9719d911017c592",
                    DigestUtils.md5("hello"));
        }

        @Test
        @DisplayName("empty string")
        void emptyString() {
            assertEquals("d41d8cd98f00b204e9800998ecf8427e",
                    DigestUtils.md5(""));
        }

        @Test
        @DisplayName("different inputs produce different hashes")
        void differentInputs() {
            assertNotEquals(DigestUtils.md5("hello"),
                    DigestUtils.md5("world"));
        }

        @Test
        @DisplayName("deterministic — same input same hash")
        void deterministic() {
            assertEquals(DigestUtils.md5("hello"),
                    DigestUtils.md5("hello"));
        }

        @Test
        @DisplayName("throws NPE when null")
        void throwsNpeWhenNull() {
            assertThrows(NullPointerException.class,
                    () -> DigestUtils.md5((String) null));
        }
    }

    @Nested
    @DisplayName("md5(byte[])")
    class Md5Bytes {

        @Test
        @DisplayName("matches string overload")
        void matchesString() {
            assertEquals(DigestUtils.md5("hello"),
                    DigestUtils.md5("hello".getBytes(StandardCharsets.UTF_8)));
        }

        @Test
        @DisplayName("throws NPE when null")
        void throwsNpeWhenNull() {
            assertThrows(NullPointerException.class,
                    () -> DigestUtils.md5((byte[]) null));
        }
    }

    @Nested
    @DisplayName("sha1(String)")
    class Sha1String {

        @Test
        @DisplayName("returns known hash")
        void knownHash() {
            assertEquals("aaf4c61ddcc5e8a2dabede0f3b482cd9aea9434d",
                    DigestUtils.sha1("hello"));
        }

        @Test
        @DisplayName("throws NPE when null")
        void throwsNpeWhenNull() {
            assertThrows(NullPointerException.class,
                    () -> DigestUtils.sha1((String) null));
        }
    }

    @Nested
    @DisplayName("sha256(String)")
    class Sha256String {

        @Test
        @DisplayName("returns known hash")
        void knownHash() {
            assertEquals("2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824",
                    DigestUtils.sha256("hello"));
        }

        @Test
        @DisplayName("empty string produces known hash")
        void emptyString() {
            assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                    DigestUtils.sha256(""));
        }

        @Test
        @DisplayName("throws NPE when null")
        void throwsNpeWhenNull() {
            assertThrows(NullPointerException.class,
                    () -> DigestUtils.sha256((String) null));
        }
    }

    @Nested
    @DisplayName("sha256(byte[])")
    class Sha256Bytes {

        @Test
        @DisplayName("matches string overload")
        void matchesString() {
            assertEquals(DigestUtils.sha256("hello"),
                    DigestUtils.sha256("hello".getBytes(StandardCharsets.UTF_8)));
        }

        @Test
        @DisplayName("throws NPE when null")
        void throwsNpeWhenNull() {
            assertThrows(NullPointerException.class,
                    () -> DigestUtils.sha256((byte[]) null));
        }
    }

    @Nested
    @DisplayName("sha256(InputStream)")
    class Sha256Stream {

        @Test
        @DisplayName("matches string overload")
        void matchesString() throws IOException {
            var is = new ByteArrayInputStream("hello".getBytes(StandardCharsets.UTF_8));
            assertEquals(DigestUtils.sha256("hello"),
                    DigestUtils.sha256(is));
        }

        @Test
        @DisplayName("empty stream")
        void emptyStream() throws IOException {
            var is = new ByteArrayInputStream(new byte[0]);
            assertEquals(DigestUtils.sha256(""),
                    DigestUtils.sha256(is));
        }

        @Test
        @DisplayName("throws NPE when null")
        void throwsNpeWhenNull() {
            assertThrows(NullPointerException.class,
                    () -> DigestUtils.sha256((java.io.InputStream) null));
        }
    }

    @Nested
    @DisplayName("sha256(Path)")
    class Sha256Path {

        @Test
        @DisplayName("reads file correctly")
        void readsFile(@TempDir Path tempDir) throws IOException {
            var file = tempDir.resolve("test.txt");
            Files.writeString(file, "hello");
            assertEquals(DigestUtils.sha256("hello"),
                    DigestUtils.sha256(file));
        }

        @Test
        @DisplayName("throws NPE when null")
        void throwsNpeWhenNull() {
            assertThrows(NullPointerException.class,
                    () -> DigestUtils.sha256((Path) null));
        }
    }

    @Nested
    @DisplayName("sha512(String)")
    class Sha512String {

        @Test
        @DisplayName("returns known hash")
        void knownHash() {
            assertEquals(
                    "9b71d224bd62f3785d96d46ad3ea3d73319bfbc2890caadae2dff72519673ca7" +
                    "2323c3d99ba5c11d7c7acc6e14b8c5da0c4663475c2e5c3adef46f73bcdec043",
                    DigestUtils.sha512("hello"));
        }
    }

    @Nested
    @DisplayName("digest(String, byte[])")
    class DigestGeneric {

        @Test
        @DisplayName("matches named methods")
        void matchesNamed() {
            assertEquals(DigestUtils.sha256("hello"),
                    DigestUtils.digest("SHA-256", "hello".getBytes(StandardCharsets.UTF_8)));
        }

        @Test
        @DisplayName("throws IAE when algorithm unknown")
        void unknownAlgorithm() {
            assertThrows(IllegalArgumentException.class,
                    () -> DigestUtils.digest("NOT-AN-ALGO", new byte[1]));
        }
    }

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("should throw UnsupportedOperationException")
        void shouldThrow() throws Exception {
            var ctor = DigestUtils.class.getDeclaredConstructor();
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
