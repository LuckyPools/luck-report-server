package com.luck.report.web.modules.agentRule.converter;

import com.luck.report.web.modules.agentRule.domain.dto.AgentRuleDTO;
import com.luck.report.web.modules.agentRule.domain.entity.AgentRule;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 智能体规则实体与 DTO 转换
 *
 * @author luck
 */
public final class AgentRuleConverter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AgentRuleConverter() {
    }

    /**
     * 实体转 DTO
     *
     * @param entity 规则实体
     * @return 规则 DTO
     */
    public static AgentRuleDTO toDTO(AgentRule entity) {
        if (entity == null) {
            return null;
        }
        return AgentRuleDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .sort(entity.getSort())
                .enabled(entity.getEnabled())
                .createTime(formatTime(entity.getCreateTime()))
                .updateTime(formatTime(entity.getUpdateTime()))
                .build();
    }

    /**
     * 新建 DTO 转实体（含默认审计与生效状态）
     *
     * @param dto 规则 DTO
     * @return 规则实体
     */
    public static AgentRule toEntity(AgentRuleDTO dto) {
        Assert.notNull(dto, "AgentRuleDTO不能为空");
        AgentRule entity = new AgentRule();
        entity.setId(dto.getId());
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setSort(dto.getSort() != null ? dto.getSort() : 0);
        // 新建默认不生效，生效状态仅通过列表启停接口切换
        entity.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : false);
        entity.setDelFlag(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return entity;
    }

    /**
     * 将更新 DTO 合并到已有实体
     *
     * @param dto 规则 DTO
     * @param entity 已有实体
     */
    public static void applyUpdate(AgentRuleDTO dto, AgentRule entity) {
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        if (dto.getSort() != null) {
            entity.setSort(dto.getSort());
        }
        entity.setUpdateTime(LocalDateTime.now());
    }

    private static String formatTime(LocalDateTime time) {
        return time == null ? null : time.format(FORMATTER);
    }
}
