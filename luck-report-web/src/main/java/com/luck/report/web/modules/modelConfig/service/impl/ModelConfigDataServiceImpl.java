package com.luck.report.web.modules.modelConfig.service.impl;

import com.luck.report.core.exception.ReportBizException;
import com.luck.report.core.security.SensitiveConfigCipher;
import com.luck.report.web.utils.SnowflakeIdGenerator;
import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.modules.modelConfig.domain.dto.ModelConfigDTO;
import com.luck.report.web.modules.modelConfig.domain.dto.ModelConfigQueryDTO;
import com.luck.report.web.modules.modelConfig.domain.entity.ModelConfig;
import com.luck.report.web.modules.modelConfig.domain.enums.ModelType;
import com.luck.report.web.modules.modelConfig.converter.ModelConfigConverter;
import com.luck.report.web.modules.modelConfig.mapper.ModelConfigMapper;
import com.luck.report.web.modules.modelConfig.service.ModelConfigDataService;
import com.luck.report.infra.modules.cache.utils.CacheUtils;
import com.luck.report.web.security.utils.SecurityUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 模型配置数据服务实现类
 *
 * @author luck
 */
@Slf4j
@Service("bean.modelConfigDataService")
@AllArgsConstructor
public class ModelConfigDataServiceImpl implements ModelConfigDataService {

    /**
     * 缓存键前缀：模型配置
     */
    private static final String MODEL_CONFIG_PREFIX = "luck-report:model-config:";

    /**
     * 缓存键：所有激活的对话模型列表
     */
    private static final String ACTIVE_CHAT_MODELS_KEY = MODEL_CONFIG_PREFIX + "enabled-chat-models";

    /**
     * 缓存键：所有激活的嵌入模型列表
     */
    private static final String ACTIVE_EMBEDDING_MODELS_KEY = MODEL_CONFIG_PREFIX + "enabled-embedding-models";

    /**
     * 缓存键：所有激活的重排序模型列表
     */
    private static final String ACTIVE_RERANK_MODELS_KEY = MODEL_CONFIG_PREFIX + "enabled-rerank-models";

    /**
     * 缓存键：单个模型配置（后缀为模型ID）
     */
    private static final String MODEL_BY_ID_PREFIX = MODEL_CONFIG_PREFIX + "id:";

    @Qualifier("bean.modelConfigMapper")
    private final ModelConfigMapper modelConfigMapper;

    /**
     * 清除所有模型配置相关的缓存
     */
    private void clearModelConfigCache() {
        Set<String> keys = CacheUtils.keys(MODEL_CONFIG_PREFIX);
        if (keys != null) {
            keys.forEach(CacheUtils::remove);
        }
        log.info("已清空所有模型配置缓存");
    }

    /**
     * 根据ID查询模型配置
     *
     * @param id 配置ID
     * @return ModelConfig实体对象,不存在则返回null
     */
    @Override
    public ModelConfig getById(String id) {
        return modelConfigMapper.selectModelConfigById(id);
    }

    /**
     * 启用模型配置
     *
     * @param id 要启用的配置ID
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void enable(String id) {
        ModelConfig entity = modelConfigMapper.selectModelConfigById(id);
        if (entity == null) {
            throw new ReportBizException("error.model.configNotFound");
        }

        entity.setEnabled(true);
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(SecurityUtils.getCurrentUserId());
        modelConfigMapper.updateById(entity);

        clearModelConfigCache();
        log.info("已启用模型配置: id={}, modelName={}", id, entity.getModelName());
    }

    /**
     * 禁用模型配置
     *
     * @param id 要禁用的配置ID
     * @throws RuntimeException 当该类型只有一个启用的模型时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void disable(String id) {
        ModelConfig entity = modelConfigMapper.selectModelConfigById(id);
        if (entity == null) {
            throw new ReportBizException("error.model.configNotFound");
        }

        if (entity.getModelType() != ModelType.RERANK) {
            int enabledCount = countEnabledByType(entity.getModelType().getCode());
            if (enabledCount <= 1 && Boolean.TRUE.equals(entity.getEnabled())) {
                throw new ReportBizException("error.model.lastEnabled");
            }
        }

        entity.setEnabled(false);
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(SecurityUtils.getCurrentUserId());
        modelConfigMapper.updateById(entity);

        clearModelConfigCache();
        log.info("已禁用模型配置: id={}, modelName={}", id, entity.getModelName());
    }

    /**
     * 根据模型类型获取所有启用的配置列表
     *
     * @param modelType 模型类型
     * @return ModelConfigDTO列表
     */
    @Override
    public List<ModelConfigDTO> listEnabledByType(ModelType modelType) {
        String cacheKey = cacheKeyForType(modelType);

        List<ModelConfigDTO> cachedList = CacheUtils.get(cacheKey);
        if (cachedList != null) {
            log.debug("从缓存获取启用模型列表: modelType={}", modelType);
            return cachedList;
        }

        List<ModelConfig> entities = modelConfigMapper.selectList(
                ModelConfigQueryDTO.builder().modelType(modelType.getCode()).enabled(Boolean.TRUE).build());
        List<ModelConfigDTO> dtoList = entities.stream()
                .map(ModelConfigConverter::toDTO)
                .collect(Collectors.toList());

        CacheUtils.put(cacheKey, dtoList);
        log.info("从数据库加载启用模型列表并缓存: modelType={}, count={}", modelType, dtoList.size());

        return dtoList;
    }

