package com.luck.report.servlet.javax;

import com.luck.report.web.modules.report.service.ViewRenderer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * javax.servlet（Spring Boot 2.x）Thymeleaf 配置，使用独立模板引擎避免与宿主冲突
 */
@Configuration
@ConditionalOnClass(name = "org.thymeleaf.spring5.SpringTemplateEngine")
public class JavaxThymeleafConfig {

    @Value("${luck-report.template.cache:false}")
    private boolean templateCache;

    @Bean(name = "bean.luckReportTemplateResolver")
    public SpringResourceTemplateResolver templateResolver() {
        SpringResourceTemplateResolver resolver = new SpringResourceTemplateResolver();
        resolver.setPrefix("classpath:/html/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode(TemplateMode.HTML);
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(templateCache);
        resolver.setOrder(100);
        resolver.setCheckExistence(true);
        return resolver;
    }

    @Bean(name = "bean.luckReportTemplateEngine")
    public SpringTemplateEngine templateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(templateResolver());
        engine.setEnableSpringELCompiler(true);
        return engine;
    }

    @Bean("bean.viewRenderer")
    public ViewRenderer viewRenderer(@Qualifier("bean.luckReportTemplateEngine") SpringTemplateEngine templateEngine) {
        return new JavaxViewRenderer(templateEngine);
    }
}
