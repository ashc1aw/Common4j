# crypto

Digest and AES encryption utilities — safe defaults, zero external dependencies.

## Classes

### `DigestUtils`

One-line hashing for checksums, cache keys, and non-security fingerprinting.

```java
// String hashing
var hex = DigestUtils.sha256("hello");
var md5  = DigestUtils.md5(input);   // for checksums only, not security

// File / stream hashing
var fileHash = DigestUtils.sha256(Path.of("/data.bin"));
var streamHash = DigestUtils.sha256(inputStream);

// Generic algorithm
var hash = DigestUtils.digest("SHA-512", bytes);
```

| Method | Returns | Notes |
|---|---|---|
| `md5(input)` | hex `String` | Non-cryptographic — checksums, cache keys only |
| `sha1(input)` | hex `String` | Legacy; prefer `sha256` |
| `sha256(input)` | hex `String` | Recommended general-purpose hash |
| `sha512(input)` | hex `String` | Longer, slower |
| `digest(algo, bytes)` | hex `String` | Any `MessageDigest` algorithm |

All `String` overloads use UTF-8. `Path` and `InputStream` overloads for `sha256` and `sha512`.

### `AesUtils`

AES-256-GCM encryption — safe defaults wrapped behind a two-line API.

```java
// Generate and store a key
var keyBase64 = AesUtils.generateKeyBase64();   // save to config/env

// Encrypt
var ciphertext = AesUtils.encrypt("hello", keyBase64);

// Decrypt
var plaintext = AesUtils.decrypt(ciphertext, keyBase64);
```

| Method | Returns | Notes |
|---|---|---|
| `generateKey()` | `SecretKey` | Random 256-bit AES key |
| `generateKeyBase64()` | `String` | Key as base64 — safe to store |
| `keyFromBase64(s)` | `SecretKey` | Load a stored key |
| `encrypt(plaintext, key)` | base64 `String` | IV prepended, GCM-authenticated |
| `decrypt(ciphertext, key)` | `String` | Verifies GCM tag, throws on tampering |

## Design notes

- **Zero dependencies** — pure JDK, no Bouncy Castle needed.
- **GCM only** — AES-GCM is the standard authenticated mode. No ECB, no CBC, no footguns.
- **Random IV every encrypt** — same plaintext produces different ciphertext each call.
- **Tamper detection** — GCM tag verification fails with `IllegalArgumentException` on wrong key or modified ciphertext.
- **Not for passwords** — `DigestUtils` uses fast hashes; use bcrypt/scrypt/argon2 for password storage.
- Both classes are pure utilities — constructors throw.
