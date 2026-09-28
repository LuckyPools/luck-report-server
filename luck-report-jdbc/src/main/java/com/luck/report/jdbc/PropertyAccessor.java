package com.luck.report.jdbc;

import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NotReadablePropertyException;

import java.util.Map;

/**
 * 从 Map / JavaBean 按路径取值
 */
public final class PropertyAccessor {

    private PropertyAccessor() {
    }

    /**
     * 从根对象按点分路径取值
     *
     * @param root 根对象（Map 或 Bean），可为 null
     * @param path 属性路径，如 queryDTO.name
     * @return 属性值；无法解析时返回 null
     */
    public static Object getValue(Object root, String path) {
        if (root == null || path == null || path.isEmpty()) {
            return null;
        }
        String[] parts = path.split("\\.");
        Object current = root;
        for (String part : parts) {
            if (current == null) {
                return null;
            }
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                try {
                    BeanWrapper wrapper = new BeanWrapperImpl(current);
                    if (!wrapper.isReadableProperty(part)) {
                        return null;
                    }
                    current = wrapper.getPropertyValue(part);
                } catch (NotReadablePropertyException ex) {
                    return null;
                }
            }
        }
        return current;
    }
}
