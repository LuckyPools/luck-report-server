package com.luck.report.core.build.paging;

import com.luck.report.core.definition.Band;
import com.luck.report.core.definition.Paper;
import com.luck.report.core.definition.PagingMode;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Column;
import com.luck.report.core.model.Report;
import com.luck.report.core.model.Row;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RepeatBandPagingSupportTest {

    @Test
    void detects_header_used_as_left_parent_on_body_row() {
        List<Row> rows = new ArrayList<Row>();
        Row header = row(rows, Band.headerrepeat, "h1", 20);
        Row body = row(rows, null, "b1", 20);
        Cell headerCell = cell(header, "A1");
        Cell bodyCell = cell(body, "A2");
        bodyCell.setLeftParentCell(headerCell);

        assertTrue(RepeatBandPagingSupport.rowUsesBandAsLeftParent(body, Band.headerrepeat));
        assertFalse(RepeatBandPagingSupport.rowUsesBandAsLeftParent(header, Band.headerrepeat));
    }

    @Test
    void block_mode_keeps_all_expanded_headers_on_each_page() {
        List<Row> rows = new ArrayList<Row>();
        Row h1 = row(rows, Band.headerrepeat, "h1", 10);
        Row h2 = row(rows, Band.headerrepeat, "h1", 10);
        Cell hc1 = cell(h1, "A1");
        hc1.setData("M1");
        hc1.setFormatData("M1");
        Cell hc2 = cell(h2, "A1");
        hc2.setData("M2");
        hc2.setFormatData("M2");
        for (int i = 0; i < 6; i++) {
            Row body = row(rows, null, "b" + i, 10);
            cell(body, "A2").setData("D" + (i + 1));
        }

        Report report = reportWithRows(rows);
        report.getHeaderRepeatRows().add(h1);
        Paper paper = new Paper();
        paper.setPagingMode(PagingMode.fixrows);
        paper.setFixRows(5);
        report.setPaper(paper);
        report.setContext(new com.luck.report.core.build.Context(
                null, report, new HashMap<String, com.luck.report.core.build.Dataset>(),
                null, new HashMap<String, Object>(), null));

        List<Page> pages = new FixRowsPagination().doPaging(report);
        assertTrue(pages.size() >= 2);
        assertEquals(2, countHeaderRows(pages.get(0)));
        assertEquals(2, countHeaderRows(pages.get(1)));
        assertEquals("M1", firstHeaderData(report, pages.get(0)));
        assertEquals("M2", secondHeaderData(report, pages.get(0)));
        assertEquals("M1", firstHeaderData(report, pages.get(1)));
        assertEquals("M2", secondHeaderData(report, pages.get(1)));
    }

    @Test
    void group_mode_snapshots_header_of_first_body_row() {
        List<Row> rows = new ArrayList<Row>();
        Row h1 = row(rows, Band.headerrepeat, "h1", 10);
        Cell c1 = cell(h1, "A1");
        c1.setData("M1");
        c1.setFormatData("M1");
        Row d1 = row(rows, null, "b1", 10);
        Cell b1 = cell(d1, "A2");
        b1.setData("D1");
        b1.setLeftParentCell(c1);
        Row d2 = row(rows, null, "b2", 10);
        Cell b2 = cell(d2, "A2");
        b2.setData("D2");
        b2.setLeftParentCell(c1);

        Row h2 = row(rows, Band.headerrepeat, "h1", 10);
        Cell c2 = cell(h2, "A1");
        c2.setData("M2");
        c2.setFormatData("M2");
        Row d3 = row(rows, null, "b3", 10);
        Cell b3 = cell(d3, "A2");
        b3.setData("D3");
        b3.setLeftParentCell(c2);
        Row d4 = row(rows, null, "b4", 10);
        Cell b4 = cell(d4, "A2");
        b4.setData("D4");
        b4.setLeftParentCell(c2);

        Report report = reportWithRows(rows);
        report.getHeaderRepeatRows().add(h1);
        Paper paper = new Paper();
        paper.setPagingMode(PagingMode.fixrows);
        paper.setFixRows(4);
        report.setPaper(paper);
        report.setContext(new com.luck.report.core.build.Context(
                null, report, new HashMap<String, com.luck.report.core.build.Dataset>(),
                null, new HashMap<String, Object>(), null));

        assertTrue(RepeatBandPagingSupport.rowUsesBandAsLeftParent(d1, Band.headerrepeat));
        List<Page> pages = new FixRowsPagination().doPaging(report);
        assertTrue(pages.size() >= 2);
        assertEquals("M1", firstHeaderData(report, pages.get(0)));
        assertEquals("M2", firstHeaderData(report, pages.get(1)));
        assertEquals("M1", firstHeaderFormatData(report, pages.get(0)));
        assertEquals("M2", firstHeaderFormatData(report, pages.get(1)));
    }

    @Test
    void group_mode_uses_left_parent_even_when_row_key_does_not_match_template() {
        List<Row> rows = new ArrayList<Row>();
        Row h1 = row(rows, Band.headerrepeat, "r1", 10);
        Cell c1 = cell(h1, "A1");
        c1.setData("M1");
        c1.setFormatData("M1");
        Row d1 = row(rows, null, "b1", 10);
        cell(d1, "A2").setLeftParentCell(c1);
        Row d2 = row(rows, null, "b2", 10);
        cell(d2, "A2").setLeftParentCell(c1);

        // 展开后若 rowKey 未与模板对齐，扫描槽位替换会失败；页顶仍应按左父格取 M2
        Row h2 = row(rows, Band.headerrepeat, "r1#2", 10);
        Cell c2 = cell(h2, "A1");
        c2.setData("M2");
        c2.setFormatData("M2");
        Row d3 = row(rows, null, "b3", 10);
        cell(d3, "A2").setLeftParentCell(c2);
        Row d4 = row(rows, null, "b4", 10);
        cell(d4, "A2").setLeftParentCell(c2);

        Report report = reportWithRows(rows);
        report.getHeaderRepeatRows().add(h1);
        Paper paper = new Paper();
        paper.setPagingMode(PagingMode.fixrows);
        paper.setFixRows(4);
        report.setPaper(paper);
        report.setContext(new com.luck.report.core.build.Context(
                null, report, new HashMap<String, com.luck.report.core.build.Dataset>(),
                null, new HashMap<String, Object>(), null));

        List<Page> pages = new FixRowsPagination().doPaging(report);
        assertTrue(pages.size() >= 2);
        assertEquals("M1", firstHeaderData(report, pages.get(0)));
        assertEquals("M2", firstHeaderData(report, pages.get(1)));
    }

    @Test
    void title_rows_stay_on_first_page_template_only() {
        List<Row> rows = new ArrayList<Row>();
        Row t1 = row(rows, Band.title, "r1", 10);
        cell(t1, "A1").setData("M1");
        Row t2 = row(rows, Band.title, "r1", 10);
        cell(t2, "A1").setData("M2");
        for (int i = 0; i < 6; i++) {
            cell(row(rows, null, "b" + i, 10), "A2").setData("D" + (i + 1));
        }

        Report report = reportWithRows(rows);
        report.getTitleRows().add(t1);
        Paper paper = new Paper();
        paper.setPagingMode(PagingMode.fixrows);
        paper.setFixRows(5);
        report.setPaper(paper);
        report.setContext(new com.luck.report.core.build.Context(
                null, report, new HashMap<String, com.luck.report.core.build.Dataset>(),
                null, new HashMap<String, Object>(), null));

        List<Page> pages = new FixRowsPagination().doPaging(report);
        assertTrue(pages.size() >= 2);
        assertEquals("M1", titleDataAt(pages.get(0), 1));
        assertEquals(null, titleDataAt(pages.get(0), 2));
        assertEquals(null, titleDataAt(pages.get(1), 1));
    }

    private static String titleDataAt(Page page, int ordinal) {
        int seen = 0;
        for (Row row : page.getRows()) {
            if (!Band.title.equals(row.getBand()) || row.getCells().isEmpty()) {
                continue;
            }
            seen++;
            if (seen == ordinal) {
                return String.valueOf(row.getCells().get(0).getData());
            }
        }
        return null;
    }

    private static int countHeaderRows(Page page) {
        int n = 0;
        for (Row row : page.getRows()) {
            if (Band.headerrepeat.equals(row.getBand())) {
                n++;
            }
        }
        return n;
    }

    private static String firstHeaderData(Report report, Page page) {
        return headerFieldAt(report, page, 1, false);
    }

    private static String firstHeaderFormatData(Report report, Page page) {
        return headerFieldAt(report, page, 1, true);
    }

    private static String secondHeaderData(Report report, Page page) {
        return headerFieldAt(report, page, 2, false);
    }

    private static String headerFieldAt(Report report, Page page, int ordinal, boolean format) {
        int seen = 0;
        Column col = report.getColumns().get(0);
        Map<Row, Map<Column, Cell>> cellMap = report.getRowColCellMap();
        for (Row row : page.getRows()) {
            if (!Band.headerrepeat.equals(row.getBand())) {
                continue;
            }
            seen++;
            if (seen != ordinal) {
                continue;
            }
            Map<Column, Cell> colMap = cellMap.get(row);
            if (colMap == null) {
                return null;
            }
            Cell cell = colMap.get(col);
            if (cell == null) {
                return null;
            }
            Object value = format ? cell.getFormatData() : cell.getData();
            return value == null ? null : String.valueOf(value);
        }
        return null;
    }

    private static Report reportWithRows(List<Row> rows) {
        Report report = new Report();
        report.setRows(rows);
        List<Column> columns = new ArrayList<Column>();
        Column col = new Column(columns);
        columns.add(col);
        report.setColumns(columns);
        Map<Row, Map<Column, Cell>> map = report.getRowColCellMap();
        for (Row row : rows) {
            Map<Column, Cell> colMap = new HashMap<Column, Cell>();
            for (Cell cell : row.getCells()) {
                cell.setColumn(col);
                colMap.put(col, cell);
            }
            map.put(row, colMap);
        }
        return report;
    }

    private static Row row(List<Row> rows, Band band, String rowKey, int height) {
        Row row = new Row(rows);
        row.setBand(band);
        row.setRowKey(rowKey);
        row.setHeight(height);
        row.setRealHeight(height);
        rows.add(row);
        return row;
    }

    private static Cell cell(Row row, String name) {
        Cell cell = new Cell();
        cell.setName(name);
        cell.setRow(row);
        cell.setRowSpan(1);
        cell.setColSpan(1);
        row.getCells().add(cell);
        return cell;
    }
}
