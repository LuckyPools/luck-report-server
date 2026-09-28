package com.luck.report.web.modules.vector.service.impl;

import com.luck.report.core.exception.ReportBizException;
import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.infra.modules.vector.domain.enums.RetrievalMethod;
import com.luck.report.infra.modules.vector.domain.param.VectorSearchParam;
import com.luck.report.web.config.KnowledgeRetrievalProperties;
import com.luck.report.web.modules.chat.service.impl.EmbeddingService;
import com.luck.report.web.modules.knowledge.constant.AgentKnowledgeMetadataConstant;
import com.luck.report.web.modules.knowledge.constant.BusinessKnowledgeMetadataConstant;
import com.luck.report.web.modules.knowledge.service.AgentKnowledgeService;
import com.luck.report.web.modules.knowledge.service.BusinessKnowledgeService;
import com.luck.report.web.modules.vector.domain.bo.SearchParams;
import com.luck.report.web.modules.vector.domain.vo.VectorSearchRequest;
import com.luck.report.web.modules.vector.domain.vo.VectorSearchResult;
import com.luck.report.web.modules.vector.service.ReportVectorSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class ReportVectorSearchServiceImpl implements ReportVectorSearchService {

    private static final Logger log = LoggerFactory.getLogger(ReportVectorSearchServiceImpl.class);

    private static final int DEFAULT_TOP_K = 5;
    private static final double DEFAULT_THRESHOLD = 0.5;

    private final AgentVectorStore agentVectorStore;
    private final BusinessKnowledgeService businessKnowledgeService;
    private final AgentKnowledgeService agentKnowledgeService;
    private final KnowledgeRetrievalProperties retrievalProperties;
    private final EmbeddingService embeddingService;

    public ReportVectorSearchServiceImpl(
            AgentVectorStore agentVectorStore,
            BusinessKnowledgeService businessKnowledgeService,
            AgentKnowledgeService agentKnowledgeService,
            KnowledgeRetrievalProperties retrievalProperties,
            EmbeddingService embeddingService) {
        this.agentVectorStore = agentVectorStore;
        this.businessKnowledgeService = businessKnowledgeService;
        this.agentKnowledgeService = agentKnowledgeService;
        this.retrievalProperties = retrievalProperties;
        this.embeddingService = embeddingService;
    }

    @Override
    public List<VectorSearchResult> search(VectorSearchRequest request) {
        Assert.notNull(request, "request required");
        Assert.hasText(request.getQuery(), "query required");
        Assert.hasText(request.getVectorType(), "vectorType required");

        boolean useRetrievalPolicy = usesKnowledgeRetrievalPolicy(request.getVectorType());
        SearchParams params = resolveSearchParams(request, useRetrievalPolicy);

        String idMetaKey = resolveIdMetaKey(request.getVectorType());
        List<String> validIds = resolveValidIds(request.getVectorType());
        if (idMetaKey != null && (validIds == null || validIds.isEmpty())) {
            log.info("向量检索空结果原因=noEnabledIds, vectorType={}, queryLen={}",
                    request.getVectorType(), request.getQuery().length());
            return Collections.emptyList();
        }

        VectorSearchParam param = VectorSearchParam.builder()
                .topK(params.getTopK())
                .threshold(params.getThreshold())
                .vectorType(request.getVectorType())
                .metadataEquals(request.getMetadataFilters())
                .idMetaKey(idMetaKey)
                .validIds(validIds)
                .build();

        List<VectorStoreSearchResult> results = recall(
                request.getQuery(), param, params.getEmbedModelId(), useRetrievalPolicy, request.getForceHybrid());
        int recallCount = results.size();
        if (results.isEmpty()) {
            log.info("向量检索空结果原因=noVectorHit, vectorType={}, modelId={}, threshold={}, recallTopK={}, queryLen={}",
                    request.getVectorType(), params.getEmbedModelId(), params.getThreshold(), params.getTopK(),
                    request.getQuery().length());
        }

        fillContent(request.getVectorType(), results);

        log.info("向量检索完成: vectorType={}, modelId={}, queryLen={}, threshold={}, recallTopK={}, recallCount={}, afterFillCount={}",
                request.getVectorType(), params.getEmbedModelId(), request.getQuery().length(),
                params.getThreshold(), params.getTopK(), recallCount, results.size());

        return toVoList(results);
    }

    private boolean usesKnowledgeRetrievalPolicy(String vectorType) {
        return AgentKnowledgeMetadataConstant.AGENT_KNOWLEDGE.equals(vectorType)
                || BusinessKnowledgeMetadataConstant.BUSINESS_KNOWLEDGE.equals(vectorType);
    }

    private SearchParams resolveSearchParams(VectorSearchRequest request, boolean useRetrievalPolicy) {
        if (useRetrievalPolicy) {
            String embedModelId = null;
            try {
                embedModelId = embeddingService.resolveDefaultEmbeddingModelId();
            } catch (Exception e) {
                log.warn("解析默认嵌入模型失败，将使用 EmbeddingService 内部默认逻辑: {}", e.getMessage());
            }
            return new SearchParams(
                    retrievalProperties.getRecallTopK(),
                    retrievalProperties.getThreshold(),
                    embedModelId);
        }
        int topK = request.getTopK() != null ? request.getTopK() : DEFAULT_TOP_K;
        double threshold = request.getThreshold() != null ? request.getThreshold() : DEFAULT_THRESHOLD;
        return new SearchParams(topK, threshold, null);
    }

    private List<VectorStoreSearchResult> recall(
            String query,
            VectorSearchParam param,
            String embedModelId,
            boolean useRetrievalPolicy,
            Boolean forceHybrid) {
        if (Boolean.TRUE.equals(forceHybrid)) {
            if (!agentVectorStore.supportsFullTextSearch()) {
                throw new ReportBizException("error.vector.fulltextUnsupportedForHybridEval");
            }
            return agentVectorStore.searchWithRetrievalPolicy(
                    query, param, embedModelId, RetrievalMethod.HYBRID);
        }
        if (useRetrievalPolicy) {
            return agentVectorStore.searchWithRetrievalPolicy(query, param, embedModelId);
        }
        return agentVectorStore.search(query, param, embedModelId);
    }

    private void fillContent(String vectorType, List<VectorStoreSearchResult> results) {
        if (BusinessKnowledgeMetadataConstant.BUSINESS_KNOWLEDGE.equals(vectorType)) {
            businessKnowledgeService.fillBusinessKnowledgeContent(results);
        } else if (AgentKnowledgeMetadataConstant.AGENT_KNOWLEDGE.equals(vectorType)) {
            agentKnowledgeService.fillAgentKnowledgeContent(results);
        }
    }

    private String resolveIdMetaKey(String vectorType) {
        if (BusinessKnowledgeMetadataConstant.BUSINESS_KNOWLEDGE.equals(vectorType)) {
            return BusinessKnowledgeMetadataConstant.DB_BUSINESS_KNOWLEDGE_ID;
        }
        if (AgentKnowledgeMetadataConstant.AGENT_KNOWLEDGE.equals(vectorType)) {
            return AgentKnowledgeMetadataConstant.DB_AGENT_KNOWLEDGE_ID;
        }
        return null;
    }

    private List<String> resolveValidIds(String vectorType) {
        if (BusinessKnowledgeMetadataConstant.BUSINESS_KNOWLEDGE.equals(vectorType)) {
            return businessKnowledgeService.selectEnabledKnowledgeIds();
        }
        if (AgentKnowledgeMetadataConstant.AGENT_KNOWLEDGE.equals(vectorType)) {
            return agentKnowledgeService.selectEnabledKnowledgeIds();
        }
        return null;
    }

    private static List<VectorSearchResult> toVoList(List<VectorStoreSearchResult> results) {
        List<VectorSearchResult> voList = new ArrayList<VectorSearchResult>(results.size());
        for (VectorStoreSearchResult r : results) {
            voList.add(new VectorSearchResult(
                    r.getDocument().getId(),
                    r.getDocument().getContent(),
                    r.getScore(),
                    r.getDocument().getMetadata()));
        }
        return voList;
    }
}
