package com.luck.report.web.filter;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.utils.HttpUtils;
import com.luck.report.infra.modules.servlet.context.RequestHolder;
import com.luck.report.web.i18n.ReportLocaleContext;
import com.luck.report.web.i18n.ReportLocaleResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHolderHandler {
    private static final Logger logger = LoggerFactory.getLogger(RequestHolderHandler.class);

    /**
     * Called before the request chain proceeds. Sets the ApiRequest into RequestHolder.
     * <p>同时解析并绑定本次请求的语言到 ReportLocaleContext，供异常出口按语言返回文案。
     */
    public void beforeRequest(Object nativeRequest) {
        ApiRequest apiRequest = HttpUtils.wrapRequest(nativeRequest);
        RequestHolder.setRequest(apiRequest);
        ReportLocaleContext.set(ReportLocaleResolver.resolve(apiRequest));
        if (logger.isDebugEnabled()) {
            logger.debug("Setting request to RequestHolder for URI: {}", apiRequest.getRequestURI());
        }
    }

    /**
     * Called after the request chain completes. Cleans RequestHolder.
     * <p>必须一并清理 ReportLocaleContext，否则线程复用会残留上一次请求的语言。
     */
    public void afterRequest() {
        if (logger.isDebugEnabled()) {
            ApiRequest req = RequestHolder.getRequest();
            logger.debug("Cleaning request from RequestHolder for URI: {}", req != null ? req.getRequestURI() : "null");
        }
        RequestHolder.clean();
        ReportLocaleContext.clear();
    }
}
