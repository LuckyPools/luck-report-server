package com.luck.report.web.i18n;

import java.util.Locale;

/**
 * 报表模块自有语言枚举。不复用 Spring 的 Locale 体系，避免与宿主应用的国际化配置干扰。
 */
public enum ReportLocale {

    /**
     * 简体中文
     */
    ZH("zh"),

    /**
     * 英文
     */
    EN("en");

    /**
     * 未获取到语言时的兜底值
     */
    public static final ReportLocale DEFAULT = ZH;

    private final String code;

    ReportLocale(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 兼容 zh / zh_CN / zh-CN / en_US 等写法；无法识别返回 null
     */
    public static ReportLocale of(String value) {
        if (value == null) {
            return null;
        }
        String raw = value.trim().toLowerCase(Locale.ROOT);
        if (raw.isEmpty()) {
            return null;
        }
        if (raw.startsWith("zh") || raw.startsWith("cn")) {
            return ZH;
        }
        if (raw.startsWith("en")) {
            return EN;
        }
        return null;
    }
}
