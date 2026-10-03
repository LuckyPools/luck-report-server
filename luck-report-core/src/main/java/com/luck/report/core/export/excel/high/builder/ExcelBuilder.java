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

import com.luck.report.core.definition.Border;
import com.luck.report.core.definition.FloatElement;
import com.luck.report.core.definition.FloatImage;
import com.luck.report.core.definition.FloatText;
import com.luck.report.core.definition.Orientation;
import com.luck.report.core.definition.Paper;
import com.luck.report.core.definition.PaperType;
import com.luck.report.core.definition.value.Source;
import com.luck.report.core.image.ImageType;
import com.luck.report.core.model.Column;
import com.luck.report.core.model.Image;
import com.luck.report.core.model.Report;
import com.luck.report.core.model.Row;
import com.luck.report.core.utils.ImageUtils;
import com.luck.report.core.utils.UnitUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.PageMargin;
import org.apache.poi.ss.usermodel.PaperSize;
import org.apache.poi.ss.usermodel.PrintOrientation;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.util.Units;
import org.apache.poi.xddf.usermodel.text.XDDFTextBody;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFPrintSetup;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFTextBox;
import org.openxmlformats.schemas.drawingml.x2006.main.CTRegularTextRun;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextFont;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraph;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextAlignType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextUnderlineType;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/**
 * @author Jacky.gao
 * @since 2017年8月10日
 */
public abstract class ExcelBuilder {

    protected int getWholeWidth(List<Column> columns, int colNumber, int colSpan) {
        int count = colSpan < 1 ? 1 : colSpan;
        int end = Math.min(colNumber + count, columns.size());
        int w = 0;
        for (int i = colNumber; i < end; i++) {
            Column c = columns.get(i);
            if (c.isHiddenFormLayout()) {
                continue;
            }
            w += c.getWidth();
        }
        w = UnitUtils.pointToPixel(w);
        return w;
    }

    protected int getWholeHeight(Row startRow, int rowSpan) {
        int count = rowSpan < 1 ? 1 : rowSpan;
        int h = 0;
        Row row = startRow;
        for (int i = 0; i < count && row != null; i++) {
            if (!row.isHiddenFormLayout()) {
                h += row.getRealHeight();
            }
            row = row.getNext();
        }
        return UnitUtils.pointToPixel(h);
    }

