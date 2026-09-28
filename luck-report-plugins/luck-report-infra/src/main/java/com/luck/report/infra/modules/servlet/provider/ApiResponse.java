package com.luck.report.infra.modules.servlet.provider;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Locale;

/**
 * 抽象 HTTP 响应，屏蔽 javax/jakarta。
 *
 * @author luck-report
 * @since 2.0.0
 */
public interface ApiResponse {

    Object getNativeResponse();

    String getCharacterEncoding();

    String getContentType();

    java.io.OutputStream getOutputStream() throws IOException;

    PrintWriter getWriter() throws IOException;

    void setCharacterEncoding(String charset);

    void setContentLength(int len);

    void setContentLengthLong(long len);

    void setContentType(String type);

    void setBufferSize(int size);

    int getBufferSize();

    void flushBuffer() throws IOException;

    void resetBuffer();

    boolean isCommitted();

    void reset();

    void setLocale(Locale loc);

    Locale getLocale();

    void addCookie(ApiRequest.Cookie cookie);

    boolean containsHeader(String name);

    String encodeURL(String url);

    String encodeRedirectURL(String url);

    String encodeUrl(String url);

    String encodeRedirectUrl(String url);

    void sendError(int sc, String msg) throws IOException;

    void sendError(int sc) throws IOException;

    void sendRedirect(String location) throws IOException;

    void setDateHeader(String name, long date);

    void addDateHeader(String name, long date);

    void setHeader(String name, String value);

    void addHeader(String name, String value);

    void setIntHeader(String name, int value);

    void addIntHeader(String name, int value);

    void setStatus(int sc);

    int getStatus();

    String getHeader(String name);

    Collection<String> getHeaders(String name);

    Collection<String> getHeaderNames();
}
