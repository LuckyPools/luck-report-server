/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.converter;

import com.luck.report.core.definition.CellDefinition;
import com.luck.report.core.definition.ConditionPropertyItem;
import com.luck.report.core.definition.LinkParameter;
import com.luck.report.core.definition.datasource.BuildinDatasourceDefinition;
import com.luck.report.core.definition.datasource.DatasourceDefinition;
import com.luck.report.core.definition.datasource.JdbcDatasourceDefinition;
import com.luck.report.core.definition.datasource.SpringBeanDatasourceDefinition;
import com.luck.report.core.definition.datasource.StaticDatasourceDefinition;
import com.luck.report.core.definition.dataset.BeanDatasetDefinition;
import com.luck.report.core.definition.dataset.DatasetDefinition;
import com.luck.report.core.definition.dataset.JsonDatasetDefinition;
import com.luck.report.core.definition.dataset.SqlDatasetDefinition;
import com.luck.report.core.definition.value.*;
import com.luck.report.core.expression.model.Condition;
import com.luck.report.core.expression.model.condition.BaseCondition;
import com.luck.report.web.modules.report.domain.vo.dataset.BeanDatasetDefinitionVo;
import com.luck.report.web.modules.report.domain.vo.datasource.BuildinDatasourceDefinitionVo;
import com.luck.report.web.modules.report.domain.vo.cell.CellDefinitionVo;
import com.luck.report.web.modules.report.domain.vo.cell.ConditionPropertyItemVo;
import com.luck.report.web.modules.report.domain.vo.cell.ConditionVo;
import com.luck.report.web.modules.report.domain.vo.dataset.JsonDatasetDefinitionVo;
import com.luck.report.web.modules.report.domain.vo.datasource.DatasourceDefinitionVo;
import com.luck.report.web.modules.report.domain.vo.dataset.DatasetDefinitionVo;
import com.luck.report.web.modules.report.domain.vo.datasource.JdbcDatasourceDefinitionVo;
import com.luck.report.web.modules.report.domain.vo.cell.LinkParameterVo;
import com.luck.report.web.modules.report.domain.vo.datasource.SpringBeanDatasourceDefinitionVo;
import com.luck.report.web.modules.report.domain.vo.dataset.SqlDatasetDefinitionVo;
import com.luck.report.web.modules.report.domain.vo.value.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * VO转换工具类
 *
 * @author system
 * @since 2024年
 */
public final class DefinitionVoConverter {

    private static final Logger logger = LoggerFactory.getLogger(DefinitionVoConverter.class);

    private DefinitionVoConverter() {
    }

    /**
     * 转换 CellDefinition 为 CellDefinitionVo
     * @param cell 单元格定义
     * @return 单元格VO
     */
    public static CellDefinitionVo toVo(CellDefinition cell) {
        if (cell == null) {
            return null;
        }
        CellDefinitionVo vo = new CellDefinitionVo();
        BeanUtils.copyProperties(cell, vo, "value", "linkParameters", "conditionPropertyItems");
        vo.setValue(toVo(cell.getValue()));
        vo.setLinkParameters(toLinkParameterVoList(cell.getLinkParameters()));
        vo.setConditionPropertyItems(toConditionPropertyItemVoList(cell.getConditionPropertyItems()));
        return vo;
    }

    /**
     * 转换 Value 为对应的 VO 对象
     * @param value 值对象
     * @return VO对象
     */
    public static Object toVo(Value value) {
        if (value == null) {
            return null;
        }

        if (value instanceof SimpleValue) {
            SimpleValue sv = (SimpleValue) value;
            ValueVo vo = new ValueVo();
            vo.setValue(sv.getValue());
            vo.setType(sv.getType());
            return vo;
        } else if (value instanceof ExpressionValue) {
            ExpressionValue ev = (ExpressionValue) value;
            ExpressionValueVo vo = new ExpressionValueVo();
            vo.setValue(ev.getValue());
            vo.setType(ev.getType());
            return vo;
        } else if (value instanceof DatasetValue) {
            DatasetValue dv = (DatasetValue) value;
            DatasetValueVo vo = new DatasetValueVo();
            BeanUtils.copyProperties(dv, vo, "conditions", "condition", "mapping");
            vo.setValue(dv.getValue());
            vo.setType(dv.getType());
            vo.setConditions(toConditionVoList(dv.getConditions()));
            return vo;
        } else if (value instanceof ImageValue) {
            ImageValue iv = (ImageValue) value;
            ImageValueVo vo = new ImageValueVo();
            BeanUtils.copyProperties(iv, vo, "expression", "path", "expr");
            vo.setValue(iv.getValue());
            vo.setType(iv.getType());
            return vo;
        } else if (value instanceof ChartValue) {
            ChartValue cv = (ChartValue) value;
            ChartValueVo vo = new ChartValueVo();
            vo.setChart(cv.getChart());
            vo.setValue(cv.getValue());
            vo.setType(cv.getType());
            return vo;
        } else if (value instanceof SlashValue) {
            SlashValue sv = (SlashValue) value;
            SlashValueVo vo = new SlashValueVo();
            vo.setSlashes(sv.getSlashes());
            vo.setValue(sv.getValue());
            vo.setType(sv.getType());
            return vo;
        } else if (value instanceof ZxingValue) {
            ZxingValue zv = (ZxingValue) value;
            ZxingValueVo vo = new ZxingValueVo();
            BeanUtils.copyProperties(zv, vo, "expression", "codeDisplay", "text", "expr");
            vo.setValue(zv.getValue());
            vo.setType(zv.getType());
            return vo;
        }

        ValueVo vo = new ValueVo();
        vo.setValue(value.getValue());
        vo.setType(value.getType());
        return vo;
    }

