package com.luck.report.core.utils;

import com.luck.report.core.definition.Border;

/**
 * HTML 预览行高钳制，与设计器 enforceRowHeight 口径一致。文本 line-height 取字号像素且不超过内容区高度：锁行高的同时保留 vertical-align 空隙。
 */
public final class HtmlRowHeightUtils {

    /**
     * 与设计器默认 cellStyle.fontSize=10pt 一致
     */
    private static final int DEFAULT_FONT_SIZE_PT = 10;

    private HtmlRowHeightUtils() {
    }

    /**
     * 文本行高（px）：字号对应像素，且不超过「行高 − 上下边框」。用行高本身作 line-height 会铺满格子导致 vertical-align 无效；用 normal / 未封顶字号则短行高会被撑开。
     *
     * @param heightPx     单元格高度（含边框的定义像素）
     * @param topBorder    上边框，可空
     * @param bottomBorder 下边框，可空
     * @param fontSizePt   字号（pt），≤0 时按 10pt
     */
    public static int lockedTextLineHeightPx(int heightPx, Border topBorder, Border bottomBorder, int fontSizePt) {
        int contentH = Math.max(1, heightPx - borderOccupyPx(topBorder) - borderOccupyPx(bottomBorder));
        int pt = fontSizePt > 0 ? fontSizePt : DEFAULT_FONT_SIZE_PT;
        int fontLh = UnitUtils.pointToPixel(pt);
        return Math.max(1, Math.min(fontLh, contentH));
    }

    private static int borderOccupyPx(Border border) {
        if (border == null || border.getStyle() == null) {
            return 0;
        }
        int w = border.getWidth();
        return w > 0 ? w : 0;
    }
}
