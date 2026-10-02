package com.luck.report.core.utils;

import com.luck.report.core.excel.ExcelParseConfig;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Excel 转 JSON 工具类
 *
 * @author luck-report
 * @since 2.0.5
 */
public final class ExcelToJsonUtil {

    private ExcelToJsonUtil() {
    }

    /**
     * 解析 Excel 为 JSON 字符串
     *
     * @param inputStream Excel 文件输入流，不可为空
     * @param config      解析配置，为 null 时使用默认配置
     * @return JSON 数组字符串
     * @throws Exception Excel 读取或解析失败时抛出
     */
    public static String parseToJson(InputStream inputStream, ExcelParseConfig config) throws Exception {
        List<Map<String, Object>> dataList = parseToList(inputStream, config);
        return JsonUtils.toJson(dataList);
    }

    /**
     * 解析 Excel 为 List<Map>，便于序列化前二次加工
     *
     * @param inputStream Excel 文件输入流，不可为空
     * @param config      解析配置，为 null 时使用默认配置
     * @return 数据行列表，每行一个 LinkedHashMap（保持表头列顺序）
     * @throws Exception Excel 读取或解析失败时抛出
     */
    public static List<Map<String, Object>> parseToList(InputStream inputStream, ExcelParseConfig config) throws Exception {
        Objects.requireNonNull(inputStream, "InputStream不能为空");
        if (config == null) {
            config = new ExcelParseConfig();
        }

        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = resolveSheet(workbook, config);

            Row headerRow = sheet.getRow(config.getHeaderRowIndex());
            if (headerRow == null) {
                throw new IllegalStateException("找不到字段名行: " + config.getHeaderRowIndex());
            }
            List<String> headers = readHeaders(headerRow);

            int firstData = config.getActualFirstDataRow();
            int lastData = config.getLastDataRowIndex() != null
                    ? Math.min(config.getLastDataRowIndex(), sheet.getLastRowNum())
                    : sheet.getLastRowNum();

            DateTimeFormatter inputDateTimeFmt = buildDateTimeFormatter(config);
            DateTimeFormatter outputDateFmt = DateTimeFormatter.ofPattern(config.getOutputDateFormat());

            return readDataRows(sheet, headers, firstData, lastData, config, inputDateTimeFmt, outputDateFmt);
        }
    }

    /**
     * 读取表头行所有单元格的字符串值作为字段名
     *
     * @param headerRow 表头行，不可为空
     * @return 字段名列表
     */
    private static List<String> readHeaders(Row headerRow) {
        List<String> headers = new ArrayList<>();
        for (int c = 0; c < headerRow.getLastCellNum(); c++) {
            Cell cell = headerRow.getCell(c);
            headers.add(getCellStringValue(cell));
        }
        return headers;
    }

    /**
     * 遍历数据行范围提取单元格值并组装为 List<Map>
     *
     * @param sheet           目标 Sheet
     * @param headers         字段名列表
     * @param firstData       起始数据行号（含）
     * @param lastData        结束数据行号（含）
     * @param config          解析配置
     * @param inputDateTimeFmt 输入日期时间格式化器
     * @param outputDateFmt   输出日期格式化器
     * @return 数据行列表，跳过空行
     */
    private static List<Map<String, Object>> readDataRows(Sheet sheet, List<String> headers,
                                                          int firstData, int lastData, ExcelParseConfig config,
                                                          DateTimeFormatter inputDateTimeFmt, DateTimeFormatter outputDateFmt) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = firstData; i <= lastData; i++) {
            Row row = sheet.getRow(i);
            if (row == null || isEmptyRow(row)) {
                continue;
            }
            Map<String, Object> rowData = buildRowData(row, headers, config, inputDateTimeFmt, outputDateFmt);
            if (!rowData.isEmpty()) {
                result.add(rowData);
            }
        }
        return result;
    }

    /**
     * 组装单行数据为 Map，跳过空列名
     *
     * @param row             数据行
     * @param headers         字段名列表
     * @param config          解析配置
     * @param inputDateTimeFmt 输入日期时间格式化器
     * @param outputDateFmt   输出日期格式化器
     * @return 单行数据 Map（LinkedHashMap 保持列顺序），仅含非 null 值
     */
    private static Map<String, Object> buildRowData(Row row, List<String> headers, ExcelParseConfig config,
                                                    DateTimeFormatter inputDateTimeFmt, DateTimeFormatter outputDateFmt) {
        Map<String, Object> rowData = new LinkedHashMap<>();
        for (int c = 0; c < headers.size(); c++) {
            String key = headers.get(c);
            if (key == null || key.trim().isEmpty()) {
                continue;
            }
            Cell cell = row.getCell(c);
            Object value = extractCellValue(cell, config, inputDateTimeFmt, outputDateFmt);
            if (value != null) {
                rowData.put(key, value);
            }
        }
        return rowData;
    }

    /**
     * 根据配置解析目标 Sheet；优先级：sheetIndex > sheetName > 默认第一个 Sheet
     *
     * @param workbook Excel Workbook
     * @param config   解析配置
     * @return 目标 Sheet
     */
    private static Sheet resolveSheet(Workbook workbook, ExcelParseConfig config) {
        if (config.getSheetIndex() != null) {
            int idx = config.getSheetIndex();
            if (idx < 0 || idx >= workbook.getNumberOfSheets()) {
                throw new IllegalArgumentException(
                        String.format("Sheet索引越界: %d, 有效范围: 0~%d", idx, workbook.getNumberOfSheets() - 1));
            }
            return workbook.getSheetAt(idx);
        }
        if (config.getSheetName() != null && !config.getSheetName().trim().isEmpty()) {
            Sheet sheet = workbook.getSheet(config.getSheetName().trim());
            if (sheet == null) {
                throw new IllegalArgumentException("找不到指定Sheet: " + config.getSheetName());
            }
            return sheet;
        }
        if (workbook.getNumberOfSheets() == 0) {
            throw new IllegalStateException("Excel文件中没有任何Sheet");
        }
        return workbook.getSheetAt(0);
    }

    /**
     * 提取单元格值并做类型推断（数字/日期/布尔/字符串）
     *
     * @param cell            单元格，可为 null
     * @param config          解析配置（用于小数符号、日期格式等）
     * @param inputDateTimeFmt 输入日期时间格式化器
     * @param outputDateFmt   输出日期格式化器
     * @return 单元格值，可能为 Long/BigDecimal/String/Boolean/null
     */
    private static Object extractCellValue(Cell cell, ExcelParseConfig config,
                                           DateTimeFormatter inputDateTimeFmt, DateTimeFormatter outputDateFmt) {
        if (cell == null) {
            return null;
        }
        CellType cellType = cell.getCellType();
        if (cellType == CellType.FORMULA) {
            cellType = cell.getCachedFormulaResultType();
        }

        switch (cellType) {
            case NUMERIC:
                return extractNumericValue(cell, outputDateFmt);
            case STRING:
                return extractStringValue(cell, config, inputDateTimeFmt, outputDateFmt);
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case BLANK:
                return null;
            default:
                return cell.toString();
        }
    }

    /**
     * 提取数值型单元格值，区分日期与纯数字
     *
     * @param cell         数值型单元格
     * @param outputDateFmt 输出日期格式化器
     * @return 日期字符串（POI 识别为日期）或 Long/BigDecimal（纯数字）
     */
    private static Object extractNumericValue(Cell cell, DateTimeFormatter outputDateFmt) {
        if (DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue().format(outputDateFmt);
        }
        double numVal = cell.getNumericCellValue();
        if (numVal == Math.floor(numVal) && !Double.isInfinite(numVal)) {
            return (long) numVal;
        }
        return BigDecimal.valueOf(numVal);
    }

    /**
     * 提取字符串型单元格值，尝试转换为数字或日期
     *
     * @param cell            字符串型单元格
     * @param config          解析配置（用于小数符号替换）
     * @param inputDateTimeFmt 输入日期时间格式化器
     * @param outputDateFmt   输出日期格式化器
     * @return Long/BigDecimal（数字）、日期字符串（日期）、原始字符串（其他）
     */
    private static Object extractStringValue(Cell cell, ExcelParseConfig config,
                                            DateTimeFormatter inputDateTimeFmt, DateTimeFormatter outputDateFmt) {
        String strVal = cell.getStringCellValue().trim();
        if (strVal.isEmpty()) {
            return null;
        }
        String normalizedStr = normalizeDecimalSymbol(strVal, config.getDecimalSymbol());

        try {
            if (normalizedStr.contains(".")) {
                return new BigDecimal(normalizedStr);
            }
            return Long.parseLong(normalizedStr);
        } catch (NumberFormatException ignored) {
        }

        try {
            LocalDateTime ldt = LocalDateTime.parse(strVal, inputDateTimeFmt);
            return ldt.format(outputDateFmt);
        } catch (DateTimeParseException ignored) {
        }
        return strVal;
    }

    /**
     * 根据配置的小数符号对字符串做归一化，转换为标准小数点；当配置小数符号为 "," 时，"." 视为千分位需移除，"," 转为 "."
     *
     * @param strVal        原始字符串
     * @param decimalSymbol 配置的小数符号
     * @return 归一化后的数字字符串
     */
    private static String normalizeDecimalSymbol(String strVal, String decimalSymbol) {
        String normalizedStr = strVal;
        if (!".".equals(decimalSymbol)) {
            normalizedStr = strVal.replace(decimalSymbol, ".");
        }
        if (",".equals(decimalSymbol)) {
            normalizedStr = normalizedStr.replace(".", "").replace(",", ".");
        }
        return normalizedStr;
    }

    /**
     * 根据配置构建输入日期时间格式化器
     *
     * @param config 解析配置
     * @return 日期时间格式化器
     */
    private static DateTimeFormatter buildDateTimeFormatter(ExcelParseConfig config) {
        String dSep = config.getDateSeparator();
        String tSep = config.getTimeSeparator();

        String datePart;
        switch (config.getDateOrder()) {
            case DMY:
                datePart = "dd" + dSep + "MM" + dSep + "yyyy";
                break;
            case MDY:
                datePart = "MM" + dSep + "dd" + dSep + "yyyy";
                break;
            case YMD:
            default:
                datePart = "yyyy" + dSep + "MM" + dSep + "dd";
                break;
        }
        String timePart = "HH" + tSep + "mm" + tSep + "ss";

        String pattern;
        switch (config.getDateTimeOrder()) {
            case TD:
                pattern = timePart + " " + datePart;
                break;
            case DT:
            default:
                pattern = datePart + " " + timePart;
                break;
        }
        return DateTimeFormatter.ofPattern(pattern).withLocale(Locale.ROOT);
    }

    /**
     * 获取单元格的字符串展示值（用于表头读取）
     *
     * @param cell 单元格，可为 null
     * @return 字符串值，null 返回空串
     */
    private static String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            double val = cell.getNumericCellValue();
            if (val == Math.floor(val)) {
                return String.valueOf((long) val);
            }
        }
        return cell.toString().trim();
    }

    /**
     * 判断数据行是否为空行（所有单元格为空或空字符串）
     *
     * @param row 数据行
     * @return true 表示空行
     */
    private static boolean isEmptyRow(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell == null || cell.getCellType() == CellType.BLANK) {
                continue;
            }
            if (cell.getCellType() == CellType.STRING) {
                if (!cell.getStringCellValue().trim().isEmpty()) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * 获取 Excel 文件中所有 Sheet 的摘要信息（索引 + 名称）；适用于前端 Sheet 下拉选择
     *
     * @param inputStream Excel 文件输入流，不可为空
     * @return Sheet 摘要信息列表，每项含 index 和 name
     * @throws Exception Excel 读取失败时抛出
     */
    public static List<Map<String, Object>> getSheetSummaryList(InputStream inputStream) throws Exception {
        Objects.requireNonNull(inputStream, "InputStream不能为空");
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            List<Map<String, Object>> summaryList = new ArrayList<>();
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                Map<String, Object> sheetInfo = new LinkedHashMap<>();
                sheetInfo.put("index", i);
                sheetInfo.put("name", workbook.getSheetName(i));
                summaryList.add(sheetInfo);
            }
            return summaryList;
        }
    }
}
