package com.luck.report.core.build.cell;

import com.luck.report.core.Range;
import com.luck.report.core.Utils;
import com.luck.report.core.build.Context;
import com.luck.report.core.build.cell.down.CellDownDuplicator;
import com.luck.report.core.build.cell.down.DownDuplocatorWrapper;
import com.luck.report.core.build.cell.right.CellRightDuplicator;
import com.luck.report.core.build.cell.right.RightDuplocatorWrapper;
import com.luck.report.core.definition.BlankCellInfo;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Column;
import com.luck.report.core.model.Row;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 从运行期扫描生成展开复制计划，并绑定为 Down/Right DuplocatorWrapper。
 */
public final class ExpandDuplicatorPlans {

	private ExpandDuplicatorPlans() {
	}

	/**
	 * 按当前主格与绝对行区间生成向下复制计划（规则与旧 buildCellDownDuplicator 一致）
	 *
	 * @param mainCell 主扩展格，非空
	 * @param context 计算上下文，非空
	 * @param absoluteRowRange 绝对行号区间，非空
	 * @return 复制计划
	 */
	public static ExpandDuplicatorPlan buildDown(Cell mainCell, Context context, Range absoluteRowRange) {
		List<ExpandDuplicatorEntry> entries = new ArrayList<ExpandDuplicatorEntry>();
		Set<Cell> addedNonDuplicateCells = new HashSet<Cell>();
		int mainRow = mainCell.getRow().getRowNumber();
		int mainCol = mainCell.getColumn().getColumnNumber();
		collectParentDownEntries(mainCell, mainCell, entries, addedNonDuplicateCells);
		for (int i = absoluteRowRange.getStart(); i <= absoluteRowRange.getEnd(); i++) {
			Row row = context.getRow(i);
			if (row == null) {
				continue;
			}
			for (Cell rowCell : row.getCells()) {
				appendDownEntry(entries, addedNonDuplicateCells, mainCell, rowCell,
						i - mainRow, rowCell.getColumn().getColumnNumber() - mainCol, false);
			}
		}
		return new ExpandDuplicatorPlan(mainCell.getName(), entries, mainCell.getDuplicateRange());
	}

	/**
	 * 按当前主格与绝对列区间生成向右复制计划
	 *
	 * @param mainCell 主扩展格，非空
	 * @param context 计算上下文，非空
	 * @param absoluteColRange 绝对列号区间，非空
	 * @return 复制计划
	 */
	public static ExpandDuplicatorPlan buildRight(Cell mainCell, Context context, Range absoluteColRange) {
		List<ExpandDuplicatorEntry> entries = new ArrayList<ExpandDuplicatorEntry>();
		Set<Cell> addedNonDuplicateCells = new HashSet<Cell>();
		int mainRow = mainCell.getRow().getRowNumber();
		int mainCol = mainCell.getColumn().getColumnNumber();
		collectParentRightEntries(mainCell, mainCell, entries, addedNonDuplicateCells);
		for (int i = absoluteColRange.getStart(); i <= absoluteColRange.getEnd(); i++) {
			Column col = context.getColumn(i);
			if (col == null) {
				continue;
			}
			for (Cell colCell : col.getCells()) {
				appendRightEntry(entries, addedNonDuplicateCells, mainCell, colCell,
						colCell.getRow().getRowNumber() - mainRow, i - mainCol, false);
			}
		}
		return new ExpandDuplicatorPlan(mainCell.getName(), entries, mainCell.getDuplicateRange());
	}

	/**
	 * 将向下计划绑定为当前实例的 DownDuplocatorWrapper；找不到格子时返回 null
	 *
	 * @param plan 计划，非空
	 * @param mainCell 当前主格实例，非空
	 * @param context 计算上下文，非空
	 * @return wrapper；绑定失败返回 null
	 */
	public static DownDuplocatorWrapper bindDown(ExpandDuplicatorPlan plan, Cell mainCell, Context context) {
		DownDuplocatorWrapper wrapper = new DownDuplocatorWrapper(mainCell.getName());
		int mainRow = mainCell.getRow().getRowNumber();
		int mainCol = mainCell.getColumn().getColumnNumber();
		for (ExpandDuplicatorEntry entry : plan.getEntries()) {
			Cell target = resolveDownCell(entry, mainCell, context, mainRow, mainCol);
			if (target == null) {
				Utils.logToConsole("[ExpandDuplicatorPlans] bindDown miss cell=" + entry.getCellName()
						+ " main=" + mainCell.getName());
				return null;
			}
			int rowNumber = entry.isFromParentChain() ? 0 : mainRow + entry.getRelativeRowOffset();
			addDownDuplicator(wrapper, target, entry, rowNumber);
		}
		return wrapper;
	}

