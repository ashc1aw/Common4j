package cc.ashclaw.common4j.poi.word;

import cc.ashclaw.common4j.poi.word.extract.FormExtractor;
import cc.ashclaw.common4j.poi.word.template.TemplateFiller;

import org.apache.poi.xwpf.usermodel.XWPFDocument;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Entry point for reading Word forms and filling Word templates.
 *
 * Read (form extraction):
 * <pre>{@code
 * // Get all label-value pairs from tables
 * Map<String, String> fields = Word.read(docxFile).toFields();
 *
 * // Map to a typed record
 * Contract c = Word.read(docxStream).toBean(Contract.class);
 *
 * // Target a specific table
 * Map<String, String> f = Word.read(docxFile).table(1).toFields();
 * }</pre>
 *
 * Template fill:
 * <pre>{@code
 * // Simple key-value
 * Word.fromTemplate(in)
 *     .put("partyA", "Acme Corp")
 *     .put("date", LocalDate.now())
 *     .to(out);
 *
 * // POJO-driven
 * Word.fromTemplate(in)
 *     .fill(contract)
 *     .to(out);
 *
 * // With table row expansion
 * Word.fromTemplate(in)
 *     .put("title", "Item List")
 *     .table("items", List.of(
 *         new Item("A", 100),
 *         new Item("B", 200)))
 *     .to(out);
 * }</pre>
 */
public final class Word {

    private Word() {
        throw new UnsupportedOperationException("utility class");
    }

    // -- Read --------------------------------------------------------------

    /** Opens a docx file for form extraction. */
    public static FormExtractor read(Path path) throws IOException {
        try (var is = Files.newInputStream(path)) {
            return new FormExtractor(new XWPFDocument(is));
        }
    }

    /** Opens a docx input stream for form extraction. */
    public static FormExtractor read(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        return new FormExtractor(new XWPFDocument(inputStream));
    }

    // -- Template fill -----------------------------------------------------

    /** Opens a docx file as a template for filling. */
    public static TemplateFiller fromTemplate(Path templatePath) throws IOException {
        Objects.requireNonNull(templatePath, "templatePath must not be null");
        try (var is = Files.newInputStream(templatePath)) {
            return new TemplateFiller(new XWPFDocument(is));
        }
    }

    /** Opens a docx input stream as a template for filling. */
    public static TemplateFiller fromTemplate(InputStream templateStream) throws IOException {
        Objects.requireNonNull(templateStream, "templateStream must not be null");
        return new TemplateFiller(new XWPFDocument(templateStream));
    }
}
