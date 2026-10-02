/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.domain.vo.dataset;

/**
 * Bean 数据集定义 VO
 *
 * @author system
 * @since 2026年
 */
public class BeanDatasetDefinitionVo extends DatasetDefinitionVo {
    private static final long serialVersionUID = 1L;

    private String method;
    private String clazz;

    /**
     * 默认无参构造器
     */
    public BeanDatasetDefinitionVo() {}

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getClazz() {
        return clazz;
    }

    public void setClazz(String clazz) {
        this.clazz = clazz;
    }
}
