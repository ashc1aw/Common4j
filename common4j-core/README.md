# common4j-core

The foundation of Common4j — zero-dependency utility library that fills real gaps in the JDK. Every tool solves a problem the standard library leaves open.

## Modules

| Module | Package | Purpose |
|--------|---------|---------|
| [codec](src/main/java/cc/ashclaw/common4j/core/codec/) | `cc.ashclaw.common4j.core.codec` | Hex dump for binary debugging |
| [collections](src/main/java/cc/ashclaw/common4j/core/collections/) | `cc.ashclaw.common4j.core.collections` | Batch partitioning, set ops, tree builder |
| [convert](src/main/java/cc/ashclaw/common4j/core/convert/) | `cc.ashclaw.common4j.core.convert` | String-to-type coercion with format patterns |
| [exception](src/main/java/cc/ashclaw/common4j/core/exception/) | `cc.ashclaw.common4j.core.exception` | Categorized error codes for APIs and logging |
| [id](src/main/java/cc/ashclaw/common4j/core/id/) | `cc.ashclaw.common4j.core.id` | Snowflake distributed IDs and UUID helpers |
| [io](src/main/java/cc/ashclaw/common4j/core/io/) | `cc.ashclaw.common4j.core.io` | Recursive delete, dir size, classpath resources |
| [stream](src/main/java/cc/ashclaw/common4j/core/stream/) | `cc.ashclaw.common4j.core.stream` | Iterator bridges and stream zipping |
| [text](src/main/java/cc/ashclaw/common4j/core/text/) | `cc.ashclaw.common4j.core.text` | Naming conventions, truncation, masking |
| [time](src/main/java/cc/ashclaw/common4j/core/time/) | `cc.ashclaw.common4j.core.time` | Smart parsing, day boundaries, date ranges, formatter constants |
| [validate](src/main/java/cc/ashclaw/common4j/core/validate/) | `cc.ashclaw.common4j.core.validate` | Precondition checks with fluent return values |

## Philosophy

**Don't wrap what the JDK already does well.** Use `java.util.HexFormat`, `Stream.toList()`, `Files.readString()`, `Objects.requireNonNull()` directly. Common4j-core only steps in where the JDK stops short — multi-format date parsing, recursive directory delete, Snowflake IDs, tree building from flat nodes.

Every class is a **pure utility** (private constructor, static methods only) unless it holds state by design (e.g. `Snowflake`, `DateRange`).

## Quick Examples

```java
// Smart date parsing — tries 6+ formats, no pattern needed
LocalDate d = DateUtils.parseDate("2024/06/15");

// Distributed ID generation
IdUtils.initialize(1);
long id = IdUtils.nextId();

// Build a tree from flat rows
List<TreeNode<Category>> forest = TreeBuilder.build(categories, Category::id, Category::parentId);

// One-liner IO
IoUtils.deleteRecursive(Path.of("/tmp/build"));
String json = IoUtils.resourceAsString("config/defaults.json");

// Set operations
Set<String> common = CollectionUtils.intersection(setA, setB, setC);

// Fluent validation
var name = ValidationUtils.notBlank(input, "name");
var count = ValidationUtils.positive(n, "count");

// String coercion with format
BigDecimal bd = ConvertUtils.to("1,234.56", BigDecimal.class, "#,##0.00");
```

## Dependencies

**Zero runtime dependencies.** Only `java.base` module. Tests use JUnit Jupiter 5.

**JDK 25+** — fully embraces modern Java: Records, sealed types, pattern matching, Virtual Threads.

## Maven

```xml
<dependency>
    <groupId>cc.ashclaw</groupId>
    <artifactId>common4j-core</artifactId>
    <version>2.0.0</version>
</dependency>
```

## Java Module

```java
module your.app {
    requires cc.ashclaw.common4j.core;
}
```

All 10 packages are exported.

## API Design Conventions

- **Null safety** — all public methods reject null arguments with `NullPointerException`
- **Unmodifiable returns** — collection-returning methods produce unmodifiable snapshots, safe to share
- **Fail fast** — invalid input throws immediately with a clear message; no silent defaults
- **Private constructors** — pure utility classes reject instantiation; stateful classes are `final`
- **Immutable formatters** — all `DateTimeFormatter` constants are thread-safe and reusable
- **No hidden I/O** — methods that read files or classpath resources are explicit about it
