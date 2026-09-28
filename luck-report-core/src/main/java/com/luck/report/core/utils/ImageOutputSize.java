package com.luck.report.core.utils;

/** 图片缩放后的目标宽高。 */
public class ImageOutputSize {
    private final int width;
    private final int height;

    public ImageOutputSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
