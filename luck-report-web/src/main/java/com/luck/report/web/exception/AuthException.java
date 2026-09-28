package com.luck.report.web.exception;

import com.luck.report.core.exception.ErrorCodeAware;

/**
 * 报表权限异常。
 * <p>由拦截器（ManageInterceptor/PreviewInterceptor）抛出，表示用户无权限访问资源（403 Forbidden）。
 * <p>由 {@code ReportExceptionHandler} 统一转 403 响应。
 *
 * @author luck-report
 * @since 1.0.0
 */
public class AuthException extends RuntimeException implements ErrorCodeAware {

    private static final long serialVersionUID = 1L;

    private final String errorCode;

    private final Object[] errorArgs;

    public AuthException(String message) {
        super(message);
        this.errorCode = null;
        this.errorArgs = null;
    }

    public AuthException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.errorArgs = null;
    }

    /** errorArgs 依次对应 {0}、{1} */
    public AuthException(String errorCode, Object... errorArgs) {
        super(errorCode);
        this.errorCode = errorCode;
        this.errorArgs = errorArgs;
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public Object[] getErrorArgs() {
        return errorArgs;
    }
}
