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
package com.luck.report.core.build;

import com.luck.report.core.Utils;
import com.luck.report.core.build.cell.CellBuilder;
import com.luck.report.core.build.cell.NoneExpandBuilder;
import com.luck.report.core.build.cell.down.DownExpandBuilder;
import com.luck.report.core.build.cell.right.RightExpandBuilder;
import com.luck.report.core.build.paging.Page;
import com.luck.report.core.build.paging.PagingBuilder;
import com.luck.report.core.definition.*;
import com.luck.report.core.definition.dataset.DatasetDefinition;
import com.luck.report.core.definition.datasource.*;
import com.luck.report.core.exception.ReportComputeException;
import com.luck.report.core.exception.ReportException;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Column;
import com.luck.report.core.model.Report;
import com.luck.report.core.model.Row;
import com.luck.report.core.utils.ImageLoadStats;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * @author Jacky.gao
 * @since 2016年11月1日
 */
public class ReportBuilder implements ApplicationContextAware {
	private Logger log = Logger.getGlobal();
	private ApplicationContext applicationContext;
	private Map<String, DatasourceProvider> datasourceProviderMap = new HashMap<String, DatasourceProvider>();
	private Map<Expand, CellBuilder> cellBuildersMap = new HashMap<Expand, CellBuilder>();
	private NoneExpandBuilder noneExpandBuilder = new NoneExpandBuilder();
	private HideRowColumnBuilder hideRowColumnBuilder;

	public ReportBuilder() {
		cellBuildersMap.put(Expand.Right, new RightExpandBuilder());
		cellBuildersMap.put(Expand.Down, new DownExpandBuilder());
		cellBuildersMap.put(Expand.None, noneExpandBuilder);
	}

	/**
	 * 构建报表（默认分页）
	 *
	 * @param reportDefinition 报表定义，非空
	 * @param parameters 参数，可空
	 * @return 计算后的报表实例
	 */
	public Report buildReport(ReportDefinition reportDefinition, Map<String, Object> parameters) {
		return buildReport(reportDefinition, parameters, true);
	}

	/**
	 * 构建报表
	 *
	 * @param reportDefinition 报表定义，非空
	 * @param parameters 参数，可空
	 * @param doPaging 是否在计算末尾分页；false 时不写入 pages
	 * @return 计算后的报表实例
	 */
	public Report buildReport(ReportDefinition reportDefinition, Map<String, Object> parameters, boolean doPaging) {
		ImageLoadStats.begin();
		try {
			ReportComputeTiming timing = new ReportComputeTiming();
			Report report = reportDefinition.newReport();
			timing.markStart();
			Map<String, Dataset> datasetMap = buildDatasets(reportDefinition, parameters, applicationContext);
			timing.endDataset();
			Context context = new Context(this, report, datasetMap, applicationContext, parameters, hideRowColumnBuilder);
			timing.markStart();
			List<Cell> cells = new ArrayList<Cell>();
			cells.add(report.getRootCell());
			do {
				buildCell(context, cells);
				cells = context.nextUnprocessedCells();
			} while (cells != null);
			doFillBlankRows(report, context);
			timing.endCompute();
			recomputeCells(report, context, doPaging, timing);
			timing.log();
			return report;
		} finally {
			ImageLoadStats.end();
		}
	}

	/**
	 * 计算一批同名格子；表达式计算期间该名字视为尚未完成
	 *
	 * @param context 本次报表计算上下文，非空
	 * @param cells 待计算格子；null 时从待算队列取下一批
	 */
	public void buildCell(Context context, List<Cell> cells) {
		if (cells == null) {
			cells = context.nextUnprocessedCells();
		}
		if (cells == null) {
			return;
		}
		for (Cell cell : cells) {
			try {
				List<BindData> dataList;
				context.markBuilding(cell.getName());
				try {
					dataList = context.buildCellData(cell);
				} finally {
					context.unmarkBuilding(cell.getName());
				}
				cell.setProcessed(true);
				int size = dataList.size();
				Cell lastCell = cell;
				boolean titleRow = cell.getRow() != null && Band.title.equals(cell.getRow().getBand());
				if (titleRow && size > 1) {
					dataList = dataList.subList(0, 1);
					size = 1;
				}
				if (size == 1) {
					lastCell = noneExpandBuilder.buildCell(dataList, cell, context);
				} else if (size > 1) {
					CellBuilder cellBuilder = cellBuildersMap.get(cell.getExpand());
					lastCell = cellBuilder.buildCell(dataList, cell, context);
				}
				if (lastCell.isFillBlankRows() && lastCell.getMultiple() > 0) {
					int result = size % lastCell.getMultiple();
					if (result > 0) {
						int value = lastCell.getMultiple() - result;
						context.addFillBlankRow(lastCell.getRow(), value);
					}
				}
			} catch (Exception e) {
				String errMsg = String.format("Cell calculation exception - Cell name: %s, Row number: %d, Column number: %d",
						cell.getName(), cell.getRow().getRowNumber(), cell.getColumn().getColumnNumber());
				log.info(errMsg);
				throw e;
			}
		}
	}

