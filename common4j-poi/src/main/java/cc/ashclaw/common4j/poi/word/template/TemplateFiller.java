package cc.ashclaw.common4j.poi.word.template;

import cc.ashclaw.common4j.poi.word.annotation.WordField;

import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Fills Word templates by replacing {@code ${key}} placeholders with data.
 *
 * <p>Handles cross-run placeholders (where {@code ${na} + me} spans multiple
 * runs) by merging runs during replacement. Supports table row expansion
 * for list data.
 *
 * <h3>Simple replacement</h3>
 * <pre>{@code
 * Word.fromTemplate(in)
 *     .put("partyA", "Acme Corp")
 *     .put("date", LocalDate.now())
 *     .to(out);
 * }</pre>
 *
 * <h3>Table row expansion</h3>
 * <pre>{@code
 * Word.fromTemplate(in)
 *     .table("items", itemList)  // expands ${items.name}, ${items.price}
 *     .to(out);
 * }</pre>
 *
 * <h3>POJO-driven fill</h3>
 * <pre>{@code
 * Word.fromTemplate(in)
 *     .fill(contract)  // reads record fields or bean properties
 *     .to(out);
 * }</pre>
 */
public final class TemplateFiller {

    // Matches ${key} where key is a letter/digit/underscore sequence with optional dot-nesting.
    // Supports Unicode letters via \p{L}.
    private static final Pattern PLACEHOLDER =
        Pattern.compile("\\$\\{([\\p{L}_][\\p{L}\\p{N}_]*(?:\\.[\\p{L}_][\\p{L}\\p{N}_]*)*)\\}");

    private final XWPFDocument doc;
    private final Map<String, String> values = new LinkedHashMap<>();
    private final Map<String, List<?>> tableData = new LinkedHashMap<>();
    private final Map<String, Integer> tableIndexes = new LinkedHashMap<>();

    public TemplateFiller(XWPFDocument doc) {
        this.doc = Objects.requireNonNull(doc, "doc must not be null");
    }

    // -- Data input --------------------------------------------------------

    /** Sets a placeholder value. {@code ${key}} in the template will be replaced with value. */
    public TemplateFiller put(String key, Object value) {
        Objects.requireNonNull(key, "key must not be null");
        values.put(key, value == null ? "" : formatValue(value));
        return this;
    }

    /**
     * Fills placeholders from a record or POJO.
     * For records, component names become keys.
     * For POJOs with {@link cc.ashclaw.common4j.poi.word.annotation.WordField},
     * the annotation label becomes the key; otherwise field names become keys.
     */
    public TemplateFiller fill(Object bean) {
        Objects.requireNonNull(bean, "bean must not be null");
        var type = bean.getClass();
        if (type.isRecord()) {
            for (var comp : type.getRecordComponents()) {
                try {
                    var m = type.getMethod(comp.getName());
                    m.setAccessible(true);
                    var v = m.invoke(bean);
                    var ann = comp.getAnnotation(WordField.class);
                    var key = ann != null ? ann.label() : comp.getName();
                    put(key, v);
                } catch (Exception ignored) {}
            }
        } else {
            for (var f : type.getDeclaredFields()) {
                if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                try {
                    f.setAccessible(true);
                    var ann = f.getAnnotation(WordField.class);
                    var key = ann != null ? ann.label() : f.getName();
                    put(key, f.get(bean));
                } catch (Exception ignored) {}
            }
        }
        return this;
    }

    /**
     * Expands a table row for each item in the list.
     * Cells containing {@code ${key.field}} in the template table are replaced
     * with the corresponding field value from each item.
     *
     * <p>Items can be records, POJOs with {@code WordField}, or Maps.
     * Scans all tables for a row containing {@code ${key.*}}.
     */
    public TemplateFiller table(String key, List<?> data) {
        return table(-1, key, data);
    }

    /**
     * Expands a table row for each item in the list, targeting a specific table.
     *
     * @param tableIndex 0-based table index, or -1 to scan all tables
     * @param key        the placeholder prefix (e.g. "items" for {@code ${items.name}})
     * @param data       the list of items to expand
     */
    public TemplateFiller table(int tableIndex, String key, List<?> data) {
        Objects.requireNonNull(key, "key must not be null");
        Objects.requireNonNull(data, "data must not be null");
        tableData.put(key, data);
        tableIndexes.put(key, tableIndex);
        return this;
    }

    // -- Output ------------------------------------------------------------

