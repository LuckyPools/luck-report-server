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
        String prefix = servletPrefix == null || servletPrefix.isEmpty() ? "report" : servletPrefix;

        // 1. Token 拦截器（order=1）- 校验 token 有效性（含匿名报表检查）
        registry.addInterceptor(new JakartaTokenInterceptor(tokenService, tokenProperties, reportRoleMapper))
                .addPathPatterns(ReportUrls.managePathPatterns(prefix))
                .addPathPatterns(ReportUrls.previewPathPatterns(prefix))
                .addPathPatterns("/" + prefix + "/res/**")
                .excludePathPatterns("/" + prefix + "/auth/**");

        // 2. 管理端拦截器（order=2）- 校验用户是否为 admin 角色
        registry.addInterceptor(new JakartaManageInterceptor(tokenService, tokenProperties))
                .addPathPatterns(ReportUrls.managePathPatterns(prefix))
                .excludePathPatterns("/" + prefix + "/auth/**")
                .order(2);

        // 3. 预览/导出拦截器（order=3）- 校验用户是否有权访问指定报表（含匿名报表放行）
        registry.addInterceptor(new JakartaPreviewInterceptor(reportAccessChecker, tokenProperties))
                .addPathPatterns(ReportUrls.previewPathPatterns(prefix))
                .excludePathPatterns("/" + prefix + "/auth/**")
                .order(3);
    }
}
