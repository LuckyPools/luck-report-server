package com.luck.report.jdbc;

import com.luck.report.jdbc.dialect.DialectFactory;
import com.luck.report.jdbc.dialect.IPageDialect;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.support.DataAccessUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.util.List;
import java.util.Map;

/**
 * 基于 NamedParameterJdbcTemplate 的 SQL 执行器
 */
public class LuckSqlExecutor {

    private final LuckSqlRegistry registry;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final DialectFactory dialectFactory;
    private final IPageDialect pageDialect;

    /**
     * 构造执行器
     *
     * @param registry        SQL 注册表
     * @param jdbcTemplate    命名参数 JDBC
     * @param dialectFactory  方言工厂
     * @param pageDialect     当前库分页方言，可为 null（禁用分页改写）
     */
    public LuckSqlExecutor(LuckSqlRegistry registry,
                           NamedParameterJdbcTemplate jdbcTemplate,
                           DialectFactory dialectFactory,
                           IPageDialect pageDialect) {
        this.registry = registry;
        this.jdbcTemplate = jdbcTemplate;
        this.dialectFactory = dialectFactory;
        this.pageDialect = pageDialect;
    }

    /**
     * 查询单行并映射为 Bean
     *
     * @param statementId 语句 id
     * @param params      参数
     * @param type        结果类型
     * @param <T>         结果泛型
     * @return 实体；无行时返回 null
     */
    public <T> T selectOne(String statementId, Map<String, Object> params, Class<T> type) {
        List<T> list = selectList(statementId, params, type);
        return DataAccessUtils.singleResult(list);
    }

    /**
     * 查询列表并映射为 Bean
     *
     * @param statementId 语句 id
     * @param params      参数
     * @param type        元素类型
     * @param <T>         元素泛型
     * @return 列表，非 null
     */
    public <T> List<T> selectList(String statementId, Map<String, Object> params, Class<T> type) {
        RenderedSql rendered = registry.getRequired(statementId).render(params);
        if (isSimpleType(type)) {
            return jdbcTemplate.query(rendered.getSql(), rendered.getParameterSource(),
                    new SingleColumnRowMapper<T>(type));
        }
        return jdbcTemplate.query(rendered.getSql(), rendered.getParameterSource(),
                BeanPropertyRowMapper.newInstance(type));
    }

    /**
     * 查询单行并用自定义 RowMapper 映射
     *
     * @param statementId 语句 id
     * @param params      参数
     * @param rowMapper   行映射器
     * @param <T>         结果泛型
     * @return 实体；无行时返回 null
     */
    public <T> T selectOne(String statementId, Map<String, Object> params, RowMapper<T> rowMapper) {
        List<T> list = selectList(statementId, params, rowMapper);
        return DataAccessUtils.singleResult(list);
    }

    /**
     * 查询列表并用自定义 RowMapper 映射
     *
     * @param statementId 语句 id
     * @param params      参数
     * @param rowMapper   行映射器
     * @param <T>         元素泛型
     * @return 列表，非 null
     */
    public <T> List<T> selectList(String statementId, Map<String, Object> params, RowMapper<T> rowMapper) {
        RenderedSql rendered = registry.getRequired(statementId).render(params);
        return jdbcTemplate.query(rendered.getSql(), rendered.getParameterSource(), rowMapper);
    }

    /**
     * 分页查询（对方言包装 LIMIT/OFFSET）
     *
     * @param statementId 语句 id（SQL 本身不应含分页子句更佳）
     * @param params      参数
     * @param offset      偏移
     * @param pageSize    页大小
     * @param type        元素类型
     * @param <T>         元素泛型
     * @return 列表
     */
    public <T> List<T> selectListPaged(String statementId, Map<String, Object> params,
                                       long offset, long pageSize, Class<T> type) {
        RenderedSql rendered = registry.getRequired(statementId).render(params);
        String sql = rendered.getSql();
        if (pageDialect != null) {
            sql = pageDialect.buildPaginationSql(sql, offset, pageSize);
        }
        if (isSimpleType(type)) {
            return jdbcTemplate.query(sql, rendered.getParameterSource(),
                    new SingleColumnRowMapper<T>(type));
        }
        return jdbcTemplate.query(sql, rendered.getParameterSource(),
                BeanPropertyRowMapper.newInstance(type));
    }

    /**
     * 执行 select 并取单一标量（如 COUNT）
     *
     * @param statementId 语句 id
     * @param params      参数
     * @param type        标量类型
     * @param <T>         标量泛型
     * @return 标量；无行时返回 null
     */
    public <T> T selectScalar(String statementId, Map<String, Object> params, Class<T> type) {
        RenderedSql rendered = registry.getRequired(statementId).render(params);
        try {
            return jdbcTemplate.queryForObject(rendered.getSql(), rendered.getParameterSource(), type);
        } catch (EmptyResultDataAccessException ex) {
            return null;
        }
    }

    /**
     * 执行 insert/update/delete
     *
     * @param statementId 语句 id
     * @param params      参数
     * @return 影响行数
     */
    public int update(String statementId, Map<String, Object> params) {
        RenderedSql rendered = registry.getRequired(statementId).render(params);
        return jdbcTemplate.update(rendered.getSql(), rendered.getParameterSource());
    }

    /**
     * 获取方言工厂
     *
     * @return 方言工厂
     */
    public DialectFactory getDialectFactory() {
        return dialectFactory;
    }

    /**
     * 获取当前分页方言
     *
     * @return 分页方言，可能为 null
     */
    public IPageDialect getPageDialect() {
        return pageDialect;
    }

    /**
     * 获取底层 NamedParameterJdbcTemplate
     *
     * @return JDBC 模板
     */
    public NamedParameterJdbcTemplate getJdbcTemplate() {
        return jdbcTemplate;
    }

    /**
     * 按 id 获取已注册语句
     *
     * @param statementId namespace.id
     * @return 语句定义
     */
    public SqlStatement getStatement(String statementId) {
        return registry.getRequired(statementId);
    }

    private static boolean isSimpleType(Class<?> type) {
        return type == String.class
                || type == Integer.class || type == int.class
                || type == Long.class || type == long.class
                || type == Boolean.class || type == boolean.class
                || type == Double.class || type == double.class
                || Number.class.isAssignableFrom(type);
    }
}
