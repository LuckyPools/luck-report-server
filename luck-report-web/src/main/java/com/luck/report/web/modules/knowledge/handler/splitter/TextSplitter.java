package com.luck.report.web.modules.knowledge.handler.splitter;

import java.util.List;

/**
 * 文本分块器接口
 * 参照 Spring AI 的 TextSplitter，适配本项目无 Spring AI 依赖的简化实现
 *
 * @author luck
 */
public interface TextSplitter {

    /**
     * 将文本分块
     *
     * @param text 原始文本
     * @return 分块后的文本列表
     */
    List<String> split(String text);
}
