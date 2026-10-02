package com.luck.report.web.modules.chat.mapper;

import com.luck.report.web.modules.chat.domain.entity.ChatMessage;
import com.luck.report.jdbc.Param;

import java.util.List;

/**
 * 聊天消息 Mapper
 *
 * @author luck
 */
public interface ChatMessageMapper {

    /**
     * 根据会话ID查询消息列表
     *
     * @param sessionId 会话ID
     * @return 消息列表
     */
    List<ChatMessage> selectBySessionId(@Param("sessionId") String sessionId);

    /**
     * 根据ID查询消息
     *
     * @param id 消息ID
     * @return 消息实体
     */
    ChatMessage selectById(@Param("id") String id);

    /**
     * 查询会话的消息数量
     *
     * @param sessionId 会话ID
     * @return 消息数量
     */
    int countBySessionId(@Param("sessionId") String sessionId);

    /**
     * 插入单条消息
     *
     * @param message 消息实体
     * @return 影响行数
     */
    int insert(ChatMessage message);

    /**
     * 批量插入消息
     *
     * @param messages 消息列表
     * @return 影响行数
     */
    int insertBatch(@Param("list") List<ChatMessage> messages);

    /**
     * 根据ID删除单条消息
     *
     * @param id 消息ID
     * @return 影响行数
     */
    int deleteById(@Param("id") String id);

    /**
     * 根据会话ID删除所有消息
     *
     * @param sessionId 会话ID
     * @return 影响行数
     */
    int deleteBySessionId(@Param("sessionId") String sessionId);
}
