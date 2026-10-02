package com.luck.report.web.modules.report.domain.vo.request;

import java.util.Map;

/**
 * 查询表单选项：单个数据集引用。
 */
public class SearchFormDatasetRef {

    private String datasourceName;
    private String datasetName;
    private String labelField;
    private String valueField;
    /**
     * 父节点值字段；不传则返回扁平选项
     */
    private String parentField;
    private Map<String, Object> parameters;

    public String getDatasourceName() {
        return datasourceName;
    }

    public void setDatasourceName(String datasourceName) {
        this.datasourceName = datasourceName;
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public String getLabelField() {
        return labelField;
    }

    public void setLabelField(String labelField) {
        this.labelField = labelField;
    }

    public String getValueField() {
        return valueField;
    }

    public void setValueField(String valueField) {
        this.valueField = valueField;
    }

    public String getParentField() {
        return parentField;
    }

    public void setParentField(String parentField) {
        this.parentField = parentField;
    }

    public Map<String, Object> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }
}
