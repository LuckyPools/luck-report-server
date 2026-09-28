package com.luck.report.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
 * 主库事务配置
 * 条件：容器中存在 DataSource Bean（v2 兜底创建的 mainDbDataSource 或宿主项目的 DataSource）
 *   - DataSource 存在 → 建 mainDbTransactionManager + transactionTemplate
 *   - DataSource 不存在 → 整个类不加载
 *
 * @author luckyPools
 */
@Slf4j
@Configuration
@ConditionalOnBean(DataSource.class)
public class TransactionConfig {

    @Bean(name = "mainDbTransactionManager")
    @Primary
    @ConditionalOnMissingBean(PlatformTransactionManager.class)
    public DataSourceTransactionManager mainDbTransactionManager(DataSource dataSource) {
        log.info("[TransactionConfig] mainDbTransactionManager Bean 开始创建, dataSource={}", dataSource);
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "bean.transactionTemplate")
    @ConditionalOnMissingBean(TransactionTemplate.class)
    public TransactionTemplate transactionTemplate(PlatformTransactionManager transactionManager) {
        log.info("[TransactionConfig] transactionTemplate Bean 开始创建, transactionManager={}", transactionManager);
        return new TransactionTemplate(transactionManager);
    }
}
