package com.luck.report.web.modules.chat.service;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.modules.chat.domain.entity.ChatSession;

import java.util.List;

/**
 * 聊天会话服务接口
 *
 * @author luck
 */
public interface ChatSessionService {

    /**
     * 查询所有未删除的会话列表
     *
     * @return 会话列表，按置顶优先、更新时间倒序
     */
    List<ChatSession> listAll();

    /**
     * 根据用户ID查询会话列表
     *
     * @param userId 用户ID（字符串形式），不可为空
     * @return 该用户下的活跃会话列表，按置顶优先、更新时间倒序
     */
    List<ChatSession> listByUserId(String userId);

    /**
     * 分页查询指定用户的会话列表
     *
     * @param userId   用户ID（字符串形式），不可为空
     * @param pageNum  页码，从1开始
     * @param pageSize 每页数量
     * @return 分页结果，包含会话列表和总数
     */
    PageResultVO<ChatSession> listPage(String userId, int pageNum, int pageSize);

    /**
     * 根据会话ID查询会话详情
     *
     * @param sessionId 会话ID，不可为空
     * @return 会话实体，不存在返回 null
     */
    ChatSession getById(String sessionId);

    /**
     * 创建新会话
     *
     * @param title  会话标题，可为空，默认"新对话"
     * @param userId 用户ID（字符串形式），可为空
     * @return 新建的会话实体
     */
    ChatSession create(String title, String userId);

    /**
     * 更新会话最后活动时间
     *
     * @param sessionId 会话ID，不可为空
     */
    void refreshSessionTime(String sessionId);

    /**
     * 置顶或取消置顶会话
     *
     * @param sessionId 会话ID，不可为空
     * @param pinned  是否置顶
     */
    void pinSession(String sessionId, Boolean pinned);

    /**
     * 重命名会话
     *
     * @param sessionId 会话ID，不可为空
     * @param newTitle  新标题，不可为空
     */
    void renameSession(String sessionId, String newTitle);

    /**
     * 删除单个会话（软删除）
     *
     * @param sessionId 会话ID，不可为空
     */
    void removeById(String sessionId);

    /**
     * 删除指定用户下的所有会话（软删除）
     *
     * @param userId 用户ID（字符串形式），不可为空
     */
    void removeByUserId(String userId);
}
