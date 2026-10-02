package com.luck.report.infra.modules.vector.domain.dto;

import com.luck.report.infra.modules.vector.domain.entity.VectorDocument;

/**
 * 向量检索结果。
 *
 * @author luck
 */
public class VectorStoreSearchResult {

    /**
     * 命中的向量文档
     */
    private VectorDocument document;

    /**
     * 相似度 0~1，越大越相似
     */
    private double score;

    public VectorStoreSearchResult(VectorDocument document, double score) {
        this.document = document;
        this.score = score;
    }

    public VectorDocument getDocument() {
        return document;
    }

    public void setDocument(VectorDocument document) {
        this.document = document;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
