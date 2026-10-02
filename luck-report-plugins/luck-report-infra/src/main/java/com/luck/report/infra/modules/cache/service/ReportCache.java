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
package com.luck.report.infra.modules.cache.service;


import java.util.Set;

/**
 * 报表缓存接口。
 *
 * @author Jacky.gao
 * @since 2017年3月8日
 */
public interface ReportCache {

    /**
     * 缓存默认过期时间（秒），对应配置 luck-report.cacheExpireSeconds，默认 15 分钟。
     */
    long DEFAULT_EXPIRE_SECONDS = 15 * 60L;

    boolean disabled();

    <T> T get(String key);

    <T> T get(String key, Class<T> clazz);

    /**
     * 使用默认过期时间（见 {@link #DEFAULT_EXPIRE_SECONDS}）。
     */
    <T> void put(String key, T value);

    /**
     * @param time 过期秒数
     */
    <T> void put(String key, T value, long time);

    boolean exists(String key);

    void remove(String key);

    Set<String> keys(String keyPatten);

    /**
     * @param time 过期秒数
     */
    boolean setExpire(String key, long time);

}
