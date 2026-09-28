package com.luck.report.web.modules.report.domain.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 报表元数据更新 DTO
 * 用于接收前端更新报表名称等元数据的请求参数。
 * 刻意不定义模板内容字段：本接口只允许修改元数据，
 * 模板内容必须走设计器保存链路，防止通过本入口篡改模板数据。
 *
 * @author luck
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportUpdateDTO {

    /** 报表完整路径（带 provider 前缀，例如 db:123），必填 */
    @NotBlank(message = "报表文件路径不能为空")
    private String file;

    /** 报表名称，必填 */
    @NotBlank(message = "报表名称不能为空")
    @Size(max = 100, message = "报表名称长度不能超过100")
    private String title;
}
