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
package com.luck.report.core.export.pdf;

import com.luck.report.core.build.paging.Page;
import com.luck.report.core.chart.ChartData;
import com.luck.report.core.definition.Alignment;
import com.luck.report.core.definition.CellStyle;
import com.luck.report.core.definition.Border;
import com.luck.report.core.definition.FloatElement;
import com.luck.report.core.definition.FloatImage;
import com.luck.report.core.definition.FloatText;
import com.luck.report.core.definition.Orientation;
import com.luck.report.core.definition.Paper;
import com.luck.report.core.definition.value.Source;
import com.luck.report.core.exception.ReportComputeException;
import com.luck.report.core.export.FullPageData;
import com.luck.report.core.export.PageBuilder;
import com.luck.report.core.export.Producer;
import com.luck.report.core.export.pdf.font.FontBuilder;
import com.luck.report.core.image.ImageType;
import com.luck.report.core.model.*;
import com.luck.report.core.model.Image;
import com.luck.report.core.utils.ImageUtils;
import com.luck.report.core.utils.UnitUtils;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Jacky.gao
 * @since 2017年3月10日
 */
public class PdfProducer implements Producer {
    @Override
    public void produce(Report report, OutputStream outputStream) {
        Paper paper = report.getPaper();
        int width = paper.getWidth();
        int height = paper.getHeight();
        Rectangle pageSize = new RectangleReadOnly(width, height);
        if (paper.getOrientation().equals(Orientation.landscape)) {
            pageSize = pageSize.rotate();
        }
        int leftMargin = paper.getLeftMargin();
        int rightMargin = paper.getRightMargin();
        int topMargin = paper.getTopMargin();
        int bottomMargin = paper.getBottomMargin();
        Document document = new Document(pageSize, leftMargin, rightMargin, topMargin, bottomMargin);
        try {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);
            PageHeaderFooterEvent headerFooterEvent = new PageHeaderFooterEvent(report);
            writer.setPageEvent(headerFooterEvent);
            document.open();
            List<Column> columns = report.getColumns();
            List<Integer> columnsWidthList = new ArrayList<Integer>();
            int[] intArr = buildColumnSizeAndTotalWidth(columns, columnsWidthList);
            int colSize = intArr[0], totalWidth = intArr[1];
            int[] columnsWidth = new int[columnsWidthList.size()];
            for (int i = 0; i < columnsWidthList.size(); i++) {
                columnsWidth[i] = columnsWidthList.get(i);
            }
            FullPageData pageData = PageBuilder.buildFullPageData(report);
            List<List<Page>> list = pageData.getPageList();
            boolean hasFloat = hasFloatElements(report);
            Map<String, byte[]> imageDataCache = hasFloat ? new HashMap<String, byte[]>() : null;
            if (!list.isEmpty()) {
                int columnCount = paper.getColumnCount();
                int w = columnCount * totalWidth + (columnCount - 1) * paper.getColumnMargin();
                int size = columnCount + (columnCount - 1);
                int[] widths = new int[size];
                for (int i = 0; i < size; i++) {
                    int mode = (i + 1) % 2;
                    if (mode == 0) {
                        widths[i] = paper.getColumnMargin();
                    } else {
                        widths[i] = totalWidth;
                    }
                }
                float tableHeight = pageSize.getHeight() - paper.getTopMargin() - paper.getBottomMargin();
                Map<Row, Map<Column, Cell>> cellMap = report.getRowColCellMap();
                int pageIndex = 0;
                for (List<Page> pages : list) {
                    PdfPTable table = new PdfPTable(size);
                    table.setLockedWidth(true);
                    table.setTotalWidth(w);
                    table.setWidths(widths);
                    table.setHorizontalAlignment(Element.ALIGN_LEFT);
                    int ps = pages.size();
                    for (int i = 0; i < ps; i++) {
                        if (i > 0) {
                            PdfPCell pdfMarginCell = new PdfPCell();
                            pdfMarginCell.setBorder(Rectangle.NO_BORDER);
                            table.addCell(pdfMarginCell);
                        }
                        Page page = pages.get(i);

                        PdfPTable childTable = new PdfPTable(colSize);
                        childTable.setLockedWidth(true);
                        childTable.setTotalWidth(totalWidth);
                        childTable.setWidths(columnsWidth);
                        childTable.setHorizontalAlignment(Element.ALIGN_LEFT);
                        List<Row> rows = page.getRows();
                        for (Row row : rows) {
                            Map<Column, Cell> colMap = cellMap.get(row);
                            if (colMap == null) {
                                continue;
                            }
                            for (Column col : columns) {
                                if (col.getWidth() < 1) {
                                    continue;
                                }
                                Cell cell = colMap.get(col);
                                if (cell == null) {
                                    continue;
                                }
                                int cellHeight = buildCellHeight(cell, rows);
                                PdfPCell pdfcell = buildPdfPCell(cell, cellHeight);
                                childTable.addCell(pdfcell);
                            }
                        }
                        float childTableHeight = childTable.calculateHeights();
                        if (tableHeight > childTableHeight) {
                            for (int j = 0; j < columns.size(); j++) {
                                PdfPCell lastCell = new PdfPCell();
                                lastCell.setBorder(Rectangle.NO_BORDER);
                                childTable.addCell(lastCell);
                            }
                        }
                        PdfPCell pdfContainerCell = new PdfPCell(childTable);
                        pdfContainerCell.setBorder(Rectangle.NO_BORDER);
                        table.addCell(pdfContainerCell);
                    }
                    if (ps < columnCount) {
                        int left = columnCount - ps;
                        for (int i = 0; i < left; i++) {
                            PdfPCell pdfMarginCell = new PdfPCell();
                            pdfMarginCell.setBorder(Rectangle.NO_BORDER);
                            table.addCell(pdfMarginCell);
                            pdfMarginCell = new PdfPCell();
                            pdfMarginCell.setBorder(Rectangle.NO_BORDER);
                            table.addCell(pdfMarginCell);
                        }
                    }
                    document.add(table);
                    if (hasFloat) {
                        renderFloatElementsForPage(writer, report, pageIndex, pageSize, paper, leftMargin, topMargin, imageDataCache);
                    }
                    document.newPage();
                    pageIndex++;
                }

            } else {
                List<Page> pages = report.getPages();
                Map<Row, Map<Column, Cell>> cellMap = report.getRowColCellMap();
                int pageIndex = 0;
                for (Page page : pages) {
                    PdfPTable table = new PdfPTable(colSize);
                    table.setLockedWidth(true);
                    table.setTotalWidth(totalWidth);
                    table.setWidths(columnsWidth);
                    table.setHorizontalAlignment(Element.ALIGN_LEFT);
                    List<Row> rows = page.getRows();
                    for (Row row : rows) {
                        Map<Column, Cell> colMap = cellMap.get(row);
                        if (colMap == null) {
                            continue;
                        }
                        for (Column col : columns) {
                            if (col.getWidth() < 1) {
                                continue;
                            }
                            Cell cell = colMap.get(col);
                            if (cell == null) {
                                continue;
                            }
                            int cellHeight = buildCellHeight(cell, rows);
                            PdfPCell pdfcell = buildPdfPCell(cell, cellHeight);
                            table.addCell(pdfcell);
                        }
                    }
                    document.add(table);
                    if (hasFloat) {
                        renderFloatElementsForPage(writer, report, pageIndex, pageSize, paper, leftMargin, topMargin, imageDataCache);
                    }
                    document.newPage();
                    pageIndex++;
                }
            }
            document.close();
        } catch (Exception ex) {
            throw new ReportComputeException(ex);
        }
    }


    private int buildCellHeight(Cell cell, List<Row> rows) {
        int height = cell.getRow().getRealHeight();
        int rowSpan = cell.getPageRowSpan();
        if (rowSpan > 0) {
            int pos = rows.indexOf(cell.getRow());
            int start = pos + 1, end = start + rowSpan - 1;
            for (int i = start; i < end; i++) {
                height += rows.get(i).getRealHeight();
            }
        }
        // 行高与 HTML（height: Npt）、纸张边距同为 pt，不可再 pixelToPoint
        return height;
    }

    private PdfPCell buildPdfPCell(Cell cellInfo, int cellHeight) throws Exception {
        CellStyle style = cellInfo.getCellStyle();
        CellStyle customStyle = cellInfo.getCustomCellStyle();
        CellStyle rowStyle = cellInfo.getRow().getCustomCellStyle();
        CellStyle colStyle = cellInfo.getColumn().getCustomCellStyle();
        PdfPCell cell = newPdfCell(cellInfo, cellHeight);
        cell.setPadding(0);
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setCellEvent(new CellBorderEvent(style, customStyle));
        int rowSpan = cellInfo.getPageRowSpan();
        if (rowSpan > 0) {
            cell.setRowspan(rowSpan);
        }
        int colSpan = cellInfo.getColSpan();
        if (colSpan > 0) {
            cell.setColspan(colSpan);
        }
        Alignment align = style.getAlign();
        if (customStyle != null && customStyle.getAlign() != null) {
            align = customStyle.getAlign();
        }
        if (rowStyle != null && rowStyle.getAlign() != null) {
            align = rowStyle.getAlign();
        }
        if (colStyle != null && colStyle.getAlign() != null) {
            align = colStyle.getAlign();
        }
        if (align != null) {
            if (align.equals(Alignment.left)) {
                cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            } else if (align.equals(Alignment.center)) {
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            } else if (align.equals(Alignment.right)) {
                cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            }
        }
        Alignment valign = style.getValign();
        if (customStyle != null && customStyle.getValign() != null) {
            valign = customStyle.getValign();
        }
        if (rowStyle != null && rowStyle.getValign() != null) {
            valign = rowStyle.getValign();
        }
        if (colStyle != null && colStyle.getValign() != null) {
            valign = colStyle.getValign();
        }
        if (valign != null) {
            if (valign.equals(Alignment.top)) {
                cell.setVerticalAlignment(Element.ALIGN_TOP);
            } else if (valign.equals(Alignment.middle)) {
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            } else if (valign.equals(Alignment.bottom)) {
                cell.setVerticalAlignment(Element.ALIGN_BOTTOM);
            }
        }
        String bgcolor = style.getBgcolor();
        if (customStyle != null && StringUtils.isNotBlank(customStyle.getBgcolor())) {
            bgcolor = customStyle.getBgcolor();
        }
        if (rowStyle != null && StringUtils.isNotBlank(rowStyle.getBgcolor())) {
            bgcolor = rowStyle.getBgcolor();
        }
        if (colStyle != null && StringUtils.isNotBlank(colStyle.getBgcolor())) {
            bgcolor = colStyle.getBgcolor();
        }
        if (StringUtils.isNotEmpty(bgcolor)) {
            String[] colors = bgcolor.split(",");
            cell.setBackgroundColor(new BaseColor(Integer.valueOf(colors[0]), Integer.valueOf(colors[1]), Integer.valueOf(colors[2])));
        }
        return cell;
    }

    private int[] buildColumnSizeAndTotalWidth(List<Column> columns, List<Integer> list) {
        int count = 0, totalWidth = 0;
        for (int i = 0; i < columns.size(); i++) {
            Column col = columns.get(i);
            int width = col.getWidth();
            if (width < 1) {
                continue;
            }
            count++;
            list.add(width);
            totalWidth += width;
        }
        return new int[]{count, totalWidth};
    }

    private PdfPCell newPdfCell(Cell cellInfo, int cellHeight) throws Exception {
        PdfPCell cell = null;
        Object cellData = cellInfo.isRenderFlag() ? cellInfo.getFormatData() : "";
        if (cellData instanceof Image) {
            Image img = (Image) cellData;
            cell = new PdfPCell(buildPdfImage(img.getBase64Data(), 0, 0));
        } else if (cellData instanceof ChartData) {
            ChartData chartData = (ChartData) cellData;
            String base64Data = chartData.retriveBase64Data();
            if (base64Data != null) {
                int widthPx = UnitUtils.pointToPixel(cellInfo.getColumn().getWidth());
                int heightPx = UnitUtils.pointToPixel(cellHeight);
                cell = new PdfPCell(buildPdfImage(base64Data, widthPx, heightPx));
            } else {
                cell = new PdfPCell();
                CellPhrase pargraph = new CellPhrase(cellInfo, "");
                cell.setPhrase(pargraph);
                cell.setFixedHeight(cellHeight);
            }
        } else {
            cell = new PdfPCell();
            CellPhrase pargraph = new CellPhrase(cellInfo, cellData);
            cell.setPhrase(pargraph);
            cell.setFixedHeight(cellHeight);
        }
        CellStyle style = cellInfo.getCellStyle();
        if (style != null && style.getLineHeight() > 0) {
            cell.setLeading(style.getLineHeight(), style.getLineHeight());
        }
        return cell;
    }

    private com.itextpdf.text.Image buildPdfImage(String base64Data, int width, int height) throws Exception {
        com.itextpdf.text.Image pdfImg = null;
        InputStream input = ImageUtils.base64DataToInputStream(base64Data);
        try {
            byte[] bytes = IOUtils.toByteArray(input);
            pdfImg = com.itextpdf.text.Image.getInstance(bytes);
            float imgWidth = pdfImg.getWidth();
            float imgHeight = pdfImg.getHeight();
            if (width == 0) {
                width = Float.valueOf(imgWidth).intValue();
            }
            if (height == 0) {
                height = Float.valueOf(imgHeight).intValue();
            }
            width = UnitUtils.pixelToPoint(width - 2);
            height = UnitUtils.pixelToPoint(height - 2);
            pdfImg.scaleToFit(width, height);
        } finally {
            IOUtils.closeQuietly(input);
        }
        return pdfImg;
    }

    /**
     * 判断当前报表是否包含悬浮元素（图片或文本）。
     * 仅当存在非空列表时返回 true，避免对旧报表产生任何影响。
     */
    private boolean hasFloatElements(Report report) {
        List<FloatImage> floatImages = report.getFloatImages();
        List<FloatText> floatTexts = report.getFloatTexts();
        boolean hasImages = floatImages != null && !floatImages.isEmpty();
        boolean hasTexts = floatTexts != null && !floatTexts.isEmpty();
        return hasImages || hasTexts;
    }

    /**
     * 将悬浮图片解析为输入流，支持 base64 与 URL（text）两种来源。
     * 对 URL 图片使用 {@link ImageUtils#getImageBase64Data}，有 image-not-exist 兜底机制。
     */
    private InputStream buildFloatImageInputStream(FloatImage fi) {
        Source source = fi.getSource();
        String value = fi.buildValue();
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (source != null && source.equals(Source.base64)) {
            try {
                return ImageUtils.base64DataToInputStream(value);
            } catch (Exception e) {
                return null;
            }
        }
        // source=text（URL）：通过 ImageProvider SPI 加载，有 image-not-exist 兜底
        try {
            String base64Data = ImageUtils.getImageBase64Data(ImageType.image, value, 0, 0);
            if (StringUtils.isNotBlank(base64Data)) {
                return ImageUtils.base64DataToInputStream(base64Data);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    private static final Logger log = Logger.getLogger(PdfProducer.class.getName());

    /**
     * 在当前 PDF 页面上绘制悬浮元素。
     * top/left 为页内相对坐标（相对每页内容区左上角），repeatPrint=true 每页渲染，repeatPrint=false 仅第1页。
     *
     * @param writer            PdfWriter
     * @param report            报表对象
     * @param pageIndex         当前 PDF 页码（从 0 开始）
     * @param pageSize          页面尺寸
     * @param paper             页面设置（用于计算内容区高度）
     * @param leftMargin        左边距
     * @param topMargin         上边距
     */
    private void renderFloatElementsForPage(PdfWriter writer, Report report, int pageIndex,
                                            Rectangle pageSize, Paper paper,
                                            int leftMargin, int topMargin,
                                            Map<String, byte[]> imageDataCache) throws Exception {
        PdfContentByte cb = writer.getDirectContent();
        List<FloatElement> elements = new ArrayList<FloatElement>();
        List<FloatImage> floatImages = report.getFloatImages();
        if (floatImages != null) {
            for (FloatImage fi : floatImages) {
                if (fi != null) {
                    elements.add(fi);
                }
            }
        }
        List<FloatText> floatTexts = report.getFloatTexts();
        if (floatTexts != null) {
            for (FloatText ft : floatTexts) {
                if (ft != null) {
                    elements.add(ft);
                }
            }
        }
        // 按 layer 升序绘制（值越大越靠上，后绘制覆盖先绘制）
        elements.sort((a, b) -> {
            int za = a.getLayer() == null ? 0 : a.getLayer();
            int zb = b.getLayer() == null ? 0 : b.getLayer();
            return Integer.compare(za, zb);
        });
        float pageContentHeight = pageSize.getHeight() - topMargin - paper.getBottomMargin();
        for (FloatElement el : elements) {
            Integer top = el.getTop();
            if (top == null) {
                continue;
            }
            float elementTopPt = UnitUtils.pixelToPoint(top.intValue());
            String elName = el.getName() != null ? el.getName() : (el instanceof FloatImage ? "image" : "text");
            if (el.isRepeatPrint()) {
                if (elementTopPt < 0 || elementTopPt >= pageContentHeight) {
                    continue;
                }
            } else {
                int targetPage = (int) Math.floor(elementTopPt / pageContentHeight);
                if (pageIndex != targetPage) {
                    continue;
                }
                elementTopPt = elementTopPt - targetPage * pageContentHeight;
            }
            Integer left = el.getLeft();
            float x = leftMargin + (left != null ? UnitUtils.pixelToPoint(left.intValue()) : 0);
            Integer elWidth = el.getWidth();
            Integer elHeight = el.getHeight();
            try {
                if (el instanceof FloatImage) {
                    FloatImage fi = (FloatImage) el;
                    float widthPt = elWidth != null ? UnitUtils.pixelToPoint(elWidth.intValue()) : 0;
                    float heightPt = elHeight != null ? UnitUtils.pixelToPoint(elHeight.intValue()) : 0;
                    // 使用缓存避免重复下载/解码同一图片
                    String cacheKey = fi.getName() != null ? fi.getName() : String.valueOf(System.identityHashCode(fi));
                    byte[] bytes = imageDataCache != null ? imageDataCache.get(cacheKey) : null;
                    if (bytes == null) {
                        InputStream input = buildFloatImageInputStream(fi);
                        if (input == null) {
                            continue;
                        }
                        try {
                            bytes = IOUtils.toByteArray(input);
                        } finally {
                            IOUtils.closeQuietly(input);
                        }
                        if (imageDataCache != null) {
                            imageDataCache.put(cacheKey, bytes);
                        }
                    }
                    try {
                        com.itextpdf.text.Image pdfImg = com.itextpdf.text.Image.getInstance(bytes);
                        if (widthPt <= 0) {
                            widthPt = pdfImg.getWidth();
                        }
                        if (heightPt <= 0) {
                            heightPt = pdfImg.getHeight();
                        }
                        pdfImg.scaleToFit(widthPt, heightPt);
                        float y = pageSize.getHeight() - topMargin - elementTopPt - pdfImg.getScaledHeight();
                        pdfImg.setAbsolutePosition(x, y);
                        cb.addImage(pdfImg);
                    } catch (Exception e) {
                        // 诊断：输出 value 开头与解码后字节魔数，定位是 Data URI 前缀未剥还是格式不支持
                        String valPreview = fi.buildValue();
                        String valHead = valPreview != null && valPreview.length() > 60
                                ? valPreview.substring(0, 60) : valPreview;
                        StringBuilder hex = new StringBuilder();
                        for (int i = 0; i < Math.min(bytes.length, 8); i++) {
                            hex.append(String.format("%02x", bytes[i] & 0xff)).append(" ");
                        }
                    }
                } else if (el instanceof FloatText) {
                    FloatText ft = (FloatText) el;
                    Integer fontSizePx = ft.getFontSize();
                    int fontSizePt = fontSizePx != null ? UnitUtils.pixelToPoint(fontSizePx.intValue()) : 12;
                    float widthPt = elWidth != null ? UnitUtils.pixelToPoint(elWidth.intValue()) : 0;
                    float heightPt = elHeight != null ? UnitUtils.pixelToPoint(elHeight.intValue()) : fontSizePt;
                    String fontName = ft.getFontFamily();
                    if (StringUtils.isBlank(fontName)) {
                        fontName = "宋体";
                    }
                    boolean bold = ft.getBold() != null && ft.getBold();
                    boolean italic = ft.getItalic() != null && ft.getItalic();
                    boolean underline = ft.getUnderline() != null && ft.getUnderline();
                    Font font = FontBuilder.getFont(fontName, fontSizePt, bold, italic, underline);
                    String forecolor = ft.getForecolor();
                    if (StringUtils.isNotEmpty(forecolor)) {
                        String[] colors = forecolor.split(",");
                        if (colors.length >= 3) {
                            font.setColor(Integer.valueOf(colors[0].trim()), Integer.valueOf(colors[1].trim()), Integer.valueOf(colors[2].trim()));
                        }
                    }
                    float y = pageSize.getHeight() - topMargin - elementTopPt - heightPt;
                    float urx = x + (widthPt > 0 ? widthPt : 200);
                    float ury = y + heightPt;

                    // 背景颜色：在文本区域绘制填充矩形
                    String bgcolor = ft.getBgcolor();
                    if (StringUtils.isNotEmpty(bgcolor)) {
                        String[] bgColors = bgcolor.split(",");
                        if (bgColors.length >= 3) {
                            cb.saveState();
                            BaseColor bgBaseColor = new BaseColor(
                                    Integer.valueOf(bgColors[0].trim()),
                                    Integer.valueOf(bgColors[1].trim()),
                                    Integer.valueOf(bgColors[2].trim()));
                            cb.setColorFill(bgBaseColor);
                            cb.rectangle(x, y, urx - x, ury - y);
                            cb.fill();
                            cb.restoreState();
                        }
                    }

                    // 边框：四边独立绘制
                    drawPdfBorder(cb, ft.getTopBorder(), x, ury, urx, ury);
                    drawPdfBorder(cb, ft.getBottomBorder(), x, y, urx, y);
                    drawPdfBorder(cb, ft.getLeftBorder(), x, y, x, ury);
                    drawPdfBorder(cb, ft.getRightBorder(), urx, y, urx, ury);

                    // 水平对齐
                    int align = Element.ALIGN_LEFT;
                    if (ft.getAlign() != null) {
                        String a = ft.getAlign();
                        if ("center".equals(a)) {
                            align = Element.ALIGN_CENTER;
                        } else if ("right".equals(a)) {
                            align = Element.ALIGN_RIGHT;
                        }
                    }

                    // 垂直对齐：通过调整文本区域顶部 y 坐标实现
                    // ColumnText 从 top 往下绘制文本，调整 top 即可控制垂直位置
                    String value = ft.getValue();
                    Phrase phrase = new Phrase(value != null ? value : "", font);
                    float availableHeight = ury - y;
                    float columnTop = ury; // 默认 top 对齐：文本从顶部开始
                    if (ft.getValign() != null && !"top".equals(ft.getValign())) {
                        // 测量文本高度
                        float textHeight = fontSizePt;
                        ColumnText simCt = new ColumnText(cb);
                        simCt.setSimpleColumn(phrase, x, 0, urx, 10000f, fontSizePt, align);
                        try {
                            simCt.go(true); // simulate
                            float yLine = simCt.getYLine();
                            float measured = 10000f - yLine;
                            if (measured > 0) {
                                textHeight = measured;
                            }
                        } catch (Exception ignored) {}
                        if ("middle".equals(ft.getValign())) {
                            columnTop = y + (availableHeight + textHeight) / 2;
                        } else if ("bottom".equals(ft.getValign())) {
                            columnTop = y + textHeight;
                        }
                    }
                    ColumnText ct = new ColumnText(cb);
                    ct.setSimpleColumn(phrase, x, y, urx, columnTop, fontSizePt, align);
                    ct.go();
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "导出 PDF 悬浮元素 [" + elName + "] 失败，已跳过", e);
            }
        }
    }

    /**
     * 绘制单边边框线。
     */
    private void drawPdfBorder(PdfContentByte cb, Border border, float x1, float y1, float x2, float y2) {
        if (border == null || border.getStyle() == null) return;
        int borderWidthPx = border.getWidth();
        float borderWidthPt = UnitUtils.pixelToPoint(borderWidthPx > 0 ? borderWidthPx : 1);
        BaseColor borderBaseColor = BaseColor.BLACK;
        String borderColorStr = border.getColor();
        if (StringUtils.isNotEmpty(borderColorStr)) {
            String[] bcArr = borderColorStr.split(",");
            if (bcArr.length >= 3) {
                borderBaseColor = new BaseColor(
                        Integer.valueOf(bcArr[0].trim()),
                        Integer.valueOf(bcArr[1].trim()),
                        Integer.valueOf(bcArr[2].trim()));
            }
        }
        cb.saveState();
        cb.setColorStroke(borderBaseColor);
        cb.setLineWidth(borderWidthPt);
        String style = border.getStyle().toString();
        if ("dashed".equals(style)) {
            cb.setLineDash(3 * borderWidthPt, 2 * borderWidthPt, 0);
        } else if ("doublesolid".equals(style)) {
            cb.setLineDash(0, 0, 0);
        } else {
            cb.setLineDash(0, 0, 0);
        }
        cb.moveTo(x1, y1);
        cb.lineTo(x2, y2);
        cb.stroke();
        cb.restoreState();
    }
}
