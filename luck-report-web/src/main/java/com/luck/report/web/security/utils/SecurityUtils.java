package com.luck.report.web.security.utils;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.utils.HttpUtils;
import com.luck.report.web.security.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 安全上下文工具类，统一获取当前登录用户身份。
 *
 * @author luck-report
 * @since 1.2.0
 */
@Component("bean.securityUtils")
public class SecurityUtils {

    /**
     * 由 Spring setter 注入的实际 TokenService，运行期供静态方法使用
     */
    private static TokenService tokenService;

    @Autowired
    public void setTokenService(TokenService tokenService) {
        SecurityUtils.tokenService = tokenService;
    }

    /**
     * 获取当前登录用户ID。
     *
     * @return 用户ID字符串；非 HTTP 上下文或未登录返回 null
     */
    public static String getCurrentUserId() {
        ApiRequest request = HttpUtils.getRequest();
        return request != null ? tokenService.getCurrentUserId(request) : null;
    }
}
