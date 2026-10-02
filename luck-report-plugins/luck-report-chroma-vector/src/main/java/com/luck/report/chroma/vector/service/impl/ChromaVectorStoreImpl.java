package com.luck.report.chroma.vector.service.impl;

import com.luck.report.infra.modules.vector.domain.entity.VectorDocument;
import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.infra.modules.vector.domain.param.VectorFullTextSearchParam;
import com.luck.report.infra.modules.vector.domain.param.VectorSearchParam;
import com.luck.report.infra.modules.vector.service.VectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.amikos.chromadb.Client;
import tech.amikos.chromadb.handler.ApiException;
import tech.amikos.chromadb.handler.DefaultApi;
import tech.amikos.chromadb.model.AddEmbedding;
import tech.amikos.chromadb.model.CreateCollection;
import tech.amikos.chromadb.model.DeleteEmbedding;
import tech.amikos.chromadb.model.GetEmbedding;
import tech.amikos.chromadb.model.QueryEmbedding;

import java.util.*;

/**
 * 基于 Chroma 的向量存储实现
 *
 * @author luck
 */
public class ChromaVectorStoreImpl implements VectorStore {

    private static final Logger log = LoggerFactory.getLogger(ChromaVectorStoreImpl.class);

    private final Client chromaClient;

    private final DefaultApi api;

    private final String defaultCollectionName;

    /**
     * 构造函数
     *
     * @param chromaClient Chroma HTTP 客户端
     * @param api Chroma 底层 API
     * @param collectionName 默认 Collection 名称
     */
    public ChromaVectorStoreImpl(Client chromaClient, DefaultApi api, String collectionName) {
        this.chromaClient = chromaClient;
        this.api = api;
        this.defaultCollectionName = collectionName;
        log.info("[ChromaVectorStore] 初始化，默认 Collection: {}", defaultCollectionName);
    }

    @Override
    public void add(List<VectorDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            log.warn("文档列表为空，跳过添加");
            return;
        }

        for (VectorDocument doc : documents) {
            if (doc.getVector() == null || doc.getVector().length == 0) {
                throw new IllegalArgumentException(
                    String.format("VectorDocument 的 vector 必须非空（docId=%s）。" +
                                  "请先调用 EmbeddingService 生成向量，再调用 VectorStore.add()。",
                                  doc.getId())
                );
            }
        }

