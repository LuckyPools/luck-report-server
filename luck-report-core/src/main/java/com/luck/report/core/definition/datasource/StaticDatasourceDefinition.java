package com.luck.report.core.definition.datasource;

import com.luck.report.core.build.Dataset;
import com.luck.report.core.utils.StaticDatasetUtils;
import com.luck.report.core.definition.dataset.DatasetDefinition;
import com.luck.report.core.definition.dataset.JsonDatasetDefinition;
import com.luck.report.core.utils.JsonUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 静态数据源定义；数据集以 JSON 数组存于 content，取数时反序列化并可按查询参数内存过滤
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
     * 按引用名与查询参数构建静态数据集；parameters 中与 JSON 字段同名的非空参数用于内存过滤
     *
     * @param datasetDefs 数据集定义列表，可空
     * @param parameters 查询参数，可空
     * @param allowedNames 允许构建的数据集名；null 表示不过滤
     * @return 运行时 Dataset 列表
     */
    public List<Dataset> buildDatasets(List<DatasetDefinition> datasetDefs,
                                       Map<String, Object> parameters,
                                       Set<String> allowedNames) {
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
            data = StaticDatasetUtils.filter(data, parameters);
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
