package com.luck.report.web.modules.vector.domain.vo;

import java.util.Map;

/**
 * 向量检索请求 VO
 *
 * @author luck
 */
public class VectorSearchRequest {

    /**
     * 查询文本，如 "柱状图怎么用"、"条件样式"
     */
    private String query;

    /**
     * 向量类型，如 COMPONENT / TEMPLATE / TABLE / COLUMN / agentKnowledge / businessKnowledge
     */
    private String vectorType;

    /**
     * 返回条数；Agent 知识检索以后端配置为准，可不传
     */
    private Integer topK;

    /**
     * 相似度阈值（0~1）；Agent 知识检索以后端配置为准，可不传
     */
    private Double threshold;

    /**
     * 为 true 时强制 hybrid；null/false 走全局 method。评测使用。
     */
    private Boolean forceHybrid;

    /**
     * 额外元数据过滤条件，如 {"componentType": "chart"}
     */
    private Map<String, Object> metadataFilters;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getVectorType() {
        return vectorType;
    }

    public void setVectorType(String vectorType) {
        this.vectorType = vectorType;
    }

    public Integer getTopK() {
        return topK;
    }

    public void setTopK(Integer topK) {
        this.topK = topK;
    }

    public Double getThreshold() {
        return threshold;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public Boolean getForceHybrid() {
        return forceHybrid;
    }

    public void setForceHybrid(Boolean forceHybrid) {
        this.forceHybrid = forceHybrid;
    }

    public Map<String, Object> getMetadataFilters() {
        return metadataFilters;
    }

    public void setMetadataFilters(Map<String, Object> metadataFilters) {
        this.metadataFilters = metadataFilters;
    }
}
