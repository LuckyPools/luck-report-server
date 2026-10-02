package com.luck.report.web.modules.report.service.impl;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.core.provider.report.ReportFile;
import com.luck.report.core.provider.report.ReportFilePage;
import com.luck.report.core.provider.report.ReportProvider;
import com.luck.report.web.modules.report.domain.dto.ReportQueryDTO;
import com.luck.report.web.modules.report.domain.dto.ReportUpdateDTO;
import com.luck.report.web.modules.report.domain.enums.ReportImportType;
import com.luck.report.web.modules.report.domain.vo.report.ReportExportTemplateVo;
import com.luck.report.web.utils.ReportIdXmlUtils;
import com.luck.report.web.utils.TransformLuckReportV1Utils;
import com.luck.report.web.utils.TransformUReportUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.luck.report.core.exception.ReportBizException;

/**
 * 报表管理服务，负责报表的查询与删除等管理类业务。
 *
 * @author luck-report
 * @since 1.0.0
 */
@Service("bean.reportManageService")
public class ReportManageService implements ApplicationContextAware {

    private static final Logger logger = LoggerFactory.getLogger(ReportManageService.class);

    /**
     * 系统中所有启用的 ReportProvider 列表。
     */
    private final List<ReportProvider> reportProviders = new ArrayList<>();

    /**
     * 分页查询报表列表。
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    public PageResultVO<ReportFile> queryReports(ReportQueryDTO queryDTO) {
        try {
            String provider = queryDTO.getProvider();
            String reportName = queryDTO.getReportName();
            String directory = queryDTO.getDirectory();
            int pageNum = queryDTO.getPageNum() == null ? 1 : queryDTO.getPageNum();
            int pageSize = queryDTO.getPageSize() == null ? 10 : queryDTO.getPageSize();

            ReportProvider targetProvider = findProviderByPrefix(provider);
            if (targetProvider == null) {
                return PageResultVO.error("Report provider not found");
            }

            Map<String, Object> params = new HashMap<>(4);
            if (directory != null && !directory.isEmpty()) {
                params.put("path", directory);
            }
            if (reportName != null && !reportName.isEmpty()) {
                params.put("name", reportName);
            }
            params.put("includeDirectory", Boolean.FALSE);

            ReportFilePage result = targetProvider.pageReportFiles(pageNum, pageSize, params);
            List<ReportFile> records = result == null ? Collections.emptyList() : result.getRecords();
            long total = result == null ? 0L : result.getTotal();
            return PageResultVO.success(records, total, pageNum, pageSize);
        } catch (Exception e) {
            logger.error("查询报表列表异常", e);
            return PageResultVO.error("Failed to query report list: " + e.getMessage());
        }
    }

    /**
     * 删除报表。
     *
     * @param file 报表完整路径（带 provider 前缀）
     */
    public ResultVO<Void> deleteReport(String file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResultVO.error(400, "Report file path cannot be empty");
            }

            ReportProvider targetProvider = null;
            String providerPrefix = null;
            for (ReportProvider provider : reportProviders) {
                if (file.startsWith(provider.getPrefix())) {
                    targetProvider = provider;
                    providerPrefix = provider.getPrefix();
                    break;
                }
            }

            if (targetProvider == null) {
                return ResultVO.error(400, "Report provider not found");
            }

            String actualPath = file.substring(providerPrefix.length());
            String correctPath = file;

            logger.info("删除报表 - 原始路径: {}, 实际路径: {}, 正确路径: {}, provider: {}",
                    file, actualPath, correctPath, providerPrefix);

            try {
                targetProvider.deleteReport(correctPath);
                logger.info("删除报表成功: {}", correctPath);
            } catch (Exception e) {
                logger.warn("使用正确路径删除失败，尝试使用实际路径: {}", actualPath, e);
                targetProvider.deleteReport(actualPath);
                logger.info("删除报表成功（使用实际路径）: {}", actualPath);
            }

