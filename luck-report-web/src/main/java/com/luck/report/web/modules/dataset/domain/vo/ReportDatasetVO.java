package com.luck.report.web.modules.dataset.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 公共数据集视图对象
 *
 * @author luck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportDatasetVO {

    /**
     * 主键ID
     */
    private String id;

    /**
     * 数据集名称
     */
    private String name;

    /**
     * 类型：sql-SQL数据集 / json-JSON数据集
     */
    private String type;

    /**
     * 绑定的公共数据源ID（sql类型非空）
     */
    private String datasourceId;

    /**
     * 所属数据源名称（联查回填，json类型为null）
     */
    private String datasourceName;

    /**
     * 所属数据源是否启用（联查回填，用于前端拦截禁用数据源的数据集）
     */
    private Boolean datasourceEnabled;

    /**
     * SQL语句（sql类型非空）
     */
    private String sqlContent;

    /**
     * JSON数组内容（json类型非空）
     */
    private String jsonContent;

    /**
     * SQL参数定义JSON数组字符串
     */
    private String parameters;

    /**
     * 字段列表JSON数组字符串
     */
    private String fields;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}
