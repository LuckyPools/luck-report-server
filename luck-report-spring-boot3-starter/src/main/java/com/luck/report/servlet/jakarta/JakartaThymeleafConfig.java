package com.luck.report.servlet.jakarta;

import com.luck.report.web.modules.report.service.ViewRenderer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.spring6.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * jakarta.servlet（Spring Boot 3.x）Thymeleaf 配置。
 * <p>使用 thymeleaf-spring6 创建独立的模板引擎，不与第三方项目的模板引擎冲突。
 */
@Configuration
public class JakartaThymeleafConfig {

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
        return new JakartaViewRenderer(templateEngine);
    }
}
