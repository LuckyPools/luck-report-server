package com.luck.report.web.modules.knowledge.service;

import com.luck.report.web.modules.knowledge.domain.vo.KnowledgeChunkVO;

import java.util.List;

/**
 * 知识库向量片段 list / update / delete
 */
public interface KnowledgeChunkService {

    List<KnowledgeChunkVO> listChunks(String vectorType, String idMetaKey, String knowledgeId);

    KnowledgeChunkVO updateChunk(String vectorType, String idMetaKey, String knowledgeId,
                                 String vectorId, String content, String modelId);

    void deleteChunk(String vectorType, String idMetaKey, String knowledgeId, String vectorId);
}
