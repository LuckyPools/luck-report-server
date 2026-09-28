package com.luck.report.servlet.javax;

import com.luck.report.web.view.ViewRenderer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.spring5.templateresolver.SpringResourceTemplateResolver;
import org.thymeleaf.templatemode.TemplateMode;

/**
 * javax.servlet（Spring Boot 2.x）Thymeleaf 配置。
 * <p>使用 thymeleaf-spring5 创建独立的模板引擎，不与第三方项目的模板引擎冲突。
 * <p>仅当 classpath 上存在 Thymeleaf 时才加载，避免宿主项目无 Thymeleaf 时启动崩溃。
 */
@Configuration
@ConditionalOnClass(name = "org.thymeleaf.spring5.SpringTemplateEngine")
public class JavaxThymeleafConfig {

    @Value("${luck-report.template.cache:false}")
    private boolean templateCache;

    @Bean(name = "bean.luckReportTemplateResolver")
    public SpringResourceTemplateResolver luckReportTemplateResolver() {
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
    public SpringTemplateEngine luckReportTemplateEngine() {
        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(luckReportTemplateResolver());
        engine.setEnableSpringELCompiler(true);
        return engine;
    }

    @Bean
    public ViewRenderer viewRenderer(@Qualifier("bean.luckReportTemplateEngine") SpringTemplateEngine templateEngine) {
        return new JavaxViewRenderer(templateEngine);
    }
}
