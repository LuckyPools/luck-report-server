package com.luck.report.web.security.service;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.web.modules.role.domain.dto.RoleInfo;
import com.luck.report.web.security.domain.bo.LoginUser;

import java.util.Collections;
import java.util.List;

/**
 * 报表访问 Token 服务 SPI。
 *
 * @author luck-report
 * @since 1.0.0
 */
public interface TokenService {

    /**
     * 签发 token。
     *
     * @param request HTTP 请求（包含用户身份、请求参数等）
     * @return token 字符串；生成失败返回 null
     */
    String generateToken(ApiRequest request);

    /**
     * 校验 token 是否有效。
     *
     * @param token 待校验 token
     * @return true 表示有效；false 表示无效或已过期
     */
    boolean verifyToken(String token);

    /**
     * 获取当前请求的登录用户信息。
     *
     * @param request HTTP 请求
     * @return LoginUser 对象（包含 id 和 roles）；未登录或解析失败返回 null
     */
    default LoginUser getCurrentUser(ApiRequest request) {
        return null;
    }

    /**
     * 获取当前请求用户的角色编码列表。
     *
     * @param request HTTP 请求
     * @return 角色编码列表；未登录或解析失败返回空列表
     * @deprecated 请实现 {@link #getCurrentUser(ApiRequest)}，             框架会自动从 LoginUser.roles 中取值
     */
    @Deprecated
    default List<String> getCurrentUserRoles(ApiRequest request) {
        LoginUser user = getCurrentUser(request);
        return user != null ? user.getRoles() : Collections.emptyList();
    }

    /**
     * 获取当前请求用户的 ID。
     *
     * @param request HTTP 请求
     * @return 用户 ID 字符串；未登录或解析失败返回 null
     * @deprecated 请实现 {@link #getCurrentUser(ApiRequest)}，             框架会自动从 LoginUser.id 中取值
     */
    @Deprecated
    default String getCurrentUserId(ApiRequest request) {
        LoginUser user = getCurrentUser(request);
        return user != null ? user.getId() : null;
    }

    /**
     * 获取第三方系统所有角色（用于"角色报表"管理页下拉/列表）。
     *
     * @return 全量角色列表；获取失败返回空列表
     */
    default List<RoleInfo> listAllRoles() {
        return Collections.emptyList();
    }
}
