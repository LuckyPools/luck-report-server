package com.luck.report.web.modules.role.service;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.web.modules.role.domain.dto.RoleInfo;
import com.luck.report.web.modules.role.domain.vo.ReportRoleListVo;
import com.luck.report.core.provider.report.ReportFile;
import com.luck.report.web.security.domain.bo.AnonymousRole;
import com.luck.report.web.security.service.TokenService;

import java.util.List;

/**
 * 角色 × 报表 绑定关系数据服务接口。
 *
 * @author luck-report
 * @since 1.2.0
 */
public interface ReportRoleService {

    int TRANSFER_REPORT_LIMIT = 10000;

    /**
     * 获取全量角色列表（第三方角色 + 内置匿名角色）。
     *
     * @return 全量角色列表
     */
    List<RoleInfo> listAllRoles();

    /**
     * 列出所有已绑定角色（去重，用于管理端表格行）。
     *
     * @return 角色列表
     */
    List<ReportRoleListVo> listBoundRoles();

    /**
     * 某角色在某 provider 下的已绑 file_path 列表（穿梭框右侧初始化用，**全量返回，不分页**）。
     *
     * @param roleCode 角色编码
     * @param provider provider 前缀（含冒号）
     * @return file_path 列表
     */
    List<String> getFilePathsByRoleAndProvider(String roleCode, String provider);

    /**
     * 某角色是否绑定 '*'（前端"全部报表"勾选状态用）。
     *
     * @param roleCode 角色编码
     * @return true = 已绑定 '*'
     */
    boolean hasAllBinding(String roleCode);

    /**
     * 列出某 provider 下所有非目录报表（穿梭框左侧用，**全量返回，不分页**）。
     *
     * @param provider provider 前缀（如 'file:' / 'db:'）
     * @return 该 provider 下所有非目录报表
     */
    List<ReportFile> listAllReports(String provider);

    /**
     * 替换某角色在指定 provider 下的全部绑定 + 可选 '*' 通配（穿梭框保存，物理删 + 插）。
     *
     * @param roleCode 角色编码
     * @param roleName 角色名（仅日志）
     * @param provider provider 前缀
     * @param reportPaths 已勾选报表 file_path 列表
     * @param hasAll   是否写一条 '*' 通配
     * @param operator 操作人（仅日志）
     */
    void saveRoleBindings(String roleCode, String roleName, String provider,
                          List<String> reportPaths, boolean hasAll, String operator);

    /**
     * 物理删除某角色全部绑定（含 '*'）。
     *
     * @param roleCode 角色编码
     */
    void deleteByRoleCode(String roleCode);

    /**
     * 预览鉴权核心
     *
     * @param request  HTTP 请求
     * @param reportPath 报表完整路径（带 provider 前缀）
     * @return true = 允许
     */
    boolean canPreview(ApiRequest request, String reportPath);
}
