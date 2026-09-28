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
package com.luck.report.core.expression.model.expr.set;

import com.luck.report.core.Utils;
import com.luck.report.core.build.Context;
import com.luck.report.core.exception.ReportComputeException;
import com.luck.report.core.expression.model.Condition;
import com.luck.report.core.expression.model.data.ExpressionData;
import com.luck.report.core.expression.model.data.ObjectExpressionData;
import com.luck.report.core.expression.model.data.ObjectListExpressionData;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Column;
import com.luck.report.core.model.Row;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;


/**
 * @author Jacky.gao
 * @since 2017年1月1日
 */
public class CellCoordinateExpression extends CellExpression {
    private static final long serialVersionUID = 1L;
    private Condition condition;
    private CellCoordinateSet leftCoordinate;
    private CellCoordinateSet topCoordinate;

    public CellCoordinateExpression() {
        super(null);
    }

    public CellCoordinateExpression(String cellName, CellCoordinateSet leftCoordinate) {
        super(cellName);
        this.leftCoordinate = leftCoordinate;
    }

    public CellCoordinateExpression(String cellName, CellCoordinateSet leftCoordinate, Condition condition) {
        super(cellName);
        this.leftCoordinate = leftCoordinate;
        this.condition = condition;
    }

    public CellCoordinateExpression(String cellName, CellCoordinateSet leftCoordinate, CellCoordinateSet topCoordinate) {
        super(cellName);
        this.leftCoordinate = leftCoordinate;
        this.topCoordinate = topCoordinate;
    }

    public CellCoordinateExpression(String cellName, CellCoordinateSet leftCoordinate, CellCoordinateSet topCoordinate, Condition condition) {
        super(cellName);
        this.leftCoordinate = leftCoordinate;
        this.topCoordinate = topCoordinate;
        this.condition = condition;
    }

    @Override
    public boolean supportPaging() {
        return false;
    }

    /**
     * 按坐标取出目标格的值；目标格和坐标格未算完时按名字先算完
     *
     * <p>目标名正在计算时跳过 {@code processTargetCell}（避免同名坐标自引用如
     * {@code D2[A2:-1]} 被误判为循环依赖），只收集已 processed 实例的值。
     *
     * @param cell 正在计算的单元格，不可为空
     * @param currentCell 当前单元格，可为 null
     * @param context 报表计算上下文，不可为空
     * @return 命中一个值时返回单值，否则返回列表；无已完成实例时返回空列表数据
     * @throws com.luck.report.core.exception.CellDependencyException 坐标格正在计算且形成真环时抛出
     */
    @Override
    protected ExpressionData<?> compute(Cell cell, Cell currentCell, Context context) {
        if (!context.isBuilding(cellName)) {
            context.processTargetCell(cellName);
        }
        List<Cell> leftCellList = buildLeftCells(cell, context);
        List<Cell> topCellList = buildTopCells(cell, context);
        List<Object> list = new ArrayList<Object>();
        if (leftCellList == null) {
            if (topCellList != null) {
                topCellList = filterCells(cell, context, condition, topCellList);
                addProcessedData(list, topCellList);
            } else {
                List<Cell> cells = context.getReport().getCellsMap().get(cellName);
                if (cells == null) {
                    throw new ReportComputeException("Cell [" + cellName + "] not exist.");
                }
                topCellList = filterCells(cell, context, condition, cells);
                addProcessedData(list, topCellList);
            }
        } else {
            if (topCellList != null) {
                leftCellList = filterCells(cell, context, condition, leftCellList);
                topCellList = filterCells(cell, context, condition, topCellList);
                for (Cell c : topCellList) {
                    if (leftCellList.contains(c) && c.isProcessed()) {
                        list.add(c.getData());
                    }
                }
            } else {
                leftCellList = filterCells(cell, context, condition, leftCellList);
                addProcessedData(list, leftCellList);
            }
        }
        if (list.size() == 1) {
            return new ObjectExpressionData(list.get(0));
        } else {
            return new ObjectListExpressionData(list);
        }
    }

