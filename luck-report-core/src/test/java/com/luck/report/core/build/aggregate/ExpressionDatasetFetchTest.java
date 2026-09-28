package com.luck.report.core.build.aggregate;

import com.luck.report.core.definition.value.ExpressionValue;
import com.luck.report.core.expression.model.Expression;
import com.luck.report.core.expression.model.expr.ExpressionBlock;
import com.luck.report.core.expression.model.expr.JoinExpression;
import com.luck.report.core.expression.model.expr.dataset.DatasetExpression;
import com.luck.report.core.utils.DataUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ExpressionDatasetFetchTest {

    @Test
    void sumExpression_isRecognizedAsDatasetParent() {
        ExpressionValue value = new ExpressionValue("t_shop.sum(amount,id > 1)");
        Expression expr = value.getExpression();
        assertNotNull(expr);
        assertEquals(ExpressionBlock.class, expr.getClass());

        DatasetExpression de = DataUtils.fetchDatasetExpression(value);
        assertNotNull(de, "fetchDatasetExpression must unwrap ExpressionBlock/ParenExpression");
        assertEquals("t_shop", de.getDatasetName());
        assertEquals("amount", de.getProperty());
        assertNotNull(de.getCondition());
    }

    @Test
    void plainGroupExpression_isRecognized() {
        ExpressionValue value = new ExpressionValue("t_shop.group(name)");
        DatasetExpression de = DataUtils.fetchDatasetExpression(value);
        assertNotNull(de);
        assertEquals("name", de.getProperty());
    }
}
