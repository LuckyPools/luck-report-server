package com.luck.report.web.modules.chat.domain.vo;

import com.luck.report.web.modules.chat.domain.vo.ToolCallMessage;
import lombok.Data;

import java.util.List;

/**
 * 上下文消息
 *
 * @author luck
 */
@Data
public class ContextMessage {

    /**
     * 消息角色：user / assistant / system / tool_result
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 关联的工具调用ID（仅 tool_result 角色需要）
     */
    private String toolCallId;

    /**
     * 关联的工具名称（仅 tool_result 角色需要）
     */
    private String toolName;

    /**
     * assistant 消息携带的工具调用列表
     */
    private List<ToolCallMessage> toolCalls;

    /**
     * assistant 消息携带的思考内容（思考型模型的 reasoning_content）
     */
    private String reasoningContent;
}