    /**
     * 转换 LinkParameter 列表为 LinkParameterVo 列表
     * @param linkParameters 链接参数列表
     * @return VO列表
     */
    public static List<LinkParameterVo> toLinkParameterVoList(List<LinkParameter> linkParameters) {
        if (linkParameters == null || linkParameters.isEmpty()) {
            return null;
        }
        List<LinkParameterVo> voList = new ArrayList<>(linkParameters.size());
        for (LinkParameter param : linkParameters) {
            voList.add(toVo(param));
        }
        return voList;
    }

    /**
     * 转换 LinkParameter 为 LinkParameterVo
     * @param linkParameter 链接参数
     * @return VO对象
     */
    public static LinkParameterVo toVo(LinkParameter linkParameter) {
        if (linkParameter == null) {
            return null;
        }
        LinkParameterVo vo = new LinkParameterVo();
        BeanUtils.copyProperties(linkParameter, vo, "valueExpression");
        return vo;
    }

    /**
     * 转换 ConditionPropertyItem 列表为 ConditionPropertyItemVo 列表
     * @param items 条件属性项列表
     * @return VO列表
     */
    public static List<ConditionPropertyItemVo> toConditionPropertyItemVoList(List<ConditionPropertyItem> items) {
        if (items == null || items.isEmpty()) {
            return null;
        }
        List<ConditionPropertyItemVo> voList = new ArrayList<>(items.size());
        for (ConditionPropertyItem item : items) {
            voList.add(toVo(item));
        }
        return voList;
    }

    /**
     * 转换 ConditionPropertyItem 为 ConditionPropertyItemVo
     * @param item 条件属性项
     * @return VO对象
     */
    public static ConditionPropertyItemVo toVo(ConditionPropertyItem item) {
        if (item == null) {
            return null;
        }
        ConditionPropertyItemVo vo = new ConditionPropertyItemVo();
        BeanUtils.copyProperties(item, vo, "conditions", "condition", "expression", "linkParameters");
        vo.setConditions(toConditionVoList(item.getConditions()));
        vo.setLinkParameters(toLinkParameterVoList(item.getLinkParameters()));
        return vo;
    }

    /**
     * 转换 Condition 列表为 ConditionVo 列表
     * @param conditions 条件列表
     * @return VO列表
     */
    public static List<ConditionVo> toConditionVoList(List<Condition> conditions) {
        if (conditions == null || conditions.isEmpty()) {
            return null;
        }
        List<ConditionVo> voList = new ArrayList<>(conditions.size());
        for (Condition condition : conditions) {
            voList.add(toVo(condition));
        }
        return voList;
    }

    /**
     * 转换 Condition 为 ConditionVo
     * @param condition 条件
     * @return VO对象
     */
    public static ConditionVo toVo(Condition condition) {
        if (condition == null) {
            return null;
        }
        ConditionVo vo = new ConditionVo();
        if (condition instanceof BaseCondition) {
            BaseCondition bc = (BaseCondition) condition;
            String operation = bc.getOperation();
            if (operation == null || operation.isEmpty()) {
                if (bc.getOp() != null) {
                    operation = bc.getOp().name();
                }
            }
            vo.setOperation(operation);
            if (bc.getJoin() != null) {
                vo.setJoin(bc.getJoin().name());
            }
            vo.setLeft(bc.getLeft());
            vo.setRight(bc.getRight());
            vo.setType(bc.getType());
        }
        return vo;
    }

    /**
     * 转换 SqlDatasetDefinition 为 SqlDatasetDefinitionVo
     *
     * @param dataset SQL 数据集定义
     * @return VO 对象；入参为 null 时返回 null
     */
    public static SqlDatasetDefinitionVo toVo(SqlDatasetDefinition dataset) {
        if (dataset == null) {
            return null;
        }
        SqlDatasetDefinitionVo vo = new SqlDatasetDefinitionVo();
        vo.setName(dataset.getName());
        vo.setFields(dataset.getFields());
        vo.setSql(dataset.getSql());
        vo.setParameters(dataset.getParameters());
        return vo;
    }

