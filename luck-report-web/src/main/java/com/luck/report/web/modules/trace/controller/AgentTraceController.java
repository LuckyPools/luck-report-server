package com.luck.report.web.modules.trace.controller;

import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.web.modules.trace.domain.vo.AgentTraceReportRequest;
import com.luck.report.web.modules.trace.domain.vo.AgentTraceReportResponse;
import com.luck.report.web.modules.trace.domain.vo.AgentTraceReportResult;
import com.luck.report.web.modules.trace.service.AgentTraceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Agent 链路日志控制器
 *
 * @author luck
 */
@RestController("bean.agentTraceController")
@RequestMapping("${luck-report.servletPrefix:}/agent/trace")
@RequiredArgsConstructor
public class AgentTraceController {

    /**
     * 限流响应码，前端识别后走退避逻辑而非停止上报
     */
    private static final int CODE_RATE_LIMITED = 429;

    @Qualifier("bean.agentTraceService")
    private final AgentTraceService agentTraceService;

    /**
     * 批量上报前端链路日志
     *
     * @param request 上报请求体，AgentTraceReportRequest，可为空
     * @return ResultVO&lt;AgentTraceReportResponse&gt;，data 携带落盘结果或关闭标记
     */
    @PostMapping("/submit")
    public ResultVO<AgentTraceReportResponse> report(@RequestBody(required = false) AgentTraceReportRequest request) {
        AgentTraceReportResult result = agentTraceService.reportLogs(request);

        if (result.isDisabled()) {
            AgentTraceReportResponse resp = new AgentTraceReportResponse();
            resp.setDisabled(true);
            return ResultVO.success(resp);
        }
        if (result.isRateLimited()) {
            return ResultVO.error(CODE_RATE_LIMITED, "Agent trace log reporting is too frequent; rate limited");
        }
        AgentTraceReportResponse resp = new AgentTraceReportResponse();
        resp.setAccepted(result.getAccepted());
        resp.setDropped(result.getDropped());
        return ResultVO.success(resp);
    }
}
