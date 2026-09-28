package com.luck.report.core.build;

import com.luck.report.core.definition.ReportDefinition;
import com.luck.report.core.export.ReportRender;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Report;
import com.luck.report.core.parser.ReportParser;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * 相对上父格不在当前列时，坐标计算应得到空结果，而不是空指针。
 */
class CellCoordinateMissTest {

    /**
     * 案例报表中 D3 的上父格对不上当前列，整表仍应计算完成
     *
     * @throws Exception 案例文件不存在或报表解析失败时抛出
     */
    @Test
    void missedTopParent_buildsReport() throws Exception {
        Path xml = Paths.get("..", "..", "doc", "案例", "单元格坐标只写上父格.ureport.xml")
                .toAbsolutePath().normalize();
        ReportDefinition def;
        try (FileInputStream in = new FileInputStream(xml.toFile())) {
            def = new ReportParser().parse(in, xml.toString());
        }
        new ReportRender().rebuildReportDefinition(def);
        Report report = new ReportBuilder().buildReport(def, new HashMap<String, Object>());

        List<Cell> leftOnly = report.getCellsMap().get("C3");
        List<Cell> missedTop = report.getCellsMap().get("D3");
        assertNotNull(leftOnly);
        assertNotNull(missedTop);
        assertFalse(leftOnly.isEmpty());
        assertEquals(leftOnly.size(), missedTop.size());
        for (Cell cell : missedTop) {
            assertEquals("", text(cell));
        }
        for (Cell cell : leftOnly) {
            assertFalse(text(cell).isEmpty());
        }
    }

    private static String text(Cell cell) {
        Object data = cell.getFormatData() != null ? cell.getFormatData() : cell.getData();
        return data == null ? "" : String.valueOf(data);
    }
}
