package com.luck.report.web.modules.knowledge.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 业务知识VO
 *
 * @author luck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessKnowledgeVO {

    private String id;

    private String title;

    private String type;

    private String question;

    private String content;

    @JsonFormat(shape = JsonFormat.Shape.BOOLEAN)
    private Boolean enabled;

    private String embeddingStatus;

    private String errorMsg;

    private String splitterType;

    private String modelId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;
}
