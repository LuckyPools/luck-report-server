package com.luck.report.web.modules.datasource.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 外键关系
 *
 * @author luck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ForeignKeyDTO {

    /**
     * 源表名（如 t_order）
     */
    private String sourceTable;

    /**
     * 源字段名（如 buyer_uid）
     */
    private String sourceColumn;

    /**
     * 目标表名（如 t_user）
     */
    private String targetTable;

    /**
     * 目标字段名（如 id）
     */
    private String targetColumn;
}
