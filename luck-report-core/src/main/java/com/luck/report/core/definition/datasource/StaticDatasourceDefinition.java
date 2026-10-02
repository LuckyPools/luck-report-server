package com.luck.report.core.definition.datasource;

import com.luck.report.core.build.Dataset;
import com.luck.report.core.definition.dataset.DatasetDefinition;
import com.luck.report.core.definition.dataset.JsonDatasetDefinition;
import com.luck.report.core.utils.JsonUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 静态数据源定义；静态数据源无需数据库连接，其数据集以 JSON 数组字符串形式存储在{@link JsonDatasetDefinition#getContent()} 中，取数时直接反序列化为 List<Map>。
 *
 * @author luck-report
 * @since 2.0.5
 */
public class StaticDatasourceDefinition implements DatasourceDefinition, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 数据源名称
     */
    private String name;

    /**
     * 数据集列表
     */
    private List<DatasetDefinition> datasets;

    /**
     * 默认无参构造器
     */
    public StaticDatasourceDefinition() {
    }

    /**
     * 构建静态数据集的运行时 Dataset 列表；静态数据源是唯一不需要 Connection 的数据源类型，直接把 JSON content；反序列化为 List<Map> 包装成 Dataset 返回。
     *
     * @param datasetDefs 数据集定义列表，可为空
     * @return 运行时 Dataset 列表；入参为空时返回空 List
     */
    /**
     * 构建全部静态数据集
     *
     * @param datasetDefs 数据集定义列表，可空
     * @return 运行时 Dataset 列表；入参为空时返回空 List
     */
    public List<Dataset> buildDatasets(List<DatasetDefinition> datasetDefs) {
        return buildDatasets(datasetDefs, null);
    }

    /**
     * 按引用名过滤后构建静态数据集
     *
     * @param datasetDefs 数据集定义列表，可空
     * @param allowedNames 允许构建的数据集名；null 表示不过滤
     * @return 运行时 Dataset 列表
     */
    public List<Dataset> buildDatasets(List<DatasetDefinition> datasetDefs, Set<String> allowedNames) {
        List<Dataset> list = new ArrayList<>();
        if (datasetDefs == null || datasetDefs.isEmpty()) {
            return list;
        }
        for (DatasetDefinition dsDef : datasetDefs) {
            if (allowedNames != null && !allowedNames.contains(dsDef.getName())) {
                continue;
            }
            JsonDatasetDefinition jsonDataset = (JsonDatasetDefinition) dsDef;
            List<Map<String, Object>> data = JsonUtils.fromJsonList(jsonDataset.getContent());
            list.add(new Dataset(jsonDataset.getName(), data));
        }
        return list;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 设置数据源名称
     *
     * @param name 数据源名称
     */
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<DatasetDefinition> getDatasets() {
        return datasets;
    }

    /**
     * 设置数据集列表
     *
     * @param datasets 数据集列表
     */
    public void setDatasets(List<DatasetDefinition> datasets) {
        this.datasets = datasets;
    }

    @Override
    public DatasourceType getType() {
        return DatasourceType.staticDs;
    }

    /**
     * 空实现，用于兼容 JSON 反序列化
     *
     * @param type 数据源类型
     */
    public void setType(DatasourceType type) {
    }
}
