package com.luck.report.web.i18n;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;

/**
 * 从 HTTP 请求解析语言
 */
public final class ReportLocaleResolver {

    public static final String LANG_HEADER = "X-Lang";

    public static final String LANG_PARAM = "lang";

    private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";

    private ReportLocaleResolver() {
    }

    public static ReportLocale resolve(ApiRequest request) {
        if (request == null) {
            return ReportLocale.DEFAULT;
        }
        ReportLocale locale = ReportLocale.of(request.getHeader(LANG_HEADER));
        if (locale != null) {
            return locale;
        }
        locale = ReportLocale.of(request.getParameter(LANG_PARAM));
        if (locale != null) {
            return locale;
        }
        locale = fromAcceptLanguage(request.getHeader(ACCEPT_LANGUAGE_HEADER));
        return locale == null ? ReportLocale.DEFAULT : locale;
    }

    /**
     * 按 q 值降序取第一个可识别的语种
     */
    private static ReportLocale fromAcceptLanguage(String header) {
        if (header == null || header.trim().isEmpty()) {
            return null;
        }
        String[] candidates = header.split(",");
        for (String candidate : candidates) {
            int idx = candidate.indexOf(';');
            String tag = (idx >= 0 ? candidate.substring(0, idx) : candidate).trim();
            ReportLocale locale = ReportLocale.of(tag);
            if (locale != null) {
                return locale;
            }
        }
        return null;
    }
}
