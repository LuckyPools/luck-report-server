package com.luck.report.web.modules.report.service;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;

import java.util.Map;

/**
 * 内置参数提供者 SPI。
 * <p>第三方实现此接口并注册为 Spring Bean，即可向报表 SQL 注入自定义内置参数。
 * <p>多个 Provider 按 @Order / @Priority 排序，同参数名后者覆盖前者。
 * 框架默认实现 UserBuiltInParamProvider 使用 @Order(Ordered.HIGHEST_PRECEDENCE)。
 *
 * @author luck-report
 * @since 2.2.0
 */
public interface BuiltInParamProvider {

    /**
     * 提供内置参数键值对
     *
     * @param request HTTP 请求上下文（含 token、header 等），不可为空
     * @return 内置参数 Map（参数名 -> 参数值），不可为空（无参数时返回空 Map）
     */
    Map<String, Object> provideBuiltInParams(ApiRequest request);
}
