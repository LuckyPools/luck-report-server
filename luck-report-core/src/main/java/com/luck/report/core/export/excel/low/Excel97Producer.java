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
package com.luck.report.core.export.excel.low;

import com.luck.report.core.Utils;
import com.luck.report.core.build.paging.Page;
import com.luck.report.core.chart.ChartData;
import com.luck.report.core.definition.Orientation;
import com.luck.report.core.definition.Paper;
import com.luck.report.core.definition.PaperType;
import com.luck.report.core.exception.ReportComputeException;
import com.luck.report.core.model.Column;
import com.luck.report.core.model.Image;
import com.luck.report.core.model.Report;
import com.luck.report.core.model.Row;
import com.luck.report.core.utils.ImageUtils;
import com.luck.report.core.utils.UnitUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPrintSetup;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Jacky.gao
 * @since 2017年7月3日
 */
public class Excel97Producer {
    public void produceWithPaging(Report report, OutputStream outputStream) {
        doProduce(report, outputStream, true, false);
    }

    public void produce(Report report, OutputStream outputStream) {
        doProduce(report, outputStream, false, false);
    }

    public void produceWithSheet(Report report, OutputStream outputStream) {
        doProduce(report, outputStream, true, true);
    }

    private void doProduce(Report report, OutputStream outputStream, boolean withPaging, boolean withSheet) {
        CellStyleContext cellStyleContext = new CellStyleContext();
        HSSFWorkbook wb = new HSSFWorkbook();
        CreationHelper creationHelper = wb.getCreationHelper();
        Paper paper = report.getPaper();

        try {
            List<Column> columns = report.getColumns();
            Map<Row, Map<Column, com.luck.report.core.model.Cell>> cellMap = report.getRowColCellMap();
            int columnSize = columns.size();
            if (withPaging) {
                List<Page> pages = report.getPages();
                int rowNumber = 0, pageIndex = 1;
                Sheet sheet = null;
                for (Page page : pages) {
                    if (withSheet) {
                        sheet = createSheet(wb, paper, "第" + pageIndex + "页");
                        rowNumber = 0;
                    } else if (sheet == null) {
                        sheet = createSheet(wb, paper, null);
                    }
                    pageIndex++;
                    Drawing<?> drawing = sheet.createDrawingPatriarch();
                    List<Row> rows = page.getRows();
                    for (Row r : rows) {
                        if (r.isHiddenFormLayout()) {
                            continue;
                        }
                        org.apache.poi.ss.usermodel.Row row = sheet.getRow(rowNumber);
                        if (row == null) {
                            row = sheet.createRow(rowNumber);
                        }
                        Map<Column, com.luck.report.core.model.Cell> colCell = cellMap.get(r);
                        int skipCol = 0;
                        for (int i = 0; i < columnSize; i++) {
                            Column col = columns.get(i);
                            if (col.isHiddenFormLayout()) {
                                skipCol++;
                                continue;
                            }
                            int w = col.getWidth();
                            int colNum = i - skipCol;
                            sheet.setColumnWidth(colNum, UnitUtils.pixelToExcelColumnWidth(UnitUtils.pointToPixel(w)));
                            org.apache.poi.ss.usermodel.Cell cell = row.getCell(colNum);
                            if (cell != null) {
                                continue;
                            }
                            cell = row.createCell(colNum);
                            com.luck.report.core.model.Cell cellInfo = colCell == null ? null : colCell.get(col);
                            if (cellInfo == null) {
                                continue;
                            }
                            HSSFCellStyle style = cellStyleContext.produceXSSFCellStyle(wb, cellInfo);
                            int colSpan = cellInfo.getLayoutColSpan();
                            int rowSpan = cellInfo.getLayoutPageRowSpan();
                            int spanRows = rowSpan > 0 ? rowSpan : 1;
                            int spanCols = colSpan > 0 ? colSpan : 1;
                            for (int j = 0; j < spanRows; j++) {
                                org.apache.poi.ss.usermodel.Row rr = sheet.getRow(rowNumber + j);
                                if (rr == null) {
                                    rr = sheet.createRow(rowNumber + j);
                                }
                                for (int c = 0; c < spanCols; c++) {
                                    Cell cc = rr.getCell(colNum + c);
                                    if (cc == null) {
                                        cc = rr.createCell(colNum + c);
                                    }
                                    cc.setCellStyle(style);
                                }
                            }
                            int mergeColSpan = colSpan;
                            int mergeRowSpan = rowSpan;
                            if (mergeColSpan > 0 || mergeRowSpan > 0) {
                                if (mergeRowSpan > 0) {
                                    mergeRowSpan--;
                                }
                                if (mergeColSpan > 0) {
                                    mergeColSpan--;
                                }
                                CellRangeAddress cellRegion = new CellRangeAddress(rowNumber, (rowNumber + mergeRowSpan), colNum, (colNum + mergeColSpan));
                                sheet.addMergedRegion(cellRegion);
                            }
                            Object obj = cellInfo.isRenderFlag() ? cellInfo.getFormatData() : null;
                            if (obj != null) {
                                if (obj instanceof String) {
                                    cell.setCellValue((String) obj);
                                } else if (obj instanceof Number) {
                                    BigDecimal bigDecimal = Utils.toBigDecimal(obj);
                                    cell.setCellValue(bigDecimal.doubleValue());
                                } else if (obj instanceof Boolean) {
                                    cell.setCellValue((Boolean) obj);
                                } else if (obj instanceof Image) {
                                    Image img = (Image) obj;
                                    InputStream inputStream = ImageUtils.base64DataToInputStream(img.getBase64Data());
                                    BufferedImage bufferedImage = ImageIO.read(inputStream);
                                    int width = bufferedImage.getWidth();
                                    int height = bufferedImage.getHeight();
                                    IOUtils.closeQuietly(inputStream);
                                    inputStream = ImageUtils.base64DataToInputStream(img.getBase64Data());
                                    try {
                                        HSSFClientAnchor anchor = new HSSFClientAnchor();
                                        byte[] bytes = IOUtils.toByteArray(inputStream);
                                        int pictureFormat = buildImageFormat(img);
                                        int pictureIndex = wb.addPicture(bytes, pictureFormat);
                                        anchor.setCol1(colNum);
                                        anchor.setCol2(colNum + mergeColSpan);
                                        anchor.setRow1(rowNumber);
                                        anchor.setRow2(rowNumber + mergeRowSpan);
                                        anchor.setDx1(Units.pixelToEMU(0));
                                        anchor.setDx2(Units.pixelToEMU(width));
                                        anchor.setDy1(Units.pixelToEMU(0));
                                        anchor.setDy2(Units.pixelToEMU(height));
                                        drawing.createPicture(anchor, pictureIndex);
                                    } finally {
                                        IOUtils.closeQuietly(inputStream);
                                    }
                                } else if (obj instanceof ChartData) {
                                    ChartData chartData = (ChartData) obj;
                                    String base64Data = chartData.retriveBase64Data();
                                    if (base64Data != null) {
                                        Image img = new Image(base64Data, chartData.getWidth(), chartData.getHeight());
                                        InputStream inputStream = ImageUtils.base64DataToInputStream(img.getBase64Data());
                                        int chartWidth = 0;
                                        Column widthCol = col;
                                        int engineSpan = cellInfo.getColSpan() < 1 ? 1 : cellInfo.getColSpan();
                                        for (int k = 0; k < engineSpan && widthCol != null; k++) {
                                            if (!widthCol.isHiddenFormLayout()) {
                                                chartWidth += widthCol.getWidth();
                                            }
                                            widthCol = widthCol.getNext();
                                        }
                                        int width = UnitUtils.pointToPixel(chartWidth);
                                        int height = UnitUtils.pointToPixel(r.getRealHeight());
                                        try {
                                            HSSFClientAnchor anchor = new HSSFClientAnchor();
                                            byte[] bytes = IOUtils.toByteArray(inputStream);
                                            int pictureFormat = buildImageFormat(img);
                                            int pictureIndex = wb.addPicture(bytes, pictureFormat);
                                            anchor.setCol1(colNum);
                                            anchor.setCol2(colNum + mergeColSpan);
                                            anchor.setRow1(rowNumber);
                                            anchor.setRow2(rowNumber + mergeRowSpan);
                                            anchor.setDx1(Units.pixelToEMU(0));
                                            anchor.setDx2(Units.pixelToEMU(width));
                                            anchor.setDy1(Units.pixelToEMU(0));
                                            anchor.setDy2(Units.pixelToEMU(height));
                                            drawing.createPicture(anchor, pictureIndex);
                                        } finally {
                                            IOUtils.closeQuietly(inputStream);
                                        }
                                    }
                                } else if (obj instanceof Date) {
                                    cell.setCellValue((Date) obj);
                                }
                            }
                        }
                        row.setHeight((short) UnitUtils.pointToTwip(r.getHeight()));
                        rowNumber++;
                    }
                    sheet.setRowBreak(rowNumber - 1);
                }
            } else {
                Sheet sheet = createSheet(wb, paper, null);
                Drawing<?> drawing = sheet.createDrawingPatriarch();
                List<Row> rows = report.getRows();
                int rowNumber = 0;
                for (Row r : rows) {
                    if (r.isHiddenFormLayout()) {
                        continue;
                    }
                    if (r.isForPaging()) {
                        return;
                    }
                    org.apache.poi.ss.usermodel.Row row = sheet.getRow(rowNumber);
                    if (row == null) {
                        row = sheet.createRow(rowNumber);
                    }
                    Map<Column, com.luck.report.core.model.Cell> colCell = cellMap.get(r);
                    int skipCol = 0;
                    for (int i = 0; i < columnSize; i++) {
                        Column col = columns.get(i);
                        if (col.isHiddenFormLayout()) {
                            skipCol++;
                            continue;
                        }
                        int w = col.getWidth();
                        int colNum = i - skipCol;
                        sheet.setColumnWidth(colNum, UnitUtils.pixelToExcelColumnWidth(UnitUtils.pointToPixel(w)));
                        org.apache.poi.ss.usermodel.Cell cell = row.getCell(colNum);
                        if (cell != null) {
                            continue;
                        }
                        cell = row.createCell(colNum);
                        com.luck.report.core.model.Cell cellInfo = colCell == null ? null : colCell.get(col);
                        if (cellInfo == null) {
                            continue;
                        }
                        if (cellInfo.isForPaging()) {
                            continue;
                        }
                        HSSFCellStyle style = cellStyleContext.produceXSSFCellStyle(wb, cellInfo);
                        int colSpan = cellInfo.getLayoutColSpan();
                        int rowSpan = cellInfo.getLayoutRowSpan();
                        int spanRows = rowSpan > 0 ? rowSpan : 1;
                        int spanCols = colSpan > 0 ? colSpan : 1;
                        for (int j = 0; j < spanRows; j++) {
                            org.apache.poi.ss.usermodel.Row rr = sheet.getRow(rowNumber + j);
                            if (rr == null) {
                                rr = sheet.createRow(rowNumber + j);
                            }
                            for (int c = 0; c < spanCols; c++) {
                                Cell cc = rr.getCell(colNum + c);
                                if (cc == null) {
                                    cc = rr.createCell(colNum + c);
                                }
                                cc.setCellStyle(style);
                            }
                        }
                        int mergeColSpan = colSpan;
                        int mergeRowSpan = rowSpan;
                        if (mergeColSpan > 0 || mergeRowSpan > 0) {
                            if (mergeRowSpan > 0) {
                                mergeRowSpan--;
                            }
                            if (mergeColSpan > 0) {
                                mergeColSpan--;
                            }
                            CellRangeAddress cellRegion = new CellRangeAddress(rowNumber, (rowNumber + mergeRowSpan), colNum, (colNum + mergeColSpan));
                            sheet.addMergedRegion(cellRegion);
                        }
                        Object obj = cellInfo.isRenderFlag() ? cellInfo.getFormatData() : null;
                        if (obj != null) {
                            if (obj instanceof String) {
                                cell.setCellValue((String) obj);
                            } else if (obj instanceof Number) {
                                BigDecimal bigDecimal = Utils.toBigDecimal(obj);
                                cell.setCellValue(bigDecimal.doubleValue());
                            } else if (obj instanceof Boolean) {
                                cell.setCellValue((Boolean) obj);
                            } else if (obj instanceof Image) {
                                Image img = (Image) obj;
                                InputStream inputStream = ImageUtils.base64DataToInputStream(img.getBase64Data());
                                BufferedImage bufferedImage = ImageIO.read(inputStream);
                                int width = bufferedImage.getWidth();
                                int height = bufferedImage.getHeight();
                                IOUtils.closeQuietly(inputStream);
                                inputStream = ImageUtils.base64DataToInputStream(img.getBase64Data());
                                try {
                                    HSSFClientAnchor anchor = new HSSFClientAnchor();
                                    byte[] bytes = IOUtils.toByteArray(inputStream);
                                    int pictureFormat = buildImageFormat(img);
                                    int pictureIndex = wb.addPicture(bytes, pictureFormat);
                                    anchor.setCol1(colNum);
                                    anchor.setCol2(colNum + mergeColSpan);
                                    anchor.setRow1(rowNumber);
                                    anchor.setRow2(rowNumber + mergeRowSpan);
                                    anchor.setDx1(Units.pixelToEMU(0));
                                    anchor.setDx2(Units.pixelToEMU(width));
                                    anchor.setDy1(Units.pixelToEMU(0));
                                    anchor.setDy2(Units.pixelToEMU(height));
                                    drawing.createPicture(anchor, pictureIndex);
                                } finally {
                                    IOUtils.closeQuietly(inputStream);
                                }
                            } else if (obj instanceof ChartData) {
                                ChartData chartData = (ChartData) obj;
                                String base64Data = chartData.retriveBase64Data();
                                if (base64Data != null) {
                                    Image img = new Image(base64Data, chartData.getWidth(), chartData.getHeight());
                                    InputStream inputStream = ImageUtils.base64DataToInputStream(img.getBase64Data());
                                    int chartWidth = 0;
                                    Column widthCol = col;
                                    int engineSpan = cellInfo.getColSpan() < 1 ? 1 : cellInfo.getColSpan();
                                    for (int k = 0; k < engineSpan && widthCol != null; k++) {
                                        if (!widthCol.isHiddenFormLayout()) {
                                            chartWidth += widthCol.getWidth();
                                        }
                                        widthCol = widthCol.getNext();
                                    }
                                    int width = UnitUtils.pointToPixel(chartWidth);
                                    int height = UnitUtils.pointToPixel(r.getRealHeight());
                                    try {
                                        HSSFClientAnchor anchor = new HSSFClientAnchor();
                                        byte[] bytes = IOUtils.toByteArray(inputStream);
                                        int pictureFormat = buildImageFormat(img);
                                        int pictureIndex = wb.addPicture(bytes, pictureFormat);
                                        anchor.setCol1(colNum);
                                        anchor.setCol2(colNum + mergeColSpan);
                                        anchor.setRow1(rowNumber);
                                        anchor.setRow2(rowNumber + mergeRowSpan);
                                        anchor.setDx1(Units.pixelToEMU(0));
                                        anchor.setDx2(Units.pixelToEMU(width));
                                        anchor.setDy1(Units.pixelToEMU(0));
                                        anchor.setDy2(Units.pixelToEMU(height));
                                        drawing.createPicture(anchor, pictureIndex);
                                    } finally {
                                        IOUtils.closeQuietly(inputStream);
                                    }
                                }
                            } else if (obj instanceof Date) {
                                cell.setCellValue((Date) obj);
                            }
                        }
                    }
                    row.setHeight((short) UnitUtils.pointToTwip(r.getRealHeight()));
                    rowNumber++;
                }
                sheet.setRowBreak(rowNumber - 1);
            }
            wb.write(outputStream);
        } catch (Exception ex) {
            throw new ReportComputeException(ex);
        } finally {
            try {
                wb.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Sheet createSheet(HSSFWorkbook wb, Paper paper, String name) {
        Sheet sheet = null;
        if (name == null) {
            sheet = wb.createSheet();
        } else {
            sheet = wb.createSheet(name);
        }
        PaperType paperType = paper.getPaperType();
        HSSFPrintSetup printSetup = (HSSFPrintSetup) sheet.getPrintSetup();
        Orientation orientation = paper.getOrientation();
        if (orientation.equals(Orientation.landscape)) {
            printSetup.setLandscape(true);
        }
        setupPaper(paperType, printSetup);
        int leftMargin = paper.getLeftMargin();
        int rightMargin = paper.getRightMargin();
        int topMargin = paper.getTopMargin();
        int bottomMargin = paper.getBottomMargin();
        sheet.setMargin(PageMargin.LEFT, UnitUtils.pointToInche(leftMargin));
        sheet.setMargin(PageMargin.RIGHT, UnitUtils.pointToInche(rightMargin));
        sheet.setMargin(PageMargin.TOP, UnitUtils.pointToInche(topMargin));
        sheet.setMargin(PageMargin.BOTTOM, UnitUtils.pointToInche(bottomMargin));
        return sheet;
    }

    private int buildImageFormat(Image img) {
        int type = Workbook.PICTURE_TYPE_PNG;
        String path = img.getPath();
        if (path == null) {
            return type;
        }
        path = path.toLowerCase();
        if (path.endsWith("jpg") || path.endsWith("jpeg")) {
            type = Workbook.PICTURE_TYPE_JPEG;
        }
        return type;
    }

    private boolean setupPaper(PaperType paperType, HSSFPrintSetup printSetup) {
        boolean setup = false;
        switch (paperType) {
            case A0:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
            case A1:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
            case A2:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
            case A3:
                printSetup.setPaperSize(HSSFPrintSetup.A3_PAPERSIZE);
                setup = true;
                break;
            case A4:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                setup = true;
                break;
            case A5:
                printSetup.setPaperSize(HSSFPrintSetup.A5_PAPERSIZE);
                setup = true;
                break;
            case A6:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
            case A7:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
            case A8:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
            case A9:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
            case A10:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
            case B0:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
            case B1:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
            case B2:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
            case B3:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
            case B4:
                printSetup.setPaperSize(HSSFPrintSetup.B4_PAPERSIZE);
                setup = true;
                break;
            case B5:
                printSetup.setPaperSize(HSSFPrintSetup.B5_PAPERSIZE);
                setup = true;
                break;
            case B6:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
            case B7:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
            case B8:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
            case B9:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
            case B10:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
            case CUSTOM:
                printSetup.setPaperSize(HSSFPrintSetup.A4_EXTRA_PAPERSIZE);
                break;
        }
        return setup;
    }
}
