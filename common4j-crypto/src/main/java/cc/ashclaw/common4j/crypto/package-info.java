/**
 * Crypto utilities — digest hashing and AES symmetric encryption.
 *
 * <p>{@link cc.ashclaw.common4j.crypto.DigestUtils} for checksums and
 * fingerprinting; {@link cc.ashclaw.common4j.crypto.AesUtils} for
 * AES-256-GCM encryption with safe defaults. Zero external dependencies.
 *
 * <p>This package is NOT for password hashing — use bcrypt, scrypt, or
 * argon2 for password storage.
 */
package cc.ashclaw.common4j.crypto;
