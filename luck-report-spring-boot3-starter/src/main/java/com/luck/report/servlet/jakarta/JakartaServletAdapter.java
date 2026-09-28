package com.luck.report.servlet.jakarta;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.provider.ApiResponse;
import com.luck.report.infra.modules.servlet.provider.ApiSession;
import com.luck.report.infra.modules.servlet.provider.ServletAdapterSpi;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class JakartaServletAdapter implements ServletAdapterSpi {

    @Override
    public ApiRequest wrapRequest(Object nativeRequest) {
        HttpServletRequest request = (HttpServletRequest) nativeRequest;
        return new JakartaApiRequest(request);
    }

    @Override
    public ApiResponse wrapResponse(Object nativeResponse) {
        HttpServletResponse response = (HttpServletResponse) nativeResponse;
        return new JakartaApiResponse(response);
    }

    @Override
    public ApiSession wrapSession(Object nativeSession) {
        HttpSession session = (HttpSession) nativeSession;
        return new JakartaApiSession(session);
    }

    @Override
    public ApiRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        return new JakartaApiRequest(request);
    }

    @Override
    public ApiResponse getCurrentResponse() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletResponse response = attrs.getResponse();
        if (response == null) {
            return null;
        }
        return new JakartaApiResponse(response);
    }
}
