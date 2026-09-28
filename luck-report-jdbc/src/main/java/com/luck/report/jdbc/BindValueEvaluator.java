package com.luck.report.jdbc;

import java.util.ArrayList;
import java.util.List;

/**
 * &lt;bind value&gt; 简单表达式：字符串字面量、属性路径、+ 拼接
 */
public final class BindValueEvaluator {

    private BindValueEvaluator() {
    }

    /**
     * 求值 bind 的 value 表达式
     *
     * @param expression 如 '%' + queryDTO.name + '%'
     * @param context    渲染上下文
     * @return 求值结果
     */
    public static Object evaluate(String expression, RenderContext context) {
        if (expression == null) {
            return null;
        }
        String expr = expression.trim();
        List<String> parts = splitByPlus(expr);
        if (parts.size() == 1) {
            return resolvePart(parts.get(0).trim(), context);
        }
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            Object value = resolvePart(part.trim(), context);
            if (value != null) {
                sb.append(value);
            }
        }
        return sb.toString();
    }

    private static List<String> splitByPlus(String expression) {
        List<String> parts = new ArrayList<String>();
        StringBuilder current = new StringBuilder();
        boolean inQuote = false;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '\'') {
                inQuote = !inQuote;
                current.append(c);
            } else if (c == '+' && !inQuote) {
                parts.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        parts.add(current.toString());
        return parts;
    }

    private static Object resolvePart(String token, RenderContext context) {
        if (token.isEmpty()) {
            return "";
        }
        if (token.length() >= 2 && token.startsWith("'") && token.endsWith("'")) {
            return token.substring(1, token.length() - 1);
        }
        return context.getValue(token);
    }
}
