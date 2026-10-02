package com.luck.report.web.modules.chat.service.impl;

import com.luck.report.web.utils.SnowflakeIdGenerator;
import com.luck.report.web.modules.chat.domain.entity.ChatMessage;
import com.luck.report.web.modules.chat.mapper.ChatMessageMapper;
import com.luck.report.web.modules.chat.service.ChatMessageService;
import com.luck.report.web.security.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 聊天消息服务实现
 *
 * @author luck
 */
@Slf4j
@Service("bean.chatMessageService")
@RequiredArgsConstructor
public class ChatMessageServiceImpl implements ChatMessageService {

    @Qualifier("bean.chatMessageMapper")
    private final ChatMessageMapper chatMessageMapper;

    @Override
    public List<ChatMessage> listBySessionId(String sessionId) {
        return chatMessageMapper.selectBySessionId(sessionId);
    }

    @Override
    public ChatMessage save(ChatMessage message) {
        if (message.getId() == null || message.getId().isEmpty()) {
            message.setId(SnowflakeIdGenerator.generateId());
        }
        message.setCreateTime(java.time.LocalDateTime.now());
        message.setCreateBy(SecurityUtils.getCurrentUserId());
        message.setUpdateBy(SecurityUtils.getCurrentUserId());
        message.setUpdateTime(java.time.LocalDateTime.now());
        message.setDelFlag(0);
        if (message.getMessageType() == null || message.getMessageType().isEmpty()) {
            message.setMessageType("text");
        }
        chatMessageMapper.insert(message);
        log.info("保存消息: id={}, sessionId={}, role={}", message.getId(), message.getSessionId(), message.getRole());
        return message;
    }

    @Override
    public int saveBatch(List<ChatMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return 0;
        }
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        for (ChatMessage message : messages) {
            if (message.getId() == null || message.getId().isEmpty()) {
                message.setId(SnowflakeIdGenerator.generateId());
            }
            message.setCreateTime(now);
            message.setCreateBy(SecurityUtils.getCurrentUserId());
            message.setUpdateBy(SecurityUtils.getCurrentUserId());
            message.setUpdateTime(now);
            message.setDelFlag(0);
            if (message.getMessageType() == null || message.getMessageType().isEmpty()) {
                message.setMessageType("text");
            }
        }
        int count = chatMessageMapper.insertBatch(messages);
        log.info("批量保存消息: count={}, sessionId={}", count, messages.get(0).getSessionId());
        return count;
    }

    @Override
    public void removeById(String id) {
        chatMessageMapper.deleteById(id);
        log.info("删除消息: id={}", id);
    }
}
