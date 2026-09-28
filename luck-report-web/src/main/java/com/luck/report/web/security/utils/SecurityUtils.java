package com.luck.report.web.security.utils;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.utils.HttpUtils;
import com.luck.report.web.security.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 安全上下文工具类，统一获取当前登录用户身份。
 * <p>通过静态方法暴露，避免各 Service 重复注入 {@link TokenService} 与重复实现 getCurrentUserId。
 * <p>实现策略：Spring 启动时通过 setter 注入将实际的 {@link TokenService} Bean
 * （第三方以 @Primary 覆盖的优先实现，否则为框架占位实现）赋给静态字段，
 * 运行期静态方法即可直接使用，无需每个业务类各自持有 tokenService。
 *
 * @author luck-report
 * @since 1.2.0
 */
@Component
public class SecurityUtils {

    /** 由 Spring setter 注入的实际 TokenService，运行期供静态方法使用 */
    private static TokenService tokenService;

    @Autowired
    public void setTokenService(TokenService tokenService) {
        SecurityUtils.tokenService = tokenService;
    }

    /**
     * 获取当前登录用户ID。
     * <p>从当前 HTTP 请求上下文解析用户身份，由注入的 {@link TokenService} 实现决定具体解析方式。
     *
     * @return 用户ID字符串；非 HTTP 上下文或未登录返回 null
     */
    public static String getCurrentUserId() {
        ApiRequest request = HttpUtils.getRequest();
        return request != null ? tokenService.getCurrentUserId(request) : null;
    }
}
