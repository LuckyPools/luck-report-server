package com.luck.report.web.modules.trace.service;

import com.luck.report.web.modules.trace.domain.vo.AgentTraceReportRequest;
import com.luck.report.web.modules.trace.domain.vo.AgentTraceReportResult;

/**
 * Agent 链路日志服务
 *
 * @author luck
 */
public interface AgentTraceService {

    /**
     * 批量落盘前端上报的链路日志
     *
     * @param request 上报请求，AgentTraceReportRequest，可为空（空列表直接忽略）
     * @return 处理结果，AgentTraceReportResult，不可为空
     */
    AgentTraceReportResult reportLogs(AgentTraceReportRequest request);
}
