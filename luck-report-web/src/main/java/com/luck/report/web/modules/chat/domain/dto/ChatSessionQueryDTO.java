package com.luck.report.web.modules.chat.domain.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 聊天会话查询DTO
 *
 * @author luck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSessionQueryDTO {

    /**
     * 用户ID（可选；为空则不按用户过滤）
     */
    private String userId;

    /**
     * 当前页码（默认第1页）
     */
    @NotNull(message = "pageNum不能为空")
    @Min(value = 1, message = "pageNum不能小于1")
    @Builder.Default
    private Integer pageNum = 1;

    /**
     * 每页大小（默认10条）
     */
    @NotNull(message = "pageSize不能为空")
    @Min(value = 1, message = "pageSize不能小于1")
    @Builder.Default
    private Integer pageSize = 10;
}