    /**
     * 向单元格写入图片：按声明或位图尺寸绘制，超出合并区时等比缩小，并按对齐方式留边
     *
     * @param sheet Excel sheet
     * @param drawing 绘图对象
     * @param wb 工作簿
     * @param img 图片
     * @param excelCol 可见列下标（起点）
     * @param excelRow 可见行下标（起点）
     * @param layoutColSpan 布局列合并数（0/1 表示单列）
     * @param layoutRowSpan 布局行合并数（0/1 表示单行）
     * @param wholeWidthPx 合并区宽（px）
     * @param wholeHeightPx 合并区高（px）
     * @param align 水平对齐
     * @param valign 垂直对齐
     * @param cellName 单元格名（解码失败时告警）
     */
    protected void addCellImage(Sheet sheet, Drawing<?> drawing, SXSSFWorkbook wb,
                                Image img, int excelCol, int excelRow,
                                int layoutColSpan, int layoutRowSpan,
                                int wholeWidthPx, int wholeHeightPx,
                                HorizontalAlignment align, VerticalAlignment valign,
                                String cellName) throws Exception {
        InputStream inputStream = ImageUtils.base64DataToInputStream(img.getBase64Data());
        BufferedImage bufferedImage;
        try {
            bufferedImage = ImageIO.read(inputStream);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        if (bufferedImage == null) {
            log.warning("[excel-cell-image] 无法解码图片 cell=" + cellName);
            return;
        }
        int naturalW = Math.max(1, bufferedImage.getWidth());
        int naturalH = Math.max(1, bufferedImage.getHeight());
        // 优先声明尺寸，仅当超出合并区时缩小
        int drawW = img.getWidth() > 0 ? img.getWidth() : naturalW;
        int drawH = img.getHeight() > 0 ? img.getHeight() : naturalH;
        if (wholeWidthPx > 0 && wholeHeightPx > 0) {
            double scale = Math.min(1.0, Math.min((double) wholeWidthPx / (double) drawW,
                    (double) wholeHeightPx / (double) drawH));
            drawW = Math.max(1, (int) Math.round(drawW * scale));
            drawH = Math.max(1, (int) Math.round(drawH * scale));
        }
        int leftMargin = 0;
        int topMargin = 0;
        if (align != null && align.equals(HorizontalAlignment.CENTER)) {
            leftMargin = Math.max(0, (wholeWidthPx - drawW) / 2);
        } else if (align != null && align.equals(HorizontalAlignment.RIGHT)) {
            leftMargin = Math.max(0, wholeWidthPx - drawW);
        }
        if (valign != null && valign.equals(VerticalAlignment.CENTER)) {
            topMargin = Math.max(0, (wholeHeightPx - drawH) / 2);
        } else if (valign != null && valign.equals(VerticalAlignment.BOTTOM)) {
            topMargin = Math.max(0, wholeHeightPx - drawH);
        }
        int rightMargin = Math.max(0, wholeWidthPx - leftMargin - drawW);
        int bottomMargin = Math.max(0, wholeHeightPx - topMargin - drawH);
        int spanCols = layoutColSpan > 0 ? layoutColSpan : 1;
        int spanRows = layoutRowSpan > 0 ? layoutRowSpan : 1;
        int col2 = excelCol + spanCols;
        int row2 = excelRow + spanRows;

        inputStream = ImageUtils.base64DataToInputStream(img.getBase64Data());
        try {
            byte[] bytes = IOUtils.toByteArray(inputStream);
            int pictureFormat = buildImageFormat(img);
            int pictureIndex = wb.addPicture(bytes, pictureFormat);
            XSSFClientAnchor anchor = new XSSFClientAnchor();
            anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);
            anchor.setCol1(excelCol);
            anchor.setRow1(excelRow);
            anchor.setCol2(col2);
            anchor.setRow2(row2);
            anchor.setDx1(Units.pixelToEMU(leftMargin));
            anchor.setDx2(-Units.pixelToEMU(rightMargin));
            anchor.setDy1(Units.pixelToEMU(topMargin));
            anchor.setDy2(-Units.pixelToEMU(bottomMargin));
            drawing.createPicture(anchor, pictureIndex);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    protected Sheet createSheet(SXSSFWorkbook wb, Paper paper, String name) {
        Sheet sheet = null;
        if (name == null) {
            sheet = wb.createSheet();
        } else {
            sheet = wb.createSheet(name);
        }
        PaperType paperType = paper.getPaperType();
        XSSFPrintSetup printSetup = (XSSFPrintSetup) sheet.getPrintSetup();
        Orientation orientation = paper.getOrientation();
        if (orientation.equals(Orientation.landscape)) {
            printSetup.setOrientation(PrintOrientation.LANDSCAPE);
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

    protected int buildImageFormat(Image img) {
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

    protected boolean setupPaper(PaperType paperType, XSSFPrintSetup printSetup) {
        boolean setup = false;
        switch (paperType) {
            case A0:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
            case A1:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
            case A2:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
            case A3:
                printSetup.setPaperSize(PaperSize.A3_PAPER);
                setup = true;
                break;
            case A4:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                setup = true;
                break;
            case A5:
                printSetup.setPaperSize(PaperSize.A5_PAPER);
                setup = true;
                break;
            case A6:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
            case A7:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
            case A8:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
            case A9:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
            case A10:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
            case B0:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
            case B1:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
            case B2:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
            case B3:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
            case B4:
                printSetup.setPaperSize(PaperSize.B4_PAPER);
                setup = true;
                break;
            case B5:
                printSetup.setPaperSize(PaperSize.B4_PAPER);
                setup = true;
                break;
            case B6:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
            case B7:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
            case B8:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
            case B9:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
            case B10:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
            case CUSTOM:
                printSetup.setPaperSize(PaperSize.A4_PAPER);
                break;
        }
        return setup;
    }

    /**
     * 判断当前报表是否包含悬浮元素（图片或文本）。
     */
    protected boolean hasFloatElements(Report report) {
        List<FloatImage> floatImages = report.getFloatImages();
        List<FloatText> floatTexts = report.getFloatTexts();
        boolean hasImages = floatImages != null && !floatImages.isEmpty();
        boolean hasTexts = floatTexts != null && !floatTexts.isEmpty();
        return hasImages || hasTexts;
    }

    /**
     * 将悬浮图片解析为输入流，支持 base64 与 URL（text）两种来源。对 URL 图片使用 {@link ImageUtils#getImageBase64Data}，有 image-not-exist 兜底机制。对于 WebP 等 Excel 不支持的格式，通过 ImageIO 读取后重新编码为 PNG。
     */
    protected InputStream buildFloatImageInputStream(FloatImage fi) {
        Source source = fi.getSource();
        String value = fi.buildValue();
        if (StringUtils.isBlank(value)) {
            return null;
        }
        if (source != null && source.equals(Source.base64)) {
            try {
                InputStream rawInput = ImageUtils.base64DataToInputStream(value);
                return convertUnsupportedFormatIfNeeded(rawInput, fi);
            } catch (Exception e) {
                String fiName = fi.getName() != null ? fi.getName() : "unknown";
                log.log(Level.WARNING, "导出 Excel 悬浮图片 [" + fiName + "] base64 解码失败", e);
                return null;
            }
        }
        try {
            String base64Data = ImageUtils.getImageBase64Data(ImageType.image, value, 0, 0);
            if (StringUtils.isNotBlank(base64Data)) {
                InputStream rawInput = ImageUtils.base64DataToInputStream(base64Data);
                return convertUnsupportedFormatIfNeeded(rawInput, fi);
            }
        } catch (Exception e) {
            String fiName = fi.getName() != null ? fi.getName() : "unknown";
            log.log(Level.WARNING, "导出 Excel 悬浮图片 [" + fiName + "] URL 加载失败: " + value, e);
        }
        return null;
    }

    /**
     * 判断图片格式是否被 Excel 直接支持（PNG/JPEG）。若不被支持（如 WebP/BMP/GIF），通过 ImageIO 读取后重新编码为 PNG。转换成功后需同步修改 FloatImage 的格式标记，由调用方处理。
     *
     * @param rawInput 原始图片输入流
     * @param fi       悬浮图片对象（用于判断格式）
     * @return 可被 Excel 直接使用的输入流（原样或转换后）
     */
    private InputStream convertUnsupportedFormatIfNeeded(InputStream rawInput, FloatImage fi) {
        if (isExcelSupportedFormat(fi)) {
            return rawInput;
        }
        String fiName = fi.getName() != null ? fi.getName() : "unknown";
        try {
            BufferedImage image = ImageIO.read(rawInput);
            IOUtils.closeQuietly(rawInput);
            if (image == null) {
                log.warning("导出 Excel 悬浮图片 [" + fiName + "] 失败：ImageIO 无法读取该格式，请确认 JVM 已安装对应 ImageIO 插件（如 WebP）");
                return null;
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            boolean written = ImageIO.write(image, "png", baos);
            if (!written) {
                log.warning("导出 Excel 悬浮图片 [" + fiName + "] 失败：ImageIO 无法编码为 PNG");
                return null;
            }
            log.info("导出 Excel 悬浮图片 [" + fiName + "]：已从不支持的格式转换为 PNG");
            return new ByteArrayInputStream(baos.toByteArray());
        } catch (Exception e) {
            IOUtils.closeQuietly(rawInput);
            log.log(Level.WARNING, "导出 Excel 悬浮图片 [" + fiName + "] 格式转换失败", e);
            return null;
        }
    }

    /**
     * 判断 FloatImage 的格式是否被 Excel 直接支持（确认是 PNG 或 JPEG）。注意：buildFloatImageFormat 对无法识别的格式默认返回 PICTURE_TYPE_PNG，但 WebP 等格式并非真正的 PNG，需要通过后缀或数据头签名明确确认才返回 true。
     */
    private boolean isExcelSupportedFormat(FloatImage fi) {
        String path = fi.getPath();
        if (path != null) {
            String lower = path.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png");
        }
        String expr = fi.getExpr();
        if (expr != null && expr.length() >= 4) {
            return expr.startsWith("/9j/") || expr.startsWith("iVBOR");
        }
        return false;
    }

    /**
     * 使用 oneCellAnchor（单锚点 + 精确 ext 尺寸）创建悬浮图片。先用 drawing.createPicture() 创建 twoCellAnchor（让 POI 自动处理图片关系），再将 OOXML 中的 twoCellAnchor 替换为 oneCellAnchor，确保宽高精确。
     */
    private void createOneCellAnchorPicture(Sheet sheet, Drawing<?> drawing, SXSSFWorkbook wb,
                                            int col, int dx, int row, int dy,
                                            long cx, long cy,
                                            int pictureIndex, FloatImage fi) throws Exception {
        XSSFClientAnchor tempAnchor = new XSSFClientAnchor(0, 0, (int) cx, (int) cy, (short) col, row, (short) col, row);
        tempAnchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
        drawing.createPicture(tempAnchor, pictureIndex);

        XSSFSheet xssfSheet = wb.getXSSFWorkbook().getSheet(sheet.getSheetName());
        XSSFDrawing xssfDrawing = xssfSheet.getDrawingPatriarch();
        if (xssfDrawing == null) {
            return;
        }
        org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTDrawing ctDrawing = xssfDrawing.getCTDrawing();
        int twoCellCount = ctDrawing.sizeOfTwoCellAnchorArray();
        if (twoCellCount == 0) {
            return;
        }

        org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor lastTwoCell = ctDrawing.getTwoCellAnchorArray(twoCellCount - 1);
        org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTPicture ctPicture = lastTwoCell.getPic();

        org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTOneCellAnchor oneCell = ctDrawing.addNewOneCellAnchor();
        org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker from = oneCell.addNewFrom();
        from.setCol(col);
        from.setColOff(dx);
        from.setRow(row);
        from.setRowOff(dy);

        org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D ext = oneCell.addNewExt();
        ext.setCx(cx);
        ext.setCy(cy);

        oneCell.setPic(ctPicture);
        oneCell.addNewClientData();

        ctDrawing.removeTwoCellAnchor(twoCellCount - 1);
    }

    /**
     * 将 XSSFDrawing 中最后一个 twoCellAnchor（文本框）替换为 oneCellAnchor，确保文本框尺寸由 ext 精确控制，不受行列宽高限制。返回 oneCellAnchor 中的 CTShape 引用（setSp 会深拷贝，需从 oneCellAnchor 重新获取）。
     */
    private org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShape convertTextboxToOneCellAnchor(XSSFDrawing xssfDrawing,
                                                int col, int dx, int row, int dy,
                                                long cx, long cy) {
        org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTDrawing ctDrawing = xssfDrawing.getCTDrawing();
        int twoCellCount = ctDrawing.sizeOfTwoCellAnchorArray();
        if (twoCellCount == 0) {
            return null;
        }

        org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTTwoCellAnchor lastTwoCell =
                ctDrawing.getTwoCellAnchorArray(twoCellCount - 1);
        org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShape srcShape = lastTwoCell.getSp();

        org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTOneCellAnchor oneCell = ctDrawing.addNewOneCellAnchor();
        org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTMarker from = oneCell.addNewFrom();
        from.setCol(col);
        from.setColOff(dx);
        from.setRow(row);
        from.setRowOff(dy);

        org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D ext = oneCell.addNewExt();
        ext.setCx(cx);
        ext.setCy(cy);

        oneCell.setSp(srcShape);
        oneCell.addNewClientData();

        ctDrawing.removeTwoCellAnchor(twoCellCount - 1);

        return oneCell.getSp();
    }

    protected static final Logger log = Logger.getLogger(ExcelBuilder.class.getName());

    /**
     * 合并 FloatImage 和 FloatText，按 layer 升序排列（值越大越靠上，后绘制覆盖先绘制）。
     */
    protected List<FloatElement> collectAndSortFloatElements(Report report) {
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
     * 推断悬浮图片的格式（PNG/JPEG）。优先从 URL 后缀判断；path 为空（base64 来源）时从 expr 数据头签名判断；均无法判断时从 buildValue() 返回的 URL 再尝试一次。
     */
    protected int buildFloatImageFormat(FloatImage fi) {
        String path = fi.getPath();
        if (path != null) {
            int format = detectFormatFromSuffix(path);
            if (format >= 0) {
                return format;
            }
        }
        String expr = fi.getExpr();
        if (expr != null && expr.length() >= 4) {
            if (expr.startsWith("/9j/")) {
                return Workbook.PICTURE_TYPE_JPEG;
            }
            if (expr.startsWith("iVBOR")) {
                return Workbook.PICTURE_TYPE_PNG;
            }
        }
        String value = fi.buildValue();
        if (value != null) {
            int format = detectFormatFromSuffix(value);
            if (format >= 0) {
                return format;
            }
        }
        return Workbook.PICTURE_TYPE_PNG;
    }

    /**
     * 从文件路径/URL 后缀推断图片格式。
     * @return PictureType 常量，无法识别时返回 -1
     */
    private int detectFormatFromSuffix(String path) {
        String lower = path.toLowerCase();
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return Workbook.PICTURE_TYPE_JPEG;
        }
        if (lower.endsWith(".png")) {
            return Workbook.PICTURE_TYPE_PNG;
        }
        return -1;
    }

    /**
     * 给定距数据区顶部的像素偏移，在行列表中定位所属行索引及行内 EMU 偏移。行高存的是 pt，需先转成 px 再和悬浮 top（px）比较。
     */
    protected int[] findRowPosition(List<Row> rows, int pixelOffset) {
        int cumulative = 0;
        for (int i = 0; i < rows.size(); i++) {
            Row r = rows.get(i);
            int rowHeightPx = UnitUtils.pointToPixel(r.getRealHeight());
            if (cumulative + rowHeightPx > pixelOffset) {
                int remainingPx = pixelOffset - cumulative;
                return new int[]{i, Units.pixelToEMU(remainingPx)};
            }
            cumulative += rowHeightPx;
        }
        int remainingPx = pixelOffset - cumulative;
        return new int[]{Math.max(rows.size() - 1, 0), Units.pixelToEMU(remainingPx)};
    }

    /**
     * 给定距数据区左侧的像素偏移，在列列表中定位所属 Excel 列索引及列内 EMU 偏移。列宽存的是 pt，需先转成 px 再和悬浮 left（px）比较。
     */
    protected int[] findColPosition(List<Column> columns, int pixelOffset) {
        int cumulative = 0;
        int excelCol = 0;
        for (int i = 0; i < columns.size(); i++) {
            Column col = columns.get(i);
            if (col.isHiddenFormLayout()) {
                continue;
            }
            int colWidthPx = UnitUtils.pointToPixel(col.getWidth());
            if (colWidthPx < 1) {
                continue;
            }
            if (cumulative + colWidthPx > pixelOffset) {
                int remainingPx = pixelOffset - cumulative;
                return new int[]{excelCol, Units.pixelToEMU(remainingPx)};
            }
            cumulative += colWidthPx;
            excelCol++;
        }
        int remainingPx = pixelOffset - cumulative;
        return new int[]{Math.max(excelCol - 1, 0), Units.pixelToEMU(remainingPx)};
    }

    /**
     * 在当前 Excel sheet 上绘制属于该页的悬浮元素。top/left 为页面相对坐标（相对于每页内容区左上角）。repeatPrint=true 时每页重复打印；repeatPrint=false 时仅在第1页渲染。
     *
     * @param sheet             SXSSF Sheet（用于访问底层 XSSFDrawing 创建文本框）
     * @param drawing           Drawing 对象（用于 createPicture）
     * @param report            报表对象
     * @param pageIndex         当前页码（从 0 开始）
     * @param currentPageHeight 当前页内容高度（磅，用于边界检查）
     * @param rows              当前页的行列表（用于行定位）
     * @param rowOffset         当前页在 sheet 中的起始行号偏移
     * @param columns           列列表
     * @param creationHelper    CreationHelper
     * @param wb                SXSSFWorkbook
     */
    protected void renderFloatElementsForPage(Sheet sheet, Drawing<?> drawing, Report report,
                                              int pageIndex, float currentPageHeight,
                                              List<Row> rows, int rowOffset, List<Column> columns,
                                              CreationHelper creationHelper, SXSSFWorkbook wb) throws Exception {
        renderFloatElementsForPage(sheet, drawing, report, pageIndex, currentPageHeight,
                rows, rowOffset, columns, creationHelper, wb, null, true, false);
    }

    /**
     * 在当前 Excel sheet 上绘制属于该页的悬浮元素。
     *
     * @param paged true=分页模式（paging/sheet），按全局 top 计算归属页；false=不分页（build），所有元素直接用全局 top 定位
     */
    protected void renderFloatElementsForPage(Sheet sheet, Drawing<?> drawing, Report report,
                                              int pageIndex, float currentPageHeight,
                                              List<Row> rows, int rowOffset, List<Column> columns,
                                              CreationHelper creationHelper, SXSSFWorkbook wb,
                                              Map<String, Integer> pictureIndexCache,
                                              boolean withSheet, boolean paged) throws Exception {
        List<FloatElement> elements = collectAndSortFloatElements(report);
        XSSFDrawing xssfDrawing = null;
        Paper paper = report.getPaper();
        float paperExtent = paper.getOrientation() != null && paper.getOrientation().equals(Orientation.landscape)
                ? paper.getWidth() : paper.getHeight();
        float pageContentHeight = paperExtent - paper.getTopMargin() - paper.getBottomMargin();
        for (FloatElement el : elements) {
            String elName = el.getName() != null ? el.getName() : (el instanceof FloatImage ? "image" : "text");
            Integer top = el.getTop();
            if (top == null) {
                continue;
            }
            float elementTopPt = UnitUtils.pixelToPoint(top.intValue());
            int topPx;
            if (!paged) {
                if (el.isRepeatPrint() && (elementTopPt < 0 || elementTopPt >= pageContentHeight)) {
                    continue;
                }
                topPx = top.intValue();
            } else if (el.isRepeatPrint()) {
                if (elementTopPt < 0 || elementTopPt >= pageContentHeight) {
                    continue;
                }
                topPx = top.intValue();
            } else {
                int targetPage = (int) Math.floor(elementTopPt / pageContentHeight);
                if (pageIndex != targetPage) {
                    continue;
                }
                float pageRelTopPt = elementTopPt - targetPage * pageContentHeight;
                topPx = UnitUtils.pointToPixel(pageRelTopPt);
            }
            Integer left = el.getLeft();
            int leftPx = left != null ? left.intValue() : 0;

            Integer elWidth = el.getWidth();
            Integer elHeight = el.getHeight();
            int widthPx = elWidth != null ? elWidth.intValue() : 100;
            int heightPx = elHeight != null ? elHeight.intValue() : 75;

            int[] fromColPos = findColPosition(columns, leftPx);
            int[] fromRowPos = findRowPosition(rows, topPx);

            int fromCol = fromColPos[0];
            int fromDx = fromColPos[1];
            int fromRow = fromRowPos[0] + rowOffset;
            int fromDy = fromRowPos[1];
            int widthEMU = widthPx * 9525;
            int heightEMU = heightPx * 9525;

            try {
                if (el instanceof FloatImage) {
                    FloatImage fi = (FloatImage) el;
                    int pictureIndex;
                    String cacheKey = fi.getName() != null ? fi.getName() : String.valueOf(System.identityHashCode(fi));
                    if (pictureIndexCache != null && pictureIndexCache.containsKey(cacheKey)) {
                        pictureIndex = pictureIndexCache.get(cacheKey);
                    } else {
                        InputStream input = buildFloatImageInputStream(fi);
                        if (input == null) {
                            log.warning("[FloatExcel] image stream null el=" + elName);
                            continue;
                        }
                        try {
                            byte[] bytes = IOUtils.toByteArray(input);
                            int pictureFormat = buildFloatImageFormat(fi);
                            pictureIndex = wb.addPicture(bytes, pictureFormat);
                            if (pictureIndexCache != null) {
                                pictureIndexCache.put(cacheKey, pictureIndex);
                            }
                        } finally {
                            IOUtils.closeQuietly(input);
                        }
                    }

                    createOneCellAnchorPicture(sheet, drawing, wb, fromCol, fromDx, fromRow, fromDy, widthEMU, heightEMU, pictureIndex, fi);
                } else if (el instanceof FloatText) {
                    FloatText ft = (FloatText) el;

                    XSSFClientAnchor tempAnchor = (XSSFClientAnchor) creationHelper.createClientAnchor();
                    tempAnchor.setAnchorType(ClientAnchor.AnchorType.DONT_MOVE_AND_RESIZE);
                    tempAnchor.setCol1(fromCol);
                    tempAnchor.setDx1(fromDx);
                    tempAnchor.setRow1(fromRow);
                    tempAnchor.setDy1(fromDy);
                    tempAnchor.setCol2(fromCol);
                    tempAnchor.setDx2((int) widthEMU);
                    tempAnchor.setRow2(fromRow);
                    tempAnchor.setDy2((int) heightEMU);

                    if (xssfDrawing == null) {
                        XSSFSheet xssfSheet = wb.getXSSFWorkbook().getSheet(sheet.getSheetName());
                        xssfDrawing = xssfSheet.getDrawingPatriarch();
                        if (xssfDrawing == null) {
                            xssfDrawing = xssfSheet.createDrawingPatriarch();
                        }
                    }

                    XSSFTextBox textbox = xssfDrawing.createTextbox(tempAnchor);

                    String value = ft.getValue();
                    if (value == null) {
                        value = "";
                    }
                    XDDFTextBody textBody = textbox.getTextBody();
                    if (textBody != null) {
                        textBody.setText(value);
                    }

                    org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShape ctShape =
                            convertTextboxToOneCellAnchor(xssfDrawing, fromCol, fromDx, fromRow, fromDy, widthEMU, heightEMU);

                    if (ctShape == null) {
                        log.warning("导出 Excel 悬浮文本 [" + ft.getName() + "]: oneCellAnchor 转换失败，跳过");
                        continue;
                    }

                    org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties ctSpPr = ctShape.getSpPr();
                    if (ctSpPr != null && ctSpPr.isSetXfrm()) {
                        org.openxmlformats.schemas.drawingml.x2006.main.CTTransform2D xfrm = ctSpPr.getXfrm();
                        if (xfrm != null) {
                            org.openxmlformats.schemas.drawingml.x2006.main.CTPositiveSize2D ext = xfrm.getExt();
                            if (ext != null) {
                                ext.setCx(widthEMU);
                                ext.setCy(heightEMU);
                            }
                        }
                    }

                    applyFloatTextFontProperties(ctShape, ft);
                }
            } catch (Exception e) {
                log.log(Level.WARNING, "导出 Excel 悬浮元素 [" + elName + "] 失败，已跳过", e);
            }
        }
    }

    /**
     * 通过底层 CT API 为文本框设置字体属性（字体、字号、颜色、粗体、斜体、下划线、对齐、垂直对齐、背景色、边框）。
     */
    private void applyFloatTextFontProperties(org.openxmlformats.schemas.drawingml.x2006.spreadsheetDrawing.CTShape ctShape, FloatText ft) {
        CTTextBody ctBody = ctShape.getTxBody();
        if (ctBody == null || ctBody.sizeOfPArray() == 0) {
            return;
        }
        CTTextParagraph ctPara = ctBody.getPArray(0);
        CTTextParagraphProperties pPr = ctPara.isSetPPr() ? ctPara.getPPr() : ctPara.addNewPPr();
        String align = ft.getAlign();
        if (StringUtils.isNotEmpty(align)) {
            if ("center".equals(align)) {
                pPr.setAlgn(STTextAlignType.CTR);
            } else if ("right".equals(align)) {
                pPr.setAlgn(STTextAlignType.R);
            } else {
                pPr.setAlgn(STTextAlignType.L);
            }
        }
        if (ctPara.sizeOfRArray() == 0) {
            return;
        }
        CTRegularTextRun ctRun = ctPara.getRArray(0);
        CTTextCharacterProperties rPr = ctRun.isSetRPr() ? ctRun.getRPr() : ctRun.addNewRPr();
        String fontFamily = ft.getFontFamily();
        if (StringUtils.isNotBlank(fontFamily)) {
            CTTextFont latin = rPr.isSetLatin() ? rPr.getLatin() : rPr.addNewLatin();
            latin.setTypeface(fontFamily);
        }
        Integer fontSize = ft.getFontSize();
        if (fontSize != null && fontSize > 0) {
            rPr.setSz((int) (100 * UnitUtils.pixelToPoint(fontSize)));
        }
        String forecolor = ft.getForecolor();
        if (StringUtils.isNotEmpty(forecolor)) {
            String[] colors = forecolor.split(",");
            if (colors.length >= 3) {
                int r = Integer.parseInt(colors[0].trim());
                int g = Integer.parseInt(colors[1].trim());
                int b = Integer.parseInt(colors[2].trim());
                CTSolidColorFillProperties fill = rPr.isSetSolidFill() ? rPr.getSolidFill() : rPr.addNewSolidFill();
                CTSRgbColor clr = fill.isSetSrgbClr() ? fill.getSrgbClr() : fill.addNewSrgbClr();
                clr.setVal(new byte[]{(byte) r, (byte) g, (byte) b});
            }
        }
        if (ft.getBold() != null && ft.getBold()) {
            rPr.setB(true);
        }
        if (ft.getItalic() != null && ft.getItalic()) {
            rPr.setI(true);
        }
        if (ft.getUnderline() != null && ft.getUnderline()) {
            rPr.setU(STTextUnderlineType.SNG);
        }

        org.openxmlformats.schemas.drawingml.x2006.main.CTTextBodyProperties bodyPr = ctBody.getBodyPr();
        if (bodyPr == null) {
            bodyPr = ctBody.addNewBodyPr();
        }
        String valign = ft.getValign();
        if (StringUtils.isNotEmpty(valign)) {
            if ("middle".equals(valign)) {
                bodyPr.setAnchor(org.openxmlformats.schemas.drawingml.x2006.main.STTextAnchoringType.CTR);
            } else if ("bottom".equals(valign)) {
                bodyPr.setAnchor(org.openxmlformats.schemas.drawingml.x2006.main.STTextAnchoringType.B);
            } else {
                bodyPr.setAnchor(org.openxmlformats.schemas.drawingml.x2006.main.STTextAnchoringType.T);
            }
        }

        org.openxmlformats.schemas.drawingml.x2006.main.CTShapeProperties spPr =
                ctShape.getSpPr() != null ? ctShape.getSpPr() : ctShape.addNewSpPr();
        String bgcolor = ft.getBgcolor();
        if (StringUtils.isNotEmpty(bgcolor)) {
            String[] bgColors = bgcolor.split(",");
            if (bgColors.length >= 3) {
                int r = Integer.parseInt(bgColors[0].trim());
                int g = Integer.parseInt(bgColors[1].trim());
                int b = Integer.parseInt(bgColors[2].trim());
                org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties bgFill =
                        spPr.isSetSolidFill() ? spPr.getSolidFill() : spPr.addNewSolidFill();
                org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor bgClr =
                        bgFill.isSetSrgbClr() ? bgFill.getSrgbClr() : bgFill.addNewSrgbClr();
                bgClr.setVal(new byte[]{(byte) r, (byte) g, (byte) b});
            }
        } else {
            if (!spPr.isSetNoFill()) {
                spPr.addNewNoFill();
            }
            if (spPr.isSetSolidFill()) {
                spPr.unsetSolidFill();
            }
        }

        Border border = ft.getTopBorder() != null ? ft.getTopBorder()
                : ft.getBottomBorder() != null ? ft.getBottomBorder()
                : ft.getLeftBorder() != null ? ft.getLeftBorder()
                : ft.getRightBorder();
        if (border != null) {
            org.openxmlformats.schemas.drawingml.x2006.main.CTLineProperties ln =
                    spPr.isSetLn() ? spPr.getLn() : spPr.addNewLn();
            int borderW = border.getWidth() > 0 ? border.getWidth() : 1;
            ln.setW(borderW * 9525); // EMU per pixel
            String borderColorStr = border.getColor();
            if (StringUtils.isNotEmpty(borderColorStr)) {
                String[] bcArr = borderColorStr.split(",");
                if (bcArr.length >= 3) {
                    int r = Integer.parseInt(bcArr[0].trim());
                    int g = Integer.parseInt(bcArr[1].trim());
                    int b = Integer.parseInt(bcArr[2].trim());
                    org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties borderFill =
                            ln.isSetSolidFill() ? ln.getSolidFill() : ln.addNewSolidFill();
                    org.openxmlformats.schemas.drawingml.x2006.main.CTSRgbColor borderClr =
                            borderFill.isSetSrgbClr() ? borderFill.getSrgbClr() : borderFill.addNewSrgbClr();
                    borderClr.setVal(new byte[]{(byte) r, (byte) g, (byte) b});
                }
            }
            org.openxmlformats.schemas.drawingml.x2006.main.CTPresetLineDashProperties dash =
                    ln.isSetPrstDash() ? ln.getPrstDash() : ln.addNewPrstDash();
            String styleName = border.getStyle() != null ? border.getStyle().toString() : "solid";
            if ("dashed".equals(styleName)) {
                dash.setVal(org.openxmlformats.schemas.drawingml.x2006.main.STPresetLineDashVal.DASH);
            } else {
                dash.setVal(org.openxmlformats.schemas.drawingml.x2006.main.STPresetLineDashVal.SOLID);
            }
        }
    }

}
