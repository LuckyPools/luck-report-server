package com.luck.report.web.modules.chat.domain.dto;

import lombok.Data;

import java.util.List;

/**
 * 聊天消息批量保存请求 DTO
 *
 * @author luck
 */
@Data
public class ChatMessageBatchDTO {

    /**
     * 待保存的消息列表
     */
    private List<ChatMessageDTO> messages;
}
