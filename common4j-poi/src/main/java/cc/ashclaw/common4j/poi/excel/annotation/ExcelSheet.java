package cc.ashclaw.common4j.poi.excel.annotation;

import cc.ashclaw.common4j.poi.excel.style.StylePreset;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Optional annotation on a POJO class to configure default sheet-level settings
 * for import and export.
 *
 * <pre>{@code
 * &#64;ExcelSheet(name = "Users", style = StylePreset.PROFESSIONAL)
 * record User(&#64;ExcelColumn(header = "Name") String name) {}
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ExcelSheet {

    /** Sheet name. Defaults to the class simple name. */
    String name() default "";

    /** Visual style preset. */
    StylePreset style() default StylePreset.PROFESSIONAL;

    /** Whether to auto-calculate column widths. */
    boolean autoWidth() default true;
}
