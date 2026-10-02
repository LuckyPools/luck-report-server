package com.luck.report.web.modules.datasource.mapper;

import com.luck.report.web.modules.datasource.domain.dto.ReportDatasourceQueryDTO;
import com.luck.report.web.modules.datasource.domain.entity.ReportDatasource;
import com.luck.report.jdbc.Param;

import java.util.List;

/**
 * 数据源Mapper
 *
 * @author luck
 */
public interface ReportDatasourceMapper {

    /**
     * 插入数据源
     *
     * @param reportDatasource 数据源实体
     * @return 影响行数
     */
    int insert(ReportDatasource reportDatasource);

    /**
     * 根据ID更新数据源
     *
     * @param reportDatasource 数据源实体
     * @return 影响行数
     */
    int updateById(ReportDatasource reportDatasource);

    /**
     * 根据ID查询数据源
     *
     * @param id 数据源ID
     * @return 数据源实体
     */
    ReportDatasource selectById(@Param("id") String id);

    /**
     * 查询所有数据源
     *
     * @return 数据源列表
     */
    List<ReportDatasource> selectAll();

    /**
     * 按启用状态查询数据源
     *
     * @param enabled 是否启用
     * @return 数据源列表
     */
    List<ReportDatasource> selectByEnabled(@Param("enabled") Boolean enabled);

    /**
     * 按类型查询数据源
     *
     * @param type 数据源类型
     * @return 数据源列表
     */
    List<ReportDatasource> selectByType(@Param("type") String type);

    /**
     * 根据ID删除数据源
     *
     * @param id 数据源ID
     * @return 影响行数
     */
    int deleteById(@Param("id") String id);

    /**
     * 更新连接测试状态
     *
     * @param id         数据源ID
     * @param testStatus 测试状态
     * @return 影响行数
     */
    int updateTestStatusById(@Param("id") String id, @Param("testStatus") String testStatus,
                             @Param("updateTime") java.time.LocalDateTime updateTime);

    /**
     * 更新数据源启用状态
     *
     * @param id     数据源ID
     * @param enabled 是否启用
     * @param updateTime 更新时间
     * @return 影响行数
     */
    int updateEnabledById(@Param("id") String id, @Param("enabled") Boolean enabled,
                          @Param("updateTime") java.time.LocalDateTime updateTime);

    /**
     * 根据名称查询数据源
     *
     * @param name 数据源名称
     * @return 数据源实体，不存在则返回null
     */
    ReportDatasource selectByName(@Param("name") String name);

    /**
     * 更新已初始化的表名列表
     *
     * @param id                数据源ID
     * @param initializedTables 已初始化的表名列表（JSON格式）
     * @param updateTime        更新时间
     * @return 影响行数
     */
    int updateInitializedTables(@Param("id") String id, @Param("initializedTables") String initializedTables,
                                @Param("updateTime") java.time.LocalDateTime updateTime);

    /**
     * 根据ID列表批量查询数据源
     *
     * @param ids 数据源ID列表，不可为空
     * @return 数据源列表
     */
    List<ReportDatasource> selectByIds(@Param("ids") List<String> ids);

    /**
     * 分页条件查询数据源
     *
     * @param queryDTO 查询条件
     * @param offset   偏移量
     * @return 数据源列表
     */
    List<ReportDatasource> selectByConditionsWithPage(@Param("queryDTO") ReportDatasourceQueryDTO queryDTO,
                                                      @Param("offset") Integer offset,
                                                      @Param("pageSize") Integer pageSize);

    /**
     * 统计符合条件的数据源数量
     *
     * @param queryDTO 查询条件
     * @return 符合条件的记录数
     */
    Long countByConditions(@Param("queryDTO") ReportDatasourceQueryDTO queryDTO);
}
