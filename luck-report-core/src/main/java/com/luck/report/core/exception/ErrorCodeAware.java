package com.luck.report.core.exception;

/**
 * 携带文案编码的异常标记接口。出口按请求语言翻译；未实现的存量异常仍返回原 message，因此改造可分批进行。
 */
public interface ErrorCodeAware {

    /**
     * 对应 messages_{lang}.properties 的 key
     */
    String getErrorCode();

    /**
     * 依次对应 {0}、{1}
     */
    Object[] getErrorArgs();
}
