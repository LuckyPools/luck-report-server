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
package com.luck.report.core.build;

import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Column;
import com.luck.report.core.model.Report;
import com.luck.report.core.model.Row;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 处理布局隐藏的行/列：标记 hide，并将落在隐藏起点的合并格挪到首个可见行列
 *
 * @author Jacky.gao
 * @since 2017年7月4日
 */
public class HideRowColumnBuilder {

    /**
     * 对报表中所有布局隐藏的行列做合并起点重定位（定义 hide / 条件宽高为 0 后统一收口）
     *
     * @param report 报表
     */
    public void relocateAllHiddenMergeStarts(Report report) {
        for (Column col : report.getColumns()) {
            if (col.isHiddenFormLayout()) {
                doHideProcessColumn(report, col);
            }
        }
        for (Row row : report.getRows()) {
            if (row.isHiddenFormLayout()) {
                doHideProcessRow(report, row);
            }
        }
    }

    /**
     * 隐藏列宽为 0 的列；合并起点在该列时挪到第一个可见列，并缩短至原合并终点的物理跨度
     *
     * @param report 报表
     * @param col 待隐藏列
     */
    public void doHideProcessColumn(Report report, Column col) {
        if (col.getWidth() < 1) {
            col.setHide(true);
        }
        if (!col.isHiddenFormLayout()) {
            return;
        }
        Map<Row, Map<Column, Cell>> cellMap = report.getRowColCellMap();
        List<Row> rows = report.getRows();
        for (Row row : rows) {
            if (row.isHiddenFormLayout()) {
                continue;
            }
            Map<Column, Cell> rowMap = cellMap.get(row);
            if (rowMap == null) {
                continue;
            }
            Cell cell = rowMap.get(col);
            if (cell == null) {
                continue;
            }
            int colSpan = cell.getColSpan();
            if (colSpan < 2) {
                continue;
            }
            Column firstVisible = null;
            int offset = 0;
            Column cursor = col;
            for (int i = 0; i < colSpan && cursor != null; i++) {
                if (!cursor.isHiddenFormLayout()) {
                    firstVisible = cursor;
                    offset = i;
                    break;
                }
                cursor = cursor.getNext();
            }
            if (firstVisible == null || firstVisible == col) {
                continue;
            }
            Cell occupied = rowMap.get(firstVisible);
            if (occupied != null && occupied != cell) {
                continue;
            }
            int newSpan = colSpan - offset;
            cell.setColSpan(newSpan < 2 ? 0 : newSpan);
            cell.setColumn(firstVisible);
            rowMap.put(firstVisible, cell);
            rowMap.remove(col);
        }
    }

    /**
     * 隐藏行高为 0 的行；合并起点在该行时挪到第一个可见行，并缩短至原合并终点的物理跨度
     *
     * @param report 报表
     * @param row 待隐藏行
     */
    public void doHideProcessRow(Report report, Row row) {
        if (row.getRealHeight() < 1) {
            row.setHide(true);
        }
        if (!row.isHiddenFormLayout()) {
            return;
        }
        Map<Row, Map<Column, Cell>> cellMap = report.getRowColCellMap();
        Map<Column, Cell> map = cellMap.get(row);
        if (map == null) {
            return;
        }
        List<Column> columns = report.getColumns();
        for (Column col : columns) {
            if (col.isHiddenFormLayout()) {
                continue;
            }
            Cell cell = map.get(col);
            if (cell == null) {
                continue;
            }
            int rowSpan = cell.getRowSpan();
            if (rowSpan < 2) {
                continue;
            }
            Row firstVisible = null;
            int offset = 0;
            Row cursor = row;
            for (int i = 0; i < rowSpan && cursor != null; i++) {
                if (!cursor.isHiddenFormLayout()) {
                    firstVisible = cursor;
                    offset = i;
                    break;
                }
                cursor = cursor.getNext();
            }
            if (firstVisible == null || firstVisible == row) {
                continue;
            }
            Map<Column, Cell> nextRowMap = cellMap.get(firstVisible);
            if (nextRowMap == null) {
                nextRowMap = new HashMap<Column, Cell>();
                cellMap.put(firstVisible, nextRowMap);
            }
            Cell occupied = nextRowMap.get(col);
            if (occupied != null && occupied != cell) {
                continue;
            }
            int newSpan = rowSpan - offset;
            cell.setRowSpan(newSpan < 2 ? 0 : newSpan);
            cell.setRow(firstVisible);
            nextRowMap.put(col, cell);
            map.remove(col);
        }
    }
}
