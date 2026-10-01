package com.luck.report.web.modules.dataset.controller;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.web.i18n.ReportI18n;
import com.luck.report.web.modules.dataset.domain.dto.ReportDatasetQueryDTO;
import com.luck.report.web.modules.dataset.domain.dto.ReportDatasetSaveDTO;
import com.luck.report.web.modules.dataset.domain.vo.ReportDatasetVO;
import com.luck.report.web.modules.dataset.service.ReportDatasetService;
import com.luck.report.web.utils.DownloadUtils;
import com.luck.report.web.utils.ResultVOUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

/**
 * 公共数据集管理Controller
 * 提供公共数据集的增删改查、分页查询和状态管理接口
 *
 * @author luck
 */
@Slf4j
@RestController("bean.reportDatasetController")
@RequestMapping("${luck-report.servletPrefix:}/report_dataset")
@AllArgsConstructor
public class ReportDatasetController {

    @Qualifier("bean.reportDatasetService")
    private final ReportDatasetService reportDatasetService;

    /**
     * 获取公共数据集列表（支持按状态筛选）
     *
     * @param enabled 启用状态筛选（可选）
     * @return 公共数据集VO列表
     */
    @GetMapping("/list")
    public ResultVO<List<ReportDatasetVO>> list(
            @RequestParam(value = "enabled", required = false) Boolean enabled) {
        try {
            return ResultVOUtils.success("success.dataset.listLoaded", reportDatasetService.listByEnabled(enabled));
        } catch (Exception e) {
            log.error("查询公共数据集列表失败", e);
            return ResultVOUtils.error("error.dataset.listFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 分页查询公共数据集列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @PostMapping("/page")
    public PageResultVO<ReportDatasetVO> queryByPage(@Valid @RequestBody ReportDatasetQueryDTO queryDTO) {
        try {
            return reportDatasetService.queryByPage(queryDTO);
        } catch (Exception e) {
            log.error("分页查询公共数据集失败", e);
            return PageResultVO.error(ReportI18n.getMessage("error.dataset.pageFailed", ReportI18n.messageOf(e)));
        }
    }

    /**
     * 获取公共数据集详情
     *
     * @param id 公共数据集ID
     * @return 公共数据集VO
     */
    @GetMapping("/detail/{id}")
    public ResultVO<ReportDatasetVO> getDetail(@PathVariable String id) {
        try {
            ReportDatasetVO vo = reportDatasetService.getById(id);
            if (vo == null) {
                return ResultVOUtils.error("error.dataset.notExist");
            }
            return ResultVOUtils.success("success.dataset.detailLoaded", vo);
        } catch (Exception e) {
            log.error("查询公共数据集详情失败", e);
            return ResultVOUtils.error("error.dataset.detailFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 创建公共数据集
     *
     * @param dto 保存参数
     * @return 创建后的公共数据集VO
     */
    @PostMapping("/create")
    public ResultVO<ReportDatasetVO> create(@Valid @RequestBody ReportDatasetSaveDTO dto) {
        try {
            return ResultVOUtils.success("success.dataset.created", reportDatasetService.create(dto));
        } catch (Exception e) {
            log.error("创建公共数据集失败", e);
            return ResultVOUtils.error("error.dataset.createFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 更新公共数据集
     *
     * @param id  公共数据集ID
     * @param dto 保存参数
     * @return 更新后的公共数据集VO
     */
    @PutMapping("/update/{id}")
    public ResultVO<ReportDatasetVO> update(@PathVariable String id, @Valid @RequestBody ReportDatasetSaveDTO dto) {
        try {
            return ResultVOUtils.success("success.dataset.updated", reportDatasetService.update(id, dto));
        } catch (Exception e) {
            log.error("更新公共数据集失败", e);
            return ResultVOUtils.error("error.dataset.updateFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 删除公共数据集
     *
     * @param id 公共数据集ID
     * @return 删除结果
     */
    @DeleteMapping("/delete/{id}")
    public ResultVO<String> delete(@PathVariable String id) {
        try {
            reportDatasetService.deleteById(id);
            return ResultVOUtils.success("success.dataset.deleted", ReportI18n.getMessage("success.dataset.deleted"));
        } catch (Exception e) {
            log.error("删除公共数据集失败", e);
            return ResultVOUtils.error("error.dataset.deleteFailed", ReportI18n.messageOf(e));
        }
    }

    @DeleteMapping("/batch/delete")
    public ResultVO<String> deleteBatch(@RequestBody List<String> ids) {
        try {
            reportDatasetService.deleteByIds(ids);
            return ResultVOUtils.success("success.dataset.deleted", ReportI18n.getMessage("success.dataset.deleted"));
        } catch (Exception e) {
            log.error("批量删除公共数据集失败", e);
            return ResultVOUtils.error("error.dataset.deleteFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 更新公共数据集状态（启用/禁用）
     *
     * @param id     公共数据集ID
     * @param enabled 是否启用
     * @return 操作结果
     */
    @PostMapping("/enable/{id}")
    public ResultVO<String> updateEnabledStatus(@PathVariable String id, @RequestParam(value = "enabled") Boolean enabled) {
        try {
            reportDatasetService.updateEnabledStatus(id, enabled);
            return ResultVOUtils.success("success.dataset.enabledUpdated", ReportI18n.getMessage("success.dataset.enabledUpdated"));
        } catch (Exception e) {
            log.error("更新公共数据集启用状态失败", e);
            return ResultVOUtils.error("error.dataset.enabledFailed", ReportI18n.messageOf(e));
        }
    }
}
