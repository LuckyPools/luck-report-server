package com.luck.report.core.definition.searchform.component;

import com.luck.report.core.definition.searchform.DatasetOption;
import com.luck.report.core.definition.searchform.Option;

import java.util.List;

/**
 * 选项类查询组件公共基类：select / cascader / tree-select 共有字段。
 *
 * @author luck-report
 * @since 2.2.0
 */
public abstract class BaseOptionComponent extends BaseInputComponent {
    private static final long serialVersionUID = 1L;

    private boolean multiple;
    private boolean clearable;
    private boolean filterable;
    private String placeholder;
    private boolean disabled;
    private List<Option> options;
    private String defaultValue;
    /** 选项来源：static=静态（默认），dataset=关联数据集 */
    private String optionSource;
    /** 数据集选项绑定（optionSource=dataset 时有效） */
    private DatasetOption datasetOption;

    /**
     * 默认无参构造器
     */
    public BaseOptionComponent() {}

    @Override
    public String initJs(com.luck.report.core.definition.searchform.RenderContext context) {
        return "";
    }

    public boolean isMultiple() {
        return multiple;
    }

    public void setMultiple(boolean multiple) {
        this.multiple = multiple;
    }

    public boolean isClearable() {
        return clearable;
    }

    public void setClearable(boolean clearable) {
        this.clearable = clearable;
    }

    public boolean isFilterable() {
        return filterable;
    }

    public void setFilterable(boolean filterable) {
        this.filterable = filterable;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public boolean getDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getOptionSource() {
        return optionSource;
    }

    public void setOptionSource(String optionSource) {
        this.optionSource = optionSource;
    }

    public DatasetOption getDatasetOption() {
        return datasetOption;
    }

    public void setDatasetOption(DatasetOption datasetOption) {
        this.datasetOption = datasetOption;
    }
}
