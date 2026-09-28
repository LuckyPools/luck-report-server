package com.luck.report.web.modules.report.domain.vo.report;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.List;

/**
 * 查询表单单个选项；children 仅在配置 parentField 时返回。
 */
public class SearchFormOption implements Serializable {

    private static final long serialVersionUID = 1L;

    private String label;
    private Object value;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SearchFormOption> children;

    public SearchFormOption() {
    }

    public SearchFormOption(String label, Object value) {
        this.label = label;
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public List<SearchFormOption> getChildren() {
        return children;
    }

    public void setChildren(List<SearchFormOption> children) {
        this.children = children;
    }
}
