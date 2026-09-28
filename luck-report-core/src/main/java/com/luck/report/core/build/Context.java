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

import com.luck.report.core.Utils;
import com.luck.report.core.chart.ChartData;
import com.luck.report.core.definition.ConditionPropertyItem;
import com.luck.report.core.definition.mapping.MappingType;
import com.luck.report.core.definition.value.SimpleValue;
import com.luck.report.core.definition.value.Value;
import com.luck.report.core.exception.CellDependencyException;
import com.luck.report.core.exception.DatasetUndefinitionException;
import com.luck.report.core.expression.model.expr.dataset.DatasetExpression;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Column;
import com.luck.report.core.model.Report;
import com.luck.report.core.model.Row;
import com.luck.report.core.utils.ElCompute;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 * @author Jacky.gao
 * @since 2016年11月1日
 */
public class Context {
    private Report report;
    private Cell rootCell;
    private int pageIndex;
    private int totalPages;
    private boolean doPaging;
    private Map<String, Object> variableMap = new HashMap<String, Object>();
    private Map<Integer, List<Row>> currentPageRowsMap = new HashMap<Integer, List<Row>>();
    private Map<String, Dataset> datasetMap;
    private ApplicationContext applicationContext;
    private ReportBuilder reportBuilder;
    private Map<String, Object> parameters;
    private HideRowColumnBuilder hideRowColumnBuilder;
    private List<Cell> existPageFunctionCells = new ArrayList<Cell>();
    private Map<String, List<Cell>> unprocessedCellsMap = new HashMap<String, List<Cell>>();
    private Set<String> buildingCellNames = new HashSet<String>();
    private Map<Row, Map<Column, Cell>> blankCellsMap = new HashMap<Row, Map<Column, Cell>>();
    private Map<Row, Integer> fillBlankRowsMap = new HashMap<Row, Integer>();
    private Map<String, ChartData> chartDataMap = new HashMap<String, ChartData>();
    private Map<String, Map<Cell, Integer>> dataSeqIndexCache = new HashMap<String, Map<Cell, Integer>>();
    private Map<String, Integer> dataSeqSiblingCountCache = new HashMap<String, Integer>();
    private Map<String, Map<Cell, Integer>> dataRowIndexCache = new HashMap<String, Map<Cell, Integer>>();
    private Map<String, Integer> dataRowSiblingCountCache = new HashMap<String, Integer>();

    private Map<String, Map<String, String>> mappingCache = new HashMap<String, Map<String, String>>();

    public Context(ReportBuilder reportBuilder, Report report, Map<String, Dataset> datasetMap, ApplicationContext applicationContext, Map<String, Object> parameters, HideRowColumnBuilder hideRowColumnBuilder) {
        this.reportBuilder = reportBuilder;
        this.report = report;
        report.setContext(this);
        this.datasetMap = datasetMap;
        this.applicationContext = applicationContext;
        this.parameters = parameters;
        this.hideRowColumnBuilder = hideRowColumnBuilder;
        Map<String, List<Cell>> cellsMap = report.getCellsMap();
        for (String key : cellsMap.keySet()) {
            if (key.equals(report.getRootCell().getName())) {
                continue;
            }
            List<Cell> list = new ArrayList<Cell>();
            list.addAll(cellsMap.get(key));
            unprocessedCellsMap.put(key, list);
        }
        this.rootCell = new Cell();
        this.rootCell.setName("ROOT");
    }

    public Context(ApplicationContext applicationContext, Map<String, Object> parameters) {
        this.applicationContext = applicationContext;
        this.parameters = parameters;
    }

    /**
     * 取数据集映射对照表；同一次计算内按映射集+键字段+值字段缓存
     *
     * @param expr 数据集表达式，非空
     * @return 键到显示值的映射；无法构建时返回 null
     */
    public Map<String, String> getMapping(DatasetExpression expr) {
        if (expr.getMappingType().equals(MappingType.simple)) {
            Map<String, String> mapping = expr.getMapping();
            return mapping;
        } else if (expr.getMappingType().equals(MappingType.dataset)) {
            if (StringUtils.isNotBlank(expr.getMappingDataset()) && StringUtils.isNotBlank(expr.getMappingKeyProperty()) && StringUtils.isNotBlank(expr.getMappingValueProperty())) {
                String cacheKey = expr.getMappingDataset() + '\0' + expr.getMappingKeyProperty() + '\0' + expr.getMappingValueProperty();
                Map<String, String> cached = mappingCache.get(cacheKey);
                if (cached != null) {
                    return cached;
                }
                Map<String, String> mapping = new HashMap<String, String>();
                List<?> list = getDatasetData(expr.getMappingDataset());
                for (Object obj : list) {
                    Object key = Utils.getProperty(obj, expr.getMappingKeyProperty());
                    Object value = Utils.getProperty(obj, expr.getMappingValueProperty());
                    if (key != null && value != null) {
                        mapping.put(key.toString(), value.toString());
                    }
                }
                mappingCache.put(cacheKey, mapping);
                return mapping;
            }
        }
        return null;
    }

