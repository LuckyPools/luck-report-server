package com.luck.report.web.modules.report.domain.vo.report;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 报表提供者（Provider）视图对象。
 *
 * @author luck-report
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
public class ReportProviderVo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String name;
    private String prefix;
    private boolean disabled;

    public ReportProviderVo(String name, String prefix, boolean disabled) {
        this.name = name;
        this.prefix = prefix;
        this.disabled = disabled;
    }
}
