# exception

Categorized error codes for core-layer exceptions — numeric codes with human-readable defaults, useful for APIs, logging, and external integration.

## Classes

### `ErrorCode`

Enum of error codes grouped by category, with code ranges deliberately spaced for future expansion.

```java
// Throw with a code
throw new BusinessException(ErrorCode.INVALID_ARGUMENT, "email must not be blank");
throw new BusinessException(ErrorCode.NOT_FOUND);

// Resolve from external input (e.g. HTTP response, message queue)
ErrorCode ec = ErrorCode.fromCode(1001);  // → INVALID_ARGUMENT
```

| Category | Range | Constants |
|---|---|---|
| General | 1xxx | `UNKNOWN(-1)`, `INVALID_ARGUMENT(1001)`, `ILLEGAL_STATE(1002)`, `OUT_OF_RANGE(1003)`, `UNSUPPORTED(1004)` |
| Data | 2xxx | `NOT_FOUND(2001)`, `DUPLICATE(2002)`, `DATA_CONFLICT(2003)`, `DATA_INTEGRITY(2004)` |
| I/O | 3xxx | `IO_ERROR(3001)`, `FILE_NOT_FOUND(3002)`, `STREAM_ERROR(3003)` |
| Concurrency | 4xxx | `CONCURRENCY_ERROR(4001)`, `TIMEOUT(4002)`, `INTERRUPTED(4003)` |
| Configuration | 5xxx | `CONFIG_ERROR(5001)`, `MISSING_CONFIG(5002)` |

| Method | Returns | Notes |
|---|---|---|
| `code()` | `int` | The numeric error code |
| `defaultMessage()` | `String` | Human-readable default description |
| `fromCode(int)` | `ErrorCode` | Lookup by code; returns `null` if no match |
| `toString()` | `String` | Format: `INVALID_ARGUMENT(1001)` |

## Design notes

- **Spaced ranges** — gaps between codes (1001, 1002, …) allow inserting new codes later without renumbering.
- **`UNKNOWN` is `-1`** — deliberately distinct from valid codes so it stands out in logs.
- **`fromCode` returns `null` for unknown codes** — caller decides whether to default or reject.
- This enum is a dependency of `BusinessException` and related exception classes.
