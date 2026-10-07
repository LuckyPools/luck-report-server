package com.luck.report.web.modules.chat.service.impl;

import com.luck.report.web.filter.TraceIdHandler;
import com.luck.report.web.i18n.ReportLocale;
import com.luck.report.web.i18n.ReportLocaleContext;
import com.luck.report.web.modules.chat.domain.vo.*;
import com.luck.report.web.modules.chat.service.ChatService;
import com.luck.report.web.modules.chat.utils.ChatUtils;
import com.luck.report.web.modules.modelConfig.domain.entity.ModelConfig;
import com.luck.report.web.modules.modelConfig.service.ModelConfigDataService;
import com.luck.report.web.common.domain.vo.ResultVO;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import okhttp3.*;
import okio.BufferedSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 聊天对话服务
 */
@Service("bean.chatService")
@AllArgsConstructor
public class ChatServiceImpl implements ChatService {

    private static final Logger log = LoggerFactory.getLogger(ChatServiceImpl.class);

    /**
     * 不支持 required/object 形态 tool_choice 的模型缓存（如思考型 deepseek）。
     */
    private static final java.util.concurrent.ConcurrentHashMap<String, Boolean> TOOL_CHOICE_INCOMPATIBLE_MODELS =
            new java.util.concurrent.ConcurrentHashMap<>();

