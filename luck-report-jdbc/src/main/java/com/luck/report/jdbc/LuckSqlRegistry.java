package com.luck.report.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SQL 语句注册表（启动加载，进程内缓存）
 */
public class LuckSqlRegistry {

    private static final Logger log = LoggerFactory.getLogger(LuckSqlRegistry.class);

    private final Map<String, SqlStatement> statements = new ConcurrentHashMap<String, SqlStatement>();

    /**
     * 按 classpath 模式加载 mapper XML
     *
     * @param locationPattern Spring Resource pattern，按库类型目录加载
     */
    public void load(String locationPattern) {
        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources(locationPattern);
            if (resources.length == 0) {
                throw new IllegalStateException(
                        "No SQL resources matched pattern: " + locationPattern);
            }
            LuckSqlParser parser = new LuckSqlParser();
            int count = 0;
            int readable = 0;
            for (Resource resource : resources) {
                if (!resource.exists() || !resource.isReadable()) {
                    continue;
                }
                readable++;
                InputStream input = resource.getInputStream();
                try {
                    Map<String, SqlStatement> parsed = parser.parse(input);
                    for (Map.Entry<String, SqlStatement> entry : parsed.entrySet()) {
                        SqlStatement previous = statements.put(entry.getKey(), entry.getValue());
                        if (previous != null) {
                            throw new IllegalStateException(
                                    "Duplicate SQL id: " + entry.getKey()
                                            + " (resource=" + resource.getDescription() + ")");
                        }
                        count++;
                    }
                } catch (RuntimeException ex) {
                    throw new IllegalStateException(
                            "Failed to parse SQL resource: " + resource.getDescription() + " - " + ex.getMessage(),
                            ex);
                } finally {
                    input.close();
                }
            }
            if (readable == 0) {
                throw new IllegalStateException(
                        "Matched resources are unreadable for pattern: " + locationPattern);
            }
            if (count == 0) {
                throw new IllegalStateException(
                        "No SQL statements loaded from pattern: " + locationPattern);
            }
            log.info("LuckSqlRegistry loaded {} statements from {}", count, locationPattern);
        } catch (IllegalStateException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load SQL resources: " + locationPattern, ex);
        }
    }

    /**
     * 按 id 获取语句
     *
     * @param statementId namespace.id
     * @return 语句
     */
    public SqlStatement getRequired(String statementId) {
        SqlStatement statement = statements.get(statementId);
        if (statement == null) {
            throw new IllegalArgumentException("Unknown SQL statement: " + statementId);
        }
        return statement;
    }

    /**
     * 是否已注册语句
     *
     * @param statementId namespace.id
     * @return 存在则 true
     */
    public boolean contains(String statementId) {
        return statements.containsKey(statementId);
    }

    /**
     * 返回已注册语句只读视图
     *
     * @return id → 语句
     */
    public Map<String, SqlStatement> getStatements() {
        return Collections.unmodifiableMap(statements);
    }

    /**
     * 已注册语句数量
     *
     * @return 数量
     */
    public int size() {
        return statements.size();
    }
}
