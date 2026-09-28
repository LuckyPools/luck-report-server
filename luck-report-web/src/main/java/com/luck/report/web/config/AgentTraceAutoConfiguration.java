package com.luck.report.web.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Agent 链路日志自动装配
 * <p>与 {@link TokenAutoConfiguration} 同模式，按"所有 bean 统一 bean.* 前缀"约定暴露 Bean 名。
 *
 * @author luck
 */
@Configuration
@EnableConfigurationProperties(AgentTraceProperties.class)
public class AgentTraceAutoConfiguration {

    /**
     * 按 bean.* 命名约定暴露配置实例
     *
     * @param props 配置属性，AgentTraceProperties，不可为空
     * @return 同一个配置实例，AgentTraceProperties
     */
    @Bean(name = "bean.agentTraceProperties")
    @Primary
    public AgentTraceProperties agentTracePropertiesBean(AgentTraceProperties props) {
        return props;
    }
}
