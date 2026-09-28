package com.luck.report.web.modules.knowledge.converter;

import com.luck.report.web.modules.knowledge.domain.dto.CreateBusinessKnowledgeDTO;
import com.luck.report.web.modules.knowledge.domain.dto.UpdateBusinessKnowledgeDTO;
import com.luck.report.web.modules.knowledge.domain.entity.BusinessKnowledge;
import com.luck.report.web.modules.knowledge.domain.enums.EmbeddingStatus;
import com.luck.report.web.modules.knowledge.domain.enums.KnowledgeType;
import com.luck.report.web.modules.knowledge.domain.enums.SplitterType;
import com.luck.report.web.modules.knowledge.domain.vo.BusinessKnowledgeVO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 业务知识转换器
 *
 * @author luck
 */
@Component("bean.businessKnowledgeConverter")
public class BusinessKnowledgeConverter {

    public BusinessKnowledgeVO toVo(BusinessKnowledge entity) {
        if (entity == null) {
            return null;
        }
        return BusinessKnowledgeVO.builder()
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

    public BusinessKnowledge toEntityForCreate(CreateBusinessKnowledgeDTO dto) {
        if (dto == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        String splitterType = SplitterType.fromValue(dto.getSplitterType()).getValue();

        BusinessKnowledge knowledge = new BusinessKnowledge();
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

        if (dto.getFile() != null && !dto.getFile().isEmpty()) {
            knowledge.setSourceFilename(dto.getFile().getOriginalFilename());
            knowledge.setFileSize(dto.getFile().getSize());
            knowledge.setFileType(dto.getFile().getContentType());
        }

        return knowledge;
    }

    public BusinessKnowledge applyUpdateToEntity(BusinessKnowledge entity, UpdateBusinessKnowledgeDTO dto) {
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
