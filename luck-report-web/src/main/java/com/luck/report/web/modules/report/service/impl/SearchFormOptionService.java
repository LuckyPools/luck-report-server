package com.luck.report.web.modules.report.service.impl;

import com.luck.report.core.Utils;
import com.luck.report.core.build.Dataset;
import com.luck.report.core.utils.StaticDatasetUtils;
import com.luck.report.core.definition.ReportDefinition;
import com.luck.report.core.definition.dataset.BeanDatasetDefinition;
import com.luck.report.core.definition.dataset.DatasetDefinition;
import com.luck.report.core.definition.dataset.JsonDatasetDefinition;
import com.luck.report.core.definition.dataset.SqlDatasetDefinition;
import com.luck.report.core.definition.datasource.BuildinDatasource;
import com.luck.report.core.definition.datasource.BuildinDatasourceDefinition;
import com.luck.report.core.definition.datasource.DatasourceDefinition;
import com.luck.report.core.definition.datasource.DatasourceProvider;
import com.luck.report.core.definition.datasource.JdbcDatasourceDefinition;
import com.luck.report.core.definition.datasource.SpringBeanDatasourceDefinition;
import com.luck.report.core.definition.datasource.StaticDatasourceDefinition;
import com.luck.report.core.exception.ReportBizException;
import com.luck.report.core.exception.ReportComputeException;
import com.luck.report.core.export.ReportRender;
import com.luck.report.core.utils.JsonUtils;
import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.web.modules.report.constant.ReportConstants;
import com.luck.report.web.modules.report.domain.vo.report.SearchFormOption;
import com.luck.report.web.modules.report.domain.vo.report.SearchFormOptionsVo;
import com.luck.report.web.modules.report.domain.vo.request.SearchFormDatasetRef;
import com.luck.report.web.modules.report.domain.vo.request.SearchFormOptionsRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 查询表单选项加载服务。
 *
 * @author luck-report
 * @since 2.1.0
 */
