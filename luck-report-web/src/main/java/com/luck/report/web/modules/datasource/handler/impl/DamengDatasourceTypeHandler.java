package com.luck.report.web.modules.datasource.handler.impl;

import com.luck.report.web.modules.datasource.domain.entity.ReportDatasource;
import com.luck.report.web.modules.datasource.domain.enums.ReportDatasourceTypeEnum;
import com.luck.report.web.modules.datasource.handler.DatasourceTypeHandler;
import org.springframework.stereotype.Component;

/**
 * 达梦数据源类型处理器
 *
 * @author luck
 */
@Component("bean.damengDatasourceTypeHandler")
public class DamengDatasourceTypeHandler implements DatasourceTypeHandler {

    @Override
    public String typeName() {
        return ReportDatasourceTypeEnum.DAMENG.getTypeName();
    }

    @Override
    public String buildConnectionUrl(ReportDatasource datasource) {
        if (!hasRequiredConnectionFields(datasource)) {
            return datasource.getConnectionUrl();
        }
        return String.format("jdbc:dm://%s:%d/%s",
                datasource.getHost(), datasource.getPort(), datasource.getDatabaseName());
    }
}
