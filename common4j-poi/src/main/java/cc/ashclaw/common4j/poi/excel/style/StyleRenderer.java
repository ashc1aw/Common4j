package cc.ashclaw.common4j.poi.excel.style;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Creates and caches POI {@link CellStyle} objects from a {@link StyleProfile}.
 * Each distinct style combination is created once and reused — critical because
 * POI limits workbooks to ~64k styles.
 */
public final class StyleRenderer {

    private final XSSFWorkbook wb;
    private final StyleProfile profile;

    // Cached styles
    private CellStyle headerStyle;
    private CellStyle headerTitleStyle; // header cell in the title column
    private CellStyle dataStyle;
    private CellStyle dataAltStyle;
    private CellStyle titleStyle;
    private final Map<String, CellStyle> formatCache = new ConcurrentHashMap<>();

    public StyleRenderer(XSSFWorkbook wb, StyleProfile profile) {
        this.wb = wb;
        this.profile = profile;
    }

    public CellStyle headerStyle() {
        if (headerStyle == null) {
            headerStyle = buildHeader(false);
        }
        return headerStyle;
    }

    public CellStyle headerTitleStyle() {
        if (headerTitleStyle == null) {
            headerTitleStyle = buildHeader(true);
        }
        return headerTitleStyle;
    }

    public CellStyle dataStyle() {
        if (dataStyle == null) {
            dataStyle = buildData(profile.dataBgColor());
        }
        return dataStyle;
    }

    public CellStyle dataAltStyle() {
        if (dataAltStyle == null) {
            dataAltStyle = buildData(profile.dataAltBgColor());
        }
        return dataAltStyle;
    }

    public CellStyle titleStyle() {
        if (titleStyle == null) {
            titleStyle = buildTitle();
        }
        return titleStyle;
    }

    /**
     * Returns a data CellStyle with the given number format applied.
     * Styles are cached by format string to stay within POI's ~64k style limit.
     */
    public CellStyle dataStyleWithFormat(String format) {
        if (format == null || format.isEmpty()) return dataStyle();
        return formatCache.computeIfAbsent(format, fmt -> {
            var cs = wb.createCellStyle();
            cs.cloneStyleFrom(dataStyle());
            cs.setDataFormat(wb.createDataFormat().getFormat(fmt));
            return cs;
        });
    }

    // -- Builders --------------------------------------------------------

    private CellStyle buildHeader(boolean isTitle) {
        var cs = wb.createCellStyle();
        cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        cs.setFillForegroundColor(parseColor(profile.headerBgColor()));
        cs.setFont(createFont(profile.headerFontColor(), profile.headerBold(), (short) 11));
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);
        cs.setWrapText(isTitle); // allow wrapping for merged header label cells
        applyBorder(cs, profile.headerBorder());
        return cs;
    }

    private CellStyle buildData(String bgHex) {
        var cs = wb.createCellStyle();
        if (!bgHex.isEmpty()) {
            cs.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cs.setFillForegroundColor(parseColor(bgHex));
        }
        cs.setFont(createFont("#000000", false, (short) 11));
        cs.setVerticalAlignment(VerticalAlignment.CENTER);
        applyBorder(cs, profile.dataBorder());
        return cs;
    }

    private CellStyle buildTitle() {
        var cs = wb.createCellStyle();
        cs.setFont(createFont(
                profile.titleFontColor(), profile.titleBold(),
                (short) profile.titleFontSize()));
        cs.setVerticalAlignment(VerticalAlignment.CENTER);
        return cs;
    }

    private Font createFont(String hex, boolean bold, short size) {
        var font = (XSSFFont) wb.createFont();
        font.setColor(parseColor(hex));
        font.setBold(bold);
        font.setFontHeightInPoints(size);
        return font;
    }

    private void applyBorder(CellStyle cs, Border border) {
        var bs = switch (border) {
            case THIN   -> BorderStyle.THIN;
            case MEDIUM -> BorderStyle.MEDIUM;
            case THICK  -> BorderStyle.THICK;
            default     -> BorderStyle.NONE;
        };
        cs.setBorderTop(bs);
        cs.setBorderBottom(bs);
        cs.setBorderLeft(bs);
        cs.setBorderRight(bs);
    }

    private static XSSFColor parseColor(String hex) {
        if (hex.startsWith("#")) hex = hex.substring(1);
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new XSSFColor(new byte[]{(byte) r, (byte) g, (byte) b}, null);
    }
}
