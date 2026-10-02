/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.domain.vo.dataset;

import java.util.List;
import java.util.Map;

/**
 * @author Jacky.gao
 * @since 2017年3月13日
 */
public class DataResult {
    private List<Map<String, Object>> data;
    private List<String> fields;
    private int total;
    private int currentTotal;

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getCurrentTotal() {
        return currentTotal;
    }

    public void setCurrentTotal(int currentTotal) {
        this.currentTotal = currentTotal;
    }
}
