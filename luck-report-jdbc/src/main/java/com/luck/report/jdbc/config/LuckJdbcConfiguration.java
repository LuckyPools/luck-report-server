package com.luck.report.jdbc.config;

import com.luck.report.jdbc.LuckSqlExecutor;
import com.luck.report.jdbc.LuckSqlRegistry;
import com.luck.report.jdbc.dialect.DbType;
import com.luck.report.jdbc.dialect.DialectFactory;
import com.luck.report.jdbc.dialect.IPageDialect;
import com.luck.report.jdbc.utils.JdbcUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;

/**
 * luck-report-jdbc 配置
 */
@Configuration
@ConditionalOnClass(NamedParameterJdbcTemplate.class)
@EnableConfigurationProperties(LuckJdbcProperties.class)
public class LuckJdbcConfiguration {

    /**
     * 第三方指定报表元数据 CRUD 数据源时使用的 Bean 名
     */
    public static final String LUCK_REPORT_DATA_SOURCE = "bean.luckReportDataSource";

    private static final Logger log = LoggerFactory.getLogger(LuckJdbcConfiguration.class);

    /**
     * 注册内置分页方言工厂
     *
     * @return 方言工厂
     */
    @Bean(name = "bean.luckDialectFactory")
    @ConditionalOnMissingBean(name = "bean.luckDialectFactory")
    public DialectFactory luckDialectFactory() {
        return new DialectFactory();
    }

    /**
     * 解析并缓存 DataSource + DbType（只探测一次）
     *
     * @param properties  配置
     * @param beanFactory Bean 工厂
     * @return 运行时上下文
     */
    @Bean(name = "bean.luckJdbcContext")
    @ConditionalOnMissingBean(name = "bean.luckJdbcContext")
    public LuckJdbcContext luckJdbcContext(LuckJdbcProperties properties, BeanFactory beanFactory) {
        DataSource dataSource = resolveDataSource(beanFactory, properties);
        DbType dbType = detectDbType(dataSource);
        if (dbType == null || dbType == DbType.OTHER) {
            throw new IllegalStateException(
                    "Cannot detect database type for report JDBC DataSource; "
                            + "check connection and JDBC URL");
        }
        log.info("[LuckJdbc] context ready, dbType={}, folder={}", dbType, dbType.toSqlFolder());
        return new LuckJdbcContext(dataSource, dbType);
    }

    /**
     * 创建报表专用 NamedParameterJdbcTemplate
     *
     * @param luckJdbcContext 运行时上下文
     * @return JDBC 模板
     */
    @Bean(name = "bean.luckReportNamedParameterJdbcTemplate")
    @ConditionalOnMissingBean(name = "bean.luckReportNamedParameterJdbcTemplate")
    public NamedParameterJdbcTemplate luckReportNamedParameterJdbcTemplate(
            @Qualifier("bean.luckJdbcContext") LuckJdbcContext luckJdbcContext) {
        return new NamedParameterJdbcTemplate(luckJdbcContext.getDataSource());
    }

    /**
     * 加载并缓存 mapper XML
     *
     * @param properties      配置
     * @param luckJdbcContext 运行时上下文
     * @return SQL 注册表
     */
    @Bean(name = "bean.luckSqlRegistry")
    @ConditionalOnMissingBean(name = "bean.luckSqlRegistry")
    public LuckSqlRegistry luckSqlRegistry(
            LuckJdbcProperties properties,
            @Qualifier("bean.luckJdbcContext") LuckJdbcContext luckJdbcContext) {
        DbType dbType = luckJdbcContext.getDbType();
        String folder = dbType.toSqlFolder();
        String pattern = properties.getSqlLocations().replace("{dbType}", folder);
        LuckSqlRegistry registry = new LuckSqlRegistry();
        registry.load(pattern);
        log.info("[LuckJdbc] SQL registry ready, dbType={}, folder={}, statements={}",
                dbType, folder, registry.size());
        return registry;
    }

