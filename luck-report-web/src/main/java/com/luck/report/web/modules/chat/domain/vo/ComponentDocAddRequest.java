package com.luck.report.web.modules.chat.domain.vo;

import java.util.Map;

/**
 * 组件文档添加请求 VO
 *
 * @author luck
 */
public class ComponentDocAddRequest {

    /**
     * 组件名称，如 "柱状图"、"条件样式"、"数据集"
     */
    private String name;

    /**
     * 组件描述/用法说明
     */
    private String description;

    /**
     * 组件类型，如 "chart"、"cell"、"dataset"、"style"
     */
    private String componentType;

    /**
     * 额外元数据，如 category、subTypes 等，可为 null
     */
    private Map<String, Object> extraMetadata;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getComponentType() {
        return componentType;
    }

    public void setComponentType(String componentType) {
        this.componentType = componentType;
    }

    public Map<String, Object> getExtraMetadata() {
        return extraMetadata;
    }

    public void setExtraMetadata(Map<String, Object> extraMetadata) {
        this.extraMetadata = extraMetadata;
    }
}
