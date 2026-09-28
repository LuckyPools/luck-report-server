package com.luck.report.milvus.vector.service.impl;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.infra.modules.vector.domain.entity.VectorDocument;
import com.luck.report.infra.modules.vector.domain.param.VectorFullTextSearchParam;
import com.luck.report.infra.modules.vector.domain.param.VectorSearchParam;
import com.luck.report.infra.modules.vector.service.VectorStore;
import io.milvus.common.clientenum.FunctionType;
import io.milvus.v2.client.MilvusClientV2;
import io.milvus.v2.common.DataType;
import io.milvus.v2.common.IndexParam;
import io.milvus.v2.service.collection.request.AddFieldReq;
import io.milvus.v2.service.collection.request.CreateCollectionReq;
import io.milvus.v2.service.collection.request.DescribeCollectionReq;
import io.milvus.v2.service.collection.request.HasCollectionReq;
import io.milvus.v2.service.collection.request.LoadCollectionReq;
import io.milvus.v2.service.collection.response.DescribeCollectionResp;
import io.milvus.v2.service.vector.request.DeleteReq;
import io.milvus.v2.service.vector.request.QueryReq;
import io.milvus.v2.service.vector.request.SearchReq;
import io.milvus.v2.service.vector.request.UpsertReq;
import io.milvus.v2.service.vector.request.data.EmbeddedText;
import io.milvus.v2.service.vector.request.data.FloatVec;
import io.milvus.v2.service.vector.response.QueryResp;
import io.milvus.v2.service.vector.response.SearchResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 基于 Milvus V2 API 的向量存储实现
 *
 * 使用 Milvus 官方 Java SDK V2 接口（MilvusClientV2）操作 Milvus 向量数据库
 * 单 Collection 设计，通过 vectorType 字段区分知识类型，通过 metadata JSON 字段存储元数据
 * 稠密检索使用 COSINE；全文检索使用 Sparse-BM25（content → sparse Function）
 *
 * Collection Schema:
 * - id: VarChar(128)，主键，不自动生成
 * - vector: FloatVector，维度由配置决定（IVF_FLAT + COSINE）
 * - vectorType: VarChar(64)，知识类型
 * - content: VarChar(65535)，enableAnalyzer=true，分块正文
 * - metadata: JSON
 * - sparse: SparseFloatVector，由 BM25 Function 从 content 生成（客户端勿写入）
 *
 * 要求：Milvus Server ≥ 2.5，milvus-sdk-java ≥ 2.5（本仓库为 2.6.18）。
 * 旧 Collection 无 sparse 时无法原地升级：请删除后重启重建，否则全文路关闭、hybrid 降级为纯向量。
 *
 * 过滤表达式语法（Milvus boolean expression）：
 * - 等值：vectorType == "X"
 * - metadata 等值：metadata["key"] == "value"
 * - metadata IN：metadata["key"] in ["v1", "v2"]
 * - 组合用 and 连接
 *
 * @author luck
 */
public class MilvusVectorStoreImpl implements VectorStore {

    private static final Logger log = LoggerFactory.getLogger(MilvusVectorStoreImpl.class);

    /** metadata key 合法字符校验：仅允许字母、数字、下划线，防止表达式注入 */
    private static final Pattern META_KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");

    /** Gson 反序列化 Map<String, Object> 的 Type，供 fromJson 使用 */
    private static final Type MAP_TYPE = new TypeToken<Map<String, Object>>() {}.getType();

    private final MilvusClientV2 client;
    private final String collectionName;
    private final int dimension;
    private final Gson gson = new Gson();

    /** 当前 Collection 是否具备 Sparse-BM25 全文能力 */
    private volatile boolean fullTextEnabled;

    /** Collection 字段名常量 */
    private static final String FIELD_ID = "id";
    private static final String FIELD_VECTOR = "vector";
    private static final String FIELD_VECTOR_TYPE = "vectorType";
    private static final String FIELD_CONTENT = "content";
    private static final String FIELD_METADATA = "metadata";
    private static final String FIELD_SPARSE = "sparse";
    private static final String BM25_FUNCTION_NAME = "content_bm25";

    /** filter-only query 单页上限（Milvus 默认 max） */
    private static final long LIST_QUERY_LIMIT = 16384L;

