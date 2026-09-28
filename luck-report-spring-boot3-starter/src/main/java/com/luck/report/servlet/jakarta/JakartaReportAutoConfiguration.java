package com.luck.report.servlet.jakarta;

import com.luck.report.web.config.DataSourceConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * 报表系统自动配置类（Spring Boot 3）
 * 业务系统集成 luck-report-spring-boot3-starter 时自动加载报表相关配置
 *
 * DataSourceConfig 已从组件扫描中排除，单独注册为自动配置类，
 * 确保在 Spring Boot DataSourceAutoConfiguration 之后执行，
 * 优先使用宿主项目已有的 DataSource。
 *
 * @author luck
 */
@Configuration
@ComponentScan(
        basePackages = {"com.luck.report"},
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = DataSourceConfig.class))
@ConditionalOnProperty(prefix = "luck-report", name = "autoConfig", havingValue = "true", matchIfMissing = true)
public class JakartaReportAutoConfiguration {

}
