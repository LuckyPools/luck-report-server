package com.luck.report.web.modules.report.domain.vo.report;

import com.luck.report.core.provider.report.ReportFile;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 报表提供者详情 VO。
 *
 * @author luck-report
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ReportProviderDetailVo extends ReportProviderVo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 指定路径下的报表文件列表（含目录）。
     */
    private List<ReportFile> reportFiles;

    public ReportProviderDetailVo(String name, String prefix, boolean disabled, List<ReportFile> reportFiles) {
        super(name, prefix, disabled);
        this.reportFiles = reportFiles;
    }
}
