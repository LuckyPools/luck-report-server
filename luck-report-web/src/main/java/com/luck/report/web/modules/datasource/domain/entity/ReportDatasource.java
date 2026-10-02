package com.luck.report.web.modules.datasource.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.luck.report.web.common.domain.entity.DataEntity;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 数据源配置实体
 *
 * @author luck
 */
@Data
@NoArgsConstructor
public class ReportDatasource extends DataEntity<ReportDatasource> {

    /**
     * 数据源名称
     */
    private String name;

    /**
     * 数据源类型：mysql/postgresql/oracle/dameng/sqlserver/hive
     */
    private String type;

    /**
     * 主机地址
     */
    private String host;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * 数据库名
     */
    private String databaseName;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码（序列化时隐藏）
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * 完整JDBC连接URL
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String connectionUrl;

    /**
     * 是否启用
     */
    private Boolean enabled;

    /**
     * 连接测试状态：success/failed/unknown
     */
    private String testStatus;

    /**
     * 描述
     */
    private String description;

    /**
     * 已初始化的表名列表（JSON格式存储）
     */
    private String initializedTables;
}
