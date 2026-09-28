package com.luck.report.core.utils;

import java.awt.FontMetrics;

/**
 * 按列宽折行。连续英文字母视为一个词，放不下时整词换到下一行。
 */
public final class TextWrapUtils {

    private TextWrapUtils() {}

    /**
     * 按列宽折行，保留原文换行，不额外加宽
     *
     * @param text 原文，可为空
     * @param metrics 字体度量，不可为空
     * @param columnWidth 列宽，与度量同一单位；小于等于 0 时不折行
     * @return 折行后的文本；原文为空时原样返回
     */
    public static String wrap(String text, FontMetrics metrics, int columnWidth) {
        if (text == null || text.isEmpty() || columnWidth <= 0) {
            return text;
        }
        StringBuilder out = new StringBuilder();
        int remaining = columnWidth;
        int length = text.length();
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == '\r' || c == '\n') {
                if (c == '\r' && i + 1 < length && text.charAt(i + 1) == '\n') {
                    i++;
                }
                out.append('\n');
                remaining = columnWidth;
                continue;
            }
            if (isLatinLetter(c)) {
                int start = i;
                int wordWidth = 0;
                while (i < length && isLatinLetter(text.charAt(i))) {
                    wordWidth += charWidth(metrics, text.charAt(i));
                    i++;
                }
                i--;
                remaining = appendWord(out, metrics, columnWidth, remaining, text.substring(start, i + 1), wordWidth);
                continue;
            }
            remaining = appendChar(out, metrics, columnWidth, remaining, c);
        }
        return out.toString();
    }

    /**
     * 写入一个英文词；本行放不下且词本身不超过列宽时先换行
     *
     * @param out 已写出的文本，不可为空
     * @param metrics 字体度量，不可为空
     * @param columnWidth 列宽，与度量同一单位
     * @param remaining 当前行剩余宽度
     * @param word 连续英文字母，不可为空
     * @param wordWidth 该词的宽度
     * @return 写入后当前行的剩余宽度
     */
    private static int appendWord(StringBuilder out, FontMetrics metrics, int columnWidth, int remaining, String word, int wordWidth) {
        if (lineHasContent(out) && wordWidth > remaining && wordWidth <= columnWidth) {
            out.append('\n');
            remaining = columnWidth;
        }
        if (wordWidth > columnWidth) {
            for (int i = 0; i < word.length(); i++) {
                remaining = appendChar(out, metrics, columnWidth, remaining, word.charAt(i));
            }
            return remaining;
        }
        out.append(word);
        return remaining - wordWidth;
    }

    /**
     * 写入一个字符，当前行放不下时先换行
     *
     * @param out 已写出的文本，不可为空
     * @param metrics 字体度量，不可为空
     * @param columnWidth 列宽，与度量同一单位
     * @param remaining 当前行剩余宽度
     * @param c 当前字符
     * @return 写入后当前行的剩余宽度
     */
    private static int appendChar(StringBuilder out, FontMetrics metrics, int columnWidth, int remaining, char c) {
        int width = charWidth(metrics, c);
        if (lineHasContent(out) && width > remaining) {
            out.append('\n');
            remaining = columnWidth;
        }
        out.append(c);
        return remaining - width;
    }

    /**
     * 当前输出末尾是否已经有本行内容
     *
     * @param out 已写出的文本，不可为空
     * @return true 表示末尾不是换行，本行已有字符
     */
    private static boolean lineHasContent(StringBuilder out) {
        return out.length() > 0 && out.charAt(out.length() - 1) != '\n';
    }

    /**
     * 取单个字符宽度，字宽为 0 时改用字符串测量
     *
     * @param metrics 字体度量，不可为空
     * @param c 当前字符
     * @return 字符宽度，度量结果为 0 时仍可能为 0
     */
    private static int charWidth(FontMetrics metrics, char c) {
        int width = metrics.charWidth(c);
        if (width > 0) {
            return width;
        }
        return metrics.stringWidth(String.valueOf(c));
    }

    /**
     * 判断是否为需要整词换行的英文字母
     *
     * @param c 当前字符
     * @return true 表示 A-Z 或 a-z
     */
    private static boolean isLatinLetter(char c) {
        return (c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z');
    }
}
