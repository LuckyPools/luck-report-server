package com.luck.report.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;

/**
 * 主库事务配置（自动配置，勿组件扫描）
 * @author luckyPools
 */
@Slf4j
@Configuration
@ConditionalOnBean(DataSource.class)
@AutoConfigureAfter(name = {
        "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration",
        "org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration",
        "com.luck.report.web.config.DataSourceConfiguration"
})
public class TransactionConfiguration {

    @Bean(name = "bean.mainDbTransactionManager")
    @Primary
    @ConditionalOnMissingBean(PlatformTransactionManager.class)
    public DataSourceTransactionManager mainDbTransactionManager(DataSource dataSource) {
        log.info("[TransactionConfiguration] bean.mainDbTransactionManager Bean 开始创建, dataSource={}", dataSource);
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "bean.transactionTemplate")
    @ConditionalOnMissingBean(name = "bean.transactionTemplate")
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        log.info("[TransactionConfiguration] bean.transactionTemplate Bean 开始创建, transactionManager={}", transactionManager);
        return new TransactionTemplate(transactionManager);
    }
}
