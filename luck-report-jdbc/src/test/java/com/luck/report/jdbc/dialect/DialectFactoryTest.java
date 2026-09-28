package com.luck.report.jdbc.dialect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * 方言别名回退单测
 */
public class DialectFactoryTest {

    @Test
    public void aliasTypesFallbackToCompatibleDialect() {
        DialectFactory factory = new DialectFactory();
        assertSame(factory.getDialect(DbType.MYSQL), factory.getDialect(DbType.H2));
        assertSame(factory.getDialect(DbType.MYSQL), factory.getDialect(DbType.MARIADB));
        assertSame(factory.getDialect(DbType.POSTGRE_SQL), factory.getDialect(DbType.KINGBASE_ES));
        assertSame(factory.getDialect(DbType.ORACLE), factory.getDialect(DbType.ORACLE_12C));
        assertSame(factory.getDialect(DbType.SQL_SERVER), factory.getDialect(DbType.SQL_SERVER2005));
        assertNotNull(factory.getDialect(DbType.DM));
    }
}
