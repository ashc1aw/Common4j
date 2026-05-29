# text

String utilities for common operations the JDK misses.

## Classes

### `MaskUtils`

Replaces the middle portion of a string, keeping prefix and suffix characters visible — useful for logging, display, and PII redaction.

```java
MaskUtils.mask("13812341234", 3, 4);        // "138****1234"
MaskUtils.mask("6222021234567890", 0, 4);   // "************7890"
MaskUtils.mask("secret", 1, 1);             // "s***t"
MaskUtils.mask("6222021234567890", 0, 4, 'X');  // "XXXXXXXXXXXX7890"
```

| Method | Returns | Notes |
|---|---|---|
| `mask(value, keepPrefix, keepSuffix)` | `String` | Uses `*` as mask character |
| `mask(value, keepPrefix, keepSuffix, maskChar)` | `String` | Custom mask character |

## Design notes

- `null` input returns `null` — safe to chain with `Optional`.
- String shorter than `keepPrefix + keepSuffix` is returned as-is — nothing meaningful to mask.
- Negative keep values throw `IllegalArgumentException`.
- `MaskUtils` constructor throws — pure utility class.
