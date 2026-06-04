module cc.ashclaw.common4j.poi {
    requires cc.ashclaw.common4j.core;
    requires org.apache.poi.ooxml;

    exports cc.ashclaw.common4j.poi.excel;
    exports cc.ashclaw.common4j.poi.excel.annotation;
    exports cc.ashclaw.common4j.poi.excel.define;
    exports cc.ashclaw.common4j.poi.excel.reader;
    exports cc.ashclaw.common4j.poi.excel.style;
    exports cc.ashclaw.common4j.poi.excel.writer;
    exports cc.ashclaw.common4j.poi.word;
    exports cc.ashclaw.common4j.poi.word.annotation;
    exports cc.ashclaw.common4j.poi.word.extract;
    exports cc.ashclaw.common4j.poi.word.template;
}
