package com.luck.report.web.modules.modelConfig.controller;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.modules.modelConfig.converter.ModelConfigConverter;
import com.luck.report.web.modules.modelConfig.domain.dto.ModelConfigDTO;
import com.luck.report.web.modules.modelConfig.domain.dto.ModelConfigQueryDTO;
import com.luck.report.web.modules.modelConfig.domain.entity.ModelConfig;
import com.luck.report.web.modules.modelConfig.domain.enums.ModelType;
import com.luck.report.web.modules.chat.domain.vo.ModelCheckVo;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.web.i18n.ReportI18n;
import com.luck.report.web.modules.modelConfig.service.ModelConfigDataService;
import javax.validation.Valid;

import com.luck.report.web.utils.DownloadUtils;
import com.luck.report.web.utils.ResultVOUtils;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 模型配置Controller
 *
 * @author luck
 */
@AllArgsConstructor
@RestController("bean.modelConfigController")
@RequestMapping("${luck-report.servletPrefix:}/model_config")
public class ModelConfigController {

    @Qualifier("bean.modelConfigDataService")
    private final ModelConfigDataService modelConfigDataService;

    /**
     * 获取模型配置列表
     *
     * @return ResultVO包含模型配置列表
     */
    @GetMapping("/list")
    public ResultVO<List<ModelConfigDTO>> list() {
        try {
            List<ModelConfigDTO> configs = modelConfigDataService.listConfigs();
            return ResultVO.success("获取模型配置列表成功", sanitizeList(configs));
        } catch (Exception e) {
            return ResultVO.error("Failed to get model configuration list: " + e.getMessage());
        }
    }

