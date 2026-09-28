package com.luck.report.web.interceptor;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.web.exception.AuthException;
import com.luck.report.web.modules.report.constant.ReportUrls;
import com.luck.report.web.security.service.impl.ReportAccessChecker;
import com.luck.report.web.config.TokenProperties;
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
        // 1. 总开关关闭时跳过
        if (tokenProperties == null || !tokenProperties.isEnabled()) {
            return true;
        }

        // 2. 从请求参数中获取 reportPath（带 provider 前缀）
        String reportPath = extractFilePath(apiReq);
        if (reportPath == null || reportPath.isEmpty()) {
            // 无 reportPath 参数的请求（如预览首页）直接放行
            return true;
        }

        // 3. 匿名报表放行（从 TokenInterceptorHandler 写入的 request attribute 读取，避免重复查库）
        Object attr = apiReq.getAttribute(ReportUrls.ATTR_ANONYMOUS_REPORT);
        if (Boolean.TRUE.equals(attr)) {
            return true;
        }

        // 4. 权限校验
        if (!accessChecker.canPreview(apiReq, reportPath)) {
            log.warn("报表预览/导出权限拒绝: reportPath={}, uri={}", reportPath, apiReq.getRequestURI());
            throw new AuthException("error.auth.noPermission", reportPath);
        }

        return true;
    }

    /**
     * 从请求中提取 reportPath 参数。
     * <p>预览 URL 格式：
     * <ul>
     *   <li>{@code /ureport/preview?_u=file:test.ureport.xml}</li>
     *   <li>{@code /ureport/preview?_u=db:1}</li>
     *   <li>{@code /ureport/preview?_u=classpath:xxx.ureport.xml}</li>
     * </ul>
     *
     * @param apiReq HTTP 请求
     * @return 带 provider 前缀的 reportPath；不存在返回 null
     */
    private String extractFilePath(ApiRequest apiReq) {
        String reportPath = apiReq.getParameter("reportPath");
        if (reportPath != null && !reportPath.isEmpty()) {
            return reportPath;
        }
        // 兼容其他可能的参数名
        reportPath = apiReq.getParameter("file");
        if (reportPath != null && !reportPath.isEmpty()) {
            return reportPath;
        }
        return null;
    }
}
