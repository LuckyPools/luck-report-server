package com.luck.report.web.utils;

import com.luck.report.web.common.domain.enums.HttpCodeEnum;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.web.i18n.ReportI18n;
import com.luck.report.web.modules.report.service.impl.DesignerService;
import org.apache.commons.lang3.StringUtils;

import com.luck.report.infra.modules.servlet.provider.ApiResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DownloadUtils {

    /**
     * 通过 DesignerService 解析报表的真实名称
     * 对于 db:xxx 类型的 reportPath，通过 provider 查询数据库获取报表标题；
     * 对于文件系统类型的 reportPath，通过 provider 获取文件名。
     *
     * @param designerService DesignerService 实例
     * @param reportPath        报表文件路径
     * @return 解析后的真实报表名称，解析失败时回退到 reportPath 本身
     */
    public static String resolveReportName(DesignerService designerService, String reportPath) {
        try {
            return designerService.resolveProvider(reportPath).getReportFile(reportPath).getName();
        } catch (Exception e) {
            return reportPath;
        }
    }

    /**
     * 构建下载文件名
     * 根据报表文件名和用户指定的文件名生成最终的下载文件名
     *
     * @param reportFileName 报表文件名，用于在用户未指定文件名时作为默认名称，类型：String，可为空
     * @param fileName       用户指定的文件名，类型：String，可为空
     * @param extName        文件扩展名（如 .pdf、.docx、.xlsx），类型：String，不可为空
     * @return 构建后的下载文件名（UTF-8编码），类型：String
     */
    public static String buildDownloadFileName(String reportFileName, String fileName, String extName) {
        StringBuilder result = new StringBuilder();
        if (StringUtils.isNotBlank(fileName)) {
            result.append(fileName);
            if (!fileName.toLowerCase().endsWith(extName)) {
                result.append(extName);
            }
        } else {
            String reportName = reportFileName == null ? "" : reportFileName;
            int pos = reportName.toLowerCase().indexOf(".ureport.xml");
            if (pos > 0) {
                reportName = reportName.substring(0, pos);
            }
            result.append(reportName).append(extName);
        }
        return result.toString();
    }

    /**
     * 构建下载响应头
     * 根据报表文件名和用户指定的文件名生成最终的下载文件名，并设置HTTP响应头
     * 使用 RFC 5987 标准解决中文文件名乱码问题
     *
     * @param response        HTTP响应对象，用于设置响应头，类型：ApiResponse，不可为空
     * @param reportFileName  报表文件名，用于在用户未指定文件名时作为默认名称，类型：String，可为空
     * @param fileName        用户指定的文件名，类型：String，可为空
     * @param extName         文件扩展名（如 .pdf、.docx、.xlsx），类型：String，不可为空
     * @return 构建后的下载文件名，类型：String
     */
    public static String buildDownloadHeader(ApiResponse response, String reportFileName, String fileName, String extName) {
        String downloadFileName = buildDownloadFileName(reportFileName, fileName, extName);

        try {
            String encodedFileName = URLEncoder.encode(downloadFileName, StandardCharsets.UTF_8.name())
                    .replaceAll("\\+", "%20");

            String fallbackFileName = new String(downloadFileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);

            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition",
                    "attachment;filename=\"" + fallbackFileName + "\";filename*=UTF-8''" + encodedFileName);
        } catch (UnsupportedEncodingException e) {
            response.setContentType("application/octet-stream;charset=UTF-8");
            response.setHeader("Content-Disposition", "attachment;filename=\"" + downloadFileName + "\"");
        }

        return downloadFileName;
    }


}
