package com.luck.report.web.modules.trace.service.impl;

import com.luck.report.infra.modules.servlet.context.RequestHolder;
import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.web.config.properties.AgentTraceProperties;
import com.luck.report.web.filter.TraceIdHandler;
import com.luck.report.web.modules.trace.domain.vo.AgentTraceLogVO;
import com.luck.report.web.modules.trace.domain.vo.AgentTraceReportRequest;
import com.luck.report.web.modules.trace.domain.vo.AgentTraceReportResult;
import com.luck.report.web.modules.trace.service.AgentTraceService;
import com.luck.report.web.modules.chat.utils.ChatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Agent 链路日志服务实现
 *
 * @author luck
 */
@Service("bean.agentTraceService")
public class AgentTraceServiceImpl implements AgentTraceService {

    private final AgentTraceProperties properties;

    /**
     * 链路日志通道，logger 名可配置，宿主据此决定是否隔离到独立文件
     */
    private final Logger traceLog;

    /**
     * 限流计数：key = ip|窗口序号，value = 该窗口内已处理次数
     */
    private final ConcurrentHashMap<String, AtomicInteger> rateBuckets = new ConcurrentHashMap<>();

    /**
     * 已输出过 client_context 的 traceId 集合，保证同一轮链路只打一次上下文
     */
    private final Set<String> printedClientContextTraceIds = ConcurrentHashMap.newKeySet();

    /**
     * client_context 去重集合上限，超限整体清空，防止长驻进程无限增长
     */
    private static final int MAX_CLIENT_CONTEXT_TRACE_IDS = 2048;

    public AgentTraceServiceImpl(@Qualifier("bean.agentTraceProperties") AgentTraceProperties properties) {
        this.properties = properties;
        this.traceLog = LoggerFactory.getLogger(properties.getLoggerName());
    }

