package com.luck.report.web.modules.agentRule.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能体规则拼接提示词 VO
 *
 * @author luck
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AgentRulePromptVO {

    /**
     * 已启用规则拼接后的全文；无启用规则时为空串
     */
    private String content;
}
