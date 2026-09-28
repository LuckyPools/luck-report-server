package com.luck.report.core.expression.parse;

/**
 * 表达式语法错误位点，供错误文案格式化使用。
 */
public class ExpressionSyntaxError {

    private final int line;

    private final int charPositionInLine;

    private final String tokenText;

    private final String rawMessage;

    /**
     * 记录一条 ANTLR 语法错误
     *
     * @param line 行号，从 1 开始
     * @param charPositionInLine 列偏移，从 0 开始
     * @param tokenText 出错 token 文本，可为空
     * @param rawMessage ANTLR 原始说明，可为空
     */
    public ExpressionSyntaxError(int line, int charPositionInLine, String tokenText, String rawMessage) {
        this.line = line;
        this.charPositionInLine = charPositionInLine;
        this.tokenText = tokenText;
        this.rawMessage = rawMessage;
    }

    public int getLine() {
        return line;
    }

    public int getCharPositionInLine() {
        return charPositionInLine;
    }

    public String getTokenText() {
        return tokenText;
    }

    public String getRawMessage() {
        return rawMessage;
    }
}
