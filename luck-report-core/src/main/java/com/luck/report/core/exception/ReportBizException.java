package com.luck.report.core.exception;

/**
 * 通用业务异常，用于替换散落的 {@code RuntimeException} / {@code IllegalArgumentException}。
 * <p>与 ReportException 的区别：ReportException 语义偏"报表内核错误"，本类用于普通业务校验与流程中断。
 */
public class ReportBizException extends RuntimeException implements ErrorCodeAware {

    private static final long serialVersionUID = 1L;

    private final String errorCode;

    private final Object[] errorArgs;

    public ReportBizException(String message) {
        super(message);
        this.errorCode = null;
        this.errorArgs = null;
    }

    public ReportBizException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = null;
        this.errorArgs = null;
    }

    /** errorArgs 依次对应 {0}、{1} */
    public ReportBizException(String errorCode, Object... errorArgs) {
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
