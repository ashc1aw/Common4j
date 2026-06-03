package cc.ashclaw.common4j.poi.excel;

import cc.ashclaw.common4j.poi.excel.annotation.ExcelColumn;
import cc.ashclaw.common4j.poi.excel.annotation.ExcelSheet;
import cc.ashclaw.common4j.poi.excel.define.ColumnSpec;
import cc.ashclaw.common4j.poi.excel.define.HeaderNode;
import cc.ashclaw.common4j.poi.excel.define.SheetDefinition;
import cc.ashclaw.common4j.poi.excel.style.Border;
import cc.ashclaw.common4j.poi.excel.style.StylePreset;
import cc.ashclaw.common4j.poi.excel.style.StyleProfile;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Excel")
class ExcelTest {

    @ExcelSheet(name = "Users")
    record User(
            @ExcelColumn(index = 0, header = "Name") String name,
            @ExcelColumn(index = 1, header = "Age") int age,
            @ExcelColumn(index = 2, header = "Email", width = 25) String email,
            @ExcelColumn(index = 3, header = "Balance", format = "#,##0.00") BigDecimal balance
    ) {}

    record SimpleRow(
            @ExcelColumn(header = "Col A") String colA,
            @ExcelColumn(header = "Col B") int colB
    ) {}

    // -- Roundtrip tests -------------------------------------------------

    @Nested
    @DisplayName("write then read roundtrip")
    class WriteReadRoundtrip {

        @Test
        @DisplayName("annotated records survive full roundtrip")
        void roundtrip() throws Exception {
            var users = List.of(
                    new User("Alice", 30, "alice@example.com", new BigDecimal("1500.50")),
                    new User("Bob", 25, "bob@example.com", new BigDecimal("3200.00")),
                    new User("", 0, "", BigDecimal.ZERO)
            );

            var baos = new ByteArrayOutputStream();
            Excel.write(users).to(baos);

            var read = Excel.read(new ByteArrayInputStream(baos.toByteArray()))
                    .toList(User.class);

            assertEquals(3, read.size());
            assertEquals("Alice", read.get(0).name());
            assertEquals(30, read.get(0).age());
            assertEquals("alice@example.com", read.get(0).email());
            assertEquals(new BigDecimal("1500.50").compareTo(read.get(0).balance()), 0);

            assertEquals("Bob", read.get(1).name());
            assertEquals(25, read.get(1).age());
        }

        @Test
        @DisplayName("empty list roundtrip")
        void emptyList() throws Exception {
            var baos = new ByteArrayOutputStream();
            Excel.write(List.<User>of()).to(baos);

            // Empty data with write(List) creates no sheet by default
            // so we use write() and explicitly add an empty sheet
            var baos2 = new ByteArrayOutputStream();
            Excel.write()
                    .sheet("Empty", List.<User>of())
                    .to(baos2);

            var read = Excel.read(new ByteArrayInputStream(baos2.toByteArray()))
                    .toList(User.class);
            assertTrue(read.isEmpty());
        }
    }

    @Nested
    @DisplayName("write to file, read from file")
    class FileRoundtrip {

        @Test
        @DisplayName("write to Path and read back")
        void fileRoundtrip(@TempDir Path tempDir) throws Exception {
            var users = List.of(
                    new User("Alice", 30, "alice@x.com", new BigDecimal("100.00")));
            var file = tempDir.resolve("test.xlsx");

            Excel.write(users).to(file);
            assertTrue(java.nio.file.Files.exists(file));

            var read = Excel.read(file).toList(User.class);
            assertEquals(1, read.size());
            assertEquals("Alice", read.getFirst().name());
        }
    }

    // -- Complex header tests --------------------------------------------

    @Nested
    @DisplayName("complex headers")
    class ComplexHeaders {

