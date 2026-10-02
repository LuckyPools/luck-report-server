package com.luck.report.web.modules.chat.service;

import com.luck.report.web.modules.chat.domain.entity.ChatMessage;

import java.util.List;

/**
 * 聊天消息服务接口
 *
 * @author luck
 */
public interface ChatMessageService {

    /**
     * 根据会话ID查询消息列表
     *
     * @param sessionId 会话ID，不可为空
     * @return 消息列表
     */
    List<ChatMessage> findBySessionId(String sessionId);

    /**
     * 保存单条消息
     *
     * @param message 消息实体，不可为空
     * @return 保存后的消息实体（含自增ID）
     */
    ChatMessage saveMessage(ChatMessage message);

    /**
     * 批量保存消息
     *
     * @param messages 消息列表，不可为空
     * @return 保存成功的消息数量
     */
    int batchSaveMessages(List<ChatMessage> messages);

    /**
     * 删除单条消息
     *
     * @param id 消息ID，不可为空
     */
    void deleteMessage(String id);
}
