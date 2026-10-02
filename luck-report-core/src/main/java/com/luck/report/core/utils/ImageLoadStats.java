package com.luck.report.core.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 单次报表计算线程内的图片 Base64 缓存。
 */
public class ImageLoadStats {
    private static final ThreadLocal<ImageLoadStats> HOLDER = new ThreadLocal<ImageLoadStats>();

    private final Map<String, String> base64Cache = new HashMap<String, String>();

    public static void begin() {
        HOLDER.set(new ImageLoadStats());
    }

    public static ImageLoadStats current() {
        return HOLDER.get();
    }

    public static void end() {
        HOLDER.remove();
    }

    public String getCachedBase64(String cacheKey) {
        return base64Cache.get(cacheKey);
    }

    public void putCachedBase64(String cacheKey, String base64) {
        if (cacheKey != null && base64 != null) {
            base64Cache.put(cacheKey, base64);
        }
    }
}
