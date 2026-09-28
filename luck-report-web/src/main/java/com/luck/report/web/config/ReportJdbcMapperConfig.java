package com.luck.report.web.config;

import com.luck.report.jdbc.LuckMapperProxyFactory;
import com.luck.report.jdbc.LuckSqlExecutor;
import com.luck.report.web.modules.chat.mapper.ChatMessageMapper;
import com.luck.report.web.modules.chat.mapper.ChatSessionMapper;
import com.luck.report.web.modules.dataset.mapper.ReportDatasetMapper;
import com.luck.report.web.modules.datasource.mapper.LogicalRelationMapper;
import com.luck.report.web.modules.datasource.mapper.ReportDatasourceMapper;
import com.luck.report.web.modules.file.mapper.ReportTemplateMapper;
import com.luck.report.web.modules.knowledge.mapper.AgentKnowledgeMapper;
import com.luck.report.web.modules.knowledge.mapper.BusinessKnowledgeMapper;
import com.luck.report.web.modules.modelConfig.mapper.ModelConfigMapper;
import com.luck.report.web.modules.role.mapper.ReportRoleMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 报表元数据 Mapper 代理注册（替代 MyBatis @MapperScan）
 */
@Configuration
public class ReportJdbcMapperConfig {

    /**
     * 注册 ReportDatasourceMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean
    public ReportDatasourceMapper reportDatasourceMapper(
            @Qualifier("luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(ReportDatasourceMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 ReportDatasetMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean
    public ReportDatasetMapper reportDatasetMapper(
            @Qualifier("luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(ReportDatasetMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 LogicalRelationMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean
    public LogicalRelationMapper logicalRelationMapper(
            @Qualifier("luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(LogicalRelationMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 ReportTemplateMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean
    public ReportTemplateMapper reportTemplateMapper(
            @Qualifier("luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(ReportTemplateMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 ReportRoleMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean
    public ReportRoleMapper reportRoleMapper(
            @Qualifier("luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(ReportRoleMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 ModelConfigMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean
    public ModelConfigMapper modelConfigMapper(
            @Qualifier("luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(ModelConfigMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 ChatMessageMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean
    public ChatMessageMapper chatMessageMapper(
            @Qualifier("luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(ChatMessageMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 ChatSessionMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean
    public ChatSessionMapper chatSessionMapper(
            @Qualifier("luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(ChatSessionMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 AgentKnowledgeMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean
    public AgentKnowledgeMapper agentKnowledgeMapper(
            @Qualifier("luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(AgentKnowledgeMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 BusinessKnowledgeMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean
    public BusinessKnowledgeMapper businessKnowledgeMapper(
            @Qualifier("luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(BusinessKnowledgeMapper.class, luckSqlExecutor);
    }
}
