package com.luck.report.web.modules.datasource.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.luck.report.web.common.domain.entity.DataEntity;
import com.luck.report.web.modules.dataset.domain.entity.ReportDataset;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 逻辑外键配置实体
 *
 * @author luck
 */
@Data
@NoArgsConstructor
public class LogicalRelation extends DataEntity<LogicalRelation> {

    /**
     * 关联的数据源ID
     */
    private String datasourceId;

    /**
     * 主表名（例如 t_order）
     */
    private String sourceTableName;

    /**
     * 主表字段名（例如 buyer_uid）
     */
    private String sourceColumnName;

    /**
     * 关联表名（例如 t_user）
     */
    private String targetTableName;

    /**
     * 关联表字段名（例如 id）
     */
    private String targetColumnName;

    /**
     * 关系类型：1:1/1:N/N:1，辅助LLM理解数据基数
     */
    private String relationType;

    /**
     * 业务描述，存入Prompt中帮助LLM理解
     */
    private String description;
}
