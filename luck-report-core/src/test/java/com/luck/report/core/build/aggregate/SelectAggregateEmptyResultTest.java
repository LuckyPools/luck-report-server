package com.luck.report.core.build.aggregate;

import com.luck.report.core.build.BindData;
import com.luck.report.core.build.Context;
import com.luck.report.core.build.Dataset;
import com.luck.report.core.definition.value.DatasetValue;
import com.luck.report.core.expression.model.Condition;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Report;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 列表滤空不得塞假行，否则子格 count 会把空 Map 算成 1 */
class SelectAggregateEmptyResultTest {

    @Test
    void allFiltered_keepsEmptyDataList() {
        DatasetValue expr = new DatasetValue();
        expr.setDatasetName("t_shop");
        expr.setProperty("name");
        expr.setCondition(rejectAll());

        Cell cell = new Cell();
        cell.setValue(expr);

        List<BindData> result = new SelectAggregate().aggregate(expr, cell, context());

        assertEquals(1, result.size());
        assertEquals("", result.get(0).getValue());
        assertTrue(result.get(0).getDataList().isEmpty());
    }

    private static Condition rejectAll() {
        return new Condition() {
            @Override
            public boolean filter(Cell cell, Cell currentCell, Object obj, Context context) {
                return false;
            }

            @Override
            public List<String> fetchCellName() {
                return Collections.emptyList();
            }
        };
    }

    private static Context context() {
        Report report = new Report();
        Cell root = new Cell();
        root.setName("root");
        report.setRootCell(root);
        report.getCellsMap().put("root", Collections.singletonList(root));
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("id", 1);
        row.put("name", "A店");
        Map<String, Dataset> datasetMap = new HashMap<String, Dataset>();
        datasetMap.put("t_shop", new Dataset("t_shop", Arrays.asList(row)));
        return new Context(null, report, datasetMap, null, new HashMap<String, Object>(), null);
    }
}
