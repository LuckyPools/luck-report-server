/**
 * ****************************************************************************
 */
package com.luck.report.web.modules.report.handler;

/**
 * @author Jacky.gao
 * @since 2017年5月26日
 */
public class Span {
    private final int rowSpan;
    private final int colSpan;

    public Span(int rowSpan, int colSpan) {
        this.rowSpan = rowSpan;
        this.colSpan = colSpan;
    }

    public int getRowSpan() {
        return rowSpan;
    }

    public int getColSpan() {
        return colSpan;
    }
}
