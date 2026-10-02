package com.luck.report.web.modules.chat.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luck.report.web.modules.modelConfig.domain.dto.ModelConfigDTO;
import com.luck.report.web.modules.modelConfig.domain.entity.ModelConfig;
import com.luck.report.web.modules.modelConfig.domain.enums.ModelType;
import com.luck.report.web.modules.modelConfig.converter.ModelConfigConverter;
import com.luck.report.web.modules.modelConfig.service.ModelConfigDataService;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.*;

import com.luck.report.core.exception.ReportBizException;
import com.luck.report.core.security.SensitiveConfigCipher;
import com.luck.report.web.i18n.ReportI18n;

/**
 * Embedding 服务
 *
 * @author luck
 */
@Service("bean.embeddingService")
public class EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingService.class);

    /**
     * 单次 Embedding 请求的最大文本条数（超出则自动分批）
     */
    private static final int MAX_EMBED_BATCH = 20;

    /**
     * 单批失败最大重试次数（不含首次）
     */
    private static final int MAX_RETRY = 2;

    private final ModelConfigDataService modelConfigDataService;
    private final OkHttpClient baseHttpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 初始化 Embedding 服务
     *
     * @param modelConfigDataService 模型配置数据服务
     */
    public EmbeddingService(@Qualifier("bean.modelConfigDataService") ModelConfigDataService modelConfigDataService) {
        this.modelConfigDataService = modelConfigDataService;
        this.baseHttpClient = new OkHttpClient.Builder()
                .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build();
    }

    /**
     * 根据嵌入模型配置获取 OkHttpClient
     *
     * @param embeddingConfig 嵌入模型配置
     * @return OkHttpClient 实例
     */
    private OkHttpClient getOrCreateClient(ModelConfig embeddingConfig) {
        if (embeddingConfig.getProxyEnabled() == null || !embeddingConfig.getProxyEnabled()) {
            return baseHttpClient;
        }

        String proxyHost = embeddingConfig.getProxyHost();
        Integer proxyPort = embeddingConfig.getProxyPort();
        if (proxyHost == null || proxyHost.isEmpty() || proxyPort == null) {
            log.warn("[EmbeddingService] 代理已启用但主机或端口为空，跳过代理配置");
            return baseHttpClient;
        }

        log.info("[EmbeddingService] 使用代理: {}:{}", proxyHost, proxyPort);
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
        OkHttpClient.Builder builder = baseHttpClient.newBuilder()
                .proxy(proxy);

        String proxyUsername = embeddingConfig.getProxyUsername();
        String proxyPassword = embeddingConfig.getProxyPassword();
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
     * 将单条文本转为向量（使用默认嵌入模型）
     *
     * @param text 输入文本，不能为空
     * @return float[] 向量数组，维度由模型决定
     */
    public float[] embed(String text) {
        return embed(text, null);
    }

    /**
     * 将单条文本转为向量（指定嵌入模型）
     *
     * @param text    输入文本，不能为空
     * @param modelId 嵌入模型配置ID，为null时使用默认激活的第一个嵌入模型
     * @return float[] 向量数组，维度由模型决定
     */
    public float[] embed(String text, String modelId) {
        List<float[]> results = embedBatch(Collections.singletonList(text), modelId);
        return results.isEmpty() ? new float[0] : results.get(0);
    }

    /**
     * 批量将文本转为向量（使用默认嵌入模型）
     *
     * @param texts 输入文本列表，不能为空
     * @return List&lt;float[]&gt; 向量列表，顺序与输入一致
     */
    public List<float[]> embedBatch(List<String> texts) {
        return embedBatch(texts, null);
    }

    /**
     * 批量将文本转为向量（指定嵌入模型）
     *
     * @param texts   输入文本列表，不能为空
     * @param modelId 嵌入模型配置ID，为null时使用默认激活的第一个嵌入模型
     * @return List&lt;float[]&gt; 向量列表，顺序与输入一致
     */
    public List<float[]> embedBatch(List<String> texts, String modelId) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }

        List<float[]> all = new ArrayList<>(texts.size());
        for (int i = 0; i < texts.size(); i += MAX_EMBED_BATCH) {
            int end = Math.min(i + MAX_EMBED_BATCH, texts.size());
            all.addAll(doEmbedOnceWithRetry(texts.subList(i, end), modelId));
        }
        return all;
    }

    /**
     * 单批 Embedding，失败时最多重试 MAX_RETRY 次（指数退避）
     */
    private List<float[]> doEmbedOnceWithRetry(List<String> batch, String modelId) {
        RuntimeException last = null;
        for (int attempt = 0; attempt <= MAX_RETRY; attempt++) {
            try {
                return doEmbedOnce(batch, modelId);
            } catch (ReportBizException e) {
                last = e;
                if (attempt >= MAX_RETRY) {
                    break;
                }
                long sleepMs = (1L << attempt) * 200L;
                log.warn("Embedding 单批失败，准备重试 {}/{}, sleep={}ms, size={}",
                        attempt + 1, MAX_RETRY, sleepMs, batch.size());
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
            }
        }
        throw last;
    }

    /**
     * 单次 HTTP Embedding 请求（batch 长度应由调用方保证 ≤ MAX_EMBED_BATCH）
     */
    private List<float[]> doEmbedOnce(List<String> texts, String modelId) {
        ModelConfig embeddingConfig = getEmbeddingConfig(modelId);
        String baseUrl = embeddingConfig.getBaseUrl();
        String apiKey = SensitiveConfigCipher.decrypt(embeddingConfig.getApiKey());
        String modelName = embeddingConfig.getModelName();
        String apiPath = embeddingConfig.getApiPath() != null && !embeddingConfig.getApiPath().isEmpty()
                ? embeddingConfig.getApiPath()
                : "/embeddings";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", modelName);
        requestBody.put("input", texts);

        String jsonBody;
        try {
            jsonBody = objectMapper.writeValueAsString(requestBody);
        } catch (Exception e) {
            log.error("序列化请求体失败", e);
            throw new ReportBizException("error.request.serializeFailed", ReportI18n.messageOf(e));
        }
        log.debug("Embedding 请求体(size={}): {}", texts.size(), jsonBody);

        Request httpRequest = new Request.Builder()
                .url(baseUrl + apiPath)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(MediaType.parse("application/json"), jsonBody))
                .build();

        try (Response response = getOrCreateClient(embeddingConfig).newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                String errorMsg = response.body() != null ? response.body().string() : "Unknown error";
                log.error("Embedding API call failed: status={}, body={}", response.code(), errorMsg);
                throw new ReportBizException("error.embedding.apiCallFailed", response.code());
            }

            String responseBody = response.body() != null ? response.body().string() : "";
            return parseEmbeddingResponse(responseBody);
        } catch (IOException e) {
            log.error("Embedding API request error: {}", e.getMessage(), e);
            throw new ReportBizException("error.embedding.apiRequestError", ReportI18n.messageOf(e));
        }
    }

    /**
     * 解析当前默认激活的嵌入模型配置 ID（listEnabled 的第一个）
     *
     * @return 嵌入模型配置 ID
     */
    public String resolveDefaultEmbeddingModelId() {
        return getEmbeddingConfig(null).getId();
    }

    /**
     * 获取嵌入模型配置
     *
     * @param modelId 嵌入模型配置ID，为null时使用默认激活的第一个
     * @return Index 嵌入模型配置
     * @throws RuntimeException 当找不到指定的嵌入模型或无可用嵌入模型时抛出
     */
    private ModelConfig getEmbeddingConfig(String modelId) {
        if (modelId != null) {
            ModelConfig config = modelConfigDataService.findById(modelId);
            if (config == null) {
                throw new ReportBizException("error.embedding.modelNotFound", modelId);
            }
            if (config.getModelType() != ModelType.EMBEDDING) {
                throw new ReportBizException("error.embedding.notEmbeddingModel", modelId);
            }
            log.info("使用指定嵌入模型: id={}, modelName={}", config.getId(), config.getModelName());
            return config;
        }

        List<ModelConfigDTO> enabledConfigs = modelConfigDataService.listEnabledConfigsByType(ModelType.EMBEDDING);
        if (enabledConfigs == null || enabledConfigs.isEmpty()) {
            throw new ReportBizException("error.embedding.noAvailableModel");
        }

        ModelConfigDTO dto = enabledConfigs.get(0);
        log.info("使用默认嵌入模型: id={}, modelName={}", dto.getId(), dto.getModelName());
        return ModelConfigConverter.toEntity(dto);
    }

    /**
     * 解析 Embedding API 响应
     *
     * @param responseBody API 响应 JSON 字符串
     * @return List&lt;float[]&gt; 按 index 排序的向量列表
     */
    @SuppressWarnings("unchecked")
    private List<float[]> parseEmbeddingResponse(String responseBody) {
        try {
            Map<String, Object> responseMap = objectMapper.readValue(responseBody, new TypeReference<Map<String, Object>>() {});

            List<Map<String, Object>> dataList = (List<Map<String, Object>>) responseMap.get("data");
            if (dataList == null || dataList.isEmpty()) {
                log.warn("Embedding API 返回空数据");
                return Collections.emptyList();
            }

            dataList.sort(Comparator.comparingInt(d -> ((Number) d.get("index")).intValue()));

            List<float[]> result = new ArrayList<>();
            for (Map<String, Object> dataItem : dataList) {
                List<Double> embeddingList = (List<Double>) dataItem.get("embedding");
                float[] vector = new float[embeddingList.size()];
                for (int i = 0; i < embeddingList.size(); i++) {
                    vector[i] = embeddingList.get(i).floatValue();
                }
                result.add(vector);
            }

            log.debug("Embedding 成功，返回 {} 条向量，维度 {}", result.size(),
                    result.isEmpty() ? 0 : result.get(0).length);
            return result;
        } catch (Exception e) {
            log.error("Failed to parse Embedding response: {}", e.getMessage(), e);
            throw new ReportBizException("error.embedding.responseParseFailed", ReportI18n.messageOf(e));
        }
    }
}
