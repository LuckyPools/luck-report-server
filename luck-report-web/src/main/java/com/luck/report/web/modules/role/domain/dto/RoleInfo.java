package com.luck.report.web.modules.role.domain.dto;

import com.luck.report.web.security.service.TokenService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 角色信息 DTO
 *
 * @author luck-report
 * @since 1.2.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleInfo {

    /**
     * 角色编码（与 luck_report_role.role_code 对应）
     */
    private String code;

    /**
     * 角色名（管理端显示）
     */
    private String name;
}
