package com.luck.report.jdbc.dialect;

/**
 * 数据库分页语句组装接口
 */
public interface IPageDialect {

    /**
     * 获取方言对应的数据库类型
     *
     * @return 数据库类型
     */
    DbType getDbType();

    /**
     * 将原始 SQL 改写为分页 SQL
     *
     * @param originalSql 原始语句，非空
     * @param offset      偏移量
     * @param limit       每页条数
     * @return 分页 SQL
     */
    String buildPaginationSql(String originalSql, long offset, long limit);

    /**
     * 将原始 SQL 包装为 COUNT 查询
     *
     * @param originalSql 原始 SQL，非空
     * @return COUNT SQL
     */
    String buildCountSql(String originalSql);
}
