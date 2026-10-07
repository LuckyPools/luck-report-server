package com.luck.report.web.modules.agentRule.domain.entity;

import com.luck.report.web.common.domain.entity.DataEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 智能体规则实体
 *
 * @author luck
 */
@Data
@NoArgsConstructor
public class AgentRule extends DataEntity<AgentRule> {

    /**
     * 规则标题
     */
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
}
