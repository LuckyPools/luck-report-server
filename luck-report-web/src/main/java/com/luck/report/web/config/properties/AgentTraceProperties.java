package com.luck.report.web.config.properties;

/**
 * Agent 链路日志配置项
 *
 * @author luck
 */
public class AgentTraceProperties {

    /**
     * 总开关：false 时上报接口直接返回 disabled，前端收到后停止上报（不再重试）。
     */
    private boolean enabled = true;

    /**
     * 单批最大落盘条数，超出部分丢弃并记一行 warn。
     */
    private int maxBatchSize = 200;

    /**
     * 单 IP 每分钟上报请求数上限。
     */
    private int rateLimitPerMinute = 120;

    /**
     * 链路日志输出的 logger 名。
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