    /**
     * 只收集已计算完成的格子数据，跳过正在计算或尚未写值的实例
     *
     * @param list 结果列表，不可为空
     * @param cells 候选格子，不可为空
     */
    private static void addProcessedData(List<Object> list, List<Cell> cells) {
        for (Cell c : cells) {
            if (c.isProcessed()) {
                list.add(c.getData());
            }
        }
    }

    /**
     * 按左父格坐标取出目标单元格
     *
     * @param cell 正在计算的单元格，不可为空
     * @param context 报表计算上下文，不可为空
     * @return 命中的单元格；未写左坐标时返回 null，坐标对不上时返回空列表
     * @throws com.luck.report.core.exception.CellDependencyException 坐标格正在计算时抛出
     */
    private List<Cell> buildLeftCells(Cell cell, Context context) {
        if (leftCoordinate == null) {
            return null;
        }
        List<Cell> cellList = null;
        Cell targetLeftCell = null;
        Row row = cell.getRow();
        int rowNumber = row.getRowNumber();
        List<CellCoordinate> leftCoordinates = leftCoordinate.getCellCoordinates();
        for (CellCoordinate coordinate : leftCoordinates) {
            String name = coordinate.getCellName();
            context.processTargetCell(name);
            if (targetLeftCell == null) {
                if (coordinate.getCoordinateType().equals(CoordinateType.relative)) {
                    cellList = Utils.fetchTargetCells(cell, context, name);
                } else {
                    cellList = context.getReport().getCellsMap().get(name);
                }
            } else {
                cellList = childrenOrNull(targetLeftCell.getRowChildrenCellsMap(), name);
            }
            if (cellList == null || cellList.isEmpty()) {
                return Collections.emptyList();
            }
            int position = coordinate.getPosition();
            if (position == 0) {
                for (Cell childCell : cellList) {
                    Row childRow = childCell.getRow();
                    if (row == childRow) {
                        targetLeftCell = childCell;
                        break;
                    }
                    int rowSpan = childCell.getRowSpan();
                    if (rowSpan > 0) {
                        int childRowNumberStart = childRow.getRowNumber();
                        int childRowNumberEnd = childRowNumberStart + rowSpan - 1;
                        if (childRowNumberStart <= rowNumber && childRowNumberEnd >= rowNumber) {
                            targetLeftCell = childCell;
                            break;
                        }
                    }
                }
            } else {
                if (position > 0) {
                    targetLeftCell = cellList.get(position - 1);
                } else if (position < 0) {
                    boolean reverse = coordinate.isReverse();
                    if (!reverse) {
                        cellList = sortCellsByRow(cellList);
                    }
                    int cellSize = cellList.size();
                    if (reverse) {
                        int newPosition = cellSize - position;
                        if (newPosition >= cellSize) {
                            newPosition = cellSize - 1;
                        }
                        targetLeftCell = cellList.get(newPosition);
                    } else {
                        int index = 0;
                        for (int i = 0; i < cellSize; i++) {
                            Cell childCell = cellList.get(i);
                            if (childCell.getRow() == cell.getRow()) {
                                index = i;
                                break;
                            }
                            int rowSpan = childCell.getRowSpan();
                            if (rowSpan > 1) {
                                int rowNum = childCell.getRow().getRowNumber();
                                int start = rowNum, end = rowNum + rowSpan - 1;
                                if (rowNumber >= start && rowNumber <= end) {
                                    index = i;
                                    break;
                                }
                            }
                        }
                        int newPosition = index + position;
                        if (newPosition < 0) {
                            newPosition = 0;
                        }
                        if (newPosition >= cellSize) {
                            newPosition = cellSize - 1;
                        }
                        targetLeftCell = cellList.get(newPosition);
                    }
                }
            }
            if (targetLeftCell == null) {
                return Collections.emptyList();
            }
        }
        return childrenOrNull(targetLeftCell.getRowChildrenCellsMap(), cellName);
    }

