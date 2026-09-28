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
package com.luck.report.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.EncodedResourceResolver;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.util.concurrent.TimeUnit;

/**
 * Web配置类，用于注册静态资源处理器等Web相关配置
 *
 * @author Jacky.gao
 * @since 2017年3月8日
 */
@Slf4j
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 后台 servlet 前缀（默认 {@code report}），与所有 controller 的
     * {@code @RequestMapping("${luck-report.servletPrefix:}/xxx")} 保持一致。
     */
    @Value("${luck-report.servletPrefix:}")
    private String servletPrefix;

    /**
     * 注册前端静态资源处理器（yml 只支持单 pattern/单 location 列表，编程式注册）：
     * {@code /<servletPrefix>/lib/**} -> classpath:/html/lib/（npm run lib 产物）。
     * <p>lib 资源链：{@link EncodedResourceResolver} 回吐构建期预生成的 .gz（无需宿主开启
     * server.compression），{@link PathResourceResolver} 兜底回原文件；
     * Cache-Control 为 <b>1 天</b>公开缓存。发版/多次 build:lib 靠壳页面
     * {@code ?v=${assetVersion}} 换 URL 强制拉新，不必依赖缩短 max-age，也不必用户清缓存。
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String prefix = servletPrefix == null || servletPrefix.isEmpty() ? "report" : servletPrefix;
        registry.addResourceHandler("/" + prefix + "/lib/**")
                .addResourceLocations("classpath:/html/lib/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .resourceChain(true)
                .addResolver(new EncodedResourceResolver())
                .addResolver(new PathResourceResolver());
    }
}
