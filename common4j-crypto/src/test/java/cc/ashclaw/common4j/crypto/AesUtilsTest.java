package cc.ashclaw.common4j.crypto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.crypto.SecretKey;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AesUtils")
class AesUtilsTest {

    @Nested
    @DisplayName("encrypt / decrypt roundtrip")
    class Roundtrip {

        @Test
        @DisplayName("encrypt then decrypt returns original string")
        void basic() {
            var key = AesUtils.generateKey();
            var ciphertext = AesUtils.encrypt("hello", key);
            assertEquals("hello", AesUtils.decrypt(ciphertext, key));
        }

        @Test
        @DisplayName("base64 key convenience overloads")
        void base64Key() {
            var keyBase64 = AesUtils.generateKeyBase64();
            var ciphertext = AesUtils.encrypt("hello", keyBase64);
            assertEquals("hello", AesUtils.decrypt(ciphertext, keyBase64));
        }

        @Test
        @DisplayName("empty string")
        void emptyString() {
            var key = AesUtils.generateKey();
            var ciphertext = AesUtils.encrypt("", key);
            assertEquals("", AesUtils.decrypt(ciphertext, key));
        }

        @Test
        @DisplayName("unicode and emoji")
        void unicode() {
            var key = AesUtils.generateKey();
            var plaintext = "Hello 世界 🌍";
            var ciphertext = AesUtils.encrypt(plaintext, key);
            assertEquals(plaintext, AesUtils.decrypt(ciphertext, key));
        }

        @Test
        @DisplayName("long string")
        void longString() {
            var key = AesUtils.generateKey();
            var plaintext = "x".repeat(10_000);
            var ciphertext = AesUtils.encrypt(plaintext, key);
            assertEquals(plaintext, AesUtils.decrypt(ciphertext, key));
        }
    }

    @Nested
    @DisplayName("encrypt produces different outputs")
    class NonDeterministic {

        @Test
        @DisplayName("same plaintext twice → different ciphertexts (random IV)")
        void randomIv() {
            var key = AesUtils.generateKey();
            var c1 = AesUtils.encrypt("hello", key);
            var c2 = AesUtils.encrypt("hello", key);
            assertNotEquals(c1, c2);
        }

        @Test
        @DisplayName("both decrypt to the same value")
        void bothDecryptSame() {
            var key = AesUtils.generateKey();
            var c1 = AesUtils.encrypt("hello", key);
            var c2 = AesUtils.encrypt("hello", key);
            assertEquals("hello", AesUtils.decrypt(c1, key));
            assertEquals("hello", AesUtils.decrypt(c2, key));
        }
    }

    @Nested
    @DisplayName("decrypt with wrong key")
    class WrongKey {

        @Test
        @DisplayName("throws IAE with wrong key")
        void wrongKeyFails() {
            var key1 = AesUtils.generateKey();
            var key2 = AesUtils.generateKey();
            var ciphertext = AesUtils.encrypt("hello", key1);
            assertThrows(IllegalArgumentException.class,
                    () -> AesUtils.decrypt(ciphertext, key2));
        }
    }

    @Nested
    @DisplayName("tampered ciphertext")
    class Tampered {

        @Test
        @DisplayName("throws IAE when ciphertext is modified (GCM auth)")
        void tamperedFails() {
            var key = AesUtils.generateKey();
            var ciphertext = AesUtils.encrypt("hello", key);
            var tampered = ciphertext.substring(0, ciphertext.length() - 2) + "==";
            assertThrows(IllegalArgumentException.class,
                    () -> AesUtils.decrypt(tampered, key));
        }

        @Test
        @DisplayName("throws IAE when ciphertext is too short")
        void tooShort() {
            var key = AesUtils.generateKey();
            assertThrows(IllegalArgumentException.class,
                    () -> AesUtils.decrypt("abc", key));
        }
    }

    @Nested
    @DisplayName("key generation")
    class KeyGeneration {

        @Test
        @DisplayName("generated key is 256-bit AES")
        void keyProperties() {
            var key = AesUtils.generateKey();
            assertEquals("AES", key.getAlgorithm());
            assertEquals(32, key.getEncoded().length); // 256 bits
        }

        @Test
        @DisplayName("generateKeyBase64 returns valid base64")
        void base64Format() {
            var keyBase64 = AesUtils.generateKeyBase64();
            var decoded = Base64.getDecoder().decode(keyBase64);
            assertEquals(32, decoded.length);
        }

        @Test
        @DisplayName("keyFromBase64 returns equivalent key")
        void keyBase64Roundtrip() {
            var key = AesUtils.generateKey();
            var base64 = Base64.getEncoder().encodeToString(key.getEncoded());
            var loaded = AesUtils.keyFromBase64(base64);
            var ciphertext = AesUtils.encrypt("test", loaded);
            assertEquals("test", AesUtils.decrypt(ciphertext, key));
        }

        @Test
        @DisplayName("generateKeyBase64 + keyFromBase64 works end to end")
        void endToEndKeyBase64() {
            var base64 = AesUtils.generateKeyBase64();
            var key = AesUtils.keyFromBase64(base64);
            var ciphertext = AesUtils.encrypt("data", key);
            assertEquals("data", AesUtils.decrypt(ciphertext, base64));
        }

        @Test
        @DisplayName("two generated keys are different")
        void keysAreRandom() {
            assertNotEquals(
                    AesUtils.generateKeyBase64(),
                    AesUtils.generateKeyBase64());
        }
    }

    @Nested
    @DisplayName("null handling")
    class NullHandling {

        @Test
        @DisplayName("encrypt(String, SecretKey) throws NPE for null plaintext")
        void encryptNullPlaintext() {
            assertThrows(NullPointerException.class,
                    () -> AesUtils.encrypt((String) null, AesUtils.generateKey()));
        }

        @Test
        @DisplayName("encrypt(String, SecretKey) throws NPE for null key")
        void encryptNullKey() {
            assertThrows(NullPointerException.class,
                    () -> AesUtils.encrypt("hello", (SecretKey) null));
        }

        @Test
        @DisplayName("decrypt(String, SecretKey) throws NPE for null ciphertext")
        void decryptNullCiphertext() {
            assertThrows(NullPointerException.class,
                    () -> AesUtils.decrypt(null, AesUtils.generateKey()));
        }

        @Test
        @DisplayName("decrypt(String, SecretKey) throws NPE for null key")
        void decryptNullKey() {
            assertThrows(NullPointerException.class,
                    () -> AesUtils.decrypt("abc", (SecretKey) null));
        }

        @Test
        @DisplayName("keyFromBase64 throws NPE for null")
        void keyFromBase64Null() {
            assertThrows(NullPointerException.class,
                    () -> AesUtils.keyFromBase64(null));
        }
    }

    @Nested
    @DisplayName("invalid key base64")
    class InvalidKey {

        @Test
        @DisplayName("throws IAE when key is wrong length")
        void wrongKeyLength() {
            // 16 bytes (128 bits) instead of 32
            var shortKey = Base64.getEncoder().encodeToString(new byte[16]);
            var e = assertThrows(IllegalArgumentException.class,
                    () -> AesUtils.keyFromBase64(shortKey));
            assertTrue(e.getMessage().contains("32 bytes"));
        }
    }

    @Nested
    @DisplayName("constructor")
    class Constructor {

        @Test
        @DisplayName("should throw UnsupportedOperationException")
        void shouldThrow() throws Exception {
            var ctor = AesUtils.class.getDeclaredConstructor();
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
