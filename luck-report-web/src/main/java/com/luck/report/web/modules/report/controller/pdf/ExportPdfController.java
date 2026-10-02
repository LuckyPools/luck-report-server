package com.luck.report.web.modules.report.controller.pdf;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.provider.ApiResponse;
import com.luck.report.infra.modules.servlet.utils.HttpUtils;
import com.luck.report.web.modules.report.service.impl.DesignerService;
import com.luck.report.web.modules.report.service.impl.ReportExportService;
import com.luck.report.web.utils.DownloadUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;

/**
 * PDF导出控制器
 */
@RestController("bean.exportPdfController")
@RequestMapping("${luck-report.servletPrefix:}/pdf")
public class ExportPdfController {

    @Autowired
    @Qualifier("bean.reportExportService")
    private ReportExportService reportExportService;

    @Autowired
    @Qualifier("bean.designerService")
    private DesignerService designerService;

    /**
     * 构建PDF报表（下载）
     */
    @RequestMapping("/build")
    public void build(@RequestParam("reportPath") String reportPath,
                      @RequestParam(value = "_n", required = false) String pdfName,
                      @RequestParam(value = "mode", required = false) String mode,
                      @RequestParam(value = "_paper", required = false) String paperJson) throws IOException {
        ApiRequest req = HttpUtils.getRequest();
        ApiResponse resp = HttpUtils.getResponse();
        String reportName = DownloadUtils.resolveReportName(designerService, reportPath);
        DownloadUtils.buildDownloadHeader(resp, reportName, pdfName, ".pdf");
        OutputStream outputStream = resp.getOutputStream();
        try {
            reportExportService.buildPdf(reportPath, mode, paperJson, req, outputStream);
        } finally {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        }
    }

    /**
     * 显示PDF报表（POST方式，支持传递纸张参数）
     */
    @RequestMapping("/show")
    public void show(@RequestParam("reportPath") String reportPath,
                     @RequestParam(value = "mode", required = false) String mode,
                     @RequestParam(value = "_paper", required = false) String paperJson) throws IOException {
        ApiRequest req = HttpUtils.getRequest();
        ApiResponse resp = HttpUtils.getResponse();
        resp.setContentType("application/pdf");
        OutputStream outputStream = resp.getOutputStream();
        try {
            reportExportService.buildPdf(reportPath, mode, paperJson, req, outputStream);
        } finally {
            if (outputStream != null) {
                outputStream.flush();
                outputStream.close();
            }
        }
    }
}
