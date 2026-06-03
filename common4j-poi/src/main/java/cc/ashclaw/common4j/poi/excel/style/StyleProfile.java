package cc.ashclaw.common4j.poi.excel.style;

import java.util.Objects;

/**
 * Visual description for a sheet — header, data, and title styles.
 * Converted to POI {@code CellStyle} objects by {@link StyleRenderer}.
 *
 * <p>Use {@link StylePreset} for common looks, or {@link #custom()} to build your own.
 */
public sealed interface StyleProfile {

    /** Returns the preset this profile is derived from, or a custom builder. */
    StylePreset preset();

    // -- Header config ---------------------------------------------------

    String headerBgColor();
    String headerFontColor();
    boolean headerBold();
    Border headerBorder();

    // -- Data config -----------------------------------------------------

    String dataBgColor();
    String dataAltBgColor();   // alternating row color, empty = no stripes
    Border dataBorder();

    // -- Title config ----------------------------------------------------

    String titleFontColor();
    int    titleFontSize();
    boolean titleBold();

    // -- Factory methods -------------------------------------------------

    /** Returns the style profile for the given preset. */
    static StyleProfile of(StylePreset preset) {
        return switch (preset) {
            case PROFESSIONAL -> new PresetProfile(preset,
                    "#4472C4", "#FFFFFF", true,  Border.THIN,
                    "#FFFFFF", "",           Border.THIN,
                    "#1F3864", 16,           true);
            case MINIMAL -> new PresetProfile(preset,
                    "#000000", "#000000", true,  Border.NONE,
                    "#FFFFFF", "",           Border.NONE,
                    "#000000", 14,           true);
            case CORPORATE -> new PresetProfile(preset,
                    "#2F5496", "#FFFFFF", true,  Border.THIN,
                    "#FFFFFF", "#F2F2F2",    Border.THIN,
                    "#1F3864", 16,           true);
            case PLAIN -> new PresetProfile(preset,
                    "#FFFFFF", "#000000", false, Border.NONE,
                    "#FFFFFF", "",           Border.NONE,
                    "#000000", 14,           false);
        };
    }

    /** Starts a custom style profile builder. */
    static Builder custom() {
        return new Builder();
    }

    // -- Builder ---------------------------------------------------------

    final class Builder {
        String  headerBgColor    = "#4472C4";
        String  headerFontColor  = "#FFFFFF";
        boolean headerBold       = true;
        Border  headerBorder     = Border.THIN;
        String  dataBgColor      = "#FFFFFF";
        String  dataAltBgColor   = "";
        Border  dataBorder       = Border.THIN;
        String  titleFontColor   = "#1F3864";
        int     titleFontSize    = 16;
        boolean titleBold        = true;

        public Builder headerBg(String hex)         { this.headerBgColor = hex; return this; }
        public Builder headerFont(String hex)       { this.headerFontColor = hex; return this; }
        public Builder headerBold(boolean bold)     { this.headerBold = bold; return this; }
        public Builder headerBorder(Border border)  { this.headerBorder = border; return this; }

        public Builder dataBg(String hex)           { this.dataBgColor = hex; return this; }
        public Builder dataAltBg(String hex)        { this.dataAltBgColor = Objects.requireNonNullElse(hex, ""); return this; }
        public Builder dataBorder(Border border)    { this.dataBorder = border; return this; }

        public Builder titleFont(String hex)        { this.titleFontColor = hex; return this; }
        public Builder titleFontSize(int size)      { this.titleFontSize = size; return this; }
        public Builder titleBold(boolean bold)      { this.titleBold = bold; return this; }

        /** Convenience: set all borders at once. */
        public Builder allBorders(Border border) {
            this.headerBorder = border;
            this.dataBorder = border;
            return this;
        }

        public StyleProfile build() {
            return new BuiltProfile(this);
        }
    }
}

/** Profile from a preset — immutable snapshot. */
record PresetProfile(
        StylePreset preset,
        String headerBgColor, String headerFontColor, boolean headerBold, Border headerBorder,
        String dataBgColor, String dataAltBgColor, Border dataBorder,
        String titleFontColor, int titleFontSize, boolean titleBold
) implements StyleProfile {}

/** Profile from the custom builder. */
record BuiltProfile(
        String headerBgColor, String headerFontColor, boolean headerBold, Border headerBorder,
        String dataBgColor, String dataAltBgColor, Border dataBorder,
        String titleFontColor, int titleFontSize, boolean titleBold
) implements StyleProfile {
    BuiltProfile(StyleProfile.Builder b) {
        this(b.headerBgColor, b.headerFontColor, b.headerBold, b.headerBorder,
             b.dataBgColor, b.dataAltBgColor, b.dataBorder,
             b.titleFontColor, b.titleFontSize, b.titleBold);
    }
    @Override public StylePreset preset() { return null; }
}
