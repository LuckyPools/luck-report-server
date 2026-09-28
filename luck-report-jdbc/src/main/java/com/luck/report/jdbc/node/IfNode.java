package com.luck.report.jdbc.node;

import com.luck.report.jdbc.RenderContext;
import com.luck.report.jdbc.SimpleTestExpression;

import java.util.List;

/**
 * &lt;if test&gt; 节点
 */
public class IfNode implements SqlNode {

    private final String test;
    private final List<SqlNode> children;

    /**
     * 构造 if 节点
     *
     * @param test     条件表达式
     * @param children 子节点
     */
    public IfNode(String test, List<SqlNode> children) {
        this.test = test;
        this.children = children;
    }

    @Override
    public void render(RenderContext context, StringBuilder sql) {
        if (SimpleTestExpression.evaluate(test, context)) {
            for (SqlNode child : children) {
                child.render(context, sql);
            }
        }
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
