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
package com.luck.report.core.expression.model.expr.cell;

import com.luck.report.core.build.Context;
import com.luck.report.core.expression.model.data.ExpressionData;
import com.luck.report.core.expression.model.data.ObjectExpressionData;
import com.luck.report.core.expression.model.expr.BaseExpression;
import com.luck.report.core.model.Cell;

/**
 * @author Jacky.gao
 * @since 2017年1月20日
 */
public class CellValueExpression extends BaseExpression {
    private static final long serialVersionUID = 1L;

    public CellValueExpression() {}

    /**
     * 读取当前格的值；当前格已在计算中，不再等待队列
     *
     * @param cell 正在计算的单元格，非空
     * @param currentCell 当前单元格，可为 null
     * @param context 本次报表计算上下文，可未使用
     * @return 当前格已写入的值，尚未写入时为 null
     */
    @Override
    protected ExpressionData<?> compute(Cell cell, Cell currentCell, Context context) {
        return new ObjectExpressionData(cell.getData());
    }
}
