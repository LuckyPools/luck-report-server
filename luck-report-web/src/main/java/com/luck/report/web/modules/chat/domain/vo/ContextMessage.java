package com.luck.report.web.modules.chat.domain.vo;

import com.luck.report.web.modules.chat.domain.vo.ToolCallMessage;
import lombok.Data;

import java.util.List;

/**
 * 上下文消息
 * 用于构建多轮对话历史，扩展支持 tool_result 角色和 assistant 的 tool_calls
 *
 * @author luck
 */
@Data
public class ContextMessage {

    /** 消息角色：user / assistant / system / tool_result */
    private String role;

    /** 消息内容 */
    private String content;

    /** 关联的工具调用ID（仅 tool_result 角色需要） */
    private String toolCallId;

    /** 关联的工具名称（仅 tool_result 角色需要） */
    private String toolName;

    /**
     * assistant 消息携带的工具调用列表
     * OpenAI Function Calling 协议要求：当 assistant 调用了工具，
     * 回传消息历史时必须包含完整的 tool_calls 信息
     */
    private List<ToolCallMessage> toolCalls;

    /**
     * assistant 消息携带的思考内容（思考型模型的 reasoning_content）
     * 思考模式下工具调用续接（回放带 tool_calls 的 assistant 消息）时，
     * 上游网关要求同时传回该轮推理内容，否则返回 400：
     * "The `reasoning_content` in the thinking mode must be passed back to the API."
     * 仅在字段非空时输出，普通消息不受影响
     */
    private String reasoningContent;
}
