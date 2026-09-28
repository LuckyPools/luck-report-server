package com.luck.report.core.expression;

import com.luck.report.core.build.Context;
import com.luck.report.core.expression.model.Condition;
import com.luck.report.core.expression.model.data.ExpressionData;
import com.luck.report.core.expression.model.expr.BaseExpression;
import com.luck.report.core.model.Cell;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** bindData 为空时单元格条件仍应可过滤（同比 B1==$B1 场景）。 */
class FilterCellsBindDataTest extends BaseExpression {

    @Override
    protected ExpressionData<?> compute(Cell cell, Cell currentCell, Context context) {
        return null;
    }

    @Test
    void filterCells_keepsMatch_whenBindDataNull() {
        Cell match = new Cell();
        match.setName("F1");
        match.setData(2983.33);
        match.setBindData(null);

        Cell other = new Cell();
        other.setName("F1");
        other.setData(3966.67);
        other.setBindData(null);

        Condition condition = new Condition() {
            @Override
            public boolean filter(Cell cell, Cell currentCell, Object obj, Context context) {
                return currentCell.getData() != null
                        && Double.parseDouble(currentCell.getData().toString()) < 3000;
            }

            @Override
            public List<String> fetchCellName() {
                return Collections.emptyList();
            }
        };

        List<Cell> filtered = filterCells(new Cell(), null, condition,
                new ArrayList<Cell>(Arrays.asList(match, other)));
        assertEquals(1, filtered.size());
        assertEquals(2983.33, filtered.get(0).getData());
    }

    @Test
    void filterCells_allowsNullBindData_whenConditionTrue() {
        Cell target = new Cell();
        target.setName("F1");
        target.setData(1);
        target.setBindData(null);

        Condition alwaysTrue = new Condition() {
            @Override
            public boolean filter(Cell cell, Cell currentCell, Object obj, Context context) {
                return true;
            }

            @Override
            public List<String> fetchCellName() {
                return Collections.emptyList();
            }
        };

        assertEquals(1, filterCells(new Cell(), null, alwaysTrue,
                Collections.singletonList(target)).size());
    }
}
