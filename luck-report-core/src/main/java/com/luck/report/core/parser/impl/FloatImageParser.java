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

import com.luck.report.core.definition.FloatImage;
import com.luck.report.core.definition.value.Source;
import com.luck.report.core.parser.Parser;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

/**
 * 悬浮图片解析器，镜像 {@link com.luck.report.core.parser.impl.value.ImageValueParser}：
 * 按 {@code source} 把 {@code <text>} 子元素内容分流到 {@code path} 或 {@code expr}。
 *
 * @author luck-report
 * @since 1.0.0
 */
public class FloatImageParser implements Parser<FloatImage> {

    @Override
    public FloatImage parse(Element element) {
        FloatImage fi = new FloatImage();
        fi.setWidth(parseInt(element.attributeValue("width")));
        fi.setHeight(parseInt(element.attributeValue("height")));
        fi.setTop(parseInt(element.attributeValue("top")));
        fi.setLeft(parseInt(element.attributeValue("left")));
        fi.setLayer(parseInt(element.attributeValue("layer")));
        fi.setRepeatPrint(Boolean.valueOf(element.attributeValue("repeat-print")));
        fi.setName(element.attributeValue("name"));

        Source source = Source.valueOf(element.attributeValue("source"));
        fi.setSource(source);

        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            if (ele.getName().equals("text")) {
                if (source.equals(Source.text)) {
                    fi.setPath(ele.getText());       // URL → path
                } else {
                    fi.setExpr(ele.getText());       // 纯 base64 → expr
                }
                break;
            }
        }
        return fi;
    }

    private Integer parseInt(String val) {
        if (StringUtils.isBlank(val)) {
            return null;
        }
        return Integer.valueOf(val);
    }
}
