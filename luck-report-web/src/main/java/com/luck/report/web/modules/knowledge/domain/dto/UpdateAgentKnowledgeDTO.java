package com.luck.report.web.modules.knowledge.domain.dto;

import lombok.Data;

/**
 * 更新智能体知识DTO
 *
 * @author luck
 */
@Data
public class UpdateAgentKnowledgeDTO {

    /**
     * 知识标题
     */
    private String title;

    /**
     * 问题（当type=QA, FAQ时可更新）
     */
    private String question;

    /**
     * 内容（当type=QA, FAQ时可更新）
     */
    private String content;

    /**
     * 嵌入模型配置ID，用于指定向量化时使用的嵌入模型
     */
    private String modelId;
}
