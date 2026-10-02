package com.luck.report.web.handler;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.web.annotation.Anonymous;
import com.luck.report.web.exception.TokenException;
import com.luck.report.web.modules.report.constant.ReportUrls;
import com.luck.report.web.modules.role.mapper.ReportRoleMapper;
import com.luck.report.web.security.domain.bo.AnonymousRole;
import com.luck.report.web.config.properties.TokenProperties;
import com.luck.report.web.security.service.TokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.method.HandlerMethod;

import java.util.List;

public class TokenInterceptorHandler {
    private static final Logger log = LoggerFactory.getLogger(TokenInterceptorHandler.class);

    private final TokenService tokenService;
    private final TokenProperties props;
    private final ReportRoleMapper roleMapper;

    public TokenInterceptorHandler(TokenService tokenService, TokenProperties props,
                                   @Qualifier("bean.reportRoleMapper") ReportRoleMapper roleMapper) {
        this.tokenService = tokenService;
        this.props = props;
        this.roleMapper = roleMapper;
    }

    public boolean preHandle(ApiRequest apiReq, Object handler) throws Exception {
        if (isAnnotatedAnonymous(handler)) {
            return true;
        }
        if ("OPTIONS".equalsIgnoreCase(apiReq.getMethod())) {
            return true;
        }
        if (!props.isEnabled()) {
            log.debug("[Token] enabled=false, skip verify, requestUri={}", apiReq.getRequestURI());
            return true;
        }
        if (ReportUrls.isPreviewPath(apiReq.getRequestURI()) && checkAndMarkAnonymousReport(apiReq)) {
            log.debug("[Token] 匿名报表放行: uri={}", apiReq.getRequestURI());
            return true;
        }
        String token = resolveToken(apiReq);
        if (token == null || token.isEmpty()) {
            throw new TokenException("error.token.missing");
        }
        if (!tokenService.verifyToken(token)) {
            throw new TokenException("error.token.invalid");
        }

        return true;
    }

    /**
     * 检查当前请求的报表是否绑定了 ANONYMOUS 角色，并将结果写入 request attribute 供下游拦截器复用。
     */
    private boolean checkAndMarkAnonymousReport(ApiRequest apiReq) {
        String reportPath = getFilePath(apiReq);
        if (reportPath == null || reportPath.isEmpty()) {
            apiReq.setAttribute(ReportUrls.ATTR_ANONYMOUS_REPORT, Boolean.FALSE);
            return false;
        }
        List<String> boundRoles = roleMapper.selectRoleCodesByFilePath(reportPath);
        boolean isAnonymous = boundRoles != null && boundRoles.contains(AnonymousRole.CODE);
        apiReq.setAttribute(ReportUrls.ATTR_ANONYMOUS_REPORT, isAnonymous);
        return isAnonymous;
    }

    /**
     * 从请求中提取 reportPath 参数（支持 query、form、multipart）。
     */
    private String getFilePath(ApiRequest apiReq) {
        String reportPath = apiReq.getParameter("reportPath");
        return reportPath;
    }

    private boolean isAnnotatedAnonymous(Object handler) {
        if (!(handler instanceof HandlerMethod)) {
            return false;
        }
        HandlerMethod hm = (HandlerMethod) handler;
        if (hm.getMethodAnnotation(Anonymous.class) != null) {
            return true;
        }
        return hm.getBeanType().getAnnotation(Anonymous.class) != null;
    }

    private String resolveToken(ApiRequest apiReq) {
        if (props.isAllowQueryToken()) {
            String t = apiReq.getParameter("token");
            if (t == null || t.isEmpty()) {
                t = apiReq.getParameter(props.getHeaderName());
            }
            if (t != null && !t.isEmpty()) {
                return t;
            }
        }
        String header = props.getHeaderName();
        String t = apiReq.getHeader(header);
        return (t == null || t.isEmpty()) ? null : t;
    }
}
