package com.luck.report.web.modules.knowledge.domain.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 业务知识分页查询DTO
 *
 * @author luck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessKnowledgeQueryDTO {

    private String title;

    private String type;

    private String embeddingStatus;

    @NotNull(message = "pageNum不能为空")
    @Min(value = 1, message = "pageNum不能小于1")
    @Builder.Default
    private Integer pageNum = 1;

    @NotNull(message = "pageSize不能为空")
    @Min(value = 1, message = "pageSize不能小于1")
    @Builder.Default
    private Integer pageSize = 10;
}
