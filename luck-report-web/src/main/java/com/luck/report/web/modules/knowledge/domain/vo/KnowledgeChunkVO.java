package com.luck.report.web.modules.knowledge.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 知识库向量片段 VO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KnowledgeChunkVO {

    private String vectorId;

    private String content;

    private Integer chunkIndex;

    private Integer chunkTotal;

    private String modelId;

    private String splitterType;
}
