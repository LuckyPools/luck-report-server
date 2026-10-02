package com.luck.report.core.chart.option.impl;

import com.luck.report.core.chart.option.Option;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 图表系列/扇区自定义调色板。不参与 Chart.js options JSON，由 Dataset 取色消费。
 */
public class ColorsOption implements Option, Serializable {
    private static final long serialVersionUID = 1L;

    private List<String> colors = new ArrayList<>();

    public ColorsOption() {}

    @Override
    public String buildOptionJson() {
        return null;
    }

    @Override
    public String getType() {
        return "colors";
    }

    public void setType(String type) {
    }

    public List<String> getColors() {
        return colors;
    }

    public void setColors(List<String> colors) {
        this.colors = colors != null ? colors : new ArrayList<>();
    }
}
