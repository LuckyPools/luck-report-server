package com.luck.report.web.modules.knowledge.converter;

import com.luck.report.web.modules.knowledge.domain.dto.CreateAgentKnowledgeDTO;
import com.luck.report.web.modules.knowledge.domain.dto.UpdateAgentKnowledgeDTO;
import com.luck.report.web.modules.knowledge.domain.entity.AgentKnowledge;
import com.luck.report.web.modules.knowledge.domain.enums.EmbeddingStatus;
import com.luck.report.web.modules.knowledge.domain.enums.KnowledgeType;
import com.luck.report.web.modules.knowledge.domain.enums.SplitterType;
import com.luck.report.web.modules.knowledge.domain.vo.AgentKnowledgeVO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 智能体知识转换器
 * 用于Entity、DTO、VO之间的相互转换
 *
 * @author luck
 */
@Component("bean.agentKnowledgeConverter")
public class AgentKnowledgeConverter {

    /**
     * 将Entity转换为VO
     *
     * @param entity 智能体知识实体
     * @return 智能体知识VO
     */
    public AgentKnowledgeVO toVo(AgentKnowledge entity) {
        if (entity == null) {
            return null;
        }
        return AgentKnowledgeVO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .type(entity.getType() != null ? entity.getType().getValue() : null)
                .question(entity.getQuestion())
                .content(entity.getContent())
                .enabled(entity.getEnabled() != null && entity.getEnabled() == 1)
                .embeddingStatus(entity.getEmbeddingStatus() != null ? entity.getEmbeddingStatus().getValue() : null)
                .errorMsg(entity.getErrorMsg())
                .splitterType(entity.getSplitterType())
                .modelId(entity.getModelId())
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    /**
     * 将CreateDTO转换为Entity
     *
     * @param dto 创建智能体知识DTO
     * @return 智能体知识实体
     */
    public AgentKnowledge toEntityForCreate(CreateAgentKnowledgeDTO dto) {
        if (dto == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();

        // 设置分块策略：blank → recursive；非法值抛业务异常
        String splitterType = SplitterType.fromValue(dto.getSplitterType()).getValue();

        AgentKnowledge knowledge = new AgentKnowledge();
        knowledge.setTitle(dto.getTitle());
        knowledge.setType(KnowledgeType.fromValue(dto.getType()));
        knowledge.setQuestion(dto.getQuestion());
        knowledge.setContent(dto.getContent());
        knowledge.setEnabled(1);
        knowledge.setEmbeddingStatus(EmbeddingStatus.PENDING);
        knowledge.setSplitterType(splitterType);
        knowledge.setModelId(dto.getModelId());
        knowledge.setCreateTime(now);
        knowledge.setUpdateTime(now);

        // 文档类型时设置文件信息
        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            knowledge.setSourceFilename(dto.getFile().getOriginalFilename());
            knowledge.setFileSize(dto.getFile().getSize());
            knowledge.setFileType(dto.getFile().getContentType());
        }

        return knowledge;
    }

    /**
     * 将UpdateDTO应用到Entity
     *
     * @param entity 智能体知识实体
     * @param dto 更新智能体知识DTO
     * @return 更新后的智能体知识实体
     */
    public AgentKnowledge applyUpdateToEntity(AgentKnowledge entity, UpdateAgentKnowledgeDTO dto) {
        if (entity == null || dto == null) {
            return entity;
        }
        if (dto.getTitle() != null) {
            entity.setTitle(dto.getTitle());
        }
        if (dto.getQuestion() != null) {
            entity.setQuestion(dto.getQuestion());
        }
        if (dto.getContent() != null) {
            entity.setContent(dto.getContent());
        }
        if (dto.getModelId() != null) {
            entity.setModelId(dto.getModelId());
        }
        entity.setUpdateTime(LocalDateTime.now());
        return entity;
    }
}
