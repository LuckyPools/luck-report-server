package com.luck.report.web.modules.report.domain.enums;

/**
 * 报表导入源格式
 */
public enum ReportImportType {

    /** 当前 V2（不做结构转换） */
    V2,

    /** LuckReport V1（u-* 查询表单） */
    V1,

    /** UReport2（search-form + 原生图表结构） */
    UREPORT;

    /**
     * 方法说明：解析前端传入的导入类型参数
     *
     * @param raw 原始参数，可空；空或未知时按 V2
     * @return 导入类型枚举
     */
    public static ReportImportType fromParam(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return V2;
        }
        switch (raw.trim().toLowerCase()) {
            case "v1":
                return V1;
            case "ureport":
                return UREPORT;
            case "v2":
            default:
                return V2;
        }
    }
}
