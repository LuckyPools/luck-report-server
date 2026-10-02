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
package com.luck.report.core.export.word.high;

import com.luck.report.core.build.paging.Page;
import com.luck.report.core.chart.ChartData;
import com.luck.report.core.definition.*;
import com.luck.report.core.definition.value.Source;
import com.luck.report.core.exception.ReportComputeException;
import com.luck.report.core.export.Producer;
import com.luck.report.core.image.ImageType;
import com.luck.report.core.export.word.DxaUtils;
import com.luck.report.core.model.*;
import com.luck.report.core.model.*;
import com.luck.report.core.utils.ImageUtils;
import com.luck.report.core.utils.NumberUtils;
import com.luck.report.core.utils.UnitUtils;
import com.luck.report.core.definition.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.*;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.apache.xmlbeans.XmlCursor;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Word报表导出生产器
 * @author Jacky.gao
 * @since 2015年5月20日
 */
public class WordProducer implements Producer {
    private HeaderFooterBuilder headerFooterBuilder = new HeaderFooterBuilder();

    /**
     * 生成Word报表
     * @param report 报表对象
     * @param outputStream 输出流
     */
    @Override
    public void produce(Report report, OutputStream outputStream) {
        XWPFDocument document = new XWPFDocument();
        try {
            CTSectPr sectpr = document.getDocument().getBody().addNewSectPr();
            if (!sectpr.isSetPgSz()) {
                sectpr.addNewPgSz();
            }
            CTPageSz pageSize = sectpr.getPgSz();
            Paper paper = report.getPaper();
            Orientation orientation = paper.getOrientation();
            if (orientation.equals(Orientation.landscape)) {
                pageSize.setOrient(STPageOrientation.LANDSCAPE);
                pageSize.setH(BigInteger.valueOf(DxaUtils.points2dxa(paper.getWidth())));
                pageSize.setW(BigInteger.valueOf(DxaUtils.points2dxa(paper.getHeight())));
            } else {
                pageSize.setOrient(STPageOrientation.PORTRAIT);
                pageSize.setW(BigInteger.valueOf(DxaUtils.points2dxa(paper.getWidth())));
                pageSize.setH(BigInteger.valueOf(DxaUtils.points2dxa(paper.getHeight())));
            }
            int columnCount = paper.getColumnCount();
            if (paper.isColumnEnabled() && columnCount > 0) {
                CTColumns cols = CTColumns.Factory.newInstance();
                cols.setNum(new BigInteger(String.valueOf(columnCount)));
                int columnMargin = paper.getColumnMargin();
                cols.setSpace(new BigInteger(String.valueOf(DxaUtils.points2dxa(columnMargin))));
                sectpr.setCols(cols);
            }
            CTPageMar pageMar = sectpr.addNewPgMar();
            pageMar.setLeft(BigInteger.valueOf(DxaUtils.points2dxa(paper.getLeftMargin())));
            pageMar.setRight(BigInteger.valueOf(DxaUtils.points2dxa(paper.getRightMargin())));
            pageMar.setTop(BigInteger.valueOf(DxaUtils.points2dxa(paper.getTopMargin())));
            pageMar.setBottom(BigInteger.valueOf(DxaUtils.points2dxa(paper.getBottomMargin())));
            List<Column> columns = report.getColumns();
            int intArr[] = buildColumnSizeAndTotalWidth(columns);
            int totalColumn = intArr[0], tableWidth = intArr[1];
            List<Page> pages = report.getPages();
            Map<Row, Map<Column, Cell>> cellMap = report.getRowColCellMap();
            int totalPages = pages.size();
            boolean hasFloat = hasFloatElements(report);
            Map<String, byte[]> imageDataCache = hasFloat ? new HashMap<String, byte[]>() : null;
            int pageIndex = 1;
            for (Page page : pages) {
                List<Row> rows = page.getRows();
                if (hasFloat) {
                    renderFloatElementsForPage(document, report, pageIndex - 1, paper, imageDataCache);
                }
                XWPFTable table = document.createTable(rows.size(), totalColumn);
                table.getCTTbl().getTblPr().unsetTblBorders();
                table.getCTTbl().addNewTblPr().addNewTblW().setW(BigInteger.valueOf(DxaUtils.points2dxa(tableWidth)));
                for (int rowNumber = 0; rowNumber < rows.size(); rowNumber++) {
                    Row row = rows.get(rowNumber);
                    int height = row.getRealHeight();
                    XWPFTableRow tableRow = table.getRow(rowNumber);
                    tableRow.setHeight(DxaUtils.points2dxa(height));
                    Map<Column, Cell> colCell = cellMap.get(row);
                    if (colCell == null) continue;
                    int skipCol = 0;
                    for (Column col : columns) {
                        int width = col.getWidth();
                        if (width < 1) {
                            skipCol++;
                            continue;
                        }
                        int colNumber = col.getColumnNumber() - 1 - skipCol;
                        Cell cell = colCell.get(col);
                        if (cell == null) {
                            continue;
                        }
                        XWPFTableCell tableCell = tableRow.getCell(colNumber);
                        if (tableCell == null) {
                            continue;
                        }
                        tableCell.getCTTc().addNewTcPr().addNewTcW().setW(BigInteger.valueOf(DxaUtils.points2dxa(width)));
                        buildTableCellStyle(table, tableCell, cell, rowNumber, colNumber);
                    }
                }
                if (pageIndex < totalPages) {
                    XWPFParagraph paragraph = document.createParagraph();
                    XWPFRun run = paragraph.createRun();
                    run.setFontSize(0);
                    run.addBreak(BreakType.PAGE);
                }
                pageIndex++;
            }
            headerFooterBuilder.build(document, sectpr, report);
            document.write(outputStream);
        } catch (Exception ex) {
            throw new ReportComputeException(ex);
        } finally {
            try {
                document.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private int[] buildColumnSizeAndTotalWidth(List<Column> columns) {
        int count = 0, totalWidth = 0;
        for (int i = 0; i < columns.size(); i++) {
            Column col = columns.get(i);
            int width = col.getWidth();
            if (width < 1) {
                continue;
            }
            count++;
            totalWidth += width;
        }
        return new int[]{count, totalWidth};
    }

    /**
     * 合并单元格总宽度（pt）
     */
    private int buildWholeWidthPt(Cell cell) {
        int width = cell.getColumn().getWidth();
        int colSpan = cell.getColSpan();
        if (colSpan > 1) {
            Column col = cell.getColumn().getNext();
            for (int i = 1; i < colSpan && col != null; i++) {
                width += Math.max(0, col.getWidth());
                col = col.getNext();
            }
        }
        return width;
    }

    /**
     * 合并单元格总高度（pt）
     */
    private int buildWholeHeightPt(Cell cell) {
        int height = cell.getRow().getRealHeight();
        int rowSpan = cell.getPageRowSpan();
        if (rowSpan > 1) {
            Row row = cell.getRow().getNext();
            for (int i = 1; i < rowSpan && row != null; i++) {
                height += row.getRealHeight();
                row = row.getNext();
            }
        }
        return height;
    }

    private void buildTableCellStyle(XWPFTable table, XWPFTableCell tableCell, Cell cell, int rowNumber, int columnNumber) {
        CellStyle style = cell.getCellStyle();
        CellStyle customStyle = cell.getCustomCellStyle();
        CellStyle rowStyle = cell.getRow().getCustomCellStyle();
        CellStyle colStyle = cell.getColumn().getCustomCellStyle();
        CTTcPr cellProperties = tableCell.getCTTc().addNewTcPr();
        Border leftBorder = style.getLeftBorder();
        Border rightBorder = style.getRightBorder();
        Border topBorder = style.getTopBorder();
        Border bottomBorder = style.getBottomBorder();
        if (customStyle != null) {
            if (customStyle.getLeftBorder() != null) {
                leftBorder = customStyle.getLeftBorder();
            }
            if (customStyle.getRightBorder() != null) {
                rightBorder = customStyle.getRightBorder();
            }
            if (customStyle.getTopBorder() != null) {
                topBorder = customStyle.getTopBorder();
            }
            if (customStyle.getBottomBorder() != null) {
                bottomBorder = customStyle.getBottomBorder();
            }
        }
        int rowSpan = cell.getPageRowSpan();
        int colSpan = cell.getColSpan();
        if (style.getLeftBorder() != null) {
            if (rowSpan > 0) {
                int start = rowNumber;
                int end = start + rowSpan;
                for (int i = start; i < end; i++) {
                    XWPFTableCell c = table.getRow(i).getCell(columnNumber);
                    buildCellBorder(leftBorder, c, 1);
                }
            } else {
                buildCellBorder(leftBorder, tableCell, 1);
            }
        }
        if (rightBorder != null) {
            int lastCol = columnNumber;
            if (colSpan > 0) {
                lastCol += colSpan - 1;
            }
            if (rowSpan > 0) {
                int start = rowNumber;
                int end = start + rowSpan;
                for (int i = start; i < end; i++) {
                    XWPFTableCell c = table.getRow(i).getCell(lastCol);
                    buildCellBorder(style.getRightBorder(), c, 2);
                }
            } else {
                XWPFTableCell c = table.getRow(rowNumber).getCell(lastCol);
                buildCellBorder(rightBorder, c, 2);
            }
        }
        if (topBorder != null) {
            if (colSpan > 0) {
                int start = columnNumber;
                int end = start + colSpan;
                for (int i = start; i < end; i++) {
                    XWPFTableCell c = table.getRow(rowNumber).getCell(i);
                    buildCellBorder(topBorder, c, 3);
                }
            } else {
                buildCellBorder(topBorder, tableCell, 3);
            }
        }
        if (bottomBorder != null) {
            int lastRow = rowNumber;
            if (rowSpan > 0) {
                lastRow += rowSpan - 1;
            }
            if (colSpan > 0) {
                int start = columnNumber;
                int end = start + colSpan;
                for (int i = start; i < end; i++) {
                    XWPFTableCell c = table.getRow(lastRow).getCell(i);
                    buildCellBorder(bottomBorder, c, 4);
                }
            } else {
                XWPFTableCell c = table.getRow(lastRow).getCell(columnNumber);
                buildCellBorder(bottomBorder, c, 4);
            }
        }
        List<XWPFParagraph> paras = tableCell.getParagraphs();
        XWPFParagraph para = null;
        if (paras != null && paras.size() > 0) {
            para = paras.get(0);
        } else {
            para = tableCell.addParagraph();
        }
        List<XWPFRun> runs = para.getRuns();
        XWPFRun run = null;
        if (runs != null && runs.size() > 0) {
            run = runs.get(0);
        } else {
            run = para.createRun();
        }
        Object value = cell.isRenderFlag() ? cell.getFormatData() : null;
        if (value instanceof String) {
            String text = value.toString();
            if (text.contains("\n")) {
                String[] line = text.split("\n");
                run.setText(line[0], 0);
                for (int i = 1; i < line.length; i++) {
                    run.addBreak();
                    run.setText(line[i], i);
                }
            } else {
                run.setText(text);
            }
        } else if (value instanceof Number) {
            run.setText(NumberUtils.toPlainString(value));
        } else if (value instanceof Boolean) {
            run.setText(value.toString());
        } else if ((value instanceof Image) || (value instanceof ChartData)) {
            Image img = null;
            if (value instanceof Image) {
                img = (Image) value;
            } else {
                ChartData chartData = (ChartData) value;
                String base64Data = chartData.retriveBase64Data();
                if (base64Data != null) {
                    img = new Image(base64Data, chartData.getWidth(), chartData.getHeight());
                } else {
                    img = new Image("", chartData.getWidth(), chartData.getHeight());
                }
            }
            String path = img.getPath();
            String imageType = "png";
            if (StringUtils.isNotBlank(path)) {
                path = path.toLowerCase();
                if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
                    imageType = "jpeg";
                } else if (path.endsWith(".gif")) {
                    imageType = "gif";
                }
            }
            String base64Data = img.getBase64Data();
            if (StringUtils.isNotBlank(base64Data)) {
                InputStream inputStream = null;
                try {
                    inputStream = ImageUtils.base64DataToInputStream(base64Data);
                    BufferedImage bufferedImage = ImageIO.read(inputStream);
                    int width = bufferedImage.getWidth();
                    int height = bufferedImage.getHeight();
                    boolean isChart = value instanceof ChartData;
                    if (isChart) {
                        ChartData chartData = (ChartData) value;
                        width = chartData.getWidth() > 0 ? chartData.getWidth() : buildWholeWidthPt(cell);
                        height = chartData.getHeight() > 0 ? chartData.getHeight() : buildWholeHeightPt(cell);
                    } else {
                        width = UnitUtils.pixelToPoint(width);
                        height = UnitUtils.pixelToPoint(height);
                    }
                    IOUtils.closeQuietly(inputStream);
                    inputStream = ImageUtils.base64DataToInputStream(base64Data);
                    if (imageType.equals("jpeg")) {
                        run.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_JPEG, "ureport-" + rowNumber + "-" + columnNumber + ".jpg", Units.toEMU(width), Units.toEMU(height));
                    } else if (imageType.equals("png")) {
                        run.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_PNG, "ureport-" + rowNumber + "-" + columnNumber + ".png", Units.toEMU(width), Units.toEMU(height));
                    } else if (imageType.equals("gif")) {
                        run.addPicture(inputStream, XWPFDocument.PICTURE_TYPE_GIF, "ureport-" + rowNumber + "-" + columnNumber + ".gif", Units.toEMU(width), Units.toEMU(height));
                    }
                } catch (Exception ex) {
                    throw new ReportComputeException(ex);
                } finally {
                    IOUtils.closeQuietly(inputStream);
                }
            }
        } else if (value instanceof Date) {
            Date date = (Date) value;
            SimpleDateFormat sd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            run.setText(sd.format(date));
        }
        String fontFamily = style.getFontFamily();
        if (customStyle != null && StringUtils.isNotBlank(customStyle.getFontFamily())) {
            fontFamily = customStyle.getFontFamily();
        }
        if (rowStyle != null && StringUtils.isNotBlank(rowStyle.getFontFamily())) {
            fontFamily = rowStyle.getFontFamily();
        }
        if (colStyle != null && StringUtils.isNotBlank(colStyle.getFontFamily())) {
            fontFamily = colStyle.getFontFamily();
        }
        if (StringUtils.isNotBlank(fontFamily)) {
            run.setFontFamily(fontFamily);
        }
        int fontSize = style.getFontSize();
        if (customStyle != null && customStyle.getFontSize() > 0) {
            fontSize = customStyle.getFontSize();
        }
        if (rowStyle != null && rowStyle.getFontSize() > 0) {
            fontSize = rowStyle.getFontSize();
        }
        if (colStyle != null && colStyle.getFontSize() > 0) {
            fontSize = colStyle.getFontSize();
        }
        if (fontSize > 0) {
            run.setFontSize(fontSize);
        }
        boolean bold = style.getBold() == null ? false : style.getBold();
        if (customStyle != null && customStyle.getBold() != null) {
            bold = customStyle.getBold();
        }
        if (rowStyle != null && rowStyle.getBold() != null) {
            bold = rowStyle.getBold();
        }
        if (colStyle != null && colStyle.getBold() != null) {
            bold = colStyle.getBold();
        }
        if (bold) {
            run.setBold(true);
        }
        boolean italic = style.getItalic() == null ? false : style.getItalic();
        if (customStyle != null && customStyle.getItalic() != null) {
            italic = customStyle.getItalic();
        }
        if (rowStyle != null && rowStyle.getItalic() != null) {
            italic = rowStyle.getItalic();
        }
        if (colStyle != null && colStyle.getItalic() != null) {
            italic = colStyle.getItalic();
        }
        if (italic) {
            run.setItalic(true);
        }
        boolean underline = style.getUnderline() == null ? false : style.getUnderline();
        if (customStyle != null && customStyle.getUnderline() != null) {
            underline = customStyle.getUnderline();
        }
        if (rowStyle != null && rowStyle.getUnderline() != null) {
            underline = rowStyle.getUnderline();
        }
        if (colStyle != null && colStyle.getUnderline() != null) {
            underline = colStyle.getUnderline();
        }
        if (underline) {
            run.setUnderline(UnderlinePatterns.SINGLE);
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
        if (bgcolor != null) {
            CTShd ctshd = cellProperties.addNewShd();
            ctshd.setFill(toHex(bgcolor.split(",")));
        }
        String forecolor = style.getForecolor();
        if (customStyle != null && StringUtils.isNotBlank(customStyle.getForecolor())) {
            forecolor = customStyle.getForecolor();
        }
        if (rowStyle != null && StringUtils.isNotBlank(rowStyle.getForecolor())) {
            forecolor = rowStyle.getForecolor();
        }
        if (colStyle != null && StringUtils.isNotBlank(colStyle.getForecolor())) {
            forecolor = colStyle.getForecolor();
        }
        if (forecolor != null) {
            run.setColor(toHex(forecolor.split(",")));
        }
        Alignment align = style.getAlign();
        if (customStyle != null && customStyle.getAlign() != null) {
            align = customStyle.getAlign();
        }
        if (rowStyle != null && rowStyle.getAlign() != null) {
            align = rowStyle.getAlign();
        }
        if (align != null) {
            if (align.equals(Alignment.left)) {
                para.setAlignment(ParagraphAlignment.LEFT);
            } else if (align.equals(Alignment.right)) {
                para.setAlignment(ParagraphAlignment.RIGHT);
            } else if (align.equals(Alignment.center)) {
                para.setAlignment(ParagraphAlignment.CENTER);
            }
        }
        if (style.getLineHeight() > 0) {
            para.setSpacingBetween(style.getLineHeight());
        }
        align = style.getValign();
        if (customStyle != null && customStyle.getValign() != null) {
            align = customStyle.getValign();
        }
        if (rowStyle != null && rowStyle.getValign() != null) {
            align = rowStyle.getValign();
        }
        if (colStyle != null && colStyle.getValign() != null) {
            align = colStyle.getValign();
        }
        if (align != null) {
            CTVerticalJc verticalAlign = cellProperties.addNewVAlign();
            if (align.equals(Alignment.top)) {
                verticalAlign.setVal(STVerticalJc.TOP);
            } else if (align.equals(Alignment.middle)) {
                verticalAlign.setVal(STVerticalJc.CENTER);
            } else if (align.equals(Alignment.bottom)) {
                verticalAlign.setVal(STVerticalJc.BOTTOM);
            }
        }
        int startCol = columnNumber;
        int startRow = rowNumber;
        int endRow = startRow, endCol = startCol;
        if (colSpan > 0) {
            endCol = startCol + colSpan - 1;
        }
        if (rowSpan > 0) {
            endRow = startRow + rowSpan - 1;
        }
        if (startCol != endCol) {
            if (rowSpan > 0) {
                for (int i = startRow; i <= endRow; i++) {
                    mergeCellsHorizontal(table, i, startCol, endCol);
                }
            } else {
                mergeCellsHorizontal(table, startRow, startCol, endCol);
            }
        }
        if (startRow != endRow) {
            if (colSpan > 0) {
                for (int i = startCol; i <= endCol; i++) {
                    mergeCellsVertically(table, i, startRow, endRow);
                }
            } else {
                mergeCellsVertically(table, startCol, startRow, endRow);
            }
        }
    }

    private void mergeCellsHorizontal(XWPFTable table, int row, int startCol, int endCol) {
        for (int cellIndex = startCol; cellIndex <= endCol; cellIndex++) {
            XWPFTableCell cell = table.getRow(row).getCell(cellIndex);
            if (cellIndex == startCol) {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.RESTART);
            } else {
                cell.getCTTc().addNewTcPr().addNewHMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    private void mergeCellsVertically(XWPFTable table, int col, int fromRow, int toRow) {
        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            XWPFTableCell cell = table.getRow(rowIndex).getCell(col);
            if (rowIndex == fromRow) {
                cell.getCTTc().addNewTcPr().addNewVMerge()
                        .setVal(STMerge.RESTART);
            } else {
                cell.getCTTc().addNewTcPr().addNewVMerge().setVal(STMerge.CONTINUE);
            }
        }
    }

    private void buildCellBorder(Border border, XWPFTableCell tableCell, int type) {
        CTTcPr cellPropertie = tableCell.getCTTc().getTcPr();
        if (cellPropertie == null) {
            cellPropertie = tableCell.getCTTc().addNewTcPr();
        }
        CTTcBorders borders = cellPropertie.getTcBorders();
        if (borders == null) {
            borders = cellPropertie.addNewTcBorders();
            ;
        }
        BorderStyle borderStyle = border.getStyle();
        CTBorder ctborder = null;
        if (type == 1) {
            ctborder = borders.addNewLeft();
        } else if (type == 2) {
            ctborder = borders.addNewRight();
        } else if (type == 3) {
            ctborder = borders.addNewTop();
        } else if (type == 4) {
            ctborder = borders.addNewBottom();
        }
        if (borderStyle.equals(BorderStyle.dashed)) {
            ctborder.setVal(STBorder.DASHED);
        } else if (borderStyle.equals(BorderStyle.doublesolid)) {
            ctborder.setVal(STBorder.DOUBLE);
        } else {
            ctborder.setVal(STBorder.SINGLE);
        }
        int borderWidth = border.getWidth();
        if (borderWidth > 1) {
            ctborder.setSz(BigInteger.valueOf(DxaUtils.points2dxa(borderWidth)));
        }
        String color = border.getColor();
        if (StringUtils.isNotBlank(color)) {
            ctborder.setColor(toHex(color.split(",")));
        }
    }

    /**
     * 判断当前报表是否包含悬浮元素（图片或文本）。仅当存在非空列表时返回 true，避免对旧报表产生任何影响。
     */
    private boolean hasFloatElements(Report report) {
        List<FloatImage> floatImages = report.getFloatImages();
        List<FloatText> floatTexts = report.getFloatTexts();
        boolean hasImages = floatImages != null && !floatImages.isEmpty();
        boolean hasTexts = floatTexts != null && !floatTexts.isEmpty();
        return hasImages || hasTexts;
    }

    /**
     * 将悬浮图片解析为输入流，支持 base64 与 URL（text）两种来源。对 URL 图片使用 {@link ImageUtils#getImageBase64Data}，有 image-not-exist 兜底机制。
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
        try {
            String base64Data = ImageUtils.getImageBase64Data(ImageType.image, value, 0, 0);
            if (StringUtils.isNotBlank(base64Data)) {
                return ImageUtils.base64DataToInputStream(base64Data);
            }
        } catch (Exception e) {
        }
        return null;
    }

    private static final Logger log = Logger.getLogger(WordProducer.class.getName());

    /**
     * 合并报表中的所有悬浮元素并按 layer 升序排序（值越大越靠上，后渲染覆盖先渲染）。
     */
    private List<FloatElement> collectAndSortFloatElements(Report report) {
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
        elements.sort((a, b) -> {
            int za = a.getLayer() == null ? 0 : a.getLayer();
            int zb = b.getLayer() == null ? 0 : b.getLayer();
            return Integer.compare(za, zb);
        });
        return elements;
    }

    /**
     * 在当前 Word 页面上渲染属于该页的悬浮元素。使用 Word 的 framePr（段落框架属性）实现绝对定位：hAnchor/vAnchor 设为 margin（相对页边距定位），wrap 设为 none（浮于文字上方）。x/y/w/h 以 twips（DXA）为单位。top/left 为页面相对坐标（相对于每页内容区左上角）。repeatPrint=true 时每页重复打印；repeatPrint=false 时仅在第1页渲染。
     *
     * @param document  XWPFDocument
     * @param report    报表对象
     * @param pageIndex 当前页码（从 0 开始）
     * @param paper     页面设置（用于计算内容区高度做边界检查）
     */
    private void renderFloatElementsForPage(XWPFDocument document, Report report, int pageIndex,
                                            Paper paper,
                                            Map<String, byte[]> imageDataCache) throws Exception {
        float paperExtent = paper.getOrientation() != null && paper.getOrientation().equals(Orientation.landscape)
                ? paper.getWidth() : paper.getHeight();
        float pageContentHeight = paperExtent - paper.getTopMargin() - paper.getBottomMargin();
        List<FloatElement> elements = collectAndSortFloatElements(report);
        String ns = "http://schemas.openxmlformats.org/wordprocessingml/2006/main";
        for (FloatElement el : elements) {
            Integer top = el.getTop();
            if (top == null) {
                continue;
            }
            float elementTopPt = UnitUtils.pixelToPoint(top.intValue());
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
            float xPt = left != null ? UnitUtils.pixelToPoint(left.intValue()) : 0;
            float yPt = elementTopPt;
            Integer elWidth = el.getWidth();
            Integer elHeight = el.getHeight();
            float widthPt = elWidth != null ? UnitUtils.pixelToPoint(elWidth.intValue()) : 0;
            float heightPt = elHeight != null ? UnitUtils.pixelToPoint(elHeight.intValue()) : 0;

            int dxaX = DxaUtils.points2dxa((int) Math.round(xPt));
            int dxaY = DxaUtils.points2dxa((int) Math.round(yPt));
            int dxaW = DxaUtils.points2dxa((int) Math.round(widthPt));
            int dxaH = DxaUtils.points2dxa((int) Math.round(heightPt));

            try {
                XWPFParagraph para = document.createParagraph();
                CTPPr pPr = para.getCTP().addNewPPr();
                CTFramePr framePr = pPr.addNewFramePr();
                XmlCursor cursor = framePr.newCursor();
                cursor.setAttributeText(new QName(ns, "wrap"), "none");
                cursor.setAttributeText(new QName(ns, "hAnchor"), "margin");
                cursor.setAttributeText(new QName(ns, "vAnchor"), "margin");
                cursor.setAttributeText(new QName(ns, "x"), String.valueOf(dxaX));
                cursor.setAttributeText(new QName(ns, "y"), String.valueOf(dxaY));
                cursor.setAttributeText(new QName(ns, "w"), String.valueOf(dxaW));
                cursor.setAttributeText(new QName(ns, "h"), String.valueOf(dxaH));
                cursor.dispose();

                if (el instanceof FloatImage) {
                    FloatImage fi = (FloatImage) el;
                    XWPFRun run = para.createRun();
                    String cacheKey = fi.getName() != null ? fi.getName() : String.valueOf(System.identityHashCode(fi));
                    byte[] imageBytes = imageDataCache != null ? imageDataCache.get(cacheKey) : null;
                    if (imageBytes == null) {
                        InputStream input = buildFloatImageInputStream(fi);
                        if (input == null) {
                            continue;
                        }
                        try {
                            imageBytes = IOUtils.toByteArray(input);
                        } finally {
                            IOUtils.closeQuietly(input);
                        }
                        if (imageDataCache != null) {
                            imageDataCache.put(cacheKey, imageBytes);
                        }
                    }
                    try {
                        int widthPx = elWidth != null ? elWidth.intValue() : 100;
                        int heightPx = elHeight != null ? elHeight.intValue() : 75;
                        String path = fi.getPath();
                        int pictureType = XWPFDocument.PICTURE_TYPE_PNG;
                        String ext = ".png";
                        if (StringUtils.isNotBlank(path)) {
                            String lowerPath = path.toLowerCase();
                            if (lowerPath.endsWith(".jpg") || lowerPath.endsWith(".jpeg")) {
                                pictureType = XWPFDocument.PICTURE_TYPE_JPEG;
                                ext = ".jpg";
                            } else if (lowerPath.endsWith(".gif")) {
                                pictureType = XWPFDocument.PICTURE_TYPE_GIF;
                                ext = ".gif";
                            }
                        } else {
                            String expr = fi.getExpr();
                            if (expr != null && expr.length() >= 4 && expr.startsWith("/9j/")) {
                                pictureType = XWPFDocument.PICTURE_TYPE_JPEG;
                                ext = ".jpg";
                            }
                        }
                        String imageName = "float-" + (fi.getName() != null ? fi.getName() : "image");
                        InputStream cachedInput = new java.io.ByteArrayInputStream(imageBytes);
                        try {
                            run.addPicture(cachedInput, pictureType, imageName + ext,
                                    Units.toEMU(UnitUtils.pixelToPoint(widthPx)),
                                    Units.toEMU(UnitUtils.pixelToPoint(heightPx)));
                        } finally {
                            IOUtils.closeQuietly(cachedInput);
                        }
                    } catch (Exception e) {
                        String elName = fi.getName() != null ? fi.getName() : "image";
                        log.log(Level.WARNING, "导出 Word 悬浮图片 [" + elName + "] 失败", e);
                    }
                } else if (el instanceof FloatText) {
                    FloatText ft = (FloatText) el;
                    XWPFRun run = para.createRun();
                    String value = ft.getValue();
                    if (value != null) {
                        run.setText(value);
                    }
                    String fontFamily = ft.getFontFamily();
                    if (StringUtils.isNotBlank(fontFamily)) {
                        run.setFontFamily(fontFamily);
                    }
                    Integer fontSize = ft.getFontSize();
                    if (fontSize != null && fontSize > 0) {
                        run.setFontSize(UnitUtils.pixelToPoint(fontSize.intValue()));
                    }
                    String forecolor = ft.getForecolor();
                    if (StringUtils.isNotEmpty(forecolor)) {
                        String[] colors = forecolor.split(",");
                        if (colors.length >= 3) {
                            run.setColor(toHex(colors));
                        }
                    }
                    if (ft.getBold() != null && ft.getBold()) {
                        run.setBold(true);
                    }
                    if (ft.getItalic() != null && ft.getItalic()) {
                        run.setItalic(true);
                    }
                    if (ft.getUnderline() != null && ft.getUnderline()) {
                        run.setUnderline(UnderlinePatterns.SINGLE);
                    }
                    if (ft.getAlign() != null) {
                        String a = ft.getAlign();
                        if ("center".equals(a)) {
                            para.setAlignment(ParagraphAlignment.CENTER);
                        } else if ("right".equals(a)) {
                            para.setAlignment(ParagraphAlignment.RIGHT);
                        }
                    }
                    if (ft.getValign() != null && !"top".equals(ft.getValign())) {
                        int totalHeightDxa = dxaH;
                        int fontSizeHalfPt = fontSize != null ? UnitUtils.pixelToPoint(fontSize) * 10 : 120;
                        if ("middle".equals(ft.getValign())) {
                            int spacing = Math.max(0, (totalHeightDxa - fontSizeHalfPt) / 2);
                            CTSpacing spacingEl = para.getCTP().getPPr().isSetSpacing() ?
                                    para.getCTP().getPPr().getSpacing() : para.getCTP().getPPr().addNewSpacing();
                            spacingEl.setBefore(spacing);
                        } else if ("bottom".equals(ft.getValign())) {
                            int spacing = Math.max(0, totalHeightDxa - fontSizeHalfPt);
                            CTSpacing spacingEl = para.getCTP().getPPr().isSetSpacing() ?
                                    para.getCTP().getPPr().getSpacing() : para.getCTP().getPPr().addNewSpacing();
                            spacingEl.setBefore(spacing);
                        }
                    }
                    String bgcolor = ft.getBgcolor();
                    if (StringUtils.isNotEmpty(bgcolor)) {
                        String[] bgColors = bgcolor.split(",");
                        if (bgColors.length >= 3) {
                            String hexBg = toHex(bgColors);
                            CTShd shading = para.getCTP().getPPr().isSetShd() ?
                                    para.getCTP().getPPr().getShd() : para.getCTP().getPPr().addNewShd();
                            shading.setVal(STShd.CLEAR);
                            shading.setFill(hexBg);
                        }
                    }
                    if (ft.getTopBorder() != null || ft.getBottomBorder() != null
                            || ft.getLeftBorder() != null || ft.getRightBorder() != null) {
                        CTPBdr pBdr = para.getCTP().getPPr().isSetPBdr() ?
                                para.getCTP().getPPr().getPBdr() : para.getCTP().getPPr().addNewPBdr();
                        if (ft.getTopBorder() != null) {
                            pBdr.setTop(buildCTBorder(ft.getTopBorder()));
                        }
                        if (ft.getBottomBorder() != null) {
                            pBdr.setBottom(buildCTBorder(ft.getBottomBorder()));
                        }
                        if (ft.getLeftBorder() != null) {
                            pBdr.setLeft(buildCTBorder(ft.getLeftBorder()));
                        }
                        if (ft.getRightBorder() != null) {
                            pBdr.setRight(buildCTBorder(ft.getRightBorder()));
                        }
                    }
                }
            } catch (Exception e) {
                String elName = el.getName() != null ? el.getName() : (el instanceof FloatImage ? "image" : "text");
                log.log(Level.WARNING, "导出 Word 悬浮元素 [" + elName + "] 失败，已跳过", e);
            }
        }
    }

    private String toHex(String rgb[]) {
        StringBuffer sb = new StringBuffer();
        String R = Integer.toHexString(Integer.valueOf(rgb[0]));
        String G = Integer.toHexString(Integer.valueOf(rgb[1]));
        String B = Integer.toHexString(Integer.valueOf(rgb[2]));
        R = R.length() == 1 ? "0" + R : R;
        G = G.length() == 1 ? "0" + G : G;
        B = B.length() == 1 ? "0" + B : B;
        sb.append(R);
        sb.append(G);
        sb.append(B);
        return sb.toString();
    }

    /**
     * 将 Border 对象转换为 Word CTBorder。
     */
    private CTBorder buildCTBorder(Border border) {
        if (border == null || border.getStyle() == null) return null;
        STBorder.Enum stBorder = STBorder.SINGLE;
        String styleName = border.getStyle().toString();
        if ("dashed".equals(styleName)) {
            stBorder = STBorder.DASHED;
        } else if ("doublesolid".equals(styleName)) {
            stBorder = STBorder.DOUBLE;
        }
        int borderW = border.getWidth() > 0 ? border.getWidth() : 1;
        String borderHex = "000000";
        String borderColorStr = border.getColor();
        if (StringUtils.isNotEmpty(borderColorStr)) {
            String[] bcArr = borderColorStr.split(",");
            if (bcArr.length >= 3) {
                borderHex = toHex(bcArr);
            }
        }
        CTBorder ctBorder = CTBorder.Factory.newInstance();
        ctBorder.setVal(stBorder);
        ctBorder.setSz(BigInteger.valueOf(borderW * 4));
        ctBorder.setSpace(BigInteger.ZERO);
        ctBorder.setColor(borderHex);
        return ctBorder;
    }
}
