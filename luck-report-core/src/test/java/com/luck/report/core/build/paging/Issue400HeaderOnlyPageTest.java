package com.luck.report.core.build.paging;

import com.luck.report.core.build.HideRowColumnBuilder;
import com.luck.report.core.build.ReportBuilder;
import com.luck.report.core.definition.ReportDefinition;
import com.luck.report.core.export.PageBuilder;
import com.luck.report.core.export.ReportRender;
import com.luck.report.core.export.SinglePageData;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Column;
import com.luck.report.core.model.Report;
import com.luck.report.core.model.Row;
import com.luck.report.core.parser.ReportParser;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 重复表头下明细被隐藏时，分页预览仍应有一页表头。
 */
class Issue400HeaderOnlyPageTest {

    /**
     * 空数据隐藏明细行后，分页预览取第 1 页不再越界，页上只剩表头
     *
     * @throws Exception 案例文件不存在或报表解析失败时抛出
     */
    @Test
    void hiddenDetail_keepsHeaderPage() throws Exception {
        Path xml = Paths.get("..", "..", "doc", "issue400", "issue400.ureport.xml")
                .toAbsolutePath().normalize();
        ReportDefinition def;
        try (FileInputStream in = new FileInputStream(xml.toFile())) {
            def = new ReportParser().parse(in, xml.toString());
        }
        new ReportRender().rebuildReportDefinition(def);
        ReportBuilder builder = new ReportBuilder();
        builder.setHideRowColumnBuilder(new HideRowColumnBuilder());
        Report report = builder.buildReport(def, new HashMap<String, Object>());

        assertEquals(1, report.getPages().size());
        SinglePageData pageData = PageBuilder.buildSinglePageData(1, report);
        assertEquals(1, pageData.getTotalPages());
        List<Row> pageRows = pageData.getPages().get(0).getRows();
        assertEquals(2, pageRows.size());
        assertEquals(0, pageRows.get(1).getRealHeight());

        Map<Column, Cell> cells = report.getRowColCellMap().get(pageRows.get(0));
        assertEquals(2, cells.size());
        StringBuilder text = new StringBuilder();
        for (Column column : report.getColumns()) {
            Cell cell = cells.get(column);
            if (cell == null) {
                continue;
            }
            Object data = cell.getFormatData() != null ? cell.getFormatData() : cell.getData();
            if (data != null) {
                text.append(data);
            }
        }
        assertEquals("年份月份", text.toString());
    }
}
