package com.luck.report.core.build.paging;

import com.luck.report.core.definition.Band;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Row;

import java.util.ArrayList;
import java.util.List;

/**
 * 重复表头/表尾：跟组（正文左父格落在 band 行）vs 整块（每页复制全部展开行）。供分页扫描单趟复用，避免额外全表遍历。
 */
public final class RepeatBandPagingSupport {

    private RepeatBandPagingSupport() {
    }

    public static boolean rowUsesBandAsLeftParent(Row bodyRow, Band band) {
        return findLeftParentBandRow(bodyRow, band) != null;
    }

    /**
     * 正文行左父格链上第一个落在指定 band 的行。
     */
    public static Row findLeftParentBandRow(Row bodyRow, Band band) {
        if (bodyRow == null || band == null) {
            return null;
        }
        List<Cell> cells = bodyRow.getCells();
        if (cells == null) {
            return null;
        }
        for (Cell cell : cells) {
            Cell left = cell.getLeftParentCell();
            while (left != null) {
                Row parentRow = left.getRow();
                if (parentRow != null && band.equals(parentRow.getBand())) {
                    return parentRow;
                }
                left = left.getLeftParentCell();
            }
        }
        return null;
    }

    public static Row firstContentRow(List<Row> rows) {
        if (rows == null) {
            return null;
        }
        for (Row row : rows) {
            if (row != null && row.getRealHeight() > 0 && row.getBand() == null) {
                return row;
            }
        }
        return null;
    }

    public static Row lastContentRow(List<Row> rows) {
        if (rows == null || rows.isEmpty()) {
            return null;
        }
        for (int i = rows.size() - 1; i >= 0; i--) {
            Row row = rows.get(i);
            if (row != null && row.getRealHeight() > 0 && row.getBand() == null) {
                return row;
            }
        }
        return null;
    }

    /**
     * 跟组：以本页首条正文的左父 band 行为准；再按模板 rowKey 从该位置往前补齐各槽。避免扫描槽位未替换时页顶一直停在第一组。
     */
    public static List<Row> resolveGroupHeaders(List<Row> pageBodyRows, List<Row> templateHeaders,
                                               List<Row> reportRows, List<Row> fallback) {
        Row firstBody = firstContentRow(pageBodyRows);
        if (firstBody == null) {
            return copyRowList(fallback);
        }
        Row anchor = findLeftParentBandRow(firstBody, Band.headerrepeat);
        List<Row> slots = templateHeaders != null ? templateHeaders : new ArrayList<Row>();
        if (slots.isEmpty()) {
            if (anchor != null) {
                List<Row> one = new ArrayList<Row>();
                one.add(anchor);
                return one;
            }
            return copyRowList(fallback);
        }
        if (slots.size() == 1 && anchor != null) {
            List<Row> one = new ArrayList<Row>();
            one.add(anchor);
            return one;
        }
        List<Row> resolved = new ArrayList<Row>(slots.size());
        for (Row slot : slots) {
            String rowKey = slot != null ? slot.getRowKey() : null;
            Row found = null;
            if (anchor != null && rowKey != null && rowKey.equals(anchor.getRowKey())) {
                found = anchor;
            } else {
                found = findNearestBandBefore(firstBody, Band.headerrepeat, rowKey, reportRows);
            }
            resolved.add(found != null ? found : (resolveTemplateSlot(fallback, resolved.size(), slot)));
        }
        return resolved;
    }

    public static List<Row> resolveGroupFooters(List<Row> pageBodyRows, List<Row> templateFooters,
                                               List<Row> reportRows, List<Row> fallback) {
        Row lastBody = lastContentRow(pageBodyRows);
        if (lastBody == null) {
            return copyRowList(fallback);
        }
        Row anchor = findLeftParentBandRow(lastBody, Band.footerrepeat);
        List<Row> slots = templateFooters != null ? templateFooters : new ArrayList<Row>();
        if (slots.isEmpty()) {
            if (anchor != null) {
                List<Row> one = new ArrayList<Row>();
                one.add(anchor);
                return one;
            }
            return copyRowList(fallback);
        }
        if (slots.size() == 1 && anchor != null) {
            List<Row> one = new ArrayList<Row>();
            one.add(anchor);
            return one;
        }
        List<Row> resolved = new ArrayList<Row>(slots.size());
        for (Row slot : slots) {
            String rowKey = slot != null ? slot.getRowKey() : null;
            Row found = null;
            if (anchor != null && rowKey != null && rowKey.equals(anchor.getRowKey())) {
                found = anchor;
            } else {
                found = findNearestBandAfter(lastBody, Band.footerrepeat, rowKey, reportRows);
            }
            resolved.add(found != null ? found : (resolveTemplateSlot(fallback, resolved.size(), slot)));
        }
        return resolved;
    }