    /**
     * 根据模型类型统计激活的配置数量
     *
     * @param modelType 模型类型
     * @return 激活的配置数量
     */
    @Override
    public int countEnabledByType(ModelType modelType) {
        return countEnabledByType(modelType.getCode());
    }

    /**
     * 获取所有模型配置列表
     *
     * @return ModelConfigDTO列表
     */
    @Override
    public List<ModelConfigDTO> list() {
        return modelConfigMapper.selectList(new ModelConfigQueryDTO()).stream()
                .map(ModelConfigConverter::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * 新增模型配置
     *
     * @param dto ModelConfigDTO对象
     */
    @Override
    public void create(ModelConfigDTO dto) {
        clean(dto);
        ModelConfig entity = ModelConfigConverter.toEntity(dto);
        entity.setId(SnowflakeIdGenerator.generateId());
        if (entity.getApiKey() != null && !entity.getApiKey().isEmpty()) {
            entity.setApiKey(SensitiveConfigCipher.encrypt(entity.getApiKey()));
        }
        String userId = SecurityUtils.getCurrentUserId();
        entity.setCreateBy(userId);
        entity.setUpdateBy(userId);
        modelConfigMapper.insert(entity);
        clearModelConfigCache();
        log.info("新增模型配置: id={}, modelName={}", entity.getId(), dto.getModelName());
    }

    /**
     * 清理DTO中的字符串字段
     *
     * @param dto ModelConfigDTO对象
     */
    private void clean(ModelConfigDTO dto) {
        dto.setModelName(dto.getModelName().trim());
        dto.setBaseUrl(dto.getBaseUrl().trim());
        if (dto.getApiKey() != null) {
            dto.setApiKey(dto.getApiKey().trim());
        }
        if (dto.getApiPath() != null) {
            dto.setApiPath(dto.getApiPath().trim());
        }
    }

    /**
     * 更新模型配置到数据库(不处理热切换)
     *
     * @param dto ModelConfigDTO对象
     * @return 更新后的ModelConfig实体
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ModelConfig update(ModelConfigDTO dto) {
        clean(dto);
        ModelConfig entity = modelConfigMapper.selectModelConfigById(dto.getId());
        if (entity == null) {
            throw new ReportBizException("error.model.configNotFound");
        }

        if (!entity.getModelType().getCode().equals(dto.getModelType())) {
            throw new ReportBizException("error.model.typeImmutable");
        }

        mergeDtoToEntity(dto, entity);
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(SecurityUtils.getCurrentUserId());

        modelConfigMapper.updateById(entity);

        clearModelConfigCache();
        log.info("更新模型配置: id={}, modelName={}", dto.getId(), dto.getModelName());

        return entity;
    }

    /**
     * 将DTO字段合并到Entity
     *
     * @param dto ModelConfigDTO对象
     * @param oldEntity 已存在的ModelConfig实体
     */
    private static void mergeDtoToEntity(ModelConfigDTO dto, ModelConfig oldEntity) {
        oldEntity.setProvider(dto.getProvider());
        oldEntity.setBaseUrl(dto.getBaseUrl());
        oldEntity.setModelName(dto.getModelName());
        oldEntity.setConfigName(dto.getConfigName());
        oldEntity.setSort(dto.getSort());
        oldEntity.setTemperature(dto.getTemperature());
        oldEntity.setContextWindowTokens(dto.getContextWindowTokens());
        oldEntity.setApiPath(dto.getApiPath());
        oldEntity.setUpdateTime(LocalDateTime.now());
        oldEntity.setProxyEnabled(dto.getProxyEnabled() != null ? dto.getProxyEnabled() : false);
        oldEntity.setProxyHost(dto.getProxyHost());
        oldEntity.setProxyPort(dto.getProxyPort());
        oldEntity.setProxyUsername(dto.getProxyUsername());
        oldEntity.setProxyPassword(dto.getProxyPassword());

        if (dto.getApiKey() != null && !dto.getApiKey().isEmpty()) {
            oldEntity.setApiKey(SensitiveConfigCipher.encrypt(dto.getApiKey()));
        }
    }

    /**
     * 删除模型配置
     *
     * @param id 配置ID
     */
    @Override
    public void removeById(String id) {
        ModelConfig entity = modelConfigMapper.selectModelConfigById(id);
        if (entity == null) {
            throw new ReportBizException("error.model.configNotFound");
        }

        if (Boolean.TRUE.equals(entity.getEnabled())) {
            int enabledCount = countEnabledByType(entity.getModelType().getCode());
            if (enabledCount <= 1) {
                throw new ReportBizException("error.model.lastEnabledDelete");
            }
        }

        entity.setDelFlag(1);
        entity.setUpdateTime(LocalDateTime.now());
        entity.setUpdateBy(SecurityUtils.getCurrentUserId());
        int updated = modelConfigMapper.updateById(entity);
        if (updated == 0) {
            throw new ReportBizException("error.model.deleteFailed");
        }

        clearModelConfigCache();
        log.info("删除模型配置: id={}, modelName={}", id, entity.getModelName());
    }

    @Override
    public void removeByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        for (String id : ids) {
            if (id != null && !id.isEmpty()) {
                removeById(id);
            }
        }
    }

    /**
     * 根据模型类型获取激活的配置
     *
     * @param modelType 模型类型
     * @return ModelConfigDTO对象,不存在则返回null
     */
    @Override
    public ModelConfigDTO getEnabledByType(ModelType modelType) {
        List<ModelConfigDTO> enabledConfigs = listEnabledByType(modelType);
        if (enabledConfigs == null || enabledConfigs.isEmpty()) {
            log.warn("未找到类型[{}]的激活模型配置", modelType);
            return null;
        }
        return enabledConfigs.get(0);
    }

    /**
     * 根据模型ID获取对话模型配置（带缓存）
     *
     * @param modelId 模型配置ID，可为null
     * @return Index 对话模型配置
     * @throws RuntimeException 当找不到可用的对话模型时抛出
     */
    @Override
    public ModelConfig getChatConfig(String modelId) {
        if (modelId != null) {
            String cacheKey = MODEL_BY_ID_PREFIX + modelId;
            ModelConfig cachedConfig = CacheUtils.get(cacheKey, ModelConfig.class);
            if (cachedConfig != null) {
                log.debug("从缓存获取模型配置: id={}", modelId);
                return cachedConfig;
            }

            ModelConfig config = modelConfigMapper.selectModelConfigById(modelId);
            if (config == null) {
                throw new ReportBizException("error.model.configNotFoundId", modelId);
            }

            CacheUtils.put(cacheKey, config);
            log.debug("从数据库加载模型配置并缓存: id={}, modelName={}", modelId, config.getModelName());

            if (!Boolean.TRUE.equals(config.getEnabled())) {
                log.warn("模型配置未启用: ID={}, modelName={}", modelId, config.getModelName());
            }
            return config;
        }

        ModelConfigDTO dto = getEnabledByType(ModelType.CHAT);
        if (dto == null) {
            throw new ReportBizException("error.model.noChatModel");
        }

        log.info("使用默认对话模型: id={}, modelName={}", dto.getId(), dto.getModelName());
        return ModelConfigConverter.toEntity(dto);
    }

    /**
     * 分页条件查询模型配置
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @Override
    public PageResultVO<ModelConfigDTO> listPage(ModelConfigQueryDTO queryDTO) {
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();

        Long total = modelConfigMapper.selectCount(queryDTO);

        List<ModelConfig> dataList = modelConfigMapper.selectPage(queryDTO, offset, queryDTO.getPageSize());
        List<ModelConfigDTO> dataListDTO = dataList.stream()
                .map(ModelConfigConverter::toDTO)
                .collect(Collectors.toList());

        return PageResultVO.success(dataListDTO, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    private static String cacheKeyForType(ModelType modelType) {
        if (modelType == ModelType.CHAT) {
            return ACTIVE_CHAT_MODELS_KEY;
        }
        if (modelType == ModelType.EMBEDDING) {
            return ACTIVE_EMBEDDING_MODELS_KEY;
        }
        if (modelType == ModelType.RERANK) {
            return ACTIVE_RERANK_MODELS_KEY;
        }
        throw new ReportBizException("error.enum.unknownModelType", modelType);
    }

    /**
     * 按模型类型统计启用中的配置数量
     *
     * @param modelTypeCode 模型类型编码
     * @return 启用数量
     */
    private int countEnabledByType(String modelTypeCode) {
        Long count = modelConfigMapper.selectCount(
                ModelConfigQueryDTO.builder().modelType(modelTypeCode).enabled(Boolean.TRUE).build());
        return count == null ? 0 : count.intValue();
    }
}
