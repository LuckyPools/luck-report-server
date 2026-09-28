package com.luck.report.web.config;

import com.luck.report.infra.modules.vector.domain.enums.RetrievalMethod;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 知识库 RAG 检索参数配置（Phase 1）
 * <p>前缀：{@code luck-report.vector.retrieval}
 *
 * @author luck
 */
@ConfigurationProperties(prefix = "luck-report.vector.retrieval")
public class KnowledgeRetrievalProperties {

    /** 向量初筛条数（裁剪前），默认 30 */
    private int recallTopK = 30;

    /** 同知识最多保留的高分 chunk 数（分条返回），默认 3 */
    private int mergeTopN = 3;

    /** 最多保留的知识条数（按知识最高分），默认 5；展开后条数 ≤ finalTopK × mergeTopN */
    private int finalTopK = 5;

    /** 余弦相似度阈值，默认 0.35（宽召回） */
    private double threshold = 0.35;

    /** MySQL 整篇兜底 content 最大字符数 */
    private int fallbackContentMaxChars = 4000;

    /** 合并后单条 content 硬上限 */
    private int mergedContentMaxChars = 6000;

    /**
     * 创建知识时若 modelId 与默认嵌入模型不一致是否直接拒绝。
     * false：仅打告警日志（Phase 1 默认）
     */
    private boolean rejectMismatchedModel = false;

    /** semantic | full_text | hybrid，默认 semantic 兼容现网 */
    private String method = "semantic";

    /** 混合时向量路召回条数，默认 40 */
    private int vectorRecallTopK = 40;

    /** 混合时全文路召回条数，默认 40 */
    private int fulltextRecallTopK = 40;

    /** 加权 RRF 向量权重，默认 0.5（对齐 FastGPT embeddingWeight） */
    private double embeddingWeight = 0.5;

    /** RRF 常数 k，默认 60 */
    private int rrfK = 60;

    /** 融合后进入 Rerank / 截断前的候选上限 */
    private int fusionTopN = 20;

    /** 精排总开关；需同时存在激活的 RERANK 模型才会 HTTP 精排 */
    private boolean rerankEnabled = true;

    /** Rerank 后（或未开 Rerank 时融合后）保留条数，默认 3 */
    private int rerankTopN = 3;

    /** 精排结果与原 RRF 再融合时精排侧权重（FastGPT rerankWeight），默认 0.5 */
    private double rerankWeight = 0.5;

    /** Rerank HTTP 超时毫秒 */
    private int rerankTimeoutMs = 10000;

    /** 送入 Rerank 的单条 content 最大字符数 */
    private int rerankDocMaxChars = 2000;

    public int getRecallTopK() {
        return recallTopK;
    }

    public void setRecallTopK(int recallTopK) {
        this.recallTopK = recallTopK;
    }

    public int getFinalTopK() {
        return finalTopK;
    }

    public void setFinalTopK(int finalTopK) {
        this.finalTopK = finalTopK;
    }

    public int getMergeTopN() {
        return mergeTopN;
    }

