package com.luck.report.web.modules.agentRule.service.impl;

import com.luck.report.core.exception.ReportBizException;
import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.modules.agentRule.converter.AgentRuleConverter;
import com.luck.report.web.modules.agentRule.domain.dto.AgentRuleDTO;
import com.luck.report.web.modules.agentRule.domain.dto.AgentRuleQueryDTO;
import com.luck.report.web.modules.agentRule.domain.entity.AgentRule;
import com.luck.report.web.modules.agentRule.domain.vo.AgentRulePromptVO;
import com.luck.report.web.modules.agentRule.mapper.AgentRuleMapper;
import com.luck.report.web.modules.agentRule.service.AgentRuleService;
import com.luck.report.web.security.utils.SecurityUtils;
import com.luck.report.web.utils.SnowflakeIdGenerator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 智能体规则服务实现
 *
 * @author luck
 */
@Slf4j
@Service("bean.agentRuleService")
@AllArgsConstructor
public class AgentRuleServiceImpl implements AgentRuleService {

    @Qualifier("bean.agentRuleMapper")
    private final AgentRuleMapper agentRuleMapper;

    /**
     * 根据ID查询规则详情
     *
     * @param id 规则ID
     * @return 规则 DTO，不存在则 null
     */
    @Override
    public AgentRuleDTO getById(String id) {
        return AgentRuleConverter.toDTO(agentRuleMapper.selectById(id));
    }

    /**
     * 创建规则
     *
     * @param dto 规则 DTO
     * @return 创建后的规则
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public AgentRuleDTO create(AgentRuleDTO dto) {
        clean(dto);
        AgentRule entity = AgentRuleConverter.toEntity(dto);
        entity.setId(SnowflakeIdGenerator.generateId());
        String userId = SecurityUtils.getCurrentUserId();
        entity.setCreateBy(userId);
        entity.setUpdateBy(userId);
        agentRuleMapper.insert(entity);
        log.info("新增智能体规则: id={}, title={}", entity.getId(), entity.getTitle());
        return AgentRuleConverter.toDTO(entity);
    }

    /**
     * 更新规则
     *
     * @param id 规则ID
     * @param dto 规则 DTO
     * @return 更新后的规则
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public AgentRuleDTO update(String id, AgentRuleDTO dto) {
        clean(dto);
        AgentRule entity = requireById(id);
        AgentRuleConverter.applyUpdate(dto, entity);
        entity.setUpdateBy(SecurityUtils.getCurrentUserId());
        agentRuleMapper.updateById(entity);
        log.info("更新智能体规则: id={}, title={}", id, entity.getTitle());
        return AgentRuleConverter.toDTO(entity);
    }

    /**
     * 更新规则生效状态
     *
     * @param id 规则ID
     * @param enabled 是否生效
     * @return 更新后的规则
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public AgentRuleDTO updateEnabledStatus(String id, Boolean enabled) {
        AgentRule entity = requireById(id);
        entity.setEnabled(Boolean.TRUE.equals(enabled));
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(SecurityUtils.getCurrentUserId());
        agentRuleMapper.updateById(entity);
        log.info("更新智能体规则生效状态: id={}, enabled={}", id, enabled);
        return AgentRuleConverter.toDTO(entity);
    }

    /**
     * 软删除规则
     *
     * @param id 规则ID
     * @return 是否删除成功
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean removeById(String id) {
        AgentRule entity = requireById(id);
        entity.setDelFlag(1);
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(SecurityUtils.getCurrentUserId());
        int updated = agentRuleMapper.updateById(entity);
        log.info("删除智能体规则: id={}, title={}", id, entity.getTitle());
        return updated > 0;
    }

    /**
     * 批量软删除规则
     *
     * @param ids 规则ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void removeByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (String id : ids) {
            removeById(id);
        }
    }

    /**
     * 分页查询规则
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResultVO<AgentRuleDTO> listPage(AgentRuleQueryDTO queryDTO) {
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        Long total = agentRuleMapper.selectCount(queryDTO);
        List<AgentRuleDTO> records = agentRuleMapper.selectPage(queryDTO, offset, queryDTO.getPageSize())
                .stream()
                .map(AgentRuleConverter::toDTO)
                .collect(Collectors.toList());
        return PageResultVO.success(records, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    /**
     * 拼接已启用规则为对话注入提示词；空正文跳过，格式为 ## 标题 + 正文
     *
     * @return 提示词 VO（content 可能为空串）
     */
    @Override
    public AgentRulePromptVO buildPrompt() {
        List<AgentRule> rules = agentRuleMapper.selectList(
                AgentRuleQueryDTO.builder().enabled(Boolean.TRUE).build());
        StringBuilder sb = new StringBuilder();
        for (AgentRule rule : rules) {
            if (rule.getContent() == null || !StringUtils.hasText(rule.getContent().trim())) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append("\n\n");
            }
            sb.append("## ").append(rule.getTitle()).append('\n').append(rule.getContent().trim());
        }
        return AgentRulePromptVO.builder().content(sb.toString()).build();
    }

    private AgentRule requireById(String id) {
        AgentRule entity = agentRuleMapper.selectById(id);
        if (entity == null) {
            throw new ReportBizException("error.agentRule.notFound");
        }
        return entity;
    }

    private void clean(AgentRuleDTO dto) {
        if (dto.getTitle() != null) {
            dto.setTitle(dto.getTitle().trim());
        }
        if (dto.getContent() != null) {
            dto.setContent(dto.getContent().trim());
        }
    }
}
