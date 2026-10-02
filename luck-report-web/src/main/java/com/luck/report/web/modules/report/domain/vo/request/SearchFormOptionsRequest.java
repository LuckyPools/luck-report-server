package com.luck.report.web.modules.report.domain.vo.request;

import java.util.List;

/**
 * 查询表单选项加载请求 VO。
 *
 * @author luck-report
 * @since 2.1.0
 */
public class SearchFormOptionsRequest {

    /**
     * 报表文件路径
     */
    private String reportPath;

    /**
     * 预览模式（与 loadHtml 的 mode 参数一致，preview 时从设计器预览缓存加载）
     */
    private String mode;

    /**
     * 数据集引用列表
     */
    private List<SearchFormDatasetRef> datasets;

    public String getReportPath() {
        return reportPath;
    }

    public void setFilePath(String reportPath) {
        this.reportPath = reportPath;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public List<SearchFormDatasetRef> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<SearchFormDatasetRef> datasets) {
        this.datasets = datasets;
    }
}
