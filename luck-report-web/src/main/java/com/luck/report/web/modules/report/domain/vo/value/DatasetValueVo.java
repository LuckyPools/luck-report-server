/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.domain.vo.value;

import com.luck.report.core.definition.Order;
import com.luck.report.core.definition.mapping.MappingItem;
import com.luck.report.core.definition.mapping.MappingType;
import com.luck.report.core.definition.value.AggregateType;
import com.luck.report.core.definition.value.GroupItem;
import com.luck.report.core.definition.value.ValueType;
import com.luck.report.web.modules.report.domain.vo.cell.ConditionVo;

import java.io.Serializable;
import java.util.List;

/**
 * DatasetValue的VO类，用于前端展示
 *
 * @author LuckyPools
 * @since 2026年
 */
public class DatasetValueVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String datasetName;
    private AggregateType aggregate;
    private String property;
    private List<GroupItem> groupItems;
    private MappingType mappingType;
    private String mappingDataset;
    private String mappingKeyProperty;
    private String mappingValueProperty;
    private List<MappingItem> mappingItems;
    private List<ConditionVo> conditions;
    private Order order;
    private String value;
    private ValueType type;

    /**
     * 默认无参构造器
     */
    public DatasetValueVo() {}

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public AggregateType getAggregate() {
        return aggregate;
    }

    public void setAggregate(AggregateType aggregate) {
        this.aggregate = aggregate;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public List<GroupItem> getGroupItems() {
        return groupItems;
    }

    public void setGroupItems(List<GroupItem> groupItems) {
        this.groupItems = groupItems;
    }

    public MappingType getMappingType() {
        return mappingType;
    }

    public void setMappingType(MappingType mappingType) {
        this.mappingType = mappingType;
    }

    public String getMappingDataset() {
        return mappingDataset;
    }

    public void setMappingDataset(String mappingDataset) {
        this.mappingDataset = mappingDataset;
    }

    public String getMappingKeyProperty() {
        return mappingKeyProperty;
    }

    public void setMappingKeyProperty(String mappingKeyProperty) {
        this.mappingKeyProperty = mappingKeyProperty;
    }

    public String getMappingValueProperty() {
        return mappingValueProperty;
    }

    public void setMappingValueProperty(String mappingValueProperty) {
        this.mappingValueProperty = mappingValueProperty;
    }

    public List<MappingItem> getMappingItems() {
        return mappingItems;
    }

    public void setMappingItems(List<MappingItem> mappingItems) {
        this.mappingItems = mappingItems;
    }

    public List<ConditionVo> getConditions() {
        return conditions;
    }

    public void setConditions(List<ConditionVo> conditions) {
        this.conditions = conditions;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ValueType getType() {
        return type;
    }

    public void setType(ValueType type) {
        this.type = type;
    }
}
