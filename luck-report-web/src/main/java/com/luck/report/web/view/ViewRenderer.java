package com.luck.report.web.view;

import org.springframework.web.context.request.NativeWebRequest;

import java.io.IOException;

/**
 * 视图渲染器接口，屏蔽 javax.servlet / jakarta.servlet 差异。
 * <p>由适配器模块提供具体实现，使用 Thymeleaf 模板引擎渲染页面。
 *
 * @author luck-report
 * @since 2.0.0
 */
public interface ViewRenderer {

    /**
     * 渲染指定模板。
     *
     * @param templateName 模板名称（不含后缀）
     * @param webRequest   当前 Web 请求
     * @param servletPrefix servlet 前缀
     * @throws IOException 渲染异常
     */
    void render(String templateName, NativeWebRequest webRequest, String servletPrefix) throws IOException;
}
