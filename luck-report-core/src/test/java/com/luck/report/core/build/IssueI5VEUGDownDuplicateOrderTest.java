package com.luck.report.core.build;

import com.luck.report.core.definition.ReportDefinition;
import com.luck.report.core.export.ReportRender;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Report;
import com.luck.report.core.model.Row;
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
 * 对照 https://gitee.com/summer-T/ureport-keep/issues/I5VEUG：
 * 父格向下展开且带子行时，插入顺序须为「父行 → 子行」。
 */
class IssueI5VEUGDownDuplicateOrderTest {

    @Test
    void parentChildDownExpand_keepsHeaderUnderEachParent() throws Exception {
        Path xml = Paths.get("..", "..", "doc", "i5veug", "I5VEUG.ureport.xml").toAbsolutePath().normalize();
        ReportDefinition def;
        try (FileInputStream in = new FileInputStream(xml.toFile())) {
            def = new ReportParser().parse(in, xml.toString());
        }
        new ReportRender().rebuildReportDefinition(def);
        Report report = new ReportBuilder().buildReport(def, new HashMap<String, Object>());

        List<Row> rows = report.getRows();
        assertEquals(6, rows.size(), "3 个父块 × (父行+标题行)");
        assertEquals("1、第一条", cellText(rows.get(0), "A1"));
        assertEquals("标题1", cellText(rows.get(1), "A2"));
        assertEquals("7、第二条", cellText(rows.get(2), "A1"));
        assertEquals("标题1", cellText(rows.get(3), "A2"));
        assertEquals("16、第三条", cellText(rows.get(4), "A1"));
        assertEquals("标题1", cellText(rows.get(5), "A2"));

        List<Cell> a2Cells = report.getCellsMap().get("A2");
        assertEquals(3, a2Cells.size());
        String rowShape = rows.stream()
                .map(r -> r.getCells().stream().map(Cell::getName).collect(Collectors.joining(",")))
                .collect(Collectors.joining(" | "));
        assertEquals("A1 | A2,B2,C2,D2 | A1 | A2,B2,C2,D2 | A1 | A2,B2,C2,D2", rowShape);
    }

    private static String cellText(Row row, String cellName) {
        for (Cell cell : row.getCells()) {
            if (cellName.equals(cell.getName())) {
                Object data = cell.getFormatData() != null ? cell.getFormatData() : cell.getData();
                return data == null ? "" : String.valueOf(data);
            }
        }
        return null;
    }
}