    /**
     * 构造 Milvus 向量存储实现，并初始化 Collection
     *
     * @param client         Milvus V2 客户端，不可为空
     * @param collectionName Collection 名称，不可为空
     * @param dimension      向量维度，必须 > 0
     */
    public MilvusVectorStoreImpl(MilvusClientV2 client, String collectionName, int dimension) {
        this.client = client;
        this.collectionName = collectionName;
        this.dimension = dimension;
        initCollection();
    }

    /**
     * 初始化 Collection：不存在则按 BM25 schema 创建；已存在则加载并探测是否含 sparse。
     * 不自动 drop 旧库。
     */
    private void initCollection() {
        Boolean has = client.hasCollection(HasCollectionReq.builder().collectionName(collectionName).build());
        if (has != null && has) {
            client.loadCollection(LoadCollectionReq.builder().collectionName(collectionName).build());
            this.fullTextEnabled = detectSparseField();
            if (this.fullTextEnabled) {
                log.info("[MilvusVectorStore] Collection 已存在并加载（BM25 全文已启用）: {}", collectionName);
            } else {
                log.error("[MilvusVectorStore] Collection {} 缺少 sparse 字段，全文检索已关闭。"
                                + "请删除该 Collection 后重启以重建含 BM25 的 schema，否则 hybrid 将降级为纯向量。",
                        collectionName);
            }
            return;
        }

        createBm25Collection();
        client.loadCollection(LoadCollectionReq.builder().collectionName(collectionName).build());
        this.fullTextEnabled = true;
        log.info("[MilvusVectorStore] Collection 创建成功（含 Sparse-BM25）: {}, dimension: {}",
                collectionName, dimension);
    }

    /**
     * 新建带稠密向量 + Sparse-BM25 的 Collection。
     */
    private void createBm25Collection() {
        CreateCollectionReq.CollectionSchema schema = client.createSchema();
        schema.addField(AddFieldReq.builder()
                .fieldName(FIELD_ID).dataType(DataType.VarChar).maxLength(128)
                .isPrimaryKey(true).autoID(false).build());
        schema.addField(AddFieldReq.builder()
                .fieldName(FIELD_VECTOR).dataType(DataType.FloatVector)
                .dimension(dimension).build());
        schema.addField(AddFieldReq.builder()
                .fieldName(FIELD_VECTOR_TYPE).dataType(DataType.VarChar).maxLength(64).build());
        schema.addField(AddFieldReq.builder()
                .fieldName(FIELD_CONTENT).dataType(DataType.VarChar).maxLength(65535)
                .enableAnalyzer(true)
                .build());
        schema.addField(AddFieldReq.builder()
                .fieldName(FIELD_METADATA).dataType(DataType.JSON).build());
        schema.addField(AddFieldReq.builder()
                .fieldName(FIELD_SPARSE).dataType(DataType.SparseFloatVector).build());

        schema.addFunction(CreateCollectionReq.Function.builder()
                .functionType(FunctionType.BM25)
                .name(BM25_FUNCTION_NAME)
                .inputFieldNames(Collections.singletonList(FIELD_CONTENT))
                .outputFieldNames(Collections.singletonList(FIELD_SPARSE))
                .build());

        IndexParam denseIndex = IndexParam.builder()
                .fieldName(FIELD_VECTOR)
                .indexType(IndexParam.IndexType.IVF_FLAT)
                .metricType(IndexParam.MetricType.COSINE)
                .build();

        Map<String, Object> sparseParams = new HashMap<String, Object>();
        sparseParams.put("inverted_index_algo", "DAAT_MAXSCORE");
        sparseParams.put("bm25_k1", 1.2);
        sparseParams.put("bm25_b", 0.75);
        IndexParam sparseIndex = IndexParam.builder()
                .fieldName(FIELD_SPARSE)
                .indexType(IndexParam.IndexType.SPARSE_INVERTED_INDEX)
                .metricType(IndexParam.MetricType.BM25)
                .extraParams(sparseParams)
                .build();

        client.createCollection(CreateCollectionReq.builder()
                .collectionName(collectionName)
                .collectionSchema(schema)
                .indexParams(Arrays.asList(denseIndex, sparseIndex))
                .build());
    }

