/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.domain.vo.value;

import com.luck.report.core.definition.value.Source;
import com.luck.report.core.definition.value.ValueType;
import com.luck.report.core.definition.value.ZxingCategory;

import java.io.Serializable;

/**
 * ZxingValue的VO类，用于前端展示
 *
 * @author LuckyPools
 * @since 2026年
 */
public class ZxingValueVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private int width;
    private int height;
    private Source source;
    private String format;
    private ZxingCategory category;
    private String value;
    private ValueType type;

    /**
     * 默认无参构造器
     */
    public ZxingValueVo() {}

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public ZxingCategory getCategory() {
        return category;
    }

    public void setCategory(ZxingCategory category) {
        this.category = category;
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
