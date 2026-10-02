package com.luck.report.web.modules.datasource.handler;

import com.luck.report.web.modules.datasource.domain.entity.ReportDatasource;
import org.springframework.util.StringUtils;

/**
 * 数据源类型处理器接口
 *
 * @author luck
 */
public interface DatasourceTypeHandler {

    /**
     * 获取处理器对应的数据源类型标识
     *
     * @return 类型标识名，如 "mysql"、"postgresql"
     */
    String typeName();

    /**
     * 判断是否支持指定类型
     *
     * @param type 数据源类型
     * @return 是否支持
     */
    default boolean supports(String type) {
        return typeName().equalsIgnoreCase(type);
    }

    /**
     * 检查必要连接字段是否存在
     *
     * @param datasource 数据源实体
     * @return 是否具备必要字段
     */
    default boolean hasRequiredConnectionFields(ReportDatasource datasource) {
        return datasource.getHost() != null && datasource.getPort() != null
                && datasource.getDatabaseName() != null;
    }

    /**
     * 构建JDBC连接URL
     *
     * @param datasource 数据源实体
     * @return JDBC连接URL
     */
    String buildConnectionUrl(ReportDatasource datasource);

    /**
     * 解析连接URL
     *
     * @param datasource 数据源实体
     * @return JDBC连接URL
     */
    default String resolveConnectionUrl(ReportDatasource datasource) {
        String existing = datasource.getConnectionUrl();
        if (StringUtils.hasText(existing)) {
            return existing;
        }
        return buildConnectionUrl(datasource);
    }

    /**
     * 提取Schema名称
     *
     * @param datasource 数据源实体
     * @return Schema名称
     */
    default String extractSchemaName(ReportDatasource datasource) {
        return datasource.getDatabaseName();
    }
}
