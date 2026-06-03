package cc.ashclaw.common4j.poi.word.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a field or record component to a Word form label.
 *
 * <p>Used by {@code FormExtractor} to match table label-value pairs
 * against POJO fields during Word form import.
 *
 * <pre>{@code
 * record Contract(
 *     &#64;WordField(label = "Contract No.") String contractNo,
 *     &#64;WordField(label = "Party A")      String partyA,
 *     &#64;WordField(label = "Sign Date")    LocalDate signDate,
 *     &#64;WordField(label = "Amount", format = "#,##0.00") BigDecimal amount
 * ) {}
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface WordField {

    /** The label text to match in the Word table (exact match, trimmed). */
    String label();

    /**
     * Number format pattern (e.g. {@code "#,##0.00"}, {@code "yyyy-MM-dd"}).
     * Empty means auto-detect from raw string.
     */
    String format() default "";
}
