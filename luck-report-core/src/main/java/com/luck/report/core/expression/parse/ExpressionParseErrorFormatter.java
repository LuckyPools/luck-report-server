package com.luck.report.core.expression.parse;

import com.luck.report.core.exception.ReportParseException;

import java.util.List;

/**
 * 把表达式语法错误转成带文案编码的异常。只使用首条错误，后续连锁报错忽略。
 */
public final class ExpressionParseErrorFormatter {

    /**
     * 字符串拼接链中夹了括号表达式
     */
    public static final String CODE_CONCAT_PAREN = "error.expression.parse.concatParen";

    /**
     * 未能归类的语法错误
     */
    public static final String CODE_DEFAULT = "error.expression.parse.default";

    private static final int SNIPPET_RADIUS = 28;

    private ExpressionParseErrorFormatter() {
    }

    /**
     * 将首条语法错误格式化为解析异常
     *
     * @param source 原始表达式文本，不可为空
     * @param errors 语法错误列表，不可为空
     * @return 携带行号、列号和出错片段的解析异常
     */
    public static ReportParseException toException(String source, List<ExpressionSyntaxError> errors) {
        ExpressionSyntaxError primary = errors.get(0);
        int line = primary.getLine();
        int column = primary.getCharPositionInLine() + 1;
        String snippet = snippet(source, primary);
        if (isStringConcatWithParen(source, primary)) {
            return new ReportParseException(CODE_CONCAT_PAREN, line, column, snippet);
        }
        return new ReportParseException(CODE_DEFAULT, line, column, snippet);
    }

    /**
     * 判断是否为「字符串 + (表达式)」这类拼接写法
     *
     * @param source 原始表达式文本，不可为空
     * @param error 首条语法错误，不可为空
     * @return true 表示运算符紧跟在字符串之后，且右侧是左括号
     */
    private static boolean isStringConcatWithParen(String source, ExpressionSyntaxError error) {
        String token = error.getTokenText();
        if (!isOperator(token)) {
            return false;
        }
        String line = lineAt(source, error.getLine());
        int col = error.getCharPositionInLine();
        if (line.isEmpty() || col < 0 || col >= line.length()) {
            return false;
        }
        int ahead = col + token.length();
        while (ahead < line.length() && Character.isWhitespace(line.charAt(ahead))) {
            ahead++;
        }
        if (ahead >= line.length() || line.charAt(ahead) != '(') {
            return false;
        }
        int behind = col - 1;
        while (behind >= 0 && Character.isWhitespace(line.charAt(behind))) {
            behind--;
        }
        if (behind < 0) {
            return false;
        }
        char previous = line.charAt(behind);
        return previous == '"' || previous == '\'';
    }

    /**
     * 取出出错位置附近的源码片段
     *
     * @param source 原始表达式文本，可为空
     * @param error 语法错误，不可为空
     * @return 截断后的单行片段；定位失败时返回空串
     */
    private static String snippet(String source, ExpressionSyntaxError error) {
        String line = lineAt(source, error.getLine());
        if (line.isEmpty()) {
            return error.getTokenText() == null ? "" : error.getTokenText();
        }
        int col = error.getCharPositionInLine();
        if (col < 0) {
            col = 0;
        }
        if (col > line.length()) {
            col = line.length();
        }
        int start = Math.max(0, col - SNIPPET_RADIUS);
        int end = Math.min(line.length(), col + SNIPPET_RADIUS);
        String piece = line.substring(start, end).trim();
        if (start > 0) {
            piece = "..." + piece;
        }
        if (end < line.length()) {
            piece = piece + "...";
        }
        return piece;
    }

    /**
     * 按 1-based 行号取源码行
     *
     * @param source 原始表达式文本，可为空
     * @param line 行号，从 1 开始
     * @return 去掉回车后的该行；越界时返回空串
     */
    private static String lineAt(String source, int line) {
        if (source == null || line < 1) {
            return "";
        }
        String normalized = source.replace("\r\n", "\n").replace('\r', '\n');
        String[] lines = normalized.split("\n", -1);
        int index = line - 1;
        if (index >= lines.length) {
            return "";
        }
        return lines[index];
    }

    /**
     * 判断 token 是否为算术运算符
     *
     * @param token 出错 token 文本，可为空
     * @return true 表示是 + - * / %
     */
    private static boolean isOperator(String token) {
        return "+".equals(token) || "-".equals(token) || "*".equals(token)
                || "/".equals(token) || "%".equals(token);
    }
}
