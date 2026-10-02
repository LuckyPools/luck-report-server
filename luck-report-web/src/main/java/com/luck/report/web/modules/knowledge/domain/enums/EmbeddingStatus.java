package com.luck.report.web.modules.knowledge.domain.enums;

import lombok.Getter;

import com.luck.report.core.exception.ReportBizException;

/**
 * 向量化状态枚举类
 *
 * @author luck
 */
@Getter
public enum EmbeddingStatus {

    /**
     * 待处理
     */
    PENDING("PENDING"),

    /**
     * 处理中
     */
    PROCESSING("PROCESSING"),

    /**
     * 已完成
     */
    COMPLETED("COMPLETED"),

    /**
     * 失败
     */
    FAILED("FAILED");

    private final String value;

    EmbeddingStatus(String value) {
        this.value = value;
    }

    /**
     * 根据字符串值获取对应的枚举实例
     *
     * @param value 字符串值
     * @return 对应的枚举实例
     * @throws IllegalArgumentException 如果值不匹配任何枚举
     */
    public static EmbeddingStatus fromValue(String value) {
        for (EmbeddingStatus status : EmbeddingStatus.values()) {
            if (status.value.equals(value)) {
                return status;
            }
        }
        throw new ReportBizException("error.enum.unknownEmbeddingStatus", value);
    }
}
