package com.luck.report.web.modules.trace.domain.vo;

import lombok.Data;

/**
 * Agent 链路日志上报处理结果
 * 用于让 Controller 区分"已落盘 / 功能关闭 / 被限流"，前端据此决定是否继续上报
 *
 * @author luck
 */
@Data
public class AgentTraceReportResult {

    /** 功能已关闭：前端应停止上报，不再重试 */
    private boolean disabled;

    /** 被限流：前端按失败处理，走已有冷却逻辑延后重试 */
    private boolean rateLimited;

    /** 实际落盘条数 */
    private int accepted;

    /** 因超出单批上限被丢弃的条数 */
    private int dropped;
}
