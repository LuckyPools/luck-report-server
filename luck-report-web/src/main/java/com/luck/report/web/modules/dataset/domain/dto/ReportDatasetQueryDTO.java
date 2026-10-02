package com.luck.report.web.modules.dataset.domain.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公共数据集分页查询DTO
 *
 * @author luck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDatasetQueryDTO {

    /**
     * 数据集名称（模糊查询）
     */
    private String name;

    /**
     * 类型：sql/json（可选）
     */
    private String type;

    /**
     * 绑定的公共数据源ID（可选）
     */
    private String datasourceId;

    /**
     * 是否启用（可选）
     */
    private Boolean enabled;

    /**
     * 当前页码（默认第1页）
     */
    @NotNull(message = "pageNum不能为空")
    @Min(value = 1, message = "pageNum不能小于1")
    @Builder.Default
    private Integer pageNum = 1;

    /**
     * 每页大小（默认10条）
     */
    @NotNull(message = "pageSize不能为空")
    @Min(value = 1, message = "pageSize不能小于1")
    @Builder.Default
    private Integer pageSize = 10;
}
