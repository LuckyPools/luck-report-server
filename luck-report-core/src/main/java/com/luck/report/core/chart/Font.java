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
package com.luck.report.core.chart;

import java.io.Serializable;

/**
 * 数据标签字体配置（对应 ECharts series.label.fontSize/color/fontWeight）；用于 DataLabelsPlugin 的 font 字段，独立于 FontStyle 枚举，避免语义混淆。调用方：DataLabelsPlugin.toJson() 消费本类输出 JSON
 */
public class Font implements Serializable {
    private static final long serialVersionUID = 1L;

    public Font() {}
    private int size = 12;
    private String color = "#333";
    private String weight = "normal";

    /**
     * 序列化为 Chart.js datalabels.font 风格 JSON（供前端 chartToEcharts 消费）
     * @return JSON 字符串，如 {"size":12,"color":"#333","weight":"normal"}
     */
    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"size\":").append(size).append(",");
        sb.append("\"color\":\"").append(color).append("\",");
        sb.append("\"weight\":\"").append(weight).append("\"");
        sb.append("}");
        return sb.toString();
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }
}
