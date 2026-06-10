package cc.ashclaw.common4j.poi.excel.define;

import cc.ashclaw.common4j.poi.excel.style.StylePreset;
import cc.ashclaw.common4j.poi.excel.style.StyleProfile;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Complete description of one Excel sheet — headers, columns, merge rules,
 * and style — independent of the data source.
 *
 * <p>Built programmatically for complex cases. For simple annotation-driven
 * scenarios, see {@link cc.ashclaw.common4j.poi.excel.Excel}.
 *
 * Examples:
 * <pre>{@code
 * // Simple
 * var def = SheetDefinition.of("Users")
 *     .simpleHeader("Name", "Email", "Age")
 *     .build();
 *
 * // Complex header with merged title
 * var def = SheetDefinition.of("Report")
 *     .title("Q4 2024 Sales", 6)   // title row, merged across 6 columns
 *     .complexHeader(
 *         List.of("",    "",    "H1", "H1", "H2", "H2"),
 *         List.of("Name", "Dept", "Rev", "Cost", "Rev", "Cost"))
 *     .columns(ColumnSpec.of("name").withWidth(20),
 *              ColumnSpec.of("dept").withWidth(15))
 *     .mergeSame("Dept")
 *     .style(StylePreset.PROFESSIONAL)
 *     .build();
 * }</pre>
 */
public final class SheetDefinition {

    private final String         name;
    private final String         title;
    private final int            titleCols;       // columns to merge for title row
    private final HeaderNode     header;
    private final List<ColumnSpec> columns;        // null = derive from data annotations
    private final Set<String>    mergeColumns;     // data rows: merge consecutive same values
    private final StyleProfile   style;
    private final boolean        autoWidth;

    private SheetDefinition(Builder builder) {
        this.name         = Objects.requireNonNull(builder.name, "name must not be null");
        this.title        = builder.title;
        this.titleCols    = builder.titleCols;
        this.header       = builder.header;
        this.columns      = builder.columns.isEmpty() ? null : List.copyOf(builder.columns);
        this.mergeColumns  = builder.mergeColumns.isEmpty() ? null : Set.copyOf(builder.mergeColumns);
        this.style        = builder.style;
        this.autoWidth     = builder.autoWidth;
    }

    // -- Getters ---------------------------------------------------------

    public String name()              { return name; }
    public String title()             { return title; }
    public int    titleCols()         { return titleCols; }
    public HeaderNode header()        { return header; }
    public List<ColumnSpec> columns()  { return columns; }
    public Set<String> mergeColumns()  { return mergeColumns; }
    public StyleProfile style()        { return style; }
    public boolean autoWidth()         { return autoWidth; }

    // -- Factory ---------------------------------------------------------

    /** Start defining a sheet with the given name. */
    public static Builder of(String name) {
        return new Builder(name);
    }

    // -- Builder ---------------------------------------------------------

    public static final class Builder {
        private String            name;
        private String            title;
        private int               titleCols;
        private HeaderNode        header;
        private final List<ColumnSpec> columns     = new ArrayList<>();
        private final List<String>     mergeColumns = new ArrayList<>();
        private StyleProfile      style       = StyleProfile.of(StylePreset.PROFESSIONAL);
        private boolean           autoWidth   = true;

        private Builder(String name) {
            this.name = Objects.requireNonNull(name, "name must not be null");
        }

        /** Overrides the sheet name. */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /** Sets a merged title row at the top. */
        public Builder title(String title, int mergeCols) {
            this.title = title;
            this.titleCols = mergeCols;
            return this;
        }

        /** Simple single-row header. */
        public Builder simpleHeader(String... labels) {
            this.header = HeaderNode.simple(labels);
            return this;
        }

        /** Simple single-row header. */
        public Builder simpleHeader(List<String> labels) {
            this.header = HeaderNode.simple(labels);
            return this;
        }

        /** Multi-level complex header. Each list is one row, top to bottom. */
        @SafeVarargs
        public final Builder complexHeader(List<String>... levels) {
            this.header = HeaderNode.of(levels);
            return this;
        }

        /** Adds a column spec. */
        public Builder column(ColumnSpec col) {
            this.columns.add(col);
            return this;
        }

        /** Adds multiple column specs. */
        public Builder columns(ColumnSpec... cols) {
            for (var c : cols) this.columns.add(c);
            return this;
        }

        /**
         * Merge cells vertically in the given data columns
         * when consecutive rows have the same value.
         */
        public Builder mergeSame(String... columnFields) {
            for (var f : columnFields) this.mergeColumns.add(f);
            return this;
        }

        /** Sets the style profile. */
        public Builder style(StyleProfile profile) {
            this.style = Objects.requireNonNull(profile, "profile must not be null");
            return this;
        }

        /** Sets the style preset (convenience). */
        public Builder style(StylePreset preset) {
            this.style = StyleProfile.of(preset);
            return this;
        }

        /** Enables or disables auto column width calculation. */
        public Builder autoWidth(boolean v) {
            this.autoWidth = v;
            return this;
        }

        public SheetDefinition build() {
            if (header == null && !columns.isEmpty()) {
                // Derive simple header from column specs
                header = HeaderNode.simple(
                        columns.stream().map(ColumnSpec::header).toList());
            }
            return new SheetDefinition(this);
        }
    }
}
