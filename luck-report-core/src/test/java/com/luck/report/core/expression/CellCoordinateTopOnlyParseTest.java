package com.luck.report.core.expression;

import com.luck.report.core.expression.model.Expression;
import com.luck.report.core.expression.model.expr.ExpressionBlock;
import com.luck.report.core.expression.model.expr.JoinExpression;
import com.luck.report.core.expression.model.expr.set.CellCoordinateExpression;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 只写上父格的单元格坐标（如 B3[;B2]）应能解析，且左边坐标为空。
 */
class CellCoordinateTopOnlyParseTest {

    /**
     * 解析仅含上父格的坐标表达式
     *
     * @return 无
     */
    @Test
    void parsesTopOnlyCoordinate() {
        CellCoordinateExpression coord = parseCoordinate("B3[;B2]");
        assertNull(coord.getLeftCoordinate());
        assertNotNull(coord.getTopCoordinate());
        assertNotNull(coord.getTopCoordinate().getCellCoordinates());
    }

    /**
     * 解析左右父格都写的坐标表达式
     *
     * @return 无
     */
    @Test
    void parsesLeftAndTopCoordinate() {
        CellCoordinateExpression coord = parseCoordinate("B3[A3;B2]");
        assertNotNull(coord.getLeftCoordinate());
        assertNotNull(coord.getTopCoordinate());
    }

    /**
     * 解析仅含左父格的坐标表达式
     *
     * @return 无
     */
    @Test
    void parsesLeftOnlyCoordinate() {
        CellCoordinateExpression coord = parseCoordinate("B3[A3]");
        assertNotNull(coord.getLeftCoordinate());
        assertNull(coord.getTopCoordinate());
    }

    /**
     * 解析带条件的仅上父格坐标表达式
     *
     * @return 无
     */
    @Test
    void parsesTopOnlyCoordinateWithCondition() {
        CellCoordinateExpression coord = parseCoordinate("B3[;B2]{B3>0}");
        assertNull(coord.getLeftCoordinate());
        assertNotNull(coord.getTopCoordinate());
        assertNotNull(coord.getCondition());
    }

    /**
     * 从解析结果中取出单元格坐标表达式
     *
     * @param text 表达式原文，不可为空
     * @return 坐标表达式，不可为空
     */
    private static CellCoordinateExpression parseCoordinate(String text) {
        Expression expr = ExpressionUtils.parseExpression(text);
        while (expr instanceof ExpressionBlock) {
            expr = ((ExpressionBlock) expr).getExpressionList().get(0);
        }
        while (expr instanceof JoinExpression) {
            expr = ((JoinExpression) expr).getExpressions().get(0);
        }
        return assertInstanceOf(CellCoordinateExpression.class, expr);
    }
}
