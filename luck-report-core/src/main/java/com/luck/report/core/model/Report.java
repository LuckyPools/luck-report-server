/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.luck.report.core.model;

import com.luck.report.core.build.ConditionPropertyDeferral;
import com.luck.report.core.build.Context;
import com.luck.report.core.build.paging.Page;
import com.luck.report.core.build.paging.PagingBuilder;
import com.luck.report.core.definition.Band;
import com.luck.report.core.definition.ConditionPropertyItem;
import com.luck.report.core.definition.FloatImage;
import com.luck.report.core.definition.FloatText;
import com.luck.report.core.definition.HeaderFooterDefinition;
import com.luck.report.core.definition.Paper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Jacky.gao
 * @since 2016年11月1日
 */
public class Report {
    private Paper paper;
    private HeaderFooterDefinition header;
    private HeaderFooterDefinition footer;
    private Cell rootCell;
    private Context context;
    private List<Row> rows;
    private List<Row> headerRepeatRows = new ArrayList<Row>();
    private List<Row> footerRepeatRows = new ArrayList<Row>();
    private List<Row> titleRows = new ArrayList<Row>();
    private List<Row> summaryRows = new ArrayList<Row>();
    private int repeatHeaderRowHeight = 0, repeatFooterRowHeight = 0, titleRowsHeight = 0, summaryRowsHeight = 0;
    private List<Column> columns;
    private List<Page> pages;
    private String reportFullName;
    private List<Cell> lazyComputeCells = new ArrayList<Cell>();
    private Map<Row, Map<Column, Cell>> rowColCellMap = new HashMap<Row, Map<Column, Cell>>();
    private Map<String, List<Cell>> cellsMap = new HashMap<String, List<Cell>>();
    /**
     * 悬浮图片元素列表（设计态配置，运行期不变）
     */
    private List<FloatImage> floatImages;
    /**
     * 悬浮文本元素列表（设计态配置，运行期不变）
     */
    private List<FloatText> floatTexts;

    /**
     * 按 1-based 行号插入单行（仅改 ArrayList；建表结束后须 chainRowsByListOrder）
     *
     * @param row 待插入行，非空
     * @param rowNumber 1-based 插入位置
     */
    public void insertRow(Row row, int rowNumber) {
        int pos = rowNumber - 1;
        rows.add(pos, row);
        Band band = row.getBand();
        if (band == null) {
            return;
        }
    }

    /**
     * 将一批行按列表顺序挂接到指定 1-based 位置，再重建序号数组；顺序与历史 {@code addAll(pos, insertRows)} 一致；不在此方法内按 tempRowNumber 排序。
     *
     * @param firstRowIndex 1-based 插入位置（pos = firstRowIndex - 1）
     * @param insertRows 已排好序的待插入行；空则忽略
     */
    public void insertRows(int firstRowIndex, List<Row> insertRows) {
        if (insertRows == null || insertRows.isEmpty()) {
            return;
        }
        int pos = firstRowIndex - 1;
        if (pos > rows.size()) {
            return;
        }
        linkRowSegment(insertRows);
        Row first = insertRows.get(0);
        Row last = insertRows.get(insertRows.size() - 1);
        Row head;
        if (rows.isEmpty()) {
            first.setPrev(null);
            last.setNext(null);
            head = first;
        } else if (pos == 0) {
            Row oldHead = rows.get(0);
            last.setNext(oldHead);
            oldHead.setPrev(last);
            first.setPrev(null);
            head = first;
        } else if (pos == rows.size()) {
            Row oldTail = rows.get(rows.size() - 1);
            oldTail.setNext(first);
            first.setPrev(oldTail);
            last.setNext(null);
            head = rows.get(0);
        } else {
            Row after = rows.get(pos);
            Row before = after.getPrev();
            if (before == null) {
                before = rows.get(pos - 1);
            }
            before.setNext(first);
            first.setPrev(before);
            last.setNext(after);
            after.setPrev(last);
            head = rows.get(0);
        }
        rebuildRowIndex(head);
    }

