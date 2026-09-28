package com.luck.report.servlet.javax;

import com.luck.report.web.filter.RequestHolderHandler;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class JavaxRequestHolderFilter implements Filter {
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
