package com.luck.report.web.modules.role.service.impl;

import com.luck.report.core.exception.ReportBizException;
import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.web.security.domain.bo.AnonymousRole;
import com.luck.report.web.config.properties.TokenProperties;
import com.luck.report.web.modules.role.domain.entity.ReportRole;
import com.luck.report.web.modules.role.domain.vo.ReportRoleListVo;
import com.luck.report.web.modules.role.mapper.ReportRoleMapper;
import com.luck.report.web.modules.role.service.ReportRoleService;
import com.luck.report.core.provider.report.ReportFile;
import com.luck.report.core.provider.report.ReportProvider;
import com.luck.report.web.modules.role.domain.dto.RoleInfo;
import com.luck.report.web.utils.SnowflakeIdGenerator;
import com.luck.report.web.security.service.TokenService;
import com.luck.report.web.security.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 角色 × 报表 绑定关系数据服务实现。
 *
 * @author luck-report
 * @since 1.2.0
 */
@Slf4j
@Service("bean.reportRoleService")
public class ReportRoleServiceImpl implements ReportRoleService, ApplicationContextAware {

    private final ReportRoleMapper roleMapper;
    private final TokenService tokenService;
    private final TokenProperties tokenProperties;

    /**
     * 由 {@link #setApplicationContext} 注入的 ReportProvider 列表副本。
     */
    private List<ReportProvider> reportProviders = Collections.emptyList();

    public ReportRoleServiceImpl(@Qualifier("bean.reportRoleMapper") ReportRoleMapper roleMapper,
                                 TokenService tokenService,
                                 @Qualifier("bean.tokenProperties") TokenProperties tokenProperties) {
        this.roleMapper = roleMapper;
        this.tokenService = tokenService;
        this.tokenProperties = tokenProperties;
    }

    @Override
    public List<RoleInfo> listAllRoles() {
        List<RoleInfo> roles = new ArrayList<>();
        try {
            List<RoleInfo> thirdPartyRoles = tokenService.listAllRoles();
            if (thirdPartyRoles != null) {
                roles.addAll(thirdPartyRoles);
            }
        } catch (Exception e) {
            log.warn("调用 TokenService.listAllRoles() 失败: {}", e.getMessage());
        }
        roles.add(new RoleInfo(AnonymousRole.CODE, AnonymousRole.NAME));
        return roles;
    }

