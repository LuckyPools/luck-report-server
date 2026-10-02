/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.domain.vo.cell;

import com.luck.report.core.expression.model.condition.ConditionType;

import java.io.Serializable;

/**
 * Condition的VO类，用于前端展示
 *
 * @author LuckyPools
 * @since 2026年
 */
public class ConditionVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String operation;
    private String join;
    private String left;
    private String right;
    private ConditionType type;

    /**
     * 默认无参构造器
     */
    public ConditionVo() {}

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public String getJoin() {
        return join;
    }

    public void setJoin(String join) {
        this.join = join;
    }

    public String getLeft() {
        return left;
    }

    public void setLeft(String left) {
        this.left = left;
    }

    public String getRight() {
        return right;
    }

    public void setRight(String right) {
        this.right = right;
    }

    public ConditionType getType() {
        return type;
    }

    public void setType(ConditionType type) {
        this.type = type;
    }
}
