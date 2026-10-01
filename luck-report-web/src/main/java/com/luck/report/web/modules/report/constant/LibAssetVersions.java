package com.luck.report.web.modules.report.constant;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

/**
 * lib 静态资源缓存戳：用于壳页面 {@code ?v=} 强制刷新。
 * <p>
 * {@link com.luck.report.web.config.WebConfiguration} 对 {@code /lib/**} 设置了 1 天缓存；
 * 一天多次 {@code build:lib} 时，靠本戳变化让浏览器拉取新 URL，无需手动清缓存。
 */
public final class LibAssetVersions {

    /** 参与指纹的关键产物（任一变更都会换 ?v=） */
    private static final String[] FINGERPRINT_FILES = {
        "/html/lib/style.css",
        "/html/lib/luck-report-ui.umd.js",
        "/html/lib/vendor.js",
        "/html/lib/langchain.js",
        "/html/lib/iconfont.woff2"
    };

    private LibAssetVersions() {}

    /**
     * 综合多个 lib 产物的 lastModified + length 生成版本戳；
     * 读不到时退化为分钟级时间桶，避免空字符串。
     */
    public static String current() {
        long maxModified = 0L;
        long totalLength = 0L;
        boolean any = false;
        for (String path : FINGERPRINT_FILES) {
            URL url = LibAssetVersions.class.getResource(path);
            if (url == null) {
                continue;
            }
            try {
                URLConnection conn = url.openConnection();
                long modified = conn.getLastModified();
                long length = conn.getContentLengthLong();
                if (modified > maxModified) {
                    maxModified = modified;
                }
                if (length > 0) {
                    totalLength += length;
                }
                any = true;
            } catch (IOException ignored) {
                // try next
            }
        }
        if (any && (maxModified > 0 || totalLength > 0)) {
            return maxModified + "-" + totalLength;
        }
        return String.valueOf(System.currentTimeMillis() / 60_000L);
    }
}
