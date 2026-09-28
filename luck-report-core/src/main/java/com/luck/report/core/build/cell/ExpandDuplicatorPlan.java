package com.luck.report.core.build.cell;

import com.luck.report.core.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 某模板格的向下或向右复制计划（只含名字与相对偏移）。
 */
public class ExpandDuplicatorPlan {

	private final String mainCellName;
	private final List<ExpandDuplicatorEntry> entries;
	private final Range relativeDuplicateRange;

	/**
	 * 构造复制计划
	 *
	 * @param mainCellName 主扩展格名字
	 * @param entries 条目列表，顺序与现扫描一致
	 * @param relativeDuplicateRange 与 Cell.duplicateRange 相同的相对区间
	 */
	public ExpandDuplicatorPlan(String mainCellName, List<ExpandDuplicatorEntry> entries,
			Range relativeDuplicateRange) {
		this.mainCellName = mainCellName;
		this.entries = Collections.unmodifiableList(new ArrayList<ExpandDuplicatorEntry>(entries));
		this.relativeDuplicateRange = relativeDuplicateRange;
	}

	public String getMainCellName() {
		return mainCellName;
	}

	public List<ExpandDuplicatorEntry> getEntries() {
		return entries;
	}

	public Range getRelativeDuplicateRange() {
		return relativeDuplicateRange;
	}
}
