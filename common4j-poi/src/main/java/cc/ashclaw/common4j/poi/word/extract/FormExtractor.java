package cc.ashclaw.common4j.poi.word.extract;

import cc.ashclaw.common4j.core.convert.ConvertUtils;
import cc.ashclaw.common4j.poi.word.annotation.WordField;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Extracts label-value pairs from Word document tables.
 *
 * <p>Scans tables for rows where the first cell is a label and the
 * second cell is a value — the most common layout in contracts,
 * registration forms, and info sheets.
 *
 * <pre>{@code
 * Map<String, String> fields = Word.read(doc).toFields();
 * Contract c = Word.read(doc).toBean(Contract.class);
 * }</pre>
 */
public final class FormExtractor {

    private final XWPFDocument doc;
    private int tableIndex = -1; // -1 = scan all tables

    /**
     * Creates a new form extractor for the given document.
     *
     * @param doc the Word document to extract from, must not be null
     */
    public FormExtractor(XWPFDocument doc) {
        this.doc = Objects.requireNonNull(doc, "doc must not be null");
    }

    /**
     * Targets a specific table by index (0-based). By default, scans all tables.
     *
     * @param index the zero-based table index
     * @return this instance for method chaining
     */
    public FormExtractor table(int index) {
        this.tableIndex = index;
        return this;
    }

    // -- Map output --------------------------------------------------------

    /**
     * Extracts all label-value pairs into a map.
     * Keys and values are trimmed strings; empty labels are skipped.
     *
     * @return an ordered map of label → value pairs
     */
    public Map<String, String> toFields() {
        var result = new LinkedHashMap<String, String>();
        for (var row : collectRows()) {
            extractPairs(row).forEach(pair -> {
                if (!pair.label().isEmpty()) {
                    result.put(pair.label(), pair.value());
                }
            });
        }
        return result;
    }

    // -- POJO output -------------------------------------------------------

    /**
     * Maps label-value pairs to a POJO or record using {@link WordField} annotations.
     *
     * @param <T>  the target type
     * @param type the target class (POJO or record), must have a no-arg constructor or canonical constructor
     * @return the populated instance
     * @throws IllegalArgumentException if a required label is missing
     */
    public <T> T toBean(Class<T> type) {
        var fields = toFields();
        if (type.isRecord()) {
            return mapToRecord(type, fields);
        } else {
            return mapToPojo(type, fields);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T mapToRecord(Class<T> type, Map<String, String> fields) {
        var components = type.getRecordComponents();
        var args = new Object[components.length];
        for (int i = 0; i < components.length; i++) {
            var comp = components[i];
            var ann = comp.getAnnotation(WordField.class);
            if (ann == null) {
                args[i] = ConvertUtils.defaultValue(comp.getType());
                continue;
            }
            var raw = fields.get(ann.label());
            args[i] = raw == null ? ConvertUtils.defaultValue(comp.getType())
                                  : ConvertUtils.coerce(raw, comp.getType(), ann.format());
        }
        try {
            var paramTypes = new Class<?>[components.length];
            for (int i = 0; i < components.length; i++) paramTypes[i] = components[i].getType();
            var ctor = type.getDeclaredConstructor(paramTypes);
            ctor.setAccessible(true);
            return (T) ctor.newInstance(args);
        } catch (Exception e) {
            throw new RuntimeException("cannot instantiate " + type.getSimpleName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T mapToPojo(Class<T> type, Map<String, String> fields) {
        try {
            var ctor = type.getDeclaredConstructor();
            ctor.setAccessible(true);
            var instance = ctor.newInstance();
            for (var f : collectWordFields(type)) {
                var raw = fields.get(f.getAnnotation(WordField.class).label());
                if (raw == null) continue;
                f.setAccessible(true);
                f.set(instance, ConvertUtils.coerce(raw, f.getType(), f.getAnnotation(WordField.class).format()));
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("cannot instantiate " + type.getSimpleName(), e);
        }
    }

    // -- Table scanning ----------------------------------------------------

    private List<XWPFTableRow> collectRows() {
        var rows = new ArrayList<XWPFTableRow>();
        if (tableIndex >= 0) {
            var tables = doc.getTables();
            if (tableIndex >= tables.size()) {
                throw new IllegalArgumentException(
                    "table %d not found (total: %d)".formatted(tableIndex, tables.size()));
            }
            for (var row : tables.get(tableIndex).getRows()) rows.add(row);
        } else {
            for (var table : doc.getTables()) {
                for (var row : table.getRows()) rows.add(row);
            }
        }
        return rows;
    }

    /** Extracts 0-to-many label-value pairs from a single row. */
    private static List<Pair> extractPairs(XWPFTableRow row) {
        var cells = row.getTableCells();
        if (cells.size() < 2) return List.of();
        var pairs = new ArrayList<Pair>();
        // Walk cells in pairs: (label, value), (label, value), ...
        for (int i = 0; i + 1 < cells.size(); i += 2) {
            String label = cellText(cells.get(i)).trim();
            String value = cellText(cells.get(i + 1)).trim();
            pairs.add(new Pair(label, value));
        }
        return pairs;
    }

    /** Extracts plain text from a cell, stripping all formatting. */
    static String cellText(XWPFTableCell cell) {
        var sb = new StringBuilder();
        for (var p : cell.getParagraphs()) {
            if (!sb.isEmpty()) sb.append('\n');
            sb.append(p.getText());
        }
        return sb.toString();
    }

    // -- Reflection helpers ------------------------------------------------

    private static List<Field> collectWordFields(Class<?> type) {
        var fields = new ArrayList<Field>();
        for (var f : type.getDeclaredFields()) {
            if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
            if (f.isAnnotationPresent(WordField.class)) fields.add(f);
        }
        return fields;
    }

    // -- Internal ----------------------------------------------------------

    private record Pair(String label, String value) {}
}
