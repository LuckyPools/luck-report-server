package com.luck.report.web.modules.knowledge.utils;

import com.luck.report.web.modules.knowledge.domain.enums.SplitterType;
import org.springframework.util.StringUtils;

/**
 * 按用户选择优先，否则按文件名后缀解析默认分块策略（编排层，非切分算法）。
 */
public final class SplitterTypeResolver {

    private SplitterTypeResolver() {
    }

    public static SplitterType resolve(String userSplitterType, String sourceFilename) {
        if (StringUtils.hasText(userSplitterType)) {
            return SplitterType.fromValue(userSplitterType.trim());
        }
        return resolveByFilename(sourceFilename);
    }

    public static SplitterType resolveByFilename(String sourceFilename) {
        if (!StringUtils.hasText(sourceFilename)) {
            return SplitterType.RECURSIVE;
        }
        String lower = sourceFilename.toLowerCase();
        int slash = Math.max(lower.lastIndexOf('/'), lower.lastIndexOf('\\'));
        String name = slash >= 0 ? lower.substring(slash + 1) : lower;
        if (name.endsWith(".md") || name.endsWith(".markdown")) {
            return SplitterType.STRUCTURE;
        }
        if (name.endsWith(".doc") || name.endsWith(".docx")) {
            return SplitterType.STRUCTURE;
        }
        if (name.endsWith(".xls") || name.endsWith(".xlsx") || name.endsWith(".csv")) {
            return SplitterType.TABLE;
        }
        return SplitterType.RECURSIVE;
    }
}
