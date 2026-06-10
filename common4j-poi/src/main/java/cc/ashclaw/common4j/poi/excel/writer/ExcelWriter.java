package cc.ashclaw.common4j.poi.excel.writer;

import cc.ashclaw.common4j.poi.excel.annotation.ExcelColumn;
import cc.ashclaw.common4j.poi.excel.annotation.ExcelSheet;
import cc.ashclaw.common4j.poi.excel.define.ColumnSpec;
import cc.ashclaw.common4j.poi.excel.define.HeaderNode;
import cc.ashclaw.common4j.poi.excel.define.SheetDefinition;
import cc.ashclaw.common4j.poi.excel.style.Border;
import cc.ashclaw.common4j.poi.excel.style.StylePreset;
import cc.ashclaw.common4j.poi.excel.style.StyleProfile;
import cc.ashclaw.common4j.poi.excel.style.StyleRenderer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Writes POJOs or row data to Excel files with declarative styling,
 * complex headers, and cell merging.
 *
 * Simple export (annotation-driven):
 * <pre>{@code
 * Excel.write(users).to(outputStream);
 * }</pre>
 *
 * Multi-sheet with custom definition:
 * <pre>{@code
 * Excel.write()
 *     .sheet("Users", users)
 *     .sheet(def -> def.name("Report")
 *         .title("Q4 Report", 4)
 *         .complexHeader(List.of("", "H1", "H1"), List.of("Name", "Rev", "Cost"))
 *         .style(StylePreset.CORPORATE), rows)
 *     .to(outputStream);
 * }</pre>
 */
public final class ExcelWriter implements AutoCloseable {

    private final SXSSFWorkbook  wb;
    private final List<SheetTask> tasks = new ArrayList<>();

    public ExcelWriter() {
        // SXSSFWorkbook: streaming, 100-row window before disk flush
        this.wb = new SXSSFWorkbook(100);
        wb.setCompressTempFiles(true);
    }

    public ExcelWriter(XSSFWorkbook template) {
        this.wb = new SXSSFWorkbook(template);
    }

    // -- Sheet DSL ------------------------------------------------------

    /**
     * Adds a sheet from annotated POJOs.
     *
     * @param name  sheet name (overrides any {@link ExcelSheet} annotation)
     * @param data  list of annotated POJOs
     */
    public ExcelWriter sheet(String name, List<?> data) {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(data, "data must not be null");
        tasks.add(new SheetTask(name, null, null, data));
        return this;
    }

    /**
     * Adds a sheet using a programmatic {@link SheetDefinition}.
     *
     * @param definition  the sheet definition (name, headers, merge, style, ...)
     * @param data        the data rows (POJOs or {@code List<Object[]>} or {@code List<Map>})
     */
    public ExcelWriter sheet(SheetDefinition definition, List<?> data) {
        Objects.requireNonNull(definition, "definition must not be null");
        Objects.requireNonNull(data, "data must not be null");
        tasks.add(new SheetTask(definition.name(), definition, null, data));
        return this;
    }

    /**
     * Adds a sheet defined via a builder lambda.
     *
     * <pre>{@code
     * .sheet(def -> def.name("Report").simpleHeader("A","B").data(rows))
     * }</pre>
     */
    public ExcelWriter sheet(Consumer<SheetDefinition.Builder> builder) {
        Objects.requireNonNull(builder, "builder must not be null");
        tasks.add(new SheetTask(null, null, builder, null));
        return this;
    }

    /**
     * Adds a sheet defined via a builder lambda, with data provided separately.
     */
    public ExcelWriter sheet(Consumer<SheetDefinition.Builder> builder, List<?> data) {
        Objects.requireNonNull(builder, "builder must not be null");
        Objects.requireNonNull(data, "data must not be null");
        tasks.add(new SheetTask(null, null, builder, data));
        return this;
    }

    // -- Output ---------------------------------------------------------

    /** Writes the workbook to an output stream. Caller must close the stream. */
    public void to(OutputStream os) throws IOException {
        try {
            for (var task : tasks) {
                renderSheet(task);
            }
            wb.write(os);
        } finally {
            wb.dispose();
        }
    }

    /** Writes the workbook to a file. */
    public void to(Path path) throws IOException {
        try (var os = Files.newOutputStream(path)) {
            to(os);
        }
    }

    @Override
    public void close() {
        wb.dispose();
    }

    // -- Template fill --------------------------------------------------

    /**
     * Fills data into a specific sheet of the template workbook,
     * starting at the given row (0-based). Does not write headers.
     */
    public ExcelWriter fill(String sheetName, List<?> data, int startRow) {
        return fill(sheetName, data, startRow, StyleProfile.of(StylePreset.PLAIN));
    }

