package com.luck.report.servlet.javax;

import com.luck.report.web.modules.report.constant.LibAssetVersions;
import com.luck.report.web.modules.report.service.ViewRenderer;
import org.springframework.web.context.request.NativeWebRequest;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * javax.servlet（Spring Boot 2.x）ViewRenderer 实现。
 * <p>使用 Thymeleaf + javax.servlet WebContext 渲染页面。
 */
public class JavaxViewRenderer implements ViewRenderer {

    private final SpringTemplateEngine templateEngine;

    public JavaxViewRenderer(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    @Override
    public void render(String templateName, NativeWebRequest webRequest, String servletPrefix) throws IOException {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);

        // 构建 Thymeleaf WebContext
        WebContext context = new WebContext(request, response, request.getServletContext());

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
