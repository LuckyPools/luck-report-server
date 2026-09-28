package com.luck.report.core.build.aggregate;

import com.luck.report.core.build.BindData;
import com.luck.report.core.build.Context;
import com.luck.report.core.build.Dataset;
import com.luck.report.core.definition.Order;
import com.luck.report.core.definition.value.DatasetValue;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Report;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 列表排序含多个 null 时，比较器必须满足 TimSort 契约。
 */
class NullOrderBindDataTest {

    /** JDK 8 上会触发 Comparison method violates its general contract 的原序 */
    private static final Integer[] NAMES = {
            25, 2, 3, 18, null, 10, 1, 22, 15, 27, null, null, 19, 23, 31, 9,
            17, null, 26, 30, null, null, 11, 14, 6, 7, 13, 29, null, 21, null, 5
    };

    @Test
    void selectAsc_nullsSortFirstWithoutContractError() {
        List<BindData> result = new SelectAggregate().aggregate(expr(Order.asc), new Cell(), context(NAMES));

        assertEquals(NAMES.length, result.size());
        for (int i = 0; i < 8; i++) {
            assertNull(result.get(i).getValue());
        }
        assertEquals(1, ((Number) result.get(8).getValue()).intValue());
        assertEquals(31, ((Number) result.get(result.size() - 1).getValue()).intValue());
    }

    @Test
    void selectDesc_nullsStaySmallest() {
        List<BindData> result = new SelectAggregate().aggregate(expr(Order.desc), new Cell(), context(NAMES));

        assertNull(result.get(0).getValue());
        assertEquals(31, ((Number) result.get(8).getValue()).intValue());
        assertEquals(1, ((Number) result.get(result.size() - 1).getValue()).intValue());
    }

    private static DatasetValue expr(Order order) {
        DatasetValue expr = new DatasetValue();
        expr.setDatasetName("nullSort");
        expr.setProperty("name");
        expr.setOrder(order);
        return expr;
    }

    private static Context context(Integer[] names) {
        Report report = new Report();
        Cell root = new Cell();
        root.setName("root");
        report.setRootCell(root);
        report.getCellsMap().put("root", Collections.singletonList(root));
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        for (Integer name : names) {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("name", name);
            rows.add(row);
        }
        Map<String, Dataset> datasetMap = new HashMap<String, Dataset>();
        datasetMap.put("nullSort", new Dataset("nullSort", rows));
        return new Context(null, report, datasetMap, null, new HashMap<String, Object>(), null);
    }
}
