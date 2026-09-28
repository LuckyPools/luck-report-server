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
package com.luck.report.infra.modules.cache.utils;


import com.luck.report.infra.exception.BeanException;
import com.luck.report.infra.modules.cache.service.ReportCache;
import com.luck.report.infra.modules.cache.service.ReportCacheKeyResolver;
import com.luck.report.infra.utils.SpringBeanUtils;

import java.util.Collection;
import java.util.Set;

/**
 * 缓存操作统一入口。
 *
 * @author luckyPools
 * @since 2026年05月15日
 */
public class CacheUtils {

    private static volatile ReportCache reportCache;

    private static volatile ReportCacheKeyResolver reportCacheKeyResolver;

    private static ReportCache getReportCache() {
        if (reportCache == null) {
            synchronized (CacheUtils.class) {
                if (reportCache == null) {
                    Collection<ReportCache> services = SpringBeanUtils.getBeans(ReportCache.class);
                    for (ReportCache cache : services) {
                        if (cache.disabled()) {
                            continue;
                        }
                        reportCache = cache;
                        break;
                    }
                    if (reportCache == null) {
                        throw new BeanException("Missing ReportCache implementation. Please verify your configuration.");
                    }
                }
            }
        }
        return reportCache;
    }

    private static ReportCacheKeyResolver getReportCacheKeyResolver() {
        if (reportCacheKeyResolver == null) {
            synchronized (CacheUtils.class) {
                if (reportCacheKeyResolver == null) {
                    Collection<ReportCacheKeyResolver> services = SpringBeanUtils.getBeans(ReportCacheKeyResolver.class);
                    for (ReportCacheKeyResolver resolver : services) {
                        if (resolver.disabled()) {
                            continue;
                        }
                        reportCacheKeyResolver = resolver;
                        break;
                    }
                    if (reportCacheKeyResolver == null) {
                        throw new BeanException("Missing ReportCacheKeyResolver implementation. Please verify your configuration.");
                    }
                }
            }
        }
        return reportCacheKeyResolver;
    }

    public static <T> T get(String key) {
        return getReportCache().get(key);
    }

    public static <T> T get(String key, Class<T> clazz) {
        return getReportCache().get(key, clazz);
    }

    /**
     * 存入缓存，使用默认过期时间（由 luck-report.cacheExpireSeconds 配置，默认 15 分钟）。
     */
    public static <T> void put(String key, T value) {
        getReportCache().put(key, value);
    }

    /** @param time 过期秒数 */
    public static <T> void put(String key, T value, long time) {
        getReportCache().put(key, value, time);
    }

    public static boolean exists(String key) {
        return getReportCache().exists(key);
    }

    public static void remove(String key) {
        getReportCache().remove(key);
    }

    public static Set<String> keys(String keyPatten) {
        return getReportCache().keys(keyPatten);
    }

    /** @param time 过期秒数 */
    public static boolean setExpire(String key, long time) {
        return getReportCache().setExpire(key, time);
    }

    public static String getCacheScopePrefix() {
        return getReportCacheKeyResolver().getPrefix();
    }
}