    /**
     * 按上父格坐标取出目标单元格
     *
     * @param cell 正在计算的单元格，不可为空
     * @param context 报表计算上下文，不可为空
     * @return 命中的单元格；未写上坐标时返回 null，坐标对不上时返回空列表
     * @throws com.luck.report.core.exception.CellDependencyException 坐标格正在计算时抛出
     */
    private List<Cell> buildTopCells(Cell cell, Context context) {
        if (topCoordinate == null) {
            return null;
        }
        List<Cell> cellList = null;
        Cell targetTopCell = null;
        Column col = cell.getColumn();
        int colNumber = col.getColumnNumber();
        List<CellCoordinate> topCoordinates = topCoordinate.getCellCoordinates();
        for (CellCoordinate coordinate : topCoordinates) {
            String name = coordinate.getCellName();
            context.processTargetCell(name);
            if (cellList == null) {
                if (coordinate.getCoordinateType().equals(CoordinateType.relative)) {
                    cellList = Utils.fetchTargetCells(cell, context, name);
                } else {
                    cellList = context.getReport().getCellsMap().get(name);
                }
            } else {
                cellList = childrenOrNull(targetTopCell.getColumnChildrenCellsMap(), name);
            }
            if (cellList == null || cellList.isEmpty()) {
                return Collections.emptyList();
            }
            int position = coordinate.getPosition();
            if (position == 0) {
                for (Cell childCell : cellList) {
                    Column childCol = childCell.getColumn();
                    if (col == childCol) {
                        targetTopCell = childCell;
                        break;
                    }
                    int colSpan = childCell.getColSpan();
                    if (colSpan > 0) {
                        int childColNumberStart = childCol.getColumnNumber();
                        int childColNumberEnd = childColNumberStart + colSpan - 1;
                        if (childColNumberStart <= colNumber && childColNumberEnd >= colNumber) {
                            targetTopCell = childCell;
                            break;
                        }
                    }
                }
            } else {
                if (position > 0) {
                    targetTopCell = cellList.get(position - 1);
                } else if (position < 0) {
                    boolean reverse = coordinate.isReverse();
                    if (!reverse) {
                        cellList = sortCellsByColumn(cellList);
                    }
                    int cellSize = cellList.size();
                    if (reverse) {
                        int newPosition = cellSize - position;
                        if (newPosition >= cellSize) {
                            newPosition = cellSize - 1;
                        }
                        targetTopCell = cellList.get(newPosition);
                    } else {
                        int index = 0;
                        for (int i = 0; i < cellSize; i++) {
                            Cell childCell = cellList.get(i);
                            if (childCell.getColumn() == cell.getColumn()) {
                                index = i;
                                break;
                            }
                            int colSpan = childCell.getColSpan();
                            if (colSpan > 1) {
                                int colNum = childCell.getColumn().getColumnNumber();
                                int start = colNum, end = colNum + colSpan - 1;
                                if (colNumber >= start && colNumber <= end) {
                                    index = i;
                                    break;
                                }
                            }
                        }
                        int newPosition = index + position;
                        if (newPosition < 0) {
                            newPosition = 0;
                        }
                        if (newPosition >= cellSize) {
                            newPosition = cellSize - 1;
                        }
                        targetTopCell = cellList.get(newPosition);
                    }
                }
            }
            if (targetTopCell == null) {
                return Collections.emptyList();
            }
        }
        return childrenOrNull(targetTopCell.getColumnChildrenCellsMap(), cellName);
    }

    /**
     * 从父格的子格映射中取出指定名称的单元格
     *
     * @param children 父格的行子格或列子格映射，可为空
     * @param cellName 目标单元格名称，不可为空
     * @return 子格列表；映射不存在时返回空列表，没有该名称时返回 null（与原先 map.get 一致）
     */
    private List<Cell> childrenOrNull(Map<String, List<Cell>> children, String cellName) {
        if (children == null) {
            return Collections.emptyList();
        }
        return children.get(cellName);
    }

