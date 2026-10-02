package com.luck.report.chroma.vector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Chroma 向量存储配置
 *
 * @author luck
 */
@Data
@ConfigurationProperties(prefix = "luck-report.vector.datasource")
public class ChromaVectorProperties {

    /**
     * Chroma 服务地址（如 http://localhost:8000）
     */
    private String url = "http://localhost:8000";

    /**
     * 默认 Collection 名称
     */
    private String collectionName = "luck_vector_document";
}
