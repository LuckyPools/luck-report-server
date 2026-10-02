package com.luck.report.web.modules.chat.mapper;

import com.luck.report.web.modules.chat.domain.dto.ChatSessionQueryDTO;
import com.luck.report.web.modules.chat.domain.entity.ChatSession;
import com.luck.report.jdbc.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天会话 Mapper
 *
 * @author luck
 */
public interface ChatSessionMapper {

    /**
     * 按条件查询会话列表（非分页）；userId 可选，为空则查全部未删除会话
     *
     * @param queryDTO 查询条件
     * @return 会话列表
     */
    List<ChatSession> selectList(@Param("queryDTO") ChatSessionQueryDTO queryDTO);

    /**
     * 分页条件查询会话列表
     *
     * @param queryDTO 查询条件
     * @param offset   偏移量
     * @param pageSize 每页数量
     * @return 会话列表
     */
    List<ChatSession> selectPage(@Param("queryDTO") ChatSessionQueryDTO queryDTO,
                                            @Param("offset") int offset,
                                            @Param("pageSize") int pageSize);

    /**
     * 统计符合条件的会话数量
     *
     * @param queryDTO 查询条件
     * @return 会话总数
     */
    long selectCount(@Param("queryDTO") ChatSessionQueryDTO queryDTO);

    /**
     * 根据ID查询会话详情
     *
     * @param id 会话ID
     * @return 会话实体，不存在返回 null
     */
    ChatSession selectById(@Param("id") String id);

    /**
     * 插入新会话
     *
     * @param session 会话实体
     * @return 影响行数
     */
    int insert(ChatSession session);

    /**
     * 更新会话最后活动时间
     *
     * @param id         会话ID
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int updateSessionTime(@Param("id") String id, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 更新会话置顶状态
     *
     * @param id         会话ID
     * @param pinned   是否置顶
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int updatePinStatus(@Param("id") String id, @Param("pinned") Boolean pinned,
                        @Param("updateTime") LocalDateTime updateTime);

    /**
     * 重命名会话
     *
     * @param id         会话ID
     * @param title      新标题
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int updateTitle(@Param("id") String id, @Param("title") String title,
                    @Param("updateTime") LocalDateTime updateTime);

    /**
     * 软删除单个会话（del_flag=1）
     *
     * @param id         会话ID
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int deleteById(@Param("id") String id, @Param("updateTime") LocalDateTime updateTime);

    /**
     * 软删除指定用户下的所有会话
     *
     * @param userId     用户ID（字符串形式）
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") String userId, @Param("updateTime") LocalDateTime updateTime);
}
