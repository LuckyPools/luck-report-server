package com.luck.report.web.modules.dataset.service;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.modules.dataset.domain.dto.ReportDatasetQueryDTO;
import com.luck.report.web.modules.dataset.domain.dto.ReportDatasetSaveDTO;
import com.luck.report.web.modules.dataset.domain.vo.ReportDatasetVO;

import java.util.List;

/**
 * 公共数据集服务接口
 * 提供公共数据集的增删改查、分页查询和状态管理能力
 *
 * @author luck
 */
public interface ReportDatasetService {

    /**
     * 按状态查询公共数据集列表
     *
     * @param status 状态筛选（active/inactive，可为空则查全部）
     * @return 公共数据集VO列表（已回填所属数据源名称与状态）
     */
    List<ReportDatasetVO> listByStatus(String status);

    /**
     * 分页条件查询公共数据集列表
     *
     * @param queryDTO 查询条件（name模糊/type/datasourceId/status/分页参数）
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
     * 校验名称唯一、类型合法、sql类型必绑active公共数据源且SQL通过安全校验、json类型内容必须为合法JSON数组
     *
     * @param dto 保存参数
     * @return 创建后的公共数据集VO
     */
    ReportDatasetVO create(ReportDatasetSaveDTO dto);

    /**
     * 更新公共数据集
     * 校验规则与创建一致，改名时重新查重
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
    void deleteById(String id);

    /**
     * 批量删除公共数据集
     *
     * @param ids 公共数据集ID列表
     */
    void deleteByIds(List<String> ids);

    /**
     * 更新公共数据集状态（启用/禁用）
     *
     * @param id     公共数据集ID
     * @param status 状态：active/inactive
     */
    void updateStatus(String id, String status);

    /**
     * 统计引用指定公共数据源的公共数据集数量
     * 数据源删除前的引用检查使用
     *
     * @param datasourceId 公共数据源ID
     * @return 引用数量
     */
    Long countByDatasourceId(String datasourceId);
}
