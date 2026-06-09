package cc.ashclaw.common4j.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;

/**
 * AES-256-GCM encryption utilities — safe defaults wrapped behind a
 * two-line API.
 *
 * Usage example:
 * <pre>{@code
 * // Generate and store a key
 * var keyBase64 = AesUtils.generateKeyBase64();  // save to config
 *
 * // Encrypt / decrypt
 * var ciphertext = AesUtils.encrypt("hello", keyBase64);
 * var plaintext  = AesUtils.decrypt(ciphertext, keyBase64);
 * }</pre>
 *
 * <p>Encrypted output is base64: the 12-byte random IV followed by the
 * GCM ciphertext (which includes the 128-bit authentication tag).
 * Every call to {@code encrypt} produces a different output for the same
 * plaintext — the random IV ensures this.
 */
public final class AesUtils {

    private static final String ALGORITHM   = "AES/GCM/NoPadding";
    private static final int    GCM_IV_BYTES    = 12;
    private static final int    GCM_TAG_BITS     = 128;
    private static final int    KEY_SIZE_BITS    = 256;

    private AesUtils() {
        throw new UnsupportedOperationException("utility class");
    }

    // -- Key generation --------------------------------------------------

    /**
     * Generates a new random 256-bit AES key.
     *
     * @return a new {@link SecretKey} suitable for AES-256-GCM encryption/decryption
     */
    public static SecretKey generateKey() {
        try {
            var keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(KEY_SIZE_BITS);
            return keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("AES not available in this JDK", e);
        }
    }

    /**
     * Generates a new random 256-bit AES key and returns it as a base64 string.
     *
     * @return base64-encoded 256-bit key (44 characters)
     */
    public static String generateKeyBase64() {
        return Base64.getEncoder().encodeToString(generateKey().getEncoded());
    }

    /**
     * Loads a key from a base64-encoded string previously returned by
     * {@link #generateKeyBase64()}.
     *
     * @param base64 the base64-encoded key, must not be null, must be exactly 32 bytes when decoded
     * @return the reconstructed {@link SecretKey}
     * @throws NullPointerException     if base64 is null
     * @throws IllegalArgumentException if the decoded key length is not 32 bytes (256 bits)
     */
    public static SecretKey keyFromBase64(String base64) {
        Objects.requireNonNull(base64, "base64 must not be null");
        byte[] decoded = Base64.getDecoder().decode(base64);
        if (decoded.length != 32) {
            throw new IllegalArgumentException(
                    "key must be 256 bits (32 bytes), got %d bytes".formatted(decoded.length));
        }
        return new SecretKeySpec(decoded, "AES");
    }

    // -- Encrypt (String → base64) ---------------------------------------

    /**
     * Encrypts the plaintext and returns a base64-encoded ciphertext
     * (random IV + GCM ciphertext).
     *
     * @param plaintext the UTF-8 string to encrypt, must not be null
     * @param key       the secret key, must not be null
     * @return base64-encoded ciphertext (IV + encrypted data + auth tag)
     */
    public static String encrypt(String plaintext, SecretKey key) {
        Objects.requireNonNull(plaintext, "plaintext must not be null");
        Objects.requireNonNull(key, "key must not be null");
        return encrypt(plaintext.getBytes(StandardCharsets.UTF_8), key);
    }

    /**
     * Convenience overload that accepts a base64-encoded key.
     *
     * @param plaintext the UTF-8 string to encrypt, must not be null
     * @param keyBase64 the base64-encoded key from {@link #generateKeyBase64()}, must not be null
     * @return base64-encoded ciphertext (IV + encrypted data + auth tag)
     */
    public static String encrypt(String plaintext, String keyBase64) {
        Objects.requireNonNull(plaintext, "plaintext must not be null");
        Objects.requireNonNull(keyBase64, "keyBase64 must not be null");
        return encrypt(plaintext, keyFromBase64(keyBase64));
    }

