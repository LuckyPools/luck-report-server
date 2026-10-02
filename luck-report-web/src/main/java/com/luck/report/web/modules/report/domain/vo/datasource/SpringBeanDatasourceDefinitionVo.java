/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.domain.vo.datasource;

/**
 * Spring Bean 数据源定义 VO
 *
 * @author system
 * @since 2026年
 */
public class SpringBeanDatasourceDefinitionVo extends DatasourceDefinitionVo {
    private static final long serialVersionUID = 1L;

    private String beanId;

    /**
     * 默认无参构造器
     */
    public SpringBeanDatasourceDefinitionVo() {}

    public String getBeanId() {
        return beanId;
    }

    public void setBeanId(String beanId) {
        this.beanId = beanId;
    }
}
