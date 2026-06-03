package cc.ashclaw.common4j.poi.excel;

import cc.ashclaw.common4j.poi.excel.annotation.ExcelSheet;
import cc.ashclaw.common4j.poi.excel.reader.ExcelReader;
import cc.ashclaw.common4j.poi.excel.writer.ExcelWriter;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Entry point for reading and writing Excel files.
 *
 * <h3>Read</h3>
 * <pre>{@code
 * List<User> users = Excel.read(file).toList(User.class);
 *
 * // With options
 * var result = Excel.read(inputStream)
 *     .sheet("Employees")
 *     .headerRow(0)
 *     .toResult(User.class);
 * }</pre>
 *
 * <h3>Write</h3>
 * <pre>{@code
 * // Annotation-driven
 * Excel.write(users).to(outputStream);
 *
 * // Multi-sheet
 * Excel.write()
 *     .sheet("Users", users)
 *     .sheet("Orders", orders)
 *     .to(outputStream);
 *
 * // Complex
 * Excel.write()
 *     .sheet(def -> def.name("Report")
 *         .title("2024 Report", 4)
 *         .complexHeader(List.of("","H1","H1"), List.of("Name","Q1","Q2"))
 *         .mergeSame("Dept")
 *         .style(StylePreset.CORPORATE), rows)
 *     .to(outputStream);
 * }</pre>
 *
 * <h3>Template fill</h3>
 * <pre>{@code
 * Excel.fromTemplate(templateStream)
 *     .fill("Users", users, 2)   // data starts at row 2
 *     .to(outputStream);
 *
 * // With custom style
 * Excel.fromTemplate(templateStream)
 *     .fill("Users", users, 2, StyleProfile.custom()
 *         .dataBg("#FFFFFF").allBorders(Border.THIN).build())
 *     .to(outputStream);
 * }</pre>
 */
public final class Excel {

    private Excel() {
        throw new UnsupportedOperationException("utility class");
    }

    // -- Read -----------------------------------------------------------

    /** Opens a workbook from a file path for reading. */
    public static ExcelReader read(Path path) throws IOException {
        try (var is = Files.newInputStream(path)) {
            return new ExcelReader(new XSSFWorkbook(is));
        }
    }

    /** Opens a workbook from an input stream for reading. */
    public static ExcelReader read(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream, "inputStream must not be null");
        return new ExcelReader(new XSSFWorkbook(inputStream));
    }

    // -- Write ----------------------------------------------------------

    /** Creates a writer for the given POJO list (annotation-driven). */
    public static ExcelWriter write(List<?> data) {
        Objects.requireNonNull(data, "data must not be null");
        var w = new ExcelWriter();
        if (!data.isEmpty()) {
            var type = data.getFirst().getClass();
            var sheetAnn = type.getAnnotation(ExcelSheet.class);
            var name = sheetAnn != null && !sheetAnn.name().isEmpty()
                    ? sheetAnn.name() : type.getSimpleName();
            w.sheet(name, data);
        }
        return w;
    }

    /** Creates an empty writer for programmatic sheet definitions. */
    public static ExcelWriter write() {
        return new ExcelWriter();
    }

    // -- Template fill --------------------------------------------------

    /**
     * Opens an existing xlsx file as a template and returns a writer
     * that can {@link ExcelWriter#fill fill} data into it.
     */
    public static ExcelWriter fromTemplate(InputStream templateStream) throws IOException {
        Objects.requireNonNull(templateStream, "templateStream must not be null");
        return new ExcelWriter(new XSSFWorkbook(templateStream));
    }

    /** Opens an existing xlsx file as a template. */
    public static ExcelWriter fromTemplate(Path templatePath) throws IOException {
        Objects.requireNonNull(templatePath, "templatePath must not be null");
        try (var is = Files.newInputStream(templatePath)) {
            return new ExcelWriter(new XSSFWorkbook(is));
        }
    }
}
