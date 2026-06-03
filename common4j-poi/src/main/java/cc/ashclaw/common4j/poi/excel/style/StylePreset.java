package cc.ashclaw.common4j.poi.excel.style;

/**
 * Pre-built visual themes for Excel sheets.
 */
public enum StylePreset {

    /** Blue header with white bold text, bordered cells. General-purpose reports. */
    PROFESSIONAL,

    /** Bold underlined header, no borders, clean look. Data export / CSV-replacement. */
    MINIMAL,

    /** Dark blue header, alternating row colors, thin borders. Formal reports. */
    CORPORATE,

    /** No styling beyond what the cell data naturally has. Fastest. */
    PLAIN
}
