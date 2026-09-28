package com.luck.report.jdbc.node;

import com.luck.report.jdbc.BindValueEvaluator;
import com.luck.report.jdbc.RenderContext;

/**
 * &lt;bind&gt; 节点
 */
public class BindNode implements SqlNode {

    private final String name;
    private final String valueExpression;

    /**
     * 构造 bind 节点
     *
     * @param name            绑定变量名
     * @param valueExpression 值表达式，如 '%' + queryDTO.name + '%'
     */
    public BindNode(String name, String valueExpression) {
        this.name = name;
        this.valueExpression = valueExpression;
    }

    @Override
    public void render(RenderContext context, StringBuilder sql) {
        Object value = BindValueEvaluator.evaluate(valueExpression, context);
        context.putBinding(name, value);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
