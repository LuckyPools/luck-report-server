package com.luck.report.web.modules.datasource.domain.vo;

import com.luck.report.web.modules.datasource.domain.dto.SchemaDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 跨数据源Schema搜索结果项（返回给前端）
 *
 * @author luck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchemaSearchResultVO {

    /**
     * 数据源ID
     */
    private String datasourceId;

    /**
     * 数据源名称
     */
    private String datasourceName;

    /**
     * 数据源类型（如 mysql、postgresql 等）
     */
    private String datasourceType;

    /**
     * 命中的Schema结构（含表结构、字段、外键）
     */
    private SchemaDTO schema;
}
