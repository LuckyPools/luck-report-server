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
package com.luck.report.core.definition;

import java.io.Serializable;

/**
 * 预览工具栏配置。每个布尔字段对应预览页工具栏中的一个按钮或一组控件是否可见。
 *
 * @author luck-report
 * @since 2026年08月29日
 */
public class Tool implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 是否显示工具栏整体
     */
    private boolean show = true;
    /**
     * 打印按钮
     */
    private boolean print = true;
    /**
     * PDF直接打印按钮
     */
    private boolean pdfPrint = true;
    /**
     * PDF预览打印按钮
     */
    private boolean pdfPreviewPrint = true;
    /**
     * 导出PDF按钮
     */
    private boolean pdf = true;
    /**
     * 导出Word按钮
     */
    private boolean word = true;
    /**
     * 导出Excel按钮
     */
    private boolean excel = true;
    /**
     * 分页导出Excel按钮
     */
    private boolean pagingExcel = true;
    /**
     * 分页分Sheet导出Excel按钮
     */
    private boolean sheetPagingExcel = true;
    /**
     * 分页控件（首页/上一页/下一页/末页等）
     */
    private boolean paging = true;

    /**
     * 默认无参构造器
     */
    public Tool() {
    }

    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public boolean isPrint() {
        return print;
    }

    public void setPrint(boolean print) {
        this.print = print;
    }

    public boolean isPdfPrint() {
        return pdfPrint;
    }

    public void setPdfPrint(boolean pdfPrint) {
        this.pdfPrint = pdfPrint;
    }

    public boolean isPdfPreviewPrint() {
        return pdfPreviewPrint;
    }

    public void setPdfPreviewPrint(boolean pdfPreviewPrint) {
        this.pdfPreviewPrint = pdfPreviewPrint;
    }

    public boolean isPdf() {
        return pdf;
    }

    public void setPdf(boolean pdf) {
        this.pdf = pdf;
    }

    public boolean isWord() {
        return word;
    }

    public void setWord(boolean word) {
        this.word = word;
    }

    public boolean isExcel() {
        return excel;
    }

    public void setExcel(boolean excel) {
        this.excel = excel;
    }

    public boolean isPagingExcel() {
        return pagingExcel;
    }

    public void setPagingExcel(boolean pagingExcel) {
        this.pagingExcel = pagingExcel;
    }

    public boolean isSheetPagingExcel() {
        return sheetPagingExcel;
    }

    public void setSheetPagingExcel(boolean sheetPagingExcel) {
        this.sheetPagingExcel = sheetPagingExcel;
    }

    public boolean isPaging() {
        return paging;
    }

    public void setPaging(boolean paging) {
        this.paging = paging;
    }
}
