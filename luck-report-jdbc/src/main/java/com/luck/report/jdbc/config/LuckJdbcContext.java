package com.luck.report.jdbc.config;

import com.luck.report.jdbc.dialect.DbType;

import javax.sql.DataSource;

/**
 * 报表 JDBC 运行时上下文：同一 DataSource + DbType，避免重复探测
 */
public class LuckJdbcContext {

    private final DataSource dataSource;

    private final DbType dbType;

    /**
     * 构造运行时上下文
     *
     * @param dataSource 元数据数据源，非空
     * @param dbType     已识别库类型，非 OTHER
     */
    public LuckJdbcContext(DataSource dataSource, DbType dbType) {
        this.dataSource = dataSource;
        this.dbType = dbType;
    }

    /**
     * 获取元数据数据源
     *
     * @return DataSource
     */
    public DataSource getDataSource() {
        return dataSource;
    }

    /**
     * 获取库类型
     *
     * @return DbType
     */
    public DbType getDbType() {
        return dbType;
    }
}
