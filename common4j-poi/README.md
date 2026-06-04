# common4j-poi

Office document toolkit built on Apache POI — read, write, and template-fill Excel and Word files with minimal boilerplate.

## Modules

| Module | Package | Purpose |
|--------|---------|---------|
| [Excel](src/main/java/cc/ashclaw/common4j/poi/excel/) | `cc.ashclaw.common4j.poi.excel` | Read/write `.xlsx` with annotations, complex headers, cell merging, style presets, template fill |
| [Word](src/main/java/cc/ashclaw/common4j/poi/word/) | `cc.ashclaw.common4j.poi.word` | Extract form fields from `.docx` tables, fill templates with `${placeholder}` substitution and table row expansion |

## Philosophy

Every feature solves a real problem. No XML manipulation, no low-level POI boilerplate.
Annotations for the simple cases, a fluent DSL for the complex ones.

## Quick Examples

### Excel

```java
// Read
List<User> users = Excel.read(file).toList(User.class);

// Write
Excel.write(users).to(outputStream);

// Multi-sheet with complex headers and merge
Excel.write()
    .sheet(def -> def.name("Report")
        .title("Q4 Report", 4)
        .complexHeader(List.of("", "H1", "H1"), List.of("Name", "Rev", "Cost"))
        .mergeSame("Dept")
        .style(StylePreset.CORPORATE), rows)
    .to(outputStream);

// Template fill
Excel.fromTemplate(templateStream)
    .fill("Users", users, 2)
    .to(outputStream);
```

### Word

```java
// Read form fields
Contract c = Word.read(docxFile).toBean(Contract.class);

// Fill template
Word.fromTemplate(templateStream)
    .put("name", "Alice")
    .put("date", LocalDate.now())
    .table("items", itemList)
    .to(outputStream);
```

## Dependencies

- **Apache POI** 5.4.0 (`poi`, `poi-ooxml`)
- **JDK 25+**
- No other runtime dependencies

## Maven

```xml
<dependency>
    <groupId>cc.ashclaw</groupId>
    <artifactId>common4j-poi</artifactId>
    <version>2.0-SNAPSHOT</version>
</dependency>
```

## API Design Conventions

- **Entry point** — every module has a single `Word` or `Excel` facade class; never construct readers/writers directly
- **Fluent chain** — configure → execute in one expression
- **Annotations for mapping** — `@ExcelColumn`, `@ExcelSheet`, `@WordField` control column/form-field mapping; optional for simple positional cases
- **Records and POJOs** — both supported equally
- **Null safety** — all public methods reject null arguments with `Objects.requireNonNull`
- **Auto-close** — readers close the underlying workbook automatically after `toList()`/`toResult()`/`toMaps()`