            return ResultVO.success();
        } catch (Exception e) {
            logger.error("删除报表异常: {}", file, e);
            return ResultVO.error(500, "Failed to delete report: " + e.getMessage());
        }
    }

    /**
     * 导入报表模板。按 XML reportId 识别
     */
    public ResultVO<ReportFile> importTemplate(String providerPrefix, String fileName, String content,
                                               boolean confirmOverwrite, ReportImportType importType) {
        try {
            if (providerPrefix == null || providerPrefix.trim().isEmpty()) {
                return ResultVO.error(400, "Report provider cannot be empty");
            }
            if (fileName == null || fileName.trim().isEmpty()) {
                return ResultVO.error(400, "File name cannot be empty");
            }
            if (content == null) {
                return ResultVO.error(400, "File content cannot be empty");
            }
            providerPrefix = providerPrefix.trim();
            fileName = fileName.trim();
            if (fileName.contains("..") || fileName.contains("/") || fileName.contains("\\")) {
                return ResultVO.error(400, "Invalid file name: " + fileName);
            }
            if (!fileName.toLowerCase().endsWith(ReportProvider.REPORT_FILE_SUFFIX)) {
                fileName = fileName + ReportProvider.REPORT_FILE_SUFFIX;
            }

            ReportProvider provider;
            try {
                provider = resolveProvider(providerPrefix + "probe");
            } catch (Exception e) {
                return ResultVO.error(404, "Report provider not found: " + providerPrefix);
            }

            content = transformImportContent(content, importType == null ? ReportImportType.V2 : importType);

            String titleFromFile = ReportProvider.stripReportSuffix(fileName);
            String reportId = ReportIdXmlUtils.readReportId(content);

            if (StringUtils.isNotBlank(reportId)) {
                String reportPath = providerPrefix + reportId;
                ReportFile existing = null;
                try {
                    existing = provider.getReportFile(reportPath);
                } catch (Exception ignore) {
                    existing = null;
                }
                if (existing != null) {
                    if (!confirmOverwrite) {
                        String existTitle = existing.getName() == null ? reportId : existing.getName();
                        return ResultVO.error(409,
                                "Report \"" + existTitle + "\" already exists; continuing import will overwrite the template in the database",
                                existing);
                    }
                    String synced = ReportIdXmlUtils.ensureReportId(content, reportId);
                    ReportFile savedFile = provider.saveReport("", reportPath, synced);
                    if (savedFile == null) {
                        return ResultVO.error(500, "Provider returned empty ReportFile after import.");
                    }
                    return ResultVO.success("Imported(overwrite)", savedFile);
                }
                String synced = ReportIdXmlUtils.ensureReportId(content, reportId);
                ReportFile savedFile = provider.saveReport(titleFromFile, reportPath, synced);
                if (savedFile == null) {
                    return ResultVO.error(500, "Provider returned empty ReportFile after import.");
                }
                return ResultVO.success("Imported", savedFile);
            }

            String reportPath = providerPrefix + fileName;
            ReportFile savedFile = provider.saveReport(titleFromFile, reportPath, content);
            if (savedFile == null) {
                return ResultVO.error(500, "Provider returned empty ReportFile after import.");
            }
            return ResultVO.success("Imported", savedFile);
        } catch (Exception e) {
            logger.error("导入报表异常: {} / {}", providerPrefix, fileName, e);
            return ResultVO.error(500, "Failed to import report: " + e.getMessage());
        }
    }

    public ResultVO<ReportFile> importTemplate(String providerPrefix, String fileName, String content) {
        return importTemplate(providerPrefix, fileName, content, false, ReportImportType.V2);
    }

    public ResultVO<ReportFile> importTemplate(String providerPrefix, String fileName, String content,
                                               boolean confirmOverwrite) {
        return importTemplate(providerPrefix, fileName, content, confirmOverwrite, ReportImportType.V2);
    }

    /**
     * 按导入类型转换报表 XML
     *
     * @param content 原始 XML
     * @param importType 导入类型，非空
     * @return 转换后的 XML；V2 原样返回
     */
    private String transformImportContent(String content, ReportImportType importType) {
        switch (importType) {
            case V1:
                return TransformLuckReportV1Utils.transformXmlContent(content);
            case UREPORT:
                return TransformUReportUtils.transformXmlContent(content);
            case V2:
            default:
                return content;
        }
    }

    /**
     * 导出报表
     *
     * @param reportPath 报表完整路径（带 provider 前缀），如 file:xxx.ureport.xml / db:123
     * @return 包含下载文件名与字节内容的结果
     */
    public ResultVO<ReportExportTemplateVo> exportTemplate(String reportPath) {
        try {
            if (reportPath == null || reportPath.trim().isEmpty()) {
                return ResultVO.error(400, "Report file path cannot be empty");
            }
            reportPath = reportPath.trim();
            ReportProvider provider;
            try {
                provider = resolveProvider(reportPath);
            } catch (Exception e) {
                return ResultVO.error(404, "Report provider not found: " + reportPath);
            }
            String downloadName;
            try {
                ReportFile rf = provider.getReportFile(reportPath);
                downloadName = (rf != null && rf.getName() != null) ? rf.getName() : null;
            } catch (Exception ignore) {
                downloadName = null;
            }
            if (downloadName == null || downloadName.isEmpty()) {
                int slash = Math.max(reportPath.lastIndexOf('/'), reportPath.lastIndexOf(':'));
                downloadName = (slash >= 0 ? reportPath.substring(slash + 1) : reportPath);
                if (!downloadName.toLowerCase().endsWith(ReportProvider.REPORT_FILE_SUFFIX)) {
                    downloadName = downloadName + ReportProvider.REPORT_FILE_SUFFIX;
                }
            }

            InputStream in = null;
            byte[] bytes;
            try {
                in = provider.loadReport(reportPath);
                if (in == null) {
                    return ResultVO.error(404, "Report content is empty: " + reportPath);
                }
                bytes = IOUtils.toByteArray(in);
            } finally {
                IOUtils.closeQuietly(in);
            }
            String content = new String(bytes, StandardCharsets.UTF_8);
            String id = reportPath;
            int colon = reportPath.lastIndexOf(':');
            if (colon >= 0 && colon < reportPath.length() - 1) {
                id = reportPath.substring(colon + 1);
            }
            if (StringUtils.isNotBlank(id)) {
                content = ReportIdXmlUtils.ensureReportId(content, id);
                bytes = content.getBytes(StandardCharsets.UTF_8);
            }
            return ResultVO.success("Exported", new ReportExportTemplateVo(downloadName, bytes));
        } catch (Exception e) {
            logger.error("导出报表异常: {}", reportPath, e);
            return ResultVO.error(500, "Failed to export report: " + e.getMessage());
        }
    }

    /**
     * 按完整路径取报表元数据（不含模板 XML）
     */
    public ResultVO<ReportFile> getReport(String file) {
        try {
            if (file == null || file.trim().isEmpty()) {
                return ResultVO.error(400, "Report file path cannot be empty");
            }
            file = file.trim();
            ReportProvider provider;
            try {
                provider = resolveProvider(file);
            } catch (Exception e) {
                return ResultVO.error(404, "Report provider not found: " + file);
            }
            ReportFile reportFile = provider.getReportFile(file);
            if (reportFile == null) {
                return ResultVO.error(404, "Report not found: " + file);
            }
            return ResultVO.success("Loaded", reportFile);
        } catch (Exception e) {
            logger.error("查询报表详情异常: {}", file, e);
            return ResultVO.error(500, "Failed to get report detail: " + e.getMessage());
        }
    }

    /**
     * 更新报表元数据（名称等描述性信息）。
     *
     * @param updateDTO 更新参数（file + title，title 已通过 Bean Validation 校验）
     * @return 更新后的 ReportFile；provider 不支持元数据更新时返回错误码
     */
    public ResultVO<ReportFile> updateReport(ReportUpdateDTO updateDTO) {
        try {
            String file = updateDTO.getFile();
            String title = updateDTO.getTitle();
            if (file == null || file.trim().isEmpty()) {
                return ResultVO.error(400, "Report file path cannot be empty");
            }
            if (title == null || title.trim().isEmpty()) {
                return ResultVO.error(400, "Report name cannot be empty");
            }
            file = file.trim();
            title = title.trim();

            ReportProvider provider;
            try {
                provider = resolveProvider(file);
            } catch (Exception e) {
                return ResultVO.error(404, "Report provider not found: " + file);
            }

            ReportFile updated = provider.updateReport(file, title);
            if (updated == null) {
                return ResultVO.error(400, "Current report provider does not support updating report metadata");
            }
            logger.info("更新报表元数据成功: {} -> {}", file, title);
            return ResultVO.success("Updated", updated);
        } catch (Exception e) {
            logger.error("更新报表元数据异常: {}", updateDTO.getFile(), e);
            return ResultVO.error(500, "Failed to update report metadata: " + e.getMessage());
        }
    }

    /**
     * 根据文件路径匹配对应的 ReportProvider，找不到时抛 IllegalStateException。
     */
    private ReportProvider resolveProvider(String reportPath) {
        for (ReportProvider p : reportProviders) {
            String prefix = p.getPrefix();
            if (prefix != null && reportPath.startsWith(prefix)) {
                return p;
            }
        }
        throw new ReportBizException("error.report.providerNotFound", reportPath);
    }

    /**
     * 根据 prefix 查找对应的 ReportProvider。
     */
    private ReportProvider findProviderByPrefix(String prefix) {
        if (prefix == null) {
            return null;
        }
        for (ReportProvider p : reportProviders) {
            if (prefix.equals(p.getPrefix())) {
                return p;
            }
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (!reportProviders.isEmpty()) {
            return;
        }
        for (ReportProvider provider : applicationContext.getBeansOfType(ReportProvider.class).values()) {
            if (provider.disabled() || provider.getName() == null) {
                continue;
            }
            reportProviders.add(provider);
        }
        logger.info("报表管理服务初始化完成,共加载 {} 个报表来源", reportProviders.size());
    }

    /**
     * 暴露给同包或同模块的内部辅助
     */
    public List<ReportProvider> getReportProviders() {
        return Collections.unmodifiableList(reportProviders);
    }
}
