package com.luck.report.web.modules.vector.service.impl;

import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.infra.modules.vector.domain.entity.VectorDocument;
import com.luck.report.infra.modules.vector.domain.enums.RetrievalMethod;
import com.luck.report.infra.modules.vector.domain.param.VectorFullTextSearchParam;
import com.luck.report.infra.modules.vector.domain.param.VectorSearchParam;
import com.luck.report.infra.modules.vector.service.VectorStore;
import com.luck.report.web.config.KnowledgeRetrievalProperties;
import com.luck.report.web.modules.chat.service.impl.EmbeddingService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HybridRetrievalServiceTest {

    @Test
    void hybrid_prefers_dual_hit_id_after_rrf_and_noop_rerank() throws Exception {
        List<VectorStoreSearchResult> vectorHits = Arrays.asList(
                hit("a", 0.9), hit("b", 0.8));
        List<VectorStoreSearchResult> fullTextHits = Arrays.asList(
                hit("a", 4.0), hit("c", 3.0));

        FakeVectorStore fakeStore = new FakeVectorStore(vectorHits, fullTextHits);
        KnowledgeRetrievalProperties props = hybridProps();

        HybridRetrievalService service = new HybridRetrievalService();
        inject(service, "vectorStore", fakeStore);
        inject(service, "embeddingService", new StubEmbeddingService());
        inject(service, "props", props);
        inject(service, "rerankService", new NoOpRerankService());

        VectorSearchParam param = VectorSearchParam.builder()
                .topK(5)
                .threshold(0.35)
                .vectorType("agentKnowledge")
                .build();

        List<VectorStoreSearchResult> results = service.retrieve("query", param, "model-1");

        assertTrue(results.size() >= 2);
        assertEquals("a", results.get(0).getDocument().getId());
        assertTrue(results.stream().anyMatch(r -> "b".equals(r.getDocument().getId())));
        assertTrue(results.stream().anyMatch(r -> "c".equals(r.getDocument().getId())));
    }

    @Test
    void override_hybrid_ignores_global_semantic_method() throws Exception {
        List<VectorStoreSearchResult> vectorHits = Arrays.asList(hit("a", 0.9));
        List<VectorStoreSearchResult> fullTextHits = Arrays.asList(hit("b", 3.0));
        FakeVectorStore fakeStore = new FakeVectorStore(vectorHits, fullTextHits);

        KnowledgeRetrievalProperties props = hybridProps();
        props.setMethod("semantic");

        HybridRetrievalService service = new HybridRetrievalService();
        inject(service, "vectorStore", fakeStore);
        inject(service, "embeddingService", new StubEmbeddingService());
        inject(service, "props", props);
        inject(service, "rerankService", new NoOpRerankService());

        VectorSearchParam param = VectorSearchParam.builder()
                .topK(5)
                .threshold(0.35)
                .vectorType("agentKnowledge")
                .build();

        service.retrieve("query", param, "model-1", RetrievalMethod.HYBRID);

        assertEquals(1, fakeStore.vectorSearchCount.get());
        assertEquals(1, fakeStore.fullTextSearchCount.get());
    }

    private static KnowledgeRetrievalProperties hybridProps() {
        KnowledgeRetrievalProperties props = new KnowledgeRetrievalProperties();
        props.setMethod("hybrid");
        props.setVectorRecallTopK(10);
        props.setFulltextRecallTopK(10);
        props.setEmbeddingWeight(0.5);
        props.setRrfK(60);
        props.setFusionTopN(10);
        props.setRerankTopN(10);
        return props;
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = HybridRetrievalService.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static VectorStoreSearchResult hit(String id, double score) {
        Map<String, Object> meta = new HashMap<String, Object>();
        meta.put("vectorType", "agentKnowledge");
        VectorDocument doc = new VectorDocument(id, "content-" + id, meta);
        return new VectorStoreSearchResult(doc, score);
    }

    private static final class StubEmbeddingService extends EmbeddingService {
        StubEmbeddingService() {
            super(null);
        }

        @Override
        public float[] embed(String text, String modelId) {
            return new float[] {1.0f, 0.0f};
        }
    }

    private static final class FakeVectorStore implements VectorStore {
        private final List<VectorStoreSearchResult> vectorHits;
        private final List<VectorStoreSearchResult> fullTextHits;
        private final AtomicInteger vectorSearchCount = new AtomicInteger();
        private final AtomicInteger fullTextSearchCount = new AtomicInteger();

        FakeVectorStore(List<VectorStoreSearchResult> vectorHits, List<VectorStoreSearchResult> fullTextHits) {
            this.vectorHits = vectorHits;
            this.fullTextHits = fullTextHits;
        }

        @Override
        public void add(List<VectorDocument> documents) {
        }

        @Override
        public boolean delete(List<String> ids) {
            return false;
        }

        @Override
        public boolean deleteByVectorType(String vectorType) {
            return false;
        }

        @Override
        public boolean deleteByVectorTypeAndMetadata(String vectorType, String metaKey, Object metaValue) {
            return false;
        }

        @Override
        public List<VectorDocument> listByVectorTypeAndMetadata(String vectorType, String metaKey, Object metaValue) {
            return Collections.emptyList();
        }

        @Override
        public List<VectorStoreSearchResult> search(VectorSearchParam param) {
            vectorSearchCount.incrementAndGet();
            return vectorHits;
        }

        @Override
        public boolean supportsFullTextSearch() {
            return true;
        }

        @Override
        public List<VectorStoreSearchResult> searchByFullText(VectorFullTextSearchParam param) {
            fullTextSearchCount.incrementAndGet();
            return fullTextHits;
        }
    }
}
