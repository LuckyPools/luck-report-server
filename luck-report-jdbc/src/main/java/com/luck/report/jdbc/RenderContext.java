package com.luck.report.jdbc;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.HashMap;
import java.util.Map;

/**
 * SQL 渲染上下文：参数解析与命名占位符生成
 */
public class RenderContext {

    private final Map<String, Object> bindings;
    private final MapSqlParameterSource parameterSource = new MapSqlParameterSource();
    private int paramIndex;

    /**
     * 用初始参数构造渲染上下文
     *
     * @param params 初始参数，可为 null
     */
    public RenderContext(Map<String, Object> params) {
        this.bindings = new HashMap<String, Object>();
        if (params != null) {
            this.bindings.putAll(params);
        }
    }

    /**
     * 按路径取值（支持 a.b.c）
     *
     * @param path 属性路径
     * @return 值；不存在时返回 null
     */
    public Object getValue(String path) {
        return PropertyAccessor.getValue(bindings, path);
    }

    /**
     * 写入绑定变量（供 bind 标签使用）
     *
     * @param name  变量名
     * @param value 变量值
     */
    public void putBinding(String name, Object value) {
        bindings.put(name, value);
    }

    /**
     * 为字面值分配命名参数占位符
     *
     * @param value 参数值
     * @return 形如 :__p0 的占位符（含冒号）
     */
    public String bindParam(Object value) {
        String name = "__p" + (paramIndex++);
        parameterSource.addValue(name, value);
        return ":" + name;
    }

    /**
     * 直接以指定名写入命名参数（用于集合 IN）
     *
     * @param name  参数名
     * @param value 参数值
     * @return :name
     */
    public String bindNamed(String name, Object value) {
        parameterSource.addValue(name, value);
        return ":" + name;
    }

    /**
     * 获取已绑定的命名参数源
     *
     * @return MapSqlParameterSource
     */
    public MapSqlParameterSource getParameterSource() {
        return parameterSource;
    }
}
