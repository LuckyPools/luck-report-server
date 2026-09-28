package com.luck.report.infra.modules.cache.service.impl;

import com.luck.report.infra.modules.cache.service.ReportCacheKeyResolver;
import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.context.RequestHolder;

/**
 * 以 Session ID 作为缓存隔离前缀。
 *
 * @author 24731
 */
public class SessionCacheKeyResolver implements ReportCacheKeyResolver {

    private boolean disabled;

    @Override
    public boolean disabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }

    @Override
    public String getPrefix() {
        return getSessionId();
    }

    private static String getSessionId() {
        ApiRequest req = RequestHolder.getRequest();
        if (req == null) {
            return null;
        }
        return req.getSession().getId();
    }
}