    /**
     * 按 1-based 列号插入单列（仅改 ArrayList；建表结束后须 chainColumnsByListOrder）
     *
     * @param column 待插入列，非空
     * @param columnNumber 1-based 插入位置
     */
    public void insertColumn(Column column, int columnNumber) {
        int pos = columnNumber - 1;
        columns.add(pos, column);
    }

    /**
     * 将一批列按列表顺序挂接到指定 1-based 位置，再重建序号数组；顺序与历史 {@code addAll(pos, insertColumns)} 一致；不在此方法内按 tempColumnNumber 排序。
     *
     * @param firstColumnIndex 1-based 插入位置（pos = firstColumnIndex - 1）
     * @param insertColumns 已排好序的待插入列；空则忽略
     */
    public void insertColumns(int firstColumnIndex, List<Column> insertColumns) {
        if (insertColumns == null || insertColumns.isEmpty()) {
            return;
        }
        int pos = firstColumnIndex - 1;
        if (pos > columns.size()) {
            return;
        }
        linkColumnSegment(insertColumns);
        Column first = insertColumns.get(0);
        Column last = insertColumns.get(insertColumns.size() - 1);
        Column head;
        if (columns.isEmpty()) {
            first.setPrev(null);
            last.setNext(null);
            head = first;
        } else if (pos == 0) {
            Column oldHead = columns.get(0);
            last.setNext(oldHead);
            oldHead.setPrev(last);
            first.setPrev(null);
            head = first;
        } else if (pos == columns.size()) {
            Column oldTail = columns.get(columns.size() - 1);
            oldTail.setNext(first);
            first.setPrev(oldTail);
            last.setNext(null);
            head = columns.get(0);
        } else {
            Column after = columns.get(pos);
            Column before = after.getPrev();
            if (before == null) {
                before = columns.get(pos - 1);
            }
            before.setNext(first);
            first.setPrev(before);
            last.setNext(after);
            after.setPrev(last);
            head = columns.get(0);
        }
        rebuildColumnIndex(head);
    }

