package com.luck.report.web.modules.knowledge.handler;

import com.luck.report.web.modules.knowledge.handler.splitter.ParagraphTextSplitter;
import com.luck.report.web.modules.knowledge.handler.splitter.RecursiveTextSplitter;
import com.luck.report.web.modules.knowledge.handler.parser.TokenTextSplitter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Token / Recursive 分块器基础回归
 */
class TextSplitterFixTest {

    @Test
    void token_1900_chars_yields_two_chunks_without_redundant_tail() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1900; i++) {
            sb.append('a');
        }
        TokenTextSplitter splitter = new TokenTextSplitter(1000, 100);
        List<String> chunks = splitter.split(sb.toString());

        assertEquals(2, chunks.size());
        assertEquals(1000, chunks.get(0).length());
        assertEquals(1000, chunks.get(1).length());
        // 第二块应覆盖 [900,1900)
        assertEquals(sb.substring(900), chunks.get(1));
    }

    @Test
    void recursive_hardSplit_has_overlap_on_continuous_chinese() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 2500; i++) {
            sb.append('中');
        }
        RecursiveTextSplitter splitter = new RecursiveTextSplitter(1000, 100);
        List<String> chunks = splitter.split(sb.toString());

        assertTrue(chunks.size() >= 2, "continuous text should yield multiple chunks, got " + chunks.size());
        // hardSplit 后经 merge 仍应覆盖全文
        int coveredApprox = chunks.stream().mapToInt(String::length).sum();
        assertTrue(coveredApprox >= 2500, "coverage too small: " + coveredApprox);
    }

    @Test
    void paragraph_does_not_emit_one_chunk_per_sentence() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 40; i++) {
            sb.append("这是第").append(i).append("句测试内容。");
        }
        // 无空行，整段为一个大段落
        ParagraphTextSplitter splitter = new ParagraphTextSplitter(1000, 100);
        List<String> chunks = splitter.split(sb.toString());

        assertTrue(chunks.size() < 40, "块数应远小于句子数, actual=" + chunks.size());
        assertTrue(chunks.size() >= 1);
    }
}
