# convert

String-to-type coercion for the types most common in configuration parsing, file import, and annotation-driven mapping.

## Classes

### `ConvertUtils`

One-line conversion from strings to strongly-typed values, with optional format patterns.

```java
// Typed convenience
int port = ConvertUtils.to("8080", int.class);
LocalDate d = ConvertUtils.to("2024/06/15", LocalDate.class, "yyyy/MM/dd");
BigDecimal bd = ConvertUtils.to("1,234.56", BigDecimal.class, "#,##0.00");

// Framework-style dispatch (target type known only at runtime)
Object value = ConvertUtils.coerce(rawString, field.getType(), annotation.format());
```

| Method | Returns | Notes |
|---|---|---|
| `to(value, type)` | `<T>` | Sensible defaults — `LocalDate.parse()`, `Integer.parseInt()`, etc. |
| `to(value, type, format)` | `<T>` | Number/date parsing with `DecimalFormat` or `DateTimeFormatter` pattern |
| `coerce(value, type, format)` | `Object` | Same as `to()` but returns `Object` — for reflection-based dispatch |
| `defaultValue(type)` | `Object` | `null` for reference types, `0` for numeric primitives, `false` for boolean |

### Supported types

| Category | Types |
|---|---|
| String | `String` (passthrough) |
| Integer | `int`, `Integer` |
| Long | `long`, `Long` |
| Double | `double`, `Double` |
| Decimal | `BigDecimal` |
| Boolean | `boolean`, `Boolean` — `"true"`, `"1"`, `"yes"` (case-insensitive) |
| Date | `LocalDate`, `LocalDateTime`, `java.util.Date` |
| Blank | Returns `defaultValue(type)` for null/empty/whitespace |

### Format examples

```java
// Numbers — DecimalFormat patterns
ConvertUtils.to("1,234.56", BigDecimal.class, "#,##0.00");  // 1234.56
ConvertUtils.to("85%", double.class, "0%");                   // 0.85

// Dates — DateTimeFormatter patterns
ConvertUtils.to("2024/06/15", LocalDate.class, "yyyy/MM/dd");
ConvertUtils.to("2024/06/15 10:30", LocalDateTime.class, "yyyy/MM/dd HH:mm");

// No format = ISO / standard parsing
ConvertUtils.to("2024-06-15", LocalDate.class);               // ISO_LOCAL_DATE
ConvertUtils.to("2024-06-15T10:30:00", LocalDateTime.class);  // ISO_LOCAL_DATE_TIME
```

## Design notes

- **Format is always optional** — omit for standard ISO/plain parsing; provide for custom patterns.
- **Blank strings return zero/empty** — `""` → `0`, `false`, or `null` depending on type. Never throws on blank input.
- **Unparseable values throw `IllegalArgumentException`** with the offending string in the message.
- `ConvertUtils` constructor throws — pure utility class.
- Used internally by `common4j-poi` (`FormExtractor`, `ExcelReader`) to eliminate duplicated coercion logic.
