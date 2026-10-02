package com.luck.report.web.modules.knowledge.utils;

import com.luck.report.infra.modules.vector.domain.dto.VectorStoreSearchResult;

import java.util.List;

/**
 * 同一知识下已选中的 chunk 组（按最高分代表该知识）。
 */
final class KnowledgeChunkGroup {

    private final double bestScore;
    private final List<VectorStoreSearchResult> chunks;

    KnowledgeChunkGroup(double bestScore, List<VectorStoreSearchResult> chunks) {
        this.bestScore = bestScore;
        this.chunks = chunks;
    }

    double getBestScore() {
        return bestScore;
    }

    List<VectorStoreSearchResult> getChunks() {
        return chunks;
    }
}
