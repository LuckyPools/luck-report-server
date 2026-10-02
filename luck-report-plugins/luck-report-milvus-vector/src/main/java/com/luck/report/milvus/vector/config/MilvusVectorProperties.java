package com.luck.report.milvus.vector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Milvus 向量存储配置
 *
 * @author luck
 */
@Data
@ConfigurationProperties(prefix = "luck-report.vector.datasource")
public class MilvusVectorProperties {

    /**
     * Milvus 服务地址（默认 localhost）
     */
    private String host = "localhost";

    /**
     * Milvus gRPC 端口（默认 19530）
     */
    private int port = 19530;

    /**
     * Collection 名称（默认 luck_vector_document）
     */
    private String collectionName = "luck_vector_document";

    /**
     * 向量维度，必须与 Embedding 模型输出维度一致（如 OpenAI text-embedding-ada-002 为 1536）
     */
    private int dimension;

    /**
     * 认证用户名（可选，未配置时不启用认证）
     */
    private String username;

    /**
     * 认证密码（可选）
     */
    private String password;
}
