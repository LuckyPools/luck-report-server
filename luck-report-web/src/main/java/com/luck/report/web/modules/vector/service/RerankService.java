package com.luck.report.web.modules.vector.service;

import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;

import java.util.List;

public interface RerankService {
    /**
     * 对候选重排后返回至多 topN 条；实现可替换 score。
     * NoOp：按原 score 降序截断。
     */
    List<VectorStoreSearchResult> rerank(String query, List<VectorStoreSearchResult> candidates, int topN);
}
