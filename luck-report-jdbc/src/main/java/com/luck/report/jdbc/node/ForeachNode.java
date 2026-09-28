package com.luck.report.jdbc.node;

import com.luck.report.jdbc.RenderContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * &lt;foreach&gt; 节点
 */
public class ForeachNode implements SqlNode {

    private final String collection;
    private final String item;
    private final String open;
    private final String close;
    private final String separator;
    private final List<SqlNode> children;

    /**
     * 构造 foreach 节点
     *
     * @param collection 集合属性路径
     * @param item       迭代变量名
     * @param open       前缀
     * @param close      后缀
     * @param separator  分隔符
     * @param children   子节点
     */
    public ForeachNode(String collection, String item, String open, String close,
                       String separator, List<SqlNode> children) {
        this.collection = collection;
        this.item = item == null ? "item" : item;
        this.open = open == null ? "" : open;
        this.close = close == null ? "" : close;
        this.separator = separator == null ? "" : separator;
        this.children = children;
    }

    @Override
    public void render(RenderContext context, StringBuilder sql) {
        Object coll = context.getValue(collection);
        List<Object> items = toList(coll);
        if (items.isEmpty()) {
            throw new IllegalArgumentException("foreach collection '" + collection + "' is empty");
        }
        sql.append(open);
        Object previous = context.getValue(item);
        boolean first = true;
        for (Object element : items) {
            if (!first) {
                sql.append(separator);
            }
            first = false;
            context.putBinding(item, element);
            for (SqlNode child : children) {
                child.render(context, sql);
            }
        }
        if (previous == null) {
            context.putBinding(item, null);
        } else {
            context.putBinding(item, previous);
        }
        sql.append(close);
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    private static List<Object> toList(Object coll) {
        if (coll == null) {
            return Collections.emptyList();
        }
        if (coll instanceof List) {
            return (List<Object>) coll;
        }
        if (coll instanceof Collection) {
            return new ArrayList<Object>((Collection<?>) coll);
        }
        if (coll.getClass().isArray()) {
            Object[] arr = (Object[]) coll;
            List<Object> list = new ArrayList<Object>(arr.length);
            Collections.addAll(list, arr);
            return list;
        }
        throw new IllegalArgumentException("foreach collection is not a Collection/array: " + coll.getClass());
    }
}
