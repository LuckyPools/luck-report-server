package com.luck.report.web.modules.knowledge.domain.dto;

import lombok.Data;

/**
 * 更新业务知识DTO
 *
 * @author luck
 */
@Data
public class UpdateBusinessKnowledgeDTO {

    private String title;

    private String question;

    private String content;

    private String modelId;
}