    @Qualifier("bean.modelConfigDataService")
    private final ModelConfigDataService modelConfigDataService;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    /**
     * SSE 流式对话；支持 tools / tool_calls → tool_use 事件。
     */
    @Override
    public SseEmitter chatStream(ChatRequest request) {
        SseEmitter emitter = new SseEmitter(300000L);

        final String traceId = MDC.get(TraceIdHandler.MDC_TRACE_ID);

        try {
            emitter.send(SseEmitter.event().comment("connected"));
        } catch (Exception e) {
            log.warn("Failed to send SSE connected comment: {}", e.getMessage());
        }

        executorService.submit(withContext(traceId, () -> {
            try {
                ModelConfig chatConfig = modelConfigDataService.getChatConfig(request.getModelId());
                List<Map<String, Object>> messages = buildMessages(request);
                List<Map<String, Object>> openaiTools = buildOpenAiTools(request.getTools());

                Object effectiveToolChoice = request.getToolChoice();
                if (effectiveToolChoice == null && openaiTools != null) {
                    effectiveToolChoice = "auto";
                }

                if (effectiveToolChoice != null
                        && isRequiredOrObjectToolChoice(effectiveToolChoice)
                        && Boolean.TRUE.equals(TOOL_CHOICE_INCOMPATIBLE_MODELS.get(chatConfig.getModelName()))) {
                    log.info("[ChatService] 模型 {} 已确认不支持指定型 tool_choice（能力缓存命中），构建时直接降级 auto",
                            chatConfig.getModelName());
                    effectiveToolChoice = "auto";
                }

                log.info("[ChatService] LLM请求: model={}, tools数量={}, toolChoice={}, effectiveToolChoice={}",
                        chatConfig.getModelName(),
                        request.getTools() != null ? request.getTools().size() : 0,
                        request.getToolChoice(), effectiveToolChoice);

                Map<String, Object> streamOptions = new LinkedHashMap<>(1);
                streamOptions.put("include_usage", true);

                AskModelRequest askRequest = new AskModelRequest(chatConfig, messages)
                        .stream(true)
                        .tools(openaiTools)
                        .toolChoice(effectiveToolChoice)
                        .streamOptions(streamOptions)
                        .deepThink(request.getDeepThink());

                int inputTextLength = 0;
                for (Map<String, Object> msg : messages) {
                    Object msgContent = msg.get("content");
                    if (msgContent != null) {
                        inputTextLength += msgContent.toString().length();
                    }
                }
                final int finalInputTextLength = inputTextLength;

                Call call = ChatUtils.buildStreamCall(askRequest);
                java.util.concurrent.atomic.AtomicBoolean emitterCompleted = new java.util.concurrent.atomic.AtomicBoolean(false);

                call.enqueue(withContext(traceId, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        if (emitterCompleted.getAndSet(true)) return;
                        log.error("LLM API call failed: {}", e.getMessage(), e);
                        try {
                            emitter.send(SseEmitter.event().name("error").data("LLM API call failed: " + e.getMessage()));
                            emitter.complete();
                        } catch (Exception ex) {
                            emitter.complete();
                        }
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (!response.isSuccessful()) {
                            String errorMsg = response.body() != null ? response.body().string() : "Unknown error";
                            log.error("大模型API返回错误: status={}, body={}", response.code(), errorMsg);

                            String lowerMsg = errorMsg.toLowerCase();
                            if (response.code() == 400 && lowerMsg.contains("tool_choice")
                                    && lowerMsg.contains("thinking mode")
                                    && isRequiredOrObjectToolChoice(askRequest.getToolChoice())) {
                                TOOL_CHOICE_INCOMPATIBLE_MODELS.putIfAbsent(chatConfig.getModelName(), Boolean.TRUE);
                                log.info("[ChatService] 检测到模型 {} thinking mode 与指定型 tool_choice 冲突，降级为 auto 重试（已写入能力缓存）",
                                        chatConfig.getModelName());
                                AskModelRequest retryRequest = new AskModelRequest(chatConfig, messages)
                                        .stream(true)
                                        .tools(openaiTools)
                                        .toolChoice("auto")
                                        .streamOptions(streamOptions)
                                        .deepThink(request.getDeepThink());
                                Call retryCall = ChatUtils.buildStreamCall(retryRequest);
                                retryCall.enqueue(withContext(traceId, new Callback() {
                                    @Override
                                    public void onFailure(Call c, IOException e) {
                                        if (emitterCompleted.getAndSet(true)) return;
                                        log.error("重试API调用失败: {}", e.getMessage(), e);
                                        try {
                                            emitter.send(SseEmitter.event().name("error").data("LLM API call failed: " + e.getMessage()));
                                            emitter.complete();
                                        } catch (Exception ex) {
                                            emitter.complete();
                                        }
                                    }

                                    @Override
                                    public void onResponse(Call c, Response r) throws IOException {
                                        if (!r.isSuccessful()) {
                                            if (emitterCompleted.getAndSet(true)) return;
                                            String retryErr = r.body() != null ? r.body().string() : "Unknown error";
                                            log.error("重试API仍返回错误: status={}, body={}", r.code(), retryErr);
                                            try {
                                                emitter.send(SseEmitter.event().name("error").data("API error: " + r.code() + ", " + retryErr));
                                                emitter.complete();
                                            } catch (Exception ex) {
                                                emitter.complete();
                                            }
                                            return;
                                        }
                                        ChatServiceImpl.this.handleSuccessfulResponse(r, emitter, finalInputTextLength, emitterCompleted);
                                    }
                                }));
                                return;
                            }

                            if (emitterCompleted.getAndSet(true)) return;
                            try {
                                emitter.send(SseEmitter.event().name("error").data("API error: " + response.code() + ", " + errorMsg));
                                emitter.complete();
                            } catch (Exception ex) {
                                emitter.complete();
                            }
                            return;
                        }

                        handleSuccessfulResponse(response, emitter, finalInputTextLength, emitterCompleted);
                    }
                }));

                emitter.onCompletion(() -> {
                    emitterCompleted.set(true);
                    if (!call.isCanceled()) {
                        call.cancel();
                    }
                });
                emitter.onTimeout(withContext(traceId, () -> {
                    log.warn("SseEmitter超时，取消LLM请求");
                    emitterCompleted.set(true);
                    if (!call.isCanceled()) {
                        call.cancel();
                    }
                    emitter.complete();
                }));

            } catch (Exception | LinkageError e) {
                log.error("Failed to build request: {}", e.getMessage(), e);
                try {
                    emitter.send(SseEmitter.event().name("error").data("Failed to build request: " + e.getMessage()));
                    emitter.complete();
                } catch (Exception ex) {
                    emitter.complete();
                }
            }
        }));

        return emitter;
    }

    /**
     * 包装 Runnable，向线程池任务传播 traceId 与语言。
     */
    private Runnable withContext(String traceId, Runnable task) {
        final ReportLocale locale = ReportLocaleContext.get();
        Runnable localeAware = ReportLocaleContext.wrap(locale, task);
        return () -> {
            if (traceId == null) {
                localeAware.run();
                return;
            }
            MDC.put(TraceIdHandler.MDC_TRACE_ID, traceId);
            try {
                localeAware.run();
            } finally {
                MDC.remove(TraceIdHandler.MDC_TRACE_ID);
            }
        };
    }

    /**
     * 包装 OkHttp Callback 传播 traceId 与语言。
     */
    private Callback withContext(String traceId, Callback callback) {
        final ReportLocale locale = ReportLocaleContext.get();
        return new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (traceId == null) {
                    ReportLocaleContext.wrap(locale, () -> callback.onFailure(call, e)).run();
                    return;
                }
                MDC.put(TraceIdHandler.MDC_TRACE_ID, traceId);
                try {
                    ReportLocaleContext.wrap(locale, () -> callback.onFailure(call, e)).run();
                } finally {
                    MDC.remove(TraceIdHandler.MDC_TRACE_ID);
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ReportLocaleContext.set(locale);
                try {
                    if (traceId == null) {
                        callback.onResponse(call, response);
                        return;
                    }
                    MDC.put(TraceIdHandler.MDC_TRACE_ID, traceId);
                    try {
                        callback.onResponse(call, response);
                    } finally {
                        MDC.remove(TraceIdHandler.MDC_TRACE_ID);
                    }
                } finally {
                    ReportLocaleContext.clear();
                }
            }
        };
    }

    /**
     * LLM 生成结构化摘要，失败时规则兜底。
     */
    @Override
    public ResultVO<CompactResult> compact(CompactRequest request) {
        if (request.getMessages() == null || request.getMessages().isEmpty()) {
            return ResultVO.error("Compact message list is empty");
        }

        if (request.getCompactPrompt() == null || request.getCompactPrompt().isEmpty()) {
            return ResultVO.error("Compact prompt cannot be empty; please pass compactPrompt from the client");
        }

        try {
            ModelConfig chatConfig = modelConfigDataService.getChatConfig(request.getModelId());
            List<Map<String, Object>> messages = buildCompactMessages(request);

            AskModelRequest askRequest = new AskModelRequest(chatConfig, messages)
                    .stream(false)
                    .temperature(0.3)
                    .maxTokens(1024);

            AskModelResponse askResponse = ChatUtils.askModel(askRequest);

            if (!askResponse.isSuccess()) {
                log.error("Compact API call failed: status={}", askResponse.getStatusCode());
                return ResultVO.error("Compact API call failed: " + askResponse.getStatusCode());
            }

            CompactResult result = parseCompactResult(askResponse.getBody());

            if (result == null) {
                log.warn("压缩结果解析失败，使用规则压缩兜底");
                result = fallbackCompact(request);
            }

            log.info("对话压缩完成: summary长度={}, keyOperations数量={}",
                    result.getSummary() != null ? result.getSummary().length() : 0,
                    result.getKeyOperations() != null ? result.getKeyOperations().size() : 0);

            return ResultVO.success(result);
        } catch (Exception e) {
            log.error("对话压缩异常: {}", e.getMessage(), e);
            return ResultVO.error("Conversation compact failed: " + e.getMessage());
        }
    }

    /**
     * 解析 SSE
     */
    @SuppressWarnings("unchecked")
    private void processStreamResponse(BufferedSource source, SseEmitter emitter, int inputTextLength,
                                           java.util.concurrent.atomic.AtomicBoolean emitterCompleted) throws IOException {
        Map<Integer, Map<String, Object>> accumulatedToolCalls = new LinkedHashMap<>();
        int outputTextLength = 0;
        boolean hasRealUsage = false;
        int inputTokens = 0;
        int outputTokens = 0;
        int totalTokens = 0;

        while (!source.exhausted()) {
            if (emitterCompleted.get()) {
                return;
            }

            String line = source.readUtf8Line();
            if (line == null || line.isEmpty()) {
                continue;
            }

            log.debug("[ChatService] SSE行: {}", line.length() > 300 ? line.substring(0, 300) + "..." : line);

            if (!line.startsWith("data: ")) {
                continue;
            }

            String data = line.substring(6).trim();

            if ("[DONE]".equals(data)) {
                flushAccumulatedToolCalls(accumulatedToolCalls, emitter);

                if (!hasRealUsage) {
                    inputTokens = estimateTokens(inputTextLength);
                    outputTokens = estimateTokens(outputTextLength);
                    totalTokens = inputTokens + outputTokens;
                }

                log.info("[ChatService] SSE流结束, outputTextLength={}, toolCalls数量={}, hasRealUsage={}, tokens={}/{}/{}",
                        outputTextLength, accumulatedToolCalls.size(), hasRealUsage,
                        inputTokens, outputTokens, totalTokens);

                Map<String, Object> tokenUsage = new LinkedHashMap<>(3);
                tokenUsage.put("inputTokens", inputTokens);
                tokenUsage.put("outputTokens", outputTokens);
                tokenUsage.put("totalTokens", totalTokens);
                emitter.send(SseEmitter.event().name("token_usage").data(ChatUtils.getObjectMapper().writeValueAsString(tokenUsage)));

                emitter.send(SseEmitter.event().name("done").data("[DONE]"));
                return;
            }

            try {
                Map<String, Object> response = ChatUtils.getObjectMapper().readValue(data, Map.class);
                if (response == null) {
                    continue;
                }

                Map<String, Object> errorInfo = (Map<String, Object>) response.get("error");
                if (errorInfo != null) {
                    String errorMsg = errorInfo.getOrDefault("message", "Unknown API error").toString();
                    log.error("[ChatService] LLM API返回错误: {}", errorMsg);
                    emitter.send(SseEmitter.event().name("error").data("LLM API error: " + errorMsg));
                    return;
                }

                List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
                if (choices == null || choices.isEmpty()) {
                    continue;
                }

                Map<String, Object> choice = choices.get(0);
                Map<String, Object> delta = (Map<String, Object>) choice.get("delta");

                if (delta == null) {
                    continue;
                }

                Object content = delta.get("content");
                if (content != null && !content.toString().isEmpty()) {
                    String contentStr = content.toString();
                    outputTextLength += contentStr.length();
                    emitter.send(SseEmitter.event().name("message")
                            .data(ChatUtils.getObjectMapper().writeValueAsString(contentStr)));
                }

                Object reasoningContent = delta.get("reasoning_content");
                if (reasoningContent != null && !reasoningContent.toString().isEmpty()) {
                    String reasoningStr = reasoningContent.toString();
                    outputTextLength += reasoningStr.length();
                    emitter.send(SseEmitter.event().name("reasoning_content")
                            .data(ChatUtils.getObjectMapper().writeValueAsString(reasoningStr)));
                }

                List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) delta.get("tool_calls");
                if (toolCalls != null) {
                    for (Map<String, Object> tc : toolCalls) {
                        Object indexObj = tc.get("index");
                        int index = indexObj != null ? ((Number) indexObj).intValue() : 0;
                        Map<String, Object> accumulated = accumulatedToolCalls.computeIfAbsent(index, k -> new LinkedHashMap<>());

                        if (tc.containsKey("id")) {
                            Object id = tc.get("id");
                            if (id != null && !id.toString().isEmpty()) {
                                accumulated.put("id", id);
                            }
                        }
                        if (tc.containsKey("type")) {
                            Object type = tc.get("type");
                            if (type != null && !type.toString().isEmpty()) {
                                accumulated.put("type", type);
                            }
                        }
                        Map<String, Object> function = (Map<String, Object>) tc.get("function");
                        if (function != null) {
                            Map<String, Object> accFunction = (Map<String, Object>) accumulated.computeIfAbsent("function", k -> new LinkedHashMap<>());
                            if (function.containsKey("name")) {
                                Object name = function.get("name");
                                if (name != null && !name.toString().isEmpty()) {
                                    accFunction.put("name", name);
                                }
                            }
                            if (function.containsKey("arguments")) {
                                Object argsObj = function.get("arguments");
                                if (argsObj != null) {
                                    String prevArgs = (String) accFunction.getOrDefault("arguments", "");
                                    accFunction.put("arguments", prevArgs + argsObj.toString());
                                }
                            }
                        }
                    }
                }

                String finishReason = (String) choice.get("finish_reason");
                if ("tool_calls".equals(finishReason)) {
                    flushAccumulatedToolCalls(accumulatedToolCalls, emitter);
                    accumulatedToolCalls.clear();
                }

                Map<String, Object> usage = (Map<String, Object>) response.get("usage");
                if (usage != null) {
                    Object promptTokens = usage.get("prompt_tokens");
                    Object completionTokens = usage.get("completion_tokens");
                    Object totalTokensObj = usage.get("total_tokens");
                    if (promptTokens != null && completionTokens != null) {
                        hasRealUsage = true;
                        inputTokens = ((Number) promptTokens).intValue();
                        outputTokens = ((Number) completionTokens).intValue();
                        totalTokens = totalTokensObj != null ? ((Number) totalTokensObj).intValue() : inputTokens + outputTokens;
                        log.debug("API返回真实usage: inputTokens={}, outputTokens={}, totalTokens={}", inputTokens, outputTokens, totalTokens);
                    }
                }
            } catch (Exception e) {
                log.warn("解析SSE数据失败: data={}, error={}", data.length() > 200 ? data.substring(0, 200) + "..." : data, e.getMessage());
            }
        }

        flushAccumulatedToolCalls(accumulatedToolCalls, emitter);
    }

    /**
     * 约 2 字符/token，再加 20% 开销系数。
     */
    private int estimateTokens(int textLength) {
        if (textLength <= 0) return 0;
        return (int) Math.ceil(textLength / 2.0 * 1.2);
    }

    /**
     * 将累积 tool_calls 推为 tool_use 事件（toolCallId / toolName / input）。
     */
    @SuppressWarnings("unchecked")
    private void flushAccumulatedToolCalls(Map<Integer, Map<String, Object>> accumulatedToolCalls, SseEmitter emitter) throws IOException {
        for (Map.Entry<Integer, Map<String, Object>> entry : accumulatedToolCalls.entrySet()) {
            Map<String, Object> tc = entry.getValue();
            String toolCallId = (String) tc.get("id");
            Map<String, Object> function = (Map<String, Object>) tc.get("function");
            if (function == null) {
                log.warn("[ChatService] tool_call function为空, index={}", entry.getKey());
                continue;
            }

            String toolName = (String) function.get("name");
            String argumentsStr = (String) function.getOrDefault("arguments", "{}");
            if (argumentsStr == null || argumentsStr.trim().isEmpty()) {
                argumentsStr = "{}";
            }

            Map<String, Object> input;
            try {
                input = ChatUtils.getObjectMapper().readValue(argumentsStr, Map.class);
            } catch (Exception e) {
                log.warn("解析tool_call arguments失败: toolName={}, length={}, 完整内容=[{}], error={}",
                        toolName,
                        argumentsStr != null ? argumentsStr.length() : 0,
                        argumentsStr != null ? argumentsStr : "",
                        e.getMessage());
                input = new HashMap<>();
                input.put("_rawArguments", argumentsStr != null ? argumentsStr : "");
                input.put("_parseError", e.getMessage() != null ? e.getMessage() : "unknown parse error");
            }

            Map<String, Object> toolUseEvent = new LinkedHashMap<>(3);
            String effectiveToolCallId = (toolCallId != null && !toolCallId.isEmpty())
                ? toolCallId
                : UUID.randomUUID().toString();
            toolUseEvent.put("toolCallId", effectiveToolCallId);
            toolUseEvent.put("toolName", toolName);
            toolUseEvent.put("input", input);

            String eventJson = ChatUtils.getObjectMapper().writeValueAsString(toolUseEvent);
            log.info("[ChatService] 发送tool_use事件: {}", summarizeToolUseForLog(effectiveToolCallId, toolName, input));
            log.debug("[ChatService] tool_use完整事件: {}", eventJson);
            emitter.send(SseEmitter.event().name("tool_use").data(eventJson));
        }
    }

    private static final int TOOL_USE_LOG_MAX = 800;

    /**
     * 压缩 tool_use 日志，避免大入参刷屏。
     */
    private String summarizeToolUseForLog(String toolCallId, String toolName, Map<String, Object> input) {
        Map<String, Object> summary = new LinkedHashMap<>(6);
        summary.put("toolCallId", toolCallId);
        summary.put("toolName", toolName);
        if (input == null || input.isEmpty()) {
            summary.put("inputKeys", java.util.Collections.emptyList());
            return toJsonQuietly(summary);
        }

        if ("plan_tasks".equals(toolName) || "analyze_intent".equals(toolName) || "ask_user".equals(toolName)) {
            String full = toJsonQuietly(input);
            if (full.length() <= TOOL_USE_LOG_MAX) {
                summary.put("input", input);
            } else {
                summary.put("inputLength", full.length());
                summary.put("inputPreview", full.substring(0, TOOL_USE_LOG_MAX) + "...");
            }
            return toJsonQuietly(summary);
        }

        if ("write_cells".equals(toolName)) {
            Object cells = input.get("cells");
            int cellCount = -1;
            if (cells instanceof Map) {
                cellCount = ((Map<?, ?>) cells).size();
            } else if (cells instanceof List) {
                cellCount = ((List<?>) cells).size();
            }
            summary.put("cellCount", cellCount);
            summary.put("inputKeys", input.keySet());
            return toJsonQuietly(summary);
        }

        summary.put("inputKeys", input.keySet());
        String full = toJsonQuietly(input);
        summary.put("inputLength", full.length());
        if (full.length() <= 400) {
            summary.put("input", input);
        } else {
            summary.put("inputPreview", full.substring(0, 400) + "...");
        }
        return toJsonQuietly(summary);
    }

    private String toJsonQuietly(Object value) {
        try {
            return ChatUtils.getObjectMapper().writeValueAsString(value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    /**
     * 将 contextMessages + 当前 message 转为 OpenAI 消息。
     */
    private List<Map<String, Object>> buildMessages(ChatRequest request) {
        List<Map<String, Object>> messages = new ArrayList<>();

        if (request.getContextMessages() != null) {
            for (ContextMessage ctx : request.getContextMessages()) {
                if ("tool_result".equals(ctx.getRole())) {
                    Map<String, Object> toolMsg = new LinkedHashMap<>(3);
                    toolMsg.put("role", "tool");
                    toolMsg.put("content", ctx.getContent());
                    toolMsg.put("tool_call_id", ctx.getToolCallId() != null ? ctx.getToolCallId() : "");
                    messages.add(toolMsg);
                } else if ("assistant".equals(ctx.getRole()) && ctx.getToolCalls() != null && !ctx.getToolCalls().isEmpty()) {
                    Map<String, Object> assistantMsg = new LinkedHashMap<>(4);
                    assistantMsg.put("role", "assistant");
                    assistantMsg.put("content", ctx.getContent() != null ? ctx.getContent() : "");
                    List<Map<String, Object>> toolCallsList = new ArrayList<>();
                    for (ToolCallMessage tc : ctx.getToolCalls()) {
                        Map<String, Object> tcMap = new LinkedHashMap<>(3);
                        tcMap.put("id", tc.getId());
                        tcMap.put("type", tc.getType() != null ? tc.getType() : "function");
                        Map<String, Object> funcMap = new LinkedHashMap<>(2);
                        funcMap.put("name", tc.getFunction().getName());
                        funcMap.put("arguments", tc.getFunction().getArguments());
                        tcMap.put("function", funcMap);
                        toolCallsList.add(tcMap);
                    }
                    assistantMsg.put("tool_calls", toolCallsList);
                    if (ctx.getReasoningContent() != null && !ctx.getReasoningContent().isEmpty()) {
                        assistantMsg.put("reasoning_content", ctx.getReasoningContent());
                    }
                    messages.add(assistantMsg);
                } else {
                    Map<String, Object> msg = new LinkedHashMap<>(2);
                    msg.put("role", ctx.getRole());
                    msg.put("content", ctx.getContent());
                    messages.add(msg);
                }
            }
        }

        if (request.getMessage() != null && !request.getMessage().isEmpty()) {
            Map<String, Object> userMsg = new LinkedHashMap<>(2);
            userMsg.put("role", "user");
            userMsg.put("content", request.getMessage());
            messages.add(userMsg);
        }

        return messages;
    }

    /**
     * 前端 ToolDefinition → OpenAI Function Calling；无工具返回 null。
     */
    private List<Map<String, Object>> buildOpenAiTools(List<ToolDefinition> tools) {
        if (tools == null || tools.isEmpty()) {
            return null;
        }

        List<Map<String, Object>> openaiTools = new ArrayList<>();
        for (ToolDefinition td : tools) {
            Map<String, Object> tool = new LinkedHashMap<>(2);
            tool.put("type", "function");

            Map<String, Object> function = new LinkedHashMap<>(4);
            function.put("name", td.getFunction().getName());
            function.put("description", td.getFunction().getDescription());
            function.put("parameters", td.getFunction().getParameters());
            if (td.getFunction().getOutputSchema() != null) {
                function.put("outputSchema", td.getFunction().getOutputSchema());
            }

            tool.put("function", function);
            openaiTools.add(tool);
        }
        return openaiTools;
    }

    private List<Map<String, Object>> buildCompactMessages(CompactRequest request) {
        List<Map<String, Object>> messages = new ArrayList<>();

        Map<String, Object> systemMsg = new LinkedHashMap<>(2);
        systemMsg.put("role", "system");
        systemMsg.put("content", request.getCompactPrompt());
        messages.add(systemMsg);

        StringBuilder userContent = new StringBuilder();

        if (request.getExistingSummary() != null && !request.getExistingSummary().isEmpty()) {
            userContent.append("[Existing conversation summary]\n").append(request.getExistingSummary()).append("\n\n");
        }

        if (request.getReportSnapshot() != null && !request.getReportSnapshot().isEmpty()) {
            userContent.append("[Current report state snapshot]\n").append(request.getReportSnapshot()).append("\n\n");
        }

        if (request.getExistingKeyOperations() != null && !request.getExistingKeyOperations().isEmpty()) {
            userContent.append("[Existing key operations]\n");
            for (String op : request.getExistingKeyOperations()) {
                userContent.append("- ").append(op).append("\n");
            }
            userContent.append("\n");
        }

        userContent.append("[Conversation history to compact]\n");
        for (ContextMessage ctx : request.getMessages()) {
            String roleLabel;
            if ("user".equals(ctx.getRole())) {
                roleLabel = "User";
            } else if ("assistant".equals(ctx.getRole())) {
                roleLabel = "Assistant";
            } else if ("tool_result".equals(ctx.getRole())) {
                roleLabel = "Tool result(" + (ctx.getToolName() != null ? ctx.getToolName() : "unknown") + ")";
            } else {
                roleLabel = ctx.getRole();
            }
            String content = ctx.getContent();
            if (content != null && content.length() > 500) {
                content = content.substring(0, 300) + "\n...[truncated]...\n" + content.substring(content.length() - 100);
            }
            userContent.append(roleLabel).append(": ").append(content).append("\n");
        }

        userContent.append("\nBased on the information above, generate a compacted summary and a list of key operations.");

        Map<String, Object> userMsg = new LinkedHashMap<>(2);
        userMsg.put("role", "user");
        userMsg.put("content", userContent.toString());
        messages.add(userMsg);

        return messages;
    }

    /**
     * 从非流式响应 content 中提取 JSON 摘要；失败返回 null。
     */
    @SuppressWarnings("unchecked")
    private CompactResult parseCompactResult(String responseBody) {
        try {
            Map<String, Object> response = ChatUtils.getObjectMapper().readValue(responseBody, Map.class);
            if (response == null) return null;

            List<Map<String, Object>> choices = (List<Map<String, Object>>) response.get("choices");
            if (choices == null || choices.isEmpty()) return null;

            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null) return null;

            String content = (String) message.get("content");
            if (content == null || content.isEmpty()) return null;

            content = content.replace('\u201C', '"').replace('\u201D', '"')
                             .replace('\u2018', '\'').replace('\u2019', '\'');

            String jsonStr = extractJson(content);
            if (jsonStr == null) return null;

            Map<String, Object> result = ChatUtils.getObjectMapper().readValue(jsonStr, Map.class);
            if (result == null) return null;

            CompactResult compactResult = new CompactResult();
            compactResult.setSummary((String) result.get("summary"));

            List<String> keyOps = new ArrayList<>();
            Object keyOpsObj = result.get("keyOperations");
            if (keyOpsObj instanceof List) {
                for (Object item : (List<?>) keyOpsObj) {
                    keyOps.add(String.valueOf(item));
                }
            }
            compactResult.setKeyOperations(keyOps);

            if (compactResult.getSummary() == null || compactResult.getSummary().isEmpty()) {
                return null;
            }

            return compactResult;
        } catch (Exception e) {
            log.warn("解析压缩结果异常: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从 LLM 输出提取 JSON（支持 ```json / ``` 包裹或裸对象）。
     */
    private String extractJson(String content) {
        int jsonStart = content.indexOf("```json");
        if (jsonStart >= 0) {
            int jsonEnd = content.indexOf("```", jsonStart + 7);
            if (jsonEnd > jsonStart) {
                return content.substring(jsonStart + 7, jsonEnd).trim();
            }
        }

        int codeStart = content.indexOf("```");
        if (codeStart >= 0) {
            int codeEnd = content.indexOf("```", codeStart + 3);
            if (codeEnd > codeStart) {
                String inner = content.substring(codeStart + 3, codeEnd).trim();
                int braceStart = inner.indexOf('{');
                if (braceStart >= 0) {
                    return inner.substring(braceStart);
                }
            }
        }

        int braceStart = content.indexOf('{');
        int braceEnd = content.lastIndexOf('}');
        if (braceStart >= 0 && braceEnd > braceStart) {
            return content.substring(braceStart, braceEnd + 1);
        }

        return null;
    }

    /**
     * LLM 压缩失败时的规则兜底。
     */
    private CompactResult fallbackCompact(CompactRequest request) {
        StringBuilder summary = new StringBuilder();
        List<String> keyOps = new ArrayList<>();

        if (request.getExistingSummary() != null && !request.getExistingSummary().isEmpty()) {
            summary.append(request.getExistingSummary()).append("\n\n[Follow-up summary]\n");
        }

        for (ContextMessage ctx : request.getMessages()) {
            if ("user".equals(ctx.getRole()) && ctx.getContent() != null) {
                summary.append("User: ").append(ctx.getContent(), 0, Math.min(ctx.getContent().length(), 100)).append("\n");
            } else if ("assistant".equals(ctx.getRole()) && ctx.getContent() != null && !ctx.getContent().isEmpty()) {
                summary.append("Assistant: ").append(ctx.getContent(), 0, Math.min(ctx.getContent().length(), 100)).append("\n");
            } else if ("tool_result".equals(ctx.getRole()) && ctx.getToolName() != null) {
                keyOps.add(ctx.getToolName() + ": " + (ctx.getContent() != null ? ctx.getContent().substring(0, Math.min(ctx.getContent().length(), 80)) : ""));
            }
        }

        if (request.getExistingKeyOperations() != null) {
            keyOps.addAll(0, request.getExistingKeyOperations());
        }

        CompactResult result = new CompactResult();
        result.setSummary(summary.toString());
        result.setKeyOperations(keyOps);
        return result;
    }

    /**
     * 读取 SSE 流并推送；原始请求与 toolChoice 降级重试共用。
     */
    private void handleSuccessfulResponse(Response response, SseEmitter emitter, int inputTextLength,
                                            java.util.concurrent.atomic.AtomicBoolean emitterCompleted) throws IOException {
        ResponseBody body = response.body();
        if (body == null) {
            if (emitterCompleted.getAndSet(true)) return;
            try {
                emitter.send(SseEmitter.event().name("error").data("Response body is empty"));
                emitter.complete();
            } catch (Exception e) {
                emitter.complete();
            }
            return;
        }

        BufferedSource source = body.source();
        try {
            processStreamResponse(source, emitter, inputTextLength, emitterCompleted);
            if (emitterCompleted.compareAndSet(false, true)) {
                emitter.complete();
            }
        } catch (Exception e) {
            if (emitterCompleted.getAndSet(true)) return;
            log.error("SSEStream processing error: {}", e.getMessage(), e);
            try {
                emitter.send(SseEmitter.event().name("error").data("Stream processing error: " + e.getMessage()));
                emitter.complete();
            } catch (Exception ex) {
                emitter.complete();
            }
        } finally {
            body.close();
        }
    }

    /**
     * required 或指定函数对象（Map）在部分 thinking 模型下不被支持。
     */
    private boolean isRequiredOrObjectToolChoice(Object toolChoice) {
        if (toolChoice == null) return false;
        if ("required".equals(toolChoice)) return true;
        return toolChoice instanceof Map;
    }
}
