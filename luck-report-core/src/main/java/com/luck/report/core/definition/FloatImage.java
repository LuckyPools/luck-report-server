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

import com.luck.report.core.definition.value.Source;

/**
 * 悬浮图片元素。继承 {@link FloatElement} 的定位与层级属性。字段结构与 {@link com.luck.report.core.definition.value.ImageValue} 对齐：{@code source} + {@code path} + {@code expr}。{@code source=text} 时用 {@code path} 存 URL；{@code source=base64} 时用 {@code expr} 存纯 base64 字符串（不含 {@code data:image/xxx;base64,} 前缀），渲染/导出时再补前缀。不支持 {@code expression} 源（悬浮元素不参与数据展开）。
 *
 * @author luck-report
 * @since 1.0.0
 */
public class FloatImage extends FloatElement {
    private static final long serialVersionUID = 1L;

    /**
     * 图片来源：text（URL）/ base64
     */
    private Source source;
    /**
     * URL（source=text 时有效）
     */
    private String path;
    /**
     * 纯 base64 字符串，不含 data:image/xxx;base64, 前缀（source=base64 时有效）
     */
    private String expr;

    public FloatImage() {
    }

    /**
     * 取当前生效的值：source=text 返回 path，否则返回 expr。与 ImageValue.getValue() 逻辑一致。注意：故意不命名为 getValue()，避免被 JSON 序列化框架（Jackson/Fastjson 等）按 getter 命名规范识别为 "value" 属性，从而在反序列化时因无对应字段而报错。
     */
    public String buildValue() {
        if (source != null && source.equals(Source.text)) {
            return path;
        }
        return expr;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getExpr() {
        return expr;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }
}
