package com.luck.report.web.config.properties;

import java.util.ArrayList;
import java.util.List;

/**
 * 报表 Token 配置项。
 *
 * @author luck-report
 * @since 1.0.0
 */
public class TokenProperties {

    /**
     * 总开关：true 走完整校验链；false 拦截器直接放行。
     */
    private boolean enabled = false;

    /**
     * token header 名称。
     */
    private String headerName = "X-Access-Token";

    /**
     * 是否允许 URL 上传 token（用于 iframe 首次 GET 请求）。
     */
    private boolean allowQueryToken = true;

    /**
     * 报表管理员角色白名单（第三方系统角色编码）。
     */
    private List<String> adminRoles = new ArrayList<>();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public boolean isAllowQueryToken() {
        return allowQueryToken;
    }

    public void setAllowQueryToken(boolean allowQueryToken) {
        this.allowQueryToken = allowQueryToken;
    }

    public List<String> getAdminRoles() {
        return adminRoles;
    }

    public void setAdminRoles(List<String> adminRoles) {
        this.adminRoles = adminRoles;
    }
}
