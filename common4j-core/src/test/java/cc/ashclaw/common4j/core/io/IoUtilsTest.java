package cc.ashclaw.common4j.core.io;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("IoUtils")
class IoUtilsTest {

    @Nested
    @DisplayName("deleteRecursive")
    class DeleteRecursive {

        @Test
        @DisplayName("delete empty directory")
        void deleteEmptyDir(@TempDir Path tmp) throws IOException {
            var dir = tmp.resolve("empty");
            Files.createDirectory(dir);
            assertTrue(Files.exists(dir));

            IoUtils.deleteRecursive(dir);
            assertFalse(Files.exists(dir));
        }

        @Test
        @DisplayName("delete directory with files")
        void deleteDirWithFiles(@TempDir Path tmp) throws IOException {
            var dir = tmp.resolve("withFiles");
            Files.createDirectory(dir);
            Files.writeString(dir.resolve("a.txt"), "hello");
            Files.writeString(dir.resolve("b.txt"), "world");

            IoUtils.deleteRecursive(dir);
            assertFalse(Files.exists(dir));
        }

        @Test
        @DisplayName("delete nested directories")
        void deleteNested(@TempDir Path tmp) throws IOException {
            var dir = tmp.resolve("nested");
            Files.createDirectory(dir);
            var sub = dir.resolve("sub");
            Files.createDirectory(sub);
            Files.writeString(sub.resolve("x.txt"), "data");
            Files.writeString(dir.resolve("root.txt"), "root");

            IoUtils.deleteRecursive(dir);
            assertFalse(Files.exists(dir));
        }

        @Test
        @DisplayName("delete single file")
        void deleteFile(@TempDir Path tmp) throws IOException {
            var file = tmp.resolve("test.txt");
            Files.writeString(file, "hello");

            IoUtils.deleteRecursive(file);
            assertFalse(Files.exists(file));
        }

        @Test
        @DisplayName("non-existent path is no-op")
        void nonExistent(@TempDir Path tmp) {
            assertDoesNotThrow(() -> IoUtils.deleteRecursive(tmp.resolve("ghost")));
        }
    }

    @Nested
    @DisplayName("size")
    class Size {

        @Test
        @DisplayName("size of single file")
        void singleFile(@TempDir Path tmp) throws IOException {
            var file = tmp.resolve("data.txt");
            Files.writeString(file, "hello world");

            assertEquals(11, IoUtils.size(file));
        }

        @Test
        @DisplayName("size of directory")
        void directory(@TempDir Path tmp) throws IOException {
            Files.writeString(tmp.resolve("a.txt"), "abc");    // 3
            Files.writeString(tmp.resolve("b.txt"), "123456"); // 6

            assertEquals(9, IoUtils.size(tmp));
        }

        @Test
        @DisplayName("size of nested directories")
        void nestedDirectories(@TempDir Path tmp) throws IOException {
            var sub = tmp.resolve("sub");
            Files.createDirectory(sub);
            Files.writeString(tmp.resolve("a.txt"), "ab");    // 2
            Files.writeString(sub.resolve("b.txt"), "cdef");  // 4

            assertEquals(6, IoUtils.size(tmp));
        }

        @Test
        @DisplayName("empty directory returns 0")
        void emptyDir(@TempDir Path tmp) throws IOException {
            assertEquals(0, IoUtils.size(tmp));
        }

        @Test
        @DisplayName("non-existent path returns 0")
        void nonExistent(@TempDir Path tmp) throws IOException {
            assertEquals(0, IoUtils.size(tmp.resolve("ghost")));
        }
    }

    @Nested
    @DisplayName("resourceAsString")
    class ResourceAsString {

        @Test
        @DisplayName("read existing resource")
        void readResource() throws IOException {
            // Read from classpath root (using test resource)
            var content = IoUtils.resourceAsString(IoUtilsTest.class, "/io/test-resource.txt");
            assertEquals("hello io\n", content);
        }

        @Test
        @DisplayName("resource not found throws")
        void notFound() {
            assertThrows(IllegalArgumentException.class, () ->
                    IoUtils.resourceAsString(IoUtilsTest.class, "/io/nonexistent.txt"));
        }

        @Test
        @DisplayName("no-arg overload reads from classpath root")
        void noArgOverload() {
            // This tests the default classloader path
            var content = IoUtils.resourceAsString("/io/test-resource.txt");
            assertEquals("hello io\n", content);
        }
    }

    @Nested
    @DisplayName("resourceAsBytes")
    class ResourceAsBytes {

        @Test
        @DisplayName("read resource as bytes")
        void readBytes() throws IOException {
            var bytes = IoUtils.resourceAsBytes(IoUtilsTest.class, "/io/test-resource.txt");
            assertArrayEquals("hello io\n".getBytes(StandardCharsets.UTF_8), bytes);
        }
    }

    @Nested
    @DisplayName("utility class")
    class UtilityClass {

        @Test
        @DisplayName("constructor throws")
        void constructorThrows() throws Exception {
            var ctor = IoUtils.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            var ex = assertThrows(java.lang.reflect.InvocationTargetException.class, ctor::newInstance);
            assertInstanceOf(UnsupportedOperationException.class, ex.getCause());
        }
    }
}
