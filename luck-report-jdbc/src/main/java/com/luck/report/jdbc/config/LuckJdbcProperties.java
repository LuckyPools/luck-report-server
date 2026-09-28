package com.luck.report.jdbc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * luck-report JDBC 配置项
 */
@ConfigurationProperties(prefix = "luck-report.jdbc")
public class LuckJdbcProperties {

    /**
     * SQL 资源 pattern，{dbType} 会被替换为 mysql/oracle/...
     */
    private String sqlLocations = "classpath*:luck-report/sql/{dbType}/**/*.xml";

    /**
     * 元数据主数据源 Bean 名；非空时优先于约定 Bean 名
     */
    private String primaryDatasource;

    /**
     * 获取 SQL 资源 pattern
     *
     * @return pattern
     */
    public String getSqlLocations() {
        return sqlLocations;
    }

    /**
     * 设置 SQL 资源 pattern
     *
     * @param sqlLocations pattern，可含 {dbType}
     */
    public void setSqlLocations(String sqlLocations) {
        this.sqlLocations = sqlLocations;
    }

    /**
     * 获取配置的主数据源 Bean 名
     *
     * @return Bean 名；未配置时为 null
     */
    public String getPrimaryDatasource() {
        return primaryDatasource;
    }

    /**
     * 设置主数据源 Bean 名
     *
     * @param primaryDatasource Bean 名
     */
    public void setPrimaryDatasource(String primaryDatasource) {
        this.primaryDatasource = primaryDatasource;
    }
}
