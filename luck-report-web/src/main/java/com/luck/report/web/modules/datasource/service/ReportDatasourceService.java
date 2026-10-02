package com.luck.report.web.modules.datasource.service;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.modules.datasource.domain.dto.ReportDatasourceQueryDTO;
import com.luck.report.web.modules.datasource.domain.dto.SchemaDTO;
import com.luck.report.web.modules.datasource.domain.entity.ReportDatasource;
import com.luck.report.web.modules.datasource.domain.entity.LogicalRelation;
import com.luck.report.web.modules.datasource.domain.vo.ReportDatasourceVO;
import com.luck.report.web.modules.datasource.domain.vo.SchemaSearchResultVO;

import java.util.List;

/**
 * 数据源服务接口
 *
 * @author luck
 */
public interface ReportDatasourceService {

    /**
     * 获取所有数据源列表
     *
     * @return 数据源VO列表
     */
    List<ReportDatasourceVO> getAllDatasource();

    /**
     * 按启用状态查询数据源列表
     *
     * @param enabled 是否启用
     * @return 数据源VO列表
     */
    List<ReportDatasourceVO> getDatasourceByEnabled(Boolean enabled);

    /**
     * 按类型查询数据源列表
     *
     * @param type 数据源类型
     * @return 数据源VO列表
     */
    List<ReportDatasourceVO> getDatasourceByType(String type);

    /**
     * 根据ID获取数据源详情
     *
     * @param id 数据源ID
     * @return 数据源VO
     */
    ReportDatasourceVO getDatasourceById(String id);

    /**
     * 创建数据源
     *
     * @param reportDatasource 数据源实体
     * @return 创建后的数据源VO
     */
    ReportDatasourceVO createDatasource(ReportDatasource reportDatasource);

    /**
     * 更新数据源
     *
     * @param id         数据源ID
     * @param reportDatasource 数据源实体
     * @return 更新后的数据源VO
     */
    ReportDatasourceVO updateDatasource(String id, ReportDatasource reportDatasource);

    /**
     * 删除数据源
     *
     * @param id 数据源ID
     */
    void deleteDatasource(String id);

    /**
     * 批量删除数据源
     *
     * @param ids 数据源ID列表
     */
    void deleteDatasourceBatch(List<String> ids);

    /**
     * 测试数据源连接
     *
     * @param id 数据源ID
     * @return 连接是否成功
     */
    boolean testConnection(String id);

    /**
     * 获取数据源的表列表
     *
     * @param id 数据源ID
     * @return 表名列表
     * @throws Exception 数据库访问异常
     */
    List<String> getDatasourceTables(String id) throws Exception;

    /**
     * 获取表的字段列表
     *
     * @param id        数据源ID
     * @param tableName 表名
     * @return 字段名列表
     * @throws Exception 数据库访问异常
     */
    List<String> getTableColumns(String id, String tableName) throws Exception;

    /**
     * 初始化表Schema到向量数据库
     *
     * @param id      数据源ID
     * @param tables  需要初始化的表名列表
     * @param modelId 嵌入模型配置ID，为null时使用默认嵌入模型
     * @throws Exception 数据库访问或向量化异常
     */
    void initTableSchema(String id, List<String> tables, String modelId) throws Exception;

    /**
     * 更新数据源启用状态
     *
     * @param id     数据源ID
     * @param enabled 是否启用
     */
    void updateEnabledStatus(String id, Boolean enabled);

    /**
     * 获取数据源的逻辑外键列表
     *
     * @param datasourceId 数据源ID
     * @return 逻辑外键列表
     */
    List<LogicalRelation> getLogicalRelations(String datasourceId);

    /**
     * 添加逻辑外键
     *
     * @param datasourceId    数据源ID
     * @param logicalRelation 逻辑外键实体
     * @return 添加后的逻辑外键
     */
    LogicalRelation addLogicalRelation(String datasourceId, LogicalRelation logicalRelation);

    /**
     * 更新逻辑外键
     *
     * @param datasourceId    数据源ID
     * @param relationId      逻辑外键ID
     * @param logicalRelation 逻辑外键实体
     * @return 更新后的逻辑外键
     */
    LogicalRelation updateLogicalRelation(String datasourceId, String relationId, LogicalRelation logicalRelation);

    /**
     * 删除逻辑外键
     *
     * @param datasourceId   数据源ID
     * @param relationId     逻辑外键ID
     */
    void deleteLogicalRelation(String datasourceId, String relationId);

    /**
     * 批量保存逻辑外键（替换现有的所有外键）
     *
     * @param datasourceId     数据源ID
     * @param logicalRelations 逻辑外键列表
     * @return 保存后的逻辑外键列表
     */
    List<LogicalRelation> saveLogicalRelations(String datasourceId, List<LogicalRelation> logicalRelations);

    /**
     * 构建SchemaDTO
     *
     * @param datasourceId 数据源ID
     * @param query        用户自然语言查询
     * @return SchemaDTO（包含表结构 + 外键关系）
     */
    SchemaDTO buildSchemaDTO(String datasourceId, String query);

    /**
     * 获取与查询相关的表结构信息（含表/字段/外键）
     *
     * @param datasourceId 数据源ID
     * @param query        用户自然语言查询
     * @return SchemaDTO 结构化数据（包含表结构、字段、外键关系）
     */
    SchemaDTO getTableRelations(String datasourceId, String query);

    /**
     * 根据名称获取数据源
     *
     * @param name 数据源名称
     * @return 数据源VO，不存在则返回null
     */
    ReportDatasourceVO getDatasourceByName(String name);

    /**
     * 分页条件查询数据源
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResultVO<ReportDatasourceVO> queryByPage(ReportDatasourceQueryDTO queryDTO);

    /**
     * 跨数据源搜索Schema
     *
     * @param query 用户自然语言查询
     * @return 搜索结果列表，按相关度排序
     */
    List<SchemaSearchResultVO> searchSchema(String query);
}
