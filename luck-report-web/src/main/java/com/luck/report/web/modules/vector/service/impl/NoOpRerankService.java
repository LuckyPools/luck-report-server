package com.luck.report.web.modules.vector.service.impl;

import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;
import com.luck.report.web.modules.vector.service.RerankService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NoOpRerankService implements RerankService {
    @Override
    public List<VectorStoreSearchResult> rerank(String query, List<VectorStoreSearchResult> candidates, int topN) {
        if (candidates == null || candidates.isEmpty()) {
            return Collections.emptyList();
        }
        List<VectorStoreSearchResult> copy = new ArrayList<VectorStoreSearchResult>(candidates);
        Collections.sort(copy, new Comparator<VectorStoreSearchResult>() {
            @Override
            public int compare(VectorStoreSearchResult o1, VectorStoreSearchResult o2) {
                return Double.compare(o2.getScore(), o1.getScore());
            }
        });
        if (topN > 0 && copy.size() > topN) {
            return new ArrayList<VectorStoreSearchResult>(copy.subList(0, topN));
        }
        return copy;
    }
}
