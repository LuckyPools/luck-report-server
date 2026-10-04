package com.luck.report.core.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * 静态数据集工具；按查询参数对行做内存过滤，参数名对齐 JSON 字段名，空值与内置参数跳过
 *
 * @author luck-report
 * @since 2.0.7
 */
public final class StaticDatasetUtils {

    private StaticDatasetUtils() {
    }

    /**
     * 按 parameters 过滤行；无有效条件时返回原列表的拷贝
     *
     * @param rows 静态行，可为 null
     * @param parameters 查询参数，可为 null
     * @return 过滤后的新 List，不会返回 null
     */
    public static List<Map<String, Object>> filter(List<Map<String, Object>> rows,
                                                   Map<String, Object> parameters) {
        if (rows == null || rows.isEmpty()) {
            return rows == null ? new ArrayList<>() : new ArrayList<>(rows);
        }
        Map<String, Object> filters = buildFilters(rows, parameters);
        if (filters.isEmpty()) {
            return new ArrayList<>(rows);
        }
        List<Map<String, Object>> out = new ArrayList<Map<String, Object>>();
        for (Map<String, Object> row : rows) {
            if (row != null && matches(row, filters)) {
                out.add(row);
            }
        }
        return out;
    }

    private static Map<String, Object> buildFilters(List<Map<String, Object>> rows,
                                                    Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<String> presentKeys = new HashSet<String>();
        for (Map<String, Object> row : rows) {
            if (row == null) {
                continue;
            }
            presentKeys.addAll(row.keySet());
        }
        Map<String, Object> filters = new HashMap<String, Object>();
        for (Map.Entry<String, Object> e : parameters.entrySet()) {
            String name = e.getKey();
            if (name == null || name.isEmpty()) {
                continue;
            }
            if (name.startsWith("_") || name.startsWith("luck_")) {
                continue;
            }
            Object value = e.getValue();
            if (value == null) {
                continue;
            }
            String text = Objects.toString(value, "").trim();
            if (text.isEmpty()) {
                continue;
            }
            if (!presentKeys.contains(name)) {
                continue;
            }
            filters.put(name, text);
        }
        return filters;
    }

    private static boolean matches(Map<String, Object> row, Map<String, Object> filters) {
        for (Map.Entry<String, Object> e : filters.entrySet()) {
            if (!valueMatches(row.get(e.getKey()), (String) e.getValue())) {
                return false;
            }
        }
        return true;
    }

    private static boolean valueMatches(Object actual, String expected) {
        if (actual == null) {
            return false;
        }
        String actualText = Objects.toString(actual, "");
        if (expected.indexOf(',') >= 0) {
            String[] parts = expected.split(",");
            for (String part : parts) {
                if (actualText.equals(part.trim())) {
                    return true;
                }
            }
            return false;
        }
        return actualText.equals(expected);
    }
}
