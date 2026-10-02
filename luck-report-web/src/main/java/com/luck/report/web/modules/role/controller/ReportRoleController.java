package com.luck.report.web.modules.role.controller;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.utils.HttpUtils;
import com.luck.report.web.config.properties.TokenProperties;
import com.luck.report.web.modules.role.domain.dto.ReportRoleBindingDTO;
import com.luck.report.web.modules.role.domain.vo.ReportRoleBindingsVo;
import com.luck.report.web.modules.role.service.ReportRoleService;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.core.provider.report.ReportFile;
import com.luck.report.web.modules.role.domain.dto.RoleInfo;
import com.luck.report.web.security.service.TokenService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 角色报表授权管理 Controller。
 *
 * @author luck-report
 * @since 1.2.0
 */
@Slf4j
@RestController("bean.reportRoleController")
@RequestMapping("${luck-report.servletPrefix:}/role")
@AllArgsConstructor
public class ReportRoleController {

    @Qualifier("bean.reportRoleService")
    private final ReportRoleService roleDataService;
    private final TokenService tokenService;
    @Qualifier("bean.tokenProperties")
    private final TokenProperties props;

    @GetMapping("/list")
    public ResultVO<List<RoleInfo>> list() {
        return ResultVO.success(roleDataService.listAllRoles());
    }

    /**
     * 列出某 provider 下所有非目录报表（穿梭框左侧用，不分页）。
     */
    @GetMapping("/reports")
    public ResultVO<List<ReportFile>> listAllReports(@RequestParam String provider) {
        return ResultVO.success(roleDataService.listAllReports(provider));
    }

    /**
     * 某角色在某 provider 下的已绑 file_path（穿梭框右侧初始化，**全量返回，不分页**）
     */
    @GetMapping("/bindings/detail/{roleCode}")
    public ResultVO<ReportRoleBindingsVo> getBindings(
            @PathVariable String roleCode,
            @RequestParam String provider) {
        return ResultVO.success(new ReportRoleBindingsVo(
                roleDataService.getFilePathsByRoleAndProvider(roleCode, provider),
                roleDataService.hasAllBinding(roleCode)));
    }

    /**
     * 保存某角色在某 provider 下的报表绑定（覆盖式物理删+插，可选 '*' 通配）
     */
    @PostMapping("/bindings/save")
    public ResultVO<Void> saveBindings(@Valid @RequestBody ReportRoleBindingDTO req) {
        roleDataService.saveRoleBindings(req.getRoleCode(), req.getRoleName(),
                req.getProvider(), req.getReportPaths(),
                req.isHasAll(), req.getOperator());
        return ResultVO.success();
    }

    /**
     * 物理删除某角色全部绑定（含 '*'）
     */
    @DeleteMapping("/bindings/delete/{roleCode}")
    public ResultVO<Void> delete(@PathVariable String roleCode) {
        roleDataService.removeByRoleCode(roleCode);
        return ResultVO.success();
    }

    /**
     * 轻量管理员检查（前端用，决定是否显示"角色报表"菜单）。
     */
    @GetMapping("/auth/check_admin")
    public ResultVO<Boolean> checkAdmin() {
        if (props == null || !props.isEnabled()) {
            return ResultVO.success(true);
        }
        ApiRequest req = HttpUtils.getRequest();
        List<String> roles = tokenService.getCurrentUserRoles(req);
        List<String> admins = props.getAdminRoles();
        boolean isAdmin = roles != null && admins != null && !admins.isEmpty()
                && roles.stream().anyMatch(admins::contains);
        return ResultVO.success(isAdmin);
    }
}
