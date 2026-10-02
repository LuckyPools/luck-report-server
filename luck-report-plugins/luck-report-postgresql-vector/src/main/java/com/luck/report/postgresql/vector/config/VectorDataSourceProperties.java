package com.luck.report.postgresql.vector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * PostgreSQL 向量存储数据源配置
 *
 * @author luck
 */
@Data
@ConfigurationProperties(prefix = "luck-report.vector.datasource")
public class VectorDataSourceProperties {

    /**
     * JDBC 连接地址
     */
    private String url;

    /**
     * 数据库用户名
     */
    private String username;

    /**
     * 数据库密码
     */
    private String password;

    /**
     * JDBC 驱动类名
     */
    private String driverClassName;

    /**
     * 连接池最大连接数
     */
    private int maximumPoolSize = 10;
}
