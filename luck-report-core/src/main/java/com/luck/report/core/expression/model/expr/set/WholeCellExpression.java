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

import com.luck.report.core.build.Context;
import com.luck.report.core.expression.model.Condition;
import com.luck.report.core.expression.model.data.ExpressionData;
import com.luck.report.core.expression.model.data.ObjectExpressionData;
import com.luck.report.core.expression.model.data.ObjectListExpressionData;
import com.luck.report.core.model.Cell;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jacky.gao
 * @since 2017年4月6日
 */
public class WholeCellExpression extends CellExpression {
    private static final long serialVersionUID = 1L;
    private Condition condition;

    public WholeCellExpression() {
        super(null);
    }

    public WholeCellExpression(String cellName) {
        super(cellName);
    }

    @Override
    public boolean supportPaging() {
        return false;
    }

    /**
     * 取该名字下全部单元格的值；未算完时按名字先算完
     *
     * @param cell 正在计算的单元格，可为 null
     * @param currentCell 当前单元格，可为 null
     * @param context 本次报表计算上下文，非空
     * @return 仅一个值时返回单值，否则返回列表
     * @throws com.luck.report.core.exception.CellDependencyException 目标格正在计算时抛出
     */
    @Override
    protected ExpressionData<?> compute(Cell cell, Cell currentCell, Context context) {
        context.processTargetCell(cellName);
        List<Cell> cells = context.getReport().getCellsMap().get(cellName);
        List<Object> list = new ArrayList<Object>();
        for (Cell c : cells) {
            Object obj = c.getData();
            if (condition != null) {
                boolean result = condition.filter(cell, currentCell, obj, context);
                if (!result) {
                    continue;
                }
            }
            list.add(obj);
        }
        if (list.size() == 1) {
            return new ObjectExpressionData(list.get(0));
        } else {
            return new ObjectListExpressionData(list);
        }
    }

    /**
     * 获取条件
     * @return 条件
     */
    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    @Override
    public List<String> fetchCellName() {
        List<String> list = new ArrayList<String>();
        if (cellName != null && !cellName.isEmpty()) {
            list.add(cellName);
        }
        if (condition != null) {
            list.addAll(condition.fetchCellName());
        }
        return list;
    }
}
