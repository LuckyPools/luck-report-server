package com.luck.report.infra.modules.vector.service.impl;

import com.luck.report.infra.modules.vector.domain.entity.VectorDocument;
import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.infra.modules.vector.domain.param.VectorFullTextSearchParam;
import com.luck.report.infra.modules.vector.domain.param.VectorSearchParam;
import com.luck.report.infra.modules.vector.service.VectorStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * 未配置向量库时的兜底实现：检索返回空，写入抛异常。
 *
 * @author luck
 */
public class EmptyVectorStore implements VectorStore {

    private static final Logger log = LoggerFactory.getLogger(EmptyVectorStore.class);

    private static final String NOT_CONFIGURED_MSG = "向量数据库未配置，请在 application.yml 中配置 luck-report.vector 相关信息";

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public void add(List<VectorDocument> documents) {
        log.warn(NOT_CONFIGURED_MSG);
        throw new UnsupportedOperationException(NOT_CONFIGURED_MSG);
    }

    @Override
    public boolean delete(List<String> ids) {
        log.warn(NOT_CONFIGURED_MSG);
        return false;
    }

    @Override
    public boolean deleteByVectorType(String vectorType) {
        log.warn(NOT_CONFIGURED_MSG);
        return false;
    }

    @Override
    public boolean deleteByVectorTypeAndMetadata(String vectorType, String metaKey, Object metaValue) {
        log.warn(NOT_CONFIGURED_MSG);
        return false;
    }

    @Override
    public List<VectorDocument> listByVectorTypeAndMetadata(String vectorType, String metaKey, Object metaValue) {
        log.debug(NOT_CONFIGURED_MSG);
        return Collections.emptyList();
    }

    @Override
    public void upsert(List<VectorDocument> documents) {
        add(documents); // 仍抛 UnsupportedOperationException
    }

    @Override
    public List<VectorStoreSearchResult> search(VectorSearchParam param) {
        log.debug(NOT_CONFIGURED_MSG);
        return Collections.emptyList();
    }

    @Override
    public boolean supportsFullTextSearch() {
        return false;
    }

    @Override
    public List<VectorStoreSearchResult> searchByFullText(VectorFullTextSearchParam param) {
        return Collections.emptyList();
    }
}
