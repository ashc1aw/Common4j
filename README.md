# Common4j

A curated set of JDK 25 libraries — each module fills a real gap where the standard library or ecosystem leaves you writing boilerplate.

> **Pursue excellence** — every tool solves a real problem. Small & refined.

## Modules

| Module | Artifact | Purpose | Dependencies |
|--------|----------|---------|--------------|
| [common4j-core](#common4j-core) | `common4j-core` | Foundation: codec, collections, convert, exception, id, io, stream, text, time, validate | None |
| [common4j-web](#common4j-web) | `common4j-web` | Web layer: API response envelope, pagination, result codes | None |
| [common4j-crypto](#common4j-crypto) | `common4j-crypto` | Digest and AES-256-GCM encryption | None |
| [common4j-poi](#common4j-poi) | `common4j-poi` | Excel & Word read/write/template-fill via Apache POI | POI, `common4j-core` |
| [common4j-retry](#common4j-retry) | `common4j-retry` | Exponential backoff retry with jitter | None |
| [common4j-bom](#common4j-bom) | `common4j-bom` | Bill of materials — unified version management | None |

## common4j-core

The foundation module — 10 packages, zero dependencies beyond `java.base`.

| Package | Key Classes | Purpose |
|---------|-------------|---------|
| [codec](common4j-core/src/main/java/cc/ashclaw/common4j/core/codec/) | `HexUtils` | Multi-line hex dump for binary debugging |
| [collections](common4j-core/src/main/java/cc/ashclaw/common4j/core/collections/) | `CollectionUtils`, `TreeBuilder` | Partition, set operations, flat→tree |
| [convert](common4j-core/src/main/java/cc/ashclaw/common4j/core/convert/) | `ConvertUtils` | String→type coercion with format patterns |
| [exception](common4j-core/src/main/java/cc/ashclaw/common4j/core/exception/) | `ErrorCode` | Categorized error codes for API/logging |
| [id](common4j-core/src/main/java/cc/ashclaw/common4j/core/id/) | `IdUtils`, `Snowflake` | Distributed Snowflake IDs, UUID helpers |
| [io](common4j-core/src/main/java/cc/ashclaw/common4j/core/io/) | `IoUtils` | Recursive delete, directory size, classpath resources |
| [stream](common4j-core/src/main/java/cc/ashclaw/common4j/core/stream/) | `StreamUtils` | Iterator bridges, zip |
| [text](common4j-core/src/main/java/cc/ashclaw/common4j/core/text/) | `StringUtils`, `MaskUtils` | Naming conventions, case, truncate, PII masking |
| [time](common4j-core/src/main/java/cc/ashclaw/common4j/core/time/) | `DateUtils`, `DateConstants`, `DateRange` | Smart parsing, formatter constants, ranges |
| [validate](common4j-core/src/main/java/cc/ashclaw/common4j/core/validate/) | `ValidationUtils` | Precondition checks with fluent return values |

### Quick examples

```java
// Smart date parsing — tries multiple formats
LocalDate d = DateUtils.parseDate("2024/06/15");
LocalDateTime dt = DateUtils.parseDateTime("2024-06-15 10:30:00");

// Type coercion — from strings with format patterns
int port = ConvertUtils.to("8080", int.class);
BigDecimal bd = ConvertUtils.to("1,234.56", BigDecimal.class, "#,##0.00");

// Naming conventions
String dbCol = StringUtils.camelToSnake("userName");       // "user_name"
String field = StringUtils.snakeToCamel("user_name");      // "userName"

// Snowflake distributed IDs
IdUtils.initialize(1);
long id = IdUtils.nextId();

// Precondition validation with fluent assignment
var name = ValidationUtils.notBlank(input, "name");
var count = ValidationUtils.positive(n, "count");

// PII masking
String masked = MaskUtils.mask("13812341234", 3, 4);       // "138****1234"

// Recursive I/O
IoUtils.deleteRecursive(Path.of("/tmp/build"));
String sql = IoUtils.resourceAsString(MyDao.class, "init.sql");
```

## common4j-web

API response envelope, pagination, and HTTP-aligned result codes. Zero dependencies beyond `java.base`.

```java
// Unified API response
R<User> ok = ResultCode.SUCCESS.toResponse(user);
R<Void> fail = ResultCode.NOT_FOUND.toResponse("User 42 not found");

// Pagination
var pq = new PageQuery(3, 20);                    // page 3, 20 per page
PageResult<User> pr = PageResult.of(users, total, pq);
PageResult<UserDto> dtos = pr.map(User::toDto);   // transform in one line

// Null-safe chaining
r.map(User::toDto).map(UserDto::toVo);            // passes through on failure
```

| Class | Type | Purpose |
|-------|------|---------|
| `R<T>` | record | Response envelope: `code`, `message`, `data` |
| `ResultCode` | enum | HTTP-aligned codes (200, 400, 401, 403, 404, …) |
| `PageQuery` | record | 1-based page, size clamped to [1, 100] |
| `PageResult<T>` | record | Records + total + navigation (`hasNext`, `hasPrev`, `totalPages`) |

## common4j-crypto

Digest and AES-256-GCM encryption — safe defaults, zero external dependencies.

```java
// Hashing
var hash = DigestUtils.sha256("hello");
var fileHash = DigestUtils.sha256(Path.of("/data.bin"));

// AES-256-GCM — generate a key, encrypt, decrypt
var key = AesUtils.generateKeyBase64();
var ciphertext = AesUtils.encrypt("sensitive data", key);
var plaintext = AesUtils.decrypt(ciphertext, key);
```

| Class | Purpose |
|-------|---------|
| `DigestUtils` | MD5, SHA-1, SHA-256, SHA-512 — string, file, stream, generic |
| `AesUtils` | AES-256-GCM encrypt/decrypt with random IV, GCM authentication |

## common4j-poi

Excel & Word read/write/template-fill built on Apache POI — annotations for the simple cases, fluent DSL for the complex ones.

```java
// Excel — read/write with annotations
List<User> users = Excel.read(file).toList(User.class);
Excel.write(users).to(outputStream);

// Excel — complex layouts
Excel.write()
    .sheet(def -> def.name("Report")
        .title("Q4 Report", 4)
        .complexHeader(topHeaders, subHeaders)
        .mergeSame("Dept")
        .style(StylePreset.CORPORATE), rows)
    .to(outputStream);

// Excel — template fill
Excel.fromTemplate(templateStream).fill("Users", users, 2).to(outputStream);

// Word — extract form fields
Contract c = Word.read(docxFile).toBean(Contract.class);

// Word — template fill with table row expansion
Word.fromTemplate(templateStream)
    .put("name", "Alice")
    .table("items", itemList)
    .to(outputStream);
```

## common4j-retry

Lightweight retry with exponential backoff and jitter — zero dependencies, Virtual Thread friendly.

```java
// Default config: 3 attempts, 200ms initial, 2x backoff, 20% jitter
var retryer = new Retryer(RetryConfig.DEFAULTS);
String result = retryer.execute(() -> unstableService.fetch());

// Custom config
var config = RetryConfig.builder()
    .maxAttempts(5)
    .initialDelay(500)
    .jitter(0.3)
    .retryOn(IOException.class, TimeoutException.class)
    .build();

// Interface proxy — one-liner wrapping
var client = Retryer.proxy(ApiClient.class, realClient, config);
client.fetch();  // auto-retries on failure
```

## common4j-bom

Bill of materials — import once, manage all Common4j versions centrally.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>cc.ashclaw</groupId>
            <artifactId>common4j-bom</artifactId>
            <version>2.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## Requirements

- **JDK 25+**
- Modules with external dependencies: only `common4j-poi` (Apache POI 5.4.0)
- All other modules have zero runtime dependencies beyond `java.base`

## Philosophy

1. **Fill real gaps** — if the JDK or a ubiquitous library already solves it well, don't reinvent it.
2. **Small & refined** — each class earns its place. No placeholder utilities for hypothetical use cases.
3. **From the user's perspective** — the API should read like what the user wants to accomplish, not like the underlying implementation.
4. **Zero surprises** — sensible defaults, null-safe, immutable where possible, thread-safe where it matters.

## API Conventions

- Pure utility classes have private constructors that throw — never instantiated.
- Public APIs reject `null` with `NullPointerException`; behavior documented per method.
- Records preferred for data carriers; enums for fixed constants.
- Each package has a `README.md` and `package-info.java`.
- Java Platform Module System (`module-info.java`) on every module.
