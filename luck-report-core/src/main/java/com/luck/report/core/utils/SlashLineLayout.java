package com.luck.report.core.utils;

/**
 * 斜表头分割线：从左上角出发的射线终点与倾角。
 */
public class SlashLineLayout {
    private final int endX;
    private final int endY;
    private final double angleDeg;

    public SlashLineLayout(int endX, int endY, double angleDeg) {
        this.endX = endX;
        this.endY = endY;
        this.angleDeg = angleDeg;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    public double getAngleDeg() {
        return angleDeg;
    }
}
