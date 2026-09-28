package com.luck.report.infra.exception;

/**
 * Bean 相关运行时异常。
 *
 * @author luck-report
 * @since 1.0.0
 */
public class BeanException extends RuntimeException {

    public BeanException(String message) {
        super(message);
    }

    public BeanException(String message, Throwable cause) {
        super(message, cause);
    }
}
