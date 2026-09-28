package com.luck.report.jdbc.dialect;

/**
 * PostgreSQL 分页方言
 */
public class PostgreSqlDialect implements IPageDialect {

    @Override
    public DbType getDbType() {
        return DbType.POSTGRE_SQL;
    }

    @Override
    public String buildPaginationSql(String originalSql, long offset, long limit) {
        return originalSql + " LIMIT " + limit + " OFFSET " + offset;
    }

    @Override
    public String buildCountSql(String originalSql) {
        return "SELECT COUNT(*) FROM (" + originalSql + ") tmp";
    }
}
