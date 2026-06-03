package cc.ashclaw.common4j.poi.excel.reader;

import cc.ashclaw.common4j.core.convert.ConvertUtils;
import cc.ashclaw.common4j.poi.excel.ParseError;
import cc.ashclaw.common4j.poi.excel.ReadResult;
import cc.ashclaw.common4j.poi.excel.annotation.ExcelColumn;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Reads Excel files into POJO lists or generic row maps.
 *
 * <p>Use via {@link cc.ashclaw.common4j.poi.excel.Excel#read(java.io.InputStream)} —
 * not constructed directly.
 *
 * <pre>{@code
 * List<User> users = Excel.read(file).sheet(0).toList(User.class);
 * }</pre>
 */
public final class ExcelReader implements AutoCloseable {

    private final XSSFWorkbook wb;
    private int    sheetIndex    = 0;
    private String sheetName;
    private int    headerRow     = 0;
    private int    dataStartRow  = 1;
    private int    dataEndRow    = -1;   // -1 = auto

    public ExcelReader(XSSFWorkbook wb) {
        this.wb = Objects.requireNonNull(wb, "workbook must not be null");
    }

    /** Select a sheet by index (0-based). */
    public ExcelReader sheet(int index) {
        this.sheetIndex = index;
        this.sheetName = null;
        return this;
    }

    /** Select a sheet by name. */
    public ExcelReader sheet(String name) {
        this.sheetName = Objects.requireNonNull(name, "name must not be null");
        return this;
    }

    /** Set the header row index (0-based). Default 0. */
    public ExcelReader headerRow(int row) {
        this.headerRow = row;
        return this;
    }

    /** Set the first data row index (0-based). Default 1. */
    public ExcelReader dataStartRow(int row) {
        this.dataStartRow = row;
        return this;
    }

    /** Set the last data row index (0-based, inclusive). Default -1 means auto (to last row). */
    public ExcelReader dataEndRow(int row) {
        this.dataEndRow = row;
        return this;
    }

    /**
     * Reads all data rows and maps them to instances of the given type.
     * The type must be a record or have a no-arg constructor and
     * {@link ExcelColumn}-annotated fields.
     *
     * <p>This method closes the underlying workbook after reading.
     * For multi-sheet reads, create separate {@code ExcelReader} instances
     * via {@link cc.ashclaw.common4j.poi.excel.Excel#read(InputStream)}.
     */
    public <T> List<T> toList(Class<T> type) {
        try (this) {
            return toResult(type).data();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Reads data rows, returning both mapped objects and parse errors.
     *
     * <p>This method closes the underlying workbook after reading.
     */
    public <T> ReadResult<T> toResult(Class<T> type) {
        try (this) {
            return readTyped(type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private <T> ReadResult<T> readTyped(Class<T> type) {
        var sheet = resolveSheet();
        var headers = readHeaders(sheet);
        var mapper = new RowMapper<>(type, headers);
        int lastRow = dataEndRow >= 0 ? dataEndRow : sheet.getLastRowNum();
        var data = new ArrayList<T>();
        var errors = new ArrayList<ParseError>();

        for (int r = dataStartRow; r <= lastRow; r++) {
            var row = sheet.getRow(r);
            if (row == null || isRowBlank(row)) continue;
            try {
                data.add(mapper.map(row));
            } catch (Exception e) {
                errors.add(new ParseError(r, -1, e.getMessage()));
            }
        }
        return new ReadResult<>(List.copyOf(data), List.copyOf(errors));
    }

    /**
     * Reads all data rows as maps (header → cell string value).
     *
     * <p>This method closes the underlying workbook after reading.
     */
    public List<Map<String, String>> toMaps() {
        try (this) {
            return readMaps();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<Map<String, String>> readMaps() {
        var sheet = resolveSheet();
        var headers = readHeaders(sheet);
        var fmt = new DataFormatter();
        int lastRow = dataEndRow >= 0 ? dataEndRow : sheet.getLastRowNum();
        var result = new ArrayList<Map<String, String>>();
        for (int r = dataStartRow; r <= lastRow; r++) {
            var row = sheet.getRow(r);
            if (row == null || isRowBlank(row)) continue;
            var map = new LinkedHashMap<String, String>();
            for (int c = 0; c < headers.size(); c++) {
                var cell = row.getCell(c);
                map.put(headers.get(c), cell == null ? "" : fmt.formatCellValue(cell));
            }
            result.add(map);
        }
        return result;
    }

    /**
     * Reads all data rows as maps with raw cell values (typed, not string-formatted).
     * Numeric cells return {@code Double}, dates return {@code LocalDateTime},
     * booleans return {@code Boolean}, strings return {@code String}.
     *
     * <p>This method closes the underlying workbook after reading.
     */
    public List<Map<String, Object>> toRawMaps() {
        try (this) {
            return readRawMaps();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private List<Map<String, Object>> readRawMaps() {
        var sheet = resolveSheet();
        var headers = readHeaders(sheet);
        int lastRow = dataEndRow >= 0 ? dataEndRow : sheet.getLastRowNum();
        var result = new ArrayList<Map<String, Object>>();
        for (int r = dataStartRow; r <= lastRow; r++) {
            var row = sheet.getRow(r);
            if (row == null || isRowBlank(row)) continue;
            var map = new LinkedHashMap<String, Object>();
            for (int c = 0; c < headers.size(); c++) {
                var cell = row.getCell(c);
                map.put(headers.get(c), cell == null ? null : rawCellValue(cell));
            }
            result.add(map);
        }
        return result;
    }

    private static Object rawCellValue(Cell cell) {
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue();
            case NUMERIC -> {
                if (org.apache.poi.ss.usermodel.DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue();
                }
                yield cell.getNumericCellValue();
            }
            case BOOLEAN -> cell.getBooleanCellValue();
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception e1) {
                    try {
                        yield cell.getNumericCellValue();
                    } catch (Exception e2) {
                        yield cell.getCellFormula();  // error formula like #REF!
                    }
                }
            }
            case BLANK -> null;
            default    -> null;
        };
    }

    @Override
    public void close() throws IOException {
        wb.close();
    }

    // -- Internal --------------------------------------------------------

    private Sheet resolveSheet() {
        if (sheetName != null) {
            var s = wb.getSheet(sheetName);
            if (s == null) throw new IllegalArgumentException("sheet not found: " + sheetName);
            return s;
        }
        return wb.getSheetAt(sheetIndex);
    }

    private List<String> readHeaders(Sheet sheet) {
        var row = sheet.getRow(headerRow);
        if (row == null) return List.of();
        var fmt = new DataFormatter();
        var headers = new ArrayList<String>();
        for (int c = 0; c < row.getLastCellNum(); c++) {
            var cell = row.getCell(c);
            headers.add(cell == null ? "" : fmt.formatCellValue(cell).trim());
        }
        return List.copyOf(headers);
    }

    private static boolean isRowBlank(Row row) {
        for (int c = 0; c < row.getLastCellNum(); c++) {
            var cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) return false;
        }
        return true;
    }
}

// ─────────────────────────────────────────────────────────────────

/**
 * Maps a POI {@link Row} to a POJO using {@link ExcelColumn} annotations.
 */
final class RowMapper<T> {

    private final Class<T>              type;
    private final boolean               isRecord;
    private final List<ColumnBinding>   bindings;
    private final Constructor<T>        constructor;
    private final List<Field>           fields;

    RowMapper(Class<T> type, List<String> headers) {
        this.type = type;
        this.isRecord = type.isRecord();
        if (isRecord) {
            this.constructor = recordConstructor(type);
            this.fields = null;
            this.bindings = buildRecordBindings(constructor, headers);
        } else {
            this.constructor = noArgConstructor(type);
            this.fields = collectFields(type);
            this.bindings = buildFieldBindings(fields, headers);
        }
    }

    T map(Row row) {
        if (isRecord) {
            try {
                var args = new Object[bindings.size()];
                for (var b : bindings) {
                    args[b.paramIndex] = readCell(row, b.colIndex, b.paramType, b.format);
                }
                return constructor.newInstance(args);
            } catch (Exception e) {
                throw new RuntimeException(
                    "%s row %d: %s".formatted(
                        type.getSimpleName(), row.getRowNum() + 1, e.getMessage()), e);
            }
        } else {
            try {
                var instance = constructor.newInstance();
                for (var b : bindings) {
                    var value = readCell(row, b.colIndex, b.field.getType(), b.format);
                    b.field.setAccessible(true);
                    b.field.set(instance, value);
                }
                return instance;
            } catch (Exception e) {
                throw new RuntimeException(
                    "%s row %d: %s".formatted(
                        type.getSimpleName(), row.getRowNum() + 1, e.getMessage()), e);
            }
        }
    }

    // -- Binding resolution ---------------------------------------------

    private static record ColumnBinding(int paramIndex, int colIndex,
                                        Class<?> paramType, String format,
                                        Field field) {}

    private static List<ColumnBinding> buildRecordBindings(
            Constructor<?> ctor, List<String> headers) {
        var params = ctor.getParameters();
        var bindings = new ArrayList<ColumnBinding>();
        for (int i = 0; i < params.length; i++) {
            var ann = params[i].getAnnotation(ExcelColumn.class);
            int ci = resolveColIndex(ann, params[i].getName(), i, headers);
            var fmt = ann != null ? ann.format() : "";
            bindings.add(new ColumnBinding(i, ci, params[i].getType(), fmt, null));
        }
        return bindings;
    }

    private static List<ColumnBinding> buildFieldBindings(
            List<Field> fields, List<String> headers) {
        var bindings = new ArrayList<ColumnBinding>();
        for (int i = 0; i < fields.size(); i++) {
            var f = fields.get(i);
            var ann = f.getAnnotation(ExcelColumn.class);
            int ci = resolveColIndex(ann, f.getName(), i, headers);
            var fmt = ann != null ? ann.format() : "";
            bindings.add(new ColumnBinding(i, ci, f.getType(), fmt, f));
        }
        return bindings;
    }

    private static int resolveColIndex(
            ExcelColumn ann, String fieldName, int fallback,
            List<String> headers) {
        if (ann == null) return fallback;
        if (ann.index() >= 0) return ann.index();
        if (!ann.header().isEmpty() && !headers.isEmpty()) {
            for (int i = 0; i < headers.size(); i++) {
                if (headers.get(i).equalsIgnoreCase(ann.header())) return i;
            }
        }
        return fallback;
    }

    // -- Constructors ---------------------------------------------------

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> recordConstructor(Class<T> type) {
        var components = type.getRecordComponents();
        var paramTypes = new Class<?>[components.length];
        for (int i = 0; i < components.length; i++) {
            paramTypes[i] = components[i].getType();
        }
        try {
            var ctor = type.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);  // needed when target class is package-private
            return (Constructor<T>) ctor;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("cannot find canonical constructor for " + type, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Constructor<T> noArgConstructor(Class<T> type) {
        try {
            var ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);  // needed when target class is package-private
            return (Constructor<T>) ctor;
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("cannot find no-arg constructor for " + type, e);
        }
    }

    private static List<Field> collectFields(Class<?> type) {
        var fields = new ArrayList<Field>();
        for (var f : type.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
            if (f.isAnnotationPresent(ExcelColumn.class)) {
                fields.add(f);
                continue;
            }
            // If no annotations anywhere, include non-static fields in declaration order
            if (!hasAnyExcelColumn(type)) {
                fields.add(f);
            }
        }
        fields.sort((a, b) -> {
            var aa = a.getAnnotation(ExcelColumn.class);
            var bb = b.getAnnotation(ExcelColumn.class);
            int oa = aa != null ? aa.order() : 0;
            int ob = bb != null ? bb.order() : 0;
            return Integer.compare(oa, ob);
        });
        return fields;
    }

    private static boolean hasAnyExcelColumn(Class<?> type) {
        for (var f : type.getDeclaredFields()) {
            if (f.isAnnotationPresent(ExcelColumn.class)) return true;
        }
        return false;
    }

    // -- Cell reading ---------------------------------------------------

    private static Object readCell(Row row, int colIndex,
                                   Class<?> targetType, String format) {
        var cell = row.getCell(colIndex);
        if (cell == null) return ConvertUtils.defaultValue(targetType);
        try {
            return switch (cell.getCellType()) {
                case STRING  -> ConvertUtils.coerce(cell.getStringCellValue(), targetType, format);
                case NUMERIC -> coerceNumeric(cell, targetType);
                case BOOLEAN -> cell.getBooleanCellValue();
                case FORMULA -> {
                    try {
                        yield ConvertUtils.coerce(cell.getStringCellValue(), targetType, format);
                    } catch (Exception e1) {
                        try {
                            yield coerceNumeric(cell, targetType);
                        } catch (Exception e2) {
                            // Error formula (#REF!, #DIV/0!, etc.)
                            var formula = cell.getCellFormula();
                            throw new IllegalArgumentException(
                                "error formula \"%s\"".formatted(formula));
                        }
                    }
                }
                case BLANK -> ConvertUtils.defaultValue(targetType);
                default    -> ConvertUtils.defaultValue(targetType);
            };
        } catch (IllegalArgumentException e) {
            // Enrich with row/column context
            throw new IllegalArgumentException(
                "row %d, col %s: %s".formatted(
                    row.getRowNum() + 1, columnLabel(colIndex), e.getMessage()), e);
        }
    }

    /** Converts a 0-based column index to an Excel column label (A, B, ..., Z, AA, ...). */
    private static String columnLabel(int idx) {
        var sb = new StringBuilder();
        for (int n = idx; n >= 0; n = n / 26 - 1) {
            sb.append((char) ('A' + n % 26));
        }
        return sb.reverse().toString();
    }

    private static Object coerceNumeric(Cell cell, Class<?> targetType) {
        double v = cell.getNumericCellValue();
        if (targetType == int.class || targetType == Integer.class) return (int) v;
        if (targetType == long.class || targetType == Long.class) return (long) v;
        if (targetType == double.class || targetType == Double.class) return v;
        if (targetType == BigDecimal.class) return BigDecimal.valueOf(v);
        if (targetType == String.class) return new DataFormatter().formatCellValue(cell);

        // Try date conversion for date-typed targets
        // (getLocalDateTimeCellValue works even when our custom style overwrites the date format)
        if (targetType == LocalDate.class || targetType == LocalDateTime.class
                || targetType == Date.class) {
            try {
                var d = cell.getLocalDateTimeCellValue();
                if (targetType == LocalDate.class) return d.toLocalDate();
                if (targetType == Date.class)
                    return Date.from(d.atZone(ZoneId.systemDefault()).toInstant());
                return d; // LocalDateTime
            } catch (Exception ignored) {
                throw new IllegalArgumentException(
                    "cannot convert numeric value %s to %s".formatted(v, targetType.getSimpleName()));
            }
        }

        throw new IllegalArgumentException(
            "cannot convert numeric value %s to %s".formatted(v, targetType.getSimpleName()));
    }
}

