package com.luck.report.web.modules.vector.service.impl;

import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.infra.modules.vector.domain.enums.RetrievalMethod;
import com.luck.report.infra.modules.vector.domain.param.VectorFullTextSearchParam;
import com.luck.report.infra.modules.vector.domain.param.VectorSearchParam;
import com.luck.report.infra.modules.vector.service.VectorStore;
import com.luck.report.web.config.properties.KnowledgeRetrievalProperties;
import com.luck.report.web.modules.chat.service.impl.EmbeddingService;
import com.luck.report.web.modules.vector.service.RerankService;
import com.luck.report.web.modules.vector.utils.ReciprocalRankFusion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 双路召回 + RRF + 可选 Rerank。学 Dify
 */
@Service("bean.hybridRetrievalService")
public class HybridRetrievalService {

    private static final Logger log = LoggerFactory.getLogger(HybridRetrievalService.class);

    @Autowired
    private VectorStore vectorStore;

    @Autowired
    @Qualifier("bean.embeddingService")
    private EmbeddingService embeddingService;

    @Autowired
    @Qualifier("bean.knowledgeRetrievalProperties")
    private KnowledgeRetrievalProperties props;

    @Autowired
    @Qualifier("bean.rerankService")
    private RerankService rerankService;

    private final ExecutorService pool = Executors.newFixedThreadPool(2);

    public List<VectorStoreSearchResult> retrieve(
            String query,
            VectorSearchParam baseParam,
            String embedModelId) {
        return retrieve(query, baseParam, embedModelId, null);
    }

    /**
     * @param methodOverride 非 null 时覆盖全局 method（评测强制 hybrid 用）
     */
    public List<VectorStoreSearchResult> retrieve(
            String query,
            VectorSearchParam baseParam,
            String embedModelId,
            RetrievalMethod methodOverride) {
        Assert.hasText(query, "query required");
        Assert.notNull(baseParam, "param required");

        RetrievalMethod method = methodOverride != null
                ? methodOverride
                : RetrievalMethod.fromConfig(props.getMethod());
        switch (method) {
            case FULL_TEXT:
                return finish(query, searchFullText(query, baseParam, props.getFulltextRecallTopK()));
            case HYBRID:
                return hybrid(query, baseParam, embedModelId);
            case SEMANTIC:
            default:
                return semantic(query, baseParam, embedModelId, props.getThreshold());
        }
    }

    private List<VectorStoreSearchResult> hybrid(String query, VectorSearchParam baseParam, String modelId) {
        final VectorSearchParam vectorParam = baseParam.toBuilder()
                .topK(props.getVectorRecallTopK())
                .threshold(0.0d)
                .build();
        final int ftTopK = props.getFulltextRecallTopK();

        Future<List<VectorStoreSearchResult>> fv = pool.submit(new Callable<List<VectorStoreSearchResult>>() {
            @Override
            public List<VectorStoreSearchResult> call() {
                return semantic(query, vectorParam, modelId, 0.0d);
            }
        });
        Future<List<VectorStoreSearchResult>> ff = pool.submit(new Callable<List<VectorStoreSearchResult>>() {
            @Override
            public List<VectorStoreSearchResult> call() {
                return searchFullText(query, baseParam, ftTopK);
            }
        });

        List<VectorStoreSearchResult> vectorHits = safeGet(fv, "vector");
        List<VectorStoreSearchResult> fullTextHits = safeGet(ff, "fulltext");
        log.info("hybrid recall: vector={}, fulltext={}, supportsFT={}",
                vectorHits.size(), fullTextHits.size(), vectorStore.supportsFullTextSearch());

        double wV = props.getEmbeddingWeight();
        double wF = 1.0d - wV;
        List<VectorStoreSearchResult> fused = ReciprocalRankFusion.fuse(
                vectorHits, fullTextHits, wV, wF, props.getRrfK(), props.getFusionTopN());
        return finish(query, fused);
    }

    private List<VectorStoreSearchResult> finish(String query, List<VectorStoreSearchResult> candidates) {
        return rerankService.rerank(query, candidates, props.getRerankTopN());
    }

    private List<VectorStoreSearchResult> semantic(
            String query, VectorSearchParam baseParam, String modelId, double threshold) {
        float[] qv = embeddingService.embed(query, modelId);
        VectorSearchParam full = baseParam.toBuilder()
                .queryVector(qv)
                .threshold(threshold)
                .build();
        return vectorStore.search(full);
    }

    private List<VectorStoreSearchResult> searchFullText(String query, VectorSearchParam base, int topK) {
        if (!vectorStore.supportsFullTextSearch()) {
            return Collections.emptyList();
        }
        VectorFullTextSearchParam ft = VectorFullTextSearchParam.builder()
                .queryText(query)
                .topK(topK)
                .vectorType(base.getVectorType())
                .metadataEquals(base.getMetadataEquals())
                .idMetaKey(base.getIdMetaKey())
                .validIds(base.getValidIds())
                .build();
        return vectorStore.searchByFullText(ft);
    }

    private List<VectorStoreSearchResult> safeGet(Future<List<VectorStoreSearchResult>> f, String name) {
        try {
            List<VectorStoreSearchResult> r = f.get(30, TimeUnit.SECONDS);
            return r != null ? r : Collections.<VectorStoreSearchResult>emptyList();
        } catch (Exception e) {
            log.warn("hybrid {} recall failed: {}", name, e.getMessage());
            return Collections.emptyList();
        }
    }
}