    /**
     * 创建 SQL 执行器
     *
     * @param registry                           注册表
     * @param luckReportNamedParameterJdbcTemplate JDBC 模板
     * @param luckDialectFactory                 方言工厂
     * @param luckJdbcContext                    运行时上下文
     * @return 执行器
     */
    @Bean(name = "bean.luckSqlExecutor")
    @ConditionalOnMissingBean(name = "bean.luckSqlExecutor")
    public LuckSqlExecutor luckSqlExecutor(
            @Qualifier("bean.luckSqlRegistry") LuckSqlRegistry registry,
            @Qualifier("bean.luckReportNamedParameterJdbcTemplate") NamedParameterJdbcTemplate luckReportNamedParameterJdbcTemplate,
            @Qualifier("bean.luckDialectFactory") DialectFactory luckDialectFactory,
            @Qualifier("bean.luckJdbcContext") LuckJdbcContext luckJdbcContext) {
        DbType dbType = luckJdbcContext.getDbType();
        IPageDialect pageDialect = luckDialectFactory.getDialect(dbType);
        if (pageDialect == null) {
            throw new IllegalStateException(
                    "No page dialect registered for DbType=" + dbType
                            + " (folder=" + dbType.toSqlFolder() + ")");
        }
        return new LuckSqlExecutor(registry, luckReportNamedParameterJdbcTemplate, luckDialectFactory, pageDialect);
    }

    /**
     * 解析报表元数据主数据源
     *
     * @param beanFactory Bean 工厂
     * @param properties  配置；primaryDatasource 优先
     * @return DataSource
     */
    public static DataSource resolveDataSource(BeanFactory beanFactory, LuckJdbcProperties properties) {
        if (properties != null && StringUtils.hasText(properties.getPrimaryDatasource())) {
            String beanName = properties.getPrimaryDatasource().trim();
            if (!beanFactory.containsBean(beanName)) {
                throw new IllegalStateException(
                        "Configured luck-report.jdbc.primary-datasource bean not found: " + beanName);
            }
            log.info("[LuckJdbc] using DataSource bean from properties: {}", beanName);
            return beanFactory.getBean(beanName, DataSource.class);
        }
        if (beanFactory.containsBean(LUCK_REPORT_DATA_SOURCE)) {
            log.info("[LuckJdbc] using DataSource bean: {}", LUCK_REPORT_DATA_SOURCE);
            return beanFactory.getBean(LUCK_REPORT_DATA_SOURCE, DataSource.class);
        }
        if (beanFactory.containsBean("dataSource")) {
            log.info("[LuckJdbc] using DataSource bean: dataSource");
            return beanFactory.getBean("dataSource", DataSource.class);
        }
        if (beanFactory.containsBean("bean.mainDbDataSource")) {
            log.info("[LuckJdbc] using DataSource bean: bean.mainDbDataSource");
            return beanFactory.getBean("bean.mainDbDataSource", DataSource.class);
        }
        log.info("[LuckJdbc] auto-resolve DataSource by type");
        return beanFactory.getBean(DataSource.class);
    }

    /**
     * 解析报表元数据主数据源（无配置时走 Bean 名约定）
     *
     * @param beanFactory Bean 工厂
     * @return DataSource
     */
    public static DataSource resolveDataSource(BeanFactory beanFactory) {
        return resolveDataSource(beanFactory, null);
    }

    /**
     * 识别 DataSource 对应 DbType
     *
     * @param dataSource 数据源
     * @return DbType
     */
    public static DbType detectDbType(DataSource dataSource) {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            DatabaseMetaData metaData = connection.getMetaData();
            String url = metaData.getURL();
            if (StringUtils.hasText(url)) {
                DbType fromUrl = JdbcUtils.getDbType(url);
                if (fromUrl != null && fromUrl != DbType.OTHER) {
                    return fromUrl;
                }
            }
            DbType fromProduct = JdbcUtils.getDbTypeByProductName(metaData.getDatabaseProductName());
            if (fromProduct != null && fromProduct != DbType.OTHER) {
                return fromProduct;
            }
            throw new IllegalStateException(
                    "Unsupported database, url=" + url
                            + ", product=" + metaData.getDatabaseProductName());
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Failed to detect database type from DataSource: " + ex.getMessage(), ex);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception ignored) {
                    // ignore close failure
                }
            }
        }
    }
}
