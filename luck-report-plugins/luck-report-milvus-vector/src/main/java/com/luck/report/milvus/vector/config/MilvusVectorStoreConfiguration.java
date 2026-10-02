package com.luck.report.milvus.vector.config;

import com.luck.report.infra.modules.vector.config.EmptyVectorStoreConfiguration;
import com.luck.report.infra.modules.vector.service.VectorStore;
import com.luck.report.milvus.vector.service.impl.MilvusVectorStoreImpl;
import io.milvus.v2.client.ConnectConfig;
import io.milvus.v2.client.MilvusClientV2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Milvus 向量存储配置
 *
 * @author luck
 */
@Configuration
@ConditionalOnProperty(name = "luck-report.vector.type", havingValue = "milvus")
@EnableConfigurationProperties(MilvusVectorProperties.class)
@AutoConfigureBefore(EmptyVectorStoreConfiguration.class)
public class MilvusVectorStoreConfiguration {

    private static final Logger log = LoggerFactory.getLogger(MilvusVectorStoreConfiguration.class);

    /**
     * 创建 Milvus V2 客户端
     *
     * @param props Milvus 配置属性
     * @return MilvusClientV2 实例
     */
    @Bean("bean.milvusClient")
    @ConditionalOnMissingBean(name = "bean.milvusClient")
    public MilvusClientV2 milvusClient(MilvusVectorProperties props) {
        String uri = buildUri(props.getHost(), props.getPort());

        ConnectConfig.ConnectConfigBuilder builder = ConnectConfig.builder().uri(uri);
        if (props.getUsername() != null && !props.getUsername().isEmpty()) {
            builder.username(props.getUsername()).password(props.getPassword());
        }

        log.info("[MilvusVectorStore] 连接 Milvus 服务: {}", uri);
        return new MilvusClientV2(builder.build());
    }

    /**
     * 注册 Milvus 向量存储 Bean
     *
     * @param client Milvus V2 客户端
     * @param props  Milvus 配置属性
     * @return MilvusVectorStoreImpl 实例
     * @throws IllegalStateException 如果 dimension 未配置或 <= 0
     */
    @Bean("bean.milvusVectorStore")
    @ConditionalOnMissingBean(VectorStore.class)
    public VectorStore milvusVectorStore(
            @Qualifier("bean.milvusClient") MilvusClientV2 client, MilvusVectorProperties props) {
        if (props.getDimension() <= 0) {
            throw new IllegalStateException(
                    "luck-report.vector.datasource.dimension 必须配置且大于 0，" +
                    "该值应与 Embedding 模型输出维度一致（如 OpenAI text-embedding-ada-002 为 1536）"
            );
        }
        log.info("[MilvusVectorStore] 初始化，Collection: {}, dimension: {}",
                 props.getCollectionName(), props.getDimension());
        return new MilvusVectorStoreImpl(client, props.getCollectionName(), props.getDimension());
    }

    /**
     * 拼接 Milvus 连接 URI
     *
     * @param host 主机地址，可为纯主机名或带协议前缀的 URL，不可为空
     * @param port 端口号
     * @return 完整 URI，如 http://localhost:19530 或 https://xxx.zilliz.com.cn:19530
     */
    private String buildUri(String host, int port) {
        String trimmed = host.endsWith("/") ? host.substring(0, host.length() - 1) : host;
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed + ":" + port;
        }
        return "http://" + trimmed + ":" + port;
    }
}
