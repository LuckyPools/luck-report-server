package com.luck.report.web.modules.role.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色绑定查询 DTO。
 *
 * @author luck-report
 * @since 1.2.0
 */
@Data
@NoArgsConstructor
public class ReportRoleQueryDTO {

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 报表来源前缀
     */
    private String provider;
}
