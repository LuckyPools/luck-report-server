package com.luck.report.web.security.domain.bo;

/**
 * 内置匿名角色常量。
 *
 * @author luck-report
 * @since 1.2.0
 */
public final class AnonymousRole {

    /**
     * 角色编码（与 luck_report_role.role_code 对应）
     */
    public static final String CODE = "ANONYMOUS";

    /**
     * 角色显示名
     */
    public static final String NAME = "匿名用户";

    private AnonymousRole() {
    }
}
