package com.luck.report.web.modules.report.service.impl;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.web.modules.report.service.BuiltInParamProvider;
import com.luck.report.web.utils.UrlParameterUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 报表参数服务。
 * <p>从请求构建参数 Map，并收集所有 BuiltInParamProvider 提供的内置参数注入其中。
 * <p>安全策略：内置参数强制覆盖目标 Map 中的同名值，防止前端伪造用户身份。
 *
 * @author luck-report
 * @since 2.2.0
 */
@Service("bean.paramService")
public class ParamService {

    private static final Logger log = LoggerFactory.getLogger(ParamService.class);

    @Autowired
    private List<BuiltInParamProvider> providers;

    /**
     * 从 HTTP 请求构建完整参数 Map（URL 参数 + 内置参数）。
     * <p>供预览、导出等需要「请求参数 + 内置参数」的场景统一调用。
     *
     * @param request HTTP 请求上下文，不可为空
     * @return 包含 URL 参数与内置参数的 Map，不可为空
     */
    public Map<String, Object> buildAllParameters(ApiRequest request) {
        Map<String, Object> parameters = UrlParameterUtils.buildParameters(request);
        injectBuiltInParams(parameters, request);
        return parameters;
    }

    /**
     * 收集所有提供者的内置参数并注入到目标 Map
     * <p>内置参数强制覆盖目标 Map 中的同名值（安全防伪）。
     *
     * @param targetParams 目标参数 Map，不可为空
     * @param request HTTP 请求上下文，可为空（非 HTTP 场景下跳过注入）
     */
    public void injectBuiltInParams(Map<String, Object> targetParams, ApiRequest request) {
        if (targetParams == null || request == null) {
            return;
        }
        // 按 @Order 排序，保证框架默认 Provider 先执行
        List<BuiltInParamProvider> sorted = new ArrayList<>(providers);
        OrderComparator.sort(sorted);
        for (BuiltInParamProvider provider : sorted) {
            try {
                Map<String, Object> provided = provider.provideBuiltInParams(request);
                if (provided == null || provided.isEmpty()) {
                    continue;
                }
                // 强制覆盖：内置参数不可被前端同名参数覆盖
                targetParams.putAll(provided);
            } catch (Exception e) {
                log.error("内置参数注入异常, provider={}: {}", provider.getClass().getName(), e.getMessage(), e);
            }
        }
    }
}
