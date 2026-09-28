package com.luck.report.web.modules.knowledge.handler.splitter;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * FastGPT markdownTableSplit 移植回归
 */
class TableTextSplitterTest {

    @Test
    void fiveShortRows_withSmallChunkSize_yieldsMultipleChunksEachWithHeader() {
        String table =
                "| Name | Age |\n"
                        + "| --- | --- |\n"
                        + "| Alice | 1 |\n"
                        + "| Bob | 2 |\n"
                        + "| Carol | 3 |\n"
                        + "| Dave | 4 |\n"
                        + "| Eve | 5 |\n";

        // header+sep ~ 28 chars; each row ~ 14 → chunkSize forces multiple windows
        TableTextSplitter splitter = new TableTextSplitter(50);
        List<String> chunks = splitter.split(table);

        assertTrue(chunks.size() >= 2, "expected multiple chunks, got " + chunks.size());
        for (String chunk : chunks) {
            assertTrue(chunk.startsWith("| Name | Age |"), "chunk missing header: " + chunk);
            assertTrue(chunk.contains("| --- | --- |"), "chunk missing separator: " + chunk);
        }
    }

    @Test
    void headerAndSeparatorOnly_returnsEmpty() {
        String table = "| Name | Age |\n| --- | --- |\n";
        TableTextSplitter splitter = new TableTextSplitter(200);
        List<String> chunks = splitter.split(table);
        assertEquals(0, chunks.size());
    }
}