    public void setMergeTopN(int mergeTopN) {
        this.mergeTopN = mergeTopN;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public int getFallbackContentMaxChars() {
        return fallbackContentMaxChars;
    }

    public void setFallbackContentMaxChars(int fallbackContentMaxChars) {
        this.fallbackContentMaxChars = fallbackContentMaxChars;
    }

    public int getMergedContentMaxChars() {
        return mergedContentMaxChars;
    }

    public void setMergedContentMaxChars(int mergedContentMaxChars) {
        this.mergedContentMaxChars = mergedContentMaxChars;
    }

    public boolean isRejectMismatchedModel() {
        return rejectMismatchedModel;
    }

    public void setRejectMismatchedModel(boolean rejectMismatchedModel) {
        this.rejectMismatchedModel = rejectMismatchedModel;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getVectorRecallTopK() {
        return vectorRecallTopK;
    }

    public void setVectorRecallTopK(int vectorRecallTopK) {
        this.vectorRecallTopK = vectorRecallTopK;
    }

    public int getFulltextRecallTopK() {
        return fulltextRecallTopK;
    }

    public void setFulltextRecallTopK(int fulltextRecallTopK) {
        this.fulltextRecallTopK = fulltextRecallTopK;
    }

    public double getEmbeddingWeight() {
        return embeddingWeight;
    }

    public void setEmbeddingWeight(double embeddingWeight) {
        this.embeddingWeight = embeddingWeight;
    }

    public int getRrfK() {
        return rrfK;
    }

    public void setRrfK(int rrfK) {
        this.rrfK = rrfK;
    }

    public int getFusionTopN() {
        return fusionTopN;
    }

    public void setFusionTopN(int fusionTopN) {
        this.fusionTopN = fusionTopN;
    }

    public boolean isRerankEnabled() {
        return rerankEnabled;
    }

    public void setRerankEnabled(boolean rerankEnabled) {
        this.rerankEnabled = rerankEnabled;
    }

    public int getRerankTopN() {
        return rerankTopN;
    }

    public void setRerankTopN(int rerankTopN) {
        this.rerankTopN = rerankTopN;
    }

    public double getRerankWeight() {
        return rerankWeight;
    }

    public void setRerankWeight(double rerankWeight) {
        this.rerankWeight = rerankWeight;
    }

    public int getRerankTimeoutMs() {
        return rerankTimeoutMs;
    }

    public void setRerankTimeoutMs(int rerankTimeoutMs) {
        this.rerankTimeoutMs = rerankTimeoutMs;
    }

    public int getRerankDocMaxChars() {
        return rerankDocMaxChars;
    }

    public void setRerankDocMaxChars(int rerankDocMaxChars) {
        this.rerankDocMaxChars = rerankDocMaxChars;
    }

    /**
     * 校验配置合法性，非法时抛出 IllegalStateException（启动期快速失败）
     */
    public void validate() {
        if (finalTopK < 1) {
            throw new IllegalStateException(
                    "luck-report.vector.retrieval.final-top-k must be >= 1, got: " + finalTopK);
        }
        if (recallTopK < finalTopK) {
            throw new IllegalStateException(
                    "luck-report.vector.retrieval.recall-top-k must be >= final-top-k, got recall="
                            + recallTopK + ", final=" + finalTopK);
        }
        if (mergeTopN < 1) {
            throw new IllegalStateException(
                    "luck-report.vector.retrieval.merge-top-n must be >= 1, got: " + mergeTopN);
        }
        if (threshold < 0 || threshold > 1) {
            throw new IllegalStateException(
                    "luck-report.vector.retrieval.threshold must be in [0,1], got: " + threshold);
        }
        if (fallbackContentMaxChars < 1 || mergedContentMaxChars < 1) {
            throw new IllegalStateException(
                    "luck-report.vector.retrieval content max chars must be >= 1");
        }
        RetrievalMethod m;
        try {
            m = RetrievalMethod.fromConfig(method);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Invalid retrieval method: " + method, ex);
        }
        if (m == RetrievalMethod.HYBRID || m == RetrievalMethod.FULL_TEXT) {
            if (vectorRecallTopK < 1 || fulltextRecallTopK < 1) {
                throw new IllegalStateException("vector/fulltext recall-top-k must be >= 1");
            }
        }
        if (embeddingWeight < 0 || embeddingWeight > 1) {
            throw new IllegalStateException("embedding-weight must be in [0,1]");
        }
        if (rrfK < 1) {
            throw new IllegalStateException("rrf-k must be >= 1");
        }
        if (fusionTopN < 1 || rerankTopN < 1) {
            throw new IllegalStateException("fusion-top-n / rerank-top-n must be >= 1");
        }
        if (rerankWeight < 0 || rerankWeight > 1) {
            throw new IllegalStateException("rerank-weight must be in [0,1]");
        }
        if (rerankTimeoutMs < 1 || rerankDocMaxChars < 1) {
            throw new IllegalStateException("rerank-timeout-ms / rerank-doc-max-chars must be >= 1");
        }
    }
}
