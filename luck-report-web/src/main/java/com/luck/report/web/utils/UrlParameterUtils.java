/**
 * ****************************************************************************
 */
package com.luck.report.web.utils;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * url参数解析
 *
 * @author luckyPools
 * @date 2026-05-21
 */
public class UrlParameterUtils {
    public static Map<String, Object> buildParameters(ApiRequest req) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        Enumeration<?> enumeration = req.getParameterNames();
        while (enumeration.hasMoreElements()) {
            Object obj = enumeration.nextElement();
            if (obj == null) {
                continue;
            }
            String name = obj.toString();
            String value = req.getParameter(name);
            if (name == null || value == null || name.startsWith("_")) {
                continue;
            }
            parameters.put(name, value);
        }
        return parameters;
    }
}
