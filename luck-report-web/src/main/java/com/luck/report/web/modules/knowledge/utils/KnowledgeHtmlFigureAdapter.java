package com.luck.report.web.modules.knowledge.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 博客 MD 缺口薄适配
 */
public final class KnowledgeHtmlFigureAdapter {

    private static final Pattern FIGURE_PATTERN = Pattern.compile(
            "(?is)<figure\\b[^>]*>.*?</figure>");
    private static final Pattern IMG_PATTERN = Pattern.compile(
            "(?is)<img\\b[^>]*>");
    private static final Pattern ATTR_PATTERN = Pattern.compile(
            "(?i)(alt|src)\\s*=\\s*([\"'])(.*?)\\2");

    private KnowledgeHtmlFigureAdapter() {
    }

    public static String adapt(String text) {
        if (text == null || text.isEmpty()) {
            return text == null ? "" : text;
        }
        Matcher figureMatcher = FIGURE_PATTERN.matcher(text);
        StringBuffer sb = new StringBuffer();
        while (figureMatcher.find()) {
            String figure = figureMatcher.group();
            figureMatcher.appendReplacement(sb, Matcher.quoteReplacement(figureToMarkdown(figure)));
        }
        figureMatcher.appendTail(sb);

        Matcher imgMatcher = IMG_PATTERN.matcher(sb.toString());
        StringBuffer sb2 = new StringBuffer();
        while (imgMatcher.find()) {
            imgMatcher.appendReplacement(sb2, Matcher.quoteReplacement(imgToMarkdown(imgMatcher.group())));
        }
        imgMatcher.appendTail(sb2);
        return sb2.toString();
    }

    private static String figureToMarkdown(String figureHtml) {
        Matcher img = IMG_PATTERN.matcher(figureHtml);
        if (img.find()) {
            return imgToMarkdown(img.group());
        }
        String stripped = figureHtml.replaceAll("(?is)</?figure\\b[^>]*>", "").trim();
        return stripped;
    }

    private static String imgToMarkdown(String imgTag) {
        String alt = "";
        String src = "";
        Matcher attr = ATTR_PATTERN.matcher(imgTag);
        while (attr.find()) {
            String name = attr.group(1).toLowerCase();
            String value = attr.group(3);
            if ("alt".equals(name)) {
                alt = value;
            } else if ("src".equals(name)) {
                src = value;
            }
        }
        if (src.isEmpty()) {
            return alt;
        }
        return "![" + alt + "](" + src + ")";
    }
}
