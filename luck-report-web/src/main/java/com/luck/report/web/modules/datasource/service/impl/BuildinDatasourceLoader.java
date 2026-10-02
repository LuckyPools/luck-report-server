package com.luck.report.web.modules.datasource.service.impl;

import com.luck.report.web.modules.datasource.domain.bo.DynamicBuildinDatasource;
import com.luck.report.web.modules.datasource.domain.entity.ReportDatasource;
import com.luck.report.web.modules.datasource.mapper.ReportDatasourceMapper;
import com.luck.report.core.definition.datasource.BuildinDatasource;
import com.luck.report.core.definition.datasource.BuildinDatasourceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 内置数据源加载器
 *
 * @author luck
 */
@Slf4j
@Component("bean.buildinDatasourceLoader")
public class BuildinDatasourceLoader implements SmartInitializingSingleton, BuildinDatasourceRegistry {

    /**
     * 数据源 Mapper
     */
    private final ReportDatasourceMapper datasourceMapper;

    /**
     * 动态数据源管理器
     */
    private final DynamicDatasourceManager dynamicDatasourceManager;

    /**
     * 内置数据源缓存：数据源名称 -> BuildinDatasource
     */
    private final Map<String, BuildinDatasource> buildinDatasourceMap = new ConcurrentHashMap<>();

    /**
     * 构造函数
     *
     * @param datasourceMapper 数据源 Mapper
     * @param dynamicDatasourceManager 动态数据源管理器
     */
    public BuildinDatasourceLoader(@Qualifier("bean.reportDatasourceMapper") ReportDatasourceMapper datasourceMapper,
                                   @Qualifier("bean.dynamicDatasourceManager") DynamicDatasourceManager dynamicDatasourceManager) {
        this.datasourceMapper = datasourceMapper;
        this.dynamicDatasourceManager = dynamicDatasourceManager;
    }

    /**
     * 所有单例 Bean 初始化完成后调用
     */
    @Override
    public void afterSingletonsInstantiated() {
        log.info("开始加载内置数据源...");

        try {
            List<ReportDatasource> datasources = datasourceMapper.selectByEnabled(Boolean.TRUE);

            if (datasources == null || datasources.isEmpty()) {
                log.warn("数据库中没有找到已启用的数据源配置");
                return;
            }

            int loadedCount = 0;
            for (ReportDatasource datasource : datasources) {
                try {
                    DynamicBuildinDatasource buildinDatasource = new DynamicBuildinDatasource(
                            datasource, dynamicDatasourceManager);
                    buildinDatasourceMap.put(datasource.getName(), buildinDatasource);
                    loadedCount++;
                    log.info("已加载内置数据源: id={}, name={}", datasource.getId(), datasource.getName());
                } catch (Exception e) {
                    log.error("加载内置数据源失败: id={}, name={}, error={}",
                            datasource.getId(), datasource.getName(), e.getMessage());
                }
            }

            log.info("内置数据源加载完成，共加载 {} 个（数据库中共 {} 个已启用数据源）", loadedCount, datasources.size());
        } catch (Exception e) {
            log.error("加载内置数据源失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 获取所有内置数据源
     *
     * @return 内置数据源集合
     */
    public Collection<BuildinDatasource> getBuildinDatasources() {
        return new ArrayList<>(buildinDatasourceMap.values());
    }

    /**
     * 根据名称获取内置数据源
     *
     * @param name 数据源名称
     * @return 内置数据源，不存在返回 null
     */
    public BuildinDatasource getBuildinDatasource(String name) {
        return buildinDatasourceMap.get(name);
    }

    /**
     * 根据名称获取内置数据源的 ID
     *
     * @param name 数据源名称
     * @return 数据源 ID，不存在返回 null
     */
    public String getBuildinDatasourceId(String name) {
        BuildinDatasource datasource = buildinDatasourceMap.get(name);
        return datasource != null ? datasource.getId() : null;
    }

    /**
     * 获取所有内置数据源的名称和 ID 映射
     *
     * @return Map<名称, ID>
     */
    public Map<String, String> getBuildinDatasourceIdMap() {
        Map<String, String> map = new ConcurrentHashMap<>();
        for (BuildinDatasource datasource : buildinDatasourceMap.values()) {
            String id = datasource.getId();
            if (id != null) {
                map.put(datasource.name(), id);
            }
        }
        return map;
    }

    /**
     * 刷新内置数据源缓存
     */
    public void refresh() {
        log.info("刷新内置数据源缓存...");
        buildinDatasourceMap.clear();
        afterSingletonsInstantiated();
    }

    /**
     * 添加或更新内置数据源
     *
     * @param datasource 数据源实体
     */
    public void addOrUpdateDatasource(ReportDatasource datasource) {
        if (Boolean.TRUE.equals(datasource.getEnabled())) {
            DynamicBuildinDatasource buildinDatasource = new DynamicBuildinDatasource(
                    datasource, dynamicDatasourceManager);
            buildinDatasourceMap.put(datasource.getName(), buildinDatasource);
            log.info("添加/更新内置数据源: id={}, name={}", datasource.getId(), datasource.getName());
        } else {
            removeDatasource(datasource.getName());
        }
    }

    /**
     * 移除内置数据源
     *
     * @param name 数据源名称
     */
    public void removeDatasource(String name) {
        BuildinDatasource removed = buildinDatasourceMap.remove(name);
        if (removed != null) {
            log.info("移除内置数据源: name={}", name);
        }
    }
}
