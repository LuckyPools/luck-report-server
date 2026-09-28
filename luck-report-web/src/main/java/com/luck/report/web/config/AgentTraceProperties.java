package com.luck.report.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Agent 链路日志配置项
 * <p>前缀：{@code luck-report.agent-trace}
 * <p>上报接口由前端触发且通常不带 token，属于系统边界，开关/条数/频次三道闸门都在这里，
 * 不依赖客户端自律。
 *
 * @author luck
 */
@ConfigurationProperties(prefix = "luck-report.agent.trace")
public class AgentTraceProperties {

    /**
     * 总开关：false 时上报接口直接返回 disabled，前端收到后停止上报（不再重试）。
     * <p>生产环境临时止损用：链路日志异常暴涨时可不停机止血。
     */
    private boolean enabled = true;

    /**
     * 单批最大落盘条数，超出部分丢弃并记一行 warn。
     * <p>客户端侧也有预算（单轮 200 条），这里是服务端防线，不信任客户端。
     */
    private int maxBatchSize = 200;

    /**
     * 单 IP 每分钟上报请求数上限。
     * <p>0 或负数表示不限流（仅建议在后端已有网关限流时关闭）。
     */
    private int rateLimitPerMinute = 120;

    /**
     * 链路日志输出的 logger 名。
     * <p>starter 不提供日志配置：宿主的 logback 决定它落到哪。没有为该名字单独配置
     * appender 时，日志按 additivity 冒泡到 root——不丢，只是混在主日志里；
     * 想隔离到独立文件，宿主声明一个同名 logger 并挂自己的 appender 即可。
     */
    private String loggerName = "AGENT_TRACE";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    public int getRateLimitPerMinute() {
        return rateLimitPerMinute;
    }

    public void setRateLimitPerMinute(int rateLimitPerMinute) {
        this.rateLimitPerMinute = rateLimitPerMinute;
    }

    public String getLoggerName() {
        return loggerName;
    }

    public void setLoggerName(String loggerName) {
        this.loggerName = loggerName;
    }
}
