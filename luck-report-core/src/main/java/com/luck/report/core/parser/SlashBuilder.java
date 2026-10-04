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
package com.luck.report.core.parser;

import com.luck.report.core.build.Context;
import com.luck.report.core.cache.ResourceCache;
import com.luck.report.core.definition.CellDefinition;
import com.luck.report.core.definition.CellStyle;
import com.luck.report.core.definition.ColumnDefinition;
import com.luck.report.core.definition.ReportDefinition;
import com.luck.report.core.definition.RowDefinition;
import com.luck.report.core.definition.value.Slash;
import com.luck.report.core.definition.value.SlashValue;
import com.luck.report.core.exception.ReportComputeException;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Column;
import com.luck.report.core.model.Row;
import com.luck.report.core.slash.SlashEqualAngleResult;
import com.luck.report.core.slash.SlashEqualAngleUtils;
import com.luck.report.core.slash.SlashLabelLayout;
import com.luck.report.core.slash.SlashLineLayout;
import com.luck.report.core.utils.UnitUtils;

import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * 斜表头图片构建：左上角 90° 按标签数均分。
 *
 * @author Jacky.gao
 * @since 2017年3月17日
 */
public class SlashBuilder {
    /**
     * 构建斜表头缓存在 ResourceCache 中的 key
     *
     * @param reportFullName 报表全名
     * @param cellName 单元格名
     * @return 缓存 key
     */
    public static String buildKey(String reportFullName, String cellName) {
        return "slash-" + reportFullName + "-" + cellName;
    }

    /**
     * 按定义期行列宽高生成斜表头 PNG，并写回 SlashValue.base64Data
     *
     * @param cell 单元格定义
     * @param report 报表定义
     */
    public void buildSlashImage(CellDefinition cell, ReportDefinition report) {
        int rowNumber = cell.getRowNumber();
        int colNumber = cell.getColumnNumber();
        int rowSpan = cell.getRowSpan();
        int colSpan = cell.getColSpan();
        if (rowSpan == 0) {
            rowSpan = 1;
        }
        if (colSpan == 0) {
            colSpan = 1;
        }
        List<ColumnDefinition> columns = report.getColumns();
        List<RowDefinition> rows = report.getRows();
        int width = 0;
        int height = 0;
        for (int i = colNumber; i < (colNumber + colSpan); i++) {
            ColumnDefinition col = columns.get(i - 1);
            width += UnitUtils.pointToPixel(col.getWidth());
        }
        for (int i = rowNumber; i < (rowNumber + rowSpan); i++) {
            RowDefinition row = rows.get(i - 1);
            height += UnitUtils.pointToPixel(row.getHeight());
        }
        width = Math.max(1, width - computeHorizontalBorderWidth(cell.getCellStyle()));
        height = Math.max(1, height - computeVerticalBorderWidth(cell.getCellStyle()));
        SlashValue content = (SlashValue) cell.getValue();
        renderEqualAngleImage(content, cell.getCellStyle(), width, height, report.getReportFullName(), cell.getName());
    }

    /**
     * 按运行时行列宽高生成斜表头 PNG，并写回 SlashValue.base64Data
     *
     * @param cell 运行时单元格
     * @param context 构建上下文
     * @return base64 PNG（不含 data: 前缀）
     */
    public String buildSlashImage(Cell cell, Context context) {
        int rowSpan = cell.getRowSpan();
        int colSpan = cell.getColSpan();
        if (rowSpan == 0) {
            rowSpan = 1;
        }
        if (colSpan == 0) {
            colSpan = 1;
        }
        int rowNumber = cell.getRow().getRowNumber();
        int colNumber = cell.getColumn().getColumnNumber();
        int width = 0;
        int height = 0;
        for (int i = 0; i < colSpan; i++) {
            Column col = context.getColumn(colNumber + i);
            width += UnitUtils.pointToPixel(col.getWidth());
        }
        for (int i = 0; i < rowSpan; i++) {
            Row row = context.getRow(rowNumber + i);
            height += UnitUtils.pointToPixel(row.getRealHeight());
        }
        width = Math.max(1, width - computeHorizontalBorderWidth(cell.getCellStyle()));
        height = Math.max(1, height - computeVerticalBorderWidth(cell.getCellStyle()));
        SlashValue content = (SlashValue) cell.getValue();
        String reportName = context.getReport() == null ? "" : context.getReport().getReportFullName();
        return renderEqualAngleImage(content, cell.getCellStyle(), width, height, reportName, cell.getName());
    }

