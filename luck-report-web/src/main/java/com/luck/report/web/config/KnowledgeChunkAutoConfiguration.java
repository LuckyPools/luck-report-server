package com.luck.report.web.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 知识库分块配置自动装配
 *
 * @author luck
 */
@Configuration
@EnableConfigurationProperties({
        KnowledgeChunkProperties.class,
        KnowledgeDocumentParseProperties.class,
        KnowledgeRetrievalProperties.class
})
public class KnowledgeChunkAutoConfiguration {

    @Bean(name = "bean.knowledgeChunkProperties")
    @Primary
    public KnowledgeChunkProperties knowledgeChunkPropertiesBean(KnowledgeChunkProperties props) {
        props.validate();
        return props;
    }

    @Bean(name = "bean.knowledgeDocumentParseProperties")
    @Primary
    public KnowledgeDocumentParseProperties knowledgeDocumentParsePropertiesBean(
            KnowledgeDocumentParseProperties props) {
        return props;
    }

    @Bean(name = "bean.knowledgeRetrievalProperties")
    @Primary
    public KnowledgeRetrievalProperties knowledgeRetrievalPropertiesBean(KnowledgeRetrievalProperties props) {
        props.validate();
        return props;
    }
}
