package com.luck.report.infra.modules.vector.config;

import com.luck.report.infra.modules.vector.service.VectorStore;
import com.luck.report.infra.modules.vector.service.impl.EmptyVectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

/**
 * 无其他 VectorStore 时注册 EmptyVectorStore 兜底。
 *
 * @author luck
 */
@Configuration
@Conditional(EmptyVectorCondition.class)
public class EmptyVectorStoreConfiguration {

    @Bean("bean.emptyVectorStore")
    @ConditionalOnMissingBean(VectorStore.class)
    public VectorStore emptyVectorStore() {
        return new EmptyVectorStore();
    }
}