    /**
     * Encrypts the plaintext bytes and returns a base64-encoded ciphertext
     * (random IV + GCM ciphertext).
     *
     * @param plaintext the bytes to encrypt, must not be null
     * @param key       the secret key, must not be null
     * @return base64-encoded ciphertext (IV + encrypted data + auth tag)
     */
    public static String encrypt(byte[] plaintext, SecretKey key) {
        Objects.requireNonNull(plaintext, "plaintext must not be null");
        Objects.requireNonNull(key, "key must not be null");
        try {
            var cipher = Cipher.getInstance(ALGORITHM);
            var iv = new byte[GCM_IV_BYTES];
            SecureRandom.getInstanceStrong().nextBytes(iv);
            var spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, spec);
            byte[] ciphertext = cipher.doFinal(plaintext);
            byte[] combined = Arrays.copyOf(iv, GCM_IV_BYTES + ciphertext.length);
            System.arraycopy(ciphertext, 0, combined, GCM_IV_BYTES, ciphertext.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("encryption failed: " + e.getMessage(), e);
        }
    }

    // -- Decrypt (base64 → String) ---------------------------------------

    /**
     * Decrypts a base64-encoded ciphertext previously produced by
     * {@link #encrypt}.
     *
     * @param ciphertext the base64-encoded ciphertext from {@link #encrypt}, must not be null
     * @param key        the secret key used during encryption, must not be null
     * @return the decrypted UTF-8 string
     * @throws IllegalArgumentException if the ciphertext is malformed or decryption fails
     */
    public static String decrypt(String ciphertext, SecretKey key) {
        Objects.requireNonNull(ciphertext, "ciphertext must not be null");
        Objects.requireNonNull(key, "key must not be null");
        return new String(decryptToBytes(ciphertext, key), StandardCharsets.UTF_8);
    }

    /**
     * Convenience overload that accepts a base64-encoded key.
     *
     * @param ciphertext the base64-encoded ciphertext from {@link #encrypt}, must not be null
     * @param keyBase64  the base64-encoded key from {@link #generateKeyBase64()}, must not be null
     * @return the decrypted UTF-8 string
     * @throws IllegalArgumentException if the ciphertext is malformed or decryption fails
     */
    public static String decrypt(String ciphertext, String keyBase64) {
        Objects.requireNonNull(ciphertext, "ciphertext must not be null");
        Objects.requireNonNull(keyBase64, "keyBase64 must not be null");
        return decrypt(ciphertext, keyFromBase64(keyBase64));
    }

    /**
     * Decrypts a base64-encoded ciphertext and returns the raw plaintext bytes.
     *
     * @param ciphertext the base64-encoded ciphertext from {@link #encrypt}, must not be null
     * @param key        the secret key used during encryption, must not be null
     * @return the decrypted raw bytes
     * @throws IllegalArgumentException if the ciphertext is too short or decryption fails
     */
    public static byte[] decryptToBytes(String ciphertext, SecretKey key) {
        Objects.requireNonNull(ciphertext, "ciphertext must not be null");
        Objects.requireNonNull(key, "key must not be null");
        try {
            byte[] combined = Base64.getDecoder().decode(ciphertext);
            if (combined.length < GCM_IV_BYTES + 1) {
                throw new IllegalArgumentException("ciphertext too short");
            }
            var iv = new byte[GCM_IV_BYTES];
            System.arraycopy(combined, 0, iv, 0, GCM_IV_BYTES);
            byte[] encrypted = new byte[combined.length - GCM_IV_BYTES];
            System.arraycopy(combined, GCM_IV_BYTES, encrypted, 0, encrypted.length);
            var cipher = Cipher.getInstance(ALGORITHM);
            var spec = new GCMParameterSpec(GCM_TAG_BITS, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, spec);
            return cipher.doFinal(encrypted);
        } catch (GeneralSecurityException e) {
            throw new IllegalArgumentException(
                    "decryption failed — wrong key or tampered data", e);
        }
    }
}
