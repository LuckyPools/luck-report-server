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
package com.luck.report.core.parser.impl;

import com.luck.report.core.definition.Border;
import com.luck.report.core.definition.BorderStyle;
import com.luck.report.core.definition.FloatText;
import com.luck.report.core.parser.Parser;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

/**
 * 悬浮文本解析器。
 *
 * @author luck-report
 * @since 1.0.0
 */
public class FloatTextParser implements Parser<FloatText> {

    @Override
    public FloatText parse(Element element) {
        FloatText ft = new FloatText();
        ft.setWidth(parseInt(element.attributeValue("width")));
        ft.setHeight(parseInt(element.attributeValue("height")));
        ft.setTop(parseInt(element.attributeValue("top")));
        ft.setLeft(parseInt(element.attributeValue("left")));
        ft.setLayer(parseInt(element.attributeValue("layer")));
        ft.setRepeatPrint(Boolean.valueOf(element.attributeValue("repeat-print")));
        ft.setName(element.attributeValue("name"));
        ft.setValue(element.getText());
        ft.setFontFamily(element.attributeValue("font-family"));
        ft.setFontSize(parseInt(element.attributeValue("font-size")));
        ft.setForecolor(element.attributeValue("forecolor"));
        ft.setBold(parseBoolean(element.attributeValue("bold")));
        ft.setItalic(parseBoolean(element.attributeValue("italic")));
        ft.setUnderline(parseBoolean(element.attributeValue("underline")));
        ft.setAlign(element.attributeValue("align"));
        ft.setValign(element.attributeValue("valign"));
        ft.setBgcolor(element.attributeValue("bgcolor"));
        ft.setTopBorder(parseBorder(element, "top-border"));
        ft.setRightBorder(parseBorder(element, "right-border"));
        ft.setBottomBorder(parseBorder(element, "bottom-border"));
        ft.setLeftBorder(parseBorder(element, "left-border"));
        return ft;
    }

    /**
     * 从 XML 元素中解析单边边框。
     * @param element XML 元素
     * @param prefix 属性前缀，如 "top-border"
     */
    private Border parseBorder(Element element, String prefix) {
        String style = element.attributeValue(prefix + "-style");
        if (StringUtils.isBlank(style) || "none".equals(style)) {
            return null;
        }
        Border border = new Border();
        border.setStyle(BorderStyle.toBorderStyle(style));
        border.setWidth(parseInt(element.attributeValue(prefix + "-width"), 1));
        String color = element.attributeValue(prefix + "-color");
        if (StringUtils.isNotBlank(color)) {
            border.setColor(color);
        }
        return border;
    }

    private Integer parseInt(String val) {
        if (StringUtils.isBlank(val)) {
            return null;
        }
        return Integer.valueOf(val);
    }

    private int parseInt(String val, int defaultValue) {
        if (StringUtils.isBlank(val)) {
            return defaultValue;
        }
        return Integer.parseInt(val);
    }

    private Boolean parseBoolean(String val) {
        if (StringUtils.isBlank(val)) {
            return null;
        }
        return Boolean.valueOf(val);
    }
}
