package cc.ashclaw.common4j.poi.word;

import cc.ashclaw.common4j.poi.word.annotation.WordField;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
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

@DisplayName("Word")
class WordTest {

    // -- Test data records -------------------------------------------------

    record Contract(
            @WordField(label = "合同编号") String contractNo,
            @WordField(label = "甲方") String partyA,
            @WordField(label = "乙方") String partyB,
            @WordField(label = "签署日期") LocalDate signDate,
            @WordField(label = "金额") BigDecimal amount
    ) {}

    record Item(
            String name,
            int quantity,
            BigDecimal price
    ) {}

    static class PojoContract {
        @WordField(label = "合同编号") String contractNo;
        @WordField(label = "甲方") String partyA;
        @WordField(label = "金额") BigDecimal amount;
    }

    // -- Docx builders -----------------------------------------------------

    /** Creates a simple docx with one table containing label-value pairs. */
    static byte[] formDocx(Map<String, String> fields) throws Exception {
        try (var doc = new XWPFDocument(); var baos = new ByteArrayOutputStream()) {
            var table = doc.createTable(fields.size(), 2);
            int r = 0;
            for (var entry : fields.entrySet()) {
                var row = table.getRow(r++);
                row.getCell(0).setText(entry.getKey());
                row.getCell(1).setText(entry.getValue());
            }
            doc.write(baos);
            return baos.toByteArray();
        }
    }

    /** Creates a template docx with paragraphs containing ${key} placeholders. */
    static byte[] templateDocx(Map<String, String> placeholders) throws Exception {
        try (var doc = new XWPFDocument(); var baos = new ByteArrayOutputStream()) {
            for (var entry : placeholders.entrySet()) {
                var p = doc.createParagraph();
                // Split placeholder across runs to test cross-run handling
                var key = entry.getKey();
                var placeholder = "${" + key + "}";
                int mid = placeholder.length() / 2;
                var r1 = p.createRun();
                r1.setText(placeholder.substring(0, mid), 0);
                var r2 = p.createRun();
                r2.setText(placeholder.substring(mid), 0);
            }
            doc.write(baos);
            return baos.toByteArray();
        }
    }

    /** Creates a template docx with a table containing row-expansion placeholders. */
    static byte[] tableTemplateDocx(String prefix, List<String> fields, int existingRows) throws Exception {
        try (var doc = new XWPFDocument(); var baos = new ByteArrayOutputStream()) {
            // A paragraph before the table
            var p = doc.createParagraph();
            p.createRun().setText("Report", 0);

            // Table with headers + template row
            var table = doc.createTable(existingRows + 1, fields.size());
            // Existing rows (e.g., header)
            for (int r = 0; r < existingRows; r++) {
                var row = table.getRow(r);
                for (int c = 0; c < fields.size(); c++) {
                    row.getCell(c).setText("Header" + (c + 1));
                }
            }
            // Template row with ${prefix.field}
            var tmplRow = table.getRow(existingRows);
            for (int c = 0; c < fields.size(); c++) {
                tmplRow.getCell(c).setText("${" + prefix + "." + fields.get(c) + "}");
            }

            doc.write(baos);
            return baos.toByteArray();
        }
    }

    // -- Form extraction tests ---------------------------------------------

    @Nested
    @DisplayName("form extraction")
    class FormExtraction {

        @Test
        @DisplayName("extract label-value pairs to Map")
        void toFields() throws Exception {
            var data = Map.of("合同编号", "HT-2024-001", "甲方", "某某有限公司", "签署日期", "2024-06-15");
            var bytes = formDocx(data);

            var fields = Word.read(new ByteArrayInputStream(bytes)).toFields();
            assertEquals("HT-2024-001", fields.get("合同编号"));
            assertEquals("某某有限公司", fields.get("甲方"));
            assertEquals("2024-06-15", fields.get("签署日期"));
        }

        @Test
        @DisplayName("extract to typed record via @WordField")
        void toBean() throws Exception {
            var data = Map.of(
                    "合同编号", "HT-2024-001",
                    "甲方", "某某有限公司",
                    "乙方", "某科技有限公司",
                    "签署日期", "2024-06-15",
                    "金额", "150000.00");
            var bytes = formDocx(data);

            var contract = Word.read(new ByteArrayInputStream(bytes)).toBean(Contract.class);
            assertEquals("HT-2024-001", contract.contractNo());
            assertEquals("某某有限公司", contract.partyA());
            assertEquals("某科技有限公司", contract.partyB());
            assertEquals(LocalDate.of(2024, 6, 15), contract.signDate());
            assertEquals(new BigDecimal("150000.00"), contract.amount());
        }

