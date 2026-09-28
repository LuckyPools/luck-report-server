/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.luck.report.core.provider.image;

import com.luck.report.core.exception.ReportComputeException;
import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.utils.HttpUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.ResourceUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * @author Jacky.gao
 * @since 2017年3月6日
 */
public class DefaultImageProvider implements ImageProvider, ApplicationContextAware {
    private ApplicationContext applicationContext;
    private String baseWebPath;

    @Override
    public InputStream getImage(String path) {
        try {
            if (path.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX) || path.startsWith("/WEB-INF")) {
                return applicationContext.getResource(path).getInputStream();
            } else {
                path = baseWebPath + path;
                return new FileInputStream(path);
            }
        } catch (IOException e) {
            throw new ReportComputeException(e);
        }
    }

    @Override
    public boolean support(String path) {
        if (path.startsWith(ResourceUtils.CLASSPATH_URL_PREFIX)) {
            return true;
        } else if (baseWebPath != null && (path.startsWith("/") || path.startsWith("/WEB-INF"))) {
            return true;
        }
        return false;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 优先通过反射从 WebApplicationContext 获取 ServletContext.getRealPath("/")
        // 使用反射避免编译时依赖 javax.servlet.ServletContext / jakarta.servlet.ServletContext
        baseWebPath = resolveWebPathFromContext(applicationContext);
        if (baseWebPath == null) {
            // 降级：从 HttpUtils 获取 ApiRequest.ServletContext
            try {
                ApiRequest request = HttpUtils.getRequest();
                if (request != null) {
                    ApiRequest.ServletContext servletContext = request.getServletContext();
                    if (servletContext != null) {
                        baseWebPath = servletContext.getRealPath("/");
                    }
                }
            } catch (Exception ignored) {
                // HttpUtils 可能尚未初始化（SPI 未加载），忽略
            }
        }
        this.applicationContext = applicationContext;
    }

    /**
     * 通过反射从 WebApplicationContext 获取 baseWebPath。
     * <p>兼容 javax.servlet（Spring Boot 2）和 jakarta.servlet（Spring Boot 3），
     * 避免编译时依赖具体的 Servlet API。
     *
     * @param ctx ApplicationContext
     * @return web 应用根路径，获取失败返回 null
     */
    private String resolveWebPathFromContext(ApplicationContext ctx) {
        try {
            // 检查是否为 WebApplicationContext（通过类名判断，避免 import）
            Class<?> wacClass = Class.forName(
                    "org.springframework.web.context.WebApplicationContext");
            if (!wacClass.isInstance(ctx)) {
                return null;
            }
            // 调用 getServletContext()
            Method getServletContext = wacClass.getMethod("getServletContext");
            Object servletContext = getServletContext.invoke(ctx);
            if (servletContext == null) {
                return null;
            }
            // 调用 ServletContext.getRealPath("/")
            Method getRealPath = servletContext.getClass().getMethod("getRealPath", String.class);
            Object result = getRealPath.invoke(servletContext, "/");
            return result != null ? result.toString() : null;
        } catch (Exception ignored) {
            // 反射失败（非 Web 环境或类不存在），返回 null
            return null;
        }
    }
}
