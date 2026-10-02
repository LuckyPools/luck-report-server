package com.luck.report.web.handler;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.web.exception.AuthException;
import com.luck.report.web.modules.report.constant.ReportUrls;
import com.luck.report.web.security.service.impl.ReportAccessChecker;
import com.luck.report.web.config.properties.TokenProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PreviewInterceptorHandler {
    private final ReportAccessChecker accessChecker;
    private final TokenProperties tokenProperties;

    public PreviewInterceptorHandler(ReportAccessChecker accessChecker, TokenProperties tokenProperties) {
        this.accessChecker = accessChecker;
        this.tokenProperties = tokenProperties;
    }

    public boolean preHandle(ApiRequest apiReq) {
        if (tokenProperties == null || !tokenProperties.isEnabled()) {
            return true;
        }

        String reportPath = extractFilePath(apiReq);
        if (reportPath == null || reportPath.isEmpty()) {
            return true;
        }

        Object attr = apiReq.getAttribute(ReportUrls.ATTR_ANONYMOUS_REPORT);
        if (Boolean.TRUE.equals(attr)) {
            return true;
        }

        if (!accessChecker.canPreview(apiReq, reportPath)) {
            log.warn("报表预览/导出权限拒绝: reportPath={}, uri={}", reportPath, apiReq.getRequestURI());
            throw new AuthException("error.auth.noPermission", reportPath);
        }

        return true;
    }

    /**
     * 从请求中提取 reportPath 参数。
     *
     * @param apiReq HTTP 请求
     * @return 带 provider 前缀的 reportPath；不存在返回 null
     */
    private String extractFilePath(ApiRequest apiReq) {
        String reportPath = apiReq.getParameter("reportPath");
        if (reportPath != null && !reportPath.isEmpty()) {
            return reportPath;
        }
        reportPath = apiReq.getParameter("file");
        if (reportPath != null && !reportPath.isEmpty()) {
            return reportPath;
        }
        return null;
    }
}
