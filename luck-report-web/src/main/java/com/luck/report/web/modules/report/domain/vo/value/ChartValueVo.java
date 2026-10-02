/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.domain.vo.value;

import com.luck.report.core.chart.Chart;
import com.luck.report.core.definition.value.ValueType;

import java.io.Serializable;

/**
 * ChartValue的VO类，用于前端展示
 *
 * @author LuckyPools
 * @since 2026年
 */
public class ChartValueVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Chart chart;
    private String value;
    private ValueType type;

    /**
     * 默认无参构造器
     */
    public ChartValueVo() {}

    public Chart getChart() {
        return chart;
    }

    public void setChart(Chart chart) {
        this.chart = chart;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ValueType getType() {
        return type;
    }

    public void setType(ValueType type) {
        this.type = type;
    }
}
