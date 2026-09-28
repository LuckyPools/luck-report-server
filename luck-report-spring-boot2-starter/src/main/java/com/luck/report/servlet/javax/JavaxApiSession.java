package com.luck.report.servlet.javax;

import com.luck.report.infra.modules.servlet.provider.ApiSession;

import javax.servlet.http.HttpSession;
import java.util.Enumeration;

/**
 * javax.servlet.http.HttpSession → ApiSession 适配。
 *
 * @author luck-report
 * @since 2.0.0
 */
public class JavaxApiSession implements ApiSession {

    private final HttpSession session;

    public JavaxApiSession(HttpSession session) {
        this.session = session;
    }

    @Override
    public Object getNativeSession() {
        return session;
    }

    @Override
    public long getCreationTime() {
        return session.getCreationTime();
    }

    @Override
    public String getId() {
        return session.getId();
    }

    @Override
    public long getLastAccessedTime() {
        return session.getLastAccessedTime();
    }

    @Override
    public void setMaxInactiveInterval(int interval) {
        session.setMaxInactiveInterval(interval);
    }

    @Override
    public int getMaxInactiveInterval() {
        return session.getMaxInactiveInterval();
    }

    @Override
    public Object getAttribute(String name) {
        return session.getAttribute(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return session.getAttributeNames();
    }

    @Override
    public void setAttribute(String name, Object value) {
        session.setAttribute(name, value);
    }

    @Override
    public void removeAttribute(String name) {
        session.removeAttribute(name);
    }

    @Override
    public void invalidate() {
        session.invalidate();
    }

    @Override
    public boolean isNew() {
        return session.isNew();
    }
}
