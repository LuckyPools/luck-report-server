package com.luck.report.web.modules.datasource.handler.impl;

import com.luck.report.web.modules.datasource.domain.entity.ReportDatasource;
import com.luck.report.web.modules.datasource.domain.enums.ReportDatasourceTypeEnum;
import com.luck.report.web.modules.datasource.handler.DatasourceTypeHandler;
import org.springframework.stereotype.Component;

/**
 * MySQL数据源类型处理器
 *
 * @author luck
 */
@Component("bean.mysqlDatasourceTypeHandler")
public class MysqlDatasourceTypeHandler implements DatasourceTypeHandler {

    @Override
    public String typeName() {
        return ReportDatasourceTypeEnum.MYSQL.getTypeName();
    }

    @Override
    public String buildConnectionUrl(ReportDatasource datasource) {
        if (!hasRequiredConnectionFields(datasource)) {
            return datasource.getConnectionUrl();
        }
        return String.format(
                "jdbc:mysql://%s:%d/%s?useUnicode=true&characterEncoding=utf-8" +
                        "&zeroDateTimeBehavior=convertToNull&tinyInt1isBit=false" +
                        "&allowMultiQueries=true&allowPublicKeyRetrieval=true&useSSL=false" +
                        "&serverTimezone=Asia/Shanghai",
                datasource.getHost(), datasource.getPort(), datasource.getDatabaseName());
    }
}
