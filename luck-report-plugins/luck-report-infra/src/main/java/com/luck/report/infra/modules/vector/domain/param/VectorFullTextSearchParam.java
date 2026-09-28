package com.luck.report.infra.modules.vector.domain.param;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 全文检索参数；过滤语义同 {@link VectorSearchParam}。
 * 全文通道不使用向量 threshold（混合时延后到融合后）。
 */
@Getter
@Builder(toBuilder = true)
public class VectorFullTextSearchParam {

    /** 查询原文，必填 */
    private final String queryText;

    private final int topK;

    private final String vectorType;

    private final Map<String, Object> metadataEquals;

    private final String idMetaKey;

    /** idMetaKey 非空且为空列表 → 直接返回空 */
    private final List<String> validIds;
}
