package com.luck.report.web.modules.agentRule.mapper;

import com.luck.report.jdbc.Param;
import com.luck.report.web.modules.agentRule.domain.dto.AgentRuleQueryDTO;
import com.luck.report.web.modules.agentRule.domain.entity.AgentRule;

import java.util.List;

/**
 * 智能体规则 Mapper
 *
 * @author luck
 */
public interface AgentRuleMapper {

    /**
     * 根据ID查询未删除的规则
     *
     * @param id 规则ID
     * @return 规则实体，不存在则 null
     */
    AgentRule selectById(@Param("id") String id);

    /**
     * 插入规则
     *
     * @param agentRule 规则实体
     * @return 影响行数
     */
    int insert(AgentRule agentRule);

    /**
     * 按ID更新规则
     *
     * @param agentRule 规则实体
     * @return 影响行数
     */
    int updateById(AgentRule agentRule);

    /**
     * 分页查询规则
     *
     * @param queryDTO 查询条件
     * @param offset 偏移量
     * @param pageSize 每页大小
     * @return 规则列表
     */
    List<AgentRule> selectPage(@Param("queryDTO") AgentRuleQueryDTO queryDTO,
                               @Param("offset") Integer offset,
                               @Param("pageSize") Integer pageSize);

    /**
     * 统计符合条件的规则数量
     *
     * @param queryDTO 查询条件
     * @return 记录数
     */
    Long selectCount(@Param("queryDTO") AgentRuleQueryDTO queryDTO);

    /**
     * 按条件查询规则列表（非分页）；title 精确匹配，enabled 可选，queryDTO 为空条件时查全部
     *
     * @param queryDTO 查询条件
     * @return 规则列表，按 sort 升序、update_time 降序
     */
    List<AgentRule> selectList(@Param("queryDTO") AgentRuleQueryDTO queryDTO);
}
