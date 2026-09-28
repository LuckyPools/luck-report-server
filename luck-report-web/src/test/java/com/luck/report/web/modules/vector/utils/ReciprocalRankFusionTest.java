package com.luck.report.web.modules.vector.utils;

import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.infra.modules.vector.domain.entity.VectorDocument;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReciprocalRankFusionTest {

    @Test
    void fuse_prefers_docs_appearing_in_both_lists() {
        List<VectorStoreSearchResult> vector = Arrays.asList(hit("a", 0.9), hit("b", 0.8), hit("c", 0.7));
        List<VectorStoreSearchResult> fulltext = Arrays.asList(hit("c", 5.0), hit("a", 4.0), hit("d", 3.0));

        List<VectorStoreSearchResult> fused = ReciprocalRankFusion.fuse(
                vector, fulltext, 0.5, 0.5, 60, 10);

        assertEquals("a", fused.get(0).getDocument().getId());
        assertEquals("c", fused.get(1).getDocument().getId());
        assertTrue(fused.stream().anyMatch(r -> "d".equals(r.getDocument().getId())));
    }

    @Test
    void fuse_returns_other_side_when_one_empty() {
        List<VectorStoreSearchResult> vector = Arrays.asList(hit("a", 0.9));
        List<VectorStoreSearchResult> fused = ReciprocalRankFusion.fuse(
                vector, Collections.emptyList(), 0.5, 0.5, 60, 5);
        assertEquals(1, fused.size());
        assertEquals("a", fused.get(0).getDocument().getId());
        assertEquals(0.9, fused.get(0).getScore(), 1e-9);
    }

    private static VectorStoreSearchResult hit(String id, double score) {
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("vectorType", "agentKnowledge");
        VectorDocument doc = new VectorDocument(id, "content-" + id, meta);
        return new VectorStoreSearchResult(doc, score);
    }
}
