package com.luck.report.core.expression;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luck.report.core.definition.value.ExpressionValue;
import com.luck.report.core.expression.model.Expression;
import com.luck.report.core.expression.model.condition.BaseCondition;
import com.luck.report.core.expression.model.condition.Join;
import com.luck.report.core.expression.model.expr.ExpressionBlock;
import com.luck.report.core.expression.model.expr.JoinExpression;
import com.luck.report.core.expression.model.expr.dataset.DatasetExpression;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatasetGroupConditionParseTest {

    @Test
    void group_with_id_gt_100_parses_condition() {
        DatasetExpression de = findDataset(ExpressionUtils.parseExpression("t_shop.group(name, id > 100)"));
        assertEquals("name", de.getProperty());
        assertNotNull(de.getCondition());
        BaseCondition bc = (BaseCondition) de.getCondition();
        assertEquals("id", bc.getLeft());
        assertEquals("100", bc.getRight());
        assertEquals(1, de.getConditions().size());
    }

    @Test
    void dataset_expression_rebuilds_condition_from_conditions_after_jackson() throws Exception {
        DatasetExpression original = findDataset(
                ExpressionUtils.parseExpression("t_shop.group(name, id > 100 and id < 200)"));
        assertEquals(2, original.getConditions().size());
        assertEquals(Join.and, ((BaseCondition) original.getCondition()).getNextJoin());

        ObjectMapper mapper = redisMapper();
        DatasetExpression restored = mapper.readValue(mapper.writeValueAsString(original), DatasetExpression.class);
        BaseCondition head = (BaseCondition) restored.getCondition();
        assertNotNull(head);
        assertEquals("id", head.getLeft());
        assertEquals("100", head.getRight());
        assertEquals(Join.and, head.getNextJoin());
        assertEquals("200", ((BaseCondition) head.getNextCondition()).getRight());
    }

    @Test
    void expression_value_keeps_condition_after_jackson_round_trip() throws Exception {
        ExpressionValue original = new ExpressionValue("t_shop.group(name, id > 100)");
        assertNotNull(findDataset(original.getExpression()).getCondition());

        ObjectMapper mapper = redisMapper();

        ExpressionValue restored = mapper.readValue(mapper.writeValueAsString(original), ExpressionValue.class);
        DatasetExpression de = findDataset(restored.getExpression());
        assertNotNull(de.getCondition(), "condition must survive Redis/Jackson cache round-trip");
        BaseCondition bc = (BaseCondition) de.getCondition();
        assertEquals("id", bc.getLeft());
        assertEquals("100", bc.getRight());
    }

    private static DatasetExpression findDataset(Expression expr) {
        assertNotNull(expr);
        if (expr instanceof ExpressionBlock) {
            return findDataset(((ExpressionBlock) expr).getExpressionList().get(0));
        }
        if (expr instanceof JoinExpression) {
            JoinExpression join = (JoinExpression) expr;
            assertTrue(join.getExpressions() != null && !join.getExpressions().isEmpty());
            return findDataset(join.getExpressions().get(0));
        }
        assertTrue(expr instanceof DatasetExpression, "unexpected: " + expr.getClass().getName());
        return (DatasetExpression) expr;
    }

    private static ObjectMapper redisMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.activateDefaultTyping(
                mapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY);
        return mapper;
    }
}
