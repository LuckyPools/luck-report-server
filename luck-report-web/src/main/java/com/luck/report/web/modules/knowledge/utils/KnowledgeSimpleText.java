package com.luck.report.web.modules.knowledge.utils;

/**
 * Port of FastGPT simpleText (packages/global/common/string/tools.ts).
 */
public final class KnowledgeSimpleText {

    private KnowledgeSimpleText() {
    }

    public static String simpleText(String text) {
        if (text == null) {
            return "";
        }
        text = text.trim();
        text = text.replaceAll("([\\u4e00-\\u9fa5])[^\\S\\n]+([\\u4e00-\\u9fa5])", "$1$2");
        text = text.replace("\r\n", "\n").replace("\r", "\n");
        text = text.replaceAll("\\n{3,}", "\n\n");
        text = text.replaceAll("[^\\S\\n]{2,}", " ");
        text = text.replaceAll("[\\x00-\\x08]", " ");
        return text;
    }
}
