package com.luck.report.web.modules.knowledge.service.impl;

import com.luck.report.web.modules.chat.service.impl.EmbeddingService;

/** 片段单测用：固定返回向量，不发起真实 HTTP */
final class FixedEmbeddingService extends EmbeddingService {

    private final float[] vector;

    FixedEmbeddingService(float[] vector) {
        super(null);
        this.vector = vector;
    }

    @Override
    public float[] embed(String text, String modelId) {
        return vector;
    }
}
