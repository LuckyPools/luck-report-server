package com.luck.report.web.modules.chat.controller;

import com.luck.report.web.modules.chat.domain.dto.ChatMessageBatchDTO;
import com.luck.report.web.modules.chat.domain.dto.ChatMessageDTO;
import com.luck.report.web.modules.chat.domain.entity.ChatMessage;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.web.modules.chat.service.ChatMessageService;
import com.luck.report.web.modules.chat.service.ChatSessionService;
import com.luck.report.web.utils.DownloadUtils;
import com.luck.report.web.utils.ResultVOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天消息控制器
 *
 * @author luck
 */
@Slf4j
@RestController("bean.chatMessageController")
@RequestMapping("${luck-report.servletPrefix:}/sessions")
@RequiredArgsConstructor
public class ChatMessageController {

    @Qualifier("bean.chatMessageService")
    private final ChatMessageService chatMessageService;
    @Qualifier("bean.chatSessionService")
    private final ChatSessionService chatSessionService;

    /**
     * 根据会话ID查询消息列表
     *
     * @param sessionId 会话ID
     * @return 消息列表，按创建时间升序
     */
    @GetMapping("/{sessionId}/messages/list")
    public ResultVO<List<ChatMessage>> getMessages(@PathVariable String sessionId) {
        List<ChatMessage> messages = chatMessageService.findBySessionId(sessionId);
        return ResultVOUtils.success("success.chat.messagesLoaded", messages);
    }

    /**
     * 保存单条消息
     *
     * @param sessionId 会话ID
     * @param dto       消息请求体
     * @return 保存后的消息实体
     */
    @PostMapping("/{sessionId}/messages/create")
    public ResultVO<ChatMessage> saveMessage(
            @PathVariable String sessionId,
            @RequestBody ChatMessageDTO dto) {
        if (dto == null || dto.getRole() == null) {
            return ResultVOUtils.error("error.chat.messageRoleEmpty");
        }

        ChatMessage message = new ChatMessage();
        message.setSessionId(sessionId);
        message.setRole(dto.getRole());
        message.setContent(dto.getContent());
        message.setMessageType(dto.getMessageType() != null ? dto.getMessageType() : "text");
        message.setMetadata(dto.getMetadata());

        ChatMessage saved = chatMessageService.saveMessage(message);

        chatSessionService.refreshSessionTime(sessionId);

        return ResultVOUtils.success("success.chat.messageSaved", saved);
    }

    /**
     * 批量保存消息
     *
     * @param sessionId 会话ID
     * @param batchDTO  批量消息请求体
     * @return 保存成功的消息数量
     */
    @PostMapping("/{sessionId}/messages/save_batch")
    public ResultVO<Integer> batchSaveMessages(
            @PathVariable String sessionId,
            @RequestBody ChatMessageBatchDTO batchDTO) {
        if (batchDTO == null || batchDTO.getMessages() == null || batchDTO.getMessages().isEmpty()) {
            return ResultVOUtils.error("error.chat.messageListEmpty");
        }

        List<ChatMessage> messages = new ArrayList<>();
        for (ChatMessageDTO dto : batchDTO.getMessages()) {
            ChatMessage message = new ChatMessage();
            message.setSessionId(sessionId);
            message.setRole(dto.getRole());
            message.setContent(dto.getContent());
            message.setMessageType(dto.getMessageType() != null ? dto.getMessageType() : "text");
            message.setMetadata(dto.getMetadata());
            messages.add(message);
        }

        int count = chatMessageService.batchSaveMessages(messages);

        chatSessionService.refreshSessionTime(sessionId);

        return ResultVOUtils.success("success.chat.messagesSaved", count);
    }

    /**
     * 删除单条消息
     *
     * @param id 消息ID
     * @return 操作结果
     */
    @DeleteMapping("/{sessionId}/messages/delete/{id}")
    public ResultVO<Void> deleteMessage(@PathVariable String sessionId,
                                        @PathVariable String id) {
        chatMessageService.deleteMessage(id);
        return ResultVOUtils.<Void>success("success.chat.messageDeleted", null);
    }
}
