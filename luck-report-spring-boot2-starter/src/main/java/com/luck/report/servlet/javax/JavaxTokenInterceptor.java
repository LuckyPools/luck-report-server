package com.luck.report.servlet.javax;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.utils.HttpUtils;
import com.luck.report.web.config.TokenProperties;
import com.luck.report.web.interceptor.TokenInterceptorHandler;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JavaxTokenInterceptor extends TokenInterceptorHandler implements HandlerInterceptor {

    public JavaxTokenInterceptor(com.luck.report.web.security.service.TokenService tokenService,
                                 TokenProperties props,
                                 com.luck.report.web.modules.role.mapper.ReportRoleMapper roleMapper) {
        super(tokenService, props, roleMapper);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ApiRequest apiReq = HttpUtils.wrapRequest(request);
        return super.preHandle(apiReq, handler);
    }
}
