package cc.ashclaw.common4j.poi.excel.define;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Tree node representing a multi-level (complex) header.
 *
 * <p>Each level is a row of header labels. Adjacent identical labels
 * are automatically merged horizontally. The tree flattens to a list
 * of leaf column paths, each paired with its column index.
 *
 * <pre>{@code
 * HeaderNode.of(
 *     List.of("",    "",    "H1",  "H1"),
 *     List.of("Name", "Age", "Rev", "Cost")
 * );
 * // Produces:
 * // Row 0: [        ][  H1        ]
 * // Row 1: [Name][Age][Rev  ][Cost ]
 * }</pre>
 */
public sealed interface HeaderNode permits HeaderNode.SingleLevel, HeaderNode.MultiLevel {

    /** Number of header rows (tree depth). */
    int depth();

    /** Total leaf-column count. */
    int leafCount();

    /** Labels at the given depth level (0 = top row). */
    List<String> labels(int level);

    /**
     * Merge regions for the header rows (top-left inclusive, bottom-right exclusive):
     * each entry is {@code [firstRow, lastRowExcl, firstCol, lastColExcl]}.
     */
    List<int[]> mergeRegions();

    /** Returns the leaf labels in column order (the bottom row). */
    List<String> leafLabels();

    // -- Factory methods -------------------------------------------------

    /** Single-row header. */
    static HeaderNode simple(List<String> labels) {
        return new SingleLevel(List.copyOf(labels));
    }

    /** Single-row header (varargs). */
    static HeaderNode simple(String... labels) {
        return new SingleLevel(List.of(labels));
    }

    /**
     * Multi-level header. Each list in {@code levels} is one row,
     * from top to bottom. Adjacent identical labels in a row are merged.
     */
    @SafeVarargs
    static HeaderNode of(List<String>... levels) {
        Objects.requireNonNull(levels, "levels must not be null");
        if (levels.length == 0)
            throw new IllegalArgumentException("levels must not be empty");
        if (levels.length == 1)
            return new SingleLevel(List.copyOf(levels[0]));
        var rows = Arrays.stream(levels).map(List::copyOf).toList();
        return new MultiLevel(rows);
    }

    // -- Implementations -------------------------------------------------

    record SingleLevel(List<String> labels) implements HeaderNode {
        @Override public int depth() { return 1; }
        @Override public int leafCount() { return labels.size(); }
        @Override public List<String> labels(int level) {
            if (level != 0) throw new IndexOutOfBoundsException(level);
            return labels;
        }
        @Override public List<int[]> mergeRegions() { return List.of(); }
        @Override public List<String> leafLabels() { return labels; }
    }

    record MultiLevel(List<List<String>> levels) implements HeaderNode {
        @Override public int depth() { return levels.size(); }
        @Override public int leafCount() { return levels.getLast().size(); }
        @Override public List<String> labels(int level) { return levels.get(level); }

        @Override
        public List<int[]> mergeRegions() {
            var regions = new ArrayList<int[]>();
            for (int row = 0; row < levels.size(); row++) {
                var rowLabels = levels.get(row);
                int startCol = 0;
                for (int col = 1; col <= rowLabels.size(); col++) {
                    if (col == rowLabels.size()
                            || !Objects.equals(rowLabels.get(col), rowLabels.get(startCol))) {
                        if (col - startCol > 1) {
                            // span from row to the bottom leaf row, merging columns
                            int spanRows = 1;
                            // look down: does this merged cell span multiple rows?
                            // Simple heuristic: if next row has same label at same position
                            for (int r = row + 1; r < levels.size(); r++) {
                                var below = levels.get(r);
                                if (startCol < below.size()
                                        && Objects.equals(below.get(startCol), rowLabels.get(startCol))) {
                                    spanRows++;
                                } else break;
                            }
                            regions.add(new int[]{row, row + spanRows, startCol, col});
                        }
                        startCol = col;
                    }
                }
            }
            return List.copyOf(regions);
        }

        @Override
        public List<String> leafLabels() { return List.copyOf(levels.getLast()); }
    }
}
