package com.luck.report.core.build;

import com.luck.report.core.definition.ConditionPaging;
import com.luck.report.core.definition.ConditionPropertyItem;
import com.luck.report.core.expression.model.Condition;
import com.luck.report.core.expression.model.condition.BothExpressionCondition;
import com.luck.report.core.expression.model.condition.CellExpressionCondition;
import com.luck.report.core.expression.model.condition.CurrentValueExpressionCondition;
import com.luck.report.core.expression.model.condition.PropertyExpressionCondition;
import com.luck.report.core.model.Cell;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 判断条件属性是否必须后置到展开结束后再算（判定偏保守）。
 */
public final class ConditionPropertyDeferral {

	private ConditionPropertyDeferral() {
	}

	/**
	 * 是否必须进入 lazy 后置列表
	 *
	 * @param cell 单元格，非空
	 * @return true 表示须等全部展开后再 doCompute
	 */
	public static boolean mustDefer(Cell cell) {
		if (cell == null) {
			return false;
		}
		if (cell.isExistPageFunction()) {
			return true;
		}
		List<ConditionPropertyItem> items = cell.getConditionPropertyItems();
		if (items == null || items.isEmpty()) {
			return false;
		}
		for (ConditionPropertyItem item : items) {
			if (item == null) {
				continue;
			}
			ConditionPaging paging = item.getPaging();
			if (paging != null) {
				return true;
			}
			if (mustDeferCondition(item.getCondition())) {
				return true;
			}
		}
		return false;
	}

	private static boolean mustDeferCondition(Condition condition) {
		if (condition == null) {
			return false;
		}
		if (condition instanceof CellExpressionCondition) {
			return true;
		}
		if (condition instanceof PropertyExpressionCondition || condition instanceof CurrentValueExpressionCondition) {
			return false;
		}
		if (condition instanceof BothExpressionCondition) {
			BothExpressionCondition both = (BothExpressionCondition) condition;
			String left = both.getLeft();
			if (!"#".equals(StringUtils.trimToEmpty(left))) {
				return true;
			}
			List<String> cellNames = both.fetchCellName();
			return cellNames != null && !cellNames.isEmpty();
		}
		return true;
	}
}
