package com.luck.report.infra.modules.servlet.provider;

/**
 * 抽象 HTTP 会话，屏蔽 javax/jakarta。
 *
 * @author luck-report
 * @since 2.0.0
 */
public interface ApiSession {

    Object getNativeSession();

    long getCreationTime();

    String getId();

    long getLastAccessedTime();

    void setMaxInactiveInterval(int interval);

    int getMaxInactiveInterval();

    Object getAttribute(String name);

    java.util.Enumeration<String> getAttributeNames();

    void setAttribute(String name, Object value);

    void removeAttribute(String name);

    void invalidate();

    boolean isNew();
}
