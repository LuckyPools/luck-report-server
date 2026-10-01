package com.luck.report.web.modules.knowledge.service.impl;

import com.luck.report.infra.modules.vector.domain.entity.VectorDocument;
import com.luck.report.infra.modules.vector.service.VectorStore;
import com.luck.report.web.modules.chat.service.impl.EmbeddingService;
import com.luck.report.web.modules.knowledge.domain.vo.KnowledgeChunkVO;
import com.luck.report.web.modules.knowledge.service.KnowledgeChunkService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 以向量库为权威源的片段管理
 */
@Service("bean.knowledgeChunkService")
@AllArgsConstructor
public class KnowledgeChunkServiceImpl implements KnowledgeChunkService {

    private static final String META_CHUNK_INDEX = "chunk_index";
    private static final String META_CHUNK_TOTAL = "chunk_total";
    private static final String META_MODEL_ID = "modelId";
    private static final String META_SPLITTER_TYPE = "splitterType";

    private final VectorStore vectorStore;
    @Qualifier("bean.embeddingService")
    private final EmbeddingService embeddingService;

    @Override
    public List<KnowledgeChunkVO> listChunks(String vectorType, String idMetaKey, String knowledgeId) {
        List<VectorDocument> docs = vectorStore.listByVectorTypeAndMetadata(vectorType, idMetaKey, knowledgeId);
        if (docs == null || docs.isEmpty()) {
            return Collections.emptyList();
        }
        List<KnowledgeChunkVO> list = new ArrayList<>(docs.size());
        for (VectorDocument doc : docs) {
            list.add(toVo(doc));
        }
        list.sort(Comparator.comparingInt(vo -> vo.getChunkIndex() == null ? 0 : vo.getChunkIndex()));
        return list;
    }

    @Override
    public KnowledgeChunkVO updateChunk(String vectorType, String idMetaKey, String knowledgeId,
                                        String vectorId, String content, String modelId) {
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("片段内容不能为空");
        }
        VectorDocument existing = requireOwnedChunk(vectorType, idMetaKey, knowledgeId, vectorId);
        float[] vector = embeddingService.embed(content, modelId);
        Map<String, Object> metadata = existing.getMetadata() == null
                ? new HashMap<>()
                : new HashMap<>(existing.getMetadata());
        VectorDocument updated = new VectorDocument(vectorId, content, metadata);
        updated.setVector(vector);
        vectorStore.upsert(Collections.singletonList(updated));
        return toVo(updated);
    }

    @Override
    public void deleteChunk(String vectorType, String idMetaKey, String knowledgeId, String vectorId) {
        requireOwnedChunk(vectorType, idMetaKey, knowledgeId, vectorId);
        vectorStore.deleteByVectorTypeAndIds(vectorType, Collections.singletonList(vectorId));
    }

    private VectorDocument requireOwnedChunk(String vectorType, String idMetaKey,
                                             String knowledgeId, String vectorId) {
        List<VectorDocument> docs = vectorStore.listByVectorTypeAndMetadata(vectorType, idMetaKey, knowledgeId);
        if (docs == null || docs.isEmpty()) {
            throw new IllegalArgumentException("片段不存在或不属于当前知识");
        }
        for (VectorDocument doc : docs) {
            if (doc == null || !vectorIdEquals(doc.getId(), vectorId)) {
                continue;
            }
            Object ownedId = doc.getMetadata() == null ? null : doc.getMetadata().get(idMetaKey);
            if (ownedId == null || !String.valueOf(ownedId).equals(String.valueOf(knowledgeId))) {
                throw new IllegalArgumentException("片段不存在或不属于当前知识");
            }
            return doc;
        }
        throw new IllegalArgumentException("片段不存在或不属于当前知识");
    }

    private static boolean vectorIdEquals(String left, String right) {
        return left != null && left.equals(right);
    }

    private static KnowledgeChunkVO toVo(VectorDocument doc) {
        Map<String, Object> meta = doc.getMetadata();
        KnowledgeChunkVO vo = new KnowledgeChunkVO();
        vo.setVectorId(doc.getId());
        vo.setContent(doc.getContent());
        vo.setChunkIndex(coerceInteger(meta, META_CHUNK_INDEX, 0));
        vo.setChunkTotal(coerceInteger(meta, META_CHUNK_TOTAL, null));
        vo.setModelId(coerceString(meta, META_MODEL_ID));
        vo.setSplitterType(coerceString(meta, META_SPLITTER_TYPE));
        return vo;
    }

    private static Integer coerceInteger(Map<String, Object> meta, String key, Integer defaultValue) {
        if (meta == null) {
            return defaultValue;
        }
        Object raw = meta.get(key);
        if (raw == null) {
            return defaultValue;
        }
        if (raw instanceof Number) {
            return ((Number) raw).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(raw).trim());
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static String coerceString(Map<String, Object> meta, String key) {
        if (meta == null) {
            return null;
        }
        Object raw = meta.get(key);
        return raw == null ? null : String.valueOf(raw);
    }
}
