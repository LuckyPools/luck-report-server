/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.domain.vo.cell;

import java.io.Serializable;

/**
 * LinkParameter的VO类，用于前端展示
 *
 * @author LuckyPools
 * @since 2026年
 */
public class LinkParameterVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private String value;

    /**
     * 默认无参构造器
     */
    public LinkParameterVo() {}

    /**
     * 构造函数
     * @param name 参数名
     * @param value 参数值
     */
    public LinkParameterVo(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
