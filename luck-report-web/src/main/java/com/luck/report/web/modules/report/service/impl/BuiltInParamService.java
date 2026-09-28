package com.luck.report.web.modules.report.service.impl;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.web.modules.report.service.BuiltInParamProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.OrderComparator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 内置参数注入服务。
 * <p>收集所有 BuiltInParamProvider 提供的内置参数，注入到目标参数 Map 中。
 * <p>安全策略：内置参数强制覆盖目标 Map 中的同名值，防止前端伪造用户身份。
 *
 * @author luck-report
 * @since 2.2.0
 */
@Service
public class BuiltInParamService {

    private static final Logger log = LoggerFactory.getLogger(BuiltInParamService.class);

    @Autowired
    private List<BuiltInParamProvider> providers;

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
