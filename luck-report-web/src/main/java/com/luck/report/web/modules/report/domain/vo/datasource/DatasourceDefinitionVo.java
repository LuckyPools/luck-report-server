/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.domain.vo.datasource;

import com.luck.report.core.definition.datasource.DatasourceType;
import com.luck.report.web.modules.report.domain.vo.dataset.DatasetDefinitionVo;

import java.io.Serializable;
import java.util.List;

/**
 * 数据源定义 VO（聚合根）
 *
 * @author system
 * @since 2026年
 */
public class DatasourceDefinitionVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private DatasourceType type;
    private List<DatasetDefinitionVo> datasets;

    /**
     * 默认无参构造器
     */
    public DatasourceDefinitionVo() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DatasourceType getType() {
        return type;
    }

    public void setType(DatasourceType type) {
        this.type = type;
    }

    public List<DatasetDefinitionVo> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<DatasetDefinitionVo> datasets) {
        this.datasets = datasets;
    }
}
