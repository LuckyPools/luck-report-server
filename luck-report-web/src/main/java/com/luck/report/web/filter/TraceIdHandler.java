package com.luck.report.web.filter;

import org.slf4j.MDC;

import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 链路追踪处理器：将前端注入的 X-Trace-Id 写入 MDC，使本次请求的所有日志自动携带链路标识。
 *
 * @author luck-report
 */
public class TraceIdHandler {

    /**
     * 链路追踪请求头名称，前端 agent 执行期间注入
     */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * MDC 中的 traceId 键名；宿主的 logback pattern 需含 %X{traceId} 才会输出，starter 无法代劳
     */
    public static final String MDC_TRACE_ID = "traceId";

    /**
     * traceId 白名单：只允许字母数字与 : . _ -，最长 64 位。
     */
    private static final Pattern VALID_TRACE_ID = Pattern.compile("[A-Za-z0-9:._-]{1,64}");

    /**
     * 请求进入时将 traceId 写入 MDC
     *
     * @param traceId 前端传入的链路标识，String，可为空；为空或不合法时生成兜底值，保证后端日志始终有链路标识
     * @return 实际写入 MDC 的 traceId，String，不可为空；由 Filter 回写到响应头，便于前端/运维核对
     */
    public String beforeRequest(String traceId) {
        String value = sanitize(traceId);
        MDC.put(MDC_TRACE_ID, value);
        return value;
    }

    /**
     * 请求结束时清理 MDC，线程池复用必须清理，否则跨请求串数据
     */
    public void afterRequest() {
        MDC.remove(MDC_TRACE_ID);
    }

    /**
     * 链路标识消毒，阻断日志注入
     *
     * @param traceId 原始链路标识，String，可为空
     * @return 合法则原样返回，否则返回随机 UUID，String，不可为空
     */
    public static String sanitize(String traceId) {
        if (traceId == null) {
            return UUID.randomUUID().toString();
        }
        String value = traceId.trim();
        if (VALID_TRACE_ID.matcher(value).matches()) {
            return value;
        }
        return UUID.randomUUID().toString();
    }
}
