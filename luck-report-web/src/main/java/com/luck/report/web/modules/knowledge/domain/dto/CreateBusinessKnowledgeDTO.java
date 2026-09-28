package com.luck.report.web.modules.knowledge.domain.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 创建业务知识DTO
 *
 * @author luck
 */
@Data
public class CreateBusinessKnowledgeDTO {

    @NotBlank(message = "知识标题不能为空")
    private String title;

    @NotBlank(message = "知识类型不能为空")
    private String type;

    private String question;

    private String content;

    private MultipartFile file;

    private String splitterType;

    @NotNull(message = "嵌入模型不能为空")
    private String modelId;
}
