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
package com.luck.report.core.parser.impl;

import com.luck.report.core.definition.Tool;
import com.luck.report.core.parser.Parser;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Element;

/**
 * 预览工具栏配置 XML 解析器。
 * <p>解析 {@code <tool>} 元素的各属性，映射为 {@link Tool} 对象的布尔字段。
 *
 * @author luck-report
 * @since 2026年08月29日
 */
public class ToolParser implements Parser<Tool> {

    @Override
    public Tool parse(Element element) {
        Tool tool = new Tool();
        String show = element.attributeValue("show");
        if (StringUtils.isNotBlank(show)) {
            tool.setShow(Boolean.parseBoolean(show));
        }
        String print = element.attributeValue("print");
        if (StringUtils.isNotBlank(print)) {
            tool.setPrint(Boolean.parseBoolean(print));
        }
        String pdfPrint = element.attributeValue("pdf-print");
        if (StringUtils.isNotBlank(pdfPrint)) {
            tool.setPdfPrint(Boolean.parseBoolean(pdfPrint));
        }
        String pdfPreviewPrint = element.attributeValue("pdf-preview-print");
        if (StringUtils.isNotBlank(pdfPreviewPrint)) {
            tool.setPdfPreviewPrint(Boolean.parseBoolean(pdfPreviewPrint));
        }
        String pdf = element.attributeValue("pdf");
        if (StringUtils.isNotBlank(pdf)) {
            tool.setPdf(Boolean.parseBoolean(pdf));
        }
        String word = element.attributeValue("word");
        if (StringUtils.isNotBlank(word)) {
            tool.setWord(Boolean.parseBoolean(word));
        }
        String excel = element.attributeValue("excel");
        if (StringUtils.isNotBlank(excel)) {
            tool.setExcel(Boolean.parseBoolean(excel));
        }
        String pagingExcel = element.attributeValue("paging-excel");
        if (StringUtils.isNotBlank(pagingExcel)) {
            tool.setPagingExcel(Boolean.parseBoolean(pagingExcel));
        }
        String sheetPagingExcel = element.attributeValue("sheet-paging-excel");
        if (StringUtils.isNotBlank(sheetPagingExcel)) {
            tool.setSheetPagingExcel(Boolean.parseBoolean(sheetPagingExcel));
        }
        String paging = element.attributeValue("paging");
        if (StringUtils.isNotBlank(paging)) {
            tool.setPaging(Boolean.parseBoolean(paging));
        }
        return tool;
    }
}
