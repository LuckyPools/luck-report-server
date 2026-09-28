package com.luck.report.web.modules.vector.domain.bo;

/**
 * 向量检索运行时参数（topK / threshold / 嵌入模型）。
 */
public final class SearchParams {

    private final int topK;
    private final double threshold;
    private final String embedModelId;

    public SearchParams(int topK, double threshold, String embedModelId) {
        this.topK = topK;
        this.threshold = threshold;
        this.embedModelId = embedModelId;
    }

    public int getTopK() {
        return topK;
    }

    public double getThreshold() {
        return threshold;
    }

    public String getEmbedModelId() {
        return embedModelId;
    }
}
