package com.luck.report.web.modules.chat.domain.vo;

import com.luck.report.web.modules.chat.domain.vo.AttachmentPayload;
import com.luck.report.web.modules.chat.domain.vo.ContextMessage;
import com.luck.report.web.modules.chat.domain.vo.ToolDefinition;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 聊天请求 DTO
 *
 * @author luck
 */
@Data
public class ChatRequest {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 大模型配置ID
     */
    private String modelId;

    /**
     * 用户输入的消息内容
     */
    private String message;

    /**
     * 是否启用联网搜索
     */
    private Boolean searchEnabled = false;

    /**
     * 历史消息上下文列表
     */
    private List<ContextMessage> contextMessages;

    /**
     * 图片附件列表
     */
    private List<AttachmentPayload> attachments;

    /**
     * 工具定义列表（Agent Function Calling）
     */
    private List<ToolDefinition> tools;

    /**
     * 工具调用策略（Agent Function Calling）
     */
    private Object toolChoice;

    /**
     * 是否启用深度思考
     */
    private Boolean deepThink = false;
}
