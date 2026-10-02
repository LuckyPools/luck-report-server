package com.luck.report.web.modules.chat.controller;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.modules.chat.domain.entity.ChatSession;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.web.modules.chat.service.ChatSessionService;
import com.luck.report.web.exception.TokenException;
import com.luck.report.web.security.service.TokenService;
import com.luck.report.web.security.utils.SecurityUtils;
import com.luck.report.web.utils.DownloadUtils;
import com.luck.report.web.utils.ResultVOUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天会话控制器
 *
 * @author luck
 */
@Slf4j
@RestController("bean.chatSessionController")
@RequestMapping("${luck-report.servletPrefix:}/sessions")
@RequiredArgsConstructor
public class ChatSessionController {

    @Qualifier("bean.chatSessionService")
    private final ChatSessionService chatSessionService;

    /**
     * 查询所有未删除的会话列表
     *
     * @return 会话列表
     */
    @GetMapping("/list")
    public ResultVO<List<ChatSession>> listSessions() {
        List<ChatSession> sessions = chatSessionService.listAll();
        return ResultVOUtils.success("success.chat.sessionsLoaded", sessions);
    }

    /**
     * 查询当前用户的会话列表
     *
     * @return 当前用户的会话列表
     */
    @GetMapping("/me/list")
    public ResultVO<List<ChatSession>> getSessionsOfMe() {
        String userId = resolveCurrentUserId();
        List<ChatSession> sessions = chatSessionService.listByUserId(userId);
        return ResultVOUtils.success("success.chat.sessionsLoaded", sessions);
    }

    /**
     * 分页查询当前用户的会话列表
     *
     * @param pageNum  页码，从1开始，默认1
     * @param pageSize 每页数量，默认10
     * @return 分页结果
     */
    @GetMapping("/me/page")
    public ResultVO<PageResultVO<ChatSession>> getSessionsOfMeWithPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        pageSize = Math.min(pageSize, 50);
        String userId = resolveCurrentUserId();
        PageResultVO<ChatSession> result = chatSessionService.listPage(userId, pageNum, pageSize);
        return ResultVOUtils.success("success.chat.sessionsLoaded", result);
    }

    /**
     * 根据会话ID查询会话详情
     *
     * @param sessionId 会话ID
     * @return 会话详情
     */
    @GetMapping("/detail/{sessionId}")
    public ResultVO<ChatSession> getSession(@PathVariable String sessionId) {
        ChatSession session = chatSessionService.getById(sessionId);
        if (session == null) {
            return ResultVOUtils.error("error.chat.sessionNotFound");
        }
        return ResultVOUtils.success("success.chat.sessionsLoaded", session);
    }

    /**
     * 创建新会话
     *
     * @param title 可选，会话标题
     * @return 新建的会话实体
     */
    @PostMapping("/create")
    public ResultVO<ChatSession> create(@RequestParam(value = "title", required = false) String title) {
        String userId = resolveCurrentUserId();
        ChatSession session = chatSessionService.create(title, userId);
        return ResultVOUtils.success("success.chat.sessionCreated", session);
    }

    /**
     * 重命名会话
     *
     * @param sessionId 会话ID
     * @param title     新标题
     * @return 操作结果
     */
    @PostMapping("/{sessionId}/rename")
    public ResultVO<Void> renameSession(
            @PathVariable String sessionId,
            @RequestParam("title") String title) {
        if (!StringUtils.hasText(title)) {
            return ResultVOUtils.error("error.chat.titleEmpty");
        }
        chatSessionService.renameSession(sessionId, title.trim());
        return ResultVOUtils.<Void>success("success.chat.renamed", null);
    }

    /**
     * 置顶或取消置顶会话
     *
     * @param sessionId 会话ID
     * @param pinned    是否置顶
     * @return 操作结果
     */
    @PostMapping("/{sessionId}/pin")
    public ResultVO<Void> pinSession(
            @PathVariable String sessionId,
            @RequestParam("pinned") Boolean pinned) {
        if (pinned == null) {
            return ResultVOUtils.error("error.chat.pinnedEmpty");
        }
        chatSessionService.pinSession(sessionId, pinned);
        return ResultVOUtils.<Void>success(
                Boolean.TRUE.equals(pinned) ? "success.chat.pinned" : "success.chat.unpinned", null);
    }

    /**
     * 删除单个会话（软删除）
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{sessionId}")
    public ResultVO<Void> removeById(@PathVariable String sessionId) {
        chatSessionService.removeById(sessionId);
        return ResultVOUtils.<Void>success("success.chat.sessionDeleted", null);
    }

    /**
     * 删除当前用户下的所有会话（软删除）
     *
     * @return 操作结果
     */
    @DeleteMapping("/me/delete")
    public ResultVO<Void> removeSessionsOfMe() {
        String userId = resolveCurrentUserId();
        chatSessionService.removeByUserId(userId);
        return ResultVOUtils.<Void>success("success.chat.allCleared", null);
    }

    /**
     * 解析当前请求的用户 ID。
     *
     * @return 当前用户 ID（字符串形式）
     */
    private String resolveCurrentUserId() {
        String userId = SecurityUtils.getCurrentUserId();
        if (userId == null || userId.trim().isEmpty()) {
            throw new TokenException("error.token.noUserId");
        }
        return userId;
    }
}
