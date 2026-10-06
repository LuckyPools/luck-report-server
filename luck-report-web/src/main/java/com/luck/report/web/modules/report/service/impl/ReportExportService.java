package com.luck.report.web.modules.report.service.impl;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.luck.report.core.build.ReportBuilder;
import com.luck.report.core.definition.Paper;
import com.luck.report.core.definition.ReportDefinition;
import com.luck.report.core.exception.ReportComputeException;
import com.luck.report.core.exception.ReportException;
import com.luck.report.core.export.ExportConfigure;
import com.luck.report.core.export.ExportConfigureImpl;
import com.luck.report.core.export.ExportManager;
import com.luck.report.core.export.ReportRender;
import com.luck.report.core.export.excel.high.ExcelProducer;
import com.luck.report.core.export.excel.low.Excel97Producer;
import com.luck.report.core.export.pdf.PdfProducer;
import com.luck.report.core.export.word.high.WordProducer;
import com.luck.report.core.model.Report;
import com.luck.report.web.modules.report.constant.ReportConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * 报表导出服务，统一处理 Excel / Excel97 / PDF / Word 导出业务。
 *
 * @author luck-report
 * @since 1.0.0
 */
@Service("bean.reportExportService")
public class ReportExportService {

    @Autowired
    @Qualifier("bean.reportBuilder")
    private ReportBuilder reportBuilder;

    @Autowired
    @Qualifier("bean.exportManager")
    private ExportManager exportManager;

    @Autowired
    @Qualifier("bean.reportRender")
    private ReportRender reportRender;

    @Autowired
    @Qualifier("bean.reportDefinitionService")
    private ReportDefinitionService reportDefinitionService;

    @Autowired
    @Qualifier("bean.paramService")
    private ParamService paramService;

    private final ExcelProducer excelProducer = new ExcelProducer();
    private final Excel97Producer excel97Producer = new Excel97Producer();
    private final PdfProducer pdfProducer = new PdfProducer();
    private final WordProducer wordProducer = new WordProducer();

    /**
     * 预览时从缓存取报表定义，须在写下载头之前调用；其它模式返回 null
     *
     * @param mode 导出模式
     * @param reportPath 报表路径
     * @return 预览报表定义；非预览为 null
     */
    public ReportDefinition getPreviewDefinition(String mode, String reportPath) {
        if (!ReportConstants.MODE_KEY.equals(mode)) {
            return null;
        }
        if (StringUtils.isBlank(reportPath)) {
            throw new ReportComputeException("error.report.fileNull");
        }
        return reportDefinitionService.getReportDefinition(reportPath);
    }

    /**
     * 构建 Excel (xlsx) 报表。
     *
     * @param definition 预览定义，非预览传 null
     */
    public void buildExcel(String reportPath, String mode, ApiRequest req, OutputStream outputStream,
                           boolean withPage, boolean withSheet, ReportDefinition definition) throws IOException {
        if (StringUtils.isBlank(reportPath)) {
            throw new ReportComputeException("error.report.fileNull");
        }
        try {
            Map<String, Object> parameters = paramService.buildAllParameters(req);
            if (ReportConstants.MODE_KEY.equals(mode)) {
                if (definition == null) {
                    throw new ReportComputeException("error.report.dataExpired");
                }
                Report report = reportBuilder.buildReport(definition, parameters);
                if (withPage) {
                    excelProducer.produceWithPaging(report, outputStream);
                } else if (withSheet) {
                    excelProducer.produceWithSheet(report, outputStream);
                } else {
                    excelProducer.produce(report, outputStream);
                }
            } else {
                ExportConfigure configure = new ExportConfigureImpl(reportPath, parameters, outputStream);
                if (withPage) {
                    exportManager.exportExcelWithPaging(configure);
                } else if (withSheet) {
                    exportManager.exportExcelWithPagingSheet(configure);
                } else {
                    exportManager.exportExcel(configure);
                }
            }
        } catch (Exception ex) {
            throw new ReportException(ex);
        }
    }

    /**
     * 构建 Excel97 (xls) 报表。
     *
     * @param definition 预览定义，非预览传 null
     */
    public void buildExcel97(String reportPath, String mode, ApiRequest req, OutputStream outputStream,
                             boolean withPage, boolean withSheet, ReportDefinition definition) throws IOException {
        Map<String, Object> parameters = paramService.buildAllParameters(req);
        if (ReportConstants.MODE_KEY.equals(mode)) {
            if (definition == null) {
                throw new ReportComputeException("error.report.dataExpired");
            }
            Report report = reportBuilder.buildReport(definition, parameters);
            if (withPage) {
                excel97Producer.produceWithPaging(report, outputStream);
            } else if (withSheet) {
                excel97Producer.produceWithSheet(report, outputStream);
            } else {
                excel97Producer.produce(report, outputStream);
            }
        } else {
            ExportConfigure configure = new ExportConfigureImpl(reportPath, parameters, outputStream);
            if (withPage) {
                exportManager.exportExcelWithPaging(configure);
            } else if (withSheet) {
                exportManager.exportExcelWithPagingSheet(configure);
            } else {
                exportManager.exportExcel(configure);
            }
        }
    }

    /**
     * 构建 PDF 报表。
     *
     * @param definition 预览定义，非预览传 null
     */
    public void buildPdf(String reportPath, String mode, String paperJson, ApiRequest req,
                         OutputStream outputStream, ReportDefinition definition) throws IOException {
        try {
            Map<String, Object> parameters = paramService.buildAllParameters(req);
            ReportDefinition reportDefinition;
            if (ReportConstants.MODE_KEY.equals(mode)) {
                if (definition == null) {
                    throw new ReportComputeException("error.report.dataExpired");
                }
                reportDefinition = definition;
            } else {
                reportDefinition = reportRender.getReportDefinition(reportPath);
            }

            Report report = reportBuilder.buildReport(reportDefinition, parameters);
            if (paperJson != null && !paperJson.isEmpty()) {
                ObjectMapper mapper = new ObjectMapper();
                Paper newPaper = mapper.readValue(paperJson, Paper.class);
                report.rePaging(newPaper);
            }

            pdfProducer.produce(report, outputStream);
        } catch (Exception ex) {
            throw new ReportException(ex);
        }
    }

    /**
     * 构建 Word 报表。
     *
     * @param definition 预览定义，非预览传 null
     */
    public void buildWord(String reportPath, String mode, ApiRequest req, OutputStream outputStream,
                          ReportDefinition definition) throws IOException {
        try {
            Map<String, Object> parameters = paramService.buildAllParameters(req);
            if (ReportConstants.MODE_KEY.equals(mode)) {
                if (definition == null) {
                    throw new ReportComputeException("error.report.dataExpired");
                }
                Report report = reportBuilder.buildReport(definition, parameters);
                wordProducer.produce(report, outputStream);
            } else {
                ExportConfigure configure = new ExportConfigureImpl(reportPath, parameters, outputStream);
                exportManager.exportWord(configure);
            }
        } catch (Exception ex) {
            throw new ReportException(ex);
        }
    }
}
