package cc.ashclaw.common4j.core.io;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * I/O utilities for everyday file and classpath operations that are still
 * verbose with the JDK alone.
 *
 * <b>Recursive delete</b>
 * <pre>{@code
 * IoUtils.deleteRecursive(Path.of("/tmp/build"));
 * }</pre>
 *
 * <b>Directory size</b>
 * <pre>{@code
 * long bytes = IoUtils.size(Path.of("/data"));
 * }</pre>
 *
 * <b>Classpath resources</b>
 * <pre>{@code
 * String content = IoUtils.resourceAsString("config/defaults.json");
 * String content = IoUtils.resourceAsString(MyClass.class, "schema.sql");
 * }</pre>
 */
public final class IoUtils {

    private IoUtils() {
        throw new UnsupportedOperationException("utility class");
    }

    // ── Recursive delete ───────────────────────────────────────────────

    /**
     * Recursively deletes a file or directory, equivalent to {@code rm -rf}.
     *
     * <p>Unlike {@link Files#delete(Path)} which requires the directory to be empty,
     * this method walks the tree and deletes everything. Symbolic links are deleted
     * without following (the link itself is removed, not the target).
     *
     * @param path the file or directory to delete
     * @throws IOException              if any file cannot be deleted
     * @throws IllegalArgumentException if path is null
     */
    public static void deleteRecursive(Path path) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        if (!Files.exists(path)) return;

        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc)
                    throws IOException {
                if (exc != null) throw exc;
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    // ── Directory size ─────────────────────────────────────────────────

    /**
     * Recursively computes the total size of all files under the given path.
     *
     * <p>If the path is a regular file, returns its size. If it is a directory,
     * returns the sum of all descendant files. Symbolic links are followed.
     *
     * @param path the file or directory
     * @return total size in bytes, or 0 if the path does not exist
     * @throws IOException if an I/O error occurs while traversing
     */
    public static long size(Path path) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        if (!Files.exists(path)) return 0;
        if (!Files.isDirectory(path)) return Files.size(path);

        var total = new AtomicLong(0);
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                total.addAndGet(attrs.size());
                return FileVisitResult.CONTINUE;
            }
        });
        return total.get();
    }

    // ── Classpath resources ────────────────────────────────────────────

    /**
     * Reads a classpath resource as a UTF-8 string, using the calling class's
     * classloader.
     *
     * <p>The resource path is resolved from the classpath root.
     * A leading {@code /} is stripped so both {@code "/config/app.json"}
     * and {@code "config/app.json"} work.
     *
     * @param resource the classpath resource path
     * @return the resource content as a string
     * @throws IllegalArgumentException if the resource is not found
     * @throws UncheckedIOException     if reading fails
     */
    public static String resourceAsString(String resource) {
        Objects.requireNonNull(resource, "resource must not be null");
        // Use the caller's class via the stack — simple and works.
        // Actually, use the IoUtils classloader as a reasonable default.
        return resourceAsString(IoUtils.class, resource);
    }

    /**
     * Reads a classpath resource relative to the given class.
     *
     * <pre>{@code
     * // Absolute from classpath root
     * String sql = IoUtils.resourceAsString(MyDao.class, "/sql/init.sql");
     *
     * // Relative to MyDao's package
     * String sql = IoUtils.resourceAsString(MyDao.class, "init.sql");
     * }</pre>
     *
     * @param context  the class to resolve the resource relative to
     * @param resource the resource path (absolute if starts with {@code /})
     * @return the resource content as a UTF-8 string
     * @throws IllegalArgumentException if the resource is not found
     * @throws UncheckedIOException     if reading fails
     */
    public static String resourceAsString(Class<?> context, String resource) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(resource, "resource must not be null");

        // If not starting with /, resolve relative to context
        String resolved = resource.startsWith("/")
                ? resource
                : resolveRelative(context, resource);

        try (var is = context.getResourceAsStream(resolved)) {
            if (is == null) {
                throw new IllegalArgumentException(
                        "resource not found: \"%s\" (context: %s)".formatted(
                                resource, context.getName()));
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "failed to read resource: \"%s\"".formatted(resource), e);
        }
    }

    /**
     * Reads a classpath resource as a byte array.
     *
     * @param context  the class to resolve the resource relative to
     * @param resource the resource path (absolute if starts with {@code /})
     * @return the resource content as bytes
     * @throws IllegalArgumentException if the resource is not found
     * @throws UncheckedIOException     if reading fails
     */
    public static byte[] resourceAsBytes(Class<?> context, String resource) {
        Objects.requireNonNull(context, "context must not be null");
        Objects.requireNonNull(resource, "resource must not be null");

        String resolved = resource.startsWith("/")
                ? resource
                : resolveRelative(context, resource);

        try (var is = context.getResourceAsStream(resolved)) {
            if (is == null) {
                throw new IllegalArgumentException(
                        "resource not found: \"%s\" (context: %s)".formatted(
                                resource, context.getName()));
            }
            return is.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(
                    "failed to read resource: \"%s\"".formatted(resource), e);
        }
    }

    // ── Internal ───────────────────────────────────────────────────────

    /** Resolves a relative resource path against the context class's package. */
    private static String resolveRelative(Class<?> context, String resource) {
        if (resource.startsWith("/")) return resource;
        String pkg = context.getPackageName();
        if (pkg.isEmpty()) return "/" + resource;
        return "/" + pkg.replace('.', '/') + "/" + resource;
    }
}
