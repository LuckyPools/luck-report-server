package com.luck.report.web.modules.report.domain.vo.dataset;

import com.luck.report.core.definition.dataset.Field;

import java.io.Serializable;
import java.util.List;

/**
 * 数据集定义 VO（聚合根）
 *
 * @author system
 * @since 2026年
 */
public class DatasetDefinitionVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<Field> fields;

    /**
     * 默认无参构造器
     */
    public DatasetDefinitionVo() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Field> getFields() {
        return fields;
    }

    public void setFields(List<Field> fields) {
        this.fields = fields;
    }
}
