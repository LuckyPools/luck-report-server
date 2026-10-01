package com.luck.report.core.config;

import com.luck.report.core.UReportPropertyPlaceholderConfigurer;
import com.luck.report.core.Utils;
import com.luck.report.infra.modules.cache.utils.CacheUtils;
import com.luck.report.core.expression.ExpressionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class UtilsConfiguration {

    @Bean("bean.uReportPropertyPlaceholderConfigurer")
    public UReportPropertyPlaceholderConfigurer uReportPropertyPlaceholderConfigurer() {
        UReportPropertyPlaceholderConfigurer configurer = new UReportPropertyPlaceholderConfigurer();
        configurer.setIgnoreUnresolvablePlaceholders(true);
        return configurer;
    }

    @Bean("bean.expressionUtils")
    public ExpressionUtils expressionUtils() {
        return new ExpressionUtils();
    }

    @Bean("bean.utils")
    public Utils utils(@Value("${luck-report.debug:false}") boolean debug) {
        Utils utils = new Utils();
        utils.setDebug(debug);
        return utils;
    }

    @Bean("bean.cacheUtils")
    public CacheUtils cacheUtils() {
        return new CacheUtils();
    }
}