    /**
     * 探测已有 Collection 是否包含 sparse 字段。
     */
    private boolean detectSparseField() {
        try {
            DescribeCollectionResp desc = client.describeCollection(
                    DescribeCollectionReq.builder().collectionName(collectionName).build());
            if (desc == null) {
                return false;
            }
            List<String> names = desc.getFieldNames();
            if (names != null && names.contains(FIELD_SPARSE)) {
                return true;
            }
            List<String> vectors = desc.getVectorFieldNames();
            return vectors != null && vectors.contains(FIELD_SPARSE);
        } catch (Exception e) {
            log.warn("[MilvusVectorStore] describeCollection 失败，默认关闭全文: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 添加向量到存储（upsert 语义，相同 ID 覆盖）
     *
     * @param documents 向量文档列表，vector 必须已生成
     * @throws IllegalArgumentException 如果 documents 中的 vector 为空
     */
    @Override
    public void add(List<VectorDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            return;
        }

        List<JsonObject> rows = new ArrayList<>(documents.size());
        for (VectorDocument doc : documents) {
            if (doc.getVector() == null || doc.getVector().length == 0) {
                throw new IllegalArgumentException("VectorDocument.vector 不能为空，doc id: " + doc.getId());
            }
            rows.add(buildRow(doc));
        }

        client.upsert(UpsertReq.builder()
                .collectionName(collectionName)
                .data(rows)
                .build());

        log.info("[MilvusVectorStore] upsert 完成，数量: {}", rows.size());
    }

    /**
     * 构建单行 upsert 数据（JsonObject）
     * 仅写入 id/vector/vectorType/content/metadata；sparse 由服务端 BM25 Function 从 content 生成。
     *
     * @param doc 向量文档
     * @return Gson JsonObject
     */
    private JsonObject buildRow(VectorDocument doc) {
        JsonObject row = new JsonObject();
        row.addProperty(FIELD_ID, doc.getId());
        // vector 字段：float[] → List<Float> → JsonElement
        List<Float> vectorList = new ArrayList<>(doc.getVector().length);
        for (float v : doc.getVector()) {
            vectorList.add(v);
        }
        row.add(FIELD_VECTOR, gson.toJsonTree(vectorList));
        row.addProperty(FIELD_VECTOR_TYPE, doc.getMetadata() != null
                ? String.valueOf(doc.getMetadata().getOrDefault(FIELD_VECTOR_TYPE, "")) : "");
        row.addProperty(FIELD_CONTENT, doc.getContent() != null ? doc.getContent() : "");
        row.add(FIELD_METADATA, gson.toJsonTree(doc.getMetadata() != null ? doc.getMetadata() : new HashMap<>()));
        return row;
    }

    /**
     * 按文档ID删除
     *
     * @param ids 文档ID列表
     * @return 是否删除成功
     */
    @Override
    public boolean delete(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        try {
            client.delete(DeleteReq.builder()
                    .collectionName(collectionName)
                    .ids(new ArrayList<>(ids))
                    .build());
            log.info("[MilvusVectorStore] 按 ID 删除完成，数量: {}", ids.size());
            return true;
        } catch (Exception e) {
            log.error("[MilvusVectorStore] 按 ID 删除失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 按向量类型删除
     *
     * @param vectorType 知识类型
     * @return 是否删除成功
     */
    @Override
    public boolean deleteByVectorType(String vectorType) {
        if (vectorType == null || vectorType.isEmpty()) {
            return false;
        }
        String filter = FIELD_VECTOR_TYPE + " == " + escapeExprString(vectorType);
        return deleteByFilter(filter);
    }

    /**
     * 按向量类型 + metadata 组合删除
     *
     * @param vectorType 知识类型
     * @param metaKey    metadata 字段名
     * @param metaValue  字段值
     * @return 是否删除成功
     */
    @Override
    public boolean deleteByVectorTypeAndMetadata(String vectorType, String metaKey, Object metaValue) {
        if (vectorType == null || metaKey == null || metaValue == null) {
            return false;
        }
        validateMetaKey(metaKey);
        String filter = FIELD_VECTOR_TYPE + " == " + escapeExprString(vectorType)
                + " and " + buildMetadataEqualsExpr(metaKey, metaValue);
        return deleteByFilter(filter);
    }

    /**
     * 按 vectorType + metadata 等值完整列举（filter-only Query，非近邻 search）。
     */
    @Override
    public List<VectorDocument> listByVectorTypeAndMetadata(String vectorType, String metaKey, Object metaValue) {
        if (vectorType == null || metaKey == null) {
            return Collections.emptyList();
        }
        validateMetaKey(metaKey);
        String filter = FIELD_VECTOR_TYPE + " == " + escapeExprString(vectorType)
                + " and " + buildMetadataEqualsExpr(metaKey, metaValue);

        List<VectorDocument> docs = new ArrayList<>();
        long offset = 0L;
        try {
            while (true) {
                QueryResp resp = client.query(QueryReq.builder()
                        .collectionName(collectionName)
                        .filter(filter)
                        .outputFields(Arrays.asList(FIELD_ID, FIELD_CONTENT, FIELD_METADATA))
                        .offset(offset)
                        .limit(LIST_QUERY_LIMIT)
                        .build());
                List<QueryResp.QueryResult> page = resp == null ? null : resp.getQueryResults();
                if (page == null || page.isEmpty()) {
                    break;
                }
                for (QueryResp.QueryResult row : page) {
                    Map<String, Object> entity = row.getEntity();
                    String id = entity != null && entity.get(FIELD_ID) != null
                            ? String.valueOf(entity.get(FIELD_ID)) : null;
                    docs.add(new VectorDocument(id, extractContent(entity), extractMetadata(entity)));
                }
                if (page.size() < LIST_QUERY_LIMIT) {
                    break;
                }
                offset += LIST_QUERY_LIMIT;
            }
            log.info("[MilvusVectorStore] list 完成: vectorType={}, metaKey={}, 数量={}",
                    vectorType, metaKey, docs.size());
            return docs;
        } catch (Exception e) {
            throw new RuntimeException("Milvus listByVectorTypeAndMetadata 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 按过滤表达式删除的通用方法
     *
     * @param filter Milvus boolean 表达式
     * @return 是否删除成功
     */
    private boolean deleteByFilter(String filter) {
        try {
            client.delete(DeleteReq.builder()
                    .collectionName(collectionName)
                    .filter(filter)
                    .build());
            log.info("[MilvusVectorStore] 按表达式删除完成: {}", filter);
            return true;
        } catch (Exception e) {
            log.error("[MilvusVectorStore] 按表达式删除失败: {}, error: {}", filter, e.getMessage(), e);
            return false;
        }
    }

    /**
     * 统一向量检索入口
     *
     * @param param 检索参数，queryVector 必须已生成
     * @return 检索结果列表，按相似度降序排列
     */
    @Override
    public List<VectorStoreSearchResult> search(VectorSearchParam param) {
        if (param.getQueryVector() == null || param.getQueryVector().length == 0) {
            throw new IllegalArgumentException(
                "查询向量 queryVector 必须非空。" +
                "请先调用 EmbeddingService.embed(query) 生成向量，再调用 VectorStore.search()。"
            );
        }

        // idMetaKey 非空但 validIds 为空：无生效知识，直接返回空列表
        if (param.getIdMetaKey() != null && (param.getValidIds() == null || param.getValidIds().isEmpty())) {
            log.info("无生效知识，跳过检索: vectorType={}, idMetaKey={}", param.getVectorType(), param.getIdMetaKey());
            return new ArrayList<>();
        }

        String filter = buildSearchExpr(param);

        SearchReq.SearchReqBuilder reqBuilder = SearchReq.builder()
                .collectionName(collectionName)
                .data(Collections.singletonList(new FloatVec(param.getQueryVector())))
                .topK(param.getTopK())
                .annsField(FIELD_VECTOR)
                .metricType(IndexParam.MetricType.COSINE)
                .outputFields(Arrays.asList(FIELD_METADATA, FIELD_CONTENT));

        if (filter != null && !filter.isEmpty()) {
            reqBuilder.filter(filter);
        }

        SearchResp resp = client.search(reqBuilder.build());
        List<VectorStoreSearchResult> results = convertSearchResults(resp, param.getThreshold());

        log.info("Milvus 向量检索: vectorType={}, metadata={}, topK={}, threshold={}, 找到 {} 条结果",
                 param.getVectorType(), param.getMetadataEquals(), param.getTopK(), param.getThreshold(), results.size());
        return results;
    }

    @Override
    public boolean supportsFullTextSearch() {
        return fullTextEnabled;
    }

    @Override
    public List<VectorStoreSearchResult> searchByFullText(VectorFullTextSearchParam param) {
        if (!fullTextEnabled) {
            return Collections.emptyList();
        }
        if (param.getQueryText() == null || param.getQueryText().trim().isEmpty()) {
            return Collections.emptyList();
        }
        if (param.getIdMetaKey() != null
                && (param.getValidIds() == null || param.getValidIds().isEmpty())) {
            log.info("无生效知识，跳过全文检索: vectorType={}, idMetaKey={}",
                    param.getVectorType(), param.getIdMetaKey());
            return Collections.emptyList();
        }

        String filter = buildFilterExpr(
                param.getVectorType(),
                param.getMetadataEquals(),
                param.getIdMetaKey(),
                param.getValidIds());

        SearchReq.SearchReqBuilder reqBuilder = SearchReq.builder()
                .collectionName(collectionName)
                .data(Collections.singletonList(new EmbeddedText(param.getQueryText())))
                .topK(param.getTopK())
                .annsField(FIELD_SPARSE)
                .metricType(IndexParam.MetricType.BM25)
                .outputFields(Arrays.asList(FIELD_METADATA, FIELD_CONTENT));

        if (filter != null && !filter.isEmpty()) {
            reqBuilder.filter(filter);
        }

        try {
            SearchResp resp = client.search(reqBuilder.build());
            // BM25 分与余弦不可比，不做相似度阈值截断（threshold=0）
            List<VectorStoreSearchResult> results = convertSearchResults(resp, 0.0d);
            log.info("Milvus 全文检索: vectorType={}, metadata={}, topK={}, 找到 {} 条结果",
                    param.getVectorType(), param.getMetadataEquals(), param.getTopK(), results.size());
            return results;
        } catch (Exception e) {
            log.error("Milvus 全文检索失败: vectorType={}, error={}",
                    param.getVectorType(), e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    // ==================== 表达式构建辅助方法 ====================

    /**
     * 构建搜索过滤表达式，组合 vectorType、metadataEquals、idMetaKey IN 三种条件
     *
     * @param param 检索参数
     * @return Milvus boolean 表达式，无条件时返回 null
     */
    private String buildSearchExpr(VectorSearchParam param) {
        return buildFilterExpr(
                param.getVectorType(),
                param.getMetadataEquals(),
                param.getIdMetaKey(),
                param.getValidIds());
    }

    /**
     * 统一过滤表达式构建（稠密 / 全文共用）。
     */
    private String buildFilterExpr(
            String vectorType,
            Map<String, Object> metadataEquals,
            String idMetaKey,
            List<String> validIds) {
        List<String> conditions = new ArrayList<String>(3);

        if (vectorType != null && !vectorType.isEmpty()) {
            conditions.add(FIELD_VECTOR_TYPE + " == " + escapeExprString(vectorType));
        }

        if (metadataEquals != null && !metadataEquals.isEmpty()) {
            for (Map.Entry<String, Object> entry : metadataEquals.entrySet()) {
                validateMetaKey(entry.getKey());
                conditions.add(buildMetadataEqualsExpr(entry.getKey(), entry.getValue()));
            }
        }

        if (idMetaKey != null && !idMetaKey.isEmpty()
                && validIds != null && !validIds.isEmpty()) {
            validateMetaKey(idMetaKey);
            conditions.add(buildMetadataInExpr(idMetaKey, validIds));
        }

        return conditions.isEmpty() ? null : String.join(" and ", conditions);
    }

    /**
     * 构建 metadata 等值表达式：metadata["key"] == value
     *
     * @param key   metadata 字段名
     * @param value 字段值
     * @return Milvus 表达式片段
     */
    private String buildMetadataEqualsExpr(String key, Object value) {
        return "metadata[\"" + key + "\"] == " + formatExprValue(value);
    }

    /**
     * 构建 metadata IN 表达式：metadata["key"] in ["v1", "v2"]
     *
     * @param key   metadata 字段名
     * @param values 值列表
     * @return Milvus 表达式片段
     */
    private String buildMetadataInExpr(String key, List<String> values) {
        StringBuilder sb = new StringBuilder("metadata[\"").append(key).append("\"] in [");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(escapeExprString(values.get(i)));
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * 格式化表达式值：字符串加双引号转义，其他类型直接 toString
     *
     * @param value 原始值
     * @return Milvus 表达式中的值表示
     */
    private String formatExprValue(Object value) {
        if (value instanceof String) {
            return escapeExprString((String) value);
        }
        return String.valueOf(value);
    }

    /**
     * 转义字符串字面量：用双引号包裹，转义内部双引号和反斜杠
     *
     * @param value 原始字符串
     * @return 转义后的 Milvus 表达式字符串字面量
     */
    private String escapeExprString(String value) {
        String escaped = value.replace("\\", "\\\\").replace("\"", "\\\"");
        return "\"" + escaped + "\"";
    }

    /**
     * 校验 metadata key 合法性，防止表达式注入
     *
     * @param key metadata 字段名
     * @throws IllegalArgumentException 如果 key 包含非法字符
     */
    private void validateMetaKey(String key) {
        if (key == null || !META_KEY_PATTERN.matcher(key).matches()) {
            throw new IllegalArgumentException("metadata key 包含非法字符，仅允许字母、数字、下划线: " + key);
        }
    }

    // ==================== 结果转换辅助方法 ====================

    /**
     * 转换 Milvus V2 搜索结果为 VectorStoreSearchResult 列表
     * COSINE 度量下 score 即相似度，低于 threshold 的结果被过滤
     *
     * @param searchResp Milvus V2 搜索响应
     * @param threshold  相似度阈值
     * @return 转换后的结果列表
     */
    private List<VectorStoreSearchResult> convertSearchResults(SearchResp searchResp, double threshold) {
        List<VectorStoreSearchResult> results = new ArrayList<>();
        if (searchResp == null) {
            return results;
        }

        // V2 返回结构：外层 List 对应查询向量，内层 List 对应该向量的匹配结果
        List<List<SearchResp.SearchResult>> searchResults = searchResp.getSearchResults();
        if (searchResults == null || searchResults.isEmpty()) {
            return results;
        }

        for (SearchResp.SearchResult sr : searchResults.get(0)) {
            float score = sr.getScore();
            // COSINE metric：score 即余弦相似度，低于阈值跳过
            if (score < threshold) {
                continue;
            }

            String id = String.valueOf(sr.getId());
            Map<String, Object> metadata = extractMetadata(sr.getEntity());
            String content = extractContent(sr.getEntity());
            VectorDocument doc = new VectorDocument(id, content, metadata);
            results.add(new VectorStoreSearchResult(doc, score));
        }

        return results;
    }

    /**
     * 从搜索结果的 entity Map 中提取 content
     *
     * @param entity 搜索结果 entity Map
     * @return content 字符串，无值时返回 null
     */
    private String extractContent(Map<String, Object> entity) {
        if (entity == null || !entity.containsKey(FIELD_CONTENT)) {
            return null;
        }
        Object contentObj = entity.get(FIELD_CONTENT);
        if (contentObj == null) {
            return null;
        }
        String content = contentObj.toString();
        return content.isEmpty() ? null : content;
    }

    /**
     * 从搜索结果的 entity Map 中提取 metadata
     * V2 的 getEntity() 返回 Map<String, Object>，metadata 字段值类型可能为 JsonObject/String/Map
     *
     * @param entity 搜索结果 entity Map
     * @return metadata Map，无法解析时返回空 Map
     */
    private Map<String, Object> extractMetadata(Map<String, Object> entity) {
        if (entity == null || !entity.containsKey(FIELD_METADATA)) {
            return new HashMap<>();
        }
        Object metaObj = entity.get(FIELD_METADATA);
        if (metaObj == null) {
            return new HashMap<>();
        }
        // JsonObject（Gson）：直接反序列化
        if (metaObj instanceof JsonObject) {
            return gson.fromJson((JsonObject) metaObj, MAP_TYPE);
        }
        // String：尝试 JSON 解析
        if (metaObj instanceof String) {
            return parseMetadataJson((String) metaObj);
        }
        // Map 或其他类型：尝试 Gson 转换
        JsonElement element = gson.toJsonTree(metaObj);
        if (element.isJsonObject()) {
            return gson.fromJson(element, MAP_TYPE);
        }
        return new HashMap<>();
    }

    /**
     * 解析 metadata JSON 字符串为 Map
     *
     * @param json JSON 字符串
     * @return metadata Map，解析失败返回空 Map
     */
    private Map<String, Object> parseMetadataJson(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return gson.fromJson(json, MAP_TYPE);
        } catch (Exception e) {
            log.warn("解析 metadata JSON 失败: {}, error: {}", json, e.getMessage());
            return new HashMap<>();
        }
    }
}