        @Test
        @DisplayName("extract to non-record POJO")
        void toPojo() throws Exception {
            var data = Map.of("合同编号", "HT-2024-002", "甲方", "测试公司", "金额", "99999.99");
            var bytes = formDocx(data);

            var c = Word.read(new ByteArrayInputStream(bytes)).toBean(PojoContract.class);
            assertEquals("HT-2024-002", c.contractNo);
            assertEquals("测试公司", c.partyA);
            assertEquals(new BigDecimal("99999.99"), c.amount);
        }

        @Test
        @DisplayName("empty table returns empty map")
        void emptyTable() throws Exception {
            try (var doc = new XWPFDocument(); var baos = new ByteArrayOutputStream()) {
                doc.createTable(0, 2);
                doc.write(baos);
                var fields = Word.read(new ByteArrayInputStream(baos.toByteArray())).toFields();
                assertTrue(fields.isEmpty());
            }
        }

        @Test
        @DisplayName("target specific table by index")
        void specificTable() throws Exception {
            try (var doc = new XWPFDocument(); var baos = new ByteArrayOutputStream()) {
                // Table 0: ignored
                var t0 = doc.createTable(1, 2);
                t0.getRow(0).getCell(0).setText("ignored");
                t0.getRow(0).getCell(1).setText("value");
                // Table 1: targeted
                var t1 = doc.createTable(1, 2);
                t1.getRow(0).getCell(0).setText("target");
                t1.getRow(0).getCell(1).setText("hit");

                doc.write(baos);
                var fields = Word.read(new ByteArrayInputStream(baos.toByteArray()))
                        .table(1).toFields();
                assertEquals("hit", fields.get("target"));
                assertEquals(1, fields.size());
            }
        }
    }

    // -- Template fill tests -----------------------------------------------

    @Nested
    @DisplayName("template fill")
    class TemplateFill {

        @Test
        @DisplayName("simple key-value replacement")
        void simpleReplace() throws Exception {
            var bytes = templateDocx(Map.of("name", "Alice", "date", "2024-06-15"));

            try (var baos = new ByteArrayOutputStream()) {
                Word.fromTemplate(new ByteArrayInputStream(bytes))
                        .put("name", "Alice")
                        .put("date", "2024-06-15")
                        .to(baos);

                // Read back and verify
                try (var doc = new XWPFDocument(new ByteArrayInputStream(baos.toByteArray()))) {
                    var text = new StringBuilder();
                    for (var p : doc.getParagraphs()) text.append(p.getText()).append("\n");
                    assertTrue(text.toString().contains("Alice"));
                    assertTrue(text.toString().contains("2024-06-15"));
                    assertFalse(text.toString().contains("${name}"));
                    assertFalse(text.toString().contains("${date}"));
                }
            }
        }

        @Test
        @DisplayName("cross-run placeholder replacement")
        void crossRunReplace() throws Exception {
            // Build a docx where ${name} is split across runs
            try (var doc = new XWPFDocument(); var baos = new ByteArrayOutputStream()) {
                var p = doc.createParagraph();
                var r1 = p.createRun();
                r1.setText("${na", 0);
                var r2 = p.createRun();
                r2.setText("me}", 0);
                doc.write(baos);

                try (var baos2 = new ByteArrayOutputStream()) {
                    Word.fromTemplate(new ByteArrayInputStream(baos.toByteArray()))
                            .put("name", "Alice")
                            .to(baos2);

                    try (var doc2 = new XWPFDocument(new ByteArrayInputStream(baos2.toByteArray()))) {
                        var text = doc2.getParagraphs().getFirst().getText();
                        assertTrue(text.contains("Alice"));
                        assertFalse(text.contains("${"));
                    }
                }
            }
        }

        @Test
        @DisplayName("fill from record")
        void fillRecord() throws Exception {
            var bytes = templateDocx(Map.of("合同编号", "", "甲方", ""));
            var contract = new Contract("HT-001", "甲方公司", "乙方公司",
                    LocalDate.of(2024, 6, 15), new BigDecimal("100000.00"));

            try (var baos = new ByteArrayOutputStream()) {
                Word.fromTemplate(new ByteArrayInputStream(bytes))
                        .fill(contract)
                        .to(baos);

                try (var doc = new XWPFDocument(new ByteArrayInputStream(baos.toByteArray()))) {
                    var text = new StringBuilder();
                    for (var p : doc.getParagraphs()) text.append(p.getText()).append("\n");
                    assertTrue(text.toString().contains("HT-001"));
                    assertTrue(text.toString().contains("甲方公司"));
                }
            }
        }

