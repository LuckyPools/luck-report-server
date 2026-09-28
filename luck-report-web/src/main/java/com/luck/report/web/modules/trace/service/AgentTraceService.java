package com.luck.report.web.modules.trace.service;

import com.luck.report.web.modules.trace.domain.vo.AgentTraceReportRequest;
import com.luck.report.web.modules.trace.domain.vo.AgentTraceReportResult;

/**
 * Agent 链路日志服务
 * 接收前端上报的 agent 执行日志，校验后输出到链路日志通道（logger 名见 AgentTraceProperties），
 * 与后端请求日志通过 traceId 串联，支撑"一轮 agent 执行"的完整链路排查
 *
 * @author luck
 */
public interface AgentTraceService {

    /**
     * 批量落盘前端上报的链路日志
     * 关闭时返回 disabled 标记（前端据此停止上报），超限时返回限流标记（前端延后重试）
     *
     * @param request 上报请求，AgentTraceReportRequest，可为空（空列表直接忽略）
     * @return 处理结果，AgentTraceReportResult，不可为空
     */
    AgentTraceReportResult reportLogs(AgentTraceReportRequest request);
}
