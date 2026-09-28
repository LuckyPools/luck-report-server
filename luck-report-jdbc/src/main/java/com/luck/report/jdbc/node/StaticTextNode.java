package com.luck.report.jdbc.node;

import com.luck.report.jdbc.RenderContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 静态文本节点，支持 #{path} 占位符
 */
public class StaticTextNode implements SqlNode {

    private static final Pattern PARAM_PATTERN = Pattern.compile("#\\{([^}]+)}");

    private final String text;
    private final boolean hasParam;

    /**
     * 构造静态文本节点
     *
     * @param text 原始文本（可含 #{}）
     */
    public StaticTextNode(String text) {
        this.text = text == null ? "" : text;
        if (this.text.contains("${")) {
            throw new IllegalArgumentException(
                    "${...} is not supported; use #{...} named parameters only");
        }
        this.hasParam = this.text.contains("#{");
    }

    @Override
    public void render(RenderContext context, StringBuilder sql) {
        if (!hasParam) {
            sql.append(text);
            return;
        }
        Matcher matcher = PARAM_PATTERN.matcher(text);
        int last = 0;
        while (matcher.find()) {
            sql.append(text, last, matcher.start());
            String path = matcher.group(1).trim();
            int comma = path.indexOf(',');
            if (comma > 0) {
                path = path.substring(0, comma).trim();
            }
            Object value = context.getValue(path);
            sql.append(context.bindParam(unwrapEnumValue(value)));
            last = matcher.end();
        }
        sql.append(text.substring(last));
    }

    @Override
    public boolean isDynamic() {
        return false;
    }

    private static Object unwrapEnumValue(Object value) {
        if (!(value instanceof Enum)) {
            return value;
        }
        try {
            java.lang.reflect.Method getValue = value.getClass().getMethod("getValue");
            return getValue.invoke(value);
        } catch (Exception ignored) {
            return ((Enum<?>) value).name();
        }
    }
}
