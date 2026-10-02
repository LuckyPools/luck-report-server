package com.luck.report.infra.modules.vector.service;

import com.luck.report.infra.modules.vector.domain.entity.VectorDocument;
import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.infra.modules.vector.domain.param.VectorFullTextSearchParam;
import com.luck.report.infra.modules.vector.domain.param.VectorSearchParam;

import java.util.List;

/**
 * 向量存储接口，只负责存/删/检
 *
 * @author luck
 */
public interface VectorStore {

    /**
     * 是否为可用的真实实现（Empty 兜底返回 false）
     */
    default boolean isAvailable() {
        return true;
    }

    /**
     * 写入向量；documents.vector 为空时抛 IllegalArgumentException
     */
    void add(List<VectorDocument> documents);

    /**
     * 按文档 ID 删除
     */
    boolean delete(List<String> ids);

    /**
     * 按向量类型删除整类数据
     */
    boolean deleteByVectorType(String vectorType);

    /**
     * 按向量类型 + metadata 等值删除
     */
    boolean deleteByVectorTypeAndMetadata(String vectorType, String metaKey, Object metaValue);

    /**
     * 按 vectorType + metadata 等值完整列举（非近邻检索）
     */
    List<VectorDocument> listByVectorTypeAndMetadata(String vectorType, String metaKey, Object metaValue);

    /**
     * 同 id 覆盖写入，默认委托 add
     */
    default void upsert(List<VectorDocument> documents) {
        add(documents);
    }

    /**
     * 在指定 vectorType 作用域内按 id 删除
     */
    default boolean deleteByVectorTypeAndIds(String vectorType, List<String> ids) {
        return delete(ids);
    }

    /**
     * 统一向量检索
     */
    List<VectorStoreSearchResult> search(VectorSearchParam param);

    /**
     * postgresql / milvus(≥2.5 BM25) 为 true；chroma / empty 为 false
     */
    default boolean supportsFullTextSearch() {
        return false;
    }

    /**
     * 全文检索；不支持时返回空列表（便于 hybrid 降级）
     */
    default List<VectorStoreSearchResult> searchByFullText(VectorFullTextSearchParam param) {
        return java.util.Collections.emptyList();
    }
}
