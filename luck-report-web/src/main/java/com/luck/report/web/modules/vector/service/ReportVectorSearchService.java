package com.luck.report.web.modules.vector.service;

import com.luck.report.web.modules.vector.domain.vo.VectorSearchRequest;
import com.luck.report.web.modules.vector.domain.vo.VectorSearchResult;

import java.util.List;

public interface ReportVectorSearchService {

    /**
     * 向量检索
     */
    List<VectorSearchResult> search(VectorSearchRequest request);
}
