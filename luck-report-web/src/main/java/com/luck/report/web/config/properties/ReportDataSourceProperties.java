package com.luck.report.web.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 报表主数据源配置属性
 * 从 spring.datasource.* 读取核心连接信息，替代 Boot 内部的 DataSourceProperties，
 * 避免跨 Boot 版本（2/3/4）因 autoconfigure 包路径迁移导致 ClassNotFoundException。
 *
 * @author luck
 */
@Data
@ConfigurationProperties(prefix = "spring.datasource")
public class ReportDataSourceProperties {

    /** 数据库连接 URL */
    private String url;

    /** 数据库用户名 */
    private String username;

    /** 数据库密码 */
    private String password;

    /** JDBC 驱动类名（可省略，HikariCP 会根据 URL 自动推断） */
    private String driverClassName;
}