	private Map<String, Dataset> buildDatasets(ReportDefinition reportDefinition, Map<String, Object> parameters, ApplicationContext applicationContext) {
		Map<String, Dataset> datasetMap = new HashMap<String, Dataset>();
		List<DatasourceDefinition> datasources = reportDefinition.getDatasources();
		if (datasources == null) {
			return datasetMap;
		}
		Set<String> allowedNames = ReferencedDatasetNames.collect(reportDefinition);
		for (DatasourceDefinition dsDef : datasources) {
			if (!datasourceHasAllowedDataset(dsDef, allowedNames)) {
				continue;
			}
			if (dsDef instanceof JdbcDatasourceDefinition) {
				String dsName = dsDef.getName();
				Connection conn = null;
				try {
					if (datasourceProviderMap.containsKey(dsName)) {
						conn = datasourceProviderMap.get(dsName).getConnection();
					}
					JdbcDatasourceDefinition ds = (JdbcDatasourceDefinition) dsDef;
					List<Dataset> ls = ds.buildDatasets(conn, parameters, allowedNames);
					if (ls != null) {
						for (Dataset dataset : ls) {
							datasetMap.put(dataset.getName(), dataset);
						}
					}
				} finally {
					if (conn != null) {
						try {
							conn.close();
						} catch (SQLException e) {
						}
					}
				}
			} else if (dsDef instanceof SpringBeanDatasourceDefinition) {
				SpringBeanDatasourceDefinition ds = (SpringBeanDatasourceDefinition) dsDef;
				List<Dataset> ls = ds.getDatasets(applicationContext, parameters, allowedNames);
				if (ls != null) {
					for (Dataset dataset : ls) {
						datasetMap.put(dataset.getName(), dataset);
					}
				}
			} else if (dsDef instanceof BuildinDatasourceDefinition) {
				String dsName = dsDef.getName();
				Connection conn = null;
				try {
					if (datasourceProviderMap.containsKey(dsName)) {
						conn = datasourceProviderMap.get(dsName).getConnection();
					}
					for (BuildinDatasource datasource : Utils.getBuildinDatasources()) {
						if (datasource.name().equals(dsName)) {
							conn = datasource.getConnection();
							break;
						}
					}
					if (conn == null) {
						throw new ReportComputeException("Buildin datasource [" + dsName + "] not exist.");
					}
					BuildinDatasourceDefinition ds = (BuildinDatasourceDefinition) dsDef;
					List<Dataset> ls = ds.buildDatasets(conn, parameters, allowedNames);
					if (ls != null) {
						for (Dataset dataset : ls) {
							datasetMap.put(dataset.getName(), dataset);
						}
					}
				} finally {
					if (conn != null) {
						try {
							conn.close();
						} catch (SQLException e) {
						}
					}
				}
			} else if (dsDef instanceof StaticDatasourceDefinition) {
				StaticDatasourceDefinition ds = (StaticDatasourceDefinition) dsDef;
				List<Dataset> ls = ds.buildDatasets(ds.getDatasets(), allowedNames);
				if (ls != null) {
					for (Dataset dataset : ls) {
						datasetMap.put(dataset.getName(), dataset);
					}
				}
			}
		}
		return datasetMap;
	}

	/**
	 * 判断数据源下是否存在允许构建的数据集
	 *
	 * @param dsDef 数据源定义，非空
	 * @param allowedNames 允许的数据集名，非空（可为空集合）
	 * @return 存在至少一个命中时返回 true
	 */
	private boolean datasourceHasAllowedDataset(DatasourceDefinition dsDef, Set<String> allowedNames) {
		List<DatasetDefinition> defs = dsDef.getDatasets();
		if (defs == null || defs.isEmpty() || allowedNames == null || allowedNames.isEmpty()) {
			return false;
		}
		for (DatasetDefinition def : defs) {
			if (allowedNames.contains(def.getName())) {
				return true;
			}
		}
		return false;
	}

	private void doFillBlankRows(Report report, Context context) {
		Map<Row, Integer> map = context.getFillBlankRowsMap();
		List<Row> newRowList = new ArrayList<Row>();
		for (Row row : map.keySet()) {
			int size = map.get(row);
			Row lastRow = findLastRow(row, report);
			for (int i = 0; i < size; i++) {
				Row newRow = buildNewRow(lastRow, report);
				newRowList.add(newRow);
			}
			int rowNumber = lastRow.getRowNumber();
			if (newRowList.size() > 0) {
				report.insertRows(rowNumber + 1, newRowList);
				newRowList.clear();
			}
		}
	}

