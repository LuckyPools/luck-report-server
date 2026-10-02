/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.domain.vo.value;

import com.luck.report.core.definition.value.Source;
import com.luck.report.core.definition.value.ValueType;

import java.io.Serializable;

/**
 * ImageValue的VO类，用于前端展示
 *
 * @author LuckyPools
 * @since 2026年
 */
public class ImageValueVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Source source;
    private int width;
    private int height;
    private String value;
    private ValueType type;

    /**
     * 默认无参构造器
     */
    public ImageValueVo() {}

    public Source getSource() {
        return source;
    }

    public void setSource(Source source) {
        this.source = source;
    }

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
