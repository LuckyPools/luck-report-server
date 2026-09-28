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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 父格条件滤空时必须带空 dataList，避免子格回退全量数据集 */
class GroupAggregateEmptyParentTest {

    private final TestableGroupAggregate aggregate = new TestableGroupAggregate();

    @Test
    void singleRowConditionFail_keepsEmptyDataList() {
        DatasetValue expr = dataset("name", alwaysFalse());
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("id", 1);
        row.put("name", "shop");

        List<BindData> result = aggregate.run(expr, Collections.singletonList(row));

        assertEquals(1, result.size());
        assertEquals("", result.get(0).getValue());
        assertNotNull(result.get(0).getDataList());
        assertTrue(result.get(0).getDataList().isEmpty());
    }

    @Test
    void emptyObjList_keepsEmptyDataList() {
        DatasetValue expr = dataset("name", null);

        List<BindData> result = aggregate.run(expr, Collections.emptyList());

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getDataList());
        assertTrue(result.get(0).getDataList().isEmpty());
    }

    @Test
    void multiRowAllFiltered_keepsEmptyDataList() {
        DatasetValue expr = dataset("name", alwaysFalse());
        Map<String, Object> row1 = new HashMap<String, Object>();
        row1.put("id", 1);
        row1.put("name", "a");
        Map<String, Object> row2 = new HashMap<String, Object>();
        row2.put("id", 2);
        row2.put("name", "b");

        List<BindData> result = aggregate.run(expr, Arrays.asList(row1, row2));

        assertEquals(1, result.size());
        assertNotNull(result.get(0).getDataList());
        assertTrue(result.get(0).getDataList().isEmpty());
    }

    private static DatasetValue dataset(String property, Condition condition) {
        DatasetValue value = new DatasetValue();
        value.setDatasetName("t_shop");
        value.setProperty(property);
        value.setCondition(condition);
        return value;
    }

    private static Condition alwaysFalse() {
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

    private static Context emptyContext() {
        Report report = new Report();
        Cell root = new Cell();
        root.setName("root");
        report.setRootCell(root);
        report.getCellsMap().put("root", Collections.singletonList(root));
        Map<String, Dataset> datasetMap = new HashMap<String, Dataset>();
        return new Context(null, report, datasetMap, null, new HashMap<String, Object>(), null);
    }

    private static final class TestableGroupAggregate extends GroupAggregate {
        List<BindData> run(DatasetValue expr, List<?> objList) {
            return doAggregate(expr, new Cell(), emptyContext(), objList);
        }
    }
}