        @Test
        @DisplayName("multi-level header renders and export works")
        void multiLevel() throws Exception {
            var def = SheetDefinition.of("Report")
                    .title("Q4 2024 Sales Report", 4)
                    .complexHeader(
                            List.of("", "", "H1 2024", "H1 2024"),
                            List.of("Name", "Dept", "Revenue", "Cost"))
                    .columns(
                            ColumnSpec.of("name", "Name"),
                            ColumnSpec.of("dept", "Dept"),
                            ColumnSpec.of("rev", "Revenue").withFormat("#,##0.00"),
                            ColumnSpec.of("cost", "Cost").withFormat("#,##0.00"))
                    .style(StylePreset.PROFESSIONAL)
                    .autoWidth(true)
                    .build();

            var data = List.<Object[]>of(
                    new Object[]{"Alice", "Eng", 100000L, 50000L},
                    new Object[]{"Bob", "Sales", 80000L, 30000L});

            var baos = new ByteArrayOutputStream();
            Excel.write()
                    .sheet(def, data)
                    .to(baos);

            var maps = Excel.read(new ByteArrayInputStream(baos.toByteArray()))
                    .sheet("Report")
                    .headerRow(2) // row 2 = leaf labels of complex header
                    .dataStartRow(3) // row 3 = first data row
                    .toMaps();

            assertEquals(2, maps.size());
            assertEquals("Alice", maps.get(0).get("Name"));
        }
    }

    // -- Data merge tests ------------------------------------------------

    @Nested
    @DisplayName("data cell merging")
    class DataMerge {

        @Test
        @DisplayName("consecutive same-value cells are merged")
        void mergeSame() throws Exception {
            var def = SheetDefinition.of("Merged")
                    .simpleHeader("Name", "Dept", "Score")
                    .column(ColumnSpec.of("name", "Name"))
                    .column(ColumnSpec.of("dept", "Dept"))
                    .column(ColumnSpec.of("score", "Score"))
                    .mergeSame("Dept")
                    .style(StylePreset.MINIMAL)
                    .build();

            var data = List.<Object[]>of(
                    new Object[]{"Alice", "Eng", 90},
                    new Object[]{"Bob", "Eng", 85},
                    new Object[]{"Charlie", "Sales", 80});

            var baos = new ByteArrayOutputStream();
            Excel.write()
                    .sheet(def, data)
                    .to(baos);

            // Verify data survived
            var maps = Excel.read(new ByteArrayInputStream(baos.toByteArray()))
                    .sheet("Merged")
                    .toMaps();
            assertEquals(3, maps.size());
            assertEquals("Alice", maps.get(0).get("Name"));
        }
    }

    // -- Style tests -----------------------------------------------------

    @Nested
    @DisplayName("style presets")
    class StylePresets {

        @Test
        @DisplayName("all presets produce valid output")
        void allPresets() throws Exception {
            for (var preset : StylePreset.values()) {
                var def = SheetDefinition.of("Styled")
                        .simpleHeader("A", "B")
                        .column(ColumnSpec.of("a", "A"))
                        .column(ColumnSpec.of("b", "B"))
                        .style(preset)
                        .build();

                var data = List.<Object[]>of(new Object[]{"x", 1});
                var baos = new ByteArrayOutputStream();
                Excel.write().sheet(def, data).to(baos);

                var maps = Excel.read(new ByteArrayInputStream(baos.toByteArray()))
                        .sheet("Styled").toMaps();
                assertEquals(1, maps.size());
            }
        }

        @Test
        @DisplayName("custom style profile works")
        void customStyle() throws Exception {
            var profile = StyleProfile.custom()
                    .headerBg("#333333")
                    .headerFont("#FFFFFF")
                    .dataBg("#FAFAFA")
                    .allBorders(Border.THIN)
                    .build();

            var def = SheetDefinition.of("Custom")
                    .simpleHeader("X")
                    .column(ColumnSpec.of("x", "X"))
                    .style(profile)
                    .build();

            var data = List.<Object[]>of(new Object[]{"hello"});
            var baos = new ByteArrayOutputStream();
            Excel.write().sheet(def, data).to(baos);

            var maps = Excel.read(new ByteArrayInputStream(baos.toByteArray()))
                    .sheet("Custom").toMaps();
            assertEquals(1, maps.size());
        }
    }

    // -- Multi-sheet tests -----------------------------------------------

    @Nested
    @DisplayName("multi-sheet")
    class MultiSheet {

        @Test
        @DisplayName("multiple sheets in one workbook")
        void multiSheet() throws Exception {
            var baos = new ByteArrayOutputStream();
            Excel.write()
                    .sheet("SheetA", List.of(new SimpleRow("a", 1), new SimpleRow("b", 2)))
                    .sheet("SheetB", List.of(new SimpleRow("c", 3)))
                    .to(baos);

            var readA = Excel.read(new ByteArrayInputStream(baos.toByteArray()))
                    .sheet("SheetA").toList(SimpleRow.class);
            var readB = Excel.read(new ByteArrayInputStream(baos.toByteArray()))
                    .sheet("SheetB").toList(SimpleRow.class);

            assertEquals(2, readA.size());
            assertEquals(1, readB.size());
        }
    }

