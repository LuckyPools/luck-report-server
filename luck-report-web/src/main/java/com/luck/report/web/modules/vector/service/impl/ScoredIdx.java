package com.luck.report.web.modules.vector.service.impl;

/** Rerank 候选：原列表下标 + 相关性分。 */
final class ScoredIdx {

    final int index;
    final double score;

    ScoredIdx(int index, double score) {
        this.index = index;
        this.score = score;
    }
}
