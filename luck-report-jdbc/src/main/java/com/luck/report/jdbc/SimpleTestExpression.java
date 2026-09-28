package com.luck.report.jdbc;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 有限子集的 &lt;if test&gt; 表达式求值
 */
public final class SimpleTestExpression {

    private static final Pattern TOKEN = Pattern.compile(
            "\\s+|(\\()|(\\))|(and)|(or)|(!=)|(==)|('[^']*')|([a-zA-Z_][\\w.]*)|(null)",
            Pattern.CASE_INSENSITIVE);

    private SimpleTestExpression() {
    }

    /**
     * 求值 if test 表达式
     *
     * @param expression 表达式，如 queryDTO.name != null and queryDTO.name != ''
     * @param context    渲染上下文
     * @return 条件是否成立
     */
    public static boolean evaluate(String expression, RenderContext context) {
        if (expression == null || expression.trim().isEmpty()) {
            return false;
        }
        List<String> tokens = tokenize(expression.trim());
        int[] idx = new int[]{0};
        boolean result = parseOr(tokens, idx, context);
        if (idx[0] != tokens.size()) {
            throw new IllegalArgumentException("Unexpected token in test expression: " + expression);
        }
        return result;
    }

    private static List<String> tokenize(String expression) {
        List<String> tokens = new ArrayList<String>();
        Matcher matcher = TOKEN.matcher(expression);
        int pos = 0;
        while (matcher.find()) {
            if (matcher.start() != pos) {
                throw new IllegalArgumentException("Unsupported test expression near: " + expression.substring(pos));
            }
            pos = matcher.end();
            String t = matcher.group();
            if (!t.trim().isEmpty()) {
                tokens.add(t);
            }
        }
        if (pos != expression.length()) {
            throw new IllegalArgumentException("Unsupported test expression: " + expression);
        }
        return tokens;
    }

    private static boolean parseOr(List<String> tokens, int[] idx, RenderContext context) {
        boolean result = parseAnd(tokens, idx, context);
        while (idx[0] < tokens.size() && "or".equalsIgnoreCase(tokens.get(idx[0]))) {
            idx[0]++;
            // 必须先解析右侧，不能用 || 短路，否则剩余 token 会被当成非法表达式
            boolean right = parseAnd(tokens, idx, context);
            result = result || right;
        }
        return result;
    }

    private static boolean parseAnd(List<String> tokens, int[] idx, RenderContext context) {
        boolean result = parsePrimary(tokens, idx, context);
        while (idx[0] < tokens.size() && "and".equalsIgnoreCase(tokens.get(idx[0]))) {
            idx[0]++;
            boolean right = parsePrimary(tokens, idx, context);
            result = result && right;
        }
        return result;
    }

    private static boolean parsePrimary(List<String> tokens, int[] idx, RenderContext context) {
        if (idx[0] >= tokens.size()) {
            throw new IllegalArgumentException("Unexpected end of test expression");
        }
        if ("(".equals(tokens.get(idx[0]))) {
            idx[0]++;
            boolean inner = parseOr(tokens, idx, context);
            if (idx[0] >= tokens.size() || !")".equals(tokens.get(idx[0]))) {
                throw new IllegalArgumentException("Unclosed '(' in test expression");
            }
            idx[0]++;
            return inner;
        }
        Object left = resolveOperand(tokens.get(idx[0]), context);
        idx[0]++;
        if (idx[0] >= tokens.size()) {
            return isTruthy(left);
        }
        String op = tokens.get(idx[0]);
        if (!"!=".equals(op) && !"==".equals(op)) {
            return isTruthy(left);
        }
        idx[0]++;
        if (idx[0] >= tokens.size()) {
            throw new IllegalArgumentException("Missing right operand in test expression");
        }
        Object right = resolveOperand(tokens.get(idx[0]), context);
        idx[0]++;
        if ("!=".equals(op)) {
            return !equalsValue(left, right);
        }
        return equalsValue(left, right);
    }

    private static Object resolveOperand(String token, RenderContext context) {
        if ("null".equalsIgnoreCase(token)) {
            return null;
        }
        if (token.length() >= 2 && token.startsWith("'") && token.endsWith("'")) {
            return token.substring(1, token.length() - 1);
        }
        return context.getValue(token);
    }

    private static boolean equalsValue(Object left, Object right) {
        if (left == null && right == null) {
            return true;
        }
        if (left == null || right == null) {
            return false;
        }
        return String.valueOf(left).equals(String.valueOf(right));
    }

    private static boolean isTruthy(Object value) {
        if (value == null) {
            return false;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return !((String) value).isEmpty();
        }
        return true;
    }
}
