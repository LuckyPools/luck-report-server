package com.luck.report.web.modules.knowledge.utils;

import com.luck.report.web.modules.knowledge.domain.enums.SplitterType;
import com.luck.report.web.modules.knowledge.handler.splitter.StructureTextSplitter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StructureChunkPipelineSmokeTest {

    @Test
    void datasourceLikeMarkdown_structureSplit_noStyleNoise() {
        String raw = ""
                + "# 数据源配置\n\n"
                + "## Jdbc 数据源\n"
                + "Jdbc 说明\n"
                + "<figure style=\"display:flex\"><img src=\"/a.png\" alt=\"连库\" /></figure>\n\n"
                + "## Spring Bean 数据源\n"
                + "方法签名：`public List<?> load()`\n"
                + "```java\n"
                + "@Component(\"testBean\")\n"
                + "public class TestBean {}\n"
                + "```\n";

        String cleaned = KnowledgeContentCleaner.prepareDocumentText(raw);
        assertFalse(cleaned.contains("<figure"));
        assertTrue(cleaned.contains("![连库](/a.png)"));

        assertEquals(SplitterType.STRUCTURE, SplitterTypeResolver.resolveByFilename("Luck-Report数据源.md"));

        List<String> chunks = new StructureTextSplitter(1000, 100).split(cleaned);
        String joined = String.join("\n", chunks);
        assertTrue(joined.contains("Jdbc"));
        assertTrue(joined.contains("Spring Bean"));
        assertTrue(chunks.stream().anyMatch(c -> c.contains("```") && c.contains("TestBean")));
    }
}
