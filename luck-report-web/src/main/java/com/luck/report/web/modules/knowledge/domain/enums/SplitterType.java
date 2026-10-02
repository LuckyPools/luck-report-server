package com.luck.report.web.modules.knowledge.domain.enums;

import com.luck.report.core.exception.ReportBizException;
import lombok.Getter;
import org.springframework.util.StringUtils;

/**
 * 知识库文档分块策略枚举
 *
 * @author luck
 */
@Getter
public enum SplitterType {

    /**
     * 固定字符窗口切分
     */
    TOKEN("token"),

    /**
     * 递归分隔符切分（默认）
     */
    RECURSIVE("recursive"),

    /**
     * FastGPT 结构感知切分（MD 标题/代码/表）
     */
    STRUCTURE("structure"),

    /**
     * FastGPT Markdown 表行窗切分
     */
    TABLE("table"),

    /**
     * 按句子边界切分
     */
    SENTENCE("sentence"),

    /**
     * 按段落边界切分
     */
    PARAGRAPH("paragraph"),

    /**
     * 基于语义相似度切分
     */
    SEMANTIC("semantic");

    private final String value;

    SplitterType(String value) {
        this.value = value;
    }

    /**
     * blank → RECURSIVE；非法值抛业务异常
     *
     * @param value 策略字符串
     * @return 枚举实例
     */
    public static SplitterType fromValue(String value) {
        if (!StringUtils.hasText(value)) {
            return RECURSIVE;
        }
        String normalized = value.trim().toLowerCase();
        for (SplitterType type : values()) {
            if (type.value.equals(normalized)) {
                return type;
            }
        }
        throw new ReportBizException("error.enum.unknownSplitterType", value);
    }
}
