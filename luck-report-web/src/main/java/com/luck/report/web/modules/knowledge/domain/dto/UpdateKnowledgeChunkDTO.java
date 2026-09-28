package com.luck.report.web.modules.knowledge.domain.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新知识库片段
 */
@Data
public class UpdateKnowledgeChunkDTO {

    @NotBlank(message = "片段内容不能为空")
    private String content;
}
