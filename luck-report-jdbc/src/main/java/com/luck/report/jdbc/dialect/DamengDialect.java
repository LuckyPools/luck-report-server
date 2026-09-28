package com.luck.report.jdbc.dialect;

/**
 * 达梦分页方言（语法对齐 MySQL LIMIT）
 */
public class DamengDialect implements IPageDialect {

    @Override
    public DbType getDbType() {
        return DbType.DM;
    }

    @Override
    public String buildPaginationSql(String originalSql, long offset, long limit) {
        StringBuilder sql = new StringBuilder("SELECT * FROM (")
                .append(originalSql)
                .append(") AS tmp LIMIT ");
        if (offset != 0L) {
            sql.append(offset).append(",").append(limit);
        } else {
            sql.append(limit);
        }
        return sql.toString();
    }

    @Override
    public String buildCountSql(String originalSql) {
        return "SELECT COUNT(*) FROM (" + originalSql + ") tmp";
    }
}
