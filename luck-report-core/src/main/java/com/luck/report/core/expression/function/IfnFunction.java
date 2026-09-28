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
package com.luck.report.core.expression.function;

import com.luck.report.core.build.BindData;
import com.luck.report.core.build.Context;
import com.luck.report.core.exception.ReportComputeException;
import com.luck.report.core.expression.model.data.BindDataListExpressionData;
import com.luck.report.core.expression.model.data.ExpressionData;
import com.luck.report.core.expression.model.data.ObjectExpressionData;
import com.luck.report.core.expression.model.data.ObjectListExpressionData;
import com.luck.report.core.model.Cell;

import java.util.ArrayList;
import java.util.List;

/**
 * 若第一个参数为空（null / 空串 / 空列表），则返回第二个参数，否则返回第一个参数。
 */
public class IfnFunction implements Function {

    @Override
    public Object execute(List<ExpressionData<?>> dataList, Context context, Cell currentCell) {
        if (dataList == null || dataList.size() != 2) {
            throw new ReportComputeException("Function [" + name() + "] need two parameters.");
        }
        Object first = unwrap(dataList.get(0));
        if (isEmpty(first)) {
            return unwrap(dataList.get(1));
        }
        return first;
    }

    private Object unwrap(ExpressionData<?> data) {
        if (data == null) {
            return null;
        }
        if (data instanceof ObjectExpressionData) {
            return ((ObjectExpressionData) data).getData();
        }
        if (data instanceof ObjectListExpressionData) {
            List<?> list = ((ObjectListExpressionData) data).getData();
            if (list == null || list.isEmpty()) {
                return null;
            }
            if (list.size() == 1) {
                return list.get(0);
            }
            return list;
        }
        if (data instanceof BindDataListExpressionData) {
            List<BindData> list = ((BindDataListExpressionData) data).getData();
            if (list == null || list.isEmpty()) {
                return null;
            }
            if (list.size() == 1) {
                return list.get(0).getValue();
            }
            List<Object> values = new ArrayList<Object>();
            for (BindData bindData : list) {
                values.add(bindData.getValue());
            }
            return values;
        }
        return null;
    }

    private boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof List) {
            return ((List<?>) obj).isEmpty();
        }
        return obj.toString().trim().equals("");
    }

    @Override
    public String name() {
        return "ifn";
    }
}
