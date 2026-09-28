package com.luck.report.infra.modules.cache.service.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import com.luck.report.infra.modules.cache.service.ReportCache;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Caffeine 的本地缓存，支持按条目独立过期。
 *
 * @author luckyPools
 * @since 2017年3月8日
 */
public class LocalCacheService implements ReportCache {

    private boolean disabled;

    /**
     * 默认过期时间，单位：秒
     */
    private long defaultExpireSeconds = DEFAULT_EXPIRE_SECONDS;

    private final Cache<String, CacheEntry> cache;

    public LocalCacheService() {
        this.cache = Caffeine.newBuilder()
                .expireAfter(new Expiry<String, CacheEntry>() {
                    @Override
                    public long expireAfterCreate(String key, CacheEntry value, long currentTime) {
                        return value.getRemainingExpireNanos();
                    }

                    @Override
                    public long expireAfterUpdate(String key, CacheEntry value, long currentTime, long currentDuration) {
                        return value.getRemainingExpireNanos();
                    }

                    @Override
                    public long expireAfterRead(String key, CacheEntry value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                })
                .initialCapacity(100)
                .maximumSize(10000)
                .build();
    }

    private static class CacheEntry {
        private final Object value;
        private final long expireTimeNanos;

        public CacheEntry(Object value, long expireSeconds) {
            this.value = value;
            this.expireTimeNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(expireSeconds);
        }

        public long getRemainingExpireNanos() {
            long remaining = expireTimeNanos - System.nanoTime();
            return remaining > 0 ? remaining : 0;
        }

        public Object getValue() {
            return value;
        }
    }

    @Override
    public boolean disabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    /**
     * 设置默认过期时间。
     *
     * @param defaultExpireSeconds 过期时间，单位：秒；小于等于 0 时回退为 {@link #DEFAULT_EXPIRE_SECONDS}
     */
    public void setDefaultExpireSeconds(long defaultExpireSeconds) {
        this.defaultExpireSeconds = defaultExpireSeconds > 0 ? defaultExpireSeconds : DEFAULT_EXPIRE_SECONDS;
    }

    @Override
    public <T> T get(String key) {
        CacheEntry entry = cache.getIfPresent(key);
        if (entry == null) {
            return null;
        }
        @SuppressWarnings("unchecked")
        T value = (T) entry.getValue();
        return value;
    }

    @Override
    public <T> T get(String key, Class<T> clazz) {
        Object value = get(key);
        if (value == null) {
            return null;
        }
        return clazz.cast(value);
    }

    @Override
    public <T> void put(String key, T value) {
        put(key, value, defaultExpireSeconds);
    }

    @Override
    public <T> void put(String key, T value, long time) {
        if (key == null || value == null) {
            return;
        }
        cache.put(key, new CacheEntry(value, time));
    }

    @Override
    public boolean exists(String key) {
        return cache.getIfPresent(key) != null;
    }

    @Override
    public void remove(String key) {
        cache.invalidate(key);
    }

    @Override
    public Set<String> keys(String keyPatten) {
        Set<String> result = new HashSet<>();
        if (keyPatten == null) {
            return result;
        }
        for (String key : cache.asMap().keySet()) {
            if (key != null && key.startsWith(keyPatten)) {
                result.add(key);
            }
        }
        return result;
    }

    /** Caffeine 不支持直接改过期时间，通过重新 put 实现 */
    @Override
    public boolean setExpire(String key, long time) {
        CacheEntry entry = cache.getIfPresent(key);
        if (entry == null) {
            return false;
        }
        cache.put(key, new CacheEntry(entry.getValue(), time));
        return true;
    }
}
