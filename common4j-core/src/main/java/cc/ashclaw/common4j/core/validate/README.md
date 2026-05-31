# validate

Precondition validation utilities — fills gaps the JDK's `java.util.Objects` leaves open.

## Classes

### `ValidationUtils`

Boolean assertions and value constraints with clear exception types.

```java
// Boolean assertions — throw IAE / ISE
ValidationUtils.isTrue(x > 0, "x must be positive");
ValidationUtils.validState(!closed, "already closed");

// Value constraints — return the value for fluent inline assignment
var name  = ValidationUtils.notBlank(input, "name");
var items = ValidationUtils.notEmpty(list, "items");
var map   = ValidationUtils.notEmpty(config, "config");
var tags  = ValidationUtils.notEmpty(array, "tags");
var count = ValidationUtils.positive(n, "count");
var cap   = ValidationUtils.notNegative(n, "capacity");
```

| Method | Exception | Returns |
|---|---|---|
| `isTrue(expr, msg)` | IAE | void |
| `isTrue(expr, supplier)` | IAE | void |
| `validState(expr, msg)` | ISE | void |
| `validState(expr, supplier)` | ISE | void |
| `notBlank(value, name)` | NPE / IAE | `String` |
| `notEmpty(coll, name)` | NPE / IAE | `Collection<E>` |
| `notEmpty(map, name)` | NPE / IAE | `Map<K,V>` |
| `notEmpty(arr, name)` | NPE / IAE | `T[]` |
| `positive(value, name)` | IAE | `int` / `long` / `double` |
| `notNegative(value, name)` | IAE | `int` / `long` / `double` |

## Design notes

- `isTrue` / `validState` use distinct exception types (IAE vs ISE) so callers can catch selectively — argument validation vs. state checks.
- Value-returning methods enable fluent assignment: `var x = ValidationUtils.positive(raw, "x")`.
- Null checks throw `NullPointerException` (JDK convention); empty/invalid checks throw `IllegalArgumentException`.
- `Supplier<String>` overloads for lazy message computation — message is only computed when validation fails.
- `positive(double)` rejects NaN and negative zero; `notNegative(double)` accepts both.
- `ValidationUtils` constructor throws — pure utility class.
