/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.domain.vo.dataset;

import com.luck.report.core.definition.dataset.Parameter;

import java.util.List;

/**
 * SQL 数据集定义 VO
 *
 * @author system
 * @since 2026年
 */
public class SqlDatasetDefinitionVo extends DatasetDefinitionVo {
    private static final long serialVersionUID = 1L;

    private String sql;
    private List<Parameter> parameters;

    /**
     * 默认无参构造器
     */
    public SqlDatasetDefinitionVo() {}

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public List<Parameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<Parameter> parameters) {
        this.parameters = parameters;
    }
}
