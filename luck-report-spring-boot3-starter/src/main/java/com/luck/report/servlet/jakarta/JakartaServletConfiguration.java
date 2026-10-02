package com.luck.report.servlet.jakarta;

import com.luck.report.web.modules.report.constant.ReportUrls;
import com.luck.report.web.modules.role.mapper.ReportRoleMapper;
import com.luck.report.web.security.service.impl.ReportAccessChecker;
import com.luck.report.web.config.properties.TokenProperties;
import com.luck.report.web.security.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class JakartaServletConfiguration implements WebMvcConfigurer {

    @Value("${luck-report.servletPrefix:}")
    private String servletPrefix;

    @Autowired
    @Qualifier("bean.reportAccessChecker")
    private ReportAccessChecker reportAccessChecker;

    @Autowired
    @Qualifier("bean.tokenProperties")
    private TokenProperties tokenProperties;

    @Autowired
    private TokenService tokenService;

    @Autowired
    @Qualifier("bean.reportRoleMapper")
    private ReportRoleMapper reportRoleMapper;

    @Bean("bean.traceIdFilterRegistration")
    public FilterRegistrationBean<JakartaTraceIdFilter> traceIdFilterRegistration() {
        FilterRegistrationBean<JakartaTraceIdFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JakartaTraceIdFilter());
        registration.addUrlPatterns("/*");
        registration.setName("traceIdFilter");
        registration.setOrder(0);
        return registration;
    }

    @Bean("bean.requestHolderFilterRegistration")
    public FilterRegistrationBean<JakartaRequestHolderFilter> requestHolderFilterRegistration() {
        FilterRegistrationBean<JakartaRequestHolderFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JakartaRequestHolderFilter());
        registration.addUrlPatterns("/*");
        registration.setName("requestHolderFilter");
        registration.setOrder(1);
        return registration;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        String prefix = servletPrefix == null || servletPrefix.isEmpty() ? "" : servletPrefix;

        registry.addInterceptor(new JakartaTokenInterceptor(tokenService, tokenProperties, reportRoleMapper))
                .addPathPatterns(ReportUrls.managePathPatterns(prefix))
                .addPathPatterns(ReportUrls.previewPathPatterns(prefix))
                .addPathPatterns("/" + prefix + "/res/**")
                .excludePathPatterns("/" + prefix + "/auth/**");

        registry.addInterceptor(new JakartaManageInterceptor(tokenService, tokenProperties))
                .addPathPatterns(ReportUrls.managePathPatterns(prefix))
                .excludePathPatterns("/" + prefix + "/auth/**")
                .order(2);

        registry.addInterceptor(new JakartaPreviewInterceptor(reportAccessChecker, tokenProperties))
                .addPathPatterns(ReportUrls.previewPathPatterns(prefix))
                .excludePathPatterns("/" + prefix + "/auth/**")
                .order(3);
    }
}
