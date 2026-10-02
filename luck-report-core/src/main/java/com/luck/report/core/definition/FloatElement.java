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
package com.luck.report.core.definition;

import java.io.Serializable;

/**
 * 悬浮元素基类。所有悬浮元素（图片、文本等）共享的定位与层级属性。不绑定任何单元格，以绝对像素坐标浮于报表上方。新增悬浮类型时应继承本类，并在 {@link ReportDefinition} 中声明对应的 List 字段与 XML 序列化逻辑，保持与 FloatImage/FloatText 一致的规范。
 *
 * @author luck-report
 * @since 1.0.0
 */
public abstract class FloatElement implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 宽度（像素）
     */
    private Integer width;
    /**
     * 高度（像素）
     */
    private Integer height;
    /**
     * 距报表数据区顶部距离（像素）
     */
    private Integer top;
    /**
     * 距报表数据区左侧距离（像素）
     */
    private Integer left;
    /**
     * 层级，值越大越靠上，默认 0
     */
    private Integer layer;
    /**
     * 是否每页重复打印
     */
    private boolean repeatPrint;
    /**
     * 元素名称（同一报表内唯一，用于列表展示与标识）
     */
    private String name;

    public FloatElement() {
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getTop() {
        return top;
    }

    public void setTop(Integer top) {
        this.top = top;
    }

    public Integer getLeft() {
        return left;
    }

    public void setLeft(Integer left) {
        this.left = left;
    }

    public Integer getLayer() {
        return layer;
    }

    public void setLayer(Integer layer) {
        this.layer = layer;
    }

    public boolean isRepeatPrint() {
        return repeatPrint;
    }

    public void setRepeatPrint(boolean repeatPrint) {
        this.repeatPrint = repeatPrint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
