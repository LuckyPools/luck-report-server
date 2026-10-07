package com.luck.report.web.modules.agentRule.domain.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能体规则 DTO
 *
 * @author luck
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRuleDTO {

    /**
     * 规则ID
     */
    private String id;

    /**
     * 规则标题
     */
    @NotBlank(message = "规则标题不能为空")
    @Size(max = 255, message = "规则标题不能超过255个字")
    private String title;

    /**
     * 规则正文
     */
    private String content;

    /**
     * 排序字段，数字越小越靠前
     */
    private Integer sort;

    /**
     * 是否生效：true-生效，false-不生效
     */
    private Boolean enabled;

    /**
     * 创建时间
     */
    private String createTime;

    /**
     * 更新时间
     */
    private String updateTime;
}
