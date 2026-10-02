package com.luck.report.web.modules.datasource.domain.dto;

import com.luck.report.web.modules.datasource.domain.dto.ForeignKeyDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Schema数据载体
 *
 * @author luck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SchemaDTO {

    /**
     * 数据库名
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 表数量
     */
    private Integer tableCount;

    /**
     * 表列表
     */
    private List<TableDTO> table;

    /**
     * 外键关系列表（物理外键 + 逻辑外键合并）
     */
    private List<ForeignKeyDTO> foreignKeys;
}
