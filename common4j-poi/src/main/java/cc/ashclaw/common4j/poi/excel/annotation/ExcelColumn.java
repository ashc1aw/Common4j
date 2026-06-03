package cc.ashclaw.common4j.poi.excel.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a field or record component to an Excel column.
 *
 * <pre>{@code
 * record User(
 *     &#64;ExcelColumn(index = 0, header = "Name", width = 20)
 *     String name,
 *
 *     &#64;ExcelColumn(index = 1, header = "Balance", format = "#,##0.00")
 *     BigDecimal balance
 * ) {}
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface ExcelColumn {

    /** Column index (0-based). {@code -1} means auto-detect from header text or field order. */
    int index() default -1;

    /** Column header text. Used for column matching during import and header rendering during export. */
    String header() default "";

    /**
     * Number format pattern (e.g. {@code "#,##0.00"}, {@code "yyyy-MM-dd"}).
     * Empty means no explicit format — for export the style default applies,
     * for import the raw cell value is parsed.
     */
    String format() default "";

    /** Column width in characters. {@code -1} means auto-calculate from content. */
    int width() default -1;

    /** Column order for sorting (lower = leftmost). Only used when not using {@link #index}. */
    int order() default 0;
}
