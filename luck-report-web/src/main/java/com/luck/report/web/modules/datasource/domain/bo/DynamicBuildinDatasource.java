package com.luck.report.web.modules.datasource.domain.bo;

import com.luck.report.core.definition.datasource.BuildinDatasource;
import com.luck.report.web.modules.datasource.domain.entity.ReportDatasource;
import com.luck.report.web.modules.datasource.service.impl.DynamicDatasourceManager;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 动态内置数据源实现
 *
 * @author luck
 */
@Slf4j
public class DynamicBuildinDatasource implements BuildinDatasource {

    /**
     * 数据源实体（包含完整配置信息）
     */
    private final ReportDatasource datasource;

    /**
     * 动态数据源管理器（用于获取连接池）
     */
    private final DynamicDatasourceManager dynamicDatasourceManager;

    /**
     * 构造函数
     *
     * @param datasource 数据源实体，包含ID、名称、连接配置等信息
     * @param dynamicDatasourceManager 动态数据源管理器，用于管理连接池
     */
    public DynamicBuildinDatasource(ReportDatasource datasource, DynamicDatasourceManager dynamicDatasourceManager) {
        this.datasource = datasource;
        this.dynamicDatasourceManager = dynamicDatasourceManager;
    }

    /**
     * 返回数据源名称
     *
     * @return 数据源名称
     */
    @Override
    public String name() {
        return datasource.getName();
    }

    /**
     * 返回数据源连接
     *
     * @return 数据库连接，获取失败返回null
     */
    @Override
    public Connection getConnection() {
        try {
            HikariDataSource dataSourcePool = dynamicDatasourceManager.getOrCreateDatasourcePool(datasource);
            return dataSourcePool.getConnection();
        } catch (SQLException e) {
            log.error("数据源[{}]获取连接失败: {}", datasource.getName(), e.getMessage(), e);
            return null;
        }
    }

    /**
     * 返回数据源ID
     *
     * @return 数据源ID（String类型）
     */
    @Override
    public String getId() {
        return datasource.getId();
    }
}
