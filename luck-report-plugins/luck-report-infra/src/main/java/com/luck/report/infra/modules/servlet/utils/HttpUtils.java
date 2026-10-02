package com.luck.report.infra.modules.servlet.utils;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.provider.ApiResponse;
import com.luck.report.infra.modules.servlet.provider.ApiSession;
import com.luck.report.infra.modules.servlet.provider.ServletAdapterSpi;

import java.util.ServiceLoader;

/**
 * 通过 SPI 获取当前 ApiRequest / ApiResponse / ApiSession，屏蔽 javax/jakarta。
 *
 * @author luck-report
 * @since 2.0.0
 */
public class HttpUtils {

    private static final ServletAdapterSpi ADAPTER;

    static {
        ServiceLoader<ServletAdapterSpi> loader = ServiceLoader.load(ServletAdapterSpi.class);
        ServletAdapterSpi found = null;
        for (ServletAdapterSpi spi : loader) {
            if (found != null) {
                throw new IllegalStateException("发现多个 ServletAdapterSpi 实现，请确保 classpath 中只有一个: "
                        + found.getClass().getName() + " 和 " + spi.getClass().getName());
            }
            found = spi;
        }
        if (found == null) {
            throw new IllegalStateException("未找到 ServletAdapterSpi 实现，"
                    + "请引入 luck-report-spring-boot2-servlet 或 luck-report-spring-boot3-servlet 依赖");
        }
        ADAPTER = found;
    }

    public static ApiRequest getRequest() {
        return ADAPTER.getCurrentRequest();
    }

    public static ApiResponse getResponse() {
        return ADAPTER.getCurrentResponse();
    }

    public static ApiSession getSession() {
        ApiRequest request = getRequest();
        return request != null ? request.getSession(false) : null;
    }

    /**
     * 适配器内部使用；业务侧优先 {@link #getRequest()}
     */
    public static ApiRequest wrapRequest(Object nativeRequest) {
        return ADAPTER.wrapRequest(nativeRequest);
    }

    /**
     * 适配器内部使用；业务侧优先 {@link #getResponse()}
     */
    public static ApiResponse wrapResponse(Object nativeResponse) {
        return ADAPTER.wrapResponse(nativeResponse);
    }

    public static ApiSession wrapSession(Object nativeSession) {
        return ADAPTER.wrapSession(nativeSession);
    }

    public static ServletAdapterSpi getAdapter() {
        return ADAPTER;
    }

    private HttpUtils() {
    }
}
