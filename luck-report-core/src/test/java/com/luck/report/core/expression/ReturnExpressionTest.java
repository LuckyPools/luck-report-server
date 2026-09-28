package com.luck.report.core.expression;

import com.luck.report.core.build.Context;
import com.luck.report.core.expression.model.data.ExpressionData;
import com.luck.report.core.expression.model.data.ObjectExpressionData;
import com.luck.report.core.expression.model.data.ReturnedExpressionData;
import com.luck.report.core.expression.model.expr.IntegerExpression;
import com.luck.report.core.expression.model.expr.ReturnExpression;
import com.luck.report.core.model.Cell;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReturnExpressionTest {

    @Test
    void compute_wraps_inner_as_returned() {
        ReturnExpression ret = new ReturnExpression();
        ret.setExpression(new IntegerExpression(42));
        Cell cell = new Cell();
        Context ctx = new Context(null, new HashMap<String, Object>());
        ExpressionData<?> data = ret.execute(cell, cell, ctx);
        assertTrue(data instanceof ReturnedExpressionData);
        assertEquals(42, ((ReturnedExpressionData) data).unwrap().getData());
    }

    @Test
    void compute_does_not_double_wrap() {
        ReturnExpression inner = new ReturnExpression();
        inner.setExpression(new IntegerExpression(1));
        ReturnExpression outer = new ReturnExpression();
        outer.setExpression(inner);
        Cell cell = new Cell();
        Context ctx = new Context(null, new HashMap<String, Object>());
        ExpressionData<?> data = outer.execute(cell, cell, ctx);
        assertTrue(data instanceof ReturnedExpressionData);
        ExpressionData<?> unwrapped = ((ReturnedExpressionData) data).unwrap();
        assertTrue(unwrapped instanceof ObjectExpressionData);
        assertEquals(1, unwrapped.getData());
    }
}
