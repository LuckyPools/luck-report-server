package com.luck.report.web.modules.trace.domain.vo;

import lombok.Data;

import java.util.List;

/**
 * 前端链路日志批量上报请求体
 *
 * @author luck
 */
@Data
public class AgentTraceReportRequest {

    /**
     * 本次上报的日志条目列表，可为空（空时后端直接忽略）
     */
    private List<AgentTraceLogVO> logs;

    /**
     * 前端应用版本号，便于定位"某个版本引入的问题"
     */
    private String appVersion;

    /**
     * 浏览器 UA，便于定位浏览器兼容性问题
     */
    private String userAgent;

    /**
     * 产生日志的页面地址，便于区分设计器/预览等不同入口
     */
    private String pageUrl;
}
