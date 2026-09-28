package com.luck.report.jdbc;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * 渲染后的 SQL 与命名参数
 */
public class RenderedSql {

    private final String sql;
    private final MapSqlParameterSource parameterSource;

    /**
     * 构造渲染结果
     *
     * @param sql             SQL 文本
     * @param parameterSource 命名参数
     */
    public RenderedSql(String sql, MapSqlParameterSource parameterSource) {
        this.sql = sql;
        this.parameterSource = parameterSource;
    }

    /**
     * 获取 SQL 文本
     *
     * @return SQL
     */
    public String getSql() {
        return sql;
    }

    /**
     * 获取命名参数源
     *
     * @return 参数源
     */
    public MapSqlParameterSource getParameterSource() {
        return parameterSource;
    }
}
