package com.luck.report.jdbc.node;

import com.luck.report.jdbc.RenderContext;
import com.luck.report.jdbc.SimpleTestExpression;

import java.util.List;

/**
 * &lt;choose&gt; 节点
 */
public class ChooseNode implements SqlNode {

    private final List<WhenBranch> whens;
    private final List<SqlNode> otherwise;

    /**
     * 构造 choose 节点
     *
     * @param whens     when 分支
     * @param otherwise otherwise 子节点，可为 null
     */
    public ChooseNode(List<WhenBranch> whens, List<SqlNode> otherwise) {
        this.whens = whens;
        this.otherwise = otherwise;
    }

    @Override
    public void render(RenderContext context, StringBuilder sql) {
        for (WhenBranch when : whens) {
            if (SimpleTestExpression.evaluate(when.getTest(), context)) {
                for (SqlNode child : when.getChildren()) {
                    child.render(context, sql);
                }
                return;
            }
        }
        if (otherwise != null) {
            for (SqlNode child : otherwise) {
                child.render(context, sql);
            }
        }
    }

    @Override
    public boolean isDynamic() {
        return true;
    }
}
