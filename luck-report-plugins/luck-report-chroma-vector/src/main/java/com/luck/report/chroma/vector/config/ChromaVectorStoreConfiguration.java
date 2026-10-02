package com.luck.report.chroma.vector.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.luck.report.chroma.vector.service.impl.ChromaVectorStoreImpl;
import com.luck.report.infra.modules.vector.service.VectorStore;

import tech.amikos.chromadb.Client;
import tech.amikos.chromadb.handler.ApiClient;
import tech.amikos.chromadb.handler.DefaultApi;

/**
 * Chroma 向量存储配置
 * @author luck
 */
@Configuration
@ConditionalOnProperty(name = "luck-report.vector.type", havingValue = "chroma")
@EnableConfigurationProperties(ChromaVectorProperties.class)
public class ChromaVectorStoreConfiguration {

    /**
     * 创建 Chroma HTTP 客户端（高层 API，用于 Collection 管理）
     *
     * @param props Chroma 配置属性
     * @return Client 实例
     */
    @Bean("bean.chromaClient")
    @ConditionalOnMissingBean(name = "bean.chromaClient")
    public Client chromaClient(ChromaVectorProperties props) {
        return new Client(props.getUrl());
    }

    /**
     * 创建 Chroma 底层 DefaultApi
     *
     * @param props Chroma 配置属性
     * @return DefaultApi 实例
     */
    @Bean("bean.chromaApi")
    @ConditionalOnMissingBean(name = "bean.chromaApi")
    public DefaultApi chromaApi(ChromaVectorProperties props) {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(props.getUrl());
        return new DefaultApi(apiClient);
    }

    /**
     * 注册 Chroma 向量存储实现 Bean
     *
     * @param chromaClient Chroma 客户端（Collection 管理）
     * @param api Chroma 底层 API（增删查操作）
     * @param props Chroma 配置属性
     * @return ChromaVectorStoreImpl 实例
     */
    @Bean("bean.chromaVectorStore")
    @ConditionalOnMissingBean(VectorStore.class)
    public VectorStore chromaVectorStore(
            @Qualifier("bean.chromaClient") Client chromaClient,
            @Qualifier("bean.chromaApi") DefaultApi api,
            ChromaVectorProperties props) {
        return new ChromaVectorStoreImpl(chromaClient, api, props.getCollectionName());
    }
}
