package com.luck.report.web.modules.dataset.domain.dto;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 公共数据集保存DTO
 * 创建和更新公共数据集共用，parameters/fields为JSON数组字符串
 *
 * @author luck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDatasetSaveDTO {

    /** 数据集名称（必填，全局唯一） */
    @NotBlank(message = "数据集名称不能为空")
    private String name;

    /** 类型：sql-SQL数据集 / json-JSON数据集（必填） */
    @NotBlank(message = "数据集类型不能为空")
    private String type;

    /** 绑定的公共数据源ID（sql类型必填，json类型忽略） */
    private String datasourceId;

    /** SQL语句（sql类型必填） */
    private String sqlContent;

    /** JSON数组内容（json类型必填） */
    private String jsonContent;

    /** SQL参数定义JSON数组字符串，可为空 */
    private String parameters;

    /** 字段列表JSON数组字符串，可为空 */
    private String fields;

    /** 描述（可选） */
    private String description;

    /** 是否启用，默认 true */
    private Boolean enabled;
}
