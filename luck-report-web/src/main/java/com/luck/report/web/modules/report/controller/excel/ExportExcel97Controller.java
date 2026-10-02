package com.luck.report.web.modules.report.controller.excel;

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
 * Excel 97-2003导出控制器
 */
@RestController("bean.exportExcel97Controller")
@RequestMapping("${luck-report.servletPrefix:}/excel97")
public class ExportExcel97Controller {

    @Autowired
    @Qualifier("bean.reportExportService")
    private ReportExportService reportExportService;

    @Autowired
    @Qualifier("bean.designerService")
    private DesignerService designerService;

    @RequestMapping("/build")
    public void build(@RequestParam("reportPath") String reportPath,
                      @RequestParam(value = "mode", required = false) String mode,
                      @RequestParam(value = "_n", required = false) String excelName) throws IOException {
        ApiRequest req = HttpUtils.getRequest();
        ApiResponse resp = HttpUtils.getResponse();
        buildExcel(reportPath, mode, excelName, req, resp, false, false);
    }

    @RequestMapping("/paging")
    public void paging(@RequestParam("reportPath") String reportPath,
                       @RequestParam(value = "mode", required = false) String mode,
                       @RequestParam(value = "_n", required = false) String excelName) throws IOException {
        ApiRequest req = HttpUtils.getRequest();
        ApiResponse resp = HttpUtils.getResponse();
        buildExcel(reportPath, mode, excelName, req, resp, true, false);
    }

    @RequestMapping("/sheet")
    public void sheet(@RequestParam("reportPath") String reportPath,
                      @RequestParam(value = "mode", required = false) String mode,
                      @RequestParam(value = "_n", required = false) String excelName) throws IOException {
        ApiRequest req = HttpUtils.getRequest();
        ApiResponse resp = HttpUtils.getResponse();
        buildExcel(reportPath, mode, excelName, req, resp, false, true);
    }

    private void buildExcel(String reportPath, String mode, String excelName,
                            ApiRequest req, ApiResponse resp,
                            boolean withPage, boolean withSheet) throws IOException {
        String reportName = DownloadUtils.resolveReportName(designerService, reportPath);
        DownloadUtils.buildDownloadHeader(resp, reportName, excelName, ".xls");
        OutputStream outputStream = resp.getOutputStream();
        try {
            reportExportService.buildExcel97(reportPath, mode, req, outputStream, withPage, withSheet);
        } finally {
            outputStream.flush();
            outputStream.close();
        }
    }
}
