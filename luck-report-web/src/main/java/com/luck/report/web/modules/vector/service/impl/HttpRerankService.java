package com.luck.report.web.modules.vector.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.infra.modules.vector.domain.entity.VectorDocument;
import com.luck.report.core.security.SensitiveConfigCipher;
import com.luck.report.web.config.properties.KnowledgeRetrievalProperties;
import com.luck.report.web.modules.modelConfig.converter.ModelConfigConverter;
import com.luck.report.web.modules.modelConfig.domain.dto.ModelConfigDTO;
import com.luck.report.web.modules.modelConfig.domain.entity.ModelConfig;
import com.luck.report.web.modules.modelConfig.domain.enums.ModelType;
import com.luck.report.web.modules.modelConfig.service.ModelConfigDataService;
import com.luck.report.web.modules.vector.service.RerankService;
import com.luck.report.web.modules.vector.utils.ReciprocalRankFusion;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * rerank-enabled 且存在激活 RERANK 模型时精排；否则或失败时退回原序截断。
 */
@Service("bean.httpRerankService")
public class HttpRerankService implements RerankService {

    private static final Logger log = LoggerFactory.getLogger(HttpRerankService.class);
    private static final int MAX_CANDIDATES = 50;
    private static final String DEFAULT_PATH = "/rerank";
    private static final MediaType JSON = MediaType.parse("application/json");

    private final ModelConfigDataService modelConfigDataService;
    private final KnowledgeRetrievalProperties props;
    private final NoOpRerankService noOp = new NoOpRerankService();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final OkHttpClient baseClient;

    public HttpRerankService(
            @Qualifier("bean.modelConfigDataService") ModelConfigDataService modelConfigDataService,
            @Qualifier("bean.knowledgeRetrievalProperties") KnowledgeRetrievalProperties props) {
        this.modelConfigDataService = modelConfigDataService;
        this.props = props;
        this.baseClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(15, TimeUnit.SECONDS)
                .build();
    }

