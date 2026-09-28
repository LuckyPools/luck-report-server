package com.luck.report.web.modules.knowledge.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KnowledgeContentCleanerTest {

    @Test
    void defaultClean_stripsControlAndCompressesNewlines() {
        String input = "a\u0000b\n\n\n\nc";
        String out = KnowledgeContentCleaner.clean(input);
        assertFalse(out.contains("\u0000"));
        assertEquals("ab\n\nc", out);
    }

    @Test
    void prepareDocumentText_convertsFigureImgToMarkdown() {
        String raw = "# t\n<figure style=\"x\"><img src=\"/a.png\" alt=\"选源\" /></figure>\n正文";
        String out = KnowledgeContentCleaner.prepareDocumentText(raw);
        assertTrue(out.contains("![选源](/a.png)"));
        assertFalse(out.contains("<figure"));
        assertFalse(out.contains("style="));
    }

    @Test
    void removeUrlsEmails_keepsMarkdownLinks() {
        String input = "见 [文档](https://example.com/doc) 与 https://evil.com/x";
        String out = KnowledgeContentCleaner.clean(input, false, true);
        assertTrue(out.contains("[文档](https://example.com/doc)"));
        assertFalse(out.contains("evil.com"));
    }

    @Test
    void simpleText_compressesChineseSpaces() {
        assertEquals("中文测试", KnowledgeSimpleText.simpleText("中文  测试"));
    }
}
