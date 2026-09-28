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

import com.luck.report.core.build.Context;
import com.luck.report.core.exception.ReportComputeException;
import com.luck.report.core.expression.model.data.ExpressionData;
import com.luck.report.core.expression.model.data.ObjectExpressionData;
import com.luck.report.core.model.Cell;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 按分隔符拆分字符串，返回列表。配合 get 取某一段，例如 get(split(s,";"),-1)。
 */
public class SplitFunction extends StringFunction {

    @Override
    public Object execute(List<ExpressionData<?>> dataList, Context context, Cell currentCell) {
        if (dataList == null || dataList.size() < 2) {
            throw new ReportComputeException("Function [" + name() + "] need two parameters.");
        }
        String text = buildString(dataList);
        String separator = buildSeparator(dataList.get(1));
        if (separator == null) {
            throw new ReportComputeException("Function [" + name() + "] separator can not be null.");
        }
        String[] parts = text.split(Pattern.quote(separator), -1);
        return new ArrayList<String>(Arrays.asList(parts));
    }

    private String buildSeparator(ExpressionData<?> exprData) {
        if (exprData instanceof ObjectExpressionData) {
            Object obj = ((ObjectExpressionData) exprData).getData();
            if (obj == null) {
                return null;
            }
            return obj.toString();
        }
        throw new ReportComputeException("Function [" + name() + "] separator data is invalid : " + exprData);
    }

    @Override
    public String name() {
        return "split";
    }
}
