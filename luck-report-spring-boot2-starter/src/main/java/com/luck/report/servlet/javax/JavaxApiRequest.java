package com.luck.report.servlet.javax;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.provider.ApiResponse;
import com.luck.report.infra.modules.servlet.provider.ApiSession;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * javax.servlet.http.HttpServletRequest → ApiRequest 适配。
 *
 * @author luck-report
 * @since 2.0.0
 */
public class JavaxApiRequest implements ApiRequest {

    private final HttpServletRequest request;

    public JavaxApiRequest(HttpServletRequest request) {
        this.request = request;
    }

    @Override
    public Object getNativeRequest() {
        return request;
    }

    @Override
    public String getAuthType() {
        return request.getAuthType();
    }

    @Override
    public Cookie[] getCookies() {
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        Cookie[] result = new Cookie[cookies.length];
        for (int i = 0; i < cookies.length; i++) {
            result[i] = wrapCookie(cookies[i]);
        }
        return result;
    }

    @Override
    public long getDateHeader(String name) {
        return request.getDateHeader(name);
    }

    @Override
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return request.getHeaders(name);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return request.getHeaderNames();
    }

    @Override
    public int getIntHeader(String name) {
        return request.getIntHeader(name);
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getPathInfo() {
        return request.getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        return request.getPathTranslated();
    }

    @Override
    public String getContextPath() {
        return request.getContextPath();
    }

    @Override
    public String getQueryString() {
        return request.getQueryString();
    }

    @Override
    public String getRemoteUser() {
        return request.getRemoteUser();
    }

    @Override
    public boolean isUserInRole(String role) {
        return request.isUserInRole(role);
    }

    @Override
    public Principal getUserPrincipal() {
        return request.getUserPrincipal();
    }

    @Override
    public String getRequestedSessionId() {
        return request.getRequestedSessionId();
    }

    @Override
    public String getRequestURI() {
        return request.getRequestURI();
    }

    @Override
    public StringBuffer getRequestURL() {
        return request.getRequestURL();
    }

    @Override
    public String getServletPath() {
        return request.getServletPath();
    }

    @Override
    public ApiSession getSession(boolean create) {
        HttpSession s = request.getSession(create);
        return s != null ? new JavaxApiSession(s) : null;
    }

    @Override
    public ApiSession getSession() {
        HttpSession s = request.getSession();
        return s != null ? new JavaxApiSession(s) : null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return request.isRequestedSessionIdValid();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return request.isRequestedSessionIdFromCookie();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return request.isRequestedSessionIdFromURL();
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return request.isRequestedSessionIdFromUrl();
    }

    @Override
    public Object getAttribute(String name) {
        return request.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return request.getAttributeNames();
    }

    @Override
    public String getCharacterEncoding() {
        return request.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        request.setCharacterEncoding(env);
    }

    @Override
    public int getContentLength() {
        return request.getContentLength();
    }

    @Override
    public long getContentLengthLong() {
        return request.getContentLengthLong();
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        javax.servlet.ServletInputStream sis = request.getInputStream();
        return wrapServletInputStream(sis);
    }

    @Override
    public String getParameter(String name) {
        return request.getParameter(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return request.getParameterNames();
    }

    @Override
    public String[] getParameterValues(String name) {
        return request.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return request.getParameterMap();
    }

    @Override
    public String getProtocol() {
        return request.getProtocol();
    }

    @Override
    public String getScheme() {
        return request.getScheme();
    }

    @Override
    public String getServerName() {
        return request.getServerName();
    }

    @Override
    public int getServerPort() {
        return request.getServerPort();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return request.getReader();
    }

    @Override
    public String getRemoteAddr() {
        return request.getRemoteAddr();
    }

    @Override
    public String getRemoteHost() {
        return request.getRemoteHost();
    }

    @Override
    public void setAttribute(String name, Object o) {
        request.setAttribute(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        request.removeAttribute(name);
    }

    @Override
    public Locale getLocale() {
        return request.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return request.getLocales();
    }

    @Override
    public boolean isSecure() {
        return request.isSecure();
    }

    @Override
    public ApiRequest.RequestDispatcher getRequestDispatcher(String path) {
        javax.servlet.RequestDispatcher rd = request.getRequestDispatcher(path);
        return rd != null ? wrapRequestDispatcher(rd) : null;
    }

    @Override
    public int getRemotePort() {
        return request.getRemotePort();
    }

    @Override
    public String getLocalName() {
        return request.getLocalName();
    }

    @Override
    public String getLocalAddr() {
        return request.getLocalAddr();
    }

    @Override
    public int getLocalPort() {
        return request.getLocalPort();
    }

    @Override
    public ApiRequest.ServletContext getServletContext() {
        javax.servlet.ServletContext sc = request.getServletContext();
        return sc != null ? wrapServletContext(sc) : null;
    }

    private static Cookie wrapCookie(javax.servlet.http.Cookie c) {
        return new Cookie() {
            @Override
            public String getName() { return c.getName(); }
            @Override
            public String getValue() { return c.getValue(); }
            @Override
            public String getPath() { return c.getPath(); }
            @Override
            public String getDomain() { return c.getDomain(); }
            @Override
            public int getMaxAge() { return c.getMaxAge(); }
            @Override
            public boolean isSecure() { return c.getSecure(); }
            @Override
            public boolean isHttpOnly() { return c.isHttpOnly(); }
        };
    }

    private static ServletInputStream wrapServletInputStream(javax.servlet.ServletInputStream sis) {
        return new ServletInputStream() {
            @Override
            public int read() throws IOException { return sis.read(); }
            @Override
            public int read(byte[] b) throws IOException { return sis.read(b); }
            @Override
            public int read(byte[] b, int off, int len) throws IOException { return sis.read(b, off, len); }
            @Override
            public long skip(long n) throws IOException { return sis.skip(n); }
            @Override
            public int available() throws IOException { return sis.available(); }
            @Override
            public void close() throws IOException { sis.close(); }
            @Override
            public boolean isFinished() { return sis.isFinished(); }
            @Override
            public boolean isReady() { return sis.isReady(); }
        };
    }

    private static ApiRequest.RequestDispatcher wrapRequestDispatcher(javax.servlet.RequestDispatcher rd) {
        return new ApiRequest.RequestDispatcher() {
            @Override
            public void forward(ApiRequest request, ApiResponse response) throws IOException, ServletException {
                try {
                    rd.forward((javax.servlet.ServletRequest) request.getNativeRequest(),
                            (javax.servlet.ServletResponse) response.getNativeResponse());
                } catch (javax.servlet.ServletException e) {
                    throw new ServletException(e.getMessage(), e.getRootCause());
                }
            }

            @Override
            public void include(ApiRequest request, ApiResponse response) throws IOException, ServletException {
                try {
                    rd.include((javax.servlet.ServletRequest) request.getNativeRequest(),
                            (javax.servlet.ServletResponse) response.getNativeResponse());
                } catch (javax.servlet.ServletException e) {
                    throw new ServletException(e.getMessage(), e.getRootCause());
                }
            }
        };
    }

    private static ApiRequest.ServletContext wrapServletContext(javax.servlet.ServletContext sc) {
        return new ApiRequest.ServletContext() {
            @Override
            public String getRealPath(String path) { return sc.getRealPath(path); }
            @Override
            public java.io.InputStream getResourceAsStream(String path) { return sc.getResourceAsStream(path); }
            @Override
            public String getMimeType(String file) { return sc.getMimeType(file); }
            @Override
            public String getContextPath() { return sc.getContextPath(); }
            @Override
            public Object getAttribute(String name) { return sc.getAttribute(name); }
            @Override
            public void setAttribute(String name, Object value) { sc.setAttribute(name, value); }
        };
    }
}
