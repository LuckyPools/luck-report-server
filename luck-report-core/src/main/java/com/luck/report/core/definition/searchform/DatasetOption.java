package com.luck.report.core.definition.searchform;

import java.io.Serializable;
import java.util.List;

/**
 * 数据集选项绑定配置（optionSource=dataset 时有效）；查询表单选项组件（select / radio-group / checkbox-group）可关联报表内数据集，通过 labelField / valueField 映射为选项；paramBindings 支持把其它查询字段的 当前值作为数据集查询参数，实现级联选项。
 *
 * @author luck-report
 * @since 2.1.0
 */
public class DatasetOption implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 数据源名（报表内唯一）
     */
    private String datasourceName;
    /**
     * 数据集名（数据源内唯一）
     */
    private String datasetName;
    /**
     * 标签字段：渲染为选项文字
     */
    private String labelField;
    /**
     * 值字段：提交到查询参数
     */
    private String valueField;
    /**
     * 父节点值字段（级联/树选择数据集来源时配置，用于平铺建树）
     */
    private String parentField;
    /**
     * 级联参数绑定（可选）
     */
    private List<DatasetParam> datasetParams;

    /**
     * 默认无参构造器
     */
    public DatasetOption() {}

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

    public List<DatasetParam> getDatasetParams() {
        return datasetParams;
    }

    public void setDatasetParams(List<DatasetParam> datasetParams) {
        this.datasetParams = datasetParams;
    }

}
