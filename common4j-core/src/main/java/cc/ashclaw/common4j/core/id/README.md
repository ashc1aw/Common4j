# id

Distributed unique ID generation — Snowflake and UUID.

## Classes

### `IdUtils`

The primary entry point for ID generation.

**Snowflake**

| Method | Description |
|--------|-------------|
| `initialize(workerId)` | Configure the default Snowflake worker ID (call once before use) |
| `nextId()` | Next snowflake ID as `long` |
| `nextString()` | Next snowflake ID as decimal `String` |
| `getSnowflake()` | Get the underlying `Snowflake` instance |

Worker ID is resolved in order: explicit `initialize()` → system property `common4j.snowflake.workerId` → env var `COMMON4J_SNOWFLAKE_WORKER_ID` → default `1`.

**UUID**

| Method | Description |
|--------|-------------|
| `randomUUID()` | Random UUID (type 4) as 36-char string with dashes |
| `randomUUIDSimple()` | Random UUID (type 4) as 32-char string without dashes |
| `uuidFromString(text)` | Lenient parse accepting both 32- and 36-char formats |

### `Snowflake`

A 64-bit distributed unique ID generator based on Twitter's Snowflake algorithm.

```
1 bit (unused)
41 bits — milliseconds since epoch (default 2020-01-01T00:00:00Z)
10 bits — worker ID (0–1023)
12 bits — per-millisecond sequence (0–4095)
```

Capable of ~409,600 IDs/second per worker. Thread-safe via `synchronized`.

**Construction**

```java
var sf = new Snowflake(1);                       // default epoch (2020-01-01)
var sf = new Snowflake(1, 1700000000000L);       // custom epoch
```

**Generation**

```java
long id = sf.nextId();      // 64-bit ID
String s = sf.nextString(); // decimal string
```

**Decomposition**

```java
Snowflake.IdInfo info = sf.parse(id);
info.timestamp();   // Instant the ID was created
info.workerId();    // worker that created it
info.sequence();    // per-millisecond sequence number

// Static extraction (assumes default epoch)
Instant ts = Snowflake.extractTimestamp(id);
long wid   = Snowflake.extractWorkerId(id);
long seq   = Snowflake.extractSequence(id);
```

**Clock rollback**

The generator uses a monotonic clock guard — if the system clock moves backwards (e.g. NTP adjustment), it clamps to the last observed timestamp and continues incrementing the sequence. No duplicates, no crash. Only a sustained rollback at maximum throughput would cause blocking.

## Design notes

- All generated snowflake IDs are positive (sign bit unused), safe for `BIGINT UNSIGNED`.
- `IdUtils` caches a single `Snowflake` instance per JVM; a second call to `initialize()` is a no-op.
- UUID format is `xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx` (standard) or `xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx` (simple).
