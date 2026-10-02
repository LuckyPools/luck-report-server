package com.luck.report.web.modules.modelConfig.service;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.modules.modelConfig.domain.dto.ModelConfigDTO;
import com.luck.report.web.modules.modelConfig.domain.dto.ModelConfigQueryDTO;
import com.luck.report.web.modules.modelConfig.domain.entity.ModelConfig;
import com.luck.report.web.modules.modelConfig.domain.enums.ModelType;

import java.util.List;

/**
 * 模型配置数据服务接口
 *
 * @author luck
 */
public interface ModelConfigDataService {

    /**
     * 根据ID查询模型配置
     *
     * @param id 配置ID
     * @return ModelConfig实体对象,不存在则返回null
     */
    ModelConfig getById(String id);

    /**
     * 启用模型配置
     *
     * @param id 要启用的配置ID
     */
    void enable(String id);

    /**
     * 禁用模型配置
     *
     * @param id 要禁用的配置ID
     * @throws RuntimeException 当该类型只有一个启用的模型时抛出
     */
    void disable(String id);

    /**
     * 根据模型类型获取所有启用的配置列表
     *
     * @param modelType 模型类型
     * @return ModelConfigDTO列表
     */
    List<ModelConfigDTO> listEnabledByType(ModelType modelType);

    /**
     * 根据模型类型统计启用的配置数量
     *
     * @param modelType 模型类型
     * @return 启用的配置数量
     */
    int countEnabledByType(ModelType modelType);

    /**
     * 获取所有模型配置列表
     *
     * @return ModelConfigDTO列表
     */
    List<ModelConfigDTO> list();

    /**
     * 新增模型配置
     *
     * @param dto ModelConfigDTO对象
     */
    void create(ModelConfigDTO dto);

    /**
     * 更新模型配置到数据库(不处理热切换)
     *
     * @param dto ModelConfigDTO对象
     * @return 更新后的ModelConfig实体
     */
    ModelConfig update(ModelConfigDTO dto);

    /**
     * 删除模型配置
     *
     * @param id 配置ID
     */
    void removeById(String id);

    /**
     * 批量删除模型配置
     *
     * @param ids 配置ID列表
     */
    void removeByIds(List<String> ids);

    /**
     * 根据模型类型获取激活的配置
     *
     * @param modelType 模型类型
     * @return ModelConfigDTO对象,不存在则返回null
     */
    ModelConfigDTO getEnabledByType(ModelType modelType);

    /**
     * 根据模型ID获取对话模型配置（带缓存）
     *
     * @param modelId 模型配置ID，可为null
     * @return Index 对话模型配置
     * @throws RuntimeException 当找不到可用的对话模型时抛出
     */
    ModelConfig getChatConfig(String modelId);

    /**
     * 分页条件查询模型配置
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    PageResultVO<ModelConfigDTO> listPage(ModelConfigQueryDTO queryDTO);
}
