/*******************************************************************************
 * Copyright 2017 Bstek
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.luck.report.core.export.html;

import com.luck.report.core.build.BindData;
import com.luck.report.core.build.Context;
import com.luck.report.core.build.paging.Page;
import com.luck.report.core.chart.ChartData;
import com.luck.report.core.definition.Alignment;
import com.luck.report.core.definition.Border;
import com.luck.report.core.definition.CellStyle;
import com.luck.report.core.expression.model.Expression;
import com.luck.report.core.expression.model.data.BindDataListExpressionData;
import com.luck.report.core.expression.model.data.ExpressionData;
import com.luck.report.core.expression.model.data.ObjectExpressionData;
import com.luck.report.core.expression.model.data.ObjectListExpressionData;
import com.luck.report.core.expression.utils.ExpressionReturns;
import com.luck.report.core.model.*;
import com.luck.report.core.utils.HtmlRowHeightUtils;
import com.luck.report.core.utils.NumberUtils;
import com.luck.report.core.utils.UnitUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author Jacky.gao
 * @since 2016年12月30日
 */
public class HtmlProducer {
    public String produce(Report report) {
        List<Row> rows = report.getRows();
        List<Column> columns = report.getColumns();
        Map<Row, Map<Column, Cell>> cellMap = report.getRowColCellMap();
        StringBuilder sb = buildTable(report.getContext(), rows, columns, cellMap, false, false);
        return sb.toString();
    }

    public String produce(Context context, List<Page> pages, int columnMargin, boolean breakPage) {
        int pageSize = pages.size();
        int singleTableWidth = buildTableWidthPx(pages.get(0).getColumns());
        int tableWidth = singleTableWidth * pageSize + UnitUtils.pointToPixel(columnMargin) * (pageSize - 1);
        String bgStyle = "";
        String bgImage = context.getReport().getPaper().getBgImage();
        if (StringUtils.isNotBlank(bgImage)) {
            bgStyle = ";background:url(" + bgImage + ") no-repeat";
        }
        StringBuilder sb = new StringBuilder();
        if (breakPage) {
            sb.append("<table border='0' class='page-break' style='margin:auto;border-collapse:collapse;table-layout:fixed;width:" + tableWidth + "px" + bgStyle + "'>");
        } else {
            sb.append("<table border='0' class='page-break' style='margin:auto;border-collapse:collapse;table-layout:fixed;width:" + tableWidth + "px" + bgStyle + "'>");
        }
        sb.append("<tr>");
        for (int i = 0; i < pageSize; i++) {
            if (i > 0) {
                sb.append("<td style='width:" + UnitUtils.pointToPixel(columnMargin) + "px'></td>");
            }
            Page page = pages.get(i);
            String table = produce(context, page, false);
            sb.append("<td style='width:" + singleTableWidth + "px;vertical-align:top'>");
            sb.append(table);
            sb.append("</td>");
        }
        sb.append("</tr>");
        sb.append("</table>");
        return sb.toString();
    }

    public String produce(Context context, Page page, boolean breakPage) {
        List<Row> rows = page.getRows();
        List<Column> columns = page.getColumns();
        Map<Row, Map<Column, Cell>> cellMap = context.getReport().getRowColCellMap();
        StringBuilder sb = buildTable(context, rows, columns, cellMap, breakPage, true);
        return sb.toString();
    }

