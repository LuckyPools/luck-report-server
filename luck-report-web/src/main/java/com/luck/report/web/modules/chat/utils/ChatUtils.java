package com.luck.report.web.modules.chat.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luck.report.web.modules.chat.domain.vo.AskModelRequest;
import com.luck.report.web.modules.chat.domain.vo.AskModelResponse;
import com.luck.report.web.modules.modelConfig.domain.entity.ModelConfig;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.luck.report.core.exception.ReportBizException;
import com.luck.report.core.security.SensitiveConfigCipher;
import com.luck.report.web.i18n.ReportI18n;

/**
 * 大模型接口调用工具类
 * 封装 OkHttp 调用大模型 API 的公共逻辑，供 ChatController 和 ChatCompactController 复用
 * 统一管理 HTTP 客户端、请求构建、响应解析等通用操作
 *
 * 调用者：ChatController.chatStream() → ChatUtils.buildStreamCall()
 *         ChatCompactController.compact() → ChatUtils.askModel()
 *
 * @author luck
 */
public class ChatUtils {

    private static final Logger log = LoggerFactory.getLogger(ChatUtils.class);

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 共享的 OkHttp 客户端实例
     * 读超时 120 秒，适配流式场景；非流式场景由调用方自行控制超时
     */
    private static final OkHttpClient SHARED_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    /**
     * 根据模型配置获取 OkHttpClient
     * 如果模型配置启用了代理，则基于 SHARED_CLIENT 派生一个带代理的客户端（共享连接池和线程池）；
     * 否则直接使用无代理的共享客户端
     *
     * @param chatConfig 模型配置
     * @return OkHttpClient 实例
     */
    private static OkHttpClient getOrCreateClient(ModelConfig chatConfig) {
        if (chatConfig.getProxyEnabled() == null || !chatConfig.getProxyEnabled()) {
            return SHARED_CLIENT;
        }

        String proxyHost = chatConfig.getProxyHost();
        Integer proxyPort = chatConfig.getProxyPort();
        if (proxyHost == null || proxyHost.isEmpty() || proxyPort == null) {
            log.warn("[ChatUtils] 代理已启用但主机或端口为空，跳过代理配置");
            return SHARED_CLIENT;
        }

        log.info("[ChatUtils] 使用代理: {}:{}", proxyHost, proxyPort);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        OkHttpClient.Builder builder = SHARED_CLIENT.newBuilder()
                .proxy(proxy);

        // 配置代理认证
        String proxyUsername = chatConfig.getProxyUsername();
        String proxyPassword = chatConfig.getProxyPassword();
        if (proxyUsername != null && !proxyUsername.isEmpty()) {
            builder.proxyAuthenticator((route, response) -> {
                String credential = Credentials.basic(proxyUsername, proxyPassword != null ? proxyPassword : "");
                return response.request().newBuilder()
                        .header("Proxy-Authorization", credential)
                        .build();
            });
        }

        return builder.build();
    }

