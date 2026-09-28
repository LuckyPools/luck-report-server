package com.luck.report.jdbc;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SQL 解析与渲染单测
 */
public class LuckSqlParserTest {

    @Test
    public void shouldParseIfForeachAndBind() {
        String xml = ""
                + "<mapper namespace=\"demo.DemoMapper\">"
                + "  <select id=\"query\">"
                + "    SELECT id FROM t WHERE del_flag = 0"
                + "    <if test=\"name != null and name != ''\">"
                + "      <bind name=\"namePattern\" value=\"'%' + name + '%'\" />"
                + "      AND name LIKE #{namePattern}"
                + "    </if>"
                + "    AND id IN"
                + "    <foreach collection=\"ids\" item=\"id\" open=\"(\" separator=\",\" close=\")\">"
                + "      #{id}"
                + "    </foreach>"
                + "  </select>"
                + "</mapper>";
        LuckSqlParser parser = new LuckSqlParser();
        Map<String, SqlStatement> map = parser.parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
        SqlStatement statement = map.get("demo.DemoMapper.query");

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "abc");
        params.put("ids", Arrays.asList("1", "2"));
        RenderedSql rendered = statement.render(params);

        String sql = rendered.getSql().replaceAll("\\s+", " ");
        assertTrue(sql.contains("AND name LIKE"));
        assertTrue(sql.contains("IN ("));
        assertFalse(sql.contains("#{"));
        assertEquals("%abc%", rendered.getParameterSource().getValue("__p0"));
    }

    @Test
    public void shouldSkipIfWhenFalse() {
        String xml = ""
                + "<mapper namespace=\"demo.DemoMapper\">"
                + "  <select id=\"query\">"
                + "    SELECT 1 FROM t WHERE 1=1"
                + "    <if test=\"name != null and name != ''\"> AND name = #{name} </if>"
                + "  </select>"
                + "</mapper>";
        SqlStatement statement = new LuckSqlParser().parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))
                .get("demo.DemoMapper.query");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "");
        RenderedSql rendered = statement.render(params);
        assertFalse(rendered.getSql().contains("AND name"));
    }

    @Test
    public void shouldExpandSqlInclude() {
        String xml = ""
                + "<mapper namespace=\"demo.DemoMapper\">"
                + "  <sql id=\"cols\">id, name</sql>"
                + "  <select id=\"query\">"
                + "    SELECT <include refid=\"cols\"/> FROM t WHERE id = #{id}"
                + "  </select>"
                + "</mapper>";
        SqlStatement statement = new LuckSqlParser().parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))
                .get("demo.DemoMapper.query");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("id", "1");
        RenderedSql rendered = statement.render(params);
        String sql = rendered.getSql().replaceAll("\\s+", " ");
        assertTrue(sql.contains("SELECT id, name FROM t"));
        assertFalse(sql.contains("<include"));
    }

    @Test
    public void shouldEvaluateAndWhenLeftSideIsFalse() {
        RenderContext empty = new RenderContext(Collections.<String, Object>emptyMap());
        assertFalse(SimpleTestExpression.evaluate("name != null and name != ''", empty));

        Map<String, Object> blank = new HashMap<String, Object>();
        blank.put("name", "");
        assertFalse(SimpleTestExpression.evaluate("name != null and name != ''", new RenderContext(blank)));

        Map<String, Object> named = new HashMap<String, Object>();
        named.put("name", "报表");
        assertTrue(SimpleTestExpression.evaluate("name != null and name != ''", new RenderContext(named)));
    }

    @Test
    public void whereMustNotCorruptOrderBy() {
        String xml = ""
                + "<mapper namespace=\"demo.DemoMapper\">"
                + "  <select id=\"query\">"
                + "    SELECT id FROM t"
                + "    <where>"
                + "      <if test=\"name != null\">AND name = #{name}</if>"
                + "      ORDER BY id ASC"
                + "    </where>"
                + "  </select>"
                + "</mapper>";
        SqlStatement statement = new LuckSqlParser().parse(
                new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)))
                .get("demo.DemoMapper.query");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("name", "a");
        String sql = statement.render(params).getSql().replaceAll("\\s+", " ");
        assertTrue(sql.contains("ORDER BY id ASC"), sql);
    }

    @Test
    public void rejectDollarBracePlaceholder() {
        String xml = ""
                + "<mapper namespace=\"demo.DemoMapper\">"
                + "  <select id=\"query\">SELECT * FROM t ORDER BY ${col}</select>"
                + "</mapper>";
        try {
            new LuckSqlParser().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            throw new AssertionError("expected failure");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            assertTrue(ex.getMessage().contains("${"));
        }
    }

    @Test
    public void rejectDuplicateStatementId() {
        String xml = ""
                + "<mapper namespace=\"demo.DemoMapper\">"
                + "  <select id=\"query\">SELECT 1</select>"
                + "  <select id=\"query\">SELECT 2</select>"
                + "</mapper>";
        try {
            new LuckSqlParser().parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
            throw new AssertionError("expected failure");
        } catch (IllegalArgumentException ex) {
            assertTrue(ex.getMessage().contains("Duplicate SQL id"));
        }
    }
}
