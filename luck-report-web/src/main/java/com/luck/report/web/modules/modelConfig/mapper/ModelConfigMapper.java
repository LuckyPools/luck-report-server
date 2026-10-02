package com.luck.report.web.modules.modelConfig.mapper;

import com.luck.report.web.modules.modelConfig.domain.dto.ModelConfigQueryDTO;
import com.luck.report.web.modules.modelConfig.domain.entity.ModelConfig;
import com.luck.report.jdbc.Param;

import java.util.List;

/**
 * 模型配置Mapper接口
 *
 * @author luck
 */
public interface ModelConfigMapper {

    /**
     * 按条件查询模型配置列表（非分页）；configName/modelName 精确匹配，其余条件可选
     *
     * @param queryDTO 查询条件
     * @return 模型配置列表,按排序字段升序排列
     */
    List<ModelConfig> selectList(@Param("queryDTO") ModelConfigQueryDTO queryDTO);

    /**
     * 根据ID查询模型配置
     *
     * @param id 配置ID
     * @return ModelConfig对象,不存在则返回null
     */
    ModelConfig selectModelConfigById(@Param("id") String id);

    /**
     * 插入新的模型配置
     *
     * @param modelConfig 模型配置实体
     * @return 影响的行数
     */
    int insert(ModelConfig modelConfig);

    /**
     * 更新模型配置
     *
     * @param modelConfig 模型配置实体
     * @return 影响的行数
     */
    int updateById(ModelConfig modelConfig);

    /**
     * 删除模型配置(逻辑删除)
     *
     * @param id 配置ID
     * @return 影响的行数
     */
    int deleteById(@Param("id") String id);

    /**
     * 分页条件查询模型配置
     *
     * @param queryDTO 查询条件
     * @param offset   偏移量
     * @return 模型配置列表
     */
    List<ModelConfig> selectPage(@Param("queryDTO") ModelConfigQueryDTO queryDTO,
                                                     @Param("offset") Integer offset,
                                                     @Param("pageSize") Integer pageSize);

    /**
     * 统计符合条件的模型配置数量
     *
     * @param queryDTO 查询条件
     * @return 符合条件的记录数
     */
    Long selectCount(@Param("queryDTO") ModelConfigQueryDTO queryDTO);
}
