package com.luck.report.servlet.jakarta;

import com.luck.report.web.filter.TraceIdHandler;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 链路追踪过滤器：读取 X-Trace-Id 写入 MDC，并将生效的 traceId 回写响应头
 *
 * @author luck-report
 */
public class JakartaTraceIdFilter implements Filter {

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
