package com.luck.report.servlet.jakarta;

import com.luck.report.web.view.LibAssetVersions;
import com.luck.report.web.view.ViewRenderer;
import org.springframework.web.context.request.NativeWebRequest;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * jakarta.servlet（Spring Boot 3.x）ViewRenderer 实现。
 * <p>使用 Thymeleaf + jakarta.servlet WebContext 渲染页面。
 */
public class JakartaViewRenderer implements ViewRenderer {

    private final SpringTemplateEngine templateEngine;

    public JakartaViewRenderer(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public void render(String templateName, NativeWebRequest webRequest, String servletPrefix) throws IOException {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);

        // 构建 Thymeleaf WebContext（Thymeleaf 3.1+ 使用 JakartaServletWebApplication）
        JakartaServletWebApplication webApp = JakartaServletWebApplication.buildApplication(request.getServletContext());
        IWebExchange webExchange = webApp.buildExchange(request, response);
        WebContext context = new WebContext(webExchange, request.getLocale());

        // 注入模板变量
        String token = request.getParameter("token");
        String baseURL = request.getContextPath()
                + (servletPrefix == null || servletPrefix.isEmpty() ? "" : "/" + servletPrefix);
        context.setVariable("token", token == null ? "" : token);
        context.setVariable("baseURL", baseURL);
        context.setVariable("servletPrefix", servletPrefix == null ? "" : servletPrefix);
        // 强缓存下的 cache-bust：发版/重新 build:lib 后强制拉新 style.css（含 iconfont）
        context.setVariable("assetVersion", LibAssetVersions.current());

        // 设置响应类型
        response.setContentType("text/html;charset=UTF-8");

        // 使用独立模板引擎渲染
        templateEngine.process(templateName, context, response.getWriter());
    }
}
