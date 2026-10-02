package com.luck.report.web.modules.chat.domain.entity;

import com.luck.report.web.common.domain.entity.DataEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 聊天会话实体
 *
 * @author luck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession extends DataEntity<ChatSession> {

    /**
     * 会话标题
     */
    private String title;

    /**
     * 是否置顶
     */
    private Boolean pinned;

    /**
     * 用户ID（字符串形式，兼容数字主键、UUID、工号等第三方用户标识）
     */
    private String userId;
}
