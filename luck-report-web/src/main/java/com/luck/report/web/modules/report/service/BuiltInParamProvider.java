package com.luck.report.web.modules.report.service;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;

import java.util.Map;

/**
 * 内置参数提供者 SPI。
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
