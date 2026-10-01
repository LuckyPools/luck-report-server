package com.luck.report.postgresql.vector.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * PostgreSQL + vector 向量存储配置
 * 仅当 luck-report.vector.type=postgresql 时生效
 * 未配置向量类型时，由 EmptyVectorStoreConfiguration 提供兜底实现
 * DAO / Service 由 starter 扫描 com.luck.report 加载
 *
 * 包含：
 * 1. bean.vectorDataSource Bean：HikariCP 数据源，从 luck-report.vector.datasource.* 读取
 * 2. bean.vectorJdbcTemplate Bean：基于 bean.vectorDataSource 的 JdbcTemplate，DAO 专用
 *
 * 变更说明：
 * - plugin 自治：数据源配置 + Bean 创建全部在本模块内完成，web 模块不再持有 vector 配置
 * - 移除 MyBatis 依赖：PgSqlVectorDocumentDao 通过 @Qualifier("bean.vectorJdbcTemplate") 注入
 *
 * @author luck
 */
@Configuration
@ConditionalOnProperty(name = "luck-report.vector.type", havingValue = "postgresql")
@EnableConfigurationProperties(VectorDataSourceProperties.class)
public class PgVectorStoreConfiguration {

    /**
     * 创建 PostgreSQL 向量存储数据源（HikariCP）
     * 池名固定为 vector-pool，便于日志/监控识别
     *
     * @param props 数据源配置
     * @return HikariDataSource 实例
     */
    @Bean(name = "bean.vectorDataSource")
    @ConditionalOnMissingBean(name = "bean.vectorDataSource")
    @ConditionalOnProperty(name = "luck-report.vector.datasource.url")
    public DataSource vectorDataSource(VectorDataSourceProperties props) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(props.getUrl());
        config.setUsername(props.getUsername());
        config.setPassword(props.getPassword());
        config.setDriverClassName(props.getDriverClassName());
        config.setMaximumPoolSize(props.getMaximumPoolSize());
        config.setPoolName("vector-pool");
        return new HikariDataSource(config);
    }

    /**
     * 创建 vector 专用 JdbcTemplate
     * 直接绑定 bean.vectorDataSource，DAO 注入即可使用
     *
     * @param vectorDataSource 上一 Bean 产出的 HikariDataSource
     * @return JdbcTemplate 实例
     */
    @Bean(name = "bean.vectorJdbcTemplate")
    @ConditionalOnMissingBean(name = "bean.vectorJdbcTemplate")
    public JdbcTemplate vectorJdbcTemplate(@Qualifier("bean.vectorDataSource") DataSource vectorDataSource) {
        return new JdbcTemplate(vectorDataSource);
    }
}
