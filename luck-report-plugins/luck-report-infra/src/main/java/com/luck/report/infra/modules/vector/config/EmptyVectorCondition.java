package com.luck.report.infra.modules.vector.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.StringUtils;

/**
 * luck-report.vector.type 未配置时加载 Empty 兜底
 */
public class EmptyVectorCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String vectorType = context.getEnvironment().getProperty("luck-report.vector.type");
        return !StringUtils.hasText(vectorType);
    }
}
