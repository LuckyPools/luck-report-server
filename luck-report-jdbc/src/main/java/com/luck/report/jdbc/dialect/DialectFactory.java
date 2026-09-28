package com.luck.report.jdbc.dialect;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 分页方言工厂：显式注册内置方言，不依赖 Spring 容器扫描
 */
public class DialectFactory {

    private final Map<DbType, IPageDialect> dialectMap = new EnumMap<DbType, IPageDialect>(DbType.class);

    /**
     * 使用内置方言列表构造工厂
     */
    public DialectFactory() {
        this(defaultDialects());
    }

    /**
     * 使用给定方言列表构造工厂（后者覆盖同类型前者）
     *
     * @param dialects 方言实现，可为 null
     */
    public DialectFactory(List<IPageDialect> dialects) {
        if (dialects != null) {
            for (IPageDialect dialect : dialects) {
                if (dialect != null && dialect.getDbType() != null) {
                    dialectMap.put(dialect.getDbType(), dialect);
                }
            }
        }
    }

    /**
     * 按库类型字符串获取方言
     *
     * @param type 库类型字符串
     * @return 方言；未注册时返回 null
     */
    public IPageDialect getDialect(String type) {
        return getDialect(DbType.getDbType(type));
    }

    /**
     * 按库类型枚举获取方言
     *
     * @param dbType 库类型
     * @return 方言；未注册时按兼容别名回退（MARIADB/H2→MYSQL、KINGBASE→POSTGRE、ORACLE_12C→ORACLE、SQL_SERVER2005→SQL_SERVER），其余返回 null
     */
    public IPageDialect getDialect(DbType dbType) {
        if (dbType == null) {
            return null;
        }
        IPageDialect dialect = dialectMap.get(dbType);
        if (dialect == null && dbType == DbType.MARIADB) {
            return dialectMap.get(DbType.MYSQL);
        }
        if (dialect == null && dbType == DbType.H2) {
            return dialectMap.get(DbType.MYSQL);
        }
        if (dialect == null && dbType == DbType.ORACLE_12C) {
            return dialectMap.get(DbType.ORACLE);
        }
        if (dialect == null && dbType == DbType.SQL_SERVER2005) {
            return dialectMap.get(DbType.SQL_SERVER);
        }
        if (dialect == null && dbType == DbType.KINGBASE_ES) {
            return dialectMap.get(DbType.POSTGRE_SQL);
        }
        return dialect;
    }

    /**
     * 返回不可变方言表快照
     *
     * @return DbType → 方言
     */
    public Map<DbType, IPageDialect> getDialectMap() {
        return Collections.unmodifiableMap(dialectMap);
    }

    private static List<IPageDialect> defaultDialects() {
        return Arrays.asList(
                new MySqlDialect(),
                new OracleDialect(),
                new PostgreSqlDialect(),
                new SQLServerDialect(),
                new DamengDialect());
    }
}
