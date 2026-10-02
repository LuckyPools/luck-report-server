package com.luck.report.web.modules.trace.domain.vo;

import lombok.Data;

/**
 * Agent 链路日志上报处理结果
 *
 * @author luck
 */
@Data
public class AgentTraceReportResult {

    /**
     * 总开关已关闭，前端应停止上报并清理缓存
     */
    private boolean disabled;

    /**
     * 触发限流，前端按失败次数控制重试与退避逻辑加长等待
     */
    private boolean rateLimited;

    /**
     * 实际落库条数
     */
    private int accepted;

    /**
     * 因超长等规则被丢弃的条数
     */
    private int dropped;
}
