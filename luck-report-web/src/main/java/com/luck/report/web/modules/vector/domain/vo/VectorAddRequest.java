package com.luck.report.web.modules.vector.domain.vo;

import java.util.Map;

/**
 * 向量文档添加请求 VO
 *
 * @author luck
 */
public class VectorAddRequest {

    /**
     * 文本内容
     */
    private String content;

    /**
     * 向量类型，如 COMPONENT / TEMPLATE / TABLE / COLUMN / agentKnowledge / businessKnowledge
     */
    private String vectorType;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getVectorType() {
        return vectorType;
    }

    public void setVectorType(String vectorType) {
        this.vectorType = vectorType;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
