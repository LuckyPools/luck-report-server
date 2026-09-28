package com.luck.report.jdbc.node;

import java.util.List;

/**
 * choose/when 分支
 */
public class WhenBranch {

    private final String test;
    private final List<SqlNode> children;

    /**
     * 构造 when 分支
     *
     * @param test     条件
     * @param children 子节点
     */
    public WhenBranch(String test, List<SqlNode> children) {
        this.test = test;
        this.children = children;
    }

    /**
     * 获取条件表达式
     *
     * @return test
     */
    public String getTest() {
        return test;
    }

    /**
     * 获取子节点
     *
     * @return 子节点列表
     */
    public List<SqlNode> getChildren() {
        return children;
    }
}
