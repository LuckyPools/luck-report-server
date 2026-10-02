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
     *
     * @param targetParams 目标参数 Map，不可为空
     * @param request HTTP 请求上下文，可为空（非 HTTP 场景下跳过注入）
     */
    public void injectBuiltInParams(Map<String, Object> targetParams, ApiRequest request) {
        if (targetParams == null || request == null) {
            return;
        }
        List<BuiltInParamProvider> sorted = new ArrayList<>(providers);
        OrderComparator.sort(sorted);
        for (BuiltInParamProvider provider : sorted) {
            try {
                Map<String, Object> provided = provider.provideBuiltInParams(request);
                if (provided == null || provided.isEmpty()) {
                    continue;
                }
                targetParams.putAll(provided);
            } catch (Exception e) {
                log.error("内置参数注入异常, provider={}: {}", provider.getClass().getName(), e.getMessage(), e);
            }
        }
    }
}
