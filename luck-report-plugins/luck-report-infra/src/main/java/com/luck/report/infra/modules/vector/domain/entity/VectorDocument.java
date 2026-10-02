package com.luck.report.infra.modules.vector.domain.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 向量文档（content + vector + metadata）
 *
 * @author luck
 */
public class VectorDocument {

    /**
     * 文档 ID
     */
    private String id;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 向量；写入前必须已生成
     */
    private float[] vector;

    /**
     * 元数据，常用键：vectorType、componentType、datasourceId
     */
    private Map<String, Object> metadata;

    public VectorDocument(String content, Map<String, Object> metadata) {
        this.id = UUID.randomUUID().toString();
        this.content = content;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    public VectorDocument(String id, String content, Map<String, Object> metadata) {
        this.id = id;
        this.content = content;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public float[] getVector() {
        return vector;
    }

    public void setVector(float[] vector) {
        this.vector = vector;
    }

    public Map<String, Object> getMetadata() {
        return metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }
}
