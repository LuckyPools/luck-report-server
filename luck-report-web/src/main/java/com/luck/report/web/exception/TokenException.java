package com.luck.report.web.exception;

import com.luck.report.core.exception.ErrorCodeAware;

/**
 * 报表访问 Token 鉴权失败异常。
 * <p>由 {@code TokenInterceptor} 抛出，由 {@code TokenExceptionHandler} 统一转 401 响应。
 *
 * @author luck-report
 * @since 1.0.0
 */
public class TokenException extends RuntimeException implements ErrorCodeAware {

    private static final long serialVersionUID = 1L;

    private final String errorCode;

    private final Object[] errorArgs;

    public TokenException(String message) {
        super(message);
        this.errorCode = null;
        this.errorArgs = null;
    }

    public TokenException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.errorArgs = null;
    }

    /** errorArgs 依次对应 {0}、{1} */
    public TokenException(String errorCode, Object... errorArgs) {
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
