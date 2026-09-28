package com.luck.report.web.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

/**
 * 主数据源配置（MySQL / Oracle / DM 等关系型主库）
 *
 * 作为自动配置类，在 Spring Boot DataSourceAutoConfiguration 之后执行：
 *   - 宿主项目已有名为 "dataSource" 的 Bean（Boot 自动配置或手动声明）→ @ConditionalOnMissingBean(name="dataSource") 跳过
 *   - 宿主项目无 "dataSource" Bean → 从 spring.datasource.* 读取连接信息，构建 HikariCP DataSource
 *   - 标记 @Primary 确保主数据源优先于向量库等辅助数据源被按类型注入
 *
 * 使用自定义 ReportDataSourceProperties 替代 Boot 内部的 DataSourceProperties，
 * 避免跨 Boot 版本（2/3/4）因 autoconfigure 包路径迁移导致 ClassNotFoundException。
 * HikariCP 精细配置（连接池大小、超时等）通过 @ConfigurationProperties(prefix = "spring.datasource.hikari")
 * 自动绑定到 HikariDataSource Bean 上。
 *
 * 多数据源 starter 接入（如 dynamic-datasource-spring-boot-starter）：
 *   - 用户改用 spring.datasource.dynamic.* 格式后，spring.datasource.url 不再设置
 *   - mainDbDataSource 不创建 → 整个 TransactionConfig 不加载（外层 @ConditionalOnBean 兜底）
 *   - 3rd 方 starter 的 DataSource Bean（通常名为 "dataSource"）成为唯一 DataSource
 *   - 3rd 方 starter 自带事务管理器 / TransactionTemplate
 *   - luck-report-jdbc 自动走该 DataSource（LuckJdbcAutoConfiguration 动态解析）
 *
 * @author luck
 */
@Slf4j
@Configuration
@AutoConfigureAfter(name = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
@EnableConfigurationProperties(ReportDataSourceProperties.class)
public class DataSourceConfig {

    @Bean(name = "mainDbDataSource")
    @Primary
    @ConditionalOnMissingBean(name = "dataSource")
    @ConditionalOnProperty(name = "spring.datasource.url")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource mainDbDataSource(ReportDataSourceProperties props) {
        log.info("[DataSourceConfig] mainDbDataSource Bean 开始创建, url={}", props.getUrl());
        HikariDataSource ds = new HikariDataSource();
        ds.setJdbcUrl(props.getUrl());
        ds.setUsername(props.getUsername());
        ds.setPassword(props.getPassword());
        if (StringUtils.hasText(props.getDriverClassName())) {
            ds.setDriverClassName(props.getDriverClassName());
        }
        return ds;
    }
}
