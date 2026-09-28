package com.luck.report.core.export.excel;

import com.luck.report.core.build.paging.Page;
import com.luck.report.core.definition.CellStyle;
import com.luck.report.core.definition.Orientation;
import com.luck.report.core.definition.Paper;
import com.luck.report.core.definition.PaperType;
import com.luck.report.core.export.excel.high.builder.ExcelBuilderWithPaging;
import com.luck.report.core.export.excel.low.Excel97Producer;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Column;
import com.luck.report.core.model.Report;
import com.luck.report.core.model.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExcelNumericCellExportTest {

    private static final BigDecimal VALUE = new BigDecimal("123456789");

    @Test
    void pagingExport_keepsDoublePrecision() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new ExcelBuilderWithPaging().build(report(), out, false);
        assertNumeric(out.toByteArray());
    }

    @Test
    void excel97PagingExport_keepsDoublePrecision() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new Excel97Producer().produceWithPaging(report(), out);
        assertNumeric(out.toByteArray());
    }

    @Test
    void excel97Export_keepsDoublePrecision() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new Excel97Producer().produce(report(), out);
        assertNumeric(out.toByteArray());
    }

    private static void assertNumeric(byte[] bytes) throws Exception {
        try (Workbook workbook = WorkbookFactory.create(new ByteArrayInputStream(bytes))) {
            assertEquals(VALUE.doubleValue(), workbook.getSheetAt(0).getRow(0).getCell(0).getNumericCellValue());
        }
    }

    private static Report report() {
        List<Column> columns = new ArrayList<Column>();
        Column column = new Column(columns);
        column.setWidth(80);
        columns.add(column);

        List<Row> rows = new ArrayList<Row>();
        Row row = new Row(rows);
        row.setHeight(20);
        row.setRealHeight(20);
        rows.add(row);

        Cell cell = new Cell();
        cell.setName("A1");
        cell.setRow(row);
        cell.setColumn(column);
        cell.setCellStyle(new CellStyle());
        cell.setFormatData(VALUE);
        row.getCells().add(cell);

        Report report = new Report();
        report.setColumns(columns);
        report.setRows(rows);
        report.addCell(cell);
        Paper paper = new Paper();
        paper.setPaperType(PaperType.A4);
        paper.setOrientation(Orientation.portrait);
        report.setPaper(paper);
        report.setPages(Collections.singletonList(new Page(rows, columns)));
        return report;
    }
}
