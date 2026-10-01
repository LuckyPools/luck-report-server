package com.luck.report.web.modules.knowledge.controller;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.web.i18n.ReportI18n;
import com.luck.report.web.modules.knowledge.domain.dto.BusinessKnowledgeQueryDTO;
import com.luck.report.web.modules.knowledge.domain.dto.CreateBusinessKnowledgeDTO;
import javax.validation.Valid;

import com.luck.report.web.modules.knowledge.domain.dto.UpdateBusinessKnowledgeDTO;
import com.luck.report.web.modules.knowledge.domain.dto.UpdateKnowledgeChunkDTO;
import com.luck.report.web.modules.knowledge.domain.vo.BusinessKnowledgeVO;
import com.luck.report.web.modules.knowledge.domain.vo.KnowledgeChunkVO;
import com.luck.report.web.modules.knowledge.service.BusinessKnowledgeService;
import com.luck.report.web.utils.ResultVOUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 业务知识管理Controller
 *
 * @author luck
 */
@Slf4j
@RestController("bean.businessKnowledgeController")
@RequestMapping("${luck-report.servletPrefix:}/business_knowledge")
@AllArgsConstructor
public class BusinessKnowledgeController {

    @Qualifier("bean.businessKnowledgeService")
    private final BusinessKnowledgeService businessKnowledgeService;

    @GetMapping("/detail/{id}")
    public ResultVO<BusinessKnowledgeVO> getKnowledgeById(@PathVariable("id") String id) {
        BusinessKnowledgeVO knowledge = businessKnowledgeService.getKnowledgeById(id);
        if (knowledge == null) {
            return ResultVOUtils.error("error.knowledge.bizNotFound");
        }
        return ResultVOUtils.success("success.knowledge.bizDetailLoaded", knowledge);
    }

    @PostMapping(value = "/create")
    public ResultVO<BusinessKnowledgeVO> createKnowledge(
            @RequestParam("title") String title,
            @RequestParam("type") String type,
            @RequestParam(value = "question", required = false) String question,
            @RequestParam(value = "content", required = false) String content,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestParam(value = "splitterType", required = false) String splitterType,
            @RequestParam("modelId") String modelId) {

        CreateBusinessKnowledgeDTO dto = new CreateBusinessKnowledgeDTO();
        dto.setTitle(title);
        dto.setType(type);
        dto.setQuestion(question);
        dto.setContent(content);
        dto.setFile(file);
        dto.setSplitterType(splitterType);
        dto.setModelId(modelId);

        BusinessKnowledgeVO knowledge = businessKnowledgeService.createKnowledge(dto);
        return ResultVOUtils.success("success.knowledge.bizCreated", knowledge);
    }

    @PutMapping("/update/{id}")
    public ResultVO<BusinessKnowledgeVO> updateKnowledge(@PathVariable("id") String id,
                                                         @RequestBody UpdateBusinessKnowledgeDTO updateKnowledgeDTO) {
        BusinessKnowledgeVO knowledge = businessKnowledgeService.updateKnowledge(id, updateKnowledgeDTO);
        return ResultVOUtils.success("success.knowledge.bizUpdated", knowledge);
    }

    @PostMapping("/enable/{id}")
    public ResultVO<BusinessKnowledgeVO> updateEnabledStatus(@PathVariable("id") String id,
                                                             @RequestParam(value = "enabled") Boolean enabled) {
        BusinessKnowledgeVO knowledge = businessKnowledgeService.updateEnabledStatus(id, enabled);
        return ResultVOUtils.success("success.knowledge.bizStatusUpdated", knowledge);
    }

    @DeleteMapping("/delete/{id}")
    public ResultVO<Boolean> deleteKnowledge(@PathVariable("id") String id) {
        boolean result = businessKnowledgeService.deleteKnowledge(id);
        return result ? ResultVOUtils.success("success.knowledge.bizDeleted", true)
                : ResultVOUtils.<Boolean>error("error.knowledge.bizDeleteFailed");
    }

    @DeleteMapping("/batch/delete")
    public ResultVO<Boolean> deleteKnowledgeBatch(@RequestBody List<String> ids) {
        businessKnowledgeService.deleteKnowledgeBatch(ids);
        return ResultVOUtils.success("success.knowledge.bizDeleted", true);
    }

    @PostMapping("/page")
    public PageResultVO<BusinessKnowledgeVO> queryByPage(@Valid @RequestBody BusinessKnowledgeQueryDTO queryDTO) {
        try {
            return businessKnowledgeService.queryByPage(queryDTO);
        } catch (Exception e) {
            log.error("分页查询业务知识列表失败：{}", e.getMessage());
            return PageResultVO.error(ReportI18n.getMessage("error.knowledge.pageFailed", ReportI18n.messageOf(e)));
        }
    }

    @PostMapping("/retry_embedding/{id}")
    public ResultVO<Boolean> retryEmbedding(@PathVariable("id") String id) {
        businessKnowledgeService.retryEmbedding(id);
        return ResultVOUtils.success("success.knowledge.bizRetried", true);
    }

    @GetMapping("/{id}/chunks/list")
    public ResultVO<List<KnowledgeChunkVO>> listChunks(@PathVariable("id") String id) {
        return ResultVOUtils.success("success.knowledge.bizDetailLoaded",
                businessKnowledgeService.listChunks(id));
    }

    @PutMapping("/{id}/chunks/update/{vectorId}")
    public ResultVO<KnowledgeChunkVO> updateChunk(@PathVariable("id") String id,
                                                  @PathVariable("vectorId") String vectorId,
                                                  @Valid @RequestBody UpdateKnowledgeChunkDTO dto) {
        return ResultVOUtils.success("success.knowledge.bizUpdated",
                businessKnowledgeService.updateChunk(id, vectorId, dto));
    }

    @DeleteMapping("/{id}/chunks/delete/{vectorId}")
    public ResultVO<Void> deleteChunk(@PathVariable("id") String id,
                                      @PathVariable("vectorId") String vectorId) {
        businessKnowledgeService.deleteChunk(id, vectorId);
        return ResultVOUtils.success("success.knowledge.bizDeleted", null);
    }
}
