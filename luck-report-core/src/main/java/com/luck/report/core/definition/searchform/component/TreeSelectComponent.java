package com.luck.report.core.definition.searchform.component;

/**
 * 树选择组件（a-tree-select）。公共字段见 {@link BaseOptionComponent}。
 *
 * @author luck-report
 * @since 2.2.0
 */
public class TreeSelectComponent extends BaseOptionComponent {
    private static final long serialVersionUID = 1L;

    /**
     * 默认展开全部树节点
     */
    private boolean treeDefaultExpandAll;

    /**
     * 默认无参构造器
     */
    public TreeSelectComponent() {}

    public boolean isTreeDefaultExpandAll() {
        return treeDefaultExpandAll;
    }

    public void setTreeDefaultExpandAll(boolean treeDefaultExpandAll) {
        this.treeDefaultExpandAll = treeDefaultExpandAll;
    }
}
