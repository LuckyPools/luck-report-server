package com.luck.report.infra.modules.servlet.provider;

/**
 * Servlet 适配器 SPI（boot2/boot3 各一实现，ServiceLoader 加载）。
 *
 * @author luck-report
 * @since 2.0.0
 */
public interface ServletAdapterSpi {

    ApiRequest wrapRequest(Object nativeRequest);

    ApiResponse wrapResponse(Object nativeResponse);

    ApiSession wrapSession(Object nativeSession);

    ApiRequest getCurrentRequest();

    ApiResponse getCurrentResponse();
}
