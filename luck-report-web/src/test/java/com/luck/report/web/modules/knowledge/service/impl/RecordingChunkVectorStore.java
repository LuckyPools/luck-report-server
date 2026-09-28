package com.luck.report.web.modules.knowledge.service.impl;

import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.infra.modules.vector.domain.entity.VectorDocument;
import com.luck.report.infra.modules.vector.domain.param.VectorFullTextSearchParam;
import com.luck.report.infra.modules.vector.domain.param.VectorSearchParam;
import com.luck.report.infra.modules.vector.service.VectorStore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** 片段单测用：可配置 list 结果，并记录 upsert / delete 调用 */
final class RecordingChunkVectorStore implements VectorStore {

    private List<VectorDocument> listResult = Collections.emptyList();
    private List<VectorDocument> lastUpsert;
    private String lastDeleteVectorType;
    private List<String> lastDeleteIds;
    private int upsertCount;

    void setListResult(List<VectorDocument> listResult) {
        this.listResult = listResult != null ? listResult : Collections.<VectorDocument>emptyList();
    }

    List<VectorDocument> getLastUpsert() {
        return lastUpsert;
    }

    String getLastDeleteVectorType() {
        return lastDeleteVectorType;
    }

    List<String> getLastDeleteIds() {
        return lastDeleteIds;
    }

    int getUpsertCount() {
        return upsertCount;
    }

    @Override
    public void add(List<VectorDocument> documents) {
    }

    @Override
    public void upsert(List<VectorDocument> documents) {
        upsertCount++;
        lastUpsert = documents == null ? null : new ArrayList<VectorDocument>(documents);
    }

    @Override
    public boolean delete(List<String> ids) {
        return false;
    }

    @Override
    public boolean deleteByVectorType(String vectorType) {
        return false;
    }

    @Override
    public boolean deleteByVectorTypeAndMetadata(String vectorType, String metaKey, Object metaValue) {
        return false;
    }

    @Override
    public boolean deleteByVectorTypeAndIds(String vectorType, List<String> ids) {
        lastDeleteVectorType = vectorType;
        lastDeleteIds = ids == null ? null : new ArrayList<String>(ids);
        return true;
    }

    @Override
    public List<VectorDocument> listByVectorTypeAndMetadata(String vectorType, String metaKey, Object metaValue) {
        return listResult;
    }

    @Override
    public List<VectorStoreSearchResult> search(VectorSearchParam param) {
        return Collections.emptyList();
    }

    @Override
    public List<VectorStoreSearchResult> searchByFullText(VectorFullTextSearchParam param) {
        return Collections.emptyList();
    }
}
