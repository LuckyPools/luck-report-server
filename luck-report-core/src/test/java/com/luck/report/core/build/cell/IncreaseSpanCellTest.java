package com.luck.report.core.build.cell;

import com.luck.report.core.build.cell.down.CellDownDuplicator;
import com.luck.report.core.build.cell.down.DownDuplicate;
import com.luck.report.core.build.cell.right.CellRightDuplicator;
import com.luck.report.core.build.cell.right.RightDuplicate;
import com.luck.report.core.model.Cell;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IncreaseSpanCellTest {

    @Test
    void down_unmergedParent_tallBlock_countsOriginalRow() {
        Cell parent = new Cell();
        CellDownDuplicator duplicator = new CellDownDuplicator(parent, DuplicateType.IncreseSpan, 1);
        DownDuplicate down = new DownDuplicate(new Cell(), 2, null);

        duplicator.duplicate(down, null);
        assertEquals(3, parent.getRowSpan());
        duplicator.duplicate(down, null);
        assertEquals(5, parent.getRowSpan());
    }

    @Test
    void down_unmergedParent_singleRowBlock_staysSameAsBefore() {
        Cell parent = new Cell();
        CellDownDuplicator duplicator = new CellDownDuplicator(parent, DuplicateType.IncreseSpan, 1);
        DownDuplicate down = new DownDuplicate(new Cell(), 1, null);

        duplicator.duplicate(down, null);
        assertEquals(2, parent.getRowSpan());
        duplicator.duplicate(down, null);
        assertEquals(3, parent.getRowSpan());
    }

    @Test
    void down_alreadyMergedParent_onlyAddsBlockSize() {
        Cell parent = new Cell();
        parent.setRowSpan(2);
        CellDownDuplicator duplicator = new CellDownDuplicator(parent, DuplicateType.IncreseSpan, 1);
        DownDuplicate down = new DownDuplicate(new Cell(), 2, null);

        duplicator.duplicate(down, null);
        assertEquals(4, parent.getRowSpan());
    }

    @Test
    void right_unmergedParent_wideBlock_countsOriginalColumn() {
        Cell parent = new Cell();
        CellRightDuplicator duplicator = new CellRightDuplicator(parent, DuplicateType.IncreseSpan, 1);
        RightDuplicate right = new RightDuplicate(new Cell(), 2, null);

        duplicator.duplicate(right, null);
        assertEquals(3, parent.getColSpan());
        duplicator.duplicate(right, null);
        assertEquals(5, parent.getColSpan());
    }
}
