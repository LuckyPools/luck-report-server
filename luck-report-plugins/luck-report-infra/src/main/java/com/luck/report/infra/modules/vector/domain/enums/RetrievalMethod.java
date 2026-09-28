package com.luck.report.infra.modules.vector.domain.enums;

/** 检索模式：SEMANTIC / FULL_TEXT / HYBRID */
public enum RetrievalMethod {
    SEMANTIC,
    FULL_TEXT,
    HYBRID;

    public static RetrievalMethod fromConfig(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return SEMANTIC;
        }
        return RetrievalMethod.valueOf(raw.trim().toUpperCase().replace('-', '_'));
    }
}
