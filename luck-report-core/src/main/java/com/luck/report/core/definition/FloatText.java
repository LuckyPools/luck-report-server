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

/**
 * 悬浮文本元素。继承 {@link FloatElement} 的定位与层级属性。
 *
 * @author luck-report
 * @since 1.0.0
 */
public class FloatText extends FloatElement {
    private static final long serialVersionUID = 1L;

    /** 文本内容 */
    private String value;
    /** 字体 */
    private String fontFamily;
    /** 字号（像素） */
    private Integer fontSize;
    /** 字体颜色，"R,G,B" 格式 */
    private String forecolor;
    /** 是否粗体 */
    private Boolean bold;
    /** 是否斜体 */
    private Boolean italic;
    /** 是否下划线 */
    private Boolean underline;
    /** 水平对齐方式：left / center / right */
    private String align;
    /** 垂直对齐方式：top / middle / bottom */
    private String valign;
    /** 背景颜色，"R,G,B" 格式，空或null表示无背景 */
    private String bgcolor;
    /** 上边框 */
    private Border topBorder;
    /** 右边框 */
    private Border rightBorder;
    /** 下边框 */
    private Border bottomBorder;
    /** 左边框 */
    private Border leftBorder;

    public FloatText() {
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }

    public Integer getFontSize() {
        return fontSize;
    }

    public void setFontSize(Integer fontSize) {
        this.fontSize = fontSize;
    }

    public String getForecolor() {
        return forecolor;
    }

    public void setForecolor(String forecolor) {
        this.forecolor = forecolor;
    }

    public Boolean getBold() {
        return bold;
    }

    public void setBold(Boolean bold) {
        this.bold = bold;
    }

    public Boolean getItalic() {
        return italic;
    }

    public void setItalic(Boolean italic) {
        this.italic = italic;
    }

    public Boolean getUnderline() {
        return underline;
    }

    public void setUnderline(Boolean underline) {
        this.underline = underline;
    }

    public String getAlign() {
        return align;
    }

    public void setAlign(String align) {
        this.align = align;
    }

    public String getValign() {
        return valign;
    }

    public void setValign(String valign) {
        this.valign = valign;
    }

    public String getBgcolor() {
        return bgcolor;
    }

    public void setBgcolor(String bgcolor) {
        this.bgcolor = bgcolor;
    }

    public Border getTopBorder() {
        return topBorder;
    }

    public void setTopBorder(Border topBorder) {
        this.topBorder = topBorder;
    }

    public Border getRightBorder() {
        return rightBorder;
    }

    public void setRightBorder(Border rightBorder) {
        this.rightBorder = rightBorder;
    }

    public Border getBottomBorder() {
        return bottomBorder;
    }

    public void setBottomBorder(Border bottomBorder) {
        this.bottomBorder = bottomBorder;
    }

    public Border getLeftBorder() {
        return leftBorder;
    }

    public void setLeftBorder(Border leftBorder) {
        this.leftBorder = leftBorder;
    }
}
