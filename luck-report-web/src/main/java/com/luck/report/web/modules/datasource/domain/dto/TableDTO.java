package com.luck.report.web.modules.datasource.domain.dto;

import com.luck.report.web.modules.datasource.domain.dto.ColumnDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 表信息DTO
 *
 * @author luck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TableDTO {

    /**
     * 表名
     */
    private String name;

    /**
     * 表注释/描述
     */
    private String description;

    /**
     * 列信息列表
     */
    @Builder.Default
    private List<ColumnDTO> column = new ArrayList<>();

    /**
     * 主键字段名列表
     */
    private List<String> primaryKeys;
}