	/**
	 * 将向右计划绑定为当前实例的 RightDuplocatorWrapper
	 *
	 * @param plan 计划，非空
	 * @param mainCell 当前主格实例，非空
	 * @param context 计算上下文，非空
	 * @return wrapper；绑定失败返回 null
	 */
	public static RightDuplocatorWrapper bindRight(ExpandDuplicatorPlan plan, Cell mainCell, Context context) {
		RightDuplocatorWrapper wrapper = new RightDuplocatorWrapper(mainCell.getName());
		int mainRow = mainCell.getRow().getRowNumber();
		int mainCol = mainCell.getColumn().getColumnNumber();
		for (ExpandDuplicatorEntry entry : plan.getEntries()) {
			Cell target = resolveRightCell(entry, mainCell, context, mainRow, mainCol);
			if (target == null) {
				Utils.logToConsole("[ExpandDuplicatorPlans] bindRight miss cell=" + entry.getCellName()
						+ " main=" + mainCell.getName());
				return null;
			}
			int colNumber = entry.isFromParentChain() ? 0 : mainCol + entry.getRelativeColOffset();
			addRightDuplicator(wrapper, target, entry, colNumber);
		}
		return wrapper;
	}

	private static void collectParentDownEntries(Cell cell, Cell mainCell, List<ExpandDuplicatorEntry> entries,
			Set<Cell> addedNonDuplicateCells) {
		Cell leftParentCell = cell.getLeftParentCell();
		if (leftParentCell == null) {
			return;
		}
		appendDownEntry(entries, addedNonDuplicateCells, mainCell, leftParentCell, 0, 0, true);
		collectParentDownEntries(leftParentCell, mainCell, entries, addedNonDuplicateCells);
	}

	private static void collectParentRightEntries(Cell cell, Cell mainCell, List<ExpandDuplicatorEntry> entries,
			Set<Cell> addedNonDuplicateCells) {
		Cell topParentCell = cell.getTopParentCell();
		if (topParentCell == null) {
			return;
		}
		appendRightEntry(entries, addedNonDuplicateCells, mainCell, topParentCell, 0, 0, true);
		collectParentRightEntries(topParentCell, mainCell, entries, addedNonDuplicateCells);
	}

	private static void appendDownEntry(List<ExpandDuplicatorEntry> entries, Set<Cell> addedNonDuplicateCells,
			Cell mainCell, Cell currentCell, int relativeRowOffset, int relativeColOffset, boolean fromParentChain) {
		if (currentCell == mainCell) {
			return;
		}
		ExpandDuplicatorEntry entry = classify(mainCell, currentCell, relativeRowOffset, relativeColOffset,
				fromParentChain);
		if (entry == null) {
			return;
		}
		if (entry.getType() != DuplicateType.Duplicate && entry.getType() != DuplicateType.Self) {
			if (addedNonDuplicateCells.contains(currentCell)) {
				return;
			}
			addedNonDuplicateCells.add(currentCell);
		}
		entries.add(entry);
	}

	private static void appendRightEntry(List<ExpandDuplicatorEntry> entries, Set<Cell> addedNonDuplicateCells,
			Cell mainCell, Cell currentCell, int relativeRowOffset, int relativeColOffset, boolean fromParentChain) {
		if (mainCell.equals(currentCell)) {
			return;
		}
		ExpandDuplicatorEntry entry = classify(mainCell, currentCell, relativeRowOffset, relativeColOffset,
				fromParentChain);
		if (entry == null) {
			return;
		}
		if (entry.getType() != DuplicateType.Duplicate && entry.getType() != DuplicateType.Self) {
			if (addedNonDuplicateCells.contains(currentCell)) {
				return;
			}
			addedNonDuplicateCells.add(currentCell);
		}
		entries.add(entry);
	}