    /**
     * 均分 90° 绘制斜表头并写入 base64 / ResourceCache
     *
     * @param content 斜线值（文案取自 slashes）
     * @param cellStyle 单元格样式
     * @param width 像素宽
     * @param height 像素高
     * @param reportFullName 报表全名，用于缓存 key
     * @param cellName 单元格名，用于缓存 key
     * @return base64 PNG
     */
    private String renderEqualAngleImage(
            SlashValue content,
            CellStyle cellStyle,
            int width,
            int height,
            String reportFullName,
            String cellName) {
        List<String> labels = extractLabels(content);
        SlashEqualAngleResult layout = SlashEqualAngleUtils.compute(width, height, labels);
        syncSlashCoords(content, layout.getLabels());

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        Graphics2D g = (Graphics2D) image.getGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        Font font = cellStyle.getFont();
        g.setFont(font);
        g.setStroke(new BasicStroke(1f));
        String bgColor = cellStyle.getBgcolor();
        if (bgColor == null) {
            bgColor = "255,255,255";
        }
        g.setColor(parseColor(bgColor));
        g.fillRect(0, 0, width, height);

        String fc = cellStyle.getForecolor();
        if (fc == null) {
            fc = "0,0,0";
        }
        Color lineColor = parseColor(fc);
        Color fontColor = parseColor(fc);
        g.setColor(lineColor);
        for (SlashLineLayout line : layout.getLines()) {
            g.drawLine(0, 0, line.getEndX(), line.getEndY());
        }

        AffineTransform transform = g.getTransform();
        g.setColor(fontColor);
        for (SlashLabelLayout lab : layout.getLabels()) {
            int x = lab.getX();
            int y = lab.getY();
            g.rotate(Math.toRadians(lab.getDegree()), x, y);
            g.drawString(lab.getText() == null ? "" : lab.getText(), x, y);
            g.setTransform(transform);
        }

        byte[] imageBytes;
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        MemoryCacheImageOutputStream memoryImage = new MemoryCacheImageOutputStream(byteOutput);
        try {
            ImageIO.write(image, "png", memoryImage);
            imageBytes = byteOutput.toByteArray();
            String base64Data = Base64.getEncoder().encodeToString(imageBytes);
            content.setBase64Data(base64Data);
            try {
                ResourceCache.putObject(buildKey(reportFullName, cellName), imageBytes);
            } catch (Throwable ignore) {
            }
            return base64Data;
        } catch (Exception ex) {
            throw new ReportComputeException(ex);
        } finally {
            try {
                memoryImage.close();
                byteOutput.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            g.dispose();
        }
    }

    /**
     * 从 SlashValue 取出标签文案；无数据时默认三项
     *
     * @param content 斜线值
     * @return 文案列表，非空
     */
    private List<String> extractLabels(SlashValue content) {
        List<String> labels = new ArrayList<>();
        List<Slash> slashes = content.getSlashes();
        if (slashes != null) {
            for (Slash slash : slashes) {
                if (slash != null && slash.getText() != null) {
                    labels.add(slash.getText());
                }
            }
        }
        if (labels.isEmpty()) {
            labels.add("项目1");
            labels.add("项目2");
            labels.add("项目3");
        }
        return labels;
    }

    /**
     * 将均分后的文字坐标写回 SlashValue，便于导出与设计器一致
     *
     * @param content 斜线值
     * @param labels 均分布局标签
     */
    private void syncSlashCoords(SlashValue content, List<SlashLabelLayout> labels) {
        List<Slash> slashes = new ArrayList<>();
        for (SlashLabelLayout lab : labels) {
            Slash slash = new Slash();
            slash.setText(lab.getText());
            slash.setX(lab.getX());
            slash.setY(lab.getY());
            slash.setDegree(lab.getDegree());
            slashes.add(slash);
        }
        content.setSlashes(slashes);
    }

    /**
     * 左右边框宽度之和（像素近似用磅值累加，与历史实现一致）
     *
     * @param cellStyle 样式
     * @return 垂直方向边框占用
     */
    private int computeVerticalBorderWidth(CellStyle cellStyle) {
        int verticalBorderWidth = 0;
        if (cellStyle.getLeftBorder() != null) {
            verticalBorderWidth += cellStyle.getLeftBorder().getWidth();
        }
        if (cellStyle.getRightBorder() != null) {
            verticalBorderWidth += cellStyle.getRightBorder().getWidth();
        }
        return verticalBorderWidth;
    }

    /**
     * 上下边框宽度之和
     *
     * @param cellStyle 样式
     * @return 水平方向边框占用
     */
    private int computeHorizontalBorderWidth(CellStyle cellStyle) {
        int horizontalBorderWidth = 0;
        if (cellStyle.getTopBorder() != null) {
            horizontalBorderWidth += cellStyle.getTopBorder().getWidth();
        }
        if (cellStyle.getBottomBorder() != null) {
            horizontalBorderWidth += cellStyle.getBottomBorder().getWidth();
        }
        return horizontalBorderWidth;
    }

    /**
     * 解析 "r,g,b" 颜色字符串
     *
     * @param text 颜色文本
     * @return Color；text 为 null 时返回 null
     */
    private Color parseColor(String text) {
        if (text == null) {
            return null;
        }
        String[] str = text.split(",");
        return new Color(Integer.valueOf(str[0]), Integer.valueOf(str[1]), Integer.valueOf(str[2]));
    }
}
