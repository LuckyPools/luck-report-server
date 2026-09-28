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
package com.luck.report.core.definition.searchform;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

/**
 * @author Jacky.gao
 * @since 2017年10月23日
 */
public class Option implements Serializable {
    private static final long serialVersionUID = 1L;
    private String label;
    private String value;
    /** 子选项（级联选择 / 树选择的层级选项；扁平选项为 null 不输出） */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<Option> children;

    /**
     * 默认无参构造器
     */
    public Option() {}

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<Option> getChildren() {
        return children;
    }

    public void setChildren(List<Option> children) {
        this.children = children;
    }
}
