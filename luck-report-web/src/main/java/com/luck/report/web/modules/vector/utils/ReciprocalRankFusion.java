package com.luck.report.web.modules.vector.utils;

import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 加权 Reciprocal Rank Fusion。
 * score(d) = w_v / (k + rank_v) + w_f / (k + rank_f)；rank 从 1 开始。
 * 同 id 累加 RRF；文档对象优先取向量侧（通常含完整 content）。
 */
public final class ReciprocalRankFusion {

    private ReciprocalRankFusion() {
    }

    public static List<VectorStoreSearchResult> fuse(
            List<VectorStoreSearchResult> vectorHits,
            List<VectorStoreSearchResult> fullTextHits,
            double vectorWeight,
            double fullTextWeight,
            int rrfK,
            int topN) {

        if (vectorHits == null) {
            vectorHits = Collections.emptyList();
        }
        if (fullTextHits == null) {
            fullTextHits = Collections.emptyList();
        }
        if (vectorHits.isEmpty() && fullTextHits.isEmpty()) {
            return Collections.emptyList();
        }
        if (fullTextHits.isEmpty()) {
            return truncate(vectorHits, topN);
        }
        if (vectorHits.isEmpty()) {
            return truncate(fullTextHits, topN);
        }

        Map<String, RrfAccumulator> acc = new HashMap<String, RrfAccumulator>();
        accumulate(acc, vectorHits, vectorWeight, rrfK, true);
        accumulate(acc, fullTextHits, fullTextWeight, rrfK, false);

        List<VectorStoreSearchResult> out = new ArrayList<VectorStoreSearchResult>();
        for (RrfAccumulator a : acc.values()) {
            out.add(new VectorStoreSearchResult(a.document, a.rrfScore));
        }
        Collections.sort(out, new Comparator<VectorStoreSearchResult>() {
            @Override
            public int compare(VectorStoreSearchResult o1, VectorStoreSearchResult o2) {
                return Double.compare(o2.getScore(), o1.getScore());
            }
        });
        return truncate(out, topN);
    }

    private static void accumulate(
            Map<String, RrfAccumulator> acc,
            List<VectorStoreSearchResult> hits,
            double weight,
            int rrfK,
            boolean preferDoc) {
        for (int i = 0; i < hits.size(); i++) {
            VectorStoreSearchResult hit = hits.get(i);
            if (hit == null || hit.getDocument() == null || hit.getDocument().getId() == null) {
                continue;
            }
            String id = hit.getDocument().getId();
            double add = weight / (rrfK + i + 1);
            RrfAccumulator cur = acc.get(id);
            if (cur == null) {
                cur = new RrfAccumulator(hit.getDocument(), add);
                acc.put(id, cur);
            } else {
                cur.rrfScore += add;
                if (preferDoc) {
                    cur.document = hit.getDocument();
                }
            }
        }
    }

    private static List<VectorStoreSearchResult> truncate(List<VectorStoreSearchResult> list, int topN) {
        if (topN <= 0 || list.size() <= topN) {
            return list;
        }
        return new ArrayList<VectorStoreSearchResult>(list.subList(0, topN));
    }
}
