package com.luck.report.core.utils;

import org.junit.jupiter.api.Test;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 折行按字符真实宽度，英文整词换行。
 */
class TextWrapUtilsTest {

    /**
     * 本行剩余宽度放不下整个英文词时，在词前换行
     */
    @Test
    void keepsLatinWordTogether() {
        FontMetrics metrics = metrics();
        int width = metrics.stringWidth("hello ");
        assertEquals("hello \nworld", TextWrapUtils.wrap("hello world", metrics, width));
    }

    /**
     * 汉字按单字宽度折行，不额外提前断开
     */
    @Test
    void wrapsCJKByCharacterWidth() {
        FontMetrics metrics = metrics();
        int width = metrics.charWidth('中') * 2;
        assertEquals("中中\n中", TextWrapUtils.wrap("中中中", metrics, width));
    }

    /**
     * 原文中的换行保留，回车换行合并为一个换行
     */
    @Test
    void keepsExistingLineBreak() {
        FontMetrics metrics = metrics();
        int width = metrics.stringWidth("abcd");
        assertEquals("ab\ncd", TextWrapUtils.wrap("ab\ncd", metrics, width));
        assertEquals("ab\ncd", TextWrapUtils.wrap("ab\r\ncd", metrics, width));
    }

    /**
     * 超过列宽的英文词按字符断开
     */
    @Test
    void splitsWordLongerThanColumn() {
        FontMetrics metrics = metrics();
        int width = metrics.charWidth('A') * 2;
        assertEquals("AB\nCD", TextWrapUtils.wrap("ABCD", metrics, width));
    }

    private static FontMetrics metrics() {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        return image.getGraphics().getFontMetrics(new Font("Monospaced", Font.PLAIN, 12));
    }
}