    public static Row findNearestBandBefore(Row fromBody, Band band, String rowKey, List<Row> reportRows) {
        if (fromBody == null || reportRows == null) {
            return null;
        }
        int idx = indexOfRow(reportRows, fromBody);
        if (idx < 0) {
            idx = reportRows.size();
        }
        for (int i = idx - 1; i >= 0; i--) {
            Row row = reportRows.get(i);
            if (row == null || !band.equals(row.getBand())) {
                continue;
            }
            if (rowKey == null || rowKey.equals(row.getRowKey())) {
                return row;
            }
        }
        return null;
    }

    public static Row findNearestBandAfter(Row fromBody, Band band, String rowKey, List<Row> reportRows) {
        if (fromBody == null || reportRows == null) {
            return null;
        }
        int idx = indexOfRow(reportRows, fromBody);
        if (idx < 0) {
            return null;
        }
        for (int i = idx + 1; i < reportRows.size(); i++) {
            Row row = reportRows.get(i);
            if (row == null || !band.equals(row.getBand())) {
                continue;
            }
            if (rowKey == null || rowKey.equals(row.getRowKey())) {
                return row;
            }
        }
        return null;
    }

    /**
     * 优先用行号在报表行列表中定位目标行，避免大表 List.indexOf 全表扫描
     *
     * @param reportRows 报表行列表，非空
     * @param target 待定位行，非空
     * @return 行下标；找不到时返回 -1
     */
    private static int indexOfRow(List<Row> reportRows, Row target) {
        int hint = -1;
        try {
            hint = target.getRowNumber() - 1;
        } catch (Exception ignored) {
            hint = -1;
        }
        if (hint >= 0 && hint < reportRows.size() && reportRows.get(hint) == target) {
            return hint;
        }
        return reportRows.indexOf(target);
    }

    /**
     * 跟组槽位未命中时回退到快照或模板行
     *
     * @param fallback 页级快照或当前槽列表，可为空
     * @param index 槽位下标，从 0 起
     * @param slot 模板槽位行，可为空
     * @return 优先 fallback 对应下标，否则返回 slot
     */
    private static Row resolveTemplateSlot(List<Row> fallback, int index, Row slot) {
        if (fallback != null && index < fallback.size() && fallback.get(index) != null) {
            return fallback.get(index);
        }
        return slot;
    }

    public static boolean replaceByRowKey(List<Row> slots, Row row) {
        if (slots == null || row == null) {
            return false;
        }
        String rowKey = row.getRowKey();
        for (int j = 0; j < slots.size(); j++) {
            Row slot = slots.get(j);
            if (slot != null && rowKey != null && rowKey.equals(slot.getRowKey())) {
                slots.set(j, row);
                return true;
            }
        }
        return false;
    }

    public static List<Row> copyRowList(List<Row> source) {
        if (source == null || source.isEmpty()) {
            return new ArrayList<Row>();
        }
        return new ArrayList<Row>(source);
    }

    public static int sumRealHeight(List<Row> rows) {
        int height = 0;
        if (rows == null) {
            return height;
        }
        for (Row row : rows) {
            if (row == null) {
                continue;
            }
            height += row.getRealHeight();
        }
        return height;
    }

    /**
     * 按跟组/整块模式选出本页应使用的重复表头或表尾行
     *
     * @param groupMode true 为跟组，false 为整块
     * @param snapshot 跟组模式下的页首快照，可为空
     * @param block 整块模式下已收集的展开行，可为空
     * @param current 当前槽位列表，可为空
     * @return 本页应挂载的重复行；跟组优先 snapshot，整块优先 block
     */
    public static List<Row> selectRepeatRowsByMode(boolean groupMode, List<Row> snapshot, List<Row> block, List<Row> current) {
        if (groupMode) {
            return snapshot != null ? snapshot : copyRowList(current);
        }
        return block != null ? block : copyRowList(current);
    }
}
