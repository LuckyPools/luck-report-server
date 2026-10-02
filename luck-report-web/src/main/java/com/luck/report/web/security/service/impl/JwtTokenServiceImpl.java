package com.luck.report.web.security.service.impl;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.web.modules.role.domain.dto.RoleInfo;
import com.luck.report.web.security.domain.bo.LoginUser;
import com.luck.report.web.security.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 框架默认的 TokenService 空实现（占位符）。
 *
 * @author luck-report
 * @since 1.0.0
 */
@Component("bean.tokenService")
public class JwtTokenServiceImpl implements TokenService {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenServiceImpl.class);

    public JwtTokenServiceImpl() {
        log.warn("[LuckReport-Token] 使用默认空实现 JwtTokenServiceImpl。" +
                "请提供自定义 TokenService 实现（标记 @Primary）以替换此占位实现。");
    }

    /**
     * 签发 token（空实现，返回 null）。
     *
     * @param request HTTP 请求
     * @return null（占位实现）
     */
    @Override
    public String generateToken(ApiRequest request) {
        log.warn("[LuckReport-Token] generateToken 空实现被调用，请提供自定义 TokenService 实现");
        return null;
    }

    /**
     * 校验 token 是否有效（空实现，返回 false）。
     *
     * @param token 待校验 token
     * @return false（占位实现）
     */
    @Override
    public boolean verifyToken(String token) {
        log.warn("[LuckReport-Token] verifyToken 空实现被调用，请提供自定义 TokenService 实现");
        return false;
    }

    /**
     * 获取当前请求的登录用户信息（占位实现，返回固定 ID "1"）。
     *
     * @param request HTTP 请求
     * @return new LoginUser("1", Collections.emptyList())（占位实现）
     */
    @Override
    public LoginUser getCurrentUser(ApiRequest request) {
        return new LoginUser("1", Collections.emptyList());
    }

    /**
     * 获取第三方系统所有角色（空实现，返回空列表）。
     *
     * @return 空列表（占位实现）
     */
    @Override
    public List<RoleInfo> listAllRoles() {
        log.warn("[LuckReport-Token] listAllRoles 空实现被调用，请提供自定义 TokenService 实现");
        return Collections.emptyList();
    }
}