    /**
     * 级别白名单：前端 debug 级不上报，越界级别统一降级 info
     */
    private static final Set<String> ALLOWED_LEVELS = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("info", "warn", "error")));
    /**
     * 各字段截断上限（接口侧防线，不信任客户端）
     */
    private static final int MAX_DATA_LENGTH = 8192;
    private static final int MAX_MESSAGE_LENGTH = 1000;
    private static final int MAX_NAMESPACE_LENGTH = 64;
    private static final int MAX_CONTEXT_LENGTH = 500;
    private static final int MAX_VERSION_LENGTH = 64;

    /**
     * 客户端时间戳的输出格式，便于直接人读
     */
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS").withZone(ZoneId.systemDefault());

    private static final long RATE_WINDOW_MS = 60_000L;
    /**
     * 限流桶数量上限，达到后触发一次过期清理，防止异常 IP 把 Map 撑大
     */
    private static final int MAX_RATE_BUCKETS = 4096;

    /**
     * 批量落盘前端上报的链路日志
     *
     * @param request 上报请求，AgentTraceReportRequest，可为空（空列表直接忽略）
     * @return 处理结果，AgentTraceReportResult，不可为空
     */
    @Override
    public AgentTraceReportResult reportLogs(AgentTraceReportRequest request) {
        AgentTraceReportResult result = new AgentTraceReportResult();
        if (!properties.isEnabled()) {
            result.setDisabled(true);
            return result;
        }
        if (request == null || request.getLogs() == null || request.getLogs().isEmpty()) {
            return result;
        }
        if (!allowByRateLimit()) {
            result.setRateLimited(true);
            return result;
        }

        List<AgentTraceLogVO> logs = request.getLogs();
        int maxBatchSize = properties.getMaxBatchSize();
        if (maxBatchSize > 0 && logs.size() > maxBatchSize) {
            traceLog.warn("上报条数超限截断: total={}, kept={}", logs.size(), maxBatchSize);
            result.setDropped(logs.size() - maxBatchSize);
            logs = logs.subList(0, maxBatchSize);
        }

        printClientContext(request, logs.get(0).getTraceId());
        for (AgentTraceLogVO entry : logs) {
            printEntry(entry);
            result.setAccepted(result.getAccepted() + 1);
        }
        return result;
    }

    /**
     * 单条日志规范化并按级别输出到 AGENT_TRACE 通道
     *
     * @param entry 日志条目，AgentTraceLogVO，可为空（跳过）
     */
    private void printEntry(AgentTraceLogVO entry) {
        if (entry == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Map<String, Object> line = new LinkedHashMap<>(9);
        line.put("traceId", TraceIdHandler.sanitize(entry.getTraceId()));
        line.put("seq", entry.getSeq());
        line.put("ct", entry.getTimestamp() != null
                ? TIME_FORMATTER.format(Instant.ofEpochMilli(entry.getTimestamp())) : null);
        line.put("st", TIME_FORMATTER.format(Instant.ofEpochMilli(now)));
        String level = normalizeLevel(entry.getLevel());
        line.put("level", level);
        line.put("namespace", truncate(entry.getNamespace(), MAX_NAMESPACE_LENGTH));
        line.put("message", truncate(entry.getMessage(), MAX_MESSAGE_LENGTH));
        line.put("data", parseDataSafely(truncate(entry.getData(), MAX_DATA_LENGTH)));

        String json = toJsonSafely(line);
        if ("error".equals(level)) {
            traceLog.error(json);
        } else if ("warn".equals(level)) {
            traceLog.warn(json);
        } else {
            traceLog.info(json);
        }
    }

    /**
     * 输出本批日志的客户端上下文（版本/UA/页面）
     *
     * @param request 上报请求，AgentTraceReportRequest，不可为空
     * @param traceId 本批首个条目的 traceId，String，可为空
     */
    private void printClientContext(AgentTraceReportRequest request, String traceId) {
        if (isBlank(request.getAppVersion()) && isBlank(request.getUserAgent())
                && isBlank(request.getPageUrl())) {
            return;
        }
        String sanitizedTraceId = TraceIdHandler.sanitize(traceId);
        String dedupeKey = isBlank(sanitizedTraceId) ? "_" : sanitizedTraceId;
        if (!printedClientContextTraceIds.add(dedupeKey)) {
            return;
        }
        if (printedClientContextTraceIds.size() > MAX_CLIENT_CONTEXT_TRACE_IDS) {
            printedClientContextTraceIds.clear();
            printedClientContextTraceIds.add(dedupeKey);
        }

        Map<String, Object> data = new LinkedHashMap<>(4);
        data.put("appVersion", truncate(request.getAppVersion(), MAX_VERSION_LENGTH));
        data.put("userAgent", truncate(request.getUserAgent(), MAX_CONTEXT_LENGTH));
        data.put("pageUrl", truncate(request.getPageUrl(), MAX_CONTEXT_LENGTH));

        Map<String, Object> line = new LinkedHashMap<>(6);
        line.put("traceId", sanitizedTraceId);
        line.put("level", "info");
        line.put("namespace", "client");
        line.put("message", "client_context");
        line.put("data", data);
        traceLog.info(toJsonSafely(line));
    }

    /**
     * 级别白名单过滤，越界降级 info
     */
    private String normalizeLevel(String level) {
        return level != null && ALLOWED_LEVELS.contains(level) ? level : "info";
    }

    /**
     * 限流判定，按 IP + 分钟窗口计数
     *
     * @return 允许处理返回 true，超出配额返回 false
     */
    private boolean allowByRateLimit() {
        int limit = properties.getRateLimitPerMinute();
        if (limit <= 0) {
            return true;
        }
        long window = System.currentTimeMillis() / RATE_WINDOW_MS;
        if (rateBuckets.size() > MAX_RATE_BUCKETS) {
            purgeExpiredBuckets(window);
        }
        String key = clientKey() + '|' + window;
        return rateBuckets.computeIfAbsent(key, k -> new AtomicInteger()).incrementAndGet() <= limit;
    }

    /**
     * 清理过期窗口的计数桶，保留当前窗口与上一个窗口
     */
    private void purgeExpiredBuckets(long currentWindow) {
        for (String key : rateBuckets.keySet()) {
            int idx = key.lastIndexOf('|');
            if (idx < 0) {
                rateBuckets.remove(key);
                continue;
            }
            try {
                long window = Long.parseLong(key.substring(idx + 1));
                if (window < currentWindow - 1) {
                    rateBuckets.remove(key);
                }
            } catch (NumberFormatException e) {
                rateBuckets.remove(key);
            }
        }
    }

    /**
     * 取限流维度（客户端 IP）
     *
     * @return 客户端 IP，String；取不到时返回 unknown
     */
    private String clientKey() {
        ApiRequest request = RequestHolder.getRequest();
        String addr = request == null ? null : request.getRemoteAddr();
        return isBlank(addr) ? "unknown" : addr;
    }

    /**
     * 长度截断，null 安全
     */
    private String truncate(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...[truncated(len=" + text.length() + ")]";
    }

    private boolean isBlank(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * 尝试把 data 字段解析为 JSON 节点，避免日志行出现双重转义的字符串
     *
     * @param data 前端序列化后的 JSON 字符串，String，可为空
     * @return JsonNode（合法 JSON 时）或原始字符串；入参为空返回 null
     */
    private Object parseDataSafely(String data) {
        if (data == null || data.isEmpty()) {
            return null;
        }
        try {
            return ChatUtils.getObjectMapper().readTree(data);
        } catch (Exception e) {
            return data;
        }
    }

    /**
     * 序列化为 JSON 文本，失败时降级为 Map.toString 保证不丢日志
     *
     * @param line 组装好的日志行字段，Map，不可为空
     * @return JSON 文本
     */
    private String toJsonSafely(Map<String, Object> line) {
        String json;
        try {
            json = ChatUtils.getObjectMapper().writeValueAsString(line);
        } catch (Exception e) {
            json = line.toString();
        }
        return json.replace("\r", "\\r").replace("\n", "\\n");
    }
}
