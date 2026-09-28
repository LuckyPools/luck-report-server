package com.luck.report.core.expression.function;

import com.luck.report.core.expression.model.data.ExpressionData;
import com.luck.report.core.expression.model.data.ObjectExpressionData;
import com.luck.report.core.expression.model.data.ObjectListExpressionData;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * order() 对含多个 null 的列表排序时，比较器必须满足 TimSort 契约。
 */
class OrderFunctionNullTest {

    private static final Integer[] NAMES = {
            25, 2, 3, 18, null, 10, 1, 22, 15, 27, null, null, 19, 23, 31, 9,
            17, null, 26, 30, null, null, 11, 14, 6, 7, 13, 29, null, 21, null, 5
    };

    @Test
    void asc_nullsSortFirstWithoutContractError() {
        List<Object> result = sort(Boolean.TRUE);
        assertEquals(NAMES.length, result.size());
        for (int i = 0; i < 8; i++) {
            assertNull(result.get(i));
        }
        assertEquals(1, ((Number) result.get(8)).intValue());
        assertEquals(31, ((Number) result.get(result.size() - 1)).intValue());
    }

    @Test
    void desc_nullsStaySmallest() {
        List<Object> result = sort(Boolean.FALSE);
        assertNull(result.get(0));
        assertEquals(31, ((Number) result.get(8)).intValue());
        assertEquals(1, ((Number) result.get(result.size() - 1)).intValue());
    }

    @SuppressWarnings("unchecked")
    private static List<Object> sort(Boolean asc) {
        List<Object> src = new ArrayList<Object>(Arrays.asList(NAMES));
        List<ExpressionData<?>> args = Arrays.<ExpressionData<?>>asList(
                new ObjectListExpressionData(src),
                new ObjectExpressionData(asc)
        );
        return (List<Object>) new OrderFunction().execute(args, null, null);
    }
}
