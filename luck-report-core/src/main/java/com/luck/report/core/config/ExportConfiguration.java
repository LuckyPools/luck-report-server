package com.luck.report.core.config;

import com.luck.report.core.build.ReportBuilder;
import com.luck.report.core.export.ExportManagerImpl;
import com.luck.report.core.export.ReportRender;
import com.luck.report.core.export.pdf.font.FontBuilder;
import com.luck.report.core.parser.ReportParser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ExportConfiguration {

    @Bean("bean.exportManager")
    public ExportManagerImpl exportManager(
            @Qualifier("bean.reportRender") ReportRender reportRender) {
        ExportManagerImpl exportManager = new ExportManagerImpl();
        exportManager.setReportRender(reportRender);
        return exportManager;
    }

    @Bean("bean.reportRender")
    public ReportRender reportRender(
            @Qualifier("bean.reportParser") ReportParser reportParser,
            @Qualifier("bean.reportBuilder") ReportBuilder reportBuilder) {
        ReportRender reportRender = new ReportRender();
        reportRender.setReportParser(reportParser);
        reportRender.setReportBuilder(reportBuilder);
        return reportRender;
    }

    @Bean("bean.fontBuilder")
    public FontBuilder fontBuilder() {
        return new FontBuilder();
    }
}
