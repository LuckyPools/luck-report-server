package com.luck.report.web.modules.role.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * 角色绑定保存 DTO。
 *
 * @author luck-report
 * @since 1.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportRoleBindingDTO {

    /**
     * 角色编码（与 luck_report_role.role_code 对应）
     */
    @NotBlank(message = "角色编码不能为空")
    private String roleCode;

    /**
     * 角色名称（用于返回 VO / 日志，不入库）
     */
    private String roleName;

    /**
     * 报表来源前缀，如 'file:' / 'db:' / 'classpath:'
     */
    @NotBlank(message = "报表来源不能为空")
    private String provider;

    /**
     * 已拼好前缀的报表路径列表，如 ['file:a.ureport.xml', 'file:b.ureport.xml']
     */
    private List<String> reportPaths;

    /**
     * true 时额外写一条 file_path = '*'
     */
    private boolean hasAll;

    /**
     * 操作人（仅用于日志，不入库）
     */
    private String operator;
}
