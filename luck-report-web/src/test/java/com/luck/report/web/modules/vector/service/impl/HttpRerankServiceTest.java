package com.luck.report.web.modules.vector.service.impl;

import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.infra.modules.vector.domain.entity.VectorDocument;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HttpRerankServiceTest {

    @Test
    void applyProviderResults_ordersByRelevanceScore() throws Exception {
        List<VectorStoreSearchResult> slice = Arrays.asList(
                hit("a", "doc-a"),
                hit("b", "doc-b"),
                hit("c", "doc-c"));
        String json = "{\"results\":["
                + "{\"index\":2,\"relevance_score\":0.95},"
                + "{\"index\":0,\"relevance_score\":0.5},"
                + "{\"index\":1,\"relevance_score\":0.8}"
                + "]}";

        List<VectorStoreSearchResult> out = HttpRerankService.applyProviderResults(slice, json);

        assertEquals(3, out.size());
        assertEquals("c", out.get(0).getDocument().getId());
        assertEquals("b", out.get(1).getDocument().getId());
        assertEquals("a", out.get(2).getDocument().getId());
        assertEquals(0.95, out.get(0).getScore(), 1e-6);
    }

    @Test
    void applyProviderResults_supportsBailianOutputWrapper() throws Exception {
        List<VectorStoreSearchResult> slice = Arrays.asList(hit("a", "x"), hit("b", "y"));
        String json = "{\"output\":{\"results\":[{\"index\":1,\"relevance_score\":0.88}]}}";

        List<VectorStoreSearchResult> out = HttpRerankService.applyProviderResults(slice, json);

        assertEquals(1, out.size());
        assertEquals("b", out.get(0).getDocument().getId());
    }

    private static VectorStoreSearchResult hit(String id, String content) {
        Map<String, Object> meta = new HashMap<String, Object>();
        return new VectorStoreSearchResult(new VectorDocument(id, content, meta), 0.1);
    }
}
