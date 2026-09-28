package com.luck.report.core.expression;

import com.luck.report.core.build.Context;
import com.luck.report.core.expression.model.Expression;
import com.luck.report.core.expression.model.data.ExpressionData;
import com.luck.report.core.expression.utils.ExpressionReturns;
import com.luck.report.core.model.Cell;
import com.luck.report.core.utils.NumberUtils;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExpressionReturnEvalTest {

    private static Object eval(String text) {
        Expression expr = ExpressionUtils.parseExpression(text);
        Cell cell = new Cell();
        Context ctx = new Context(null, new HashMap<String, Object>());
        ExpressionData<?> data = ExpressionReturns.unwrap(expr.execute(cell, cell, ctx));
        return data == null ? null : data.getData();
    }

    private static int asInt(Object obj) {
        return ((Number) obj).intValue();
    }

    @Test
    void return_inside_if_stops_outer() {
        assertEquals(100, asInt(eval("if (1==1) { return 100; } return 200;")));
    }

    @Test
    void false_if_falls_through_to_later_return() {
        assertEquals(200, asInt(eval("if (1==0) { return 100; } return 200;")));
    }

    @Test
    void return_in_if_skips_trailing_bare_expr() {
        assertEquals(100, asInt(eval("if (1==1) { return 100; } else { return 200; } 300;")));
    }

    @Test
    void return_with_variable() {
        assertEquals(10, asInt(eval("a=1; if (a>0) { return a*10; } return 0;")));
    }

    @Test
    void bare_block_value_does_not_global_return() {
        assertEquals(200, asInt(eval("if (1==1) { 100; } 200;")));
    }

    @Test
    void top_level_return() {
        assertEquals(5, asInt(eval("return 5;")));
    }

    @Test
    void decimal_literal_keeps_scale() {
        BigDecimal expected = new BigDecimal("1234567.89");
        assertEquals(0, expected.compareTo((BigDecimal) eval("return 1234567.89;")));
        assertEquals("1234567.89", NumberUtils.toPlainString(eval("1234567.89")));
    }
}
