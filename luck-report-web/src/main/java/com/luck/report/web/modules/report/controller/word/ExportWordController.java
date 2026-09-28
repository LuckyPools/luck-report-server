package com.luck.report.web.modules.report.controller.word;

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
 * Word导出控制器
 * <p>仅负责 HTTP 请求 / 响应转换，业务逻辑委托给 {@link ReportExportService}。
 */
@RestController("bean.exportWordController")
@RequestMapping("${luck-report.servletPrefix:}/word")
public class ExportWordController {

    @Autowired
    @Qualifier("bean.reportExportService")
    private ReportExportService reportExportService;

    @Autowired
    @Qualifier("bean.designerService")
    private DesignerService designerService;

    /**
     * 构建Word报表
     */
    @RequestMapping("/build")
    public void build(@RequestParam("reportPath") String reportPath,
                      @RequestParam(value = "mode", required = false) String mode,
                      @RequestParam(value = "_n", required = false) String wordName) throws IOException {
        ApiRequest req = HttpUtils.getRequest();
        ApiResponse resp = HttpUtils.getResponse();
        String reportName = DownloadUtils.resolveReportName(designerService, reportPath);
        DownloadUtils.buildDownloadHeader(resp, reportName, wordName, ".docx");
        OutputStream outputStream = resp.getOutputStream();
        try {
            reportExportService.buildWord(reportPath, mode, req, outputStream);
        } finally {
            outputStream.flush();
            outputStream.close();
        }
    }
}
