package com.luck.report.servlet.jakarta;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.utils.HttpUtils;
import com.luck.report.web.config.TokenProperties;
import com.luck.report.web.interceptor.PreviewInterceptorHandler;
import com.luck.report.web.security.service.impl.ReportAccessChecker;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JakartaPreviewInterceptor extends PreviewInterceptorHandler implements HandlerInterceptor {

    public JakartaPreviewInterceptor(ReportAccessChecker accessChecker,
                                     TokenProperties tokenProperties) {
        super(accessChecker, tokenProperties);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ApiRequest apiReq = HttpUtils.wrapRequest(request);
        return super.preHandle(apiReq);
    }
}
