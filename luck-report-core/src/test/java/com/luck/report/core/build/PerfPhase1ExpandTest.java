package com.luck.report.core.build;

import com.luck.report.core.definition.ReportDefinition;
import com.luck.report.core.export.ReportRender;
import com.luck.report.core.model.Cell;
import com.luck.report.core.model.Column;
import com.luck.report.core.model.Report;
import com.luck.report.core.model.Row;
import com.luck.report.core.parser.ReportParser;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

/**
 * 第一期性能优化验收：加载 perf-phase1-expand.ureport.xml 锁定插行/插列顺序、分组套明细与交叉表。
 */
class PerfPhase1ExpandTest {

	@Test
	void phase1Case_matchesCommentExpectations() throws Exception {
		Path xml = Paths.get("..", "..", "doc", "fixbug", "perf-phase1-expand.ureport.xml")
				.toAbsolutePath().normalize();
		ReportDefinition def;
		try (FileInputStream in = new FileInputStream(xml.toFile())) {
			def = new ReportParser().parse(in, xml.toString());
		}
		new ReportRender().rebuildReportDefinition(def);
		Report report = new ReportBuilder().buildReport(def, new HashMap<String, Object>());

		assertRowIndexConsistency(report);
		assertDownParentChildOrder(report);
		assertGroupDetail(report);
		assertRightParentChild(report);
		assertCrossTab(report);
	}

	private static void assertRowIndexConsistency(Report report) {
		List<Row> rows = report.getRows();
		for (int i = 1; i <= rows.size(); i++) {
			Row row = report.getRow(i);
			assertEquals(i, row.getRowNumber());
			assertSame(row, rows.get(i - 1));
		}
		List<Column> cols = report.getColumns();
		for (int i = 1; i <= cols.size(); i++) {
			Column col = report.getColumn(i);
			assertEquals(i, col.getColumnNumber());
			assertSame(col, cols.get(i - 1));
		}
	}

	/**
	 * R1 → 表头甲 → R2 → 表头甲 → R3 → 表头甲（不得连续两个表头）
	 */
	private static void assertDownParentChildOrder(Report report) {
		List<String> sequence = new ArrayList<String>();
		for (Row row : report.getRows()) {
			Cell a3 = findCell(row, "A3");
			if (a3 != null) {
				sequence.add(text(a3));
				continue;
			}
			Cell a4 = findCell(row, "A4");
			if (a4 != null) {
				sequence.add(text(a4));
			}
		}
		assertEquals(6, sequence.size());
		assertEquals("R1", sequence.get(0));
		assertEquals("表头甲", sequence.get(1));
		assertEquals("R2", sequence.get(2));
		assertEquals("表头甲", sequence.get(3));
		assertEquals("R3", sequence.get(4));
		assertEquals("表头甲", sequence.get(5));
	}

	/**
	 * sales→pen/book(30/70)；rd→pen(110) — 按行序扫描 A7/B7/C7
	 */
	private static void assertGroupDetail(Report report) {
		List<String> shape = new ArrayList<String>();
		for (Row row : report.getRows()) {
			for (Cell a7 : cellsNamed(row, "A7")) {
				if (!a7.isBlankCell() && a7.getData() != null && String.valueOf(a7.getData()).length() > 0) {
					shape.add("D:" + text(a7));
				}
			}
			for (Cell b7 : cellsNamed(row, "B7")) {
				if (!b7.isBlankCell() && b7.getData() != null && String.valueOf(b7.getData()).length() > 0) {
					Cell c7 = findSibling(row, "C7", b7);
					shape.add("P:" + text(b7) + "=" + number(c7));
				}
			}
		}
		assertEquals(
				java.util.Arrays.asList("D:sales", "P:pen=30.0", "P:book=70.0", "D:rd", "P:pen=110.0"),
				shape);
	}

	private static void assertRightParentChild(Report report) {
		List<String> tops = new ArrayList<String>();
		List<String> bottoms = new ArrayList<String>();
		for (Column col : report.getColumns()) {
			for (Cell a9 : cellsNamed(col, "A9")) {
				if (!a9.isBlankCell() && a9.getData() != null) {
					tops.add(text(a9));
				}
			}
			for (Cell a10 : cellsNamed(col, "A10")) {
				if (!a10.isBlankCell() && a10.getData() != null) {
					bottoms.add(text(a10));
				}
			}
		}
		assertEquals(java.util.Arrays.asList("R1", "R2", "R3"), tops);
		assertEquals(java.util.Arrays.asList("child", "child", "child"), bottoms);
	}

	private static void assertCrossTab(Report report) {
		Map<String, Double> amounts = new HashMap<String, Double>();
		for (Cell sum : report.getCellsMap().get("B13")) {
			if (sum.isBlankCell() || sum.getData() == null) {
				continue;
			}
			amounts.put(text(sum.getLeftParentCell()) + "|" + text(sum.getTopParentCell()),
					Double.valueOf(number(sum)));
		}
		assertEquals(60D, amounts.get("east|Q1").doubleValue(), 0.001D);
		assertEquals(80D, amounts.get("east|Q2").doubleValue(), 0.001D);
		assertEquals(30D, amounts.get("north|Q1").doubleValue(), 0.001D);
		assertEquals(40D, amounts.get("north|Q2").doubleValue(), 0.001D);
	}

	private static List<Cell> cellsNamed(Row row, String cellName) {
		List<Cell> list = new ArrayList<Cell>();
		for (Cell cell : row.getCells()) {
			if (cellName.equals(cell.getName())) {
				list.add(cell);
			}
		}
		return list;
	}

	private static List<Cell> cellsNamed(Column col, String cellName) {
		List<Cell> list = new ArrayList<Cell>();
		for (Cell cell : col.getCells()) {
			if (cellName.equals(cell.getName())) {
				list.add(cell);
			}
		}
		return list;
	}

	private static Cell findSibling(Row row, String cellName, Cell peer) {
		for (Cell cell : row.getCells()) {
			if (cellName.equals(cell.getName()) && cell.getLeftParentCell() == peer) {
				return cell;
			}
		}
		for (Cell cell : row.getCells()) {
			if (cellName.equals(cell.getName()) && !cell.isBlankCell()) {
				return cell;
			}
		}
		return null;
	}

	private static Cell findCell(Column col, String cellName) {
		for (Cell cell : col.getCells()) {
			if (cellName.equals(cell.getName())) {
				return cell;
			}
		}
		return null;
	}

	private static Cell findCell(Row row, String cellName) {
		for (Cell cell : row.getCells()) {
			if (cellName.equals(cell.getName())) {
				return cell;
			}
		}
		return null;
	}

	private static String text(Cell cell) {
		if (cell == null) {
			return null;
		}
		Object data = cell.getFormatData() != null ? cell.getFormatData() : cell.getData();
		return data == null ? "" : String.valueOf(data);
	}

	private static double number(Cell cell) {
		Object data = cell.getData();
		if (data instanceof Number) {
			return ((Number) data).doubleValue();
		}
		return data == null || String.valueOf(data).length() == 0 ? 0D : Double.parseDouble(String.valueOf(data));
	}
}
