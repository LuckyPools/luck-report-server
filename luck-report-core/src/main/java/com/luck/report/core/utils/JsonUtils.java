package com.luck.report.core.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luck.report.core.exception.ReportException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * JSON 工具类
 * <p>
 * 封装 Jackson ObjectMapper 的常用操作，内部维护单例 MAPPER 复用，
 * 参照既有 JsonFunction / DatasourceService 的 ObjectMapper 使用模式收敛至此。
 * </p>
 *
 * @author luck-report
 * @since 2.0.5
 */
public final class JsonUtils {

    /** Jackson ObjectMapper 单例（线程安全） */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtils() {
        // 私有构造器，防止实例化
    }

    /**
     * 将 JSON 数组字符串反序列化为 List<Map>
     * <p>
     * 用于静态数据集取数：把 dataset.content 解析为 List<Map<String,Object>> 供 Dataset 使用
     * </p>
     *
     * @param json JSON 数组字符串，可为空
     * @return 反序列化后的 List；入参为空时返回空 List
     * @throws ReportException JSON 解析失败时抛出
     */
    public static List<Map<String, Object>> fromJsonList(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            return MAPPER.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
        } catch (Exception e) {
            throw new ReportException("error.json.parseFailed", e.getMessage());
        }
    }

    /**
     * 提取 JSON 数组中所有对象的 key 集合
     * <p>
     * 用于静态数据集字段提取：遍历所有对象 key，用 LinkedHashSet 去重并保持首次出现顺序
     * </p>
     *
     * @param json JSON 数组字符串，可为空
     * @return 去重后的 key 列表；入参为空或解析失败时返回空 List
     */
    public static List<String> extractArrayKeys(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            List<Map<String, Object>> list = MAPPER.readValue(json, new TypeReference<List<Map<String, Object>>>() {
            });
            // LinkedHashSet 保持首次出现顺序，避免不同对象 key 顺序不一致导致字段顺序抖动
            Set<String> keySet = new LinkedHashSet<>();
            for (Map<String, Object> map : list) {
                if (map != null) {
                    keySet.addAll(map.keySet());
                }
            }
            return new ArrayList<>(keySet);
        } catch (Exception e) {
            // 字段提取失败不影响取数流程，返回空列表由上层处理
            return new ArrayList<>();
        }
    }

    /**
     * 将对象序列化为 JSON 字符串
     *
     * @param obj 待序列化对象，可为空
     * @return JSON 字符串；入参为空时返回 null
     * @throws ReportException JSON 序列化失败时抛出
     */
    public static String toJson(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return MAPPER.writeValueAsString(obj);
        } catch (Exception e) {
            throw new ReportException("error.json.serializeFailed", e.getMessage());
        }
    }
}
