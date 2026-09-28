package com.luck.report.core.expression;

import com.luck.report.core.expression.model.data.ObjectExpressionData;
import com.luck.report.core.expression.model.data.ReturnedExpressionData;
import com.luck.report.core.expression.utils.ExpressionReturns;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ExpressionReturnsTest {

    @Test
    void unwrap_returns_inner_when_returned() {
        ObjectExpressionData inner = new ObjectExpressionData(100);
        ReturnedExpressionData wrapped = new ReturnedExpressionData(inner);
        assertSame(inner, ExpressionReturns.unwrap(wrapped));
        assertEquals(100, ExpressionReturns.unwrap(wrapped).getData());
    }

    @Test
    void unwrap_passthrough_when_normal() {
        ObjectExpressionData data = new ObjectExpressionData(5);
        assertSame(data, ExpressionReturns.unwrap(data));
    }
}
