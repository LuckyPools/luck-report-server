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
 *
 * @author luck
 */
@Configuration
@ConditionalOnProperty(name = "luck-report.vector.type", havingValue = "postgresql")
@EnableConfigurationProperties(VectorDataSourceProperties.class)
public class PgVectorStoreConfiguration {

    /**
     * 创建 PostgreSQL 向量存储数据源
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