    /**
     * 按行号排序单元格副本，供负向坐标取上一格。
     * @param cells 原单元格列表
     * @return 按行号排序后的新列表
     */
    private List<Cell> sortCellsByRow(List<Cell> cells) {
        List<Cell> sorted = new ArrayList<Cell>(cells);
        Collections.sort(sorted, new Comparator<Cell>() {
            @Override
            public int compare(Cell a, Cell b) {
                int ra = a.getRow() == null ? 0 : a.getRow().getRowNumber();
                int rb = b.getRow() == null ? 0 : b.getRow().getRowNumber();
                if (ra != rb) {
                    return ra - rb;
                }
                int ca = a.getColumn() == null ? 0 : a.getColumn().getColumnNumber();
                int cb = b.getColumn() == null ? 0 : b.getColumn().getColumnNumber();
                return ca - cb;
            }
        });
        return sorted;
    }

    /**
     * 按列号排序单元格副本，供负向坐标取上一格。
     * @param cells 原单元格列表
     * @return 按列号排序后的新列表
     */
    private List<Cell> sortCellsByColumn(List<Cell> cells) {
        List<Cell> sorted = new ArrayList<Cell>(cells);
        Collections.sort(sorted, new Comparator<Cell>() {
            @Override
            public int compare(Cell a, Cell b) {
                int ca = a.getColumn() == null ? 0 : a.getColumn().getColumnNumber();
                int cb = b.getColumn() == null ? 0 : b.getColumn().getColumnNumber();
                if (ca != cb) {
                    return ca - cb;
                }
                int ra = a.getRow() == null ? 0 : a.getRow().getRowNumber();
                int rb = b.getRow() == null ? 0 : b.getRow().getRowNumber();
                return ra - rb;
            }
        });
        return sorted;
    }

    /**
     * 获取条件
     * @return 条件
     */
    public Condition getCondition() {
        return condition;
    }

    /**
     * 设置条件
     * @param condition 条件
     */
    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    /**
     * 获取左侧坐标集
     * @return 左侧坐标集
     */
    public CellCoordinateSet getLeftCoordinate() {
        return leftCoordinate;
    }

    /**
     * 设置左侧坐标集
     * @param leftCoordinate 左侧坐标集
     */
    public void setLeftCoordinate(CellCoordinateSet leftCoordinate) {
        this.leftCoordinate = leftCoordinate;
    }

    /**
     * 获取顶部坐标集
     * @return 顶部坐标集
     */
    public CellCoordinateSet getTopCoordinate() {
        return topCoordinate;
    }

    /**
     * 设置顶部坐标集
     * @param topCoordinate 顶部坐标集
     */
    public void setTopCoordinate(CellCoordinateSet topCoordinate) {
        this.topCoordinate = topCoordinate;
    }

    @Override
    public List<String> fetchCellName() {
        List<String> list = new ArrayList<String>();
        if (condition != null) {
            list.addAll(condition.fetchCellName());
        }
        if (leftCoordinate != null) {
            List<CellCoordinate> cellCoordinates = leftCoordinate.getCellCoordinates();
            if (cellCoordinates != null && cellCoordinates.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (CellCoordinate c : cellCoordinates) {
                    sb.append(c.getCellName()).append(":");
                }
                sb.append(cellName);
                list.add(sb.toString());
            }
        }
        if (topCoordinate != null) {
            List<CellCoordinate> cellCoordinates = topCoordinate.getCellCoordinates();
            if (cellCoordinates != null && cellCoordinates.size() > 0) {
                StringBuilder sb = new StringBuilder();
                for (CellCoordinate c : cellCoordinates) {
                    sb.append(c.getCellName()).append(":");
                }
                sb.append(cellName);
                list.add(sb.toString());
            }
        }
        return list;
    }
}
