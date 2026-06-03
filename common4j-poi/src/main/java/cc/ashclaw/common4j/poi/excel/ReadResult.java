package cc.ashclaw.common4j.poi.excel;

import java.util.List;

/**
 * Read result combining successfully parsed data rows and any parse errors.
 *
 * @param data   successfully parsed records
 * @param errors rows that failed to parse, with row number and message
 */
public record ReadResult<T>(List<T> data, List<ParseError> errors) {

    /** True if every row was parsed without error. */
    public boolean isSuccess() {
        return errors.isEmpty();
    }
}
