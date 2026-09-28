package com.luck.report.web.modules.knowledge.service;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.modules.knowledge.domain.dto.BusinessKnowledgeQueryDTO;
import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.web.modules.knowledge.domain.dto.CreateBusinessKnowledgeDTO;
import com.luck.report.web.modules.knowledge.domain.dto.UpdateBusinessKnowledgeDTO;
import com.luck.report.web.modules.knowledge.domain.dto.UpdateKnowledgeChunkDTO;
import com.luck.report.web.modules.knowledge.domain.entity.BusinessKnowledge;
import com.luck.report.web.modules.knowledge.domain.vo.BusinessKnowledgeVO;
import com.luck.report.web.modules.knowledge.domain.vo.KnowledgeChunkVO;

import java.util.List;

/**
 * 业务知识服务
 *
 * @author luck
 */
public interface BusinessKnowledgeService {

    BusinessKnowledgeVO getKnowledgeById(String id);

    BusinessKnowledgeVO createKnowledge(CreateBusinessKnowledgeDTO createKnowledgeDTO);

    BusinessKnowledgeVO updateKnowledge(String id, UpdateBusinessKnowledgeDTO updateKnowledgeDTO);

    boolean deleteKnowledge(String id);

    void deleteKnowledgeBatch(List<String> ids);

    PageResultVO<BusinessKnowledgeVO> queryByPage(BusinessKnowledgeQueryDTO queryDTO);

    BusinessKnowledgeVO updateEnabledStatus(String id, Boolean enabled);

    void retryEmbedding(String id);

    List<KnowledgeChunkVO> listChunks(String knowledgeId);

    KnowledgeChunkVO updateChunk(String knowledgeId, String vectorId, UpdateKnowledgeChunkDTO dto);

    void deleteChunk(String knowledgeId, String vectorId);

    List<BusinessKnowledge> selectByIds(List<String> ids);

    List<String> selectEnabledKnowledgeIds();

    void fillBusinessKnowledgeContent(List<VectorStoreSearchResult> results);
}