    /**
     * Fills data into a specific sheet of the template workbook,
     * starting at the given row (0-based), with the given style profile.
     * Does not write headers.
     */
    public ExcelWriter fill(String sheetName, List<?> data, int startRow,
                            StyleProfile style) {
        Objects.requireNonNull(sheetName, "sheetName must not be null");
        Objects.requireNonNull(data, "data must not be null");
        if (startRow < 0) throw new IllegalArgumentException("startRow must be >= 0");
        var def = SheetDefinition.of(sheetName).style(style).autoWidth(false).build();
        tasks.add(new SheetTask(sheetName, def, startRow, data));
        return this;
    }

    // -- Sheet rendering ------------------------------------------------

    private void renderSheet(SheetTask task) {
        // Resolve definition
        SheetDefinition def;
        List<?> data;
        int templateStartRow = task.templateStartRow;

        if (task.definition != null) {
            def = task.definition;
            data = task.data;
        } else if (task.builder != null) {
            var b = SheetDefinition.of(task.name != null ? task.name : "Sheet1");
            task.builder.accept(b);
            def = b.build();
            data = task.data;
        } else {
            // Annotation-driven from POJO class
            data = task.data;
            if (data.isEmpty()) {
                def = SheetDefinition.of(task.name).build();
            } else {
                def = deriveFromAnnotations(task.name, data.getFirst().getClass(), data);
            }
        }

        if (data == null) data = List.of();

        // Reuse existing sheet (template mode) or create new one
        var existingSheet = wb.getSheet(def.name());
        var sheet = existingSheet != null ? existingSheet : wb.createSheet(def.name());
        var renderer = new StyleRenderer(
                (XSSFWorkbook) wb.getXSSFWorkbook(), def.style());
        int rowIdx = 0;

        // Title row
        if (def.title() != null && templateStartRow <= 0) {
            var row = sheet.createRow(rowIdx++);
            var cell = row.createCell(0);
            cell.setCellValue(def.title());
            cell.setCellStyle(renderer.titleStyle());
            if (def.titleCols() > 1) {
                sheet.addMergedRegion(
                        new CellRangeAddress(rowIdx - 1, rowIdx - 1, 0, def.titleCols() - 1));
            }
        }

        // Header row(s)
        if (def.header() != null && templateStartRow <= 0) {
            for (int level = 0; level < def.header().depth(); level++) {
                var row = sheet.createRow(rowIdx++);
                var labels = def.header().labels(level);
                for (int c = 0; c < labels.size(); c++) {
                    var cell = row.createCell(c);
                    cell.setCellValue(labels.get(c));
                    cell.setCellStyle(renderer.headerStyle());
                }
            }
            // Apply header merge regions
            for (var region : def.header().mergeRegions()) {
                int titleOffset = def.title() != null ? 1 : 0;
                sheet.addMergedRegion(new CellRangeAddress(
                        region[0] + titleOffset, region[1] - 1 + titleOffset,
                        region[2], region[3] - 1));
            }
        }

        // Column count
        int colCount = def.columns() != null
                ? def.columns().size()
                : def.header() != null ? def.header().leafCount() : 8;

        // Column extractors for data
        var extractors = buildExtractors(def, data);

        // Data rows
        int dataStartRowIdx = templateStartRow > 0 ? templateStartRow : rowIdx;

        // Pre-scan for merge regions
        Map<String, List<int[]>> mergeRuns = null;
        if (def.mergeColumns() != null && !def.mergeColumns().isEmpty() && !data.isEmpty()) {
            mergeRuns = computeMergeRuns(def, data, extractors);
        }

        for (int i = 0; i < data.size(); i++) {
            var row = sheet.createRow(i + dataStartRowIdx);
            var obj = data.get(i);
            var values = extractors.apply(obj);
            for (int c = 0; c < Math.min(values.size(), colCount); c++) {
                var cell = row.createCell(c);
                var val = values.get(c);
                setCellValue(cell, val);
                cell.setCellStyle(dataCellStyle(def, renderer, i, c));
            }
        }

        // Apply data merge regions
        if (mergeRuns != null) {
            for (var entry : mergeRuns.entrySet()) {
                int col = findColumnIndex(def, entry.getKey());
                if (col < 0) continue;
                for (var run : entry.getValue()) {
                    sheet.addMergedRegion(new CellRangeAddress(
                            run[0] + dataStartRowIdx, run[1] + dataStartRowIdx,
                            col, col));
                }
            }
        }

        // Auto column width
        if (def.autoWidth()) {
            int maxScan = Math.min(data.size(), 200);
            for (int c = 0; c < colCount; c++) {
                int maxLen = 0;
                // Measure header
                if (def.header() != null) {
                    for (var label : def.header().leafLabels()) {
                        if (c < def.header().leafLabels().size()) {
                            maxLen = Math.max(maxLen, def.header().leafLabels().get(c).length());
                        }
                    }
                }
                // Measure data (sample)
                for (int i = 0; i < maxScan; i++) {
                    var values = extractors.apply(data.get(i));
                    if (c < values.size() && values.get(c) != null) {
                        maxLen = Math.max(maxLen, values.get(c).toString().length());
                    }
                }
                sheet.setColumnWidth(c, Math.min((maxLen + 2) * 256, 255 * 256));
            }
        }

        // Fixed column widths from spec
        if (def.columns() != null) {
            for (int c = 0; c < def.columns().size(); c++) {
                int w = def.columns().get(c).width();
                if (w > 0) sheet.setColumnWidth(c, w * 256);
            }
        }
    }

