package com.luck.report.web.modules.datasource.domain.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 数据源连接测试状态枚举
 *
 * @author luck
 */
@Getter
@AllArgsConstructor
public enum DatasourceTestStatusEnum {

    SUCCESS("success"),
    FAILED("failed"),
    UNKNOWN("unknown");

    /**
     * 状态标识（与数据库存储一致）
     */
    private final String value;
}
