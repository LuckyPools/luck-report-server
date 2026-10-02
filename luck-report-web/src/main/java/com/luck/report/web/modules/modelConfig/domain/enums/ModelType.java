package com.luck.report.web.modules.modelConfig.domain.enums;

import lombok.Getter;

import com.luck.report.core.exception.ReportBizException;

/**
 * 模型类型枚举
 *
 * @author luck
 */
@Getter
public enum ModelType {

    /**
     * 对话模型（如 qwen3.6-plus）
     */
    CHAT("CHAT"),

    /**
     * 嵌入模型（如 text-embedding-v3）
     */
    EMBEDDING("EMBEDDING"),

    /**
     * 重排序模型（如 qwen3-rerank、bge-reranker）
     */
    RERANK("RERANK");

    private final String code;

    ModelType(String code) {
        this.code = code;
    }

    /**
     * 根据代码获取枚举
     *
     * @param code 模型类型代码
     * @return 对应的 ModelType 枚举
     * @throws IllegalArgumentException 未知代码时抛出
     */
    public static ModelType fromCode(String code) {
        for (ModelType type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new ReportBizException("error.enum.unknownModelType", code);
    }
}
