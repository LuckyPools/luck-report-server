package com.luck.report.web.modules.agentRule.domain.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能体规则分页查询 DTO
 *
 * @author luck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentRuleQueryDTO {

    /**
     * 规则标题；selectList 精确匹配，分页查询模糊匹配
     */
    private String title;

    /**
     * 是否生效：true-生效，false-不生效
     */
    private Boolean enabled;

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
