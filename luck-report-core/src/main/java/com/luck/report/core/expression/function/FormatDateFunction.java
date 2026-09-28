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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Jacky.gao
 * @since 2017年5月23日
 */
public class FormatDateFunction implements Function {
    private final String defaultPattern = "yyyy-MM-dd HH:mm:ss";
    @Override
    public Object execute(List<ExpressionData<?>> dataList, Context context, Cell currentCell) {
        if (dataList == null || dataList.isEmpty()) {
            return "";
        }
        Object obj = null;
        String pattern = defaultPattern;

        ExpressionData<?> dateData = dataList.get(0);
        if (dateData instanceof ObjectListExpressionData) {
            List<?> list = ((ObjectListExpressionData) dateData).getData();
            if (!list.isEmpty()) {
                obj = list.get(0);
            }
            if (list.size() > 1) {
                pattern = list.get(1).toString();
            }
        } else if (dateData instanceof ObjectExpressionData) {
            obj = ((ObjectExpressionData) dateData).getData();
        } else if (dateData instanceof BindDataListExpressionData) {
            List<BindData> list = ((BindDataListExpressionData) dateData).getData();
            if (!list.isEmpty()) {
                obj = list.get(0).getValue();
            }
            if (list.size() > 1) {
                pattern = list.get(1).getValue().toString();
            }
        }

        if (dataList.size() > 1) {
            Object patternObj = extractSimpleValue(dataList.get(1));
            if (patternObj != null) {
                pattern = patternObj.toString();
            }
        }

        if (obj == null) {
            throw new ReportComputeException("Function [formatdate] need a Date type parameter at least");
        }
        if (obj instanceof Date) {
            SimpleDateFormat sd = new SimpleDateFormat(pattern);
            return sd.format((Date) obj);
        }
        throw new ReportComputeException("Function [formatdate] first parameter is Date type");
    }

    /**
     * 从ExpressionData中提取简单值，用于获取格式串参数
     * @param data ExpressionData参数，可为空
     * @return 提取到的值，可能为null
     */
    private Object extractSimpleValue(ExpressionData<?> data) {
        if (data instanceof ObjectListExpressionData) {
            List<?> list = ((ObjectListExpressionData) data).getData();
            if (!list.isEmpty()) {
                return list.get(0);
            }
        } else if (data instanceof ObjectExpressionData) {
            return ((ObjectExpressionData) data).getData();
        } else if (data instanceof BindDataListExpressionData) {
            List<BindData> list = ((BindDataListExpressionData) data).getData();
            if (!list.isEmpty()) {
                return list.get(0).getValue();
            }
        }
        return null;
    }

    @Override
    public String name() {
        return "formatdate";
    }
}