        @Test
        @DisplayName("write to file")
        void writeToFile(@TempDir Path tempDir) throws Exception {
            var bytes = templateDocx(Map.of("key", "value"));
            var file = tempDir.resolve("out.docx");

            Word.fromTemplate(new ByteArrayInputStream(bytes))
                    .put("key", "hello")
                    .to(file);

            assertTrue(java.nio.file.Files.exists(file));
            assertTrue(java.nio.file.Files.size(file) > 0);
        }
    }

    // -- Table expansion tests ---------------------------------------------

    @Nested
    @DisplayName("table row expansion")
    class TableExpansion {

        @Test
        @DisplayName("expands template row for list data")
        void expandRows() throws Exception {
            var items = List.of(
                    new Item("Widget", 10, new BigDecimal("9.99")),
                    new Item("Gadget", 5, new BigDecimal("19.99")),
                    new Item("Doohickey", 2, new BigDecimal("49.99")));

            var bytes = tableTemplateDocx("item",
                    List.of("name", "quantity", "price"), 1);

            try (var baos = new ByteArrayOutputStream()) {
                Word.fromTemplate(new ByteArrayInputStream(bytes))
                        .table("item", items)
                        .to(baos);

                try (var doc = new XWPFDocument(new ByteArrayInputStream(baos.toByteArray()))) {
                    // Should have header + 3 data rows
                    var tables = doc.getTables();
                    assertEquals(1, tables.size());
                    var rows = tables.getFirst().getRows();
                    assertEquals(1 + items.size(), rows.size(),
                            "header + " + items.size() + " data rows");

                    // Verify header
                    assertTrue(rows.get(0).getCell(0).getText().contains("Header"));

                    // Verify data rows (no ${} left)
                    for (int r = 1; r < rows.size(); r++) {
                        for (var cell : rows.get(r).getTableCells()) {
                            assertFalse(cell.getText().contains("${"),
                                    "row " + r + " should not contain placeholder");
                        }
                    }
                }
            }
        }

        @Test
        @DisplayName("empty list removes template row")
        void emptyList() throws Exception {
            var bytes = tableTemplateDocx("item",
                    List.of("name", "quantity", "price"), 1);

            try (var baos = new ByteArrayOutputStream()) {
                Word.fromTemplate(new ByteArrayInputStream(bytes))
                        .table("item", List.of())
                        .to(baos);

                try (var doc = new XWPFDocument(new ByteArrayInputStream(baos.toByteArray()))) {
                    var rows = doc.getTables().getFirst().getRows();
                    // Only header row remains (template row removed, nothing inserted)
                    assertEquals(1, rows.size());
                }
            }
        }
    }

    // -- Format tests ------------------------------------------------------

    @Nested
    @DisplayName("value formatting")
    class ValueFormatting {

        @Test
        @DisplayName("various types are formatted correctly")
        void typeFormatting() throws Exception {
            // Build template with placeholders for each type
            try (var doc = new XWPFDocument(); var baos = new ByteArrayOutputStream()) {
                var p = doc.createParagraph();
                p.createRun().setText("int:${intVal}", 0);
                p = doc.createParagraph();
                p.createRun().setText("long:${longVal}", 0);
                p = doc.createParagraph();
                p.createRun().setText("decimal:${decVal}", 0);
                p = doc.createParagraph();
                p.createRun().setText("date:${dateVal}", 0);
                p = doc.createParagraph();
                p.createRun().setText("bool:${boolVal}", 0);
                doc.write(baos);

                try (var baos2 = new ByteArrayOutputStream()) {
                    Word.fromTemplate(new ByteArrayInputStream(baos.toByteArray()))
                            .put("intVal", 42)
                            .put("longVal", 10000000000L)
                            .put("decVal", new BigDecimal("99.99"))
                            .put("dateVal", LocalDate.of(2024, 6, 15))
                            .put("boolVal", true)
                            .to(baos2);

                    try (var doc2 = new XWPFDocument(new ByteArrayInputStream(baos2.toByteArray()))) {
                        var text = new StringBuilder();
                        for (var para : doc2.getParagraphs()) text.append(para.getText()).append("\n");
                        var s = text.toString();
                        assertTrue(s.contains("42"));
                        assertTrue(s.contains("10000000000"));
                        assertTrue(s.contains("99.99"));
                        assertTrue(s.contains("2024-06-15"));
                        assertTrue(s.contains("true"));
                    }
                }
            }
        }
    }

    // -- Utility class test ------------------------------------------------

    @Nested
    @DisplayName("utility classes")
    class UtilityClasses {

        @Test
        @DisplayName("Word constructor throws")
        void wordCtor() throws Exception {
            var ctor = Word.class.getDeclaredConstructor();
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
