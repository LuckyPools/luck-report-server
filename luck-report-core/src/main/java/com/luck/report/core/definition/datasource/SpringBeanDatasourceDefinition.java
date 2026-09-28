/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.luck.report.core.definition.datasource;

import com.luck.report.core.build.Dataset;
import com.luck.report.core.definition.dataset.BeanDatasetDefinition;
import com.luck.report.core.definition.dataset.DatasetDefinition;
import org.springframework.context.ApplicationContext;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Jacky.gao
 * @since 2016年12月27日
 */
public class SpringBeanDatasourceDefinition implements DatasourceDefinition, Serializable {
    private static final long serialVersionUID = 1L;
    private String beanId;
    private String name;
    private List<DatasetDefinition> datasets;

    /**
     * 默认无参构造器
     */
    public SpringBeanDatasourceDefinition() {}

    /**
     * 构建全部 Bean 数据集
     *
     * @param applicationContext Spring 上下文，非空
     * @param parameters 报表参数，可空
     * @return 数据集列表
     */
    public List<Dataset> getDatasets(ApplicationContext applicationContext, Map<String, Object> parameters) {
        return getDatasets(applicationContext, parameters, null);
    }

    /**
     * 按引用名过滤后构建 Bean 数据集
     *
     * @param applicationContext Spring 上下文，非空
     * @param parameters 报表参数，可空
     * @param allowedNames 允许构建的数据集名；null 表示不过滤
     * @return 数据集列表；全部被过滤时返回空列表
     */
    public List<Dataset> getDatasets(ApplicationContext applicationContext, Map<String, Object> parameters,
            Set<String> allowedNames) {
        Object targetBean = applicationContext.getBean(beanId);
        List<Dataset> list = new ArrayList<Dataset>();
        if (datasets == null) {
            return list;
        }
        for (DatasetDefinition dsDef : datasets) {
            if (allowedNames != null && !allowedNames.contains(dsDef.getName())) {
                continue;
            }
            BeanDatasetDefinition beanDef = (BeanDatasetDefinition) dsDef;
            Dataset ds = beanDef.buildDataset(name, targetBean, parameters);
            list.add(ds);
        }
        return list;
    }

    @Override
    public DatasourceType getType() {
        return DatasourceType.spring;
    }

    /**
     * 空实现，用于兼容JSON反序列化
     * @param type 数据源类型
     */
    public void setType(DatasourceType type) {
        // 空实现，忽略type字段
    }

    @Override
    public List<DatasetDefinition> getDatasets() {
        return datasets;
    }

    public void setDatasets(List<DatasetDefinition> datasets) {
        this.datasets = datasets;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBeanId() {
        return beanId;
    }

    public void setBeanId(String beanId) {
        this.beanId = beanId;
    }
}
