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
package com.luck.report.core.build.aggregate;

import com.luck.report.core.Utils;
import com.luck.report.core.build.BindData;
import com.luck.report.core.build.Context;
import com.luck.report.core.definition.Order;
import com.luck.report.core.definition.value.Value;
import com.luck.report.core.expression.model.Condition;
import com.luck.report.core.expression.model.expr.dataset.DatasetExpression;
import com.luck.report.core.model.Cell;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Jacky.gao
 * @since 2016年12月21日
 */
public abstract class Aggregate {
    public abstract List<BindData> aggregate(DatasetExpression expr, Cell cell, Context context);

    /**
     * 取同数据集父格上的绑定行；父格已定位但未带行时按空约束，不能当成无父格回退全表
     *
     * @param parent 同数据集左/上父格，可为 null
     * @return 父格 bindData；无父格返回 null，父格未带行返回空列表
     */
    protected List<Object> parentBindData(Cell parent) {
        if (parent == null) {
            return null;
        }
        List<Object> data = parent.getBindData();
        if (data == null) {
            return Collections.emptyList();
        }
        return data;
    }

    protected Condition getCondition(Cell cell) {
        Value value = cell.getValue();
        Condition condition = null;
        if (value instanceof DatasetExpression) {
            DatasetExpression dsValue = (DatasetExpression) value;
            condition = dsValue.getCondition();
        }
        return condition;
    }

    protected Object mappingData(Map<String, String> mappingMap, Object data) {
        if (mappingMap == null || data == null) {
            return data;
        }
        String label = mappingMap.get(data.toString());
        if (label == null) {
            return data;
        }
        return label;
    }

    protected boolean doCondition(Condition condition, Cell cell, Object obj, Context context) {
        if (condition == null) {
            return true;
        }
        return condition.filter(cell, cell, obj, context);
    }

    /**
     * 按单元格排序方式排列绑定数据
     */
    protected void orderBindDataList(List<BindData> list, final Order order) {
        if (order == null || order.equals(Order.none)) {
            return;
        }
        Collections.sort(list, new Comparator<BindData>() {
            @Override
            public int compare(BindData o1, BindData o2) {
                Object data1 = o1.getValue();
                Object data2 = o2.getValue();
                if (data1 == null || data2 == null) {
                    if (data1 == data2) {
                        return 0;
                    }
                    return data1 == null ? -1 : 1;
                }
                if (data1 instanceof Date) {
                    Date d1 = (Date) data1;
                    Date d2 = (Date) data2;
                    if (order.equals(Order.asc)) {
                        return d1.compareTo(d2);
                    } else {
                        return d2.compareTo(d1);
                    }
                } else if (data1 instanceof Number) {
                    int result = compareNumbers((Number) data1, data2);
                    return order.equals(Order.asc) ? result : -result;
                } else {
                    String str1 = data1.toString();
                    String str2 = data2.toString();
                    if (order.equals(Order.asc)) {
                        return str1.compareTo(str2);
                    } else {
                        return str2.compareTo(str1);
                    }
                }
            }
        });
    }

    /**
     * 数字比较：同类型走原生 compare，否则落到 BigDecimal
     *
     * @param n1 左值，非空
     * @param data2 右值，非空
     * @return 负/零/正
     */
    private static int compareNumbers(Number n1, Object data2) {
        if (n1 instanceof BigDecimal && data2 instanceof BigDecimal) {
            return ((BigDecimal) n1).compareTo((BigDecimal) data2);
        }
        if (n1 instanceof Integer && data2 instanceof Integer) {
            return Integer.compare((Integer) n1, (Integer) data2);
        }
        if (n1 instanceof Long && data2 instanceof Long) {
            return Long.compare((Long) n1, (Long) data2);
        }
        if (n1 instanceof Double && data2 instanceof Double) {
            return Double.compare((Double) n1, (Double) data2);
        }
        if (n1 instanceof Float && data2 instanceof Float) {
            return Float.compare((Float) n1, (Float) data2);
        }
        BigDecimal a = Utils.toBigDecimal(n1);
        BigDecimal b = Utils.toBigDecimal(data2);
        return a.compareTo(b);
    }
}
