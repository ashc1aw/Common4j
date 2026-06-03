# Excel

Annotation-driven Excel read/write with complex headers, cell merging, template filling, and style presets.

## Quick Start

```java
// Read — annotation-driven
List<User> users = Excel.read(file).toList(User.class);

// Write — one-liner
Excel.write(users).to(outputStream);

// Template fill
Excel.fromTemplate(templateStream)
    .fill("Users", users, 2)   // data starts at row 2
    .to(outputStream);
```

## Read

`Excel.read()` returns an `ExcelReader` with a fluent configuration chain.

```java
// From file
List<User> users = Excel.read(Path.of("data.xlsx")).toList(User.class);

// From stream
var result = Excel.read(inputStream)
    .sheet("Employees")      // by name, or .sheet(0) by index
    .headerRow(0)            // which row contains headers (default 0)
    .dataStartRow(1)         // first data row (default 1)
    .dataEndRow(100)         // last data row (default -1 = auto)
    .toResult(User.class);   // get both data and parse errors

if (!result.isSuccess()) {
    result.errors().forEach(e -> log.warn("Row {}: {}", e.row(), e.message()));
}
```

### Read output formats

| Method | Returns | Description |
|--------|---------|-------------|
| `toList(Class)` | `List<T>` | Typed list; closes workbook after read |
| `toResult(Class)` | `ReadResult<T>` | Typed list + `List<ParseError>` |
| `toMaps()` | `List<Map<String, String>>` | Header → cell text (formatted) |
| `toRawMaps()` | `List<Map<String, Object>>` | Header → typed cell value |

### Data class mapping

Works with both records and POJOs:

```java
record User(
    @ExcelColumn(index = 0, header = "Name")
    String name,

    @ExcelColumn(index = 1, header = "Balance", format = "#,##0.00")
    BigDecimal balance,

    @ExcelColumn(header = "Birthday")
    LocalDate birthday
) {}
```

Column matching priority: explicit `index` → `header` text match → field declaration order. If the class has no `@ExcelColumn` annotations at all, fields are mapped positionally.

### Supported types

`String`, `int`/`Integer`, `long`/`Long`, `double`/`Double`, `BigDecimal`, `boolean`/`Boolean`, `LocalDate`, `LocalDateTime`, `Date`.

## Write

`Excel.write()` supports three styles: annotation-driven, programmatic DSL, and template fill.

### Annotation-driven

```java
@ExcelSheet(name = "Users", style = StylePreset.PROFESSIONAL)
record User(
    @ExcelColumn(header = "Name", width = 20)
    String name,

    @ExcelColumn(header = "Balance", format = "#,##0.00")
    BigDecimal balance
) {}

// Export
Excel.write(users).to(outputStream);
Excel.write(users).to(Path.of("users.xlsx"));
```

### Multi-sheet

```java
Excel.write()
    .sheet("Users", users)
    .sheet("Orders", orders)
    .to(outputStream);
```

### Programmatic DSL with `SheetDefinition`

```java
Excel.write()
    .sheet(def -> def.name("Report")
        .title("Q4 2024 Sales", 4)                       // merged title row
        .complexHeader(
            List.of("",    "H1",  "H1"),                  // parent headers
            List.of("Name", "Rev", "Cost"))               // leaf headers
        .columns(
            ColumnSpec.of("name", "Name").withWidth(20),
            ColumnSpec.of("rev",  "Rev").withFormat("#,##0.00"),
            ColumnSpec.of("cost", "Cost").withFormat("#,##0.00"))
        .mergeSame("Dept")                                 // merge consecutive same values
        .style(StylePreset.CORPORATE)
        .autoWidth(true),
        rows)
    .to(outputStream);
```

The data parameter accepts `List<Record>`, `List<POJO>`, `List<Map<String, ?>>`, or `List<Object[]>`.

### Builder-only (no data)

```java
Excel.write()
    .sheet(def -> def.name("Readme").simpleHeader("Note"))
    .to(outputStream);
```

## Template Fill

Fill data into an existing `.xlsx` template without touching headers or layout:

```java
Excel.fromTemplate(templateStream)
    .fill("Users", users, 2)          // sheet name, data, start row
    .to(outputStream);

// With custom styling for the filled rows
var style = StyleProfile.custom()
    .dataBg("#FFFFFF")
    .allBorders(Border.THIN)
    .build();

Excel.fromTemplate(templateStream)
    .fill("Users", users, 2, style)
    .to(outputStream);
```

## Style Presets

| Preset | Use Case |
|--------|----------|
| `PROFESSIONAL` | Blue header, white bold text, bordered — general reports |
| `MINIMAL` | Bold underlined header, no borders — data dumps |
| `CORPORATE` | Dark blue header, alternating row colors — formal reports |
| `PLAIN` | No styling — fastest, raw data |

Create custom styles:

```java
var custom = StyleProfile.custom()
    .headerBg("#2F5496")
    .headerFg("#FFFFFF")
    .dataBg("#FFFFFF")
    .dataAltBg("#F2F2F2")
    .allBorders(Border.THIN)
    .fontName("Arial")
    .fontSize(10)
    .build();

Excel.write()
    .sheet(def -> def.name("Report").style(custom), rows)
    .to(out);
```

## Maven

```xml
<dependency>
    <groupId>cc.ashclaw</groupId>
    <artifactId>common4j-poi</artifactId>
    <version>2.0-SNAPSHOT</version>
</dependency>
```

Requires JDK 25+.