@Service("bean.searchFormOptionService")
public class SearchFormOptionService implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(SearchFormOptionService.class);

    @Autowired
    @Qualifier("bean.reportRender")
    private ReportRender reportRender;

    @Autowired
    @Qualifier("bean.reportDefinitionService")
    private ReportDefinitionService reportDefinitionService;

    @Autowired
    @Qualifier("bean.paramService")
    private ParamService paramService;

    private ApplicationContext applicationContext;

    private final Map<String, DatasourceProvider> datasourceProviderMap = new HashMap<>();

    /**
     * 批量加载查询表单选项。
     *
     * @param request 请求（报表路径 + 数据集引用列表）
     * @param apiRequest HTTP 请求上下文（用于注入内置参数），可为空
     * @return key 为 "数据源名/数据集名"，value 为 {label, value} 选项列表；单项失败返回空列表并记录 errors
     */
    public SearchFormOptionsVo loadOptions(SearchFormOptionsRequest request, ApiRequest apiRequest) {
        SearchFormOptionsVo result = new SearchFormOptionsVo();
        if (request == null || isBlank(request.getReportPath()) || request.getDatasets() == null) {
            return result;
        }
        ReportDefinition reportDefinition = loadReportDefinition(request.getReportPath(), request.getMode());
        for (SearchFormDatasetRef ref : request.getDatasets()) {
            String key = ref.getDatasourceName() + "/" + ref.getDatasetName();
            try {
                result.putOptions(key, buildOptions(reportDefinition, ref, apiRequest));
            } catch (Exception e) {
                logger.warn("加载查询表单选项失败: {} - {}", key, e.getMessage());
                result.putOptions(key, new ArrayList<>());
                result.putError(key, e.getMessage());
            }
        }
        return result;
    }

    private ReportDefinition loadReportDefinition(String reportPath, String mode) {
        boolean isPreview = ReportConstants.MODE_KEY.equals(mode);
        if (isPreview) {
            return reportDefinitionService.getReportDefinition(reportPath);
        }
        return reportRender.getReportDefinition(reportPath);
    }

    /**
     * 执行单个数据集并映射为 label/value 选项。
     */
    private List<SearchFormOption> buildOptions(ReportDefinition reportDefinition,
                                                          SearchFormDatasetRef ref,
                                                          ApiRequest apiRequest) {
        DatasourceDefinition dsDef = findDatasource(reportDefinition, ref.getDatasourceName());
        if (dsDef == null) {
            throw new ReportBizException("error.datasource.notExistNamed", ref.getDatasourceName());
        }
        DatasetDefinition datasetDef = findDataset(dsDef, ref.getDatasetName());
        if (datasetDef == null) {
            throw new ReportBizException("error.dataset.notExistNamed", ref.getDatasetName());
        }
        Map<String, Object> parameters = ref.getParameters() == null ? new HashMap<>() : ref.getParameters();
        paramService.injectBuiltInParams(parameters, apiRequest);
        List<?> data = executeDataset(dsDef, datasetDef, parameters);
        if (!isBlank(ref.getParentField())) {
            return buildTreeOptions(data, ref.getLabelField(), ref.getValueField(), ref.getParentField());
        }
        return mapToOptions(data, ref.getLabelField(), ref.getValueField());
    }

    /**
     * 按数据源类型执行单个数据集，返回行列表
     */
    private List<?> executeDataset(DatasourceDefinition dsDef, DatasetDefinition datasetDef,
                                   Map<String, Object> parameters) {
        if (dsDef instanceof StaticDatasourceDefinition) {
            JsonDatasetDefinition jsonDataset = (JsonDatasetDefinition) datasetDef;
            List<Map<String, Object>> dataList = JsonUtils.fromJsonList(jsonDataset.getContent());
            return StaticDatasetUtils.filter(dataList, parameters);
        }
        if (dsDef instanceof SpringBeanDatasourceDefinition) {
            SpringBeanDatasourceDefinition springDs = (SpringBeanDatasourceDefinition) dsDef;
            Object targetBean = applicationContext.getBean(springDs.getBeanId());
            BeanDatasetDefinition beanDef = (BeanDatasetDefinition) datasetDef;
            Dataset ds = beanDef.buildDataset(springDs.getName(), targetBean, parameters);
            return ds.getData();
        }
        SqlDatasetDefinition sqlDataset = (SqlDatasetDefinition) datasetDef;
        Connection conn = null;
        try {
            conn = openConnection(dsDef);
            Dataset ds = sqlDataset.buildDataset(parameters, conn);
            return ds.getData();
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception ignore) {
                }
            }
        }
    }

    /**
     * 获取数据库连接：优先 DatasourceProvider，其次内置数据源，JDBC 数据源兜底自建连接
     */
    private Connection openConnection(DatasourceDefinition dsDef) {
        String dsName = dsDef.getName();
        if (datasourceProviderMap.containsKey(dsName)) {
            return datasourceProviderMap.get(dsName).getConnection();
        }
        if (dsDef instanceof BuildinDatasourceDefinition) {
            for (BuildinDatasource datasource : Utils.getBuildinDatasources()) {
                if (datasource.name().equals(dsName)) {
                    return datasource.getConnection();
                }
            }
            throw new ReportBizException("error.datasource.buildinNotExist", dsName);
        }
        if (dsDef instanceof JdbcDatasourceDefinition) {
            JdbcDatasourceDefinition jdbcDs = (JdbcDatasourceDefinition) dsDef;
            try {
                Class.forName(jdbcDs.getDriver());
                return DriverManager.getConnection(jdbcDs.getUrl(), jdbcDs.getUsername(), jdbcDs.getPassword());
            } catch (Exception e) {
                throw new ReportComputeException(e);
            }
        }
        throw new ReportBizException("error.search.unsupportedDatasource", dsDef.getClass().getName());
    }

    /**
     * 行列表映射为 label/value 选项
     */
    private List<SearchFormOption> mapToOptions(List<?> data, String labelField, String valueField) {
        List<SearchFormOption> list = new ArrayList<>();
        if (data == null) {
            return list;
        }
        for (Object row : data) {
            if (!(row instanceof Map)) {
                continue;
            }
            Map<?, ?> map = (Map<?, ?>) row;
            Object label = labelField == null ? null : map.get(labelField);
            Object value = valueField == null ? null : map.get(valueField);
            if (value == null) {
                continue;
            }
            list.add(new SearchFormOption(
                    label == null ? String.valueOf(value) : String.valueOf(label), value));
        }
        return list;
    }

    /**
     * 平铺行列表按 valueField / parentField 建树，返回带 children 的层级选项。
     */
    private List<SearchFormOption> buildTreeOptions(List<?> data, String labelField,
                                                              String valueField, String parentField) {
        List<SearchFormOption> list = new ArrayList<>();
        if (data == null) {
            return list;
        }
        Map<String, SearchFormOption> index = new LinkedHashMap<>();
        Map<SearchFormOption, String> parentValues = new LinkedHashMap<>();
        for (Object row : data) {
            if (!(row instanceof Map)) {
                continue;
            }
            Map<?, ?> map = (Map<?, ?>) row;
            Object label = labelField == null ? null : map.get(labelField);
            Object value = valueField == null ? null : map.get(valueField);
            if (value == null) {
                continue;
            }
            SearchFormOption option = new SearchFormOption(
                    label == null ? String.valueOf(value) : String.valueOf(label), value);
            list.add(option);
            index.putIfAbsent(String.valueOf(value), option);
            Object parentValue = map.get(parentField);
            parentValues.put(option, parentValue == null ? null : String.valueOf(parentValue));
        }
        List<SearchFormOption> roots = new ArrayList<>();
        for (SearchFormOption option : list) {
            String parentValue = parentValues.get(option);
            if (parentValue == null) {
                roots.add(option);
                continue;
            }
            SearchFormOption parent = index.get(parentValue);
            if (parent == null) {
                roots.add(option);
            } else if (isInCycle(option, parentValue, index, parentValues)) {
                logger.warn("查询表单选项树出现循环引用，节点 [{}] 按根节点输出", option.getValue());
                roots.add(option);
            } else {
                if (parent.getChildren() == null) {
                    parent.setChildren(new ArrayList<>());
                }
                parent.getChildren().add(option);
            }
        }
        return roots;
    }

    /**
     * 判断 option 沿父链向上是否构成环（回到自身）
     */
    private boolean isInCycle(SearchFormOption option, String startParentValue,
                              Map<String, SearchFormOption> index,
                              Map<SearchFormOption, String> parentValues) {
        Set<String> visited = new HashSet<>();
        String current = startParentValue;
        while (current != null) {
            if (String.valueOf(option.getValue()).equals(current)) {
                return true;
            }
            if (!visited.add(current)) {
                return false;
            }
            SearchFormOption parentNode = index.get(current);
            current = parentNode == null ? null : parentValues.get(parentNode);
        }
        return false;
    }

    private DatasourceDefinition findDatasource(ReportDefinition reportDefinition, String name) {
        List<DatasourceDefinition> datasources = reportDefinition.getDatasources();
        if (datasources == null) {
            return null;
        }
        for (DatasourceDefinition ds : datasources) {
            if (ds.getName() != null && ds.getName().equals(name)) {
                return ds;
            }
        }
        return null;
    }

    private DatasetDefinition findDataset(DatasourceDefinition dsDef, String name) {
        List<DatasetDefinition> datasets = dsDef.getDatasets();
        if (datasets == null) {
            return null;
        }
        for (DatasetDefinition dt : datasets) {
            if (dt.getName() != null && dt.getName().equals(name)) {
                return dt;
            }
        }
        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
        Collection<DatasourceProvider> providers = applicationContext.getBeansOfType(DatasourceProvider.class).values();
        for (DatasourceProvider provider : providers) {
            datasourceProviderMap.put(provider.getName(), provider);
        }
    }
}
