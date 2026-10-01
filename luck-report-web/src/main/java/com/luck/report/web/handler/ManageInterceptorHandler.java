package com.luck.report.web.handler;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.web.exception.AuthException;
import com.luck.report.web.config.properties.TokenProperties;
import com.luck.report.web.security.service.TokenService;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ManageInterceptorHandler {
    private final TokenService tokenService;
    private final TokenProperties tokenProperties;

    public ManageInterceptorHandler(TokenService tokenService, TokenProperties tokenProperties) {
        this.tokenService = tokenService;
        this.tokenProperties = tokenProperties;
    }

    public boolean preHandle(ApiRequest apiReq) {
        // 1. 总开关关闭时跳过
        if (tokenProperties == null || !tokenProperties.isEnabled()) {
            return true;
        }

        // 2. 获取用户角色
        List<String> userRoles = tokenService.getCurrentUserRoles(apiReq);
        List<String> adminRoles = tokenProperties.getAdminRoles();

        // 3. 校验是否为管理员
        boolean isAdmin = userRoles != null && adminRoles != null && !adminRoles.isEmpty()
                && userRoles.stream().anyMatch(adminRoles::contains);

        if (!isAdmin) {
            log.warn("管理端权限拒绝: userRoles={}, adminRoles={}, uri={}",
                    userRoles, adminRoles, apiReq.getRequestURI());
            throw new AuthException("error.auth.notAdmin");
        }

        return true;
    }
}
