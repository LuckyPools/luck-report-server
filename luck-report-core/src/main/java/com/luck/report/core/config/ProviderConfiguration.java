package com.luck.report.core.config;

import com.luck.report.core.provider.image.DefaultImageProvider;
import com.luck.report.core.provider.image.HttpImageProvider;
import com.luck.report.core.provider.image.HttpsImageProvider;
import com.luck.report.core.provider.report.classpath.ClasspathReportProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProviderConfiguration {

    @Bean("bean.defaultImageProvider")
    public DefaultImageProvider defaultImageProvider() {
        return new DefaultImageProvider();
    }

    @Bean("bean.httpImageProvider")
    public HttpImageProvider httpImageProvider() {
        return new HttpImageProvider();
    }

    @Bean("bean.httpsImageProvider")
    public HttpsImageProvider httpsImageProvider() {
        return new HttpsImageProvider();
    }

    @Bean("bean.classpathReportProvider")
    public ClasspathReportProvider classpathReportProvider() {
        return new ClasspathReportProvider();
    }
}
