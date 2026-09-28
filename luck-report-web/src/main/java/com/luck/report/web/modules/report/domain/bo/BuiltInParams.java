package com.luck.report.web.modules.report.domain.bo;

/**
 * 内置参数名常量定义。
 * <p>所有内置参数以 luck_ 前缀命名，与用户自定义参数区分。
 *
 * @author luck-report
 * @since 2.2.0
 */
public final class BuiltInParams {

    /** 当前登录用户 ID（String） */
    public static final String LUCK_USER_ID = "luck_user_id";

    /** 当前用户角色编码列表（List<String>，适配 SQL IN 查询） */
    public static final String LUCK_USER_ROLES = "luck_user_roles";

    private BuiltInParams() {
        // 常量类，禁止实例化
    }
}
