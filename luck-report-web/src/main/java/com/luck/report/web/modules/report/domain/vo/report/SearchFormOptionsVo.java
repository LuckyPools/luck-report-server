package com.luck.report.web.modules.report.domain.vo.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 查询表单选项加载结果视图对象，用于 {@code /html/load_search_form_options} 接口返回。
 * <p>字段名与前端 {@code SearchFormOptionsResult} 契约保持一致，避免破坏调用方。
 *
 * @author luck-report
 * @since 2.1.0
 */
public class SearchFormOptionsVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 选项集：key 为 "数据源名/数据集名"，value 为 {label, value} 选项列表 */
    private Map<String, List<SearchFormOption>> options = new HashMap<>();

    /** 单项加载失败的错误信息：key 为 "数据源名/数据集名" */
    private Map<String, String> errors = new HashMap<>();

    public SearchFormOptionsVo() {
    }

    public Map<String, List<SearchFormOption>> getOptions() {
        return options;
    }

    public void setOptions(Map<String, List<SearchFormOption>> options) {
        this.options = options;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    public void putOptions(String key, List<SearchFormOption> list) {
        options.put(key, list == null ? new ArrayList<>() : list);
    }

    public void putError(String key, String message) {
        errors.put(key, message);
    }
}
