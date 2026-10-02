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
package com.luck.report.core.chart.option;

import com.luck.report.core.chart.FontStyle;

import java.io.Serializable;

/**
 * @author Jacky.gao
 * @since 2017年6月8日
 */
public class Labels implements Serializable {
    private static final long serialVersionUID = 1L;

    public Labels() {}
    private int boxWidth = 40;
    private int fontSize = 12;
    private FontStyle fontStyle = FontStyle.normal;
    private String fontColor = "#666";
    private int padding = 10;
    private int itemWidth = 25;
    private int itemHeight = 14;
    private int itemGap = 10;
    private String fontWeight = "normal";

    public String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"boxWidth\":").append(boxWidth).append(",");
        sb.append("\"fontSize\":").append(fontSize).append(",");
        sb.append("\"fontStyle\":\"").append(fontStyle).append("\",");
        sb.append("\"fontColor\":\"").append(fontColor).append("\",");
        sb.append("\"itemWidth\":").append(itemWidth).append(",");
        sb.append("\"itemHeight\":").append(itemHeight).append(",");
        sb.append("\"itemGap\":").append(itemGap).append(",");
        sb.append("\"fontWeight\":\"").append(fontWeight).append("\"");
        sb.append("}");
        return sb.toString();
    }

    public int getBoxWidth() {
        return boxWidth;
    }

    public void setBoxWidth(int boxWidth) {
        this.boxWidth = boxWidth;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public FontStyle getFontStyle() {
        return fontStyle;
    }

    public void setFontStyle(FontStyle fontStyle) {
        this.fontStyle = fontStyle;
    }

    public String getFontColor() {
        return fontColor;
    }

    public void setFontColor(String fontColor) {
        this.fontColor = fontColor;
    }

    public int getPadding() {
        return padding;
    }

    public void setPadding(int padding) {
        this.padding = padding;
    }

    public int getItemWidth() {
        return itemWidth;
    }

    public void setItemWidth(int itemWidth) {
        this.itemWidth = itemWidth;
    }

    public int getItemHeight() {
        return itemHeight;
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
    }

    public int getItemGap() {
        return itemGap;
    }

    public void setItemGap(int itemGap) {
        this.itemGap = itemGap;
    }

    public String getFontWeight() {
        return fontWeight;
    }

    public void setFontWeight(String fontWeight) {
        this.fontWeight = fontWeight;
    }
}
