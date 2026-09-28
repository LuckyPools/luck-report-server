package com.luck.report.web.modules.report.service.impl;

import com.luck.report.core.definition.ReportDefinition;
import com.luck.report.core.definition.ReportDefinitionWrapper;
import com.luck.report.core.excel.ExcelParseConfig;
import com.luck.report.core.exception.ReportException;
import com.luck.report.core.utils.ExcelToJsonUtil;
import com.luck.report.web.cache.ReportScopedCache;
import com.luck.report.web.i18n.ReportI18n;
import com.luck.report.web.modules.report.handler.ExcelParser;
import com.luck.report.web.modules.report.handler.HSSFExcelParser;
import com.luck.report.web.modules.report.handler.XSSFExcelParser;
import com.luck.report.web.utils.ReportUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel 导入服务，负责两类职责：
 * <ul>
 *   <li>导入 Excel 为报表定义并写入缓存</li>
 *   <li>Excel 转 JSON（静态数据集导入向导）：获取 Sheet 摘要、按配置解析为 JSON</li>
 * </ul>
 * <p>Bean 名：{@code bean.importExcelService}，避免与第三方系统 Bean 冲突。
 * <p>调用方：ImportExcelController#importExcel / getExcelSheet / parseExcelToJson。
 *
 * @author luck-report
 * @since 1.0.0
 */
@Service("bean.importExcelService")
public class ImportExcelService {

    private static final Logger logger = LoggerFactory.getLogger(ImportExcelService.class);

    private final List<ExcelParser> excelParsers = new ArrayList<>();

    public ImportExcelService() {
        excelParsers.add(new HSSFExcelParser());
        excelParsers.add(new XSSFExcelParser());
    }

    /**
     * 导入 Excel 文件并解析为报表定义。
     *
     * @return 导入结果，data 中包含 result 标志
     * @throws ReportException 文件格式非法或解析失败
     */
    public Map<String, Object> importExcel(MultipartFile file) {
        Map<String, Object> result = new HashMap<>();
        ReportDefinition reportDefinition = null;
        try {
            String fileName = file.getOriginalFilename();
            if (fileName != null
                    && (fileName.toLowerCase().endsWith(".xls") || fileName.toLowerCase().endsWith(".xlsx"))) {
                InputStream inputStream = file.getInputStream();
                try {
                    for (ExcelParser parser : excelParsers) {
                        if (parser.support(fileName)) {
                            reportDefinition = parser.parse(inputStream);
                            break;
                        }
                    }
                } finally {
                    inputStream.close();
                }
            } else {
                throw new ReportException("error.import.fileRequired");
            }
        } catch (Exception e) {
            logger.error("Import Excel Error: {}", e);
            throw new ReportException("error.import.parseFailedWithMsg", ReportI18n.messageOf(e));
        }

        if (reportDefinition != null) {
            result.put("result", true);
            ReportDefinitionWrapper wrapper = new ReportDefinitionWrapper(reportDefinition);
            String defaultTemplatePath = ReportUtils.getClassTemplatePath();
            ReportScopedCache.putObject(defaultTemplatePath, wrapper);
        } else {
            throw new ReportException("error.import.parseFailed");
        }
        return result;
    }

    /**
     * 获取 Excel 文件所有 Sheet 的摘要信息（索引 + 名称）
     *
     * @param file 上传的 Excel 文件，不可为空
     * @return Sheet 摘要列表，每项含 index 和 name
     * @throws ReportException 文件为空或解析失败时抛出
     */
    public List<Map<String, Object>> getExcelSheet(MultipartFile file) {
        validateExcelFile(file);
        try (InputStream inputStream = file.getInputStream()) {
            return ExcelToJsonUtil.getSheetSummaryList(inputStream);
        } catch (Exception e) {
            logger.error("获取Excel Sheet列表失败: {}", e.getMessage());
            throw new ReportException("error.import.sheetListFailed", ReportI18n.messageOf(e));
        }
    }

