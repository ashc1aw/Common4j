# Word

Read label-value pairs from Word forms and fill `.docx` templates with `${placeholder}` substitution and table row expansion.

## Quick Start

```java
// Extract form fields from a table
Map<String, String> fields = Word.read(docxFile).toFields();

// Map to a typed record
Contract c = Word.read(docxStream).toBean(Contract.class);

// Fill a template with key-value data
Word.fromTemplate(templateStream)
    .put("name", "Alice")
    .put("date", LocalDate.now())
    .to(outputStream);

// Fill from a POJO
Word.fromTemplate(templateStream)
    .fill(contract)
    .to(outputStream);
```

## Read — Form Extraction

Extracts label-value pairs from Word tables where each row has the pattern
`[label cell, value cell, ...]` — the most common layout in contracts, registration
forms, and info sheets.

### Basic extraction

```java
// All tables → map
Map<String, String> fields = Word.read(docxFile).toFields();
String contractNo = fields.get("Contract No.");

// Target a specific table by index (0-based)
Map<String, String> fields = Word.read(docxFile)
    .table(1)
    .toFields();
```

### Mapping to typed objects

Annotate fields with `@WordField` to control label matching:

```java
record Contract(
    @WordField(label = "Contract No.")
    String contractNo,

    @WordField(label = "Party A")
    String partyA,

    @WordField(label = "Sign Date")
    LocalDate signDate,

    @WordField(label = "Amount", format = "#,##0.00")
    BigDecimal amount
) {}

// Read
Contract c = Word.read(docxFile).toBean(Contract.class);
```

Works with POJOs too — `@WordField`-annotated non-static fields are populated via the no-arg constructor.

### Type coercion

Strings are automatically coerced to the target type. The optional `format` parameter
enables non-standard number and date formats:

| Target type | Without format | With `format = "#,##0.00"` | With `format = "yyyy/MM/dd"` |
|---|---|---|---|
| `BigDecimal` | `new BigDecimal("1500.50")` | Parses `"1,500.50"` | — |
| `LocalDate` | `LocalDate.parse("2024-06-15")` | — | Parses `"2024/06/15"` |
| `Integer` | `Integer.parseInt("42")` | Parses `"1,234"` → 1234 | — |

Supported types: `String`, `int`/`Integer`, `long`/`Long`, `double`/`Double`, `BigDecimal`,
`boolean`/`Boolean`, `LocalDate`, `LocalDateTime`, `Date`.

## Template Fill

Replace `${key}` placeholders in paragraphs and table cells with actual values.

### Simple replacement

The template `.docx` contains text like `Dear ${name}, your appointment is on ${date}.`

```java
Word.fromTemplate(inputStream)
    .put("name", "Alice")
    .put("date", LocalDate.now())
    .put("amount", new BigDecimal("1500.00"))
    .to(outputStream);

// Or write to a file
Word.fromTemplate(Path.of("template.docx"))
    .put("key", "value")
    .to(Path.of("output.docx"));
```

### POJO-driven fill

`fill()` reads fields from a record or POJO and maps them to placeholders:

```java
Word.fromTemplate(inputStream)
    .fill(contract)    // record components → ${componentName}
    .to(outputStream);
```

For records: uses `@WordField.label()` as the placeholder key when the annotation is present,
otherwise uses the component name. For POJOs: same behavior — checks `@WordField` first,
falls back to field name.

### Cross-run placeholders

Word often splits `${key}` across multiple XML runs (e.g., `${na` + `me}`).
The filler handles this transparently by merging runs before replacement — no
need to preprocess the template.

### Table row expansion

Expand a template table row for each item in a list. The template has a row with
`${prefix.field}` cells; each data item produces one output row.

```java
record Item(String name, int quantity, BigDecimal price) {}

var items = List.of(
    new Item("Widget", 10, new BigDecimal("9.99")),
    new Item("Gadget", 5, new BigDecimal("19.99")));

Word.fromTemplate(inputStream)
    .table("item", items)   // expands ${item.name}, ${item.quantity}, ${item.price}
    .to(outputStream);
```

Data items can be records, POJOs, or `Map<String, ?>` instances.

**Targeting a specific table:**

```java
// Only expand in table index 1 (0-based); -1 scans all tables
Word.fromTemplate(inputStream)
    .table(1, "item", items)
    .to(outputStream);
```

When the list is empty, the template row is removed with no data rows inserted.

### Placeholder key syntax

Keys support Unicode letters, digits, underscores, and dot-notation nesting:

```
${name}            — simple field
${item.name}       — table expansion field
${partyA}          — Unicode letter support (Chinese, Japanese, etc.)
${contract_no}     — underscores
```

## API Reference

### `Word` (entry point)

| Method | Description |
|--------|-------------|
| `Word.read(Path)` | Open a `.docx` file for form extraction |
| `Word.read(InputStream)` | Open a `.docx` stream for form extraction |
| `Word.fromTemplate(Path)` | Open a `.docx` template for filling |
| `Word.fromTemplate(InputStream)` | Open a `.docx` template stream for filling |

### `FormExtractor`

| Method | Description |
|--------|-------------|
| `.table(int)` | Target a specific table (0-based). Default: scan all |
| `.toFields()` | Extract all label-value pairs → `Map<String, String>` |
| `.toBean(Class)` | Map to typed record or POJO via `@WordField` |

### `TemplateFiller`

| Method | Description |
|--------|-------------|
| `.put(key, value)` | Set a placeholder value. `null` → `""` |
| `.fill(Object)` | Fill from record/POJO, respecting `@WordField` |
| `.table(key, data)` | Expand rows for list, scanning all tables |
| `.table(index, key, data)` | Expand rows targeting a specific table |
| `.to(OutputStream)` | Write filled document |
| `.to(Path)` | Write filled document to file |

## Maven

```xml
<dependency>
    <groupId>cc.ashclaw</groupId>
    <artifactId>common4j-poi</artifactId>
    <version>2.0.1</version>
</dependency>
```

Requires JDK 25+.