	private Row buildNewRow(Row row, Report report) {
		Row newRow = row.newRow();
		newRow.setBand(null);
		List<Row> rows = report.getRows();
		List<Column> columns = report.getColumns();
		int start = -1, colSize = columns.size();
		Map<Row, Map<Column, Cell>> rowMap = report.getRowColCellMap();
		Map<Column, Cell> newCellMap = new HashMap<Column, Cell>();
		rowMap.put(newRow, newCellMap);

		Map<Column, Cell> colMap = rowMap.get(row);
		for (int index = 0; index < colSize; index++) {
			Column column = columns.get(index);
			Cell currentCell = colMap.get(column);
			if (currentCell == null) {
				if (start == -1) {
					start = row.getRowNumber() - 2;
				}
				for (int i = start; i > -1; i--) {
					Row currentRow = rows.get(i);
					Map<Column, Cell> prevColMap = rowMap.get(currentRow);
					if (prevColMap == null) {
						continue;
					}
					if (prevColMap.containsKey(column)) {
						currentCell = prevColMap.get(column);
						break;
					}
				}
			}
			if (currentCell == null) {
				throw new ReportException("Insert blank rows fail.");
			}
			int colSpan = currentCell.getColSpan();
			if (colSpan > 0) {
				colSpan--;
				index += colSpan;
			}
			int rowSpan = currentCell.getRowSpan();
			if (rowSpan > 1) {
				currentCell.setRowSpan(rowSpan + 1);
			} else {
				Cell newCell = newBlankCell(currentCell, column, report);
				newCell.setRow(newRow);
				newRow.getCells().add(newCell);
				newCellMap.put(newCell.getColumn(), newCell);
			}
		}
		return newRow;
	}

	private Row findLastRow(Row row, Report report) {
		List<Row> rows = report.getRows();
		List<Cell> cells = row.getCells();
		Row lastRow = row;
		int span = 0;
		for (Cell cell : cells) {
			int rowSpan = cell.getRowSpan();
			if (rowSpan < 2) {
				continue;
			}
			if (span == 0) {
				span = rowSpan;
			} else if (rowSpan > span) {
				span = rowSpan;
			}
		}
		if (span > 1) {
			int rowIndex = row.getRowNumber() - 1 + span - 1;
			lastRow = rows.get(rowIndex);
		}
		return lastRow;
	}

	private Cell newBlankCell(Cell cell, Column column, Report report) {
		Cell newCell = new Cell();
		newCell.setData("");
		newCell.setColSpan(cell.getColSpan());
		newCell.setConditionPropertyItems(cell.getConditionPropertyItems());
		report.addLazyCell(newCell);
		newCell.setCellStyle(cell.getCellStyle());
		newCell.setName(cell.getName());
		newCell.setColumn(column);
		column.getCells().add(newCell);
		Cell leftParent = cell.getLeftParentCell();
		if (leftParent != null) {
			newCell.setLeftParentCell(leftParent);
			leftParent.addRowChild(newCell);
		}
		Cell topParent = cell.getTopParentCell();
		if (topParent != null) {
			newCell.setTopParentCell(topParent);
			topParent.addColumnChild(newCell);
		}
		return newCell;
	}

	private void recomputeCells(Report report, Context context, boolean doPaging, ReportComputeTiming timing) {
		timing.markStart();
		List<Cell> lazyCells = report.getLazyComputeCells();
		for (Cell cell : lazyCells) {
			cell.doCompute(context);
		}
		timing.endLazy();
		if (!doPaging) {
			return;
		}
		timing.markStart();
		context.setDoPaging(true);
		List<Page> pages = PagingBuilder.buildPages(report);
		report.setPages(pages);
		timing.endPaging();
	}

	public void setHideRowColumnBuilder(HideRowColumnBuilder hideRowColumnBuilder) {
		this.hideRowColumnBuilder = hideRowColumnBuilder;
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
		Collection<DatasourceProvider> datasourceProviders = applicationContext.getBeansOfType(DatasourceProvider.class).values();
		for (DatasourceProvider dp : datasourceProviders) {
			datasourceProviderMap.put(dp.getName(), dp);
		}

		Environment env = applicationContext.getEnvironment();
		String enableString = env.getProperty("luck-report.banner.enable", "false");
		boolean enabled = Boolean.parseBoolean(enableString);
		if (enabled){
			new Splash().doPrint();
		}
	}
}
