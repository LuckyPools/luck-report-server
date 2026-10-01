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
 * <p>仅负责 HTTP 请求 / 响应转换，所有业务逻辑委托给 {@link DesignerService}。
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
     * - reportPath: 报表唯一路径（如 file:xxx.ureport.xml / db:123），作为 ReportScopedCache 的 key
     * - content: 报表 XML 内容
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
     * <p>供 Virtual 引擎：校验 XML 并转换为与 {@link #loadReport} 同构的前端定义。
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
     * - title: 报表展示名（db: provider 用作 title，file: provider 忽略）
     * - reportPath: 报表唯一路径（带 provider 前缀），如 file:xxx.ureport.xml / db:123
     * - content: 报表 XML 内容
     *
     * 兼容旧版：仅传 file 时，title 缺省为空字符串
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
     * <p>仅返回 provider 基础信息（name/prefix/disabled），不包含任何文件。
     * 用于：管理页下拉、报表来源过滤等"仅需 provider 元数据"的场景。
     */
    @RequestMapping("/load_providers")
    @ResponseBody
    public ResultVO<List<ReportProviderVo>> loadReportProviders() {
        return ResultVO.success(designerService.listReportProviders());
    }

    /**
     * 分页查询报表列表
     * <p>用于设计器的"打开报表"弹窗、"另存为"弹窗等需要分页加载报表的场景。
     */
    @PostMapping("/page_reports")
    @ResponseBody
    public PageResultVO<ReportFile> queryReports(@Valid @RequestBody ReportQueryDTO queryDTO) {
        return designerService.queryReports(queryDTO);
    }

    /**
     * 加载每个 provider 在指定路径下的报表文件列表（含目录）。
     * <p>响应结构：{@code List<ReportProviderDetailVo>}，与 {@link #loadReportProviders()} 形式一致；
     * 前端按 {@code vo.prefix} 识别 provider。
     * 用于：设计器的"打开报表"弹窗、"另存为"弹窗等需要展示文件树的场景。
     */
    @RequestMapping("/load_reports")
    @ResponseBody
    public ResultVO<List<ReportProviderDetailVo>> loadReportFiles(@RequestParam("path") String path) {
        return ResultVO.success(designerService.loadReportFiles(path));
    }

    /**
     * 新建报表
     * - 接收 fileName（报表名，含 .ureport.xml 后缀）与 provider（报表来源前缀，例如 file:）
     * - 使用 classpath:template/template.ureport.xml 空白模板在指定 provider 下创建报表
     * - 完整文件路径 = provider + fileName（例如 file:xxx.ureport.xml）
     */
    @RequestMapping("/create_report")
    @ResponseBody
    public ResultVO<ReportFile> createReport(@RequestParam("fileName") String fileName,
                                             @RequestParam("provider") String providerPrefix) {
        return designerService.createReport(fileName, providerPrefix);
    }

    /**
     * 复制报表
     * - sourceFilePath: 源报表完整路径（带 provider 前缀），如 file:xxx.ureport.xml / db:123
     * - newFilePath: 新报表完整路径（带 provider 前缀），如 file:xxx_copy.ureport.xml / db:xxx_copy
     * - newTitle: 新报表展示名（db: provider 用作 title）
     *
     * 注意：newFilePath 必须与 sourceFilePath 属于同一 provider。
     */
    @RequestMapping("/copy_report")
    @ResponseBody
    public ResultVO<ReportFile> copyReport(@RequestParam("sourceFilePath") String sourceFilePath,
                                          @RequestParam("newFilePath") String newFilePath,
                                          @RequestParam(value = "newTitle", required = false) String newTitle) {
        return designerService.copyReport(sourceFilePath, newFilePath, newTitle);
    }
}
