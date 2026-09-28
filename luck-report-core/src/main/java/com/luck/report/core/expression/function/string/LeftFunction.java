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
package com.luck.report.core.expression.function.string;

import com.luck.report.core.Utils;
import com.luck.report.core.build.Context;
import com.luck.report.core.exception.ReportComputeException;
import com.luck.report.core.expression.model.data.ExpressionData;
import com.luck.report.core.expression.model.data.ObjectExpressionData;
import com.luck.report.core.model.Cell;

import java.util.List;

/**
 * 取字符串左边指定长度的子串。n&lt;0 时按 length+n 计算长度。
 */
public class LeftFunction extends StringFunction {

    @Override
    public Object execute(List<ExpressionData<?>> dataList, Context context, Cell currentCell) {
        if (dataList == null || dataList.size() < 2) {
            throw new ReportComputeException("Function [" + name() + "] need two parameters.");
        }
        String text = buildString(dataList);
        int n = buildLength(dataList.get(1));
        if (n < 0) {
            n = text.length() + n;
        }
        if (n <= 0) {
            return "";
        }
        if (n >= text.length()) {
            return text;
        }
        return text.substring(0, n);
    }

    private int buildLength(ExpressionData<?> exprData) {
        if (exprData instanceof ObjectExpressionData) {
            Object obj = ((ObjectExpressionData) exprData).getData();
            if (obj == null) {
                throw new ReportComputeException("Function [" + name() + "] second parameter can not be null.");
            }
            return Utils.toBigDecimal(obj).intValue();
        }
        throw new ReportComputeException("Function [" + name() + "] length data is invalid : " + exprData);
    }

    @Override
    public String name() {
        return "left";
    }
}
