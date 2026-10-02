/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.domain.vo.report;

import com.luck.report.core.definition.*;
import com.luck.report.core.definition.searchform.SearchForm;
import com.luck.report.web.modules.report.converter.DefinitionVoConverter;
import com.luck.report.web.modules.report.domain.vo.cell.CellDefinitionVo;
import com.luck.report.web.modules.report.domain.vo.datasource.DatasourceDefinitionVo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 报表定义VO类，用于前端展示
 *
 * @author Jacky.gao
 * @since 2017年1月29日
 */
public class ReportDefinitionVo {
    private String reportName;
    private final Paper paper;
    private final HeaderFooterDefinition header;
    private final HeaderFooterDefinition footer;
    private final List<RowDefinition> rows;
    private final List<ColumnDefinition> columns;
    private final List<DatasourceDefinitionVo> datasources;
    private final Map<String, CellDefinitionVo> cellsMap = new HashMap<String, CellDefinitionVo>();
    private final List<FloatImage> floatImages;
    private final List<FloatText> floatTexts;
    private SearchForm searchForm;
    /**
     * 预览工具栏配置（字段名用 tools 与前端 reportDef.tools 对齐；未配置时为 null）
     */
    private Tool tools;

    /**
     * 构造函数，将 ReportDefinition 转换为 ReportDefinitionVo
     * @param report 报表定义
     */
    public ReportDefinitionVo(ReportDefinition report, String reportName) {
        this.reportName = reportName;
        this.paper = report.getPaper();
        this.header = report.getHeader();
        this.footer = report.getFooter();
        this.searchForm = report.getSearchForm();
        this.rows = report.getRows();
        this.columns = report.getColumns();
        this.datasources = DefinitionVoConverter.toDatasourceVoList(report.getDatasources());
        this.floatImages = report.getFloatImages();
        this.floatTexts = report.getFloatTexts();
        this.tools = report.getTool();
        for (CellDefinition cell : report.getCells()) {
            CellDefinitionVo cellVo = DefinitionVoConverter.toVo(cell);
            cellsMap.put(cell.getRowNumber() + "," + cell.getColumnNumber(), cellVo);
        }
    }

    public List<ColumnDefinition> getColumns() {
        return columns;
    }

    public List<DatasourceDefinitionVo> getDatasources() {
        return datasources;
    }

    public HeaderFooterDefinition getFooter() {
        return footer;
    }

    public HeaderFooterDefinition getHeader() {
        return header;
    }

    public Paper getPaper() {
        return paper;
    }

    public SearchForm getSearchForm() {
        return searchForm;
    }

    public void setSearchForm(SearchForm searchForm) {
        this.searchForm = searchForm;
    }

    public Map<String, CellDefinitionVo> getCellsMap() {
        return cellsMap;
    }

    public List<RowDefinition> getRows() {
        return rows;
    }

    public String getReportName() {
        return reportName;
    }

    public List<FloatImage> getFloatImages() {
        return floatImages;
    }

    public List<FloatText> getFloatTexts() {
        return floatTexts;
    }

    public Tool getTools() {
        return tools;
    }

    public void setTools(Tool tools) {
        this.tools = tools;
    }
}
