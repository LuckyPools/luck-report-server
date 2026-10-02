package com.luck.report.web.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 报表模块自有文案翻译器
 */
public final class ReportI18n {

    private static final Logger logger = LoggerFactory.getLogger(ReportI18n.class);

    /**
     * 带 luck-report/ 私有前缀，避免与宿主项目资源重名
     */
    public static final String RESOURCE_BASE = "luck-report/i18n/messages_";

    private static final String RESOURCE_SUFFIX = ".properties";

    private static final Map<String, Map<String, String>> BUNDLES =
            new ConcurrentHashMap<String, Map<String, String>>();

    static {
        reload();
    }

    private ReportI18n() {
    }

    /**
     * 按当前线程语言翻译
     */
    public static String getMessage(String code, Object... args) {
        return getMessage(ReportLocaleContext.get(), code, args);
    }

    /**
     * 按指定语言翻译，未命中返回 code 本身而非空串，避免漏配文案时报错
     */
    public static String getMessage(ReportLocale locale, String code, Object... args) {
        if (code == null || code.isEmpty()) {
            return code;
        }
        ReportLocale target = locale == null ? ReportLocale.DEFAULT : locale;
        String pattern = lookup(target, code);
        if (pattern == null && target != ReportLocale.DEFAULT) {
            pattern = lookup(ReportLocale.DEFAULT, code);
        }
        if (pattern == null) {
            return code;
        }
        return format(pattern, args);
    }

    /**
     * 取异常的可展示文案
     */
    public static String messageOf(Throwable t) {
        if (t == null) {
            return null;
        }
        if (t instanceof com.luck.report.core.exception.ErrorCodeAware) {
            com.luck.report.core.exception.ErrorCodeAware aware =
                    (com.luck.report.core.exception.ErrorCodeAware) t;
            if (aware.getErrorCode() != null && !aware.getErrorCode().isEmpty()) {
                return getMessage(aware.getErrorCode(), aware.getErrorArgs());
            }
        }
        String msg = t.getMessage();
        return msg == null ? null : getMessage(msg);
    }

    /**
     * 任一语言命中即返回 true
     */
    public static boolean hasMessage(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        for (ReportLocale locale : ReportLocale.values()) {
            if (lookup(locale, code) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * 注册或覆盖文案，供第三方项目扩展
     */
    public static void register(ReportLocale locale, Map<String, String> messages) {
        if (locale == null || messages == null || messages.isEmpty()) {
            return;
        }
        Map<String, String> bundle = BUNDLES.get(locale.getCode());
        if (bundle == null) {
            bundle = new ConcurrentHashMap<String, String>();
            BUNDLES.put(locale.getCode(), bundle);
        }
        bundle.putAll(messages);
    }

    /**
     * 重新加载资源文件，保留 {@link #register} 注入的文案
     */
    public static void reload() {
        for (ReportLocale locale : ReportLocale.values()) {
            Map<String, String> loaded = loadBundle(locale.getCode());
            Map<String, String> bundle = BUNDLES.get(locale.getCode());
            if (bundle == null) {
                bundle = new ConcurrentHashMap<String, String>();
                BUNDLES.put(locale.getCode(), bundle);
            }
            bundle.putAll(loaded);
        }
    }

    /**
     * 只读文案快照，便于排查
     */
    public static Map<String, String> snapshot(ReportLocale locale) {
        Map<String, String> bundle = BUNDLES.get(locale.getCode());
        if (bundle == null) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<String, String>(bundle));
    }

    private static String lookup(ReportLocale locale, String code) {
        Map<String, String> bundle = BUNDLES.get(locale.getCode());
        return bundle == null ? null : bundle.get(code);
    }

    private static String format(String pattern, Object... args) {
        if (args == null || args.length == 0) {
            return pattern;
        }
        String result = pattern;
        for (int i = 0; i < args.length; i++) {
            result = result.replace("{" + i + "}", args[i] == null ? "" : String.valueOf(args[i]));
        }
        return result;
    }

    private static Map<String, String> loadBundle(String langCode) {
        String path = RESOURCE_BASE + langCode + RESOURCE_SUFFIX;
        Map<String, String> messages = new LinkedHashMap<String, String>();
        InputStream in = openStream(path);
        if (in == null) {
            logger.warn("[ReportI18n] resource not found: {}", path);
            return messages;
        }
        try {
            Properties props = new Properties();
            InputStreamReader reader = new InputStreamReader(in, StandardCharsets.UTF_8);
            try {
                props.load(reader);
            } finally {
                reader.close();
            }
            for (String key : props.stringPropertyNames()) {
                messages.put(key, props.getProperty(key));
            }
            logger.debug("[ReportI18n] loaded {} entries from {}", messages.size(), path);
        } catch (Exception e) {
            logger.warn("[ReportI18n] failed to load resource: " + path, e);
        } finally {
            try {
                in.close();
            } catch (Exception ignore) {
            }
        }
        return messages;
    }

    /**
     * 优先用 TCCL，适配宿主容器的类加载体系
     */
    private static InputStream openStream(String path) {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader != null) {
            InputStream in = loader.getResourceAsStream(path);
            if (in != null) {
                return in;
            }
        }
        loader = ReportI18n.class.getClassLoader();
        return loader == null ? null : loader.getResourceAsStream(path);
    }
}