	private static ExpandDuplicatorEntry classify(Cell mainCell, Cell currentCell, int relativeRowOffset,
			int relativeColOffset, boolean fromParentChain) {
		String name = currentCell.getName();
		Map<String, BlankCellInfo> newBlankCellNamesMap = mainCell.getNewBlankCellsMap();
		Set<String> increaseCellNames = mainCell.getIncreaseSpanCellNames();
		Set<String> newCellNames = mainCell.getNewCellNames();
		if (newBlankCellNamesMap.containsKey(name)) {
			return new ExpandDuplicatorEntry(relativeRowOffset, relativeColOffset, name, DuplicateType.Blank,
					newBlankCellNamesMap.get(name), fromParentChain);
		}
		if (increaseCellNames.contains(name)) {
			return new ExpandDuplicatorEntry(relativeRowOffset, relativeColOffset, name, DuplicateType.IncreseSpan,
					null, fromParentChain);
		}
		if (newCellNames.contains(name)) {
			return new ExpandDuplicatorEntry(relativeRowOffset, relativeColOffset, name, DuplicateType.Duplicate,
					null, fromParentChain);
		}
		if (mainCell.getName().equals(name)) {
			return new ExpandDuplicatorEntry(relativeRowOffset, relativeColOffset, name, DuplicateType.Self, null,
					fromParentChain);
		}
		return null;
	}

	private static Cell resolveDownCell(ExpandDuplicatorEntry entry, Cell mainCell, Context context, int mainRow,
			int mainCol) {
		if (entry.isFromParentChain()) {
			return findLeftParentByName(mainCell, entry.getCellName());
		}
		Row row = context.getRow(mainRow + entry.getRelativeRowOffset());
		if (row == null) {
			return null;
		}
		int targetCol = mainCol + entry.getRelativeColOffset();
		return findCellByNameAndColumn(row.getCells(), entry.getCellName(), targetCol);
	}

	private static Cell resolveRightCell(ExpandDuplicatorEntry entry, Cell mainCell, Context context, int mainRow,
			int mainCol) {
		if (entry.isFromParentChain()) {
			return findTopParentByName(mainCell, entry.getCellName());
		}
		Column col = context.getColumn(mainCol + entry.getRelativeColOffset());
		if (col == null) {
			return null;
		}
		int targetRow = mainRow + entry.getRelativeRowOffset();
		return findCellByNameAndRow(col.getCells(), entry.getCellName(), targetRow);
	}

	private static Cell findLeftParentByName(Cell cell, String name) {
		Cell p = cell.getLeftParentCell();
		while (p != null) {
			if (name.equals(p.getName())) {
				return p;
			}
			p = p.getLeftParentCell();
		}
		return null;
	}

	private static Cell findTopParentByName(Cell cell, String name) {
		Cell p = cell.getTopParentCell();
		while (p != null) {
			if (name.equals(p.getName())) {
				return p;
			}
			p = p.getTopParentCell();
		}
		return null;
	}

	private static Cell findCellByNameAndColumn(List<Cell> cells, String name, int columnNumber) {
		for (Cell c : cells) {
			if (name.equals(c.getName()) && c.getColumn().getColumnNumber() == columnNumber) {
				return c;
			}
		}
		return null;
	}

	private static Cell findCellByNameAndRow(List<Cell> cells, String name, int rowNumber) {
		for (Cell c : cells) {
			if (name.equals(c.getName()) && c.getRow().getRowNumber() == rowNumber) {
				return c;
			}
		}
		return null;
	}

	private static void addDownDuplicator(DownDuplocatorWrapper wrapper, Cell target, ExpandDuplicatorEntry entry,
			int rowNumber) {
		DuplicateType type = entry.getType();
		CellDownDuplicator duplicator;
		if (type == DuplicateType.Blank) {
			if (wrapper.contains(target)) {
				return;
			}
			duplicator = new CellDownDuplicator(target, type, entry.getBlankCellInfo(), rowNumber);
		} else if (type == DuplicateType.IncreseSpan) {
			if (wrapper.contains(target)) {
				return;
			}
			duplicator = new CellDownDuplicator(target, type, rowNumber);
		} else {
			duplicator = new CellDownDuplicator(target, type, rowNumber);
		}
		wrapper.addCellDownDuplicator(duplicator);
	}

	private static void addRightDuplicator(RightDuplocatorWrapper wrapper, Cell target, ExpandDuplicatorEntry entry,
			int colNumber) {
		DuplicateType type = entry.getType();
		CellRightDuplicator duplicator;
		if (type == DuplicateType.Blank) {
			if (wrapper.contains(target)) {
				return;
			}
			duplicator = new CellRightDuplicator(target, type, entry.getBlankCellInfo(), colNumber);
		} else if (type == DuplicateType.IncreseSpan) {
			if (wrapper.contains(target)) {
				return;
			}
			duplicator = new CellRightDuplicator(target, type, colNumber);
		} else {
			duplicator = new CellRightDuplicator(target, type, colNumber);
		}
		wrapper.addCellRightDuplicator(duplicator);
	}
}
