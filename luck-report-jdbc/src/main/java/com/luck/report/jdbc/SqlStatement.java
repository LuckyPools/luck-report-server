package com.luck.report.jdbc;

import com.luck.report.jdbc.node.SqlNode;

import java.util.List;

/**
 * 一条 mapper 语句
 */
public class SqlStatement {

    public enum StatementType {
        SELECT, INSERT, UPDATE, DELETE
    }

    private final String id;
    private final StatementType type;
    private final List<SqlNode> nodes;
    private final boolean dynamic;
    private volatile String cachedSql;

    /**
     * 构造语句对象
     *
     * @param id    namespace.id
     * @param type  语句类型
     * @param nodes AST 子节点
     */
    public SqlStatement(String id, StatementType type, List<SqlNode> nodes) {
        this.id = id;
        this.type = type;
        this.nodes = nodes;
        boolean dyn = false;
        for (SqlNode node : nodes) {
            if (node.isDynamic()) {
                dyn = true;
                break;
            }
        }
        this.dynamic = dyn;
    }

    /**
     * 按参数渲染 SQL
     *
     * @param params 参数 Map，可为 null
     * @return 渲染结果
     */
    public RenderedSql render(java.util.Map<String, Object> params) {
        if (!dynamic && cachedSql != null) {
            RenderContext context = new RenderContext(params);
            // 静态结构仍需绑定参数值
            StringBuilder sql = new StringBuilder();
            for (SqlNode node : nodes) {
                node.render(context, sql);
            }
            return new RenderedSql(sql.toString().trim(), context.getParameterSource());
        }
        RenderContext context = new RenderContext(params);
        StringBuilder sql = new StringBuilder();
        for (SqlNode node : nodes) {
            node.render(context, sql);
        }
        String rendered = sql.toString().trim();
        if (!dynamic) {
            cachedSql = rendered;
        }
        return new RenderedSql(rendered, context.getParameterSource());
    }

    /**
     * 获取语句完整 id
     *
     * @return namespace.id
     */
    public String getId() {
        return id;
    }

    /**
     * 获取语句类型
     *
     * @return 类型
     */
    public StatementType getType() {
        return type;
    }

    /**
     * 是否含动态标签
     *
     * @return 动态则 true
     */
    public boolean isDynamic() {
        return dynamic;
    }
}
