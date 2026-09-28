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
package com.luck.report.core.model;

import java.util.List;


/**
 * @author Jacky.gao
 * @since 2016年11月1日
 */
public class Column extends Line {
    private int width;
    private boolean hide;
    /** 展开插列前的排序键，仅 complete() 排序用 */
    private int tempColumnNumber;
    /** 正式列号（1-based），insert/chain 后由 Report 写入 */
    private int columnNumber;
    private Column prev;
    private Column next;
    private List<Column> columns;

    public Column(List<Column> columns) {
        this.columns = columns;
    }

    public Column newColumn() {
        Column col = new Column(columns);
        col.setWidth(width);
        return col;
    }

    public int getColumnNumber() {
        return columnNumber;
    }

    public void setColumnNumber(int columnNumber) {
        this.columnNumber = columnNumber;
    }

    public Column getPrev() {
        return prev;
    }

    public void setPrev(Column prev) {
        this.prev = prev;
    }

    public Column getNext() {
        return next;
    }

    public void setNext(Column next) {
        this.next = next;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    public int getTempColumnNumber() {
        return tempColumnNumber;
    }

    public void setTempColumnNumber(int tempColumnNumber) {
        this.tempColumnNumber = tempColumnNumber;
    }
}
