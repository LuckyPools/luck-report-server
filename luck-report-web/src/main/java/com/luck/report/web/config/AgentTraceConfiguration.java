package com.luck.report.web.config;

import com.luck.report.web.config.properties.AgentTraceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Agent 链路日志自动装配
 * <p>与 {@link TokenConfiguration} 同模式，按"所有 bean 统一 bean.* 前缀"约定暴露 Bean 名。
 *
 * @author luck
 */
@Configuration
public class AgentTraceConfiguration {

    /**
     * 唯一注册 {@code bean.agentTraceProperties}，绑定 {@code luck-report.agent.trace.*}。
     *
     * @return 配置实例
     */
    @Bean(name = "bean.agentTraceProperties")
    @ConfigurationProperties(prefix = "luck-report.agent.trace")
    public AgentTraceProperties agentTraceProperties() {
        return new AgentTraceProperties();
    }
}
