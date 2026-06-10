/**
 * Excel read / write utilities built on Apache POI.
 *
 * Quick start:
 * <pre>{@code
 * // Export — 1 line
 * Excel.write(users).to(Paths.get("users.xlsx"));
 *
 * // Import — 1 line
 * List<User> users = Excel.read(file).toList(User.class);
 * }</pre>
 *
 * <p>Annotate your records with
 * {@link cc.ashclaw.common4j.poi.excel.annotation.ExcelColumn}
 * and {@link cc.ashclaw.common4j.poi.excel.annotation.ExcelSheet} for zero-config
 * read/write. Use {@link cc.ashclaw.common4j.poi.excel.define.SheetDefinition}
 * for complex headers, cell merging, and custom styling via
 * {@link cc.ashclaw.common4j.poi.excel.style.StyleProfile}.
 *
 * <p>Writing always uses streaming (SXSSF) to keep memory under control.
 * Reading uses DOM mode which handles up to ~100k rows comfortably.
 */
package cc.ashclaw.common4j.poi.excel;
