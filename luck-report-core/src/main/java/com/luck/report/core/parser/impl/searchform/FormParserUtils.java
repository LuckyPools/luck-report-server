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
package com.luck.report.core.parser.impl.searchform;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luck.report.core.definition.searchform.DatasetOption;
import com.luck.report.core.definition.searchform.Option;
import com.luck.report.core.definition.searchform.DatasetParam;
import com.luck.report.core.definition.searchform.component.BaseOptionComponent;
import com.luck.report.core.definition.searchform.component.Component;
import org.dom4j.Element;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.*;

/**
 * @author Jacky.gao
 * @since 2017年10月24日
 */
public class FormParserUtils implements ApplicationContextAware {
    @SuppressWarnings("rawtypes")
    private static Collection<FormParser> parsers = null;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static List<Component> parse(Element element) {
        List<Component> list = new ArrayList<Component>();
        if (parsers == null || parsers.isEmpty()) {
            return list;
        }
        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            String name = ele.getName();
            FormParser<?> targetParser = null;
            for (FormParser<?> parser : parsers) {
                if (parser.support(name)) {
                    targetParser = parser;
                    break;
                }
            }
            if (targetParser == null) {
                continue;
            }
            list.add((Component) targetParser.parse(ele));
        }
        return list;
    }

    /**
     * 解析选项类查询组件（select / cascader / tree-select）的公共属性到组件实例。
     */
    public static void parseBaseOptionAttributes(BaseOptionComponent component, Element element) {
        component.setLabel(parseStringAttribute(element.attributeValue("label")));
        component.setPlaceholder(parseStringAttribute(element.attributeValue("placeholder")));
        component.setStyle(parseStyle(element.attributeValue("style")));
        component.setTag(parseStringAttribute(element.attributeValue("tag")));
        component.setTagIcon(parseStringAttribute(element.attributeValue("tagIcon")));

        component.setDisabled(parseBooleanAttribute(element.attributeValue("disabled")));
        component.setMultiple(parseBooleanAttribute(element.attributeValue("multiple")));
        component.setClearable(parseBooleanAttribute(element.attributeValue("clearable")));
        component.setFilterable(parseBooleanAttribute(element.attributeValue("filterable")));

        component.setSpan(parseIntAttribute(element.attributeValue("span")));
        component.setLabelWidth(parseStringAttribute(element.attributeValue("labelWidth")));

        component.setRequired(parseBooleanAttribute(element.attributeValue("required")));
        component.setRegList(parseStringList(element.attributeValue("regList")));
        component.setChangeTag(parseBooleanAttribute(element.attributeValue("changeTag")));

        component.setDocument(parseStringAttribute(element.attributeValue("document")));
        component.setFormId(parseStringAttribute(element.attributeValue("formId")));
        component.setRenderKey(parseStringAttribute(element.attributeValue("renderKey")));
        component.setVModel(parseStringAttribute(element.attributeValue("vModel")));
        component.setDefaultValue(parseStringAttribute(element.attributeValue("defaultValue")));
        component.setLayout(parseStringAttribute(element.attributeValue("layout")));
        if (component.getLayout() == null) {
            component.setLayout("colFormItem");
        }
        component.setOptionSource(parseStringAttribute(element.attributeValue("optionSource")));
        component.setDatasetOption(parseDatasetOption(element));
    }

    /**
     * 解析 &lt;datasetOption&gt; 子元素为 {@link DatasetOption} 对象。
     */
    public static DatasetOption parseDatasetOption(Element element) {
        Element dsElement = null;
        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            if (ele.getName().equals("datasetOption")) {
                dsElement = ele;
                break;
            }
        }
        if (dsElement != null) {
            DatasetOption datasetOption = new DatasetOption();
            datasetOption.setDatasourceName(parseStringAttribute(dsElement.attributeValue("datasourceName")));
            datasetOption.setDatasetName(parseStringAttribute(dsElement.attributeValue("datasetName")));
            datasetOption.setLabelField(parseStringAttribute(dsElement.attributeValue("labelField")));
            datasetOption.setValueField(parseStringAttribute(dsElement.attributeValue("valueField")));
            datasetOption.setParentField(parseStringAttribute(dsElement.attributeValue("parentField")));

            List<DatasetParam> bindings = new ArrayList<DatasetParam>();
            for (Object obj : dsElement.elements()) {
                if (obj == null || !(obj instanceof Element)) {
                    continue;
                }
                Element ele = (Element) obj;
                if (!ele.getName().equals("datasetParam")) {
                    continue;
                }
                DatasetParam binding = new DatasetParam();
                binding.setParamKey(parseStringAttribute(ele.attributeValue("paramKey")));
                binding.setParentField(parseStringAttribute(ele.attributeValue("parentField")));
                bindings.add(binding);
            }
            if (!bindings.isEmpty()) {
                datasetOption.setDatasetParams(bindings);
            }
            return datasetOption;
        }
        // 兼容旧格式：回退到 JSON 字符串属性
        return parseObjectAttribute(element.attributeValue("datasetOption"), DatasetOption.class);
    }

    /**
     * 递归解析 <option> 子元素（支持嵌套 children 层级选项）。
     */
    public static List<Option> parseOptions(Element element) {
        List<Option> options = new ArrayList<Option>();
        for (Object obj : element.elements()) {
            if (obj == null || !(obj instanceof Element)) {
                continue;
            }
            Element ele = (Element) obj;
            if (!ele.getName().equals("option")) {
                continue;
            }
            options.add(parseOption(ele));
        }
        return options;
    }

    private static Option parseOption(Element ele) {
        Option option = new Option();
        option.setLabel(parseStringAttribute(ele.attributeValue("label")));
        option.setValue(parseStringAttribute(ele.attributeValue("value")));
        List<Option> children = parseOptions(ele);
        if (!children.isEmpty()) {
            option.setChildren(children);
        }
        return option;
    }

    public static Map<String, String> parseStyle(String styleStr) {
        if (styleStr == null || styleStr.trim().isEmpty()) {
            return null;
        }
        try {
            Map<String, String> styleMap = objectMapper.readValue(styleStr, new TypeReference<Map<String, String>>(){});
            return styleMap;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<String> parseStringList(String regListStr) {
        if (regListStr == null || regListStr.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(regListStr, new TypeReference<List<String>>(){});
        } catch (Exception e) {
            return null;
        }
    }

    public static String parseStringAttribute(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        if ("null".equals(value)) {
            return null;
        }
        return value;
    }

    /**
     * 解析 JSON 字符串属性为指定类型对象（如 datasetOption）。
     *
     * @param value XML 属性值（JSON 字符串）
     * @param clazz 目标类型
     * @return 解析失败或入参为空时返回 null
     */
    public static <T> T parseObjectAttribute(String value, Class<T> clazz) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        if ("null".equals(value)) {
            return null;
        }
        try {
            return objectMapper.readValue(value, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public static Boolean parseBooleanAttribute(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        if ("null".equals(value)) {
            return null;
        }
        return Boolean.parseBoolean(value);
    }

    public static Integer parseIntAttribute(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        if ("null".equals(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        FormParserUtils.parsers = applicationContext.getBeansOfType(FormParser.class).values();
    }
}
