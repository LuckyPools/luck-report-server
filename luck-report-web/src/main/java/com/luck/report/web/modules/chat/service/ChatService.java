package com.luck.report.web.modules.chat.service;

import com.luck.report.web.modules.chat.domain.vo.ChatRequest;
import com.luck.report.web.modules.chat.domain.vo.CompactRequest;
import com.luck.report.web.modules.chat.domain.vo.CompactResult;
import com.luck.report.web.common.domain.vo.ResultVO;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 聊天对话服务接口
 *
 * @author luck
 */
public interface ChatService {

    /**
     * 流式对话
     *
     * @param request 聊天请求 DTO，包含消息内容、历史上下文、附件、工具定义、模型ID等
     * @return SSE事件流，包含 message / tool_use / done / error 事件
     */
    SseEmitter chatStream(ChatRequest request);

    /**
     * 对话压缩
     *
     * @param request 压缩请求，包含 messages、existingSummary、reportSnapshot、compactPrompt、modelId 等
     * @return ResultVO<CompactResult> 压缩结果，包含 summary 和 keyOperations
     */
    ResultVO<CompactResult> compact(CompactRequest request);
}
