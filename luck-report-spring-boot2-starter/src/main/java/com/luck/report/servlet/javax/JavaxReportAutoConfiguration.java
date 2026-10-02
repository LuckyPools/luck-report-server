package com.luck.report.servlet.javax;

import com.luck.report.web.config.DataSourceConfiguration;
import com.luck.report.web.config.TransactionConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * 报表系统自动配置（Spring Boot 2）
 *
 * @author luck
 */
@Configuration
@ComponentScan(
        basePackages = {
                "com.luck.report.web",
                "com.luck.report.core.config",
                "com.luck.report.font.config",
                "com.luck.report.jdbc.config",
                "com.luck.report.infra",
                "com.luck.report.postgresql",
                "com.luck.report.redis",
                "com.luck.report.chroma",
                "com.luck.report.milvus"
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {DataSourceConfiguration.class, TransactionConfiguration.class}))
@ConditionalOnProperty(prefix = "luck-report", name = "autoConfig", havingValue = "true", matchIfMissing = true)
public class JavaxReportAutoConfiguration {

}
