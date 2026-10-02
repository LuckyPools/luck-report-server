/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.domain.vo.value;

import com.luck.report.core.definition.value.Slash;
import com.luck.report.core.definition.value.ValueType;

import java.io.Serializable;
import java.util.List;

/**
 * SlashValue的VO类，用于前端展示
 *
 * @author LuckyPools
 * @since 2026年
 */
public class SlashValueVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private List<Slash> slashes;
    private String value;
    private ValueType type;

    /**
     * 默认无参构造器
     */
    public SlashValueVo() {}

    public List<Slash> getSlashes() {
        return slashes;
    }

    public void setSlashes(List<Slash> slashes) {
        this.slashes = slashes;
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