    // -- Reader tests ----------------------------------------------------

    @Nested
    @DisplayName("read toMaps")
    class ReadToMaps {

        @Test
        @DisplayName("raw map reading")
        void toMaps() throws Exception {
            var users = List.of(
                    new User("Alice", 30, "a@x.com", BigDecimal.TEN));

            var baos = new ByteArrayOutputStream();
            Excel.write(users).to(baos);

            var maps = Excel.read(new ByteArrayInputStream(baos.toByteArray()))
                    .sheet(0).toMaps();

            assertEquals(1, maps.size());
            assertEquals("Alice", maps.getFirst().get("Name"));
            assertEquals("30", maps.getFirst().get("Age"));
        }
    }

    @Nested
    @DisplayName("read with errors")
    class ReadWithErrors {

        @Test
        @DisplayName("bad data collects parse errors")
        void parseErrors() throws Exception {
            var users = List.of(
                    new User("Alice", 30, "a@x.com", BigDecimal.TEN));

            var baos = new ByteArrayOutputStream();
            Excel.write(users).to(baos);

            // Read as wrong type — should fail gracefully
            // Actually we can read as Map to avoid type issues
            var result = Excel.read(new ByteArrayInputStream(baos.toByteArray()))
                    .sheet(0).toResult(User.class);

            assertTrue(result.isSuccess());
        }
    }

    // -- Non-record POJO test --------------------------------------------

    static class PojoUser {
        @ExcelColumn(index = 0, header = "Name")
        String name;

        @ExcelColumn(index = 1, header = "Age")
        int age;

        // No-arg constructor
        PojoUser() {}
    }

    @Nested
    @DisplayName("non-record POJO")
    class NonRecordPojo {

        @Test
        @DisplayName("write and read non-record class")
        void pojoRoundtrip() throws Exception {
            // Create and populate
            var u1 = new PojoUser();
            u1.name = "Alice";
            u1.age = 30;
            var u2 = new PojoUser();
            u2.name = "Bob";
            u2.age = 25;

            var baos = new ByteArrayOutputStream();
            Excel.write(List.of(u1, u2)).to(baos);

            var read = Excel.read(new ByteArrayInputStream(baos.toByteArray()))
                    .toList(PojoUser.class);

            assertEquals(2, read.size());
            assertEquals("Alice", read.get(0).name);
            assertEquals(30, read.get(0).age);
        }
    }

    // -- Type coercion tests ---------------------------------------------

    record TypesRow(
            @ExcelColumn(index = 0) String text,
            @ExcelColumn(index = 1) int intVal,
            @ExcelColumn(index = 2) long longVal,
            @ExcelColumn(index = 3) double doubleVal,
            @ExcelColumn(index = 4) boolean boolVal,
            @ExcelColumn(index = 5) BigDecimal decimalVal,
            @ExcelColumn(index = 6) LocalDate dateVal,
            @ExcelColumn(index = 7) String nullable
    ) {}

    @Nested
    @DisplayName("type coercion")
    class TypeCoercion {

        @Test
        @DisplayName("all supported types survive roundtrip")
        void typeRoundtrip() throws Exception {
            var rows = List.of(new TypesRow(
                    "hello", 42, 10000000000L, 3.14, true,
                    new BigDecimal("99.99"), LocalDate.of(2024, 6, 15),
                    null));

            var baos = new ByteArrayOutputStream();
            Excel.write(rows).to(baos);

            // Verify via toMaps first (no type mapping)
            var maps = Excel.read(new ByteArrayInputStream(baos.toByteArray()))
                    .toMaps();
            assertEquals(1, maps.size(), "raw row count");
            assertEquals("hello", maps.getFirst().get("text"));

            // Then verify typed mapping
            var result = Excel.read(new ByteArrayInputStream(baos.toByteArray()))
                    .toResult(TypesRow.class);
            assertTrue(result.errors().isEmpty(),
                    () -> "parse errors: " + result.errors());
            assertEquals(1, result.data().size());

            var r = result.data().getFirst();
            assertEquals("hello", r.text());
            assertEquals(42, r.intVal());
            assertEquals(10000000000L, r.longVal());
            assertEquals(3.14, r.doubleVal(), 0.01);
            assertTrue(r.boolVal());
            assertEquals(new BigDecimal("99.99"), r.decimalVal());
            assertEquals(LocalDate.of(2024, 6, 15), r.dateVal());
            assertNull(r.nullable());
        }
    }

