package com.luck.report.web.modules.chat.domain.vo;

import com.luck.report.web.modules.chat.domain.vo.ContextMessage;
import lombok.Data;

import java.util.List;

/**
 * 对话压缩请求 DTO
 *
 * @author luck
 */
@Data
public class CompactRequest {

    /**
     * 大模型配置ID
     */
    private String modelId;

    /**
     * 需要压缩的历史消息列表
     */
    private List<ContextMessage> messages;

    /**
     * 已有的摘要内容（增量压缩时传入，LLM 基于旧摘要 + 新消息生成新摘要）
     */
    private String existingSummary;

    /**
     * 已有的关键操作记录（增量压缩时传入）
     */
    private List<String> existingKeyOperations;

    /**
     * 报表状态快照（压缩时注入，帮助 LLM 理解当前报表上下文）
     */
    private String reportSnapshot;

    /**
     * 压缩对话的系统提示词
     */
    private String compactPrompt;
}
