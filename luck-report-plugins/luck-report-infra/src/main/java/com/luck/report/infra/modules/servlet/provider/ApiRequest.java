package com.luck.report.infra.modules.servlet.provider;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;

/**
 * 抽象 HTTP 请求，屏蔽 javax/jakarta。
 *
 * @author luck-report
 * @since 2.0.0
 */
public interface ApiRequest {

    /**
     * 原生 request；降级使用时需自行处理 javax/jakarta 差异
     */
    Object getNativeRequest();

    String getAuthType();

    Cookie[] getCookies();

    long getDateHeader(String name);

    String getHeader(String name);

    Enumeration<String> getHeaders(String name);

    Enumeration<String> getHeaderNames();

    int getIntHeader(String name);

    String getMethod();

    String getPathInfo();

    String getPathTranslated();

    String getContextPath();

    String getQueryString();

    String getRemoteUser();

    boolean isUserInRole(String role);

    Principal getUserPrincipal();

    String getRequestedSessionId();

    String getRequestURI();

    StringBuffer getRequestURL();

    String getServletPath();

    ApiSession getSession(boolean create);

    ApiSession getSession();

    boolean isRequestedSessionIdValid();

    boolean isRequestedSessionIdFromCookie();

    boolean isRequestedSessionIdFromURL();

    boolean isRequestedSessionIdFromUrl();

    Object getAttribute(String name);

    Enumeration<String> getAttributeNames();

    String getCharacterEncoding();

    void setCharacterEncoding(String env) throws UnsupportedEncodingException;

    int getContentLength();

    long getContentLengthLong();

    String getContentType();

    ServletInputStream getInputStream() throws IOException;

    String getParameter(String name);

    Enumeration<String> getParameterNames();

    String[] getParameterValues(String name);

    Map<String, String[]> getParameterMap();

    String getProtocol();

    String getScheme();

    String getServerName();

    int getServerPort();

    BufferedReader getReader() throws IOException;

    String getRemoteAddr();

    String getRemoteHost();

    void setAttribute(String name, Object o);

    void removeAttribute(String name);

    Locale getLocale();

    Enumeration<Locale> getLocales();

    boolean isSecure();

    RequestDispatcher getRequestDispatcher(String path);

    int getRemotePort();

    String getLocalName();

    String getLocalAddr();

    int getLocalPort();

    ServletContext getServletContext();

    interface Cookie {
        String getName();
        String getValue();
        String getPath();
        String getDomain();
        int getMaxAge();
        boolean isSecure();
        boolean isHttpOnly();
    }

    /**
     * 不能 extends InputStream（抽象类），由适配器委托原生流
     */
    interface ServletInputStream {
        int read() throws IOException;
        int read(byte[] b) throws IOException;
        int read(byte[] b, int off, int len) throws IOException;
        long skip(long n) throws IOException;
        int available() throws IOException;
        void close() throws IOException;
        boolean isFinished();
        boolean isReady();
    }

    interface RequestDispatcher {
        void forward(ApiRequest request, ApiResponse response) throws IOException, ServletException;
        void include(ApiRequest request, ApiResponse response) throws IOException, ServletException;
    }

    interface ServletContext {
        String getRealPath(String path);
        java.io.InputStream getResourceAsStream(String path);
        String getMimeType(String file);
        String getContextPath();
        Object getAttribute(String name);
        void setAttribute(String name, Object value);
    }

    class ServletException extends Exception {
        public ServletException(String message) { super(message); }
        public ServletException(String message, Throwable rootCause) { super(message, rootCause); }
        public ServletException(Throwable rootCause) { super(rootCause); }
    }
}
