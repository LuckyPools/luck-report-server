package com.luck.report.jdbc.dialect;

/**
 * 数据库类型枚举
 */
public enum DbType {
    MYSQL("mysql", "MySql数据库"),
    MARIADB("mariadb", "MariaDB数据库"),
    ORACLE("oracle", "Oracle11g及以下数据库(高版本推荐使用ORACLE_NEW)"),
    ORACLE_12C("oracle12c", "Oracle12c+数据库"),
    DB2("db2", "DB2数据库"),
    H2("h2", "H2数据库"),
    HSQL("hsql", "HSQL数据库"),
    SQLITE("sqlite", "SQLite数据库"),
    POSTGRE_SQL("postgresql", "Postgre数据库"),
    SQL_SERVER2005("sqlserver2005", "SQLServer2005数据库"),
    SQL_SERVER("sqlserver", "SQLServer数据库"),
    DM("dm", "达梦数据库"),
    XU_GU("xugu", "虚谷数据库"),
    KINGBASE_ES("kingbasees", "人大金仓数据库"),
    PHOENIX("phoenix", "Phoenix HBase数据库"),
    GAUSS("zenith", "Gauss 数据库"),
    CLICK_HOUSE("clickhouse", "clickhouse 数据库"),
    GBASE("gbase", "南大通用(华库)数据库"),
    GBASE_8S("gbase-8s", "南大通用数据库 GBase 8s"),
    /**
     * @deprecated
     */
    @Deprecated
    GBASEDBT("gbasedbt", "南大通用数据库"),
    /**
     * @deprecated
     */
    @Deprecated
    GBASE_INFORMIX("gbase 8s", "南大通用数据库 GBase 8s"),
    OSCAR("oscar", "神通数据库"),
    SYBASE("sybase", "Sybase ASE 数据库"),
    OCEAN_BASE("oceanbase", "OceanBase 数据库"),
    FIREBIRD("Firebird", "Firebird 数据库"),
    HIGH_GO("highgo", "瀚高数据库"),
    CUBRID("cubrid", "CUBRID数据库"),
    GOLDILOCKS("goldilocks", "GOLDILOCKS数据库"),
    CSIIDB("csiidb", "CSIIDB数据库"),
    SAP_HANA("hana", "SAP_HANA数据库"),
    IMPALA("impala", "impala数据库"),
    VERTICA("vertica", "vertica数据库"),
    XCloud("xcloud", "行云数据库"),
    OTHER("other", "其他数据库");

    private final String db;
    private final String desc;

    /**
     * 按库类型字符串解析枚举
     *
     * @param dbType 库类型字符串，大小写不敏感；未知时返回 OTHER
     * @return 对应枚举
     */
    public static DbType getDbType(String dbType) {
        for (DbType type : values()) {
            if (type.db.equalsIgnoreCase(dbType)) {
                return type;
            }
        }
        return OTHER;
    }

    /**
     * 返回 SQL 资源目录名（如 mysql、postgresql）
     *
     * @return 目录名
     */
    public String getDb() {
        return this.db;
    }

    /**
     * 返回库类型描述
     *
     * @return 描述文案
     */
    public String getDesc() {
        return this.desc;
    }

    DbType(final String db, final String desc) {
        this.db = db;
        this.desc = desc;
    }

    /**
     * 映射到 luck-report/sql 下实际存在的资源目录
     *
     * @return mysql / oracle / postgresql / sqlserver
     * @throws IllegalStateException 当前枚举无对应 SQL 资源目录时抛出
     */
    public String toSqlFolder() {
        switch (this) {
            case ORACLE:
            case ORACLE_12C:
                return "oracle";
            case POSTGRE_SQL:
            case KINGBASE_ES:
                return "postgresql";
            case SQL_SERVER:
            case SQL_SERVER2005:
                return "sqlserver";
            case MYSQL:
            case MARIADB:
            case DM:
            case H2:
                return "mysql";
            default:
                throw new IllegalStateException(
                        "No SQL resource folder for DbType=" + this);
        }
    }
}
