package com.luck.report.servlet.javax;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.utils.HttpUtils;
import com.luck.report.web.config.properties.TokenProperties;
import com.luck.report.web.handler.PreviewInterceptorHandler;
import com.luck.report.web.security.service.impl.ReportAccessChecker;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JavaxPreviewInterceptor extends PreviewInterceptorHandler implements HandlerInterceptor {

    public JavaxPreviewInterceptor(ReportAccessChecker accessChecker,
                                   TokenProperties tokenProperties) {
        super(accessChecker, tokenProperties);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ApiRequest apiReq = HttpUtils.wrapRequest(request);
        return super.preHandle(apiReq);
    }
}