    /** Writes the filled document to an output stream. */
    public void to(OutputStream os) throws IOException {
        // Phase 1: process table expansions first (cloning rows)
        processTableExpansions();

        // Phase 2: process paragraph and remaining table-cell placeholders
        for (var p : doc.getParagraphs()) {
            replaceInParagraph(p, values);
        }
        for (var table : doc.getTables()) {
            for (var row : table.getRows()) {
                for (var cell : row.getTableCells()) {
                    for (var p : cell.getParagraphs()) {
                        replaceInParagraph(p, values);
                    }
                }
            }
        }

        doc.write(os);
    }

    /** Writes the filled document to a file. */
    public void to(Path path) throws IOException {
        try (var os = Files.newOutputStream(path)) {
            to(os);
        }
    }

    // -- Table expansion ---------------------------------------------------

    @SuppressWarnings("unchecked")
    private void processTableExpansions() {
        var tables = doc.getTables();
        for (var entry : tableData.entrySet()) {
            String prefix = entry.getKey();
            List<?> items = entry.getValue();
            int targetIdx = tableIndexes.getOrDefault(prefix, -1);

            // If a specific table is targeted, only check that one
            if (targetIdx >= 0) {
                if (targetIdx >= tables.size()) {
                    throw new IllegalArgumentException(
                        "table %d not found (total: %d)".formatted(targetIdx, tables.size()));
                }
                expandTable(tables.get(targetIdx), prefix, items);
            } else {
                // Scan all tables for the first match
                for (var table : tables) {
                    if (expandTable(table, prefix, items)) break;
                }
            }
        }
    }

    /** Expands template rows in the given table. Returns true if a template row was found. */
    private boolean expandTable(XWPFTable table, String prefix, List<?> items) {
        int templateRowIdx = findTemplateRow(table, prefix);
        if (templateRowIdx < 0) return false;

        var templateRow = table.getRows().get(templateRowIdx);
        var templateCtRow = templateRow.getCtRow();

        if (!items.isEmpty()) {
            // Insert cloned rows directly at the right position via CT-level insert
            int insertPos = templateRowIdx + 1;
            for (int i = items.size() - 1; i >= 0; i--) {
                var item = items.get(i);
                var newCtRow = table.getCTTbl().insertNewTr(insertPos);
                // Copy template content into the new row via XmlObject.set()
                newCtRow.set(templateCtRow);
                var newRow = new XWPFTableRow(newCtRow, table);
                // Replace placeholders
                var itemVals = itemValues(prefix, item);
                replaceRowPlaceholders(newRow, itemVals);
            }
        }

        // Remove template row
        table.removeRow(templateRowIdx);
        return true;
    }

    /** Replaces placeholders in all cells of a row by working at CT paragraph level. */
    private static void replaceRowPlaceholders(XWPFTableRow row, Map<String, String> values) {
        for (var cell : row.getTableCells()) {
            for (var p : cell.getParagraphs()) {
                replaceInParagraph(p, values);
            }
        }
    }

    /** Finds the first row containing {@code ${prefix.*}}. */
    private static int findTemplateRow(XWPFTable table, String prefix) {
        var rows = table.getRows();
        for (int r = 0; r < rows.size(); r++) {
            var row = rows.get(r);
            for (var cell : row.getTableCells()) {
                for (var p : cell.getParagraphs()) {
                    var m = PLACEHOLDER.matcher(p.getText());
                    while (m.find()) {
                        String key = m.group(1);
                        if (key.startsWith(prefix + ".")) return r;
                    }
                }
            }
        }
        return -1;
    }

