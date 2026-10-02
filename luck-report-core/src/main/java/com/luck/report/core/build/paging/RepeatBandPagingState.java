package com.luck.report.core.build.paging;

import com.luck.report.core.definition.Band;
import com.luck.report.core.model.Report;
import com.luck.report.core.model.Row;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 分页扫描过程中的重复表头/表尾状态（单趟收集 + 模式判定）。
 */
public class RepeatBandPagingState {

    private static final List<Row> EMPTY_ROWS = Collections.emptyList();

    private final List<Row> templateHeaders;
    private final List<Row> templateFooters;
    private final List<Row> headerBlock = new ArrayList<Row>();
    private final List<Row> footerBlock = new ArrayList<Row>();
    private final List<Row> currentHeaders;
    private final List<Row> currentFooters;
    private boolean headerGroupMode;
    private boolean footerGroupMode;
    private List<Row> headerSnapshot;
    private List<Row> footerSnapshot;

    public RepeatBandPagingState(List<Row> templateHeaders, List<Row> templateFooters) {
        this.templateHeaders = templateHeaders != null ? templateHeaders : new ArrayList<Row>();
        this.templateFooters = templateFooters != null ? templateFooters : new ArrayList<Row>();
        this.currentHeaders = RepeatBandPagingSupport.copyRowList(this.templateHeaders);
        this.currentFooters = RepeatBandPagingSupport.copyRowList(this.templateFooters);
    }

    /**
     * 判断当前是否存在重复表头或表尾
     *
     * @return true 表示模板或扫描中已有 headerrepeat/footerrepeat，需做跟组判定
     */
    public boolean hasRepeatBands() {
        return !templateHeaders.isEmpty() || !templateFooters.isEmpty()
                || !headerBlock.isEmpty() || !footerBlock.isEmpty();
    }

    public void acceptHeader(Row row) {
        headerBlock.add(row);
        RepeatBandPagingSupport.replaceByRowKey(currentHeaders, row);
    }

    public void acceptFooter(Row row) {
        footerBlock.add(row);
        RepeatBandPagingSupport.replaceByRowKey(currentFooters, row);
    }

    /**
     * 接收正文行，判定跟组模式；若为本页首条正文则快照当前重复表头/表尾
     *
     * @param bodyRow 正文行，非空
     * @param firstBodyOnPage true 表示本页尚无正文，需对当前表头/表尾做快照
     */
    public void acceptBodyRow(Row bodyRow, boolean firstBodyOnPage) {
        if (!hasRepeatBands()) {
            return;
        }
        if (!headerGroupMode && (!templateHeaders.isEmpty() || !headerBlock.isEmpty())) {
            headerGroupMode = RepeatBandPagingSupport.rowUsesBandAsLeftParent(bodyRow, Band.headerrepeat);
        }
        if (!footerGroupMode && (!templateFooters.isEmpty() || !footerBlock.isEmpty())) {
            footerGroupMode = RepeatBandPagingSupport.rowUsesBandAsLeftParent(bodyRow, Band.footerrepeat);
        }
        if (firstBodyOnPage) {
            if (headerGroupMode) {
                headerSnapshot = RepeatBandPagingSupport.copyRowList(currentHeaders);
            }
            if (footerGroupMode) {
                footerSnapshot = RepeatBandPagingSupport.copyRowList(currentFooters);
            }
        }
    }

    /**
     * 取固定行分页中重复表头占用的行数
     *
     * @return 跟组模式为模板槽位数，整块模式为已收集的展开表头行数
     */
    public int repeatHeaderRowCount() {
        return headerGroupMode ? templateHeaders.size() : headerBlock.size();
    }

    /**
     * 取固定行分页中重复表尾占用的行数
     *
     * @return 跟组模式为模板槽位数；整块模式下 block 仍空时回退模板行数
     */
    public int repeatFooterRowCount() {
        if (footerGroupMode) {
            return templateFooters.size();
        }
        return footerBlock.isEmpty() ? templateFooters.size() : footerBlock.size();
    }

    public int headerHeight(Report report) {
        if (headerGroupMode) {
            return report.getRepeatHeaderRowHeight();
        }
        int sum = RepeatBandPagingSupport.sumRealHeight(headerBlock);
        return sum > 0 ? sum : report.getRepeatHeaderRowHeight();
    }

    public int footerHeight(Report report) {
        if (footerGroupMode) {
            return report.getRepeatFooterRowHeight();
        }
        int sum = RepeatBandPagingSupport.sumRealHeight(footerBlock);
        return sum > 0 ? sum : report.getRepeatFooterRowHeight();
    }

    public List<Row> headersForPage(List<Row> pageBodyRows, Report report) {
        if (!hasRepeatBands()) {
            return EMPTY_ROWS;
        }
        if (headerGroupMode) {
            List<Row> fallback = headerSnapshot != null ? headerSnapshot : currentHeaders;
            return RepeatBandPagingSupport.resolveGroupHeaders(
                    pageBodyRows, templateHeaders, report != null ? report.getRows() : null, fallback);
        }
        return RepeatBandPagingSupport.selectRepeatRowsByMode(false, null, headerBlock, currentHeaders);
    }

    public List<Row> footersForPage(List<Row> pageBodyRows, Report report) {
        if (!hasRepeatBands()) {
            return EMPTY_ROWS;
        }
        if (footerGroupMode) {
            List<Row> fallback = footerSnapshot != null ? footerSnapshot : currentFooters;
            return RepeatBandPagingSupport.resolveGroupFooters(
                    pageBodyRows, templateFooters, report != null ? report.getRows() : null, fallback);
        }
        return RepeatBandPagingSupport.selectRepeatRowsByMode(false, null, footerBlock, currentFooters);
    }

    public void clearPageSnapshots() {
        headerSnapshot = null;
        footerSnapshot = null;
    }

    public boolean isHeaderGroupMode() {
        return headerGroupMode;
    }

    public boolean isFooterGroupMode() {
        return footerGroupMode;
    }

    public List<Row> getHeaderBlock() {
        return headerBlock;
    }
}
