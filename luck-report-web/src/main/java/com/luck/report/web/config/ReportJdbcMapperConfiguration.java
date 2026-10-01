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
public class ReportJdbcMapperConfiguration {

    /**
     * 注册 ReportDatasourceMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean("bean.reportDatasourceMapper")
    public ReportDatasourceMapper reportDatasourceMapper(
            @Qualifier("bean.luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(ReportDatasourceMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 ReportDatasetMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean("bean.reportDatasetMapper")
    public ReportDatasetMapper reportDatasetMapper(
            @Qualifier("bean.luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(ReportDatasetMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 LogicalRelationMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean("bean.logicalRelationMapper")
    public LogicalRelationMapper logicalRelationMapper(
            @Qualifier("bean.luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(LogicalRelationMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 ReportTemplateMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean("bean.reportTemplateMapper")
    public ReportTemplateMapper reportTemplateMapper(
            @Qualifier("bean.luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(ReportTemplateMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 ReportRoleMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean("bean.reportRoleMapper")
    public ReportRoleMapper reportRoleMapper(
            @Qualifier("bean.luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(ReportRoleMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 ModelConfigMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean("bean.modelConfigMapper")
    public ModelConfigMapper modelConfigMapper(
            @Qualifier("bean.luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(ModelConfigMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 ChatMessageMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean("bean.chatMessageMapper")
    public ChatMessageMapper chatMessageMapper(
            @Qualifier("bean.luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(ChatMessageMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 ChatSessionMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean("bean.chatSessionMapper")
    public ChatSessionMapper chatSessionMapper(
            @Qualifier("bean.luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(ChatSessionMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 AgentKnowledgeMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean("bean.agentKnowledgeMapper")
    public AgentKnowledgeMapper agentKnowledgeMapper(
            @Qualifier("bean.luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(AgentKnowledgeMapper.class, luckSqlExecutor);
    }

    /**
     * 注册 BusinessKnowledgeMapper 代理 Bean
     *
     * @param luckSqlExecutor SQL 执行器
     * @return Mapper 代理
     */
    @Bean("bean.businessKnowledgeMapper")
    public BusinessKnowledgeMapper businessKnowledgeMapper(
            @Qualifier("bean.luckSqlExecutor") LuckSqlExecutor luckSqlExecutor) {
        return LuckMapperProxyFactory.create(BusinessKnowledgeMapper.class, luckSqlExecutor);
    }
}
