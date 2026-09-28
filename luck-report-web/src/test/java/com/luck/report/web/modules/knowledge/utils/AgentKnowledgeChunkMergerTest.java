package com.luck.report.web.modules.knowledge.utils;

import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.infra.modules.vector.domain.entity.VectorDocument;
import com.luck.report.web.modules.knowledge.constant.AgentKnowledgeMetadataConstant;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 同知识 Top-N chunk 分条返回回归
 */
class AgentKnowledgeChunkMergerTest {

    @Test
    void select_same_knowledge_returns_separate_chunks_with_own_scores() {
        List<VectorStoreSearchResult> input = new ArrayList<>();
        input.add(hit("k1", 0, 0.70, "A"));
        input.add(hit("k1", 2, 0.95, "C"));
        input.add(hit("k1", 1, 0.80, "B"));
        input.add(hit("k1", 3, 0.60, "D")); // mergeTopN=3 时丢弃

        List<VectorStoreSearchResult> selected =
                AgentKnowledgeChunkMerger.selectTopChunksByKnowledge(input, 3, 5);

        assertEquals(3, selected.size());
        // 按 score 降序：C(0.95), B(0.80), A(0.70)
        assertEquals(0.95, selected.get(0).getScore(), 1e-6);
        assertEquals("C", selected.get(0).getDocument().getContent());
        assertEquals(0.80, selected.get(1).getScore(), 1e-6);
        assertEquals("B", selected.get(1).getDocument().getContent());
        assertEquals(0.70, selected.get(2).getScore(), 1e-6);
        assertEquals("A", selected.get(2).getDocument().getContent());

        List<Integer> indexes = selected.stream()
                .map(r -> ((Number) r.getDocument().getMetadata().get("chunk_index")).intValue())
                .collect(Collectors.toList());
        assertTrue(indexes.contains(0));
        assertTrue(indexes.contains(1));
        assertTrue(indexes.contains(2));
        assertTrue(!indexes.contains(3));
    }

    @Test
    void select_respects_finalTopK_knowledges_then_expands_chunks() {
        List<VectorStoreSearchResult> input = new ArrayList<>();
        input.add(hit("k1", 0, 0.9, "k1a"));
        input.add(hit("k1", 1, 0.85, "k1b"));
        input.add(hit("k2", 0, 0.8, "k2a"));
        input.add(hit("k3", 0, 0.7, "k3a"));

        // finalTopK=2 → 只保留 k1、k2；k1 有 2 块 → 共 3 条
        List<VectorStoreSearchResult> selected =
                AgentKnowledgeChunkMerger.selectTopChunksByKnowledge(input, 3, 2);

        assertEquals(3, selected.size());
        long distinctKnowledge = selected.stream()
                .map(r -> String.valueOf(r.getDocument().getMetadata()
                        .get(AgentKnowledgeMetadataConstant.DB_AGENT_KNOWLEDGE_ID)))
                .distinct()
                .count();
        assertEquals(2, distinctKnowledge);
        assertTrue(selected.stream().noneMatch(r ->
                "k3".equals(String.valueOf(r.getDocument().getMetadata()
                        .get(AgentKnowledgeMetadataConstant.DB_AGENT_KNOWLEDGE_ID)))));
    }

    @Test
    void qa_single_chunk_without_index_still_one_result() {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(AgentKnowledgeMetadataConstant.DB_AGENT_KNOWLEDGE_ID, "qa1");
        metadata.put("title", "FAQ");
        VectorDocument doc = new VectorDocument("问题: x, 答案: y", metadata);
        List<VectorStoreSearchResult> input = Collections.singletonList(new VectorStoreSearchResult(doc, 0.88));

        List<VectorStoreSearchResult> selected =
                AgentKnowledgeChunkMerger.selectTopChunksByKnowledge(input, 3, 5);
        assertEquals(1, selected.size());
        assertEquals(0.88, selected.get(0).getScore(), 1e-6);
        assertEquals("问题: x, 答案: y", selected.get(0).getDocument().getContent());
    }

    private static VectorStoreSearchResult hit(String knowledgeId, int chunkIndex, double score, String content) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(AgentKnowledgeMetadataConstant.DB_AGENT_KNOWLEDGE_ID, knowledgeId);
        metadata.put("chunk_index", chunkIndex);
        metadata.put("chunk_total", 10);
        metadata.put("title", "doc-" + knowledgeId);
        return new VectorStoreSearchResult(new VectorDocument(content, metadata), score);
    }
}
