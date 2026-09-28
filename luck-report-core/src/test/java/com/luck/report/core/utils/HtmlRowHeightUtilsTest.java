package com.luck.report.core.utils;

import com.luck.report.core.definition.Border;
import com.luck.report.core.definition.BorderStyle;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HtmlRowHeightUtilsTest {

    @Test
    void contentBoxLineHeight_noBorder_equalsRowHeight() {
        assertEquals(24, HtmlRowHeightUtils.contentBoxLineHeightPx(24, null, null));
    }

    @Test
    void contentBoxLineHeight_subtractsTopAndBottomBorder() {
        Border top = border(1);
        Border bottom = border(1);
        assertEquals(22, HtmlRowHeightUtils.contentBoxLineHeightPx(24, top, bottom));
    }

    @Test
    void contentBoxLineHeight_neverBelowOne() {
        Border top = border(10);
        Border bottom = border(10);
        assertEquals(1, HtmlRowHeightUtils.contentBoxLineHeightPx(5, top, bottom));
    }

    private static Border border(int width) {
        Border b = new Border();
        b.setWidth(width);
        b.setStyle(BorderStyle.solid);
        b.setColor("0,0,0");
        return b;
    }
}
