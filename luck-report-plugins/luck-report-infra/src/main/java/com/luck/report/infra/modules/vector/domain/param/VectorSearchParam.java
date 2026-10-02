package com.luck.report.infra.modules.vector.domain.param;

import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 向量检索参数
 *
 * @author luck
 */
@Getter
@Builder(toBuilder = true)
public class VectorSearchParam {

    /**
     * 查询向量，必填
     */
    private final float[] queryVector;

    /**
     * 返回条数
     */
    private final int topK;

    /**
     * 相似度阈值（0~1）
     */
    private final double threshold;

    /**
     * 知识类型；null 表示全库
     */
    private final String vectorType;

    /**
     * metadata 等值过滤
     */
    private final Map<String, Object> metadataEquals;

    /**
     * metadata 业务 ID 字段名
     */
    private final String idMetaKey;

    /**
     * 生效业务 ID 列表
     */
    private final List<String> validIds;
}
