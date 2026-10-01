package com.luck.report.web.modules.dataset.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.luck.report.web.common.domain.entity.DataEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 公共数据集实体
 * 独立于报表存储、可被多个报表重复引用的数据集定义，支持sql和json两种类型
 *
 * @author luck
 */
@Data
@NoArgsConstructor
public class ReportDataset extends DataEntity<ReportDataset> {

    /** 数据集名称（全局唯一） */
    private String name;

    /** 类型：sql-SQL数据集 / json-JSON数据集 */
    private String type;

    /** 绑定的公共数据源ID（luck_datasource.id，sql类型必填，json类型为null） */
    private String datasourceId;

    /** SQL语句（sql类型使用） */
    private String sqlContent;

    /** JSON数组内容（json类型使用） */
    private String jsonContent;

    /** SQL参数定义JSON数组：[{"name":"deptId","type":"String","defaultValue":"1"}] */
    private String parameters;

    /** 字段列表JSON数组：[{"name":"username"},...]，可为空 */
    private String fields;

    /** 描述 */
    private String description;

    /** 是否启用 */
    private Boolean enabled;
}
