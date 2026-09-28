package com.luck.report.jdbc.dialect;

/**
 * SQL Server 分页方言
 */
public class SQLServerDialect implements IPageDialect {

    @Override
    public DbType getDbType() {
        return DbType.SQL_SERVER;
    }

    @Override
    public String buildPaginationSql(String originalSql, long offset, long limit) {
        String orderby = getOrderByPart(originalSql);
        String distinctStr = "";
        String loweredString = originalSql.toLowerCase();
        String sqlPartString = originalSql;
        if (loweredString.trim().startsWith("select")) {
            int index = loweredString.indexOf("select") + 6;
            if (loweredString.trim().startsWith("select distinct")) {
                distinctStr = "DISTINCT ";
                index = loweredString.indexOf("select distinct") + 15;
            }
            sqlPartString = originalSql.substring(index);
        }
        if (orderby == null || orderby.trim().isEmpty()) {
            orderby = "ORDER BY CURRENT_TIMESTAMP";
        }
        long firstParam = offset + 1L;
        long secondParam = offset + limit;
        return "WITH selectTemp AS (SELECT " + distinctStr + "TOP 100 PERCENT  ROW_NUMBER() OVER ("
                + orderby + ") as __row_number__, " + sqlPartString
                + ") SELECT * FROM selectTemp WHERE __row_number__ BETWEEN " + firstParam
                + " AND " + secondParam + " ORDER BY __row_number__";
    }

    @Override
    public String buildCountSql(String originalSql) {
        return "SELECT COUNT(*) FROM (" + originalSql + ") tmp";
    }

    private static String getOrderByPart(String sql) {
        String loweredString = sql.toLowerCase();
        int orderByIndex = loweredString.indexOf("order by");
        return orderByIndex != -1 ? sql.substring(orderByIndex) : "";
    }
}
