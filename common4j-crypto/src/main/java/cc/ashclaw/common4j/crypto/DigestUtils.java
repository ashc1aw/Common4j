package cc.ashclaw.common4j.crypto;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Digest utilities — one-line hashing for checksums, cache keys, and
 * non-security fingerprinting.
 *
 * <p>For password storage, use a dedicated password hashing library
 * (bcrypt, scrypt, or argon2). The algorithms here are fast hashes,
 * not suitable for passwords.
 *
 * Usage example:
 * <pre>{@code
 * var hex = DigestUtils.sha256("hello");
 * var fileHash = DigestUtils.sha256(Path.of("/data.bin"));
 * }</pre>
 */
public final class DigestUtils {

    private DigestUtils() {
        throw new UnsupportedOperationException("utility class");
    }

    // -- MD5 (non-cryptographic — checksums / cache keys) ----------------

    /**
     * Returns the MD5 hex digest of the given string (UTF-8).
     *
     * @param input the string to hash, must not be null
     * @return the MD5 hash as a lowercase hex string
     */
    public static String md5(String input) {
        Objects.requireNonNull(input, "input must not be null");
        return digest("MD5", input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Returns the MD5 hex digest of the given bytes.
     *
     * @param input the bytes to hash, must not be null
     * @return the MD5 hash as a lowercase hex string
     */
    public static String md5(byte[] input) {
        Objects.requireNonNull(input, "input must not be null");
        return digest("MD5", input);
    }

    // -- SHA-1 -----------------------------------------------------------

    /**
     * Returns the SHA-1 hex digest of the given string (UTF-8).
     *
     * @param input the string to hash, must not be null
     * @return the SHA-1 hash as a lowercase hex string
     */
    public static String sha1(String input) {
        Objects.requireNonNull(input, "input must not be null");
        return digest("SHA-1", input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Returns the SHA-1 hex digest of the given bytes.
     *
     * @param input the bytes to hash, must not be null
     * @return the SHA-1 hash as a lowercase hex string
     */
    public static String sha1(byte[] input) {
        Objects.requireNonNull(input, "input must not be null");
        return digest("SHA-1", input);
    }

    // -- SHA-256 ---------------------------------------------------------

    /**
     * Returns the SHA-256 hex digest of the given string (UTF-8).
     *
     * @param input the string to hash, must not be null
     * @return the SHA-256 hash as a lowercase hex string
     */
    public static String sha256(String input) {
        Objects.requireNonNull(input, "input must not be null");
        return digest("SHA-256", input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Returns the SHA-256 hex digest of the given bytes.
     *
     * @param input the bytes to hash, must not be null
     * @return the SHA-256 hash as a lowercase hex string
     */
    public static String sha256(byte[] input) {
        Objects.requireNonNull(input, "input must not be null");
        return digest("SHA-256", input);
    }

    /**
     * Returns the SHA-256 hex digest of the given file.
     *
     * @param path the file path, must not be null
     * @return the SHA-256 hash as a lowercase hex string
     * @throws IOException if an I/O error occurs reading the file
     */
    public static String sha256(Path path) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        try (var is = Files.newInputStream(path)) {
            return sha256(is);
        }
    }

    /**
     * Returns the SHA-256 hex digest of the given input stream.
     *
     * @param inputStream the input stream to read, must not be null; stream is NOT closed by this method
     * @return the SHA-256 hash as a lowercase hex string
     * @throws IOException if an I/O error occurs reading from the stream
     */
    public static String sha256(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        return digestStream("SHA-256", inputStream);
    }

    // -- SHA-512 ---------------------------------------------------------

    /**
     * Returns the SHA-512 hex digest of the given string (UTF-8).
     *
     * @param input the string to hash, must not be null
     * @return the SHA-512 hash as a lowercase hex string
     */
    public static String sha512(String input) {
        Objects.requireNonNull(input, "input must not be null");
        return digest("SHA-512", input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    /**
     * Returns the SHA-512 hex digest of the given bytes.
     *
     * @param input the bytes to hash, must not be null
     * @return the SHA-512 hash as a lowercase hex string
     */
    public static String sha512(byte[] input) {
        Objects.requireNonNull(input, "input must not be null");
        return digest("SHA-512", input);
    }

    /**
     * Returns the SHA-512 hex digest of the given file.
     *
     * @param path the file path, must not be null
     * @return the SHA-512 hash as a lowercase hex string
     * @throws IOException if an I/O error occurs reading the file
     */
    public static String sha512(Path path) throws IOException {
        Objects.requireNonNull(path, "path must not be null");
        try (var is = Files.newInputStream(path)) {
            return sha512(is);
        }
    }

    /**
     * Returns the SHA-512 hex digest of the given input stream.
     *
     * @param inputStream the input stream to read, must not be null; stream is NOT closed by this method
     * @return the SHA-512 hash as a lowercase hex string
     * @throws IOException if an I/O error occurs reading from the stream
     */
    public static String sha512(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        return digestStream("SHA-512", inputStream);
    }

    // -- Generic ---------------------------------------------------------

    /**
     * Returns the hex digest of the given bytes using the named algorithm.
     *
     * @param algorithm the digest algorithm name (e.g. "MD5", "SHA-256"), must not be null
     * @param input     the bytes to hash, must not be null
     * @return the hash as a lowercase hex string
     * @throws IllegalArgumentException if the algorithm is unknown
     */
    public static String digest(String algorithm, byte[] input) {
        Objects.requireNonNull(algorithm, "algorithm must not be null");
        Objects.requireNonNull(input, "input must not be null");
        var md = messageDigest(algorithm);
        return HexFormat.of().formatHex(md.digest(input));
    }

    /**
     * Returns the hex digest of the given input stream using the named algorithm.
     *
     * @param algorithm    the digest algorithm name, must not be null
     * @param inputStream the input stream to read, must not be null; stream is NOT closed by this method
     * @return the hash as a lowercase hex string
     * @throws IOException              if an I/O error occurs reading from the stream
     * @throws IllegalArgumentException if the algorithm is unknown
     */
    public static String digestStream(String algorithm, InputStream inputStream)
            throws IOException {
        Objects.requireNonNull(algorithm, "algorithm must not be null");
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        var md = messageDigest(algorithm);
        byte[] buf = new byte[8192];
        int read;
        while ((read = inputStream.read(buf)) != -1) {
            md.update(buf, 0, read);
        }
        return HexFormat.of().formatHex(md.digest());
    }

    // -- Internal --------------------------------------------------------

    private static MessageDigest messageDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("unknown algorithm: " + algorithm, e);
        }
    }
}
