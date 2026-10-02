package com.luck.report.infra.modules.vector.domain.param;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 全文检索参数，过滤语义同 {@link VectorSearchParam}
 */
@Getter
@Builder(toBuilder = true)
public class VectorFullTextSearchParam {

    /**
     * 查询原文，必填
     */
    private final String queryText;

    /**
     * 返回条数
     */
    private final int topK;

    /**
     * 知识类型；null 表示全库
     */
    private final String vectorType;

    /**
     * metadata 等值过滤
     */
    private final Map<String, Object> metadataEquals;

    /**
     * metadata 业务 ID 字段名
     */
    private final String idMetaKey;

    /**
     * 生效业务 ID 列表；idMetaKey 非空且为空列表时直接返回空
     */
    private final List<String> validIds;
}
