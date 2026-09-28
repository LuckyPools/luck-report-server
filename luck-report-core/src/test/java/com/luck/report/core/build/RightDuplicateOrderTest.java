package com.luck.report.core.build;

import com.luck.report.core.definition.ReportDefinition;
import com.luck.report.core.export.ReportRender;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Column;
import com.luck.report.core.model.Report;
import com.luck.report.core.parser.ReportParser;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 向右展开时插入列须按 tempColumnNumber 排序（与 DownDuplicate 对称）。
 */
class RightDuplicateOrderTest {

    @Test
    void parentChildRightExpand_keepsHeaderUnderEachParentColumn() throws Exception {
        Path xml = Paths.get("..", "..", "doc", "i5veug", "right-duplicate-order.ureport.xml")
                .toAbsolutePath().normalize();
        ReportDefinition def;
        try (FileInputStream in = new FileInputStream(xml.toFile())) {
            def = new ReportParser().parse(in, xml.toString());
        }
        new ReportRender().rebuildReportDefinition(def);
        Report report = new ReportBuilder().buildReport(def, new HashMap<String, Object>());

        List<Column> cols = report.getColumns();
        assertEquals(3, cols.size(), "3 个父列块");
        assertEquals(3, report.getCellsMap().get("A1").size());
        assertEquals(3, report.getCellsMap().get("A2").size());

        String top = cols.stream()
                .map(c -> text(findCell(c, "A1")))
                .collect(Collectors.joining(","));
        String bottom = cols.stream()
                .map(c -> text(findCell(c, "A2")))
                .collect(Collectors.joining(","));
        assertEquals("一,二,三", top);
        assertEquals("标题,标题,标题", bottom);
    }

    private static Cell findCell(Column col, String name) {
        for (Cell cell : col.getCells()) {
            if (name.equals(cell.getName())) {
                return cell;
            }
        }
        return null;
    }

    private static String text(Cell cell) {
        if (cell == null) {
            return null;
        }
        Object data = cell.getFormatData() != null ? cell.getFormatData() : cell.getData();
        return data == null ? "" : String.valueOf(data);
    }
}
