package com.luck.report.jdbc.node;

import com.luck.report.jdbc.RenderContext;

/**
 * SQL AST 节点
 */
public interface SqlNode {

    /**
     * 将节点渲染到 SQL 缓冲
     *
     * @param context 渲染上下文
     * @param sql     SQL 缓冲
     */
    void render(RenderContext context, StringBuilder sql);

    /**
     * 是否包含动态逻辑（影响静态缓存）
     *
     * @return 动态则 true
     */
    boolean isDynamic();
}
