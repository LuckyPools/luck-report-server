package com.luck.report.web.security.service.impl;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.web.modules.role.service.ReportRoleService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * 报表预览权限校验器。
 *
 * @author luck-report
 * @since 1.2.0
 */
@Component("bean.reportAccessChecker")
@AllArgsConstructor
public class ReportAccessChecker {

    @Qualifier("bean.reportRoleService")
    private final ReportRoleService roleDataService;

    public boolean canPreview(ApiRequest request, String reportPath) {
        return roleDataService.canPreview(request, reportPath);
    }
}
