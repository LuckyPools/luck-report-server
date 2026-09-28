package com.luck.report.web.config;

import com.luck.report.web.modules.vector.service.RerankService;
import com.luck.report.web.modules.vector.service.impl.HttpRerankService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RerankAutoConfiguration {

    /** 默认 HttpRerankService；是否真正精排仍看 rerank-enabled + 激活模型 */
    @Bean
    @ConditionalOnMissingBean(RerankService.class)
    public RerankService rerankService(HttpRerankService httpRerankService) {
        return httpRerankService;
    }
}
