package com.luck.report.web.modules.trace.domain.vo;

import lombok.Data;

/**
 * Agent 链路日志上报响应体；字段与前端 TraceReportResult 一一对应
 *
 * @author luck
 */
@Data
public class AgentTraceReportResponse {

    /**
     * 总开关已关闭，前端停止上报并清理缓存
     */
    private boolean disabled;

    /**
     * 实际落库条数
     */
    private int accepted;

    /**
     * 因超长等规则被丢弃的条数
     */
    private int dropped;
}
