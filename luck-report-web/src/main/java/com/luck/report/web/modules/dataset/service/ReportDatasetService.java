package com.luck.report.web.modules.dataset.service;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.modules.dataset.domain.dto.ReportDatasetQueryDTO;
import com.luck.report.web.modules.dataset.domain.dto.ReportDatasetSaveDTO;
import com.luck.report.web.modules.dataset.domain.vo.ReportDatasetVO;

import java.util.List;

/**
 * 公共数据集服务接口
 *
 * @author luck
 */
public interface ReportDatasetService {

    /**
     * 按启用状态查询公共数据集列表
     *
     * @param active 是否启用（可为空则查全部）
     * @return 公共数据集VO列表（已回填所属数据源名称与状态）
     */
    List<ReportDatasetVO> listByEnabled(Boolean enabled);

    /**
     * 分页条件查询公共数据集列表
     *
     * @param queryDTO 查询条件（name模糊/type/datasourceId/enabled/分页参数）
     * @return 分页结果（已回填所属数据源名称与状态）
     */
    PageResultVO<ReportDatasetVO> queryByPage(ReportDatasetQueryDTO queryDTO);

    /**
     * 根据ID查询公共数据集详情
     *
     * @param id 公共数据集ID
     * @return 公共数据集VO，不存在则返回null
     */
    ReportDatasetVO getById(String id);

    /**
     * 创建公共数据集
     *
     * @param dto 保存参数
     * @return 创建后的公共数据集VO
     */
    ReportDatasetVO create(ReportDatasetSaveDTO dto);

    /**
     * 更新公共数据集
     *
     * @param id 公共数据集ID
     * @param dto 保存参数
     * @return 更新后的公共数据集VO
     */
    ReportDatasetVO update(String id, ReportDatasetSaveDTO dto);

    /**
     * 根据ID删除公共数据集
     *
     * @param id 公共数据集ID
     */
    void removeById(String id);

    /**
     * 批量删除公共数据集
     *
     * @param ids 公共数据集ID列表
     */
    void removeByIds(List<String> ids);

    /**
     * 更新公共数据集启用状态
     *
     * @param id     公共数据集ID
     * @param enabled 是否启用
     */
    void updateEnabledStatus(String id, Boolean enabled);

    /**
     * 统计引用指定公共数据源的公共数据集数量
     *
     * @param datasourceId 公共数据源ID
     * @return 引用数量
     */
    Long countByDatasource(String datasourceId);
}
