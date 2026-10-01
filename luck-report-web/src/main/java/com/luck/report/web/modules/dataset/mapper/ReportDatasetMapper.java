package com.luck.report.web.modules.dataset.mapper;

import com.luck.report.web.modules.dataset.domain.dto.ReportDatasetQueryDTO;
import com.luck.report.web.modules.dataset.domain.entity.ReportDataset;
import com.luck.report.jdbc.Param;

import java.util.List;

/**
 * 公共数据集Mapper
 * 操作 luck_report_dataset 表
 * SQL 定义在 resources/luck-report/sql/{dbType}/ReportDatasetMapper.xml 中，支持多数据库方言
 *
 * @author luck
 */
public interface ReportDatasetMapper {

    /**
     * 插入公共数据集
     *
     * @param dataset 公共数据集实体
     * @return 影响行数
     */
    int insert(ReportDataset dataset);

    /**
     * 根据ID更新公共数据集
     *
     * @param dataset 公共数据集实体
     * @return 影响行数
     */
    int updateById(ReportDataset dataset);

    /**
     * 根据ID查询公共数据集
     *
     * @param id 公共数据集ID
     * @return 公共数据集实体，不存在则返回null
     */
    ReportDataset selectById(@Param("id") String id);

    /**
     * 按启用状态查询公共数据集列表
     *
     * @param enabled 是否启用
     * @return 公共数据集列表
     */
    List<ReportDataset> selectByEnabled(@Param("enabled") Boolean enabled);

    /**
     * 查询所有公共数据集（按创建时间倒序）
     *
     * @return 公共数据集列表
     */
    List<ReportDataset> selectAll();

    /**
     * 根据名称查询公共数据集
     *
     * @param name 数据集名称
     * @return 公共数据集实体，不存在则返回null
     */
    ReportDataset selectByName(@Param("name") String name);

    /**
     * 根据ID删除公共数据集
     *
     * @param id 公共数据集ID
     * @return 影响行数
     */
    int deleteById(@Param("id") String id);

    /**
     * 更新公共数据集启用状态
     *
     * @param id     公共数据集ID
     * @param enabled 是否启用
     * @return 影响行数
     */
    int updateEnabledById(@Param("id") String id, @Param("enabled") Boolean enabled,
                          @Param("updateTime") java.time.LocalDateTime updateTime);

    /**
     * 按数据源ID查询公共数据集数量（数据源删除前引用检查用）
     *
     * @param datasourceId 公共数据源ID
     * @return 引用该数据源的公共数据集数量
     */
    Long countByDatasourceId(@Param("datasourceId") String datasourceId);

    /**
     * 分页条件查询公共数据集
     *
     * @param queryDTO 查询条件
     * @param offset   偏移量
     * @param pageSize 每页大小
     * @return 公共数据集列表
     */
    List<ReportDataset> selectByConditionsWithPage(@Param("queryDTO") ReportDatasetQueryDTO queryDTO,
                                                   @Param("offset") Integer offset,
                                                   @Param("pageSize") Integer pageSize);

    /**
     * 统计符合条件的公共数据集数量
     *
     * @param queryDTO 查询条件
     * @return 符合条件的记录数
     */
    Long countByConditions(@Param("queryDTO") ReportDatasetQueryDTO queryDTO);
}
