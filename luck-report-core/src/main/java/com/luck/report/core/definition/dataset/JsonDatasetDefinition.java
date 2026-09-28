package com.luck.report.core.definition.dataset;

import com.luck.report.core.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * JSON 静态数据集定义
 * <p>
 * 静态数据集的数据以 JSON 数组字符串形式存储在 content 字段中，
 * 字段集合通过 {@link #getFields()} 从 content 中提取所有对象 key 去重得到。
 * </p>
 *
 * @author luck-report
 * @since 2.0.5
 */
public class JsonDatasetDefinition implements DatasetDefinition {

    private static final long serialVersionUID = 4581019308843195488L;

    /** 数据集名称 */
    private String name;

    /** JSON 数组字符串，存储静态数据集的完整数据 */
    private String content;

    /**
     * 字段列表
     * <p>
     * 由 DatasourceParser 从 XML &lt;field&gt; 节点解析注入；
     * 若为空，{@link #getFields()} 会回退到从 content 实时提取
     * </p>
     */
    private List<Field> fields;

    /**
     * 默认无参构造器
     */
    public JsonDatasetDefinition() {
    }

    /**
     * 获取字段列表
     * <p>
     * 优先返回 XML 解析注入的 fields；若为空则从 content 实时提取，
     * 与前端 buildFields 逻辑保持一致：遍历 JSON 数组所有对象 key，去重并保持首次出现顺序
     * </p>
     *
     * @return 字段列表；content 为空或解析失败时返回空 List
     */
    @Override
    public List<Field> getFields() {
        if (fields != null) {
            return fields;
        }
        List<String> keys = JsonUtils.extractArrayKeys(content);
        List<Field> list = new ArrayList<>(keys.size());
        for (String key : keys) {
            list.add(new Field(key));
        }
        return list;
    }

    /**
     * 设置字段列表
     *
     * @param fields 字段列表
     */
    public void setFields(List<Field> fields) {
        this.fields = fields;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 设置数据集名称
     *
     * @param name 数据集名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取 JSON 数组字符串
     *
     * @return content JSON 字符串
     */
    public String getContent() {
        return content;
    }

    /**
     * 设置 JSON 数组字符串
     *
     * @param content JSON 数组字符串
     */
    public void setContent(String content) {
        this.content = content;
    }
}
