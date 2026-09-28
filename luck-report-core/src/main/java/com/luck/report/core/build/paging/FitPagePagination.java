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
package com.luck.report.core.build.paging;

import com.luck.report.core.definition.Band;
import com.luck.report.core.definition.Orientation;
import com.luck.report.core.definition.Paper;
import com.luck.report.core.model.Report;
import com.luck.report.core.model.Row;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Jacky.gao
 * @since 2017年1月17日
 */
public class FitPagePagination extends BasePagination implements Pagination {

    /**
     * 按纸张高度分页；行高为 0 的正文留在当前页且不占高度
     *
     * @param report 已计算完成的报表，非空
     * @return 分页结果；明细全隐藏时仍有一页，重复表头接在该页前面
     */
    @Override
    public List<Page> doPaging(Report report) {
        Paper paper = report.getPaper();
        int height = paper.getHeight() - paper.getBottomMargin() - paper.getTopMargin() - 5;
        if (paper.getOrientation().equals(Orientation.landscape)) {
            height = paper.getWidth() - paper.getBottomMargin() - paper.getTopMargin() - 5;
        }
        List<Row> rows = report.getRows();
        List<Row> headerRows = report.getHeaderRepeatRows();
        List<Row> footerRows = report.getFooterRepeatRows();
        List<Row> titleRows = report.getTitleRows();
        List<Row> summaryRows = report.getSummaryRows();

        RepeatBandPagingState state = new RepeatBandPagingState(headerRows, footerRows);
        int titleRowHeight = report.getTitleRowsHeight();
        int bandHeight = -1;
        int rowHeight = titleRowHeight;

        List<Page> pages = new ArrayList<Page>();
        List<Row> pageRows = new ArrayList<Row>();

        int i = 0;
        int rowSize = rows.size();
        Row row = rowSize > 0 ? rows.get(i) : null;
        int pageIndex = 1;
        while (row != null) {
            Band band = row.getBand();
            if (band != null) {
                if (band.equals(Band.headerrepeat)) {
                    state.acceptHeader(row);
                } else if (band.equals(Band.footerrepeat)) {
                    state.acceptFooter(row);
                }
                i++;
                row = i < rowSize ? rows.get(i) : null;
                continue;
            }
            int rowRealHeight = row.getRealHeight();
            if (state.hasRepeatBands()) {
                state.acceptBodyRow(row, pageRows.isEmpty());
            }
            pageRows.add(row);
            row.setPageIndex(pageIndex);
            if (rowRealHeight == 0) {
                i++;
                row = i < rowSize ? rows.get(i) : null;
                continue;
            }
            if (bandHeight < 0) {
                bandHeight = state.headerHeight(report) + state.footerHeight(report);
                rowHeight += bandHeight;
            }
            rowHeight += rowRealHeight + 1;
            boolean overflow = false;
            if ((i + 1) < rows.size()) {
                Row nextRow = rows.get(i + 1);
                if ((rowHeight + nextRow.getRealHeight()) > height) {
                    overflow = true;
                }
            }
            if (!overflow && row.isPageBreak()) {
                overflow = true;
            }
            if (overflow) {
                Page newPage = buildPage(pageRows, state.headersForPage(pageRows, report),
                        state.footersForPage(pageRows, report), titleRows, pageIndex, report);
                pageIndex++;
                pages.add(newPage);
                rowHeight = bandHeight > 0 ? bandHeight : (state.headerHeight(report) + state.footerHeight(report));
                pageRows = new ArrayList<Row>();
                state.clearPageSnapshots();
            }
            i++;
            row = i < rowSize ? rows.get(i) : null;
        }
        if (!pageRows.isEmpty()) {
            Page newPage = buildPage(pageRows, state.headersForPage(pageRows, report),
                    state.footersForPage(pageRows, report), titleRows, pageIndex, report);
            pages.add(newPage);
        }
        report.getContext().setTotalPages(pages.size());
        buildPageHeaderFooter(pages, report);
        if (!CollectionUtils.isEmpty(summaryRows)) {
            buildSummaryRows(summaryRows, pages);
        }
        return pages;
    }
}
