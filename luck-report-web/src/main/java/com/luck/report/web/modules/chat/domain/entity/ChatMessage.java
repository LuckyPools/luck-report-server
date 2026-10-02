package com.luck.report.web.modules.chat.domain.entity;

import com.luck.report.web.common.domain.entity.DataEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天消息实体
 *
 * @author luck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage extends DataEntity<ChatMessage> {

    /**
     * 所属会话ID
     */
    private String sessionId;

    /**
     * 角色：user-用户，assistant-助手，system-系统，tool_result-工具结果
     */
    private String role;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型：text-文本，tool_call-工具调用，tool_result-工具结果，error-错误
     */
    private String messageType;

    /**
     * 元数据（JSON格式），存储 tool_calls 数组或 tool_call_id 等扩展信息
     */
    private String metadata;
}
