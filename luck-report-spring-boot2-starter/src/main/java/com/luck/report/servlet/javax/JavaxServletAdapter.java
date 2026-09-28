package com.luck.report.servlet.javax;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.provider.ApiResponse;
import com.luck.report.infra.modules.servlet.provider.ApiSession;
import com.luck.report.infra.modules.servlet.provider.ServletAdapterSpi;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * javax.servlet（Spring Boot 2.x）适配器 SPI 实现。
 * <p>通过 {@link RequestContextHolder} 获取当前线程绑定的 request / response，
 * 并包装为 {@link ApiRequest} / {@link ApiResponse}。
 *
 * @author luck-report
 * @since 2.0.0
 */
public class JavaxServletAdapter implements ServletAdapterSpi {

    @Override
    public ApiRequest wrapRequest(Object nativeRequest) {
        HttpServletRequest request = (HttpServletRequest) nativeRequest;
        return new JavaxApiRequest(request);
    }

    @Override
    public ApiResponse wrapResponse(Object nativeResponse) {
        HttpServletResponse response = (HttpServletResponse) nativeResponse;
        return new JavaxApiResponse(response);
    }

    @Override
    public ApiSession wrapSession(Object nativeSession) {
        HttpSession session = (HttpSession) nativeSession;
        return new JavaxApiSession(session);
    }

    @Override
    public ApiRequest getCurrentRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        HttpServletRequest request = attrs.getRequest();
        return new JavaxApiRequest(request);
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
        return new JavaxApiResponse(response);
    }
}
