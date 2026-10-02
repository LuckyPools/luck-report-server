package com.luck.report.web.modules.chat.controller;

import com.luck.report.web.modules.chat.domain.vo.CompactRequest;
import com.luck.report.web.modules.chat.domain.vo.CompactResult;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.web.modules.chat.service.ChatService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 对话压缩控制器
 *
 * @author luck
 */
@RestController("bean.chatCompactController")
@RequestMapping("${luck-report.servletPrefix:}/chat")
@AllArgsConstructor
public class ChatCompactController {

    @Qualifier("bean.chatService")
    private final ChatService chatService;

    /**
     * 对话压缩接口（POST）
     *
     * @param request 压缩请求，包含 messages、existingSummary、reportSnapshot、compactPrompt、modelId 等
     * @return ResultVO<CompactResult> 压缩结果，包含 summary 和 keyOperations
     */
    @PostMapping(value = "/compact", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResultVO<CompactResult> compact(@RequestBody CompactRequest request) {
        return chatService.compact(request);
    }
}
