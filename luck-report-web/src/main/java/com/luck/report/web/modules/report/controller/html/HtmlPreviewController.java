package com.luck.report.web.modules.report.controller.html;

import com.luck.report.infra.modules.servlet.provider.ApiRequest;
import com.luck.report.infra.modules.servlet.utils.HttpUtils;
import com.luck.report.web.common.domain.vo.ResultVO;
import com.luck.report.core.definition.Paper;
import com.luck.report.web.modules.report.domain.vo.report.HtmlReportVo;
import com.luck.report.web.modules.report.domain.vo.report.SearchFormOptionsVo;
import com.luck.report.web.modules.report.domain.vo.request.SearchFormOptionsRequest;
import com.luck.report.web.modules.report.service.impl.HtmlPreviewService;
import com.luck.report.web.modules.report.service.impl.SearchFormOptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * HTML 预览控制器
 * <p>仅负责 HTTP 请求 / 响应转换，所有业务逻辑委托给 {@link HtmlPreviewService}。
 */
@RestController("bean.htmlPreviewController")
@RequestMapping("${luck-report.servletPrefix:}/html")
public class HtmlPreviewController {

    @Autowired
    @Qualifier("bean.htmlPreviewService")
    private HtmlPreviewService htmlPreviewService;

    @Autowired
    @Qualifier("bean.searchFormOptionService")
    private SearchFormOptionService searchFormOptionService;

    /**
     * 加载 HTML 预览内容
     */
    @RequestMapping("/loadHtml")
    public ResultVO<HtmlReportVo> loadHtml(@RequestParam("reportPath") String reportPath,
                                           @RequestParam(value = "mode", required = false) String mode,
                                           @RequestParam(value = "_i", required = false) String pageIndex) {
        ApiRequest req = HttpUtils.getRequest();
        return ResultVO.success(htmlPreviewService.loadHtml(reportPath, mode, pageIndex, req));
    }

    /**
     * 加载打印页 HTML
     */
    @RequestMapping("/loadPrintPages")
    public ResultVO<Map<String, String>> loadPrintPages(@RequestParam(value = "mode", required = false) String mode,
                                                         @RequestParam("reportPath") String reportPath) {
        ApiRequest req = HttpUtils.getRequest();
        Map<String, String> map = new HashMap<>(2);
        map.put("html", htmlPreviewService.loadPrintPages(reportPath, mode, req));
        return ResultVO.success(map);
    }

    /**
     * 加载报表纸张信息
     */
    @RequestMapping("/loadPagePaper")
    public ResultVO<Paper> loadPagePaper(@RequestParam(value = "mode", required = false) String mode,
                                         @RequestParam("reportPath") String reportPath) {
        return ResultVO.success(htmlPreviewService.loadPagePaper(reportPath, mode));
    }

    /**
     * 加载数据（不渲染 HTML，只返回分页信息和图表数据）
     */
    @RequestMapping("/loadData")
    public ResultVO<HtmlReportVo> loadData(@RequestParam("reportPath") String reportPath,
                                           @RequestParam(value = "mode", required = false) String mode,
                                           @RequestParam(value = "_i", required = false) String pageIndex) {
        ApiRequest req = HttpUtils.getRequest();
        return ResultVO.success(htmlPreviewService.loadData(reportPath, mode, pageIndex, req));
    }

    /**
     * 批量加载查询表单选项：按报表文件 + 数据集引用执行数据集，返回 label/value 选项
     */
    @RequestMapping("/loadSearchFormOptions")
    public ResultVO<SearchFormOptionsVo> loadSearchFormOptions(@RequestBody SearchFormOptionsRequest request) {
        ApiRequest req = HttpUtils.getRequest();
        return ResultVO.success(searchFormOptionService.loadOptions(request, req));
    }
}
