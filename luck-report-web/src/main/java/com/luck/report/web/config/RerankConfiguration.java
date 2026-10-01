package com.luck.report.web.config;

import com.luck.report.web.modules.vector.service.RerankService;
import com.luck.report.web.modules.vector.service.impl.HttpRerankService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RerankConfiguration {

    /**
     * 对外精排入口 bean.rerankService（默认同 HttpRerankService）。
     * 宿主可自行声明同名 Bean 覆盖；不按类型 MissingBean，避免与 @Service HttpRerankService 冲突。
     */
    @Bean("bean.rerankService")
    @ConditionalOnMissingBean(name = "bean.rerankService")
    public RerankService rerankService(
            @Qualifier("bean.httpRerankService") HttpRerankService httpRerankService) {
        return httpRerankService;
    }
}
