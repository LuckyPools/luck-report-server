package com.luck.report.infra.modules.vector.domain.entity;

import lombok.Data;

/**
 * vector_document 表行映射（含 vectorType、similarity）。
 *
 * @author luck
 */
@Data
public class VectorDocumentRow {

    private String id;
    /** 向量字符串，如 "[0.1,0.2,0.3]" */
    private String vector;
    private String content;
    /** metadata JSON */
    private String metadata;
    private String vectorType;
    /** 仅检索时有值 */
    private Double similarity;
}
