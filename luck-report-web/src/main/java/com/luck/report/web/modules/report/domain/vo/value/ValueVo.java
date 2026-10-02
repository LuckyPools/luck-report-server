/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.domain.vo.value;

import com.luck.report.core.definition.value.Value;
import com.luck.report.core.definition.value.ValueType;

import java.io.Serializable;

/**
 * Value的VO类，用于前端展示
 *
 * @author LuckyPools
 * @since 2026年
 */
public class ValueVo implements Value, Serializable {
    private static final long serialVersionUID = 1L;

    private String value;
    private ValueType type;

    /**
     * 默认无参构造器
     */
    public ValueVo() {}

    /**
     * 构造函数
     * @param value 值
     * @param type 类型
     */
    public ValueVo(String value, ValueType type) {
        this.value = value;
        this.type = type;
    }

    @Override
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public ValueType getType() {
        return type;
    }

    public void setType(ValueType type) {
        this.type = type;
    }
}
