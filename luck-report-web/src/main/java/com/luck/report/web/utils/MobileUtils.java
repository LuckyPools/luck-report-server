/**
 * ****************************************************************************
 */
package com.luck.report.web.utils;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Jacky.gao
 * @since 2017年10月11日
 */
public class MobileUtils {
    private static final String phoneReg = "\\b(ip(hone|od)|android|opera m(ob|in)i"
            + "|windows (phone|ce)|blackberry"
            + "|s(ymbian|eries60|amsung)|p(laybook|alm|rofile/midp"
            + "|laystation portable)|nokia|fennec|htc[-_]"
            + "|mobile|up.browser|[1-4][0-9]{2}x[1-4][0-9]{2})\\b";
    private static final String tableReg = "\\b(ipad|tablet|(Nexus 7)|up.browser"
            + "|[1-4][0-9]{2}x[1-4][0-9]{2})\\b";
    private static final Pattern phonePat = Pattern.compile(phoneReg, Pattern.CASE_INSENSITIVE);
    private static final Pattern tablePat = Pattern.compile(tableReg, Pattern.CASE_INSENSITIVE);

    public static boolean isMobile(ApiRequest req) {
        String userAgent = req.getHeader("USER-AGENT");
        if (userAgent == null) {
            userAgent = "";
        }
        userAgent = userAgent.toLowerCase();
        Matcher matcherPhone = phonePat.matcher(userAgent);
        Matcher matcherTable = tablePat.matcher(userAgent);
        return matcherPhone.find() || matcherTable.find();
    }
}
