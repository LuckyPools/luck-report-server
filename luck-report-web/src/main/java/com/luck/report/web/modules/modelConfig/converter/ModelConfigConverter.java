package com.luck.report.web.modules.modelConfig.converter;

import com.luck.report.web.modules.modelConfig.domain.dto.ModelConfigDTO;
import com.luck.report.web.modules.modelConfig.domain.entity.ModelConfig;
import com.luck.report.web.modules.modelConfig.domain.enums.ModelType;
import org.springframework.util.Assert;

import java.time.LocalDateTime;

public class ModelConfigConverter {

    public static ModelConfigDTO toDTO(ModelConfig entity) {
        if (entity == null) {
            return null;
        }
        return ModelConfigDTO.builder()
                .id(entity.getId())
                .provider(entity.getProvider())
                .baseUrl(entity.getBaseUrl())
                .modelName(entity.getModelName())
                .configName(entity.getConfigName())
                .sort(entity.getSort())
                .temperature(entity.getTemperature())
                .contextWindowTokens(entity.getContextWindowTokens())
                .enabled(entity.getEnabled())
                .apiKey(entity.getApiKey())
                .modelType(entity.getModelType().getCode())
                .apiPath(entity.getApiPath())
                .proxyEnabled(entity.getProxyEnabled())
                .proxyHost(entity.getProxyHost())
                .proxyPort(entity.getProxyPort())
                .proxyUsername(entity.getProxyUsername())
                .proxyPassword(entity.getProxyPassword())
                .build();
    }

    public static ModelConfig toEntity(ModelConfigDTO dto) {
        Assert.notNull(dto, "ModelConfigDTO不能为空");
        ModelConfig entity = new ModelConfig();
        entity.setId(dto.getId());
        entity.setProvider(dto.getProvider());
        entity.setBaseUrl(dto.getBaseUrl());
        entity.setApiKey(dto.getApiKey());
        entity.setModelName(dto.getModelName());
        entity.setConfigName(dto.getConfigName());
        entity.setSort(dto.getSort() != null ? dto.getSort() : 0);
        entity.setTemperature(dto.getTemperature());
        entity.setContextWindowTokens(dto.getContextWindowTokens());
        entity.setModelType(ModelType.fromCode(dto.getModelType()));
        entity.setApiPath(dto.getApiPath());
        entity.setProxyHost(dto.getProxyHost());
        entity.setProxyPort(dto.getProxyPort());
        entity.setProxyUsername(dto.getProxyUsername());
        entity.setProxyPassword(dto.getProxyPassword());
        entity.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : false);
        entity.setProxyEnabled(dto.getProxyEnabled() != null ? dto.getProxyEnabled() : false);
        entity.setDelFlag(0);
        entity.setCreateTime(LocalDateTime.now());
        entity.setUpdateTime(LocalDateTime.now());
        return entity;
    }
}