    private StringBuilder buildTable(Context context, List<Row> rows, List<Column> columns, Map<Row, Map<Column, Cell>> cellMap, boolean breakPage, boolean forPage) {
        StringBuilder sb = new StringBuilder();
        int tableWidth = buildTableWidthPx(columns);
        String bgStyle = "";
        String bgImage = context.getReport().getPaper().getBgImage();
        if (StringUtils.isNotBlank(bgImage)) {
            bgStyle = ";background:url(" + bgImage + ") no-repeat";
        }
        if (breakPage) {
            sb.append("<table class='page-break' border='0' style='margin:auto;border-collapse:collapse;table-layout:fixed;width:" + tableWidth + "px" + bgStyle + "'>");
        } else {
            sb.append("<table border='0' style='margin:auto;border-collapse:collapse;table-layout:fixed;width:" + tableWidth + "px" + bgStyle + "'>");
        }
        int colSize = columns.size();
        sb.append("<colgroup>");
        for (int j = 0; j < colSize; j++) {
            Column colDef = columns.get(j);
            if (colDef.isHiddenFormLayout()) {
                continue;
            }
            sb.append("<col style='width:" + UnitUtils.pointToPixel(colDef.getWidth()) + "px'>");
        }
        sb.append("</colgroup>");
        int rowSize = rows.size();
        for (int i = 0; i < rowSize; i++) {
            Row row = rows.get(i);
            if (!forPage && row.isForPaging()) {
                continue;
            }
            int height = row.getRealHeight();
            if (row.isHiddenFormLayout()) {
                continue;
            }
            int heightPx = UnitUtils.pointToPixel(height);
            sb.append("<tr style=\"height:" + heightPx + "px\">");
            for (int j = 0; j < colSize; j++) {
                Column col = columns.get(j);
                if (col.isHiddenFormLayout()) {
                    continue;
                }
                Cell cell = null;
                if (cellMap.containsKey(row)) {
                    Map<Column, Cell> colMap = cellMap.get(row);
                    if (colMap.containsKey(col)) {
                        cell = colMap.get(col);
                    }
                }
                if (cell == null || (!forPage && cell.isForPaging())) {
                    continue;
                }
                int colSpan = cell.getLayoutColSpan();
                int rowSpan = forPage ? cell.getLayoutPageRowSpan() : cell.getLayoutRowSpan();
                int engineRowSpan = forPage ? cell.getPageRowSpan() : cell.getRowSpan();
                if (rowSpan > 0) {
                    if (colSpan > 0) {
                        sb.append("<td rowspan=\"" + rowSpan + "\" colspan=\"" + colSpan + "\"");
                    } else {
                        sb.append("<td rowspan=\"" + rowSpan + "\"");
                    }
                } else {
                    if (colSpan > 0) {
                        sb.append("<td colspan=\"" + colSpan + "\"");
                    } else {
                        sb.append("<td");
                    }
                }
                sb.append(buildCellClass(cell));
                int cellHeightPx = engineRowSpan > 1
                        ? buildHeightPx(rows, i, engineRowSpan)
                        : heightPx;
                String style = buildCustomStyle(cell, columns, j, cellHeightPx);
                sb.append(" " + style + "");
                sb.append(">");
                boolean hasLink = false;
                String linkURL = cell.getLinkUrl();
                if (StringUtils.isNotBlank(linkURL) && cell.isRenderFlag()) {
                    Expression urlExpression = cell.getLinkUrlExpression();
                    if (urlExpression != null) {
                        ExpressionData<?> exprData = urlExpression.execute(cell, cell, context);
                        exprData = ExpressionReturns.unwrap(exprData);
                        if (exprData instanceof BindDataListExpressionData) {
                            BindDataListExpressionData listExprData = (BindDataListExpressionData) exprData;
                            List<BindData> bindDataList = listExprData.getData();
                            if (bindDataList != null && bindDataList.size() > 0) {
                                Object data = bindDataList.get(0).getValue();
                                if (data != null) {
                                    linkURL = data.toString();
                                }
                            }
                        } else if (exprData instanceof ObjectExpressionData) {
                            ObjectExpressionData objExprData = (ObjectExpressionData) exprData;
                            Object data = objExprData.getData();
                            if (data != null) {
                                linkURL = data.toString();
                            }
                        } else if (exprData instanceof ObjectListExpressionData) {
                            ObjectListExpressionData objListExprData = (ObjectListExpressionData) exprData;
                            List<?> list = objListExprData.getData();
                            if (list != null && list.size() > 0) {
                                Object data = list.get(0);
                                if (data != null) {
                                    linkURL = data.toString();
                                }
                            }
                        }
                    }
                    hasLink = true;
                    String urlParameter = cell.buildLinkParameters(context);
                    if (StringUtils.isNotBlank(urlParameter)) {
                        if (linkURL.indexOf("?") == -1) {
                            linkURL += "?" + urlParameter;
                        } else {
                            linkURL += "&" + urlParameter;
                        }
                    }
                    String target = cell.getLinkTargetWindow();
                    if (StringUtils.isBlank(target)) target = "_self";
                    sb.append("<a href=\"" + linkURL + "\" target=\"" + target + "\">");
                }
                Object obj = "";
                if (cell.isRenderFlag() && cell.getFormatData() != null) {
                    obj = cell.getFormatData();
                }
                if (obj instanceof Image) {
                    Image img = (Image) obj;
                    String path = img.getPath();
                    String imageType = "image/png";
                    if (StringUtils.isNotBlank(path)) {
                        path = path.toLowerCase();
                        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                            imageType = "image/jpeg";
                        } else if (path.endsWith(".gif")) {
                            imageType = "image/gif";
                        }
                    }
                    sb.append("<img src=\"data:" + imageType + ";base64," + img.getBase64Data() + "\"");
                    sb.append(">");
                } else if (obj instanceof ChartData) {
                    ChartData chartData = (ChartData) obj;
                    String canvasId = chartData.getId();
                    int widthPx;
                    if (colSpan > 0) {
                        widthPx = Math.max(1, buildWidthPx(columns, j, cell.getColSpan()) - UnitUtils.pointToPixel(2));
                    } else {
                        widthPx = Math.max(1, UnitUtils.pointToPixel(col.getWidth()) - UnitUtils.pointToPixel(2));
                    }
                    int chartHeightPx;
                    if (engineRowSpan > 1) {
                        chartHeightPx = Math.max(1, buildHeightPx(rows, i, engineRowSpan) - UnitUtils.pointToPixel(2));
                    } else {
                        chartHeightPx = Math.max(1, heightPx - UnitUtils.pointToPixel(2));
                    }
                    sb.append("<div style=\"position: relative;width:" + widthPx + "px;height:" + chartHeightPx + "px\">");
                    sb.append("<div id=\"" + canvasId + "\" style=\"position:relative;width:" + widthPx + "px;height:" + chartHeightPx + "px;overflow:visible\"");
                    String base64Data = chartData.retriveBase64Data();
                    if (StringUtils.isNotBlank(base64Data)) {
                        sb.append(" data-chart-base64=\"data:image/png;base64," + base64Data + "\"");
                    }
                    sb.append("></div>");
                    sb.append("</div>");
                } else {
                    String text = NumberUtils.toPlainString(obj);
                    text = StringEscapeUtils.escapeHtml4(text);
                    text = text.replaceAll("\r\n", "<br>");
                    text = text.replaceAll("\n", "<br>");
                    text = text.replaceAll(" ", "&nbsp;");
                    if (text.equals("")) {
                        text = "&nbsp;";
                    }
                    sb.append(text);
                }
                if (hasLink) {
                    sb.append("</a>");
                }
                sb.append("</td>");
            }
            sb.append("</tr>");
        }
        sb.append("</table>");
        return sb;
    }

    private int buildWidth(List<Column> columns, int colIndex, int colSpan) {
        int width = 0;
        int count = colSpan < 1 ? 1 : colSpan;
        int end = Math.min(colIndex + count, columns.size());
        for (int i = colIndex; i < end; i++) {
            Column col = columns.get(i);
            if (col.isHiddenFormLayout()) {
                continue;
            }
            width += col.getWidth();
        }
        return width;
    }

    /**
     * 按原始跨度累加可见列像素宽
     *
     * @param columns 列列表
     * @param colIndex 起始列下标
     * @param colSpan 引擎列跨度（不足 1 视为 1）
     * @return 像素宽之和
     */
    private int buildWidthPx(List<Column> columns, int colIndex, int colSpan) {
        int width = 0;
        int count = colSpan < 1 ? 1 : colSpan;
        int end = Math.min(colIndex + count, columns.size());
        for (int i = colIndex; i < end; i++) {
            Column col = columns.get(i);
            if (col.isHiddenFormLayout()) {
                continue;
            }
            width += UnitUtils.pointToPixel(col.getWidth());
        }
        return width;
    }

    private int buildHeight(List<Row> rows, int rowIndex, int rowSpan) {
        int height = 0;
        int count = rowSpan < 1 ? 1 : rowSpan;
        int end = Math.min(rowIndex + count, rows.size());
        for (int i = rowIndex; i < end; i++) {
            Row row = rows.get(i);
            if (row.isHiddenFormLayout()) {
                continue;
            }
            height += row.getRealHeight();
        }
        return height;
    }

    /**
     * 按原始跨度累加可见行像素高
     *
     * @param rows 行列表
     * @param rowIndex 起始行下标
     * @param rowSpan 引擎行跨度（不足 1 视为 1）
     * @return 像素高之和
     */
    private int buildHeightPx(List<Row> rows, int rowIndex, int rowSpan) {
        int height = 0;
        int count = rowSpan < 1 ? 1 : rowSpan;
        int end = Math.min(rowIndex + count, rows.size());
        for (int i = rowIndex; i < end; i++) {
            Row row = rows.get(i);
            if (row.isHiddenFormLayout()) {
                continue;
            }
            height += UnitUtils.pointToPixel(row.getRealHeight());
        }
        return height;
    }

    private String buildCustomStyle(Cell cell, List<Column> columns, int colIndex, int heightPx) {
        CellStyle style = cell.getCustomCellStyle();
        CellStyle rowStyle = cell.getRow().getCustomCellStyle();
        CellStyle colStyle = cell.getColumn().getCustomCellStyle();
        StringBuilder sb = new StringBuilder();
        String forecolor = null;
        if (style != null) {
            forecolor = style.getForecolor();
        }
        if (rowStyle != null && rowStyle.getForecolor() != null) {
            forecolor = rowStyle.getForecolor();
        }
        if (colStyle != null && colStyle.getForecolor() != null) {
            forecolor = colStyle.getForecolor();
        }
        if (StringUtils.isNotBlank(forecolor)) {
            sb.append("color:rgb(" + forecolor + ");");
        }
        String bgcolor = null;
        if (style != null) {
            bgcolor = style.getBgcolor();
        }
        if (rowStyle != null && rowStyle.getBgcolor() != null) {
            bgcolor = rowStyle.getBgcolor();
        }
        if (colStyle != null && colStyle.getBgcolor() != null) {
            bgcolor = colStyle.getBgcolor();
        }
        if (StringUtils.isNotBlank(bgcolor)) {
            sb.append("background-color:rgb(" + bgcolor + ");");
        }
        String fontFamily = null;
        if (style != null) {
            fontFamily = style.getFontFamily();
        }
        if (rowStyle != null && rowStyle.getFontFamily() != null) {
            fontFamily = rowStyle.getFontFamily();
        }
        if (colStyle != null && colStyle.getFontFamily() != null) {
            fontFamily = colStyle.getFontFamily();
        }
        if (StringUtils.isNotBlank(fontFamily)) {
            sb.append("font-family:" + fontFamily + ";");
        }
        int fontSize = 0;
        if (style != null) {
            fontSize = style.getFontSize();
        }
        if (rowStyle != null && rowStyle.getFontSize() > 0) {
            fontSize = rowStyle.getFontSize();
        }
        if (colStyle != null && colStyle.getFontSize() > 0) {
            fontSize = colStyle.getFontSize();
        }
        if (fontSize > 0) {
            sb.append("font-size:" + fontSize + "pt;");
        }
        Boolean bold = null;
        if (style != null) {
            bold = style.getBold();
        }
        if (rowStyle != null && rowStyle.getBold() != null) {
            bold = rowStyle.getBold();
        }
        if (colStyle != null && colStyle.getBold() != null) {
            bold = colStyle.getBold();
        }
        if (bold != null) {
            if (bold) {
                sb.append("font-weight:bold;");
            } else {
                sb.append("font-weight:normal;");
            }
        }
        Boolean italic = null;
        if (style != null) {
            italic = style.getItalic();
        }
        if (rowStyle != null && rowStyle.getItalic() != null) {
            italic = rowStyle.getItalic();
        }
        if (colStyle != null && colStyle.getItalic() != null) {
            italic = colStyle.getItalic();
        }
        if (italic != null) {
            if (italic) {
                sb.append("font-style:italic;");
            } else {
                sb.append("font-style:normal;");

            }
        }
        Boolean underline = null;
        if (style != null) {
            underline = style.getUnderline();
        }
        if (rowStyle != null && rowStyle.getUnderline() != null) {
            underline = rowStyle.getUnderline();
        }
        if (colStyle != null && colStyle.getUnderline() != null) {
            underline = colStyle.getUnderline();
        }
        if (underline != null) {
            if (underline) {
                sb.append("text-decoration:underline;");
            } else {
                sb.append("text-decoration:none;");
            }
        }
        Alignment align = null;
        if (style != null) {
            align = style.getAlign();
        }
        if (rowStyle != null && rowStyle.getAlign() != null) {
            align = rowStyle.getAlign();
        }
        if (colStyle != null && colStyle.getAlign() != null) {
            align = colStyle.getAlign();
        }
        if (align != null) {
            sb.append("text-align:" + align.name() + ";");
        }
        Alignment valign = null;
        if (style != null) {
            valign = style.getValign();
        }
        if (rowStyle != null && rowStyle.getValign() != null) {
            valign = rowStyle.getValign();
        }
        if (colStyle != null && colStyle.getValign() != null) {
            valign = colStyle.getValign();
        }
        if (valign != null) {
            sb.append("vertical-align:" + valign.name() + ";");
        }
        Border border = null;
        if (style != null) {
            border = style.getLeftBorder();
        }
        if (border != null) {
            sb.append("border-left:" + border.getStyle().name() + " " + border.getWidth() + "px rgb(" + border.getColor() + ");");
        }
        Border rightBorder = null;
        if (style != null) {
            rightBorder = style.getRightBorder();
        }
        if (rightBorder != null) {
            sb.append("border-right:" + rightBorder.getStyle().name() + " " + rightBorder.getWidth() + "px rgb(" + rightBorder.getColor() + ");");
        }
        Border topBorder = null;
        if (style != null) {
            topBorder = style.getTopBorder();
        }
        if (topBorder != null) {
            sb.append("border-top:" + topBorder.getStyle().name() + " " + topBorder.getWidth() + "px rgb(" + topBorder.getColor() + ");");
        }
        Border bottomBorder = null;
        if (style != null) {
            bottomBorder = style.getBottomBorder();
        }
        if (bottomBorder != null) {
            sb.append("border-bottom:" + bottomBorder.getStyle().name() + " " + bottomBorder.getWidth() + "px rgb(" + bottomBorder.getColor() + ");");
        }
        int engineColSpan = cell.getColSpan();
        int widthPx = (engineColSpan > 1)
                ? buildWidthPx(columns, colIndex, engineColSpan)
                : UnitUtils.pointToPixel(cell.getColumn().getWidth());
        int effectiveFontSize = fontSize;
        if (effectiveFontSize <= 0 && cell.getCellStyle() != null) {
            effectiveFontSize = cell.getCellStyle().getFontSize();
        }
        int lineHeightPx = HtmlRowHeightUtils.lockedTextLineHeightPx(
                heightPx, topBorder, bottomBorder, effectiveFontSize);
        sb.append("box-sizing:border-box;");
        sb.append("padding:0;");
        sb.append("overflow:hidden;");
        sb.append("white-space:nowrap;");
        sb.append("word-break:keep-all;");
        sb.append("line-height:" + lineHeightPx + "px;");
        sb.append("height:" + heightPx + "px;");
        sb.append("max-height:" + heightPx + "px;");
        sb.append("min-height:0;");
        sb.append("width:" + widthPx + "px");
        sb.insert(0, "style=\"");
        sb.append("\"");
        return sb.toString();
    }

    /**
     * 构建 td 的 class（单元格名）
     *
     * @param cell 单元格
     * @return class 属性片段
     */
    private String buildCellClass(Cell cell) {
        return " class='_" + cell.getName() + "' ";
    }

    private int buildTableWidth(List<Column> columns) {
        int width = 0;
        for (Column col : columns) {
            if (col.isHiddenFormLayout()) {
                continue;
            }
            width += col.getWidth();
        }
        return width;
    }

    private int buildTableWidthPx(List<Column> columns) {
        int width = 0;
        for (Column col : columns) {
            if (col.isHiddenFormLayout()) {
                continue;
            }
            width += UnitUtils.pointToPixel(col.getWidth());
        }
        return width;
    }
}
