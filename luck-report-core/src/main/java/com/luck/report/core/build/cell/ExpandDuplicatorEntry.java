package com.luck.report.core.build.cell;

import com.luck.report.core.definition.BlankCellInfo;

/**
 * 展开复制计划中的单条描述（相对行/列偏移 + 类型，不含运行期 Cell）。
 */
public class ExpandDuplicatorEntry {

	private final int relativeRowOffset;
	private final int relativeColOffset;
	private final String cellName;
	private final DuplicateType type;
	private final BlankCellInfo blankCellInfo;
	private final boolean fromParentChain;

	/**
	 * 构造一条复制计划条目
	 *
	 * @param relativeRowOffset 相对主格行号的偏移；父链条目为 0
	 * @param relativeColOffset 相对主格列号的偏移；父链条目为 0
	 * @param cellName 格子名，非空
	 * @param type 复制类型，非空
	 * @param blankCellInfo 仅 Blank 时非空
	 * @param fromParentChain 是否来自左/上父格链扫描
	 */
	public ExpandDuplicatorEntry(int relativeRowOffset, int relativeColOffset, String cellName, DuplicateType type,
			BlankCellInfo blankCellInfo, boolean fromParentChain) {
		this.relativeRowOffset = relativeRowOffset;
		this.relativeColOffset = relativeColOffset;
		this.cellName = cellName;
		this.type = type;
		this.blankCellInfo = blankCellInfo;
		this.fromParentChain = fromParentChain;
	}

	public int getRelativeRowOffset() {
		return relativeRowOffset;
	}

	public int getRelativeColOffset() {
		return relativeColOffset;
	}

	/**
	 * 向下展开时的主偏移（行）；向右展开时请用 {@link #getRelativeColOffset()}
	 *
	 * @return 相对行偏移
	 */
	public int getRelativeOffset() {
		return relativeRowOffset;
	}

	public String getCellName() {
		return cellName;
	}

	public DuplicateType getType() {
		return type;
	}

	public BlankCellInfo getBlankCellInfo() {
		return blankCellInfo;
	}

	public boolean isFromParentChain() {
		return fromParentChain;
	}
}
