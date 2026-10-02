package com.luck.report.web.modules.report.domain.vo.dataset;

/**
 * JSON 静态数据集定义 VO
 *
 * @author luck-report
 * @since 2.0.5
 */
public class JsonDatasetDefinitionVo extends DatasetDefinitionVo {

    private static final long serialVersionUID = 1L;

    /**
     * JSON 数组字符串，存储静态数据集的完整数据
     */
    private String content;

    /**
     * 默认无参构造器
     */
    public JsonDatasetDefinitionVo() {
    }

    /**
     * 获取 JSON 数组字符串
     *
     * @return content JSON 字符串
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置 JSON 数组字符串
     *
     * @param content JSON 数组字符串
     */
    public void setContent(String content) {
        this.content = content;
    }
}
