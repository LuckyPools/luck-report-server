package com.luck.report.web.modules.file.domain.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.luck.report.web.common.domain.entity.DataEntity;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 报表文件实体类
 *
 * @author luck
 */
@Data
@NoArgsConstructor
public class ReportTemplate extends DataEntity<ReportTemplate> {

    /**
     * 报表标题
     */
    private String title;

    /**
     * 报表模板内容（XML）
     */
    private String template;
}
