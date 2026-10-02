package com.luck.report.web.modules.report.controller.designer;

import com.luck.report.web.common.domain.vo.PageResultVO;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.core.expression.ErrorInfo;
import com.luck.report.core.provider.report.ReportFile;
import com.luck.report.web.modules.report.domain.dto.ReportQueryDTO;
import com.luck.report.web.modules.report.domain.vo.report.ReportDefinitionVo;
import com.luck.report.web.modules.report.domain.vo.report.ReportProviderDetailVo;
import com.luck.report.web.modules.report.domain.vo.report.ReportProviderVo;
import com.luck.report.web.modules.report.service.impl.DesignerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 报表设计器控制器
 *
 * @author Jacky.gao
 * @since 2017年1月25日
 */
@Controller("bean.designerController")
@RequestMapping("${luck-report.servletPrefix:}/designer")
public class DesignerController {

    @Autowired
    @Qualifier("bean.designerService")
    private DesignerService designerService;

    /**
     * 脚本验证
     */
    @RequestMapping("/validate_script")
    @ResponseBody
    public ResultVO<List<ErrorInfo>> scriptValidation(@RequestParam("content") String content) {
        return ResultVO.success(designerService.scriptValidation(content));
    }

    /**
     * 条件脚本验证
     */
    @RequestMapping("/validate_condition_script")
    @ResponseBody
    public ResultVO<List<ErrorInfo>> conditionScriptValidation(@RequestParam("content") String content) {
        return ResultVO.success(designerService.conditionScriptValidation(content));
    }

    /**
     * 解析数据集名称
     */
    @RequestMapping("/parse_dataset_name")
    @ResponseBody
    public ResultVO<Map<String, String>> parseDatasetName(@RequestParam("expr") String expr) {
        Map<String, String> result = new java.util.HashMap<>(2);
        result.put("datasetName", designerService.parseDatasetName(expr));
        return ResultVO.success(result);
    }

    /**
     * 保存预览文件
     */
    @RequestMapping("/save_preview_report")
    @ResponseBody
    public ResultVO<Void> savePreviewFile(@RequestParam("reportPath") String reportPath,
                                          @RequestParam("content") String content) throws IOException {
        designerService.savePreviewFile(reportPath, content);
        return ResultVO.success();
    }

    /**
     * 加载报表
     */
    @RequestMapping(value = "/load_report")
    @ResponseBody
    public ResultVO<ReportDefinitionVo> loadReport(@RequestParam("reportPath") String reportPath) {
        return ResultVO.success(designerService.loadReport(reportPath));
    }

    /**
     * 解析报表 XML 为定义 JSON（不落盘、不写缓存）。
     *
     * @param content 报表 XML 全文
     * @param reportPath 可选上下文路径，仅用于解析日志/命名，不触发读写
     */
    @RequestMapping("/parse_xml")
    @ResponseBody
    public ResultVO<ReportDefinitionVo> parseReportXml(
            @RequestParam("content") String content,
            @RequestParam(value = "reportPath", required = false) String reportPath) {
        return ResultVO.success(designerService.parseReportXml(content, reportPath));
    }

    /**
     * 删除报表文件
     */
    @RequestMapping("/delete_report")
    @ResponseBody
    public ResultVO<Void> deleteReportFile(@RequestParam("reportPath") String reportPath) {
        designerService.deleteReportFile(reportPath);
        return ResultVO.success();
    }

    /**
     * 保存报表文件
     */
    @RequestMapping("/save_report")
    @ResponseBody
    public ResultVO<ReportFile> saveReportFile(@RequestParam(value = "fileName", required = false) String fileName,
                                         @RequestParam("reportPath") String reportPath,
                                         @RequestParam("content") String content) {
        ReportFile reportFile = designerService.saveReportFile(fileName, reportPath, content);
        return ResultVO.success(reportFile);
    }

    /**
     * 加载所有已启用的报表提供者元数据列表。
     */
    @RequestMapping("/load_providers")
    @ResponseBody
    public ResultVO<List<ReportProviderVo>> loadReportProviders() {
        return ResultVO.success(designerService.listReportProviders());
    }

    /**
     * 分页查询报表列表
     */
    @PostMapping("/page_reports")
    @ResponseBody
    public PageResultVO<ReportFile> queryReports(@Valid @RequestBody ReportQueryDTO queryDTO) {
        return designerService.queryReports(queryDTO);
    }

    /**
     * 加载每个 provider 在指定路径下的报表文件列表（含目录）。
     */
    @RequestMapping("/load_reports")
    @ResponseBody
    public ResultVO<List<ReportProviderDetailVo>> loadReportFiles(@RequestParam("path") String path) {
        return ResultVO.success(designerService.loadReportFiles(path));
    }

    /**
     * 新建报表
     */
    @RequestMapping("/create_report")
    @ResponseBody
    public ResultVO<ReportFile> createReport(@RequestParam("fileName") String fileName,
                                             @RequestParam("provider") String providerPrefix) {
        return designerService.createReport(fileName, providerPrefix);
    }

    /**
     * 复制报表
     */
    @RequestMapping("/copy_report")
    @ResponseBody
    public ResultVO<ReportFile> copyReport(@RequestParam("sourceFilePath") String sourceFilePath,
                                          @RequestParam("newFilePath") String newFilePath,
                                          @RequestParam(value = "newTitle", required = false) String newTitle) {
        return designerService.copyReport(sourceFilePath, newFilePath, newTitle);
    }
}