        try {
            String vectorType = extractVectorType(documents.get(0));
            String collectionName = vectorType != null
                ? getCollectionNameByVectorType(vectorType)
                : defaultCollectionName;

            String collectionId = getOrCreateCollectionId(collectionName);
            DefaultApi api = this.api;

            List<Object> embeddings = new ArrayList<>();
            List<Map<String, Object>> metadatas = new ArrayList<>();
            List<String> documentsList = new ArrayList<>();
            List<String> ids = new ArrayList<>();

            for (VectorDocument doc : documents) {
                List<Float> embeddingList = new ArrayList<>(doc.getVector().length);
                for (float v : doc.getVector()) {
                    embeddingList.add(v);
                }
                embeddings.add(embeddingList);

                Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
                metadatas.add(metadata);

                documentsList.add(doc.getContent() != null ? doc.getContent() : "");
                ids.add(doc.getId());
            }

            AddEmbedding addRequest = new AddEmbedding();
            addRequest.setIds(ids);
            addRequest.setEmbeddings(embeddings);
            addRequest.setMetadatas(metadatas);
            addRequest.setDocuments(documentsList);

            api.add(addRequest, collectionId);

            log.info("成功添加 {} 条文档到 Chroma 向量存储（Collection: {}, vectorType: {}）",
                     documents.size(), collectionName, vectorType);
        } catch (Exception e) {
            log.error("添加文档到 Chroma 失败: 文档数量={}, error={}", documents.size(), e.getMessage());
            throw new RuntimeException("添加文档到 Chroma 失败", e);
        }
    }

    @Override
    public void upsert(List<VectorDocument> documents) {
        if (documents == null || documents.isEmpty()) {
            log.warn("文档列表为空，跳过 upsert");
            return;
        }

        for (VectorDocument doc : documents) {
            if (doc.getVector() == null || doc.getVector().length == 0) {
                throw new IllegalArgumentException(
                    String.format("VectorDocument 的 vector 必须非空（docId=%s）。" +
                                  "请先调用 EmbeddingService 生成向量，再调用 VectorStore.upsert()。",
                                  doc.getId())
                );
            }
        }

        try {
            String vectorType = extractVectorType(documents.get(0));
            String collectionName = vectorType != null
                ? getCollectionNameByVectorType(vectorType)
                : defaultCollectionName;
            String collectionId = getOrCreateCollectionId(collectionName);

            List<Object> embeddings = new ArrayList<>();
            List<Map<String, Object>> metadatas = new ArrayList<>();
            List<String> documentsList = new ArrayList<>();
            List<String> ids = new ArrayList<>();

            for (VectorDocument doc : documents) {
                List<Float> embeddingList = new ArrayList<>(doc.getVector().length);
                for (float v : doc.getVector()) {
                    embeddingList.add(v);
                }
                embeddings.add(embeddingList);
                metadatas.add(new HashMap<>(doc.getMetadata()));
                documentsList.add(doc.getContent() != null ? doc.getContent() : "");
                ids.add(doc.getId());
            }

            AddEmbedding upsertRequest = new AddEmbedding();
            upsertRequest.setIds(ids);
            upsertRequest.setEmbeddings(embeddings);
            upsertRequest.setMetadatas(metadatas);
            upsertRequest.setDocuments(documentsList);

            api.upsert(upsertRequest, collectionId);

            log.info("成功 upsert {} 条文档到 Chroma（Collection: {}, vectorType: {}）",
                     documents.size(), collectionName, vectorType);
        } catch (Exception e) {
            log.error("Chroma upsert 失败: 文档数量={}, error={}", documents.size(), e.getMessage());
            throw new RuntimeException("Chroma upsert 失败", e);
        }
    }

    @Override
    public boolean delete(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            log.warn("ID 列表为空，跳过删除");
            return true;
        }

        try {
            String collectionId = getCollectionId(defaultCollectionName);
            DefaultApi api = this.api;

            DeleteEmbedding deleteRequest = new DeleteEmbedding();
            deleteRequest.setIds(ids);

            api.delete(deleteRequest, collectionId);

            log.info("从 Chroma 删除 {} 条文档（Collection: {}）", ids.size(), defaultCollectionName);
            return true;
        } catch (Exception e) {
            log.error("从 Chroma 删除文档失败: ID数量={}, error={}", ids.size(), e.getMessage());
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

        // idMetaKey 非空且 validIds 为空时返回空列表
        if (param.getIdMetaKey() != null && (param.getValidIds() == null || param.getValidIds().isEmpty())) {
            log.info("无生效知识，跳过检索: vectorType={}, idMetaKey={}", param.getVectorType(), param.getIdMetaKey());
            return new ArrayList<>();
        }

        try {
            String collectionName = (param.getVectorType() != null && !param.getVectorType().isEmpty())
                ? getCollectionNameByVectorType(param.getVectorType())
                : defaultCollectionName;
            String collectionId = getOrCreateCollectionId(collectionName);
            DefaultApi api = this.api;

            Map<String, Object> where = buildWhere(param.getMetadataEquals(), param.getIdMetaKey(), param.getValidIds());

            QueryEmbedding queryRequest = buildQueryRequest(param.getQueryVector(), param.getTopK(), where);

            Object response = api.getNearestNeighbors(queryRequest, collectionId);

            List<VectorStoreSearchResult> results = convertQueryResponse(response, param.getThreshold());

            log.info("Chroma 向量检索: vectorType={}, metadata={}, topK={}, threshold={}, 找到 {} 条结果",
                     param.getVectorType(), param.getMetadataEquals(), param.getTopK(), param.getThreshold(), results.size());
            return results;
        } catch (Exception e) {
            log.error("Chroma 向量检索失败: vectorType={}, error={}", param.getVectorType(), e.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public boolean supportsFullTextSearch() {
        return false;
    }

    @Override
    public List<VectorStoreSearchResult> searchByFullText(VectorFullTextSearchParam param) {
        // Chroma 无 BM25，混合检索时全文路返回空
        return Collections.emptyList();
    }

    @Override
    public boolean deleteByVectorType(String vectorType) {
        if (vectorType == null || vectorType.isEmpty()) {
            log.warn("向量类型为空，跳过删除");
            return false;
        }

        try {
            String collectionName = getCollectionNameByVectorType(vectorType);
            chromaClient.deleteCollection(collectionName);
            log.info("删除整个 Collection: {}", collectionName);
            return true;
        } catch (Exception e) {
            log.error("从 Chroma 删除向量类型失败: vectorType={}, error={}", vectorType, e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteByVectorTypeAndIds(String vectorType, List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        try {
            String collectionId = getOrCreateCollectionId(getCollectionNameByVectorType(vectorType));
            DeleteEmbedding deleteRequest = new DeleteEmbedding();
            deleteRequest.setIds(ids);
            api.delete(deleteRequest, collectionId);
            log.info("Chroma 按类型删 id: vectorType={}, count={}", vectorType, ids.size());
            return true;
        } catch (Exception e) {
            log.error("Chroma 按类型删 id 失败: vectorType={}, count={}, error={}",
                      vectorType, ids.size(), e.getMessage());
            return false;
        }
    }

    @Override
    public List<VectorDocument> listByVectorTypeAndMetadata(String vectorType, String metaKey, Object metaValue) {
        if (vectorType == null || vectorType.isEmpty()) {
            throw new IllegalArgumentException("vectorType 不能为空");
        }
        if (metaKey == null || metaKey.isEmpty()) {
            throw new IllegalArgumentException("metaKey 不能为空");
        }

        try {
            String collectionName = getCollectionNameByVectorType(vectorType);
            String collectionId = getOrCreateCollectionId(collectionName);

            Map<String, Object> where = new HashMap<>();
            where.put(metaKey, metaValue);

            GetEmbedding getRequest = new GetEmbedding();
            getRequest.setWhere(where);

            Object response = api.get(getRequest, collectionId);
            List<VectorDocument> results = convertGetResponse(response);

            log.info("Chroma list: Collection={}, where={}={}, count={}",
                     collectionName, metaKey, metaValue, results.size());
            return results;
        } catch (Exception e) {
            log.error("Chroma listByVectorTypeAndMetadata 失败: vectorType={}, metaKey={}, error={}",
                      vectorType, metaKey, e.getMessage());
            throw new RuntimeException("Chroma listByVectorTypeAndMetadata 失败", e);
        }
    }

    @Override
    public boolean deleteByVectorTypeAndMetadata(String vectorType, String metaKey, Object metaValue) {
        if (vectorType == null || vectorType.isEmpty()) {
            log.warn("向量类型为空，跳过删除");
            return false;
        }
        if (metaKey == null || metaKey.isEmpty()) {
            log.warn("metadata 字段名为空，跳过删除");
            return false;
        }

        try {
            String collectionName = getCollectionNameByVectorType(vectorType);
            String collectionId = getOrCreateCollectionId(collectionName);
            DefaultApi api = this.api;

            Map<String, Object> where = new HashMap<>();
            where.put(metaKey, metaValue);

            DeleteEmbedding deleteRequest = new DeleteEmbedding();
            deleteRequest.setWhere(where);

            api.delete(deleteRequest, collectionId);

            log.info("Chroma 组合删除: Collection={}, where={}={}", collectionName, metaKey, metaValue);
            return true;
        } catch (Exception e) {
            log.error("Chroma 组合删除失败: vectorType={}, metaKey={}, metaValue={}, error={}",
                      vectorType, metaKey, metaValue, e.getMessage());
            return false;
        }
    }

    /**
     * 从文档 metadata 中提取 vectorType
     *
     * @param doc 向量文档
     * @return vectorType 值，若不存在则返回 null
     */
    private String extractVectorType(VectorDocument doc) {
        if (doc == null || doc.getMetadata() == null) {
            return null;
        }
        Object vectorTypeObj = doc.getMetadata().get("vectorType");
        return vectorTypeObj != null ? vectorTypeObj.toString() : null;
    }

    /**
     * 根据 vectorType 获取 Collection 名称
     */
    private String getCollectionNameByVectorType(String vectorType) {
        return "luck_vector_" + vectorType.toLowerCase();
    }

    /**
     * 获取或创建 Collection，返回 UUID
     *
     * @param collectionName Collection 名称
     * @return Collection 的 UUID
     */
    @SuppressWarnings("unchecked")
    private String getOrCreateCollectionId(String collectionName) {
        try {
            CreateCollection createRequest = new CreateCollection();
            createRequest.setName(collectionName);
            createRequest.setGetOrCreate(true);

            Object result = api.createCollection(createRequest);

            log.debug("createCollection 返回值类型: {}, 内容: {}",
                     result != null ? result.getClass().getName() : "null", result);

            if (result instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) result;
                Object idObj = map.get("id");
                if (idObj == null) {
                    log.error("createCollection 返回的 Map 中缺少 'id' 字段, Map keys: {}", map.keySet());
                    throw new RuntimeException("Collection created but 'id' field missing in response: " + collectionName);
                }
                return (String) idObj;
            }

            log.error("createCollection 返回值类型异常: 期望 Map, 实际: {}",
                     result != null ? result.getClass().getName() : "null");
            throw new RuntimeException("Failed to get/create collection: " + collectionName +
                                      " (response type: " + (result != null ? result.getClass().getName() : "null") + ")");
        } catch (ApiException e) {
            log.error("createCollection API 调用失败: collectionName={}, code={}, message={}",
                     collectionName, e.getCode(), e.getMessage(), e);
            throw new RuntimeException("Failed to get/create collection: " + collectionName, e);
        }
    }

    /**
     * 通过 DefaultApi 获取已有 Collection 的 UUID
     *
     * @param collectionName Collection 名称
     * @return Collection 的 UUID
     */
    @SuppressWarnings("unchecked")
    private String getCollectionId(String collectionName) {
        try {
            Object result = api.getCollection(collectionName);
            if (result instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) result;
                return (String) map.get("id");
            }
            throw new RuntimeException("Failed to get collection: " + collectionName);
        } catch (ApiException e) {
            throw new RuntimeException("Failed to get collection: " + collectionName, e);
        }
    }

    /**
     * 构建业务 ID 的 IN 过滤条件
     *
     * @param idMetaKey metadata 中业务ID字段名，为 null 时返回 null
     * @param validIds 生效的业务ID列表
     * @return Chroma where 条件 Map，或 null 表示不过滤
     */
    private Map<String, Object> buildIdFilter(String idMetaKey, List<String> validIds) {
        if (idMetaKey == null || validIds == null || validIds.isEmpty()) {
            return null;
        }
        Map<String, Object> inClause = new HashMap<>();
        inClause.put("$in", validIds);
        Map<String, Object> where = new HashMap<>();
        where.put(idMetaKey, inClause);
        return where;
    }

    /**
     * 统一构建 Chroma where 条件
     *
     * @param metadataEquals metadata 等值过滤 Map，为 null 或空表示不过滤
     * @param idMetaKey 业务ID字段名，为 null 表示不按ID过滤
     * @param validIds 生效的业务ID列表
     * @return Chroma where 条件 Map，或 null 表示不过滤
     */
    private Map<String, Object> buildWhere(Map<String, Object> metadataEquals, String idMetaKey, List<String> validIds) {
        Map<String, Object> metaWhere = null;
        if (metadataEquals != null && !metadataEquals.isEmpty()) {
            metaWhere = new HashMap<>(metadataEquals);
        }

        Map<String, Object> idWhere = buildIdFilter(idMetaKey, validIds);

        if (metaWhere == null && idWhere == null) {
            return null;
        }
        if (metaWhere != null && idWhere == null) {
            return metaWhere;
        }
        if (metaWhere == null) {
            return idWhere;
        }

        List<Map<String, Object>> andConditions = new ArrayList<>();
        andConditions.add(metaWhere);
        andConditions.add(idWhere);

        Map<String, Object> combinedWhere = new HashMap<>();
        combinedWhere.put("$and", andConditions);
        return combinedWhere;
    }

    /**
     * 构造 QueryEmbedding 请求
     *
     * @param queryVector 查询向量
     * @param topK 返回条数
     * @param where 过滤条件（可为 null）
     * @return QueryEmbedding 请求对象
     */
    private QueryEmbedding buildQueryRequest(float[] queryVector, int topK, Map<String, Object> where) {
        List<Float> embeddingList = new ArrayList<>(queryVector.length);
        for (float v : queryVector) {
            embeddingList.add(v);
        }

        QueryEmbedding queryRequest = new QueryEmbedding();
        queryRequest.setQueryEmbeddings(Collections.singletonList(embeddingList));
        queryRequest.setNResults(topK);
        if (where != null) {
            queryRequest.setWhere(where);
        }

        // 指定返回结果包含的字段
        List<QueryEmbedding.IncludeEnum> include = Arrays.asList(
            QueryEmbedding.IncludeEnum.DOCUMENTS,
            QueryEmbedding.IncludeEnum.DISTANCES,
            QueryEmbedding.IncludeEnum.METADATAS
        );
        queryRequest.setInclude(include);

        return queryRequest;
    }

    /**
     * Chroma get 响应为平铺数组：ids / documents / metadatas（非 query 的嵌套结构）。
     */
    @SuppressWarnings("unchecked")
    private List<VectorDocument> convertGetResponse(Object response) {
        List<VectorDocument> results = new ArrayList<>();
        if (!(response instanceof Map)) {
            log.warn("Chroma get 响应格式异常，期望 Map，实际: {}",
                     response != null ? response.getClass() : "null");
            return results;
        }

        Map<String, Object> responseMap = (Map<String, Object>) response;
        List<String> ids = (List<String>) responseMap.get("ids");
        if (ids == null || ids.isEmpty()) {
            return results;
        }

        List<String> documents = (List<String>) responseMap.get("documents");
        List<Map<String, Object>> metadatas = (List<Map<String, Object>>) responseMap.get("metadatas");

        for (int i = 0; i < ids.size(); i++) {
            String content = documents != null && i < documents.size() ? documents.get(i) : "";
            Map<String, Object> metadata = metadatas != null && i < metadatas.size() && metadatas.get(i) != null
                ? metadatas.get(i)
                : new HashMap<>();
            results.add(new VectorDocument(ids.get(i), content != null ? content : "", metadata));
        }
        return results;
    }

    /**
     * 转换 Chroma 查询响应为检索结果
     *
     * @param response Chroma 查询响应
     * @param threshold 相似度阈值
     * @return 转换后的结果列表
     */
    @SuppressWarnings("unchecked")
    private List<VectorStoreSearchResult> convertQueryResponse(Object response, double threshold) {
        List<VectorStoreSearchResult> results = new ArrayList<>();

        if (!(response instanceof Map)) {
            log.warn("Chroma 响应格式异常，期望 Map，实际: {}", response != null ? response.getClass() : "null");
            return results;
        }

        Map<String, Object> responseMap = (Map<String, Object>) response;

        List<List<String>> idsList = (List<List<String>>) responseMap.get("ids");
        if (idsList == null || idsList.isEmpty()) {
            return results;
        }

        List<String> ids = idsList.get(0);
        List<List<Number>> distancesList = (List<List<Number>>) responseMap.get("distances");
        List<Number> distances = distancesList != null && !distancesList.isEmpty() ? distancesList.get(0) : null;

        List<List<String>> documentsList = (List<List<String>>) responseMap.get("documents");
        List<String> documents = documentsList != null && !documentsList.isEmpty() ? documentsList.get(0) : null;

        List<List<Map<String, Object>>> metadatasList = (List<List<Map<String, Object>>>) responseMap.get("metadatas");
        List<Map<String, Object>> metadatas = metadatasList != null && !metadatasList.isEmpty() ? metadatasList.get(0) : null;

        for (int i = 0; i < ids.size(); i++) {
            double distance = distances != null ? distances.get(i).doubleValue() : 0.0;
            double similarity = 1.0 / (1.0 + distance);  // L2 距离转相似度：距离越小相似度越高

            if (similarity < threshold) {
                continue;
            }

            String id = ids.get(i);
            String content = documents != null ? documents.get(i) : "";
            Map<String, Object> metadata = metadatas != null ? metadatas.get(i) : new HashMap<>();

            VectorDocument doc = new VectorDocument(id, content, metadata);

            VectorStoreSearchResult result = new VectorStoreSearchResult(doc, similarity);

            results.add(result);
        }

        return results;
    }
}