    private static CellStyle dataCellStyle(
            SheetDefinition def, StyleRenderer renderer, int rowIdx, int colIdx) {
        if (def.columns() != null && colIdx < def.columns().size()) {
            var fmt = def.columns().get(colIdx).format();
            if (!fmt.isEmpty()) return renderer.dataStyleWithFormat(fmt);
        }
        // Alternating row color
        var profile = def.style();
        if (!profile.dataAltBgColor().isEmpty() && rowIdx % 2 == 1) {
            return renderer.dataAltStyle();
        }
        return renderer.dataStyle();
    }

    private static void setCellValue(Cell cell, Object val) {
        if (val == null) return;
        switch (val) {
            case String s           -> cell.setCellValue(s);
            case Integer i          -> cell.setCellValue(i);
            case Long l             -> cell.setCellValue(l);
            case Double d           -> cell.setCellValue(d);
            case java.math.BigDecimal bd -> cell.setCellValue(bd.doubleValue());
            case Boolean b          -> cell.setCellValue(b);
            case java.time.LocalDate ld    -> cell.setCellValue(ld);
            case java.time.LocalDateTime ldt -> cell.setCellValue(ldt);
            case java.util.Date dt  -> cell.setCellValue(dt);
            default                 -> cell.setCellValue(val.toString());
        }
    }

    // -- Annotation derivation ------------------------------------------

    private static SheetDefinition deriveFromAnnotations(
            String name, Class<?> type, List<?> data) {
        var sheetAnn = type.getAnnotation(ExcelSheet.class);
        var b = SheetDefinition.of(name);
        if (sheetAnn != null) {
            b.style(sheetAnn.style());
            b.autoWidth(sheetAnn.autoWidth());
        }
        // Derive columns from record components or fields
        var columns = new ArrayList<ColumnSpec>();
        if (type.isRecord()) {
            for (var comp : type.getRecordComponents()) {
                var colAnn = comp.getAnnotation(ExcelColumn.class);
                if (colAnn != null) {
                    var hdr = colAnn.header().isEmpty() ? comp.getName() : colAnn.header();
                    columns.add(new ColumnSpec(
                            comp.getName(), hdr, colAnn.format(),
                            colAnn.width(), colAnn.order()));
                } else {
                    columns.add(ColumnSpec.of(comp.getName(), comp.getName()));
                }
            }
        } else {
            for (var f : type.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                var colAnn = f.getAnnotation(ExcelColumn.class);
                if (colAnn != null) {
                    var hdr = colAnn.header().isEmpty() ? f.getName() : colAnn.header();
                    columns.add(new ColumnSpec(
                            f.getName(), hdr, colAnn.format(),
                            colAnn.width(), colAnn.order()));
                } else {
                    columns.add(ColumnSpec.of(f.getName(), f.getName()));
                }
            }
        }
        if (!columns.isEmpty()) {
            b.simpleHeader(columns.stream().map(ColumnSpec::header).toList());
        }
        for (var c : columns) b.column(c);
        return b.build();
    }

    // -- Extractors -----------------------------------------------------

    private static Function<Object, List<Object>> buildExtractors(
            SheetDefinition def, List<?> data) {
        if (data.isEmpty()) return obj -> List.of();

        var first = data.getFirst();
        // If data is List<Object[]>
        if (first instanceof Object[] arr) {
            return obj -> {
                var a = (Object[]) obj;
                var result = new ArrayList<Object>(a.length);
                for (var item : a) result.add(item);
                return result;
            };
        }
        // If data is List<Map>
        if (first instanceof Map<?, ?> m) {
            if (def.columns() != null) {
                var colFields = def.columns().stream()
                        .map(ColumnSpec::field).toList();
                return obj -> {
                    var map = (Map<?, ?>) obj;
                    var result = new ArrayList<Object>();
                    for (var f : colFields) result.add(map.get(f));
                    return result;
                };
            }
            return obj -> {
                var map = (Map<?, ?>) obj;
                return new ArrayList<>(map.values());
            };
        }

        // POJO — extract fields by name
        var type = first.getClass();
        List<Function<Object, Object>> getters = buildGetters(def, type);
        return obj -> {
            var result = new ArrayList<Object>();
            for (var g : getters) result.add(g.apply(obj));
            return result;
        };
    }

