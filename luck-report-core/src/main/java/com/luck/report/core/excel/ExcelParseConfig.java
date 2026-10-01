package com.luck.report.core.excel;

/**
 * Excel 转 JSON 解析配置
 *
 * @author luck-report
 * @since 2.0.5
 */
public class ExcelParseConfig {

    /**
     * 指定解析的 Sheet 索引（从 0 开始）
     * <p>优先级高于 sheetName，为 null 时降级使用 sheetName</p>
     */
    private Integer sheetIndex;

    /**
     * 指定解析的 Sheet 名称
     * <p>当 sheetIndex 为 null 时生效，为 null 时默认读取第一个 Sheet</p>
     */
    private String sheetName;

    /**
     * 字段名(列名)所在行号，从 0 开始，默认 0
     */
    private int headerRowIndex = 0;

    /**
     * 第一个数据行号，从 0 开始，为 null 时默认 = headerRowIndex + 1
     */
    private Integer firstDataRowIndex;

    /**
     * 最后一个数据行号(含)，从 0 开始，null 表示到 Sheet 末尾
     */
    private Integer lastDataRowIndex;

    /**
     * 日期排序：DMY(日/月/年)、YMD(年/月/日)、MDY(月/日/年)
     */
    private DateOrder dateOrder = DateOrder.YMD;

    /**
     * 日期分隔符，默认 "/"
     */
    private String dateSeparator = "/";

    /**
     * 时间分隔符，默认 ":"
     */
    private String timeSeparator = ":";

    /**
     * 小数点符号，默认 "."，部分地区为 ","
     */
    private String decimalSymbol = ".";

    /**
     * 日期时间组合排序：DT(日期在前)、TD(时间在前)
     */
    private DateTimeOrder dateTimeOrder = DateTimeOrder.DT;

    /**
     * 输出到 JSON 时的标准日期格式，默认 "yyyy-MM-dd HH:mm:ss"
     */
    private String outputDateFormat = "yyyy-MM-dd HH:mm:ss";

    public Integer getSheetIndex() {
        return sheetIndex;
    }

    public void setSheetIndex(Integer sheetIndex) {
        this.sheetIndex = sheetIndex;
    }

    public String getSheetName() {
        return sheetName;
    }

    public void setSheetName(String sheetName) {
        this.sheetName = sheetName;
    }

    public int getHeaderRowIndex() {
        return headerRowIndex;
    }

    public void setHeaderRowIndex(int headerRowIndex) {
        this.headerRowIndex = headerRowIndex;
    }

    public Integer getFirstDataRowIndex() {
        return firstDataRowIndex;
    }

    public void setFirstDataRowIndex(Integer firstDataRowIndex) {
        this.firstDataRowIndex = firstDataRowIndex;
    }

    public Integer getLastDataRowIndex() {
        return lastDataRowIndex;
    }

    public void setLastDataRowIndex(Integer lastDataRowIndex) {
        this.lastDataRowIndex = lastDataRowIndex;
    }

    public DateOrder getDateOrder() {
        return dateOrder;
    }

    public void setDateOrder(DateOrder dateOrder) {
        this.dateOrder = dateOrder;
    }

    public String getDateSeparator() {
        return dateSeparator;
    }

    public void setDateSeparator(String dateSeparator) {
        this.dateSeparator = dateSeparator;
    }

    public String getTimeSeparator() {
        return timeSeparator;
    }

    public void setTimeSeparator(String timeSeparator) {
        this.timeSeparator = timeSeparator;
    }

    public String getDecimalSymbol() {
        return decimalSymbol;
    }

    public void setDecimalSymbol(String decimalSymbol) {
        this.decimalSymbol = decimalSymbol;
    }

    public DateTimeOrder getDateTimeOrder() {
        return dateTimeOrder;
    }

    public void setDateTimeOrder(DateTimeOrder dateTimeOrder) {
        this.dateTimeOrder = dateTimeOrder;
    }

    public String getOutputDateFormat() {
        return outputDateFormat;
    }

    public void setOutputDateFormat(String outputDateFormat) {
        this.outputDateFormat = outputDateFormat;
    }

    /**
     * 计算实际第一个数据行号，未配置时取表头行 + 1
     *
     * @return 实际第一个数据行号
     */
    public int getActualFirstDataRow() {
        return firstDataRowIndex != null ? firstDataRowIndex : headerRowIndex + 1;
    }

    /**
     * 日期字段各部分排列顺序
     */
    public enum DateOrder {DMY, YMD, MDY}

    /**
     * 日期时间组合排列顺序
     */
    public enum DateTimeOrder {DT, TD}
}
