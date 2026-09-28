package com.luck.report.core.build;

import com.luck.report.core.definition.ReportDefinition;
import com.luck.report.core.export.ReportRender;
import com.luck.report.core.expression.ExpressionUtils;
import com.luck.report.core.expression.function.SumFunction;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Report;
import com.luck.report.core.parser.ReportParser;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * issue #409：合计应在小计赋值之后计算。
 */
class Issue409ExpressionOrderTest {

    /**
     * 同一左父格下，合计等于该部门各学历小计之和
     *
     * @throws Exception 案例文件不存在或报表解析失败时抛出
     */
    @Test
    void totalEqualsSumOfSubtotals() throws Exception {
        Path xml = Paths.get("..", "..", "doc", "案例", "表达式依赖提前计算.ureport.xml")
                .toAbsolutePath().normalize();
        ReportDefinition def;
        try (FileInputStream in = new FileInputStream(xml.toFile())) {
            def = new ReportParser().parse(in, xml.toString());
        }
        new ReportRender().rebuildReportDefinition(def);
        ExpressionUtils.getFunctions().put("sum", new SumFunction());
        Report report = new ReportBuilder().buildReport(def, new HashMap<String, Object>());

        List<Cell> subtotals = report.getCellsMap().get("E4");
        List<Cell> totals = report.getCellsMap().get("F4");
        Map<Cell, Double> sumByDept = new HashMap<Cell, Double>();
        for (Cell subtotal : subtotals) {
            Cell dept = subtotal.getLeftParentCell();
            Double current = sumByDept.get(dept);
            sumByDept.put(dept, Double.valueOf((current == null ? 0D : current.doubleValue()) + number(subtotal)));
        }
        List<Double> totalValues = new ArrayList<Double>();
        for (Cell total : totals) {
            double actual = number(total);
            assertEquals(sumByDept.get(total.getLeftParentCell()).doubleValue(), actual, 0.001D);
            totalValues.add(Double.valueOf(actual));
        }
        Collections.sort(totalValues);
        assertEquals(2, totalValues.size());
        assertEquals(40D, totalValues.get(0).doubleValue(), 0.001D);
        assertEquals(60D, totalValues.get(1).doubleValue(), 0.001D);
    }

    private static double number(Cell cell) {
        Object data = cell.getData();
        if (data instanceof Number) {
            return ((Number) data).doubleValue();
        }
        return data == null || String.valueOf(data).length() == 0 ? 0D : Double.parseDouble(String.valueOf(data));
    }
}
