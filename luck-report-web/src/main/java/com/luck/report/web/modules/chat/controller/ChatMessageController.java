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
 * 提供消息的查询、单条保存、批量保存等 REST 接口
 * URL 模式参照 data-agent-management：/sessions/{sessionId}/messages
 *
 * 消息存储流程（方案A：Loop 结束后批量存）：
 * 1. 用户发消息 → 前端 MemoryManager 追加到内存
 * 2. Agentic Loop 运行 → 全程前端内存管理
 * 3. Loop 结束 → 前端调用 POST /sessions/{sessionId}/messages/save_batch 批量保存
 * 4. 进入旧对话 → 前端调用 GET /sessions/{sessionId}/messages/list 加载历史
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
     * 前端进入旧对话时调用，从 DB 加载历史消息到前端 MemoryManager
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
     * 适用于非 Agent 场景或需要实时保存的场景
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

        // 更新会话活动时间，用于会话列表排序
        chatSessionService.updateSessionTime(sessionId);

        return ResultVOUtils.success("success.chat.messageSaved", saved);
    }

    /**
     * 批量保存消息
     * Agentic Loop 结束后，前端一次性同步本轮新增的所有消息
     * 包含完整的对话链路：user → assistant(tool_calls) → tool_result → assistant 最终回复
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

        // DTO 转 Entity
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

        // 更新会话活动时间
        chatSessionService.updateSessionTime(sessionId);

        return ResultVOUtils.success("success.chat.messagesSaved", count);
    }

    /**
     * 删除单条消息
     * 前端删除消息按钮调用，物理删除
     *
     * @param id 消息ID
     * @return 操作结果
     */
    @DeleteMapping("/{sessionId}/messages/delete/{id}")
    public ResultVO<Void> deleteMessage(@PathVariable String sessionId,
                                        @PathVariable String id) {
        // sessionId 仅用于路径一致性，删除按消息 id
        chatMessageService.deleteMessage(id);
        return ResultVOUtils.<Void>success("success.chat.messageDeleted", null);
    }
}
