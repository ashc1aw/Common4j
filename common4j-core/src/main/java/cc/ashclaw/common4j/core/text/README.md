# text

String utilities for common operations the JDK misses.

## Classes

### `StringUtils`

Naming conventions, case helpers, truncation, and null-safe defaults.

```java
// Naming conventions
StringUtils.camelToSnake("userName");   // "user_name"
StringUtils.snakeToCamel("user_name");  // "userName"

// Case
StringUtils.capitalize("hello");        // "Hello"
StringUtils.uncapitalize("Hello");      // "hello"

// Truncation
StringUtils.truncate("Hello World", 8); // "Hello..."

// Null-safe defaults
StringUtils.defaultIfBlank(input, "N/A");
StringUtils.defaultString(null);        // ""
StringUtils.defaultString(null, "N/A"); // "N/A"
```

| Method | Returns | Notes |
|---|---|---|
| `camelToSnake(camel)` | `String` | `"HTTPHeader"` → `"http_header"`, preserves digits: `"json2Xml"` → `"json2_xml"` |
| `snakeToCamel(snake)` | `String` | `"user_name"` → `"userName"`, preserves leading underscores: `"_private"` → `"_private"` |
| `capitalize(value)` | `String` | First char to uppercase; already capitalized is a no-op |
| `uncapitalize(value)` | `String` | First char to lowercase; already lowercase is a no-op |
| `truncate(value, maxLen)` | `String` | Appends `"..."` if truncated; `maxLen` must be ≥ 3 |
| `defaultIfBlank(value, def)` | `String` | Returns `def` if null, empty, or whitespace |
| `defaultString(value)` | `String` | Returns `""` if null |
| `defaultString(value, def)` | `String` | Returns `def` if null |

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

- All methods return `null` for `null` input — safe to chain with `Optional`.
- `StringUtils` constructor throws — pure utility class.
- `MaskUtils` constructor throws — pure utility class.
- `truncate` throws `IllegalArgumentException` if `maxLen < 3` (no room for ellipsis).
- `camelToSnake` and `snakeToCamel` are symmetric for standard inputs (round-trip safe).
- Negative keep values in `MaskUtils` throw `IllegalArgumentException`.
