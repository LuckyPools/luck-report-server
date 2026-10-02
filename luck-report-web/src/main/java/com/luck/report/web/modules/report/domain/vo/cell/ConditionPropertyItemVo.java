/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.domain.vo.cell;

import com.luck.report.core.definition.ConditionCellStyle;
import com.luck.report.core.definition.ConditionPaging;

import java.io.Serializable;
import java.util.List;

/**
 * ConditionPropertyItem的VO类，用于前端展示
 *
 * @author LuckyPools
 * @since 2026年
 */
public class ConditionPropertyItemVo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String name;
    private List<ConditionVo> conditions;
    private int rowHeight = -1;
    private int colWidth = -1;
    private String newValue;
    private Boolean renderFlag;
    private String linkUrl;
    private String linkTargetWindow;
    private List<LinkParameterVo> linkParameters;
    private ConditionCellStyle cellStyle;
    private ConditionPaging paging;
    private String expr;

    /**
     * 默认无参构造器
     */
    public ConditionPropertyItemVo() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<ConditionVo> getConditions() {
        return conditions;
    }

    public void setConditions(List<ConditionVo> conditions) {
        this.conditions = conditions;
    }

    public int getRowHeight() {
        return rowHeight;
    }

    public void setRowHeight(int rowHeight) {
        this.rowHeight = rowHeight;
    }

    public int getColWidth() {
        return colWidth;
    }

    public void setColWidth(int colWidth) {
        this.colWidth = colWidth;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }

    public Boolean getRenderFlag() {
        return renderFlag;
    }

    public void setRenderFlag(Boolean renderFlag) {
        this.renderFlag = renderFlag;
    }

    public String getLinkUrl() {
        return linkUrl;
    }

    public void setLinkUrl(String linkUrl) {
        this.linkUrl = linkUrl;
    }

    public String getLinkTargetWindow() {
        return linkTargetWindow;
    }

    public void setLinkTargetWindow(String linkTargetWindow) {
        this.linkTargetWindow = linkTargetWindow;
    }

    public List<LinkParameterVo> getLinkParameters() {
        return linkParameters;
    }

    public void setLinkParameters(List<LinkParameterVo> linkParameters) {
        this.linkParameters = linkParameters;
    }

    public ConditionCellStyle getCellStyle() {
        return cellStyle;
    }

    public void setCellStyle(ConditionCellStyle cellStyle) {
        this.cellStyle = cellStyle;
    }

    public ConditionPaging getPaging() {
        return paging;
    }

    public void setPaging(ConditionPaging paging) {
        this.paging = paging;
    }

    public String getExpr() {
        return expr;
    }

    public void setExpr(String expr) {
        this.expr = expr;
    }
}
