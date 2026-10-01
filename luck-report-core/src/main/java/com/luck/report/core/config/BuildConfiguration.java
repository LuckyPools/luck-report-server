package com.luck.report.core.config;

import com.luck.report.core.build.HideRowColumnBuilder;
import com.luck.report.core.build.ReportBuilder;
import com.luck.report.core.parser.ReportParser;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BuildConfiguration {

    @Bean("bean.reportBuilder")
    public ReportBuilder reportBuilder(
            @Qualifier("bean.hideRowColumnBuilder") HideRowColumnBuilder hideRowColumnBuilder) {
        ReportBuilder reportBuilder = new ReportBuilder();
        reportBuilder.setHideRowColumnBuilder(hideRowColumnBuilder);
        return reportBuilder;
    }

    @Bean("bean.hideRowColumnBuilder")
    public HideRowColumnBuilder hideRowColumnBuilder() {
        return new HideRowColumnBuilder();
    }

    @Bean("bean.reportParser")
    public ReportParser reportParser() {
        return new ReportParser();
    }
}
