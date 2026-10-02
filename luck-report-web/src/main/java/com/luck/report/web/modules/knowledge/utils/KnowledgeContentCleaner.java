package com.luck.report.web.modules.knowledge.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Port of Dify CleanProcessor.clean (api/core/rag/cleaner/clean_processor.py).
 */
public final class KnowledgeContentCleaner {

    private static final Pattern CONTROL_CHARS = Pattern.compile("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F\\x7F]");
    private static final Pattern EXTRA_NEWLINES = Pattern.compile("\\n{3,}");
    private static final Pattern EXTRA_SPACES = Pattern.compile(
            "[\\t\\f\\r\\x20\\u00a0\\u1680\\u180e\\u2000-\\u200a\\u202f\\u205f\\u3000]{2,}");
    private static final Pattern EMAIL = Pattern.compile(
            "([a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+)");
    private static final Pattern MD_LINK = Pattern.compile("\\[([^\\]]*)\\]\\((https?://[^)]+)\\)");
    private static final Pattern MD_IMAGE = Pattern.compile("!\\[.*?\\]\\((https?://[^)]+)\\)");
    private static final Pattern BARE_URL = Pattern.compile("https?://\\S+");

    private KnowledgeContentCleaner() {
    }

    /**
     * 默认对齐 Dify automatic：remove_extra_spaces=true，remove_urls_emails=false。
     */
    public static String clean(String text) {
        return clean(text, true, false);
    }

    /**
     * 清洗文本：控制字符、多余空白，可选去除 URL/邮箱。
     */
    public static String clean(String text, boolean removeExtraSpaces, boolean removeUrlsEmails) {
        if (text == null || text.isEmpty()) {
            return text == null ? "" : text;
        }

        text = text.replace("<|", "<");
        text = text.replace("|>", ">");
        text = CONTROL_CHARS.matcher(text).replaceAll("");
        text = text.replace("\ufffe", "");

        if (removeExtraSpaces) {
            text = EXTRA_NEWLINES.matcher(text).replaceAll("\n\n");
            text = EXTRA_SPACES.matcher(text).replaceAll(" ");
        }

        if (removeUrlsEmails) {
            text = EMAIL.matcher(text).replaceAll("");
            List<String> types = new ArrayList<>();
            List<String> texts = new ArrayList<>();
            List<String> urls = new ArrayList<>();

            Matcher linkMatcher = MD_LINK.matcher(text);
            StringBuffer linkBuf = new StringBuffer();
            while (linkMatcher.find()) {
                String placeholder = "__MARKDOWN_PLACEHOLDER_" + types.size() + "__";
                types.add("link");
                texts.add(linkMatcher.group(1));
                urls.add(linkMatcher.group(2));
                linkMatcher.appendReplacement(linkBuf, Matcher.quoteReplacement(placeholder));
            }
            linkMatcher.appendTail(linkBuf);
            text = linkBuf.toString();

            Matcher imageMatcher = MD_IMAGE.matcher(text);
            StringBuffer imageBuf = new StringBuffer();
            while (imageMatcher.find()) {
                String placeholder = "__MARKDOWN_PLACEHOLDER_" + types.size() + "__";
                types.add("image");
                texts.add("image");
                urls.add(imageMatcher.group(1));
                imageMatcher.appendReplacement(imageBuf, Matcher.quoteReplacement(placeholder));
            }
            imageMatcher.appendTail(imageBuf);
            text = imageBuf.toString();

            text = BARE_URL.matcher(text).replaceAll("");

            for (int i = 0; i < types.size(); i++) {
                String placeholder = "__MARKDOWN_PLACEHOLDER_" + i + "__";
                String restored = "link".equals(types.get(i))
                        ? "[" + texts.get(i) + "](" + urls.get(i) + ")"
                        : "![" + texts.get(i) + "](" + urls.get(i) + ")";
                text = text.replace(placeholder, restored);
            }
        }

        return text;
    }

    /**
     * figure 薄适配 + Dify clean（automatic 默认）。
     */
    public static String prepareDocumentText(String raw) {
        return clean(KnowledgeHtmlFigureAdapter.adapt(raw == null ? "" : raw));
    }
}
