package com.luck.report.core.expression;

import com.luck.report.core.expression.model.Expression;
import com.luck.report.core.expression.model.expr.ExpressionBlock;
import com.luck.report.core.expression.model.expr.ReturnExpression;
import com.luck.report.core.expression.model.expr.ifelse.IfExpression;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpressionReturnParseTest {

    @Test
    void top_level_return_is_ReturnExpression() {
        Expression expr = ExpressionUtils.parseExpression("return 5;");
        assertTrue(expr instanceof ExpressionBlock);
        ExpressionBlock block = (ExpressionBlock) expr;
        assertTrue(block.getExpressionList().get(0) instanceof ReturnExpression);
    }

    @Test
    void if_block_return_keyword_wraps_returnExpression() {
        Expression expr = ExpressionUtils.parseExpression("if (1==1) { return 100; }");
        ExpressionBlock root = (ExpressionBlock) expr;
        IfExpression ifExpr = (IfExpression) root.getExpressionList().get(0);
        assertNotNull(ifExpr.getExpression());
        assertTrue(ifExpr.getExpression().getReturnExpression() instanceof ReturnExpression);
    }

    @Test
    void if_block_bare_trailing_expr_is_not_ReturnExpression() {
        Expression expr = ExpressionUtils.parseExpression("if (1==1) { 100; }");
        ExpressionBlock root = (ExpressionBlock) expr;
        IfExpression ifExpr = (IfExpression) root.getExpressionList().get(0);
        assertNotNull(ifExpr.getExpression().getReturnExpression());
        assertTrue(!(ifExpr.getExpression().getReturnExpression() instanceof ReturnExpression));
    }
}
