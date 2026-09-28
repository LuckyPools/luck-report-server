package com.luck.report.web.modules.knowledge.service.impl;

import com.luck.report.infra.modules.vector.domain.entity.VectorDocument;
import com.luck.report.web.modules.knowledge.domain.vo.KnowledgeChunkVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class KnowledgeChunkServiceImplTest {

    private RecordingChunkVectorStore vectorStore;
    private KnowledgeChunkServiceImpl service;

    @BeforeEach
    void setUp() {
        vectorStore = new RecordingChunkVectorStore();
        service = new KnowledgeChunkServiceImpl(
                vectorStore, new FixedEmbeddingService(new float[]{0.1f, 0.2f}));
    }

    @Test
    void listChunks_sortsByChunkIndex() {
        vectorStore.setListResult(Arrays.asList(
                doc("1", "B", 1, "k1"),
                doc("2", "A", 0, "k1")));

        List<KnowledgeChunkVO> list = service.listChunks(
                "agentKnowledge", "agentKnowledgeId", "k1");

        assertEquals("2", list.get(0).getVectorId());
        assertEquals(0, list.get(0).getChunkIndex());
    }

    @Test
    void updateChunk_rejectsWrongKnowledge() {
        vectorStore.setListResult(Collections.<VectorDocument>emptyList());

        assertThrows(IllegalArgumentException.class,
                () -> service.updateChunk("agentKnowledge", "agentKnowledgeId", "k1",
                        "v1", "new", "model-1"));
        assertEquals(0, vectorStore.getUpsertCount());
    }

    @Test
    void updateChunk_embedsAndUpsertsSameId() {
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("vectorType", "agentKnowledge");
        meta.put("agentKnowledgeId", "k1");
        meta.put("chunk_index", 0);
        meta.put("chunk_total", 1);
        vectorStore.setListResult(Collections.singletonList(new VectorDocument("v1", "old", meta)));

        KnowledgeChunkVO vo = service.updateChunk(
                "agentKnowledge", "agentKnowledgeId", "k1", "v1", "new", "model-1");

        assertEquals("new", vo.getContent());
        VectorDocument saved = vectorStore.getLastUpsert().get(0);
        assertEquals("v1", saved.getId());
        assertEquals("new", saved.getContent());
        assertArrayEquals(new float[]{0.1f, 0.2f}, saved.getVector(), 0.0001f);
        assertEquals("k1", saved.getMetadata().get("agentKnowledgeId"));
    }

    @Test
    void deleteChunk_checksOwnershipThenDeletes() {
        vectorStore.setListResult(Collections.singletonList(doc("v1", "x", 0, "k1")));

        service.deleteChunk("agentKnowledge", "agentKnowledgeId", "k1", "v1");

        assertEquals("agentKnowledge", vectorStore.getLastDeleteVectorType());
        assertEquals(Collections.singletonList("v1"), vectorStore.getLastDeleteIds());
    }

    @Test
    void deleteChunk_rejectsMissingChunk() {
        vectorStore.setListResult(Collections.<VectorDocument>emptyList());

        assertThrows(IllegalArgumentException.class,
                () -> service.deleteChunk("agentKnowledge", "agentKnowledgeId", "k1", "v1"));
        assertNull(vectorStore.getLastDeleteIds());
    }

    private static VectorDocument doc(String id, String content, int index, String kid) {
        Map<String, Object> m = new HashMap<String, Object>();
        m.put("agentKnowledgeId", kid);
        m.put("chunk_index", index);
        m.put("chunk_total", 2);
        return new VectorDocument(id, content, m);
    }
}
