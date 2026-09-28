package com.luck.report.core.utils;

/**
 * 斜表头文字落点与旋转角。
 */
public class SlashLabelLayout {
    private final String text;
    private final int x;
    private final int y;
    private final int degree;

    public SlashLabelLayout(String text, int x, int y, int degree) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.degree = degree;
    }

    public String getText() {
        return text;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getDegree() {
        return degree;
    }
}