    private static List<Function<Object, Object>> buildGetters(
            SheetDefinition def, Class<?> type) {
        var getters = new ArrayList<Function<Object, Object>>();
        var colFields = def.columns() != null
                ? def.columns().stream().map(ColumnSpec::field).toList()
                : null;

        if (colFields != null && !colFields.isEmpty()) {
            for (var fieldName : colFields) {
                getters.add(makeGetter(type, fieldName));
            }
        } else if (type.isRecord()) {
            for (var comp : type.getRecordComponents()) {
                try {
                    var m = type.getMethod(comp.getName());
                    m.setAccessible(true);  // needed when target class is package-private
                    getters.add(obj -> {
                        try { return m.invoke(obj); }
                        catch (Exception e) { return null; }
                    });
                } catch (NoSuchMethodException ignored) {}
            }
        } else {
            for (var f : type.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                f.setAccessible(true);
                getters.add(obj -> {
                    try { return f.get(obj); }
                    catch (Exception e) { return null; }
                });
            }
        }
        return getters;
    }

    private static Function<Object, Object> makeGetter(Class<?> type, String fieldName) {
        if (type.isRecord()) {
            try {
                var m = type.getMethod(fieldName);
                m.setAccessible(true);  // needed when target class is package-private
                return obj -> {
                    try { return m.invoke(obj); }
                    catch (Exception e) { return null; }
                };
            } catch (NoSuchMethodException ignored) {}
        }
        try {
            var f = type.getDeclaredField(fieldName);
            f.setAccessible(true);
            return obj -> {
                try { return f.get(obj); }
                catch (Exception e) { return null; }
            };
        } catch (NoSuchFieldException e) {
            return obj -> null;
        }
    }

    // -- Merge computation ----------------------------------------------

    private static Map<String, List<int[]>> computeMergeRuns(
            SheetDefinition def, List<?> data,
            Function<Object, List<Object>> extractors) {
        if (data.isEmpty()) return Map.of();
        var result = new LinkedHashMap<String, List<int[]>>();
        for (var colField : def.mergeColumns()) {
            var runs = new ArrayList<int[]>();
            int runStart = 0;
            Object prev = null;
            for (int i = 0; i < data.size(); i++) {
                var values = extractors.apply(data.get(i));
                int ci = findColumnIndex(def, colField);
                Object curr = ci >= 0 && ci < values.size() ? values.get(ci) : null;
                if (i > 0 && !Objects.equals(curr, prev)) {
                    if (i - runStart > 1) {
                        runs.add(new int[]{runStart, i - 1});
                    }
                    runStart = i;
                }
                prev = curr;
            }
            if (data.size() - runStart > 1) {
                runs.add(new int[]{runStart, data.size() - 1});
            }
            if (!runs.isEmpty()) result.put(colField, runs);
        }
        return result;
    }

    private static int findColumnIndex(SheetDefinition def, String field) {
        if (def.columns() != null) {
            for (int i = 0; i < def.columns().size(); i++) {
                if (def.columns().get(i).field().equals(field)) return i;
            }
        }
        if (def.header() != null) {
            var leaves = def.header().leafLabels();
            for (int i = 0; i < leaves.size(); i++) {
                if (leaves.get(i).equalsIgnoreCase(field)) return i;
            }
        }
        return -1;
    }

    // -- Task record ----------------------------------------------------

    // Using a simple class to avoid record complexity with nulls
    static final class SheetTask {
        final String name;
        final SheetDefinition definition;
        final Consumer<SheetDefinition.Builder> builder;
        final List<?> data;
        final int templateStartRow;   // >0 means template fill mode

        SheetTask(String name, SheetDefinition definition,
                  Consumer<SheetDefinition.Builder> builder, List<?> data) {
            this.name = name;
            this.definition = definition;
            this.builder = builder;
            this.data = data;
            this.templateStartRow = 0;
        }

        SheetTask(String name, SheetDefinition definition, int templateStartRow, List<?> data) {
            this.name = name;
            this.definition = definition;
            this.builder = null;
            this.data = data;
            this.templateStartRow = templateStartRow;
        }
    }
}