    /**
     * 按当前 ArrayList 顺序串接 prev/next 并写入缓存行号；用于 {@code newReport} 建表结束，或外部仅改了列表顺序后需对齐链。
     */
    public void chainRowsByListOrder() {
        if (rows == null || rows.isEmpty()) {
            return;
        }
        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            row.setPrev(i > 0 ? rows.get(i - 1) : null);
            row.setNext(i < rows.size() - 1 ? rows.get(i + 1) : null);
            row.setRowNumber(i + 1);
        }
    }

    /**
     * 按当前 ArrayList 顺序串接 prev/next 并写入缓存列号；用于 {@code newReport} 建表结束，或外部仅改了列表顺序后需对齐链。
     */
    public void chainColumnsByListOrder() {
        if (columns == null || columns.isEmpty()) {
            return;
        }
        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);
            column.setPrev(i > 0 ? columns.get(i - 1) : null);
            column.setNext(i < columns.size() - 1 ? columns.get(i + 1) : null);
            column.setColumnNumber(i + 1);
        }
    }

    /**
     * 从已知或可回退的链表头遍历，重写行号并重建 rows 序号数组
     */
    public void rebuildRowIndex() {
        rebuildRowIndex(findFirstRow());
    }

    /**
     * 从指定头节点遍历，重写行号并重建 rows 序号数组
     *
     * @param head 链表头；null 时清空 rows
     */
    public void rebuildRowIndex(Row head) {
        if (rows == null) {
            return;
        }
        if (head == null) {
            rows.clear();
            return;
        }
        while (head.getPrev() != null) {
            head = head.getPrev();
        }
        List<Row> rebuilt = new ArrayList<Row>();
        int number = 1;
        for (Row row = head; row != null; row = row.getNext()) {
            row.setRowNumber(number++);
            rebuilt.add(row);
        }
        rows.clear();
        rows.addAll(rebuilt);
    }

    /**
     * 从已知或可回退的链表头遍历，重写列号并重建 columns 序号数组
     */
    public void rebuildColumnIndex() {
        rebuildColumnIndex(findFirstColumn());
    }

    /**
     * 从指定头节点遍历，重写列号并重建 columns 序号数组
     *
     * @param head 链表头；null 时清空 columns
     */
    public void rebuildColumnIndex(Column head) {
        if (columns == null) {
            return;
        }
        if (head == null) {
            columns.clear();
            return;
        }
        while (head.getPrev() != null) {
            head = head.getPrev();
        }
        List<Column> rebuilt = new ArrayList<Column>();
        int number = 1;
        for (Column column = head; column != null; column = column.getNext()) {
            column.setColumnNumber(number++);
            rebuilt.add(column);
        }
        columns.clear();
        columns.addAll(rebuilt);
    }

    /**
     * 在表尾追加一行并重建行索引
     *
     * @param row 待追加行，非空
     */
    public void appendRow(Row row) {
        List<Row> single = new ArrayList<Row>(1);
        single.add(row);
        insertRows(rows.size() + 1, single);
    }

    /**
     * 在表尾追加一列并重建列索引
     *
     * @param column 待追加列，非空
     */
    public void appendColumn(Column column) {
        List<Column> single = new ArrayList<Column>(1);
        single.add(column);
        insertColumns(columns.size() + 1, single);
    }

    /**
     * 将待插入段内部串成双向链
     *
     * @param segment 非空行列表
     */
    private void linkRowSegment(List<Row> segment) {
        for (int i = 0; i < segment.size(); i++) {
            Row row = segment.get(i);
            row.setPrev(i > 0 ? segment.get(i - 1) : null);
            row.setNext(i < segment.size() - 1 ? segment.get(i + 1) : null);
        }
    }

    /**
     * 将待插入段内部串成双向链
     *
     * @param segment 非空列列表
     */
    private void linkColumnSegment(List<Column> segment) {
        for (int i = 0; i < segment.size(); i++) {
            Column column = segment.get(i);
            column.setPrev(i > 0 ? segment.get(i - 1) : null);
            column.setNext(i < segment.size() - 1 ? segment.get(i + 1) : null);
        }
    }

    /**
     * 定位行链起点（优先沿 prev 回退，否则取当前数组首元素）
     *
     * @return 首行；无行时 null
     */
    private Row findFirstRow() {
        if (rows.isEmpty()) {
            return null;
        }
        Row head = rows.get(0);
        while (head.getPrev() != null) {
            head = head.getPrev();
        }
        return head;
    }

    /**
     * 定位列链起点（优先沿 prev 回退，否则取当前数组首元素）
     *
     * @return 首列；无列时 null
     */
    private Column findFirstColumn() {
        if (columns.isEmpty()) {
            return null;
        }
        Column head = columns.get(0);
        while (head.getPrev() != null) {
            head = head.getPrev();
        }
        return head;
    }

    public Row getRow(int rowNumber) {
        if (rowNumber > rows.size()) {
            return null;
        }
        return rows.get(rowNumber - 1);
    }

    public Column getColumn(int columnNumber) {
        if (columnNumber > columns.size()) {
            return null;
        }
        return columns.get(columnNumber - 1);
    }

    public Cell getRootCell() {
        return rootCell;
    }
    public void setRootCell(Cell rootCell) {
        this.rootCell = rootCell;
    }

    public boolean addCell(Cell cell) {
        String cellName = cell.getName();
        List<Cell> cells = null;
        if (cellsMap.containsKey(cellName)) {
            cells = cellsMap.get(cellName);
        } else {
            cells = new ArrayList<Cell>();
            cellsMap.put(cellName, cells);
        }
        cells.add(cell);
        Row row = cell.getRow();
        Column col = cell.getColumn();
        Map<Column, Cell> colMap = null;
        if (rowColCellMap.containsKey(row)) {
            colMap = rowColCellMap.get(row);
        } else {
            colMap = new HashMap<Column, Cell>();
            rowColCellMap.put(row, colMap);
        }
        colMap.put(col, cell);
        return addLazyCell(cell);
    }

    public boolean addLazyCell(Cell cell) {
        List<ConditionPropertyItem> conditionPropertyItems = cell.getConditionPropertyItems();
        if (conditionPropertyItems != null && !conditionPropertyItems.isEmpty()) {
            if (ConditionPropertyDeferral.mustDefer(cell)) {
                lazyComputeCells.add(cell);
                return true;
            }
        }
        return false;
    }

    public Map<Row, Map<Column, Cell>> getRowColCellMap() {
        return rowColCellMap;
    }

    public List<Page> getPages() {
        if (pages == null) {
            pages = PagingBuilder.buildPages(this);
        }
        return pages;
    }
    public void setPages(List<Page> pages) {
        this.pages = pages;
    }

    public void rePaging(Paper paper) {
        paper.setColumnCount(this.paper.getColumnCount());
        paper.setColumnEnabled(this.paper.isColumnEnabled());
        paper.setFixRows(this.paper.getFixRows());
        paper.setPagingMode(this.paper.getPagingMode());
        setPaper(paper);
        pages = PagingBuilder.buildPages(this);
    }

    public Context getContext() {
        return context;
    }
    public void setContext(Context context) {
        this.context = context;
    }

    public String getReportFullName() {
        return reportFullName;
    }
    public void setReportFullName(String reportFullName) {
        this.reportFullName = reportFullName;
    }
    public Paper getPaper() {
        return paper;
    }
    public void setPaper(Paper paper) {
        this.paper = paper;
    }
    public Map<String, List<Cell>> getCellsMap() {
        return cellsMap;
    }
    public List<Cell> getLazyComputeCells() {
        return lazyComputeCells;
    }
    public List<Row> getRows() {
        return rows;
    }
    public void setRows(List<Row> rows) {
        this.rows = rows;
    }
    public List<Column> getColumns() {
        return columns;
    }
    public void setColumns(List<Column> columns) {
        this.columns = columns;
    }
    public List<Row> getHeaderRepeatRows() {
        return headerRepeatRows;
    }
    public void setHeaderRepeatRows(List<Row> headerRepeatRows) {
        this.headerRepeatRows = headerRepeatRows;
    }
    public List<Row> getFooterRepeatRows() {
        return footerRepeatRows;
    }
    public void setFooterRepeatRows(List<Row> footerRepeatRows) {
        this.footerRepeatRows = footerRepeatRows;
    }
    public List<Row> getTitleRows() {
        return titleRows;
    }
    public List<Row> getSummaryRows() {
        return summaryRows;
    }

    public int getRepeatHeaderRowHeight() {
        return repeatHeaderRowHeight;
    }
    public void setRepeatHeaderRowHeight(int repeatHeaderRowHeight) {
        this.repeatHeaderRowHeight = repeatHeaderRowHeight;
    }
    public int getRepeatFooterRowHeight() {
        return repeatFooterRowHeight;
    }
    public void setRepeatFooterRowHeight(int repeatFooterRowHeight) {
        this.repeatFooterRowHeight = repeatFooterRowHeight;
    }
    public int getTitleRowsHeight() {
        return titleRowsHeight;
    }
    public void setTitleRowsHeight(int titleRowsHeight) {
        this.titleRowsHeight = titleRowsHeight;
    }
    public int getSummaryRowsHeight() {
        return summaryRowsHeight;
    }
    public void setSummaryRowsHeight(int summaryRowsHeight) {
        this.summaryRowsHeight = summaryRowsHeight;
    }
    public HeaderFooterDefinition getHeader() {
        return header;
    }
    public void setHeader(HeaderFooterDefinition header) {
        this.header = header;
    }
    public HeaderFooterDefinition getFooter() {
        return footer;
    }
    public void setFooter(HeaderFooterDefinition footer) {
        this.footer = footer;
    }
    public List<FloatImage> getFloatImages() {
        return floatImages;
    }
    public void setFloatImages(List<FloatImage> floatImages) {
        this.floatImages = floatImages;
    }
    public List<FloatText> getFloatTexts() {
        return floatTexts;
    }
    public void setFloatTexts(List<FloatText> floatTexts) {
        this.floatTexts = floatTexts;
    }
}