    public void doHideProcessColumn(Column col) {
        hideRowColumnBuilder.doHideProcessColumn(report, col);
    }

    public void doHideProcessRow(Row row) {
        hideRowColumnBuilder.doHideProcessRow(report, row);
    }

    public void addFillBlankRow(Row row, int value) {
        fillBlankRowsMap.put(row, value);
    }

    public Map<Row, Integer> getFillBlankRowsMap() {
        return fillBlankRowsMap;
    }

    public ReportBuilder getReportBuilder() {
        return reportBuilder;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public boolean isDoPaging() {
        return doPaging;
    }

    public void setDoPaging(boolean doPaging) {
        this.doPaging = doPaging;
    }

    public List<BindData> buildCellData(Cell cell) {
        return DataCompute.buildCellData(cell, this);
    }

    public Cell getBlankCell(Row row, Column column) {
        Map<Column, Cell> colCellMap = blankCellsMap.get(row);
        if (colCellMap == null) {
            return null;
        }
        Cell targetCell = colCellMap.get(column);
        return targetCell;
    }

    public void removeBlankCell(Cell blankCell) {
        Row row = blankCell.getRow();
        Column col = blankCell.getColumn();
        Map<Column, Cell> colCellMap = blankCellsMap.get(row);
        colCellMap.remove(col);
    }

    public void addBlankCell(Cell cell) {
        cell.setBlankCell(true);
        Row row = cell.getRow();
        Column column = cell.getColumn();
        Map<Column, Cell> cellMap = blankCellsMap.get(row);
        if (cellMap == null) {
            cellMap = new HashMap<Column, Cell>();
            blankCellsMap.put(row, cellMap);
        }
        cellMap.put(column, cell);
        addReportCell(cell);
    }

    public void addCell(Cell newCell) {
        addReportCell(newCell);
        addUnprocessedCell(newCell);
    }

    public void addUnprocessedCell(Cell cell) {
        String cellName = cell.getName();
        List<Cell> cells = null;
        if (unprocessedCellsMap.containsKey(cellName)) {
            cells = unprocessedCellsMap.get(cellName);
        } else {
            cells = new ArrayList<Cell>();
            unprocessedCellsMap.put(cellName, cells);
        }
        cells.add(cell);
    }

    public Map<Row, Map<Column, Cell>> getBlankCellsMap() {
        return blankCellsMap;
    }

    public void addReportCell(Cell newCell) {
        boolean lazyAdd = report.addCell(newCell);
        if (lazyAdd) {
            return;
        }
        finishCellAfterBind(newCell);
    }

    /**
     * 绑定数据后完成条件/格式/折行：可提前的条件立刻算，须后置的进入 lazy 列表
     *
     * @param cell 已 setData 的单元格，非空
     */
    public void finishCellAfterBind(Cell cell) {
        List<ConditionPropertyItem> items = cell.getConditionPropertyItems();
        if (items != null && !items.isEmpty()) {
            if (ConditionPropertyDeferral.mustDefer(cell)) {
                report.getLazyComputeCells().add(cell);
            } else {
                cell.doCompute(this);
            }
        } else {
            cell.doFormat();
            cell.doDataWrapCompute(this);
        }
    }

    public void addChartData(ChartData data) {
        chartDataMap.put(data.getId(), data);
    }

    public Map<String, ChartData> getChartDataMap() {
        return chartDataMap;
    }

    public Row getRow(int rowNumber) {
        return report.getRow(rowNumber);
    }


    public Column getColumn(int columnNumber) {
        return report.getColumn(columnNumber);
    }

    public Report getReport() {
        return report;
    }

    public List<?> getDatasetData(String name) {
        if (datasetMap.containsKey(name)) {
            return datasetMap.get(name).getData();
        }
        throw new DatasetUndefinitionException(name);
    }

    public Map<String, Dataset> getDatasetMap() {
        return datasetMap;
    }

    public List<Cell> nextUnprocessedCells() {
        if (unprocessedCellsMap.size() == 0) {
            return null;
        }
        List<Cell> targetCellsList = null;
        String targetCellName = null;
        Set<String> keySet = unprocessedCellsMap.keySet();
        for (String cellName : keySet) {
            List<Cell> cells = unprocessedCellsMap.get(cellName);
            Cell cell = cells.get(0);
            Value value = cell.getValue();
            Cell leftParent = cell.getLeftParentCell();
            Cell topParent = cell.getTopParentCell();
            if ((leftParent == null || leftParent.isProcessed()) && (topParent == null || topParent.isProcessed())) {
                targetCellsList = cells;
                targetCellName = cellName;
                break;
            }
            if (value instanceof SimpleValue) {
                targetCellsList = cells;
                targetCellName = cellName;
                break;
            }
        }
        if (targetCellName == null) {
            throw new CellDependencyException();
        } else {
            unprocessedCellsMap.remove(targetCellName);
        }
        return targetCellsList;
    }

    public Object evalExpr(String expression) {
        return new ElCompute().doCompute(expression);
    }

    public boolean isCellPocessed(String cellName) {
        return !unprocessedCellsMap.containsKey(cellName);
    }

    /**
     * 按名字计算尚未完成的目标格；非文本格会先算完未完成的左父格和上父格
     *
     * @param cellName 目标格名称，非空
     * @throws CellDependencyException 该名字正在计算，或父格链回到该名字时抛出
     */
    public void processTargetCell(String cellName) {
        if (!buildingCellNames.add(cellName)) {
            throw new CellDependencyException();
        }
        try {
            if (!unprocessedCellsMap.containsKey(cellName)) {
                return;
            }
            List<Cell> cells = unprocessedCellsMap.get(cellName);
            if (cells == null || cells.isEmpty()) {
                unprocessedCellsMap.remove(cellName);
                return;
            }
            Cell first = cells.get(0);
            if (!(first.getValue() instanceof SimpleValue)) {
                ensureParentProcessed(first.getLeftParentCell());
                ensureParentProcessed(first.getTopParentCell());
            }
            cells = unprocessedCellsMap.remove(cellName);
            if (cells != null && !cells.isEmpty()) {
                reportBuilder.buildCell(this, cells);
            }
        } finally {
            buildingCellNames.remove(cellName);
        }
    }

    /**
     * 标记格子名正在计算表达式，值尚未写上
     *
     * @param cellName 格子名称，非空
     */
    public void markBuilding(String cellName) {
        buildingCellNames.add(cellName);
    }

    /**
     * 表达式计算结束，取消正在计算标记
     *
     * @param cellName 格子名称，非空
     */
    public void unmarkBuilding(String cellName) {
        buildingCellNames.remove(cellName);
    }

    /**
     * 判断指定格子名是否正在计算表达式（值可能尚未写完）
     *
     * @param cellName 格子名称，非空
     * @return true 表示该名在 buildingCellNames 中
     */
    public boolean isBuilding(String cellName) {
        return buildingCellNames.contains(cellName);
    }

    /**
     * 父格尚未写上值时，先按名字把父格算完
     *
     * @param parent 左父格或上父格，可为 null
     * @throws CellDependencyException 父格正在计算时抛出
     */
    private void ensureParentProcessed(Cell parent) {
        if (parent == null || parent.isProcessed()) {
            return;
        }
        processTargetCell(parent.getName());
    }

    /**
     * 在报表尾部追加一行（走 Report 链挂接与索引重建）
     *
     * @param row 待追加行，非空
     */
    public void addRow(Row row) {
        this.report.appendRow(row);
    }

    /**
     * 在报表尾部追加一列（走 Report 链挂接与索引重建）
     *
     * @param column 待追加列，非空
     */
    public void addColumn(Column column) {
        this.report.appendColumn(column);
    }

    public int getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(int pageIndex) {
        this.pageIndex = pageIndex;
    }

    public void setCurrentPageRows(int pageIndex, List<Row> currentPageRows) {
        currentPageRowsMap.put(pageIndex, currentPageRows);
    }

    public List<Row> getCurrentPageRows(int pageIndex) {
        return currentPageRowsMap.get(pageIndex);
    }

    public void addExistPageFunctionCells(Cell cell) {
        existPageFunctionCells.add(cell);
    }

    public List<Cell> getExistPageFunctionCells() {
        return existPageFunctionCells;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public Cell getRootCell() {
        return rootCell;
    }

    public void putVariable(String key, Object value) {
        variableMap.put(key, value);
    }

    public void resetVariableMap() {
        variableMap.clear();
    }

    public Object getVariable(String key) {
        return variableMap.get(key);
    }

    /** 同名实例数量变化时返回 null，由调用方重建 */
    public Map<Cell, Integer> getDataSeqIndexCache(String cellName, int siblingCount) {
        Integer cachedCount = dataSeqSiblingCountCache.get(cellName);
        if (cachedCount == null || cachedCount.intValue() != siblingCount) {
            return null;
        }
        return dataSeqIndexCache.get(cellName);
    }

    public void putDataSeqIndexCache(String cellName, int siblingCount, Map<Cell, Integer> indexMap) {
        dataSeqSiblingCountCache.put(cellName, Integer.valueOf(siblingCount));
        dataSeqIndexCache.put(cellName, indexMap);
    }

    /** 同名实例数量变化时返回 null，由调用方重建 */
    public Map<Cell, Integer> getDataRowIndexCache(String cellName, int siblingCount) {
        Integer cachedCount = dataRowSiblingCountCache.get(cellName);
        if (cachedCount == null || cachedCount.intValue() != siblingCount) {
            return null;
        }
        return dataRowIndexCache.get(cellName);
    }

    public void putDataRowIndexCache(String cellName, int siblingCount, Map<Cell, Integer> indexMap) {
        dataRowSiblingCountCache.put(cellName, Integer.valueOf(siblingCount));
        dataRowIndexCache.put(cellName, indexMap);
    }
}