    @Override
    public List<ReportRoleListVo> listBoundRoles() {
        List<String> roleCodes = roleMapper.selectDistinctRoleCodes();
        if (roleCodes == null || roleCodes.isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, String> nameMap = new HashMap<>();
        try {
            List<RoleInfo> allRoles = listAllRoles();
            if (allRoles != null) {
                for (RoleInfo r : allRoles) {
                    if (r != null && r.getCode() != null) {
                        nameMap.put(r.getCode(), r.getName());
                    }
                }
            }
        } catch (Exception e) {
            log.warn("获取角色列表失败，按角色编码降级展示: {}", e.getMessage());
        }

        List<ReportRoleListVo> result = new ArrayList<>(roleCodes.size());
        for (String roleCode : roleCodes) {
            int hasAll = roleMapper.countAllBindingByRole(roleCode);
            int count = roleMapper.countBindingsByRole(roleCode);
            result.add(new ReportRoleListVo(roleCode, nameMap.get(roleCode), count - hasAll, hasAll > 0));
        }
        return result;
    }

    @Override
    public List<String> getFilePathsByRoleAndProvider(String roleCode, String provider) {
        if (roleCode == null || roleCode.isEmpty() || provider == null || provider.isEmpty()) {
            return Collections.emptyList();
        }
        return roleMapper.selectFilePathsByRoleAndProvider(roleCode, provider);
    }

    @Override
    public boolean hasAllBinding(String roleCode) {
        if (roleCode == null || roleCode.isEmpty()) {
            return false;
        }
        return roleMapper.countAllBindingByRole(roleCode) > 0;
    }

    /**
     * 列出某 provider 下所有非目录报表（穿梭框左侧用，**全量返回，不分页**）。
     */
    @Override
    public List<ReportFile> listAllReports(String provider) {
        ReportProvider target = findProviderByPrefix(provider);
        if (target == null) {
            return Collections.emptyList();
        }
        List<ReportFile> all = target.getReportFiles();
        if (all == null || all.isEmpty()) {
            return Collections.emptyList();
        }
        List<ReportFile> files = all.stream()
                .filter(rf -> rf != null && !rf.isDirectory())
                .collect(Collectors.toList());
        if (files.size() > TRANSFER_REPORT_LIMIT) {
            log.warn("穿梭框报表数量过大: provider={}, count={}, limit={}",
                    provider, files.size(), TRANSFER_REPORT_LIMIT);
        }
        return files;
    }

    /**
     * 替换某角色在指定 provider 下的全部绑定 + 可选 '*' 通配（穿梭框保存，物理删+插）。
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRoleBindings(String roleCode, String roleName, String provider,
                                 List<String> reportPaths, boolean hasAll, String operator) {
        if (!StringUtils.hasText(roleCode)) {
            throw new ReportBizException("error.role.roleCodeEmpty");
        }
        if (!StringUtils.hasText(provider)) {
            throw new ReportBizException("error.role.providerEmpty");
        }
        roleMapper.deleteByRoleCodeAndProvider(roleCode, provider);

        Set<String> seen = new HashSet<>();
        if (reportPaths != null) {
            for (String fp : reportPaths) {
                if (fp == null || fp.isEmpty() || "*".equals(fp)) {
                    continue;
                }
                if (!fp.startsWith(provider)) {
                    log.warn("saveRoleBindings 跳过越权路径: roleCode={}, provider={}, reportPath={}",
                            roleCode, provider, fp);
                    continue;
                }
                if (seen.add(fp)) {
                    ReportRole reportRole = new ReportRole();
                    reportRole.setRoleCode(roleCode);
                    reportRole.setReportPath(fp);
                    roleMapper.insert(reportRole);
                }
            }
        }

        if (hasAll) {
            ReportRole reportRole = new ReportRole();
            reportRole.setRoleCode(roleCode);
            reportRole.setReportPath("*");
            roleMapper.insert(reportRole);
        } else {
            roleMapper.deleteBinding(roleCode, "*");
        }

        log.info("保存角色报表绑定: operator={}, roleCode={}, roleName={}, provider={}, fileCount={}, hasAll={}",
                operator, roleCode, roleName, provider,
                reportPaths == null ? 0 : reportPaths.size(), hasAll);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByRoleCode(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            throw new ReportBizException("error.role.roleCodeEmpty");
        }
        int rows = roleMapper.deleteByRoleCode(roleCode);
        log.info("删除角色全部绑定: roleCode={}, rows={}", roleCode, rows);
    }

    /**
     * 预览鉴权核心
     */
    @Override
    public boolean canPreview(ApiRequest request, String reportPath) {
        List<String> userRoles = tokenService.getCurrentUserRoles(request);
        if (userRoles == null || userRoles.isEmpty()) {
            return false;
        }
        List<String> admins = tokenProperties.getAdminRoles();
        if (admins != null && !admins.isEmpty()
                && userRoles.stream().anyMatch(admins::contains)) {
            return true;
        }
        List<String> allRoles = roleMapper.selectRoleCodesByFilePath("*");
        if (allRoles != null && !allRoles.isEmpty()) {
            Set<String> allSet = new HashSet<>(allRoles);
            if (userRoles.stream().anyMatch(allSet::contains)) {
                return true;
            }
        }
        if (reportPath == null || reportPath.isEmpty()) {
            return false;
        }
        List<String> boundRoles = roleMapper.selectRoleCodesByFilePath(reportPath);
        if (boundRoles == null || boundRoles.isEmpty()) {
            return false;
        }
        Set<String> boundSet = new HashSet<>(boundRoles);
        return userRoles.stream().anyMatch(boundSet::contains);
    }

    /**
     * 根据 prefix 查找 ReportProvider。
     */
    private ReportProvider findProviderByPrefix(String prefix) {
        if (prefix == null) {
            return null;
        }
        for (ReportProvider p : reportProviders) {
            if (prefix.equals(p.getPrefix())) {
                return p;
            }
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (!reportProviders.isEmpty()) {
            return;
        }
        List<ReportProvider> beans = new ArrayList<>();
        for (ReportProvider provider : applicationContext.getBeansOfType(ReportProvider.class).values()) {
            if (provider.disabled()) {
                continue;
            }
            beans.add(provider);
        }
        this.reportProviders = Collections.unmodifiableList(beans);
        log.info("RoleDataServiceImpl 初始化完成,共加载 {} 个报表来源", reportProviders.size());
    }
}
