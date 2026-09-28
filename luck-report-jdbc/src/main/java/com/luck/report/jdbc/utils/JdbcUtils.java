package com.luck.report.jdbc.utils;

import com.luck.report.jdbc.dialect.DbType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * JDBC URL 工具：识别数据库类型
 */
public final class JdbcUtils {

    private static final Logger log = LoggerFactory.getLogger(JdbcUtils.class);

    private JdbcUtils() {
    }

    /**
     * 根据数据库产品名推断类型
     *
     * @param productName DatabaseMetaData 产品名，如 MySQL、Oracle；可空
     * @return 识别到的 DbType；无法识别时返回 OTHER
     */
    public static DbType getDbTypeByProductName(String productName) {
        if (productName == null || productName.trim().isEmpty()) {
            return DbType.OTHER;
        }
        String token = toJdbcToken(productName.toLowerCase());
        if (token == null) {
            return DbType.OTHER;
        }
        return getDbType("jdbc:" + token + ":");
    }

    private static String toJdbcToken(String product) {
        if (product.contains("mariadb")) {
            return "mariadb";
        }
        if (product.contains("mysql") || product.contains("cobar")) {
            return "mysql";
        }
        if (product.contains("oracle")) {
            return "oracle";
        }
        if (product.contains("sql server") || product.contains("microsoft")) {
            return "microsoft";
        }
        if (product.contains("postgres")) {
            return "postgresql";
        }
        if (product.contains("hsql")) {
            return "hsqldb";
        }
        if (product.contains("db2")) {
            return "db2";
        }
        if (product.contains("sqlite")) {
            return "sqlite";
        }
        if (product.contains("h2")) {
            return "h2";
        }
        if (product.contains("dameng") || product.contains("dm")) {
            return "dm";
        }
        if (product.contains("kingbase")) {
            return "kingbase";
        }
        if (product.contains("clickhouse")) {
            return "clickhouse";
        }
        return null;
    }

    /**
     * 根据 JDBC URL 推断数据库类型
     *
     * @param jdbcUrl JDBC 连接串，非空
     * @return 识别到的 DbType；无法识别时返回 OTHER
     */
    public static DbType getDbType(String jdbcUrl) {
        String url = jdbcUrl.toLowerCase();
        if (!url.contains(":mysql:") && !url.contains(":cobar:")) {
            if (url.contains(":mariadb:")) {
                return DbType.MARIADB;
            } else if (url.contains(":oracle:")) {
                return DbType.ORACLE;
            } else if (!url.contains(":sqlserver:") && !url.contains(":microsoft:")) {
                if (url.contains(":sqlserver2012:")) {
                    return DbType.SQL_SERVER;
                } else if (url.contains(":postgresql:")) {
                    return DbType.POSTGRE_SQL;
                } else if (url.contains(":hsqldb:")) {
                    return DbType.HSQL;
                } else if (url.contains(":db2:")) {
                    return DbType.DB2;
                } else if (url.contains(":sqlite:")) {
                    return DbType.SQLITE;
                } else if (url.contains(":h2:")) {
                    return DbType.H2;
                } else if (regexFind(":dm:", url)) {
                    return DbType.DM;
                } else if (url.contains(":xugu:")) {
                    return DbType.XU_GU;
                } else if (regexFind(":kingbase\\d*:", url)) {
                    return DbType.KINGBASE_ES;
                } else if (url.contains(":phoenix:")) {
                    return DbType.PHOENIX;
                } else if (url.contains(":zenith:")) {
                    return DbType.GAUSS;
                } else if (url.contains(":gbase:")) {
                    return DbType.GBASE;
                } else if (!url.contains(":gbasedbt-sqli:") && !url.contains(":informix-sqli:")) {
                    if (url.contains(":clickhouse:")) {
                        return DbType.CLICK_HOUSE;
                    } else if (url.contains(":oscar:")) {
                        return DbType.OSCAR;
                    } else if (url.contains(":sybase:")) {
                        return DbType.SYBASE;
                    } else if (url.contains(":oceanbase:")) {
                        return DbType.OCEAN_BASE;
                    } else if (url.contains(":highgo:")) {
                        return DbType.HIGH_GO;
                    } else if (url.contains(":cubrid:")) {
                        return DbType.CUBRID;
                    } else if (url.contains(":goldilocks:")) {
                        return DbType.GOLDILOCKS;
                    } else if (url.contains(":csiidb:")) {
                        return DbType.CSIIDB;
                    } else if (url.contains(":sap:")) {
                        return DbType.SAP_HANA;
                    } else if (url.contains(":impala:")) {
                        return DbType.IMPALA;
                    } else if (url.contains(":vertica:")) {
                        return DbType.VERTICA;
                    } else if (url.contains(":xcloud:")) {
                        return DbType.XCloud;
                    } else if (url.contains(":firebirdsql:")) {
                        return DbType.FIREBIRD;
                    } else {
                        log.warn("Cannot detect database type from jdbcUrl: {}", jdbcUrl);
                        return DbType.OTHER;
                    }
                } else {
                    return DbType.GBASE_8S;
                }
            } else {
                return DbType.SQL_SERVER2005;
            }
        } else {
            return DbType.MYSQL;
        }
    }

    /**
     * 判断输入是否匹配正则
     *
     * @param regex 正则
     * @param input 待匹配文本
     * @return 匹配则 true
     */
    public static boolean regexFind(String regex, CharSequence input) {
        return null != input && Pattern.compile(regex).matcher(input).find();
    }
}
