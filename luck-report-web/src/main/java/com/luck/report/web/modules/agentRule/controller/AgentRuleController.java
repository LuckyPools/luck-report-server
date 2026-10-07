package com.luck.report.web.modules.agentRule.controller;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.web.i18n.ReportI18n;
import com.luck.report.web.modules.agentRule.domain.dto.AgentRuleDTO;
import com.luck.report.web.modules.agentRule.domain.dto.AgentRuleQueryDTO;
import com.luck.report.web.modules.agentRule.domain.vo.AgentRulePromptVO;
import com.luck.report.web.modules.agentRule.service.AgentRuleService;
import com.luck.report.web.utils.ResultVOUtils;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 智能体规则管理 Controller
 *
 * @author luck
 */
@AllArgsConstructor
@RestController("bean.agentRuleController")
@RequestMapping("${luck-report.servletPrefix:}/agent_rule")
public class AgentRuleController {

    @Qualifier("bean.agentRuleService")
    private final AgentRuleService agentRuleService;

    /**
     * 拼接已启用规则，供 Agent/Harness 对话前注入
     *
     * @return 拼接后的提示词
     */
    @GetMapping("/prompt")
    public ResultVO<AgentRulePromptVO> prompt() {
        try {
            return ResultVOUtils.success("success.agentRule.promptLoaded", agentRuleService.buildPrompt());
        } catch (Exception e) {
            return ResultVOUtils.error("error.agentRule.promptFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 根据ID查询规则详情
     *
     * @param id 规则ID
     * @return 规则详情
     */
    @GetMapping("/detail/{id}")
    public ResultVO<AgentRuleDTO> getById(@PathVariable("id") String id) {
        try {
            AgentRuleDTO rule = agentRuleService.getById(id);
            if (rule == null) {
                return ResultVOUtils.error("error.agentRule.notFound");
            }
            return ResultVOUtils.success("success.agentRule.detailLoaded", rule);
        } catch (Exception e) {
            return ResultVOUtils.error("error.agentRule.detailFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 创建规则
     *
     * @param dto 规则 DTO
     * @return 创建后的规则
     */
    @PostMapping("/create")
    public ResultVO<AgentRuleDTO> create(@Valid @RequestBody AgentRuleDTO dto) {
        try {
            return ResultVOUtils.success("success.agentRule.saved", agentRuleService.create(dto));
        } catch (Exception e) {
            return ResultVOUtils.error("error.agentRule.saveFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 更新规则
     *
     * @param id 规则ID
     * @param dto 规则 DTO
     * @return 更新后的规则
     */
    @PutMapping("/update/{id}")
    public ResultVO<AgentRuleDTO> update(@PathVariable("id") String id,
                                         @Valid @RequestBody AgentRuleDTO dto) {
        try {
            return ResultVOUtils.success("success.agentRule.updated", agentRuleService.update(id, dto));
        } catch (Exception e) {
            return ResultVOUtils.error("error.agentRule.updateFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 启用或停用规则
     *
     * @param id 规则ID
     * @param enabled 是否生效
     * @return 更新后的规则
     */
    @PostMapping("/enable/{id}")
    public ResultVO<AgentRuleDTO> updateEnabledStatus(@PathVariable("id") String id,
                                                      @RequestParam("enabled") Boolean enabled) {
        try {
            AgentRuleDTO rule = agentRuleService.updateEnabledStatus(id, enabled);
            String key = Boolean.TRUE.equals(enabled) ? "success.agentRule.enabled" : "success.agentRule.disabled";
            return ResultVOUtils.success(key, rule);
        } catch (Exception e) {
            return ResultVOUtils.error(
                    Boolean.TRUE.equals(enabled) ? "error.agentRule.enableFailed" : "error.agentRule.disableFailed",
                    ReportI18n.messageOf(e));
        }
    }

    /**
     * 删除规则
     *
     * @param id 规则ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public ResultVO<Boolean> removeById(@PathVariable("id") String id) {
        try {
            boolean result = agentRuleService.removeById(id);
            return result ? ResultVOUtils.success("success.agentRule.deleted", true)
                    : ResultVOUtils.<Boolean>error("error.agentRule.deleteFailed");
        } catch (Exception e) {
            return ResultVOUtils.error("error.agentRule.deleteFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 批量删除规则
     *
     * @param ids 规则ID列表
     * @return 删除结果
     */
    @DeleteMapping("/batch/delete")
    public ResultVO<Boolean> removeByIds(@RequestBody List<String> ids) {
        try {
            agentRuleService.removeByIds(ids);
            return ResultVOUtils.success("success.agentRule.deleted", true);
        } catch (Exception e) {
            return ResultVOUtils.error("error.agentRule.deleteFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 分页查询规则列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @PostMapping("/page")
    public PageResultVO<AgentRuleDTO> listPage(@Valid @RequestBody AgentRuleQueryDTO queryDTO) {
        try {
            return agentRuleService.listPage(queryDTO);
        } catch (Exception e) {
            return PageResultVO.error(ReportI18n.getMessage("error.agentRule.pageFailed", ReportI18n.messageOf(e)));
        }
    }
}
