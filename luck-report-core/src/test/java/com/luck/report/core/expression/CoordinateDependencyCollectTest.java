package com.luck.report.core.expression;

import com.luck.report.core.definition.CellDefinition;
import com.luck.report.core.expression.model.Expression;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 坐标依赖串 "A1:F1" 应保留，供白名单登记。 */
class CoordinateDependencyCollectTest {

    @Test
    void keepsCoordinateDependency_whenAllSegmentsExist() {
        Expression expr = ExpressionUtils.parseExpression(
                "if(&A1==1){return 0;}else{F1 - F1[A1:-1]{B1==$B1}}");
        Map<String, CellDefinition> map = new HashMap<String, CellDefinition>();
        map.put("A1", new CellDefinition());
        map.put("B1", new CellDefinition());
        map.put("F1", new CellDefinition());

        Set<String> names = ExpressionUtils.getCellNamesFromExpression(expr, map);
        assertTrue(names.contains("A1:F1"), "should keep A1:F1 for rowChildCellNames registration");
    }

    @Test
    void dropsCoordinateDependency_whenSegmentMissing() {
        Expression expr = ExpressionUtils.parseExpression("F1[A1:-1]");
        Map<String, CellDefinition> map = new HashMap<String, CellDefinition>();
        map.put("A1", new CellDefinition());
        // F1 missing

        Set<String> names = ExpressionUtils.getCellNamesFromExpression(expr, map);
        assertFalse(names.contains("A1:F1"));
    }
}
