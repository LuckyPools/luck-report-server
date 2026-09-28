package com.luck.report.servlet.javax;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.utils.HttpUtils;
import com.luck.report.web.config.TokenProperties;
import com.luck.report.web.interceptor.ManageInterceptorHandler;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JavaxManageInterceptor extends ManageInterceptorHandler implements HandlerInterceptor {

    public JavaxManageInterceptor(com.luck.report.web.security.service.TokenService tokenService,
                                  TokenProperties tokenProperties) {
        super(tokenService, tokenProperties);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ApiRequest apiReq = HttpUtils.wrapRequest(request);
        return super.preHandle(apiReq);
    }
}
