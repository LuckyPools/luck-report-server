package com.luck.report.core.utils;

import com.luck.report.core.definition.Border;

/**
 * HTML 预览行高钳制，与设计器 enforceRowHeight 口径一致。
 */
public final class HtmlRowHeightUtils {

    private HtmlRowHeightUtils() {
    }

    /**
     * table-cell 的 max-height 压不住行高，需用 line-height 锁内容行盒。
     * 取「行高 − 上下边框」：用行高本身会在有 border 时多撑 1~2px/行。
     */
    public static int contentBoxLineHeightPx(int heightPx, Border topBorder, Border bottomBorder) {
        return Math.max(1, heightPx - borderOccupyPx(topBorder) - borderOccupyPx(bottomBorder));
    }

    private static int borderOccupyPx(Border border) {
        if (border == null || border.getStyle() == null) {
            return 0;
        }
        int w = border.getWidth();
        return w > 0 ? w : 0;
    }
}
