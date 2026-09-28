package com.luck.report.web.modules.datasource.mapper;

import com.luck.report.jdbc.Param;
import com.luck.report.web.modules.datasource.domain.entity.LogicalRelation;

import java.util.List;

/**
 * 逻辑外键Mapper
 * 操作MySQL的luck_logical_relation表
 *
 * @author luck
 */
public interface LogicalRelationMapper {

    /**
     * 插入逻辑外键
     *
     * @param relation 逻辑外键实体
     * @return 影响行数
     */
    int insert(LogicalRelation relation);

    /**
     * 根据ID更新逻辑外键
     *
     * @param relation 逻辑外键实体
     * @return 影响行数
     */
    int updateById(LogicalRelation relation);

    /**
     * 根据ID查询逻辑外键
     *
     * @param id 逻辑外键ID
     * @return 逻辑外键实体
     */
    LogicalRelation selectById(@Param("id") String id);

    /**
     * 按数据源ID查询逻辑外键列表
     *
     * @param datasourceId 数据源ID
     * @return 逻辑外键列表
     */
    List<LogicalRelation> selectByDatasourceId(@Param("datasourceId") String datasourceId);

    /**
     * 按数据源ID列表批量查询逻辑外键
     *
     * @param datasourceIds 数据源ID列表，不可为空
     * @return 逻辑外键列表
     */
    List<LogicalRelation> selectByDatasourceIds(@Param("datasourceIds") List<String> datasourceIds);

    /**
     * 逻辑删除逻辑外键
     *
     * @param id 逻辑外键ID
     * @return 影响行数
     */
    int deleteById(@Param("id") String id);

    /**
     * 检查逻辑外键是否已存在
     *
     * @param datasourceId 数据源ID
     * @param sourceTableName 主表名
     * @param sourceColumnName 主表字段名
     * @param targetTableName 关联表名
     * @param targetColumnName 关联表字段名
     * @return 存在的记录数
     */
    int checkExists(@Param("datasourceId") String datasourceId,
                    @Param("sourceTableName") String sourceTableName,
                    @Param("sourceColumnName") String sourceColumnName,
                    @Param("targetTableName") String targetTableName,
                    @Param("targetColumnName") String targetColumnName);

    /**
     * 逻辑删除数据源下所有逻辑外键
     *
     * @param datasourceId 数据源ID
     * @return 影响行数
     */
    int deleteByDatasourceId(@Param("datasourceId") String datasourceId);
}
