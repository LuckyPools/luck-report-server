package com.luck.report.web.modules.report.controller.chart;

import com.luck.report.core.cache.ChartScopeCache;
import com.luck.report.core.chart.ChartData;
import com.luck.report.core.utils.UnitUtils;
import com.luck.report.web.common.domain.vo.ResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 图表控制器
 */
@RestController("bean.chartController")
@RequestMapping("${luck-report.servletPrefix:}/chart")
public class ChartController {

    private static final Logger logger = LoggerFactory.getLogger(ChartController.class);

    /**
     * 存储图表数据
     */
    @RequestMapping("/store_data")
    public ResultVO<Void> storeData(
            @RequestParam("_chartId") String chartId,
            @RequestParam("_base64Data") String base64Data,
            @RequestParam("_width") Integer width,
            @RequestParam("_height") Integer height,
            @RequestParam(value = "reportPath", required = false) String reportPath,
            @RequestParam(value = "mode", required = false) String mode) {

        ChartData chartData = ChartScopeCache.getChartData(chartId);
        if (chartData == null) {
            logger.warn("[chart-store] 缓存未命中, chartId={}, reportPath={}, base64长度={}", chartId, reportPath, base64Data == null ? 0 : base64Data.length());
            return ResultVO.success();
        }
        String prefix = "data:image/png;base64,";
        if (base64Data != null && base64Data.startsWith(prefix)) {
            base64Data = base64Data.substring(prefix.length());
        }
        chartData.setBase64Data(base64Data);
        chartData.setHeight(UnitUtils.pixelToPoint(height));
        chartData.setWidth(UnitUtils.pixelToPoint(width));
        ChartScopeCache.putChartData(chartId, chartData);
        logger.info("[chart-store] 回填成功, chartId={}, base64长度={}, width={}, height={}", chartId, base64Data.length(), width, height);
        return ResultVO.success();
    }

}