    @Override
    public List<VectorStoreSearchResult> rerank(
            String query, List<VectorStoreSearchResult> candidates, int topN) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        if (!props.isRerankEnabled()) {
            log.debug("rerank-enabled=false, skip rerank");
            return noOp.rerank(query, candidates, topN);
        }
        ModelConfig config = resolveEnabledRerankOrNull();
        if (config == null) {
            log.debug("no enabled RERANK model, skip rerank");
            return noOp.rerank(query, candidates, topN);
        }
        try {
            List<VectorStoreSearchResult> slice = candidates.size() > MAX_CANDIDATES
                    ? new ArrayList<VectorStoreSearchResult>(candidates.subList(0, MAX_CANDIDATES))
                    : candidates;
            List<VectorStoreSearchResult> ordered = callRerankApi(config, query, slice);
            if (ordered.isEmpty()) {
                return noOp.rerank(query, candidates, topN);
            }
            double wR = props.getRerankWeight();
            double wO = 1.0d - wR;
            return ReciprocalRankFusion.fuse(
                    ordered, candidates, wR, wO, props.getRrfK(), topN);
        } catch (Exception e) {
            log.warn("rerank failed, fallback to original order: {}", e.getMessage());
            return noOp.rerank(query, candidates, topN);
        }
    }

    private ModelConfig resolveEnabledRerankOrNull() {
        List<ModelConfigDTO> enabled = modelConfigDataService.listEnabledConfigsByType(ModelType.RERANK);
        if (enabled == null || enabled.isEmpty()) {
            return null;
        }
        ModelConfigDTO dto = enabled.get(0);
        log.info("using RERANK model: id={}, modelName={}", dto.getId(), dto.getModelName());
        return ModelConfigConverter.toEntity(dto);
    }

    private List<VectorStoreSearchResult> callRerankApi(
            ModelConfig config, String query, List<VectorStoreSearchResult> slice) throws Exception {
        List<String> documents = new ArrayList<String>(slice.size());
        int maxChars = props.getRerankDocMaxChars();
        for (VectorStoreSearchResult hit : slice) {
            String text = "";
            if (hit.getDocument() != null && hit.getDocument().getContent() != null) {
                text = hit.getDocument().getContent();
            }
            if (maxChars > 0 && text.length() > maxChars) {
                text = text.substring(0, maxChars);
            }
            documents.add(text);
        }

        Map<String, Object> body = new LinkedHashMap<String, Object>();
        body.put("model", config.getModelName());
        body.put("query", query);
        body.put("documents", documents);

        String path = config.getApiPath();
        if (path == null || path.isEmpty()) {
            path = DEFAULT_PATH;
        }
        String url = config.getBaseUrl() + path;

        Request httpRequest = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + SensitiveConfigCipher.decrypt(config.getApiKey()))
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(JSON, objectMapper.writeValueAsString(body)))
                .build();

        OkHttpClient client = clientFor(config);
        try (Response response = client.newCall(httpRequest).execute()) {
            String respBody = response.body() != null ? response.body().string() : "";
            if (!response.isSuccessful()) {
                throw new IllegalStateException("HTTP " + response.code() + ": " + respBody);
            }
            return applyProviderResults(slice, respBody);
        }
    }

    @SuppressWarnings("unchecked")
    static List<VectorStoreSearchResult> applyProviderResults(
            List<VectorStoreSearchResult> slice, String responseJson) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> root = mapper.readValue(responseJson, new TypeReference<Map<String, Object>>() {});
        List<Map<String, Object>> results = extractResults(root);
        if (results == null || results.isEmpty()) {
            return Collections.emptyList();
        }

        List<ScoredIdx> scored = new ArrayList<ScoredIdx>();
        for (Map<String, Object> item : results) {
            Object idxObj = item.get("index");
            Object scoreObj = item.get("relevance_score");
            if (scoreObj == null) {
                scoreObj = item.get("relevanceScore");
            }
            if (idxObj == null || scoreObj == null) {
                continue;
            }
            int index = ((Number) idxObj).intValue();
            if (index < 0 || index >= slice.size()) {
                continue;
            }
            scored.add(new ScoredIdx(index, ((Number) scoreObj).doubleValue()));
        }
        if (scored.isEmpty()) {
            return Collections.emptyList();
        }
        Collections.sort(scored, new Comparator<ScoredIdx>() {
            @Override
            public int compare(ScoredIdx a, ScoredIdx b) {
                return Double.compare(b.score, a.score);
            }
        });

        Set<Integer> seen = new HashSet<Integer>();
        List<VectorStoreSearchResult> out = new ArrayList<VectorStoreSearchResult>();
        for (ScoredIdx s : scored) {
            if (!seen.add(s.index)) {
                continue;
            }
            VectorStoreSearchResult src = slice.get(s.index);
            VectorDocument doc = src.getDocument();
            out.add(new VectorStoreSearchResult(doc, s.score));
        }
        return out;
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> extractResults(Map<String, Object> root) {
        Object results = root.get("results");
        if (results instanceof List) {
            return (List<Map<String, Object>>) results;
        }
        Object output = root.get("output");
        if (output instanceof Map) {
            Object nested = ((Map<String, Object>) output).get("results");
            if (nested instanceof List) {
                return (List<Map<String, Object>>) nested;
            }
        }
        return null;
    }

    private OkHttpClient clientFor(ModelConfig config) {
        int timeoutMs = props.getRerankTimeoutMs();
        OkHttpClient.Builder builder = baseClient.newBuilder()
                .connectTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .readTimeout(timeoutMs, TimeUnit.MILLISECONDS)
                .writeTimeout(timeoutMs, TimeUnit.MILLISECONDS);

        if (config.getProxyEnabled() != null && config.getProxyEnabled()
                && config.getProxyHost() != null && !config.getProxyHost().isEmpty()
                && config.getProxyPort() != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP,
                    new InetSocketAddress(config.getProxyHost(), config.getProxyPort()));
            builder.proxy(proxy);
            if (config.getProxyUsername() != null && !config.getProxyUsername().isEmpty()) {
                final String user = config.getProxyUsername();
                final String pass = config.getProxyPassword() != null ? config.getProxyPassword() : "";
                builder.proxyAuthenticator((route, response) -> response.request().newBuilder()
                        .header("Proxy-Authorization", Credentials.basic(user, pass))
                        .build());
            }
        }
        return builder.build();
    }
}
