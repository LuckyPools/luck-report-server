package com.luck.report.web.modules.vector.service;

import com.luck.report.web.modules.vector.domain.vo.VectorSearchRequest;
import com.luck.report.web.modules.vector.domain.vo.VectorSearchResult;

import java.util.List;

public interface ReportVectorSearchService {

    /**
     * 向量检索：解析参数 → 生效 ID 过滤 → 召回（agent 走检索策略）→ 回填原文 → VO。
     */
    List<VectorSearchResult> search(VectorSearchRequest request);
}
