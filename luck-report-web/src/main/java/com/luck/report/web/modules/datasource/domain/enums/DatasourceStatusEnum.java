package com.luck.report.web.modules.datasource.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据源状态枚举
 * 与数据库 del_flag 之外的业务状态对应
 *
 * @author luck
 */
@Getter
@AllArgsConstructor
public enum DatasourceStatusEnum {

    ACTIVE("active"),
    INACTIVE("inactive");

    /** 状态标识（与数据库存储一致） */
    private final String value;
}
