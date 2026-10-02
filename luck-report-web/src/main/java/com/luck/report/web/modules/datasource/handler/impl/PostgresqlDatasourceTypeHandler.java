package com.luck.report.web.modules.datasource.handler.impl;

import com.luck.report.web.modules.datasource.domain.entity.ReportDatasource;
import com.luck.report.web.modules.datasource.domain.enums.ReportDatasourceTypeEnum;
import com.luck.report.web.modules.datasource.handler.DatasourceTypeHandler;
import org.springframework.stereotype.Component;

/**
 * PostgreSQL数据源类型处理器
 *
 * @author luck
 */
@Component("bean.postgresqlDatasourceTypeHandler")
public class PostgresqlDatasourceTypeHandler implements DatasourceTypeHandler {

    @Override
    public String typeName() {
        return ReportDatasourceTypeEnum.POSTGRESQL.getTypeName();
    }

    @Override
    public String buildConnectionUrl(ReportDatasource datasource) {
        if (!hasRequiredConnectionFields(datasource)) {
            return datasource.getConnectionUrl();
        }
        String dbName = datasource.getDatabaseName();
        if (dbName != null && dbName.contains("|")) {
            dbName = dbName.split("\\|")[0];
        }
        return String.format("jdbc:postgresql://%s:%d/%s?currentSchema=%s",
                datasource.getHost(), datasource.getPort(), dbName, extractSchemaName(datasource));
    }

    @Override
    public String extractSchemaName(ReportDatasource datasource) {
        String dbName = datasource.getDatabaseName();
        if (dbName != null && dbName.contains("|")) {
            return dbName.split("\\|")[1];
        }
        return "public";
    }
}
