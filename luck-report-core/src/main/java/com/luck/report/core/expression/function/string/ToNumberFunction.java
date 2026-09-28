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
import com.luck.report.core.build.BindData;
import com.luck.report.core.build.Context;
import com.luck.report.core.expression.model.data.BindDataListExpressionData;
import com.luck.report.core.expression.model.data.ExpressionData;
import com.luck.report.core.expression.model.data.ObjectExpressionData;
import com.luck.report.core.expression.model.data.ObjectListExpressionData;
import com.luck.report.core.model.Cell;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 将字符串转换为数字，便于参与算术运算。
 */
public class ToNumberFunction extends StringFunction {

    @Override
    public Object execute(List<ExpressionData<?>> dataList, Context context, Cell currentCell) {
        if (dataList == null || dataList.isEmpty()) {
            return "";
        }
        Object obj = buildExpressionData(dataList.get(0));
        if (obj == null) {
            return null;
        }
        String text = String.valueOf(obj).trim();
        if (StringUtils.isBlank(text)) {
            return null;
        }
        return Utils.toBigDecimal(text);
    }

    private Object buildExpressionData(ExpressionData<?> data) {
        if (data instanceof ObjectListExpressionData) {
            List<?> list = ((ObjectListExpressionData) data).getData();
            if (list != null && !list.isEmpty()) {
                return list.get(0);
            }
        } else if (data instanceof ObjectExpressionData) {
            return ((ObjectExpressionData) data).getData();
        } else if (data instanceof BindDataListExpressionData) {
            List<BindData> list = ((BindDataListExpressionData) data).getData();
            if (list != null && !list.isEmpty()) {
                return list.get(0).getValue();
            }
        }
        return null;
    }

    @Override
    public String name() {
        return "toNumber";
    }
}