    /**
     * 调用大模型 API（非流式）
     * 构建 OpenAI 格式的请求体，发送 POST 请求，返回完整响应
     * 适用于对话压缩等不需要流式输出的场景
     *
     * @param request 调用请求参数，包含 chatConfig、messages 等
     * @return AskModelResponse 响应结果，包含状态码和响应体
     * @throws IOException 网络请求异常
     */
    public static AskModelResponse askModel(AskModelRequest request) throws IOException {
        String requestBody = buildOpenAiRequestBody(request);
        OkHttpClient client = getOrCreateClient(request.getChatConfig());

        Request httpRequest = new Request.Builder()
                .url(request.getChatConfig().getBaseUrl() + resolveApiPath(request.getChatConfig(), "/v1/chat/completions"))
                .addHeader("Authorization", "Bearer " + resolveApiKey(request.getChatConfig()))
                .addHeader("Content-Type", "application/json")
                .post(jsonRequestBody(requestBody))
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                return new AskModelResponse(response.code(), responseBody, false);
            }
            return new AskModelResponse(response.code(), responseBody, true);
        }
    }

    /**
     * 构建流式请求的 OkHttp Call 对象
     * 供 ChatController 等流式场景使用，调用方自行处理异步回调
     * 自动设置 stream=true，支持 tools、stream_options 等参数
     *
     * @param request 调用请求参数
     * @return OkHttp Call 对象，可调用 enqueue() 进行异步请求
     */
    public static Call buildStreamCall(AskModelRequest request) {
        request.stream(true);
        String requestBody = buildOpenAiRequestBody(request);
        OkHttpClient client = getOrCreateClient(request.getChatConfig());

        Request httpRequest = new Request.Builder()
                .url(request.getChatConfig().getBaseUrl() + resolveApiPath(request.getChatConfig(), "/v1/chat/completions"))
                .addHeader("Authorization", "Bearer " + resolveApiKey(request.getChatConfig()))
                .addHeader("Content-Type", "application/json")
                .post(jsonRequestBody(requestBody))
                .build();

        return client.newCall(httpRequest);
    }

    public static RequestBody jsonRequestBody(String json) {
        return RequestBody.create(MediaType.parse("application/json"), json);
    }

    private static String resolveApiPath(ModelConfig config, String defaultPath) {
        String path = config.getApiPath();
        return path != null && !path.isEmpty() ? path : defaultPath;
    }

    /**
     * 解析模型 API Key 明文（库内可能为密文或历史明文）
     *
     * @param config 模型配置
     * @return 明文 API Key
     */
    private static String resolveApiKey(ModelConfig config) {
        return SensitiveConfigCipher.decrypt(config.getApiKey());
    }

    /**
     * 构建 OpenAI 格式的请求体 JSON
     * 将消息列表、模型参数、工具定义等组装为标准 OpenAI API 请求格式
     * 支持 Function Calling（tools + tool_choice）和流式选项（stream_options）
     *
     * @param request 调用请求参数
     * @return JSON 格式的请求体字符串
     */
    private static String buildOpenAiRequestBody(AskModelRequest request) {
        Map<String, Object> body = new LinkedHashMap<>(8);
        body.put("model", request.getChatConfig().getModelName());
        body.put("messages", request.getMessages());
        body.put("stream", request.isStream());

        // 温度参数：优先使用请求指定的值，否则使用模型配置中的默认值
        if (request.getTemperature() != null) {
            body.put("temperature", request.getTemperature());
        } else if (request.getChatConfig().getTemperature() != null) {
            body.put("temperature", request.getChatConfig().getTemperature());
        }

        // 仅当 AskModelRequest 显式指定时写入 API max_tokens（如压缩接口限制摘要长度）。
        // ModelConfig.contextWindowTokens 表示上下文窗口，不作为输出上限下发。
        Integer maxTokens = request.getMaxTokens();
        if (maxTokens != null) {
            int capped = Math.min(maxTokens, 8192);
            if (capped < maxTokens) {
                log.warn("[ChatUtils] max_tokens={} 超过上限8192，已自动截断为{}", maxTokens, capped);
            }
            body.put("max_tokens", capped);
        }

        // 工具定义（Function Calling）
        if (request.getTools() != null && !request.getTools().isEmpty()) {
            body.put("tools", request.getTools());

            // 确定 toolChoice：深度思考模式下部分模型（如阿里百炼 Qwen3）不支持 required/object，
            // 需降级为 auto，避免 400 错误
            Object effectiveToolChoice = request.getToolChoice();
            if (effectiveToolChoice != null && Boolean.TRUE.equals(request.getDeepThink())) {
                boolean isRequiredOrObject = "required".equals(effectiveToolChoice)
                        || (effectiveToolChoice instanceof Map);
                if (isRequiredOrObject) {
                    log.info("[ChatUtils] thinking mode 下 toolChoice={} 不兼容，降级为 auto", effectiveToolChoice);
                    effectiveToolChoice = "auto";
                }
            }
            if (effectiveToolChoice != null) {
                body.put("tool_choice", effectiveToolChoice);
            }
        }

        // 工具数量/toolChoice 已由 ChatService 调用入口统一打点，此处仅 debug 避免双份流水账
        log.debug("[ChatUtils] 请求体工具数量: {}, toolChoice: {}, deepThink: {}",
                request.getTools() != null ? request.getTools().size() : 0,
                request.getToolChoice(), request.getDeepThink());

        // 流式选项
        if (request.getStreamOptions() != null) {
            body.put("stream_options", request.getStreamOptions());
        }

        // 深度思考配置
        // 启用后，大模型会先生成推理过程（reasoning_content），再生成最终回复
        // 适用于阿里百炼 Qwen 等支持 thinking 参数的模型
        if (Boolean.TRUE.equals(request.getDeepThink())) {
            // 构建 extra_body 配置，启用思考过程
            Map<String, Object> extraBody = new LinkedHashMap<>(1);
            Map<String, Object> thinking = new LinkedHashMap<>(2);
            thinking.put("type", "thinking");
            thinking.put("budget_tokens", 3000); // 思考 token 预算，可根据需要调整
            extraBody.put("thinking", thinking);
            body.put("extra_body", extraBody);
            log.info("[ChatUtils] 已启用深度思考模式");
        }

        try {
            String jsonBody = objectMapper.writeValueAsString(body);
            // 整包请求体仅 debug：生产排障靠 trace + tool 摘要 + DB 消息，避免刷屏与体积膨胀
            log.debug("[ChatUtils] 实际发送给LLM的请求体: {}", jsonBody);
            return jsonBody;
        } catch (Exception e) {
            log.error("序列化请求体失败", e);
            throw new ReportBizException("error.request.serializeFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 获取共享的 ObjectMapper 实例
     * 供 Controller 复用，避免重复创建
     *
     * @return ObjectMapper 实例
     */
    public static ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * 获取共享的 OkHttpClient 实例
     * 供 Controller 复用，统一连接池管理
     *
     * @return OkHttpClient 实例
     */
    public static OkHttpClient getHttpClient() {
        return SHARED_CLIENT;
    }
}