    /**
     * 按配置参数解析 Excel 为 JSON 字符串
     * <p>前端传入的行号参数（headerRowIndex/firstDataRowIndex/lastDataRowIndex）采用从 1 开始的语义，
     * 内部会转换为 POI 所需的从 0 开始的索引。</p>
     *
     * @param file              Excel 文件，不可为空
     * @param sheetIndex        指定解析的 Sheet 索引（从 0 开始，可为空），为空时默认读取第一个 Sheet
     * @param headerRowIndex    字段名行号（从 1 开始），默认 1
     * @param firstDataRowIndex 第一个数据行号（从 1 开始，可为空），为空时自动取字段名行的下一行
     * @param lastDataRowIndex  最后一个数据行号（从 1 开始，可为空），为空时解析到 Sheet 末尾
     * @param dateOrder         日期排序：DMY / YMD / MDY
     * @param dateSeparator     日期分隔符
     * @param timeSeparator     时间分隔符
     * @param decimalSymbol     小数点符号
     * @param dateTimeOrder     日期时间排序：DT / TD
     * @param outputDateFormat  输出 JSON 中的日期格式
     * @return JSON 数组字符串
     * @throws ReportException 文件为空、行号非法或解析失败时抛出
     */
    public String parseExcelToJson(MultipartFile file, Integer sheetIndex, int headerRowIndex,
                                   Integer firstDataRowIndex, Integer lastDataRowIndex,
                                   ExcelParseConfig.DateOrder dateOrder, String dateSeparator,
                                   String timeSeparator, String decimalSymbol,
                                   ExcelParseConfig.DateTimeOrder dateTimeOrder, String outputDateFormat) {
        validateExcelFile(file);
        validateRowIndexes(headerRowIndex, firstDataRowIndex, lastDataRowIndex);

        // 构建解析配置：将 1-based 行号转换为 POI 所需的 0-based 索引
        ExcelParseConfig config = new ExcelParseConfig();
        config.setSheetIndex(sheetIndex);
        config.setHeaderRowIndex(headerRowIndex - 1);
        config.setFirstDataRowIndex(firstDataRowIndex != null ? firstDataRowIndex - 1 : null);
        config.setLastDataRowIndex(lastDataRowIndex != null ? lastDataRowIndex - 1 : null);
        config.setDateOrder(dateOrder);
        config.setDateSeparator(dateSeparator);
        config.setTimeSeparator(timeSeparator);
        config.setDecimalSymbol(decimalSymbol);
        config.setDateTimeOrder(dateTimeOrder);
        config.setOutputDateFormat(outputDateFormat);

        try (InputStream inputStream = file.getInputStream()) {
            return ExcelToJsonUtil.parseToJson(inputStream, config);
        } catch (ReportException e) {
            // 业务异常直接抛出，保留原始错误信息
            throw e;
        } catch (IllegalStateException e) {
            logger.error("Excel格式错误: {}", e.getMessage());
            throw new ReportException("error.import.invalidFormat", ReportI18n.messageOf(e));
        } catch (Exception e) {
            logger.error("Excel解析失败: {}", e.getMessage());
            throw new ReportException("error.import.parseFailedWithMsg", ReportI18n.messageOf(e));
        }
    }

    /**
     * 校验上传的 Excel 文件非空且格式合法
     *
     * @param file 上传的文件
     * @throws ReportException 文件为空或格式非法时抛出
     */
    private void validateExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ReportException("error.import.fileEmpty");
        }
        String fileName = file.getOriginalFilename();
        if (fileName == null
                || (!fileName.toLowerCase().endsWith(".xlsx") && !fileName.toLowerCase().endsWith(".xls"))) {
            throw new ReportException("error.import.unsupportedExt");
        }
    }

    /**
     * 校验前端传入的行号参数合法（均需大于等于 1）
     *
     * @param headerRowIndex    字段名行号（从 1 开始）
     * @param firstDataRowIndex 第一个数据行号（从 1 开始，可为空）
     * @param lastDataRowIndex  最后一个数据行号（从 1 开始，可为空）
     * @throws ReportException 行号非法时抛出
     */
    private void validateRowIndexes(int headerRowIndex, Integer firstDataRowIndex, Integer lastDataRowIndex) {
        if (headerRowIndex < 1) {
            throw new ReportException("error.import.headerRowInvalid");
        }
        if (firstDataRowIndex != null && firstDataRowIndex < 1) {
            throw new ReportException("error.import.firstDataRowInvalid");
        }
        if (lastDataRowIndex != null && lastDataRowIndex < 1) {
            throw new ReportException("error.import.lastDataRowInvalid");
        }
    }
}
