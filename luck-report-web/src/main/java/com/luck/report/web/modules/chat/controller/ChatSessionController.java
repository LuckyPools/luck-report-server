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
import java.util.Map;

/**
 * 聊天会话控制器
 * 提供会话的创建、查询、重命名、置顶、删除等 REST 接口
 * 前端进入对话页面时通过此接口加载会话列表，进入旧对话时通过此接口获取会话信息
 *
 * <p>用户身份解析：所有"当前用户"相关的接口（/sessions/me/list、/sessions/me/page、
 * POST /sessions/create、DELETE /sessions/me/delete）均通过 {@link TokenService#getCurrentUserId(ApiRequest)}
 * 从第三方系统获取真实用户 ID，前端不再传 userId。
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
     * 按置顶优先、更新时间倒序返回
     *
     * @return 会话列表
     */
    @GetMapping("/list")
    public ResultVO<List<ChatSession>> listSessions() {
        List<ChatSession> sessions = chatSessionService.findAll();
        return ResultVOUtils.success("success.chat.sessionsLoaded", sessions);
    }

    /**
     * 查询当前用户的会话列表
     * 用户 ID 由 TokenService 从第三方系统解析，前端无需传递
     *
     * @return 当前用户的会话列表
     */
    @GetMapping("/me/list")
    public ResultVO<List<ChatSession>> getSessionsOfMe() {
        String userId = resolveCurrentUserId();
        List<ChatSession> sessions = chatSessionService.findByUserId(userId);
        return ResultVOUtils.success("success.chat.sessionsLoaded", sessions);
    }

    /**
     * 分页查询当前用户的会话列表
     * 按置顶优先、更新时间倒序返回，支持滚动加载
     *
     * @param pageNum  页码，从1开始，默认1
     * @param pageSize 每页数量，默认10
     * @return 分页结果
     */
    @GetMapping("/me/page")
    public ResultVO<PageResultVO<ChatSession>> getSessionsOfMeWithPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {
        // 限制每页最大数量，防止一次加载过多
        pageSize = Math.min(pageSize, 50);
        String userId = resolveCurrentUserId();
        PageResultVO<ChatSession> result = chatSessionService.findByUserIdWithPage(userId, pageNum, pageSize);
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
        ChatSession session = chatSessionService.findBySessionId(sessionId);
        if (session == null) {
            return ResultVOUtils.error("error.chat.sessionNotFound");
        }
        return ResultVOUtils.success("success.chat.sessionsLoaded", session);
    }

    /**
     * 创建新会话
     * 前端首次发送消息时调用，返回包含 UUID 的会话对象
     * 用户 ID 由 TokenService 从第三方系统解析，前端 body 无需传递 userId
     *
     * @param body    请求体，可选字段：title-标题
     * @return 新建的会话实体
     */
    @PostMapping("/create")
    public ResultVO<ChatSession> createSession(@RequestBody(required = false) Map<String, Object> body) {
        String title = body != null ? (String) body.get("title") : null;
        String userId = resolveCurrentUserId();

        ChatSession session = chatSessionService.createSession(title, userId);
        return ResultVOUtils.success("success.chat.sessionCreated", session);
    }

    /**
     * 重命名会话
     *
     * @param sessionId 会话ID
     * @param request   请求体，包含 title 字段
     * @return 操作结果
     */
    @PostMapping("/{sessionId}/rename")
    public ResultVO<Void> renameSession(
            @PathVariable String sessionId,
            @RequestBody Map<String, String> request) {
        String title = request.get("title");
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
     * @param request   请求体，包含 isPinned 字段（0-否，1-是）
     * @return 操作结果
     */
    @PostMapping("/{sessionId}/pin")
    public ResultVO<Void> pinSession(
            @PathVariable String sessionId,
            @RequestBody Map<String, Integer> request) {
        Integer isPinned = request.get("isPinned");
        if (isPinned == null) {
            return ResultVOUtils.error("error.chat.isPinnedEmpty");
        }
        chatSessionService.pinSession(sessionId, isPinned);
        return ResultVO.success(isPinned == 1 ? "已置顶" : "已取消置顶", null);
    }

    /**
     * 删除单个会话（软删除）
     *
     * @param sessionId 会话ID
     * @return 操作结果
     */
    @DeleteMapping("/delete/{sessionId}")
    public ResultVO<Void> deleteSession(@PathVariable String sessionId) {
        chatSessionService.deleteSession(sessionId);
        return ResultVOUtils.<Void>success("success.chat.sessionDeleted", null);
    }

    /**
     * 删除当前用户下的所有会话（软删除）
     * 用户 ID 由 TokenService 从第三方系统解析
     *
     * @return 操作结果
     */
    @DeleteMapping("/me/delete")
    public ResultVO<Void> deleteSessionsOfMe() {
        String userId = resolveCurrentUserId();
        chatSessionService.deleteSessionsByUserId(userId);
        return ResultVOUtils.<Void>success("success.chat.allCleared", null);
    }

    /**
     * 解析当前请求的用户 ID。
     * <p>通过 TokenService.getCurrentUserId 获取第三方系统返回的用户 ID 字符串，
     * 为空（未登录或解析失败）抛出 TokenException。
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
