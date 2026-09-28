package com.luck.report.web.modules.datasource.handler.impl;

import com.luck.report.web.modules.datasource.domain.entity.ReportDatasource;
import com.luck.report.web.modules.datasource.domain.enums.ReportDatasourceTypeEnum;
import com.luck.report.web.modules.datasource.handler.DatasourceTypeHandler;
import org.springframework.stereotype.Component;

/**
 * Hive数据源类型处理器
 *
 * @author luck
 */
@Component("bean.hiveDatasourceTypeHandler")
public class HiveDatasourceTypeHandler implements DatasourceTypeHandler {

    @Override
    public String typeName() {
        return ReportDatasourceTypeEnum.HIVE.getTypeName();
    }

    @Override
    public String buildConnectionUrl(ReportDatasource datasource) {
        if (!hasRequiredConnectionFields(datasource)) {
            return datasource.getConnectionUrl();
        }
        return String.format("jdbc:hive2://%s:%d/%s",
                datasource.getHost(), datasource.getPort(), datasource.getDatabaseName());
    }
}
