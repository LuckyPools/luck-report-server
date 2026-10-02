package com.luck.report.web.modules.knowledge.service;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.modules.knowledge.domain.dto.AgentKnowledgeQueryDTO;
import com.luck.report.web.modules.knowledge.domain.dto.CreateAgentKnowledgeDTO;
import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.web.modules.knowledge.domain.dto.UpdateAgentKnowledgeDTO;
import com.luck.report.web.modules.knowledge.domain.dto.UpdateKnowledgeChunkDTO;
import com.luck.report.web.modules.knowledge.domain.entity.AgentKnowledge;
import com.luck.report.web.modules.knowledge.domain.vo.AgentKnowledgeVO;
import com.luck.report.web.modules.knowledge.domain.vo.KnowledgeChunkVO;

import java.util.List;

/**
 * 智能体知识服务接口
 *
 * @author luck
 */
public interface AgentKnowledgeService {

    /**
     * 根据ID查询智能体知识详情
     *
     * @param id 智能体知识ID
     * @return 智能体知识VO
     */
    AgentKnowledgeVO getKnowledgeById(String id);

    /**
     * 创建智能体知识
     *
     * @param createKnowledgeDTO 创建智能体知识DTO
     * @return 智能体知识VO
     */
    AgentKnowledgeVO createKnowledge(CreateAgentKnowledgeDTO createKnowledgeDTO);

    /**
     * 更新智能体知识
     *
     * @param id 智能体知识ID
     * @param updateKnowledgeDTO 更新智能体知识DTO
     * @return 智能体知识VO
     */
    AgentKnowledgeVO updateKnowledge(String id, UpdateAgentKnowledgeDTO updateKnowledgeDTO);

    /**
     * 删除智能体知识
     *
     * @param id 智能体知识ID
     * @return 是否删除成功
     */
    boolean deleteKnowledge(String id);

    /**
     * 批量删除智能体知识
     *
     * @param ids 智能体知识ID列表
     */
    void deleteKnowledgeBatch(List<String> ids);

    /**
     * 分页条件查询智能体知识
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResultVO<AgentKnowledgeVO> queryByPage(AgentKnowledgeQueryDTO queryDTO);

    /**
     * 更新智能体知识是否生效
     *
     * @param id 智能体知识ID
     * @param enabled 是否生效
     * @return 智能体知识VO
     */
    AgentKnowledgeVO updateEnabledStatus(String id, Boolean enabled);

    /**
     * 重试向量化
     *
     * @param id 智能体知识ID
     */
    void retryEmbedding(String id);

    List<KnowledgeChunkVO> listChunks(String knowledgeId);

    KnowledgeChunkVO updateChunk(String knowledgeId, String vectorId, UpdateKnowledgeChunkDTO dto);

    void deleteChunk(String knowledgeId, String vectorId);

    /**
     * 根据ID列表批量查询智能体知识实体
     *
     * @param ids 智能体知识ID列表
     * @return 智能体知识实体列表
     */
    List<AgentKnowledge> selectByIds(List<String> ids);

    /**
     * 查询所有已生效且向量化完成的智能体知识ID列表
     *
     * @return 已生效的智能体知识ID列表
     */
    List<String> selectEnabledKnowledgeIds();

    /**
     * 回填智能体知识原文内容
     *
     * @param results 向量检索结果列表
     */
    void fillAgentKnowledgeContent(List<VectorStoreSearchResult> results);
}
