package com.luck.report.web.modules.chat.service.impl;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.modules.chat.domain.dto.ChatSessionQueryDTO;
import com.luck.report.web.modules.chat.domain.entity.ChatSession;
import com.luck.report.web.modules.chat.mapper.ChatSessionMapper;
import com.luck.report.web.modules.chat.service.ChatSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 聊天会话服务实现
 *
 * @author luck
 */
@Slf4j
@Service("bean.chatSessionService")
@RequiredArgsConstructor
public class ChatSessionServiceImpl implements ChatSessionService {

    @Qualifier("bean.chatSessionMapper")
    private final ChatSessionMapper chatSessionMapper;

    @Override
    public List<ChatSession> findAll() {
        return chatSessionMapper.selectList(new ChatSessionQueryDTO());
    }

    @Override
    public List<ChatSession> findByUserId(String userId) {
        return chatSessionMapper.selectList(ChatSessionQueryDTO.builder().userId(userId).build());
    }

    @Override
    public PageResultVO<ChatSession> findByUserIdWithPage(String userId, int pageNum, int pageSize) {
        ChatSessionQueryDTO queryDTO = ChatSessionQueryDTO.builder().userId(userId).build();
        long total = chatSessionMapper.selectCount(queryDTO);
        int offset = (pageNum - 1) * pageSize;
        List<ChatSession> records = chatSessionMapper.selectPage(queryDTO, offset, pageSize);
        return PageResultVO.success(records, total, pageNum, pageSize);
    }

    @Override
    public ChatSession findBySessionId(String sessionId) {
        return chatSessionMapper.selectById(sessionId);
    }

    @Override
    public ChatSession createSession(String title, String userId) {
        LocalDateTime now = LocalDateTime.now();
        ChatSession session = new ChatSession();
        session.setId(UUID.randomUUID().toString());
        session.setTitle(title != null ? title : "新对话");
        session.setPinned(false);
        session.setUserId(userId);
        session.setCreateBy(userId);
        session.setUpdateBy(userId);
        session.setCreateTime(now);
        session.setDelFlag(0);
        session.setUpdateTime(now);

        chatSessionMapper.insert(session);
        log.info("创建会话: sessionId={}", session.getId());
        return session;
    }

    @Override
    public void refreshSessionTime(String sessionId) {
        chatSessionMapper.updateSessionTime(sessionId, LocalDateTime.now());
    }

    @Override
    public void pinSession(String sessionId, Boolean pinned) {
        chatSessionMapper.updatePinStatus(sessionId, Boolean.TRUE.equals(pinned), LocalDateTime.now());
        log.info("更新会话置顶状态: sessionId={}, pinned={}", sessionId, pinned);
    }

    @Override
    public void renameSession(String sessionId, String newTitle) {
        chatSessionMapper.updateTitle(sessionId, newTitle, LocalDateTime.now());
        log.info("重命名会话: sessionId={}, newTitle={}", sessionId, newTitle);
    }

    @Override
    public void deleteSession(String sessionId) {
        chatSessionMapper.deleteById(sessionId, LocalDateTime.now());
        log.info("删除会话: sessionId={}", sessionId);
    }

    @Override
    public void deleteSessionsByUserId(String userId) {
        int count = chatSessionMapper.deleteByUserId(userId, LocalDateTime.now());
        log.info("删除用户下所有会话: userId={}, count={}", userId, count);
    }
}
