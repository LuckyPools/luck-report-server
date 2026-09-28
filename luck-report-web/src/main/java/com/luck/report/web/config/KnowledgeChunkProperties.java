package com.luck.report.web.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 知识库分块参数配置
 * <p>前缀：{@code luck-report.vector}
 * <p>与向量库 type/datasource 同级；单位为<strong>字符</strong>（本轮不做 tokenizer）。
 *
 * @author luck
 */
@ConfigurationProperties(prefix = "luck-report.vector")
public class KnowledgeChunkProperties {

    /**
     * 分块大小（字符数），默认 1000
     */
    private int chunkSize = 1000;

    /**
     * 块间重叠（字符数），默认 100；须满足 {@code 0 <= chunkOverlap < chunkSize}
     */
    private int chunkOverlap = 100;

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getChunkOverlap() {
        return chunkOverlap;
    }

    public void setChunkOverlap(int chunkOverlap) {
        this.chunkOverlap = chunkOverlap;
    }

    /**
     * 校验配置合法性，非法时抛出 IllegalStateException（启动期快速失败）
     */
    public void validate() {
        if (chunkSize <= 0) {
            throw new IllegalStateException("luck-report.vector.chunk-size must be > 0, got: " + chunkSize);
        }
        if (chunkOverlap < 0) {
            throw new IllegalStateException("luck-report.vector.chunk-overlap must be >= 0, got: " + chunkOverlap);
        }
        if (chunkOverlap >= chunkSize) {
            throw new IllegalStateException(
                    "luck-report.vector.chunk-overlap must be < chunk-size, got overlap="
                            + chunkOverlap + ", size=" + chunkSize);
        }
    }
}
