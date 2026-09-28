package com.luck.report.web.modules.trace.domain.vo;

import lombok.Data;

/**
 * Agent 链路日志上报响应体；字段与前端 TraceReportResult 一一对应
 *
 * @author luck
 */
@Data
public class AgentTraceReportResponse {

    /** 功能已关闭：前端停止上报，不再重试 */
    private boolean disabled;

    /** 实际落盘条数 */
    private int accepted;

    /** 超出单批上限被丢弃的条数 */
    private int dropped;
}
