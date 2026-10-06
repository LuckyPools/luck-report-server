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
package com.luck.report.core.export.excel.high.builder;

import com.luck.report.core.Utils;
import com.luck.report.core.chart.ChartData;
import com.luck.report.core.definition.Paper;
import com.luck.report.core.exception.ReportComputeException;
import com.luck.report.core.export.excel.high.CellStyleContext;
import com.luck.report.core.model.Column;
import com.luck.report.core.model.Image;
import com.luck.report.core.model.Report;
import com.luck.report.core.model.Row;
import com.luck.report.core.utils.ImageUtils;
import com.luck.report.core.utils.UnitUtils;
import com.luck.report.core.utils.FreezeUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.Units;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Jacky.gao
 * @since 2017年8月10日
 */
public class ExcelBuilderDirect extends ExcelBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ExcelBuilderDirect.class);

    public void build(Report report, OutputStream outputStream) {
        CellStyleContext cellStyleContext = new CellStyleContext();
        SXSSFWorkbook wb = new SXSSFWorkbook(100000);
        CreationHelper creationHelper = wb.getCreationHelper();
        Paper paper = report.getPaper();
        try {
            List<Column> columns = report.getColumns();
            Map<Row, Map<Column, com.luck.report.core.model.Cell>> cellMap = report.getRowColCellMap();
            int columnSize = columns.size();
            Sheet sheet = createSheet(wb, paper, null);
            Drawing<?> drawing = sheet.createDrawingPatriarch();
            List<Row> rows = report.getRows();
            int freezeRowCount = FreezeUtils.computeFreezeRowCount(report, paper.getFreezeRowCellName());
            int freezeColCount = FreezeUtils.computeFreezeColCount(report, paper.getFreezeColCellName());
            int freezeRowSplit = FreezeUtils.computeVisibleRowCount(rows, freezeRowCount);
            int freezeColSplit = FreezeUtils.computeVisibleColCount(columns, freezeColCount);
            boolean hasFloat = hasFloatElements(report);
            int rowNumber = 0;
            for (Row r : rows) {
                if (r.isHiddenFormLayout()) {
                    continue;
                }
                if (r.isForPaging()) {
                    break;
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
                    com.luck.report.core.model.Cell cellInfo = null;
                    if (colCell != null) {
                        cellInfo = colCell.get(col);
                    }
                    if (cellInfo == null) {
                        continue;
                    }
                    if (cellInfo.isForPaging()) {
                        continue;
                    }
                    XSSFCellStyle style = cellStyleContext.produceXSSFCellStyle(wb, cellInfo);
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
                            int wholeWidth = getWholeWidth(columns, i, cellInfo.getColSpan());
                            int wholeHeight = getWholeHeight(r, cellInfo.getRowSpan());
                            try {
                                addCellImage(sheet, drawing, wb, img, colNum, rowNumber,
                                        colSpan, rowSpan,
                                        wholeWidth, wholeHeight,
                                        style.getAlignment(), style.getVerticalAlignment(),
                                        cellInfo.getName());
                            } catch (Exception e) {
                                throw new ReportComputeException(e);
                            }
                        } else if (obj instanceof ChartData) {
                            ChartData chartData = (ChartData) obj;
                            String base64Data = chartData.retriveBase64Data();
                            if (base64Data != null) {
                                Image img = new Image(base64Data, chartData.getWidth(), chartData.getHeight());
                                InputStream inputStream = ImageUtils.base64DataToInputStream(img.getBase64Data());
                                int width = getWholeWidth(columns, i, cellInfo.getColSpan());
                                int height = getWholeHeight(r, cellInfo.getRowSpan());
                                try {
                                    XSSFClientAnchor anchor = (XSSFClientAnchor) creationHelper.createClientAnchor();
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
                                    logger.info("[excel-chart] 画图成功, chartId={}, row={}, col={}, base64长度={}", chartData.getId(), rowNumber, i, base64Data.length());
                                } finally {
                                    IOUtils.closeQuietly(inputStream);
                                }
                            } else {
                                logger.warn("[excel-chart] 跳过画图, chartId={}, row={}, col={}, base64为空", chartData.getId(), rowNumber, i);
                            }
                        } else if (obj instanceof Date) {
                            cell.setCellValue((Date) obj);
                        }
                    }
                }
                row.setHeight((short) UnitUtils.pointToTwip(r.getRealHeight()));
                rowNumber++;
            }
            if (hasFloat) {
                List<Row> visibleRows = new ArrayList<>();
                for (Row r : rows) {
                    if (r.isHiddenFormLayout()) {
                        continue;
                    }
                    if (r.isForPaging()) {
                        break;
                    }
                    visibleRows.add(r);
                }
                float totalHeight = 0;
                for (Row r : visibleRows) {
                    totalHeight += r.getRealHeight();
                }
                renderFloatElementsForPage(sheet, drawing, report, 0, totalHeight, visibleRows, 0, columns, creationHelper, wb);
            }
            if (freezeRowSplit > 0 || freezeColSplit > 0) {
                sheet.createFreezePane(freezeColSplit, freezeRowSplit);
            }
            sheet.setRowBreak(rowNumber - 1);
            wb.write(outputStream);
        } catch (Exception ex) {
            throw new ReportComputeException(ex);
        } finally {
            wb.dispose();
        }
    }
}
