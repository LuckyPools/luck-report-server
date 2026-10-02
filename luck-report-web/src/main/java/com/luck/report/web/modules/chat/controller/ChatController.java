package com.luck.report.web.modules.chat.controller;

import com.luck.report.web.modules.chat.domain.vo.ChatRequest;
import com.luck.report.web.modules.chat.service.ChatService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * 聊天控制器
 *
 * @author luck
 */
@RestController("bean.chatController")
@RequestMapping("${luck-report.servletPrefix:}/chat")
@AllArgsConstructor
public class ChatController {

    @Qualifier("bean.chatService")
    private final ChatService chatService;

    /**
     * 流式对话接口（POST）
     *
     * @param request 聊天请求 DTO，包含消息内容、历史上下文、附件、工具定义、模型ID等
     * @return SSE事件流，包含 message / tool_use / done / error 事件
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> chatStream(@RequestBody ChatRequest request) {
        SseEmitter emitter = chatService.chatStream(request);
        return ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL, "no-cache, no-transform")
                .header("X-Accel-Buffering", "no")
                .body(emitter);
    }
}
