package com.luck.report.web.modules.datasource.handler.impl;

import com.luck.report.web.modules.datasource.domain.entity.ReportDatasource;
import com.luck.report.web.modules.datasource.domain.enums.ReportDatasourceTypeEnum;
import com.luck.report.web.modules.datasource.handler.DatasourceTypeHandler;
import org.springframework.stereotype.Component;

/**
 * Oracle数据源类型处理器
 * 负责Oracle类型的JDBC URL生成和Schema提取
 * Oracle的databaseName格式为 "数据库名|Schema名"
 *
 * @author luck
 */
@Component("bean.oracleDatasourceTypeHandler")
public class OracleDatasourceTypeHandler implements DatasourceTypeHandler {

    @Override
    public String typeName() {
        return ReportDatasourceTypeEnum.ORACLE.getTypeName();
    }

    @Override
    public String buildConnectionUrl(ReportDatasource datasource) {
        if (!hasRequiredConnectionFields(datasource)) {
            return datasource.getConnectionUrl();
        }
        return String.format("jdbc:oracle:thin:@%s:%d:%s",
                datasource.getHost(), datasource.getPort(), datasource.getDatabaseName());
    }

    @Override
    public String extractSchemaName(ReportDatasource datasource) {
        String dbName = datasource.getDatabaseName();
        if (dbName != null && dbName.contains("|")) {
            return dbName.split("\\|")[1];
        }
        return datasource.getUsername();
    }
}
