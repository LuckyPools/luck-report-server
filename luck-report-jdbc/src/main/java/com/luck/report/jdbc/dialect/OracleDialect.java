package com.luck.report.jdbc.dialect;

/**
 * Oracle 分页方言
 */
public class OracleDialect implements IPageDialect {

    @Override
    public DbType getDbType() {
        return DbType.ORACLE;
    }

    @Override
    public String buildPaginationSql(String originalSql, long offset, long limit) {
        long endRow = offset + limit;
        return "SELECT * FROM ( SELECT TMP.*, ROWNUM ROW_ID FROM ( " + originalSql
                + " ) TMP WHERE ROWNUM <= " + endRow + ") WHERE ROW_ID > " + offset;
    }

    @Override
    public String buildCountSql(String originalSql) {
        return "SELECT COUNT(*) FROM (" + originalSql + ") tmp";
    }
}
