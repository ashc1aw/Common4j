package cc.ashclaw.common4j.poi.excel;

/**
 * A single row-level parse error during import.
 *
 * @param row     0-based row index in the sheet
 * @param column  column index, or -1 if the whole row failed
 * @param message description of the failure
 */
public record ParseError(int row, int column, String message) {}
