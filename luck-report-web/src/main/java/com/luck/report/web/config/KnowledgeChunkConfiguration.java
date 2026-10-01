package com.luck.report.web.config;

import com.luck.report.web.config.properties.KnowledgeChunkProperties;
import com.luck.report.web.config.properties.KnowledgeDocumentParseProperties;
import com.luck.report.web.config.properties.KnowledgeRetrievalProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 知识库分块配置自动装配
 *
 * @author luck
 */
@Configuration
public class KnowledgeChunkConfiguration {

    /**
     * 唯一注册；{@code initMethod=validate} 在属性绑定完成后执行启动期校验。
     */
    @Bean(name = "bean.knowledgeChunkProperties", initMethod = "validate")
    @ConfigurationProperties(prefix = "luck-report.vector")
    public KnowledgeChunkProperties knowledgeChunkProperties() {
        return new KnowledgeChunkProperties();
    }

    @Bean(name = "bean.knowledgeDocumentParseProperties")
    @ConfigurationProperties(prefix = "luck-report.vector.document-parse")
    public KnowledgeDocumentParseProperties knowledgeDocumentParseProperties() {
        return new KnowledgeDocumentParseProperties();
    }

    @Bean(name = "bean.knowledgeRetrievalProperties", initMethod = "validate")
    @ConfigurationProperties(prefix = "luck-report.vector.retrieval")
    public KnowledgeRetrievalProperties knowledgeRetrievalProperties() {
        return new KnowledgeRetrievalProperties();
    }
}
