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

import com.luck.report.core.definition.*;
import com.luck.report.core.definition.datasource.DatasourceDefinition;
import com.luck.report.core.definition.searchform.SearchForm;
import com.luck.report.core.exception.ReportException;
import com.luck.report.core.exception.ReportParseException;
import com.luck.report.core.parser.impl.*;
import com.luck.report.core.parser.impl.searchform.SearchFormParser;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.SAXParseException;

import java.io.InputStream;
import java.util.*;

/**
 * @author Jacky.gao
 * @since 2016年12月2日
 */
public class ReportParser {
    private Map<String, Parser<?>> parsers = new HashMap<String, Parser<?>>();

    public ReportParser() {
        parsers.put("row", new RowParser());
        parsers.put("column", new ColumnParser());
        parsers.put("cell", new CellParser());
        parsers.put("datasource", new DatasourceParser());
        parsers.put("paper", new PaperParser());
        parsers.put("header", new HeaderFooterParser());
        parsers.put("footer", new HeaderFooterParser());
        parsers.put("form", new SearchFormParser());
        parsers.put("float-image", new FloatImageParser());
        parsers.put("float-text", new FloatTextParser());
        parsers.put("tool", new ToolParser());
    }

    public ReportDefinition parse(InputStream inputStream, String reportPath) {
        ReportDefinition report = new ReportDefinition();
        report.setReportFullName(reportPath);
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(inputStream);
            Element element = document.getRootElement();
            if (!element.getName().equals("ureport")) {
                throw new ReportParseException("Unknow report file.");
            }
            initReportLists(report);
            for (Object obj : element.elements()) {
                if (!(obj instanceof Element)) {
                    continue;
                }
                parseElement((Element) obj, report);
            }
            Collections.sort(report.getRows());
            Collections.sort(report.getColumns());
        } catch (DocumentException ex) {
            // SAX 语法错误单独处理：从底层 SAXParseException 还原行列号
            throw new ReportParseException(buildXmlSyntaxMessage(ex));
        } catch (ReportParseException ex) {
            // 已带定位信息的解析异常直接透传，避免再包一层丢失消息
            throw ex;
        } catch (Exception ex) {
            throw new ReportParseException(ex);
        }
        rebuild(report);
        return report;
    }

    /**
     * 方法说明：初始化报表定义的集合字段，保证子元素解析时有挂载目标
     * @param report 报表定义对象，不可为空
     */
    private void initReportLists(ReportDefinition report) {
        report.setRows(new ArrayList<RowDefinition>());
        report.setColumns(new ArrayList<ColumnDefinition>());
        report.setCells(new ArrayList<CellDefinition>());
        report.setDatasources(new ArrayList<DatasourceDefinition>());
        report.setFloatImages(new ArrayList<FloatImage>());
        report.setFloatTexts(new ArrayList<FloatText>());
    }

    /**
     * 方法说明：解析单个子元素，失败时带上标签名构造异常，便于定位出错的 XML 片段
     * @param ele 当前子元素，不可为空
     * @param report 报表定义对象，不可为空
     */
    private void parseElement(Element ele, ReportDefinition report) {
        Parser<?> parser = parsers.get(ele.getName());
        if (parser == null) {
            // 未知标签与历史行为保持一致：静默跳过，保证向前兼容
            return;
        }
        Object target;
        try {
            target = parser.parse(ele);
        } catch (Exception ex) {
            throw new ReportParseException("解析 <" + ele.getName() + "> 元素失败：" + resolveExceptionMessage(ex));
        }
        attachTarget(ele, target, report);
    }

    /**
     * 方法说明：按解析结果类型将目标对象挂载到报表定义对应字段
     * @param ele 当前子元素，仅用于区分 header/footer，不可为空
     * @param target 子解析器返回的目标对象，可为 null（各解析器约定）
     * @param report 报表定义对象，不可为空
     */
    private void attachTarget(Element ele, Object target, ReportDefinition report) {
        if (target instanceof RowDefinition) {
            report.getRows().add((RowDefinition) target);
        } else if (target instanceof ColumnDefinition) {
            report.getColumns().add((ColumnDefinition) target);
        } else if (target instanceof CellDefinition) {
            report.getCells().add((CellDefinition) target);
        } else if (target instanceof DatasourceDefinition) {
            report.getDatasources().add((DatasourceDefinition) target);
        } else if (target instanceof FloatImage) {
            report.getFloatImages().add((FloatImage) target);
        } else if (target instanceof FloatText) {
            report.getFloatTexts().add((FloatText) target);
        } else if (target instanceof Paper) {
            report.setPaper((Paper) target);
        } else if (target instanceof HeaderFooterDefinition) {
            HeaderFooterDefinition hf = (HeaderFooterDefinition) target;
            if (ele.getName().equals("header")) {
                report.setHeader(hf);
            } else {
                report.setFooter(hf);
            }
        } else if (target instanceof SearchForm) {
            report.setSearchForm((SearchForm) target);
        } else if (target instanceof Tool) {
            report.setTool((Tool) target);
        } else {
            throw new ReportParseException("Unknow element :" + ele.getName());
        }
    }

    /**
     * 方法说明：将 dom4j 的 XML 语法异常格式化为带行列号的中文提示
     * @param ex dom4j 抛出的文档异常，不可为空
     * @return 形如「XML 语法错误（第 X 行第 Y 列）：原因」的提示文本
     */
    private String buildXmlSyntaxMessage(DocumentException ex) {
        Throwable nested = ex.getNestedException();
        if (nested instanceof SAXParseException) {
            SAXParseException se = (SAXParseException) nested;
            return "XML 语法错误（第 " + se.getLineNumber() + " 行第 " + se.getColumnNumber()
                    + " 列）：" + se.getMessage();
        }
        return "XML 语法错误：" + resolveExceptionMessage(ex);
    }

    /**
     * 方法说明：提取异常的可读消息，消息为空时退回异常类名
     * @param ex 任意异常，不可为空
     * @return 异常消息或类名
     */
    private String resolveExceptionMessage(Exception ex) {
        String msg = ex.getMessage();
        if (msg == null || msg.trim().isEmpty()) {
            msg = ex.getClass().getSimpleName();
        }
        return msg;
    }

    private void rebuild(ReportDefinition report) {
        List<CellDefinition> cells = report.getCells();
        Map<String, CellDefinition> cellsMap = new HashMap<String, CellDefinition>();
        Map<String, CellDefinition> cellsRowColMap = new HashMap<String, CellDefinition>();
        for (CellDefinition cell : cells) {
            cellsMap.put(cell.getName(), cell);
            int rowNum = cell.getRowNumber(), colNum = cell.getColumnNumber(), rowSpan = cell.getRowSpan(), colSpan = cell.getColSpan();
            rowSpan = rowSpan > 0 ? rowSpan-- : 1;
            colSpan = colSpan > 0 ? colSpan-- : 1;
            int rowStart = rowNum, rowEnd = rowNum + rowSpan, colStart = colNum, colEnd = colNum + colSpan;
            for (int i = rowStart; i < rowEnd; i++) {
                cellsRowColMap.put(i + "," + colNum, cell);
            }
            for (int i = colStart; i < colEnd; i++) {
                cellsRowColMap.put(rowNum + "," + i, cell);
            }
        }
        for (CellDefinition cell : cells) {
            int rowNumber = cell.getRowNumber();
            int colNumber = cell.getColumnNumber();
            String leftParentCellName = cell.getLeftParentCellName();
            if (StringUtils.isNotBlank(leftParentCellName)) {
                if (!leftParentCellName.equals("root")) {
                    CellDefinition targetCell = cellsMap.get(leftParentCellName);
                    if (targetCell == null) {
                        throw new ReportException("Cell [" + cell.getName() + "] 's left parent cell [" + leftParentCellName + "] not exist.");
                    }
                    cell.setLeftParentCell(targetCell);
                }
            } else {
                if (colNumber > 1) {
                    CellDefinition targetCell = cellsRowColMap.get(rowNumber + "," + (colNumber - 1));
                    cell.setLeftParentCell(targetCell);
                }
            }
            String topParentCellName = cell.getTopParentCellName();
            if (StringUtils.isNotBlank(topParentCellName)) {
                if (!topParentCellName.equals("root")) {
                    CellDefinition targetCell = cellsMap.get(topParentCellName);
                    if (targetCell == null) {
                        throw new ReportException("Cell [" + cell.getName() + "] 's top parent cell [" + topParentCellName + "] not exist.");
                    }
                    cell.setTopParentCell(targetCell);
                }
            } else {
                if (rowNumber > 1) {
                    CellDefinition targetCell = cellsRowColMap.get((rowNumber - 1) + "," + colNumber);
                    cell.setTopParentCell(targetCell);
                }
            }
        }
    }
}