    /**
     * 转换 BeanDatasetDefinition 为 BeanDatasetDefinitionVo
     * @param dataset Bean 数据集定义
     * @return VO 对象；入参为 null 时返回 null
     */
    public static BeanDatasetDefinitionVo toVo(BeanDatasetDefinition dataset) {
        if (dataset == null) {
            return null;
        }
        BeanDatasetDefinitionVo vo = new BeanDatasetDefinitionVo();
        vo.setName(dataset.getName());
        vo.setFields(dataset.getFields());
        vo.setMethod(dataset.getMethod());
        vo.setClazz(dataset.getClazz());
        return vo;
    }

    /**
     * 转换 JsonDatasetDefinition 为 JsonDatasetDefinitionVo
     *
     * @param dataset JSON 静态数据集定义
     * @return VO 对象；入参为 null 时返回 null
     */
    public static JsonDatasetDefinitionVo toVo(JsonDatasetDefinition dataset) {
        if (dataset == null) {
            return null;
        }
        JsonDatasetDefinitionVo vo = new JsonDatasetDefinitionVo();
        vo.setName(dataset.getName());
        vo.setFields(dataset.getFields());
        vo.setContent(dataset.getContent());
        return vo;
    }

    /**
     * 转换 DatasetDefinition 为对应 VO（多态分发）
     *
     * @param dataset 数据集定义
     * @return VO 对象；入参为 null 时返回 null
     */
    public static DatasetDefinitionVo toDatasetVo(DatasetDefinition dataset) {
        if (dataset == null) {
            return null;
        }
        if (dataset instanceof SqlDatasetDefinition) {
            return toVo((SqlDatasetDefinition) dataset);
        }
        if (dataset instanceof BeanDatasetDefinition) {
            return toVo((BeanDatasetDefinition) dataset);
        }
        if (dataset instanceof JsonDatasetDefinition) {
            return toVo((JsonDatasetDefinition) dataset);
        }
        logger.warn("未识别的 DatasetDefinition 子类: {}", dataset.getClass().getName());
        DatasetDefinitionVo vo = new DatasetDefinitionVo();
        vo.setName(dataset.getName());
        vo.setFields(dataset.getFields());
        return vo;
    }

    /**
     * 批量转换 DatasetDefinition 列表
     * @param datasets 数据集定义列表
     * @return VO 列表；入参为 null 时返回 null
     */
    public static List<DatasetDefinitionVo> toDatasetVoList(List<DatasetDefinition> datasets) {
        if (datasets == null || datasets.isEmpty()) {
            return null;
        }
        List<DatasetDefinitionVo> voList = new ArrayList<>(datasets.size());
        for (DatasetDefinition dataset : datasets) {
            voList.add(toDatasetVo(dataset));
        }
        return voList;
    }

    /**
     * 转换 DatasourceDefinition 为对应 VO（多态分发）
     *
     * @param datasource 数据源定义
     * @return VO 对象；入参为 null 时返回 null
     */
    public static DatasourceDefinitionVo toVo(DatasourceDefinition datasource) {
        if (datasource == null) {
            return null;
        }
        DatasourceDefinitionVo vo;
        if (datasource instanceof JdbcDatasourceDefinition) {
            JdbcDatasourceDefinition jdbc = (JdbcDatasourceDefinition) datasource;
            JdbcDatasourceDefinitionVo jdbcVo = new JdbcDatasourceDefinitionVo();
            jdbcVo.setDriver(jdbc.getDriver());
            jdbcVo.setUrl(jdbc.getUrl());
            jdbcVo.setUsername(jdbc.getUsername());
            jdbcVo.setPassword(jdbc.getPassword());
            vo = jdbcVo;
        } else if (datasource instanceof BuildinDatasourceDefinition) {
            vo = new BuildinDatasourceDefinitionVo();
        } else if (datasource instanceof SpringBeanDatasourceDefinition) {
            SpringBeanDatasourceDefinition spring = (SpringBeanDatasourceDefinition) datasource;
            SpringBeanDatasourceDefinitionVo springVo = new SpringBeanDatasourceDefinitionVo();
            springVo.setBeanId(spring.getBeanId());
            vo = springVo;
        } else if (datasource instanceof StaticDatasourceDefinition) {
            vo = new DatasourceDefinitionVo();
        } else {
            logger.warn("未识别的 DatasourceDefinition 子类: {}", datasource.getClass().getName());
            vo = new DatasourceDefinitionVo();
        }
        vo.setName(datasource.getName());
        vo.setType(datasource.getType());
        vo.setDatasets(toDatasetVoList(datasource.getDatasets()));
        return vo;
    }

    /**
     * 批量转换 DatasourceDefinition 列表
     * @param datasources 数据源定义列表
     * @return VO 列表；入参为 null 时返回 null
     */
    public static List<DatasourceDefinitionVo> toDatasourceVoList(List<DatasourceDefinition> datasources) {
        if (datasources == null || datasources.isEmpty()) {
            return null;
        }
        List<DatasourceDefinitionVo> voList = new ArrayList<>(datasources.size());
        for (DatasourceDefinition datasource : datasources) {
            voList.add(toVo(datasource));
        }
        return voList;
    }
}
