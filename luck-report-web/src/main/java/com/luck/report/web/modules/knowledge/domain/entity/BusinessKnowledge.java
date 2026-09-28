package com.luck.report.web.modules.knowledge.domain.entity;

import com.luck.report.web.common.domain.entity.DataEntity;
import com.luck.report.web.modules.knowledge.domain.enums.EmbeddingStatus;
import com.luck.report.web.modules.knowledge.domain.enums.KnowledgeType;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 业务知识实体
 *
 * @author luck
 */
@Data
@NoArgsConstructor
public class BusinessKnowledge extends DataEntity<BusinessKnowledge> {

    private String title;

    private KnowledgeType type;

    private String question;

    private String content;

    private Integer enabled = 1;

    private EmbeddingStatus embeddingStatus;

    private String errorMsg;

    private String sourceFilename;

    private String reportPath;

    private Long fileSize;

    private String fileType;

    private String splitterType = "recursive";

    private String modelId;

    private Integer isResourceCleaned = 0;
}
