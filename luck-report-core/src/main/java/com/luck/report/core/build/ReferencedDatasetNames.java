package com.luck.report.core.build;

import com.luck.report.core.chart.Chart;
import com.luck.report.core.chart.dataset.Dataset;
import com.luck.report.core.chart.dataset.impl.BubbleDataset;
import com.luck.report.core.chart.dataset.impl.MixDataset;
import com.luck.report.core.chart.dataset.impl.ScatterDataset;
import com.luck.report.core.chart.dataset.impl.category.BarDataset;
import com.luck.report.core.chart.dataset.impl.category.CategoryDataset;
import com.luck.report.core.chart.dataset.impl.category.LineDataset;
import com.luck.report.core.definition.CellDefinition;
import com.luck.report.core.definition.ReportDefinition;
import com.luck.report.core.definition.mapping.MappingType;
import com.luck.report.core.definition.value.ChartValue;
import com.luck.report.core.definition.value.DatasetValue;
import com.luck.report.core.definition.value.ExpressionValue;
import com.luck.report.core.definition.value.Value;
import com.luck.report.core.expression.model.Expression;
import com.luck.report.core.expression.model.expr.BaseExpression;
import com.luck.report.core.expression.model.expr.ExpressionBlock;
import com.luck.report.core.expression.model.expr.FunctionExpression;
import com.luck.report.core.expression.model.expr.JoinExpression;
import com.luck.report.core.expression.model.expr.dataset.DatasetExpression;
import com.luck.report.core.expression.model.expr.ifelse.ElseExpression;
import com.luck.report.core.expression.model.expr.ifelse.ElseIfExpression;
import com.luck.report.core.expression.model.expr.ifelse.IfExpression;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 收集报表定义中单元格实际引用的数据集名（含映射集、图表），不含查询表单选项。
 */
public final class ReferencedDatasetNames {

	private ReferencedDatasetNames() {
	}

	/**
	 * 从报表定义收集计算期需要构建的数据集名
	 *
	 * @param reportDefinition 报表定义，可空
	 * @return 引用名集合；无引用时为空集合（非 null）
	 */
	public static Set<String> collect(ReportDefinition reportDefinition) {
		Set<String> names = new HashSet<String>();
		if (reportDefinition == null || reportDefinition.getCells() == null) {
			return names;
		}
		for (CellDefinition cellDef : reportDefinition.getCells()) {
			if (cellDef == null) {
				continue;
			}
			collectFromValue(cellDef.getValue(), names);
		}
		return names;
	}

	/**
	 * 从单元格值收集数据集名
	 *
	 * @param value 单元格值，可空
	 * @param names 输出集合，非空
	 */
	public static void collectFromValue(Value value, Set<String> names) {
		if (value == null || names == null) {
			return;
		}
		if (value instanceof DatasetValue) {
			collectFromDatasetExpression((DatasetExpression) value, names);
		} else if (value instanceof ExpressionValue) {
			collectFromExpression(((ExpressionValue) value).getExpression(), names);
		} else if (value instanceof ChartValue) {
			Chart chart = ((ChartValue) value).getChart();
			if (chart != null) {
				collectFromChartDataset(chart.getDataset(), names);
			}
		}
	}

	private static void collectFromDatasetExpression(DatasetExpression expr, Set<String> names) {
		if (expr == null) {
			return;
		}
		if (StringUtils.isNotBlank(expr.getDatasetName())) {
			names.add(expr.getDatasetName());
		}
		if (MappingType.dataset.equals(expr.getMappingType()) && StringUtils.isNotBlank(expr.getMappingDataset())) {
			names.add(expr.getMappingDataset());
		}
	}

	private static void collectFromExpression(Expression expr, Set<String> names) {
		if (expr == null) {
			return;
		}
		if (expr instanceof DatasetExpression) {
			collectFromDatasetExpression((DatasetExpression) expr, names);
			return;
		}
		if (expr instanceof ExpressionBlock) {
			ExpressionBlock block = (ExpressionBlock) expr;
			List<Expression> list = block.getExpressionList();
			if (list != null) {
				for (Expression child : list) {
					collectFromExpression(child, names);
				}
			}
			collectFromExpression(block.getReturnExpression(), names);
			return;
		}
		if (expr instanceof JoinExpression) {
			List<BaseExpression> list = ((JoinExpression) expr).getExpressions();
			if (list != null) {
				for (BaseExpression child : list) {
					collectFromExpression(child, names);
				}
			}
			return;
		}
		if (expr instanceof FunctionExpression) {
			List<BaseExpression> list = ((FunctionExpression) expr).getExpressions();
			if (list != null) {
				for (BaseExpression child : list) {
					collectFromExpression(child, names);
				}
			}
			return;
		}
		if (expr instanceof IfExpression) {
			IfExpression ifExpr = (IfExpression) expr;
			if (ifExpr.getExpression() != null) {
				collectFromExpression(ifExpr.getExpression(), names);
			}
			List<ElseIfExpression> elseIfList = ifExpr.getElseIfExpressions();
			if (elseIfList != null) {
				for (ElseIfExpression elseIf : elseIfList) {
					if (elseIf != null && elseIf.getExpression() != null) {
						collectFromExpression(elseIf.getExpression(), names);
					}
				}
			}
			ElseExpression elseExpr = ifExpr.getElseExpression();
			if (elseExpr != null && elseExpr.getExpression() != null) {
				collectFromExpression(elseExpr.getExpression(), names);
			}
		}
	}

	private static void collectFromChartDataset(Dataset dataset, Set<String> names) {
		if (dataset == null) {
			return;
		}
		if (dataset instanceof MixDataset) {
			MixDataset mix = (MixDataset) dataset;
			if (mix.getBarDatasets() != null) {
				for (BarDataset bar : mix.getBarDatasets()) {
					addCategoryDatasetName(bar, names);
				}
			}
			if (mix.getLineDatasets() != null) {
				for (LineDataset line : mix.getLineDatasets()) {
					addCategoryDatasetName(line, names);
				}
			}
			return;
		}
		if (dataset instanceof CategoryDataset) {
			addCategoryDatasetName((CategoryDataset) dataset, names);
			return;
		}
		if (dataset instanceof ScatterDataset) {
			String name = ((ScatterDataset) dataset).getDatasetName();
			if (StringUtils.isNotBlank(name)) {
				names.add(name);
			}
			return;
		}
		if (dataset instanceof BubbleDataset) {
			String name = ((BubbleDataset) dataset).getDatasetName();
			if (StringUtils.isNotBlank(name)) {
				names.add(name);
			}
		}
	}

	private static void addCategoryDatasetName(CategoryDataset dataset, Set<String> names) {
		if (dataset == null) {
			return;
		}
		String name = dataset.getDatasetName();
		if (StringUtils.isNotBlank(name)) {
			names.add(name);
		}
	}
}
