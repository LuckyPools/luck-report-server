package com.luck.report.web.config;

import com.luck.report.web.config.properties.ReportDataSourceProperties;
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
 * 主数据源配置（MySQL / Oracle / PgSQL 等关系型主库）
 * @author luck
 */
@Slf4j
@Configuration
@AutoConfigureAfter(name = "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration")
@EnableConfigurationProperties(ReportDataSourceProperties.class)
public class DataSourceConfiguration {

    @Bean(name = "bean.mainDbDataSource")
    @Primary
    @ConditionalOnMissingBean(name = "dataSource")
    @ConditionalOnProperty(name = "spring.datasource.url")
    @ConfigurationProperties(prefix = "spring.datasource.hikari")
    public DataSource mainDbDataSource(ReportDataSourceProperties props) {
        log.info("[DataSourceConfiguration] mainDbDataSource Bean 开始创建, url={}", props.getUrl());
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
