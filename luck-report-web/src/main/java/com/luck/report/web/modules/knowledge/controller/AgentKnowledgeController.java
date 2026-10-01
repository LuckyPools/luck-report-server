package com.luck.report.web.modules.knowledge.controller;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.web.i18n.ReportI18n;
import com.luck.report.web.modules.knowledge.domain.dto.AgentKnowledgeQueryDTO;
import com.luck.report.web.modules.knowledge.domain.dto.CreateAgentKnowledgeDTO;
import javax.validation.Valid;

import com.luck.report.web.modules.knowledge.domain.dto.UpdateAgentKnowledgeDTO;
import com.luck.report.web.modules.knowledge.domain.dto.UpdateKnowledgeChunkDTO;
import com.luck.report.web.modules.knowledge.domain.vo.AgentKnowledgeVO;
import com.luck.report.web.modules.knowledge.domain.vo.KnowledgeChunkVO;
import com.luck.report.web.modules.knowledge.service.AgentKnowledgeService;
import com.luck.report.web.utils.DownloadUtils;
import com.luck.report.web.utils.ResultVOUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 智能体知识管理Controller
 * 提供智能体知识的增删改查和向量化管理接口
 *
 * @author luck
 */
@Slf4j
@RestController("bean.agentKnowledgeController")
@RequestMapping("${luck-report.servletPrefix:}/agent_knowledge")
@AllArgsConstructor
public class AgentKnowledgeController {

    private final AgentKnowledgeService agentKnowledgeService;

    /**
     * 根据ID查询智能体知识详情
     *
     * @param id 智能体知识ID
     * @return 智能体知识详情
     */
    @GetMapping("/detail/{id}")
    public ResultVO<AgentKnowledgeVO> getKnowledgeById(@PathVariable("id") String id) {
        AgentKnowledgeVO knowledge = agentKnowledgeService.getKnowledgeById(id);
        if (knowledge == null) {
            return ResultVOUtils.error("error.knowledge.agentNotFound");
        }
        return ResultVOUtils.success("success.knowledge.agentDetailLoaded", knowledge);
    }

    /**
     * 创建智能体知识，支持文件上传
     *
     * @param title 知识标题
     * @param type 知识类型（DOCUMENT/QA/FAQ）
     * @param question 问题（QA/FAQ类型时使用）
     * @param content 内容（QA/FAQ类型时使用）
     * @param file 上传的文件（DOCUMENT类型时使用）
     * @param splitterType 分块策略类型
     * @return 创建的智能体知识
     */
    @PostMapping(value = "/create")
    public ResultVO<AgentKnowledgeVO> createKnowledge(
            @RequestParam("title") String title,
            @RequestParam("type") String type,
            @RequestParam(value = "question", required = false) String question,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "splitterType", required = false) String splitterType,
            @RequestParam("modelId") String modelId) {

        CreateAgentKnowledgeDTO dto = new CreateAgentKnowledgeDTO();
        dto.setTitle(title);
        dto.setType(type);
        dto.setQuestion(question);
        dto.setContent(content);
        dto.setFile(file);
        dto.setSplitterType(splitterType);
        dto.setModelId(modelId);

        AgentKnowledgeVO knowledge = agentKnowledgeService.createKnowledge(dto);
        return ResultVOUtils.success("success.knowledge.agentCreated", knowledge);
    }

    /**
     * 更新智能体知识
     *
     * @param id 智能体知识ID
     * @param updateKnowledgeDTO 更新智能体知识DTO
     * @return 更新的智能体知识
     */
    @PutMapping("/update/{id}")
    public ResultVO<AgentKnowledgeVO> updateKnowledge(@PathVariable("id") String id,
                                                          @RequestBody UpdateAgentKnowledgeDTO updateKnowledgeDTO) {
        AgentKnowledgeVO knowledge = agentKnowledgeService.updateKnowledge(id, updateKnowledgeDTO);
        return ResultVOUtils.success("success.knowledge.agentUpdated", knowledge);
    }

    /**
     * 更新生效状态
     *
     * @param id 智能体知识ID
     * @param enabled 是否生效
     * @return 更新的智能体知识
     */
    @PostMapping("/enable/{id}")
    public ResultVO<AgentKnowledgeVO> updateEnabledStatus(@PathVariable("id") String id,
                                                              @RequestParam(value = "enabled") Boolean enabled) {
        AgentKnowledgeVO knowledge = agentKnowledgeService.updateEnabledStatus(id, enabled);
        return ResultVOUtils.success("success.knowledge.agentUpdated", knowledge);
    }

    /**
     * 删除智能体知识
     *
     * @param id 智能体知识ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public ResultVO<Boolean> deleteKnowledge(@PathVariable("id") String id) {
        boolean result = agentKnowledgeService.deleteKnowledge(id);
        return result ? ResultVOUtils.success("success.knowledge.agentDeleted", true)
                : ResultVOUtils.<Boolean>error("error.knowledge.agentDeleteFailed");
    }

    @DeleteMapping("/batch/delete")
    public ResultVO<Boolean> deleteKnowledgeBatch(@RequestBody List<String> ids) {
        agentKnowledgeService.deleteKnowledgeBatch(ids);
        return ResultVOUtils.success("success.knowledge.agentDeleted", true);
    }

    /**
     * 分页查询智能体知识列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @PostMapping("/page")
    public PageResultVO<AgentKnowledgeVO> queryByPage(@Valid @RequestBody AgentKnowledgeQueryDTO queryDTO) {
        try {
            return agentKnowledgeService.queryByPage(queryDTO);
        } catch (Exception e) {
            log.error("分页查询知识列表失败：{}", e.getMessage());
            return PageResultVO.error(ReportI18n.getMessage("error.knowledge.pageFailed", ReportI18n.messageOf(e)));
        }
    }

    /**
     * 重试向量化
     *
     * @param id 智能体知识ID
     * @return 重试结果
     */
    @PostMapping("/retry_embedding/{id}")
    public ResultVO<Boolean> retryEmbedding(@PathVariable("id") String id) {
        agentKnowledgeService.retryEmbedding(id);
        return ResultVOUtils.success("success.knowledge.agentRetried", true);
    }

    @GetMapping("/{id}/chunks/list")
    public ResultVO<List<KnowledgeChunkVO>> listChunks(@PathVariable("id") String id) {
        return ResultVOUtils.success("success.knowledge.agentDetailLoaded",
                agentKnowledgeService.listChunks(id));
    }

    @PutMapping("/{id}/chunks/update/{vectorId}")
    public ResultVO<KnowledgeChunkVO> updateChunk(@PathVariable("id") String id,
                                                  @PathVariable("vectorId") String vectorId,
                                                  @Valid @RequestBody UpdateKnowledgeChunkDTO dto) {
        return ResultVOUtils.success("success.knowledge.agentUpdated",
                agentKnowledgeService.updateChunk(id, vectorId, dto));
    }

    @DeleteMapping("/{id}/chunks/delete/{vectorId}")
    public ResultVO<Void> deleteChunk(@PathVariable("id") String id,
                                      @PathVariable("vectorId") String vectorId) {
        agentKnowledgeService.deleteChunk(id, vectorId);
        return ResultVOUtils.success("success.knowledge.agentDeleted", null);
    }
}
