package com.luck.report.jdbc.node;

import com.luck.report.jdbc.RenderContext;

import java.util.List;

/**
 * &lt;trim&gt; / &lt;where&gt; / &lt;set&gt; 节点
 */
public class TrimNode implements SqlNode {

    private final String prefix;
    private final String suffix;
    private final String[] prefixesToOverride;
    private final String[] suffixesToOverride;
    private final List<SqlNode> children;

    /**
     * 构造 trim 节点
     *
     * @param prefix              内容非空时追加的前缀
     * @param suffix              内容非空时追加的后缀
     * @param prefixesToOverride  去掉内容开头的这些词
     * @param suffixesToOverride  去掉内容结尾的这些词
     * @param children            子节点
     */
    public TrimNode(String prefix, String suffix, String prefixesToOverride,
                    String suffixesToOverride, List<SqlNode> children) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.prefixesToOverride = split(prefixesToOverride);
        this.suffixesToOverride = split(suffixesToOverride);
        this.children = children;
    }

    /**
     * 构造 where 语义 trim（去掉开头 AND/OR）
     *
     * @param children 子节点
     * @return where 节点
     */
    public static TrimNode where(List<SqlNode> children) {
        return new TrimNode("WHERE", null, "AND |OR ", null, children);
    }

    /**
     * 构造 set 语义 trim（去掉末尾逗号）
     *
     * @param children 子节点
     * @return set 节点
     */
    public static TrimNode set(List<SqlNode> children) {
        return new TrimNode("SET", null, null, ",", children);
    }

    @Override
    public void render(RenderContext context, StringBuilder sql) {
        StringBuilder body = new StringBuilder();
        for (SqlNode child : children) {
            child.render(context, body);
        }
        String content = body.toString().trim();
        if (content.isEmpty()) {
            return;
        }
        content = stripPrefixes(content);
        content = stripSuffixes(content);
        if (content.isEmpty()) {
            return;
        }
        sql.append(' ');
        if (prefix != null) {
            sql.append(prefix).append(' ');
        }
        sql.append(content);
        if (suffix != null) {
            sql.append(' ').append(suffix);
        }
        sql.append(' ');
    }

    @Override
    public boolean isDynamic() {
        return true;
    }

    private String stripPrefixes(String content) {
        String result = content;
        boolean changed = true;
        while (changed) {
            changed = false;
            String upper = result.toUpperCase();
            for (String p : prefixesToOverride) {
                if (p.isEmpty()) {
                    continue;
                }
                if (upper.startsWith(p.toUpperCase())) {
                    result = result.substring(p.length()).trim();
                    changed = true;
                    break;
                }
            }
        }
        return result;
    }

    private String stripSuffixes(String content) {
        String result = content;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (String s : suffixesToOverride) {
                if (s.isEmpty()) {
                    continue;
                }
                if (result.endsWith(s)) {
                    result = result.substring(0, result.length() - s.length()).trim();
                    changed = true;
                    break;
                }
            }
        }
        return result;
    }

    private static String[] split(String raw) {
        if (raw == null || raw.isEmpty()) {
            return new String[0];
        }
        // 保留每段尾部空格：MyBatis 的 "AND |OR " 必须匹配 "AND "/"OR "，
        // 若 trim 成 "OR" 会误伤 "ORDER BY"
        return raw.split("\\|", -1);
    }
}