    /**
     * 分页查询模型配置列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @PostMapping("/page")
    public PageResultVO<ModelConfigDTO> queryByPage(@Valid @RequestBody ModelConfigQueryDTO queryDTO) {
        try {
            PageResultVO<ModelConfigDTO> pageResult = modelConfigDataService.queryByPage(queryDTO);
            if (pageResult.getRecords() != null) {
                pageResult.setRecords(sanitizeList(pageResult.getRecords()));
            }
            return pageResult;
        } catch (Exception e) {
            return PageResultVO.error(ReportI18n.getMessage("error.model.pageFailed", ReportI18n.messageOf(e)));
        }
    }

    @GetMapping("/detail/{id}")
    public ResultVO<ModelConfigDTO> getDetail(@PathVariable String id) {
        try {
            ModelConfig entity = modelConfigDataService.getById(id);
            if (entity == null) {
                return ResultVOUtils.error("error.model.configNotFound");
            }
            return ResultVOUtils.success("success.model.detailLoaded",
                    sanitize(ModelConfigConverter.toDTO(entity)));
        } catch (Exception e) {
            return ResultVOUtils.error("error.model.detailFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 新增模型配置
     *
     * @param config ModelConfigDTO对象
     * @return ResultVO操作结果
     */
    @PostMapping("/create")
    public ResultVO<String> add(@Valid @RequestBody ModelConfigDTO config) {
        try {
            modelConfigDataService.addConfig(config);
            return ResultVOUtils.success("success.model.saved", ReportI18n.getMessage("success.model.saved"));
        } catch (Exception e) {
            return ResultVOUtils.error("error.model.saveFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 更新模型配置
     *
     * @param config ModelConfigDTO对象
     * @return ResultVO操作结果
     */
    @PutMapping("/update/{id}")
    public ResultVO<String> update(@PathVariable String id,
                                   @Valid @RequestBody ModelConfigDTO config) {
        try {
            config.setId(id);
            modelConfigDataService.updateConfigInDb(config);
            return ResultVOUtils.success("success.model.updated", ReportI18n.getMessage("success.model.updated"));
        } catch (Exception e) {
            return ResultVOUtils.error("error.model.updateFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 删除模型配置
     *
     * @param id 配置ID
     * @return ResultVO操作结果
     */
    @DeleteMapping("/delete/{id}")
    public ResultVO<String> delete(@PathVariable String id) {
        try {
            modelConfigDataService.deleteConfig(id);
            return ResultVOUtils.success("success.model.deleted", ReportI18n.getMessage("success.model.deleted"));
        } catch (Exception e) {
            return ResultVOUtils.error("error.model.deleteFailed", ReportI18n.messageOf(e));
        }
    }

    @DeleteMapping("/batch/delete")
    public ResultVO<String> deleteBatch(@RequestBody List<String> ids) {
        try {
            modelConfigDataService.deleteConfigBatch(ids);
            return ResultVOUtils.success("success.model.deleted", ReportI18n.getMessage("success.model.deleted"));
        } catch (Exception e) {
            return ResultVOUtils.error("error.model.deleteFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 启用/禁用模型配置
     *
     * @param id      配置ID
     * @param enabled 是否启用
     * @return ResultVO操作结果
     */
    @PostMapping("/enable/{id}")
    public ResultVO<String> updateEnabledStatus(@PathVariable String id,
                                                @RequestParam(value = "enabled") Boolean enabled) {
        try {
            if (Boolean.TRUE.equals(enabled)) {
                modelConfigDataService.enableConfig(id);
                return ResultVOUtils.success("success.model.enabled", ReportI18n.getMessage("success.model.enabled"));
            }
            modelConfigDataService.disableConfig(id);
            return ResultVOUtils.success("success.model.disabled", ReportI18n.getMessage("success.model.disabled"));
        } catch (Exception e) {
            return ResultVOUtils.error(
                    Boolean.TRUE.equals(enabled) ? "error.model.enableFailed" : "error.model.disableFailed",
                    ReportI18n.messageOf(e));
        }
    }

    /**
     * 根据模型类型获取所有启用的模型配置列表
     *
     * @param modelType 模型类型(CHAT/EMBEDDING/RERANK)
     * @return ResultVO包含启用的模型配置列表
     */
    @GetMapping("/list_enabled/{modelType}")
    public ResultVO<List<ModelConfigDTO>> getEnabledList(@PathVariable String modelType) {
        try {
            ModelType type = ModelType.fromCode(modelType);
            List<ModelConfigDTO> enabledConfigs = modelConfigDataService.listEnabledConfigsByType(type);
            return ResultVOUtils.success("success.model.enabledListLoaded", sanitizeList(enabledConfigs));
        } catch (Exception e) {
            return ResultVOUtils.error("error.model.enabledListFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 检查模型配置是否就绪
     *
     * @return ResultVO包含模型检查结果
     */
    @GetMapping("/check_ready")
    public ResultVO<ModelCheckVo> checkReady() {
        ModelConfigDTO chatModel = modelConfigDataService.getEnabledConfigByType(ModelType.CHAT);
        ModelConfigDTO embeddingModel = modelConfigDataService.getEnabledConfigByType(ModelType.EMBEDDING);

        boolean chatModelReady = chatModel != null;
        boolean embeddingModelReady = embeddingModel != null;
        boolean ready = chatModelReady && embeddingModelReady;

        return ResultVOUtils.success("success.model.checkReadyDone",
                ModelCheckVo.builder()
                        .chatModelReady(chatModelReady)
                        .embeddingModelReady(embeddingModelReady)
                        .ready(ready)
                        .build());
    }

    /**
     * 批量脱敏
     *
     * @param configs 原始 DTO 列表
     * @return 脱敏后的 DTO 列表（新对象，apiKey 为 null）
     */
    private List<ModelConfigDTO> sanitizeList(List<ModelConfigDTO> configs) {
        if (configs == null) {
            return null;
        }
        return configs.stream().map(this::sanitize).collect(Collectors.toList());
    }

    /**
     * 单个脱敏
     *
     * @param config 原始 DTO
     * @return 脱敏后的 DTO（新对象，apiKey 为 null）
     */
    private ModelConfigDTO sanitize(ModelConfigDTO config) {
        if (config == null) {
            return null;
        }
        ModelConfigDTO copy = new ModelConfigDTO();
        BeanUtils.copyProperties(config, copy);
        copy.setApiKey(null);
        return copy;
    }
}
