package com.luck.report.servlet.jakarta;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.utils.HttpUtils;
import com.luck.report.web.config.TokenProperties;
import com.luck.report.web.interceptor.TokenInterceptorHandler;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JakartaTokenInterceptor extends TokenInterceptorHandler implements HandlerInterceptor {

    public JakartaTokenInterceptor(com.luck.report.web.security.service.TokenService tokenService,
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
