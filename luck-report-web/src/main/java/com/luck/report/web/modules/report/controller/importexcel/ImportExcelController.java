package com.luck.report.web.modules.report.controller.importexcel;

import com.luck.report.core.excel.ExcelParseConfig;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.web.modules.report.service.impl.ImportExcelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Excel 导入控制器
 *
 * @author luck-report
 * @since 1.0.0
 */
@RestController("bean.importExcelController")
@RequestMapping("${luck-report.servletPrefix:}/import")
public class ImportExcelController {

    @Autowired
    @Qualifier("bean.importExcelService")
    private ImportExcelService importExcelService;

    /**
     * 导入 Excel 文件并解析为报表定义
     *
     * @param file 上传的 Excel 文件，参数名 _excel_file
     * @return 导入结果
     */
    @PostMapping("/import_excel")
    public ResultVO<Map<String, Object>> importExcel(@RequestParam("_excel_file") MultipartFile file) {
        return ResultVO.success(importExcelService.importExcel(file));
    }

    /**
     * 获取 Excel 文件所有 Sheet 的摘要信息（索引 + 名称）
     *
     * @param file 上传的 Excel 文件，参数名 _excel_file
     * @return Sheet 摘要列表，每项含 index 和 name
     */
    @PostMapping("/get_sheet")
    public ResultVO<List<Map<String, Object>>> getExcelSheet(@RequestParam("_excel_file") MultipartFile file) {
        return ResultVO.success(importExcelService.getExcelSheet(file));
    }

    /**
     * 按配置参数解析 Excel 为 JSON 字符串
     *
     * @param file              上传的 Excel 文件，参数名 _excel_file
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
     */
    @PostMapping("/parse_json")
    public ResultVO<String> parseExcelToJson(
            @RequestParam("_excel_file") MultipartFile file,
            @RequestParam(value = "sheetIndex", required = false) Integer sheetIndex,
            @RequestParam(value = "headerRowIndex", defaultValue = "1") int headerRowIndex,
            @RequestParam(value = "firstDataRowIndex", required = false) Integer firstDataRowIndex,
            @RequestParam(value = "lastDataRowIndex", required = false) Integer lastDataRowIndex,
            @RequestParam(value = "dateOrder", defaultValue = "YMD") ExcelParseConfig.DateOrder dateOrder,
            @RequestParam(value = "dateSeparator", defaultValue = "/") String dateSeparator,
            @RequestParam(value = "timeSeparator", defaultValue = ":") String timeSeparator,
            @RequestParam(value = "decimalSymbol", defaultValue = ".") String decimalSymbol,
            @RequestParam(value = "dateTimeOrder", defaultValue = "DT") ExcelParseConfig.DateTimeOrder dateTimeOrder,
            @RequestParam(value = "outputDateFormat", defaultValue = "yyyy-MM-dd HH:mm:ss") String outputDateFormat) {
        String json = importExcelService.parseExcelToJson(file, sheetIndex, headerRowIndex,
                firstDataRowIndex, lastDataRowIndex, dateOrder, dateSeparator,
                timeSeparator, decimalSymbol, dateTimeOrder, outputDateFormat);
        return ResultVO.success(json);
    }
}