    /** Builds a value map for one item's fields, keyed by {@code prefix.field}. */
    private static Map<String, String> itemValues(String prefix, Object item) {
        var map = new LinkedHashMap<String, String>();
        if (item instanceof Map<?, ?> m) {
            for (var e : m.entrySet()) {
                map.put(prefix + "." + e.getKey(), formatValue(e.getValue()));
            }
        } else {
            var type = item.getClass();
            if (type.isRecord()) {
                for (var comp : type.getRecordComponents()) {
                    try {
                        var m = type.getMethod(comp.getName());
                        m.setAccessible(true);
                        map.put(prefix + "." + comp.getName(), formatValue(m.invoke(item)));
                    } catch (Exception ignored) {}
                }
            } else {
                for (var f : type.getDeclaredFields()) {
                    if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) continue;
                    try {
                        f.setAccessible(true);
                        map.put(prefix + "." + f.getName(), formatValue(f.get(item)));
                    } catch (Exception ignored) {}
                }
            }
        }
        return map;
    }

    /**
     * Replaces all {@code ${key}} placeholders in a paragraph with values from
     * the given map. Handles placeholders that span multiple runs.
     */
    static void replaceInParagraph(XWPFParagraph paragraph, Map<String, String> values) {
        // Collect run texts and their positions
        var runs = paragraph.getRuns();
        if (runs.isEmpty()) return;

        var runTexts = new ArrayList<String>();
        for (var run : runs) {
            String text = run.getText(0);
            runTexts.add(text != null ? text : "");
        }

        // Build full text and track cumulative offsets
        var fullText = new StringBuilder();
        var offsets = new int[runTexts.size()];
        for (int i = 0; i < runTexts.size(); i++) {
            offsets[i] = fullText.length();
            fullText.append(runTexts.get(i));
        }

        String text = fullText.toString();
        var matcher = PLACEHOLDER.matcher(text);

        // Find all matches
        record Match(int start, int end, String key, String replacement) {}
        var matches = new ArrayList<Match>();
        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = values.get(key);
            if (replacement != null) {
                matches.add(new Match(matcher.start(), matcher.end(), key, replacement));
            }
        }

        if (matches.isEmpty()) return;

        // Apply replacements from end to start (preserving earlier offsets)
        for (int mi = matches.size() - 1; mi >= 0; mi--) {
            var m = matches.get(mi);
            applyReplacement(runs, runTexts, offsets, m.start, m.end, m.replacement);
            // Update runTexts to reflect changes for subsequent replacements
            runTexts.clear();
            for (var run : runs) {
                String t = run.getText(0);
                runTexts.add(t != null ? t : "");
            }
            fullText.setLength(0);
            for (int i = 0; i < runTexts.size(); i++) {
                offsets[i] = fullText.length();
                fullText.append(runTexts.get(i));
            }
        }
    }

    /**
     * Applies a single replacement across potentially multiple runs.
     * Clears fully-consumed runs and updates the start/end runs.
     */
    private static void applyReplacement(List<XWPFRun> runs, List<String> runTexts,
                                          int[] offsets, int replaceStart, int replaceEnd,
                                          String replacement) {
        // Find which runs contain the replacement span
        int startRun = -1, endRun = -1;
        for (int i = 0; i < runs.size(); i++) {
            int runStart = offsets[i];
            int runEnd = runStart + runTexts.get(i).length();
            if (runStart <= replaceStart && replaceStart < runEnd) startRun = i;
            if (runStart < replaceEnd && replaceEnd <= runEnd) endRun = i;
        }

        if (startRun < 0 || endRun < 0) return;

        // Case: replacement is entirely within one run
        if (startRun == endRun) {
            var run = runs.get(startRun);
            String t = runTexts.get(startRun);
            int localStart = replaceStart - offsets[startRun];
            int localEnd = replaceEnd - offsets[startRun];
            String newText = t.substring(0, localStart) + replacement + t.substring(localEnd);
            run.setText(newText, 0);
            return;
        }

        // Case: replacement spans multiple runs
        // Update first run: keep text before replaceStart + replacement
        var firstRun = runs.get(startRun);
        String firstText = runTexts.get(startRun);
        int firstLocalStart = replaceStart - offsets[startRun];
        firstRun.setText(firstText.substring(0, firstLocalStart) + replacement, 0);

        // Update last run: keep text after replaceEnd
        var lastRun = runs.get(endRun);
        String lastText = runTexts.get(endRun);
        int lastLocalEnd = replaceEnd - offsets[endRun];
        lastRun.setText(lastText.substring(lastLocalEnd), 0);

        // Clear all runs between startRun+1 and endRun
        for (int i = startRun + 1; i < endRun; i++) {
            runs.get(i).setText("", 0);
        }
        // Clear last run's beginning (we already set its remaining text)
        // Actually, we've already set lastRun's text to the suffix.
        // Now clear text from runs between startRun+1 and endRun-1
    }

    // -- Formatting --------------------------------------------------------

    static String formatValue(Object value) {
        if (value == null) return "";
        return switch (value) {
            case String s            -> s;
            case Integer i           -> i.toString();
            case Long l              -> l.toString();
            case Double d            -> d.toString();
            case BigDecimal bd       -> bd.toPlainString();
            case Boolean b           -> b.toString();
            case LocalDate ld        -> ld.toString();
            case LocalDateTime ldt   -> ldt.toString();
            case Date dt             -> dt.toInstant().toString();
            default                  -> value.toString();
        };
    }
}
