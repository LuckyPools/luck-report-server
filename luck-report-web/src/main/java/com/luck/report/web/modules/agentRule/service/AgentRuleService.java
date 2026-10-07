package com.luck.report.web.modules.agentRule.service;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.modules.agentRule.domain.dto.AgentRuleDTO;
import com.luck.report.web.modules.agentRule.domain.dto.AgentRuleQueryDTO;
import com.luck.report.web.modules.agentRule.domain.vo.AgentRulePromptVO;

import java.util.List;

/**
 * 智能体规则服务
 *
 * @author luck
 */
public interface AgentRuleService {

    /**
     * 根据ID查询规则详情
     *
     * @param id 规则ID
     * @return 规则 DTO，不存在则 null
     */
    AgentRuleDTO getById(String id);

    /**
     * 创建规则
     *
     * @param dto 规则 DTO
     * @return 创建后的规则
     */
    AgentRuleDTO create(AgentRuleDTO dto);

    /**
     * 更新规则
     *
     * @param id 规则ID
     * @param dto 规则 DTO
     * @return 更新后的规则
     */
    AgentRuleDTO update(String id, AgentRuleDTO dto);

    /**
     * 更新规则生效状态
     *
     * @param id 规则ID
     * @param enabled 是否生效
     * @return 更新后的规则
     */
    AgentRuleDTO updateEnabledStatus(String id, Boolean enabled);

    /**
     * 软删除规则
     *
     * @param id 规则ID
     * @return 是否删除成功
     */
    boolean removeById(String id);

    /**
     * 批量软删除规则
     *
     * @param ids 规则ID列表
     */
    void removeByIds(List<String> ids);

    /**
     * 分页查询规则
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResultVO<AgentRuleDTO> listPage(AgentRuleQueryDTO queryDTO);

    /**
     * 拼接已启用规则为对话注入提示词
     *
     * @return 提示词 VO（content 可能为空串）
     */
    AgentRulePromptVO buildPrompt();
}
