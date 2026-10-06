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
import org.openxmlformats.schemas.drawingml.x2006.main.CTGraphicalObject;
import org.openxmlformats.schemas.drawingml.x2006.main.CTNonVisualDrawingProps;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPoint2D;
import org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTAnchor;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTEffectExtent;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTPosH;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.CTPosV;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.STRelFromH;
import org.openxmlformats.schemas.drawingml.x2006.wordprocessingDrawing.STRelFromV;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.*;
import org.apache.xmlbeans.XmlCursor;

import javax.imageio.ImageIO;
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
    private static final Logger log = Logger.getLogger(WordProducer.class.getName());
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
            int[] floatDrawingIdSeq = new int[]{1};
            int pageIndex = 1;
            for (Page page : pages) {
                List<Row> rows = page.getRows();
                int visibleRowCount = 0;
                for (Row row : rows) {
                    if (!row.isHiddenFormLayout()) {
                        visibleRowCount++;
                    }
                }
                if (visibleRowCount < 1) {
                    visibleRowCount = 1;
                }
                XWPFTable table = document.createTable(visibleRowCount, totalColumn);
                lockWordTableGeometry(table, columns, tableWidth);
                int wordRowNumber = 0;
                for (int rowNumber = 0; rowNumber < rows.size(); rowNumber++) {
                    Row row = rows.get(rowNumber);
                    if (row.isHiddenFormLayout()) {
                        continue;
                    }
                    int height = row.getRealHeight();
                    XWPFTableRow tableRow = table.getRow(wordRowNumber);
                    applyExactRowHeight(tableRow, height);
                    Map<Column, Cell> colCell = cellMap.get(row);
                    if (colCell == null) {
                        wordRowNumber++;
                        continue;
                    }
                    int skipCol = 0;
                    for (Column col : columns) {
                        if (col.isHiddenFormLayout()) {
                            skipCol++;
                            continue;
                        }
                        int width = col.getWidth();
                        int colNumber = col.getColumnNumber() - 1 - skipCol;
                        Cell cell = colCell.get(col);
                        XWPFTableCell tableCell = tableRow.getCell(colNumber);
                        if (tableCell == null) {
                            continue;
                        }
                        writeCellWidth(ensureSingleTcPr(tableCell), width);
                        if (cell == null) {
                            continue;
                        }
                        buildTableCellStyle(table, tableCell, cell, wordRowNumber, colNumber);
                    }
                    wordRowNumber++;
                }
                if (hasFloat) {
                    renderFloatElementsForPage(document, report, pageIndex - 1, paper, imageDataCache, floatDrawingIdSeq);
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

    /**
     * 按设计列宽锁定表格几何（固定布局、tblGrid、零边距）
     *
     * @param table      当前页表格
     * @param columns    报表列（隐藏列跳过）
     * @param tableWidth 可见列宽合计（磅）
     */
    private void lockWordTableGeometry(XWPFTable table, List<Column> columns, int tableWidth) {
        CTTbl ctTbl = table.getCTTbl();
        CTTblPr tblPr = ctTbl.getTblPr() != null ? ctTbl.getTblPr() : ctTbl.addNewTblPr();
        writeTableBordersNone(tblPr);
        int tableDxa = DxaUtils.points2dxa(tableWidth);
        CTTblWidth tblW = tblPr.isSetTblW() ? tblPr.getTblW() : tblPr.addNewTblW();
        tblW.setType(STTblWidth.DXA);
        tblW.setW(BigInteger.valueOf(tableDxa));
        CTTblLayoutType layout = tblPr.isSetTblLayout() ? tblPr.getTblLayout() : tblPr.addNewTblLayout();
        layout.setType(STTblLayoutType.FIXED);
        zeroTableCellMargins(tblPr);
        CTTblGrid grid = ctTbl.getTblGrid() != null ? ctTbl.getTblGrid() : ctTbl.addNewTblGrid();
        while (grid.sizeOfGridColArray() > 0) {
            grid.removeGridCol(0);
        }
        for (Column col : columns) {
            if (col.isHiddenFormLayout()) {
                continue;
            }
            grid.addNewGridCol().setW(BigInteger.valueOf(DxaUtils.points2dxa(col.getWidth())));
        }
    }

    /**
     * 表级四面和内部边框写成 none
     *
     * @param tblPr 表格属性
     */
    private void writeTableBordersNone(CTTblPr tblPr) {
        CTTblBorders borders = tblPr.isSetTblBorders() ? tblPr.getTblBorders() : tblPr.addNewTblBorders();
        writeNoneBorder(borders.isSetTop() ? borders.getTop() : borders.addNewTop());
        writeNoneBorder(borders.isSetLeft() ? borders.getLeft() : borders.addNewLeft());
        writeNoneBorder(borders.isSetBottom() ? borders.getBottom() : borders.addNewBottom());
        writeNoneBorder(borders.isSetRight() ? borders.getRight() : borders.addNewRight());
        writeNoneBorder(borders.isSetInsideH() ? borders.getInsideH() : borders.addNewInsideH());
        writeNoneBorder(borders.isSetInsideV() ? borders.getInsideV() : borders.addNewInsideV());
    }

    /**
     * 将一条表边框写成 none
     *
     * @param ctBorder 边框节点
     */
    private void writeNoneBorder(CTBorder ctBorder) {
        ctBorder.setVal(STBorder.NONE);
        if (ctBorder.isSetSz()) {
            ctBorder.unsetSz();
        }
        if (ctBorder.isSetColor()) {
            ctBorder.unsetColor();
        }
    }

    /**
     * 取或创建单元格唯一的 tcPr
     *
     * @param tableCell Word 单元格
     * @return 单元格属性
     */
    private CTTcPr ensureSingleTcPr(XWPFTableCell tableCell) {
        CTTc ctTc = tableCell.getCTTc();
        return ctTc.isSetTcPr() ? ctTc.getTcPr() : ctTc.addNewTcPr();
    }

    /**
     * 把列宽写进已有 tcPr
     *
     * @param tcPr  单元格属性
     * @param width 列宽（磅）
     */
    private void writeCellWidth(CTTcPr tcPr, int width) {
        CTTblWidth tcW = tcPr.isSetTcW() ? tcPr.getTcW() : tcPr.addNewTcW();
        tcW.setType(STTblWidth.DXA);
        tcW.setW(BigInteger.valueOf(DxaUtils.points2dxa(width)));
    }

    /**
     * 表格单元格边距清零
     *
     * @param tblPr 表格属性
     */
    private void zeroTableCellMargins(CTTblPr tblPr) {
        CTTblCellMar cellMar = tblPr.isSetTblCellMar() ? tblPr.getTblCellMar() : tblPr.addNewTblCellMar();
        setDxaMargin(cellMar.isSetTop() ? cellMar.getTop() : cellMar.addNewTop(), 0);
        setDxaMargin(cellMar.isSetLeft() ? cellMar.getLeft() : cellMar.addNewLeft(), 0);
        setDxaMargin(cellMar.isSetBottom() ? cellMar.getBottom() : cellMar.addNewBottom(), 0);
        setDxaMargin(cellMar.isSetRight() ? cellMar.getRight() : cellMar.addNewRight(), 0);
    }

    /**
     * 将表格边距写成 DXA 值
     *
     * @param margin 边距节点
     * @param dxa    缇
     */
    private void setDxaMargin(CTTblWidth margin, int dxa) {
        margin.setType(STTblWidth.DXA);
        margin.setW(BigInteger.valueOf(dxa));
    }

    /**
     * 行高按设计磅写成 EXACT
     *
     * @param tableRow  Word 行
     * @param heightPt  行高（磅）
     */
    private void applyExactRowHeight(XWPFTableRow tableRow, int heightPt) {
        int dxa = DxaUtils.points2dxa(heightPt);
        tableRow.setHeight(dxa);
        CTRow ctRow = tableRow.getCtRow();
        CTTrPr trPr = ctRow.isSetTrPr() ? ctRow.getTrPr() : ctRow.addNewTrPr();
        CTHeight ctHeight = trPr.sizeOfTrHeightArray() > 0 ? trPr.getTrHeightArray(0) : trPr.addNewTrHeight();
        ctHeight.setVal(BigInteger.valueOf(dxa));
        ctHeight.setHRule(STHeightRule.EXACT);
    }

    private int[] buildColumnSizeAndTotalWidth(List<Column> columns) {
        int count = 0, totalWidth = 0;
        for (int i = 0; i < columns.size(); i++) {
            Column col = columns.get(i);
            if (col.isHiddenFormLayout()) {
                continue;
            }
            count++;
            totalWidth += col.getWidth();
        }
        return new int[]{count, totalWidth};
    }

    /**
     * 合并格可见列总宽（pt）
     *
     * @param cell 单元格
     * @return 宽度（pt）
     */
    private int buildWholeWidthPt(Cell cell) {
        int width = 0;
        int colSpan = cell.getColSpan();
        int count = colSpan < 1 ? 1 : colSpan;
        Column col = cell.getColumn();
        for (int i = 0; i < count && col != null; i++) {
            if (!col.isHiddenFormLayout()) {
                width += col.getWidth();
            }
            col = col.getNext();
        }
        return width;
    }

    /**
     * 合并格可见行总高（pt）
     *
     * @param cell 单元格
     * @return 高度（pt）
     */
    private int buildWholeHeightPt(Cell cell) {
        int height = 0;
        int rowSpan = cell.getPageRowSpan();
        int count = rowSpan < 1 ? 1 : rowSpan;
        Row row = cell.getRow();
        for (int i = 0; i < count && row != null; i++) {
            if (!row.isHiddenFormLayout()) {
                height += row.getRealHeight();
            }
            row = row.getNext();
        }
        return height;
    }

    private void buildTableCellStyle(XWPFTable table, XWPFTableCell tableCell, Cell cell, int rowNumber, int columnNumber) {
        CellStyle style = cell.getCellStyle();
        CellStyle customStyle = cell.getCustomCellStyle();
        CellStyle rowStyle = cell.getRow().getCustomCellStyle();
        CellStyle colStyle = cell.getColumn().getCustomCellStyle();
        CTTcPr cellProperties = ensureSingleTcPr(tableCell);
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
        int rowSpan = cell.getLayoutPageRowSpan();
        int colSpan = cell.getLayoutColSpan();
        applyCellBorder(tableCell, leftBorder, 1);
        applyCellBorder(tableCell, rightBorder, 2);
        applyCellBorder(tableCell, topBorder, 3);
        applyCellBorder(tableCell, bottomBorder, 4);
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
            CTTcPr tcPr = ensureSingleTcPr(cell);
            CTHMerge hMerge = tcPr.isSetHMerge() ? tcPr.getHMerge() : tcPr.addNewHMerge();
            hMerge.setVal(cellIndex == startCol ? STMerge.RESTART : STMerge.CONTINUE);
        }
    }

    private void mergeCellsVertically(XWPFTable table, int col, int fromRow, int toRow) {
        for (int rowIndex = fromRow; rowIndex <= toRow; rowIndex++) {
            XWPFTableCell cell = table.getRow(rowIndex).getCell(col);
            CTTcPr tcPr = ensureSingleTcPr(cell);
            CTVMerge vMerge = tcPr.isSetVMerge() ? tcPr.getVMerge() : tcPr.addNewVMerge();
            vMerge.setVal(rowIndex == fromRow ? STMerge.RESTART : STMerge.CONTINUE);
        }
    }

    /**
     * 判断边框是否可见
     *
     * @param border 单元格边框，null 表示无线
     * @return 需要画线时为 true
     */
    private boolean isDrawnBorder(Border border) {
        return border != null && border.getStyle() != null;
    }

    /**
     * 有设计边框才写节点，无线不写，避免相邻格 nil 冲掉有框格子的线
     *
     * @param tableCell 目标格，null 则忽略
     * @param border    设计边框，null 表示关闭
     * @param type      1 左 / 2 右 / 3 上 / 4 下
     */
    private void applyCellBorder(XWPFTableCell tableCell, Border border, int type) {
        if (tableCell == null) {
            return;
        }
        CTTcPr cellProperties = ensureSingleTcPr(tableCell);
        if (!isDrawnBorder(border)) {
            return;
        }
        CTTcBorders borders = cellProperties.isSetTcBorders() ? cellProperties.getTcBorders() : cellProperties.addNewTcBorders();
        CTBorder ctborder = borderSide(borders, type);
        BorderStyle borderStyle = border.getStyle();
        if (borderStyle.equals(BorderStyle.dashed)) {
            ctborder.setVal(STBorder.DASHED);
        } else if (borderStyle.equals(BorderStyle.doublesolid)) {
            ctborder.setVal(STBorder.DOUBLE);
        } else {
            ctborder.setVal(STBorder.SINGLE);
        }
        int borderWidth = Math.max(border.getWidth(), 1);
        ctborder.setSz(BigInteger.valueOf(borderWidth * 8L));
        String color = border.getColor();
        if (StringUtils.isNotBlank(color)) {
            ctborder.setColor(toHex(color.split(",")));
        }
    }

    /**
     * 取或创建指定方向的边框节点
     *
     * @param borders 单元格边框集合
     * @param type    1 左 / 2 右 / 3 上 / 4 下
     * @return 对应方向的边框
     */
    private CTBorder borderSide(CTTcBorders borders, int type) {
        if (type == 1) {
            return borders.isSetLeft() ? borders.getLeft() : borders.addNewLeft();
        }
        if (type == 2) {
            return borders.isSetRight() ? borders.getRight() : borders.addNewRight();
        }
        if (type == 3) {
            return borders.isSetTop() ? borders.getTop() : borders.addNewTop();
        }
        return borders.isSetBottom() ? borders.getBottom() : borders.addNewBottom();
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
     * 渲染当前页悬浮元素：图用 wp:anchor，文本用 VML 文本框
     *
     * @param document          XWPFDocument
     * @param report            报表对象
     * @param pageIndex         当前页码（从 0 开始）
     * @param paper             页面设置
     * @param imageDataCache    悬浮图片字节缓存
     * @param floatDrawingIdSeq 文档内 docPr id 自增序列（长度为 1）
     */
    private void renderFloatElementsForPage(XWPFDocument document, Report report, int pageIndex,
                                            Paper paper,
                                            Map<String, byte[]> imageDataCache,
                                            int[] floatDrawingIdSeq) throws Exception {
        float paperExtent = paper.getOrientation() != null && paper.getOrientation().equals(Orientation.landscape)
                ? paper.getWidth() : paper.getHeight();
        float pageContentHeight = paperExtent - paper.getTopMargin() - paper.getBottomMargin();
        float leftMargin = paper.getLeftMargin();
        float topMargin = paper.getTopMargin();
        List<FloatElement> elements = collectAndSortFloatElements(report);
        for (FloatElement el : elements) {
            Integer top = el.getTop();
            String elName = el.getName() != null ? el.getName() : el.getClass().getSimpleName();
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

            if (el instanceof FloatImage) {
                renderFloatImageAsAnchor(document, (FloatImage) el, leftMargin, topMargin,
                        xPt, yPt, widthPt, heightPt, elWidth, elHeight, imageDataCache, floatDrawingIdSeq);
                continue;
            }

            if (!(el instanceof FloatText)) {
                continue;
            }
            try {
                renderFloatTextAsOverlay(document, (FloatText) el, xPt, yPt, widthPt, heightPt, floatDrawingIdSeq);
            } catch (Exception e) {
                log.log(Level.WARNING, "导出 Word 悬浮元素 [" + elName + "] 失败，已跳过", e);
            }
        }
    }

    /**
     * 用 VML 绝对定位文本框写悬浮文字
     *
     * @param document          Word 文档
     * @param ft                悬浮文本
     * @param xPt               相对页边 X（磅）
     * @param yPt               相对页边 Y（磅）
     * @param widthPt           宽（磅）
     * @param heightPt          高（磅）
     * @param floatDrawingIdSeq 形状 id 序列
     */
    private void renderFloatTextAsOverlay(XWPFDocument document, FloatText ft,
                                          float xPt, float yPt, float widthPt, float heightPt,
                                          int[] floatDrawingIdSeq) throws Exception {
        XWPFParagraph para = document.createParagraph();
        minimizeFloatParagraph(para);
        long shapeId = floatDrawingIdSeq[0]++;
        if (shapeId <= 0) {
            floatDrawingIdSeq[0] = 1;
            shapeId = floatDrawingIdSeq[0]++;
        }
        String xml = buildFloatTextVml(ft, shapeId, xPt, yPt, widthPt, heightPt);
        CTR ctr = para.createRun().getCTR();
        CTPicture pict = ctr.addNewPict();
        org.apache.xmlbeans.XmlObject parsed = org.apache.xmlbeans.XmlObject.Factory.parse(xml);
        pict.set(parsed);
    }

    /**
     * 构造相对页边、不绕排的 VML 文本框 XML
     *
     * @param ft      悬浮文本
     * @param shapeId 形状 id
     * @param xPt     X（磅）
     * @param yPt     Y（磅）
     * @param widthPt 宽（磅）
     * @param heightPt 高（磅）
     * @return pict 内 XML
     */
    private String buildFloatTextVml(FloatText ft, long shapeId, float xPt, float yPt, float widthPt, float heightPt) {
        String fill = rgbToVmlColor(ft.getBgcolor(), null);
        Border strokeBorder = firstDrawnBorder(ft);
        String stroke = strokeBorder != null ? rgbToVmlColor(strokeBorder.getColor(), "000000") : "000000";
        String stroked = strokeBorder != null ? "t" : "f";
        String strokeWeight = "0.75pt";
        if (strokeBorder != null && strokeBorder.getWidth() > 0) {
            strokeWeight = Math.max(0.5, strokeBorder.getWidth() * 0.75) + "pt";
        }
        int z = ft.getLayer() == null ? 1 : Math.max(1, ft.getLayer().intValue());
        String jc = "left";
        if ("center".equals(ft.getAlign())) {
            jc = "center";
        } else if ("right".equals(ft.getAlign())) {
            jc = "right";
        }
        int fontPt = ft.getFontSize() != null && ft.getFontSize() > 0
                ? Math.max(1, UnitUtils.pixelToPoint(ft.getFontSize().intValue())) : 11;
        int sz = fontPt * 2;
        String family = StringUtils.isNotBlank(ft.getFontFamily()) ? ft.getFontFamily() : "宋体";
        String color = rgbToVmlColor(ft.getForecolor(), "000000");
        String b = ft.getBold() != null && ft.getBold() ? "<w:b/>" : "";
        String i = ft.getItalic() != null && ft.getItalic() ? "<w:i/>" : "";
        String u = ft.getUnderline() != null && ft.getUnderline() ? "<w:u w:val=\"single\"/>" : "";
        int beforeDxa = 0;
        if ("middle".equals(ft.getValign())) {
            beforeDxa = Math.max(0, (int) Math.round((heightPt - fontPt) * 10));
        } else if ("bottom".equals(ft.getValign())) {
            beforeDxa = Math.max(0, (int) Math.round((heightPt - fontPt) * 20));
        }
        String text = ft.getValue() == null ? "" : escapeXml(ft.getValue());
        String filled = fill != null ? "t" : "f";
        String fillColor = fill != null ? fill : "ffffff";
        StringBuilder xml = new StringBuilder();
        xml.append("<xml-fragment xmlns:v=\"urn:schemas-microsoft-com:vml\" xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" xmlns:o=\"urn:schemas-microsoft-com:office:office\">");
        xml.append("<v:shape id=\"_s").append(shapeId).append("\" type=\"#_x0000_t202\" filled=\"").append(filled).append("\"");
        xml.append(" fillcolor=\"#").append(fillColor).append("\" stroked=\"").append(stroked).append("\"");
        xml.append(" strokecolor=\"#").append(stroke).append("\" strokeweight=\"").append(strokeWeight).append("\"");
        xml.append(" style=\"position:absolute;margin-left:").append(xPt).append("pt;margin-top:").append(yPt).append("pt;");
        xml.append("width:").append(widthPt).append("pt;height:").append(heightPt).append("pt;");
        xml.append("z-index:").append(z).append(";mso-wrap-style:none;mso-position-horizontal-relative:margin;");
        xml.append("mso-position-vertical-relative:margin\">");
        xml.append("<v:textbox inset=\"0,0,0,0\"><w:txbxContent><w:p>");
        xml.append("<w:pPr><w:jc w:val=\"").append(jc).append("\"/>");
        if (beforeDxa > 0) {
            xml.append("<w:spacing w:before=\"").append(beforeDxa).append("\"/>");
        }
        xml.append("</w:pPr><w:r><w:rPr>");
        xml.append("<w:rFonts w:ascii=\"").append(escapeXml(family)).append("\" w:hAnsi=\"").append(escapeXml(family))
                .append("\" w:eastAsia=\"").append(escapeXml(family)).append("\"/>");
        xml.append("<w:sz w:val=\"").append(sz).append("\"/><w:color w:val=\"").append(color).append("\"/>");
        xml.append(b).append(i).append(u);
        xml.append("</w:rPr><w:t xml:space=\"preserve\">").append(text).append("</w:t></w:r></w:p></w:txbxContent></v:textbox>");
        xml.append("</v:shape></xml-fragment>");
        return xml.toString();
    }

    /**
     * 取第一条可见边框，供文本框描边
     *
     * @param ft 悬浮文本
     * @return 边框，没有则 null
     */
    private Border firstDrawnBorder(FloatText ft) {
        if (isDrawnBorder(ft.getTopBorder())) {
            return ft.getTopBorder();
        }
        if (isDrawnBorder(ft.getLeftBorder())) {
            return ft.getLeftBorder();
        }
        if (isDrawnBorder(ft.getRightBorder())) {
            return ft.getRightBorder();
        }
        if (isDrawnBorder(ft.getBottomBorder())) {
            return ft.getBottomBorder();
        }
        return null;
    }

    /**
     * RGB 字符串转 VML 六位色
     *
     * @param rgb      如 208,2,27
     * @param fallback 无颜色时的六位色，null 表示不填充
     * @return 六位色或 null
     */
    private String rgbToVmlColor(String rgb, String fallback) {
        if (StringUtils.isBlank(rgb)) {
            return fallback;
        }
        String[] parts = rgb.split(",");
        if (parts.length < 3) {
            return fallback;
        }
        return toHex(parts);
    }

    /**
     * XML 文本转义
     *
     * @param raw 原文
     * @return 转义结果
     */
    private String escapeXml(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    /**
     * 将悬浮图片转为 wp:anchor 写入文档，失败时移除段落
     *
     * @param document          Word 文档
     * @param fi                悬浮图片
     * @param leftMargin        左边距（磅）
     * @param topMargin         上边距（磅）
     * @param xPt               相对内容区 X（磅）
     * @param yPt               相对内容区 Y（磅）
     * @param widthPt           宽（磅）
     * @param heightPt          高（磅）
     * @param elWidth           宽（像素），可空
     * @param elHeight          高（像素），可空
     * @param imageDataCache    图片缓存
     * @param floatDrawingIdSeq docPr id 序列
     */
    private void renderFloatImageAsAnchor(XWPFDocument document, FloatImage fi,
                                          float leftMargin, float topMargin,
                                          float xPt, float yPt, float widthPt, float heightPt,
                                          Integer elWidth, Integer elHeight,
                                          Map<String, byte[]> imageDataCache,
                                          int[] floatDrawingIdSeq) {
        String elName = fi.getName() != null ? fi.getName() : "image";
        String cacheKey = fi.getName() != null ? fi.getName() : String.valueOf(System.identityHashCode(fi));
        byte[] imageBytes = imageDataCache != null ? imageDataCache.get(cacheKey) : null;
        if (imageBytes == null) {
            InputStream input = buildFloatImageInputStream(fi);
            if (input == null) {
                return;
            }
            try {
                imageBytes = IOUtils.toByteArray(input);
            } catch (Exception e) {
                return;
            } finally {
                IOUtils.closeQuietly(input);
            }
            if (imageDataCache != null) {
                imageDataCache.put(cacheKey, imageBytes);
            }
        }
        if (imageBytes == null || imageBytes.length == 0) {
            return;
        }

        int widthPx = elWidth != null ? elWidth.intValue() : 100;
        int heightPx = elHeight != null ? elHeight.intValue() : 75;
        int emuW = (int) Units.toEMU(widthPt > 0 ? widthPt : UnitUtils.pixelToPoint(widthPx));
        int emuH = (int) Units.toEMU(heightPt > 0 ? heightPt : UnitUtils.pixelToPoint(heightPx));
        int emuX = (int) Units.toEMU(leftMargin + xPt);
        int emuY = (int) Units.toEMU(topMargin + yPt);
        long relativeHeight = fi.getLayer() != null ? Math.max(0, fi.getLayer().longValue()) : 0L;
        long docPrId = floatDrawingIdSeq[0]++;
        if (docPrId <= 0 || docPrId > Integer.MAX_VALUE) {
            floatDrawingIdSeq[0] = 1;
            docPrId = floatDrawingIdSeq[0]++;
        }

        XWPFParagraph para = document.createParagraph();
        minimizeFloatParagraph(para);
        XWPFRun run = para.createRun();
        try {
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
            String imageName = "float-" + elName;
            InputStream cachedInput = new java.io.ByteArrayInputStream(imageBytes);
            try {
                run.addPicture(cachedInput, pictureType, imageName + ext, emuW, emuH);
            } finally {
                IOUtils.closeQuietly(cachedInput);
            }
            if (run.getCTR().sizeOfDrawingArray() < 1
                    || run.getCTR().getDrawingArray(0).sizeOfInlineArray() < 1) {
                throw new IllegalStateException("addPicture 未生成 inline drawing");
            }
            CTDrawing drawing = run.getCTR().getDrawingArray(0);
            CTGraphicalObject graphic = drawing.getInlineArray(0).getGraphic();
            CTAnchor anchor = buildFloatImageAnchor(graphic, imageName, emuW, emuH, emuX, emuY,
                    relativeHeight, docPrId);
            drawing.setAnchorArray(new CTAnchor[]{anchor});
            drawing.removeInline(0);
            run.setFontSize(1);
            run.setColor("FFFFFF");
            run.setText(" ", 0);
        } catch (Exception e) {
            removeParagraphQuietly(document, para);
            log.log(Level.WARNING, "导出 Word 悬浮图片 [" + elName + "] 失败", e);
        }
    }

    /**
     * 构造相对 page、wrapNone 的悬浮图片 CTAnchor
     *
     * @param graphic         图片 graphic
     * @param name            图名
     * @param emuW            宽 EMU
     * @param emuH            高 EMU
     * @param emuX            页内 X EMU
     * @param emuY            页内 Y EMU
     * @param relativeHeight  叠放高度（layer）
     * @param docPrId         文档内唯一 id
     * @return CTAnchor
     */
    private CTAnchor buildFloatImageAnchor(CTGraphicalObject graphic, String name,
                                           int emuW, int emuH, int emuX, int emuY,
                                           long relativeHeight, long docPrId) {
        CTAnchor anchor = CTAnchor.Factory.newInstance();
        anchor.setDistT(0);
        anchor.setDistB(0);
        anchor.setDistL(0);
        anchor.setDistR(0);
        anchor.setSimplePos2(false);
        anchor.setRelativeHeight(relativeHeight);
        anchor.setBehindDoc(false);
        anchor.setLocked(false);
        anchor.setLayoutInCell(true);
        anchor.setAllowOverlap(true);

        CTPoint2D simplePos = anchor.addNewSimplePos();
        simplePos.setX(0);
        simplePos.setY(0);

        CTPosH posH = anchor.addNewPositionH();
        posH.setRelativeFrom(STRelFromH.PAGE);
        posH.setPosOffset(emuX);

        CTPosV posV = anchor.addNewPositionV();
        posV.setRelativeFrom(STRelFromV.PAGE);
        posV.setPosOffset(emuY);

        CTPositiveSize2D extent = anchor.addNewExtent();
        extent.setCx(emuW);
        extent.setCy(emuH);

        CTEffectExtent effectExtent = anchor.addNewEffectExtent();
        effectExtent.setL(0L);
        effectExtent.setT(0L);
        effectExtent.setR(0L);
        effectExtent.setB(0L);

        anchor.addNewWrapNone();

        CTNonVisualDrawingProps docPr = anchor.addNewDocPr();
        docPr.setId(docPrId);
        docPr.setName(name);
        if (StringUtils.isNotBlank(name)) {
            docPr.setDescr(name);
        }

        anchor.addNewCNvGraphicFramePr();
        anchor.setGraphic(graphic);
        return anchor;
    }

    /**
     * 压缩悬浮段落的流式占位
     *
     * @param para 段落
     */
    private void minimizeFloatParagraph(XWPFParagraph para) {
        para.setSpacingBefore(0);
        para.setSpacingAfter(0);
        try {
            para.setSpacingBetween(1.0, LineSpacingRule.EXACT);
        } catch (Exception ignore) {
            // 旧 POI 无此 API 时忽略
        }
        CTPPr pPr = para.getCTP().isSetPPr() ? para.getCTP().getPPr() : para.getCTP().addNewPPr();
        CTSpacing spacing = pPr.isSetSpacing() ? pPr.getSpacing() : pPr.addNewSpacing();
        spacing.setBefore(BigInteger.ZERO);
        spacing.setAfter(BigInteger.ZERO);
    }

    /**
     * 移除已创建的段落
     *
     * @param document 文档
     * @param para     段落
     */
    private void removeParagraphQuietly(XWPFDocument document, XWPFParagraph para) {
        try {
            int pos = document.getPosOfParagraph(para);
            if (pos >= 0) {
                document.removeBodyElement(pos);
            }
        } catch (Exception e) {
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