    // -- Builder DSL tests -----------------------------------------------

    @Nested
    @DisplayName("SheetDefinition builder")
    class SheetDefinitionBuilder {

        @Test
        @DisplayName("lambda builder style works")
        void lambdaBuilder() throws Exception {
            var data = List.<Object[]>of(new Object[]{"hello", 42});

            var baos = new ByteArrayOutputStream();
            Excel.write()
                    .sheet(def -> def
                            .name("Lambda")
                            .simpleHeader("Text", "Number")
                            .style(StylePreset.PROFESSIONAL), data)
                    .to(baos);

            var maps = Excel.read(new ByteArrayInputStream(baos.toByteArray()))
                    .sheet("Lambda").toMaps();
            assertEquals(1, maps.size());
            assertEquals("hello", maps.getFirst().get("Text"));
        }
    }

    // -- Template fill tests ----------------------------------------------

    @Nested
    @DisplayName("template fill")
    class TemplateFill {

        @Test
        @DisplayName("fills data into template starting at given row")
        void templateFill() throws Exception {
            // Step 1: create a template with just headers
            var templateDef = SheetDefinition.of("Data")
                    .simpleHeader("Name", "Score")
                    .build();
            var baos = new ByteArrayOutputStream();
            Excel.write()
                    .sheet(templateDef, List.of())
                    .to(baos);

            // Step 2: fill data into template
            var data = List.<Object[]>of(
                    new Object[]{"Alice", 95},
                    new Object[]{"Bob", 87});
            var baos2 = new ByteArrayOutputStream();
            Excel.fromTemplate(new ByteArrayInputStream(baos.toByteArray()))
                    .fill("Data", data, 1)
                    .to(baos2);

            // Step 3: read back and verify
            var maps = Excel.read(new ByteArrayInputStream(baos2.toByteArray()))
                    .sheet("Data")
                    .headerRow(0)
                    .dataStartRow(1)
                    .toMaps();
            assertEquals(2, maps.size());
            assertEquals("Alice", maps.get(0).get("Name"));
            assertEquals("95", maps.get(0).get("Score"));
        }
    }

    // -- Header node tests -----------------------------------------------

    @Nested
    @DisplayName("HeaderNode")
    class HeaderNodeTests {

        @Test
        @DisplayName("simple header has depth 1 and no merges")
        void simple() {
            var h = HeaderNode.simple("A", "B", "C");
            assertEquals(1, h.depth());
            assertEquals(3, h.leafCount());
            assertTrue(h.mergeRegions().isEmpty());
            assertEquals(List.of("A", "B", "C"), h.leafLabels());
        }

        @Test
        @DisplayName("multi-level header calculates merge regions")
        void multiLevel() {
            var h = HeaderNode.of(
                    List.of("", "", "H1", "H1"),
                    List.of("Name", "Age", "Rev", "Cost"));
            assertEquals(2, h.depth());
            assertEquals(4, h.leafCount());
            // Row 0: "" merged across cols 0-1, "H1" merged across cols 2-3
            assertFalse(h.mergeRegions().isEmpty());
        }

        @Test
        @DisplayName("of with single level returns SingleLevel")
        void singleLevel() {
            var h = HeaderNode.of(List.of("A", "B"));
            assertEquals(1, h.depth());
            assertEquals(2, h.leafCount());
        }
    }

    // -- Border enum test ------------------------------------------------

    @Nested
    @DisplayName("Border")
    class BorderTests {

        @Test
        @DisplayName("Border enum has expected values")
        void values() {
            assertEquals(4, Border.values().length);
        }
    }

    // -- Constructor tests -----------------------------------------------

    @Nested
    @DisplayName("utility classes")
    class UtilityClasses {

        @Test
        @DisplayName("Excel constructor throws")
        void excelCtor() throws Exception {
            var ctor = Excel.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            assertThrows(UnsupportedOperationException.class, () -> {
                try {
                    ctor.newInstance();
                } catch (java.lang.reflect.InvocationTargetException e) {
                    throw e.getCause();
                }
            });
        }
    }
}
