package com.luck.report.core.build.aggregate;

import com.luck.report.core.build.BindData;
import com.luck.report.core.build.Context;
import com.luck.report.core.build.Dataset;
import com.luck.report.core.definition.Expand;
import com.luck.report.core.definition.value.DatasetValue;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Report;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 标量聚合要把参与范围的行带回 dataList，供子格继续取数 */
class ScalarAggregateRowListTest {

    @Test
    void sumWithoutParent_keepsSourceRows() {
        Map<String, Object> a = row(1, "A店", 10);
        Map<String, Object> b = row(2, "B店", 20);
        Map<String, Object> c = row(3, "C店", 30);
        List<Map<String, Object>> data = Arrays.asList(a, b, c);

        BindData bind = new SumAggregate().aggregate(expr("amount"), new Cell(), context(data)).get(0);

        assertEquals(60.0, ((Number) bind.getValue()).doubleValue(), 0.001);
        assertEquals(3, bind.getDataList().size());
        assertSame(a, bind.getDataList().get(0));
        assertSame(c, bind.getDataList().get(2));
    }

    @Test
    void sumUnderNullParentBindData_doesNotFallbackToFullDataset() {
        List<Map<String, Object>> data = Arrays.asList(row(1, "A店", 10), row(2, "B店", 20), row(3, "C店", 30));
        DatasetValue value = expr("amount");
        Cell cell = childOf(parent(value, null));

        BindData bind = new SumAggregate().aggregate(value, cell, context(data)).get(0);

        assertEquals(0.0, ((Number) bind.getValue()).doubleValue(), 0.001);
        assertTrue(bind.getDataList().isEmpty());
    }

    @Test
    void sumUnderParentRows_keepsThoseRows() {
        Map<String, Object> a = row(1, "A店", 10);
        Map<String, Object> b = row(2, "B店", 20);
        DatasetValue value = expr("amount");
        Cell cell = childOf(parent(value, Arrays.<Object>asList(a, b)));

        BindData bind = new SumAggregate().aggregate(value, cell, context(Arrays.asList(a, b, row(3, "C店", 30)))).get(0);

        assertEquals(30.0, ((Number) bind.getValue()).doubleValue(), 0.001);
        assertEquals(Arrays.<Object>asList(a, b), bind.getDataList());
    }

    @Test
    void sumKeepsRowWhenAmountBlank() {
        Map<String, Object> blank = row(1, "A店", null);
        Map<String, Object> b = row(2, "B店", 20);
        DatasetValue value = expr("amount");

        BindData bind = new SumAggregate().aggregate(value, new Cell(), context(Arrays.asList(blank, b))).get(0);

        assertEquals(20.0, ((Number) bind.getValue()).doubleValue(), 0.001);
        assertEquals(Arrays.<Object>asList(blank, b), bind.getDataList());
    }

    @Test
    void countWithoutParent_keepsAllRows() {
        List<Map<String, Object>> data = Arrays.asList(row(1, "A店", 10), row(2, "B店", 20));

        BindData bind = new CountAggregate().aggregate(expr(null), new Cell(), context(data)).get(0);

        assertEquals(2, ((Number) bind.getValue()).intValue());
        assertEquals(data, bind.getDataList());
    }

    @Test
    void avgMaxMin_keepPassingRows() {
        List<Map<String, Object>> data = Arrays.asList(row(1, "A店", 10), row(2, "B店", 30));
        Context context = context(data);
        DatasetValue value = expr("amount");

        BindData avg = new AvgAggregate().aggregate(value, new Cell(), context).get(0);
        BindData max = new MaxAggregate().aggregate(value, new Cell(), context).get(0);
        BindData min = new MinAggregate().aggregate(value, new Cell(), context).get(0);

        assertEquals(20.0, ((Number) avg.getValue()).doubleValue(), 0.001);
        assertEquals(30.0, ((Number) max.getValue()).doubleValue(), 0.001);
        assertEquals(10.0, ((Number) min.getValue()).doubleValue(), 0.001);
        assertEquals(data, avg.getDataList());
        assertEquals(data, max.getDataList());
        assertEquals(data, min.getDataList());
    }

    private static DatasetValue expr(String property) {
        DatasetValue value = new DatasetValue();
        value.setDatasetName("t_shop");
        value.setProperty(property);
        return value;
    }

    private static Cell parent(DatasetValue value, List<Object> bindData) {
        Cell parent = new Cell();
        parent.setName("B4");
        parent.setExpand(Expand.Down);
        parent.setValue(value);
        parent.setBindData(bindData);
        return parent;
    }

    private static Cell childOf(Cell parent) {
        Cell cell = new Cell();
        cell.setName("C4");
        cell.setValue(parent.getValue());
        cell.setLeftParentCell(parent);
        return cell;
    }

    private static Context context(List<Map<String, Object>> data) {
        Report report = new Report();
        Cell root = new Cell();
        root.setName("root");
        report.setRootCell(root);
        report.getCellsMap().put("root", Collections.singletonList(root));
        Map<String, Dataset> datasetMap = new HashMap<String, Dataset>();
        datasetMap.put("t_shop", new Dataset("t_shop", data));
        return new Context(null, report, datasetMap, null, new HashMap<String, Object>(), null);
    }

    private static Map<String, Object> row(int id, String name, Integer amount) {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("id", id);
        row.put("name", name);
        row.put("amount", amount);
        return row;
    }
}
