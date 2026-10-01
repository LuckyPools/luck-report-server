package com.luck.report.web.modules.report.service.impl;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.web.modules.report.domain.bo.BuiltInParams;
import com.luck.report.web.modules.report.service.BuiltInParamProvider;
import com.luck.report.web.security.utils.SecurityUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认内置参数提供者：提供用户身份相关参数。
 * <p>使用 @Order(Ordered.HIGHEST_PRECEDENCE) 保证最先执行，
 * 第三方 Provider 可用更低优先级覆盖同名参数。
 *
 * @author luck-report
 * @since 2.2.0
 */
@Component("bean.userBuiltInParamProvider")
@Order(Ordered.HIGHEST_PRECEDENCE)
public class UserBuiltInParamProvider implements BuiltInParamProvider {

    /**
     * 从 TokenService 提取当前用户 ID 和角色列表，作为内置参数返回
     *
     * @param request HTTP 请求上下文，不可为空
     * @return 包含 luck_user_id 和 luck_user_roles 的 Map，不可为空
     */
    @Override
    public Map<String, Object> provideBuiltInParams(ApiRequest request) {
        Map<String, Object> params = new HashMap<>(2);
        if (request == null) {
            return params;
        }
        String userId = SecurityUtils.getCurrentUserId();
        if (StringUtils.isBlank(userId)) {
            return params;
        }
        params.put(BuiltInParams.LUCK_USER_ID, userId);
        return params;
    }
}
