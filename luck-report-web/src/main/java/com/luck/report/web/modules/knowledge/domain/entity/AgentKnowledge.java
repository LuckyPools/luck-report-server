package com.luck.report.web.modules.knowledge.domain.entity;

import com.luck.report.web.common.domain.entity.DataEntity;
import com.luck.report.web.modules.knowledge.domain.enums.EmbeddingStatus;
import com.luck.report.web.modules.knowledge.domain.enums.KnowledgeType;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能体知识实体类
 *
 * @author luck
 */
@Data
@NoArgsConstructor
public class AgentKnowledge extends DataEntity<AgentKnowledge> {

    /**
     * 知识标题
     */
    private String title;

    /**
     * 知识类型：DOCUMENT, QA, FAQ
     */
    private KnowledgeType type;

    /**
     * 问题（FAQ和QA类型时使用）
     */
    private String question;

    /**
     * 内容（当type=QA, FAQ时有内容）
     */
    private String content;

    /**
     * 是否生效：true-参与检索，false-不参与
     */
    private Boolean enabled = true;

    /**
     * 向量化状态：PENDING待处理，PROCESSING处理中，COMPLETED已完成，FAILED失败
     */
    private EmbeddingStatus embeddingStatus;

    /**
     * 操作失败的错误信息
     */
    private String errorMsg;

    /**
     * 原始文件名
     */
    private String sourceFilename;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String fileType;

    /**
     * 分块策略类型：token, recursive, sentence, paragraph, semantic（默认 recursive）
     */
    private String splitterType = "recursive";

    /**
     * 嵌入模型配置ID，用于指定向量化时使用的嵌入模型
     */
    private String modelId;
}
