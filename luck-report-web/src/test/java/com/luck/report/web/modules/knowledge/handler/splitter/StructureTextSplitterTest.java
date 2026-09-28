package com.luck.report.web.modules.knowledge.handler.splitter;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FastGPT commonSplit / splitText2Chunks（char 模式）移植回归
 */
class StructureTextSplitterTest {

    @Test
    void markdownSections_preserveBothHeadings() {
        String md =
                "## A\n"
                        + "section a content about apples and oranges.\n\n"
                        + "## B\n"
                        + "section b content about bananas and grapes.\n";

        StructureTextSplitter splitter = new StructureTextSplitter(80, 10);
        List<String> chunks = splitter.split(md);
        String joined = chunks.stream().collect(Collectors.joining("\n"));

        assertTrue(joined.contains("## A"), "missing ## A in: " + chunks);
        assertTrue(joined.contains("## B"), "missing ## B in: " + chunks);
    }

    @Test
    void fencedJavaCodeBlock_staysIntactWhenUnderChunkSize() {
        String md =
                "Intro text before code.\n\n"
                        + "```java\n"
                        + "public class Hello {\n"
                        + "  public static void main(String[] args) {\n"
                        + "    System.out.println(\"hi\");\n"
                        + "  }\n"
                        + "}\n"
                        + "```\n\n"
                        + "After code.\n";

        StructureTextSplitter splitter = new StructureTextSplitter(500, 50);
        List<String> chunks = splitter.split(md);

        boolean foundIntact = false;
        for (String chunk : chunks) {
            if (chunk.contains("```java")
                    && chunk.contains("public class Hello")
                    && chunk.contains("```")
                    && chunk.indexOf("```java") < chunk.lastIndexOf("```")) {
                foundIntact = true;
                // fence body should still contain newlines (markers restored)
                assertTrue(chunk.contains("\n"), "code block newlines should be restored");
                break;
            }
        }
        assertTrue(foundIntact, "expected intact code fence in chunks: " + chunks);
    }
}
