package cc.ashclaw.common4j.poi.excel.define;

import java.util.Objects;

/**
 * Column specification — field binding, width, and number format.
 * Used by {@link SheetDefinition} to describe each column.
 *
 * @param field  the data field name (POJO field or Map key)
 * @param header column header text
 * @param format Excel number format pattern, or empty for default
 * @param width  column width in characters, -1 for auto-calculate
 * @param order  display order (lower = left), 0 keeps declaration order
 */
public record ColumnSpec(
        String field,
        String header,
        String format,
        int width,
        int order) {

    public ColumnSpec {
        Objects.requireNonNull(field, "field must not be null");
        Objects.requireNonNull(header, "header must not be null");
        Objects.requireNonNull(format, "format must not be null");
    }

    /** A column with just field + header, auto width, no format. */
    public static ColumnSpec of(String field, String header) {
        return new ColumnSpec(field, header, "", -1, 0);
    }

    /** A column where the field name is also the header text. */
    public static ColumnSpec of(String field) {
        return new ColumnSpec(field, field, "", -1, 0);
    }

    /** Fluent: set width. */
    public ColumnSpec withWidth(int w) {
        return new ColumnSpec(field, header, format, w, order);
    }

    /** Fluent: set format. */
    public ColumnSpec withFormat(String f) {
        return new ColumnSpec(field, header, f, width, order);
    }

    /** Fluent: set order. */
    public ColumnSpec withOrder(int o) {
        return new ColumnSpec(field, header, format, width, o);
    }
}
