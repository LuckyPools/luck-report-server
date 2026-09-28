package com.luck.report.servlet.jakarta;

import com.luck.report.web.filter.RequestHolderHandler;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

public class JakartaRequestHolderFilter implements Filter {
    private final RequestHolderHandler handler = new RequestHolderHandler();

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        handler.beforeRequest(httpRequest);
        try {
            chain.doFilter(request, response);
        } finally {
            handler.afterRequest();
        }
    }

    @Override
    public void destroy() {}
}
