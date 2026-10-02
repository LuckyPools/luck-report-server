package com.luck.report.servlet.javax;

import com.luck.report.web.filter.TraceIdHandler;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 链路追踪过滤器：读取 X-Trace-Id 写入 MDC，并将生效的 traceId 回写响应头
 *
 * @author luck-report
 */
public class JavaxTraceIdFilter implements Filter {

    private final TraceIdHandler handler = new TraceIdHandler();

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String traceId = handler.beforeRequest(httpRequest.getHeader(TraceIdHandler.TRACE_ID_HEADER));
        if (response instanceof HttpServletResponse) {
            ((HttpServletResponse) response).setHeader(TraceIdHandler.TRACE_ID_HEADER, traceId);
        }
        try {
            chain.doFilter(request, response);
        } finally {
            handler.afterRequest();
        }
    }

    @Override
    public void destroy() {}
}
