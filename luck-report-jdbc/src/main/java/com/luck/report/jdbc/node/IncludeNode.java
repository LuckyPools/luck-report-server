package com.luck.report.jdbc.node;

import com.luck.report.jdbc.RenderContext;

import java.util.List;

/**
 * &lt;include&gt; 展开后的片段节点（解析期已内联为子节点列表）
 */
public class IncludeNode implements SqlNode {

    private final List<SqlNode> children;

    /**
     * 构造 include 展开节点
     *
     * @param children 片段子节点
     */
    public IncludeNode(List<SqlNode> children) {
        this.children = children;
    }

    @Override
    public void render(RenderContext context, StringBuilder sql) {
        for (SqlNode child : children) {
            child.render(context, sql);
        }
    }

    @Override
    public boolean isDynamic() {
        for (SqlNode child : children) {
            if (child.isDynamic()) {
                return true;
            }
        }
        return false;
    }
}
