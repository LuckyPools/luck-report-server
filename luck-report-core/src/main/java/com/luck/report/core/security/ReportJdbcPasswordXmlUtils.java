package com.luck.report.core.security;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 报表 XML 中自定义 JDBC 数据源 password 属性的加密工具。原地替换属性值，避免整文档 DOM 重写改变格式 / CDATA。
 */
public final class ReportJdbcPasswordXmlUtils {

    private static final Logger log = LoggerFactory.getLogger(ReportJdbcPasswordXmlUtils.class);

    /**
     * 匹配单个 datasource 开始标签；要求同标签内 type=jdbc，并捕获 password 属性引号与值。
     */
    private static final Pattern JDBC_DATASOURCE_PASSWORD = Pattern.compile(
            "<datasource\\b(?=[^>]*\\btype\\s*=\\s*([\"'])jdbc\\1)[^>]*\\bpassword\\s*=\\s*([\"'])(.*?)\\2",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private ReportJdbcPasswordXmlUtils() {
    }

    /**
     * 加密报表 XML 中所有 JDBC 数据源密码后返回；无需改动时返回原串。
     */
    public static String encryptJdbcPasswords(String xml) {
        if (xml == null || xml.isEmpty()) {
            return xml;
        }
        if (!SensitiveConfigCipher.isEnabled()) {
            return xml;
        }
        try {
            Matcher matcher = JDBC_DATASOURCE_PASSWORD.matcher(xml);
            StringBuffer sb = new StringBuffer();
            boolean changed = false;
            while (matcher.find()) {
                String quote = matcher.group(2);
                String rawAttr = matcher.group(3);
                String decoded = StringEscapeUtils.unescapeXml(rawAttr);
                if (decoded == null || decoded.isEmpty() || SensitiveConfigCipher.isEncrypted(decoded)) {
                    matcher.appendReplacement(sb, Matcher.quoteReplacement(matcher.group(0)));
                    continue;
                }
                String encrypted = SensitiveConfigCipher.encrypt(decoded);
                String encoded = StringEscapeUtils.escapeXml10(encrypted);
                String replacement = rebuildTag(matcher.group(0), quote, encoded);
                matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
                changed = true;
            }
            if (!changed) {
                return xml;
            }
            matcher.appendTail(sb);
            return sb.toString();
        } catch (Exception e) {
            log.warn("Encrypt JDBC passwords in report XML failed, keep original content: {}", e.getMessage());
            return xml;
        }
    }

    private static String rebuildTag(String fullMatch, String quote, String newEncoded) {
        int passwordKey = indexOfIgnoreCase(fullMatch, "password");
        if (passwordKey < 0) {
            return fullMatch;
        }
        int eq = fullMatch.indexOf('=', passwordKey);
        int openQuote = fullMatch.indexOf(quote.charAt(0), eq + 1);
        if (openQuote < 0) {
            return fullMatch;
        }
        int closeQuote = fullMatch.indexOf(quote.charAt(0), openQuote + 1);
        if (closeQuote < 0) {
            return fullMatch;
        }
        return fullMatch.substring(0, openQuote + 1) + newEncoded + fullMatch.substring(closeQuote);
    }

    private static int indexOfIgnoreCase(String text, String token) {
        return text.toLowerCase().indexOf(token.toLowerCase());
    }
}
