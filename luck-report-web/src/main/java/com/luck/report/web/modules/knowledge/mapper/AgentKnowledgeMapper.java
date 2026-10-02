package com.luck.report.web.modules.knowledge.mapper;

import com.luck.report.web.modules.knowledge.domain.dto.AgentKnowledgeQueryDTO;
import com.luck.report.web.modules.knowledge.domain.entity.AgentKnowledge;
import com.luck.report.jdbc.Param;

import java.util.List;

/**
 * 智能体知识Mapper
 *
 * @author luck
 */
public interface AgentKnowledgeMapper {

    /**
     * 插入智能体知识
     *
     * @param knowledge 智能体知识实体
     * @return 影响行数
     */
    int insert(AgentKnowledge knowledge);

    /**
     * 根据ID更新智能体知识（动态更新非空字段）
     *
     * @param knowledge 智能体知识实体
     * @return 影响行数
     */
    int update(AgentKnowledge knowledge);

    /**
     * 根据ID查询智能体知识（排除已删除）
     *
     * @param id 智能体知识ID
     * @return 智能体知识实体
     */
    AgentKnowledge selectById(@Param("id") String id);

    /**
     * 分页条件查询智能体知识
     *
     * @param queryDTO 查询条件
     * @param offset   偏移量
     * @return 智能体知识列表
     */
    List<AgentKnowledge> selectByConditionsWithPage(@Param("queryDTO") AgentKnowledgeQueryDTO queryDTO,
                                                     @Param("offset") Integer offset,
                                                     @Param("pageSize") Integer pageSize);

    /**
     * 统计符合条件的智能体知识数量
     *
     * @param queryDTO 查询条件
     * @return 符合条件的记录数
     */
    Long countByConditions(@Param("queryDTO") AgentKnowledgeQueryDTO queryDTO);

    /**
     * 查询所有生效的智能体知识ID列表
     *
     * @return 生效的智能体知识ID列表
     */
    List<String> selectEnabledKnowledgeIds();

    /**
     * 根据ID列表批量查询智能体知识
     *
     * @param ids 智能体知识ID列表
     * @return 智能体知识实体列表
     */
    List<AgentKnowledge> selectByIds(@Param("ids") List<String> ids);
}
