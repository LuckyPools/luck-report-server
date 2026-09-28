package com.luck.report.web.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** 读写 ureport XML 根节点 reportId（仅改起始标签，不整树序列化）。 */
public final class ReportIdXmlUtils {

    public static final String ATTR_REPORT_ID = "reportId";

    private static final Pattern UREPORT_OPEN_TAG =
            Pattern.compile("<ureport\\b([^>]*)>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final Pattern REPORT_ID_ATTR =
            Pattern.compile("\\breportId\\s*=\\s*[\"']([^\"']*)[\"']", Pattern.CASE_INSENSITIVE);

    private static final Pattern REPORT_ID_ATTR_REPLACE =
            Pattern.compile("\\breportId\\s*=\\s*[\"'][^\"']*[\"']", Pattern.CASE_INSENSITIVE);

    private static final Pattern SNOWFLAKE_ID = Pattern.compile("^\\d{15,19}$");

    private ReportIdXmlUtils() {
    }

    public static String readReportId(String xml) {
        if (StringUtils.isBlank(xml)) {
            return null;
        }
        Matcher tag = UREPORT_OPEN_TAG.matcher(xml);
        if (!tag.find()) {
            return null;
        }
        Matcher attr = REPORT_ID_ATTR.matcher(tag.group(1));
        if (!attr.find()) {
            return null;
        }
        String id = attr.group(1) == null ? null : attr.group(1).trim();
        return StringUtils.isBlank(id) ? null : id;
    }

    public static String ensureReportId(String xml, String reportId) {
        if (xml == null || StringUtils.isBlank(reportId)) {
            return xml;
        }
        Matcher tag = UREPORT_OPEN_TAG.matcher(xml);
        if (!tag.find()) {
            return xml;
        }
        String attrs = tag.group(1);
        String newAttrs;
        Matcher existing = REPORT_ID_ATTR_REPLACE.matcher(attrs);
        if (existing.find()) {
            newAttrs = existing.replaceFirst("reportId=\"" + Matcher.quoteReplacement(reportId.trim()) + "\"");
        } else {
            newAttrs = " reportId=\"" + reportId.trim() + "\"" + attrs;
        }
        return xml.substring(0, tag.start()) + "<ureport" + newAttrs + ">" + xml.substring(tag.end());
    }

    /** 15~19 位纯数字，用于区分雪花 id 与按标题创建的路径。 */
    public static boolean isSnowflakeId(String id) {
        return id != null && SNOWFLAKE_ID.matcher(id.trim()).matches();
    }
}
